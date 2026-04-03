namespace RetroPlayHubLauncher;

internal static class LauncherStorage
{
    public static void EnsureLayoutExists(LauncherPaths paths, LauncherLayout layout)
    {
        Directory.CreateDirectory(paths.SitesRootDirectory);
        Directory.CreateDirectory(layout.LaunchDirectory);

        if (!string.IsNullOrEmpty(layout.SiteId))
        {
            Directory.CreateDirectory(layout.SiteRootDirectory);
            Directory.CreateDirectory(Path.Combine(layout.SiteRootDirectory, "app"));
        }
    }

    public static void EnsureSiteBundle(LauncherPaths paths, LauncherLayout layout)
    {
        if (string.IsNullOrEmpty(layout.SiteId))
        {
            return;
        }

        var siteClientJar = Path.Combine(layout.AppDirectory, "client.jar");
        var siteYogDirectory = Path.Combine(layout.AppDirectory, "yog");
        if (File.Exists(siteClientJar) && Directory.Exists(siteYogDirectory))
        {
            return;
        }

        if (!Directory.Exists(paths.TemplateAppDirectory))
        {
            return;
        }

        CopyDirectory(paths.TemplateAppDirectory, layout.AppDirectory);
    }

    private static void CopyDirectory(string sourcePath, string targetPath)
    {
        Directory.CreateDirectory(targetPath);

        foreach (var filePath in Directory.EnumerateFiles(sourcePath))
        {
            var fileName = Path.GetFileName(filePath);
            File.Copy(filePath, Path.Combine(targetPath, fileName), overwrite: true);
        }

        foreach (var directoryPath in Directory.EnumerateDirectories(sourcePath))
        {
            var directoryName = Path.GetFileName(directoryPath);
            CopyDirectory(directoryPath, Path.Combine(targetPath, directoryName));
        }
    }
}
