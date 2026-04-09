using System.Windows.Forms;

namespace RetroPlayHubLauncher;

internal static class Program
{
    [STAThread]
    private static int Main(string[] args)
    {
        Application.EnableVisualStyles();
        Application.SetCompatibleTextRenderingDefault(false);
        return LauncherApplication.Run(args);
    }
}
