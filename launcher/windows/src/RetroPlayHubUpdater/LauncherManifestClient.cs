using System.Net.Http;

namespace RetroPlayHubUpdater;

internal static class LauncherManifestClient
{
    private static readonly string[] DefaultManifestEntries =
    {
        "launcher_version.txt",
        "install_launcher.bat",
        "uninstall_launcher.bat",
        "README.md",
        "runtime/README.md"
    };

    public static IReadOnlyList<string> FetchOrDefault(string manifestUrl)
    {
        var launcherManifestUrl = LauncherBundleUrls.BuildBundleUrl(manifestUrl, "launcher_manifest.txt");
        if (string.IsNullOrWhiteSpace(launcherManifestUrl))
        {
            return DefaultManifestEntries;
        }

        try
        {
            using var httpClient = new HttpClient();
            using var response = httpClient.GetAsync(launcherManifestUrl).GetAwaiter().GetResult();
            response.EnsureSuccessStatusCode();
            var body = response.Content.ReadAsStringAsync().GetAwaiter().GetResult();
            var entries = Parse(body);
            return entries.Count == 0 ? DefaultManifestEntries : entries;
        }
        catch
        {
            return DefaultManifestEntries;
        }
    }

    private static IReadOnlyList<string> Parse(string body)
    {
        var entries = new List<string>();
        foreach (var rawLine in body.Replace("\r\n", "\n", StringComparison.Ordinal).Split('\n'))
        {
            var line = rawLine.Trim();
            if (string.IsNullOrWhiteSpace(line) || line.StartsWith('#'))
            {
                continue;
            }

            entries.Add(line.Replace('/', Path.DirectorySeparatorChar));
        }

        return entries;
    }
}
