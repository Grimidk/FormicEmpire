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

HOST_OS="$(uname -s)"

echo "[Build] Checking bundled runtimes..."
chmod +x scripts/setup_jre.sh scripts/setup_jre_linux.sh
if [ -f "jre/bin/java.exe" ]; then
    ./scripts/setup_jre.sh
elif [ "$HOST_OS" = "Darwin" ]; then
    ./scripts/setup_jre.sh
fi
if [ "$HOST_OS" = "Linux" ]; then
    ./scripts/setup_jre_linux.sh
fi

echo "[Build] Building Formic Empire..."
$MVN_EXEC clean package -DskipTests

OUTPUT_DIR="outputs"
JAR_NAME="FormicEmpire-1.0-SNAPSHOT.jar"
JAR_ZIP="$OUTPUT_DIR/FormicEmpire.jar.zip"
JAR_STAGING="$OUTPUT_DIR/.jar-staging"
WIN_ZIP="$OUTPUT_DIR/FormicEmpire.windows.zip"
WIN_STAGING="$OUTPUT_DIR/.windows-staging"
LINUX_ZIP="$OUTPUT_DIR/FormicEmpire.linux.zip"
LINUX_APP_DIR="$OUTPUT_DIR/FormicEmpire"
MAC_APP="$OUTPUT_DIR/FormicEmpire.app"
BUILD_JAR="target/$JAR_NAME"
BUILD_EXE="target/FormicEmpire.exe"
MAIN_CLASS="com.grimidk.formicempire.FormicEmpire"
ICON_ICO="src/main/resources/meta/icon.ico"
ICON_ICNS="src/main/resources/meta/icon.icns"
ICON_PNG="src/main/resources/meta/icon.iconset/icon_256x256.png"

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

if [ ! -f "$ICON_ICO" ]; then
    echo "Error: $ICON_ICO not found (required for .exe and .app icons)."
    exit 1
fi

if [ "$HOST_OS" = "Darwin" ] && [ ! -f "$ICON_ICNS" ]; then
    echo "Error: $ICON_ICNS not found. Commit src/main/resources/meta/icon.icns (build on macOS with iconutil if needed)."
    exit 1
fi

if [ "$HOST_OS" = "Linux" ] && [ ! -f "$ICON_PNG" ]; then
    echo "Error: $ICON_PNG not found (required for Linux jpackage icon)."
    exit 1
fi

if [ ! -f "$BUILD_JAR" ]; then
    echo "Error: $BUILD_JAR not found. Did the build fail?"
    exit 1
fi

rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

HAS_JAR_ZIP=0
HAS_WIN_ZIP=0
HAS_LINUX_ZIP=0
HAS_APP=0

pack_jar_zip() {
    echo "[Pack] JAR zip -> $JAR_ZIP (jar + launcher + saves)..."
    rm -rf "$JAR_STAGING"
    mkdir -p "$JAR_STAGING/saves"
    cp "$BUILD_JAR" "$JAR_STAGING/$JAR_NAME"

    cat > "$JAR_STAGING/FormicEmpire.sh" <<EOF
#!/bin/bash
set -e
cd "\$(dirname "\$0")"
JAVA_BIN=""
if [ -x "./jre/bin/java" ]; then
    JAVA_BIN="./jre/bin/java"
elif command -v java >/dev/null 2>&1; then
    JAVA_BIN="java"
else
    echo "Error: Java 17+ is required."
    exit 1
fi
exec "\$JAVA_BIN" -jar "$JAR_NAME"
EOF
    chmod +x "$JAR_STAGING/FormicEmpire.sh"
    rm -f "$JAR_ZIP"
    (
        cd "$JAR_STAGING"
        zip -r -X "../FormicEmpire.jar.zip" . \
            -x "*.DS_Store" \
            -x "*/.*"
    )
    rm -rf "$JAR_STAGING"
    HAS_JAR_ZIP=1
}

