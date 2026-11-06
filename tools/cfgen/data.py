"""Turns the geometry into models, blockstates, item models, loot tables, recipes,
tags and the language file."""
import json

from . import decor, furniture as F
from .core import (ASSETS, DATA, M, MC_DATA, MODID, ROOT, centred_for_item, check_range, display_for, move,
                   rot_y90, serialise, split, tex_vars, write_json)
from .textures import COLORS

COLOR_NAMES = {"baby_pink": "Baby Pink", "white": "White", "cream": "Cream", "lavender": "Lavender",
               "mint": "Mint", "baby_blue": "Baby Blue"}
DYES = {"baby_pink": "minecraft:pink_dye", "white": "minecraft:white_dye", "cream": "minecraft:yellow_dye",
        "lavender": "minecraft:purple_dye", "mint": "minecraft:lime_dye", "baby_blue": "minecraft:light_blue_dye"}

COLOR_VARS = {"paint", "trim", "panel", "fabric", "lining", "quilt", "print", "gingham", "stripe", "ceramic",
              "shade", "rug", "rug_edge", "bow", "sheer"}

NEUTRAL = {
    "gold": "block/gold", "silver": "block/silver", "lace": "block/lace", "sheet": "block/sheet",
    "glass": "block/glass", "mirror": "block/mirror", "water": "block/water", "water_flow": "block/water_flow",
    "porcelain": "block/porcelain", "marble": "block/marble", "burner": "block/burner", "oven": "block/oven",
    "dark_metal": "block/dark_metal", "pages": "block/pages", "wood": "block/wood", "rope": "block/rope",
    "flowers": "block/flowers", "soil": "block/soil", "foam": "block/foam", "clock": "block/clock",
    "bulb": "block/bulb", "eye": "block/eye", "leaves": "block/leaves", "pearl": "block/pearl", "heart": "block/heart",
    "yellow": "block/yellow", "orange": "block/orange", "tea": "block/tea", "glass_pink": "block/glass_pink",
    "glass_lavender": "block/glass_lavender", "glass_mint": "block/glass_mint", "glass_blue": "block/glass_blue",
    "frosting_pink": "block/frosting_pink", "frosting_white": "block/frosting_white",
    "frosting_lavender": "block/frosting_lavender", "cherry": "block/cherry", "strawberry": "block/strawberry",
    "stem": "block/stem", "milk": "block/porcelain", "baby_blue_cap": "block/baby_blue/paint", "frost": "block/glass_blue",
    "book_pink": "block/baby_pink/trim", "book_blue": "block/baby_blue/trim", "book_lavender": "block/lavender/trim",
    "book_mint": "block/mint/trim", "book_cream": "block/cream/trim",
    "dress1": "block/baby_pink/print", "dress2": "block/lavender/gingham", "dress3": "block/baby_blue/stripe",
    "dress4": "block/mint/print", "bird": "block/baby_blue/fabric", "monstera": "block/monstera",
    "rose_bush": "block/rose_bush", "lavender_plant": "block/lavender_plant", "blossom": "block/blossom",
    "bark": "block/bark", "candle_pink": "block/candle_pink", "candle_white": "block/candle_white",
    "candle_lavender": "block/candle_lavender", "flame": "block/flame", "photo": "block/photo",
    "wicker": "block/wicker", "feather": "block/feather", "burner_lit": "block/burner_lit", "oven_lit": "block/oven_lit",
}

FACING_Y = {"north": 0, "east": 90, "south": 180, "west": 270}
SHAPES4 = ("single", "left", "middle", "right")
SINKS = range(5)


# --------------------------------------------------------------------------- the furniture table

class FType:
    def __init__(self, tid, name, room, kind, templates, item, particle="paint", tool="axe", recipe=None,
                 lit=None, loot="self", count=1):
        self.id = tid
        self.name = name
        self.room = room
        self.kind = kind
        self.templates = templates      # {variant: elements}
        self.item = item                # elements for the item model, or a template variant name
        self.particle = particle
        self.tool = tool
        self.recipe = recipe            # (pattern, key, count)
        self.lit = lit                  # texture overrides for *_lit child variants
        self.loot = loot
        self.count = count


def sinks(build):
    base = build()
    return {f"s{k}": move(base, 0, -2 * k, 0) for k in SINKS}


def tall(build_closed, build_open=None):
    out = {}
    lo, hi = split(build_closed(), 1, 16)
    out["lower"], out["upper"] = lo, hi
    if build_open:
        lo, hi = split(build_open(), 1, 16)
        out["lower_open"], out["upper_open"] = lo, hi
    return out


def long2(build, suffix=""):
    head, foot = split(build(), 2, 16)
    return {"head" + suffix: head, "foot" + suffix: foot}


def P(*rows):
    return list(rows)


def recipe(pattern, key, count=1):
    return (pattern, key, count)


PLANKS = {"tag": "minecraft:planks"}
WOOL = {"tag": "minecraft:wool"}
CARPET = {"tag": "minecraft:wool_carpets"}
FLOWERS = {"tag": "minecraft:small_flowers"}


def I(item):
    return {"item": "minecraft:" + item}


