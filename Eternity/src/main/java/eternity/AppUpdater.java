package eternity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class AppUpdater {
    private AppUpdater() { /* Utility class */ }

    static boolean canAutoUpdate(UpdateChecker.UpdateInfo updateInfo) {
        return updateInfo != null
                && updateInfo.hasArtifact()
                && AppPaths.currentAppJarPath() != null
                && AppPaths.relaunchSpec() != null;
    }

    static void installUpdate(UpdateChecker.UpdateInfo updateInfo, Listener listener) {
        Thread worker = new Thread(() -> runInstall(updateInfo, listener), "eternity-update-installer");
        worker.setDaemon(true);
        worker.start();
    }

    private static void runInstall(UpdateChecker.UpdateInfo updateInfo, Listener listener) {
        try {
            Path targetJar = AppPaths.currentAppJarPath();
            AppPaths.RelaunchSpec relaunchSpec = AppPaths.relaunchSpec();
            if (targetJar == null || relaunchSpec == null) {
                throw new IOException("Automatic updating is not supported in this launch mode");
            }

            Path stagedJar = Files.createTempFile("eternity-update-", ".jar");
            listener.onStatus("Downloading " + updateInfo.latestVersion() + "...");
            UpdateDownloader.downloadTo(updateInfo.artifactUrl(), stagedJar);

            if (updateInfo.sha256() != null && !updateInfo.sha256().isBlank()) {
                listener.onStatus("Verifying update...");
                verifySha256(stagedJar, updateInfo.sha256());
            }

            listener.onStatus("Applying update and restarting...");
            launchUpdaterScript(stagedJar, targetJar, relaunchSpec);
            listener.onReadyToExit();
        } catch (Exception ex) {
            listener.onFailure(ex.getMessage() == null ? "Update failed" : ex.getMessage());
        }
    }

    private static void verifySha256(Path file, String expectedHash) throws Exception {
        String expected = expectedHash.trim().toLowerCase();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] actualBytes = digest.digest(Files.readAllBytes(file));
        StringBuilder actual = new StringBuilder();
        for (byte current : actualBytes) {
            actual.append(String.format("%02x", current));
        }
        if (!actual.toString().equals(expected)) {
            throw new IOException("Downloaded update checksum did not match");
        }
    }

    private static void launchUpdaterScript(Path stagedJar, Path targetJar, AppPaths.RelaunchSpec relaunchSpec) throws Exception {
        Path scriptPath = Files.createTempFile("eternity-update-", isWindows() ? ".ps1" : ".sh");
        Files.writeString(scriptPath, isWindows() ? buildWindowsUpdaterScript() : buildUnixUpdaterScript());

        List<String> command = buildScriptLaunchCommand(scriptPath, stagedJar, targetJar, relaunchSpec, ProcessHandle.current().pid());
        new ProcessBuilder(command).start();
    }

    private static List<String> buildScriptLaunchCommand(
            Path scriptPath,
            Path stagedJar,
            Path targetJar,
            AppPaths.RelaunchSpec relaunchSpec,
            long currentPid) {
        List<String> command = new ArrayList<>();
        if (isWindows()) {
            command.add("powershell.exe");
            command.add("-ExecutionPolicy");
            command.add("Bypass");
            command.add("-WindowStyle");
            command.add("Hidden");
            command.add("-File");
            command.add(scriptPath.toString());
            command.add("-SourceJar");
            command.add(stagedJar.toString());
            command.add("-TargetJar");
            command.add(targetJar.toString());
            command.add("-LaunchExecutable");
            command.add(relaunchSpec.executable());
            command.add("-CurrentPid");
            command.add(Long.toString(currentPid));
            if (!relaunchSpec.arguments().isEmpty()) {
                command.add("-LaunchArguments");
                command.add(String.join("\u0000", relaunchSpec.arguments()));
            }
            return command;
        }

        command.add("/bin/sh");
        command.add(scriptPath.toString());
        command.add(stagedJar.toString());
        command.add(targetJar.toString());
        command.add(relaunchSpec.executable());
        command.add(Long.toString(currentPid));
        command.addAll(relaunchSpec.arguments());
        return command;
    }

    private static String buildWindowsUpdaterScript() {
        return """
param(
    [string]$SourceJar,
    [string]$TargetJar,
    [string]$LaunchExecutable,
    [string]$CurrentPid,
    [string]$LaunchArguments = ""
)

$ErrorActionPreference = "Stop"

$deadline = (Get-Date).AddSeconds(30)
while ($true) {
    $process = Get-Process -Id $CurrentPid -ErrorAction SilentlyContinue
    if ($null -eq $process) {
        break
    }
    if ((Get-Date) -gt $deadline) {
        break
    }
    Start-Sleep -Milliseconds 500
}

Start-Sleep -Milliseconds 500
Copy-Item -LiteralPath $SourceJar -Destination $TargetJar -Force
if ($LaunchArguments) {
    $arguments = $LaunchArguments -split [char]0
    Start-Process -FilePath $LaunchExecutable -ArgumentList $arguments
} else {
    Start-Process -FilePath $LaunchExecutable
}
Remove-Item -LiteralPath $SourceJar -Force -ErrorAction SilentlyContinue
Remove-Item -LiteralPath $PSCommandPath -Force -ErrorAction SilentlyContinue
""";
    }

    private static String buildUnixUpdaterScript() {
        return """
#!/bin/sh
set -eu

SOURCE_JAR="$1"
TARGET_JAR="$2"
LAUNCH_EXECUTABLE="$3"
CURRENT_PID="$4"
shift 4

DEADLINE=$(( $(date +%s) + 30 ))
while kill -0 "$CURRENT_PID" 2>/dev/null; do
    if [ "$(date +%s)" -ge "$DEADLINE" ]; then
        break
    fi
    sleep 0.5
done

sleep 0.5
cp "$SOURCE_JAR" "$TARGET_JAR"

if [ "$#" -gt 0 ]; then
    "$LAUNCH_EXECUTABLE" "$@" >/dev/null 2>&1 &
else
    "$LAUNCH_EXECUTABLE" >/dev/null 2>&1 &
fi

rm -f "$SOURCE_JAR" "$0"
""";
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    interface Listener {
        void onStatus(String text);
        void onReadyToExit();
        void onFailure(String message);
    }
}
