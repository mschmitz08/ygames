namespace RetroPlayHubLauncher;

internal static class FileNameUtilities
{
    public static string CleanFileName(string value)
    {
        var invalid = new[]
        {
            '\\', '/', ':', '*', '?', '"', '<', '>', '|', ' '
        };

        foreach (var character in invalid)
        {
            value = value.Replace(character, '_');
        }

        return value;
    }
}
