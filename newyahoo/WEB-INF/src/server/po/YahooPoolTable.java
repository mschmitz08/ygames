package server.po;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

import server.io.YahooConnectionId;
import server.yutils.YahooRoom;
import server.yutils.YahooTable;

import common.io.YData;
import common.po.IBall;
import common.po.Pool;
import common.po.PoolData;
import common.po.PoolHandler;
import common.po.Vel;
import common.po.YIPoint;
import common.po.YIVector;
import common.po.YRectangle;
import common.utils.ReverseClock;
import common.yutils.Game;

public class YahooPoolTable extends YahooTable implements PoolHandler {

	private static final long	POOL_ENGINE_TIMER_INTERVAL	= 15L;
	public int			turnNum	= 0;
	public Pool			pool;
	public YIPoint		cueDist;
	public YIPoint		englishDist;
	public YIPoint		firstColl;
	public int			collBall;
	public ReverseClock	poolEngineTimer;
	PoolData			J;
	Cue					cue;
	English				english;
	PoolReplayRecorder	replayRecorder;
	PoolReplayPlayback	replayPlayback;
	long				lastReplayShotAt;

	public YahooPoolTable(YahooRoom room, int number) {
		super(room, number);
		cueDist = new YIPoint();
		englishDist = new YIPoint();
		firstColl = new YIPoint();
		collBall = -1;
		J = new PoolData();
		cue = new Cue();
		english = new English();
		replayRecorder = null;
		replayPlayback = null;
	}


	private void broadcastFullGame() {
		ids.readLock();
		try {
			for (int i = 0; i < ids.size(); i++)
				updateGame(ids.elementAt(i), pool);
		}
		finally {
			ids.readUnlock();
		}
	}

	private void handleBugReport(YahooConnectionId id, String comment) {
		String replayKey = replayRecorder != null ? replayRecorder.getReplayKey()
				: null;
		int eventSeq = replayRecorder != null ? replayRecorder.getSeq() : 0;
		if (replayKey == null && replayPlayback != null)
			replayKey = replayPlayback.getReplayKey();
		if (replayKey == null) {
			room.alert(id, "No active replay is available for this bug report.");
			return;
		}
		PoolReplayReporter.report(room, number, id, replayKey, eventSeq, comment);
		doTableLog("Bug report saved for replay " + replayKey + ".");
	}
	private void handleReplayCommand(YahooConnectionId id, String command) {
		if (command == null)
			return;
		String trimmed = command.trim();
		String lower = trimmed.toLowerCase();
		if (lower.startsWith("bug report ")) {
			handleBugReport(id, trimmed.substring("bug report ".length()));
			return;
		}
		if (!lower.startsWith("replay"))
			return;
		if (!"test".equalsIgnoreCase(room.getYport())) {
			room.alert(id, "Replay commands are only available in the test room.");
			return;
		}
		if (!isHost(id)) {
			room.alert(id, "Only the table host can control replay playback.");
			return;
		}
		try {
			if (lower.equals("replay next") || lower.equals("replay step")) {
				replayStep(id);
				return;
			}
			if (lower.equals("replay reset")) {
				replayLoadInitial(id);
				return;
			}
			String replayKey = trimmed.substring("replay".length()).trim();
			if (replayKey.toLowerCase().startsWith("load "))
				replayKey = replayKey.substring(5).trim();
			if (replayKey.length() == 0) {
				room.alert(id, "Use: replay load <replay_key>, replay next, or replay reset.");
				return;
			}
			replayPlayback = PoolReplayPlayback.load(room.getGameLogTable(), replayKey);
			if (replayPlayback == null) {
				room.alert(id, "Replay not found: " + replayKey);
				return;
			}
			replayLoadInitial(id);
			doTableLog("Replay loaded: " + replayKey + " (" + replayPlayback.getEventCount()
					+ " events). Use replay next to step.");
		}
		catch (Throwable e) {
			e.printStackTrace();
			room.alert(id, "Replay command failed: " + e.getMessage());
		}
	}

