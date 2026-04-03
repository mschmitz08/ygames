namespace RetroPlayHubLauncher;

internal sealed record AppletDescriptor(
    string AppletCode,
    string DefaultPort,
    string IntlCode,
    string DictionaryPath,
    string Room,
    string RoomLabel,
    string PageTitle,
    IReadOnlyDictionary<string, string> Parameters)
{
    public static AppletDescriptor Create(LaunchOptions options, LauncherLayout layout)
    {
        var intlCode = options.IntlCode;
        var room = options.Room;
        string fallbackRoomLabel;
        string appletCode;
        string defaultPort;
        string dictionaryPath;
        string pageTitle;
        var parameters = new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);

        if (options.Game.Equals("checkers", StringComparison.OrdinalIgnoreCase))
        {
            appletCode = "y.k.YahooCheckers";
            defaultPort = "11999";
            fallbackRoomLabel = "Badger Bridge";
            pageTitle = LauncherBranding.CheckersTitle;
            dictionaryPath = Path.Combine(layout.AppDirectory, "yog", "y", "k", $"{intlCode}-t4.ldict");
            if (!File.Exists(dictionaryPath))
            {
                intlCode = "us";
                dictionaryPath = Path.Combine(layout.AppDirectory, "yog", "y", "k", "us-t4.ldict");
            }

            if (string.IsNullOrWhiteSpace(room))
            {
                room = "badger_bridge";
            }

            parameters["game"] = "checkers";
        }
        else
        {
            appletCode = "y.po.YahooPool";
            defaultPort = "11998";
            fallbackRoomLabel = "Corner Pocket";
            pageTitle = LauncherBranding.PoolTitle;
            dictionaryPath = Path.Combine(layout.AppDirectory, "yog", "y", "po", $"{intlCode}-ti.ldict");
            if (!File.Exists(dictionaryPath))
            {
                intlCode = "us";
                dictionaryPath = Path.Combine(layout.AppDirectory, "yog", "y", "po", "us-ti.ldict");
            }

            if (string.IsNullOrWhiteSpace(room))
            {
                room = "corner_pocket";
            }

            parameters["path"] = "ny/servlet/YahooPoolServlet";
            parameters["update"] = "1";
        }

        var roomLabel = RoomLabelFormatter.Format(room, fallbackRoomLabel);
        parameters["label"] = roomLabel;
        parameters["page_title"] = pageTitle;

        return new AppletDescriptor(
            appletCode,
            defaultPort,
            intlCode,
            dictionaryPath,
            room,
            roomLabel,
            pageTitle,
            parameters);
    }
}
