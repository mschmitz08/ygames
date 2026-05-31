<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@page import="core.*, data.*, java.sql.*, java.io.*, java.net.*, java.text.*, java.util.*"%>
<%!
    public static class ProfileStats {
        public String name = "";
        public int rating;
        public int wins;
        public int losses;
        public int draws;
        public int aborteds;
        public int streak;
        public String ip = "";
        public boolean found;
    }

    public static class GameRow {
        public int gameId;
        public Timestamp date;
        public String[] players;
        public int[] oldRatings;
        public int[] newRatings;
        public int[] results;
        public int playerIndex = -1;
    }

    private String html(String value) {
        if (value == null)
            return "";
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '&')
                out.append("&amp;");
            else if (c == '<')
                out.append("&lt;");
            else if (c == '>')
                out.append("&gt;");
            else if (c == '"')
                out.append("&quot;");
            else
                out.append(c);
        }
        return out.toString();
    }

    private String url(String value) {
        try {
            return URLEncoder.encode(value == null ? "" : value, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    private String normalizeGame(String value) {
        if ("checkers".equalsIgnoreCase(value))
            return "checkers";
        return "pool";
    }

    private int parseInt(String value, int fallback) {
        if (value == null)
            return fallback;
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
            return fallback;
        }
    }

    private int normalizeLimit(String value) {
        int limit = parseInt(value, 20);
        if (limit == 50 || limit == 100)
            return limit;
        return 20;
    }

    private boolean isValidTimeZone(String value) {
        if (value == null || value.length() == 0)
            return false;
        String[] ids = TimeZone.getAvailableIDs();
        for (int i = 0; i < ids.length; i++) {
            if (ids[i].equals(value))
                return true;
        }
        return false;
    }

    private String normalizeTimeZone(String value) {
        if (isValidTimeZone(value))
            return value;
        return TimeZone.getDefault().getID();
    }

    private boolean contains(String[] values, String value) {
        if (values == null || value == null)
            return false;
        for (int i = 0; i < values.length; i++) {
            if (value.equals(values[i]))
                return true;
        }
        return false;
    }

    private int[] readIntBlob(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0)
            return new int[0];
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes));
        int[] values = new int[bytes.length / 4];
        for (int i = 0; i < values.length; i++)
            values[i] = input.readInt();
        return values;
    }

    private int[] readRatingBlob(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length < 2)
            return new int[0];
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes));
        int count = input.readUnsignedShort();
        int[] values = new int[count];
        for (int i = 0; i < count; i++)
            values[i] = input.readInt();
        return values;
    }

    private String[] readPlayersBlob(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length < 2)
            return new String[0];
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes));
        int count = input.readUnsignedShort();
        String[] values = new String[count];
        for (int i = 0; i < count; i++)
            values[i] = input.readUTF();
        return values;
    }

    private int indexOfPlayer(String[] players, String name) {
        if (players == null || name == null)
            return -1;
        for (int i = 0; i < players.length; i++) {
            if (players[i] != null && players[i].equalsIgnoreCase(name))
                return i;
        }
        return -1;
    }

    private String opponentText(GameRow row) {
        StringBuffer text = new StringBuffer();
        if (row.players != null) {
            for (int i = 0; i < row.players.length; i++) {
                if (i == row.playerIndex || row.players[i] == null || row.players[i].length() == 0)
                    continue;
                if (text.length() > 0)
                    text.append(", ");
                text.append(row.players[i]);
            }
        }
        return text.length() == 0 ? "Training / empty seat" : text.toString();
    }

    private int safeAt(int[] values, int index, int fallback) {
        if (values == null || index < 0 || index >= values.length)
            return fallback;
        return values[index];
    }

    private String resultText(int value) {
        if (value == 1)
            return "Win";
        if (value == 2)
            return "Loss";
        return "Draw";
    }

    private String resultClass(int value) {
        if (value == 1)
            return "win";
        if (value == 2)
            return "loss";
        return "draw";
    }

    private ProfileStats loadProfile(MySQLTable table, String name) {
        ProfileStats stats = new ProfileStats();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = table.prepareStatement("SELECT name, rating, wins, losses, draws, aborteds, streak, ip FROM "
                    + table.name + " WHERE name = ?");
            ps.setString(1, name);
            rs = ps.executeQuery();
            if (rs.next()) {
                stats.found = true;
                stats.name = rs.getString("name");
                stats.rating = rs.getInt("rating");
                stats.wins = rs.getInt("wins");
                stats.losses = rs.getInt("losses");
                stats.draws = rs.getInt("draws");
                stats.aborteds = rs.getInt("aborteds");
                stats.streak = rs.getInt("streak");
                stats.ip = rs.getString("ip");
            }
        }
        catch (SQLException e) {
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                }
                catch (SQLException e) {
                }
            }
            if (ps != null)
                table.closePreparedStatement(ps);
        }
        return stats;
    }

    private Vector loadGames(MySQLTable table, String name, int offset, int limit) {
        Vector rows = new Vector();
        ResultSet rs = null;
        PreparedStatement ps = null;
        int matched = 0;
        try {
            ps = table.prepareStatement("SELECT game_id, date, players, oldratings, newratings, result FROM "
                    + table.name
                    + " WHERE LOCATE(?, LOWER(CONVERT(players USING latin1))) > 0 ORDER BY date DESC, game_id DESC");
            ps.setString(1, name.toLowerCase(Locale.US));
            rs = ps.executeQuery();
            while (rs.next() && rows.size() < limit + 1) {
                GameRow row = new GameRow();
                row.gameId = rs.getInt("game_id");
                row.date = rs.getTimestamp("date");
                row.players = readPlayersBlob(rs.getBytes("players"));
                row.playerIndex = indexOfPlayer(row.players, name);
                if (row.playerIndex < 0)
                    continue;
                matched++;
                if (matched <= offset)
                    continue;
                row.oldRatings = readRatingBlob(rs.getBytes("oldratings"));
                row.newRatings = readRatingBlob(rs.getBytes("newratings"));
                row.results = readIntBlob(rs.getBytes("result"));
                rows.addElement(row);
            }
        }
        catch (Exception e) {
        }
        finally {
            if (rs != null)
                try {
                    rs.close();
                }
                catch (SQLException e) {
                }
            if (ps != null)
                table.closePreparedStatement(ps);
        }
        return rows;
    }
