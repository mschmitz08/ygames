package common.po;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class PoolTraceLog {
	private static final Object	LOCK		= new Object();
	private static final File	LOG_FILE	= new File(System.getProperty(
			"user.home"), "pool-collision-debug.log");

	private PoolTraceLog() {
	}

	public static void log(String category, String message) {
		synchronized (LOCK) {
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(LOG_FILE, true));
				writer.write(System.currentTimeMillis() + " [" + category + "] "
						+ message);
				writer.newLine();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				if (writer != null)
					try {
						writer.close();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
	}

	public static String getPath() {
		return LOG_FILE.getAbsolutePath();
	}

	public static String fmt(YIPoint point) {
		if (point == null)
			return "null";
		return "(" + PoolMath.yintToFloat(point.a) + ","
				+ PoolMath.yintToFloat(point.b) + ")";
	}

	public static String fmt(YIVector vector) {
		if (vector == null)
			return "null";
		return "(" + PoolMath.yintToFloat(vector.a) + ","
				+ PoolMath.yintToFloat(vector.b) + ")";
	}

	public static String fmt(YPoint point) {
		if (point == null)
			return "null";
		return "(" + point.x + "," + point.y + ")";
	}

	public static String fmt(YVector vector) {
		if (vector == null)
			return "null";
		return "(" + vector.x + "," + vector.y + ")";
	}

	public static String fmt(Vel vel) {
		if (vel == null)
			return "null";
		return "{he=" + PoolMath.yintToFloat(vel.he()) + ",ke="
				+ PoolMath.yintToFloat(vel.ke()) + "}";
	}

	public static String fmt(IBall ball) {
		if (ball == null)
			return "null";
		String s = "ball#" + ball.getIndex() + " pos="
				+ fmt(new YIPoint(ball.getX(), ball.getY())) + " vel="
				+ fmt(ball.getVel()) + " slot=" + ball.getSlot();
		if (ball instanceof PoolBall) {
			PoolBall poolBall = (PoolBall) ball;
			s += " wX=" + fmt(poolBall.wX) + " h=" + fmt(poolBall.h)
					+ " sliding=" + poolBall.sliding + " break=" + poolBall.L
					+ " ballColided=" + poolBall.ballColided + " collBall="
					+ poolBall.collBall + " firstColl="
					+ fmt(poolBall.firstColl);
		}
		return s;
	}
}
