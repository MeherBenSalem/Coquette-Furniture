"""Geometry for every colourable piece of furniture. Models face north (front at
z = 0, wall/back at z = 16). Multi-block pieces are designed whole (beds and tubs
along z 0..32 with the head at z = 0, tall pieces along y 0..32) and split later."""
from .core import B, box, hide, mirror_x, mirror_z, move, rbox, rot_y90, sym
from .shapes import (book, bow, cross_plane, heart, heart_flat, knob, ring, scallops,
                     side_bow, standing_book)

LACE_HEM = ("lace", "bottom")


def legs4(leg):
    """Copy a north-west leg to all four corners."""
    return sym(leg + mirror_z(leg))


# =========================================================================== bedroom

def canopy_bed():
    e = []
    # gold bun feet and the bed frame
    for x in (1, 13.5):
        for z in (1.5, 29):
            e += B(x, 0, z, x + 1.5, 2.5, z + 1.5, "gold")
    e += B(0.75, 2.5, 1.5, 15.25, 5, 30.5, "paint")
    e += B(0.5, 5, 1.25, 15.5, 5.75, 30.75, "trim")
    # lace bed skirt
    e += B(0.4, 2.25, 2.5, 0.5, 5.25, 30, LACE_HEM, n=None, s=None, u=None, d=None, e=None)
    e += B(15.5, 2.25, 2.5, 15.6, 5.25, 30, LACE_HEM, n=None, s=None, u=None, d=None, w=None)
    # mattress, duvet with a turned-down sheet and lace hem
    e += B(1, 5.75, 2, 15, 9, 30, "sheet")
    e += B(0.5, 6.25, 11.5, 15.5, 9.75, 29.75, "print", d=None)
    e += B(0.4, 9.75, 11.25, 15.6, 10.4, 13.75, "sheet")
    e += B(0.35, 5.5, 11.5, 0.5, 6.5, 29.75, LACE_HEM, n=None, s=None, u=None, d=None, e=None)
    e += B(15.5, 5.5, 11.5, 15.65, 6.5, 29.75, LACE_HEM, n=None, s=None, u=None, d=None, w=None)
    # pillows and a heart cushion
    e += rbox(2, 9, 2.5, 7.75, 11.75, 7, "sheet", r=0.75)
    e += rbox(8.25, 9, 2.5, 14, 11.75, 7, "sheet", r=0.75)
    e += heart(8, 9, 7.25, 8.75, 0.62, "fabric")
    # headboard: tufted, arched, piped
    e += B(1.75, 4.5, 0.25, 14.25, 19.5, 0.75, "trim")
    e += B(2, 5, 0.5, 14, 19, 1.75, "paint", s="quilt")
    e += B(4, 19, 0.5, 12, 20.5, 1.75, "paint", s="quilt")
    e += B(6, 20.5, 0.5, 10, 21.5, 1.75, "paint", s="quilt")
    e += B(2, 19, 0.25, 4, 19.5, 2, "trim")
    e += B(12, 19, 0.25, 14, 19.5, 2, "trim")
    e += B(4, 20.5, 0.25, 6, 21, 2, "trim")
    e += B(10, 20.5, 0.25, 12, 21, 2, "trim")
    e += B(6, 21.5, 0.25, 10, 22, 2, "trim")
    e += bow(8, 20.25, 1.85, 2.4, 0.8)
    # footboard
    e += B(1.75, 3, 30, 14.25, 12, 31.5, "paint")
    e += B(1.75, 12, 29.75, 14.25, 12.75, 31.75, "trim")
    e += B(3, 4.5, 31.5, 13, 10.5, 31.75, "quilt", n=None)
    e += heart(8, 6.25, 31.75, 32, 0.32, "gold")
    # four posts with gold rings and finials
    for x in (0.25, 14.25):
        for z in (0.25, 30.25):
            e += B(x, 0, z, x + 1.5, 29.5, z + 1.5, "trim")
            e += B(x - 0.25, 6, z - 0.25, x + 1.75, 6.75, z + 1.75, "gold")
            e += B(x - 0.25, 18, z - 0.25, x + 1.75, 18.5, z + 1.75, "gold")
            e += B(x + 0.25, 29.5, z + 0.25, x + 1.25, 31.25, z + 1.25, "gold")
    # canopy frame, sheer roof and scalloped valance
    e += B(0.25, 28.25, 1.75, 1.75, 29.5, 30.25, "trim")
    e += B(14.25, 28.25, 1.75, 15.75, 29.5, 30.25, "trim")
    e += B(1.75, 28.25, 0.25, 14.25, 29.5, 1.75, "trim")
    e += B(1.75, 28.25, 30.25, 14.25, 29.5, 31.75, "trim")
    e += B(1, 29.25, 1, 15, 29.5, 31, "sheer")
    sheer_hem = ("sheer", "bottom")
    e += B(-0.25, 25.5, 0, 0.25, 29.5, 32, sheer_hem, u=None, d=None)
    e += B(15.75, 25.5, 0, 16.25, 29.5, 32, sheer_hem, u=None, d=None)
    e += B(0.25, 25.5, -0.25, 15.75, 29.5, 0.25, sheer_hem, u=None, d=None)
    e += B(0.25, 25.5, 31.75, 15.75, 29.5, 32.25, sheer_hem, u=None, d=None)
    # gathered drapes at the four corners, tied with bows
    for zc, zs in ((2.5, 1), (29.5, -1)):
        drape = []
        z_in = zc + 2.25 * zs
        z_lo, z_hi = sorted((zc - 2.25 * zs, z_in))
        drape += B(-0.6, 19, z_lo - 0.4, 0.2, 25.5, z_hi + 0.4, "sheer")
        drape += B(-0.6, 14, z_lo, 0.2, 19, z_hi, "sheer")
        drape += B(-0.6, 4, z_lo - 0.3, 0.2, 12.75, z_hi + 0.3, ("sheer", "bottom"))
        drape += B(-0.75, 12.75, z_lo + 0.4, 0.35, 14, z_hi - 0.4, "bow")
        drape += side_bow(-1.1, -0.6, 12.4, (z_lo + z_hi) / 2, 0.42)
        e += drape
        e += mirror_x(drape)
    return e


def pillows(count):
    e = []
    e += rbox(1.5, 0, 1.5, 14.5, 3.5, 14.5, "fabric", r=1, u="quilt")
    e += B(1.25, 1.5, 1.25, 14.75, 2, 14.75, "trim", u=None, d=None)
    for x in (1, 14):
        for z in (1, 14):
            e += B(x, 0.5, z, x + 1, 1.75, z + 1, "gold")
    if count >= 2:
        e += heart(8, 3.25, 10, 12, 0.8, "print")
        e += bow(8, 9.6, 9.6, 10.1, 0.5)
    if count >= 3:
        e += rbox(2.25, 3.5, 3.5, 7, 6, 7.5, "stripe", r=1)
        e += B(2, 4.25, 4.25, 2.35, 5.25, 6.75, "bow", n=None, s=None, u=None, d=None)
        e += B(6.9, 4.25, 4.25, 7.25, 5.25, 6.75, "bow", n=None, s=None, u=None, d=None)
        e += rbox(8.75, 3.5, 3.75, 13.25, 7.5, 7.75, "lining", r=0.75, rot=("y", 22.5, (11, 5.5, 5.75)))
    return e


def bedside_table(open_):
    e = []
    leg = B(2.25, 0.75, 2.25, 3.75, 3.5, 3.75, "trim") + B(2, 0, 2, 4, 0.75, 4, "gold")
    e += legs4(leg)
    e += B(2.5, 3, 2.5, 13.5, 13, 14.5, "paint", n=None)
    e += B(2.5, 3, 2.4, 13.5, 13, 2.5, "paint", s=None, u=None, d=None, e=None, w=None) if not open_ else []
    if open_:
        e += B(2.5, 7.5, 2.4, 13.5, 13, 2.5, "paint", s=None, u=None, d=None, e=None, w=None, n=None)
        e += B(2.5, 3, 2.4, 13.5, 8.5, 2.5, "paint", s=None, u=None, d=None, e=None, w=None)
        e += B(2.5, 12, 2.4, 13.5, 13, 2.5, "paint", s=None, u=None, d=None, e=None, w=None)
        e += B(2.5, 8.5, 2.4, 3.5, 12, 2.5, "paint", s=None, u=None, d=None, e=None, w=None)
        e += B(12.5, 8.5, 2.4, 13.5, 12, 2.5, "paint", s=None, u=None, d=None, e=None, w=None)
        e += B(3.5, 8.5, 2.45, 12.5, 12, 2.5, "lining", s=None, u=None, d=None, e=None, w=None)
    e += scallops(3, 13, 3, 2.25, 2.5, 5, "trim")
    e += B(1, 13, 1.5, 15, 14, 15.5, "paint")
    e += B(1.25, 12.5, 1.75, 14.75, 13, 15.25, "trim", u=None)
    # drawers
    e += B(3.5, 4, 2, 12.5, 7.5, 2.5, "panel")
    e += knob(8, 5.75, 2)
    if open_:
        dz = -5
        e += B(3.5, 8.5, 2 + dz, 12.5, 12, 2.5 + dz, "panel")
        e += knob(8, 10.25, 2 + dz)
        e += B(3.75, 8.75, 2.5 + dz, 4.25, 11.5, 2.5, "panel")
        e += B(11.75, 8.75, 2.5 + dz, 12.25, 11.5, 2.5, "panel")
        e += B(4.25, 8.75, 2.5 + dz, 11.75, 9.25, 2.5, "panel", u="lining")
        e += rbox(5, 9.25, -1.75, 7, 10.75, 0.75, "bow", r=0.5)
        e += B(8.25, 9.25, -2, 11, 9.75, 0.25, "pearl")
        e += B(8.75, 9.75, -1.5, 10.5, 10.25, -0.25, "gold")
    else:
        e += B(3.5, 8.5, 2, 12.5, 12, 2.5, "panel")
        e += knob(8, 10.25, 2)
        e += heart(8, 11.25, 1.85, 2, 0.12, "gold")
    return e


