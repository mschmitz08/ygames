package core;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

import common.po.IBall;
import common.po.PoolMath;
import common.po.YIPoint;
import common.po.YIVector;

public class PoolTraceLog {
	private static final String	LOG_FILE_NAME	= "pool-collision-debug.log";
	private static final String	PROCESS_TAG		= buildProcessTag();

	private static String buildProcessTag() {
		try {
			return ManagementFactory.getRuntimeMXBean().getName();
		}
		catch (Throwable t) {
			return "unknown-process";
		}
	}

	public static String getDefaultLogFilePath() {
		return new File(System.getProperty("user.home"), LOG_FILE_NAME).getPath();
	}

	private static PrintWriter openWriter() throws Exception {
		File file = new File(getDefaultLogFilePath());
		File parent = file.getParentFile();
		if (parent != null && !parent.exists())
			parent.mkdirs();
		return new PrintWriter(new FileWriter(file, true));
	}

	public static synchronized void log(String scope, String msg) {
		PrintWriter out = null;
		try {
			out = openWriter();
			String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.format(new Date());
			out.println(ts + " | " + PROCESS_TAG + " | " + scope + " | "
					+ msg);
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
		finally {
			if (out != null)
				out.close();
		}
	}

	public static synchronized void log(String scope, String msg, Throwable t) {
		PrintWriter out = null;
		try {
			out = openWriter();
			String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.format(new Date());
			out.println(ts + " | " + PROCESS_TAG + " | " + scope + " | "
					+ msg);
			if (t != null)
				t.printStackTrace(out);
		}
		catch (Throwable ex) {
			ex.printStackTrace();
		}
		finally {
			if (out != null)
				out.close();
		}
	}

	public static String yi(int value) {
		return Float.toString(PoolMath.yintToFloat(value));
	}

	public static String point(YIPoint point) {
		if (point == null)
			return "null";
		return "(" + yi(point.a) + "," + yi(point.b) + ")";
	}

	public static String vec(YIVector vector) {
		if (vector == null)
			return "null";
		return "(" + yi(vector.a) + "," + yi(vector.b) + ")|abs="
				+ yi(vector.abs());
	}

	public static String ball(IBall ball) {
		if (ball == null)
			return "null";
		return "ball#" + ball.getIndex() + "@(" + yi(ball.getX()) + ","
				+ yi(ball.getY()) + ") slot=" + ball.getSlot() + " vel="
				+ vec(ball.getVel());
	}
}
