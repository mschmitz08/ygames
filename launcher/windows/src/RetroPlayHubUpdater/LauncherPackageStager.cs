using System.Net.Http;

namespace RetroPlayHubUpdater;

internal static class LauncherPackageStager
{
    public static bool TryStage(string manifestUrl, string requiredVersion, out string stageRoot, out string message)
    {
        stageRoot = string.Empty;
        message = string.Empty;

        if (string.IsNullOrWhiteSpace(manifestUrl))
        {
            message = "Manifest URL is missing.";
            return false;
        }

        var entries = LauncherManifestClient.FetchOrDefault(manifestUrl);
        stageRoot = CreateStageRoot();

        try
        {
            Directory.CreateDirectory(stageRoot);

            using var httpClient = new HttpClient();
            foreach (var entry in entries)
            {
                var relativePath = entry.Replace(Path.DirectorySeparatorChar, '/');
                var downloadUrl = LauncherBundleUrls.BuildBundleUrl(manifestUrl, relativePath);
                if (string.IsNullOrWhiteSpace(downloadUrl))
                {
                    message = $"Could not build the download URL for {entry}.";
                    return false;
                }

                var targetPath = Path.Combine(stageRoot, entry);
                var targetDirectory = Path.GetDirectoryName(targetPath);
                if (!string.IsNullOrWhiteSpace(targetDirectory))
                {
                    Directory.CreateDirectory(targetDirectory);
                }

                using var response = httpClient.GetAsync(downloadUrl).GetAwaiter().GetResult();
                response.EnsureSuccessStatusCode();
                using var responseStream = response.Content.ReadAsStreamAsync().GetAwaiter().GetResult();
                using var outputStream = File.Create(targetPath);
                responseStream.CopyTo(outputStream);
            }
        }
        catch (Exception ex)
        {
            message = $"Failed to stage launcher package: {ex.Message}";
            return false;
        }

        var stagedVersion = UpdaterPaths.ReadLauncherVersion(stageRoot);
        if (VersionComparison.Compare(stagedVersion, requiredVersion) < 0)
        {
            message = $"Staged launcher version {stagedVersion} is older than required version {requiredVersion}.";
            return false;
        }

        message = $"Staged launcher package at {stageRoot}";
        return true;
    }

    private static string CreateStageRoot()
    {
        var tempRoot = Path.GetTempPath();
        return Path.Combine(tempRoot, $"RetroPlayHubLauncherUpdate_{DateTime.Now:yyyyMMdd_HHmmss}");
    }
}
