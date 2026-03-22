<%@page pageEncoding="Cp1252" contentType="text/html; charset=Cp1252"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=Cp1252"/>
<title>Yahoo Pool</title>
</head>
<%
    String game = request.getParameter("game");
    if(game == null)
        game = "pool";

    String room = request.getParameter("room");
    if(room == null || room.length() == 0)
        room = "corner_pocket";
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
        port = "pool2".equalsIgnoreCase(game) ? Initializer.selfInstance.getPool2Port()
                : Initializer.selfInstance.getPoolPort();
    if(portParam != null && portParam.length() > 0){
        try{
            port = Integer.parseInt(portParam);
        }catch(NumberFormatException e){
        }
    }
    if(appletHost == null || appletHost.length() == 0){
        if(Initializer.selfInstance != null && Initializer.selfInstance.getGameHost() != null
                && Initializer.selfInstance.getGameHost().length() > 0)
            appletHost = Initializer.selfInstance.getGameHost();
        else
            appletHost = request.getServerName();
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

    String title = "Yahoo Pool";
    if("pool2".equalsIgnoreCase(game)){
        title = "Yahoo Pool 2";
    }
%>
<body>
<h3><%=title%></h3>
<applet code="y.po.YahooPool" name="ygames_applet" codebase="/ny/" archive="client.jar" width="100%" height="100%">
<param name="port" value="<%=port%>">
<param name="host" value="<%out.print(appletHost);%>">
<param name="yport" value="<%=room%>">
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
</applet>
</body>
</html>
