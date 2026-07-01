#!/bin/bash
set -e

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

echo "[Build] Syncing roadmap.txt and credits.txt from the project root into src/main/resources/meta/..."
cp -f roadmap.txt src/main/resources/meta/roadmap.txt
cp -f credits.txt src/main/resources/meta/credits.txt

echo "[Build] Building Formic Empire..."
$MVN_EXEC clean install

echo "[Run] Starting the Game..."
java -jar target/FormicEmpire-1.0-SNAPSHOT.jar