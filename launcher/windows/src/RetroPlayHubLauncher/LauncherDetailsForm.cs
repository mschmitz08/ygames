using System.Drawing;
using System.Windows.Forms;

namespace RetroPlayHubLauncher;

internal sealed class LauncherDetailsForm : Form
{
    public LauncherDetailsForm(string headline, string body, string details, string logPath, MessageBoxIcon icon)
    {
        Text = $"{LauncherBranding.ProductName} - {headline}";
        StartPosition = FormStartPosition.CenterScreen;
        FormBorderStyle = FormBorderStyle.FixedDialog;
        MaximizeBox = false;
        MinimizeBox = false;
        ShowInTaskbar = true;
        TopMost = true;
        ClientSize = new Size(720, 520);
        BackColor = Color.FromArgb(18, 24, 38);
        ForeColor = Color.FromArgb(245, 234, 213);
        Font = new Font("Segoe UI", 9F, FontStyle.Regular, GraphicsUnit.Point);

        var headerLabel = new Label
        {
            AutoSize = false,
            Bounds = new Rectangle(24, 18, 672, 34),
            Font = new Font("Segoe UI", 18F, FontStyle.Bold, GraphicsUnit.Point),
            ForeColor = Color.FromArgb(255, 247, 232),
            Text = headline
        };

        var bodyLabel = new Label
        {
            AutoSize = false,
            Bounds = new Rectangle(24, 64, 672, 56),
            Font = new Font("Segoe UI", 10F, FontStyle.Regular, GraphicsUnit.Point),
            ForeColor = Color.FromArgb(223, 211, 190),
            Text = body
        };

        var logPathLabel = new Label
        {
            AutoSize = false,
            Bounds = new Rectangle(24, 126, 672, 38),
            Font = new Font("Segoe UI", 8.5F, FontStyle.Regular, GraphicsUnit.Point),
            ForeColor = Color.FromArgb(244, 211, 156),
            Text = string.IsNullOrWhiteSpace(logPath)
                ? "Run log path unavailable."
                : $"Run log saved to:{Environment.NewLine}{logPath}"
        };

        var detailsLabel = new Label
        {
            AutoSize = false,
            Bounds = new Rectangle(24, 176, 672, 20),
            Font = new Font("Segoe UI Semibold", 9F, FontStyle.Bold, GraphicsUnit.Point),
            ForeColor = Color.FromArgb(244, 211, 156),
            Text = "Details"
        };

        var detailsTextBox = new TextBox
        {
            Bounds = new Rectangle(24, 202, 672, 252),
            BackColor = Color.FromArgb(11, 16, 27),
            ForeColor = Color.FromArgb(231, 225, 214),
            BorderStyle = BorderStyle.FixedSingle,
            Multiline = true,
            ReadOnly = true,
            ScrollBars = ScrollBars.Vertical,
            Font = new Font("Consolas", 8.5F, FontStyle.Regular, GraphicsUnit.Point),
            WordWrap = true,
            Text = details ?? string.Empty
        };

        var okButton = new Button
        {
            Text = "OK",
            DialogResult = DialogResult.OK,
            Bounds = new Rectangle(606, 470, 90, 30)
        };

        var copyButton = new Button
        {
            Text = "Copy Details",
            Bounds = new Rectangle(494, 470, 104, 30)
        };
        copyButton.Click += (_, _) =>
        {
            try
            {
                Clipboard.SetText(detailsTextBox.Text);
            }
            catch
            {
                // Clipboard failures should not block the dialog.
            }
        };

        AcceptButton = okButton;
        CancelButton = okButton;

        Controls.Add(headerLabel);
        Controls.Add(bodyLabel);
        Controls.Add(logPathLabel);
        Controls.Add(detailsLabel);
        Controls.Add(detailsTextBox);
        Controls.Add(copyButton);
        Controls.Add(okButton);

        switch (icon)
        {
            case MessageBoxIcon.Error:
                headerLabel.ForeColor = Color.FromArgb(255, 215, 215);
                break;
            case MessageBoxIcon.Information:
                headerLabel.ForeColor = Color.FromArgb(221, 238, 255);
                break;
        }
    }
}
