#!/bin/bash
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

case "$(uname -s)" in
    CYGWIN*|MINGW*|MSYS*)
        MVN_EXEC="./mvnw.cmd"
        ;;
    *)
        MVN_EXEC="./mvnw"
        ;;
esac

if [ -f "$MVN_EXEC" ]; then
    chmod +x "$MVN_EXEC"
else
    echo "Error: Could not find Maven Wrapper ($MVN_EXEC)"
    exit 1
fi

echo "[Build] Building Formic Empire..."
$MVN_EXEC clean install

SAVES_DIR="saves"
TARGET_SAVES="target/saves"
mkdir -p "$SAVES_DIR"
if [ -d "$TARGET_SAVES" ]; then
    for f in "$TARGET_SAVES"/*; do
        [ -e "$f" ] || continue
        base=$(basename "$f")
        if [ ! -e "$SAVES_DIR/$base" ]; then
            echo "[Run] Moving $f -> $SAVES_DIR/$base"
            mv "$f" "$SAVES_DIR/$base"
        fi
    done
    rmdir "$TARGET_SAVES" 2>/dev/null || true
fi

echo "[Run] Starting the Game..."
java -jar target/FormicEmpire-1.0-SNAPSHOT.jar
