using System.Text;

namespace RetroPlayHubLauncher;

internal static class LaunchArtifactWriter
{
    private static readonly UTF8Encoding Utf8WithoutBom = new(encoderShouldEmitUTF8Identifier: false);

    public static string WritePolicyFile(LauncherLayout layout)
    {
        Directory.CreateDirectory(layout.LaunchDirectory);
        var policyPath = Path.Combine(layout.LaunchDirectory, "launch.policy");
        File.WriteAllLines(
            policyPath,
            new[]
            {
                "grant {",
                "    permission java.security.AllPermission;",
                "};"
            });
        return policyPath;
    }

    public static string WriteAppletHtml(
        LauncherLayout layout,
        LaunchOptions options,
        LauncherSettings settings,
        AppletDescriptor descriptor)
    {
        Directory.CreateDirectory(layout.LaunchDirectory);
        var htmlPath = Path.Combine(
            layout.LaunchDirectory,
            $"{FileNameUtilities.CleanFileName($"{options.Game}_{descriptor.Room}")}.html");

        var port = string.IsNullOrWhiteSpace(options.Port) ? descriptor.DefaultPort : options.Port;
        var loginUrl = BuildWebUrl(options.WebBase, "/applet_login.jsp");
        var registerUrl = BuildWebUrl(options.WebBase, "/applet_register.jsp");
        var changePasswordUrl = BuildWebUrl(options.WebBase, "/applet_change_password.jsp");

        var builder = new StringBuilder();
        builder.AppendLine("<html>");
        builder.AppendLine("  <head>");
        builder.AppendLine("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        builder.AppendLine($"    <title>{HtmlEscape(descriptor.PageTitle)} - Room: {HtmlEscape(descriptor.RoomLabel)}</title>");
        builder.AppendLine("  </head>");
        builder.AppendLine("  <body>");
        builder.AppendLine("    <applet");
        builder.AppendLine($"      code=\"{HtmlEscape(descriptor.AppletCode)}\"");
        builder.AppendLine("      archive=\"client.jar\"");
        builder.AppendLine($"      codebase=\"{FileUrl(layout.AppDirectory)}\"");
        builder.AppendLine($"      width=\"{HtmlEscape(settings.AppletWidth)}\"");
        builder.AppendLine($"      height=\"{HtmlEscape(settings.AppletHeight)}\">");
        builder.AppendLine($"      <param name=\"host\" value=\"{HtmlEscape(options.Host)}\">");
        builder.AppendLine($"      <param name=\"port\" value=\"{HtmlEscape(port)}\">");
        builder.AppendLine($"      <param name=\"intl_code\" value=\"{HtmlEscape(descriptor.IntlCode)}\">");
        builder.AppendLine("      <param name=\"uselogin\" value=\"0\">");
        builder.AppendLine("      <param name=\"logsentmessages\" value=\"0\">");
        builder.AppendLine("      <param name=\"logreceivedmessages\" value=\"0\">");
        builder.AppendLine($"      <param name=\"room\" value=\"{HtmlEscape(descriptor.Room)}\">");
        builder.AppendLine($"      <param name=\"yport\" value=\"{HtmlEscape(descriptor.Room)}\">");
        AppendOptionalParam(builder, "account_mode", options.AccountMode);
        AppendOptionalParam(builder, "launcher_version", options.LauncherVersion);
        builder.AppendLine($"      <param name=\"login_url\" value=\"{HtmlEscape(loginUrl)}\">");
        builder.AppendLine($"      <param name=\"register_url\" value=\"{HtmlEscape(registerUrl)}\">");
        builder.AppendLine($"      <param name=\"change_password_url\" value=\"{HtmlEscape(changePasswordUrl)}\">");
        builder.AppendLine($"      <param name=\"ldict_url\" value=\"{HtmlEscape(descriptor.DictionaryPath)}\">");

        foreach (var parameter in descriptor.Parameters)
        {
            builder.AppendLine($"      <param name=\"{HtmlEscape(parameter.Key)}\" value=\"{HtmlEscape(parameter.Value)}\">");
        }

        builder.AppendLine("    </applet>");
        builder.AppendLine("  </body>");
        builder.AppendLine("</html>");

        File.WriteAllText(htmlPath, builder.ToString(), Utf8WithoutBom);
        return htmlPath;
    }

    private static void AppendOptionalParam(StringBuilder builder, string name, string? value)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return;
        }

        builder.AppendLine($"      <param name=\"{HtmlEscape(name)}\" value=\"{HtmlEscape(value)}\">");
    }

    private static string BuildWebUrl(string webBase, string relativePath)
    {
        var normalizedBase = webBase.TrimEnd('/');
        if (!relativePath.StartsWith('/'))
        {
            relativePath = "/" + relativePath;
        }

        return normalizedBase + relativePath;
    }

    private static string FileUrl(string path) => "file:///" + path.Replace("\\", "/");

    private static string HtmlEscape(string value) =>
        value
            .Replace("&", "&amp;", StringComparison.Ordinal)
            .Replace("\"", "&quot;", StringComparison.Ordinal)
            .Replace("<", "&lt;", StringComparison.Ordinal)
            .Replace(">", "&gt;", StringComparison.Ordinal);
}
