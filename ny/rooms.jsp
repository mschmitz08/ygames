<%@page pageEncoding="Cp1252" contentType="text/html; charset=Cp1252" %>
<%@page import="core.*, java.sql.*, data.*"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=Cp1252"/>
<title>RetroPlayHub Rooms</title>
<script language="JavaScript" type="text/JavaScript">
<!--
function MM_reloadPage(init) {  //reloads the window if Nav4 resized
  if (init==true) with (navigator) {if ((appName=="Netscape")&&(parseInt(appVersion)==4)) {
    document.MM_pgW=innerWidth; document.MM_pgH=innerHeight; onresize=MM_reloadPage; }}
  else if (innerWidth!=document.MM_pgW || innerHeight!=document.MM_pgH) location.reload();
}
MM_reloadPage(true);
//-->
</script>
</head>
<body>
<div id="Layer1" style="position:absolute; left:9px; top:65px; width:1044px; height:385px; z-index:1"></div>
<%
    String game = request.getParameter("game");
    if(game == null)
        game = "checkers";

    String pageTitle = "RetroPlayHub Checkers";
    String targetPage = "checkers.jsp";
    MySQLTable roomsTable = null;

    if(Initializer.selfInstance != null){
        if("pool".equalsIgnoreCase(game)){
            pageTitle = "RetroPlayHub Pool";
            targetPage = "pool.jsp";
            roomsTable = Initializer.selfInstance.pool_rooms;
        }
        else {
            game = "checkers";
            pageTitle = "RetroPlayHub Checkers";
            targetPage = "checkers.jsp";
            roomsTable = Initializer.selfInstance.checkers_rooms;
        }
    }
    out.print(pageTitle);
%>
</body>

<script>
java_vendor = "";
var launchPage = "<%=targetPage%>";
var gameName = "<%=game%>";
function launchAnteroom(name) {
    window.open("/ny/" + launchPage + "?game=" + encodeURIComponent(gameName) + "&room=" + encodeURIComponent(name), "", "top=2,left=2,toolbar=0,location=0,directories=0,status=1,menubar=0,scrollbars=0,resizable=yes");
    return false;
}
</script>

<script language="javascript">
function initialize() {
    this.name = "retroplayhub_home";
}

function lobbyopen(name) {
    return launchAnteroom(name);
}

initialize();
</script>
<%
    if(roomsTable == null){
        out.println("<br>No room table was initialized for game: " + game);
    }
    else {
        ResultSet rs = roomsTable.getAllValues();
        try {
            while(rs.next()){
                String name = rs.getString("name");
                String label = rs.getString("label");
                int idCount = rs.getInt("id_count");
                out.println("<br><a href=\"#stayhere\" onClick='return lobbyopen(\"" + name + "\");'>" + label + "</a> (" + idCount + ")</br>");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            roomsTable.closeResultSet(rs);
            rs = null;
        }
    }
%>
</html>
