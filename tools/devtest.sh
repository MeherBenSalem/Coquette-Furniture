#!/usr/bin/env bash
# Load test for one workspace and loader: starts the dev server (waits for "Done", stops it over RCON),
# then the dev client under Xvfb with the showcase harness (it creates a flat world, furnishes rooms,
# takes screenshots and quits). Prints every WARN/ERROR line that mentions the mod or an exception.
#
#   tools/devtest.sh 1.20.1 forge          # also: fabric (1.20.1), neoforge / fabric (1.21.1, 26.2, 26.3)
#   tools/devtest.sh 1.20.1 forge server   # server only (or: client)
#
# Needs Xvfb and Mesa (software OpenGL) for the client. Logs go to $DEVTEST_LOGS (default /tmp/coquette-devtest).
set -u
ROOT=$(cd "$(dirname "$0")/.." && pwd)
VERSION=$1; LOADER=$2; WHAT=${3:-both}
WS="$ROOT/$VERSION"; RUN="$WS/$LOADER/run"
LOGS=${DEVTEST_LOGS:-/tmp/coquette-devtest}; mkdir -p "$LOGS"
TAG="$VERSION-$LOADER"
case "$VERSION" in 1.20.1) N=0;; 1.21.1) N=1;; 26.2) N=2;; *) N=3;; esac
RCON=$((25590 + N)); PORT=$((25570 + N)); DISP=":$((77 + N))"
[ "$LOADER" = fabric ] && RUN="$WS/fabric/run"
mkdir -p "$RUN"
echo "eula=true" > "$RUN/eula.txt"
printf '%s\n' online-mode=false enable-rcon=true rcon.port=$RCON server-port=$PORT rcon.password=coquette \
  level-name=devtest spawn-monsters=false max-tick-time=-1 > "$RUN/server.properties"
mkdir -p "$RUN/defaultconfigs" && printf '[server]\n\tadvertiseDedicatedServerToLan = false\n' > "$RUN/defaultconfigs/forge-server.toml"

rcon() { python3 - "$1" "$RCON" <<'PY'
import socket, struct, sys
s = socket.create_connection(("127.0.0.1", int(sys.argv[2])), timeout=10)
def pkt(i, t, body):
    b = struct.pack("<ii", i, t) + body.encode() + b"\0\0"
    s.sendall(struct.pack("<i", len(b)) + b)
    n = struct.unpack("<i", s.recv(4))[0]; return s.recv(n)[8:-2].decode(errors="replace")
pkt(1, 3, "coquette"); print(pkt(2, 2, sys.argv[1]))
PY
}

scan() {
  echo "---- $1: problems"
  grep -nE "ERROR|Exception|\bWARN\b.*(coquette|Coquette|missing|Missing|Unable|Failed|Couldn't)|Missing (model|texture|block)" "$2" \
    | grep -vE "Narrator|narrator|AL lib|ALSA|OpenAL|Realms|realms|server.properties|Failed to fetch user properties|YggdrasilUserApiService|authlib|com.mojang.authlib|User Properties|profile key|telemetry|Telemetry|LWJGL|GLFW error 65544|libpulse|Pulse|ReferenceError|sound engine|SoundEngine|Socket closed|session.lock|advertiseDedicatedServerToLan|SoundSystem|Missing sound for event: minecraft:|is missing mods.toml file|uses unexpected schema|is not correct. Correcting|preferIPv6Addresses" \
    | head -60
  echo "---- end"
}

if [ "$WHAT" != client ]; then
  rm -rf "$RUN/devtest"
  (cd "$WS" && timeout 900 ./gradlew --no-daemon ":$LOADER:runServer" > "$LOGS/$TAG-server.log" 2>&1) &
  GPID=$!
  for i in $(seq 1 600); do
    grep -q 'Done (' "$LOGS/$TAG-server.log" 2>/dev/null && break
    kill -0 $GPID 2>/dev/null || break; sleep 1
  done
  if grep -q 'Done (' "$LOGS/$TAG-server.log"; then
    sleep 5; rcon "list"; rcon "stop"
  else
    echo "SERVER DID NOT START"
  fi
  wait $GPID
  scan "$TAG server" "$LOGS/$TAG-server.log"
fi

if [ "$WHAT" != server ]; then
  export DISPLAY=$DISP
  pgrep -f "Xvfb $DISP " > /dev/null || (Xvfb $DISP -screen 0 1920x1080x24 +extension GLX +render -noreset > "$LOGS/xvfb$N.log" 2>&1 &)
  sleep 2
  export LIBGL_ALWAYS_SOFTWARE=1 GALLIUM_DRIVER=llvmpipe
  rm -f "$RUN"/screenshots/coquette_*.png
  [ -f "$RUN/options.txt" ] || printf '%s\n' onboardAccessibility:false skipMultiplayerWarning:true tutorialStep:none \
    pauseOnLostFocus:false guiScale:2 renderDistance:8 soundCategory_master:0.0 > "$RUN/options.txt"
  (cd "$WS" && timeout 1500 ./gradlew --no-daemon ":$LOADER:runClient" -Pshowcase > "$LOGS/$TAG-client.log" 2>&1)
  echo "client exit: $?   screenshots: $(ls "$RUN"/screenshots/coquette_*.png 2>/dev/null | wc -l)"
  scan "$TAG client" "$LOGS/$TAG-client.log"
fi