def build_types():
    T = []
    # ---------------------------------------------------------------- bedroom
    T.append(FType("canopy_bed", "Canopy Bed", "bedroom", "bed", long2(F.canopy_bed), rot_y90(F.canopy_bed(), 2),
                   recipe=recipe(P("SDS", "WWW", "PPP"), {"S": I("stick"), "W": WOOL, "P": PLANKS}), loot="head"))
    T.append(FType("decorative_pillow", "Decorative Pillows", "bedroom", "pillows",
                   {f"p{n}_s{k}": move(F.pillows(n), 0, -2 * k, 0) for n in (1, 2, 3) for k in SINKS}, "p1_s0",
                   particle="fabric", tool=None, recipe=recipe(P(" F ", "WDW"), {"F": I("feather"), "W": WOOL}, 2),
                   loot="pillows"))
    T.append(FType("bedside_table", "Bedside Table", "bedroom", "open",
                   {"closed": F.bedside_table(False), "open": F.bedside_table(True)}, "closed",
                   recipe=recipe(P("PDP", "PCP", "S S"), {"P": PLANKS, "C": I("chest"), "S": I("stick")})))
    T.append(FType("wardrobe", "Wardrobe", "bedroom", "tall_open",
                   tall(lambda: F.wardrobe(False), lambda: F.wardrobe(True)), F.wardrobe(False),
                   recipe=recipe(P("PDP", "PCP", "PCP"), {"P": PLANKS, "C": I("chest")}), loot="lower"))
    T.append(FType("dresser", "Dresser", "bedroom", "open", {"closed": F.dresser(False), "open": F.dresser(True)},
                   "closed", recipe=recipe(P("PPP", "PCP", "PDP"), {"P": PLANKS, "C": I("chest")})))
    T.append(FType("vanity_table", "Vanity Table", "bedroom", "open",
                   {"closed": F.vanity_table(False), "open": F.vanity_table(True)}, "closed",
                   recipe=recipe(P(" M ", "PDP", "S S"), {"M": I("glass_pane"), "P": PLANKS, "S": I("stick")})))
    T.append(FType("standing_mirror", "Standing Mirror", "bedroom", "tall", tall(F.standing_mirror), F.standing_mirror(),
                   recipe=recipe(P("SMS", "SMS", "SDS"), {"S": I("stick"), "M": I("glass_pane")}), loot="lower"))
    T.append(FType("jewelry_box", "Jewelry Box", "bedroom", "sink_open",
                   {**{f"closed_s{k}": move(F.jewelry_box(False), 0, -2 * k, 0) for k in SINKS},
                    **{f"open_s{k}": move(F.jewelry_box(True), 0, -2 * k, 0) for k in SINKS}}, "closed_s0",
                   recipe=recipe(P("gDg", "PCP"), {"g": I("gold_nugget"), "P": PLANKS, "C": I("chest")})))
    T.append(FType("night_lamp", "Night Lamp", "bedroom", "sink_lit", sinks(F.night_lamp), "s0", particle="shade",
                   tool=None, lit={"shade": "shade_lit"},
                   recipe=recipe(P(" W ", "DTD", " g "), {"W": WOOL, "T": I("torch"), "g": I("gold_nugget")})))
    # ---------------------------------------------------------------- living room
    T.append(FType("sofa", "Sofa", "living_room", "connect", {s: F.sofa(s) for s in SHAPES4}, "single",
                   particle="fabric", recipe=recipe(P("WDW", "WWW", "P P"), {"W": WOOL, "P": PLANKS})))
    T.append(FType("armchair", "Armchair", "living_room", "simple", {"main": F.armchair()}, "main", particle="fabric",
                   recipe=recipe(P("W W", "WDW", "P P"), {"W": WOOL, "P": PLANKS})))
    T.append(FType("coffee_table", "Coffee Table", "living_room", "simple", {"main": F.coffee_table()}, "main",
                   recipe=recipe(P("PDP", "g g"), {"P": PLANKS, "g": I("gold_nugget")})))
    T.append(FType("bookshelf", "Bookshelf", "living_room", "simple", {"main": F.bookshelf()}, "main",
                   recipe=recipe(P("PDP", "BBB", "PPP"), {"P": PLANKS, "B": I("book")})))
    T.append(FType("display_cabinet", "Display Cabinet", "living_room", "simple", {"main": F.display_cabinet()}, "main",
                   recipe=recipe(P("PDP", "M M", "PPP"), {"P": PLANKS, "M": I("glass_pane")})))
    T.append(FType("rug", "Rug", "living_room", "rug", {"center": F.rug_center(), "edge": F.rug_edge()},
                   F.rug_center() + _all_sides(F.rug_edge), particle="rug", tool=None,
                   recipe=recipe(P("CDC"), {"C": CARPET}, 2)))
    T.append(FType("curtains", "Curtains", "living_room", "tall_open", tall(lambda: F.curtains(False), lambda: F.curtains(True)),
                   F.curtains(False), particle="fabric", tool=None,
                   recipe=recipe(P("SSS", "WDW", "W W"), {"S": I("stick"), "W": WOOL}), loot="lower"))
    # ---------------------------------------------------------------- kitchen
    T.append(FType("kitchen_cabinet", "Kitchen Cabinet", "kitchen", "open",
                   {"closed": F.kitchen_cabinet(False), "open": F.kitchen_cabinet(True)}, "closed",
                   recipe=recipe(P("QQQ", "PDP", "PCP"), {"Q": I("quartz"), "P": PLANKS, "C": I("chest")})))
    T.append(FType("refrigerator", "Refrigerator", "kitchen", "tall_open",
                   tall(lambda: F.refrigerator(False), lambda: F.refrigerator(True)), F.refrigerator(False),
                   particle="ceramic", tool="pickaxe",
                   recipe=recipe(P("IDI", "ICI", "I I"), {"I": I("iron_ingot"), "C": I("chest")}), loot="lower"))
    T.append(FType("kitchen_counter", "Kitchen Counter", "kitchen", "open",
                   {"closed": F.kitchen_counter(False), "open": F.kitchen_counter(True)}, "closed",
                   recipe=recipe(P("QQQ", "PDP", "PPP"), {"Q": I("quartz"), "P": PLANKS})))
    T.append(FType("kitchen_sink", "Kitchen Sink", "kitchen", "running",
                   {"off": F.kitchen_sink(False), "on": F.kitchen_sink(True)}, "off",
                   recipe=recipe(P("QBQ", "PDP", "PPP"), {"Q": I("quartz"), "B": I("bucket"), "P": PLANKS})))
    T.append(FType("stove", "Stove", "kitchen", "lit", {"main": F.stove()}, "main", particle="ceramic", tool="pickaxe",
                   lit={"burner": "burner_lit", "oven": "oven_lit"},
                   recipe=recipe(P("IDI", "ISI", "I I"), {"I": I("iron_ingot"), "S": I("smoker")})))
    T.append(FType("dining_table", "Dining Table", "kitchen", "table",
                   {"top": F.dining_table_top(), "skirt": F.dining_table_skirt(), "leg": F.dining_table_leg()},
                   F.dining_table_top() + _all_sides(F.dining_table_skirt) + _all_corners(F.dining_table_leg),
                   recipe=recipe(P("PPP", "SDS", "S S"), {"P": PLANKS, "S": I("stick")})))
    T.append(FType("dining_chair", "Dining Chair", "kitchen", "simple", {"main": F.dining_chair()}, "main",
                   recipe=recipe(P("S  ", "PDP", "S S"), {"S": I("stick"), "P": PLANKS})))
    T.append(FType("tea_set", "Tea Set", "kitchen", "sink", sinks(F.tea_set), "s0", particle="ceramic", tool=None,
                   recipe=recipe(P("KDK", "gKg"), {"K": I("clay_ball"), "g": I("gold_nugget")})))
    # ---------------------------------------------------------------- bathroom
    T.append(FType("bathtub", "Bathtub", "bathroom", "tub",
                   {**long2(lambda: F.bathtub(False)), **long2(lambda: F.bathtub(True), "_filled")}, rot_y90(F.bathtub(False), 2),
                   particle="ceramic", tool="pickaxe",
                   recipe=recipe(P("Q Q", "QDQ", "QQQ"), {"Q": I("quartz")}), loot="head"))
    T.append(FType("bathroom_sink", "Bathroom Sink", "bathroom", "running",
                   {"off": F.bathroom_sink(False), "on": F.bathroom_sink(True)}, "off", particle="ceramic", tool="pickaxe",
                   recipe=recipe(P("QDQ", " Q ", " Q "), {"Q": I("quartz")})))
    T.append(FType("wall_mirror", "Wall Mirror", "bathroom", "simple", {"main": F.wall_mirror()}, "main",
                   recipe=recipe(P(" D ", "PMP", " P "), {"P": PLANKS, "M": I("glass_pane")})))
    T.append(FType("bathroom_cabinet", "Bathroom Cabinet", "bathroom", "open",
                   {"closed": F.bathroom_cabinet(False), "open": F.bathroom_cabinet(True)}, "closed",
                   recipe=recipe(P("PDP", "PMP", "PCP"), {"P": PLANKS, "M": I("glass_pane"), "C": I("chest")})))
    T.append(FType("towel_rack", "Towel Rack", "bathroom", "simple", {"main": F.towel_rack()}, "main", particle="fabric",
                   tool=None, recipe=recipe(P("iDi", "WWW"), {"i": I("iron_nugget"), "W": WOOL})))
    T.append(FType("soap_dispenser", "Soap Dispenser", "bathroom", "sink", sinks(F.soap_dispenser), "s0",
                   particle="ceramic", tool=None, recipe=recipe(P(" i ", "KDK", " K "), {"i": I("iron_nugget"), "K": I("clay_ball")})))
    # ---------------------------------------------------------------- garden
    T.append(FType("garden_bench", "Garden Bench", "garden", "connect", {s: F.garden_bench(s) for s in SHAPES4}, "single",
                   recipe=recipe(P("S S", "PDP", "S S"), {"S": I("stick"), "P": PLANKS})))
    T.append(FType("flower_pot", "Flower Pot", "garden", "sink", sinks(F.flower_pot), "s0", particle="ceramic", tool=None,
                   recipe=recipe(P("fff", "KDK", " K "), {"f": FLOWERS, "K": I("clay_ball")})))
    T.append(FType("birdhouse", "Birdhouse", "garden", "simple", {"main": F.birdhouse()}, "main",
                   recipe=recipe(P(" P ", "PDP", " S "), {"P": PLANKS, "S": I("stick")})))
    T.append(FType("picket_fence", "Picket Fence", "garden", "fence",
                   {"post": F.fence_post(), "side": F.fence_side(), "inventory": F.fence_inventory()}, "inventory",
                   recipe=recipe(P("SDS", "SPS"), {"S": I("stick"), "P": PLANKS}, 3)))
    T.append(FType("outdoor_table", "Outdoor Table", "garden", "simple", {"main": F.outdoor_table()}, "main",
                   recipe=recipe(P("PPP", "iDi", " i "), {"P": PLANKS, "i": I("iron_nugget")})))
    T.append(FType("picnic_blanket", "Picnic Blanket", "garden", "rug",
                   {"center": F.picnic_center(), "edge": F.picnic_edge()},
                   F.picnic_center() + _all_sides(F.picnic_edge), particle="gingham", tool=None,
                   recipe=recipe(P("CWC", "CDC"), {"C": CARPET, "W": I("white_wool")}, 2)))
    T.append(FType("swing_seat", "Swing Seat", "garden", "tall", tall(F.swing_seat), F.swing_seat(),
                   recipe=recipe(P("T T", "T T", "PDP"), {"T": I("string"), "P": PLANKS}), loot="lower"))
    return T


