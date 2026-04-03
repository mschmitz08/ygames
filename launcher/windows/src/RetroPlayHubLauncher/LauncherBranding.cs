namespace RetroPlayHubLauncher;

internal static class LauncherBranding
{
    public const string ProductName = "RetroPlayHub Launcher";
    public const string PublisherName = "RetroPlayHub";
    public const string PoolTitle = "RetroPlayHub Pool";
    public const string CheckersTitle = "RetroPlayHub Checkers";
    public const string GamesTitle = "RetroPlayHub Games";

    public static string GetGameTitle(string game) =>
        game.Equals("checkers", StringComparison.OrdinalIgnoreCase)
            ? CheckersTitle
            : PoolTitle;
}
