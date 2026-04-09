<%@page pageEncoding="Cp1252" contentType="text/html; charset=Cp1252"%>
<%@page import="java.io.*, java.security.*"%>
<%!
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

    private boolean isLoopbackHost(String host) {
        if(host == null)
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
%>
<%
    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    String launcherVersion = readLauncherVersion(application, "0.7.4");
    String game = request.getParameter("game");
    String room = request.getParameter("room");
    String host = request.getParameter("host");
    String port = request.getParameter("port");
    String width = request.getParameter("width");
    String height = request.getParameter("height");
    String intlCode = request.getParameter("intl_code");
    if ((intlCode == null || intlCode.length() == 0)) {
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
    String siteId = request.getParameter("site_id");
    String expectedClientHash = "";
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
    if (host == null || host.length() == 0 || isLoopbackHost(host))
        host = request.getServerName();
    if (port == null || port.length() == 0)
        port = "pool".equalsIgnoreCase(game) ? "11998" : "11999";
    if (width == null || width.length() == 0)
        width = "1400";
    if (height == null || height.length() == 0)
        height = "900";
    if (intlCode == null || intlCode.length() == 0)
        intlCode = "us";
    if (accountMode == null)
        accountMode = "";
    if (siteId == null)
        siteId = "";
    File launcherClientJar = new File(application.getRealPath("/downloads/ygames_launcher_windows/app/newyahoo/client.jar"));
    expectedClientHash = sha256Hex(launcherClientJar);
    String launcherPackageName = "RetroPlayHubLauncher_" + launcherVersion + ".msi";
    String javaDownloadUrl = "https://www.azul.com/downloads/?architecture=x86-64-bit&os=windows&package=jdk&version=java-8-lts";
    String dotnetDownloadUrl = "https://dotnet.microsoft.com/en-us/download/dotnet-framework/net48";
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=Cp1252"/>
<title>Download RetroPlayHub Launcher</title>
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
.requirements {
    margin-top: 20px;
    padding: 16px 18px;
    border-radius: 14px;
    background: rgba(255, 255, 255, 0.04);
    border: 1px solid rgba(255, 228, 183, 0.18);
}
.requirements h3 {
    margin: 0 0 10px 0;
    font-size: 20px;
}
.requirements p {
    margin: 0 0 10px 0;
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
        + '&width=' + encodeURIComponent('<%=width%>')
        + '&height=' + encodeURIComponent('<%=height%>')
        + '&intl_code=' + encodeURIComponent('<%=intlCode%>')
        + '&site_id=' + encodeURIComponent('<%=siteId%>')
        + '&expected_client_hash=' + encodeURIComponent('<%=expectedClientHash%>')
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

function goBackToLauncher() {
    window.location.href = '<%=webBase%>/index.jsp?intl_code=<%=intlCode%>';
    return false;
}
</script>
</head>
<body>
<div class="shell">
    <div class="card">
        <div class="hero">
            <h1>Download RetroPlayHub Launcher</h1>
            <p>Modern browsers no longer run Java applets in-page, so RetroPlayHub now hands off to a local launcher that can perform checks and open the game window for you.</p>
        </div>
        <div class="body">
            <div class="pane">
                <h2>First-Time Setup</h2>
                <p>Download the Windows installer, run it once, and let RetroPlayHub register the launcher so future game launches can hand off automatically.</p>
                <p><a class="button" href="downloads/<%=launcherPackageName%>">Download RetroPlayHub Launcher <%=launcherVersion%></a></p>
                <ol>
                    <li>Run the MSI installer.</li>
                    <li>Let it install the launcher and updater.</li>
                    <li>If the launcher says Java 8 AppletViewer support is missing, follow the requirement prompt it shows.</li>
                    <li>Come back here and press Launch Again.</li>
                </ol>
                <p><strong>Version tip:</strong> If you already installed an older launcher, rerun this installer version so Windows refreshes the local launcher files.</p>
                <div class="requirements">
                    <h3>Requirements</h3>
                    <p>If the launcher says Java 8 AppletViewer is missing, install a Windows x64 Java 8 JDK with AppletViewer support.</p>
                    <p><a class="button" href="<%=javaDownloadUrl%>" target="_blank" rel="noopener">Download Java 8 JDK</a></p>
                    <p>If Windows says a .NET runtime is missing before the launcher opens, install .NET Framework 4.8. This is the runtime the current launcher uses, and it is the better fit for older systems like Windows 7.</p>
                    <p><a class="button button-secondary" href="<%=dotnetDownloadUrl%>" target="_blank" rel="noopener">Download .NET Framework 4.8</a></p>
                </div>
            </div>
            <div class="pane pane-light">
                <h2>Launch Target</h2>
                <p>The launcher will try to take you straight into the game selection you already chose.</p>
                <div class="launch-box">
                    <strong>Pending launch</strong>
                    Game: <%=game%><br/>
                    Room: <%=room%><br/>
                    Host: <%=host%><br/>
                    Port: <%=port%><br/>
                    Size: <%=width%> x <%=height%>
                </div>
                <p style="margin-top:20px;"><a class="button button-secondary" href="#" onclick="return tryLaunchAgain()">Launch Again</a></p>
                <p style="margin-top:12px;"><a class="button button-secondary" href="#" onclick="return goBackToLauncher()">Back To Launcher</a></p>
                <p style="margin-top:10px; font-size:13px; line-height:1.45;">Use Back To Launcher if you want to pick a different game, room, or window size before trying again.</p>
            </div>
        </div>
    </div>
</div>
</body>
</html>
