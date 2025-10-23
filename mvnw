#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

# Minimal mvnw shim that downloads the Takari maven-wrapper jar if missing
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WRAPPER_DIR="$ROOT_DIR/.mvn/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/maven-wrapper.jar"
WRAPPER_DOWNLOAD_URL="https://repo1.maven.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar"

if [[ ! -f "$WRAPPER_JAR" ]]; then
  echo "maven wrapper jar not found; downloading to $WRAPPER_JAR"
  mkdir -p "$WRAPPER_DIR"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$WRAPPER_DOWNLOAD_URL" -o "$WRAPPER_JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -qO "$WRAPPER_JAR" "$WRAPPER_DOWNLOAD_URL"
  else
    echo "Error: curl or wget is required to download the maven wrapper jar." >&2
    exit 2
  fi
fi

exec java -jar "$WRAPPER_JAR" "$@"
