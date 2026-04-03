using System.Net.Http;

namespace RetroPlayHubUpdater;

internal static class LauncherStateManifestClient
{
    public static LauncherStateManifest Fetch(string manifestUrl)
    {
        using var httpClient = new HttpClient();
        using var response = httpClient.GetAsync(manifestUrl).GetAwaiter().GetResult();
        response.EnsureSuccessStatusCode();

        var body = response.Content.ReadAsStringAsync().GetAwaiter().GetResult();
        return Parse(body);
    }

    public static LauncherStateManifest Parse(string body)
    {
        var launcherVersion = string.Empty;
        var clientHash = string.Empty;

        foreach (var rawLine in body.Replace("\r\n", "\n", StringComparison.Ordinal).Split('\n'))
        {
            var line = rawLine.Trim();
            if (string.IsNullOrWhiteSpace(line))
            {
                continue;
            }

            var equalsIndex = line.IndexOf('=');
            if (equalsIndex <= 0)
            {
                continue;
            }

            var key = line[..equalsIndex].Trim().ToLowerInvariant();
            var value = line[(equalsIndex + 1)..].Trim();

            if (key == "launcher_version" && value.Length > 0)
            {
                launcherVersion = value;
            }
            else if (key == "client_hash" && value.Length > 0)
            {
                clientHash = value;
            }
        }

        return new LauncherStateManifest(launcherVersion, clientHash);
    }
}
