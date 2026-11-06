#!/usr/bin/env python3
"""Generates every Coquette Furniture asset: textures, template models and their
per-colour children, blockstates, item models, loot tables, recipes, tags and lang.

    python tools/generate_assets.py

The output folders (assets/coquettefurniture, data/coquettefurniture and the
data/minecraft tags) are wiped and fully regenerated on every run, then every
blockstate, model and texture reference is checked.
"""
import gzip
import json
import shutil
import struct
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from cfgen import data, textures  # noqa: E402
from cfgen.core import ASSETS, DATA, MC_DATA, MODID  # noqa: E402

KEEP = {ASSETS / "textures" / "gui"}


def wipe():
    for root in (ASSETS, DATA, MC_DATA):
        if not root.exists():
            continue
        for child in root.iterdir():
            if child in KEEP:
                continue
            shutil.rmtree(child) if child.is_dir() else child.unlink()


def empty_structure(size=(6, 5, 6)):
    """A blank structure template (NBT) used by the GameTests."""
    def name(text):
        raw = text.encode("utf-8")
        return struct.pack(">H", len(raw)) + raw

    body = b"\x03" + name("DataVersion") + struct.pack(">i", 3465)
    body += b"\x09" + name("size") + b"\x03" + struct.pack(">i", 3) + b"".join(struct.pack(">i", v) for v in size)
    for key in ("palette", "blocks", "entities"):
        body += b"\x09" + name(key) + b"\x00" + struct.pack(">i", 0)
    nbt = b"\x0a" + name("") + body + b"\x00"
    out = DATA / "structures" / "empty.nbt"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_bytes(gzip.compress(nbt, mtime=0))


def resolve(ref):
    ns, path = ref.split(":") if ":" in ref else ("minecraft", ref)
    return ns, path


def validate(ids):
    problems = []
    models = {}

    def load_model(ref):
        if ref in models:
            return models[ref]
        ns, path = resolve(ref)
        if ns != MODID:
            models[ref] = None
            return None
        f = ASSETS / "models" / (path + ".json")
        if not f.exists():
            problems.append(f"missing model {ref}")
            models[ref] = None
            return None
        models[ref] = json.loads(f.read_text(encoding="utf-8"))
        return models[ref]

    def textures_of(ref):
        chain = []
        cur = ref
        while cur:
            m = load_model(cur)
            if m is None:
                break
            chain.append(m)
            cur = m.get("parent")
        tex = {}
        for m in reversed(chain):
            tex.update(m.get("textures", {}))
        elements = next((m["elements"] for m in chain if "elements" in m), [])
        return tex, elements, chain

    def check_model(ref):
        tex, elements, chain = textures_of(ref)
        if not chain:
            return
        for el in elements:
            for face in el["faces"].values():
                var = face["texture"].lstrip("#")
                seen = set()
                while var in tex and tex[var].startswith("#") and var not in seen:
                    seen.add(var)
                    var = tex[var].lstrip("#")
                if var not in tex:
                    problems.append(f"{ref}: unresolved texture #{var}")
        for v in tex.values():
            if v.startswith("#"):
                continue
            ns, path = resolve(v)
            if ns == MODID and not (ASSETS / "textures" / (path + ".png")).exists():
                problems.append(f"{ref}: missing texture {v}")

    for bid in ids:
        bs = ASSETS / "blockstates" / f"{bid}.json"
        if not bs.exists():
            problems.append(f"missing blockstate {bid}")
            continue
        state = json.loads(bs.read_text(encoding="utf-8"))
        refs = [v["model"] for v in state.get("variants", {}).values()]
        refs += [p["apply"]["model"] for p in state.get("multipart", [])]
        for ref in set(refs):
            check_model(ref)
        check_model(f"{MODID}:item/{bid}")
        if not (DATA / "loot_tables" / "blocks" / f"{bid}.json").exists():
            problems.append(f"missing loot table {bid}")
    for iid in ("interior_design_catalog", "cup_of_tea"):
        check_model(f"{MODID}:item/{iid}")
    return problems


def main():
    wipe()
    textures.generate()
    ids = data.generate()
    empty_structure()
    problems = validate(ids)
    pngs = sum(1 for _ in ASSETS.rglob("*.png"))
    jsons = sum(1 for _ in ASSETS.parent.parent.rglob("*.json"))
    print(f"{len(ids)} blocks, {pngs} textures, {jsons} json files")
    if problems:
        print(f"{len(problems)} problems:")
        for p in problems[:60]:
            print("  " + p)
        sys.exit(1)
    print("all references resolved")


if __name__ == "__main__":
    main()