def wardrobe(open_):
    e = []
    for x in (1.5, 13):
        for z in (2, 13.5):
            e += B(x, 0, z, x + 1.5, 1.5, z + 1.5, "gold")
    e += B(1, 1.5, 2, 15, 2.5, 15, "trim")
    # carcass (walls so the interior shows when open)
    e += B(1, 2.5, 2, 2, 30, 15, "paint", e="panel")
    e += B(14, 2.5, 2, 15, 30, 15, "paint", w="panel")
    e += B(2, 2.5, 14, 14, 30, 15, "paint", n="panel")
    e += B(2, 2.5, 2, 14, 3.5, 14, "paint", u="panel")
    e += B(2, 29, 2, 14, 30, 14, "paint", d="panel")
    # crown with an arched pediment and a heart
    e += B(0.5, 30, 1.25, 15.5, 31, 15.5, "trim")
    e += B(1, 31, 1.5, 15, 31.5, 15.25, "paint")
    e += B(5, 31.5, 1.5, 11, 32, 2.5, "paint")
    e += heart(8, 28.6, 1.15, 1.35, 0.35, "gold")
    e += scallops(2, 14, 30, 1.25, 1.5, 6, "trim")
    if not open_:
        for x0, x1, kx in ((1.5, 7.9, 6.9), (8.1, 14.5, 9.1)):
            e += B(x0, 3, 1.5, x1, 29.5, 2, "paint")
            e += B(x0 + 1, 4.5, 1.25, x1 - 1, 13, 1.5, "panel", s=None)
            e += B(x0 + 1, 15, 1.25, x1 - 1, 26, 1.5, "panel", s=None)
            e += B(x0 + 1.75, 26, 1.25, x1 - 1.75, 27, 1.5, "panel", s=None)
            e += knob(kx, 16, 1.25, 0.9)
        e += bow(8, 26.25, 1, 1.5, 0.75)
    else:
        e += B(2, 24, 8, 14, 24.5, 8.75, "gold")
        dresses = (("dress1", 3.25), ("dress2", 6), ("dress3", 8.75), ("dress4", 11.5))
        for i, (tex, x) in enumerate(dresses):
            e += B(x + 0.5, 23, 8, x + 1, 24, 8.75, "silver")
            e += B(x, 18, 7.5, x + 1.5, 23, 9.25, tex)
            e += B(x - 0.5, 12 + (i % 2), 7, x + 2, 18, 9.75, tex)
            e += B(x - 0.75, 11.5 + (i % 2), 6.75, x + 2.25, 12.5 + (i % 2), 10, ("lace", "bottom"))
        e += B(2, 7, 4, 14, 7.5, 14, "panel")
        for x, tex in ((3, "dress2"), (6.5, "dress4"), (10, "dress1")):
            e += B(x, 7.5, 5, x + 3, 9, 9, tex)
        e += B(3, 3.5, 4, 5, 4.5, 7, "fabric")
        e += B(5.5, 3.5, 4, 7.5, 4.5, 7, "fabric")
        for door_x0, mx in ((1.5, False), (8.1, True)):
            door = []
            door += B(0.75, 3, 1.5 - 6.4, 1.25, 29.5, 1.5, "paint")
            door += B(0.5, 4.5, -4.4, 0.75, 13, 0.5, "panel", e=None)
            door += B(0.5, 15, -4.4, 0.75, 26, 0.5, "panel", e=None)
            door += B(1.25, 4.5, -4.4, 1.5, 26, 0.5, "mirror", w=None)
            door += B(-0.25, 15.5, -4.25, 0.5, 16.5, -3.25, "gold")
            e += mirror_x(door) if mx else door
    return e


def dresser(open_):
    e = []
    for x in (1.5, 13):
        for z in (2, 13):
            e += B(x, 0, z, x + 1.5, 1.5, z + 1.5, "gold")
    e += B(1, 1.5, 1.5, 15, 2.5, 15, "trim")
    e += B(1, 2.5, 1.5, 15, 15, 15, "paint")
    e += B(0.5, 15, 1, 15.5, 16, 15.5, "paint")
    e += B(0.75, 14.5, 1.25, 15.25, 15, 15.25, "trim", u=None)
    e += B(0.75, 2.5, 1, 2, 15, 1.5, "trim")
    e += B(14, 2.5, 1, 15.25, 15, 1.5, "trim")
    e += scallops(2, 14, 2.5, 1.25, 1.5, 6, "trim")
    rows = [(3.25, 6.5), (7.25, 10.5), (11.25, 14.25)]
    for i, (y0, y1) in enumerate(rows):
        if i == 2:
            for x0, x1 in ((2.25, 7.85), (8.15, 13.75)):
                e += B(x0, y0, 1, x1, y1, 1.5, "panel")
                e += knob((x0 + x1) / 2, (y0 + y1) / 2, 1, 0.9)
            continue
        if open_ and i == 1:
            dz = -5.5
            e += B(2.25, y0, 1.05, 13.75, y1, 1.5, "lining", s=None, u=None, d=None, e=None, w=None)
            e += B(2.25, y0, 1 + dz, 13.75, y1, 1.5 + dz, "panel")
            e += knob(5, (y0 + y1) / 2, 1 + dz, 0.9)
            e += knob(11, (y0 + y1) / 2, 1 + dz, 0.9)
            e += B(2.5, y0 + 0.25, 1.5 + dz, 3, y1 - 0.5, 1.5, "panel")
            e += B(13, y0 + 0.25, 1.5 + dz, 13.5, y1 - 0.5, 1.5, "panel")
            e += B(3, y0 + 0.25, 1.5 + dz, 13, y0 + 0.75, 1.5, "panel", u="lining")
            e += B(3.5, y0 + 0.75, -3.5, 7.5, y0 + 1.75, 0.5, "dress1")
            e += B(3.75, y0 + 1.75, -3.25, 7.25, y0 + 2.5, 0.25, "dress3")
            e += B(8.5, y0 + 0.75, -3.5, 12.5, y0 + 1.75, 0.5, "dress2")
            continue
        e += B(2.25, y0, 1, 13.75, y1, 1.5, "panel")
        e += knob(5, (y0 + y1) / 2, 1, 0.9)
        e += knob(11, (y0 + y1) / 2, 1, 0.9)
    e += heart(8, 7.75, 0.85, 1, 0.2, "gold") if not open_ else []
    return e


def vanity_table(open_):
    e = []
    leg = B(1.75, 0.75, 3.75, 3, 11, 5, "paint") + B(1.5, 0, 3.5, 3.25, 0.75, 5.25, "gold")
    e += sym(leg)
    e += sym(move(leg, 0, 0, 9.5))
    e += B(1.5, 8.5, 3.5, 14.5, 11, 14.75, "paint")
    e += B(0.5, 11, 2.5, 15.5, 12, 15.5, "paint")
    e += B(0.75, 10.5, 2.75, 15.25, 11, 15.25, "trim", u=None, d=None)
    e += B(1.5, 7, 3.25, 14.5, 8.75, 3.4, LACE_HEM, s=None, u=None, d=None, e=None, w=None)
    if open_:
        dz = -4.5
        e += B(5, 8.75, 3.35, 11, 10.75, 3.5, "lining", s=None, u=None, d=None, e=None, w=None)
        e += B(5, 8.75, 3 + dz, 11, 10.75, 3.5 + dz, "panel")
        e += knob(8, 9.75, 3 + dz)
        e += B(5.25, 8.75, 3.5 + dz, 5.75, 10.25, 3.5, "panel")
        e += B(10.25, 8.75, 3.5 + dz, 10.75, 10.25, 3.5, "panel")
        e += B(5.75, 8.75, 3.5 + dz, 10.25, 9.25, 3.5, "panel", u="lining")
        e += B(6.25, 9.25, -0.5, 6.75, 10.5, 0, "gold")
        e += B(6.25, 10.5, -0.5, 6.75, 10.75, 0, "heart")
        e += B(7.5, 9.25, -0.75, 9.75, 9.75, 1.5, "ceramic")
        e += B(8, 9.75, -0.25, 9.25, 10, 1, "pearl")
    else:
        e += B(5, 8.75, 3, 11, 10.75, 3.5, "panel")
        e += knob(8, 9.75, 3)
    # mirror with Hollywood bulbs and a bow
    e += B(5, 12, 13, 11, 13, 15.25, "paint")
    frame = [(13, 14, 5.5, 10.5), (14, 15, 4.5, 11.5), (15, 17, 3.5, 12.5), (17, 24, 3, 13),
             (24, 26, 3.5, 12.5), (26, 27, 4.5, 11.5), (27, 28.5, 5.5, 10.5)]
    for y0, y1, x0, x1 in frame:
        e += B(x0, y0, 13.25, x1, y1, 14.75, "paint")
    glass = [(14, 15, 6, 10), (15, 16, 5, 11), (16, 18, 4.25, 11.75), (18, 23.5, 4, 12),
             (23.5, 25.5, 4.5, 11.5), (25.5, 26.5, 5.5, 10.5), (26.5, 27.5, 6.5, 9.5)]
    for y0, y1, x0, x1 in glass:
        e += B(x0, y0, 13.1, x1, y1, 13.25, "mirror", s=None, u=None, d=None)
    e += bow(8, 27.6, 12.85, 13.35, 0.8)
    for y in (15.75, 19.75, 23.75):
        e += B(2.25, y, 13.5, 3, y + 0.75, 14.25, "bulb", shade=False)
        e += B(13, y, 13.5, 13.75, y + 0.75, 14.25, "bulb", shade=False)
    # perfume and a powder compact on the desk
    e += B(11.75, 12, 6, 13, 13.75, 7.25, "glass_pink")
    e += B(12, 13.75, 6.25, 12.75, 14.25, 7, "gold")
    e += rbox(2.75, 12, 6, 5.25, 12.75, 8.5, "ceramic", r=0.5)
    e += B(3.25, 12.75, 6.5, 4.75, 12.85, 8, "gold", d=None)
    return e


