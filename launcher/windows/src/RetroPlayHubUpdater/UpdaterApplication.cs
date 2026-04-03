namespace RetroPlayHubUpdater;

internal static class UpdaterApplication
{
    public static int Run(string[] args)
    {
        var options = UpdaterOptions.Parse(args);
        var installRoot = UpdaterPaths.ResolveInstallRoot(options.InstallRoot);

        WriteInfo(options, "RetroPlayHub Updater");
        WriteInfo(options, $"Mode: {options.Mode}");
        WriteInfo(options, $"Manifest URL: {options.ManifestUrl ?? "(none)"}");
        WriteInfo(options, $"Package URL: {options.PackageUrl ?? "(none)"}");
        WriteInfo(options, $"Install Root: {installRoot}");

        var mode = options.Mode.ToLowerInvariant();
        if (mode is not "check" and not "ensure")
        {
            WriteError(options, "Only check and ensure modes are implemented so far.");
            return 1;
        }

        if (string.IsNullOrWhiteSpace(options.ManifestUrl))
        {
            WriteError(options, "Manifest URL is required for check mode.");
            return 1;
        }

        var localVersion = UpdaterPaths.ReadLauncherVersion(installRoot);
        var localClientJarPath = Path.Combine(installRoot, "app", "newyahoo", "client.jar");
        var localClientHash = HashUtilities.ComputeSha256(localClientJarPath);

        WriteInfo(options, $"Local Launcher Version: {DisplayValue(localVersion)}");
        WriteInfo(options, $"Local Client Hash: {DisplayValue(localClientHash)}");
        WriteInfo(options, string.Empty);
        WriteInfo(options, "Checking server manifest...");

        LauncherStateManifest manifest;
        try
        {
            manifest = LauncherStateManifestClient.Fetch(options.ManifestUrl);
        }
        catch (Exception ex)
        {
            WriteError(options, $"Manifest fetch failed: {ex.Message}");
            return 1;
        }

        WriteInfo(options, $"Remote Launcher Version: {DisplayValue(manifest.LauncherVersion)}");
        WriteInfo(options, $"Remote Client Hash: {DisplayValue(manifest.ClientHash)}");
        WriteInfo(options, string.Empty);

        var launcherUpdateNeeded =
            !string.IsNullOrWhiteSpace(manifest.LauncherVersion) &&
            VersionComparison.Compare(localVersion, manifest.LauncherVersion) < 0;

        var clientUpdateNeeded =
            !string.IsNullOrWhiteSpace(manifest.ClientHash) &&
            !string.Equals(localClientHash, manifest.ClientHash, StringComparison.OrdinalIgnoreCase);

        if (!launcherUpdateNeeded && !clientUpdateNeeded)
        {
            WriteInfo(options, "Status: up to date");
            return 0;
        }

        WriteActionable(options, "Status: update needed");
        WriteActionable(options, $"Launcher Update Needed: {launcherUpdateNeeded}");
        WriteActionable(options, $"Client Update Needed: {clientUpdateNeeded}");

        if (launcherUpdateNeeded)
        {
            WriteActionable(options, "Next planned behavior: download and apply a newer launcher package/MSI.");
        }

        if (clientUpdateNeeded)
        {
            WriteActionable(options, "Next planned behavior: refresh client.jar from the published launcher bundle.");
        }

        if (mode == "ensure" && clientUpdateNeeded && !launcherUpdateNeeded)
        {
            WriteActionable(options, string.Empty);
            WriteActionable(options, "Attempting to refresh client.jar...");
            if (!ClientJarRefresher.TryRefresh(options.ManifestUrl, installRoot, manifest.ClientHash, out var refreshMessage))
            {
                WriteError(options, refreshMessage);
                return 11;
            }

            WriteActionable(options, refreshMessage);
            var refreshedHash = HashUtilities.ComputeSha256(localClientJarPath);
            if (!string.Equals(refreshedHash, manifest.ClientHash, StringComparison.OrdinalIgnoreCase))
            {
                WriteError(options, "client.jar still does not match the expected hash after refresh.");
                return 11;
            }

            WriteActionable(options, "Status: client.jar refreshed successfully");
            return 0;
        }

        if (mode == "ensure" && launcherUpdateNeeded)
        {
            WriteActionable(options, string.Empty);
            WriteActionable(options, "Attempting to stage launcher package update...");
            if (!LauncherPackageStager.TryStage(options.ManifestUrl, manifest.LauncherVersion, out var stageRoot, out var stageMessage))
            {
                WriteError(options, stageMessage);
                return 10;
            }

            WriteActionable(options, stageMessage);
            WriteActionable(options, $"Staged Version: {UpdaterPaths.ReadLauncherVersion(stageRoot)}");
            WriteActionable(options, "Attempting to launch MSI installer...");

            if (!MsiPackageInstaller.TryDownloadAndLaunch(options.ManifestUrl, manifest.LauncherVersion, options.PackageUrl, out var installerMessage))
            {
                WriteError(options, installerMessage);
                return 10;
            }

            WriteActionable(options, installerMessage);
            return 12;
        }

        if (launcherUpdateNeeded)
        {
            return 10;
        }

        return 11;
    }

    private static string DisplayValue(string? value) =>
        string.IsNullOrWhiteSpace(value) ? "(none)" : value;

    private static void WriteInfo(UpdaterOptions options, string message)
    {
        if (options.Quiet)
        {
            return;
        }

        Console.WriteLine(message);
    }

    private static void WriteError(UpdaterOptions options, string message)
    {
        if (options.Quiet)
        {
            Console.Error.WriteLine(message);
            return;
        }

        Console.WriteLine();
        Console.WriteLine(message);
    }

    private static void WriteActionable(UpdaterOptions options, string message)
    {
        if (options.Quiet)
        {
            Console.Error.WriteLine(message);
            return;
        }

        Console.WriteLine(message);
    }
}
