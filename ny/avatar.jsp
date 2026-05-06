<%@page import="core.*, data.*, login.*, java.sql.*, java.io.*" pageEncoding="Cp1252"%>
<%
    String name = request.getParameter("name");
    String versionParam = request.getParameter("v");
    int version = 0;
    try {
        if (versionParam != null)
            version = Integer.parseInt(versionParam);
    } catch (NumberFormatException e) {
        version = 0;
    }

    if (Initializer.selfInstance == null || Initializer.selfInstance.ids == null
            || name == null || name.length() == 0) {
        response.sendError(404);
        return;
    }

    MySQLTable avatars = new MySQLTable(Initializer.selfInstance.ids.getPool(), "id_avatars");
    ResultSet rs = null;
    try {
        rs = avatars.getValue(new String[] { "name" }, new Object[] { name },
                new String[] { "mime", "image", "version" });
        if (rs == null || !rs.next()) {
            response.sendError(404);
            return;
        }
        int currentVersion = rs.getInt("version");
        if (version > 0 && version != currentVersion) {
            response.sendError(404);
            return;
        }
        String mime = rs.getString("mime");
        if (mime == null || mime.length() == 0)
            mime = "image/png";
        Blob blob = rs.getBlob("image");
        if (blob == null) {
            response.sendError(404);
            return;
        }
        response.setContentType(mime);
        response.setHeader("Cache-Control", "public, max-age=31536000");
        response.setHeader("ETag", "\"" + name + "-" + currentVersion + "\"");
        response.setContentLength((int) blob.length());
        InputStream input = blob.getBinaryStream();
        OutputStream output = response.getOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = input.read(buffer)) != -1)
            output.write(buffer, 0, read);
        input.close();
        output.flush();
    } catch (SQLException e) {
        e.printStackTrace();
        response.sendError(500);
    } finally {
        if (rs != null)
            avatars.closeResultSet(rs);
    }
%>