def standing_mirror():
    e = []
    for x0, x1 in ((1, 4), (12, 15)):
        e += B(x0, 0, 5.5, x1, 1, 10.5, "paint")
        e += B(x0 + 0.5, 1, 6.5, x1 - 0.5, 1.75, 9.5, "paint")
        e += B(x0, 0, 5.25, x1, 0.5, 5.5, "gold")
    e += B(1.75, 1.75, 7.25, 2.75, 26, 8.75, "paint")
    e += B(13.25, 1.75, 7.25, 14.25, 26, 8.75, "paint")
    e += B(1.5, 26, 7, 3, 26.5, 9, "gold")
    e += B(13, 26, 7, 14.5, 26.5, 9, "gold")
    e += B(1.75, 26.5, 7.5, 2.75, 28, 8.5, "gold")
    e += B(13.25, 26.5, 7.5, 14.25, 28, 8.5, "gold")
    e += B(2.75, 16.25, 7.5, 3.5, 17.25, 8.5, "gold")
    e += B(12.5, 16.25, 7.5, 13.25, 17.25, 8.5, "gold")
    frame = [(3, 4, 6.5, 9.5), (4, 5, 5.5, 10.5), (5, 7, 4.5, 11.5), (7, 25, 3.5, 12.5),
             (25, 27, 4.5, 11.5), (27, 28, 5.5, 10.5), (28, 29, 6.5, 9.5)]
    for y0, y1, x0, x1 in frame:
        e += B(x0, y0, 7.25, x1, y1, 8.75, "paint")
    glass = [(4, 5, 7, 9), (5, 6, 6, 10), (6, 8, 5, 11), (8, 24, 4.5, 11.5),
             (24, 26, 5, 11), (26, 27, 6, 10), (27, 28, 7, 9)]
    for y0, y1, x0, x1 in glass:
        e += B(x0, y0, 7.1, x1, y1, 7.25, "mirror", s=None, u=None, d=None)
    e += bow(8, 28.6, 7.25, 8.75, 0.95)
    e += heart(8, 2, 7.35, 7.5, 0.25, "gold") if False else []
    return e


def jewelry_box(open_):
    e = []
    for x in (4.25, 11):
        for z in (5.25, 10):
            e += B(x, 0, z, x + 0.75, 0.5, z + 0.75, "gold")
    if not open_:
        e += B(4, 0.5, 5, 12, 4, 11, "paint")
        e += B(3.85, 3.4, 4.85, 12.15, 3.9, 11.15, "gold", u=None, d=None)
        e += B(3.75, 4, 4.75, 12.25, 5.25, 11.25, "paint", u="quilt")
        e += B(4.5, 5.25, 5.5, 11.5, 5.75, 10.5, "paint", u="quilt")
        e += heart(8, 2.4, 4.7, 4.85, 0.25, "gold")
        e += bow(8, 5.75, 7.75, 8.25, 0.55)
    else:
        e += B(4, 0.5, 5, 12, 4, 5.75, "paint", s="lining")
        e += B(4, 0.5, 10.25, 12, 4, 11, "paint", n="lining")
        e += B(4, 0.5, 5.75, 4.75, 4, 10.25, "paint", e="lining")
        e += B(11.25, 0.5, 5.75, 12, 4, 10.25, "paint", w="lining")
        e += B(4.75, 0.5, 5.75, 11.25, 2.5, 10.25, "lining")
        e += B(3.85, 3.4, 4.85, 12.15, 3.9, 5.75, "gold", u=None, d=None)
        e += heart(8, 2.4, 4.7, 4.85, 0.25, "gold")
        # lid swung back, mirror inside
        e += B(3.75, 4, 11, 12.25, 10.5, 12.25, "paint", s="quilt")
        e += B(4.5, 4.75, 10.9, 11.5, 9.75, 11, "mirror", s=None, u=None, d=None, e=None, w=None)
        e += B(4.5, 4.75, 12.25, 11.5, 9.75, 12.75, "paint", s="quilt")
        # pearls, a ring and a tiny ballerina
        for i in range(6):
            e += B(5.25 + i * 0.9, 2.5, 6.5 + (i % 2) * 0.4, 5.85 + i * 0.9, 3.1, 7.1 + (i % 2) * 0.4, "pearl")
        e += B(9.5, 2.5, 8.75, 10.75, 3, 9.25, "gold")
        e += B(9.85, 3, 8.75, 10.4, 3.5, 9.25, "heart")
        e += B(7.25, 3.6, 8.5, 8.75, 4, 10, "lace")
        e += B(7.7, 2.5, 8.95, 8.3, 5.5, 9.55, "porcelain")
        e += B(7.65, 5.5, 8.9, 8.35, 6.2, 9.6, "porcelain")
    return e


def night_lamp():
    e = []
    e += rbox(5.5, 0, 5.5, 10.5, 1, 10.5, "gold", r=0.5)
    e += B(6.5, 1, 6.5, 9.5, 2, 9.5, "ceramic")
    e += rbox(5.75, 2, 5.75, 10.25, 4.75, 10.25, "ceramic", r=0.75)
    e += B(6.5, 4.75, 6.5, 9.5, 5.5, 9.5, "ceramic")
    e += B(7.5, 5.5, 7.5, 8.5, 7.5, 8.5, "gold")
    e += B(7.25, 6.75, 7.25, 8.75, 7.5, 8.75, "bulb", shade=False)
    e += B(4, 7, 4, 12, 9.75, 12, "shade", d=("shade", "fit"))
    e += B(4.75, 9.75, 4.75, 11.25, 12, 11.25, "shade")
    e += B(3.85, 6.25, 3.85, 12.15, 7.4, 12.15, LACE_HEM, u=None, d=None)
    e += B(4.6, 11.75, 4.6, 11.4, 12.25, 11.4, "trim")
    e += bow(8, 7.9, 3.45, 3.95, 0.55)
    e += B(10.25, 5.25, 10, 10.5, 7, 10.25, "gold")
    e += B(10.1, 4.75, 9.85, 10.65, 5.25, 10.4, "gold")
    return e


# =========================================================================== living room

def sofa(shape):
    arm_l = shape in ("single", "left")
    arm_r = shape in ("single", "right")
    e = []
    bx0 = 1 if arm_l else 0
    bx1 = 15 if arm_r else 16
    sx0 = 2.75 if arm_l else 0
    sx1 = 13.25 if arm_r else 16
    e += B(bx0, 2, 2, bx1, 5, 15, "fabric")
    e += B(bx0, 1.25, 1.85, bx1, 3.5, 2, LACE_HEM, s=None, u=None, d=None, e=None, w=None)
    e += B(sx0, 5, 1.5, sx1, 7.5, 12.5, "fabric", u="quilt")
    e += B(sx0, 6.75, 1.35, sx1, 7.25, 1.5, "trim", s=None, u=None, d=None)
    e += B(bx0, 5, 12.5, bx1, 15, 15.5, "fabric", n="quilt")
    e += B(bx0, 15, 12.75, bx1, 16.5, 15.75, "fabric")
    e += B(bx0, 16.5, 13.25, bx1, 17, 15.25, "fabric")
    if shape == "single":
        e += B(4, 17, 13.25, 12, 18, 15.25, "fabric")
        e += B(6, 18, 13.5, 10, 18.75, 15, "fabric")
        e += heart(8, 7.5, 10.75, 12.25, 0.6, "print")
    for has, flip in ((arm_l, False), (arm_r, True)):
        if not has:
            continue
        arm = []
        arm += B(0.5, 5, 2, 3, 10.5, 15.5, "fabric")
        arm += B(0.25, 10, 1.75, 3.25, 11.5, 15.25, "fabric")
        arm += B(0.75, 5.5, 1.6, 2.75, 9.75, 1.75, "quilt", s=None, u=None, d=None, e=None, w=None)
        arm += B(1.25, 10.25, 1.25, 2.25, 11.25, 1.75, "gold")
        arm += B(1.25, 0, 2.5, 2.75, 2, 4, "gold")
        arm += B(1.25, 0, 13, 2.75, 2, 14.5, "gold")
        if shape != "single":
            arm += rbox(3.25, 7.5, 9.25, 7.75, 12.25, 11.75, "print", r=0.75, rot=("x", -22.5, (5.5, 7.5, 11.5)))
        e += mirror_x(arm) if flip else arm
    return e


