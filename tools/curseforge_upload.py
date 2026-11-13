#!/usr/bin/env python3
"""Upload the release jars that are not on CurseForge yet.

Reads the CurseForge upload token from CURSEFORGE_TOKEN. Lists the files the
project already has (through the public cfwidget API), and uploads every jar in
release/<version>/ whose file name is not among them, with the matching
Minecraft version, loader, Java version and environment ticked, the patch notes
as the changelog, and Fabric API as a required dependency on Fabric.

    CURSEFORGE_TOKEN=... python3 tools/curseforge_upload.py [--dry-run]

Standard library only, so it runs on a bare GitHub Actions runner.
"""
import argparse
import json
import os
import re
import sys
import time
import urllib.error
import urllib.request
import uuid
from pathlib import Path

PROJECT_ID = 1787099
PROJECT_SLUG = "coquette-furniture"
MOD_VERSION = "1.0.0"
API = "https://minecraft.curseforge.com/api"
ROOT = Path(__file__).resolve().parent.parent
RELEASE_DIR = ROOT / "release" / MOD_VERSION
CHANGELOG = ROOT / "patchnotes" / f"{MOD_VERSION}.md"

JAR_RE = re.compile(r"^coquettefurniture-(forge|neoforge|fabric)-([0-9.]+)-" + re.escape(MOD_VERSION) + r"\.jar$")
LOADER_NAMES = {"forge": "Forge", "neoforge": "NeoForge", "fabric": "Fabric"}


def java_for(mc):
    if mc.startswith("1.20"):
        return "Java 17"
    if mc.startswith("1.21"):
        return "Java 21"
    return "Java 25"


def request(url, token=None, data=None, headers=None, retries=4):
    headers = dict(headers or {})
    headers.setdefault("User-Agent", "coquettefurniture-release-uploader")
    if token:
        headers["X-Api-Token"] = token
    for attempt in range(retries):
        req = urllib.request.Request(url, data=data, headers=headers, method="POST" if data else "GET")
        try:
            with urllib.request.urlopen(req, timeout=120) as resp:
                return json.loads(resp.read().decode())
        except urllib.error.HTTPError as e:
            body = e.read().decode(errors="replace")
            if e.code >= 500 and attempt < retries - 1:
                time.sleep(2 ** (attempt + 1))
                continue
            raise SystemExit(f"HTTP {e.code} from {url}: {body}")
        except urllib.error.URLError as e:
            if attempt < retries - 1:
                time.sleep(2 ** (attempt + 1))
                continue
            raise SystemExit(f"Could not reach {url}: {e}")


def existing_file_names():
    data = request(f"https://api.cfwidget.com/minecraft/mc-mods/{PROJECT_SLUG}")
    if data.get("id") != PROJECT_ID:
        raise SystemExit(f"cfwidget returned project {data.get('id')}, expected {PROJECT_ID}")
    return {f["name"] for f in data.get("files", [])}


def game_version_ids(token):
    types = {t["id"]: t["slug"] for t in request(f"{API}/game/version-types", token)}
    versions = request(f"{API}/game/versions", token)

    def find(name, slug_pred):
        hits = [v["id"] for v in versions if v["name"] == name and slug_pred(types.get(v["gameVersionTypeID"], ""))]
        if not hits:
            raise SystemExit(f"CurseForge has no game version named {name!r} of the expected type")
        return hits[0]

    def ids(loader, mc):
        return [
            find(mc, lambda s: s.startswith("minecraft-")),
            find(LOADER_NAMES[loader], lambda s: s == "modloader"),
            find(java_for(mc), lambda s: s == "java"),
            find("Client", lambda s: s == "environment"),
            find("Server", lambda s: s == "environment"),
        ]

    return ids


def multipart(fields, file_field, file_path):
    boundary = uuid.uuid4().hex
    parts = []
    for name, value in fields.items():
        parts.append(f'--{boundary}\r\nContent-Disposition: form-data; name="{name}"\r\n\r\n{value}\r\n'.encode())
    parts.append(
        f'--{boundary}\r\nContent-Disposition: form-data; name="{file_field}"; filename="{file_path.name}"\r\n'
        f"Content-Type: application/java-archive\r\n\r\n".encode()
    )
    parts.append(file_path.read_bytes())
    parts.append(f"\r\n--{boundary}--\r\n".encode())
    return b"".join(parts), f"multipart/form-data; boundary={boundary}"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true", help="show what would be uploaded, upload nothing")
    args = ap.parse_args()

    jars = sorted(p for p in RELEASE_DIR.glob("*.jar") if JAR_RE.match(p.name))
    if not jars:
        raise SystemExit(f"No release jars in {RELEASE_DIR}")

    existing = existing_file_names()
    print(f"Already on CurseForge: {sorted(existing) or 'nothing'}")
    missing = [p for p in jars if p.name not in existing]
    if not missing:
        print("Every release jar is already on CurseForge.")
        return

    token = os.environ.get("CURSEFORGE_TOKEN", "").strip()
    if not token:
        if args.dry_run:
            for p in missing:
                print(f"Would upload {p.name}")
            return
        raise SystemExit("CURSEFORGE_TOKEN is not set. Add it as a repository secret (Settings > Secrets and variables > Actions).")

    ids_for = game_version_ids(token)
    changelog = CHANGELOG.read_text(encoding="utf-8")
    failures = 0
    for jar in missing:
        loader, mc = JAR_RE.match(jar.name).groups()
        metadata = {
            "changelog": changelog,
            "changelogType": "markdown",
            "displayName": jar.name,
            "gameVersions": ids_for(loader, mc),
            "releaseType": "release",
        }
        if loader == "fabric":
            metadata["relations"] = {"projects": [{"slug": "fabric-api", "type": "requiredDependency"}]}
        print(f"{'Would upload' if args.dry_run else 'Uploading'} {jar.name} (game versions {metadata['gameVersions']})")
        if args.dry_run:
            continue
        body, ctype = multipart({"metadata": json.dumps(metadata)}, "file", jar)
        try:
            result = request(f"{API}/projects/{PROJECT_ID}/upload-file", token, body, {"Content-Type": ctype})
            print(f"  uploaded, file id {result.get('id')}")
        except SystemExit as e:
            print(f"  FAILED: {e}", file=sys.stderr)
            failures += 1
    if failures:
        raise SystemExit(f"{failures} upload(s) failed")


if __name__ == "__main__":
    main()
