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

        if (!Directory.Exists(paths.TemplateAppDirectory))
        {
            return;
        }

        if (!SiteBundleNeedsRefresh(paths, layout))
        {
            return;
        }

        MirrorDirectory(paths.TemplateAppDirectory, layout.AppDirectory);
    }

    private static bool SiteBundleNeedsRefresh(LauncherPaths paths, LauncherLayout layout)
    {
        var templateClientJar = Path.Combine(paths.TemplateAppDirectory, "client.jar");
        var siteClientJar = Path.Combine(layout.AppDirectory, "client.jar");
        if (!File.Exists(templateClientJar))
        {
            return false;
        }

        if (!File.Exists(siteClientJar))
        {
            return true;
        }

        var templateHash = ComputeSha256(templateClientJar);
        var siteHash = ComputeSha256(siteClientJar);

        if (string.IsNullOrWhiteSpace(templateHash) || string.IsNullOrWhiteSpace(siteHash))
        {
            return true;
        }

        return !string.Equals(templateHash, siteHash, StringComparison.OrdinalIgnoreCase);
    }

    private static string ComputeSha256(string filePath)
    {
        using var stream = File.OpenRead(filePath);
        using var sha256 = System.Security.Cryptography.SHA256.Create();
        var hash = sha256.ComputeHash(stream);
        return BitConverter.ToString(hash).Replace("-", string.Empty);
    }

    private static void MirrorDirectory(string sourcePath, string targetPath)
    {
        Directory.CreateDirectory(targetPath);

        foreach (var targetFilePath in Directory.EnumerateFiles(targetPath))
        {
            var fileName = Path.GetFileName(targetFilePath);
            var sourceFilePath = Path.Combine(sourcePath, fileName);
            if (!File.Exists(sourceFilePath))
            {
                File.Delete(targetFilePath);
            }
        }

        foreach (var targetDirectoryPath in Directory.EnumerateDirectories(targetPath))
        {
            var directoryName = Path.GetFileName(targetDirectoryPath);
            var sourceDirectoryPath = Path.Combine(sourcePath, directoryName);
            if (!Directory.Exists(sourceDirectoryPath))
            {
                Directory.Delete(targetDirectoryPath, recursive: true);
            }
        }

        foreach (var filePath in Directory.EnumerateFiles(sourcePath))
        {
            var fileName = Path.GetFileName(filePath);
            File.Copy(filePath, Path.Combine(targetPath, fileName), overwrite: true);
        }

        foreach (var directoryPath in Directory.EnumerateDirectories(sourcePath))
        {
            var directoryName = Path.GetFileName(directoryPath);
            MirrorDirectory(directoryPath, Path.Combine(targetPath, directoryName));
        }
    }
}
