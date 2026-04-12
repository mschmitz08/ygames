<%@page pageEncoding="Cp1252" contentType="text/html; charset=Cp1252"%>
<%@page import="core.*"%>
<%@page import="data.*"%>
<%@page import="java.io.*"%>
<%!
    private boolean isLoopbackHost(String host) {
        if(host == null)
            return true;
        host = host.trim().toLowerCase();
        return host.length() == 0 || "127.0.0.1".equals(host) || "localhost".equals(host)
                || "::1".equals(host) || "0:0:0:0:0:0:0:1".equals(host);
    }

    private String findRoomLabel(MySQLTable table, String room, String fallback) {
        if(table == null || room == null || room.length() == 0)
            return fallback;
        java.sql.ResultSet rs = null;
        try {
            rs = table.getAllValues(new String[] { "name" }, new Object[] { room });
            if(rs.next()) {
                String label = rs.getString("label");
                if(label != null && label.length() > 0)
                    return label;
            }
        } catch (java.sql.SQLException e) {
        } finally {
            if(rs != null)
                table.closeResultSet(rs);
        }
        return fallback;
    }

    private String normalizeIntlCode(String value) {
        if(value == null)
            return "us";
        value = value.trim().toLowerCase();
        if(value.length() == 0)
            return "us";
        StringBuffer normalized = new StringBuffer();
        for(int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-')
                normalized.append(c);
        }
        if(normalized.length() == 0)
            return "us";
        return normalized.toString();
    }

    private String resolvePoolDictionary(javax.servlet.ServletContext context, String intlCode) {
        String normalized = normalizeIntlCode(intlCode);
        String candidate = "/yog/y/po/" + normalized + "-ti.ldict";
        if(context != null) {
            String realPath = context.getRealPath(candidate);
            if(realPath != null && new File(realPath).exists())
                return candidate;
        }
        return "/yog/y/po/us-ti.ldict";
    }
%>
<%
    Cookie[] intlCookies = request.getCookies();
    String room = request.getParameter("room");
    if(room == null || room.length() == 0)
        room = "corner_pocket";
    String intlCode = request.getParameter("intl_code");
    if((intlCode == null || intlCode.length() == 0) && intlCookies != null){
        for(int i = 0; i < intlCookies.length; i++){
            if("intl_code".equals(intlCookies[i].getName())){
                intlCode = intlCookies[i].getValue();
                break;
            }
        }
    }
    intlCode = normalizeIntlCode(intlCode);
    String accountMode = request.getParameter("account_mode");
    String launcherVersion = request.getParameter("launcher_version");
    String appletHost = request.getParameter("host");
    String portParam = request.getParameter("port");
    String agent = request.getHeader("agent");
    int port = 11998;
    if(accountMode == null)
        accountMode = "";
    if(launcherVersion == null)
        launcherVersion = "";
    if(Initializer.selfInstance != null)
        port = Initializer.selfInstance.getPoolPort();
    if(portParam != null && portParam.length() > 0){
        try{
            port = Integer.parseInt(portParam);
        }catch(NumberFormatException e){
        }
    }
    if(appletHost == null || appletHost.length() == 0){
        String requestHost = request.getServerName();
        appletHost = requestHost;
        if(Initializer.selfInstance != null && Initializer.selfInstance.getGameHost() != null
                && Initializer.selfInstance.getGameHost().length() > 0
                && (!isLoopbackHost(Initializer.selfInstance.getGameHost())
                    || isLoopbackHost(requestHost)))
            appletHost = Initializer.selfInstance.getGameHost();
    }

    String baseUrl = request.getScheme() + "://" + request.getServerName();
    if(!(request.getScheme().equals("http") && request.getServerPort() == 80)
            && !(request.getScheme().equals("https") && request.getServerPort() == 443))
        baseUrl += ":" + request.getServerPort();
    baseUrl += request.getContextPath();
    String clientJarCacheToken = "";
    String launcherClientJarPath = application.getRealPath("/downloads/ygames_launcher_windows/app/newyahoo/client.jar");
    if(launcherClientJarPath != null) {
        File launcherClientJar = new File(launcherClientJarPath);
        if(launcherClientJar.exists())
            clientJarCacheToken = "?v=" + launcherClientJar.lastModified();
    }

    Cookie[] cookies = request.getCookies();
    String cookie = "";
    String ycookie = "";
    if(cookies != null){
        for(int i = 0; i < cookies.length; i++){
            String cookieName = cookies[i].getName();
            if(cookieName.equals("cookie"))
                cookie = cookies[i].getValue();
            else if(cookieName.equals("ycookie"))
                ycookie = cookies[i].getValue();
        }
    }

    String roomLabel = findRoomLabel(Initializer.selfInstance != null ? Initializer.selfInstance.pool_rooms : null,
            room, "Corner Pocket");
    String title = "RetroPlayHub Pool";
    String pageTitle = title + " - Room: " + roomLabel;
    String dictionaryUrl = resolvePoolDictionary(application, intlCode);
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=Cp1252"/>
<title><%=pageTitle%></title>
</head>
<body>
<h3><%=pageTitle%></h3>
<applet code="y.po.YahooPool" name="ygames_applet" codebase="/ny/" archive="downloads/ygames_launcher_windows/app/newyahoo/client.jar<%=clientJarCacheToken%>" width="100%" height="100%">
<param name="port" value="<%=port%>">
<param name="host" value="<%out.print(appletHost);%>">
<param name="yport" value="<%=room%>">
<param name="label" value="<%out.print(roomLabel);%>">
<param name="page_title" value="<%out.print(title);%>">
<param name="cookie" value="<%out.print(cookie);%>">
<param name="uselogin" value="0">
<param name="agent" value="<%out.print(agent);%>">
<param name="ycookie" value="<%out.print(ycookie);%>">
<param name="logsentmessages" value="0">
<param name="logreceivedmessages" value="0">
<param name="room" value="<%=room%>">
<param name="account_mode" value="<%out.print(accountMode);%>">
<param name="launcher_version" value="<%out.print(launcherVersion);%>">
<param name="login_url" value="<%out.print(baseUrl);%>/applet_login.jsp">
<param name="register_url" value="<%out.print(baseUrl);%>/applet_register.jsp">
<param name="change_password_url" value="<%out.print(baseUrl);%>/applet_change_password.jsp">
<param name="path" value="/ny/servlet/YahooPoolServlet">
<param name="update" value="1">
<param name="intl_code" value="<%out.print(intlCode);%>">
<param name="ldict_url" value="<%out.print(dictionaryUrl);%>">
</applet>
</body>
</html>
