package core;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class PoolTrace {

	private static final String CLIENT_LOG = "pool-reconnect-client.log";
	private static final String SERVER_LOG = "pool-reconnect-server.log";

	private PoolTrace() {
	}

	private static String resolve(String name) {
		String home = System.getProperty("user.home");
		if (home == null || home.length() == 0)
			home = System.getProperty("user.dir", ".");
		return new File(home, name).getPath();
	}

	private static synchronized void write(String path, String message) {
		PrintWriter out = null;
		try {
			File file = new File(path);
			File parent = file.getParentFile();
			if (parent != null && !parent.exists())
				parent.mkdirs();
			out = new PrintWriter(new FileWriter(file, true));
			String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.format(new Date());
			out.println(ts + " | " + message);
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
		finally {
			if (out != null)
				out.close();
		}
	}

	public static void client(String message) {
		write(resolve(CLIENT_LOG), message);
	}

	public static void client(Throwable throwable) {
		if (throwable == null) {
			client("Throwable: null");
			return;
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);
		pw.flush();
		String[] lines = sw.toString().split("\\r?\\n");
		for (int i = 0; i < lines.length; i++)
			client(lines[i]);
	}

	public static void server(String message) {
		write(resolve(SERVER_LOG), message);
	}
}
