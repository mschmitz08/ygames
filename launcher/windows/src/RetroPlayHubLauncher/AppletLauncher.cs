using System.Diagnostics;
using System.Text;

namespace RetroPlayHubLauncher;

internal static class AppletLauncher
{
    public static void Launch(string appletViewerPath, string policyPath, string htmlPath, string workingDirectory)
    {
        var startInfo = new ProcessStartInfo
        {
            FileName = appletViewerPath,
            WorkingDirectory = workingDirectory,
            UseShellExecute = true,
            Arguments = BuildArguments(policyPath, htmlPath)
        };

        Process.Start(startInfo);
    }

    private static string BuildArguments(string policyPath, string htmlPath)
    {
        var builder = new StringBuilder();
        builder.Append(Quote($"-J-Djava.security.policy={policyPath}"));
        builder.Append(' ');
        builder.Append("-J-Dsun.java2d.dpiaware=false");
        builder.Append(' ');
        builder.Append(Quote(htmlPath));
        return builder.ToString();
    }

    private static string Quote(string value) => $"\"{value}\"";
}
