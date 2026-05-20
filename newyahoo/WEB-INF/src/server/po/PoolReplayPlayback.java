package server.po;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import data.MySQLTable;

class PoolReplayPlayback {

	static class Event {
		int		actorSeat;
		String	eventType;
		byte[]	payload;
		int		seq;
	}

	private final String	replayKey;
	private final byte[]	initialState;
	private final Vector<Event>	events;
	private int			cursor;

	private PoolReplayPlayback(String replayKey, byte[] initialState,
			Vector<Event> events) {
		this.replayKey = replayKey;
		this.initialState = initialState;
		this.events = events;
		cursor = 0;
	}

	static PoolReplayPlayback load(MySQLTable baseTable, String replayKey)
			throws SQLException {
		MySQLTable games = new MySQLTable(baseTable.getPool(),
				"pool_replay_games");
		MySQLTable eventsTable = new MySQLTable(baseTable.getPool(),
				"pool_replay_events");
		byte[] initialState = null;
		ResultSet rs = null;
		try {
			rs = games.executeQuery("SELECT initial_state FROM " + games.name
					+ " WHERE replay_key=" + MySQLTable.formatValue(replayKey));
			if (rs == null || !rs.next())
				return null;
			initialState = rs.getBytes("initial_state");
		}
		finally {
			if (rs != null)
				games.closeResultSet(rs);
		}
		Vector<Event> events = new Vector<Event>();
		try {
			rs = eventsTable.executeQuery("SELECT seq, actor_seat, event_type, payload FROM "
					+ eventsTable.name + " WHERE replay_key="
					+ MySQLTable.formatValue(replayKey) + " ORDER BY seq");
			while (rs != null && rs.next()) {
				Event event = new Event();
				event.seq = rs.getInt("seq");
				event.actorSeat = rs.getInt("actor_seat");
				event.eventType = rs.getString("event_type");
				event.payload = rs.getBytes("payload");
				events.add(event);
			}
		}
		finally {
			if (rs != null)
				eventsTable.closeResultSet(rs);
		}
		return new PoolReplayPlayback(replayKey, initialState, events);
	}

	int getCursor() {
		return cursor;
	}

	int getEventCount() {
		return events.size();
	}

	byte[] getInitialState() {
		return initialState;
	}

	String getReplayKey() {
		return replayKey;
	}

	boolean hasNext() {
		return cursor < events.size();
	}

	Event next() {
		if (!hasNext())
			return null;
		return events.elementAt(cursor++);
	}

	Event peek() {
		if (!hasNext())
			return null;
		return events.elementAt(cursor);
	}

	void reset() {
		cursor = 0;
	}
}
