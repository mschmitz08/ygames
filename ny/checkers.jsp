<%@page pageEncoding="Cp1252" contentType="text/html; charset=Cp1252"%>
<%@page import="core.*"%>
<%!
    private boolean isLoopbackHost(String host) {
        if(host == null)
            return true;
        host = host.trim().toLowerCase();
        return host.length() == 0 || "127.0.0.1".equals(host) || "localhost".equals(host)
                || "::1".equals(host) || "0:0:0:0:0:0:0:1".equals(host);
    }
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=Cp1252"/>
<title>Yahoo Checkers</title>
</head>
<%
    String agent = request.getHeader("agent");
    String room = request.getParameter("room");
    String intl_code = request.getParameter("intl_code");
    String accountMode = request.getParameter("account_mode");
    String launcherVersion = request.getParameter("launcher_version");
    String appletHost = request.getParameter("host");
    String portParam = request.getParameter("port");
    int appletPort = 11999;
    if(intl_code == null)
        intl_code = "us";
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
%>
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
<param name="account_mode" value="<%out.print(accountMode);%>">
<param name="launcher_version" value="<%out.print(launcherVersion);%>">
<param name="login_url" value="<%out.print(baseUrl);%>/applet_login.jsp">
<param name="register_url" value="<%out.print(baseUrl);%>/applet_register.jsp">
<param name="change_password_url" value="<%out.print(baseUrl);%>/applet_change_password.jsp">
<param name="ldict_url" value="/yog/y/k/us-t4.ldict">
<param name="host" value="<%out.print(appletHost);%>">
<param name="ratingmilestones" value="2100|1800|1500|1200|0">
<param name="intl_code" value="<%out.print(intl_code);%>">
</applet>
</body>
</html>
