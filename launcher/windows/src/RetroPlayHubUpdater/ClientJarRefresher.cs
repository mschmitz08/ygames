using System.Net.Http;

namespace RetroPlayHubUpdater;

internal static class ClientJarRefresher
{
    public static bool TryRefresh(string manifestUrl, string installRoot, string expectedHash, out string message)
    {
        message = string.Empty;

        if (string.IsNullOrWhiteSpace(manifestUrl))
        {
            message = "Manifest URL is missing.";
            return false;
        }

        var clientJarUrl = BuildBundleUrl(manifestUrl, "app/newyahoo/client.jar");
        if (string.IsNullOrWhiteSpace(clientJarUrl))
        {
            message = "Could not build the client.jar download URL.";
            return false;
        }

        var appDirectory = Path.Combine(installRoot, "app", "newyahoo");
        Directory.CreateDirectory(appDirectory);

        var tempJarPath = Path.Combine(appDirectory, "client.jar.download");
        var targetJarPath = Path.Combine(appDirectory, "client.jar");

        SafeDelete(tempJarPath);

        try
        {
            using var httpClient = new HttpClient();
            using var response = httpClient.GetAsync(clientJarUrl).GetAwaiter().GetResult();
            response.EnsureSuccessStatusCode();

            using var responseStream = response.Content.ReadAsStreamAsync().GetAwaiter().GetResult();
            using var outputStream = File.Create(tempJarPath);
            responseStream.CopyTo(outputStream);
        }
        catch (Exception ex)
        {
            SafeDelete(tempJarPath);
            message = $"Failed to download client.jar: {ex.Message}";
            return false;
        }

        var downloadedHash = HashUtilities.ComputeSha256(tempJarPath);
        if (string.IsNullOrWhiteSpace(downloadedHash))
        {
            SafeDelete(tempJarPath);
            message = "Downloaded client.jar hash could not be computed.";
            return false;
        }

        if (!string.IsNullOrWhiteSpace(expectedHash) &&
            !string.Equals(downloadedHash, expectedHash, StringComparison.OrdinalIgnoreCase))
        {
            SafeDelete(tempJarPath);
            message = $"Downloaded client.jar hash mismatch. Expected {expectedHash}, got {downloadedHash}.";
            return false;
        }

        try
        {
            if (File.Exists(targetJarPath))
            {
                File.Delete(targetJarPath);
            }

            File.Move(tempJarPath, targetJarPath);
            RefreshSiteClientJarCopies(installRoot, targetJarPath);
        }
        catch (Exception ex)
        {
            SafeDelete(tempJarPath);
            message = $"Failed to replace client.jar: {ex.Message}";
            return false;
        }

        message = $"Refreshed client.jar from {clientJarUrl}";
        return true;
    }

    private static string BuildBundleUrl(string manifestUrl, string relativePath)
    {
        return LauncherBundleUrls.BuildBundleUrl(manifestUrl, relativePath);
    }

    private static void SafeDelete(string filePath)
    {
        try
        {
            if (File.Exists(filePath))
            {
                File.Delete(filePath);
            }
        }
        catch
        {
            // Best-effort cleanup only.
        }
    }

    private static void RefreshSiteClientJarCopies(string installRoot, string sourceJarPath)
    {
        var sitesRoot = Path.Combine(installRoot, "sites");
        if (!Directory.Exists(sitesRoot))
        {
            return;
        }

        foreach (var siteRoot in Directory.EnumerateDirectories(sitesRoot))
        {
            var targetDirectory = Path.Combine(siteRoot, "app", "newyahoo");
            if (!Directory.Exists(targetDirectory))
            {
                continue;
            }

            Directory.CreateDirectory(targetDirectory);
            File.Copy(sourceJarPath, Path.Combine(targetDirectory, "client.jar"), overwrite: true);
        }
    }
}
