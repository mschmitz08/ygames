package core;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DebugLog {
    private static final String LOG_FILE = "C:\\Users\\mschm\\Desktop\\ny-master\\ny\\checkers-debug.log";

    public static synchronized void log(String msg) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(LOG_FILE, true));
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
            out = new PrintWriter(new FileWriter(LOG_FILE, true));
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