	private void replayLoadInitial(YahooConnectionId id) throws IOException {
		if (replayPlayback == null) {
			room.alert(id, "No replay is loaded.");
			return;
		}
		byte[] initialState = replayPlayback.getInitialState();
		if (initialState == null || initialState.length == 0) {
			room.alert(id, "Replay has no initial state.");
			return;
		}
		if (poolEngineTimer != null)
			stopPoolEngineTimer();
		pool.read(new DataInputStream(new ByteArrayInputStream(initialState)));
		replayPlayback.reset();
		lastReplayShotAt = 0L;
		replayRecorder = null;
		broadcastFullGame();
		doTableLog("Replay reset to initial state: " + replayPlayback.getReplayKey());
	}

	private void applyReplayTurnStat(PoolReplayPlayback.Event event)
			throws IOException {
		DataInputStream input = new DataInputStream(new ByteArrayInputStream(
				event.payload != null ? event.payload : new byte[0]));
		PoolData data = new PoolData();
		data.read(input);
		if (poolEngineTimer != null)
			stopPoolEngineTimer();
		pool.doNotifyTStat(data, false);
		ids.readLock();
		try {
			for (int i = 0; i < ids.size(); i++)
				updategame(ids.elementAt(i), data);
		}
		finally {
			ids.readUnlock();
		}
	}

	private void replayStep(YahooConnectionId id) throws IOException {
		if (replayPlayback == null) {
			room.alert(id, "No replay is loaded.");
			return;
		}
		long now = System.currentTimeMillis();
		if (lastReplayShotAt > 0L && now - lastReplayShotAt < 5000L) {
			doTableLog("Replay shot is still settling; wait a moment before replay next.");
			return;
		}
		PoolReplayPlayback.Event event = replayPlayback.next();
		if (event == null) {
			doTableLog("Replay is already at the end.");
			return;
		}
		DataInputStream input = new DataInputStream(new ByteArrayInputStream(
				event.payload != null ? event.payload : new byte[0]));
		if ("STRIKE".equals(event.eventType)) {
			input.readInt();
			int index = input.readInt();
			int replayCollBall = input.readByte();
			YIPoint replayCueDist = new YIPoint();
			YIPoint replayEnglishDist = new YIPoint();
			YIPoint replayFirstColl = new YIPoint();
			replayCueDist.read(input);
			replayEnglishDist.read(input);
			replayFirstColl.read(input);
			if (pool.doStrike(event.actorSeat, index, replayCueDist,
					replayEnglishDist, replayFirstColl, replayCollBall)) {
				ids.readLock();
				try {
					for (int i = 0; i < ids.size(); i++)
						strike(ids.elementAt(i), event.actorSeat, index,
								replayCollBall, replayCueDist, replayEnglishDist,
								replayFirstColl);
				}
				finally {
					ids.readUnlock();
				}
				lastReplayShotAt = System.currentTimeMillis();
				doTableLog("Replay shot " + event.seq + "/"
						+ replayPlayback.getEventCount() + ": STRIKE");
				PoolReplayPlayback.Event turnStatEvent = replayPlayback.peek();
				if (turnStatEvent != null
						&& "TURN_STAT".equals(turnStatEvent.eventType)) {
					turnStatEvent = replayPlayback.next();
					applyReplayTurnStat(turnStatEvent);
					doTableLog("Replay shot " + event.seq + " settled with event "
							+ turnStatEvent.seq + "/"
							+ replayPlayback.getEventCount() + ".");
				}
				return;
			}
		}
		else if ("CHANGE_BALL".equals(event.eventType)) {
			int index = input.readInt();
			int x = input.readInt();
			int y = input.readInt();
			pool.doUpdatePB(event.actorSeat, index, x, y);
			ids.readLock();
			try {
				for (int i = 0; i < ids.size(); i++)
					changeBall(ids.elementAt(i), index, x, y);
			}
			finally {
				ids.readUnlock();
			}
		}
		else if ("SET_SLOT".equals(event.eventType)) {
			int slot = input.readInt();
			pool.doSetSlot(event.actorSeat, slot);
			ids.readLock();
			try {
				for (int i = 0; i < ids.size(); i++)
					setSlot(ids.elementAt(i), slot);
			}
			finally {
				ids.readUnlock();
			}
		}
		else if ("SELECT_TYPE".equals(event.eventType)) {
			int type = input.readInt();
			pool.selectType(event.actorSeat, type);
			ids.readLock();
			try {
				for (int i = 0; i < ids.size(); i++)
					selectType(ids.elementAt(i), type);
			}
			finally {
				ids.readUnlock();
			}
		}
		else if ("RESET".equals(event.eventType)) {
			pool.actionReset(event.actorSeat);
			ids.readLock();
			try {
				for (int i = 0; i < ids.size(); i++)
					reset(ids.elementAt(i), event.actorSeat);
			}
			finally {
				ids.readUnlock();
			}
		}
		else if ("TURN_STAT".equals(event.eventType)) {
			applyReplayTurnStat(event);
		}
		else if ("TIME_EMPTY".equals(event.eventType)) {
			PoolData data = new PoolData();
			data.read(input);
			pool.doNotifyTELAPS(data);
			broadcastFullGame();
		}
		doTableLog("Replay event " + event.seq + "/" + replayPlayback.getEventCount()
				+ ": " + event.eventType);
	}
	public void Ad() {
		// TODO Auto-generated method stub

	}

