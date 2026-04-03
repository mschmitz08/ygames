using System.Diagnostics;

namespace RetroPlayHubLauncher;

internal static class AppletViewerLocator
{
    private static readonly string[] KnownSearchRoots =
    {
        @"C:\Program Files\Java",
        @"C:\Program Files (x86)\Java",
        @"C:\Program Files\Eclipse Adoptium",
        @"C:\Program Files\AdoptOpenJDK",
        @"C:\Program Files\BellSoft"
    };

    public static string? Find(LauncherPaths paths)
    {
        var bundledPath = Path.Combine(paths.BaseDirectory, "runtime", "bin", "appletviewer.exe");
        if (File.Exists(bundledPath))
        {
            return bundledPath;
        }

        var javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
        if (!string.IsNullOrWhiteSpace(javaHome))
        {
            var javaHomeAppletViewer = Path.Combine(javaHome, "bin", "appletviewer.exe");
            if (File.Exists(javaHomeAppletViewer))
            {
                return javaHomeAppletViewer;
            }
        }

        var discoveredPath = FindInKnownLocations();
        if (!string.IsNullOrWhiteSpace(discoveredPath))
        {
            return discoveredPath;
        }

        return FindOnPath();
    }

    public static string GetDiagnostics(LauncherPaths paths)
    {
        var report = new List<string>
        {
            "RetroPlayHub Launcher diagnostics",
            $"Time: {DateTime.Now}",
            $"Launcher folder: {paths.BaseDirectory}",
            $"Stable install folder: {paths.StableInstallDirectory}",
            string.Empty
        };

        var javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
        report.Add($"JAVA_HOME: {javaHome ?? "(not set)"}");
        if (!string.IsNullOrWhiteSpace(javaHome))
        {
            report.Add($"JAVA_HOME appletviewer: {CheckCandidatePath(Path.Combine(javaHome, "bin", "appletviewer.exe"))}");
        }

        report.Add($"Bundled runtime appletviewer: {CheckCandidatePath(Path.Combine(paths.BaseDirectory, "runtime", "bin", "appletviewer.exe"))}");
        report.Add(string.Empty);
        report.Add("Known install folders:");
        foreach (var root in KnownSearchRoots)
        {
            report.AddRange(DescribeRoot(root));
        }

        report.Add(string.Empty);
        report.Add("PATH lookup (where appletviewer):");
        report.AddRange(DescribeWhereLookup());

        return string.Join(Environment.NewLine, report);
    }

    private static string? FindInKnownLocations()
    {
        foreach (var root in KnownSearchRoots)
        {
            if (!Directory.Exists(root))
            {
                continue;
            }

            foreach (var subDirectory in Directory.EnumerateDirectories(root))
            {
                var candidate = Path.Combine(subDirectory, "bin", "appletviewer.exe");
                if (File.Exists(candidate))
                {
                    return candidate;
                }
            }
        }

        return null;
    }

    private static string? FindOnPath()
    {
        try
        {
            var startInfo = new ProcessStartInfo
            {
                FileName = "cmd.exe",
                Arguments = "/c where appletviewer",
                RedirectStandardOutput = true,
                RedirectStandardError = true,
                UseShellExecute = false,
                CreateNoWindow = true
            };

            using var process = Process.Start(startInfo);
            if (process is null)
            {
                return null;
            }

            while (!process.StandardOutput.EndOfStream)
            {
                var output = process.StandardOutput.ReadLine()?.Trim();
                if (!string.IsNullOrWhiteSpace(output) && File.Exists(output))
                {
                    return output;
                }
            }
        }
        catch
        {
            return null;
        }

        return null;
    }

    private static string CheckCandidatePath(string candidatePath) =>
        File.Exists(candidatePath) ? $"{candidatePath} [FOUND]" : $"{candidatePath} [missing]";

    private static IEnumerable<string> DescribeRoot(string rootPath)
    {
        if (!Directory.Exists(rootPath))
        {
            yield return $"- {rootPath} (missing)";
            yield break;
        }

        yield return $"- {rootPath}";
        foreach (var subDirectory in Directory.EnumerateDirectories(rootPath))
        {
            yield return $"    {CheckCandidatePath(Path.Combine(subDirectory, "bin", "appletviewer.exe"))}";
        }
    }

    private static IEnumerable<string> DescribeWhereLookup()
    {
        var lines = new List<string>();
        try
        {
            var startInfo = new ProcessStartInfo
            {
                FileName = "cmd.exe",
                Arguments = "/c where appletviewer",
                RedirectStandardOutput = true,
                RedirectStandardError = true,
                UseShellExecute = false,
                CreateNoWindow = true
            };

            using var process = Process.Start(startInfo);
            if (process is null)
            {
                lines.Add("Unable to run PATH lookup.");
                return lines;
            }

            var wroteLine = false;
            while (!process.StandardOutput.EndOfStream)
            {
                wroteLine = true;
                lines.Add("    " + process.StandardOutput.ReadLine()?.Trim());
            }

            while (!process.StandardError.EndOfStream)
            {
                wroteLine = true;
                lines.Add("    " + process.StandardError.ReadLine()?.Trim());
            }

            if (!wroteLine)
            {
                lines.Add("    No appletviewer.exe found on PATH");
            }
        }
        catch (Exception ex)
        {
            lines.Add("Unable to run PATH lookup: " + ex.Message);
        }

        return lines;
    }
}
