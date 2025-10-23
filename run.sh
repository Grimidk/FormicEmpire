#!/usr/bin/env bash
# run.sh — build and run the FormicEmpire project from any directory
# Usage: ./run.sh [project_root] [-- <app args>]
# If project_root is omitted, defaults to the script's parent directory.
# Anything after -- is passed to the Java app as program args.
set -euo pipefail
IFS=$'\n\t'

# Resolve script dir
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="${1:-$SCRIPT_DIR}"
shift 1 || true

# If the user passed --, separate app args
APP_ARGS=()
if [[ "$#" -gt 0 ]]; then
  if [[ "$1" == "--" ]]; then
    shift
    APP_ARGS=("$@")
  else
    # If they passed something that looks like a path, ignore; otherwise treat as app args
    # We'll be strict: only treat first arg as project_root if it contains a pom.xml
    if [[ -f "$PROJECT_ROOT/pom.xml" ]]; then
      APP_ARGS=("$@")
    fi
  fi
fi

# Validate pom.xml
if [[ ! -f "$PROJECT_ROOT/pom.xml" ]]; then
  echo "Error: pom.xml not found in project root: $PROJECT_ROOT" >&2
  exit 2
fi

echo "Building project at $PROJECT_ROOT..."
# Prefer a project-local Maven wrapper if present
MVN_CMD=""
if [[ -x "$PROJECT_ROOT/mvnw" ]]; then
  MVN_CMD="$PROJECT_ROOT/mvnw"
elif command -v mvn >/dev/null 2>&1; then
  MVN_CMD="mvn"
else
  echo "Error: neither ./mvnw nor mvn (Maven) were found. Please install Maven or add a Maven wrapper to the project." >&2
  echo "On macOS you can install Maven with Homebrew: brew install maven" >&2
  exit 3
fi

# Build (clean and package) — uses shade plugin from pom to create an executable jar
echo "Building project at $PROJECT_ROOT using: $MVN_CMD"
( cd "$PROJECT_ROOT" && "$MVN_CMD" clean package )

# Find the shaded jar in target/
JAR_FILE=$(find "$PROJECT_ROOT/target" -maxdepth 1 -type f -name "*jar" -print | head -n 1 || true)
if [[ -z "$JAR_FILE" ]]; then
  echo "Error: No jar file found in $PROJECT_ROOT/target. Build may have failed." >&2
  exit 4
fi

# Run the jar
echo "Running $JAR_FILE"
java -jar "$JAR_FILE" "${APP_ARGS[@]:-}"
