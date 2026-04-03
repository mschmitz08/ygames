using System.Windows.Forms;

namespace RetroPlayHubLauncher;

internal static class Program
{
    [STAThread]
    private static int Main(string[] args)
    {
        ApplicationConfiguration.Initialize();
        return LauncherApplication.Run(args);
    }
}
