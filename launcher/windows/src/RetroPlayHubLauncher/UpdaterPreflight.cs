using System.Diagnostics;
using System.Text;

namespace RetroPlayHubLauncher;

internal static class UpdaterPreflight
{
    public static UpdaterCheckOutcome CheckForUpdates(
        LaunchOptions options,
        LauncherPaths paths,
        Action<string>? log,
        out string details)
    {
        details = string.Empty;

        if (options.SkipSelfUpdate)
        {
            details = "Update check skipped because --skip_self_update was requested.";
            return UpdaterCheckOutcome.UpToDate;
        }

        var updater = UpdaterLocator.Find();
        if (updater is null)
        {
            details = "RetroPlayHubUpdater was not found, so update checking was skipped.";
            return UpdaterCheckOutcome.UpToDate;
        }

        log?.Invoke($"Using updater: {updater.FilePath}");
        var manifestUrl = BuildWebUrl(options.WebBase, "/launcher_state.jsp");
        if (string.IsNullOrWhiteSpace(manifestUrl))
        {
            details = "Update check skipped because no launcher_state.jsp URL could be constructed.";
            return UpdaterCheckOutcome.UpToDate;
        }

        log?.Invoke($"Reading manifest: {manifestUrl}");
        var startInfo = new ProcessStartInfo
        {
            FileName = updater.RequiresDotnetHost ? "dotnet" : updater.FilePath,
            UseShellExecute = false,
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            CreateNoWindow = true,
            Arguments = BuildArguments(updater, manifestUrl, paths.BaseDirectory)
        };

        using var process = Process.Start(startInfo);
        if (process is null)
        {
            details = "Failed to start RetroPlayHubUpdater.";
            return UpdaterCheckOutcome.UpdateCheckFailed;
        }

        var transcript = new List<string>();
        var transcriptLock = new object();

        void CaptureLine(string? line, bool isError)
        {
            if (line is null)
            {
                return;
            }

            var display = isError && !string.IsNullOrWhiteSpace(line)
                ? "STDERR: " + line
                : line;

            lock (transcriptLock)
            {
                transcript.Add(display);
            }

            log?.Invoke(display);
        }

        process.OutputDataReceived += (_, eventArgs) => CaptureLine(eventArgs.Data, false);
        process.ErrorDataReceived += (_, eventArgs) => CaptureLine(eventArgs.Data, true);
        process.BeginOutputReadLine();
        process.BeginErrorReadLine();
        process.WaitForExit();
        process.WaitForExit();

        details = BuildDetails(transcript);

        return process.ExitCode switch
        {
            0 => UpdaterCheckOutcome.UpToDate,
            10 => UpdaterCheckOutcome.LauncherUpdateNeeded,
            11 => UpdaterCheckOutcome.ClientUpdateNeeded,
            12 => UpdaterCheckOutcome.InstallerLaunched,
            _ => UpdaterCheckOutcome.UpdateCheckFailed
        };
    }

    private static string BuildWebUrl(string webBase, string relativePath)
    {
        var normalizedBase = webBase.TrimEnd('/');
        if (!relativePath.StartsWith("/"))
        {
            relativePath = "/" + relativePath;
        }

        return normalizedBase + relativePath;
    }

    private static string BuildArguments(UpdaterExecutable updater, string manifestUrl, string installRoot)
    {
        var parts = new List<string>();
        if (updater.RequiresDotnetHost)
        {
            parts.Add(Quote(updater.FilePath));
        }

        parts.Add("--mode");
        parts.Add("ensure");
        parts.Add("--manifest");
        parts.Add(manifestUrl);
        parts.Add("--install-root");
        parts.Add(installRoot);

        return string.Join(" ", parts.ConvertAll(Quote));
    }

    private static string BuildDetails(List<string> transcript)
    {
        if (transcript.Count == 0)
        {
            return string.Empty;
        }

        var builder = new StringBuilder();
        foreach (var line in transcript)
        {
            builder.AppendLine(line);
        }

        return builder.ToString().TrimEnd();
    }

    private static string Quote(string value)
    {
        return "\"" + value.Replace("\"", "\\\"") + "\"";
    }
}
