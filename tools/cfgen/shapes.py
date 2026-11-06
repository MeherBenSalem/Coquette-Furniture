"""Reusable decorative motifs: bows, hearts, knobs, scallops, books, pots."""
from .core import B, box, rbox, rot_y90, move

HEART_ROWS = [(0, 1, 4, 6), (1, 2, 3, 7), (2, 3, 2, 8), (3, 5, 1, 9), (5, 7, 0, 10),
              (7, 8, 0.5, 4.5), (7, 8, 5.5, 9.5), (8, 9, 1.5, 3.5), (8, 9, 6.5, 8.5)]


def heart(cx, y, z0, z1, size, tex, picture=False, **faces):
    """An upright heart (10 x 9 units of `size`) in the x/y plane, standing on y.
    With picture=True the whole texture is spread across the heart's north face."""
    out = []
    for ya, yb, xa, xb in HEART_ROWS:
        if picture:
            uv = ((10 - xb) * 1.6, (9 - yb) * 16 / 9, (10 - xa) * 1.6, (9 - ya) * 16 / 9)
            faces = dict(faces, n=(tex, uv))
        out += B(cx + (xa - 5) * size, y + ya * size, z0, cx + (xb - 5) * size, y + yb * size, z1, tex, **faces)
    return out


def heart_flat(cx, cz, y0, y1, size, tex, **faces):
    """A heart lying flat, tip pointing north (to the front)."""
    out = []
    for ya, yb, xa, xb in HEART_ROWS:
        out += B(cx + (xa - 5) * size, y0, cz + (ya - 4.5) * size, cx + (xb - 5) * size, y1, cz + (yb - 4.5) * size,
                 tex, **faces)
    return out


def bow(cx, y, z0, z1, s=1.0, tex="bow"):
    """A satin bow in the x/y plane (6.5s wide, 3s tall), tails hanging down to y."""
    out = []
    out += B(cx - 3 * s, y + 1.25 * s, z0, cx - 0.5 * s, y + 3 * s, z1, tex)
    out += B(cx - 3.4 * s, y + 1.6 * s, z0 + 0.05, cx - 3 * s, y + 2.65 * s, z1 - 0.05, tex)
    out += B(cx + 0.5 * s, y + 1.25 * s, z0, cx + 3 * s, y + 3 * s, z1, tex)
    out += B(cx + 3 * s, y + 1.6 * s, z0 + 0.05, cx + 3.4 * s, y + 2.65 * s, z1 - 0.05, tex)
    out += B(cx - 1.6 * s, y, z0 + 0.05, cx - 0.5 * s, y + 1.5 * s, z1 - 0.05, tex)
    out += B(cx + 0.5 * s, y, z0 + 0.05, cx + 1.6 * s, y + 1.5 * s, z1 - 0.05, tex)
    out += B(cx - 0.6 * s, y + 1.4 * s, z0 - 0.15, cx + 0.6 * s, y + 2.85 * s, z1 + 0.15, tex)
    return out


def side_bow(x0, x1, y, cz, s=1.0, tex="bow"):
    """A bow in the z/y plane (on the side of something), centred on z = cz."""
    els = bow(8, y, 7.5, 8.5, s, tex)
    els = rot_y90(els, 1)
    # after rotation the bow sits in the plane x ~ 8, centred on z = 8
    mid = (x0 + x1) / 2
    return move(els, mid - 8, 0, cz - 8)


def knob(x, y, z, size=1.0, depth=0.75, tex="gold"):
    """A little cabinet knob on a north-facing surface at z."""
    h = size / 2
    return B(x - h, y - h, z - depth, x + h, y + h, z, tex)


def scallops(x0, x1, ytop, z0, z1, n, tex):
    """A scalloped edge hanging below ytop."""
    out = []
    w = (x1 - x0) / n
    for i in range(n):
        xa = x0 + i * w
        out += B(xa + 0.12 * w, ytop - 0.75, z0, xa + 0.88 * w, ytop, z1, tex)
        out += B(xa + 0.3 * w, ytop - 1.25, z0, xa + 0.7 * w, ytop - 0.75, z1, tex)
    return out


def book(x0, y0, z0, x1, y1, z1, cover, spine="west", rot=None):
    faces = {"up": cover, "down": cover, spine: cover}
    return box((x0, y0, z0), (x1, y1, z1), "pages", rot=rot, **faces)


def standing_book(x, w, h, y, z0, z1, cover, band=True):
    """A book standing on a shelf with its spine to the front."""
    out = box((x, y, z0), (x + w, y + h, z1), cover, up="pages")
    if band and h > 3:
        out += B(x, y + h - 1.5, z0 - 0.05, x + w, y + h - 1, z0, "gold", s=None, e=None, w=None, u=None, d=None)
    return out


def ring(x0, z0, x1, z1, y0, y1, t, tex):
    """A square ring (rim) of thickness t."""
    out = []
    out += B(x0, y0, z0, x1, y1, z0 + t, tex)
    out += B(x0, y0, z1 - t, x1, y1, z1, tex)
    out += B(x0, y0, z0 + t, x0 + t, y1, z1 - t, tex)
    out += B(x1 - t, y0, z0 + t, x1, y1, z1 - t, tex)
    return out


def gold_foot(x, z, h=0.75, s=1.5):
    return B(x, 0, z, x + s, h, z + s, "gold")


def cross_plane(x0, y0, x1, y1, tex, uv="fit", angle=45, cx=8, cz=8, shade=False):
    """Two planes crossed at +/- angle (like vanilla plants)."""
    out = []
    for a in (angle, -angle):
        out += box((x0, y0, cz), (x1, y1, cz), (tex, uv), rot=("y", a, (cx, (y0 + y1) / 2, cz)), shade=shade)
    return out


def leaf_plane(x0, y0, x1, y1, z, tex, rot, uv="fit"):
    return box((x0, y0, z), (x1, y1, z), (tex, uv), rot=rot, shade=False)


def pot(tex, rim_tex=None, band=True):
    """A large floor pot (top of soil at 7.5)."""
    rim_tex = rim_tex or tex
    out = []
    out += B(5, 0, 5, 11, 1, 11, tex)
    out += B(4, 1, 4, 12, 7, 12, tex, u=None)
    out += ring(3.5, 3.5, 12.5, 12.5, 7, 8, 1, rim_tex)
    out += B(4.5, 7, 4.5, 11.5, 7.5, 11.5, "soil", n=None, s=None, e=None, w=None, d=None)
    if band:
        out += B(3.9, 5, 3.9, 12.1, 5.5, 12.1, "gold", u=None, d=None)
    return out


def rounded(x0, y0, z0, x1, y1, z1, tex, r=0.5, **faces):
    return rbox(x0, y0, z0, x1, y1, z1, tex, r=r, **faces)
