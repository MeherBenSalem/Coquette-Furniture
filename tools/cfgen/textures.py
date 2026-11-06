"""Texture painters. Every block texture is 16x16 so all furniture keeps vanilla's
texel density; colour textures are painted once per colourway and shared by every
model through texture variables."""
import math
import random

from .core import ASSETS, C, Img, darken, lighten, mix, scale, with_alpha

WHITE = C("#FFFFFF")

COLORS = ["baby_pink", "white", "cream", "lavender", "mint", "baby_blue"]

PALETTES = {
    "baby_pink": dict(main="#F7B8CF", trim="#EC98B8", light="#FDE3EE", deep="#D7739B", bow="#FFFFFF", sheer="#FFF0F5"),
    "white": dict(main="#F8F4F1", trim="#E6DCD5", light="#FFFFFF", deep="#C7B6AC", bow="#F5A6C4", sheer="#FFFFFF"),
    "cream": dict(main="#F5E5C6", trim="#E6CD9F", light="#FFF7E7", deep="#C49F6B", bow="#F2A3BA", sheer="#FFFBF2"),
    "lavender": dict(main="#CFBDF2", trim="#B6A0E5", light="#F0E9FE", deep="#9176CB", bow="#FFFFFF", sheer="#F8F3FF"),
    "mint": dict(main="#B6EAD2", trim="#91D7B8", light="#E8FAF2", deep="#5FB690", bow="#F7B8CF", sheer="#F3FCF8"),
    "baby_blue": dict(main="#B9DAF7", trim="#94C2EB", light="#E8F3FE", deep="#6A9ED2", bow="#FFFFFF", sheer="#F3F9FF"),
}

HEART = [".X.X.", "XXXXX", "XXXXX", ".XXX.", "..X.."]
SMALL_HEART = ["X.X", "XXX", ".X."]


def rng_for(*parts):
    return random.Random("|".join(str(p) for p in parts))


def noise_fill(img, base, rng, amount=0.03, grain=0.0, rib=0.0, sheen=0.0):
    for y in range(img.h):
        for x in range(img.w):
            f = 1.0 + (rng.random() - 0.5) * amount * 2
            if grain:
                f += grain * math.sin(x * 0.9 + math.sin(y * 0.35 + x) * 1.4)
            if rib:
                f += rib if x % 2 == 0 else -rib * 0.6
            if sheen:
                f += sheen * math.sin((x + y) * 0.45)
            img.set(x, y, scale(base, f))


# --------------------------------------------------------------------------- colour materials

def paint(base, seed):
    img = Img(16, 16)
    noise_fill(img, base, rng_for("paint", seed), 0.02, grain=0.016)
    return img


def fabric(base, seed):
    img = Img(16, 16)
    noise_fill(img, base, rng_for("fabric", seed), 0.035, rib=0.012, sheen=0.02)
    return img


def quilt(pal, seed):
    base = C(pal["main"])
    img = Img(16, 16)
    rng = rng_for("quilt", seed)
    for y in range(16):
        for x in range(16):
            a = (x + y) % 8
            b = (x - y) % 8
            da = min(a, 8 - a)
            db = min(b, 8 - b)
            d = min(da, db)
            f = {0: 0.84, 1: 0.95}.get(d, 1.0 + 0.025 * (d - 1))
            f += (rng.random() - 0.5) * 0.04
            img.set(x, y, scale(base, f))
    button = mix(pal["deep"], pal["main"], 0.25)
    for bx, by in ((0, 0), (8, 0), (4, 4), (12, 4), (0, 8), (8, 8), (4, 12), (12, 12)):
        img.set(bx, by, button)
        img.set((bx + 1) % 16, by, scale(button, 1.08))
    return img


