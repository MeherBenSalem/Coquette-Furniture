"""Decorative plants and the small decorations that can be set on tables and shelves."""
from .core import B, box, mirror_x, move, rbox, rot_y90, sym
from .shapes import bow, cross_plane, heart, heart_flat, leaf_plane, pot, ring

# --------------------------------------------------------------------------- plants (floor)


def potted_monstera():
    e = pot("pot")
    e += B(7.5, 7.5, 7.5, 8.5, 13, 8.5, "stem")
    e += B(7.75, 12, 7.75, 8.25, 17, 8.25, "stem", rot=("z", 22.5, (8, 12, 8)))
    leaves = [
        ((1, 10, 13, 22, 7), ("x", -22.5, (7, 10, 7))),
        ((3, 13, 15, 25, 9), ("x", 22.5, (9, 13, 9))),
    ]
    for (x0, y0, x1, y1, z), rot in leaves:
        e += leaf_plane(x0, y0, x1, y1, z, "monstera", rot)
    e += rot_y90(leaf_plane(2, 11, 13, 22, 8, "monstera", ("x", -45, (7.5, 11, 8))), 1)
    e += rot_y90(leaf_plane(3, 9, 13, 19, 8, "monstera", ("x", 45, (8, 9, 8))), 1)
    return e


def potted_rose_bush():
    e = pot("pot")
    e += B(7.25, 7.5, 7.25, 8.75, 9, 8.75, "bark")
    e += rbox(3, 8.5, 3, 13, 15.5, 13, "rose_bush", r=1.5)
    e += rbox(4.5, 15, 4.5, 11.5, 17.5, 11.5, "rose_bush", r=1)
    for x, y, z in ((2.5, 12, 6), (12, 11, 8.5), (6, 16.5, 4), (9.5, 14, 12), (5, 10, 12.25), (10, 17, 8)):
        e += B(x, y, z, x + 1.75, y + 1.5, z + 1.75, "rose")
        e += B(x + 0.5, y + 1.5, z + 0.5, x + 1.25, y + 1.75, z + 1.25, "rose_dark", d=None)
    e += bow(8, 4.5, 3.25, 3.5, 0.6)
    return e


def potted_lavender():
    e = pot("pot")
    e += cross_plane(2, 7.5, 14, 20, "lavender_plant", angle=22.5)
    e += cross_plane(3, 7.5, 13, 18, "lavender_plant", angle=67.5) if False else []
    e += rot_y90(cross_plane(2.5, 7.5, 13.5, 19, "lavender_plant", angle=22.5), 1)
    e += bow(8, 4.5, 3.25, 3.5, 0.6)
    return e


def potted_cherry_blossom():
    e = pot("pot")
    e += B(7, 7.5, 7, 9, 17, 9, "bark")
    e += B(8.5, 13, 7.5, 12, 14.25, 8.75, "bark", rot=("z", 22.5, (8.5, 13, 8)))
    e += B(4.5, 15, 7.25, 7.5, 16, 8.5, "bark", rot=("z", -22.5, (7.5, 15, 8)))
    e += rbox(1.5, 16.5, 1.5, 14.5, 24, 14.5, "blossom", r=2)
    e += rbox(3.5, 23.5, 3.5, 12.5, 26.5, 12.5, "blossom", r=1.5)
    e += rbox(0.5, 17.5, 5, 4, 21.5, 11, "blossom", r=1)
    e += rbox(12, 18, 4.5, 15.5, 22, 10, "blossom", r=1)
    for x, z in ((3, 2), (12, 13.5), (6, 14)):
        e += B(x, 15.75, z, x + 1, 16.5, z + 1, "blossom")
    return e


PLANTS = {
    "potted_monstera": (potted_monstera, {"pot": "block/porcelain"}),
    "potted_rose_bush": (potted_rose_bush, {"pot": "block/baby_pink/ceramic", "rose": "block/frosting_pink",
                                            "rose_dark": "block/heart", "bow": "block/baby_pink/bow"}),
    "potted_lavender": (potted_lavender, {"pot": "block/lavender/ceramic", "bow": "block/lavender/bow"}),
    "potted_cherry_blossom": (potted_cherry_blossom, {"pot": "block/cream/ceramic"}),
}


# --------------------------------------------------------------------------- decorations (sinkable)

