param(
    [string]$Configuration = "Release",
    [string]$Version = "",
    [string]$OutputDirectory = "",
    [switch]$PublishToDownloads
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent (Split-Path -Parent (Split-Path -Parent $PSScriptRoot))
$windowsRoot = Join-Path $repoRoot "launcher\windows"
$srcRoot = Join-Path $windowsRoot "src"
$downloadsRoot = Join-Path $repoRoot "ny\downloads\ygames_launcher_windows"
$dotnetHome = Join-Path $repoRoot ".dotnet-home"
$nugetConfig = Join-Path $dotnetHome "NuGet.Config"

New-Item -ItemType Directory -Force -Path $dotnetHome | Out-Null
$env:DOTNET_CLI_HOME = $dotnetHome
$env:HOME = $dotnetHome

if (-not (Test-Path $nugetConfig)) {
    @"
<?xml version="1.0" encoding="utf-8"?>
<configuration>
  <packageSources>
    <clear />
    <add key="nuget.org" value="https://api.nuget.org/v3/index.json" />
  </packageSources>
</configuration>
"@ | Set-Content -Encoding UTF8 $nugetConfig
}

if (-not (Test-Path $downloadsRoot)) {
    throw "Launcher bundle root not found: $downloadsRoot"
}

$launcherProject = Join-Path $srcRoot "RetroPlayHubLauncher\RetroPlayHubLauncher.csproj"
$updaterProject = Join-Path $srcRoot "RetroPlayHubUpdater\RetroPlayHubUpdater.csproj"
$publishRoot = Join-Path $windowsRoot "out\publish"
$launcherPublishRoot = Join-Path $publishRoot "RetroPlayHubLauncher"
$updaterPublishRoot = Join-Path $publishRoot "RetroPlayHubUpdater"
$stagingRoot = Join-Path $windowsRoot "out\msi-layout"
$generatedRoot = Join-Path $PSScriptRoot "generated"

if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $OutputDirectory = Join-Path $windowsRoot "out\installer"
}

New-Item -ItemType Directory -Force -Path $publishRoot | Out-Null
New-Item -ItemType Directory -Force -Path $stagingRoot | Out-Null
New-Item -ItemType Directory -Force -Path $generatedRoot | Out-Null
New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null

Write-Host "Publishing RetroPlayHubLauncher..."
if (Test-Path $launcherPublishRoot) {
    Remove-Item -Recurse -Force $launcherPublishRoot
}
dotnet publish $launcherProject -c $Configuration -o $launcherPublishRoot --no-restore -p:UseAppHost=true
if ($LASTEXITCODE -ne 0) {
    throw "dotnet publish failed for RetroPlayHubLauncher"
}

Write-Host "Publishing RetroPlayHubUpdater..."
if (Test-Path $updaterPublishRoot) {
    Remove-Item -Recurse -Force $updaterPublishRoot
}
dotnet publish $updaterProject -c $Configuration -o $updaterPublishRoot --no-restore -p:UseAppHost=true
if ($LASTEXITCODE -ne 0) {
    throw "dotnet publish failed for RetroPlayHubUpdater"
}

Write-Host "Preparing MSI staging layout..."
if (Test-Path $stagingRoot) {
    Remove-Item -Recurse -Force $stagingRoot
}
New-Item -ItemType Directory -Force -Path $stagingRoot | Out-Null

Get-ChildItem -Path $downloadsRoot -File -ErrorAction SilentlyContinue | Where-Object {
    $_.Name -like "RetroPlayHubLauncher.*" -or $_.Name -like "RetroPlayHubUpdater.*"
} | ForEach-Object {
    Remove-Item -Force $_.FullName
}