def _all_sides(build):
    from .core import rot_y90
    els = []
    for t in range(4):
        els += rot_y90(build(), t)
    return els


def _all_corners(build):
    return _all_sides(build)


TYPES = build_types()
ROOM_ORDER = ["bedroom", "living_room", "kitchen", "bathroom", "garden"]


# --------------------------------------------------------------------------- model output

def textures_for(els, color=None, overrides=None):
    tex = {}
    for v in sorted(tex_vars(els)):
        if v in COLOR_VARS:
            if color is None:
                continue
            tex[v] = f"{M}block/{color}/{v}"
        elif v in NEUTRAL:
            tex[v] = M + NEUTRAL[v]
        elif v in decor.SHARED:
            tex[v] = M + decor.SHARED[v]
        else:
            raise KeyError(f"unknown texture variable {v}")
    if overrides:
        for k, v in overrides.items():
            if k in tex:
                tex[k] = f"{M}block/{color}/{v}" if v in ("shade_lit",) else M + NEUTRAL.get(v, "block/" + v)
    return tex


def reaches_outside(els):
    """Vanilla ambient occlusion extrapolates (and can go black) for faces far outside the block."""
    for e in els:
        if e.frm[1] >= 16.5 or min(e.frm[0], e.frm[2]) < -0.5 or max(e.to[0], e.to[2]) > 16.5:
            return True
    return False


