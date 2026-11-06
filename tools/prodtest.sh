#!/usr/bin/env bash
# Production server test for a release jar: installs a real server for the loader with its official installer
# (Fabric server launcher + Fabric API, Forge or NeoForge installer), adds the built mod jar, starts the server,
# waits for "Done", stops it and prints any error lines.
#
#   tools/prodtest.sh 1.20.1 forge         # also: fabric; neoforge / fabric for 1.21.1, 26.2, 26.3
#
# Uses <version>/gradle.properties for the loader versions and <version>/<loader>/build/libs for the mod jar.
# Servers are cached in $PRODTEST_DIR (default /tmp/coquette-prodtest). Needs a JDK for the version on PATH or in
# $JAVA (Java 17 for 1.20.1, 21 for 1.21.1, 25 for 26.x).
set -u
ROOT=$(cd "$(dirname "$0")/.." && pwd)
VERSION=$1; LOADER=$2
BASE=${PRODTEST_DIR:-/tmp/coquette-prodtest}; DIR="$BASE/$VERSION-$LOADER"; mkdir -p "$DIR"
JAVA=${JAVA:-java}
# run.sh scripts call plain `java`
[ "$JAVA" != java ] && export PATH="$(dirname "$JAVA"):$PATH"
prop() { grep "^$1=" "$ROOT/$VERSION/gradle.properties" | cut -d= -f2-; }
MC=$(prop minecraft_version)
JAR=$(ls "$ROOT/$VERSION/$LOADER/build/libs/"coquettefurniture-$LOADER-$VERSION-*.jar | grep -v sources | head -1)
[ -f "$JAR" ] || { echo "no mod jar for $VERSION $LOADER; build first"; exit 1; }
cd "$DIR"
rm -rf mods world logs config/coquettefurniture-*.toml; mkdir -p mods
case "$LOADER" in
  fabric)
    LOADER_V=$(prop fabric_loader_version); API_V=$(prop fabric_version)
    INST=$(curl -s https://meta.fabricmc.net/v2/versions/installer | python3 -c "import json,sys;print(json.load(sys.stdin)[0]['version'])")
    [ -f server.jar ] || curl -sfo server.jar "https://meta.fabricmc.net/v2/versions/loader/$MC/$LOADER_V/$INST/server/jar"
    curl -sfo mods/fabric-api.jar "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/$API_V/fabric-api-$API_V.jar"
    RUN=("$JAVA" -Xmx2G -jar server.jar nogui) ;;
  forge)
    V="$MC-$(prop forge_version)"
    if [ ! -f run.sh ]; then
      curl -sfo installer.jar "https://maven.minecraftforge.net/net/minecraftforge/forge/$V/forge-$V-installer.jar"
      "$JAVA" -jar installer.jar --installServer > install.log 2>&1 || { tail install.log; exit 1; }
    fi
    echo "-Xmx2G" > user_jvm_args.txt; RUN=(bash run.sh nogui) ;;
  neoforge)
    V=$(prop neoforge_version)
    if [ ! -f run.sh ]; then
      curl -sfo installer.jar "https://maven.neoforged.net/releases/net/neoforged/neoforge/$V/neoforge-$V-installer.jar"
      "$JAVA" -jar installer.jar --installServer > install.log 2>&1 || { tail install.log; exit 1; }
    fi
    echo "-Xmx2G" > user_jvm_args.txt; RUN=(bash run.sh nogui) ;;
esac
cp "$JAR" mods/
echo "eula=true" > eula.txt
printf '%s\n' online-mode=false server-port=25580 spawn-monsters=false > server.properties
mkfifo -m 600 in.fifo 2>/dev/null || true
( "${RUN[@]}" < in.fifo > server.log 2>&1; echo "exit $?" >> server.log ) &
exec 3> in.fifo
for i in $(seq 1 400); do grep -qE 'Done \(|exit [0-9]' server.log && break; sleep 1; done
if grep -q 'Done (' server.log; then sleep 3; echo stop >&3; else echo "SERVER DID NOT START"; fi
exec 3>&-; wait
echo "---- $VERSION $LOADER ($(basename "$JAR"))"
grep -hE 'Done \(|Found valid mod file coquettefurniture|- coquettefurniture ' server.log logs/debug.log 2>/dev/null | head -3
[ -f config/coquettefurniture-common.toml ] && echo "config/coquettefurniture-common.toml written (the mod initialised)" \
  || echo "MOD DID NOT INITIALISE (no config file)"
echo "---- problems"
grep -nE 'ERROR|Exception|FATAL|\bWARN\b.*(coquette|Coquette)' server.log | grep -vE 'server.properties|Appender DebugFile|kqueue|Only supported on OSX' | head -40
echo "---- end"