def book_stack(count):
    covers = ["book_pink", "book_blue", "book_lavender"]
    e = []
    e += box((3.75, 0, 5), (12.25, 1.5, 11), "pages", up=covers[0], down=covers[0], west=covers[0])
    e += B(3.7, 0.5, 5, 3.75, 1, 11, "gold", n=None, s=None, u=None, d=None, e=None)
    e += B(7, 0.25, 10.95, 7.5, 1.25, 11.3, "bow")
    if count >= 2:
        e += box((4.25, 1.5, 5.25), (11.75, 2.75, 10.75), "pages", up=covers[1], down=covers[1], east=covers[1],
                 rot=("y", 22.5, (8, 2, 8)))
    if count >= 3:
        e += box((4.75, 2.75, 5.5), (11.25, 4, 10.5), "pages", up=covers[2], down=covers[2], west=covers[2],
                 rot=("y", -22.5, (8, 3.5, 8)))
        e += heart_flat(8, 8, 4, 4.4, 0.25, "heart") if False else []
    return e


def rose_vase():
    e = []
    e += B(6.5, 0, 6.5, 9.5, 1, 9.5, "porcelain")
    e += B(6, 1, 6, 10, 4, 10, "porcelain")
    e += B(5.9, 2.25, 5.9, 10.1, 2.75, 10.1, "gold", u=None, d=None)
    e += B(6.75, 4, 6.75, 9.25, 5.5, 9.25, "porcelain")
    e += ring(6.25, 6.25, 9.75, 9.75, 5.5, 6.25, 0.6, "porcelain")
    e += heart(8, 1.4, 5.85, 6, 0.2, "heart")
    for x, z, h in ((7.5, 7.5, 10), (8.25, 8.25, 11), (7, 8.25, 9.5)):
        e += B(x, 5.5, z, x + 0.4, h, z + 0.4, "stem")
    for x, y, z in ((6, 9.25, 6.75), (8, 10.25, 7.75), (6.5, 8.75, 8.5)):
        e += B(x, y, z, x + 1.75, y + 1.6, z + 1.75, "rose")
        e += B(x + 0.5, y + 1.6, z + 0.5, x + 1.25, y + 1.8, z + 1.25, "rose_dark", d=None)
    e += cross_plane(5.25, 6, 10.75, 10, "leaves", angle=22.5)
    return e


def tulip_vase():
    e = []
    e += B(6, 0, 6, 10, 0.5, 10, "glass_blue")
    e += B(6, 0.5, 6, 6.5, 5, 10, "glass_blue") + B(9.5, 0.5, 6, 10, 5, 10, "glass_blue")
    e += B(6.5, 0.5, 6, 9.5, 5, 6.5, "glass_blue") + B(6.5, 0.5, 9.5, 9.5, 5, 10, "glass_blue")
    e += B(6.5, 0.5, 6.5, 9.5, 3.5, 9.5, "water", n=None, s=None, e=None, w=None, d=None)
    e += B(5.75, 3, 5.75, 10.25, 3.75, 10.25, "bow", u=None, d=None)
    e += bow(8, 2.5, 5.5, 5.75, 0.45)
    for x, z, h, tex in ((7, 7.25, 9, "frosting_pink"), (8.5, 8, 10, "frosting_white"), (7.5, 8.75, 8.25, "frosting_lavender")):
        e += B(x + 0.3, 0.5, z + 0.3, x + 0.7, h, z + 0.7, "stem")
        e += B(x - 0.1, h, z - 0.1, x + 1.1, h + 1.6, z + 1.1, tex)
        e += B(x - 0.35, h + 1, z - 0.35, x + 1.35, h + 1.9, z - 0.1, tex)
    e += cross_plane(6, 4.5, 10, 8.5, "leaves", angle=45)
    return e


def pastel_candles():
    e = []
    e += rbox(3.5, 0, 3.5, 12.5, 0.75, 12.5, "gold", r=0.75)
    e += bow(8, 0.15, 3.15, 3.4, 0.4)
    candles = ((6, 6.5, 7, "candle_pink"), (10, 7.5, 5, "candle_white"), (8, 10.25, 3.5, "candle_lavender"))
    for cx, cz, h, tex in candles:
        top = 0.75 + h
        e += B(cx - 1, 0.75, cz - 1, cx + 1, top, cz + 1, tex)
        e += B(cx - 0.15, top, cz - 0.15, cx + 0.15, top + 0.6, cz + 0.15, "eye")
    return e


def candle_flames():
    e = []
    candles = ((6, 6.5, 7), (10, 7.5, 5), (8, 10.25, 3.5))
    for cx, cz, h in candles:
        top = 0.75 + h
        for a in (45, -45):
            e += box((cx - 0.9, top + 0.2, cz), (cx + 0.9, top + 2.6, cz), ("flame", (4, 0, 10, 8)),
                     rot=("y", a, (cx, top + 1, cz)), shade=False)
    return e