def armchair():
    e = []
    leg = B(1.75, 0, 2.25, 3, 2, 3.5, "trim") + B(1.5, 0, 2, 3.25, 0.5, 3.75, "gold")
    e += legs4(leg)
    e += B(1, 2, 1.5, 15, 5, 15, "fabric")
    e += B(0.85, 1.25, 1.35, 15.15, 3.5, 1.5, LACE_HEM, s=None, u=None, d=None, e=None, w=None)
    e += rbox(3, 5, 1.25, 13, 8, 12.75, "fabric", r=0.75, u="quilt")
    e += B(2, 5, 12.5, 14, 19, 15, "fabric", n="quilt")
    e += B(3, 19, 12.75, 13, 20.5, 15, "fabric", n="quilt")
    e += B(5, 20.5, 13, 11, 21.5, 14.75, "fabric")
    e += bow(8, 18.75, 12.15, 12.6, 0.75)
    side = []
    side += B(0.75, 5, 2, 3, 11, 15, "fabric")
    side += B(0.5, 10.5, 1.75, 3.25, 12, 14.5, "fabric")
    side += B(1, 12, 9, 2.75, 19, 15, "fabric")
    side += B(1.25, 19, 10, 2.5, 19.75, 15, "fabric")
    side += B(0.85, 5.5, 1.6, 2.9, 10.25, 1.75, "quilt", s=None, u=None, d=None, e=None, w=None)
    side += B(1.4, 10.6, 1.25, 2.4, 11.6, 1.75, "gold")
    e += sym(side)
    return e


def coffee_table():
    e = []
    e += rbox(1, 7, 2, 15, 8, 14, "paint", r=1)
    e += B(1.75, 6.5, 2.75, 14.25, 7, 13.25, "trim", u=None)
    leg = []
    leg += B(2.25, 0.75, 3.25, 3.25, 6.5, 4.25, "gold")
    leg += B(1.75, 5.25, 2.75, 3.25, 6.5, 4.25, "gold")
    leg += B(2, 0, 3, 3.5, 0.75, 4.5, "gold")
    e += sym(leg + mirror_z(leg))
    e += B(2.75, 2, 3.75, 13.25, 2.75, 12.25, "paint")
    e += book(4, 2.75, 5, 9, 3.75, 9.5, "book_pink")
    e += book(4.5, 3.75, 5.5, 8.5, 4.5, 9, "book_lavender", spine="east")
    e += B(10, 2.75, 6.5, 12, 4.5, 8.5, "ceramic")
    e += B(10.25, 4.5, 6.75, 11.75, 4.75, 8.25, "soil", d=None)
    e += cross_plane(9.5, 4.5, 12.5, 7, "leaves", cx=11, cz=7.5)
    e += scallops(2, 14, 6.5, 2.5, 2.75, 6, "trim")
    e += scallops(2, 14, 6.5, 13.25, 13.5, 6, "trim")
    return e


def bookshelf():
    e = []
    e += B(0, 0, 1, 1.5, 16, 16, "paint")
    e += B(14.5, 0, 1, 16, 16, 16, "paint")
    e += B(0, 14.5, 0.5, 16, 16, 16, "paint")
    e += B(1.5, 0, 1, 14.5, 1.5, 16, "paint")
    e += B(1.5, 0, 0.75, 14.5, 1, 1, "trim")
    e += B(1.5, 7.25, 1.5, 14.5, 8, 16, "paint")
    e += B(1.5, 1.5, 15, 14.5, 14.5, 16, "paint", n="print")
    e += scallops(1.5, 14.5, 14.5, 0.75, 1, 6, "trim")
    e += heart(8, 14.1, 0.35, 0.5, 0.2, "gold")
    covers = ["book_pink", "book_blue", "book_lavender", "book_mint", "book_cream"]
    row1 = [(1.25, 5), (1.5, 4.5), (1, 5.25), (1.25, 4.75), (1.5, 5), (1, 4), (1.25, 5.25), (1.5, 4.5)]
    x = 1.75
    for i, (w, h) in enumerate(row1):
        e += standing_book(x, w, h, 1.5, 4, 14.75, covers[i % 5])
        x += w + 0.1
    e += book(x + 0.5, 1.5, 4, x + 1.75, 6, 14.75, covers[2], spine="north", rot=("z", -22.5, (x + 0.5, 1.5, 9)))
    row2 = [(1.5, 5), (1.25, 5.5), (1, 4.75), (1.5, 5.25), (1.25, 4.5)]
    x = 1.75
    for i, (w, h) in enumerate(row2):
        e += standing_book(x, w, h, 8, 4, 14.75, covers[(i + 2) % 5])
        x += w + 0.1
    e += B(10.5, 8, 7, 13.25, 10.5, 10, "ceramic")
    e += B(10.75, 10.5, 7.25, 13, 10.75, 9.75, "soil", d=None)
    e += cross_plane(10, 10.5, 13.75, 14, "leaves", cx=11.9, cz=8.5)
    return e


def display_cabinet():
    e = []
    e += B(1, 0.5, 2, 15, 1.5, 15, "trim")
    for x in (1.25, 13.25):
        for z in (2.25, 13.25):
            e += B(x, 0, z, x + 1.5, 0.75, z + 1.5, "gold")
    e += B(1, 1.5, 14.5, 15, 14.5, 15.5, "paint", n="print")
    for x in (1, 13.75):
        e += B(x, 1.5, 2, x + 1.25, 14.5, 3.25, "paint")
        e += B(x, 1.5, 13.25, x + 1.25, 14.5, 14.5, "paint")
    e += B(1.25, 1.5, 3.25, 1.75, 14.5, 13.25, "glass", u=None, d=None)
    e += B(14.25, 1.5, 3.25, 14.75, 14.5, 13.25, "glass", u=None, d=None)
    e += B(0.5, 14.5, 1.5, 15.5, 16, 15.75, "paint")
    e += scallops(1, 15, 14.5, 1.5, 1.75, 7, "trim")
    e += B(2.25, 1.5, 3, 13.75, 2, 14.5, "panel")
    e += B(2.25, 7.75, 3.25, 13.75, 8.25, 14.5, "panel")
    e += B(2.25, 12.5, 3.1, 13.75, 14.5, 3.25, LACE_HEM, s=None, u=None, d=None, e=None, w=None)
    e += B(2.25, 1.5, 2, 13.75, 2.5, 2.75, "paint")
    e += B(2.25, 13.5, 2, 13.75, 14.5, 2.75, "paint")
    e += B(2.25, 2.5, 2.25, 13.75, 13.5, 2.5, "glass", u=None, d=None, e=None, w=None)
    e += knob(12.6, 8, 2, 0.9)
    return e


def rug_center():
    return B(0, 0, 0, 16, 0.75, 16, "rug")


def rug_edge():
    e = B(0, 0, 0, 16, 1, 3, ("rug_edge", "top"), d=None)
    for i in range(8):
        e += B(i * 2 + 0.75, 0, -1, i * 2 + 1.25, 0.25, 0, "sheet", u="sheet")
    return e