def template_model(name, els, display=None):
    check_range(name, els)
    model = {"ambientocclusion": not reaches_outside(els), "render_type": "minecraft:cutout",
             "textures": textures_for(els), "elements": serialise(els)}
    if display:
        model["display"] = display
    return model


def child_model(parent, els, color, particle, overrides=None):
    tex = {k: v for k, v in textures_for(els, color, overrides).items() if _is_color_or_override(k, overrides)}
    tex["particle"] = f"{M}block/{color}/{particle}"
    return {"parent": parent, "render_type": "minecraft:cutout", "textures": tex}


def _is_color_or_override(k, overrides):
    return k in COLOR_VARS or (overrides is not None and k in overrides)


def write_models(t):
    """Templates + one child per colour (and lit variants)."""
    item_els = t.templates[t.item] if isinstance(t.item, str) else centred_for_item(t.item)
    display = display_for(item_els)
    for var, els in t.templates.items():
        write_json(ASSETS / "models" / "block" / "template" / t.id / f"{var}.json", template_model(f"{t.id}/{var}", els, display))
        for col in COLORS:
            parent = f"{M}block/template/{t.id}/{var}"
            write_json(ASSETS / "models" / "block" / t.id / col / f"{var}.json", child_model(parent, els, col, t.particle))
            if t.lit:
                write_json(ASSETS / "models" / "block" / t.id / col / f"{var}_lit.json",
                           child_model(parent, els, col, t.particle, t.lit))
    if not isinstance(t.item, str):
        write_json(ASSETS / "models" / "block" / "template" / t.id / "item.json", template_model(f"{t.id}/item", item_els, display))
    for col in COLORS:
        bid = f"{col}_{t.id}"
        if isinstance(t.item, str):
            parent = f"{M}block/{t.id}/{col}/{t.item}"
            write_json(ASSETS / "models" / "item" / f"{bid}.json", {"parent": parent})
        else:
            write_json(ASSETS / "models" / "item" / f"{bid}.json",
                       child_model(f"{M}block/template/{t.id}/item", item_els, col, t.particle))


# --------------------------------------------------------------------------- blockstates

def variants(model_for, props):
    """Enumerate every combination of `props` ({name: values}) plus facing."""
    out = {}

    def rec(i, state):
        if i == len(names):
            model, y = model_for(state)
            key = ",".join(f"{k}={_s(state[k])}" for k in names)
            v = {"model": model}
            if y:
                v["y"] = y
            out[key] = v
            return
        for val in props[names[i]]:
            state[names[i]] = val
            rec(i + 1, state)

    names = list(props)
    rec(0, {})
    return {"variants": out}


def _s(v):
    if isinstance(v, bool):
        return "true" if v else "false"
    return str(v)


