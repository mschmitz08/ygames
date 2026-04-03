namespace RetroPlayHubLauncher;

internal sealed record UpdaterExecutable(
    string FilePath,
    bool RequiresDotnetHost);