%>
<%
    String name = request.getParameter("name");
    if (name == null)
        name = "";
    name = name.trim();
    if (name.length() > 32)
        name = name.substring(0, 32);
    String game = normalizeGame(request.getParameter("game"));
    int limit = normalizeLimit(request.getParameter("limit"));
    int pageNumber = parseInt(request.getParameter("page"), 1);
    if (pageNumber < 1)
        pageNumber = 1;
    int offset = (pageNumber - 1) * limit;
    String timeZoneId = normalizeTimeZone(request.getParameter("tz"));
    String[] timeZoneOptions = new String[] {
        "UTC",
        "America/New_York",
        "America/Chicago",
        "America/Denver",
        "America/Los_Angeles",
        "Europe/London",
        "Europe/Paris",
        "Asia/Tokyo",
        "Australia/Sydney"
    };

    Initializer initializer = Initializer.selfInstance;
    MySQLTable profileTable = null;
    MySQLTable gamesTable = null;
    if (initializer != null && initializer.ids != null) {
        if ("checkers".equals(game)) {
            profileTable = new MySQLTable(initializer.ids.getPool(), "checkers_profiles");
            gamesTable = new MySQLTable(initializer.ids.getPool(), "checkers_games");
        }
        else {
            profileTable = new MySQLTable(initializer.ids.getPool(), "pool_profiles");
            gamesTable = new MySQLTable(initializer.ids.getPool(), "pool_games");
        }
    }

    ProfileStats stats = new ProfileStats();
    Vector games = new Vector();
    boolean hasMore = false;
    if (profileTable != null && gamesTable != null && name.length() > 0) {
        stats = loadProfile(profileTable, name);
        games = loadGames(gamesTable, stats.found ? stats.name : name, offset, limit);
        hasMore = games.size() > limit;
        if (hasMore)
            games.setSize(limit);
    }

    int total = stats.wins + stats.losses + ("checkers".equals(game) ? stats.draws : 0) + stats.aborteds;
    String titleName = stats.found ? stats.name : name;
    String otherGame = "checkers".equals(game) ? "pool" : "checkers";
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm a z");
    dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
