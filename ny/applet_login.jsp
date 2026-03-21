<%@page import="core.*, data.*, login.*" pageEncoding="Cp1252" contentType="text/plain; charset=Cp1252"%>
<%
    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    String username = request.getParameter("username");
    String password = request.getParameter("password");

    if (Initializer.selfInstance == null || Initializer.selfInstance.ids == null) {
        out.println("ERROR");
        out.println("message=Server not initialized");
        return;
    }

    if (username != null)
        username = username.trim();
    if (password == null)
        password = "";

    MySQLTable ids = Initializer.selfInstance.ids;
    String ip = request.getRemoteAddr();
    String ycookie[] = new String[1];

    int result = Login.login(ids, username, password, ip, ycookie);
    if (result == 0) {
        out.println("OK");
        out.println("cookie=id=" + username);
        out.println("ycookie=" + ycookie[0]);
        return;
    }

    String message;
    switch (result) {
        case 1:
            message = "Invalid user name";
            break;
        case 2:
            message = "Invalid password format";
            break;
        case 3:
            message = "User does not exist";
            break;
        case 4:
            message = "Incorrect password";
            break;
        case 5:
            message = "Email confirmation pending";
            break;
        case 6:
            message = "Login disabled";
            break;
        default:
            message = "Internal server error";
            break;
    }
    out.println("ERROR");
    out.println("message=" + message);
%>
