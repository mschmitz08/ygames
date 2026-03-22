<%@page pageEncoding="Cp1252" contentType="text/html; charset=Cp1252"%>
<%
    String game = request.getParameter("game");
    String room = request.getParameter("room");
    String host = request.getParameter("host");
    String port = request.getParameter("port");
    String accountMode = request.getParameter("account_mode");
    String webBase = request.getScheme() + "://" + request.getServerName();
    if (!(request.getScheme().equals("http") && request.getServerPort() == 80)
            && !(request.getScheme().equals("https") && request.getServerPort() == 443))
        webBase += ":" + request.getServerPort();
    webBase += request.getContextPath();
    if (game == null || game.length() == 0)
        game = "pool";
    if (room == null || room.length() == 0)
        room = "corner_pocket";
    if (host == null || host.length() == 0)
        host = request.getServerName();
    if (port == null || port.length() == 0)
        port = "pool".equalsIgnoreCase(game) ? "11998" : "11999";
    if (accountMode == null)
        accountMode = "";
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=Cp1252"/>
<title>Download Launcher</title>
<style type="text/css">
body {
    margin: 0;
    font-family: Georgia, "Times New Roman", serif;
    background: linear-gradient(135deg, #151c2e 0%, #2b354c 54%, #6a543c 100%);
    color: #f4ead8;
}
.shell {
    max-width: 860px;
    margin: 0 auto;
    padding: 48px 24px 64px 24px;
}
.card {
    background: rgba(14, 18, 30, 0.9);
    border: 1px solid rgba(255, 222, 166, 0.18);
    border-radius: 22px;
    box-shadow: 0 22px 70px rgba(0, 0, 0, 0.35);
    overflow: hidden;
}
.hero {
    padding: 28px 32px;
    background: linear-gradient(135deg, #2b406d 0%, #263252 100%);
}
.hero h1 {
    margin: 0 0 10px 0;
    font-size: 40px;
}
.hero p {
    margin: 0;
    max-width: 620px;
    font-size: 18px;
    line-height: 1.5;
}
.body {
    display: grid;
    grid-template-columns: 1fr 1fr;
}
.pane {
    padding: 30px 32px 34px 32px;
}
.pane-light {
    background: linear-gradient(180deg, rgba(238, 224, 194, 0.96) 0%, rgba(224, 206, 172, 0.96) 100%);
    color: #32251a;
}
.pane h2 {
    margin: 0 0 12px 0;
    font-size: 28px;
}
.pane p, .pane li {
    font-size: 17px;
    line-height: 1.5;
}
.pane ul, .pane ol {
    margin: 14px 0 0 24px;
}
.button {
    display: inline-block;
    padding: 14px 18px;
    border-radius: 12px;
    background: linear-gradient(90deg, #cf8233 0%, #f0b45e 100%);
    color: #23170e;
    text-decoration: none;
    font-weight: bold;
}
.button-secondary {
    background: #213150;
    color: #f4e7cf;
}
.launch-box {
    margin-top: 18px;
    padding: 14px 16px;
    border-radius: 14px;
    background: rgba(255, 255, 255, 0.06);
    border: 1px solid rgba(255, 228, 183, 0.18);
}
.launch-box strong {
    display: block;
    margin-bottom: 8px;
}
@media (max-width: 760px) {
    .body {
        grid-template-columns: 1fr;
    }
}
</style>
<script type="text/javascript">
function tryLaunchAgain() {
    var protocolUrl = 'nygames://launch?game=' + encodeURIComponent('<%=game%>')
        + '&room=' + encodeURIComponent('<%=room%>')
        + '&host=' + encodeURIComponent('<%=host%>')
        + '&port=' + encodeURIComponent('<%=port%>')
        + '&webbase=' + encodeURIComponent('<%=webBase%>');
<%
    if (accountMode.length() > 0) {
%>    protocolUrl += '&account_mode=' + encodeURIComponent('<%=accountMode%>');
<%
    }
%>
    window.location.href = protocolUrl;
    return false;
}
</script>
</head>
<body>
<div class="shell">
    <div class="card">
        <div class="hero">
            <h1>Download The Launcher</h1>
            <p>Modern browsers no longer run Java applets in-page, so the website now hands off to a local launcher package that can perform checks and open the game window for you.</p>
        </div>
        <div class="body">
            <div class="pane">
                <h2>First-Time Setup</h2>
                <p>Download the Windows launcher package, extract it somewhere permanent, and run the installer once so your browser can hand off future game launches automatically.</p>
                <p><a class="button" href="downloads/ygames_launcher_windows.zip">Download Windows Launcher</a></p>
                <ol>
                    <li>Extract the ZIP file.</li>
                    <li>Run <code>install_launcher.bat</code>.</li>
                    <li>If the launcher says Java 8 AppletViewer support is missing, follow the requirement prompt it shows.</li>
                    <li>Come back here and press Launch Again.</li>
                </ol>
            </div>
            <div class="pane pane-light">
                <h2>Launch Target</h2>
                <p>The launcher will try to take you straight into the game selection you already chose.</p>
                <div class="launch-box">
                    <strong>Pending launch</strong>
                    Game: <%=game%><br/>
                    Room: <%=room%><br/>
                    Host: <%=host%><br/>
                    Port: <%=port%>
                </div>
                <p style="margin-top:20px;"><a class="button button-secondary" href="#" onclick="return tryLaunchAgain()">Launch Again</a></p>
            </div>
        </div>
    </div>
</div>
</body>
</html>
