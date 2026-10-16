using System;
using System.Diagnostics;
using System.IO;
using System.IO.Compression;
using System.Reflection;
using System.Security.Principal;
using System.Windows.Forms;

internal static class EternitySetupBootstrap
{
    [STAThread]
    private static int Main()
    {
        try
        {
            string workDir = Path.Combine(Path.GetTempPath(), "EternitySetup-" + GetEmbeddedVersion());
            if (Directory.Exists(workDir))
            {
                try { Directory.Delete(workDir, true); } catch { }
            }
            Directory.CreateDirectory(workDir);
            string payloadDir = Path.Combine(workDir, "payload");
            ExtractPayload(workDir, payloadDir);

            if (!IsAdministrator())
            {
                ProcessStartInfo info = new ProcessStartInfo
                {
                    FileName = Assembly.GetExecutingAssembly().Location,
                    UseShellExecute = true,
                    Verb = "runas"
                };
                Process elevated = Process.Start(info);
                if (elevated == null)
                {
                    return 1;
                }
                elevated.WaitForExit();
                return elevated.ExitCode;
            }

            string script = Path.Combine(payloadDir, "Install-Eternity.ps1");
            if (!File.Exists(script))
            {
                throw new FileNotFoundException("Install-Eternity.ps1 was missing from the installer payload.", script);
            }

            ProcessStartInfo psi = new ProcessStartInfo
            {
                FileName = "powershell.exe",
                Arguments = "-NoProfile -ExecutionPolicy Bypass -File \"" + script + "\"",
                UseShellExecute = false,
                WorkingDirectory = payloadDir
            };
            Process install = Process.Start(psi);
            if (install == null)
            {
                return 1;
            }
            install.WaitForExit();
            return install.ExitCode;
        }
        catch (Exception ex)
        {
            MessageBox.Show(ex.Message, "Eternity Installer", MessageBoxButtons.OK, MessageBoxIcon.Error);
            return 1;
        }
    }

    private static bool IsAdministrator()
    {
        WindowsIdentity identity = WindowsIdentity.GetCurrent();
        WindowsPrincipal principal = new WindowsPrincipal(identity);
        return principal.IsInRole(WindowsBuiltInRole.Administrator);
    }

    private static string GetEmbeddedVersion()
    {
        using (Stream stream = Assembly.GetExecutingAssembly().GetManifestResourceStream("installer-version.txt"))
        {
            if (stream == null)
            {
                return "dev";
            }
            using (StreamReader reader = new StreamReader(stream))
            {
                return reader.ReadToEnd().Trim();
            }
        }
    }

    private static void ExtractPayload(string workDir, string payloadDir)
    {
        using (Stream stream = Assembly.GetExecutingAssembly().GetManifestResourceStream("payload.zip"))
        {
            if (stream == null)
            {
                throw new InvalidOperationException("Installer payload.zip resource is missing.");
            }
            string zipPath = Path.Combine(workDir, "payload.zip");
            using (FileStream file = File.Create(zipPath))
            {
                stream.CopyTo(file);
            }
            if (Directory.Exists(payloadDir))
            {
                Directory.Delete(payloadDir, true);
            }
            ZipFile.ExtractToDirectory(zipPath, payloadDir);
        }
    }
}
