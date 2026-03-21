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

    String nick = request.getParameter("nick");
    if(nick == null || nick.length() == 0)
        nick = "guest" + (int)(99999 * Math.random());

    int port = 11998;
    String title = "Yahoo Pool";
    if("pool2".equalsIgnoreCase(game)){
        port = 12002;
        title = "Yahoo Pool 2";
    }
%>
<body>
<h3><%=title%></h3>
<applet code="y.po.YahooPool" name="ygames_applet" codebase="/ny/" archive="client.jar" width="100%" height="100%">
<param name="port" value="<%=port%>">
<param name="host" value="127.0.0.1">
<param name="yport" value="<%=room%>">
<param name="cookie" value="id=<%out.print(nick);%>">
<param name="uselogin" value="0">
<param name="ycookie" value=".">
<param name="logsentmessages" value="1">
<param name="logreceivedmessages" value="1">
<param name="room" value="<%=room%>">
<param name="path" value="/ny/servlet/YahooPoolServlet">
<param name="update" value="1">
</applet>
</body>
</html>
