# Coquette Furniture 1.0.0 "Sweet Home": release files

Each jar targets one Minecraft version and one loader. On CurseForge, upload each file separately and tick the matching game version, loader and Java version. On Fabric, add Fabric API as a required dependency. Paste `patchnotes/1.0.0.md` as the changelog.

| File | Minecraft | Loader | Java | Required dependencies |
|---|---|---|---|---|
| `coquettefurniture-forge-1.20.1-1.0.0.jar` | 1.20.1 | Forge 47+ | 17 | None |
| `coquettefurniture-fabric-1.20.1-1.0.0.jar` | 1.20.1 | Fabric | 17 | Fabric API |
| `coquettefurniture-neoforge-1.21.1-1.0.0.jar` | 1.21.1 | NeoForge 21.1+ | 21 | None |
| `coquettefurniture-fabric-1.21.1-1.0.0.jar` | 1.21.1 | Fabric | 21 | Fabric API |
| `coquettefurniture-neoforge-26.2-1.0.0.jar` | 26.2 | NeoForge | 25 | None |
| `coquettefurniture-fabric-26.2-1.0.0.jar` | 26.2 | Fabric | 25 | Fabric API |
| `coquettefurniture-neoforge-26.3-1.0.0.jar` | 26.3 | NeoForge | 25 | None |
| `coquettefurniture-fabric-26.3-1.0.0.jar` | 26.3 | Fabric | 25 | Fabric API |

Built from this commit with `./gradlew build` in each version folder. Every jar was checked three ways:

- `tools/prodtest.sh`: loaded on a real dedicated server installed with the loader's official installer (and Fabric API on Fabric); the server started, the mod initialised and wrote its config, and the log showed no errors from the mod.
- `tools/devtest.sh`: the development server and client started cleanly, and the client built a furnished world and saved screenshots of every room and the catalog.
- GameTests: all 14 pass on Forge 1.20.1 and NeoForge 1.21.1.
