# Requires: Windows PowerShell 5+
# Wrapped by the Eternity installer EXE. Tokens @PROJECT_VERSION@ and @INNER_SETUP_NAME@
# are replaced at package time.

param()

$ErrorActionPreference = "Stop"
$ProductName = "Eternity"
$InstallerVersion = "@PROJECT_VERSION@"
$InnerSetupName = "@INNER_SETUP_NAME@"
$UserDataRoot = Join-Path $env:LOCALAPPDATA $ProductName
$UserCharactersDir = Join-Path $UserDataRoot "Characters"
$UserImagesDir = Join-Path $UserDataRoot "Images"

function Test-IsAdministrator {
    $identity = [Security.Principal.WindowsIdentity]::GetCurrent()
    $principal = New-Object Security.Principal.WindowsPrincipal($identity)
    return $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

function Show-Message {
    param(
        [string]$Text,
        [string]$Title = "Eternity Installer",
        [System.Windows.Forms.MessageBoxButtons]$Buttons = [System.Windows.Forms.MessageBoxButtons]::OK,
        [System.Windows.Forms.MessageBoxIcon]$Icon = [System.Windows.Forms.MessageBoxIcon]::Information
    )
    return [System.Windows.Forms.MessageBox]::Show($Text, $Title, $Buttons, $Icon)
}

function Compare-VersionStrings {
    param([string]$Left, [string]$Right)
    $leftParts = @([regex]::Matches(($Left + ""), "\d+") | ForEach-Object { [int]$_.Value })
    $rightParts = @([regex]::Matches(($Right + ""), "\d+") | ForEach-Object { [int]$_.Value })
    if ($leftParts.Count -eq 0) { $leftParts = @(0) }
    if ($rightParts.Count -eq 0) { $rightParts = @(0) }
    $max = [Math]::Max($leftParts.Count, $rightParts.Count)
    for ($i = 0; $i -lt $max; $i++) {
        $l = if ($i -lt $leftParts.Count) { $leftParts[$i] } else { 0 }
        $r = if ($i -lt $rightParts.Count) { $rightParts[$i] } else { 0 }
        if ($l -ne $r) { return [Math]::Sign($l - $r) }
    }
    return 0
}

function Get-ExistingEternityInstalls {
    $paths = @(
        "HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\*",
        "HKLM:\SOFTWARE\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall\*",
        "HKCU:\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\*"
    )
    $found = @()
    foreach ($path in $paths) {
        Get-ItemProperty -Path $path -ErrorAction SilentlyContinue | ForEach-Object {
            $name = "" + $_.DisplayName
            if ($name -ne $ProductName -and $name -notlike "Eternity TTRPG*") { return }
            if ($name -like "*Setup*" -or $name -like "*Installer*") { return }

            $installLocation = "" + $_.InstallLocation
            if ([string]::IsNullOrWhiteSpace($installLocation) -and $_.UninstallString) {
                $uninstall = [string]$_.UninstallString
                if ($uninstall -match '(?i)MsiExec\.exe') {
                    $installLocation = ""
                } else {
                    $exe = ($uninstall.Trim('"') -split '"')[0]
                    if (Test-Path -LiteralPath $exe) {
                        $installLocation = Split-Path -Parent $exe
                    }
                }
            }

            $found += [pscustomobject]@{
                DisplayName = $name
                DisplayVersion = "" + $_.DisplayVersion
                InstallLocation = $installLocation.TrimEnd('\')
                UninstallString = "" + $_.UninstallString
                QuietUninstallString = "" + $_.QuietUninstallString
                ProductCode = $_.PSChildName
            }
        }
    }

    foreach ($candidate in @(
            (Join-Path ${env:ProgramFiles} $ProductName),
            (Join-Path ${env:ProgramFiles(x86)} $ProductName)
        )) {
        if (-not (Test-Path -LiteralPath $candidate)) { continue }
        $already = $found | Where-Object { $_.InstallLocation -and (Join-Path $_.InstallLocation ".") -eq (Join-Path $candidate ".") }
        if ($already) { continue }
        $found += [pscustomobject]@{
            DisplayName = $ProductName
            DisplayVersion = ""
            InstallLocation = $candidate
            UninstallString = ""
            QuietUninstallString = ""
            ProductCode = ""
        }
    }

    return $found | Sort-Object DisplayVersion, InstallLocation -Unique
}

function Copy-DirectoryMerge {
    param([string]$Source, [string]$Destination)
    if ([string]::IsNullOrWhiteSpace($Source) -or -not (Test-Path -LiteralPath $Source)) {
        return $false
    }
    $sourceFull = [System.IO.Path]::GetFullPath($Source)
    $destFull = [System.IO.Path]::GetFullPath($Destination)
    if ($sourceFull.TrimEnd('\') -ieq $destFull.TrimEnd('\')) {
        return $false
    }

    New-Item -ItemType Directory -Force -Path $Destination | Out-Null
    $copiedAny = $false
    Get-ChildItem -LiteralPath $Source -Recurse -Force | ForEach-Object {
        $relative = $_.FullName.Substring($sourceFull.Length).TrimStart('\')
        if ($relative -ieq "app icons" -or $relative -like "app icons\*") {
            return
        }
        $target = Join-Path $Destination $relative
        if ($_.PSIsContainer) {
            New-Item -ItemType Directory -Force -Path $target | Out-Null
            return
        }
        $targetParent = Split-Path -Parent $target
        New-Item -ItemType Directory -Force -Path $targetParent | Out-Null
        if (-not (Test-Path -LiteralPath $target)) {
            Copy-Item -LiteralPath $_.FullName -Destination $target
            $copiedAny = $true
        }
    }
    return $copiedAny
}

function Copy-InstallUserData {
    param($Install)
    $roots = @()
    if ($Install.InstallLocation) { $roots += $Install.InstallLocation }
    foreach ($root in $roots) {
        Copy-DirectoryMerge -Source (Join-Path $root "Characters") -Destination $UserCharactersDir | Out-Null
        Copy-DirectoryMerge -Source (Join-Path $root "app\Characters") -Destination $UserCharactersDir | Out-Null
        Copy-DirectoryMerge -Source (Join-Path $root "Images") -Destination $UserImagesDir | Out-Null
        Copy-DirectoryMerge -Source (Join-Path $root "app\Images") -Destination $UserImagesDir | Out-Null
    }
    New-Item -ItemType Directory -Force -Path (Join-Path $UserCharactersDir "Auto") | Out-Null
    New-Item -ItemType Directory -Force -Path (Join-Path $UserCharactersDir "Backup") | Out-Null
}

function Remove-LegacyUserData {
    $legacyPaths = @(
        (Join-Path $UserImagesDir "app icons")
    )
    foreach ($path in $legacyPaths) {
        if (Test-Path -LiteralPath $path) {
            Remove-Item -LiteralPath $path -Recurse -Force
        }
    }
}

function Uninstall-Eternity {
    param($Install)
    if ($Install.QuietUninstallString) {
        $cmd = $Install.QuietUninstallString
        cmd.exe /c $cmd
        return
    }

    $uninstall = $Install.UninstallString
    if ($uninstall -match '\{[0-9A-Fa-f-]{36}\}') {
        $code = $Matches[0]
        Start-Process -FilePath "msiexec.exe" -ArgumentList "/x", $code, "/qn" -Wait -NoNewWindow
        return
    }

    if (-not [string]::IsNullOrWhiteSpace($uninstall)) {
        cmd.exe /c $uninstall
        return
    }

    if ($Install.ProductCode -match '\{[0-9A-Fa-f-]{36}\}') {
        Start-Process -FilePath "msiexec.exe" -ArgumentList "/x", $Install.ProductCode, "/qn" -Wait -NoNewWindow
    }
}

function Get-InnerSetupPath {
    $searchRoots = @(
        $workDir,
        (Get-Location).Path,
        (Split-Path -Parent $PSCommandPath)
    ) | Select-Object -Unique
    foreach ($here in $searchRoots) {
        if ([string]::IsNullOrWhiteSpace($here) -or -not (Test-Path -LiteralPath $here)) { continue }
        $direct = Join-Path $here $InnerSetupName
        if (Test-Path -LiteralPath $direct) { return $direct }
        $matches = @(Get-ChildItem -LiteralPath $here -Filter "*.exe" | Where-Object {
            $_.Name -ne "Install-Eternity.exe" -and $_.Name -notlike "Install.cmd"
        })
        if ($matches.Count -ge 1) { return $matches[0].FullName }
    }
    throw "Could not find the inner Eternity setup executable."
}

Add-Type -AssemblyName System.Windows.Forms

# IExpress deletes its extract folder when the first process exits, so copy to a stable work dir.
$workDir = Join-Path $env:TEMP ("EternitySetup-" + $InstallerVersion)
New-Item -ItemType Directory -Force -Path $workDir | Out-Null
Copy-Item -Path (Join-Path (Split-Path -Parent $PSCommandPath) "*") -Destination $workDir -Force
$workScript = Join-Path $workDir "Install-Eternity.ps1"

if (-not (Test-IsAdministrator)) {
    $process = Start-Process -FilePath "powershell.exe" -Verb RunAs -Wait -PassThru -ArgumentList @(
        "-NoProfile",
        "-ExecutionPolicy", "Bypass",
        "-File", "`"$workScript`""
    )
    exit $process.ExitCode
}

if ((Split-Path -Parent $PSCommandPath) -ne $workDir) {
    Set-Location $workDir
}

$existing = @(Get-ExistingEternityInstalls)
$newer = @($existing | Where-Object {
    $_.DisplayVersion -and (Compare-VersionStrings $_.DisplayVersion $InstallerVersion) -gt 0
})

if ($newer.Count -gt 0) {
    $versions = ($newer | ForEach-Object { if ($_.DisplayVersion) { $_.DisplayVersion } else { "unknown" } }) -join ", "
    $choice = Show-Message -Icon Warning -Buttons YesNo -Text (
        "A newer version of Eternity is already installed ($versions).`n`n" +
        "This installer is version $InstallerVersion.`n`n" +
        "Yes: uninstall the existing version and continue.`n" +
        "No: cancel this install."
    )
    if ($choice -ne [System.Windows.Forms.DialogResult]::Yes) {
        exit 1
    }
}

foreach ($install in $existing) {
    Copy-InstallUserData -Install $install
    Uninstall-Eternity -Install $install
}

New-Item -ItemType Directory -Force -Path $UserCharactersDir | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $UserCharactersDir "Auto") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $UserCharactersDir "Backup") | Out-Null
New-Item -ItemType Directory -Force -Path $UserImagesDir | Out-Null
Remove-LegacyUserData

$innerSetup = Get-InnerSetupPath
$setup = Start-Process -FilePath $innerSetup -WorkingDirectory (Split-Path -Parent $innerSetup) -Wait -PassThru
exit $setup.ExitCode
