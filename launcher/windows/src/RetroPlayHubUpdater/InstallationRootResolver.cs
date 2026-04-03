namespace RetroPlayHubUpdater;

internal static class InstallationRootResolver
{
    public static string Resolve()
    {
        var current = new DirectoryInfo(AppContext.BaseDirectory);
        while (current is not null)
        {
            var repoPackageRoot = Path.Combine(current.FullName, "ny", "downloads", "ygames_launcher_windows");
            if (LooksLikeLauncherBundleRoot(repoPackageRoot))
            {
                return repoPackageRoot;
            }

            current = current.Parent;
        }

        current = new DirectoryInfo(AppContext.BaseDirectory);
        while (current is not null)
        {
            var installedLikeRoot = current.FullName.TrimEnd(Path.DirectorySeparatorChar);
            if (LooksLikeLauncherBundleRoot(installedLikeRoot))
            {
                return installedLikeRoot;
            }

            current = current.Parent;
        }

        return AppContext.BaseDirectory.TrimEnd(Path.DirectorySeparatorChar);
    }

    public static bool LooksLikeLauncherBundleRoot(string path)
    {
        if (string.IsNullOrWhiteSpace(path) || !Directory.Exists(path))
        {
            return false;
        }

        var clientJarPath = Path.Combine(path, "app", "newyahoo", "client.jar");
        var yogDirectoryPath = Path.Combine(path, "app", "newyahoo", "yog");
        return File.Exists(clientJarPath) && Directory.Exists(yogDirectoryPath);
    }
}