def blockstate(t, col):
    base = f"{M}block/{t.id}/{col}/"
    F4 = list(FACING_Y)
    k = t.kind
    if k == "simple":
        return variants(lambda s: (base + "main", FACING_Y[s["facing"]]), {"facing": F4})
    if k == "open":
        return variants(lambda s: (base + ("open" if s["open"] else "closed"), FACING_Y[s["facing"]]),
                        {"facing": F4, "open": [False, True]})
    if k == "sink":
        return variants(lambda s: (base + f"s{s['sink']}", FACING_Y[s["facing"]]), {"facing": F4, "sink": list(SINKS)})
    if k == "sink_open":
        return variants(lambda s: (base + f"{'open' if s['open'] else 'closed'}_s{s['sink']}", FACING_Y[s["facing"]]),
                        {"facing": F4, "open": [False, True], "sink": list(SINKS)})
    if k == "sink_lit":
        return variants(lambda s: (base + f"s{s['sink']}" + ("_lit" if s["lit"] else ""), FACING_Y[s["facing"]]),
                        {"facing": F4, "lit": [False, True], "sink": list(SINKS)})
    if k == "pillows":
        return variants(lambda s: (base + f"p{s['pillows']}_s{s['sink']}", FACING_Y[s["facing"]]),
                        {"facing": F4, "pillows": [1, 2, 3], "sink": list(SINKS)})
    if k == "tall":
        return variants(lambda s: (base + s["half"], FACING_Y[s["facing"]]), {"facing": F4, "half": ["lower", "upper"]})
    if k == "tall_open":
        return variants(lambda s: (base + s["half"] + ("_open" if s["open"] else ""), FACING_Y[s["facing"]]),
                        {"facing": F4, "half": ["lower", "upper"], "open": [False, True]})
    if k == "bed":
        return variants(lambda s: (base + s["part"], FACING_Y[s["facing"]]), {"facing": F4, "part": ["head", "foot"]})
    if k == "tub":
        return variants(lambda s: (base + s["part"] + ("_filled" if s["filled"] else ""), FACING_Y[s["facing"]]),
                        {"facing": F4, "part": ["head", "foot"], "filled": [False, True]})
    if k == "connect":
        return variants(lambda s: (base + s["shape"], FACING_Y[s["facing"]]), {"facing": F4, "shape": list(SHAPES4)})
    if k == "running":
        return variants(lambda s: (base + ("on" if s["running"] else "off"), FACING_Y[s["facing"]]),
                        {"facing": F4, "running": [False, True]})
    if k == "lit":
        return variants(lambda s: (base + ("main_lit" if s["lit"] else "main"), FACING_Y[s["facing"]]),
                        {"facing": F4, "lit": [False, True]})
    if k == "table":
        parts = [{"apply": {"model": base + "top"}}]
        for d, y in FACING_Y.items():
            parts.append({"when": {d: "false"}, "apply": {"model": base + "skirt", "y": y} if y else {"model": base + "skirt"}})
        corners = [("north", "west", 0), ("north", "east", 90), ("south", "east", 180), ("south", "west", 270)]
        for a, b, y in corners:
            apply = {"model": base + "leg"}
            if y:
                apply["y"] = y
            parts.append({"when": {a: "false", b: "false"}, "apply": apply})
        return {"multipart": parts}
    if k == "rug":
        parts = [{"apply": {"model": base + "center"}}]
        for d, y in FACING_Y.items():
            apply = {"model": base + "edge"}
            if y:
                apply["y"] = y
            parts.append({"when": {d: "false"}, "apply": apply})
        return {"multipart": parts}
    if k == "fence":
        parts = [{"apply": {"model": base + "post"}}]
        for d, y in FACING_Y.items():
            apply = {"model": base + "side", "uvlock": False}
            if y:
                apply["y"] = y
            parts.append({"when": {d: "true"}, "apply": apply})
        return {"multipart": parts}
    raise ValueError(k)


# --------------------------------------------------------------------------- loot, recipes, tags

def loot_table(bid, loot, prop_values=None):
    entry = {"type": "minecraft:item", "name": M + bid}
    if loot in ("head", "lower"):
        prop = {"head": ("part", "head"), "lower": ("half", "lower")}[loot]
        entry["conditions"] = [{"condition": "minecraft:block_state_property", "block": M + bid,
                                "properties": {prop[0]: prop[1]}}]
    elif loot in ("pillows", "books"):
        entry["functions"] = [{"function": "minecraft:set_count", "count": n, "add": False,
                               "conditions": [{"condition": "minecraft:block_state_property", "block": M + bid,
                                               "properties": {loot: str(n)}}]} for n in (2, 3)]
        entry["functions"].append({"function": "minecraft:explosion_decay"})
    pool = {"rolls": 1, "bonus_rolls": 0, "entries": [entry]}
    if loot not in ("pillows", "books"):
        pool["conditions"] = [{"condition": "minecraft:survives_explosion"}]
    return {"type": "minecraft:block", "pools": [pool], "random_sequence": f"{MODID}:blocks/{bid}"}


RECIPE_SIGS = {}


def shaped(name, pattern, key, result, count=1):
    sig = (tuple(pattern), tuple(sorted((k, json.dumps(v, sort_keys=True)) for k, v in key.items())))
    canon = tuple(row.translate(str.maketrans({k: json.dumps(v, sort_keys=True)[-24:] for k, v in key.items()})) for row in pattern)
    if canon in RECIPE_SIGS:
        raise ValueError(f"recipe collision: {name} vs {RECIPE_SIGS[canon]}")
    RECIPE_SIGS[canon] = name
    write_json(DATA / "recipes" / f"{name}.json", {
        "type": "minecraft:crafting_shaped", "category": "building", **_group(name),
        "pattern": pattern, "key": key, "result": {"item": M + result, "count": count}, "show_notification": False})


def _group(name):
    folder = name.split("/")[0]
    return {} if folder == "decor" else {"group": f"{MODID}:{folder}"}


def shapeless(name, ingredients, result, count=1, group=None):
    obj = {"type": "minecraft:crafting_shapeless", "category": "building", "ingredients": ingredients,
           "result": {"item": M + result, "count": count}}
    if group:
        obj["group"] = group
    write_json(DATA / "recipes" / f"{name}.json", obj)


