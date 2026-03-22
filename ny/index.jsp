<%@page pageEncoding="Cp1252" contentType="text/html; charset=Cp1252"%>
<%@page import="core.*, data.*, java.sql.*, java.util.*"%>
<%!
    private String jsEscape(String value) {
        if (value == null)
            return "";
        StringBuffer escaped = new StringBuffer();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' || c == '\'')
                escaped.append('\\');
            if (c == '\r')
                continue;
            if (c == '\n')
                escaped.append("\\n");
            else
                escaped.append(c);
        }
        return escaped.toString();
    }

    private boolean isLoopbackHost(String host) {
        if (host == null)
            return true;
        host = host.trim().toLowerCase();
        return host.length() == 0 || "127.0.0.1".equals(host) || "localhost".equals(host)
                || "::1".equals(host) || "0:0:0:0:0:0:0:1".equals(host);
    }
%>
<%
    String launcherVersion = "0.7.2";
    String requestHost = request.getServerName();
    String defaultHost = requestHost;
    int defaultCheckersPort = 11999;
    int defaultPoolPort = 11998;
    String webBase = request.getScheme() + "://" + request.getServerName();
    if (!(request.getScheme().equals("http") && request.getServerPort() == 80)
            && !(request.getScheme().equals("https") && request.getServerPort() == 443))
        webBase += ":" + request.getServerPort();
    webBase += request.getContextPath();
    Vector<String[]> checkersRooms = new Vector<String[]>();
    Vector<String[]> poolRooms = new Vector<String[]>();

    if (Initializer.selfInstance != null) {
        String configuredHost = Initializer.selfInstance.getGameHost();
        if (configuredHost != null && configuredHost.length() > 0
                && (!isLoopbackHost(configuredHost) || isLoopbackHost(requestHost)))
            defaultHost = configuredHost;
        defaultCheckersPort = Initializer.selfInstance.getCheckersPort();
        defaultPoolPort = Initializer.selfInstance.getPoolPort();
    }

    if (Initializer.selfInstance != null) {
        MySQLTable[] tables = new MySQLTable[] {
                Initializer.selfInstance.checkers_rooms,
                Initializer.selfInstance.pool_rooms
        };
        Vector<String[]>[] targets = new Vector[] { checkersRooms, poolRooms };

        for (int tableIndex = 0; tableIndex < tables.length; tableIndex++) {
            ResultSet rs = null;
            try {
                if (tables[tableIndex] != null)
                    rs = tables[tableIndex].getAllValues();
                while (rs != null && rs.next()) {
                    String name = rs.getString("name");
                    String label = rs.getString("label");
                    if (label == null || label.length() == 0)
                        label = name;
                    targets[tableIndex].add(new String[] { name, label });
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            finally {
                if (rs != null && tables[tableIndex] != null)
                    tables[tableIndex].closeResultSet(rs);
            }
        }
    }
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=Cp1252"/>
<title>New Yahoo! Launcher</title>
<style type="text/css">
body {
    margin: 0;
    font-family: Georgia, "Times New Roman", serif;
    background:
        radial-gradient(circle at top right, #e1be80 0, rgba(225, 190, 128, 0.05) 35%),
        linear-gradient(135deg, #101523 0%, #182239 48%, #3f2a1e 100%);
    color: #f4ead5;
    min-height: 100vh;
}
.launcher-shell {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 32px;
}
.launcher-card {
    width: 880px;
    max-width: 100%;
    border-radius: 24px;
    overflow: hidden;
    box-shadow: 0 24px 80px rgba(0, 0, 0, 0.42);
    background: rgba(14, 18, 30, 0.92);
    border: 1px solid rgba(255, 230, 180, 0.2);
}
.hero {
    position: relative;
    padding: 36px 40px 28px 40px;
    background:
        linear-gradient(115deg, rgba(255, 221, 158, 0.14), rgba(255, 221, 158, 0) 42%),
        linear-gradient(135deg, #1c2540 0%, #2c406a 54%, #1a1d2c 100%);
}
.hero:after {
    content: "";
    position: absolute;
    top: 14px;
    right: -70px;
    width: 220px;
    height: 220px;
    border-radius: 999px;
    background: radial-gradient(circle, rgba(250, 220, 158, 0.34) 0%, rgba(250, 220, 158, 0) 68%);
    animation: drift 6s ease-in-out infinite alternate;
}
.eyebrow {
    display: inline-block;
    margin-bottom: 12px;
    padding: 4px 10px;
    border-radius: 999px;
    background: rgba(10, 12, 22, 0.34);
    color: #f5ddb5;
    font-size: 11px;
    letter-spacing: 0.18em;
}
.hero h1 {
    margin: 0;
    font-size: 42px;
    line-height: 1.05;
}
.hero p {
    margin: 12px 0 0 0;
    max-width: 560px;
    font-size: 18px;
    line-height: 1.45;
    color: #e7dbc2;
}
.version {
    position: absolute;
    right: 24px;
    bottom: 18px;
    font-size: 11px;
    letter-spacing: 0.1em;
    color: rgba(250, 232, 204, 0.78);
}
.content {
    display: grid;
    grid-template-columns: 1.15fr 0.85fr;
    gap: 0;
}
.panel {
    padding: 32px 36px 36px 36px;
}
.panel-left {
    background: rgba(14, 18, 30, 0.72);
}
.panel-right {
    background: linear-gradient(180deg, rgba(238, 224, 194, 0.96) 0%, rgba(224, 206, 172, 0.96) 100%);
    color: #312216;
}
.section-title {
    margin: 0 0 14px 0;
    font-size: 22px;
}
.section-copy {
    margin: 0 0 22px 0;
    line-height: 1.5;
    color: #d6c8aa;
}
.panel-right .section-copy {
    color: #5f4930;
}
.choice-grid {
    display: grid;
    gap: 12px;
}
.game-choice {
    position: relative;
    padding: 16px 18px;
    border-radius: 16px;
    border: 1px solid rgba(255, 227, 174, 0.18);
    background: rgba(255, 255, 255, 0.04);
    cursor: pointer;
    transition: transform 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}
.game-choice:hover {
    transform: translateY(-1px);
    border-color: rgba(255, 218, 145, 0.55);
}
.game-choice.active {
    background: linear-gradient(135deg, rgba(219, 145, 62, 0.28), rgba(102, 139, 210, 0.22));
    border-color: rgba(255, 214, 145, 0.72);
}
.game-choice input {
    position: absolute;
    opacity: 0;
}
.game-name {
    display: block;
    font-size: 22px;
    color: #fff2d3;
}
.game-desc {
    display: block;
    margin-top: 6px;
    font-size: 14px;
    color: #d6c9ae;
}
.field-label {
    display: block;
    margin-bottom: 8px;
    font-size: 12px;
    font-weight: bold;
    letter-spacing: 0.09em;
    text-transform: uppercase;
}
.room-select {
    width: 100%;
    padding: 12px 14px;
    border-radius: 12px;
    border: 1px solid #b99f6f;
    background: #fffaf0;
    color: #2f2219;
    font-size: 16px;
}
.room-note {
    margin: 10px 0 24px 0;
    min-height: 20px;
    font-size: 13px;
    color: #6c5538;
}
.connection-grid {
    display: grid;
    grid-template-columns: 1.3fr 0.7fr;
    gap: 12px;
    margin-bottom: 16px;
}
.connection-input {
    width: 100%;
    box-sizing: border-box;
    padding: 11px 12px;
    border-radius: 12px;
    border: 1px solid #b99f6f;
    background: #fffaf0;
    color: #2f2219;
    font-size: 15px;
}
.action-stack {
    display: grid;
    gap: 12px;
}
.action-button {
    display: block;
    width: 100%;
    padding: 14px 16px;
    border: 0;
    border-radius: 12px;
    cursor: pointer;
    font-size: 16px;
    font-weight: bold;
    letter-spacing: 0.03em;
}
.action-primary {
    background: linear-gradient(90deg, #cc7d35 0%, #efb15a 100%);
    color: #20140d;
}
.action-secondary {
    background: #20314d;
    color: #f0e2c6;
}
.action-tertiary {
    background: #4d3725;
    color: #f7ebd2;
}
.microcopy {
    margin-top: 18px;
    font-size: 12px;
    line-height: 1.5;
    color: #70583b;
}
@keyframes drift {
    from { transform: translate3d(0, 0, 0); }
    to { transform: translate3d(-28px, 14px, 0); }
}
@media (max-width: 760px) {
    .launcher-shell {
        padding: 16px;
    }
    .content {
        grid-template-columns: 1fr;
    }
    .hero {
        padding: 28px 24px 24px 24px;
    }
    .hero h1 {
        font-size: 32px;
    }
    .panel {
        padding: 24px;
    }
}
</style>
<script type="text/javascript">
var launcherVersion = '<%=launcherVersion%>';
var defaultHost = '<%=jsEscape(defaultHost)%>';
var webBase = '<%=jsEscape(webBase)%>';
var defaultPorts = {
    checkers: <%=defaultCheckersPort%>,
    pool: <%=defaultPoolPort%>
};
var roomsByGame = {
    checkers: [
<%
    for (int i = 0; i < checkersRooms.size(); i++) {
        String[] roomData = checkersRooms.elementAt(i);
        if (i > 0)
            out.println(",");
%>        { name: '<%=jsEscape(roomData[0])%>', label: '<%=jsEscape(roomData[1])%>' }<%
    }
%>
    ],
    pool: [
<%
    for (int i = 0; i < poolRooms.size(); i++) {
        String[] roomData = poolRooms.elementAt(i);
        if (i > 0)
            out.println(",");
%>        { name: '<%=jsEscape(roomData[0])%>', label: '<%=jsEscape(roomData[1])%>' }<%
    }
%>
    ]
};

function selectedGame() {
    var options = document.getElementsByName('game');
    for (var i = 0; i < options.length; i++) {
        if (options[i].checked)
            return options[i].value;
    }
    return 'checkers';
}

function refreshGameCards() {
    var cards = document.getElementsByClassName('game-choice');
    var game = selectedGame();
    for (var i = 0; i < cards.length; i++) {
        var card = cards[i];
        if (card.getAttribute('data-game') == game)
            card.className = 'game-choice active';
        else
            card.className = 'game-choice';
    }
}

function populateRooms() {
    var game = selectedGame();
    var select = document.getElementById('room');
    var note = document.getElementById('roomNote');
    var hostField = document.getElementById('host');
    var portField = document.getElementById('port');
    var rooms = roomsByGame[game] || [];
    if (!hostField.value)
        hostField.value = defaultHost;
    portField.value = defaultPorts[game] || '';
    while (select.options.length > 0)
        select.remove(0);
    if (rooms.length === 0) {
        var empty = document.createElement('option');
        empty.value = '';
        empty.text = 'No rooms available';
        select.add(empty);
        note.innerHTML = 'The server has not published any rooms for this game yet.';
        return;
    }
    for (var i = 0; i < rooms.length; i++) {
        var room = rooms[i];
        var option = document.createElement('option');
        option.value = room.name;
        option.text = room.label;
        select.add(option);
    }
    note.innerHTML = 'Pick a room, then choose whether you want to play, register, or change your password.';
}

function syncLauncher() {
    refreshGameCards();
    populateRooms();
}

function launch(mode) {
    var game = selectedGame();
    var room = document.getElementById('room').value;
    var host = document.getElementById('host').value;
    var port = document.getElementById('port').value;
    if (!room) {
        alert('Please choose a room first.');
        return false;
    }
    var protocolUrl = 'nygames://launch?game=' + encodeURIComponent(game)
        + '&room=' + encodeURIComponent(room)
        + '&host=' + encodeURIComponent(host)
        + '&port=' + encodeURIComponent(port)
        + '&launcher_version=' + encodeURIComponent(launcherVersion)
        + '&webbase=' + encodeURIComponent(webBase);
    if (mode && mode.length > 0)
        protocolUrl += '&account_mode=' + encodeURIComponent(mode);

    var downloadUrl = 'launcher_download.jsp?game=' + encodeURIComponent(game)
        + '&room=' + encodeURIComponent(room)
        + '&host=' + encodeURIComponent(host)
        + '&port=' + encodeURIComponent(port)
        + '&launcher_version=' + encodeURIComponent(launcherVersion);
    if (mode && mode.length > 0)
        downloadUrl += '&account_mode=' + encodeURIComponent(mode);

    var iframe = document.getElementById('launcherHandoff');
    if (!iframe) {
        iframe = document.createElement('iframe');
        iframe.id = 'launcherHandoff';
        iframe.style.display = 'none';
        document.body.appendChild(iframe);
    }
    iframe.src = protocolUrl;
    window.setTimeout(function () {
        window.location.href = downloadUrl;
    }, 1400);
    return false;
}
</script>
</head>
<body onload="syncLauncher()">
<div class="launcher-shell">
    <div class="launcher-card">
        <div class="hero">
            <div class="eyebrow">Y! GAMES REVIVAL</div>
            <h1>Pick a game, choose a room, and step right in.</h1>
            <p>The launcher keeps the front door simple now: one place to enter Checkers or Pool, create an account, or update your password before you hit the table.</p>
            <div class="version">Launcher v<%=launcherVersion%></div>
        </div>
        <div class="content">
            <div class="panel panel-left">
                <h2 class="section-title">Choose Your Game</h2>
                <p class="section-copy">Checkers and Pool share the same polished entry flow now. Pool 2 is staying off this launcher for the moment.</p>
                <div class="choice-grid">
                    <label class="game-choice active" data-game="checkers">
                        <input type="radio" name="game" value="checkers" checked="checked" onclick="syncLauncher()"/>
                        <span class="game-name">Checkers</span>
                        <span class="game-desc">Classic lobby play with room-based entry and in-applet account tools.</span>
                    </label>
                    <label class="game-choice" data-game="pool">
                        <input type="radio" name="game" value="pool" onclick="syncLauncher()"/>
                        <span class="game-name">Pool</span>
                        <span class="game-desc">Enter the cue room you want, then sign in or manage your account without leaving the launcher flow.</span>
                    </label>
                </div>
            </div>
            <div class="panel panel-right">
                <h2 class="section-title">Pick A Room</h2>
                <p class="section-copy">Your room selection is carried straight into the applet, so the welcome dialog already knows where you meant to go.</p>
                <label class="field-label" for="room">Room</label>
                <select id="room" class="room-select"></select>
                <p id="roomNote" class="room-note"></p>
                <div class="connection-grid">
                    <div>
                        <label class="field-label" for="host">Connection Host</label>
                        <input id="host" class="connection-input" type="text" value="<%=defaultHost%>"/>
                    </div>
                    <div>
                        <label class="field-label" for="port">Game Port</label>
                        <input id="port" class="connection-input" type="text" value="<%=defaultCheckersPort%>"/>
                    </div>
                </div>
                <div class="action-stack">
                    <button class="action-button action-primary" onclick="return launch('')">Play Now</button>
                    <button class="action-button action-secondary" onclick="return launch('register')">Register In Applet</button>
                    <button class="action-button action-tertiary" onclick="return launch('change_password')">Change Password</button>
                </div>
                <p class="microcopy">The tiny version tag on this screen and the in-applet welcome dialog makes it easy to see which launcher build you are testing as the flow evolves.</p>
            </div>
        </div>
    </div>
</div>
</body>
</html>
