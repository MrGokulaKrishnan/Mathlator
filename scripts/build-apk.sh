#!/usr/bin/env bash
# ==============================================================================
# MATHLATOR APK BUILD PIPELINE (macOS / Linux)
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

SERVE=false
RELEASE=false

for arg in "$@"; do
    case "$arg" in
        --serve)
            SERVE=true
            ;;
        --release)
            RELEASE=true
            ;;
        *)
            echo "Unknown argument: $arg"
            echo "Usage: ./scripts/build-apk.sh [--serve] [--release]"
            exit 1
            ;;
    esac
done

START_TIME=$(date +%s)

echo "========================================"
echo " MATHLATOR APK BUILD PIPELINE"
echo "========================================"

# Locate Java 17 if not set
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

if [ "$RELEASE" = true ]; then
    KEYSTORE_DIR="$PROJECT_ROOT/keystore"
    KEYSTORE_FILE="$KEYSTORE_DIR/release.keystore"
    KEYSTORE_PROPS="$PROJECT_ROOT/keystore.properties"

    if [ ! -f "$KEYSTORE_FILE" ]; then
        echo "Release keystore not found. Generating new keystore with keytool..."
        mkdir -p "$KEYSTORE_DIR"
        keytool -genkeypair -v \
            -keystore "$KEYSTORE_FILE" \
            -alias mathlator \
            -keyalg RSA \
            -keysize 2048 \
            -validity 10000 \
            -storepass mathlatorpass \
            -keypass mathlatorpass \
            -dname "CN=Mathlator, OU=Mobile, O=Mathlator, L=Local, ST=State, C=US"

        cat <<EOF > "$KEYSTORE_PROPS"
storeFile=keystore/release.keystore
storePassword=mathlatorpass
keyAlias=mathlator
keyPassword=mathlatorpass
EOF
        echo "Created local release keystore at: $KEYSTORE_FILE"
        echo "WARNING: BACK UP this keystore file and keystore.properties! NEVER commit them to Git."
    fi
    BUILD_TASK="assembleRelease"
    SOURCE_APK="$PROJECT_ROOT/app/build/outputs/apk/release/app-release.apk"
    if [ ! -f "$SOURCE_APK" ]; then
        SOURCE_APK="$PROJECT_ROOT/app/build/outputs/apk/release/app-release-unsigned.apk"
    fi
else
    BUILD_TASK="assembleDebug"
    SOURCE_APK="$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"
fi

# Clean old APK so we never copy stale builds
rm -f "$SOURCE_APK"

echo "Executing: ./gradlew $BUILD_TASK..."
./gradlew "$BUILD_TASK"

if [ ! -f "$SOURCE_APK" ]; then
    echo "ERROR: Build completed but output APK was not found at $SOURCE_APK" >&2
    exit 1
fi

DIST_DIR="$PROJECT_ROOT/dist"
mkdir -p "$DIST_DIR"

VERSION_NAME="1.0.0"
TIMESTAMP=$(date +"%Y%m%d-%H%M")
GIT_HASH=$(git rev-parse --short HEAD 2>/dev/null || echo "dev")

if [ "$RELEASE" = true ]; then
    LATEST_APK="$DIST_DIR/Mathlator-latest-release.apk"
    cp -f "$SOURCE_APK" "$LATEST_APK"
    TARGET_APK="$LATEST_APK"
