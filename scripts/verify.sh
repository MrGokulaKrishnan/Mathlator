#!/usr/bin/env bash
# ==============================================================================
# MATHLATOR VERIFICATION SCRIPT (Tests + Lint)
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

# Ensure Java 17
if [ -z "${JAVA_HOME:-}" ] || [ ! -x "${JAVA_HOME}/bin/java" ]; then
    for cand in \
        "/usr/lib/jvm/java-17-openjdk-amd64" \
        "/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home" \
        "/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home" \
        "/Applications/Android Studio.app/Contents/jbr/Contents/Home"; do
        if [ -x "$cand/bin/java" ]; then
            export JAVA_HOME="$cand"
            export PATH="$JAVA_HOME/bin:$PATH"
            break
        fi
    done
fi

echo "========================================"
echo " RUNNING UNIT TESTS & LINT VERIFICATION"
echo "========================================"

./gradlew testDebugUnitTest lintDebug

echo ""
echo "All verification checks passed!"
