#!/bin/bash
# Verify the Windows x64 Temurin JRE at jre/ (used by launch4j + FormicEmpire.windows.zip).
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

JRE_DIR="jre"

if [ ! -f "$JRE_DIR/bin/java.exe" ]; then
    echo "Error: $JRE_DIR/bin/java.exe not found."
    echo "Extract Temurin 17 JRE (Windows x64) into the project root as jre/"
    echo "Download: https://adoptium.net/temurin/releases/?version=17&os=windows&arch=x64&package=jre"
    exit 1
fi

if ! file "$JRE_DIR/bin/java.exe" | grep -qE "x86-64|x64"; then
    echo "Error: $JRE_DIR/bin/java.exe is not Windows 64-bit (x64)."
    exit 1
fi

JAVA_VERSION="$(grep '^JAVA_VERSION=' "$JRE_DIR/release" | cut -d= -f2 | tr -d '"')"
OS_ARCH="$(grep '^OS_ARCH=' "$JRE_DIR/release" | cut -d= -f2 | tr -d '"')"
echo "[setup_jre] OK: Java $JAVA_VERSION ($OS_ARCH) at $JRE_DIR/"