pack_windows_zip() {
    echo "[Pack] Windows zip -> $WIN_ZIP (FormicEmpire.exe + jre/)..."
    if [ ! -f "$BUILD_EXE" ]; then
        echo "Error: $BUILD_EXE not found. launch4j output is missing."
        return 1
    fi
    if [ ! -d "jre" ] || [ ! -f "jre/bin/java.exe" ]; then
        echo "[Pack] Skipping Windows zip (Windows jre/ not found)."
        return 0
    fi
    rm -rf "$WIN_STAGING"
    mkdir -p "$WIN_STAGING"
    cp "$BUILD_EXE" "$WIN_STAGING/FormicEmpire.exe"
    cp -r "jre" "$WIN_STAGING/"
    rm -f "$WIN_ZIP"
    (
        cd "$WIN_STAGING"
        zip -r -X "../FormicEmpire.windows.zip" . \
            -x "*.DS_Store" \
            -x "*/.*"
    )
    rm -rf "$WIN_STAGING"
    HAS_WIN_ZIP=1
}

pack_macos_app() {
    if [ "$HOST_OS" != "Darwin" ]; then
        echo "[Pack] Skipping macOS app (run scripts/package.sh on macOS to build $MAC_APP)."
        return 0
    fi

    if ! command -v jpackage >/dev/null 2>&1; then
        echo "Error: jpackage not found (needs JDK 17+ on macOS to build FormicEmpire.app)."
        return 1
    fi

    echo "[Pack] macOS app -> $MAC_APP..."
    JPACKAGE_INPUT="$OUTPUT_DIR/.jpackage-input"
    rm -rf "$JPACKAGE_INPUT"
    mkdir -p "$JPACKAGE_INPUT/saves"
    cp "$BUILD_JAR" "$JPACKAGE_INPUT/$JAR_NAME"

    JPACKAGE_ARGS=(
        --input "$JPACKAGE_INPUT"
        --name FormicEmpire
        --main-jar "$JAR_NAME"
        --main-class "$MAIN_CLASS"
        --type app-image
        --dest "$OUTPUT_DIR"
        --icon "$ICON_ICNS"
        --app-version 1.0
        --vendor GrimIDK
        --description "A game about ants."
        --java-options "-Dapple.awt.application.name=FormicEmpire"
        --java-options "-Dawt.useSystemAAFontSettings=on"
        --java-options "-Dswing.aatext=true"
    )

    if [ -d "jre-mac" ] && [ -x "jre-mac/bin/java" ]; then
        echo "[Pack] Using bundled runtime from jre-mac/ for .app"
        JPACKAGE_ARGS+=(--runtime-image "jre-mac")
    elif [ -d "jre-macos" ] && [ -x "jre-macos/bin/java" ]; then
        echo "[Pack] Using bundled runtime from jre-macos/ for .app"
        JPACKAGE_ARGS+=(--runtime-image "jre-macos")
    else
        echo "[Pack] Embedding JDK runtime via jpackage (optional: add jre-mac/ for a custom macOS JRE)"
    fi

    jpackage "${JPACKAGE_ARGS[@]}"
    rm -rf "$JPACKAGE_INPUT"
    HAS_APP=1
}

