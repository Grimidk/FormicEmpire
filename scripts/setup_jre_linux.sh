#!/bin/bash
# Verify the Linux x64 Temurin JRE (used by jpackage + FormicEmpire.linux.zip).
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

resolve_linux_jre_dir() {
    if [ -x "jre-linux/bin/java" ]; then
        echo "jre-linux"
        return 0
    fi
    for candidate in jdk-*-jre; do
        if [ -d "$candidate" ] && [ -x "$candidate/bin/java" ]; then
            if file "$candidate/bin/java" | grep -qE "ELF.*(x86-64|aarch64)"; then
                echo "$candidate"
                return 0
            fi
        fi
    done
    return 1
}

JRE_DIR="$(resolve_linux_jre_dir)" || {
    echo "Error: Linux JRE not found."
    echo "Extract Temurin 17 JRE (Linux x64) into the project root as jre-linux/"
    echo "Download: https://adoptium.net/temurin/releases/?version=17&os=linux&arch=x64&package=jre"
    exit 1
}

if ! file "$JRE_DIR/bin/java" | grep -qE "ELF.*(x86-64|aarch64)"; then
    echo "Error: $JRE_DIR/bin/java is not a Linux 64-bit binary."
    exit 1
fi

JAVA_VERSION="$(grep '^JAVA_VERSION=' "$JRE_DIR/release" | cut -d= -f2 | tr -d '"')"
OS_ARCH="$(grep '^OS_ARCH=' "$JRE_DIR/release" | cut -d= -f2 | tr -d '"')"
OS_NAME="$(grep '^OS_NAME=' "$JRE_DIR/release" | cut -d= -f2 | tr -d '"')"
echo "[setup_jre_linux] OK: Java $JAVA_VERSION ($OS_NAME $OS_ARCH) at $JRE_DIR/"