else
    LATEST_APK="$DIST_DIR/Mathlator-latest.apk"
    STAMPED_APK="$DIST_DIR/Mathlator-$VERSION_NAME-$TIMESTAMP-$GIT_HASH-debug.apk"

    cp -f "$SOURCE_APK" "$LATEST_APK"
    cp -f "$SOURCE_APK" "$STAMPED_APK"
    TARGET_APK="$LATEST_APK"

    # Keep only the 5 newest stamped debug copies, delete older ones
    # list stamped apks sorted by modification time descending
    STAMPED_FILES=( $(ls -t "$DIST_DIR"/Mathlator-*-*-*-debug.apk 2>/dev/null || true) )
    if [ ${#STAMPED_FILES[@]} -gt 5 ]; then
        for ((i=5; i<${#STAMPED_FILES[@]}; i++)); do
            echo "Pruned older build: $(basename "${STAMPED_FILES[$i]}")"
            rm -f "${STAMPED_FILES[$i]}"
        done
    fi
fi

END_TIME=$(date +%s)
ELAPSED=$((END_TIME - START_TIME))
ELAPSED_MIN=$((ELAPSED / 60))
ELAPSED_SEC=$((ELAPSED % 60))

# APK Size and SHA256
if command -v stat >/dev/null 2>&1; then
    # BSD stat (macOS) vs GNU stat (Linux)
    FILE_SIZE=$(stat -f%z "$TARGET_APK" 2>/dev/null || stat -c%s "$TARGET_APK" 2>/dev/null || echo 0)
else
    FILE_SIZE=$(wc -c < "$TARGET_APK" || echo 0)
fi
FILE_SIZE_MB=$(awk "BEGIN {printf \"%.2f\", $FILE_SIZE/1048576}")

if command -v sha256sum >/dev/null 2>&1; then
    SHA256=$(sha256sum "$TARGET_APK" | awk '{print $1}')
elif command -v shasum >/dev/null 2>&1; then
    SHA256=$(shasum -a 256 "$TARGET_APK" | awk '{print $1}')
else
    SHA256="unknown"
fi

echo ""
echo "========================================"
echo " BUILD ARTIFACT PRODUCED SUCCESSFULLY"
echo "========================================"
echo "Path:       $TARGET_APK"
echo "Size:       ${FILE_SIZE_MB} MB (${FILE_SIZE} bytes)"
echo "SHA-256:    $SHA256"
echo "Build Time: ${ELAPSED_MIN}m ${ELAPSED_SEC}s"
echo "========================================"

# ADB Check and Installation
if command -v adb >/dev/null 2>&1; then
    DEVICES=( $(adb devices | awk 'NR>1 && $2=="device" {print $1}') )
    DEV_COUNT=${#DEVICES[@]}

    if [ "$DEV_COUNT" -eq 1 ]; then
        DEV="${DEVICES[0]}"
        echo ""
        echo "Detected connected device: $DEV"
        echo "Installing $TARGET_APK..."
        if ! adb -s "$DEV" install -r -d "$TARGET_APK"; then
            echo "Install failed (possibly signature mismatch). Uninstalling old debug build..."
            adb -s "$DEV" uninstall com.mathlator.app.debug || true
            adb -s "$DEV" install -r -d "$TARGET_APK"
        fi
        echo "Launching Mathlator..."
        adb -s "$DEV" shell am start -n com.mathlator.app.debug/com.mathlator.app.MainActivity
    elif [ "$DEV_COUNT" -gt 1 ]; then
        echo ""
        echo "Multiple Android devices connected: ${DEVICES[*]}"
        echo "To install to a specific device, run:"
        for d in "${DEVICES[@]}"; do
            echo "  adb -s $d install -r -d \"$TARGET_APK\""
        done
    else
        echo ""
        echo "No ADB devices currently connected."
        echo "Manual Installation Steps:"
        echo " 1. Connect phone with USB debugging enabled, OR"
        echo " 2. Transfer '$TARGET_APK' to your phone and tap to install, OR"
        echo " 3. Run this script with --serve to install over local Wi-Fi."
    fi
else
    echo ""
    echo "ADB not found. Transfer '$TARGET_APK' directly to your phone to install."
fi

# Wi-Fi Serve option
if [ "$SERVE" = true ]; then
    # Detect local IP
    LOCAL_IP=$(ipconfig getifaddr en0 2>/dev/null || hostname -I 2>/dev/null | awk '{print $1}' || echo "localhost")
    PORT=8080
    DOWNLOAD_URL="http://${LOCAL_IP}:${PORT}/Mathlator-latest.apk"

    echo ""
    echo "========================================"
    echo " LOCAL HTTP APK DOWNLOAD SERVER"
    echo "========================================"
    echo "Download URL: $DOWNLOAD_URL"
    echo "Open this URL in Chrome on your phone connected to the same Wi-Fi."

    if command -v curl >/dev/null 2>&1; then
        curl -s -m 3 "https://qrenco.de/$DOWNLOAD_URL" || true
    fi

    echo "Serving $DIST_DIR on port $PORT... (Press Ctrl+C to stop)"
    cd "$DIST_DIR"
    if command -v python3 >/dev/null 2>&1; then
        python3 -m http.server "$PORT"
    elif command -v python >/dev/null 2>&1; then
        python -m SimpleHTTPServer "$PORT"
    else
        echo "Python not installed. Cannot start simple HTTP server."
    fi
fi
