<#
.SYNOPSIS
Builds, stamps, packages, and deploys the Mathlator Android APK.

.DESCRIPTION
Runs Gradle build, copies to dist/ with timestamp and Git hash stamping,
maintains the 5 latest stamped builds, deploys via ADB if connected,
or optionally serves over local HTTP with -Serve.

.PARAMETER Serve
Serves the dist/ directory over HTTP on port 8080 for Wi-Fi download to device.

.PARAMETER Release
Builds a release-like minified APK signed with a local keystore.
#>
param(
    [switch]$Serve,
    [switch]$Release
)

$ErrorActionPreference = "Stop"

# Project root resolution
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = (Get-Item $ScriptDir).Parent.FullName
Set-Location $ProjectRoot

# Ensure Java 17 and Android SDK
if (-not $env:JAVA_HOME -or -not (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $candidates = @(
        "C:\Program Files\Java\jdk-17",
        "C:\Program Files\Eclipse Adoptium\jdk-17",
        "C:\Program Files\Android\Android Studio\jbr"
    )
    foreach ($cand in $candidates) {
        if (Test-Path "$cand\bin\java.exe") {
            $env:JAVA_HOME = $cand
            break
        }
    }
}
$env:PATH = "$env:JAVA_HOME\bin;C:\Users\gokul\android-sdk\platform-tools;$env:PATH"

$StartTime = Get-Date

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " MATHLATOR APK BUILD PIPELINE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Handle Release Keystore Generation
if ($Release) {
    $KeystoreDir = Join-Path $ProjectRoot "keystore"
    $KeystorePath = Join-Path $KeystoreDir "release.keystore"
    $KeystoreProps = Join-Path $ProjectRoot "keystore.properties"

    if (-not (Test-Path $KeystorePath)) {
        Write-Host "Release keystore not found. Generating new keystore with keytool..." -ForegroundColor Yellow
        New-Item -ItemType Directory -Force -Path $KeystoreDir | Out-Null
        
        $keytoolCmd = "& `"$env:JAVA_HOME\bin\keytool.exe`" -genkeypair -v -keystore `"$KeystorePath`" -alias mathlator -keyalg RSA -keysize 2048 -validity 10000 -storepass mathlatorpass -keypass mathlatorpass -dname `"CN=Mathlator, OU=Mobile, O=Mathlator, L=Local, ST=State, C=US`""
        Invoke-Expression $keytoolCmd

        @"
storeFile=keystore/release.keystore
storePassword=mathlatorpass
keyAlias=mathlator
keyPassword=mathlatorpass
"@ | Set-Content -Path $KeystoreProps

        Write-Host "Created local release keystore at: $KeystorePath" -ForegroundColor Green
        Write-Host "WARNING: BACK UP this keystore file and keystore.properties! NEVER commit them to Git." -ForegroundColor Yellow
    }
    $BuildTask = "assembleRelease"
    $SourceApk = Join-Path $ProjectRoot "app\build\outputs\apk\release\app-release.apk"
    if (-not (Test-Path $SourceApk)) {
        $SourceApk = Join-Path $ProjectRoot "app\build\outputs\apk\release\app-release-unsigned.apk"
    }
} else {
    $BuildTask = "assembleDebug"
    $SourceApk = Join-Path $ProjectRoot "app\build\outputs\apk\debug\app-debug.apk"
}

# 1. Clean old APK from build output before building so we never copy stale builds
if (Test-Path $SourceApk) {
    Remove-Item $SourceApk -Force -ErrorAction SilentlyContinue
}

Write-Host "Executing: .\gradlew.bat $BuildTask..." -ForegroundColor Cyan
& ".\gradlew.bat" $BuildTask
if ($LASTEXITCODE -ne 0) {
    Write-Error "Build failed with exit code $LASTEXITCODE. Never copying or distributing an old APK."
    exit 1
}

if (-not (Test-Path $SourceApk)) {
    Write-Error "Build finished but output APK not found at: $SourceApk"
    exit 1
}

# 2. Dist output and stamping
$DistDir = Join-Path $ProjectRoot "dist"
New-Item -ItemType Directory -Force -Path $DistDir | Out-Null

$VersionName = "1.0.0"
$Timestamp = (Get-Date -Format "yyyyMMdd-HHmm")
$GitHash = (git rev-parse --short HEAD 2>$null)
if (-not $GitHash) { $GitHash = "dev" }

if ($Release) {
    $LatestApk = Join-Path $DistDir "Mathlator-latest-release.apk"
    Copy-Item -Path $SourceApk -Destination $LatestApk -Force
    $TargetApkForInstall = $LatestApk
} else {
    $LatestApk = Join-Path $DistDir "Mathlator-latest.apk"
    $StampedApk = Join-Path $DistDir "Mathlator-$VersionName-$Timestamp-$GitHash-debug.apk"

    Copy-Item -Path $SourceApk -Destination $LatestApk -Force
    Copy-Item -Path $SourceApk -Destination $StampedApk -Force
    $TargetApkForInstall = $LatestApk

    # Keep only the 5 newest stamped debug copies, delete older ones
    $StampedFiles = Get-ChildItem -Path $DistDir -Filter "Mathlator-*-*-*-debug.apk" |
        Sort-Object LastWriteTime -Descending

    if ($StampedFiles.Count -gt 5) {
        $ToPrune = $StampedFiles | Select-Object -Skip 5
        foreach ($oldFile in $ToPrune) {
            Remove-Item $oldFile.FullName -Force
            Write-Host "Pruned older build: $($oldFile.Name)" -ForegroundColor DarkGray
        }
    }
}

$Elapsed = (Get-Date) - $StartTime
$ApkItem = Get-Item $TargetApkForInstall
$FileSizeMb = [math]::Round($ApkItem.Length / 1MB, 2)
$Sha256 = (Get-FileHash -Path $TargetApkForInstall -Algorithm SHA256).Hash

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host " BUILD ARTIFACT PRODUCED SUCCESSFULLY" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "Path:       $($ApkItem.FullName)" -ForegroundColor White
Write-Host "Size:       $FileSizeMb MB ($($ApkItem.Length) bytes)" -ForegroundColor White
Write-Host "SHA-256:    $Sha256" -ForegroundColor White
Write-Host "Build Time: $($Elapsed.Minutes)m $($Elapsed.Seconds)s" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Green

# 3. ADB Deployment Check
$adbCmd = Get-Command "adb" -ErrorAction SilentlyContinue
$adbPath = if ($adbCmd) { $adbCmd.Source } else { "C:\Users\gokul\android-sdk\platform-tools\adb.exe" }

$AdbAvailable = Test-Path $adbPath
if ($AdbAvailable) {
    $rawDevices = & $adbPath devices
    $deviceList = @()
    foreach ($line in ($rawDevices -split "`r?`n")) {
        if ($line -match '^(\S+)\s+device$') {
            $deviceList += $matches[1]
        }
    }

    if ($deviceList.Count -eq 1) {
        $dev = $deviceList[0]
        Write-Host ""
        Write-Host "Detected connected device: $dev" -ForegroundColor Cyan
        Write-Host "Installing $LatestApk..." -ForegroundColor Cyan

        $installOutput = & $adbPath -s $dev install -r -d "$LatestApk" 2>&1
        Write-Host $installOutput

        if ($installOutput -match "INSTALL_FAILED_UPDATE_INCOMPATIBLE") {
            Write-Host "Signature mismatch detected (old installation has different keystore signature)." -ForegroundColor Red
            Write-Host "Uninstalling existing build..." -ForegroundColor Yellow
            & $adbPath -s $dev uninstall com.mathlator.app.debug
            & $adbPath -s $dev install -r -d "$LatestApk"
        }

        Write-Host "Launching Mathlator..." -ForegroundColor Cyan
        & $adbPath -s $dev shell am start -n com.mathlator.app.debug/com.mathlator.app.MainActivity
    } elseif ($deviceList.Count -gt 1) {
        Write-Host ""
        Write-Host "Multiple Android devices connected: $($deviceList -join ', ')" -ForegroundColor Yellow
        Write-Host "To install to a specific device, run:"
        foreach ($d in $deviceList) {
            Write-Host "  adb -s $d install -r -d `"$LatestApk`"" -ForegroundColor White
        }
    } else {
        Write-Host ""
        Write-Host "No ADB devices currently connected." -ForegroundColor Yellow
        Write-Host "Manual Installation Steps:" -ForegroundColor White
        Write-Host " 1. Connect phone with USB debugging enabled, OR" -ForegroundColor White
        Write-Host " 2. Transfer '$LatestApk' to your phone and tap to install, OR" -ForegroundColor White
        Write-Host " 3. Run this script with -Serve to install over local Wi-Fi." -ForegroundColor White
    }
} else {
    Write-Host ""
    Write-Host "ADB not found. Transfer '$LatestApk' directly to your phone to install." -ForegroundColor Yellow
}

# 4. Optional HTTP Server for Wi-Fi Download
if ($Serve) {
    $localIp = (Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
        Where-Object { $_.InterfaceAlias -notmatch 'Loopback' -and $_.IPAddress -notlike '169.254*' -and $_.IPAddress -notlike '127.*' } |
        Select-Object -First 1).IPAddress

    if (-not $localIp) { $localIp = "localhost" }
    $Port = 8080
    $DownloadUrl = "http://${localIp}:${Port}/Mathlator-latest.apk"

    Write-Host ""
    Write-Host "========================================" -ForegroundColor Magenta
    Write-Host " LOCAL HTTP APK DOWNLOAD SERVER" -ForegroundColor Magenta
    Write-Host "========================================" -ForegroundColor Magenta
    Write-Host "Download URL: $DownloadUrl" -ForegroundColor Cyan
    Write-Host "Open this URL in Chrome on your phone connected to the same Wi-Fi." -ForegroundColor White

    # Attempt terminal ASCII QR Code via qrenco.de
    try {
        $qr = (Invoke-RestMethod -Uri "https://qrenco.de/$DownloadUrl" -TimeoutSec 3 -ErrorAction SilentlyContinue)
        if ($qr) {
            Write-Host ""
            Write-Host $qr -ForegroundColor Yellow
        }
    } catch {
        # Offline or QR timeout, continue cleanly
    }

    $listener = New-Object System.Net.HttpListener
    $listener.Prefixes.Add("http://*:${Port}/")
    try {
        $listener.Start()
    } catch {
        # Fall back to localhost prefix if wildcard requires elevation
        $listener = New-Object System.Net.HttpListener
        $listener.Prefixes.Add("http://+:${Port}/")
        $listener.Start()
    }

    Write-Host "Listening on port $Port... Press Ctrl+C to terminate server." -ForegroundColor Green
    try {
        while ($listener.IsListening) {
            $context = $listener.GetContext()
            $request = $context.Request
            $response = $context.Response

            $reqPath = $request.Url.LocalPath.TrimStart('/')
            if ([string]::IsNullOrWhiteSpace($reqPath)) {
                $reqPath = "Mathlator-latest.apk"
            }
            $targetFilePath = Join-Path $DistDir $reqPath

            if (Test-Path $targetFilePath) {
                $fileBytes = [System.IO.File]::ReadAllBytes($targetFilePath)
                $response.ContentType = "application/vnd.android.package-archive"
                $response.ContentLength64 = $fileBytes.Length
                $response.AddHeader("Content-Disposition", "attachment; filename=$([System.IO.Path]::GetFileName($targetFilePath))")
                $response.OutputStream.Write($fileBytes, 0, $fileBytes.Length)
                Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Served $reqPath to $($request.RemoteEndPoint)" -ForegroundColor Green
            } else {
                $response.StatusCode = 404
                $notFound = [System.Text.Encoding]::UTF8.GetBytes("File not found.")
                $response.OutputStream.Write($notFound, 0, $notFound.Length)
            }
            $response.OutputStream.Close()
        }
    } finally {
        $listener.Stop()
    }
}