def furniture_data(t, lang, tags):
    for col in COLORS:
        bid = f"{col}_{t.id}"
        write_json(ASSETS / "blockstates" / f"{bid}.json", blockstate(t, col))
        write_json(DATA / "loot_tables" / "blocks" / f"{bid}.json", loot_table(bid, t.loot))
        lang[f"block.{MODID}.{bid}"] = f"{COLOR_NAMES[col]} {t.name}"
        pattern, key, count = t.recipe
        key = dict(key)
        key["D"] = {"item": DYES[col]}
        shaped(f"{t.id}/{col}", pattern, key, bid, count)
        shapeless(f"{t.id}/{col}_from_dyeing", [{"tag": f"{MODID}:{t.id}"}, {"item": DYES[col]}], bid,
                  group=f"{MODID}:dye_{t.id}")
        tags["items"].setdefault(f"{MODID}:{t.id}", []).append(M + bid)
        tags["items"].setdefault(f"{MODID}:furniture", []).append(M + bid)
        tags["blocks"].setdefault(f"{MODID}:furniture", []).append(M + bid)
        if t.tool:
            tags["blocks"].setdefault(f"minecraft:mineable/{t.tool}", []).append(M + bid)
        if t.kind == "fence":
            for kind in ("blocks", "items"):
                tags[kind].setdefault("minecraft:fences", []).append(M + bid)
                tags[kind].setdefault("minecraft:wooden_fences", []).append(M + bid)


# --------------------------------------------------------------------------- plants & decorations

DECOR_RECIPES = {
    "book_stack": (P("BB"), {"B": I("book")}, 1),
    "rose_vase": (P("R", "Q"), {"R": I("rose_bush"), "Q": I("quartz")}, 1),
    "tulip_vase": (P("T", "G"), {"T": I("pink_tulip"), "G": I("glass_bottle")}, 1),
    "pastel_candles": (P("CCC", " g "), {"C": I("candle"), "g": I("gold_nugget")}, 1),
    "perfume_bottles": (P("GGG", " D "), {"G": I("glass_bottle"), "D": I("pink_dye")}, 1),
    "heart_music_box": (P(" g ", "PNP"), {"g": I("gold_nugget"), "P": PLANKS, "N": I("note_block")}, 1),
    "teddy_bear": (P(" W ", "WSW", " W "), {"W": I("pink_wool"), "S": I("string")}, 1),
    "bunny_plush": (P("W W", "WSW", " W "), {"W": I("white_wool"), "S": I("string")}, 1),
    "cupcake_stand": (P("SES", "QgQ"), {"S": I("sugar"), "E": I("egg"), "Q": I("quartz"), "g": I("gold_nugget")}, 1),
    "heart_photo_frame": (P("DFD"), {"D": I("pink_dye"), "F": I("item_frame")}, 1),
    "ribbon_gift_box": (P("SDS", "PPP"), {"S": I("string"), "D": I("pink_dye"), "P": I("paper")}, 1),
    "strawberry_basket": (P(" S ", "BBB", "HHH"), {"S": I("stick"), "B": I("sweet_berries"), "H": I("wheat")}, 1),
    "angel_figurine": (P(" g ", "FQF", " Q "), {"g": I("gold_nugget"), "F": I("feather"), "Q": I("quartz")}, 1),
    "potted_monstera": (P("J", "J", "F"), {"J": I("jungle_leaves"), "F": I("flower_pot")}, 1),
    "potted_rose_bush": (P("R", "F"), {"R": I("rose_bush"), "F": I("flower_pot")}, 1),
    "potted_lavender": (P("L", "F"), {"L": I("lilac"), "F": I("flower_pot")}, 1),
    "potted_cherry_blossom": (P("C", "F"), {"C": I("cherry_sapling"), "F": I("flower_pot")}, 1),
}

DECOR_NAMES = {
    "book_stack": "Book Stack", "rose_vase": "Rose Vase", "tulip_vase": "Tulip Vase", "pastel_candles": "Pastel Candles",
    "perfume_bottles": "Perfume Bottles", "heart_music_box": "Heart Music Box", "teddy_bear": "Teddy Bear",
    "bunny_plush": "Bunny Plush", "cupcake_stand": "Cupcake Stand", "heart_photo_frame": "Heart Photo Frame",
    "ribbon_gift_box": "Ribbon Gift Box", "strawberry_basket": "Strawberry Basket", "angel_figurine": "Angel Figurine",
    "potted_monstera": "Potted Monstera", "potted_rose_bush": "Potted Rose Bush", "potted_lavender": "Potted Lavender",
    "potted_cherry_blossom": "Potted Cherry Blossom",
}


def standalone_model(name, els, extra_tex, display):
    check_range(name, els)
    tex = {}
    for v in sorted(tex_vars(els)):
        if v in extra_tex:
            tex[v] = M + extra_tex[v]
        elif v in NEUTRAL:
            tex[v] = M + NEUTRAL[v]
        elif v in decor.SHARED:
            tex[v] = M + decor.SHARED[v]
        else:
            raise KeyError(f"{name}: unknown texture variable {v}")
    first = next(iter(tex.values()))
    tex["particle"] = tex.get("pot", tex.get("porcelain", first))
    model = {"ambientocclusion": False, "render_type": "minecraft:cutout", "textures": tex, "elements": serialise(els)}
    if display:
        model["display"] = display
    return model


