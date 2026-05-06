<%@page import="core.*, data.*, login.*, java.sql.*, java.io.*, java.awt.*, java.awt.image.*, javax.imageio.*" pageEncoding="Cp1252" contentType="text/plain; charset=Cp1252"%>
<%
    final int AVATAR_WIDTH = 34;
    final int AVATAR_HEIGHT = 23;
    final int MAX_UPLOAD_BYTES = 2 * 1024 * 1024;

    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    String name = request.getParameter("name");
    String ycookie = request.getParameter("ycookie");
    if (name != null && name.startsWith("id="))
        name = name.substring(3);

    if (Initializer.selfInstance == null || Initializer.selfInstance.ids == null) {
        out.println("ERROR");
        out.println("message=Server not initialized");
        return;
    }
    if (!Login.isValidCookie(Initializer.selfInstance.ids, name, ycookie)) {
        out.println("ERROR");
        out.println("message=Invalid login");
        return;
    }
    int contentLength = request.getContentLength();
    if (contentLength <= 0 || contentLength > MAX_UPLOAD_BYTES) {
        out.println("ERROR");
        out.println("message=Image is too large");
        return;
    }

    ByteArrayOutputStream upload = new ByteArrayOutputStream();
    InputStream input = request.getInputStream();
    byte[] buffer = new byte[4096];
    int read;
    while ((read = input.read(buffer)) != -1) {
        upload.write(buffer, 0, read);
        if (upload.size() > MAX_UPLOAD_BYTES) {
            out.println("ERROR");
            out.println("message=Image is too large");
            return;
        }
    }

    BufferedImage source = ImageIO.read(new ByteArrayInputStream(upload.toByteArray()));
    if (source == null) {
        out.println("ERROR");
        out.println("message=Unsupported image");
        return;
    }

    BufferedImage normalized = new BufferedImage(AVATAR_WIDTH, AVATAR_HEIGHT,
            BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = normalized.createGraphics();
    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    double scale = Math.min((double) AVATAR_WIDTH / (double) source.getWidth(),
            (double) AVATAR_HEIGHT / (double) source.getHeight());
    int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
    int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
    int x = (AVATAR_WIDTH - width) / 2;
    int y = (AVATAR_HEIGHT - height) / 2;
    graphics.drawImage(source, x, y, width, height, null);
    graphics.dispose();

    ByteArrayOutputStream png = new ByteArrayOutputStream();
    ImageIO.write(normalized, "png", png);

    MySQLTable avatars = new MySQLTable(Initializer.selfInstance.ids.getPool(), "id_avatars");
    PreparedStatement ps = null;
    ResultSet rs = null;
    int version = 1;
    try {
        rs = avatars.getValue(new String[] { "name" }, new Object[] { name },
                new String[] { "version" });
        if (rs != null && rs.next())
            version = rs.getInt("version") + 1;
        if (rs != null) {
            avatars.closeResultSet(rs);
            rs = null;
        }
        ps = avatars.prepareStatement("REPLACE INTO id_avatars (name, mime, image, version, updated_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)");
        ps.setString(1, name);
        ps.setString(2, "image/png");
        ps.setBytes(3, png.toByteArray());
        ps.setInt(4, version);
        ps.executeUpdate();
        out.println("OK");
        out.println("version=" + version);
    } catch (SQLException e) {
        e.printStackTrace();
        out.println("ERROR");
        out.println("message=Unable to save avatar");
    } finally {
        if (rs != null)
            avatars.closeResultSet(rs);
        if (ps != null)
            avatars.closePreparedStatement(ps);
    }
%>