Copy-Item -Recurse -Force (Join-Path $downloadsRoot "*") $stagingRoot
Remove-Item (Join-Path $stagingRoot "ygames_launcher.vbs") -Force -ErrorAction SilentlyContinue
Get-ChildItem -Path $stagingRoot -File -ErrorAction SilentlyContinue | Where-Object {
    $_.Name -like "RetroPlayHubLauncher.*" -or $_.Name -like "RetroPlayHubUpdater.*"
} | ForEach-Object {
    Remove-Item -Force $_.FullName
}

$launcherExecutable = Join-Path $launcherPublishRoot "RetroPlayHubLauncher.exe"
$updaterExecutable = Join-Path $updaterPublishRoot "RetroPlayHubUpdater.exe"

if (-not (Test-Path $launcherExecutable)) {
    throw "RetroPlayHubLauncher.exe was not produced at $launcherExecutable"
}

if (-not (Test-Path $updaterExecutable)) {
    throw "RetroPlayHubUpdater.exe was not produced at $updaterExecutable"
}

Copy-Item -Force $launcherExecutable (Join-Path $stagingRoot "RetroPlayHubLauncher.exe")
Copy-Item -Force $updaterExecutable (Join-Path $stagingRoot "RetroPlayHubUpdater.exe")
Get-ChildItem -Path $launcherPublishRoot -File | Where-Object { $_.Name -ne "RetroPlayHubLauncher.exe" } | ForEach-Object {
    Copy-Item -Force $_.FullName (Join-Path $stagingRoot $_.Name)
}

Get-ChildItem -Path $updaterPublishRoot -File | Where-Object { $_.Name -ne "RetroPlayHubUpdater.exe" } | ForEach-Object {
    Copy-Item -Force $_.FullName (Join-Path $stagingRoot $_.Name)
}

$launcherVersionPath = Join-Path $stagingRoot "launcher_version.txt"
if ([string]::IsNullOrWhiteSpace($Version)) {
    if (Test-Path $launcherVersionPath) {
        $Version = (Get-Content $launcherVersionPath -Raw).Trim()
    }
}

if ([string]::IsNullOrWhiteSpace($Version)) {
    $Version = "0.8.2"
}

$msiVersion = "$Version.0"

Write-Host "Refreshing native launcher files in the download bundle..."
Get-ChildItem -Path $launcherPublishRoot -File | ForEach-Object {
    Copy-Item -Force $_.FullName (Join-Path $downloadsRoot $_.Name)
}

Get-ChildItem -Path $updaterPublishRoot -File | ForEach-Object {
    Copy-Item -Force $_.FullName (Join-Path $downloadsRoot $_.Name)
}

Write-Host "Generating WiX file manifest..."
$generator = Join-Path $PSScriptRoot "Generate-WixFragment.ps1"
$fragmentPath = Join-Path $generatedRoot "LauncherFiles.wxs"
& $generator -SourceRoot $stagingRoot -OutputPath $fragmentPath

$msiName = "RetroPlayHubLauncher_$Version.msi"
$msiPath = Join-Path $OutputDirectory $msiName

Write-Host "Building MSI..."
wix build `
    (Join-Path $PSScriptRoot "RetroPlayHubLauncher.wxs") `
    $fragmentPath `
    -d LauncherSourceDir=$stagingRoot `
    -d RetroPlayHubVersion=$msiVersion `
    -o $msiPath
if ($LASTEXITCODE -ne 0) {
    throw "wix build failed"
}

Write-Host ""
Write-Host "MSI created:"
Write-Host $msiPath

if ($PublishToDownloads.IsPresent) {
    $downloadsOutputPath = Join-Path (Join-Path $repoRoot "ny\downloads") $msiName
    $downloadsLatestPath = Join-Path (Join-Path $repoRoot "ny\downloads") "RetroPlayHubLauncher.msi"

    Copy-Item -Force $msiPath $downloadsOutputPath
    Copy-Item -Force $msiPath $downloadsLatestPath

    Write-Host ""
    Write-Host "Published MSI to downloads:"
    Write-Host $downloadsOutputPath
    Write-Host $downloadsLatestPath
}