def perfume_bottles():
    e = []
    e += B(3, 0, 4.5, 13, 0.5, 11.5, "gold", u="mirror")
    e += B(4, 0.5, 6, 6.5, 4, 8.5, "glass_pink")
    e += B(4.75, 4, 6.75, 5.75, 4.75, 7.75, "gold")
    e += heart(5.25, 4.75, 7.05, 7.45, 0.2, "glass_pink")
    e += rbox(7.5, 0.5, 6.5, 10, 3.5, 9, "glass_lavender", r=0.6)
    e += B(8.5, 3.5, 7.5, 9, 4.5, 8, "gold")
    e += B(9, 4, 7.5, 10.5, 4.25, 7.75, "silver")
    e += rbox(10.25, 3, 6.75, 11.75, 4.5, 8.5, "frosting_pink", r=0.4)
    e += B(10.85, 2, 7.55, 11.15, 3, 7.7, "gold")
    e += B(10.5, 0.5, 9, 12, 5.5, 10.5, "glass_mint")
    e += B(10.75, 5.5, 9.25, 11.75, 6.5, 10.25, "gold")
    e += B(10.9, 6.5, 9.4, 11.6, 7, 10.1, "pearl")
    return e


def heart_music_box():
    e = []
    for x, z in ((4.5, 6), (10.75, 6), (7.6, 11.75)):
        e += B(x, 0, z, x + 0.75, 0.5, z + 0.75, "gold")
    e += heart_flat(8, 8.5, 0.5, 3.75, 0.95, "box", u=None)
    e += heart_flat(8, 8.5, 3.25, 3.75, 1.0, "gold", u=None)
    e += heart_flat(8, 8.5, 3.75, 4.75, 0.95, "box", u="quilt_box")
    e += B(12.75, 1.75, 8, 13.75, 2.25, 9, "gold") + B(13.5, 1.75, 8.25, 14, 3.25, 8.75, "gold")
    e += B(13.4, 3.25, 8.1, 14.1, 3.75, 8.9, "pearl")
    e += B(7.75, 4.75, 8.5, 8.25, 6.75, 9, "porcelain")
    e += B(7.15, 5.6, 7.9, 8.85, 6.1, 9.6, "lace")
    e += B(7.65, 6.75, 8.4, 8.35, 7.45, 9.1, "porcelain")
    e += B(7.6, 7.2, 8.6, 8.4, 7.6, 9.2, "box")
    return e


def teddy_bear():
    t, lt = "teddy", "teddy_light"
    e = []
    e += rbox(5, 0, 6.5, 11, 5, 11, t, r=1)
    e += B(6.5, 1, 6.4, 9.5, 4, 6.5, lt, s=None, u=None, d=None, e=None, w=None)
    e += sym(rbox(4.5, 0, 3.5, 7, 2, 7, t, r=0.5) + B(5, 0.25, 3.4, 6.5, 1.75, 3.5, lt, s=None, u=None, d=None, e=None, w=None))
    e += sym(rbox(3.75, 1.5, 6.75, 5.25, 4.5, 8.75, t, r=0.4))
    e += rbox(4.5, 5, 5.5, 11.5, 10.5, 11, t, r=1)
    e += sym(rbox(4.25, 9.5, 7.5, 6.25, 11.5, 9, t, r=0.4) + B(4.75, 10, 7.4, 5.75, 11, 7.5, lt, s=None, u=None, d=None, e=None, w=None))
    e += B(6.75, 6, 5, 9.25, 7.75, 5.5, lt)
    e += B(7.5, 7, 4.75, 8.5, 7.75, 5, "eye")
    e += sym(B(6, 8, 5.4, 6.75, 8.75, 5.5, "eye", s=None, u=None, d=None, e=None, w=None))
    e += sym(B(5.25, 6.75, 5.4, 6.25, 7.25, 5.5, "blush", s=None, u=None, d=None, e=None, w=None))
    e += bow(8, 4.6, 5.9, 6.4, 0.55, "ribbon")
    return e


