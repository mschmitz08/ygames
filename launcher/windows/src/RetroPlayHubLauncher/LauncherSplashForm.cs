using System.Drawing;
using System.Windows.Forms;

namespace RetroPlayHubLauncher;

internal sealed class LauncherSplashForm : Form
{
    private readonly Label brandLabel;
    private readonly Label headlineLabel;
    private readonly Label bodyLabel;
    private readonly ProgressBar progressBar;

    public LauncherSplashForm()
    {
        Text = LauncherBranding.ProductName;
        StartPosition = FormStartPosition.CenterScreen;
        FormBorderStyle = FormBorderStyle.FixedDialog;
        MaximizeBox = false;
        MinimizeBox = false;
        ShowInTaskbar = true;
        TopMost = true;
        ClientSize = new Size(520, 220);
        BackColor = Color.FromArgb(18, 24, 38);
        ForeColor = Color.FromArgb(245, 234, 213);
        Font = new Font("Segoe UI", 9F, FontStyle.Regular, GraphicsUnit.Point);

        brandLabel = new Label
        {
            AutoSize = false,
            Bounds = new Rectangle(24, 18, 472, 24),
            Font = new Font("Segoe UI Semibold", 10F, FontStyle.Bold, GraphicsUnit.Point),
            ForeColor = Color.FromArgb(244, 211, 156),
            Text = "RETROPLAYHUB"
        };

        headlineLabel = new Label
        {
            AutoSize = false,
            Bounds = new Rectangle(24, 54, 472, 34),
            Font = new Font("Segoe UI", 18F, FontStyle.Bold, GraphicsUnit.Point),
            ForeColor = Color.FromArgb(255, 247, 232),
            Text = "Preparing your game..."
        };

        bodyLabel = new Label
        {
            AutoSize = false,
            Bounds = new Rectangle(24, 100, 472, 58),
            Font = new Font("Segoe UI", 10F, FontStyle.Regular, GraphicsUnit.Point),
            ForeColor = Color.FromArgb(223, 211, 190),
            Text = "Checking launcher files, preparing applet settings, and getting the game window ready."
        };

        progressBar = new ProgressBar
        {
            Bounds = new Rectangle(24, 174, 472, 16),
            Style = ProgressBarStyle.Marquee,
            MarqueeAnimationSpeed = 24
        };

        Controls.Add(brandLabel);
        Controls.Add(headlineLabel);
        Controls.Add(bodyLabel);
        Controls.Add(progressBar);
    }

    public void SetStatus(string headline, string body)
    {
        headlineLabel.Text = headline;
        bodyLabel.Text = body;
        Refresh();
    }
}
