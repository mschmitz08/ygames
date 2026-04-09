namespace RetroPlayHubLauncher;

internal sealed record LaunchOptions(
    string Game,
    string Room,
    string Host,
    string Port,
    string IntlCode,
    string AccountMode,
    string LauncherVersion,
    string WebBase,
    string AppletWidth,
    string AppletHeight,
    bool WidthSpecified,
    bool HeightSpecified,
    bool OpenSettings,
    string ExpectedClientHash,
    string? SiteId,
    bool SkipSelfUpdate)
{
    public static LaunchOptions Parse(string[] args)
    {
        var values = new MutableLaunchOptions();

        if (args.Length > 0 && args[0].StartsWith("nygames://", StringComparison.OrdinalIgnoreCase))
        {
            ParseUri(values, args[0]);
            ParseNamedArgs(values, args.Skip(1).ToArray());
        }
        else
        {
            ParseNamedArgs(values, args);
        }

        return values.ToImmutable();
    }

    private static void ParseNamedArgs(MutableLaunchOptions values, string[] args)
    {
        for (var i = 0; i < args.Length; i++)
        {
            var name = args[i];
            if (!name.StartsWith("--", StringComparison.Ordinal))
            {
                continue;
            }

            string ReadValue()
            {
                if (i + 1 >= args.Length)
                {
                    return string.Empty;
                }

                i++;
                return args[i];
            }

            switch (name.ToLowerInvariant())
            {
                case "--game":
                    values.Game = ReadValue();
                    break;
                case "--room":
                    values.Room = ReadValue();
                    break;
                case "--host":
                    values.Host = ReadValue();
                    break;
                case "--port":
                    values.Port = ReadValue();
                    break;
                case "--intl_code":
                    values.IntlCode = ReadValue();
                    break;
                case "--account_mode":
                    values.AccountMode = ReadValue();
                    break;
                case "--launcher_version":
                    values.LauncherVersion = ReadValue();
                    break;
                case "--webbase":
                    values.WebBase = ReadValue();
                    break;
                case "--width":
                    values.AppletWidth = ReadValue();
                    values.WidthSpecified = true;
                    break;
                case "--height":
                    values.AppletHeight = ReadValue();
                    values.HeightSpecified = true;
                    break;
                case "--settings":
                    values.OpenSettings = true;
                    break;
                case "--expected_client_hash":
                    values.ExpectedClientHash = ReadValue();
                    break;
                case "--site_id":
                    values.SiteId = ReadValue();
                    break;
                case "--skip_self_update":
                    values.SkipSelfUpdate = true;
                    break;
            }
        }
    }

    private static void ParseUri(MutableLaunchOptions values, string uriText)
    {
        if (!Uri.TryCreate(uriText, UriKind.Absolute, out var uri))
        {
            return;
        }

        var query = uri.Query;
        if (string.IsNullOrWhiteSpace(query))
        {
            return;
        }

        foreach (var pair in query.TrimStart('?').Split(new[] { '&' }, StringSplitOptions.RemoveEmptyEntries))
        {
            var split = pair.Split(new[] { '=' }, 2);
            if (split.Length != 2)
            {
                continue;
            }

            var key = Uri.UnescapeDataString(split[0]).ToLowerInvariant();
            var value = Uri.UnescapeDataString(split[1]);

            switch (key)
            {
                case "game":
                    values.Game = value;
                    break;
                case "room":
                    values.Room = value;
                    break;
                case "host":
                    values.Host = value;
                    break;
                case "port":
                    values.Port = value;
                    break;
                case "intl_code":
                    values.IntlCode = value;
                    break;
                case "account_mode":
                    values.AccountMode = value;
                    break;
                case "launcher_version":
                    values.LauncherVersion = value;
                    break;
                case "webbase":
                    values.WebBase = value;
                    break;
                case "width":
                    values.AppletWidth = value;
                    values.WidthSpecified = true;
                    break;
                case "height":
                    values.AppletHeight = value;
                    values.HeightSpecified = true;
                    break;
                case "expected_client_hash":
                    values.ExpectedClientHash = value;
                    break;
                case "site_id":
                    values.SiteId = value;
                    break;
                case "action" when value.Equals("settings", StringComparison.OrdinalIgnoreCase):
                    values.OpenSettings = true;
                    break;
            }
        }
    }

    private sealed class MutableLaunchOptions
    {
        public string Game { get; set; } = "pool";
        public string Room { get; set; } = "corner_pocket";
        public string Host { get; set; } = "127.0.0.1";
        public string Port { get; set; } = "11998";
        public string IntlCode { get; set; } = "us";
        public string AccountMode { get; set; } = string.Empty;
        public string LauncherVersion { get; set; } = string.Empty;
        public string WebBase { get; set; } = "http://127.0.0.1:8080/ny";
        public string AppletWidth { get; set; } = "1400";
        public string AppletHeight { get; set; } = "900";
        public bool WidthSpecified { get; set; }
        public bool HeightSpecified { get; set; }
        public bool OpenSettings { get; set; }
        public string ExpectedClientHash { get; set; } = string.Empty;
        public string? SiteId { get; set; }
        public bool SkipSelfUpdate { get; set; }

        public LaunchOptions ToImmutable() =>
            new(
                Game,
                Room,
                Host,
                Port,
                IntlCode,
                AccountMode,
                LauncherVersion,
                WebBase,
                AppletWidth,
                AppletHeight,
                WidthSpecified,
                HeightSpecified,
                OpenSettings,
                ExpectedClientHash,
                SiteId,
                SkipSelfUpdate);
    }
}