def bunny_plush():
    t, lt = "bunny", "bunny_inner"
    e = []
    e += rbox(5, 0, 6.5, 11, 5, 11.5, t, r=1)
    e += sym(rbox(4.75, 0, 4, 7, 1.5, 7.5, t, r=0.5))
    e += sym(rbox(4, 1.5, 7, 5.5, 4, 9, t, r=0.4))
    e += rbox(7, 1, 11.25, 9, 3, 12.75, t, r=0.6)
    e += rbox(4.75, 5, 5.5, 11.25, 10, 10.75, t, r=1)
    e += B(5.75, 9.5, 7.5, 7.25, 15, 8.75, t)
    e += B(6, 10, 7.4, 7, 14.5, 7.5, lt, s=None, u=None, d=None, e=None, w=None)
    e += B(8.75, 9.5, 7.5, 10.25, 14, 8.75, t, rot=("z", -22.5, (9.5, 9.5, 8)))
    e += B(9, 10, 7.4, 10, 13.5, 7.5, lt, s=None, u=None, d=None, e=None, w=None, rot=("z", -22.5, (9.5, 9.5, 8)))
    e += sym(B(6.25, 7.75, 5.4, 7, 8.5, 5.5, "eye", s=None, u=None, d=None, e=None, w=None))
    e += sym(B(5.5, 6.5, 5.4, 6.5, 7, 5.5, lt, s=None, u=None, d=None, e=None, w=None))
    e += B(7.6, 6.9, 5.3, 8.4, 7.4, 5.5, lt)
    e += bow(6.5, 10, 6.9, 7.4, 0.45, "ribbon")
    return e


def cupcake(cx, y, cz, frost):
    e = []
    e += B(cx - 1, y, cz - 1, cx + 1, y + 1.25, cz + 1, "wrapper")
    e += B(cx - 1.25, y + 1.25, cz - 1.25, cx + 1.25, y + 2, cz + 1.25, frost)
    e += B(cx - 0.75, y + 2, cz - 0.75, cx + 0.75, y + 2.5, cz + 0.75, frost)
    e += B(cx - 0.35, y + 2.5, cz - 0.35, cx + 0.35, y + 3.1, cz + 0.35, "cherry")
    return e


def cupcake_stand():
    e = []
    e += rbox(6, 0, 6, 10, 0.5, 10, "gold", r=0.5)
    e += B(7.6, 0.5, 7.6, 8.4, 10, 8.4, "gold")
    e += B(2.5, 3, 4, 13.5, 3.5, 12, "porcelain") + B(4, 3, 2.5, 12, 3.5, 13.5, "porcelain")
    e += B(4.5, 8, 5.5, 11.5, 8.5, 10.5, "porcelain") + B(5.5, 8, 4.5, 10.5, 8.5, 11.5, "porcelain")
    e += B(7.25, 10, 7.25, 8.75, 11, 8.75, "pearl")
    frosts = ["frosting_pink", "frosting_white", "frosting_lavender", "frosting_pink"]
    for i, (x, z) in enumerate(((4.75, 4.75), (11.25, 4.75), (4.75, 11.25), (11.25, 11.25))):
        e += cupcake(x, 3.5, z, frosts[i])
    e += cupcake(5.75, 8.5, 8, "frosting_lavender")
    e += cupcake(10.25, 8.5, 8, "frosting_pink")
    return e


def heart_photo_frame():
    e = []
    e += B(4, 0, 6.5, 12, 0.5, 9, "gold")
    e += heart(8, 0.5, 7, 8, 0.95, "gold")
    e += heart(8, 1.6, 6.85, 7, 0.68, "photo", picture=True, s=None, u=None, d=None, e=None, w=None)
    e += B(7.5, 0.5, 8, 8.5, 5.5, 9, "gold", rot=("x", 22.5, (8, 0.5, 8)))
    e += bow(8, 7.6, 6.6, 7, 0.45)
    return e


def ribbon_gift_box():
    e = []
    e += B(4, 0, 4, 12, 5, 12, "giftbox")
    e += B(3.75, 5, 3.75, 12.25, 6.25, 12.25, "giftbox")
    e += B(7.25, 0, 3.65, 8.75, 6.35, 12.35, "ribbon")
    e += B(3.65, 0, 7.25, 12.35, 6.35, 8.75, "ribbon")
    e += B(5, 6.25, 7.4, 7.5, 8.25, 8.6, "ribbon", rot=("z", 22.5, (7.5, 6.25, 8)))
    e += B(8.5, 6.25, 7.4, 11, 8.25, 8.6, "ribbon", rot=("z", -22.5, (8.5, 6.25, 8)))
    e += B(7.4, 6.25, 7.4, 8.6, 7.25, 8.6, "ribbon")
    e += B(5.5, 6.25, 6.25, 6.75, 6.5, 7.25, "ribbon", rot=("y", 22.5, (6, 6.4, 6.75)))
    e += B(9.25, 6.25, 8.75, 10.5, 6.5, 9.75, "ribbon", rot=("y", 22.5, (10, 6.4, 9.25)))
    return e