%>
<!doctype html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>RetroPlayHub Profile - <%=html(titleName)%></title>
<style type="text/css">
body {
    margin: 0;
    min-height: 100vh;
    background: #15171a;
    color: #f2eee8;
    font-family: Arial, Helvetica, sans-serif;
}
.page {
    max-width: 1120px;
    margin: 0 auto;
    padding: 28px 18px 44px;
}
.topbar {
    display: flex;
    justify-content: space-between;
    gap: 16px;
    align-items: flex-start;
    border-bottom: 1px solid #32373b;
    padding-bottom: 18px;
}
h1 {
    margin: 0;
    font-size: 32px;
    font-weight: 800;
    letter-spacing: 0;
}
.subtitle {
    margin-top: 7px;
    color: #b9b0a6;
    font-size: 14px;
}
.tabs, .pager, .limit-switch, .history-controls {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}
.tabs a, .pager a, .limit-switch a {
    border: 1px solid #424a4f;
    color: #f2eee8;
    background: #202427;
    text-decoration: none;
    padding: 8px 11px;
    border-radius: 4px;
    font-size: 13px;
}
.tabs a.active, .limit-switch a.active {
    background: #8b1e22;
    border-color: #bd3538;
}
.summary {
    display: grid;
    grid-template-columns: repeat(6, minmax(0, 1fr));
    gap: 10px;
    margin: 22px 0;
}
.metric {
    background: #202427;
    border: 1px solid #32373b;
    border-radius: 6px;
    padding: 13px 14px;
}
.metric-label {
    color: #b9b0a6;
    font-size: 12px;
    text-transform: uppercase;
}
.metric-value {
    margin-top: 5px;
    font-size: 24px;
    font-weight: 800;
}
.panel {
    background: #1c2023;
    border: 1px solid #32373b;
    border-radius: 8px;
    overflow: hidden;
}
.panel-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
    padding: 16px;
    border-bottom: 1px solid #32373b;
}
.panel-head h2 {
    margin: 0;
    font-size: 19px;
}
.history-controls {
    align-items: center;
    justify-content: flex-end;
}
.time-zone-form {
    display: flex;
    align-items: center;
    gap: 8px;
}
.time-zone-form label {
    color: #b9b0a6;
    font-size: 12px;
    text-transform: uppercase;
}
.time-zone-form select {
    color: #f2eee8;
    background: #202427;
    border: 1px solid #424a4f;
    border-radius: 4px;
    padding: 7px 28px 7px 9px;
    font-size: 13px;
}
table {
    width: 100%;
    border-collapse: collapse;
}
th, td {
    padding: 12px 14px;
    border-bottom: 1px solid #2c3135;
    text-align: left;
    font-size: 14px;
}
th {
    color: #cfc7bd;
    background: #24292d;
    font-size: 12px;
    text-transform: uppercase;
}
.badge {
    display: inline-block;
    min-width: 52px;
    padding: 5px 8px;
    border-radius: 4px;
    text-align: center;
    font-weight: 700;
}
.win {
    color: #ecfff2;
    background: #1d7f43;
}
.loss {
    color: #fff0f0;
    background: #9d2b31;
}
.draw {
    color: #171717;
    background: #d9c36a;
}
.muted {
    color: #b9b0a6;
}
.empty {
    padding: 30px 16px;
    color: #cfc7bd;
}
.pager {
    justify-content: flex-end;
    padding: 14px 16px;
}
.delta-up {
    color: #70dd94;
}
.delta-down {
    color: #ff8b8b;
}
@media (max-width: 800px) {
    .topbar, .panel-head {
        display: block;
    }
    .tabs, .limit-switch {
        margin-top: 12px;
    }
    .summary {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }
    table, thead, tbody, tr, th, td {
        display: block;
    }
    thead {
        display: none;
    }
    td {
        border-bottom: 0;
        padding: 8px 14px;
    }
    tr {
        border-bottom: 1px solid #32373b;
        padding: 8px 0;
    }
    td:before {
        content: attr(data-label);
        display: block;
        color: #b9b0a6;
        font-size: 11px;
        text-transform: uppercase;
        margin-bottom: 3px;
    }
}
</style>
</head>
<body>
<div class="page">
    <div class="topbar">
        <div>
            <h1><%=html(titleName)%></h1>
            <div class="subtitle"><%=html("checkers".equals(game) ? "Checkers" : "Pool")%> profile and rated game history</div>
        </div>
        <div class="tabs">
            <a class="<%="pool".equals(game) ? "active" : ""%>" href="profile2.jsp?game=pool&name=<%=url(name)%>&limit=<%=limit%>&tz=<%=url(timeZoneId)%>">Pool</a>
            <a class="<%="checkers".equals(game) ? "active" : ""%>" href="profile2.jsp?game=checkers&name=<%=url(name)%>&limit=<%=limit%>&tz=<%=url(timeZoneId)%>">Checkers</a>
        </div>
    </div>

