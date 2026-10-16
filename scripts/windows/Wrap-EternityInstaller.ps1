param(
    [Parameter(Mandatory = $true)][string]$InstallerDir,
    [Parameter(Mandatory = $true)][string]$ProjectVersion,
    [Parameter(Mandatory = $true)][string]$ScriptsDir
)

$ErrorActionPreference = "Stop"

$csc = @(
    "$env:WINDIR\Microsoft.NET\Framework64\v4.0.30319\csc.exe",
    "$env:WINDIR\Microsoft.NET\Framework\v4.0.30319\csc.exe"
) | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
if (-not $csc) {
    throw "csc.exe was not found. Cannot wrap the Eternity installer."
}

$innerName = "Eternity-$ProjectVersion-inner.exe"
$innerSource = Join-Path $InstallerDir "Eternity-$ProjectVersion.exe"
if (-not (Test-Path -LiteralPath $innerSource)) {
    $innerSource = Get-ChildItem -LiteralPath $InstallerDir -Filter "Eternity*.exe" |
        Where-Object { $_.Name -notlike "*-inner.exe" } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1 -ExpandProperty FullName
}
if (-not $innerSource -or -not (Test-Path -LiteralPath $innerSource)) {
    throw "Could not find the jpackage installer EXE in $InstallerDir"
}

$stage = Join-Path $InstallerDir "wrapper-stage"
if (Test-Path -LiteralPath $stage) {
    Remove-Item -LiteralPath $stage -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $stage | Out-Null

Move-Item -LiteralPath $innerSource -Destination (Join-Path $stage $innerName) -Force

$installScript = Get-Content -LiteralPath (Join-Path $ScriptsDir "Install-Eternity.ps1") -Raw
$installScript = $installScript.Replace("@PROJECT_VERSION@", $ProjectVersion).Replace("@INNER_SETUP_NAME@", $innerName)
Set-Content -LiteralPath (Join-Path $stage "Install-Eternity.ps1") -Value $installScript -Encoding UTF8

$payloadZip = Join-Path $InstallerDir "payload.zip"
if (Test-Path -LiteralPath $payloadZip) {
    Remove-Item -LiteralPath $payloadZip -Force
}
Compress-Archive -Path (Join-Path $stage "*") -DestinationPath $payloadZip -Force

$versionFile = Join-Path $InstallerDir "installer-version.txt"
Set-Content -LiteralPath $versionFile -Value $ProjectVersion -Encoding ASCII

$finalExe = Join-Path $InstallerDir "Eternity-$ProjectVersion.exe"
$bootstrap = Join-Path $ScriptsDir "EternitySetupBootstrap.cs"
$frameworkDir = Split-Path -Parent $csc

& $csc /nologo /target:winexe /optimize+ `
    /r:System.Windows.Forms.dll `
    /r:System.Drawing.dll `
    "/r:$frameworkDir\System.IO.Compression.dll" `
    "/r:$frameworkDir\System.IO.Compression.FileSystem.dll" `
    /resource:"$payloadZip,payload.zip" `
    /resource:"$versionFile,installer-version.txt" `
    /out:"$finalExe" `
    "$bootstrap"

if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $finalExe)) {
    throw "Failed to compile the wrapped Eternity installer."
}

Write-Host "Wrapped installer: $finalExe"
