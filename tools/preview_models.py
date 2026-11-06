#!/usr/bin/env python3
"""Renders generated block/item models to isometric PNG contact sheets for a quick
visual check without starting Minecraft.

    python tools/preview_models.py OUT.png model_ref [model_ref ...] [--scale 6] [--cols 6]

A model ref is e.g. coquettefurniture:item/baby_pink_canopy_bed or
coquettefurniture:block/sofa/mint/left. Blockstate y rotations can be previewed by
suffixing @90 / @180 / @270.
"""
import json
import math
import struct
import sys
import zlib
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from cfgen.core import ASSETS, MODID, Img  # noqa: E402

_png_cache = {}


def load_png(path):
    if path in _png_cache:
        return _png_cache[path]
    raw = Path(path).read_bytes()
    pos = 8
    w = h = 0
    idat = b""
    while pos < len(raw):
        ln = struct.unpack(">I", raw[pos:pos + 4])[0]
        tag = raw[pos + 4:pos + 8]
        body = raw[pos + 8:pos + 8 + ln]
        if tag == b"IHDR":
            w, h = struct.unpack(">II", body[:8])
        elif tag == b"IDAT":
            idat += body
        pos += 12 + ln
    data = zlib.decompress(idat)
    stride = w * 4
    px = bytearray(h * stride)
    prev = bytearray(stride)
    i = 0
    for y in range(h):
        f = data[i]
        line = bytearray(data[i + 1:i + 1 + stride])
        i += 1 + stride
        for x in range(stride):
            a = line[x - 4] if x >= 4 else 0
            b = prev[x]
            c = prev[x - 4] if x >= 4 else 0
            if f == 1:
                line[x] = (line[x] + a) & 255
            elif f == 2:
                line[x] = (line[x] + b) & 255
            elif f == 3:
                line[x] = (line[x] + (a + b) // 2) & 255
            elif f == 4:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                pr = a if pa <= pb and pa <= pc else (b if pb <= pc else c)
                line[x] = (line[x] + pr) & 255
        px[y * stride:(y + 1) * stride] = line
        prev = line
    _png_cache[path] = (w, h, px)
    return _png_cache[path]


def load_model(ref):
    ns, path = ref.split(":")
    f = ASSETS / "models" / (path + ".json")
    return json.loads(f.read_text(encoding="utf-8"))


def flatten(ref):
    chain = []
    cur = ref
    while cur and cur.startswith(MODID + ":"):
        m = load_model(cur)
        chain.append(m)
        cur = m.get("parent")
    tex = {}
    for m in reversed(chain):
        tex.update(m.get("textures", {}))
    elements = next((m["elements"] for m in chain if "elements" in m), [])
    return tex, elements


def rot_point(p, axis, angle, origin):
    a = math.radians(angle)
    x, y, z = (p[i] - origin[i] for i in range(3))
    c, s = math.cos(a), math.sin(a)
    if axis == "y":
        x, z = x * c + z * s, -x * s + z * c
    elif axis == "x":
        y, z = y * c - z * s, y * s + z * c
    else:
        x, y = x * c - y * s, x * s + y * c
    return (x + origin[0], y + origin[1], z + origin[2])


def face_quad(fr, to, d):
    x0, y0, z0 = fr
    x1, y1, z1 = to
    return {
        "north": [(x1, y1, z0), (x0, y1, z0), (x0, y0, z0), (x1, y0, z0)],
        "south": [(x0, y1, z1), (x1, y1, z1), (x1, y0, z1), (x0, y0, z1)],
        "west": [(x0, y1, z0), (x0, y1, z1), (x0, y0, z1), (x0, y0, z0)],
        "east": [(x1, y1, z1), (x1, y1, z0), (x1, y0, z0), (x1, y0, z1)],
        "up": [(x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1)],
        "down": [(x0, y0, z1), (x1, y0, z1), (x1, y0, z0), (x0, y0, z0)],
    }[d]


VIEW = (1.0, -0.8, 1.0)
_vl = math.sqrt(sum(v * v for v in VIEW))
V = tuple(v / _vl for v in VIEW)
R = (-1 / math.sqrt(2), 0.0, 1 / math.sqrt(2))
U = (R[1] * V[2] - R[2] * V[1], R[2] * V[0] - R[0] * V[2], R[0] * V[1] - R[1] * V[0])
_ul = math.sqrt(sum(u * u for u in U))
U = tuple(u / _ul for u in U)
if U[1] < 0:
    U = tuple(-u for u in U)


def yrot_model(p, deg):
    """Blockstate-style rotation (clockwise from above) around the block centre."""
    for _ in range((deg // 90) % 4):
        x, y, z = p
        p = (16 - z, y, x)
    return p


def render(ref, scale=6, yrot=0, size=None):
    tex, elements = flatten(ref)
    faces = []
    for el in elements:
        fr, to = el["from"], el["to"]
        rot = el.get("rotation")
        for d, f in el["faces"].items():
            quad = face_quad(fr, to, d)
            if rot:
                quad = [rot_point(p, rot["axis"], rot["angle"], rot["origin"]) for p in quad]
            quad = [yrot_model(p, yrot) for p in quad]
            var = f["texture"].lstrip("#")
            while var in tex and tex[var].startswith("#"):
                var = tex[var].lstrip("#")
            tref = tex.get(var)
            if not tref:
                continue
            ns, tpath = tref.split(":")
            faces.append((quad, f["uv"], ASSETS / "textures" / (tpath + ".png"), el.get("shade", True)))
    pts = [p for q, *_ in faces for p in q]
    if not pts:
        return Img(8, 8)
    proj = lambda p: (sum((p[i] - 8) * R[i] for i in range(3)), sum((p[i] - 8) * U[i] for i in range(3)),
                      sum((p[i] - 8) * V[i] for i in range(3)))
    xs = [proj(p)[0] for p in pts]
    ys = [proj(p)[1] for p in pts]
    w = int((max(xs) - min(xs)) * scale) + 8
    h = int((max(ys) - min(ys)) * scale) + 8
    if size:
        w, h = size
    ox = w / 2 - (max(xs) + min(xs)) / 2 * scale
    oy = h / 2 + (max(ys) + min(ys)) / 2 * scale
    img = Img(w, h)
    zbuf = [1e9] * (w * h)
    for quad, uv, tpath, shade in faces:
        p = [proj(q) for q in quad]
        e1 = [quad[1][i] - quad[0][i] for i in range(3)]
        e2 = [quad[3][i] - quad[0][i] for i in range(3)]
        n = (e1[1] * e2[2] - e1[2] * e2[1], e1[2] * e2[0] - e1[0] * e2[2], e1[0] * e2[1] - e1[1] * e2[0])
        nl = math.sqrt(sum(c * c for c in n)) or 1
        n = tuple(-c / nl for c in n)
        if sum(n[i] * V[i] for i in range(3)) > 0.02:
            continue
        bright = 1.0
        if shade:
            bright = 0.5 + 0.5 * max(0.0, n[1]) + 0.3 * abs(n[2]) + 0.1 * abs(n[0]) - 0.5 * max(0.0, -n[1]) * 0.0
            bright = min(1.0, max(0.5, 0.6 + 0.4 * n[1] + 0.2 * abs(n[2])))
        tw, th, tpx = load_png(tpath)
        u0, v0, u1, v1 = uv
        corners_uv = [(u0, v0), (u1, v0), (u1, v1), (u0, v1)]
        sp = [(ox + a[0] * scale, oy - a[1] * scale, a[2]) for a in p]
        for tri in ((0, 1, 2), (0, 2, 3)):
            A, Bp, Cp = (sp[k] for k in tri)
            TA, TB, TC = (corners_uv[k] for k in tri)
            minx = max(0, int(min(A[0], Bp[0], Cp[0])))
            maxx = min(w - 1, int(max(A[0], Bp[0], Cp[0])) + 1)
            miny = max(0, int(min(A[1], Bp[1], Cp[1])))
            maxy = min(h - 1, int(max(A[1], Bp[1], Cp[1])) + 1)
            den = (Bp[1] - Cp[1]) * (A[0] - Cp[0]) + (Cp[0] - Bp[0]) * (A[1] - Cp[1])
            if abs(den) < 1e-9:
                continue
            for y in range(miny, maxy + 1):
                for x in range(minx, maxx + 1):
                    px, py = x + 0.5, y + 0.5
                    l1 = ((Bp[1] - Cp[1]) * (px - Cp[0]) + (Cp[0] - Bp[0]) * (py - Cp[1])) / den
                    l2 = ((Cp[1] - A[1]) * (px - Cp[0]) + (A[0] - Cp[0]) * (py - Cp[1])) / den
                    l3 = 1 - l1 - l2
                    if l1 < -1e-6 or l2 < -1e-6 or l3 < -1e-6:
                        continue
                    z = l1 * A[2] + l2 * Bp[2] + l3 * Cp[2]
                    idx = y * w + x
                    if z >= zbuf[idx]:
                        continue
                    u = l1 * TA[0] + l2 * TB[0] + l3 * TC[0]
                    v = l1 * TA[1] + l2 * TB[1] + l3 * TC[1]
                    tx = min(tw - 1, max(0, int(u / 16 * tw)))
                    ty = min(th - 1, max(0, int(v / 16 * th)))
                    k = (ty * tw + tx) * 4
                    if tpx[k + 3] < 26:
                        continue
                    zbuf[idx] = z
                    img.set(x, y, (int(tpx[k] * bright), int(tpx[k + 1] * bright), int(tpx[k + 2] * bright), 255))
    return img


def sheet(out, refs, scale=6, cols=6):
    tiles = []
    for r in refs:
        yrot = 0
        if "@" in r:
            r, deg = r.split("@")
            yrot = int(deg)
        tiles.append(render(r, scale, yrot))
    cw = max(t.w for t in tiles) + 6
    ch = max(t.h for t in tiles) + 6
    rows = (len(tiles) + cols - 1) // cols
    big = Img(cw * min(cols, len(tiles)), ch * rows, (236, 232, 240, 255))
    for i, t in enumerate(tiles):
        bx = (i % cols) * cw + (cw - t.w) // 2
        by = (i // cols) * ch + (ch - t.h) // 2
        for y in range(t.h):
            for x in range(t.w):
                c = t.get(x, y)
                if c[3]:
                    big.set(bx + x, by + y, c)
    big.save(out)


if __name__ == "__main__":
    args = sys.argv[1:]
    scale, cols = 6, 6
    if "--scale" in args:
        i = args.index("--scale")
        scale = float(args[i + 1])
        del args[i:i + 2]
    if "--cols" in args:
        i = args.index("--cols")
        cols = int(args[i + 1])
        del args[i:i + 2]
    sheet(args[0], args[1:], scale, cols)
