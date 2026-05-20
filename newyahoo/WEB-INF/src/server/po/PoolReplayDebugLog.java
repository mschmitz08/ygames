package server.po;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import server.io.YahooConnectionId;
import server.yutils.YahooRoom;

class PoolReplayDebugLog {

	private static final String	LOG_NAME	= "pool-replay-debug.log";

	private PoolReplayDebugLog() {
	}

	static synchronized void log(YahooRoom room, int tableNumber,
			YahooConnectionId id, String message) {
		PrintWriter out = null;
		try {
			File file = logFile();
			File parent = file.getParentFile();
			if (parent != null && !parent.exists())
				parent.mkdirs();
			out = new PrintWriter(new FileWriter(file, true));
			String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.format(new Date());
			String roomName = room != null ? room.getYport() : "?";
			String name = id != null && id.getName() != null ? id.getName()
					: "?";
			out.println(ts + " | room=" + roomName + " table=" + tableNumber
					+ " user=" + name + " | " + message);
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
		finally {
			if (out != null)
				out.close();
		}
	}

	static File logFile() {
		String base = System.getProperty("catalina.base");
		if (base == null || base.trim().length() == 0)
			base = System.getProperty("catalina.home");
		if (base != null && base.trim().length() > 0)
			return new File(new File(base, "logs"), LOG_NAME);
		return new File(System.getProperty("user.dir"), LOG_NAME);
	}
}
