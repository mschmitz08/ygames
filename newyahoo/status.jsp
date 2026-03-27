<%@page pageEncoding="Cp1252" contentType="text/html; charset=Cp1252"%>
<%@page import="core.*, data.*, java.sql.*, java.util.*, server.net.*, server.yutils.*, common.utils.*, server.io.*"%>
<%!
    public static class RoomStatusData {
        public String game;
        public String roomName;
        public String roomLabel;
        public int peopleCount;
        public int occupiedTableCount;
        public int seatedPlayerCount;
    }

    public static class GameSummaryData {
        public String game;
        public String title;
        public int roomCount;
        public int occupiedRoomCount;
        public int peopleCount;
        public int occupiedTableCount;
    }

    private String html(String value) {
        if (value == null)
            return "";
        StringBuffer escaped = new StringBuffer();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '&')
                escaped.append("&amp;");
            else if (c == '<')
                escaped.append("&lt;");
            else if (c == '>')
                escaped.append("&gt;");
            else if (c == '"')
                escaped.append("&quot;");
            else
                escaped.append(c);
        }
        return escaped.toString();
    }

    private String url(String value) {
        if (value == null)
            return "";
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        }
        catch (Exception e) {
            return "";
        }
    }

    private int countIds(SynchronizedVector<YahooConnectionId> ids) {
        if (ids == null)
            return 0;
        ids.readLock();
        try {
            return ids.size();
        }
        finally {
            ids.readUnlock();
        }
    }

    private int countSeatedPlayers(YahooTable table) {
        if (table == null)
            return 0;
        YahooConnectionId[] sits = table.getSits();
        int count = 0;
        if (sits != null) {
            for (int i = 0; i < sits.length; i++)
                if (sits[i] != null)
                    count++;
        }
        return count;
    }

    private Vector<RoomStatusData> buildRoomStatus(YahooServer server, MySQLTable roomsTable, String game) {
        Vector<RoomStatusData> statuses = new Vector<RoomStatusData>();
        Hashtable<String, YahooRoom> roomMap = server == null ? null : server.getRooms();
        Hashtable<String, String> seen = new Hashtable<String, String>();
        ResultSet rs = null;

        try {
            if (roomsTable != null)
                rs = roomsTable.getAllValues();
            while (rs != null && rs.next()) {
                String roomName = rs.getString("name");
                String roomLabel = rs.getString("label");
                if (roomLabel == null || roomLabel.length() == 0)
                    roomLabel = roomName;

                RoomStatusData row = new RoomStatusData();
                row.game = game;
                row.roomName = roomName;
                row.roomLabel = roomLabel;

                YahooRoom room = roomMap == null ? null : roomMap.get(roomName);
                if (room != null) {
                    row.peopleCount = countIds(room.getIds());
                    YahooTable[] tables = room.getTables();
                    if (tables != null) {
                        for (int i = 1; i < tables.length; i++) {
                            if (tables[i] == null || tables[i].isFree())
                                continue;
                            row.occupiedTableCount++;
                            row.seatedPlayerCount += countSeatedPlayers(tables[i]);
                        }
                    }
                }

                statuses.add(row);
                seen.put(roomName, roomName);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (rs != null && roomsTable != null)
                roomsTable.closeResultSet(rs);
        }

        if (roomMap != null) {
            Enumeration<String> names = roomMap.keys();
            while (names.hasMoreElements()) {
                String roomName = names.nextElement();
                if (seen.containsKey(roomName))
                    continue;

                YahooRoom room = roomMap.get(roomName);
                RoomStatusData row = new RoomStatusData();
                row.game = game;
                row.roomName = roomName;
                row.roomLabel = roomName;
                row.peopleCount = countIds(room.getIds());
                YahooTable[] tables = room.getTables();
                if (tables != null) {
                    for (int i = 1; i < tables.length; i++) {
                        if (tables[i] == null || tables[i].isFree())
                            continue;
                        row.occupiedTableCount++;
                        row.seatedPlayerCount += countSeatedPlayers(tables[i]);
                    }
                }
                statuses.add(row);
            }
        }

        return statuses;
    }

    private GameSummaryData summarize(String game, String title, Vector<RoomStatusData> rows) {
        GameSummaryData summary = new GameSummaryData();
        summary.game = game;
        summary.title = title;
        summary.roomCount = rows == null ? 0 : rows.size();
        if (rows != null) {
            for (int i = 0; i < rows.size(); i++) {
                RoomStatusData row = rows.elementAt(i);
                if (row.peopleCount > 0 || row.occupiedTableCount > 0)
                    summary.occupiedRoomCount++;
                summary.peopleCount += row.peopleCount;
                summary.occupiedTableCount += row.occupiedTableCount;
            }
        }
        return summary;
    }

    private String toggleSortDir(String sortKey, String activeSort, String activeDir) {
        if (sortKey == null)
            sortKey = "room";
        if (sortKey.equals(activeSort) && !"desc".equalsIgnoreCase(activeDir))
            return "desc";
        return "asc";
    }

    private String sortIndicator(String sortKey, String activeSort, String activeDir) {
        if (sortKey == null || !sortKey.equals(activeSort))
            return "";
        return "desc".equalsIgnoreCase(activeDir) ? " \u2193" : " \u2191";
    }
%>
<%
    String game = request.getParameter("game");
    if (!"checkers".equalsIgnoreCase(game))
        game = "pool";

    String host = request.getParameter("host");
    if (host == null || host.length() == 0)
        host = request.getServerName();

    String width = request.getParameter("width");
    if (width == null || width.length() == 0)
        width = "1400";

    String height = request.getParameter("height");
    if (height == null || height.length() == 0)
        height = "900";

    String roomOverride = request.getParameter("room");
    String sort = request.getParameter("sort");
    if (!"people".equals(sort) && !"tables".equals(sort) && !"seated".equals(sort))
        sort = "room";
    String sortDir = request.getParameter("dir");
    if (!"desc".equalsIgnoreCase(sortDir))
        sortDir = "asc";
    String intlCode = request.getParameter("intl_code");
    if ((intlCode == null || intlCode.trim().length() == 0)) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if ("intl_code".equals(cookies[i].getName())) {
                    intlCode = cookies[i].getValue();
                    break;
                }
            }
        }
    }
    if (intlCode == null || intlCode.trim().length() == 0)
        intlCode = "us";
    else
        intlCode = intlCode.trim().toLowerCase();
    String launcherPath = request.getScheme() + "://" + request.getServerName();
    if (!(request.getScheme().equals("http") && request.getServerPort() == 80)
            && !(request.getScheme().equals("https") && request.getServerPort() == 443))
        launcherPath += ":" + request.getServerPort();
    launcherPath += "/ny/index.jsp?intl_code=" + url(intlCode);

    Vector<RoomStatusData> poolRows = new Vector<RoomStatusData>();
    Vector<RoomStatusData> checkersRows = new Vector<RoomStatusData>();
    GameSummaryData poolSummary = new GameSummaryData();
    GameSummaryData checkersSummary = new GameSummaryData();
    int selectedPort = 11998;

    if (Initializer.selfInstance != null) {
        poolRows = buildRoomStatus(Initializer.selfInstance.getPoolServer(), Initializer.selfInstance.pool_rooms, "pool");
        checkersRows = buildRoomStatus(Initializer.selfInstance.getCheckersServer(), Initializer.selfInstance.checkers_rooms, "checkers");
        poolSummary = summarize("pool", "Pool", poolRows);
        checkersSummary = summarize("checkers", "Checkers", checkersRows);
        selectedPort = "checkers".equals(game) ? Initializer.selfInstance.getCheckersPort() : Initializer.selfInstance.getPoolPort();
    }

    Vector<RoomStatusData> visibleRows = "checkers".equals(game) ? checkersRows : poolRows;
    Collections.sort(visibleRows, new Comparator<RoomStatusData>() {
        public int compare(RoomStatusData left, RoomStatusData right) {
            int result = 0;
            if ("people".equals(sort))
                result = left.peopleCount - right.peopleCount;
            else if ("tables".equals(sort))
                result = left.occupiedTableCount - right.occupiedTableCount;
            else if ("seated".equals(sort))
                result = left.seatedPlayerCount - right.seatedPlayerCount;
            else {
                String leftLabel = left.roomLabel == null ? "" : left.roomLabel.toLowerCase();
                String rightLabel = right.roomLabel == null ? "" : right.roomLabel.toLowerCase();
                result = leftLabel.compareTo(rightLabel);
            }

            if (result == 0) {
                String leftName = left.roomName == null ? "" : left.roomName.toLowerCase();
                String rightName = right.roomName == null ? "" : right.roomName.toLowerCase();
                result = leftName.compareTo(rightName);
            }

            return "desc".equalsIgnoreCase(sortDir) ? -result : result;
        }
    });
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=Cp1252"/>
<title>New Yahoo! Live Rooms</title>
<style type="text/css">
body {
    margin: 0;
    font-family: Georgia, "Times New Roman", serif;
    background:
        radial-gradient(circle at top right, #e1be80 0, rgba(225, 190, 128, 0.06) 32%),
        linear-gradient(135deg, #101523 0%, #182239 48%, #3f2a1e 100%);
    color: #f4ead5;
    min-height: 100vh;
}
.page-shell {
    max-width: 1080px;
    margin: 0 auto;
    padding: 32px;
}
.topbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    margin-bottom: 22px;
}
.back-link {
    display: inline-block;
    padding: 10px 14px;
    border-radius: 999px;
    background: rgba(12, 18, 29, 0.48);
    border: 1px solid rgba(255, 223, 174, 0.24);
    color: #f4ead5;
    text-decoration: none;
}
.hero {
    padding: 30px 32px;
    border-radius: 22px;
    background:
        linear-gradient(115deg, rgba(255, 221, 158, 0.14), rgba(255, 221, 158, 0) 42%),
        linear-gradient(135deg, #1c2540 0%, #2c406a 54%, #1a1d2c 100%);
    box-shadow: 0 24px 80px rgba(0, 0, 0, 0.36);
    border: 1px solid rgba(255, 230, 180, 0.18);
}
.hero h1 {
    margin: 0 0 10px 0;
    font-size: 38px;
    line-height: 1.05;
}
.hero p {
    margin: 0;
    max-width: 760px;
    font-size: 17px;
    line-height: 1.5;
    color: #e7dbc2;
}
.summary-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 16px;
    margin: 24px 0;
}
.summary-card {
    padding: 20px;
    border-radius: 18px;
    border: 1px solid rgba(255, 223, 174, 0.24);
    background: rgba(14, 18, 30, 0.78);
}
.summary-card.active {
    background: linear-gradient(135deg, rgba(219, 145, 62, 0.28), rgba(102, 139, 210, 0.22));
    border-color: rgba(255, 214, 145, 0.72);
}
.summary-card h2 {
    margin: 0 0 14px 0;
    font-size: 26px;
}
.summary-stats {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 10px;
}
.summary-stat {
    padding: 12px;
    border-radius: 14px;
    background: rgba(255, 248, 235, 0.12);
}
.summary-label {
    display: block;
    margin-bottom: 6px;
    font-size: 11px;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    color: #2f2214;
    font-weight: bold;
}
.summary-value {
    display: block;
    font-size: 24px;
    color: #19120b;
}
.panel {
    padding: 28px 30px 30px 30px;
    border-radius: 22px;
    background: linear-gradient(180deg, rgba(238, 224, 194, 0.96) 0%, rgba(224, 206, 172, 0.96) 100%);
    color: #312216;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.28);
}
.panel-head {
    display: flex;
    justify-content: space-between;
    align-items: end;
    gap: 18px;
    margin-bottom: 18px;
}
.panel-head h2 {
    margin: 0;
    font-size: 28px;
}
.panel-copy {
    margin: 8px 0 0 0;
    color: #5f4930;
    line-height: 1.45;
}
.tabs {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
}
.tab-link {
    display: inline-block;
    padding: 10px 14px;
    border-radius: 999px;
    text-decoration: none;
    background: rgba(61, 43, 28, 0.08);
    color: #312216;
    border: 1px solid rgba(61, 43, 28, 0.14);
}
.tab-link.active {
    background: #20314d;
    border-color: #20314d;
    color: #f0e2c6;
}
.room-table {
    width: 100%;
    border-collapse: collapse;
}
.room-table th {
    padding: 0 0 10px 0;
    text-align: left;
    font-size: 12px;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    color: #4c3722;
    font-weight: bold;
}
.room-table th a {
    color: inherit;
    text-decoration: none;
}
.room-table th a:hover {
    color: #23170f;
}
.room-table td {
    padding: 14px 0;
    border-top: 1px solid rgba(78, 56, 36, 0.12);
    vertical-align: middle;
}
.room-name {
    font-size: 20px;
    color: #23170f;
}
.room-slug {
    display: block;
    margin-top: 4px;
    font-size: 12px;
    color: #7a6346;
}
.count-pill {
    display: inline-block;
    min-width: 48px;
    padding: 6px 10px;
    border-radius: 999px;
    text-align: center;
    background: rgba(32, 49, 77, 0.1);
    color: #20140d;
    font-weight: bold;
}
.launch-link {
    display: inline-block;
    padding: 10px 14px;
    border-radius: 12px;
    text-decoration: none;
    background: linear-gradient(90deg, #cc7d35 0%, #efb15a 100%);
    color: #20140d;
    font-weight: bold;
}
.empty-state {
    padding: 18px 0 2px 0;
    color: #6d5639;
}
@media (max-width: 860px) {
    .summary-grid {
        grid-template-columns: 1fr;
    }
    .panel-head {
        display: block;
    }
    .room-table,
    .room-table tbody,
    .room-table tr,
    .room-table td {
        display: block;
        width: 100%;
    }
    .room-table thead {
        display: none;
    }
    .room-table td {
        padding: 8px 0;
        border-top: 0;
    }
    .room-row {
        padding: 14px 0;
        border-top: 1px solid rgba(78, 56, 36, 0.12);
    }
}
</style>
</head>
<body>
<div class="page-shell">
    <div class="topbar">
        <a class="back-link" href="<%=html(launcherPath)%>&game=<%=url(game)%>&host=<%=url(host)%>&port=<%=selectedPort%>&width=<%=url(width)%>&height=<%=url(height)%><%=roomOverride != null && roomOverride.length() > 0 ? "&room=" + url(roomOverride) : ""%>">Back To Launcher</a>
    </div>

    <div class="hero">
        <h1>See which rooms are alive before you jump in.</h1>
        <p>This page reads the live room state from the running server so you can browse Pool and Checkers by actual room traffic, tables in use, and where people are gathered right now.</p>
    </div>

    <div class="summary-grid">
        <div class="summary-card<%="pool".equals(game) ? " active" : ""%>">
            <h2>Pool</h2>
            <div class="summary-stats">
                <div class="summary-stat">
                    <span class="summary-label">Players In Rooms</span>
                    <span class="summary-value"><%=poolSummary.peopleCount%></span>
                </div>
                <div class="summary-stat">
                    <span class="summary-label">Busy Rooms</span>
                    <span class="summary-value"><%=poolSummary.occupiedRoomCount%></span>
                </div>
                <div class="summary-stat">
                    <span class="summary-label">Tables Created</span>
                    <span class="summary-value"><%=poolSummary.occupiedTableCount%></span>
                </div>
            </div>
        </div>
        <div class="summary-card<%="checkers".equals(game) ? " active" : ""%>">
            <h2>Checkers</h2>
            <div class="summary-stats">
                <div class="summary-stat">
                    <span class="summary-label">Players In Rooms</span>
                    <span class="summary-value"><%=checkersSummary.peopleCount%></span>
                </div>
                <div class="summary-stat">
                    <span class="summary-label">Busy Rooms</span>
                    <span class="summary-value"><%=checkersSummary.occupiedRoomCount%></span>
                </div>
                <div class="summary-stat">
                    <span class="summary-label">Tables Created</span>
                    <span class="summary-value"><%=checkersSummary.occupiedTableCount%></span>
                </div>
            </div>
        </div>
    </div>

    <div class="panel">
        <div class="panel-head">
            <div>
                <h2><%="checkers".equals(game) ? "Checkers Rooms" : "Pool Rooms"%></h2>
                <p class="panel-copy">Click any room to bounce back to the launcher with that room already chosen. People count reflects everyone in the room. Tables created reflects active tables that are currently open or in use.</p>
            </div>
            <div class="tabs">
                <a class="tab-link<%="pool".equals(game) ? " active" : ""%>" href="status.jsp?game=pool&host=<%=url(host)%>&width=<%=url(width)%>&height=<%=url(height)%>&intl_code=<%=url(intlCode)%>">Pool</a>
                <a class="tab-link<%="checkers".equals(game) ? " active" : ""%>" href="status.jsp?game=checkers&host=<%=url(host)%>&width=<%=url(width)%>&height=<%=url(height)%>&intl_code=<%=url(intlCode)%>">Checkers</a>
            </div>
        </div>

        <% if (visibleRows.size() == 0) { %>
            <div class="empty-state">No rooms are published for this game yet.</div>
        <% } else { %>
            <table class="room-table">
                <thead>
                    <tr>
                        <th><a href="status.jsp?game=<%=url(game)%>&sort=room&dir=<%=url(toggleSortDir("room", sort, sortDir))%>&host=<%=url(host)%>&width=<%=url(width)%>&height=<%=url(height)%>&intl_code=<%=url(intlCode)%>">Room<%=sortIndicator("room", sort, sortDir)%></a></th>
                        <th><a href="status.jsp?game=<%=url(game)%>&sort=people&dir=<%=url(toggleSortDir("people", sort, sortDir))%>&host=<%=url(host)%>&width=<%=url(width)%>&height=<%=url(height)%>&intl_code=<%=url(intlCode)%>">People<%=sortIndicator("people", sort, sortDir)%></a></th>
                        <th><a href="status.jsp?game=<%=url(game)%>&sort=tables&dir=<%=url(toggleSortDir("tables", sort, sortDir))%>&host=<%=url(host)%>&width=<%=url(width)%>&height=<%=url(height)%>&intl_code=<%=url(intlCode)%>">Tables<%=sortIndicator("tables", sort, sortDir)%></a></th>
                        <th><a href="status.jsp?game=<%=url(game)%>&sort=seated&dir=<%=url(toggleSortDir("seated", sort, sortDir))%>&host=<%=url(host)%>&width=<%=url(width)%>&height=<%=url(height)%>&intl_code=<%=url(intlCode)%>">Seated Players<%=sortIndicator("seated", sort, sortDir)%></a></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                <% for (int i = 0; i < visibleRows.size(); i++) {
                       RoomStatusData row = visibleRows.elementAt(i);
                       int roomPort = "checkers".equals(row.game) && Initializer.selfInstance != null
                               ? Initializer.selfInstance.getCheckersPort()
                               : Initializer.selfInstance != null ? Initializer.selfInstance.getPoolPort() : 11998;
                %>
                    <tr class="room-row">
                        <td>
                            <span class="room-name"><%=html(row.roomLabel)%></span>
                            <span class="room-slug"><%=html(row.roomName)%></span>
                        </td>
                        <td><span class="count-pill"><%=row.peopleCount%></span></td>
                        <td><span class="count-pill"><%=row.occupiedTableCount%></span></td>
                        <td><span class="count-pill"><%=row.seatedPlayerCount%></span></td>
                        <td>
                            <a class="launch-link" href="<%=html(launcherPath)%>&game=<%=url(row.game)%>&room=<%=url(row.roomName)%>&host=<%=url(host)%>&port=<%=roomPort%>&width=<%=url(width)%>&height=<%=url(height)%>&intl_code=<%=url(intlCode)%>">Choose Room</a>
                        </td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        <% } %>
    </div>
</div>
</body>
</html>
