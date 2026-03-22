<%@page import="core.*, data.*, login.*" pageEncoding="Cp1252" contentType="text/plain; charset=Cp1252"%>
<%
    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    String username = request.getParameter("username");
    String password = request.getParameter("password");
    String confirmPassword = request.getParameter("confirm_password");

    if (Initializer.selfInstance == null || Initializer.selfInstance.ids == null) {
        out.println("ERROR");
        out.println("message=Server not initialized");
        return;
    }

    if (username != null)
        username = username.trim();
    if (password == null)
        password = "";
    if (confirmPassword == null)
        confirmPassword = "";

    if (!password.equals(confirmPassword)) {
        out.println("ERROR");
        out.println("message=Passwords do not match");
        return;
    }

    MySQLTable ids = Initializer.selfInstance.ids;
    String ip = request.getRemoteAddr();
    String ycookie[] = new String[1];

    int result = Login.newLoginDirect(ids, username, password, ip, ycookie);
    if (result == 0) {
        out.println("OK");
        out.println("cookie=id=" + username);
        out.println("ycookie=" + ycookie[0]);
        out.println("message=Account created");
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
        case 4:
            message = "User name already taken";
            break;
        default:
            message = "Internal server error";
            break;
    }
    out.println("ERROR");
    out.println("message=" + message);
%>
