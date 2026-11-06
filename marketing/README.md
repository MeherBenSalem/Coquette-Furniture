# Marketing assets: 1.0 "Sweet Home"

Listing copy and artwork for CurseForge and Modrinth. Nothing here ships in the mod JAR.

## Listing

| File | Use |
| --- | --- |
| [`curseforge/description.md`](curseforge/description.md) | Full project description (Markdown; also works on Modrinth) |
| [`curseforge/summary.txt`](curseforge/summary.txt) | Short project summary (215 characters) |
| `images/icon.png` | Project avatar |

Image links in the description point at `raw.githubusercontent.com/MeherBenSalem/Coquette-Furniture/main/marketing/images/...`, so they work once this folder is on `main`. The repository is private: either make it (or a copy of `marketing/images`) public, or upload the images to the CurseForge project gallery and replace each URL.

The description leads with the pitch, then the furniture room by room (bedroom, living room, kitchen, bathroom, garden), decorations, Interior Designer Mode, the furniture sets, the six colourways and recolouring, recipes, configuration, everything else, downloads and compatibility, the FAQ and a gallery.

## Adding a Minecraft version

The supported versions are listed in these places:

1. `VERSIONS` at the top of [`source/render.cjs`](source/render.cjs). Add one line, for example `{ minecraft: '26.4', loaders: ['NeoForge', 'Fabric'] },`. The banner's loader pills and version line are built from it. Then rerun the script for `banner`.
2. The table under **Downloads & Compatibility** in `curseforge/description.md`. Add one row per loader.
3. The FAQ answer **Is there a Fabric or NeoForge version?** in the same file.

No other copy or artwork names a Minecraft version. A new release name or number goes in `RELEASE` in `render.cjs` and in the banner's alt text on the first line of the description.

## Images

| File | Size | Use |
| --- | --- | --- |
| `images/banner.png` | 1800×600 | Description header: the Princess Bedroom, the title, "1.0 · Sweet Home", the loader pills and the version line |
| `images/icon.png` | 512×512 | Project avatar: the mod logo's canopy bed and wardrobe in a pink and lavender ring |
| `images/card-rooms.png` | 1600×900 | Four of the five rooms |
| `images/card-bedroom.png` | 1600×900 | The Princess Bedroom and its open wardrobe and drawers; storage sizes and the lamp's light |
| `images/card-living-room.png` | 1600×900 | The Angelcore living room: joined sofa, rug, coffee tables with a tea set |
| `images/card-kitchen.png` | 1600×900 | The Pastel Kitchen; fridge, stove and sink numbers |
| `images/card-bathroom.png` | 1600×900 | The Lavender Dreams bath corner with a filled tub and a running sink |
| `images/card-garden.png` | 1600×900 | The Strawberry Cottage garden |
| `images/card-decor.png` | 1600×900 | Decorations sunk onto the coffee table and the bedside tables |
| `images/card-night.png` | 1600×900 | The Princess Bedroom at midnight, lit by the lamp and candles |
| `images/card-catalog.png` | 1600×900 | The Interior Design Catalog on the Bedroom and Kitchen tabs |
| `images/card-sets.png` | 1600×900 | The catalog's Themed Sets page and the five sets |
| `images/card-colours.png` | 1600×900 | Every colourway of six pieces, and the dye for each |
| `images/card-config.png` | 1600×900 | Excerpts of the two config files |
| `images/header-*.png` | 1400×120, transparent | 16 section headings for the description |
| `images/divider.png` | 1400×40, transparent | Section divider |
| `images/screenshots/*.jpg` | 1920×1080 | 13 captures: gallery uploads and card art |

### How the screenshots were captured

Every screenshot is an unedited capture of the **1.20.1 Forge development client** (the furniture looks the same on every loader and version), Coquette Furniture 1.0.0 with the default configuration, rendered in software (Mesa llvmpipe) under Xvfb at 1920×1080, GUI scale 3 (4 for the catalog) and Fancy graphics.

`MarketingShots` (`1.20.1/forge/src/main/java/com/sugarplumstudios/coquette/dev/`, development only, left out of the jar) does the whole session: it creates a flat creative world, builds one open "dollhouse" room per furniture set (two walls with a window, a garden around it), places the furniture with its real block states (open doors and drawers, lit lamps and candles, a filled bathtub, running taps, a lit stove), lets joining pieces settle, puts four items in the display cabinet, then teleports the camera to each shot with the HUD hidden and saves a screenshot. The night shot is at time 18000 with no Night Vision. The catalog shots open the real Interior Design Catalog screen on the Bedroom tab, the Kitchen tab and the Themed Sets page.

To retake them (needs a 1920×1080 display, for example `Xvfb :90 -screen 0 1920x1080x24`):

```bash
cd 1.20.1 && DISPLAY=:90 ./gradlew :forge:runClient -Pmarketing
```

The captures land in `1.20.1/forge/run/screenshots/marketing_*.png`. Convert each to `images/screenshots/<name>.jpg` (quality 90), for example with ImageMagick:

```bash
for f in 1.20.1/forge/run/screenshots/marketing_*.png; do n=${f##*/marketing_}; convert "$f" -quality 90 "marketing/images/screenshots/${n%.png}.jpg"; done
```

## Regenerating the artwork

The cards, banner, icon and headers are HTML rendered with Playwright from the screenshots, the mod's own logo and the bundled fonts:

```bash
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs
```

```bash
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs banner icon
```

- `source/backdrop.jpg` is the blurred card background, a 1280×720 copy of `screenshots/princess-bedroom.jpg`.
- `source/logo.png` is the mod logo (`1.20.1/common/src/main/resources/coquettefurniture_logo.png`).
- `source/fonts/` holds Fredoka and Nunito with their SIL Open Font License 1.1 texts. The script uses only these files.

The colourways, dyes and sets on the cards are written at the top of `render.cjs`, copied from `block/FurnitureColor.java` and `catalog/FurnitureSet.java`. Update them when those change. Card crops are pixel regions of the 1920×1080 captures (`region()` calls), so new captures may need their crops adjusted.

## Wording

Every number in the copy and on the cards (slots, light levels, the vanity cooldown, set sizes, recipe counts) comes from the code, a recipe or the generated config. Update the copy when those change. Call the six colours **colourways**, the catalog's creative mode **Interior Designer Mode**, and the five collections **furniture sets**.