def plants_and_decor(lang, tags):
    for pid, (build, tex) in decor.PLANTS.items():
        els = build()
        write_json(ASSETS / "models" / "block" / "plant" / f"{pid}.json", standalone_model(pid, els, tex, display_for(els)))
        write_json(ASSETS / "blockstates" / f"{pid}.json",
                   variants(lambda s, p=pid: (f"{M}block/plant/{p}", FACING_Y[s["facing"]]), {"facing": list(FACING_Y)}))
        write_json(ASSETS / "models" / "item" / f"{pid}.json", {"parent": f"{M}block/plant/{pid}"})
        _decor_common(pid, "self", lang, tags)

    for did, spec in decor.DECOR.items():
        if "variants" in spec:
            prop = spec["prop"]
            vals = sorted(spec["variants"])
            for n, build in spec["variants"].items():
                base = build()
                display = display_for(base)
                for k in SINKS:
                    write_json(ASSETS / "models" / "block" / "decor" / did / f"{n}_s{k}.json",
                               standalone_model(did, move(base, 0, -2 * k, 0), {}, display))
            write_json(ASSETS / "blockstates" / f"{did}.json", variants(
                lambda s, d=did, p=prop: (f"{M}block/decor/{d}/{s[p]}_s{s['sink']}", FACING_Y[s["facing"]]),
                {"facing": list(FACING_Y), prop: vals, "sink": list(SINKS)}))
            write_json(ASSETS / "models" / "item" / f"{did}.json", {"parent": f"{M}block/decor/{did}/1_s0"})
            _decor_common(did, prop, lang, tags)
            continue
        base = spec["build"]()
        display = display_for(base)
        lit = spec.get("lit")
        for k in SINKS:
            write_json(ASSETS / "models" / "block" / "decor" / did / f"s{k}.json",
                       standalone_model(did, move(base, 0, -2 * k, 0), {}, display))
            if lit:
                write_json(ASSETS / "models" / "block" / "decor" / did / f"s{k}_lit.json",
                           standalone_model(did, move(base + lit(), 0, -2 * k, 0), {}, display))
        props = {"facing": list(FACING_Y), "sink": list(SINKS)}
        if lit:
            props["lit"] = [False, True]
        write_json(ASSETS / "blockstates" / f"{did}.json", variants(
            lambda s, d=did: (f"{M}block/decor/{d}/s{s['sink']}" + ("_lit" if s.get("lit") else ""), FACING_Y[s["facing"]]),
            props))
        write_json(ASSETS / "models" / "item" / f"{did}.json", {"parent": f"{M}block/decor/{did}/s0"})
        _decor_common(did, "self", lang, tags)


def _decor_common(bid, loot, lang, tags):
    write_json(DATA / "loot_tables" / "blocks" / f"{bid}.json", loot_table(bid, loot))
    lang[f"block.{MODID}.{bid}"] = DECOR_NAMES[bid]
    pattern, key, count = DECOR_RECIPES[bid]
    shaped(f"decor/{bid}", pattern, key, bid, count)
    tags["items"].setdefault(f"{MODID}:decorations", []).append(M + bid)
    tags["blocks"].setdefault(f"{MODID}:decorations", []).append(M + bid)


# --------------------------------------------------------------------------- items & language

