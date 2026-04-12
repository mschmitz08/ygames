using System.Diagnostics;
using System.Net.Http;

namespace RetroPlayHubUpdater;

internal static class MsiPackageInstaller
{
    public static bool TryDownloadAndLaunch(
        string manifestUrl,
        string requiredVersion,
        string? explicitPackageUrl,
        out string message)
    {
        message = string.Empty;

        foreach (var packageUrl in GetCandidateUrls(manifestUrl, requiredVersion, explicitPackageUrl))
        {
            if (string.IsNullOrWhiteSpace(packageUrl))
            {
                continue;
            }

            var tempMsiPath = Path.Combine(
                Path.GetTempPath(),
                $"RetroPlayHubLauncher_{requiredVersion}_{Guid.NewGuid():N}.msi");
            var tempLogPath = Path.Combine(
                Path.GetTempPath(),
                $"RetroPlayHubLauncher_{requiredVersion}_{Guid.NewGuid():N}.log");

            try
            {
                using var httpClient = new HttpClient();
                using var response = httpClient.GetAsync(packageUrl).GetAwaiter().GetResult();
                response.EnsureSuccessStatusCode();
                using var responseStream = response.Content.ReadAsStreamAsync().GetAwaiter().GetResult();
                using var outputStream = File.Create(tempMsiPath);
                responseStream.CopyTo(outputStream);
            }
            catch
            {
                SafeDelete(tempMsiPath);
                continue;
            }

            try
            {
                var startInfo = new ProcessStartInfo
                {
                    FileName = "msiexec.exe",
                    UseShellExecute = true,
                    Arguments = $"/i \"{tempMsiPath}\" /passive /norestart /L*V \"{tempLogPath}\""
                };

                Process.Start(startInfo);
                message =
                    $"Launched MSI installer from {packageUrl}{Environment.NewLine}" +
                    $"Downloaded package: {tempMsiPath}{Environment.NewLine}" +
                    $"Installer log: {tempLogPath}";
                return true;
            }
            catch (Exception ex)
            {
                SafeDelete(tempMsiPath);
                message = $"Downloaded MSI but failed to launch msiexec: {ex.Message}";
                return false;
            }
        }

        message = "Could not download a launcher MSI from any known package URL.";
        return false;
    }

    private static IEnumerable<string> GetCandidateUrls(string manifestUrl, string requiredVersion, string? explicitPackageUrl)
    {
        if (!string.IsNullOrWhiteSpace(explicitPackageUrl))
        {
            yield return explicitPackageUrl;
        }

        yield return LauncherBundleUrls.BuildDownloadUrl(manifestUrl, $"RetroPlayHubLauncher_{requiredVersion}.msi");
        yield return LauncherBundleUrls.BuildDownloadUrl(manifestUrl, "RetroPlayHubLauncher.msi");
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
}
