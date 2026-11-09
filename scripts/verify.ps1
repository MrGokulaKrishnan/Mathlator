<#
.SYNOPSIS
Runs test suites and lint checks for Mathlator.
#>
$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = (Get-Item $ScriptDir).Parent.FullName
Set-Location $ProjectRoot

# Ensure Java 17
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

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " RUNNING UNIT TESTS & LINT VERIFICATION" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

& ".\gradlew.bat" testDebugUnitTest lintDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "All verification checks passed!" -ForegroundColor Green
} else {
    Write-Error "Verification failed with exit code $LASTEXITCODE."
    exit $LASTEXITCODE
}
