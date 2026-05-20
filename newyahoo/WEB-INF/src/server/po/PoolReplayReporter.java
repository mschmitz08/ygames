package server.po;

import java.sql.Timestamp;

import data.MySQLTable;
import server.io.YahooConnectionId;
import server.yutils.YahooRoom;

class PoolReplayReporter {

	private static final int	MAX_COMMENT_LENGTH	= 500;

	private PoolReplayReporter() {
	}

	static void report(YahooRoom room, int tableNumber, YahooConnectionId id,
			String replayKey, int eventSeq, String comment) {
		if (room == null || replayKey == null || replayKey.length() == 0
				|| comment == null)
			return;
		String trimmed = comment.trim();
		if (trimmed.length() == 0)
			return;
		if (trimmed.length() > MAX_COMMENT_LENGTH)
			trimmed = trimmed.substring(0, MAX_COMMENT_LENGTH);
		MySQLTable base = room.getGameLogTable();
		MySQLTable reports = new MySQLTable(base.getPool(),
				"pool_replay_reports");
		String reporter = id != null ? id.getName() : null;
		String sql = "INSERT INTO " + reports.name
				+ " (replay_key, report_time, reporter, room_name, table_number, event_seq, comment)"
				+ " VALUES (" + MySQLTable.formatValue(replayKey) + ", "
				+ MySQLTable.formatValue(new Timestamp(System.currentTimeMillis()))
				+ ", " + MySQLTable.formatValue(reporter) + ", "
				+ MySQLTable.formatValue(room.getYport()) + ", " + tableNumber
				+ ", " + (eventSeq > 0 ? Integer.toString(eventSeq) : "NULL")
				+ ", " + MySQLTable.formatValue(trimmed) + ")";
		reports.assyncExecute(sql);
	}
}
