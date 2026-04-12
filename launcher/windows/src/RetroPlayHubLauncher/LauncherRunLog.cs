using System.Text;

namespace RetroPlayHubLauncher;

internal static class LauncherRunLog
{
    public static string Write(LauncherPaths paths, string logText)
    {
        var logsDirectory = Path.Combine(paths.StableInstallDirectory, "logs");
        Directory.CreateDirectory(logsDirectory);

        var logPath = Path.Combine(logsDirectory, "launcher-last-run.log");
        var builder = new StringBuilder();
        builder.AppendLine($"RetroPlayHub Launcher Run Log - {DateTime.Now:yyyy-MM-dd HH:mm:ss}");
        builder.AppendLine();
        builder.Append(logText ?? string.Empty);

        File.WriteAllText(builder.Length == 0 ? logPath : logPath, builder.ToString(), Encoding.UTF8);
        return logPath;
    }
}
