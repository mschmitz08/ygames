using System.Diagnostics;
using System.Windows.Forms;

namespace RetroPlayHubLauncher;

internal static class LauncherApplication
{
    public static int Run(string[] args)
    {
        var options = LaunchOptions.Parse(args);
        using var splash = new LauncherSplashForm();
        LauncherPaths? paths = null;

        string SaveRunLog()
        {
            if (paths is null)
            {
                return string.Empty;
            }

            return LauncherRunLog.Write(paths, splash.GetLogText());
        }

        void SetSplashStatus(string headline, string body, string? logLine = null)
        {
            splash.SetStatus(headline, body);
            if (!string.IsNullOrWhiteSpace(logLine))
            {
                splash.AppendLog(logLine ?? string.Empty);
            }

            Application.DoEvents();
        }

        splash.Show();
        SetSplashStatus(
            "Preparing your game...",
            "Checking launcher files, preparing applet settings, and getting the game window ready.",
            "Launcher started.");

        SetSplashStatus(
            "Checking for updates...",
            "Making sure your RetroPlayHub launcher and game files are ready before we open the game window.",
            "Checking for launcher and client updates.");

        paths = LauncherPaths.CreateDefault();
        var layout = LauncherLayout.Resolve(paths, options);
        LauncherStorage.EnsureLayoutExists(paths, layout);
        splash.AppendLog("Verified launcher layout and install folders.");
        var updateOutcome = UpdaterPreflight.CheckForUpdates(
            options,
            paths,
            line => splash.AppendLog(line ?? string.Empty),
            out var updateDetails);

        if (updateOutcome is UpdaterCheckOutcome.LauncherUpdateNeeded or UpdaterCheckOutcome.ClientUpdateNeeded)
        {
            var logPath = SaveRunLog();
            splash.Close();
            return ShowDetails(
                "Update Required",
                "RetroPlayHub Launcher needs to finish updating before it can start the game.",
                updateDetails,
                logPath,
                MessageBoxIcon.Information,
                (int)updateOutcome);
        }

        if (updateOutcome == UpdaterCheckOutcome.InstallerLaunched)
        {
            var logPath = SaveRunLog();
            splash.Close();
            return ShowDetails(
                "Installer Started",
                "RetroPlayHub Launcher started the installer. Finish that update, then launch the game again.",
                updateDetails,
                logPath,
                MessageBoxIcon.Information,
                0);
        }

        if (updateOutcome == UpdaterCheckOutcome.UpdateCheckFailed)
        {
            var logPath = SaveRunLog();
            splash.Close();
            return ShowDetails(
                "Update Check Failed",
                "RetroPlayHub Launcher could not complete its update check.",
                updateDetails,
                logPath,
                MessageBoxIcon.Error,
                1);
        }

        LauncherStorage.EnsureSiteBundle(paths, layout);
        var settings = LauncherSettings.Load(layout, options);
        splash.AppendLog("Verified active site bundle and launcher settings.");

        SetSplashStatus(
            "Preparing launch files...",
            "Building the policy and applet page for your selected room.",
            "Writing launch policy and applet HTML.");

        var descriptor = AppletDescriptor.Create(options, layout);
        var policyPath = LaunchArtifactWriter.WritePolicyFile(layout);
        var htmlPath = LaunchArtifactWriter.WriteAppletHtml(layout, options, settings, descriptor);
        var appletViewerPath = AppletViewerLocator.Find(paths);
        splash.AppendLog($"Prepared launch files for {descriptor.RoomLabel}.");

        if (!File.Exists(Path.Combine(layout.AppDirectory, "client.jar")))
        {
            splash.AppendLog($"Missing client.jar in {layout.AppDirectory}.");
            var logPath = SaveRunLog();
            splash.Close();
            return ShowDetails(
                "Missing client.jar",
                "RetroPlayHub Launcher could not find client.jar in the current app bundle.",
                layout.AppDirectory,
                logPath,
                MessageBoxIcon.Error,
                1);
        }

        if (!Directory.Exists(Path.Combine(layout.AppDirectory, "yog")))
        {
            splash.AppendLog($"Missing yog resources in {layout.AppDirectory}.");
            var logPath = SaveRunLog();
            splash.Close();
            return ShowDetails(
                "Missing Resources",
                "RetroPlayHub Launcher could not find the yog resource folder in the current app bundle.",
                layout.AppDirectory,
                logPath,
                MessageBoxIcon.Error,
                1);
        }

        if (string.IsNullOrWhiteSpace(appletViewerPath))
        {
            var logPath = SaveRunLog();
            splash.Close();
            return ShowAppletViewerRequired(paths, logPath);
        }

        SetSplashStatus(
            "Launching your game...",
            $"Opening {descriptor.PageTitle} in {descriptor.RoomLabel}.",
            $"Launching AppletViewer from {appletViewerPath}.");

        AppletLauncher.Launch(appletViewerPath, policyPath, htmlPath, layout.AppDirectory);
        splash.AppendLog("AppletViewer launch requested successfully.");
        SaveRunLog();
        splash.Close();

        return 0;
    }

    private static int ShowDetails(
        string headline,
        string body,
        string details,
        string logPath,
        MessageBoxIcon icon,
        int exitCode)
    {
        using var dialog = new LauncherDetailsForm(
            headline,
            body,
            string.IsNullOrWhiteSpace(details) ? "(no additional details)" : details.Trim(),
            logPath,
            icon);
        dialog.ShowDialog();

        return exitCode;
    }

    private static int ShowAppletViewerRequired(LauncherPaths paths, string logPath)
    {
        var message =
            "RetroPlayHub Launcher needs Java 8 with AppletViewer before it can start the game." +
            Environment.NewLine + Environment.NewLine +
            "Recommended download:" + Environment.NewLine +
            "Azul Zulu Java 8 JDK for Windows x64" + Environment.NewLine +
            PrerequisiteLinks.Java8JdkUrl + Environment.NewLine + Environment.NewLine +
            "Would you like to open that Java download page now?";

        var result = MessageBox.Show(
            message,
            $"{LauncherBranding.ProductName} - Java Required",
            MessageBoxButtons.YesNo,
            MessageBoxIcon.Information);

        if (result == DialogResult.Yes)
        {
            try
            {
                Process.Start(new ProcessStartInfo
                {
                    FileName = PrerequisiteLinks.Java8JdkUrl,
                    UseShellExecute = true
                });
            }
            catch
            {
                // If the browser handoff fails, fall through to diagnostics.
            }
        }

        ShowDetails(
            "AppletViewer Required",
            "This launcher needs Java 8 AppletViewer support before it can start the game.",
            AppletViewerLocator.GetDiagnostics(paths),
            logPath,
            MessageBoxIcon.Error,
            1);

        return 1;
    }
}
