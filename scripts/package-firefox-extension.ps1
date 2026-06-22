param(
    [string]$OutputDir = "target/firefox-extension"
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
$stagingDir = Join-Path $stagingRoot "roll20-bridge-firefox"
$zipPath = Join-Path $stagingRoot ("roll20-bridge-firefox-v{0}.zip" -f $version)

if (Test-Path $stagingDir) {
    Remove-Item -LiteralPath $stagingDir -Recurse -Force
}

if (Test-Path $zipPath) {
    Remove-Item -LiteralPath $zipPath -Force
}

New-Item -ItemType Directory -Path $stagingDir -Force | Out-Null

$firefoxManifest = $manifest | ConvertTo-Json -Depth 20 | ConvertFrom-Json

if (-not $firefoxManifest.PSObject.Properties["browser_specific_settings"]) {
    $firefoxManifest | Add-Member -NotePropertyName "browser_specific_settings" -NotePropertyValue ([pscustomobject]@{})
}

$firefoxManifest.browser_specific_settings = [pscustomobject]@{
    gecko = [pscustomobject]@{
        id = "roll20-bridge@eternity.local"
    }
}
$firefoxManifest.name = "Firefox Roll20-EternityTTRPG Bridge"

$firefoxManifest | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath (Join-Path $stagingDir "manifest.json")
Copy-Item -LiteralPath (Join-Path $sourceDir "content.js") -Destination $stagingDir
Copy-Item -LiteralPath (Join-Path $sourceDir "pageBridge.js") -Destination $stagingDir

@"
Firefox Roll20-EternityTTRPG Bridge

Temporary test install:
1. Open Firefox.
2. Open about:debugging.
3. Click This Firefox.
4. Click Load Temporary Add-on.
5. Select this ZIP file, or extract it and select any file inside the extension folder.

Note:
- Temporary installs are removed when Firefox restarts.
- Wider tester distribution in Firefox requires a Mozilla-signed package.
"@ | Set-Content -LiteralPath (Join-Path $stagingDir "INSTALL.txt")

Compress-Archive -Path (Join-Path $stagingDir "*") -DestinationPath $zipPath -Force

Write-Host "Created Firefox extension package:"
Write-Host $zipPath
