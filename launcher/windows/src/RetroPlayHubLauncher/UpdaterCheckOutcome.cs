namespace RetroPlayHubLauncher;

internal enum UpdaterCheckOutcome
{
    UpToDate = 0,
    LauncherUpdateNeeded = 10,
    ClientUpdateNeeded = 11,
    InstallerLaunched = 12,
    UpdateCheckFailed = 100
}
