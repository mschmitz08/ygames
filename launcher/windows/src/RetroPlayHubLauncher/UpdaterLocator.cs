namespace RetroPlayHubLauncher;

internal static class UpdaterLocator
{
    public static UpdaterExecutable? Find()
    {
        var sidecarExe = Path.Combine(AppContext.BaseDirectory, "RetroPlayHubUpdater.exe");
        if (File.Exists(sidecarExe))
        {
            return new UpdaterExecutable(sidecarExe, RequiresDotnetHost: false);
        }

        var sidecarDll = Path.Combine(AppContext.BaseDirectory, "RetroPlayHubUpdater.dll");
        if (File.Exists(sidecarDll))
        {
            return new UpdaterExecutable(sidecarDll, RequiresDotnetHost: true);
        }

        var current = new DirectoryInfo(AppContext.BaseDirectory);
        while (current is not null)
        {
            var debugExe = Path.Combine(
                current.FullName,
                "launcher",
                "windows",
                "src",
                "RetroPlayHubUpdater",
                "bin",
                "Debug",
                "net10.0",
                "RetroPlayHubUpdater.exe");
            if (File.Exists(debugExe))
            {
                return new UpdaterExecutable(debugExe, RequiresDotnetHost: false);
            }

            var debugDll = Path.Combine(
                current.FullName,
                "launcher",
                "windows",
                "src",
                "RetroPlayHubUpdater",
                "bin",
                "Debug",
                "net10.0",
                "RetroPlayHubUpdater.dll");
            if (File.Exists(debugDll))
            {
                return new UpdaterExecutable(debugDll, RequiresDotnetHost: true);
            }

            var releaseExe = Path.Combine(
                current.FullName,
                "launcher",
                "windows",
                "src",
                "RetroPlayHubUpdater",
                "bin",
                "Release",
                "net10.0",
                "RetroPlayHubUpdater.exe");
            if (File.Exists(releaseExe))
            {
                return new UpdaterExecutable(releaseExe, RequiresDotnetHost: false);
            }

            var releaseDll = Path.Combine(
                current.FullName,
                "launcher",
                "windows",
                "src",
                "RetroPlayHubUpdater",
                "bin",
                "Release",
                "net10.0",
                "RetroPlayHubUpdater.dll");
            if (File.Exists(releaseDll))
            {
                return new UpdaterExecutable(releaseDll, RequiresDotnetHost: true);
            }

            current = current.Parent;
        }

        return null;
    }
}
