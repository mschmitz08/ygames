namespace RetroPlayHubLauncher;

internal sealed record LauncherLayout(
    string SiteId,
    string SiteRootDirectory,
    string AppDirectory,
    string LaunchDirectory)
{
    public static LauncherLayout Resolve(LauncherPaths paths, LaunchOptions options)
    {
        if (string.IsNullOrWhiteSpace(options.SiteId))
        {
            return new LauncherLayout(
                SiteId: string.Empty,
                SiteRootDirectory: paths.BaseDirectory,
                AppDirectory: paths.TemplateAppDirectory,
                LaunchDirectory: Path.Combine(paths.BaseDirectory, "launches"));
        }

        var cleanSiteId = FileNameUtilities.CleanFileName(options.SiteId);
        var siteRootDirectory = Path.Combine(paths.SitesRootDirectory, cleanSiteId);

        return new LauncherLayout(
            SiteId: cleanSiteId,
            SiteRootDirectory: siteRootDirectory,
            AppDirectory: Path.Combine(siteRootDirectory, "app", "newyahoo"),
            LaunchDirectory: Path.Combine(siteRootDirectory, "launches"));
    }
}
