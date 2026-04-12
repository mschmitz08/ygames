using System.Drawing;
using System.Windows.Forms;

namespace RetroPlayHubLauncher;

internal sealed class LauncherSplashForm : Form
{
    private readonly Label brandLabel;
    private readonly Label headlineLabel;
    private readonly Label bodyLabel;
    private readonly Label operationsLabel;
    private readonly ProgressBar progressBar;
    private readonly TextBox operationsTextBox;

    public LauncherSplashForm()
    {
        Text = LauncherBranding.ProductName;
        StartPosition = FormStartPosition.CenterScreen;
        FormBorderStyle = FormBorderStyle.FixedDialog;
        MaximizeBox = false;
        MinimizeBox = false;
        ShowInTaskbar = true;
        TopMost = true;
        ClientSize = new Size(640, 420);
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
            Bounds = new Rectangle(24, 100, 592, 44),
            Font = new Font("Segoe UI", 10F, FontStyle.Regular, GraphicsUnit.Point),
            ForeColor = Color.FromArgb(223, 211, 190),
            Text = "Checking launcher files, preparing applet settings, and getting the game window ready."
        };

        operationsLabel = new Label
        {
            AutoSize = false,
            Bounds = new Rectangle(24, 160, 592, 20),
            Font = new Font("Segoe UI Semibold", 9F, FontStyle.Bold, GraphicsUnit.Point),
            ForeColor = Color.FromArgb(244, 211, 156),
            Text = "Operations"
        };

        operationsTextBox = new TextBox
        {
            Bounds = new Rectangle(24, 186, 592, 174),
            BackColor = Color.FromArgb(11, 16, 27),
            ForeColor = Color.FromArgb(231, 225, 214),
            BorderStyle = BorderStyle.FixedSingle,
            Multiline = true,
            ReadOnly = true,
            ScrollBars = ScrollBars.Vertical,
            Font = new Font("Consolas", 8.5F, FontStyle.Regular, GraphicsUnit.Point),
            WordWrap = true
        };

        progressBar = new ProgressBar
        {
            Bounds = new Rectangle(24, 378, 592, 16),
            Style = ProgressBarStyle.Marquee,
            MarqueeAnimationSpeed = 24
        };

        Controls.Add(brandLabel);
        Controls.Add(headlineLabel);
        Controls.Add(bodyLabel);
        Controls.Add(operationsLabel);
        Controls.Add(operationsTextBox);
        Controls.Add(progressBar);
    }

    public void SetStatus(string headline, string body)
    {
        headlineLabel.Text = headline;
        bodyLabel.Text = body;
        Refresh();
    }

    public void AppendLog(string line)
    {
        if (IsDisposed)
        {
            return;
        }

        if (InvokeRequired)
        {
            try
            {
                BeginInvoke(new Action<string>(AppendLog), line);
            }
            catch
            {
                // Ignore late updates after the window is closing.
            }

            return;
        }

        var prefix = $"[{DateTime.Now:HH:mm:ss}] ";
        operationsTextBox.AppendText(
            string.IsNullOrWhiteSpace(line)
                ? Environment.NewLine
                : prefix + line + Environment.NewLine);
        operationsTextBox.SelectionStart = operationsTextBox.TextLength;
        operationsTextBox.ScrollToCaret();
        Refresh();
    }

    public string GetLogText() => operationsTextBox.Text;
}
