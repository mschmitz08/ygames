// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) fieldsfirst

package y.po;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import y.controls.YahooComponent;
import y.controls.YahooComboBox;
import y.controls.YahooControl;
import y.controls.YahooLabel;
import y.utils.Formater;
import y.utils.TimerEngine;
import y.utils.TimerEntry;
import y.utils.TimerHandler;
import y.ycontrols.SaveCancel;
import y.ycontrols.TableControlContainer;
import y.yutils.YahooGamesTable;

import common.io.YData;
import common.po.EightBallSetup;
import common.po.IBall;
import common.po.NineBallSetup;
import common.po.Obstacle;
import common.po.Pool;
import common.po.PoolBall;
import common.po.PoolData;
import common.po.PoolHandler;
import common.po.PoolMath;
import common.po.Slot;
import common.po.Vel;
import common.po.YIPoint;
import common.po.YIVector;
import common.po.YRectangle;
import common.po.YVector;
import common.utils.ByteArrayData;

// Referenced classes of package y.po:
// _cls99, _cls145, _cls86, _cls50,
// _cls56, _cls174, _cls3, _cls170,
// _cls58, _cls175, _cls23, _cls0,
// _cls24, _cls106, _cls40, _cls105,
// _cls57, _cls89, _cls121, _cls153,
// _cls124, _cls138, _cls129, _cls100,
// _cls6, _cls131, _cls169, _cls108,
// _cls16, _cls46, _cls48, _cls168,
// _cls35, _cls78, _cls79, _cls87,
// _cls177, _cls33, _cls111