	public void Bd(int i) {
		// TODO Auto-generated method stub

	}

	private void changeBall(YahooConnectionId id, int i0, int i1, int i2) {
		synchronized (id) {
			writeHeader(id);
			id.write('\uFF9A');
			id.writeInt(i0);
			id.writeInt(i1);
			id.writeInt(i2);
			id.flush();
		}
	}

	private void changeCue(YahooConnectionId id, int sitIndex, Cue cue) {
		try {
			synchronized (id) {
				writeHeader(id);
				id.write('\uFF80');
				id.writeByte(sitIndex);
				cue.write(id);
				id.flush();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			id.close();
		}
	}

	private void changeEnglish(YahooConnectionId id, int sitIndex,
			English english) {
		try {
			synchronized (id) {
				writeHeader(id);
				id.write('\uFF81');
				id.writeByte(sitIndex);
				english.write(id);
				id.flush();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			id.close();
		}
	}

	@Override
	public void close() {
		cueDist = null;
		englishDist = null;
		firstColl = null;
		if (poolEngineTimer != null) {
			poolEngineTimer.close();
			poolEngineTimer = null;
		}
		if (pool != null) {
			pool.close();
			pool = null;
		}
		J = null;
		cue = null;
		english = null;
		replayRecorder = null;
		replayPlayback = null;
		super.close();
	}

	@Override
	protected Game createGame() {
		pool = new Pool();
		return pool;
	}

	private void doChangeBall(YahooConnectionId id, int i0, int i1, int i2) {
		pool.doUpdatePB(pool.m_turn, i0, i1, i2);
		if (replayRecorder != null)
			replayRecorder.recordChangeBall(pool.m_turn, i0, i1, i2);
		ids.readLock();
		try {
			for (int i = 0; i < ids.size(); i++)
				changeBall(ids.elementAt(i), i0, i1, i2);
		}
		finally {
			ids.readUnlock();
		}
	}

	private void doChangeCue(YahooConnectionId id, int sitIndex, Cue cue) {
		pool.doUpdateCue(sitIndex);
		ids.readLock();
		try {
			for (int i = 0; i < ids.size(); i++)
				changeCue(ids.elementAt(i), sitIndex, cue);
		}
		finally {
			ids.readUnlock();
		}
	}

	private void doChangeEnglish(YahooConnectionId id, int sitIndex,
			English english) {
		pool.doUpdateEnglish(sitIndex);
		ids.readLock();
		try {
			for (int i = 0; i < ids.size(); i++)
				changeEnglish(ids.elementAt(i), sitIndex, english);
		}
		finally {
			ids.readUnlock();
		}
	}

	private void doReset(YahooConnectionId id, int sitIndex) {
		pool.actionReset(sitIndex);
		if (replayRecorder != null)
			replayRecorder.recordReset(sitIndex);
		ids.readLock();
		try {
			for (int i = 0; i < ids.size(); i++)
				reset(ids.elementAt(i), sitIndex);
		}
		finally {
			ids.readUnlock();
		}
	}

	private void doSelectType(YahooConnectionId id, int type) {
		pool.selectType(pool.m_turn, type);
		if (replayRecorder != null)
			replayRecorder.recordSelectType(pool.m_turn, type);
		ids.readLock();
		try {
			for (int i = 0; i < ids.size(); i++)
				selectType(ids.elementAt(i), type);
		}
		finally {
			ids.readUnlock();
		}
	}

	private void doSetSlot(YahooConnectionId id, int slotIndex) {
		pool.doSetSlot(pool.m_turn, slotIndex);
		if (replayRecorder != null)
			replayRecorder.recordSetSlot(pool.m_turn, slotIndex);
		ids.readLock();
		try {
			for (int i = 0; i < ids.size(); i++)
				setSlot(ids.elementAt(i), slotIndex);
		}
		finally {
			ids.readUnlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see server.yutils.YahooTable#doStart()
	 */
	@Override
	protected void doStart() {
		for (int i = 0; i < sits.length; i++)
			startedPlayers[i] = sits[i];
	}

	private void doStrike(YahooConnectionId id, int sitIndex, int index,
			int collBall, YIPoint cueDist, YIPoint englishDist,
			YIPoint firstColl) throws IOException {
		if (!pool.doStrike(sitIndex, index, cueDist, englishDist, firstColl,
				collBall))
			return;
		if (replayRecorder != null)
			replayRecorder.recordStrike(sitIndex, turnNum, index, collBall,
					cueDist, englishDist, firstColl);
		startPoolEngineTimer();

		ids.readLock();
		try {
			for (int i = 0; i < ids.size(); i++)
				strike(ids.elementAt(i), sitIndex, index, collBall, cueDist,
						englishDist, firstColl);
		}
		finally {
			ids.readUnlock();
		}
	}

	private void doTimeEmpty(YahooConnectionId id, PoolData J)
			throws IOException {
		J.reset();
		logState("notifyTELAPS");
		pool.doNotifyTELAPS(J);
		if (replayRecorder != null)
			replayRecorder.recordPoolData(pool.m_turn, "TIME_EMPTY", J);

		ids.readLock();
		try {
			for (int i = 0; i < ids.size(); i++)
				timeEmpty(ids.elementAt(i), J);
		}
		finally {
			ids.readUnlock();
		}
	}

	private void doUpdateGame() throws IOException {
		int actorSeat = pool.m_turn;
		J = pool.tj();
		if (replayRecorder != null)
			replayRecorder.recordPoolData(actorSeat, "TURN_STAT", J);
		pool.doNotifyTStat(J, false);

		ids.readLock();
		try {
			for (int i = 0; i < ids.size(); i++) {
				updategame(ids.elementAt(i), J);
				// changeCue(ids.elementAt(i), pool.m_turn, pool.cue);
				// changeEnglish(ids.elementAt(i), pool.m_turn, pool.english);
			}
		}
		finally {
			ids.readUnlock();
		}
	}

	@Override
	protected void doUpdateGame(YahooConnectionId id) {
		super.doUpdateGame(id);
		if (pool != null && pool.isRunning() && pool.getCurrentState() == 0
				&& pool.m_turn >= 0 && pool.m_turn < sits.length) {
			changeCue(id, pool.m_turn, cue);
			changeEnglish(id, pool.m_turn, english);
		}
	}

	public Vector<IBall> getBallInPlayArea() {
		return pool.getBallInPlayArea();
	}

	public YRectangle getBounceArea() {
		// TODO Auto-generated method stub
		return null;
	}

	public YIPoint getCenterPoint() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see server.yutils.YahooTable#getGameId()
	 */
	@Override
	public int getGameId() {
		if (properties != null) {
			if (properties.containsKey("eightBall") || properties.containsKey("eightBallGame"))
				return 0;
			else if (properties.containsKey("nineBall") || properties.containsKey("nineBallGame"))
				return 1;
			else if (properties.containsKey("training"))
				return 2;
		}
		return 0;
	}

	public YRectangle getInArea() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getLinearFriction() {
		// TODO Auto-generated method stub
		return 0;
	}

	public YRectangle getPlayArea() {
		// TODO Auto-generated method stub
		return null;
	}

	public YRectangle getPocketArea() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getRotationFriction() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getSideRotationFriction() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSitCount() {
		if (properties != null
				&& (properties.containsKey("training") || properties
						.containsKey("automat")))
			return 1;
		return 2;
	}

	public void handleColl(int i) {
		// TODO Auto-generated method stub

	}

	public void handleFirtsColl(IBall ball) {
		// TODO Auto-generated method stub

	}

	public void handleIterate() {
		// TODO Auto-generated method stub

	}

	public void handleSetPos(int i, int j, int k, int l, YIVector _pcls48,
			Vel _pcls33) {
		// TODO Auto-generated method stub

	}

	public void handleShiftFromIntersect() {
		// TODO Auto-generated method stub

	}

	public void handleStopMoving() {
		try {
			doUpdateGame();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			stopPoolEngineTimer();
		}
	}

	@Override
	public void handleStopTick() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see common.po.PoolHandler#handleUpdateCue(int)
	 */
	@Override
	public void handleUpdateCue(int i) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see common.po.PoolHandler#handleUpdateEnglish(int)
	 */
	@Override
	public void handleUpdateEnglish(int i) {
		// TODO Auto-generated method stub

	}


	@Override
	public void handleStart() {
		super.handleStart();
		replayRecorder = new PoolReplayRecorder(room, number, getGameId(),
				startedPlayers, currGameLogEntry != null ? currGameLogEntry
						.getFlags() : 0L);
	}

	@Override
	public void handleStop(YData data) {
		if (replayRecorder != null) {
			replayRecorder.finish(data, getWonTurn(data));
			replayRecorder = null;
		}
		replayPlayback = null;
		super.handleStop(data);
	}

	public void kd(int i) {
		// TODO Auto-generated method stub

	}

	public void ld(int i) {
		// TODO Auto-generated method stub

	}

	public void logState(String s) {
		// TODO Auto-generated method stub

	}

	public void md(int i) {
		// TODO Auto-generated method stub

	}

	public void nd(YData _pcls111) {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void parseData(YahooConnectionId id, int byte0,
			DataInputStream input) throws IOException {
		int index;
		switch (byte0) {
		case -128: // 80 change cue
			cue.read(input);
			for (int i = 0; i < sits.length; i++)
				if (sits[i] != null && sits[i].equals(id)) {
					doChangeCue(id, i, cue);
					break;
				}
			break;
		case -127: // 81 change english
			english.read(input);
			for (int i = 0; i < sits.length; i++)
				if (sits[i] != null && sits[i].equals(id)) {
					doChangeEnglish(id, i, english);
					break;
				}
			break;
		case -126: // 82 strike
			turnNum = input.readInt();
			index = input.readInt();
			collBall = input.readByte();
			cueDist.read(input);
			englishDist.read(input);
			firstColl.read(input);
			for (int i = 0; i < sits.length; i++)
				if (sits[i] != null && sits[i].equals(id)) {
					doStrike(id, i, index, collBall, cueDist, englishDist,
							firstColl);
					break;
				}
			break;
		case -125: // 83 reset
			for (int i = 0; i < sits.length; i++)
				if (sits[i] != null && sits[i].equals(id)) {
					doReset(id, i);
					break;
				}
			break;
		case -123: // 85 update game
			turnNum = input.readInt();
			J.read(input);
			break;
		case -114: // 8E change slot
			int slotIndex = input.readInt();
			doSetSlot(id, slotIndex);
			break;
		case -111: // 91 time empty
			turnNum = input.readInt();
			J = pool.tj();
			doTimeEmpty(id, J);
			break;
		case -107: // 95 select type
			int type = input.readInt();
			doSelectType(id, type);
			break;
		case -102: // 9A change ball
			index = input.readInt();
			int x = input.readInt();
			int y = input.readInt();
			doChangeBall(id, index, x, y);
			break;
		case -97: // 9F replay/test command
			handleReplayCommand(id, input.readUTF());
			break;
		default:
			super.parseData(id, byte0, input);
		}
	}

	public void qd(IBall _pcls124) {
		// TODO Auto-generated method stub

	}

	public void rd() {
		stopPoolEngineTimer();
		if (pool != null && pool.poolEngine != null
				&& pool.poolEngine.movingExist())
			startPoolEngineTimer();
		// TODO verificar se não existe mais nada a implementar aqui
	}

	private void startPoolEngineTimer() {
		if (pool == null || pool.poolEngine == null)
			return;
		stopPoolEngineTimer();
		poolEngineTimer = new ReverseClock(pool.poolEngine,
				POOL_ENGINE_TIMER_INTERVAL, true);
		poolEngineTimer.go();
	}

	private void stopPoolEngineTimer() {
		if (poolEngineTimer != null) {
			poolEngineTimer.close();
			poolEngineTimer = null;
		}
	}

	private void reset(YahooConnectionId id, int sitIndex) {
		synchronized (id) {
			writeHeader(id);
			id.write('\uFF83');
			id.writeByte(sitIndex);
			id.flush();
		}
	}

	public void Sc() {
		// TODO Auto-generated method stub

	}

	public void selectSlot(int i) {
		// TODO Auto-generated method stub

	}

	private void selectType(YahooConnectionId id, int type) {
		synchronized (id) {
			writeHeader(id);
			id.write('\uFF86');
			id.writeInt(type);
			id.flush();
		}
	}

	private void setSlot(YahooConnectionId id, int slotIndex) {
		synchronized (id) {
			writeHeader(id);
			id.write('\uFF8D');
			id.writeInt(slotIndex);
			id.flush();
		}
	}

	private void strike(YahooConnectionId id, int sitIndex, int index,
			int collBall, YIPoint cueDist, YIPoint englishDist,
			YIPoint firstColl) throws IOException {
		synchronized (id) {
			writeHeader(id);
			id.write('\uFF82');
			id.writeByte(sitIndex);
			id.writeInt(index);
			id.writeByte(collBall);
			cueDist.write(id);
			englishDist.write(id);
			firstColl.write(id);
			id.flush();
		}
	}

	public void td() {
		// TODO Auto-generated method stub

	}

	private void timeEmpty(YahooConnectionId id, PoolData J) throws IOException {
		synchronized (id) {
			writeHeader(id);
			id.write('\uFF90');
			J.write(id);
			id.flush();
		}
	}

	private void updategame(YahooConnectionId id, PoolData J)
			throws IOException {
		synchronized (id) {
			writeHeader(id);
			id.write('\uFF9B');
			J.write(id);
			id.flush();
		}
	}

	public String Xc(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public void xd(int i) {
		// TODO Auto-generated method stub

	}

	public void zd(int i, boolean flag) {
		if (replayRecorder != null)
			replayRecorder.recordInitialState(pool);

	}

}