def strawberry_basket():
    e = []
    e += B(4, 0, 5, 12, 4, 5.75, "wicker") + B(4, 0, 10.25, 12, 4, 11, "wicker")
    e += B(4, 0, 5.75, 4.75, 4, 10.25, "wicker") + B(11.25, 0, 5.75, 12, 4, 10.25, "wicker")
    e += B(4.75, 0, 5.75, 11.25, 0.5, 10.25, "wicker")
    e += B(4.25, 4, 7.5, 4.75, 8, 8.5, "wicker") + B(11.25, 4, 7.5, 11.75, 8, 8.5, "wicker")
    e += B(4.25, 8, 7.5, 11.75, 8.75, 8.5, "wicker")
    e += B(4.5, 3, 5.5, 11.5, 3.5, 10.5, "cloth", n=None, s=None, e=None, w=None, d=None)
    e += B(4.5, 2.5, 4.9, 6.5, 4.25, 5, "cloth", s=None, u=None, d=None)
    e += side_free_bow(11.9)
    for x, y, z in ((5, 3.25, 6), (7, 3.5, 7.75), (9.25, 3.25, 6.25), (6, 3.25, 8.75), (9, 3.4, 8.75)):
        e += B(x, y, z, x + 1.5, y + 1.5, z + 1.5, "strawberry")
        e += B(x + 0.25, y + 1.5, z + 0.25, x + 1.25, y + 1.75, z + 1.25, "stem")
    return e


def side_free_bow(x):
    from .shapes import side_bow
    return side_bow(x, x + 0.5, 5.25, 8, 0.45)


def angel_figurine():
    e = []
    e += B(5.5, 0, 5.5, 10.5, 1, 10.5, "gold")
    e += B(6, 1, 6, 10, 1.5, 10, "porcelain")
    e += B(6.25, 1.5, 6.5, 9.75, 6, 9.5, "porcelain")
    e += B(6.75, 6, 6.75, 9.25, 7.5, 9.25, "porcelain")
    e += B(6.9, 7.5, 6.9, 9.1, 9.5, 9.1, "porcelain")
    e += B(6.75, 8.75, 7.25, 9.25, 9.75, 9.35, "hair")
    e += B(6.75, 7.75, 8.75, 9.25, 8.75, 9.35, "hair")
    e += sym(B(7.35, 8.25, 6.85, 7.75, 8.6, 6.9, "eye", s=None, u=None, d=None, e=None, w=None))
    e += ring(6.75, 6.75, 9.25, 9.25, 10.25, 10.5, 0.45, "gold")
    wing = B(3.5, 4, 9.25, 7, 8.5, 10, "feather", rot=("y", -22.5, (7, 6, 9.6)))
    wing += B(3, 7, 9.25, 4.5, 9, 10, "feather", rot=("y", -22.5, (7, 6, 9.6)))
    e += wing + mirror_x(wing)
    e += heart(8, 3.6, 6.15, 6.5, 0.22, "heart")
    e += B(6.5, 4.25, 6.25, 7.5, 5, 6.75, "porcelain") + B(8.5, 4.25, 6.25, 9.5, 5, 6.75, "porcelain")
    return e


SHARED = {"teddy": "block/baby_pink/fabric", "teddy_light": "block/white/fabric", "blush": "block/heart",
          "ribbon": "block/white/bow", "bunny": "block/white/fabric", "bunny_inner": "block/baby_pink/panel",
          "box": "block/baby_pink/paint", "quilt_box": "block/baby_pink/quilt", "bow": "block/white/bow",
          "giftbox": "block/baby_blue/paint", "cloth": "block/baby_pink/gingham", "wrapper": "block/baby_pink/stripe",
          "rose": "block/frosting_pink", "rose_dark": "block/heart", "hair": "block/yellow",
          "book_pink": "block/baby_pink/trim", "book_blue": "block/baby_blue/trim", "book_lavender": "block/lavender/trim"}

DECOR = {
    "book_stack": {"variants": {1: lambda: book_stack(1), 2: lambda: book_stack(2), 3: lambda: book_stack(3)}, "prop": "books"},
    "rose_vase": {"build": rose_vase},
    "tulip_vase": {"build": tulip_vase},
    "pastel_candles": {"build": pastel_candles, "lit": candle_flames},
    "perfume_bottles": {"build": perfume_bottles},
    "heart_music_box": {"build": heart_music_box},
    "teddy_bear": {"build": teddy_bear},
    "bunny_plush": {"build": bunny_plush},
    "cupcake_stand": {"build": cupcake_stand},
    "heart_photo_frame": {"build": heart_photo_frame},
    "ribbon_gift_box": {"build": ribbon_gift_box},
    "strawberry_basket": {"build": strawberry_basket},
    "angel_figurine": {"build": angel_figurine},
}
