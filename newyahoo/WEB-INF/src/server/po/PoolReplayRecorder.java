package server.po;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.UUID;

import common.io.YData;
import common.po.Pool;
import common.po.PoolData;
import common.po.YIPoint;
import data.MySQLTable;
import server.io.YahooConnectionId;
import server.yutils.YahooRoom;

class PoolReplayRecorder {

	private final MySQLTable	games;
	private final MySQLTable	events;
	private final String	replayKey;
	private int				seq;
	private boolean			active;
	private boolean			initialStateRecorded;

	PoolReplayRecorder(YahooRoom room, int tableNumber, int gameType,
			YahooConnectionId[] players, long flags, String tableSettings) {
		MySQLTable gameLogTable = room.getGameLogTable();
		games = new MySQLTable(gameLogTable.getPool(), "pool_replay_games");
		events = new MySQLTable(gameLogTable.getPool(), "pool_replay_events");
		replayKey = UUID.randomUUID().toString();
		seq = 0;
		active = true;
		initialStateRecorded = false;

		String player0 = getPlayerName(players, 0);
		String player1 = getPlayerName(players, 1);
		String sql = "INSERT INTO " + games.name
				+ " (replay_key, room_name, table_number, game_type, started_at, player0, player1, flags, table_settings)"
				+ " VALUES (" + MySQLTable.formatValue(replayKey) + ", "
				+ MySQLTable.formatValue(room.getYport()) + ", " + tableNumber
				+ ", " + gameType + ", "
				+ MySQLTable.formatValue(new Timestamp(System.currentTimeMillis()))
				+ ", " + MySQLTable.formatValue(player0) + ", "
				+ MySQLTable.formatValue(player1) + ", " + flags + ", "
				+ MySQLTable.formatValue(tableSettings) + ")";
		games.execute(sql);
	}

	public String getReplayKey() {
		return replayKey;
	}

	public int getSeq() {
		return seq;
	}

	void finish(YData stopData, int[] result) {
		if (!active)
			return;
		byte[] stopBytes = toBytes(stopData);
		byte[] resultBytes = intsToBytes(result);
		String sql = "UPDATE " + games.name + " SET ended_at="
				+ MySQLTable.formatValue(new Timestamp(System.currentTimeMillis()))
				+ ", stop_data=" + blobLiteral(stopBytes) + ", result="
				+ blobLiteral(resultBytes) + " WHERE replay_key="
				+ MySQLTable.formatValue(replayKey);
		games.assyncExecute(sql);
		active = false;
	}

	void recordChangeBall(int actorSeat, int index, int x, int y) {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream output = new DataOutputStream(bytes);
			output.writeInt(index);
			output.writeInt(x);
			output.writeInt(y);
			recordEvent(actorSeat, "CHANGE_BALL", bytes.toByteArray());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	void recordInitialState(Pool pool) {
		if (!active || initialStateRecorded || pool == null)
			return;
		byte[] bytes = toBytes(pool);
		if (bytes == null || bytes.length == 0)
			return;
		String sql = "UPDATE " + games.name + " SET initial_state="
				+ blobLiteral(bytes) + " WHERE replay_key="
				+ MySQLTable.formatValue(replayKey);
		games.execute(sql);
		initialStateRecorded = true;
	}

	void recordPoolData(int actorSeat, String eventType, PoolData data) {
		recordEvent(actorSeat, eventType, toBytes(data));
	}

	void recordReset(int actorSeat) {
		recordSingleInt(actorSeat, "RESET", actorSeat);
	}

	void recordSelectType(int actorSeat, int type) {
		recordSingleInt(actorSeat, "SELECT_TYPE", type);
	}

	void recordSetSlot(int actorSeat, int slotIndex) {
		recordSingleInt(actorSeat, "SET_SLOT", slotIndex);
	}

	void recordStrike(int actorSeat, int turnNum, int index, int collBall,
			YIPoint cueDist, YIPoint englishDist, YIPoint firstColl) {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream output = new DataOutputStream(bytes);
			output.writeInt(turnNum);
			output.writeInt(index);
			output.writeByte(collBall);
			cueDist.write(output);
			englishDist.write(output);
			firstColl.write(output);
			recordEvent(actorSeat, "STRIKE", bytes.toByteArray());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String blobLiteral(byte[] bytes) {
		if (bytes == null)
			return "NULL";
		return "X'" + toHex(bytes) + "'";
	}

	private String eventTypeValue(String eventType) {
		return MySQLTable.formatValue(eventType == null ? "" : eventType);
	}

	private String getPlayerName(YahooConnectionId[] players, int index) {
		if (players == null || index < 0 || index >= players.length
				|| players[index] == null)
			return null;
		return players[index].getName();
	}

	private byte[] intsToBytes(int[] values) {
		if (values == null)
			return null;
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream output = new DataOutputStream(bytes);
			output.writeInt(values.length);
			for (int value : values)
				output.writeInt(value);
			return bytes.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void recordEvent(int actorSeat, String eventType, byte[] payload) {
		if (!active)
			return;
		seq++;
		String sql = "INSERT INTO " + events.name
				+ " (replay_key, seq, event_time, actor_seat, event_type, payload)"
				+ " VALUES (" + MySQLTable.formatValue(replayKey) + ", " + seq
				+ ", " + MySQLTable.formatValue(new Timestamp(System.currentTimeMillis()))
				+ ", " + actorSeat + ", " + eventTypeValue(eventType) + ", "
				+ blobLiteral(payload) + ")";
		events.assyncExecute(sql);
	}

	private void recordSingleInt(int actorSeat, String eventType, int value) {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream output = new DataOutputStream(bytes);
			output.writeInt(value);
			recordEvent(actorSeat, eventType, bytes.toByteArray());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private byte[] toBytes(YData data) {
		if (data == null)
			return null;
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream output = new DataOutputStream(bytes);
			data.write(output);
			return bytes.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		catch (RuntimeException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String toHex(byte[] bytes) {
		char[] chars = new char[bytes.length * 2];
		char[] hex = "0123456789ABCDEF".toCharArray();
		for (int i = 0; i < bytes.length; i++) {
			int value = bytes[i] & 0xff;
			chars[i * 2] = hex[value >>> 4];
			chars[i * 2 + 1] = hex[value & 0x0f];
		}
		return new String(chars);
	}
}