EXTRA_LANG = {
    "item.coquettefurniture.interior_design_catalog": "Interior Design Catalog",
    "item.coquettefurniture.interior_design_catalog.desc": "Right-click to open Interior Designer Mode",
    "item.coquettefurniture.cup_of_tea": "Cup of Strawberry Tea",
    "entity.coquettefurniture.seat": "Seat",
    "itemGroup.coquettefurniture": "Coquette Furniture",
    "itemGroup.coquettefurniture.decor": "Coquette Décor",
    "key.categories.coquettefurniture": "Coquette Furniture",
    "key.coquettefurniture.catalog": "Open Interior Design Catalog",
    "container.coquettefurniture.stove": "Stove",
    "container.coquettefurniture.wardrobe": "Wardrobe",
    "container.coquettefurniture.refrigerator": "Refrigerator",
    "container.coquettefurniture.drawers": "Drawers",
    "container.coquettefurniture.cabinet": "Cabinet",
    "container.coquettefurniture.jewelry_box": "Jewelry Box",
    "container.coquettefurniture.vanity": "Vanity Drawer",
    "room.coquettefurniture.all": "Everything",
    "room.coquettefurniture.bedroom": "Bedroom",
    "room.coquettefurniture.living_room": "Living Room",
    "room.coquettefurniture.kitchen": "Kitchen",
    "room.coquettefurniture.bathroom": "Bathroom",
    "room.coquettefurniture.garden": "Garden",
    "room.coquettefurniture.decor": "Décor",
    "room.coquettefurniture.sets": "Themed Sets",
    "color.coquettefurniture.all": "All colours",
    "color.coquettefurniture.baby_pink": "Baby Pink",
    "color.coquettefurniture.white": "White",
    "color.coquettefurniture.cream": "Cream",
    "color.coquettefurniture.lavender": "Lavender",
    "color.coquettefurniture.mint": "Mint",
    "color.coquettefurniture.baby_blue": "Baby Blue",
    "set.coquettefurniture.princess_bedroom": "Princess Bedroom",
    "set.coquettefurniture.princess_bedroom.desc": "Baby pink canopy bed, vanity and wardrobe, finished with gold and pearls.",
    "set.coquettefurniture.strawberry_cottage": "Strawberry Cottage",
    "set.coquettefurniture.strawberry_cottage.desc": "Cream and pink cottage kitchen and garden with gingham picnics and berry baskets.",
    "set.coquettefurniture.lavender_dreams": "Lavender Dreams",
    "set.coquettefurniture.lavender_dreams.desc": "A sleepy lilac bedroom and bath scented with potted lavender.",
    "set.coquettefurniture.angelcore_room": "Angelcore Room",
    "set.coquettefurniture.angelcore_room.desc": "Cloud white and sky blue, with angels, candles and soft lace.",
    "set.coquettefurniture.pastel_kitchen": "Pastel Kitchen",
    "set.coquettefurniture.pastel_kitchen.desc": "A rainbow of pastel appliances: mint fridge, baby blue stove, cream counters.",
    "tooltip.coquettefurniture.set": "❀ %s set",
    "tooltip.coquettefurniture.feature.seat": "Sit on it",
    "tooltip.coquettefurniture.feature.storage": "Stores %s stacks",
    "tooltip.coquettefurniture.feature.bed": "Sleep in it",
    "tooltip.coquettefurniture.feature.light": "Gives off light",
    "tooltip.coquettefurniture.feature.surface": "Decorations sit on top",
    "tooltip.coquettefurniture.feature.water": "Endless water for buckets and bottles",
    "tooltip.coquettefurniture.feature.cooking": "Cooks food like a smoker",
    "tooltip.coquettefurniture.feature.display": "Shows off 4 items",
    "tooltip.coquettefurniture.feature.connects": "Connects to its neighbours",
    "tooltip.coquettefurniture.feature.enchanting": "Powers enchanting tables",
    "tooltip.coquettefurniture.feature.pamper": "Pamper yourself",
    "tooltip.coquettefurniture.feature.music": "Plays a little melody",
    "tooltip.coquettefurniture.feature.tea": "Pours cups of tea",
    "tooltip.coquettefurniture.feature.toggle": "Opens and closes",
    "tooltip.coquettefurniture.feature.bath": "Fill with water and take a relaxing bath",
    "tooltip.coquettefurniture.feature.decor": "Sinks onto tables, shelves and counters",
    "tooltip.coquettefurniture.feature.stack": "Stack up to 3",
    "tooltip.coquettefurniture.feature.dye": "Right-click with a dye to recolour",
    "tooltip.coquettefurniture.feature.birds": "Birds come to visit",
    "tooltip.coquettefurniture.feature.candles": "Light with flint and steel",
    "gui.coquettefurniture.catalog.title": "Interior Design Catalog",
    "gui.coquettefurniture.catalog.search": "Search furniture…",
    "gui.coquettefurniture.catalog.items": "%s pieces",
    "gui.coquettefurniture.catalog.take": "Take one",
    "gui.coquettefurniture.catalog.take_stack": "Take stack",
    "gui.coquettefurniture.catalog.take_set": "Take the whole set",
    "gui.coquettefurniture.catalog.recipe": "Recipe",
    "gui.coquettefurniture.catalog.no_recipe": "No crafting recipe",
    "gui.coquettefurniture.catalog.features": "Features",
    "gui.coquettefurniture.catalog.survival": "Craft it at a crafting table",
    "gui.coquettefurniture.catalog.select": "Pick a piece to see its details",
    "gui.coquettefurniture.catalog.pieces": "%s pieces in this set",
    "message.coquettefurniture.compliment.0": "You look absolutely lovely today ♡",
    "message.coquettefurniture.compliment.1": "Mirror, mirror… it's you! ♡",
    "message.coquettefurniture.compliment.2": "That outfit is perfection ❀",
    "message.coquettefurniture.compliment.3": "Glowing like a pearl ✧",
    "message.coquettefurniture.compliment.4": "A bow a day keeps the frowns away ♡",
    "message.coquettefurniture.compliment.5": "Your hair is giving princess ♕",
    "message.coquettefurniture.compliment.6": "Pretty in pastel ❀",
    "message.coquettefurniture.compliment.7": "Sparkly, sweet and strong ✧",
    "message.coquettefurniture.pampered": "You feel pampered and refreshed ✧",
    "message.coquettefurniture.vanity_cooldown": "Your makeup is still perfect ♡",
    "message.coquettefurniture.tea_poured": "You pour a cup of strawberry tea ☕",
    "message.coquettefurniture.tea_steam": "The teapot steams happily… bring a glass bottle for a cup",
    "message.coquettefurniture.soap": "Squeaky clean! ♡",
    "message.coquettefurniture.seat_occupied": "Someone is already sitting there",
    "message.coquettefurniture.bath_relax": "Ahh… a relaxing bubble bath",
    "message.coquettefurniture.catalog_given": "Delivered %s ♡",
    "message.coquettefurniture.catalog_creative_only": "Catalog delivery only works in creative mode",
}


def item_models(lang):
    for iid in ("interior_design_catalog", "cup_of_tea"):
        write_json(ASSETS / "models" / "item" / f"{iid}.json",
                   {"parent": "minecraft:item/generated", "textures": {"layer0": f"{M}item/{iid}"}})
    shapeless("interior_design_catalog", [{"item": "minecraft:book"}, {"item": "minecraft:pink_dye"}], "interior_design_catalog")
    shapeless("cup_of_tea", [{"item": "minecraft:glass_bottle"}, {"item": "minecraft:sweet_berries"},
                             {"item": "minecraft:sugar"}], "cup_of_tea")
    lang.update(EXTRA_LANG)


def write_tags(tags):
    for kind, entries in tags.items():
        for tag, values in entries.items():
            ns, path = tag.split(":")
            root = (ROOT / "data" / ns / "tags" / kind / path).with_suffix(".json")
            write_json(root, {"replace": False, "values": values})


def generate():
    lang = {}
    tags = {"blocks": {}, "items": {}}
    for t in TYPES:
        write_models(t)
        furniture_data(t, lang, tags)
    plants_and_decor(lang, tags)
    item_models(lang)
    write_tags(tags)
    write_json(ASSETS / "lang" / "en_us.json", dict(sorted(lang.items())))
    ids = [f"{c}_{t.id}" for t in TYPES for c in COLORS] + list(decor.PLANTS) + list(decor.DECOR)
    (ROOT.parents[2] / "tools" / "generated_ids.txt").write_text("\n".join(ids) + "\n", encoding="utf-8")
    return ids
