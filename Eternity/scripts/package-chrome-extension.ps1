param(
    [string]$OutputDir = "target/chrome-extension"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$sourceDir = Join-Path $repoRoot "src/main/java/websocket"
$manifestPath = Join-Path $sourceDir "manifest.json"

if (-not (Test-Path $manifestPath)) {
    throw "Could not find manifest at $manifestPath"
}

$manifest = Get-Content $manifestPath -Raw | ConvertFrom-Json
$version = if ($manifest.version) { $manifest.version } else { "dev" }

$stagingRoot = Join-Path $repoRoot $OutputDir
$stagingDir = Join-Path $stagingRoot "roll20-bridge"
$zipPath = Join-Path $stagingRoot ("roll20-bridge-v{0}.zip" -f $version)

if (Test-Path $stagingDir) {
    Remove-Item -LiteralPath $stagingDir -Recurse -Force
}

if (Test-Path $zipPath) {
    Remove-Item -LiteralPath $zipPath -Force
}

New-Item -ItemType Directory -Path $stagingDir -Force | Out-Null

Copy-Item -LiteralPath (Join-Path $sourceDir "manifest.json") -Destination $stagingDir
Copy-Item -LiteralPath (Join-Path $sourceDir "content.js") -Destination $stagingDir
Copy-Item -LiteralPath (Join-Path $sourceDir "pageBridge.js") -Destination $stagingDir

@"
Roll20 Bridge Chrome Extension

Install:
1. Extract this folder.
2. Open chrome://extensions
3. Enable Developer mode.
4. Click Load unpacked.
5. Select this extracted folder.
"@ | Set-Content -LiteralPath (Join-Path $stagingDir "INSTALL.txt")

Compress-Archive -Path (Join-Path $stagingDir "*") -DestinationPath $zipPath -Force

Write-Host "Created extension package:"
Write-Host $zipPath
