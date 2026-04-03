namespace RetroPlayHubLauncher;

internal static class RoomLabelFormatter
{
    public static string Format(string? roomName, string fallback)
    {
        if (string.IsNullOrWhiteSpace(roomName))
        {
            return fallback;
        }

        var buffer = new System.Text.StringBuilder(roomName.Length);
        var upper = true;
        foreach (var c in roomName)
        {
            if (c is '_' or '-')
            {
                buffer.Append(' ');
                upper = true;
                continue;
            }

            if (upper && char.IsLetter(c))
            {
                buffer.Append(char.ToUpperInvariant(c));
                upper = false;
                continue;
            }

            buffer.Append(c);
            upper = c == ' ';
        }

        return buffer.ToString();
    }
}
