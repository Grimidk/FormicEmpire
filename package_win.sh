#!/bin/bash
# 1. Clean and Build
echo "Building Formic Empire..."
./mvnw clean package

# 2. Setup Distribution Folder
DIST_DIR="FormicEmpire_Release"
rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR"

# 3. Copy Executable
echo "Copying Executable..."
if [ -f "target/FormicEmpire.exe" ]; then
    cp "target/FormicEmpire.exe" "$DIST_DIR/"
else
    echo "Error: target/FormicEmpire.exe not found! Did the build fail?"
    exit 1
fi

# 4. Instructions
echo ""
echo "=========================================="
echo "  PACKAGING COMPLETE"
echo "=========================================="
echo "The release files are in the '$DIST_DIR' folder."
echo ""
echo "CRITICAL NEXT STEP:"
echo "1. Download the Windows JRE (Java 17 x86/32-bit) from:"
echo "   https://adoptium.net/temurin/releases/?version=17&os=windows&arch=x86&package=jre"
echo "2. Unzip it."
echo "3. Rename the unzipped folder to 'jre'."
echo "4. Move the 'jre' folder inside '$DIST_DIR'."
echo ""
echo "Final Structure should look like:"
echo "  $DIST_DIR/"
echo "  ├── FormicEmpire.exe"
echo "  └── jre/"
echo "      ├── bin/"
echo "      └── lib/"
echo ""
echo "Then zip the '$DIST_DIR' folder and share it!"