pack_linux_app() {
    if [ "$HOST_OS" != "Linux" ]; then
        echo "[Pack] Skipping Linux app-image (run scripts/package.sh on Linux to build $LINUX_ZIP)."
        return 0
    fi

    if ! command -v jpackage >/dev/null 2>&1; then
        echo "Error: jpackage not found (needs JDK 17+ on Linux to build the Linux app-image)."
        return 1
    fi

    LINUX_JRE="$(resolve_linux_jre_dir)" || {
        echo "Error: Linux JRE not found (jre-linux/ or jdk-*-jre/)."
        return 1
    }

    echo "[Pack] Linux app-image -> $LINUX_ZIP (runtime from $LINUX_JRE/)..."
    JPACKAGE_INPUT="$OUTPUT_DIR/.jpackage-input"
    rm -rf "$JPACKAGE_INPUT" "$LINUX_APP_DIR"
    mkdir -p "$JPACKAGE_INPUT/saves"
    cp "$BUILD_JAR" "$JPACKAGE_INPUT/$JAR_NAME"

    jpackage \
        --input "$JPACKAGE_INPUT" \
        --name FormicEmpire \
        --main-jar "$JAR_NAME" \
        --main-class "$MAIN_CLASS" \
        --type app-image \
        --dest "$OUTPUT_DIR" \
        --runtime-image "$LINUX_JRE" \
        --icon "$ICON_PNG" \
        --app-version 1.0 \
        --vendor GrimIDK \
        --description "A game about ants." \
        --java-options "-Dawt.useSystemAAFontSettings=on" \
        --java-options "-Dswing.aatext=true"

    rm -rf "$JPACKAGE_INPUT"
    if [ ! -x "$LINUX_APP_DIR/bin/FormicEmpire" ]; then
        echo "Error: jpackage did not produce $LINUX_APP_DIR/bin/FormicEmpire"
        return 1
    fi

    rm -f "$LINUX_ZIP"
    (
        cd "$OUTPUT_DIR"
        zip -r -X "FormicEmpire.linux.zip" FormicEmpire \
            -x "*.DS_Store" \
            -x "*/.*"
    )
    rm -rf "$LINUX_APP_DIR"
    HAS_LINUX_ZIP=1
}

pack_jar_zip
pack_windows_zip
pack_macos_app
pack_linux_app

echo ""
echo "Outputs in $OUTPUT_DIR/:"
find "$OUTPUT_DIR" -maxdepth 1 -mindepth 1 | sort
echo ""
[ "$HAS_JAR_ZIP" -eq 1 ] && echo "  zip   FormicEmpire.jar.zip (jar + FormicEmpire.sh + saves — needs Java 17+)"
[ "$HAS_WIN_ZIP" -eq 1 ] && echo "  zip   FormicEmpire.windows.zip (FormicEmpire.exe + jre/)"
[ "$HAS_LINUX_ZIP" -eq 1 ] && echo "  zip   FormicEmpire.linux.zip (jpackage app-image — bin/FormicEmpire + embedded runtime)"
[ "$HAS_APP" -eq 1 ] && echo "  app   FormicEmpire.app (macOS only)"
echo ""
echo "(Maven build intermediates stay in target/ — ship artifacts under $OUTPUT_DIR/.)"

if [ "$HAS_JAR_ZIP" -ne 1 ]; then
    echo ""
    echo "Error: JAR zip is a required release artifact."
    exit 1
fi

if [ "$HOST_OS" = "Darwin" ]; then
    if [ "$HAS_WIN_ZIP" -ne 1 ]; then
        echo ""
        echo "Error: Windows zip is required when packaging on macOS."
        exit 1
    fi
    if [ "$HAS_APP" -ne 1 ]; then
        echo ""
        echo "Error: FormicEmpire.app was not created."
        exit 1
    fi
elif [ "$HOST_OS" = "Linux" ]; then
    if [ "$HAS_LINUX_ZIP" -ne 1 ]; then
        echo ""
        echo "Error: Linux zip was not created."
        exit 1
    fi
elif [ "$HAS_WIN_ZIP" -ne 1 ]; then
    echo ""
    echo "Error: Windows zip is required."
    exit 1
fi

if [ "$HAS_APP" -ne 1 ] && [ "$HOST_OS" != "Darwin" ]; then
    echo ""
    echo "Note: Run ./scripts/package.sh on macOS to also produce $MAC_APP."
fi

if [ "$HAS_LINUX_ZIP" -ne 1 ] && [ "$HOST_OS" != "Linux" ]; then
    echo ""
    echo "Note: Run ./scripts/package.sh on Linux to also produce $LINUX_ZIP."
fi

echo ""
echo "Run:"
echo "  JAR:     unzip $JAR_ZIP && ./FormicEmpire.sh"
echo "  Windows: unzip $WIN_ZIP && FormicEmpire.exe"
[ "$HAS_LINUX_ZIP" -eq 1 ] && echo "  Linux:   unzip $LINUX_ZIP && FormicEmpire/bin/FormicEmpire"
[ "$HAS_APP" -eq 1 ] && echo "  macOS:   open \"$MAC_APP\""
