namespace RetroPlayHubLauncher;

internal sealed record LauncherPaths(
    string BaseDirectory,
    string StableInstallDirectory,
    string SitesRootDirectory,
    string TemplateAppDirectory)
{
    public static LauncherPaths CreateDefault()
    {
        var baseDirectory = InstallationRootResolver.Resolve();
        var localAppData = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
        var stableInstallDirectory = Path.Combine(localAppData, "RetroPlayHubLauncher");

        return new LauncherPaths(
            BaseDirectory: baseDirectory,
            StableInstallDirectory: stableInstallDirectory,
            SitesRootDirectory: Path.Combine(baseDirectory, "sites"),
            TemplateAppDirectory: Path.Combine(baseDirectory, "app", "newyahoo"));
    }
}
