using System.Diagnostics;
using System.Windows.Forms;

namespace RetroPlayHubLauncher;

internal static class LauncherApplication
{
    public static int Run(string[] args)
    {
        var options = LaunchOptions.Parse(args);
        using var splash = new LauncherSplashForm();
        splash.Show();
        Application.DoEvents();

        splash.SetStatus(
            "Checking for updates...",
            "Making sure your RetroPlayHub launcher and game files are ready before we open the game window.");
        Application.DoEvents();

        var paths = LauncherPaths.CreateDefault();
        var layout = LauncherLayout.Resolve(paths, options);
        LauncherStorage.EnsureLayoutExists(paths, layout);
        var updateOutcome = UpdaterPreflight.CheckForUpdates(options, paths, out var updateDetails);

        if (updateOutcome is UpdaterCheckOutcome.LauncherUpdateNeeded or UpdaterCheckOutcome.ClientUpdateNeeded)
        {
            splash.Close();
            return ShowMessage(
                "Update Required",
                "RetroPlayHub Launcher needs to finish updating before it can start the game.",
                updateDetails,
                MessageBoxIcon.Information,
                (int)updateOutcome);
        }

        if (updateOutcome == UpdaterCheckOutcome.InstallerLaunched)
        {
            splash.Close();
            return ShowMessage(
                "Installer Started",
                "RetroPlayHub Launcher started the installer. Finish that update, then launch the game again.",
                updateDetails,
                MessageBoxIcon.Information,
                0);
        }

        if (updateOutcome == UpdaterCheckOutcome.UpdateCheckFailed)
        {
            splash.Close();
            return ShowMessage(
                "Update Check Failed",
                "RetroPlayHub Launcher could not complete its update check.",
                updateDetails,
                MessageBoxIcon.Error,
                1);
        }

        LauncherStorage.EnsureSiteBundle(paths, layout);
        var settings = LauncherSettings.Load(layout, options);

        splash.SetStatus(
            "Preparing launch files...",
            "Building the policy and applet page for your selected room.");
        Application.DoEvents();

        var descriptor = AppletDescriptor.Create(options, layout);
        var policyPath = LaunchArtifactWriter.WritePolicyFile(layout);
        var htmlPath = LaunchArtifactWriter.WriteAppletHtml(layout, options, settings, descriptor);
        var appletViewerPath = AppletViewerLocator.Find(paths);

        if (!File.Exists(Path.Combine(layout.AppDirectory, "client.jar")))
        {
            splash.Close();
            return ShowMessage(
                "Missing client.jar",
                "RetroPlayHub Launcher could not find client.jar in the current app bundle.",
                layout.AppDirectory,
                MessageBoxIcon.Error,
                1);
        }

        if (!Directory.Exists(Path.Combine(layout.AppDirectory, "yog")))
        {
            splash.Close();
            return ShowMessage(
                "Missing Resources",
                "RetroPlayHub Launcher could not find the yog resource folder in the current app bundle.",
                layout.AppDirectory,
                MessageBoxIcon.Error,
                1);
        }

        if (string.IsNullOrWhiteSpace(appletViewerPath))
        {
            splash.Close();
            return ShowAppletViewerRequired(paths);
        }

        splash.SetStatus(
            "Launching your game...",
            $"Opening {descriptor.PageTitle} in {descriptor.RoomLabel}.");
        Application.DoEvents();

        AppletLauncher.Launch(appletViewerPath, policyPath, htmlPath, layout.AppDirectory);
        splash.Close();

        return 0;
    }

    private static int ShowMessage(
        string headline,
        string body,
        string details,
        MessageBoxIcon icon,
        int exitCode)
    {
        var message = body;
        if (!string.IsNullOrWhiteSpace(details))
        {
            message += Environment.NewLine + Environment.NewLine + details.Trim();
        }

        MessageBox.Show(
            message,
            $"{LauncherBranding.ProductName} - {headline}",
            MessageBoxButtons.OK,
            icon);

        return exitCode;
    }

    private static int ShowAppletViewerRequired(LauncherPaths paths)
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

        ShowMessage(
            "AppletViewer Required",
            "This launcher needs Java 8 AppletViewer support before it can start the game.",
            AppletViewerLocator.GetDiagnostics(paths),
            MessageBoxIcon.Error,
            1);

        return 1;
    }
}
