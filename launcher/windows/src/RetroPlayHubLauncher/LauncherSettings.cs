namespace RetroPlayHubLauncher;

internal sealed record LauncherSettings(string AppletWidth, string AppletHeight)
{
    public static LauncherSettings Load(LauncherLayout layout, LaunchOptions options)
    {
        var width = options.AppletWidth;
        var height = options.AppletHeight;
        var settingsPath = Path.Combine(layout.SiteRootDirectory, "launcher_settings.ini");

        if (!File.Exists(settingsPath))
        {
            return new LauncherSettings(width, height);
        }

        foreach (var rawLine in File.ReadLines(settingsPath))
        {
            var line = rawLine.Trim();
            if (string.IsNullOrWhiteSpace(line) || line.StartsWith("#") || line.StartsWith(";"))
            {
                continue;
            }

            var eqPos = line.IndexOf('=');
            if (eqPos <= 0)
            {
                continue;
            }

            var key = line.Substring(0, eqPos).Trim().ToLowerInvariant();
            var value = line.Substring(eqPos + 1).Trim();

            if (key == "width" && !options.WidthSpecified && int.TryParse(value, out _))
            {
                width = value;
            }
            else if (key == "height" && !options.HeightSpecified && int.TryParse(value, out _))
            {
                height = value;
            }
        }

        return new LauncherSettings(width, height);
    }

    public static void Save(LauncherLayout layout, LauncherSettings settings)
    {
        Directory.CreateDirectory(layout.SiteRootDirectory);
        var settingsPath = Path.Combine(layout.SiteRootDirectory, "launcher_settings.ini");
        File.WriteAllLines(
            settingsPath,
            new[]
            {
                "# RetroPlayHub Launcher window size",
                $"width={settings.AppletWidth}",
                $"height={settings.AppletHeight}"
            });
    }
}