<% if (initializer == null) { %>
    <div class="panel" style="margin-top:22px"><div class="empty">The game server is not initialized yet. Start or reload the webapp, then try again.</div></div>
<% } else if (name.length() == 0) { %>
    <div class="panel" style="margin-top:22px"><div class="empty">No player name was provided.</div></div>
<% } else { %>
    <div class="summary">
        <div class="metric"><div class="metric-label">Rating</div><div class="metric-value"><%=stats.found ? String.valueOf(stats.rating) : "N/A"%></div></div>
        <div class="metric"><div class="metric-label">Wins</div><div class="metric-value"><%=stats.wins%></div></div>
        <div class="metric"><div class="metric-label">Losses</div><div class="metric-value"><%=stats.losses%></div></div>
        <div class="metric"><div class="metric-label">Draws</div><div class="metric-value"><%="checkers".equals(game) ? String.valueOf(stats.draws) : "0"%></div></div>
        <div class="metric"><div class="metric-label">Total</div><div class="metric-value"><%=total%></div></div>
        <div class="metric"><div class="metric-label">Streak</div><div class="metric-value"><%=stats.streak%></div></div>
    </div>

    <div class="panel">
        <div class="panel-head">
            <h2>Rated Game History</h2>
            <div class="history-controls">
                <form class="time-zone-form" method="get" action="profile2.jsp">
                    <input type="hidden" name="game" value="<%=html(game)%>">
                    <input type="hidden" name="name" value="<%=html(name)%>">
                    <input type="hidden" name="limit" value="<%=limit%>">
                    <label for="tz">Times</label>
                    <select id="tz" name="tz" onchange="this.form.submit()">
                        <% if (!contains(timeZoneOptions, timeZoneId)) { %>
                            <option value="<%=html(timeZoneId)%>" selected><%=html(timeZoneId)%></option>
                        <% } %>
                        <% for (int i = 0; i < timeZoneOptions.length; i++) { %>
                            <option value="<%=html(timeZoneOptions[i])%>" <%=timeZoneOptions[i].equals(timeZoneId) ? "selected" : ""%>><%=html(timeZoneOptions[i])%></option>
                        <% } %>
                    </select>
                </form>
                <div class="limit-switch">
                    <a class="<%=limit == 20 ? "active" : ""%>" href="profile2.jsp?game=<%=game%>&name=<%=url(name)%>&limit=20&tz=<%=url(timeZoneId)%>">20</a>
                    <a class="<%=limit == 50 ? "active" : ""%>" href="profile2.jsp?game=<%=game%>&name=<%=url(name)%>&limit=50&tz=<%=url(timeZoneId)%>">50</a>
                    <a class="<%=limit == 100 ? "active" : ""%>" href="profile2.jsp?game=<%=game%>&name=<%=url(name)%>&limit=100&tz=<%=url(timeZoneId)%>">100</a>
                </div>
            </div>
        </div>
        <% if (games.size() == 0) { %>
            <div class="empty">No rated games were found for this player in the latest scanned history.</div>
        <% } else { %>
            <table>
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Opponent</th>
                        <th>Result</th>
                        <th>Rating</th>
                        <th>Game ID</th>
                    </tr>
                </thead>
                <tbody>
                <% for (int i = 0; i < games.size(); i++) {
                    GameRow row = (GameRow) games.elementAt(i);
                    int resultValue = safeAt(row.results, row.playerIndex, 0);
                    int oldRating = safeAt(row.oldRatings, row.playerIndex, 0);
                    int newRating = safeAt(row.newRatings, row.playerIndex, 0);
                    int delta = newRating - oldRating;
                %>
                    <tr>
                        <td data-label="Date"><%=row.date != null ? html(dateFormat.format(row.date)) : ""%></td>
                        <td data-label="Opponent"><%=html(opponentText(row))%></td>
                        <td data-label="Result"><span class="badge <%=resultClass(resultValue)%>"><%=resultText(resultValue)%></span></td>
                        <td data-label="Rating">
                            <% if (oldRating > 0 || newRating > 0) { %>
                                <%=oldRating%> &rarr; <%=newRating%>
                                <% if (delta != 0) { %>
                                    <span class="<%=delta > 0 ? "delta-up" : "delta-down"%>">(<%=delta > 0 ? "+" : ""%><%=delta%>)</span>
                                <% } %>
                            <% } else { %>
                                <span class="muted">Not recorded</span>
                            <% } %>
                        </td>
                        <td data-label="Game ID">#<%=row.gameId%></td>
                    </tr>
                <% } %>
                </tbody>
            </table>
            <div class="pager">
                <% if (pageNumber > 1) { %>
                    <a href="profile2.jsp?game=<%=game%>&name=<%=url(name)%>&limit=<%=limit%>&page=<%=pageNumber - 1%>&tz=<%=url(timeZoneId)%>">Previous</a>
                <% } %>
                <% if (hasMore) { %>
                    <a href="profile2.jsp?game=<%=game%>&name=<%=url(name)%>&limit=<%=limit%>&page=<%=pageNumber + 1%>&tz=<%=url(timeZoneId)%>">Next</a>
                <% } %>
            </div>
        <% } %>
    </div>
<% } %>
</div>
</body>
</html>
