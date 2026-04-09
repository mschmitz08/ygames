namespace RetroPlayHubUpdater;

internal static class LauncherBundleUrls
{
    public static string BuildBundleUrl(string manifestUrl, string relativePath)
    {
        if (!Uri.TryCreate(manifestUrl, UriKind.Absolute, out var manifestUri))
        {
            return string.Empty;
        }

        var bundleRoot = new Uri(manifestUri, "downloads/ygames_launcher_windows/");
        var targetUri = new Uri(bundleRoot, relativePath.Replace("\\", "/"));
        return targetUri.ToString();
    }

    public static string BuildDownloadUrl(string manifestUrl, string relativePath)
    {
        if (!Uri.TryCreate(manifestUrl, UriKind.Absolute, out var manifestUri))
        {
            return string.Empty;
        }

        var downloadsRoot = new Uri(manifestUri, "downloads/");
        var targetUri = new Uri(downloadsRoot, relativePath.Replace("\\", "/"));
        return targetUri.ToString();
    }
}