def print_fabric(pal, seed):
    img = Img(16, 16)
    base = lighten(pal["main"], 0.42)
    noise_fill(img, base, rng_for("print", seed), 0.02, rib=0.008)
    heart = mix(pal["main"], pal["deep"], 0.35)
    if seed == "white":
        heart = C("#F4B3CB")
    img.stamp(1, 2, HEART, {"X": heart})
    img.stamp(9, 10, HEART, {"X": heart})
    dot = WHITE if seed != "white" else C("#E6DCD5")
    for x, y in ((11, 3), (3, 11), (13, 7), (6, 14)):
        img.set(x, y, dot)
    return img


def gingham(pal, seed):
    img = Img(16, 16)
    rng = rng_for("gingham", seed)
    main = C(pal["main"]) if seed != "white" else C("#F5BCD1")
    for y in range(16):
        for x in range(16):
            a = (x // 4) % 2
            b = (y // 4) % 2
            if a and b:
                c = darken(main, 0.04)
            elif a or b:
                c = mix(main, WHITE, 0.5)
            else:
                c = C("#FFFDFC")
            img.set(x, y, scale(c, 1 + (rng.random() - 0.5) * 0.03))
    return img


def stripe(pal, seed):
    img = Img(16, 16)
    rng = rng_for("stripe", seed)
    main = C(pal["main"]) if seed != "white" else C("#F7C3D6")
    for y in range(16):
        for x in range(16):
            c = main if x % 4 < 2 else C("#FFFDFC")
            img.set(x, y, scale(c, 1 + (rng.random() - 0.5) * 0.03))
    return img


def ceramic(base, seed):
    img = Img(16, 16)
    rng = rng_for("ceramic", seed)
    for y in range(16):
        for x in range(16):
            c = scale(base, 1.05 - y * 0.007 + (rng.random() - 0.5) * 0.015)
            if x in (3, 4):
                c = lighten(c, 0.28 if x == 3 else 0.14)
            img.set(x, y, c)
    return img


def shade(pal, seed, lit):
    img = Img(16, 16)
    rng = rng_for("shade", seed, lit)
    base = C(pal["main"])
    if lit:
        base = lighten(mix(base, "#FFF1C2", 0.35), 0.35)
    for y in range(16):
        for x in range(16):
            f = (0.9, 1.04, 1.0)[x % 3]
            if lit:
                f = 1 + (f - 1) * 0.4
            img.set(x, y, scale(base, f + (rng.random() - 0.5) * 0.02))
    return img


def rug(pal, seed):
    img = Img(16, 16)
    rng = rng_for("rug", seed)
    base = lighten(pal["main"], 0.3)
    noise_fill(img, base, rng, 0.03, rib=0.015)
    petal = C(pal["deep"]) if seed != "white" else C("#F2A8C4")
    centre = C("#FFF4C9")
    for cx, cy in ((4, 4), (12, 12)):
        for dx, dy in ((0, -1), (0, 1), (-1, 0), (1, 0)):
            img.set(cx + dx, cy + dy, petal)
        img.set(cx, cy, centre)
    for x, y in ((12, 4), (4, 12)):
        img.set(x, y, WHITE)
        img.set(x + 1, y, mix(base, WHITE, 0.5))
    return img


def rug_edge(pal, seed):
    img = Img(16, 16)
    rng = rng_for("rug_edge", seed)
    deep = C(pal["deep"]) if seed != "white" else C("#F2A8C4")
    for y in range(16):
        for x in range(16):
            if y == 0:
                c = darken(deep, 0.05)
            elif y == 1:
                c = deep
            elif y == 2:
                c = WHITE if x % 2 == 0 else C("#FBEFF3")
            else:
                c = C(pal["main"])
            img.set(x, y, scale(c, 1 + (rng.random() - 0.5) * 0.03))
    return img


def satin(base, seed):
    img = Img(16, 16)
    rng = rng_for("satin", seed)
    for y in range(16):
        for x in range(16):
            f = 1.0 + 0.06 * math.sin(y * 0.8 + x * 0.15) + (rng.random() - 0.5) * 0.02
            img.set(x, y, scale(base, f))
    return img


def sheer(pal, seed):
    img = Img(16, 16)
    base = C(pal["sheer"])
    dot = mix(pal["sheer"], pal["main"], 0.55)
    for y in range(16):
        for x in range(16):
            img.set(x, y, scale(base, 1.0 if (x + y) % 2 else 0.985))
    for y in range(1, 15, 4):
        for x in range((y // 4) % 2 * 2, 16, 4):
            img.set(x, y, dot)
    # scalloped hem
    for x in range(16):
        if x % 4 == 3:
            img.set(x, 15, (0, 0, 0, 0))
        img.set(x, 14, mix(base, pal["main"], 0.25))
    return img


def colour_textures():
    for col in COLORS:
        pal = PALETTES[col]
        out = ASSETS / "textures" / "block" / col
        paint(C(pal["main"]), col).save(out / "paint.png")
        paint(C(pal["trim"]), col + "t").save(out / "trim.png")
        paint(C(pal["light"]), col + "l").save(out / "panel.png")
        fabric(C(pal["main"]), col).save(out / "fabric.png")
        fabric(mix(pal["deep"], pal["main"], 0.35), col + "d").save(out / "lining.png")
        quilt(pal, col).save(out / "quilt.png")
        print_fabric(pal, col).save(out / "print.png")
        gingham(pal, col).save(out / "gingham.png")
        stripe(pal, col).save(out / "stripe.png")
        ceramic(C(pal["main"]), col).save(out / "ceramic.png")
        shade(pal, col, False).save(out / "shade.png")
        shade(pal, col, True).save(out / "shade_lit.png")
        rug(pal, col).save(out / "rug.png")
        rug_edge(pal, col).save(out / "rug_edge.png")
        satin(C(pal["bow"]), col).save(out / "bow.png")
        sheer(pal, col).save(out / "sheer.png")


# --------------------------------------------------------------------------- neutral materials

def gradient(top, bottom, seed, sparkle=None, noise=0.02):
    img = Img(16, 16)
    rng = rng_for("grad", seed)
    for y in range(16):
        for x in range(16):
            c = mix(top, bottom, y / 15)
            c = scale(c, 1 + (rng.random() - 0.5) * noise)
            if sparkle and (x * 7 + y * 3) % 23 == 0:
                c = lighten(c, sparkle)
            img.set(x, y, c)
    return img


def gold():
    img = gradient("#FFE6A0", "#D8A443", "gold", sparkle=0.45)
    for i in range(16):
        x = (i + 5) % 16
        img.set(x, i, lighten(img.get(x, i), 0.22))
    return img


def silver():
    img = gradient("#F6F8FB", "#AEB8C6", "silver", sparkle=0.5)
    for y in range(16):
        img.set(4, y, lighten(img.get(4, y), 0.35))
    return img


def lace():
    img = Img(16, 16)
    w = C("#FFFDFB")
    s = C("#F3ECEE")
    for y in range(12):
        for x in range(16):
            hole = (x + y) % 4 == 0 and (x - y) % 4 == 0
            if not hole:
                img.set(x, y, w if (x + y) % 2 else s)
    for x in range(16):
        img.set(x, 12, w)
        if x % 3 != 1:
            img.set(x, 13, s)
        img.set(x, 14, w)
        if x % 4 in (1, 2):
            img.set(x, 15, w)
    return img


def sheet():
    img = Img(16, 16)
    noise_fill(img, C("#FFFFFF"), rng_for("sheet"), 0.02, rib=0.012)
    img.map(lambda x, y, c: mix(c, "#EEF0F8", 0.25) if (x + 2 * y) % 11 == 0 else c)
    return img


def glass():
    img = Img(16, 16)
    streak = C("#FFFFFF", 230)
    soft = C("#E6F4FF", 200)
    for i, (x, y) in enumerate(((3, 1), (2, 2), (1, 3), (12, 8), (11, 9), (10, 10), (9, 11), (13, 7))):
        img.set(x, y, streak if i % 2 == 0 else soft)
    for x in range(16):
        img.set(x, 0, C("#F2FAFF", 150))
        img.set(x, 15, C("#F2FAFF", 150))
    for y in range(16):
        img.set(0, y, C("#F2FAFF", 150))
        img.set(15, y, C("#F2FAFF", 150))
    return img


def mirror():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            t = (x + y) / 30
            c = mix("#F0F8FF", "#B9D4EC", t)
            d = (x + y) % 16
            if d in (5, 6) or d == 10:
                c = lighten(c, 0.55 if d != 10 else 0.35)
            img.set(x, y, c)
    return img


def water():
    img = Img(16, 16)
    rng = rng_for("water")
    for y in range(16):
        for x in range(16):
            r = math.sin(x * 0.8 + math.sin(y * 0.6) * 2.0)
            c = mix("#9BD7EE", "#D4F1FB", max(0, r) * 0.6)
            img.set(x, y, scale(c, 1 + (rng.random() - 0.5) * 0.03))
    return img


def water_flow():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            c = C("#B6E5F6") if x % 3 else C("#E4F7FD")
            if (y + x * 5) % 7 == 0:
                c = WHITE
            img.set(x, y, c)
    return img


def porcelain():
    img = Img(16, 16)
    rng = rng_for("porcelain")
    for y in range(16):
        for x in range(16):
            c = scale("#FBFAFD", 1.0 - y * 0.004 + (rng.random() - 0.5) * 0.015)
            if x == 3:
                c = WHITE
            img.set(x, y, c)
    return img


def marble():
    img = Img(16, 16)
    rng = rng_for("marble")
    noise_fill(img, C("#F8F6F7"), rng, 0.02)
    x = 2.0
    for y in range(16):
        x += rng.choice((-1, 0, 1, 1))
        img.set(int(x) % 16, y, C("#DCD5DC"))
        img.set((int(x) + 1) % 16, y, C("#ECE6EC"))
    for y in range(16):
        xx = (13 - y // 2) % 16
        img.set(xx, y, C("#EFE3EA"))
    return img


def burner(lit):
    img = Img(16, 16)
    a = C("#FF7A3D") if lit else C("#6E6670")
    b = C("#FFC07A") if lit else C("#857C88")
    bg = C("#3E3842")
    for y in range(16):
        for x in range(16):
            ring = max(abs(x - 7.5), abs(y - 7.5))
            r = int(ring)
            if r % 2 == 1 and r < 7:
                img.set(x, y, b if (x + y) % 3 == 0 else a)
            else:
                img.set(x, y, bg)
    return img


def oven(lit):
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            if lit:
                d = math.hypot(x - 7.5, y - 9) / 10
                c = mix("#FFD28A", "#F07F45", min(1, d))
            else:
                c = mix("#6C6276", "#45404D", y / 15)
            if y == 9:
                c = darken(c, 0.25)
            if (x + y) % 13 == 0 and not lit:
                c = lighten(c, 0.3)
            img.set(x, y, c)
    return img


def dark_metal():
    img = Img(16, 16)
    noise_fill(img, C("#5D5662"), rng_for("dark"), 0.05)
    return img


def pages():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            img.set(x, y, C("#FBF4E3") if y % 2 == 0 else C("#EADFC8"))
    return img


def plant_leaf_shape(img, cx, cy, rx, ry, angle, base, vein, slits=False):
    ca, sa = math.cos(angle), math.sin(angle)
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            u = dx * ca + dy * sa
            v = -dx * sa + dy * ca
            if (u / rx) ** 2 + (v / ry) ** 2 <= 1.0:
                if slits and abs(v) > ry * 0.35 and int(u + 20) % 3 == 0:
                    continue
                c = base
                if abs(v) < 0.6:
                    c = vein
                elif v > 0:
                    c = darken(base, 0.08)
                img.set(x, y, c)


def monstera():
    img = Img(16, 16)
    plant_leaf_shape(img, 8, 8, 7.6, 6.4, -0.6, C("#4FA56C"), C("#8AD39C"), slits=True)
    return img


def leaves():
    img = Img(16, 16)
    rng = rng_for("leaves")
    for _ in range(9):
        cx, cy = rng.uniform(2, 14), rng.uniform(2, 14)
        plant_leaf_shape(img, cx, cy, rng.uniform(2.2, 3.4), rng.uniform(1.2, 1.8), rng.uniform(0, 3.14),
                         C(rng.choice(("#6DC07F", "#5AAE6E", "#7ACB8A"))), C("#9BDCA8"))
    return img


def rose_bush():
    img = Img(16, 16)
    rng = rng_for("rose_bush")
    noise_fill(img, C("#5FAF6E"), rng, 0.12)
    for y in range(16):
        for x in range(16):
            if (x * 3 + y * 5) % 7 == 0:
                img.set(x, y, C("#4C9A5C"))
    for cx, cy in ((3, 3), (11, 2), (7, 8), (2, 12), (13, 11), (9, 14)):
        img.ellipse(cx + 0.5, cy + 0.5, 1.8, 1.8, C("#F58DB2"))
        img.set(cx, cy, C("#FFC6DA"))
        img.set(cx + 1, cy + 1, C("#E2709A"))
    return img


def lavender_plant():
    img = Img(16, 16)
    for x in (2, 5, 8, 11, 14):
        top = 1 + (x * 7) % 4
        for y in range(top + 6, 16):
            img.set(x, y, C("#6DAA6E"))
        for y in range(top, top + 7):
            img.set(x, y, C("#A88BE0") if y % 2 else C("#C3A9F2"))
            if y % 2 == 0:
                img.set(x - 1, y, C("#9A7CD6"))
                img.set(x + 1, y, C("#B59BEA"))
    return img


def blossom():
    img = Img(16, 16)
    rng = rng_for("blossom")
    for y in range(16):
        for x in range(16):
            c = rng.choice(("#FFC4DA", "#FFD6E5", "#F7A9C6", "#FFE6EF"))
            img.set(x, y, c)
    for x, y in ((3, 4), (10, 2), (7, 10), (13, 13), (1, 13)):
        img.set(x, y, C("#FFF6CF"))
    return img


def bark():
    img = Img(16, 16)
    rng = rng_for("bark")
    for y in range(16):
        for x in range(16):
            c = C("#8C6656") if x % 4 else C("#6E4E42")
            img.set(x, y, scale(c, 1 + (rng.random() - 0.5) * 0.12))
    return img


def soil():
    img = Img(16, 16)
    rng = rng_for("soil")
    noise_fill(img, C("#5E4536"), rng, 0.12)
    for _ in range(14):
        img.set(rng.randrange(16), rng.randrange(16), C("#7A5D4A"))
    return img


def rope():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            img.set(x, y, C("#EBD6B0") if (x + y) % 4 < 2 else C("#CBB088"))
    return img


def flowers():
    img = Img(16, 16)
    stem = C("#6AB07A")
    for x, top in ((2, 6), (5, 3), (8, 7), (11, 2), (14, 5)):
        for y in range(top + 2, 16):
            img.set(x, y, stem)
    heads = (("#F7A8C8", 2, 5), ("#FFFFFF", 5, 2), ("#CDB4F2", 8, 6), ("#FFC9DD", 11, 1), ("#B7DAF7", 14, 4))
    for col, x, y in heads:
        for dx, dy in ((0, 0), (-1, 0), (1, 0), (0, -1), (0, 1)):
            img.set(x + dx, y + dy + 1, C(col))
        img.set(x, y + 1, C("#FFE07A"))
    img.set(4, 11, C("#7CC48B"))
    img.set(9, 12, C("#7CC48B"))
    return img


def candle(col):
    img = Img(16, 16)
    rng = rng_for("candle", col)
    for y in range(16):
        for x in range(16):
            c = scale(col, 1 + (rng.random() - 0.5) * 0.03)
            if x in (2, 3):
                c = lighten(c, 0.25)
            img.set(x, y, c)
    for x in range(16):
        drip = 1 + (x * 5) % 3
        for y in range(drip):
            img.set(x, y, lighten(col, 0.35))
    return img


def flame():
    img = Img(16, 16)
    rows = ["......XX........",
            ".....XXXX.......",
            ".....XYYX.......",
            "....XYYYYX......",
            "....XYWWYX......",
            "....XYWWYX......",
            ".....XYYX.......",
            "......XX........"]
    img.stamp(0, 0, rows, {"X": "#FF9A3C", "Y": "#FFD45C", "W": "#FFFBE6"})
    return img


def photo():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            img.set(x, y, mix("#CFE9FF", "#FDE3EE", y / 15) if y < 11 else C("#A9DDB0"))
    img.stamp(5, 4, HEART, {"X": "#F47C9F"})
    img.set(12, 3, C("#FFF6B8"))
    return img


def wicker():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            a = ((x // 2) + (y // 2)) % 2
            img.set(x, y, C("#E2BE8A") if a else C("#C99E68"))
    return img


def strawberry():
    img = Img(16, 16)
    noise_fill(img, C("#EF4E6B"), rng_for("berry"), 0.05)
    for y in range(1, 16, 3):
        for x in range((y // 3) % 2 * 2, 16, 4):
            img.set(x, y, C("#FFE38A"))
    return img


def frosting(col):
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            f = 1 + 0.06 * math.sin((x + y * 2) * 0.9)
            img.set(x, y, scale(col, f))
    return img


def sponge():
    img = Img(16, 16)
    noise_fill(img, C("#F5D9A4"), rng_for("sponge"), 0.06)
    return img


def feather():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            img.set(x, y, C("#FFFFFF") if (x + y // 2) % 4 else C("#EEF0F6"))
    return img


def flat(col, seed, amount=0.02):
    img = Img(16, 16)
    noise_fill(img, C(col), rng_for("flat", seed), amount)
    return img


def tinted_glass(col):
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            c = scale(col, 1.04 - y * 0.008)
            if x in (3, 4) and 2 < y < 13:
                c = lighten(c, 0.5)
            img.set(x, y, c)
    return img


def clock():
    img = Img(16, 16, C("#FFFDF7"))
    for a in range(12):
        x = 7.5 + 6 * math.cos(a * math.pi / 6)
        y = 7.5 + 6 * math.sin(a * math.pi / 6)
        img.set(round(x), round(y), C("#F49ABB"))
    for i in range(5):
        img.set(7, 7 - i, C("#5A4A60"))
    for i in range(4):
        img.set(7 + i, 8, C("#5A4A60"))
    return img


def foam():
    img = Img(16, 16)
    rng = rng_for("foam")
    for _ in range(14):
        cx, cy, r = rng.uniform(1, 15), rng.uniform(1, 15), rng.uniform(1.2, 2.6)
        img.ellipse(cx, cy, r, r, C("#FFFFFF"))
        img.set(int(cx - r / 2), int(cy - r / 2), C("#E8F6FD"))
    return img


def wood():
    img = Img(16, 16)
    noise_fill(img, C("#E8C79E"), rng_for("wood"), 0.03, grain=0.04)
    return img


def neutral_textures():
    out = ASSETS / "textures" / "block"
    gold().save(out / "gold.png")
    silver().save(out / "silver.png")
    lace().save(out / "lace.png")
    sheet().save(out / "sheet.png")
    glass().save(out / "glass.png")
    mirror().save(out / "mirror.png")
    water().save(out / "water.png")
    water_flow().save(out / "water_flow.png")
    porcelain().save(out / "porcelain.png")
    marble().save(out / "marble.png")
    burner(False).save(out / "burner.png")
    burner(True).save(out / "burner_lit.png")
    oven(False).save(out / "oven.png")
    oven(True).save(out / "oven_lit.png")
    dark_metal().save(out / "dark_metal.png")
    pages().save(out / "pages.png")
    monstera().save(out / "monstera.png")
    leaves().save(out / "leaves.png")
    rose_bush().save(out / "rose_bush.png")
    lavender_plant().save(out / "lavender_plant.png")
    blossom().save(out / "blossom.png")
    bark().save(out / "bark.png")
    soil().save(out / "soil.png")
    rope().save(out / "rope.png")
    flowers().save(out / "flowers.png")
    candle(C("#FBC2D6")).save(out / "candle_pink.png")
    candle(C("#FFF9F2")).save(out / "candle_white.png")
    candle(C("#D9C8F6")).save(out / "candle_lavender.png")
    flame().save(out / "flame.png")
    photo().save(out / "photo.png")
    wicker().save(out / "wicker.png")
    strawberry().save(out / "strawberry.png")
    frosting(C("#FFC1D8")).save(out / "frosting_pink.png")
    frosting(C("#FFFFFF")).save(out / "frosting_white.png")
    frosting(C("#D8C6F5")).save(out / "frosting_lavender.png")
    sponge().save(out / "sponge.png")
    feather().save(out / "feather.png")
    flat("#3A2B3F", "eye").save(out / "eye.png")
    flat("#E8445E", "cherry").save(out / "cherry.png")
    flat("#5FAE6E", "stem", 0.08).save(out / "stem.png")
    flat("#FFF6D6", "bulb", 0.0).save(out / "bulb.png")
    flat("#FFF8F6", "pearl").save(out / "pearl.png")
    flat("#F47C9F", "heart", 0.03).save(out / "heart.png")
    flat("#FFE07A", "yellow", 0.03).save(out / "yellow.png")
    flat("#FFA24C", "orange", 0.03).save(out / "orange.png")
    flat("#D9976A", "tea", 0.03).save(out / "tea.png")
    tinted_glass(C("#F9B7CF")).save(out / "glass_pink.png")
    tinted_glass(C("#CDB8F3")).save(out / "glass_lavender.png")
    tinted_glass(C("#B3E8D0")).save(out / "glass_mint.png")
    tinted_glass(C("#BFDDF7")).save(out / "glass_blue.png")
    clock().save(out / "clock.png")
    foam().save(out / "foam.png")
    wood().save(out / "wood.png")


# --------------------------------------------------------------------------- items & gui

def catalog_item():
    rows = ["................",
            "...gggggggggg...",
            "..gPPPPPPPPPPg..",
            "..gPPPPPPPPPPgW.",
            "..gPPLLLLLLPPgW.",
            "..gPPPPPPPPPPgW.",
            "..gPPPpBpPPPPgW.",
            "..gPPBpppBPPPgW.",
            "..gPPPBpBPPPPgW.",
            "..gPPPPBPPPPPgW.",
            "..gPPPPPPPPPPgW.",
            "..gPPLLLLLLPPgW.",
            "..gPPPPPPPPPPgW.",
            "..gggggggggggg..",
            "...WWWWWWWWWW...",
            "................"]
    return _ascii(rows, {"g": "#E4B14E", "P": "#F7B8CF", "L": "#FFFFFF", "p": "#F06E9B", "B": "#D9497F", "W": "#FFF7F0"})


def cup_of_tea():
    rows = ["................",
            ".....w..w.......",
            "......w..w......",
            ".....w..w.......",
            "................",
            "..PPPPPPPPP.....",
            "..WtttttttWWW...",
            "..WWWWWWWWW..W..",
            "..WWWWPWWWW..W..",
            "...WWPPPWWWWW...",
            "...WWWPWWWW.....",
            "....WWWWWW......",
            ".SSSSSSSSSSSS...",
            "..SSSSSSSSSS....",
            "................",
            "................"]
    return _ascii(rows, {"w": (255, 255, 255, 170), "P": "#F7A8C6", "W": "#FFFFFF", "t": "#E8A06A", "S": "#F3D2E0"})


def _ascii(rows, palette):
    img = Img(16, 16)
    img.stamp(0, 0, rows, palette)
    return img


def item_textures():
    out = ASSETS / "textures" / "item"
    catalog_item().save(out / "interior_design_catalog.png")
    cup_of_tea().save(out / "cup_of_tea.png")


def generate():
    colour_textures()
    neutral_textures()
    item_textures()
