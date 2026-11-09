# Mathlator — Agent Instructions & Rules

After any change to the project, run scripts/build-apk and report the APK path plus a what-to-test checklist before finishing.

## APK Delivery Workflow (Mandatory for all changes)
Whenever code or configuration is modified (features, bug fixes, refactors, dependencies, UI tweaks):
1. Execute the build script:
   - On Windows: `powershell -ExecutionPolicy Bypass -File scripts/build-apk.ps1`
   - On macOS/Linux: `./scripts/build-apk.sh`
2. Ensure the build succeeds and generates:
   - `dist/Mathlator-latest.apk`
   - A stamped copy `dist/Mathlator-<versionName>-<yyyyMMdd-HHmm>-<gitShortHash>-debug.apk` (keeps 5 latest stamped copies).
3. If a physical device or emulator is connected via ADB, the script will automatically install with `adb install -r -d` and launch the app (`com.mathlator.app.debug/com.mathlator.app.MainActivity`).
4. Always conclude every turn with a **Test build** block containing:
   - The absolute APK file path
   - APK size and SHA-256 checksum
   - Exact install commands (or `--serve` URL for local Wi-Fi download)
   - A 3–6 line checklist of what to test for the exact changes made.

## Build Hygiene & Security
- `dist/`, keystores (`keystore/`, `*.keystore`), and `keystore.properties` are strictly git-ignored.
- Never commit private keys, signing credentials, or passwords to Git.
- Fast builds: `org.gradle.daemon=true`, `org.gradle.parallel=true`, `org.gradle.caching=true`, and configuration cache are enabled.
- Separate test & lint verification into `scripts/verify.ps1` and `scripts/verify.sh`.
