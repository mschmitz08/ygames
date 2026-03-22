package core;

import java.applet.Applet;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DebugLog {
    private static final String DEFAULT_LOG_FILE = "checkers-debug.log";

    private static boolean enabled = false;
    private static String logFile = defaultLogFilePath();

    static {
        initializeFromSystemProperties();
    }

    private static String defaultLogFilePath() {
        return new File(System.getProperty("user.dir"), DEFAULT_LOG_FILE).getPath();
    }

    private static synchronized void initializeFromSystemProperties() {
        String debugValue = System.getProperty("debug.log.enabled");
        if (debugValue == null) {
            debugValue = System.getProperty("debug");
        }

        if (debugValue != null) {
            enabled = "1".equals(debugValue) || "true".equalsIgnoreCase(debugValue);
        }

        String fileValue = System.getProperty("debug.log.file");
        if (fileValue != null && fileValue.trim().length() > 0) {
            logFile = resolvePath(fileValue.trim());
        }
    }

    public static synchronized void configure(boolean debugEnabled, String filePath) {
        enabled = debugEnabled;
        if (filePath != null && filePath.trim().length() > 0) {
            logFile = resolvePath(filePath.trim());
        } else {
            logFile = defaultLogFilePath();
        }
    }

    public static synchronized void configureFromApplet(Applet applet) {
        if (applet == null) {
            return;
        }

        String debugValue = applet.getParameter("debug");
        String fileValue = applet.getParameter("debug_log");

        if (debugValue == null && fileValue == null) {
            return;
        }

        boolean debugEnabled = "1".equals(debugValue) || "true".equalsIgnoreCase(debugValue);
        enabled = debugEnabled;
        if (fileValue != null && fileValue.trim().length() > 0) {
            logFile = resolvePath(fileValue.trim(), applet.getDocumentBase());
        } else {
            logFile = resolvePath(DEFAULT_LOG_FILE, applet.getDocumentBase());
        }
    }

    private static String resolvePath(String filePath) {
        File file = new File(filePath);
        if (file.isAbsolute()) {
            return file.getPath();
        }
        return new File(System.getProperty("user.dir"), filePath).getPath();
    }

    private static String resolvePath(String filePath, URL baseUrl) {
        File file = new File(filePath);
        if (file.isAbsolute()) {
            return file.getPath();
        }

        if (baseUrl != null && "file".equalsIgnoreCase(baseUrl.getProtocol())) {
            try {
                File baseFile = new File(baseUrl.toURI());
                File parent = baseFile.isDirectory() ? baseFile : baseFile.getParentFile();
                if (parent != null) {
                    return new File(parent, filePath).getPath();
                }
            }
            catch (Throwable t) {
            }
        }

        return resolvePath(filePath);
    }

    private static PrintWriter openWriter() throws Exception {
        if (!enabled) {
            return null;
        }

        File file = new File(logFile);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        return new PrintWriter(new FileWriter(file, true));
    }

    public static synchronized void log(String msg) {
        PrintWriter out = null;
        try {
            out = openWriter();
            if (out == null) {
                return;
            }
            String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            out.println(ts + " | " + msg);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (out != null) out.close();
        }
    }

    public static synchronized void log(String msg, Throwable t) {
        PrintWriter out = null;
        try {
            out = openWriter();
            if (out == null) {
                return;
            }
            String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            out.println(ts + " | " + msg);
            if (t != null) {
                t.printStackTrace(out);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            if (out != null) out.close();
        }
    }
}
