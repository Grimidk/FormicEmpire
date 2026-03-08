#!/bin/bash
echo "Building Formic Empire..."
./mvnw clean package

DIST_DIR="FormicEmpire_Release"
rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR"

echo "Copying Executable..."
if [ -f "target/FormicEmpire.exe" ]; then
    cp "target/FormicEmpire.exe" "$DIST_DIR/"
else
    echo "Error: target/FormicEmpire.exe not found! Did the build fail?"
    exit 1
fi

echo "Copying JRE..."
if [ -d "jre" ]; then
    cp -r "jre" "$DIST_DIR/"
else
    echo "Warning: 'jre' folder not found. The bundled application may not run without it."
fi
