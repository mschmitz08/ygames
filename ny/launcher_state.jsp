<%@page pageEncoding="UTF-8" contentType="text/plain; charset=UTF-8"%>
<%@page import="java.io.*, java.security.*"%>
<%!
    private String readLauncherVersion(javax.servlet.ServletContext context, String defaultValue) {
        if (context == null)
            return defaultValue;
        FileInputStream input = null;
        try {
            String realPath = context.getRealPath("/downloads/ygames_launcher_windows/launcher_version.txt");
            if (realPath == null)
                return defaultValue;
            File file = new File(realPath);
            if (!file.exists())
                return defaultValue;
            input = new FileInputStream(file);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[256];
            int read;
            while ((read = input.read(buffer)) != -1)
                output.write(buffer, 0, read);
            String value = new String(output.toByteArray(), "UTF-8").trim();
            if (value.length() == 0)
                return defaultValue;
            return value;
        }
        catch (Exception e) {
            return defaultValue;
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException ignore) {
                }
            }
        }
    }

    private String sha256Hex(File file) {
        if (file == null || !file.exists())
            return "";
        FileInputStream input = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            input = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1)
                digest.update(buffer, 0, read);
            byte[] result = digest.digest();
            StringBuffer hex = new StringBuffer();
            for (int i = 0; i < result.length; i++) {
                String part = Integer.toHexString(result[i] & 0xff);
                if (part.length() == 1)
                    hex.append('0');
                hex.append(part);
            }
            return hex.toString();
        }
        catch (Exception e) {
            return "";
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException ignore) {
                }
            }
        }
    }
%>
<%
    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    String launcherVersion = readLauncherVersion(application, "0.7.4");
    File launcherClientJar = new File(application.getRealPath("/downloads/ygames_launcher_windows/app/newyahoo/client.jar"));
    String clientHash = sha256Hex(launcherClientJar);

    out.print("launcher_version=" + launcherVersion + "\n");
    out.print("client_hash=" + clientHash + "\n");
%>
