namespace RetroPlayHubUpdater;

internal static class VersionComparison
{
    public static int Compare(string? leftValue, string? rightValue)
    {
        var leftParts = (leftValue ?? string.Empty).Split(new[] { '.' }, StringSplitOptions.RemoveEmptyEntries);
        var rightParts = (rightValue ?? string.Empty).Split(new[] { '.' }, StringSplitOptions.RemoveEmptyEntries);
        var maxIndex = Math.Max(leftParts.Length, rightParts.Length);

        for (var i = 0; i < maxIndex; i++)
        {
            var leftPart = ParsePart(leftParts, i);
            var rightPart = ParsePart(rightParts, i);

            if (leftPart < rightPart)
            {
                return -1;
            }

            if (leftPart > rightPart)
            {
                return 1;
            }
        }

        return 0;
    }

    private static int ParsePart(string[] parts, int index)
    {
        if (index >= parts.Length)
        {
            return 0;
        }

        return int.TryParse(parts[index], out var value) ? value : 0;
    }
}
