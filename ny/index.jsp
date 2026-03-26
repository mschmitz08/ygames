<%@page pageEncoding="Cp1252" contentType="text/html; charset=Cp1252"%>
<%@page import="core.*, data.*, java.sql.*, java.util.*, java.io.*, java.security.*"%>
<%!
    public static class LocaleOption {
        public String code;
        public String label;
    }

    private String readLauncherVersion(javax.servlet.ServletContext context, String defaultValue) {
        if (context == null)
            return defaultValue;
        FileInputStream input = null;
        try {
            String realPath = context.getRealPath("/downloads/ygames_launcher_windows/launcher_version.txt");
            if (realPath == null)
                return defaultValue;
            File file = new File(realPath);
            if (!file.exists())
                return defaultValue;
            input = new FileInputStream(file);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[256];
            int read;
            while ((read = input.read(buffer)) != -1)
                output.write(buffer, 0, read);
            String value = new String(output.toByteArray(), "UTF-8").trim();
            if (value.length() == 0)
                return defaultValue;
            return value;
        }
        catch (Exception e) {
            return defaultValue;
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException ignore) {
                }
            }
        }
    }

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

    private String sha256Hex(File file) {
        if (file == null || !file.exists())
            return "";
        FileInputStream input = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            input = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1)
                digest.update(buffer, 0, read);
            byte[] result = digest.digest();
            StringBuffer hex = new StringBuffer();
            for (int i = 0; i < result.length; i++) {
                String part = Integer.toHexString(result[i] & 0xff);
                if (part.length() == 1)
                    hex.append('0');
                hex.append(part);
            }
            return hex.toString();
        }
        catch (Exception e) {
            return "";
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException ignore) {
                }
            }
        }
    }

    private String sha256Text(String value) {
        if (value == null)
            value = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] result = digest.digest(value.getBytes("UTF-8"));
            StringBuffer hex = new StringBuffer();
            for (int i = 0; i < result.length; i++) {
                String part = Integer.toHexString(result[i] & 0xff);
                if (part.length() == 1)
                    hex.append('0');
                hex.append(part);
            }
            return hex.toString();
        }
        catch (Exception e) {
            return "";
        }
    }

    private Vector<LocaleOption> readLocaleOptions(javax.servlet.ServletContext context) {
        Vector<LocaleOption> locales = new Vector<LocaleOption>();
        BufferedReader reader = null;
        try {
            String realPath = context == null ? null : context.getRealPath("/WEB-INF/i18n/locales.txt");
            if (realPath == null)
                return locales;
            File file = new File(realPath);
            if (!file.exists())
                return locales;
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.startsWith("#"))
                    continue;
                int split = line.indexOf('|');
                if (split <= 0 || split >= line.length() - 1)
                    continue;
                LocaleOption option = new LocaleOption();
                option.code = line.substring(0, split).trim();
                option.label = line.substring(split + 1).trim();
                if (option.code.length() == 0 || option.label.length() == 0)
                    continue;
                locales.add(option);
            }
        }
        catch (Exception e) {
            locales.clear();
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ignore) {
                }
            }
        }
        return locales;
    }

    private boolean hasLocale(Vector<LocaleOption> locales, String code) {
        if (code == null || locales == null)
            return false;
        for (int i = 0; i < locales.size(); i++) {
            LocaleOption option = locales.elementAt(i);
            if (code.equalsIgnoreCase(option.code))
                return true;
        }
        return false;
    }

    private String findCookieValue(javax.servlet.http.HttpServletRequest request, String name) {
        if (request == null || name == null)
            return null;
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return null;
        for (int i = 0; i < cookies.length; i++) {
            if (name.equals(cookies[i].getName()))
                return cookies[i].getValue();
        }
        return null;
    }
