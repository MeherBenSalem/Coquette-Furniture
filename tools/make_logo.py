#!/usr/bin/env python3
"""Builds the mod-list logo from real furniture renders on a pastel, lace-trimmed card.

    python tools/make_logo.py      (run after generate_assets.py)
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from cfgen.core import ROOT, Img, mix  # noqa: E402
from preview_models import render  # noqa: E402

W, H = 400, 200
PIECES = [
    ("coquettefurniture:item/baby_pink_canopy_bed", 4.2, (6, 18)),
    ("coquettefurniture:item/lavender_wardrobe", 3.6, (128, 12)),
    ("coquettefurniture:item/baby_pink_vanity_table", 4.6, (210, 30)),
    ("coquettefurniture:block/sofa/baby_blue/single", 5.4, (285, 92)),
    ("coquettefurniture:item/teddy_bear", 7.5, (318, 20)),
    ("coquettefurniture:item/potted_cherry_blossom", 4.4, (176, 92)),
    ("coquettefurniture:item/mint_tea_set", 6.0, (96, 140)),
]


def main():
    logo = Img(W, H)
    for y in range(H):
        for x in range(W):
            logo.set(x, y, mix("#FFF4F8", "#FBD3E2", y / H))
    for x in range(0, W, 10):
        for y in (0, H - 4):
            logo.rect(x + 1, y, x + 9, y + 4, (242, 167, 195, 255))
    for y in range(0, H, 10):
        for x in (0, W - 4):
            logo.rect(x, y + 1, x + 4, y + 9, (242, 167, 195, 255))
    for ref, scale, (ox, oy) in PIECES:
        tile = render(ref, scale)
        for y in range(tile.h):
            for x in range(tile.w):
                c = tile.get(x, y)
                if c[3]:
                    logo.set(ox + x, oy + y, c)
    logo.save(ROOT / "coquettefurniture_logo.png")
    print("wrote", ROOT / "coquettefurniture_logo.png")


if __name__ == "__main__":
    main()
