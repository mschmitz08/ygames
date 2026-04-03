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

    private String resolveCheckersDictionary(javax.servlet.ServletContext context, String intlCode) {
        String normalized = normalizeIntlCode(intlCode);
        String candidate = "/yog/y/k/" + normalized + "-t4.ldict";
        if(context != null) {
            String realPath = context.getRealPath(candidate);
            if(realPath != null && new File(realPath).exists())
                return candidate;
        }
        return "/yog/y/k/us-t4.ldict";
    }
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=Cp1252"/>
<%
    String agent = request.getHeader("agent");
    String room = request.getParameter("room");
    String intl_code = request.getParameter("intl_code");
    Cookie[] intlCookies = request.getCookies();
    String accountMode = request.getParameter("account_mode");
    String launcherVersion = request.getParameter("launcher_version");
    String appletHost = request.getParameter("host");
    String portParam = request.getParameter("port");
    int appletPort = 11999;
    if((intl_code == null || intl_code.length() == 0) && intlCookies != null){
        for(int i = 0; i < intlCookies.length; i++){
            if("intl_code".equals(intlCookies[i].getName())){
                intl_code = intlCookies[i].getValue();
                break;
            }
        }
    }
    intl_code = normalizeIntlCode(intl_code);
    if(room == null)
        room = "badger_bridge";
    if(accountMode == null)
        accountMode = "";
    if(launcherVersion == null)
        launcherVersion = "";
    if(Initializer.selfInstance != null)
        appletPort = Initializer.selfInstance.getCheckersPort();
    if(portParam != null && portParam.length() > 0){
        try{
            appletPort = Integer.parseInt(portParam);
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
    String roomLabel = findRoomLabel(Initializer.selfInstance != null ? Initializer.selfInstance.checkers_rooms : null,
            room, "Badger Bridge");
    String pageTitle = "RetroPlayHub Checkers - Room: " + roomLabel;
    String dictionaryUrl = resolveCheckersDictionary(application, intl_code);
%>
<title><%=pageTitle%></title>
</head>
<body>
<applet code="y.k.YahooCheckers" name="ygames_applet" codebase="/ny/" archive="client.jar" width="100%" height="100%">
<param name="port" value="<%out.print(appletPort);%>">
<param name="cookie" value="<%out.print(cookie);%>">
<param name="uselogin" value="0">
<param name="agent" value="<%out.print(agent);%>">
<param name="ycookie" value="<%out.print(ycookie);%>">
<param name="logsentmessages" value="0">
<param name="logreceivedmessages" value="0">
<param name="yport" value="<%out.print(room);%>">
<param name="label" value="<%out.print(roomLabel);%>">
<param name="page_title" value="RetroPlayHub Checkers">
<param name="account_mode" value="<%out.print(accountMode);%>">
<param name="launcher_version" value="<%out.print(launcherVersion);%>">
<param name="login_url" value="<%out.print(baseUrl);%>/applet_login.jsp">
<param name="register_url" value="<%out.print(baseUrl);%>/applet_register.jsp">
<param name="change_password_url" value="<%out.print(baseUrl);%>/applet_change_password.jsp">
<param name="ldict_url" value="<%out.print(dictionaryUrl);%>">
<param name="host" value="<%out.print(appletHost);%>">
<param name="ratingmilestones" value="2100|1800|1500|1200|0">
<param name="intl_code" value="<%out.print(intl_code);%>">
</applet>
</body>
</html>