public class YahooPoolTable extends YahooGamesTable implements PoolHandler,
		TimerHandler, PoolAreaHandler {

	private static final int	BREAK_POWER_JITTER	= 10;
	private static final int	MAX_CUE_POWER		= 120;
	private static final Random BREAK_RANDOM		= new Random();
	private static final String[] TABLE_COLOR_NAMES = { "Classic", "Aqua",
			"Apricot", "Azure", "Berry", "Black", "Blush", "Bronze",
			"Cerise", "Charcoal", "Cherry", "Copper", "Coral", "Crimson",
			"Denim", "Emerald", "Forest", "Fuchsia", "Gold", "Indigo",
			"Ivory", "Jade", "Lagoon", "Lavender", "Lemon", "Lilac",
			"Lime", "Magenta", "Maroon", "Mauve", "Mint", "Mocha",
			"Navy", "Olive", "Orange", "Orchid", "Peach", "Peri",
			"Pine", "Plum", "Purple", "Rose", "Rosewd", "Ruby",
			"Salmon", "Sand", "Seafoam", "Sienna", "Silver", "Sky",
			"Slate", "Sun", "Tan", "Teal", "Tomato", "Turq",
			"Violet", "Wheat", "Custom" };
	private static final Color[] TABLE_COLOR_VALUES = {
			new Color(45, 109, 43), new Color(0, 168, 168),
			new Color(238, 176, 112), new Color(96, 168, 224),
			new Color(130, 52, 108), new Color(18, 18, 18),
			new Color(224, 170, 196), new Color(140, 104, 46),
			new Color(214, 56, 118), new Color(54, 59, 64),
			new Color(168, 36, 48), new Color(150, 86, 48),
			new Color(205, 92, 92), new Color(163, 28, 48),
			new Color(42, 92, 132), new Color(33, 136, 79),
			new Color(42, 92, 42), new Color(208, 60, 156),
			new Color(166, 124, 0), new Color(57, 62, 151),
			new Color(214, 214, 194), new Color(56, 142, 96),
			new Color(0, 102, 153), new Color(123, 104, 238),
			new Color(208, 196, 72), new Color(170, 132, 214),
			new Color(75, 151, 32), new Color(178, 40, 123),
			new Color(108, 28, 44), new Color(145, 95, 160),
			new Color(80, 168, 112), new Color(112, 84, 62),
			new Color(28, 44, 92), new Color(78, 102, 35),
			new Color(204, 102, 0), new Color(186, 85, 211),
			new Color(228, 164, 116), new Color(102, 153, 255),
			new Color(44, 92, 52), new Color(110, 38, 98),
			new Color(92, 39, 143), new Color(196, 92, 132),
			new Color(101, 55, 55), new Color(145, 24, 52),
			new Color(219, 112, 147), new Color(194, 178, 128),
			new Color(52, 168, 128), new Color(132, 77, 49),
			new Color(142, 142, 142), new Color(42, 135, 218),
			new Color(96, 108, 132), new Color(184, 158, 0),
			new Color(210, 180, 140), new Color(0, 102, 102),
			new Color(214, 85, 56), new Color(35, 155, 166),
			new Color(118, 74, 188), new Color(222, 184, 135), null };

	public Pool		pool;
	PoolArea		poolArea;
	PoolAimer		poolAimer;
	TimerEngine		timerHandler;
	TimerEntry		poolTimer;
	TimerEntry		poolAimerTimer;
	TimerEntry		thisTimer;
	private long	cueTime;
	private long	updateTime;
	boolean			k;
	boolean			ypt_l;
	boolean			m;
	boolean			n;
	boolean			ypt_o;
	boolean			p;
	boolean			ypt_q;
	int				ypt_r;
	int				ypt_s;
	int				t;
	int				u;
	int				v;
	String			cueSound;
	String			ballSound;
	String			break_longSound;
	String			break_shortSound;
	String			pocketSound;
	String			cushionSound;
	String			ball_softSound;
	private int		update;
	public boolean	ypt_E;
	YIPoint			cueDist;
	YIPoint			englishDist;
	YIPoint			firstColl;
	YVector			ypt_I;
	PoolData		J;
	String			state;
	YahooComboBox	cmbGhostStyle;
	YahooComboBox	cmbTableColor;
	YahooComponent	customTableColorPanel;
	TableColorEditor	tableColorEditor;
	Color			currentTableColor;
	boolean			pendingCueSnapshotRestore;
	boolean			pendingEnglishSnapshotRestore;
	boolean			preserveCueSnapshotOnRestore;
	String[]		startedSeatNames;
	boolean			startedSeatNamesCaptured;

	public YahooPoolTable() {
		cueTime = 0L;
		updateTime = 0L;
		k = true;
		ypt_l = true;
		m = false;
		n = true;
		ypt_o = false;
		p = false;
		ypt_q = false;
		ypt_r = 0;
		ypt_s = 0;
		t = 0;
		u = 0;
		v = 0;
		cueSound = "yog/resource/pool/cue.au";
		ballSound = "yog/resource/pool/ball.au";
		break_longSound = "yog/resource/pool/break-long.au";
		break_shortSound = "yog/resource/pool/break-short.au";
		pocketSound = "yog/resource/pool/pocket.au";
		cushionSound = "yog/resource/pool/cushion.au";
		ball_softSound = "yog/resource/pool/ball-soft.au";
		update = 1;
		cueDist = new YIPoint();
		englishDist = new YIPoint();
		firstColl = new YIPoint();
		ypt_I = new YVector();
		J = new PoolData();
		startedSeatNames = null;
		startedSeatNamesCaptured = false;
		state = "";
		currentTableColor = TABLE_COLOR_VALUES[0];
		pendingCueSnapshotRestore = false;
		pendingEnglishSnapshotRestore = false;
		preserveCueSnapshotOnRestore = false;
		addSitParser(new SaveCancel(this));
	}

	public void actionChangeCue(byte byte1, DataInputStream datainputstream)
			throws IOException {
		poolArea.cueSprite.read(datainputstream);
		pool.doUpdateCue(byte1);
	}

	public void actionChangeCue(byte byte1, int i1, float f1, float f2,
			float f3, float f4, int power) {
		poolArea.cueSprite.cueTip.setCoords(f1, f2);
		poolArea.cueSprite.cueTipOffset.setCoords(f3, f4);
		poolArea.cueSprite.power = power;
	}

	public void actionChangeEnglish(byte byte2, DataInputStream datainputstream)
			throws IOException {
		poolArea.english.read(datainputstream);
		pool.doUpdateEnglish(byte2);
	}

	public void actionChangePoolBall(int k1, int j2, int l2) {
		logState("updatePB " + k1 + " " + j2 + " " + l2);
		pool.doUpdatePB(pool.m_turn, k1, j2, l2);
	}

	public void actionStrike() {

	}

	@Override
	public void activateControls(int i1) {
		super.activateControls(i1);
		if (pool.training)
			poolArea._c();
		if (pool.isRunning()) {
			poolArea.hideCue();
			poolArea.deactivate();
		}
	}

	public void Ad() {
		Hc();
		if (poolAimer != null)
			poolArea.poolAimer.ls();
		poolArea.lblTime.setCaption(Formater.formatTimer(0L));
		poolArea.hideCue();
		poolArea.deactivate();
		if (poolAimer != null)
			poolAimer.wb();
		poolArea.releasePowerbar();
		if (poolAimer != null)
			poolAimer.wb();
	}

	public void Bd(int i1) {
		poolArea.vc(i1);
	}

	@Override
	public void cd(YahooControl _pcls79, YahooComponent _pcls78) {
		_pcls79.addChildObject(_pcls78, 18, 0, 0, 1, 1, 0, 0);
	}

	@Override
	public void close() {
		if (poolTimer != null) {
			timerHandler.remove(poolTimer);
			poolTimer = null;
		}
		if (poolAimerTimer != null) {
			timerHandler.remove(poolAimerTimer);
			poolAimerTimer = null;
		}
		if (thisTimer != null) {
			timerHandler.remove(thisTimer);
			thisTimer = null;
		}
		pool = null;

		super.close();
	}

	@Override
	public void createArea() {
		update = ((YahooPool) getApplet()).update;
		pool = (Pool) getGame();
		poolArea = new PoolArea(this);
		poolAimer = poolArea.getPoolAimer();
		if (isSmallWidows()) {
			addYahooObject(poolArea.english);
			poolArea.english.setVisible(true);
		}
		if (pool.getSetup() instanceof EightBallSetup)
			addObject(new YahooLabel(getApplet().lookupString(0x665016b5)), -1);
		else if (pool.getSetup() instanceof NineBallSetup)
			addObject(new YahooLabel(getApplet().lookupString(0x665016b6)), -1);
		else
			addObject(new YahooLabel(getApplet().lookupString(0x665016b7)), -1);
		super.createArea();
		poolArea.setBackColor();
		applyCurrentTableColor();
		pool.getSetup().getBallCount();
		timerHandler = getTimerHandler();
		poolTimer = timerHandler.add(pool.getPoolEngine(), 15); // 15
		poolAimerTimer = timerHandler.add(poolAimer, 200);
		thisTimer = timerHandler.add(this, 15); // 15
		long l1 = 0xfffffffffe386f04L;
		long l2 = 0xfffffe386f040000L;
		l1 <<= 16;
		long bshift = l1;
		int j1 = 0xfffe9b97;
		long l4 = 0xfffffffffffe9b97L;
		long l5 = l1 / j1;
		long l6 = l1 / l4;
		long l7 = l2 / j1;
		long l8 = l2 / l4;
		String s1 = "";
		s1 = s1 + l5;
		s1 = s1 + ",bshift=" + bshift + " LMAX=" + 0x7fffffffffffffffL
				+ " LMIN=" + 0x8000000000000000L + " long/long=" + l6 + " "
				+ l7 + " " + l8;
		s1 = s1 + System.getProperty("java.version") + " "
				+ System.getProperty("java.vendor");
		send('\uFF9F', s1);
	}

	@Override
	public TableControlContainer createTableControlContainer() {
		return new PoolControlContainer(this);
	}

	public boolean cueActive() {
		if (poolArea.cueSprite == null)
			return false;
		return poolArea.cueSprite.wk();
	}

	@Override
	public void dd(YahooControl _pcls79, YahooComponent _pcls78) {
		if (!isSmallWidows())
			super.dd(_pcls79, _pcls78);
	}

	@Override
	public void deactivateControls(int i1) {
		super.deactivateControls(i1);
		if (pool.training)
			poolArea.zc();
		if (pool.isRunning() && isMyTurn())
			Jd();
	}

	public void doChangeEnglish(byte byte2, int i1, float x, float y) {
		poolArea.english.e_n.x = x;
		poolArea.english.e_n.y = y;
	}

	@Override
	public void doUpdate(DataInputStream datainputstream) throws IOException {
		super.doUpdate(datainputstream);
		if (pool != null && pool.isRunning() && pool.getCurrentState() == 0) {
			pendingCueSnapshotRestore = true;
			pendingEnglishSnapshotRestore = true;
			preserveCueSnapshotOnRestore = false;
		}
		else {
			pendingCueSnapshotRestore = false;
			pendingEnglishSnapshotRestore = false;
			preserveCueSnapshotOnRestore = false;
		}
	}

	public void doUpdateState() {
		if (ypt_E) {
			String s1 = "";
			s1 = s1 + System.getProperty("java.version") + " ";
			s1 = s1 + System.getProperty("java.vendor") + "\n";
			logState("turnStat sent turn=" + pool.m_turnNum);
			PoolData _lcls131 = pool.tj();
			send('\uFF85', pool.m_turnNum, _lcls131);
		}
	}

	private void Fc() {
		ypt_q = false;
		ypt_r = 0;
		ypt_s = 0;
		t = 0;
		v = 0;
		u = 0;
	}

	public void Fd(String s1) {
		Lq(getMySitIndex(), s1);
	}

	public boolean get_n() {
		return n;
	}

	public Vector<IBall> getBallInPlayArea() {
		return pool.getBallInPlayArea();
	}

	public YRectangle getBounceArea() {
		return (YRectangle) pool.getProperty("OUT_OF_BOUNCE_AREA");
	}

	public YIPoint getCenterPoint() {
		return (YIPoint) pool.getProperty("CENTER_POINT");
	}

	@Override
	public YahooControl getGameArea() {
		return poolArea;
	}

	public YRectangle getInArea() {
		return (YRectangle) pool.getProperty("IN_AREA");
	}

	public int getLinearFriction() {
		return pool.getIntProperty("linearFriction");
	}

	public Obstacle[] getObstacles() {
		return (Obstacle[]) pool.getProperty("OBSTACLES");
	}

	public YRectangle getPlayArea() {
		return (YRectangle) pool.getProperty("PLAY_AREA_BALLS");
	}

	public YRectangle getPocketArea() {
		return (YRectangle) pool.getProperty("OUT_OF_POCKET_AREA");
	}

	public Pool getPool() {
		return pool;
	}

	public Object getProperty(String s1) {
		return pool.getProperty(s1);
	}

	public int getRotationFriction() {
		return pool.getIntProperty("rotationFriction");
	}

	public int getSideRotationFriction() {
		return pool.getIntProperty("sideRotationFriction");
	}

	public int getSitCount() {
		return !pool.training ? 2 : 1;
	}

	public Slot[] getSlots() {
		return (Slot[]) pool.getProperty("SLOTS");
	}

	public void handleColl(int i1) {
		ypt_q = true;
		switch (i1) {
		case 0: // '\0'
			ypt_r++;
			break;

		case 1: // '\001'
			ypt_s++;
			break;

		case 2: // '\002'
			t++;
			break;

		case 3: // '\003'
			v++;
			break;

		case 4: // '\004'
			u++;
			break;
		}
	}

	public void handleFirtsColl(IBall ball) {

	}

	public void handleIterate() {
		if (poolAimer != null)
			poolAimer.wb();
	}

	public void handleSetPos(int i1, int j1, int k1, int l1, YIVector _pcls48,
			Vel _pcls33) {
		if (_pcls48 != null)
			ypt_I.setCoords(PoolMath.yintToFloat(_pcls48.a), PoolMath
					.yintToFloat(_pcls48.b));
		else
			ypt_I.setCoords(0.0F, 0.0F);
		poolArea.fc(i1, j1, k1, l1, ypt_I, _pcls33);
	}

	public void handleShiftFromIntersect() {
		doUpdateState();
	}

	@Override
	public void handleStart() {
		super.handleStart();
		if (!startedSeatNamesCaptured)
			captureStartedSeatNames();
		poolArea.xc();
		poolArea.yc();
		if (isMyTurn()) {
			Jd();
			if (k) {
				Lq(getMySitIndex(), getApplet().lookupString(0x66501217));
				k = false;
			}
		}
		else {
			Id();
		}
		poolArea.swapArrow(pool.m_turn);
		poolArea.vc(pool.nj());
		int i1;
		if ((i1 = pool.getSetup().Uo()) != -1) {
			poolArea.vc(-1);
			poolArea.qc(pool.m_turn, i1);
		}
	}

	@Override
	public void handleStop(YData data) {
		super.handleStop(data);
		clearStartedSeatNames();
	}

	public void handleStopMoving() {
	}

	public synchronized void handleTimer(long l1) {
		try {
			long l3 = System.currentTimeMillis();
			if (l3 - cueTime >= 10L) {
				poolArea.cueSprite.el(l1);
				cueTime = l3;
			}
			if (isMyTurn() && pool.getCurrentState() == 0
					&& l1 - updateTime >= update * 1000) {
				if (poolArea.cueSprite.isChanged()) {
					send('\uFF80', poolArea.cueSprite);
					poolArea.cueSprite.setChanged(false);

				}
				if (poolArea.english.isChanged()) {
					send('\uFF81', poolArea.english);
					poolArea.english.setChanged(false);
				}
				updateTime = l1;
			}
			if (ypt_q)
				playPoolSound();
			Fc();
		}
		catch (NullPointerException nullpointerexception) {
			nullpointerexception.printStackTrace();
		}
	}

	public void handleUpdateCue(int i1) {
		if (!pool.isRunning() || pool.getCurrentState() != 0
				|| pool.m_turn != i1) {
			pendingCueSnapshotRestore = false;
			preserveCueSnapshotOnRestore = false;
			return;
		}
		if (pendingCueSnapshotRestore) {
			poolArea.cueSprite.setVisible(true);
			poolArea.cueSprite.applyRemoteState();
			poolArea.cueSprite.setChanged(false);
			poolArea.pullCue(poolArea.cueSprite.power1);
			pendingCueSnapshotRestore = false;
			preserveCueSnapshotOnRestore = true;
			return;
		}
		if (getMySitIndex() == i1) {
			preserveCueSnapshotOnRestore = false;
			return;
		}
		if (getMySitIndex() != i1) {
			poolArea.cueSprite.setVisible(true);
			poolArea.cueSprite.update();
			poolArea.cueSprite.setChanged(false);
			poolArea.pullCue(poolArea.cueSprite.power1);
		}
	}

	public void handleUpdateEnglish(int i1) {
		if (!pool.isRunning() || pool.getCurrentState() != 0
				|| pool.m_turn != i1) {
			pendingEnglishSnapshotRestore = false;
			return;
		}
		if (pendingEnglishSnapshotRestore) {
			poolArea.english.update();
			poolArea.english.setChanged(false);
			pendingEnglishSnapshotRestore = false;
			return;
		}
		if (getMySitIndex() != i1) {
			poolArea.english.update();
			poolArea.english.setChanged(false);
		}
	}

	public boolean haveInitTimePorMove() {
		return pool.getIntProperty("INIT_TIME_PER_MOVE") > 0;
	}

	public void Hc() {
		Lq(-3, "");
	}

	public void Hd() {
		if (ypt_o) {
			Lq(getMySitIndex(), getApplet().lookupString(0x66501214));
			ypt_o = false;
			return;
		}
		if (k) {
			Lq(getMySitIndex(), getApplet().lookupString(0x66501217));
			k = false;
			return;
		}
		if (m) {
			Lq(getMySitIndex(), getApplet().lookupString(0x66501259));
			m = false;
			return;
		}
		if (pool.getAimStateInit()) {
			Lq(getMySitIndex(), getApplet().lookupString(0x6650125d));
			return;
		}
		if (p) {
			Lq(getMySitIndex(), getApplet().lookupString(0x66501253));
			return;
		}
		Lq(getMySitIndex(), getApplet().lookupString(0x66501254));
		p = true;
		return;
	}

	public void Id() {
		poolArea.deactivate();
		if (!pool.isRunning() || pool.getCurrentState() != 0)
			poolArea.hideCue();
		p = false;
	}

	public boolean isMyTurn() {
		int sitIndex = getMySitIndex();
		return sitIndex >= 0 && isStartedSeatOwner(sitIndex)
				&& pool.isSameTurn(sitIndex);
	}

	public void Jd() {
		restoreMyTurnView(true);
	}

	private void restoreMyTurnView(boolean playBeep) {
		poolArea.oc();
		if (pool.isRunning()) {
			boolean preserveCue = preserveCueSnapshotOnRestore;
			poolArea.activate();
			if (preserveCue) {
				poolArea.cueSprite.setVisible(true);
				poolArea.cueSprite.applyRemoteState();
				poolArea.cueSprite.setChanged(false);
			}
			else {
				poolArea.yc();
				int i1 = pool.getSetup().Uo();
				if (i1 != -1)
					poolArea.cueSprite.Tk(((YIPoint) pool.getBall(i1))
							.toYVector());
			}
			poolArea.cueSprite.setChanged(false);
			poolArea.english.setChanged(false);
		}
		Hd();
		if (playBeep)
			beep();
	}

	@Override
	public boolean Kc() {
		return false;
	}

	public void kd(int i1) {
		if (getMySitIndex() == i1) {
			poolArea.hideCue();
			new SelectTypeDialog(this, poolArea.getContainer());
		}
		else {
			Lq(getMySitIndex(), getSitIdCaption(i1)
					+ getApplet().lookupString(0x665011c5));
		}
	}

	@Override
	public void Kd(int i1) {
		super.Kd(i1);
		if (i1 >= 0 && pool.getCurrentState() == 0 && pool.useInitTimePorMove()) {
			poolArea.lblTime.setCaption(Formater.formatTimer(i1 + 999));
			if (i1 == 0 && isMyTurn())
				send('\uFF91', pool.m_turnNum);
		}
	}

	public void logState(String s1) {
		if (state.length() > 200)
			state = state.substring(state.length() - 180);
		state += "\n" + s1;
	}

	public String lookupString(int i) {
		return applet.lookupString(i);
	}

	public void nd(YData _pcls111) {
		String s1 = "YOU";
		poolArea.hideCue();
		if (!pool.training)
			s1 = ((ByteArrayData) _pcls111).byteAt(0) != 1 ? getSitIdCaption(1)
					: getSitIdCaption(0);
		if (!pool.g)
			showQuickMessage(s1 + getApplet().lookupString(0x66501413));
		if (pool.g)
			startgame();
	}

	@Override
	public synchronized void parseData(byte byte0,
			DataInputStream datainputstream) throws IOException {
		switch (byte0) {
		case -128: // 80: change cue
			byte byte1 = datainputstream.readByte();
			actionChangeCue(byte1, datainputstream);
			break;

		case -113: // 8F
			break;

		case -127: // 81: change english
			byte byte2 = datainputstream.readByte();
			actionChangeEnglish(byte2, datainputstream);
			break;

		case -125: // 83: reset
			byte byte3 = datainputstream.readByte();
			pool.actionReset(byte3);
			break;

		case -122: // 86: select type
			int i1 = datainputstream.readInt();
			pool.selectType(pool.m_turn, i1);
			break;

		case -115: // 8D: change slot
			int j1 = datainputstream.readInt();
			pool.doSetSlot(pool.m_turn, j1);
			break;

		case -102: // 9A: change pool ball
			int k1 = datainputstream.readInt();
			int j2 = datainputstream.readInt();
			int l2 = datainputstream.readInt();
			actionChangePoolBall(k1, j2, l2);
			break;

		case -126: // 82: strike
			byte byte4 = datainputstream.readByte();
			int k2 = datainputstream.readInt();
			byte byte5 = datainputstream.readByte();
			cueDist.read(datainputstream);
			englishDist.read(datainputstream);
			firstColl.read(datainputstream);
			logState("strike recvd " + byte4 + " " + k2);
			if (byte4 != getMySitIndex())
				pool.doStrike(byte4, k2, cueDist, englishDist, firstColl,
						byte5);
			break;

		case -112: // 90: time empty
			J.reset();
			J.read(datainputstream);
			logState("notifyTELAPS");
			pool.doNotifyTELAPS(J);
			break;

		case -101: // 9B: change table state
			ypt_E = false;
			J.reset();
			J.read(datainputstream);
			Xc(0);
			Xc(1);
			logState("notifyTStat.");
			pool.doNotifyTStat(J, false);
			break;

		case -105: // 97: change time
			int l1 = datainputstream.readInt();
			if (pool.isRunning()) {
				poolArea.lblTime.setCaption(Formater.formatTimer(l1 / 1000));
				handleStartTick(l1);
			}
			break;

		case -96: // A0
			datainputstream.readInt();
			break;
		case -95: // A1
			datainputstream.readInt();
			// a_y_em_fld.e(i2);
			return;

		case -94: // A2
			j2 = datainputstream.readInt();
			datainputstream.readInt();
			// a_y_em_fld.a_y_cw_fld.c(j2, i3);
			// a_y_if_fld.b("Got aimballdiameter=" + j2);
			// a_y_if_fld.b("Got aimballcolor=" + i3);
			return;

		case -124: // 84
		case -123: // 85
		case -121: // 87
		case -120: // 88
		case -119: // 89
		case -118: // 8A
		case -117: // 8B
		case -116: // 8C
		case -114: // 8E
		case -111: // 91
		case -110: // 92
		case -109: // 93
		case -108: // 94
		case -107: // 95
		case -106: // 96
		case -104: // 98
		case -103: // 99
		case -100: // 9C
		case -99: // 9D
		case -98: // 9E
		case -97: // 9F
		default:
			super.parseData(byte0, datainputstream);
			break;
		}
	}

	private void playPoolSound() {
		if (!soundEnabled())
			return;
		String s2 = null;
		if (u > 0)
			s2 = pocketSound;
		else if (ypt_r > 5)
			s2 = ballSound;
		else if (ypt_r > 2)
			s2 = ballSound;
		else if (ypt_r > 0)
			s2 = ballSound;
		else if (v > 0)
			s2 = cueSound;
		else if (ypt_s > 0)
			s2 = ball_softSound;
		else if (t > 0)
			s2 = cushionSound;
		if (s2 != null) {
			String s1 = s2;
			getApplet().playSound(s1);
		}
	}

	public void qd(IBall _pcls124) {
		poolArea.hc(_pcls124);
	}

	public void rd() {
		poolArea.gc();
	}

	public void reset() {
		if (pool.isRunning()) {
			send('\uFF83');
		}
	}

	public void applyGhostStyleSelection() {
		if (cmbGhostStyle != null && poolArea != null && poolAimer != null)
			switch (cmbGhostStyle.getItemIndex()) {
			case 0:
				poolAimer.setGhostStyle(Aim.STYLE_OUTLINE);
				break;

			case 1:
				poolAimer.setGhostStyle(Aim.STYLE_GREEN_OUTLINE);
				break;

			case 2:
				poolAimer.setGhostStyle(Aim.STYLE_RED_OUTLINE);
				break;

			default:
				poolAimer.setGhostStyle(Aim.STYLE_GHOST_IMAGE);
				break;
			}
	}

	public void applyTableColorSelection() {
		if (cmbTableColor == null)
			return;
		int index = cmbTableColor.getItemIndex();
		if (index < 0)
			index = 0;
		if (index >= TABLE_COLOR_NAMES.length)
			index = 0;
		if (index == TABLE_COLOR_NAMES.length - 1) {
			if (tableColorEditor != null)
				currentTableColor = tableColorEditor.getColor();
		}
		else {
			currentTableColor = TABLE_COLOR_VALUES[index];
		}
		refreshTableColorUi();
		applyCurrentTableColor();
	}

	private void applyCurrentTableColor() {
		if (poolAimer != null && currentTableColor != null)
			poolAimer.setTableColor(currentTableColor);
		if (poolArea != null && currentTableColor != null)
			poolArea.setTableColor(currentTableColor);
	}

	public void handleCustomTableColorChange(Color color) {
		if (color == null || cmbTableColor == null)
			return;
		if (cmbTableColor.getItemIndex() != TABLE_COLOR_NAMES.length - 1)
			cmbTableColor.fn(TABLE_COLOR_NAMES.length - 1);
		currentTableColor = color;
		refreshTableColorUi();
		applyCurrentTableColor();
	}

	public boolean isCustomTableColorPanelVisible() {
		return customTableColorPanel != null && customTableColorPanel.visible
				&& !customTableColorPanel.c;
	}

	private boolean isCustomTableColorSelected() {
		return cmbTableColor != null
				&& TABLE_COLOR_VALUES[cmbTableColor.getItemIndex()] == null;
	}

	public void setCustomTableColorPanelVisible(boolean visible) {
		if (customTableColorPanel == null)
			return;
		if (visible) {
			customTableColorPanel.visible = true;
			customTableColorPanel.c = false;
			positionCustomTableColorPanel();
			bringCustomTableColorPanelToFront();
		}
		else {
			customTableColorPanel.visible = false;
			customTableColorPanel.c = true;
			customTableColorPanel.setCoords(-1000, -1000, true);
		}
		customTableColorPanel.invalidate();
		if (customTableColorPanel.getParent() != null)
			customTableColorPanel.getParent().invalidate();
	}

	private void refreshTableColorUi() {
		if (tableColorEditor != null && currentTableColor != null)
			tableColorEditor.setColor(currentTableColor);
		if (customTableColorPanel != null) {
			setCustomTableColorPanelVisible(isCustomTableColorSelected());
		}
	}

	private void positionCustomTableColorPanel() {
		if (customTableColorPanel == null || cmbTableColor == null)
			return;
		YahooControl host = ensureCustomTableColorPanelHost();
		if (host == null)
			return;
		int left = cmbTableColor.getWorldLeft(host) - 4;
		int dropdownTop = cmbTableColor.getWorldTop(host);
		int top = dropdownTop - customTableColorPanel.getHeight() - 6;
		if (top < 0)
			top = dropdownTop + cmbTableColor.getHeight() + 6;
		customTableColorPanel.setCoords(left, top, true);
	}

	private YahooControl ensureCustomTableColorPanelHost() {
		if (customTableColorPanel == null || cmbTableColor == null)
			return null;
		YahooControl host = cmbTableColor.getContainer();
		if (host == null)
			return customTableColorPanel.getParent();
		if (customTableColorPanel.getParent() != host) {
			YahooControl oldParent = customTableColorPanel.getParent();
			int left = customTableColorPanel.left;
			int top = customTableColorPanel.top;
			if (oldParent != null)
				oldParent.removeChildObject(customTableColorPanel);
			host.addChildObject(customTableColorPanel, left, top, true);
		}
		return host;
	}

	private void bringCustomTableColorPanelToFront() {
		YahooControl parent = ensureCustomTableColorPanelHost();
		if (customTableColorPanel == null || parent == null)
			return;
		int left = customTableColorPanel.left;
		int top = customTableColorPanel.top;
		parent.removeChildObject(customTableColorPanel);
		parent.addChildObject(customTableColorPanel, left, top, true);
	}

	@Override
	public void rq() {
		cmbGhostStyle = new YahooComboBox(getTimerHandler());
		cmbGhostStyle.setText("White");
		cmbGhostStyle.setText("Green");
		cmbGhostStyle.setText("Red");
		cmbGhostStyle.setText("Ghost");
		cmbTableColor = new YahooComboBox(getTimerHandler());
		for (String tableColorName : TABLE_COLOR_NAMES)
			cmbTableColor.setText(tableColorName);
		tableColorEditor = new TableColorEditor(this);
		super.rq();
		cmbGhostStyle.fn(3);
		cmbTableColor.fn(0);
		applyGhostStyleSelection();
		applyTableColorSelection();
	}

	public void Sc() {
		poolArea.xc();
		poolArea.yc();
		poolArea.activate();
		poolArea.swapArrow(pool.m_turn);
		poolArea.vc(pool.nj());
		if (pool.getSetup().ap())
			poolArea.wc();
		int i1;
		if ((i1 = pool.getSetup().Uo()) != -1) {
			poolArea.vc(-1);
			poolArea.qc(pool.m_turn, i1);
		}
		if (!pool.training)
			if (isMyTurn()) {
				Jd();
				if (k) {
					Lq(getMySitIndex(), getApplet().lookupString(0x66501217));
					k = false;
				}
			}
			else {
				Id();
			}
	}

	public void selectMyType(int i1) {
		if (isMyTurn() && pool.getCurrentState() == 3) {
			if (getMySitIndex() == 1)
				if (i1 == 1024)
					i1 = 2048;
				else
					i1 = 1024;
			send('\uFF95', i1);
		}
	}

	public void selectSlot(int i1) {
		poolArea.sc(i1);
	}

	public void set_n(boolean value) {
		n = value;
	}

	public void setSlot(int i1) {
		if (isMyTurn() && pool.isRunning() && pool.getCurrentState() == 0
				&& pool.getSelectedSlotIndex() != -1)
			send('\uFF8E', i1);
	}

	@Override
	public void sit(int i1) {
		super.sit(i1);
		return;
	}

	public void strike() {
		if (isMyTurn() && pool.isRunning() && pool.getCurrentState() == 0) {
			IBall selectedBall = poolArea.cueSprite.getSelectedBall();
			int selectedPower = poolArea.cueSprite.power1;
			YIPoint cueDist = poolArea.cueSprite.getPos(true).copy();
			YIPoint englishDist = poolArea.english.getPos().copy();
			YIPoint firstColl = poolAimer.getFirstColl();
			int collBall = poolAimer.getIndex();
			int previewCollBall = collBall;
			YIPoint previewFirstColl = new YIPoint(firstColl.a, firstColl.b);
			collBall = resolveCollisionHint(selectedBall.getIndex(), firstColl, collBall);
			boolean openingBreak = pool.isOpeningBreakShot(selectedBall, collBall);
			if (openingBreak) {
				cueDist = applyBreakPowerJitter(selectedBall, cueDist, selectedPower);
				cueDist = buildOpeningBreakCueDist(selectedBall, cueDist);
			}
			// System.out.println("cueDist=" + cueDist + "; englishDist="
			// + englishDist + "; firstColl=" + firstColl + "; collBall="
			// + collBall);
			strike(selectedBall.getIndex(), cueDist, englishDist, firstColl,
					collBall);
		}
	}

	private int resolveCollisionHint(int cueBallIndex, YIPoint firstColl,
			int collBall) {
		if (collBall == -1 || firstColl == null || firstColl.a == 0)
			return collBall;
		int nearestBallIndex = collBall;
		int nearestDistance = Integer.MAX_VALUE;
		IBall _ball[] = pool.getBall();
		for (IBall item : _ball) {
			if (item.getIndex() == cueBallIndex || item.inSlot())
				continue;
			int distance = item.distance(firstColl);
			if (distance >= PoolMath.intToYInt(21) || distance >= nearestDistance)
				continue;
			nearestDistance = distance;
			nearestBallIndex = item.getIndex();
		}
		return nearestBallIndex;
	}

	private YIPoint buildOpeningBreakCueDist(IBall selectedBall, YIPoint cueDist) {
		YIVector boostedPullback = new YIVector((YIPoint) selectedBall, cueDist);
		boostedPullback.mul(PoolMath.floatToYInt(1.4F));
		YIPoint boostedCueDist = ((PoolBall) selectedBall).newCopy();
		boostedCueDist.add(boostedPullback);
		return boostedCueDist;
	}

	private YIPoint applyBreakPowerJitter(IBall selectedBall, YIPoint cueDist,
			int selectedPower) {
		if (selectedPower <= 0)
			return cueDist;
		int adjustedPower;
		if (selectedPower >= MAX_CUE_POWER)
			adjustedPower = selectedPower - BREAK_RANDOM.nextInt(BREAK_POWER_JITTER + 1);
		else
			adjustedPower = Math.max(0, Math.min(MAX_CUE_POWER, selectedPower
					+ BREAK_RANDOM.nextInt(BREAK_POWER_JITTER * 2 + 1)
					- BREAK_POWER_JITTER));
		if (adjustedPower == selectedPower)
			return cueDist;
		YIVector pullback = new YIVector((YIPoint) selectedBall, cueDist);
		pullback.mul(PoolMath.floatToYInt((float) adjustedPower
				/ (float) selectedPower));
		YIPoint adjustedCueDist = ((PoolBall) selectedBall).newCopy();
		adjustedCueDist.add(pullback);
		return adjustedCueDist;
	}

	public void strike(int index, YIPoint cueDist, YIPoint englishDist,
			YIPoint firstColl, int collBall) {
		logState("strike done and sent turn=" + pool.m_turnNum + " seat="
				+ getMySitIndex());
		send('\uFF82', pool.m_turnNum, index, (byte) collBall, cueDist,
				englishDist, firstColl);
		pool.poolEngine.active = false;
		pool.doStrike(getMySitIndex(), index, cueDist, englishDist, firstColl,
				collBall);
		pool.poolEngine.resetLog();
		pool.poolEngine.active = true;
	}

	public void td() {
		poolArea.xc();
	}

	public void updatePB(int i1, int j1, int k1) {
		if (isMyTurn() && pool.isRunning() && pool.getCurrentState() == 0) {
			send('\uFF9A', i1, j1, k1);
		}
	}

	public String Xc(int i1) {
		if (i1 >= getSitCount())
			return "X";
		return getSitIdCaption(i1);
	}

	public void xd(int i1) {
		poolArea.hideCue();
		if (poolAimer != null)
			poolArea.poolAimer.ls();
		poolArea.cueSprite.power1 = 0;
		poolArea.releasePowerbar();
		poolArea.Yb();
		ypt_E = true;
	}

	public void zd(int i1, boolean flag) {
		poolArea.swapArrow(i1);
		if (pool.getSetup().ap())
			poolArea.wc();
		int j1;
		if ((j1 = pool.getSetup().Uo()) != -1) {
			poolArea.vc(-1);
			poolArea.qc(i1, j1);
		}
		Lq(-3, "");
		if (pool.getAimStateInit())
			Lq(-3, getSitIdCaption(i1) + getApplet().lookupString(0x665011c6));
		if (isMyTurn())
			Jd();
		else
			Id();
		if (!pool.training) {
			int k1 = pool.getIntProperty("INIT_TIME_PER_MOVE");
			if (k1 > 0) {
				if (pool.getAimStateInit())
					k1 = 180;
				poolArea.lblTime.setCaption(Formater.formatTimer(k1));
				handleStartTick(k1 * 1000);
			}
			else {
				handleStartTick(180000);
			}
		}
	}

}
