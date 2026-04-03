namespace RetroPlayHubUpdater;

internal static class UpdaterPaths
{
    public static string ResolveInstallRoot(string? installRoot)
    {
        if (!string.IsNullOrWhiteSpace(installRoot))
        {
            return installRoot.TrimEnd(Path.DirectorySeparatorChar);
        }

        var stableInstallDirectory = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "RetroPlayHubLauncher");

        if (InstallationRootResolver.LooksLikeLauncherBundleRoot(stableInstallDirectory))
        {
            return stableInstallDirectory;
        }

        return InstallationRootResolver.Resolve();
    }

    public static string ReadLauncherVersion(string installRoot)
    {
        var versionPath = Path.Combine(installRoot, "launcher_version.txt");
        if (!File.Exists(versionPath))
        {
            return "0.7.4";
        }

        var value = File.ReadAllText(versionPath).Trim();
        return string.IsNullOrWhiteSpace(value) ? "0.7.4" : value;
    }
}
