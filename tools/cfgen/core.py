"""Core helpers for the Coquette Furniture asset generator: PNG writing, colours,
the cuboid model DSL (with automatic UVs, cullfaces, splitting) and JSON output."""
import colorsys
import json
import math
import struct
import zlib
from pathlib import Path

MODID = "coquettefurniture"
ROOT = Path(__file__).resolve().parents[2] / "src" / "main" / "resources"
ASSETS = ROOT / "assets" / MODID
DATA = ROOT / "data" / MODID
MC_DATA = ROOT / "data" / "minecraft"
M = MODID + ":"

DIRS = ("north", "south", "east", "west", "up", "down")
SHORT = {"n": "north", "s": "south", "e": "east", "w": "west", "u": "up", "d": "down"}


# --------------------------------------------------------------------------- output

def write_json(path, obj):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


# --------------------------------------------------------------------------- colours

def C(value, alpha=255):
    """Accepts '#RRGGBB', (r,g,b) or (r,g,b,a) and returns an RGBA tuple."""
    if isinstance(value, str):
        v = value.lstrip("#")
        return (int(v[0:2], 16), int(v[2:4], 16), int(v[4:6], 16), alpha)
    if len(value) == 3:
        return (value[0], value[1], value[2], alpha)
    return tuple(value)


def clamp8(v):
    return max(0, min(255, int(round(v))))


def mix(a, b, t):
    a, b = C(a), C(b)
    return tuple(clamp8(a[i] + (b[i] - a[i]) * t) for i in range(4))


def lighten(c, t):
    c = C(c)
    return mix(c, (255, 255, 255, c[3]), t)


def darken(c, t):
    c = C(c)
    return mix(c, (0, 0, 0, c[3]), t)


def scale(c, f):
    c = C(c)
    return (clamp8(c[0] * f), clamp8(c[1] * f), clamp8(c[2] * f), c[3])


def hue_shift(c, dh, ds=0.0, dv=0.0):
    c = C(c)
    h, s, v = colorsys.rgb_to_hsv(c[0] / 255, c[1] / 255, c[2] / 255)
    r, g, b = colorsys.hsv_to_rgb((h + dh) % 1.0, max(0, min(1, s + ds)), max(0, min(1, v + dv)))
    return (clamp8(r * 255), clamp8(g * 255), clamp8(b * 255), c[3])


def with_alpha(c, a):
    c = C(c)
    return (c[0], c[1], c[2], a)


# --------------------------------------------------------------------------- images

