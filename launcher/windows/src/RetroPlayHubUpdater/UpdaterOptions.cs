namespace RetroPlayHubUpdater;

internal sealed record UpdaterOptions(
    string Mode,
    string? ManifestUrl,
    string? PackageUrl,
    string? InstallRoot,
    bool Quiet)
{
    public static UpdaterOptions Parse(string[] args)
    {
        var mode = "check";
        string? manifestUrl = null;
        string? packageUrl = null;
        string? installRoot = null;
        var quiet = false;

        for (var i = 0; i < args.Length; i++)
        {
            var name = args[i];
            if (!name.StartsWith("--", StringComparison.Ordinal))
            {
                continue;
            }

            string? ReadValue()
            {
                if (i + 1 >= args.Length)
                {
                    return null;
                }

                i++;
                return args[i];
            }

            switch (name.ToLowerInvariant())
            {
                case "--mode":
                    mode = ReadValue() ?? mode;
                    break;
                case "--manifest":
                    manifestUrl = ReadValue();
                    break;
                case "--package":
                    packageUrl = ReadValue();
                    break;
                case "--install-root":
                    installRoot = ReadValue();
                    break;
                case "--quiet":
                    quiet = true;
                    break;
            }
        }

        return new UpdaterOptions(mode, manifestUrl, packageUrl, installRoot, quiet);
    }
}