%>
<%
    String launcherVersion = readLauncherVersion(application, "0.7.4");
    String requestedGame = request.getParameter("game");
    if (!"checkers".equalsIgnoreCase(requestedGame))
        requestedGame = "pool";
    String requestedRoom = request.getParameter("room");
    String requestedHost = request.getParameter("host");
    String requestedWidth = request.getParameter("width");
    String requestedHeight = request.getParameter("height");
    String requestedIntlCode = request.getParameter("intl_code");
    if (requestedIntlCode == null || requestedIntlCode.trim().length() == 0)
        requestedIntlCode = findCookieValue(request, "intl_code");
    if (requestedIntlCode == null || requestedIntlCode.trim().length() == 0)
        requestedIntlCode = "us";
    else
        requestedIntlCode = requestedIntlCode.trim().toLowerCase();
    Vector<LocaleOption> localeOptions = readLocaleOptions(application);
    if (localeOptions.size() == 0) {
        LocaleOption fallbackLocale = new LocaleOption();
        fallbackLocale.code = "us";
        fallbackLocale.label = "English";
        localeOptions.add(fallbackLocale);
    }
    if (!hasLocale(localeOptions, requestedIntlCode))
        requestedIntlCode = "us";
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
    File launcherClientJar = new File(application.getRealPath("/downloads/ygames_launcher_windows/app/newyahoo/client.jar"));
    String expectedClientHash = sha256Hex(launcherClientJar);
    String siteIdentitySource = webBase.toLowerCase();
    String siteId = sha256Text(siteIdentitySource);
    if (siteId.length() > 16)
        siteId = siteId.substring(0, 16);

    if (Initializer.selfInstance != null) {
        String configuredHost = Initializer.selfInstance.getGameHost();
        if (configuredHost != null && configuredHost.length() > 0
                && (!isLoopbackHost(configuredHost) || isLoopbackHost(requestHost)))
            defaultHost = configuredHost;
        defaultCheckersPort = Initializer.selfInstance.getCheckersPort();
        defaultPoolPort = Initializer.selfInstance.getPoolPort();
    }

    if (requestedHost != null && requestedHost.trim().length() > 0)
        defaultHost = requestedHost.trim();

    int initialPort = "checkers".equals(requestedGame) ? defaultCheckersPort : defaultPoolPort;
    int initialWidth = 1400;
    int initialHeight = 900;
    try {
        if (requestedWidth != null && requestedWidth.trim().length() > 0)
            initialWidth = Integer.parseInt(requestedWidth.trim());
    }
    catch (NumberFormatException ignore) {
    }
    try {
        if (requestedHeight != null && requestedHeight.trim().length() > 0)
            initialHeight = Integer.parseInt(requestedHeight.trim());
    }
    catch (NumberFormatException ignore) {
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
.size-grid {
    display: grid;
    grid-template-columns: 1.45fr 0.8fr 0.8fr;
    align-items: end;
    gap: 12px;
    margin-bottom: 16px;
}
.size-grid .field-label {
    white-space: nowrap;
}
.size-preset-select {
    min-width: 0;
}
.helper-copy {
    margin: -4px 0 16px 0;
    font-size: 12px;
    line-height: 1.45;
    color: #6c5538;
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
.action-ghost {
    background: rgba(32, 49, 77, 0.08);
    color: #312216;
    border: 1px solid rgba(61, 43, 28, 0.18);
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
var initialGame = '<%=jsEscape(requestedGame)%>';
var initialRoom = '<%=jsEscape(requestedRoom == null ? "" : requestedRoom)%>';
var initialRoomApplied = false;
var defaultHost = '<%=jsEscape(defaultHost)%>';
var webBase = '<%=jsEscape(webBase)%>';
var defaultPorts = {
    checkers: <%=defaultCheckersPort%>,
    pool: <%=defaultPoolPort%>
};
var sizePresets = {
    standard: { width: 1024, height: 768 },
    roomy: { width: 1400, height: 900 },
    large: { width: 1600, height: 1000 },
    widescreen: { width: 1920, height: 1080 }
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
    return initialGame || 'pool';
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
    if (!initialRoomApplied && initialRoom) {
        for (var roomIndex = 0; roomIndex < select.options.length; roomIndex++) {
            if (select.options[roomIndex].value === initialRoom) {
                select.selectedIndex = roomIndex;
                break;
            }
        }
        initialRoomApplied = true;
    }
    note.innerHTML = 'Pick a room, then choose whether you want to play, register, or change your password.';
}

function syncLauncher() {
    refreshGameCards();
    populateRooms();
}

function applySizePreset() {
    var preset = document.getElementById('sizePreset').value;
    var widthField = document.getElementById('width');
    var heightField = document.getElementById('height');
    if (preset === 'custom')
        return;
    var values = sizePresets[preset];
    if (!values)
        return;
    widthField.value = values.width;
    heightField.value = values.height;
}

function persistIntlCode() {
    var intlField = document.getElementById('intlCode');
    if (!intlField)
        return;
    var expires = new Date();
    expires.setFullYear(expires.getFullYear() + 1);
    document.cookie = 'intl_code=' + encodeURIComponent(intlField.value)
        + '; expires=' + expires.toUTCString()
        + '; path=/';
}

function browseLiveRooms() {
    var game = selectedGame();
    var host = document.getElementById('host').value;
    var width = document.getElementById('width').value;
    var height = document.getElementById('height').value;
    var intlCode = document.getElementById('intlCode').value;
    persistIntlCode();
    window.location.href = '../newyahoo/status.jsp?game=' + encodeURIComponent(game)
        + '&host=' + encodeURIComponent(host)
        + '&width=' + encodeURIComponent(width)
        + '&height=' + encodeURIComponent(height)
        + '&intl_code=' + encodeURIComponent(intlCode);
    return false;
}

function launch(mode) {
    var game = selectedGame();
    var room = document.getElementById('room').value;
    var host = document.getElementById('host').value;
    var port = document.getElementById('port').value;
    var width = document.getElementById('width').value;
    var height = document.getElementById('height').value;
    var intlCode = document.getElementById('intlCode').value;
    persistIntlCode();
    if (!room) {
        alert('Please choose a room first.');
        return false;
    }
    var protocolUrl = 'nygames://launch?game=' + encodeURIComponent(game)
        + '&room=' + encodeURIComponent(room)
        + '&host=' + encodeURIComponent(host)
        + '&port=' + encodeURIComponent(port)
        + '&width=' + encodeURIComponent(width)
        + '&height=' + encodeURIComponent(height)
        + '&intl_code=' + encodeURIComponent(intlCode)
        + '&site_id=' + encodeURIComponent('<%=siteId%>')
        + '&expected_client_hash=' + encodeURIComponent('<%=expectedClientHash%>')
        + '&launcher_version=' + encodeURIComponent(launcherVersion)
        + '&webbase=' + encodeURIComponent(webBase);
    if (mode && mode.length > 0)
        protocolUrl += '&account_mode=' + encodeURIComponent(mode);

    var downloadUrl = 'launcher_download.jsp?game=' + encodeURIComponent(game)
        + '&room=' + encodeURIComponent(room)
        + '&host=' + encodeURIComponent(host)
        + '&port=' + encodeURIComponent(port)
        + '&width=' + encodeURIComponent(width)
        + '&height=' + encodeURIComponent(height)
        + '&intl_code=' + encodeURIComponent(intlCode)
        + '&site_id=' + encodeURIComponent('<%=siteId%>')
        + '&expected_client_hash=' + encodeURIComponent('<%=expectedClientHash%>')
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
<body onload="syncLauncher(); persistIntlCode()">
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
                <p class="section-copy">Pool comes up first now, but you can bounce between Pool and Checkers and keep the same polished launcher flow. Pool 2 has been retired so the launcher stays leaner and the runtime only exposes the stable games.</p>
                <div class="choice-grid">
                    <label class="game-choice<%="checkers".equals(requestedGame) ? " active" : ""%>" data-game="checkers">
                        <input type="radio" name="game" value="checkers" <%="checkers".equals(requestedGame) ? "checked=\"checked\"" : ""%> onclick="syncLauncher()"/>
                        <span class="game-name">Checkers</span>
                        <span class="game-desc">Classic lobby play with room-based entry and in-applet account tools.</span>
                    </label>
                    <label class="game-choice<%="pool".equals(requestedGame) ? " active" : ""%>" data-game="pool">
                        <input type="radio" name="game" value="pool" <%="pool".equals(requestedGame) ? "checked=\"checked\"" : ""%> onclick="syncLauncher()"/>
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
                <label class="field-label" for="intlCode">Language</label>
                <select id="intlCode" class="room-select" onchange="persistIntlCode()">
                    <% for (int localeIndex = 0; localeIndex < localeOptions.size(); localeIndex++) {
                        LocaleOption localeOption = localeOptions.elementAt(localeIndex); %>
                    <option value="<%=localeOption.code%>" <%=localeOption.code.equals(requestedIntlCode) ? "selected=\"selected\"" : ""%>><%=localeOption.label%></option>
                    <% } %>
                </select>
                <p class="helper-copy">The launcher now exposes the top language set we plan to support. Any locale without a shipped dictionary still falls back to English until its translation files are added.</p>
                <div class="connection-grid">
                    <div>
                        <label class="field-label" for="host">Connection Host</label>
                        <input id="host" class="connection-input" type="text" value="<%=defaultHost%>"/>
                    </div>
                    <div>
                        <label class="field-label" for="port">Game Port</label>
                        <input id="port" class="connection-input" type="text" value="<%=initialPort%>"/>
                    </div>
                </div>
                <div class="size-grid">
                    <div>
                        <label class="field-label" for="sizePreset">Preset Size</label>
                        <select id="sizePreset" class="room-select size-preset-select" onchange="applySizePreset()">
                            <option value="roomy" selected="selected">Roomy (1400x900)</option>
                            <option value="standard">Standard (1024x768)</option>
                            <option value="large">Large (1600x1000)</option>
                            <option value="widescreen">Widescreen (1920x1080)</option>
                            <option value="custom">Custom</option>
                        </select>
                    </div>
                    <div>
                        <label class="field-label" for="width">Width</label>
                        <input id="width" class="connection-input" type="text" value="<%=initialWidth%>"/>
                    </div>
                    <div>
                        <label class="field-label" for="height">Height</label>
                        <input id="height" class="connection-input" type="text" value="<%=initialHeight%>"/>
                    </div>
                </div>
                <p class="helper-copy">Preset and custom size affect the applet window launched from this page. You can change these before each launch.</p>
                <div class="action-stack">
                    <button class="action-button action-primary" onclick="return launch('')">Play Now</button>
                    <button class="action-button action-secondary" onclick="return launch('register')">Register In Applet</button>
                    <button class="action-button action-tertiary" onclick="return launch('change_password')">Change Password</button>
                    <button class="action-button action-ghost" onclick="return browseLiveRooms()">Browse Live Rooms</button>
                </div>
                <p class="microcopy">The tiny version tag on this screen and the in-applet welcome dialog makes it easy to see which launcher build you are testing as the flow evolves. Browse Live Rooms shows where the activity is before you launch.</p>
            </div>
        </div>
    </div>
</div>
</body>
</html>