def curtains(open_):
    e = []
    e += B(0, 29.5, 13, 16, 30.5, 14, "gold")
    e += B(1, 29, 13.5, 2, 31, 16, "gold")
    e += B(14, 29, 13.5, 15, 31, 16, "gold")
    e += B(0, 26.5, 12, 16, 30, 13, "fabric")
    e += B(0, 25.5, 11.9, 16, 27, 12, LACE_HEM, s=None, u=None, d=None, e=None, w=None)
    e += bow(8, 26.25, 11.4, 11.9, 0.7)
    for x in range(0, 16, 2):
        e += B(x + 0.6, 29.25, 12.75, x + 1.4, 30.75, 14.25, "gold")
    if not open_:
        for x in range(0, 16, 2):
            deep = (x // 2) % 2 == 0
            z0 = 13 if deep else 13.5
            e += B(x, 1, z0, x + 2, 26.5, z0 + 1.5, "print")
        e += B(0, 0.25, 12.85, 16, 1.75, 13, LACE_HEM, s=None, u=None, d=None, e=None, w=None)
    else:
        side = []
        side += B(0, 14, 12.5, 2, 26.5, 15, "print")
        side += B(2, 15, 13, 4, 26.5, 15, "print")
        side += B(0, 11.5, 12.75, 2.5, 14, 15, "print")
        side += B(0, 1, 12.5, 2, 11.5, 15, "print")
        side += B(2, 1, 13, 3.75, 10.5, 15, "print")
        side += B(-0.1, 11.75, 12.25, 2.75, 13.25, 15.1, "bow")
        side += side_bow(2.6, 3.1, 11.5, 13.6, 0.45)
        side += B(0, 0.25, 12.35, 3.75, 1.75, 12.5, LACE_HEM, s=None, u=None, d=None, e=None, w=None)
        e += sym(side)
    return e


# =========================================================================== kitchen

def _base_carcass(top="marble"):
    e = []
    e += B(1, 0, 2, 15, 1.5, 15.5, "trim")
    e += B(0.5, 1.5, 1.5, 1.25, 14.5, 16, "paint", e="panel")
    e += B(14.75, 1.5, 1.5, 15.5, 14.5, 16, "paint", w="panel")
    e += B(1.25, 1.5, 15.25, 14.75, 14.5, 16, "paint", n="panel")
    e += B(1.25, 1.5, 1.5, 14.75, 2.25, 15.25, "paint", u="panel")
    if top:
        e += B(0, 14.5, 0, 16, 16, 16, top)
        e += B(0, 14.25, -0.1, 16, 14.5, 0.5, "gold", u=None)
    return e


def kitchen_cabinet(open_):
    e = _base_carcass()
    if not open_:
        for x0, x1, kx in ((1.25, 7.85, 6.4), (8.15, 14.75, 9.6)):
            e += B(x0, 2.25, 1, x1, 13.75, 1.5, "paint")
            e += B(x0 + 1, 3.25, 0.75, x1 - 1, 12.75, 1, "panel", s=None)
            e += heart((x0 + x1) / 2, 4.25, 0.6, 0.75, 0.3, "trim")
            e += knob(kx, 11.5, 0.75)
    else:
        e += B(1.25, 7.5, 2, 14.75, 8, 15.25, "panel")
        for x in (2.5, 4.5):
            pass
        for i in range(4):
            e += B(2.25, 2.25 + i * 0.6, 5, 6.75, 2.75 + i * 0.6, 9.5, "porcelain")
        e += B(2.25, 4.75, 5, 6.75, 4.85, 9.5, "ceramic", n=None, s=None, e=None, w=None, d=None)
        e += B(9, 2.25, 6, 11, 5.25, 8, "glass_pink")
        e += B(9.25, 5.25, 6.25, 10.75, 5.75, 7.75, "gold")
        e += B(11.75, 2.25, 9, 13.5, 4.5, 10.75, "glass_mint")
        for x in (3, 6.5, 10):
            e += B(x, 8, 7, x + 2, 9.5, 9, "ceramic")
            e += B(x + 2, 8.5, 7.75, x + 2.5, 9.25, 8.25, "ceramic")
        for door, flip in ((1.25, False), (8.15, True)):
            d = []
            d += B(0.75, 2.25, -5.6, 1.25, 13.75, 1, "paint")
            d += B(0.5, 3.25, -4.6, 0.75, 12.75, 0, "panel", e=None)
            d += knob(0.5, 11.5, -4.75, 0.8) if False else B(-0.25, 11, -5, 0.5, 12, -4, "gold")
            e += mirror_x(d) if flip else d
    return e


def kitchen_counter(open_):
    e = _base_carcass()
    rows = [(10.25, 13.75), (6.25, 9.75), (2.25, 5.75)]
    for i, (y0, y1) in enumerate(rows):
        if open_ and i == 0:
            dz = -6
            e += B(1.25, y0, 1.35, 14.75, y1, 1.5, "lining", s=None, u=None, d=None, e=None, w=None)
            e += B(1.25, y0, 1 + dz, 14.75, y1, 1.5 + dz, "paint")
            e += B(2, y0 + 0.5, 0.75 + dz, 14, y1 - 0.5, 1 + dz, "panel", s=None)
            e += B(6.5, y1 - 2, 0.25 + dz, 9.5, y1 - 1.5, 0.75 + dz, "gold")
            e += B(1.75, y0 + 0.25, 1.5 + dz, 2.25, y1 - 0.75, 1.5, "paint")
            e += B(13.75, y0 + 0.25, 1.5 + dz, 14.25, y1 - 0.75, 1.5, "paint")
            e += B(2.25, y0 + 0.25, 1.5 + dz, 13.75, y0 + 0.75, 1.5, "paint", u="lining")
            for k, x in enumerate((3.25, 4.75, 6.25)):
                e += B(x, y0 + 0.75, -3.5, x + 0.5, y0 + 1.1, 0.5, "silver")
                e += B(x - 0.25, y0 + 0.75, -4.25 + (k % 2) * 0.25, x + 0.75, y0 + 1.25, -3.25, "silver")
            e += B(8, y0 + 0.75, -3.75, 12.5, y0 + 2, -2.5, "wood")
            e += B(7.25, y0 + 1.1, -3.5, 8, y0 + 1.65, -2.75, "wood")
            e += B(12.5, y0 + 1.1, -3.5, 13.25, y0 + 1.65, -2.75, "wood")
            e += B(8.5, y0 + 0.75, -1.5, 12, y0 + 1.25, 0.5, "dress1")
            continue
        e += B(1.25, y0, 1, 14.75, y1, 1.5, "paint")
        e += B(2, y0 + 0.5, 0.75, 14, y1 - 0.5, 1, "panel", s=None)
        e += B(6.5, y1 - 2, 0.25, 9.5, y1 - 1.5, 0.75, "gold")
        e += B(6.5, y1 - 2, 0.75, 7, y1 - 1.5, 1, "gold") + B(9, y1 - 2, 0.75, 9.5, y1 - 1.5, 1, "gold")
    return e


def kitchen_sink(running):
    e = _base_carcass(top=None)
    for x0, x1, kx in ((1.25, 7.85, 6.4), (8.15, 14.75, 9.6)):
        e += B(x0, 2.25, 1, x1, 13.75, 1.5, "paint")
        e += B(x0 + 1, 3.25, 0.75, x1 - 1, 12.75, 1, "panel", s=None)
        e += knob(kx, 11.5, 0.75)
    e += B(0, 14.5, 0, 16, 16, 3, "marble")
    e += B(0, 14.5, 11.5, 16, 16, 16, "marble")
    e += B(0, 14.5, 3, 3, 16, 11.5, "marble")
    e += B(13, 14.5, 3, 16, 16, 11.5, "marble")
    e += B(0, 14.25, -0.1, 16, 14.5, 0.5, "gold", u=None)
    e += B(3, 10, 3, 3.5, 14.5, 11.5, "silver")
    e += B(12.5, 10, 3, 13, 14.5, 11.5, "silver")
    e += B(3.5, 10, 3, 12.5, 14.5, 3.5, "silver")
    e += B(3.5, 10, 11, 12.5, 14.5, 11.5, "silver")
    e += B(3.5, 10, 3.5, 12.5, 10.5, 11, "silver")
    e += B(7.25, 10.5, 6.75, 8.75, 10.55, 8.25, "dark_metal", n=None, s=None, e=None, w=None, d=None)
    e += B(6.5, 16, 12.5, 9.5, 16.5, 14.5, "silver")
    e += B(7.5, 16.5, 13, 8.5, 21, 14, "silver")
    e += B(7.5, 21, 9, 8.5, 22, 14, "silver")
    e += B(7.5, 20, 9, 8.5, 21, 10, "silver")
    for x in (4.25, 10.25):
        e += B(x, 16, 12.75, x + 1.5, 17.5, 14.25, "porcelain")
        e += B(x + 0.25, 17.5, 13, x + 1.25, 18, 14, "ceramic")
    e += heart(8, 18.5, 12.85, 13, 0.18, "gold")
    if running:
        e += B(7.65, 11, 9.15, 8.35, 20, 9.85, "water_flow", shade=False)
        e += B(3.5, 12.5, 3.5, 12.5, 12.75, 11, "water", n=None, s=None, e=None, w=None, d=None)
        e += B(6.25, 12.75, 7.75, 9.75, 12.85, 11, "foam", n=None, s=None, e=None, w=None, d=None)
    return e


def stove():
    e = []
    for x in (1, 13.5):
        for z in (2, 13.5):
            e += B(x, 0, z, x + 1.5, 1, z + 1.5, "silver")
    e += B(0.5, 1, 1.5, 15.5, 15, 15.5, "ceramic")
    e += B(0.25, 15, 1.25, 15.75, 15.5, 15.75, "porcelain")
    for x0, z0 in ((2, 2.25), (9.5, 2.25), (2, 8), (9.5, 8)):
        e += B(x0, 15.5, z0, x0 + 4.5, 15.75, z0 + 4.5, "burner", u=("burner", "fit"))
    e += B(0.5, 15, 13.5, 15.5, 20, 15.5, "ceramic")
    e += B(0.5, 20, 13.75, 15.5, 20.5, 15.25, "gold")
    e += B(6.5, 16, 13.35, 9.5, 19, 13.5, ("clock", "fit"), s=None, u=None, d=None, e=None, w=None)
    e += B(6.25, 15.75, 13.25, 9.75, 16, 13.5, "gold")
    e += B(6.25, 19, 13.25, 9.75, 19.25, 13.5, "gold")
    e += B(1.5, 12, 1, 14.5, 14.5, 1.5, "trim")
    for x in (2, 4.75, 9.75, 12.5):
        e += B(x, 12.5, 0.25, x + 1.5, 14, 1, "porcelain")
        e += B(x + 0.5, 13, 0, x + 1, 13.5, 0.25, "gold")
    e += B(1.5, 2, 1, 14.5, 11.5, 1.5, "ceramic")
    e += B(3.5, 4, 0.9, 12.5, 9.5, 1, ("oven", "fit"), s=None, u=None, d=None, e=None, w=None)
    e += B(3, 10, 0, 13, 10.75, 0.5, "silver")
    e += B(3, 10, 0.5, 3.75, 10.75, 1, "silver")
    e += B(12.25, 10, 0.5, 13, 10.75, 1, "silver")
    e += heart(8, 2.25, 0.85, 1, 0.2, "gold")
    return e


def refrigerator(open_):
    e = []
    for x in (1.5, 13):
        for z in (2.5, 13):
            e += B(x, 0, z, x + 1.5, 1, z + 1.5, "silver")
    e += B(1, 1, 2, 2, 30.5, 15, "ceramic", e="porcelain")
    e += B(14, 1, 2, 15, 30.5, 15, "ceramic", w="porcelain")
    e += B(2, 1, 14, 14, 30.5, 15, "ceramic", n="porcelain")
    e += B(2, 1, 2, 14, 2, 14, "ceramic", u="porcelain")
    e += B(2, 29.5, 2, 14, 30.5, 14, "ceramic", d="porcelain")
    e += B(1.5, 30.5, 2.5, 14.5, 31.25, 14.5, "ceramic")
    e += B(2.5, 31.25, 3.5, 13.5, 31.75, 13.5, "ceramic")
    e += B(2, 20.75, 2, 14, 21.25, 14, "porcelain")
    if not open_:
        e += B(1.5, 21.5, 1.5, 14.5, 30.25, 2, "ceramic")
        e += B(1.5, 1.5, 1.5, 14.5, 20.75, 2, "ceramic")
        e += B(1.25, 20.75, 1.25, 14.75, 21.5, 2, "silver")
        e += B(12, 22.5, 0.5, 13, 26, 1.5, "silver")
        e += B(12, 14, 0.5, 13, 19.5, 1.5, "silver")
        e += B(12.25, 25, 1, 12.75, 25.5, 1.5, "silver")
        e += heart(7, 26.5, 1.35, 1.5, 0.3, "gold")
        e += B(4, 24.75, 1.4, 10, 25.25, 1.5, "gold", s=None)
    else:
        for y in (8, 14):
            e += B(2, y, 3, 14, y + 0.5, 14, "glass", n="silver")
        e += B(2, 25, 3, 14, 25.5, 14, "glass", n="silver")
        e += rbox(3, 2, 4, 7, 4.5, 8, "frosting_pink", r=0.5)
        e += B(3.5, 4.5, 4.5, 6.5, 5, 7.5, "frosting_white")
        e += B(4.75, 5, 5.75, 5.25, 5.5, 6.25, "cherry")
        e += B(9, 2, 9, 11, 7, 11, "milk")
        e += B(9.25, 7, 9.25, 10.75, 7.5, 10.75, "baby_blue_cap")
        for x, z in ((4, 9.5), (5.5, 10.5), (6.5, 9.25)):
            e += B(x, 8.5, z, x + 1.25, 9.75, z + 1.25, "strawberry")
            e += B(x + 0.25, 9.75, z + 0.25, x + 1, 10, z + 1, "stem")
        e += B(9, 8.5, 5, 12.5, 11.5, 8, "glass_mint")
        e += B(4, 14.5, 5, 7.5, 16, 8.5, "porcelain")
        e += B(4.25, 16, 5.25, 7.25, 16.5, 8.25, "frosting_lavender")
        e += B(9.5, 14.5, 8, 12.5, 18, 11, "glass_pink")
        e += B(4, 21.25, 5, 12, 23.5, 12, "frost")
        e += B(4, 25.5, 6, 7, 27, 10, "frosting_white")
        for hinge_y0, hinge_y1, depth in ((1.5, 20.75, 13), (21.5, 30.25, 13)):
            d = []
            d += B(0.5, hinge_y0, 1.5 - depth, 1.5, hinge_y1, 1.5, "ceramic", e="porcelain")
            d += B(1.5, hinge_y0 + 1.5, -10.5, 3, hinge_y0 + 2, 0, "porcelain")
            d += B(1.5, hinge_y0 + 2, -10.5, 2.75, hinge_y0 + 4.5, -10, "porcelain")
            d += B(1.75, hinge_y0 + 2, -9, 2.75, hinge_y0 + 4, -8, "glass_pink")
            d += B(1.75, hinge_y0 + 2, -6.5, 2.75, hinge_y0 + 4.5, -5.5, "glass_lavender")
            d += B(-0.5, hinge_y1 - 6, -10.5, 0.5, hinge_y1 - 2, -9.5, "silver")
            e += d
    return e


def dining_table_top():
    return B(0, 13, 0, 16, 14, 16, "paint")


def dining_table_skirt():
    e = B(0, 11, 0.75, 16, 13, 1.75, "paint")
    e += B(0, 10, 0.6, 16, 11.5, 0.75, LACE_HEM, s=None, u=None, d=None, e=None, w=None)
    return e


def dining_table_leg():
    e = []
    e += B(1.25, 1, 1.25, 2.75, 10, 2.75, "paint")
    e += B(1, 3, 1, 3, 3.75, 3, "trim")
    e += B(1, 7, 1, 3, 7.75, 3, "trim")
    e += B(0.75, 10, 0.75, 3.25, 13, 3.25, "paint")
    e += B(0.75, 0, 0.75, 3.25, 1, 3.25, "gold")
    return e


def dining_chair():
    e = []
    leg = B(2.5, 0.5, 2.5, 3.75, 8, 3.75, "paint") + B(2.25, 0, 2.25, 4, 0.5, 4, "gold")
    e += sym(leg)
    e += sym(B(2.5, 0.5, 12.25, 3.75, 8, 13.5, "paint") + B(2.25, 0, 12, 4, 0.5, 13.75, "gold"))
    e += B(2, 8, 2, 14, 9, 14, "paint")
    e += rbox(2.5, 9, 2.25, 13.5, 10.5, 12.25, "fabric", r=0.5, u="quilt")
    e += B(2.5, 7.5, 2.15, 13.5, 8.75, 2.25, LACE_HEM, s=None, u=None, d=None, e=None, w=None)
    e += sym(B(2.5, 9, 12.25, 3.75, 21, 13.5, "paint") + B(2.4, 21, 12.15, 3.85, 21.5, 13.6, "gold")
             + B(2.65, 21.5, 12.4, 3.6, 22.25, 13.35, "gold"))
    e += heart(8, 10.75, 12.65, 13.35, 0.98, "paint")
    e += heart(8, 11.5, 12.35, 13.1, 0.82, "fabric", s=None)
    e += B(3.75, 20, 12.5, 12.25, 21, 13.25, "paint")
    e += bow(8, 19.25, 11.95, 12.4, 0.6)
    return e


def tea_set():
    e = []
    e += B(2, 0, 3, 14, 0.5, 13, "silver")
    e += ring(2, 3, 14, 13, 0.5, 1, 0.4, "silver")
    e += B(1, 0.25, 7, 2, 0.75, 9, "silver") + B(14, 0.25, 7, 15, 0.75, 9, "silver")
    e += rbox(3.75, 0.5, 5.75, 9.25, 5, 11.25, "ceramic", r=1)
    e += B(3.65, 2.75, 5.65, 9.35, 3.25, 11.35, "gold", u=None, d=None)
    e += B(5, 5, 7, 8, 5.75, 10, "ceramic")
    e += B(6, 5.75, 8, 7, 6.5, 9, "gold")
    e += B(9, 1.75, 8, 11.5, 2.75, 9, "ceramic", rot=("z", 45, (9, 2.25, 8.5)))
    e += B(2.75, 1.5, 8, 3.75, 2, 9, "ceramic")
    e += B(2.5, 1.75, 8, 3, 4, 9, "ceramic")
    e += B(2.75, 3.75, 8, 3.75, 4.25, 9, "ceramic")
    e += heart(6.5, 1.25, 5.6, 5.75, 0.22, "bow")
    for z in (3.5, 8.75):
        e += B(9.75, 0.5, z - 0.25, 13.25, 0.75, z + 3.25, "porcelain")
        e += B(10.25, 0.75, z + 0.25, 12.75, 2.5, z + 2.75, "ceramic", u="tea")
        e += B(12.75, 1.25, z + 1.25, 13.25, 2, z + 1.75, "ceramic")
    e += B(4.25, 0.5, 3.5, 5.75, 1, 5, "frosting_pink")
    e += B(4.25, 1, 3.5, 5.75, 1.25, 5, "frosting_white")
    e += B(4.25, 1.25, 3.5, 5.75, 1.75, 5, "frosting_pink")
    return e


# =========================================================================== bathroom

def bathtub(filled):
    e = []
    for x in (1.5, 12.5):
        for z in (3, 27):
            e += B(x, 0, z, x + 2, 2, z + 2, "gold")
            e += B(x - 0.25, 0, z - 0.5, x + 2.25, 0.75, z + 0.25, "gold")
    e += B(2, 1.5, 2, 14, 2, 30, "ceramic")
    e += B(1, 2, 1, 2.5, 13, 31, "ceramic", e="porcelain")
    e += B(13.5, 2, 1, 15, 13, 31, "ceramic", w="porcelain")
    e += B(2.5, 2, 1, 13.5, 13, 2.5, "ceramic", s="porcelain")
    e += B(2.5, 2, 29.5, 13.5, 13, 31, "ceramic", n="porcelain")
    e += B(2.5, 2, 2.5, 13.5, 3.5, 29.5, "porcelain")
    e += B(0.75, 13, 0.75, 2.75, 13.75, 31.25, "porcelain")
    e += B(13.25, 13, 0.75, 15.25, 13.75, 31.25, "porcelain")
    e += B(2.75, 13, 29.25, 13.25, 13.75, 31.25, "porcelain")
    e += B(2.5, 13, 0.75, 13.5, 16, 2.5, "ceramic", s="porcelain")
    e += B(0.75, 13.75, 0.75, 2.75, 15, 3, "ceramic")
    e += B(13.25, 13.75, 0.75, 15.25, 15, 3, "ceramic")
    e += B(3.5, 16, 1, 12.5, 17, 2.25, "ceramic")
    e += B(2.5, 16, 0.75, 13.5, 16.5, 2.75, "porcelain")
    e += B(0.9, 6.5, 1, 1, 7, 31, "gold", n=None, s=None, u=None, d=None, e=None)
    e += B(15, 6.5, 1, 15.1, 7, 31, "gold", n=None, s=None, u=None, d=None, w=None)
    e += side_bow(0.4, 0.9, 6.9, 16, 0.85)
    e += mirror_x(side_bow(0.4, 0.9, 6.9, 16, 0.85))
    e += B(7, 13, 29.75, 9, 16.5, 31.25, "gold")
    e += B(7.5, 15.5, 27.5, 8.5, 16.5, 29.75, "gold")
    e += B(7.5, 14.75, 27.5, 8.5, 15.5, 28.25, "gold")
    e += B(4.5, 13.5, 29.75, 6, 15, 31.25, "porcelain") + B(10, 13.5, 29.75, 11.5, 15, 31.25, "porcelain")
    e += B(4.75, 15, 30, 5.75, 15.5, 31, "gold") + B(10.25, 15, 30, 11.25, 15.5, 31, "gold")
    if filled:
        e += B(2.5, 3.5, 2.5, 13.5, 10.5, 29.5, "water", d=None, n=None, s=None, e=None, w=None)
        for x0, z0, x1, z1 in ((3, 4, 8, 9), (9, 20, 13, 26.5), (3.5, 14, 7.5, 18.5), (8.5, 6, 12, 8.5)):
            e += B(x0, 10.5, z0, x1, 10.75, z1, "foam", n=None, s=None, e=None, w=None, d=None)
            e += B(x0 + 0.75, 10.75, z0 + 0.75, x1 - 0.75, 11.25, z1 - 0.75, "foam", d=None)
        e += B(9.75, 10.25, 12, 11.75, 11.75, 14, "yellow")
        e += B(10.25, 11.75, 11.75, 11.25, 12.75, 12.75, "yellow")
        e += B(10.5, 12, 11.25, 11, 12.4, 11.75, "orange")
        e += B(10.15, 12.25, 12, 10.25, 12.5, 12.25, "eye", n=None, s=None, u=None, d=None, e=None)
        e += B(11.25, 12.25, 12, 11.35, 12.5, 12.25, "eye", n=None, s=None, u=None, d=None, w=None)
    return e


def bathroom_sink(running):
    e = []
    e += B(5, 0, 5.5, 11, 1.5, 11.5, "ceramic")
    e += B(6, 1.5, 6.5, 10, 2.5, 10.5, "ceramic")
    e += B(6.5, 2.5, 7, 9.5, 9, 10, "ceramic")
    e += B(6, 5, 6.5, 10, 5.5, 10.5, "gold")
    e += B(5.5, 9, 6, 10.5, 10, 11, "ceramic")
    e += B(3, 9.5, 4, 13, 10, 13, "ceramic")
    e += B(2.5, 10, 3, 13.5, 13.5, 4, "ceramic", s="porcelain")
    e += B(2.5, 10, 13, 13.5, 13.5, 14, "ceramic", n="porcelain")
    e += B(2, 10, 3, 3, 13.5, 14, "ceramic", e="porcelain")
    e += B(13, 10, 3, 14, 13.5, 14, "ceramic", w="porcelain")
    e += B(3, 10, 4, 13, 11, 13, "porcelain")
    e += B(1.75, 13.5, 2.75, 14.25, 14, 4, "porcelain")
    e += B(1.75, 13.5, 13, 14.25, 14, 14.25, "porcelain")
    e += B(1.75, 13.5, 4, 3, 14, 13, "porcelain")
    e += B(13, 13.5, 4, 14.25, 14, 13, "porcelain")
    e += B(7.25, 11, 8, 8.75, 11.05, 9.5, "dark_metal", n=None, s=None, e=None, w=None, d=None)
    e += B(2, 13.5, 14, 14, 16, 15.5, "ceramic")
    e += B(4, 16, 14.25, 12, 16.75, 15.25, "ceramic")
    e += B(6, 16.75, 14.5, 10, 17.25, 15, "ceramic")
    e += heart(8, 14.25, 13.85, 14, 0.25, "gold")
    e += B(7, 14, 12, 9, 14.5, 13.5, "gold")
    e += B(7.5, 14.5, 12.25, 8.5, 17, 13.25, "gold")
    e += B(7.5, 16.25, 9, 8.5, 17, 12.25, "gold")
    e += B(7.5, 15.5, 9, 8.5, 16.25, 9.75, "gold")
    for x in (4.5, 10.25):
        e += B(x, 14, 12, x + 1.25, 15, 13.25, "porcelain")
        e += B(x + 0.25, 15, 12.25, x + 1, 15.5, 13, "ceramic")
    if running:
        e += B(7.7, 11, 9.1, 8.3, 15.5, 9.7, "water_flow", shade=False)
        e += B(3, 11.5, 4, 13, 11.75, 13, "water", n=None, s=None, e=None, w=None, d=None)
        e += B(6, 11.75, 7.5, 10, 11.85, 10.5, "foam", n=None, s=None, e=None, w=None, d=None)
    return e


def wall_mirror():
    e = []
    frame = [(1, 2, 5, 11), (2, 3, 3.5, 12.5), (3, 13, 2.5, 13.5), (13, 14, 3.5, 12.5), (14, 15, 5, 11)]
    for y0, y1, x0, x1 in frame:
        e += B(x0, y0, 14.75, x1, y1, 16, "paint")
    e += B(4, 0.5, 15, 12, 1, 16, "trim")
    glass = [(2, 3, 6, 10), (3, 4, 4.5, 11.5), (4, 12, 3.5, 12.5), (12, 13, 4.5, 11.5), (13, 14, 6, 10)]
    for y0, y1, x0, x1 in glass:
        e += B(x0, y0, 14.5, x1, y1, 14.75, "mirror", s=None, u=None, d=None)
    for x, y in ((3, 7.5), (13, 7.5)):
        e += B(x - 0.5, y, 14.35, x + 0.5, y + 1, 14.75, "gold")
    e += bow(8, 13.6, 14.1, 14.6, 0.75)
    e += heart(8, 1.25, 14.6, 14.75, 0.2, "gold")
    return e


def bathroom_cabinet(open_):
    e = []
    e += B(2, 2, 15, 14, 14, 16, "paint", n="panel")
    e += B(2, 2, 11, 3, 14, 15, "paint", e="panel")
    e += B(13, 2, 11, 14, 14, 15, "paint", w="panel")
    e += B(3, 13, 11, 13, 14, 15, "paint", d="panel")
    e += B(3, 2, 11, 13, 3, 15, "paint", u="panel")
    e += B(1.5, 14, 10.5, 14.5, 15, 16, "trim")
    e += B(3, 15, 11, 13, 15.5, 16, "paint")
    e += heart(8, 14.1, 10.25, 10.5, 0.3, "gold")
    e += B(2.5, 1, 11.5, 13.5, 2, 16, "trim")
    e += scallops(3, 13, 1, 11.75, 12, 5, "trim")
    e += B(3, 7.75, 11.25, 13, 8.25, 15, "panel")
    e += B(4, 3, 12, 5.5, 6, 13.5, "glass_pink") + B(4.25, 6, 12.25, 5.25, 6.75, 13.25, "gold")
    e += B(7, 3, 12.5, 8.5, 5.25, 14, "glass_mint") + B(7.25, 5.25, 12.75, 8.25, 5.75, 13.75, "silver")
    e += B(10, 8.25, 12, 11.5, 11, 13.5, "glass_lavender") + B(10.25, 11, 12.25, 11.25, 11.75, 13.25, "gold")
    e += B(5, 8.25, 12.5, 7.25, 9.75, 14.5, "porcelain") + B(5.25, 9.75, 12.75, 7, 10.25, 14.25, "frosting_pink")
    if not open_:
        e += B(2.5, 2.5, 10.5, 13.5, 13.5, 11, "paint")
        e += B(3.5, 3.5, 10.4, 12.5, 11.25, 10.5, "mirror", s=None, u=None, d=None)
        e += B(4.5, 11.25, 10.4, 11.5, 12.25, 10.5, "mirror", s=None, u=None, d=None)
        e += B(6, 12.25, 10.4, 10, 12.75, 10.5, "mirror", s=None, u=None, d=None)
        e += knob(12.75, 8, 10.5, 0.8)
    else:
        e += B(2, 2.5, -0.5, 2.5, 13.5, 10.5, "paint")
        e += B(1.9, 3.5, 0.5, 2, 11.25, 9.5, "mirror", n=None, s=None, u=None, d=None, e=None)
        e += B(1.25, 7.5, 0, 1.9, 8.5, 0.75, "gold")
    return e


def towel_rack():
    e = []
    e += B(1.5, 10, 13.5, 2.5, 11, 16, "gold") + B(13.5, 10, 13.5, 14.5, 11, 16, "gold")
    e += B(1, 9.75, 15.5, 3, 11.25, 16, "gold") + B(13, 9.75, 15.5, 15, 11.25, 16, "gold")
    e += B(1.5, 10, 12.5, 14.5, 11, 13.5, "gold")
    e += B(3.5, 2.5, 11.75, 12.5, 11, 12.5, "fabric")
    e += B(3.5, 4.25, 11.6, 12.5, 5.25, 11.75, "trim", s=None, u=None, d=None, e=None, w=None)
    e += B(3.5, 1.5, 11.6, 12.5, 2.75, 11.75, LACE_HEM, s=None, u=None, d=None, e=None, w=None)
    e += B(3.5, 11, 11.75, 12.5, 11.75, 14, "fabric")
    e += B(3.5, 6, 13.5, 12.5, 11, 14, "fabric")
    e += heart(8, 6.25, 11.6, 11.75, 0.3, "bow")
    e += B(3.5, 8.5, 11.5, 12.5, 9, 11.75, "stripe", s=None, u=None, d=None)
    return e


def soap_dispenser():
    e = []
    e += rbox(6, 0, 6, 10, 5.5, 10, "ceramic", r=0.75)
    e += B(5.95, 1.5, 5.95, 10.05, 3.75, 10.05, "porcelain", u=None, d=None)
    e += heart(8, 2.05, 5.8, 5.95, 0.17, "bow")
    e += B(7, 5.5, 7, 9, 6.25, 9, "gold")
    e += B(7.5, 6.25, 7.5, 8.5, 7.75, 8.5, "silver")
    e += B(7, 7.75, 6.5, 9, 8.5, 9, "silver")
    e += B(7.5, 7.75, 4.75, 8.5, 8.25, 6.5, "silver")
    e += rbox(10.25, 0, 9.75, 13.5, 1.25, 12.25, "panel", r=0.4)
    e += B(11.25, 1.25, 10.5, 12.5, 1.35, 11.5, "foam", d=None)
    return e


# =========================================================================== garden

def garden_bench(shape):
    arm_l = shape in ("single", "left")
    arm_r = shape in ("single", "right")
    sx0 = 1.25 if arm_l else 0
    sx1 = 14.75 if arm_r else 16
    e = []
    for z0 in (2.75, 5.25, 7.75, 10.25):
        e += B(sx0, 6.5, z0, sx1, 7.5, z0 + 2, "paint")
    e += B(sx0, 5.5, 3.5, sx1, 6.5, 4.5, "trim")
    e += B(sx0, 5.5, 11, sx1, 6.5, 12, "trim")
    e += B(sx0, 9, 12.5, sx1, 10.5, 13.5, "paint")
    e += B(sx0, 11.5, 12.75, sx1, 13, 13.75, "paint")
    e += B(sx0, 14, 13, sx1, 15.5, 14, "paint")
    e += B(sx0, 15.5, 12.75, sx1, 16.5, 14.5, "trim")
    if shape == "single":
        e += heart(8, 16.5, 13.25, 14, 0.35, "trim")
    for has, flip in ((arm_l, False), (arm_r, True)):
        if not has:
            continue
        end = []
        end += B(0.5, 0, 3, 1.5, 10, 4, "trim")
        end += B(0.5, 0, 12.5, 1.5, 17, 13.75, "trim")
        end += B(0.25, 10, 2.5, 1.75, 11, 13, "trim")
        end += B(0.25, 9, 2, 1.75, 10.5, 3, "trim")
        end += B(0.5, 2, 4, 1.5, 3, 12.5, "trim")
        end += B(0.25, 0, 2.75, 1.75, 0.5, 4.25, "gold")
        end += B(0.25, 0, 12.25, 1.75, 0.5, 14, "gold")
        hz = rot_y90(heart(8, 0, 7.75, 8.25, 0.42, "trim"), 1)
        end += move(hz, 1 - 8, 3.5, 8 - 8)
        e += mirror_x(end) if flip else end
    return e


def flower_pot():
    e = []
    e += B(5, 0, 5, 11, 1, 11, "ceramic")
    e += B(4.5, 1, 4.5, 11.5, 5, 11.5, "ceramic", u=None)
    e += ring(4, 4, 12, 12, 5, 6, 1, "ceramic")
    e += B(4.75, 5, 4.75, 11.25, 5.5, 11.25, "soil", n=None, s=None, e=None, w=None, d=None)
    e += heart(8, 1.6, 4.35, 4.5, 0.3, "porcelain")
    e += cross_plane(3, 5.25, 13, 14.25, "flowers")
    for x, y, z, tex in ((6, 11.5, 6.5, "frosting_pink"), (9.25, 12.5, 8.75, "porcelain"), (7.5, 10, 9.75, "frosting_lavender")):
        e += B(x, y, z, x + 1.25, y + 1, z + 1.25, tex)
        e += B(x + 0.375, y + 1, z + 0.375, x + 0.875, y + 1.2, z + 0.875, "yellow")
    return e


def birdhouse():
    e = []
    e += B(6, 0, 6, 10, 0.75, 10, "trim")
    e += B(7, 0.75, 7, 9, 7, 9, "trim")
    e += cross_plane(5.5, 0.75, 10.5, 5, "flowers")
    e += B(4, 6.75, 4.5, 12, 7.25, 11.5, "trim")
    e += B(4.5, 7.25, 5, 11.5, 13, 11, "paint")
    e += B(7, 9, 4.9, 9, 11, 5, "dark_metal", s=None, u=None, d=None, e=None, w=None)
    e += B(6.75, 8.75, 4.85, 9.25, 9, 5, "trim") + B(6.75, 11, 4.85, 9.25, 11.25, 5, "trim")
    for y0, y1, x0, x1 in ((13, 14, 3.5, 12.5), (14, 15, 4.5, 11.5), (15, 16, 5.5, 10.5), (16, 17, 6.5, 9.5), (17, 17.75, 7.25, 8.75)):
        e += B(x0, y0, 4, x1, y1, 12, "trim")
    e += heart(8, 13.6, 3.85, 4, 0.25, "bow")
    e += B(7.75, 8, 3, 8.25, 8.5, 5, "gold")
    e += B(7.25, 8.5, 3, 8.75, 9.75, 4.5, "bird")
    e += B(7.4, 9.75, 3.1, 8.6, 10.75, 4.1, "bird")
    e += B(7.8, 10, 2.6, 8.2, 10.3, 3.1, "yellow")
    e += B(7.35, 10.2, 3.2, 7.4, 10.45, 3.45, "eye", n=None, s=None, u=None, d=None, e=None)
    e += B(8.6, 10.2, 3.2, 8.65, 10.45, 3.45, "eye", n=None, s=None, u=None, d=None, w=None)
    e += B(7.5, 9, 4.5, 8.5, 9.5, 5.5, "bird")
    return e


def fence_post():
    e = []
    e += B(6.5, 0, 6.5, 9.5, 14, 9.5, "paint")
    e += B(7, 14, 7, 9, 15, 9, "paint")
    e += B(7.5, 15, 7.5, 8.5, 15.75, 8.5, "paint")
    e += B(6.25, 12, 6.25, 9.75, 12.5, 9.75, "trim")
    return e


def fence_side():
    e = []
    e += B(7.25, 4, 0, 8.75, 5.5, 6.5, "trim")
    e += B(7.25, 10, 0, 8.75, 11.5, 6.5, "trim")
    e += B(7, 1, 1.5, 9, 12.5, 4.5, "paint")
    e += B(7, 12.5, 2, 9, 13.25, 4, "paint")
    e += B(7, 13.25, 2.5, 9, 13.75, 3.5, "paint")
    e += heart(8, 6.5, 2.4, 3.6, 0.2, "bow") if False else []
    return e


def fence_inventory():
    return fence_post() + fence_side() + mirror_z(fence_side())


def outdoor_table():
    e = []
    e += B(1, 13, 3, 15, 14, 13, "paint")
    e += B(3, 13, 1, 13, 14, 15, "paint")
    e += B(2, 13, 2, 14, 14, 14, "paint")
    e += B(2, 12.5, 4, 14, 13, 12, "trim", u=None) + B(4, 12.5, 2, 12, 13, 14, "trim", u=None)
    e += B(7, 2, 7, 9, 12.5, 9, "trim")
    e += B(6.5, 11.5, 6.5, 9.5, 12.5, 9.5, "trim")
    e += B(6.5, 5, 6.5, 9.5, 6, 9.5, "gold")
    foot = B(7.25, 0, 1.5, 8.75, 1.5, 7, "trim") + B(7.25, 1.5, 1.5, 8.75, 2.5, 2.5, "trim")
    foot += B(7, 0, 1.25, 9, 0.5, 2.75, "gold")
    arm = B(7.25, 9, 3, 8.75, 10, 7, "trim") + B(7.25, 10, 3, 8.75, 12.5, 4, "trim")
    for t in range(4):
        e += rot_y90(foot + arm, t)
    return e


def picnic_center():
    return B(0, 0, 0, 16, 0.5, 16, "gingham")


def picnic_edge():
    e = B(0, 0, 0, 16, 0.75, 1.5, "sheet", d=None)
    for i in range(4):
        e += B(i * 4 + 1, 0, -0.75, i * 4 + 3, 0.5, 0, "sheet")
        e += B(i * 4 + 1.5, 0, -1.1, i * 4 + 2.5, 0.5, -0.75, "sheet")
    return e


def swing_seat():
    e = []
    e += B(1, 5.5, 4, 15, 7, 12, "paint")
    e += B(1.5, 7, 4.5, 14.5, 8.5, 11, "fabric", u="quilt")
    e += B(1.5, 6.75, 4.35, 14.5, 7.75, 4.5, LACE_HEM, s=None, u=None, d=None, e=None, w=None)
    e += B(1, 8.5, 11.5, 15, 10, 12.5, "paint")
    e += B(1, 11, 11.75, 15, 12.5, 12.75, "paint")
    e += B(1, 13.5, 12, 15, 15, 13, "paint")
    e += heart(8, 8.5, 10.5, 11.5, 0.6, "print")
    e += B(1, 8.5, 4.5, 2.5, 9.5, 11.5, "paint") + B(13.5, 8.5, 4.5, 15, 9.5, 11.5, "paint")
    for x in (1.5, 13.75):
        for z in (4.75, 11):
            e += B(x, 7, z, x + 0.75, 31.5, z + 0.75, "rope")
            e += B(x - 0.25, 31, z - 0.25, x + 1, 32, z + 1, "gold")
            for i, y in enumerate((10.5, 15, 19.5, 24, 28.5)):
                tex = ("frosting_pink", "porcelain", "frosting_lavender")[(i + int(z)) % 3]
                e += B(x - 0.4, y, z - 0.4, x + 1.15, y + 0.9, z + 1.15, tex)
                e += B(x + 0.75, y - 1, z - 0.2, x + 1.25, y, z + 0.5, "stem")
    return e