class Img:
    def __init__(self, w, h, fill=(0, 0, 0, 0)):
        self.w, self.h = w, h
        self.data = bytearray(bytes(C(fill)) * (w * h))

    def get(self, x, y):
        i = (int(y) * self.w + int(x)) * 4
        return tuple(self.data[i:i + 4])

    def set(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            i = (int(y) * self.w + int(x)) * 4
            self.data[i:i + 4] = bytes(C(c))

    def blend(self, x, y, c, alpha=None):
        if not (0 <= x < self.w and 0 <= y < self.h):
            return
        c = C(c)
        a = (c[3] / 255) if alpha is None else alpha
        under = self.get(x, y)
        if under[3] == 0:
            self.set(x, y, (c[0], c[1], c[2], clamp8(255 * a)))
            return
        out = tuple(clamp8(under[i] + (c[i] - under[i]) * a) for i in range(3)) + (max(under[3], clamp8(255 * a)),)
        self.set(x, y, out)

    def rect(self, x0, y0, x1, y1, c):
        for y in range(max(0, y0), min(self.h, y1)):
            for x in range(max(0, x0), min(self.w, x1)):
                self.set(x, y, c)

    def ellipse(self, cx, cy, rx, ry, c, alpha=None):
        for y in range(int(math.floor(cy - ry)), int(math.ceil(cy + ry)) + 1):
            for x in range(int(math.floor(cx - rx)), int(math.ceil(cx + rx)) + 1):
                dx = (x + 0.5 - cx) / max(rx, 0.01)
                dy = (y + 0.5 - cy) / max(ry, 0.01)
                if dx * dx + dy * dy <= 1.0:
                    if alpha is None:
                        self.set(x, y, c)
                    else:
                        self.blend(x, y, c, alpha)

    def stamp(self, x0, y0, rows, palette):
        for dy, row in enumerate(rows):
            for dx, ch in enumerate(row):
                if ch in palette and palette[ch] is not None:
                    col = C(palette[ch])
                    if col[3] < 255:
                        self.blend(x0 + dx, y0 + dy, col)
                    else:
                        self.set(x0 + dx, y0 + dy, col)

    def map(self, fn):
        for y in range(self.h):
            for x in range(self.w):
                self.set(x, y, fn(x, y, self.get(x, y)))

    def save(self, path):
        path = Path(path)
        path.parent.mkdir(parents=True, exist_ok=True)
        raw = bytearray()
        stride = self.w * 4
        for y in range(self.h):
            raw.append(0)
            raw += self.data[y * stride:(y + 1) * stride]

        def chunk(tag, payload):
            return struct.pack(">I", len(payload)) + tag + payload + struct.pack(">I", zlib.crc32(tag + payload) & 0xFFFFFFFF)

        png = b"\x89PNG\r\n\x1a\n"
        png += chunk(b"IHDR", struct.pack(">IIBBBBB", self.w, self.h, 8, 6, 0, 0, 0))
        png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
        png += chunk(b"IEND", b"")
        path.write_bytes(png)


# --------------------------------------------------------------------------- model DSL
#
# Models face north: the front of a piece of furniture is at z = 0 and its back
# (the wall side) at z = 16. Blockstates rotate it (east 90, south 180, west 270).
#
# A face is either None (hidden), a texture variable name ("paint") which gets a
# position-based UV wrapped into 0..16 (so wood grain and fabric run continuously
# across neighbouring cuboids), or a tuple (texture, mode[, rotation]) where mode is
#   "fit"    - the whole texture stretched over the face
#   "size"   - a w x h window from the texture's top-left corner
#   "top"    - position-based u, v anchored to the top of the texture
#   "bottom" - position-based u, v anchored to the bottom of the texture
#   (u0, v0, u1, v1) - an explicit rectangle.

class El:
    def __init__(self, frm, to, faces, rot=None, shade=True):
        self.frm = [float(v) for v in frm]
        self.to = [float(v) for v in to]
        self.faces = dict(faces)
        self.rot = rot      # (axis, angle, (ox, oy, oz))
        self.shade = shade

    def copy(self):
        return El(self.frm, self.to, dict(self.faces), self.rot, self.shade)

    def size(self):
        return [self.to[i] - self.frm[i] for i in range(3)]

    def face_dims(self, d):
        dx, dy, dz = self.size()
        if d in ("north", "south"):
            return dx, dy
        if d in ("east", "west"):
            return dz, dy
        return dx, dz


def box(frm, to, tex, rot=None, shade=True, **faces):
    """One cuboid. Keyword faces (n/s/e/w/u/d or full names) override `tex`; None hides a face."""
    spec = {d: tex for d in DIRS}
    for k, v in faces.items():
        spec[SHORT.get(k, k)] = v
    e = El(frm, to, spec, rot, shade)
    # flat planes only show their two large faces
    sx, sy, sz = e.size()
    if sx == 0:
        for d in ("north", "south", "up", "down"):
            e.faces[d] = None
    if sy == 0:
        for d in ("north", "south", "east", "west"):
            e.faces[d] = None
    if sz == 0:
        for d in ("east", "west", "up", "down"):
            e.faces[d] = None
    return [e]


def B(x0, y0, z0, x1, y1, z1, tex, **kw):
    """Shorthand for box((x0, y0, z0), (x1, y1, z1), tex)."""
    return box((x0, y0, z0), (x1, y1, z1), tex, **kw)


def rbox(x0, y0, z0, x1, y1, z1, tex, r=0.5, rot=None, **faces):
    """A soft, rounded cuboid built from three overlapping boxes."""
    r = min(r, max(0.125, min(x1 - x0, y1 - y0, z1 - z0) / 2 - 0.125))
    out = []
    out += box((x0 + r, y0 + r, z0), (x1 - r, y1 - r, z1), tex, rot=rot, **faces)
    out += box((x0, y0 + r, z0 + r), (x1, y1 - r, z1 - r), tex, rot=rot, **faces)
    out += box((x0 + r, y0, z0 + r), (x1 - r, y1, z1 - r), tex, rot=rot, **faces)
    return out


def cyl(cx, cz, r, y0, y1, tex, steps=2, **faces):
    """A rounded column (octagon-ish) made of `steps` crossed boxes."""
    out = []
    if steps <= 1:
        return box((cx - r, y0, cz - r), (cx + r, y1, cz + r), tex, **faces)
    inset = r * 0.42
    out += box((cx - r, y0, cz - r + inset), (cx + r, y1, cz + r - inset), tex, **faces)
    out += box((cx - r + inset, y0, cz - r), (cx + r - inset, y1, cz + r), tex, **faces)
    return out


def mirror_x(els):
    out = []
    for e in els:
        m = e.copy()
        m.frm = [16 - e.to[0], e.frm[1], e.frm[2]]
        m.to = [16 - e.frm[0], e.to[1], e.to[2]]
        m.faces["east"], m.faces["west"] = e.faces["west"], e.faces["east"]
        if e.rot:
            axis, angle, origin = e.rot
            m.rot = (axis, angle if axis == "x" else -angle, (16 - origin[0], origin[1], origin[2]))
        out.append(m)
    return out


def sym(els):
    """Elements plus their mirror image across x = 8."""
    els = list(els)
    return els + mirror_x(els)


def mirror_z(els):
    out = []
    for e in els:
        m = e.copy()
        m.frm = [e.frm[0], e.frm[1], 16 - e.to[2]]
        m.to = [e.to[0], e.to[1], 16 - e.frm[2]]
        m.faces["north"], m.faces["south"] = e.faces["south"], e.faces["north"]
        if e.rot:
            axis, angle, origin = e.rot
            m.rot = (axis, angle if axis == "z" else -angle, (origin[0], origin[1], 16 - origin[2]))
        out.append(m)
    return out


def move(els, dx=0.0, dy=0.0, dz=0.0):
    out = []
    for e in els:
        m = e.copy()
        m.frm = [e.frm[0] + dx, e.frm[1] + dy, e.frm[2] + dz]
        m.to = [e.to[0] + dx, e.to[1] + dy, e.to[2] + dz]
        if e.rot:
            axis, angle, o = e.rot
            m.rot = (axis, angle, (o[0] + dx, o[1] + dy, o[2] + dz))
        out.append(m)
    return out


def rot_y90(els, turns=1):
    """Rotate elements clockwise (seen from above) around the block centre in 90 degree steps."""
    out = list(els)
    for _ in range(turns % 4):
        nxt = []
        for e in out:
            m = e.copy()
            # (x, z) -> (16 - z, x): north -> east
            x0, z0 = 16 - e.to[2], e.frm[0]
            x1, z1 = 16 - e.frm[2], e.to[0]
            m.frm = [x0, e.frm[1], z0]
            m.to = [x1, e.to[1], z1]
            f = e.faces
            m.faces = dict(f)
            m.faces["east"], m.faces["south"], m.faces["west"], m.faces["north"] = f["north"], f["east"], f["south"], f["west"]
            if e.rot:
                axis, angle, o = e.rot
                no = (16 - o[2], o[1], o[0])
                if axis == "y":
                    m.rot = (axis, angle, no)
                elif axis == "x":
                    m.rot = ("z", -angle, no)
                else:
                    m.rot = ("x", angle, no)
            nxt.append(m)
        out = nxt
    return out


def retex(els, mapping):
    """Swap texture variables (e.g. {'paint': 'trim'})."""
    out = []
    for e in els:
        m = e.copy()
        for d, f in m.faces.items():
            if f is None:
                continue
            if isinstance(f, str):
                m.faces[d] = mapping.get(f, f)
            else:
                m.faces[d] = (mapping.get(f[0], f[0]),) + tuple(f[1:])
        out.append(m)
    return out


def hide(els, *dirs):
    out = []
    for e in els:
        m = e.copy()
        for d in dirs:
            m.faces[SHORT.get(d, d)] = None
        out.append(m)
    return out


def split(els, axis, at):
    """Cut elements at a plane (axis index 0/1/2, coordinate `at`).
    Returns (below, above); `above` is translated so the plane becomes 0.
    Rotated elements go wholly to the side holding their centre."""
    lo, hi = [], []
    names = {0: ("west", "east"), 1: ("down", "up"), 2: ("north", "south")}
    neg, pos = names[axis]
    for e in els:
        if e.to[axis] <= at:
            lo.append(e.copy())
            continue
        if e.frm[axis] >= at:
            hi.append(e.copy())
            continue
        if e.rot:
            c = (e.frm[axis] + e.to[axis]) / 2
            (lo if c < at else hi).append(e.copy())
            continue
        a = e.copy()
        a.to[axis] = at
        a.faces[pos] = None
        b = e.copy()
        b.frm[axis] = at
        b.faces[neg] = None
        lo.append(a)
        hi.append(b)
    shift = [0.0, 0.0, 0.0]
    shift[axis] = -at
    return lo, move(hi, *shift)


# --------------------------------------------------------------------------- serialisation

def _auto_uv(e, d):
    x0, y0, z0 = e.frm
    x1, y1, z1 = e.to
    if d == "north":
        return [16 - x1, 16 - y1, 16 - x0, 16 - y0]
    if d == "south":
        return [x0, 16 - y1, x1, 16 - y0]
    if d == "west":
        return [z0, 16 - y1, z1, 16 - y0]
    if d == "east":
        return [16 - z1, 16 - y1, 16 - z0, 16 - y0]
    if d == "up":
        return [x0, z0, x1, z1]
    return [x0, 16 - z1, x1, 16 - z0]


def _wrap(a, b):
    """Shift the span [a, b] into 0..16 (keeping its length, up to 16)."""
    span = min(16.0, b - a)
    k = math.floor(a / 16.0)
    a2 = a - 16 * k
    if a2 + span > 16.0001:
        a2 = 16 - span
    return a2, a2 + span


def face_uv(e, d, mode):
    w, h = e.face_dims(d)
    if mode == "fit":
        return [0, 0, 16, 16]
    if mode == "size":
        return [0, 0, min(16, w), min(16, h)]
    if isinstance(mode, (list, tuple)):
        return list(mode)
    u0, v0, u1, v1 = _auto_uv(e, d)
    u0, u1 = _wrap(u0, u1)
    if mode == "top":
        v0, v1 = 0, min(16, h)
    elif mode == "bottom":
        v0, v1 = 16 - min(16, h), 16
    else:
        v0, v1 = _wrap(v0, v1)
    return [u0, v0, u1, v1]


CULL_PLANES = {"north": (2, 0.0), "south": (2, 16.0), "west": (0, 0.0), "east": (0, 16.0), "down": (1, 0.0), "up": (1, 16.0)}


def _cullface(e, d):
    if e.rot:
        return None
    axis, plane = CULL_PLANES[d]
    coord = e.frm[axis] if plane == 0.0 else e.to[axis]
    if abs(coord - plane) > 1e-6:
        return None
    for i in range(3):
        if i != axis and (e.frm[i] < -1e-6 or e.to[i] > 16 + 1e-6):
            return None
    return d


def r4(v):
    v = round(v, 4)
    return int(v) if v == int(v) else v


def serialise(els):
    out = []
    for e in els:
        faces = {}
        for d in DIRS:
            f = e.faces.get(d)
            if f is None:
                continue
            w, h = e.face_dims(d)
            if w <= 0 or h <= 0:
                continue
            if isinstance(f, str):
                tex, mode, rot = f, "auto", 0
            else:
                tex, mode = f[0], f[1]
                rot = f[2] if len(f) > 2 else 0
            face = {"uv": [r4(v) for v in face_uv(e, d, mode)], "texture": "#" + tex}
            if rot:
                face["rotation"] = rot
            cf = _cullface(e, d)
            if cf:
                face["cullface"] = cf
            faces[d] = face
        if not faces:
            continue
        el = {"from": [r4(v) for v in e.frm], "to": [r4(v) for v in e.to]}
        if e.rot:
            axis, angle, origin = e.rot
            el["rotation"] = {"angle": angle, "axis": axis, "origin": [r4(v) for v in origin]}
        if not e.shade:
            el["shade"] = False
        el["faces"] = faces
        out.append(el)
    return out


def bounds(els):
    lo = [99.0, 99.0, 99.0]
    hi = [-99.0, -99.0, -99.0]
    for e in els:
        for i in range(3):
            lo[i] = min(lo[i], e.frm[i], e.to[i])
            hi[i] = max(hi[i], e.frm[i], e.to[i])
    return lo, hi


def check_range(name, els):
    for e in els:
        for v in e.frm + e.to:
            if v < -16 or v > 32:
                raise ValueError(f"{name}: element out of range {e.frm} {e.to}")
        if e.rot and e.rot[1] not in (-45, -22.5, 0, 22.5, 45):
            raise ValueError(f"{name}: bad rotation {e.rot}")


def tex_vars(els):
    used = set()
    for e in els:
        for f in e.faces.values():
            if f is None:
                continue
            used.add(f if isinstance(f, str) else f[0])
    return used


def centred_for_item(els):
    """Shift a (possibly multi-block) model so its footprint is centred on the block."""
    lo, hi = bounds(els)
    dx = 8 - (lo[0] + hi[0]) / 2
    dz = 8 - (lo[2] + hi[2]) / 2
    dy = 0.0
    if hi[1] - lo[1] > 16:
        dy = 8 - (lo[1] + hi[1]) / 2
    return move(els, round(dx * 4) / 4, round(dy * 4) / 4, round(dz * 4) / 4)


def display_for(els):
    lo, hi = bounds(els)
    size = max(hi[0] - lo[0], hi[1] - lo[1], hi[2] - lo[2])
    s = min(0.625, 0.625 * 16.0 / max(size, 1.0) * 1.12)
    k = s / 0.625
    small = size < 9
    gui_s = min(0.9, s * 1.45) if small else s
    cy = (lo[1] + hi[1]) / 2
    gui_ty = round((8 - cy) * gui_s, 2) if small else 0

    def r(v):
        return round(v, 3)

    return {
        "gui": {"rotation": [30, 225, 0], "translation": [0, gui_ty, 0], "scale": [r(gui_s)] * 3},
        "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [r(0.25 * k * (1.6 if small else 1))] * 3},
        "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [r(0.5 * k * (1.5 if small else 1))] * 3},
        "head": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [r(0.6 * k)] * 3},
        "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [r(0.375 * k * (1.4 if small else 1))] * 3},
        "thirdperson_lefthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [r(0.375 * k * (1.4 if small else 1))] * 3},
        "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [r(0.4 * k * (1.4 if small else 1))] * 3},
        "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 0, 0], "scale": [r(0.4 * k * (1.4 if small else 1))] * 3},
    }
