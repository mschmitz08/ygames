// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) fieldsfirst

package y.po;

import java.awt.Color;
import java.awt.Event;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import y.controls.YahooComponent;
import y.controls.YahooButton;
import y.controls.YahooComboBox;
import y.controls.YahooControl;
import y.controls.YahooLabel;
import y.controls.YahooTextBox;
import y.dialogs.YahooDialog;
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
import common.yutils.GameHandler;

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
	private static final float BREAK_ANGLE_JITTER_DEGREES = 2.5F;
	private static final int	MAX_CUE_POWER		= 120;
	private static final int	TEST_SHOT_BATCH_SIZE	= 50000;
	private static final int	TEST_SHOT_MAX_TICKS	= 900;
	private static final int	TEST_SHOT_BLACK_BALL_INDEX	= 6;
	public static final int	DEFAULT_CUE_TAP_UNITS		= 8;
	public static final int	DEFAULT_CUE_MAX_UNITS		= 8;
	public static final int	DEFAULT_CUE_ACCEL_DELAY_MS	= 500;
	public static final int	DEFAULT_CUE_ACCEL_RAMP_MS	= 900;
	private static final String PROP_TABLE_COLOR		= "pool_table_color";
	private static final String PROP_CUE_TAP		= "pool_cue_tap";
	private static final String PROP_CUE_MAX		= "pool_cue_max";
	private static final String PROP_CUE_DELAY		= "pool_cue_delay";
	private static final String PROP_CUE_RAMP		= "pool_cue_ramp";
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
	YahooButton		btnCueControls;
	YahooTextBox	txtTestShot;
	YahooButton		btnTestShot;
	YahooComponent	customTableColorPanel;
	YahooComponent	cueControlPanel;
	TableColorEditor	tableColorEditor;
	CueControlEditor	cueControlEditor;
	Color			currentTableColor;
	int				cueTapUnits;
	int				cueMaxUnits;
	int				cueAccelDelayMs;
	int				cueAccelRampMs;
	boolean			loadingPreferences;
	boolean			tableColorPreferenceDirty;
	boolean			cueControlPreferenceDirty;
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
		cueTapUnits = DEFAULT_CUE_TAP_UNITS;
		cueMaxUnits = DEFAULT_CUE_MAX_UNITS;
		cueAccelDelayMs = DEFAULT_CUE_ACCEL_DELAY_MS;
		cueAccelRampMs = DEFAULT_CUE_ACCEL_RAMP_MS;
		loadingPreferences = false;
		tableColorPreferenceDirty = false;
		cueControlPreferenceDirty = false;
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
		saveTableColorPreferenceIfDirty();
		saveCueControlPreferencesIfDirty();
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
		return sitIndex >= 0 && pool.isSameTurn(sitIndex);
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
		saveTableColorPreference();
	}

	private void applyCurrentTableColor() {
		if (poolAimer != null && currentTableColor != null)
			poolAimer.setTableColor(currentTableColor);
		if (poolArea != null && currentTableColor != null)
			poolArea.setTableColor(currentTableColor);
	}

	private void applyCueControlSettings() {
		if (poolArea != null && poolArea.cueSprite != null)
			poolArea.cueSprite.setDirectionControl(cueTapUnits, cueMaxUnits,
					cueAccelDelayMs, cueAccelRampMs);
		if (cueControlEditor != null)
			cueControlEditor.setValues(cueTapUnits, cueMaxUnits,
					cueAccelDelayMs, cueAccelRampMs);
	}

	public void handleCustomTableColorChange(Color color) {
		if (color == null || cmbTableColor == null)
			return;
		if (cmbTableColor.getItemIndex() != TABLE_COLOR_NAMES.length - 1)
			cmbTableColor.fn(TABLE_COLOR_NAMES.length - 1);
		currentTableColor = color;
		refreshTableColorUi();
		applyCurrentTableColor();
		tableColorPreferenceDirty = true;
	}

	public void handleTableColorSliderChange(char channel, int value) {
		Color color = currentTableColor != null ? currentTableColor
				: TABLE_COLOR_VALUES[0];
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		switch (channel) {
		case 'r':
		case 'R':
			red = value;
			break;

		case 'g':
		case 'G':
			green = value;
			break;

		case 'b':
		case 'B':
			blue = value;
			break;

		default:
			return;
		}
		handleCustomTableColorChange(new Color(red, green, blue));
	}

	public void handleCueControlChange(int tapUnits, int maxUnits,
			int accelDelayMs, int accelRampMs) {
		cueTapUnits = clamp(tapUnits, 1, 40);
		cueMaxUnits = clamp(maxUnits, cueTapUnits, 140);
		cueAccelDelayMs = clamp(accelDelayMs, 0, 3000);
		cueAccelRampMs = clamp(accelRampMs, 100, 5000);
		applyCueControlSettings();
		cueControlPreferenceDirty = true;
	}

	public boolean isCueControlPanelVisible() {
		return cueControlPanel != null && cueControlPanel.visible
				&& !cueControlPanel.c;
	}

	public void setCueControlPanelVisible(boolean visible) {
		if (cueControlPanel == null)
			return;
		if (visible) {
			cueControlPanel.visible = true;
			cueControlPanel.c = false;
			positionPopupPanel(cueControlPanel, btnCueControls);
			bringPanelToFront(cueControlPanel, btnCueControls);
		}
		else {
			saveCueControlPreferencesIfDirty();
			cueControlPanel.visible = false;
			cueControlPanel.c = true;
			cueControlPanel.setCoords(-1000, -1000, true);
		}
		cueControlPanel.invalidate();
		if (cueControlPanel.getParent() != null)
			cueControlPanel.getParent().invalidate();
	}

	private int clamp(int value, int min, int max) {
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	private void loadCueControlPreferences() {
		cueTapUnits = readIntPreference(PROP_CUE_TAP, DEFAULT_CUE_TAP_UNITS, 1,
				40);
		cueMaxUnits = readIntPreference(PROP_CUE_MAX, DEFAULT_CUE_MAX_UNITS,
				cueTapUnits, 140);
		cueAccelDelayMs = readIntPreference(PROP_CUE_DELAY,
				DEFAULT_CUE_ACCEL_DELAY_MS, 0, 3000);
		cueAccelRampMs = readIntPreference(PROP_CUE_RAMP,
				DEFAULT_CUE_ACCEL_RAMP_MS, 100, 5000);
	}

	private void loadTableColorPreference() {
		if (cmbTableColor == null)
			return;
		String value = getApplet().getIdProperty(PROP_TABLE_COLOR);
		if (value == null || value.length() == 0) {
			cmbTableColor.fn(0);
			currentTableColor = TABLE_COLOR_VALUES[0];
			return;
		}
		for (int i = 0; i < TABLE_COLOR_NAMES.length - 1; i++) {
			if (TABLE_COLOR_NAMES[i].equalsIgnoreCase(value)) {
				cmbTableColor.fn(i);
				currentTableColor = TABLE_COLOR_VALUES[i];
				return;
			}
		}
		Color custom = parseColorPreference(value);
		if (custom != null) {
			currentTableColor = custom;
			if (tableColorEditor != null)
				tableColorEditor.setColor(custom);
			cmbTableColor.fn(TABLE_COLOR_NAMES.length - 1);
			return;
		}
		cmbTableColor.fn(0);
		currentTableColor = TABLE_COLOR_VALUES[0];
	}

	private Color parseColorPreference(String value) {
		try {
			if (value != null && value.length() == 7 && value.charAt(0) == '#')
				return new Color(Integer.parseInt(value.substring(1), 16));
		}
		catch (NumberFormatException _ex) {
		}
		return null;
	}

	private int readIntPreference(String key, int defaultValue, int min, int max) {
		String value = getApplet().getIdProperty(key);
		try {
			if (value != null && value.length() > 0)
				return clamp(Integer.parseInt(value), min, max);
		}
		catch (NumberFormatException _ex) {
		}
		return clamp(defaultValue, min, max);
	}

	private void saveCueControlPreferences() {
		if (loadingPreferences)
			return;
		getApplet().changeIdProperty(PROP_CUE_TAP, Integer.toString(cueTapUnits));
		getApplet().changeIdProperty(PROP_CUE_MAX, Integer.toString(cueMaxUnits));
		getApplet().changeIdProperty(PROP_CUE_DELAY,
				Integer.toString(cueAccelDelayMs));
		getApplet().changeIdProperty(PROP_CUE_RAMP,
				Integer.toString(cueAccelRampMs));
	}

	private void saveCueControlPreferencesIfDirty() {
		if (!cueControlPreferenceDirty)
			return;
		cueControlPreferenceDirty = false;
		saveCueControlPreferences();
	}

	private void saveTableColorPreference() {
		if (loadingPreferences)
			return;
		if (cmbTableColor == null || currentTableColor == null)
			return;
		int index = cmbTableColor.getItemIndex();
		if (index >= 0 && index < TABLE_COLOR_NAMES.length - 1) {
			getApplet().changeIdProperty(PROP_TABLE_COLOR, TABLE_COLOR_NAMES[index]);
			return;
		}
		getApplet().changeIdProperty(PROP_TABLE_COLOR, String.format("#%02X%02X%02X",
				currentTableColor.getRed(), currentTableColor.getGreen(),
				currentTableColor.getBlue()));
	}

	private void saveTableColorPreferenceIfDirty() {
		if (!tableColorPreferenceDirty)
			return;
		tableColorPreferenceDirty = false;
		saveTableColorPreference();
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
			saveTableColorPreferenceIfDirty();
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
		positionPopupPanel(customTableColorPanel, cmbTableColor);
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
		bringPanelToFront(customTableColorPanel, cmbTableColor);
	}

	private void bringPanelToFront(YahooComponent panel, YahooComponent anchor) {
		YahooControl parent = ensurePanelHost(panel, anchor);
		if (panel == null || parent == null)
			return;
		int left = panel.left;
		int top = panel.top;
		parent.removeChildObject(panel);
		parent.addChildObject(panel, left, top, true);
	}

	private YahooControl ensurePanelHost(YahooComponent panel, YahooComponent anchor) {
		if (panel == null || anchor == null)
			return null;
		YahooControl host = anchor.getContainer();
		if (host == null)
			return panel.getParent();
		if (panel.getParent() != host) {
			YahooControl oldParent = panel.getParent();
			int left = panel.left;
			int top = panel.top;
			if (oldParent != null)
				oldParent.removeChildObject(panel);
			host.addChildObject(panel, left, top, true);
		}
		return host;
	}

	private void positionPopupPanel(YahooComponent panel, YahooComponent anchor) {
		if (panel == null || anchor == null)
			return;
		YahooControl host = ensurePanelHost(panel, anchor);
		if (host == null)
			return;
		int left = anchor.getWorldLeft(host) - 4;
		int dropdownTop = anchor.getWorldTop(host);
		int top = dropdownTop - panel.getHeight() - 6;
		if (top < 0)
			top = dropdownTop + anchor.getHeight() + 6;
		panel.setCoords(left, top, true);
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
		btnCueControls = new YahooButton("Adjust");
		cueControlEditor = new CueControlEditor(this);
		txtTestShot = new YahooTextBox(getTimerHandler(), "solid,stripe", 72);
		btnTestShot = new YahooButton("Find");
		loadingPreferences = true;
		loadTableColorPreference();
		loadCueControlPreferences();
		super.rq();
		cmbGhostStyle.fn(3);
		applyGhostStyleSelection();
		applyTableColorSelection();
		applyCueControlSettings();
		loadingPreferences = false;
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
				cueDist = applyBreakAngleJitter(selectedBall, cueDist);
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

	private YIPoint applyBreakAngleJitter(IBall selectedBall, YIPoint cueDist) {
		YIVector pullback = new YIVector((YIPoint) selectedBall, cueDist);
		if (pullback.abs() == 0)
			return cueDist;
		int maxJitter = PoolMath.mul(PoolMath.pi_180, PoolMath
				.floatToYInt(BREAK_ANGLE_JITTER_DEGREES));
		int jitter = BREAK_RANDOM.nextInt(maxJitter * 2 + 1) - maxJitter;
		if (jitter == 0)
			return cueDist;
		pullback.rotate(jitter);
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

	public void testShot(String spec) {
		if (!isTestRoom()) {
			Fd("Test shot finder is only available in the test room.");
			return;
		}
		if (isTestShotHelpSpec(spec)) {
			showTestShotHelp();
			return;
		}
		if (!isMyTurn() || !pool.isRunning() || pool.getCurrentState() != 0) {
			Fd("Test shot finder only runs on your active turn.");
			return;
		}
		TestShotTarget target = parseTestShotSpec(spec);
		if (target == null) {
			Fd("Use balls or fouls, e.g. solid,stripe,cue or wrongfirst.");
			return;
		}
		TestShotResult result = findTestShot(target);
		if (result == null) {
			Fd("No exact test shot found in " + TEST_SHOT_BATCH_SIZE + " tries.");
			return;
		}
		Fd("Test shot found after " + result.attempts + " tries.");
		strike(result.cueBallIndex, result.cueDist, result.englishDist,
				result.firstColl, result.collBall);
	}

	public boolean isTestRoom() {
		return getApplet() != null && getApplet().room != null
				&& getApplet().room.equalsIgnoreCase("test");
	}

	private boolean isTestShotHelpSpec(String spec) {
		if (spec == null)
			return false;
		String token = spec.toLowerCase().trim();
		if (token.indexOf("help") != -1 || token.indexOf("?") != -1)
			return true;
		return token.equals("help") || token.equals("?")
				|| token.equals("cheat") || token.equals("cheatsheet")
				|| token.equals("cheat sheet") || token.equals("syntax");
	}

	private void showTestShotHelp() {
		logMessage("*** Test shot help (local only)", Color.blue);
		for (int i = 0; i < TestShotHelpDialog.HELP_LINES.length; i++)
			logMessage("*** " + TestShotHelpDialog.HELP_LINES[i], Color.blue);
		Fd("Test shot help was written to your local chat log.");
	}

	private TestShotResult findTestShot(TestShotTarget target) {
		IBall selectedBall = poolArea.cueSprite.getSelectedBall();
		if (selectedBall == null || selectedBall.inSlot())
			selectedBall = pool.getSetup().getWhiteBall();
		if (selectedBall == null || selectedBall.inSlot())
			return null;
		int cueBallIndex = selectedBall.getIndex();
		for (int attempt = 1; attempt <= TEST_SHOT_BATCH_SIZE; attempt++) {
			double angle = BREAK_RANDOM.nextDouble() * Math.PI * 2D;
			YIPoint cueDist = buildTestCueDist(selectedBall, angle);
			YIPoint englishDist = new YIPoint(0.0F, 0.0F);
			CollisionHint hint = calculateCollisionHint(pool, cueBallIndex,
					cueDist);
			Pool simPool = createSimulationPool();
			if (simPool == null)
				return null;
			if (!simPool.doStrike(simPool.m_turn, cueBallIndex, cueDist,
					englishDist, hint.firstColl, hint.collBall))
				continue;
			runSimulation(simPool);
			if (matchesPocketedSet(simPool, target)) {
				TestShotResult result = new TestShotResult();
				result.attempts = attempt;
				result.cueBallIndex = cueBallIndex;
				result.cueDist = cueDist;
				result.englishDist = englishDist;
				result.firstColl = hint.firstColl;
				result.collBall = hint.collBall;
				return result;
			}
		}
		return null;
	}

	private YIPoint buildTestCueDist(IBall selectedBall, double angle) {
		float cueX = selectedBall.getYIntX();
		float cueY = selectedBall.getYIntY();
		float pullX = (float) Math.cos(angle) * MAX_CUE_POWER;
		float pullY = (float) Math.sin(angle) * MAX_CUE_POWER;
		return new YIPoint(cueX - pullX, cueY - pullY);
	}

	private CollisionHint calculateCollisionHint(Pool sourcePool,
			int cueBallIndex, YIPoint cueDist) {
		CollisionHint hint = new CollisionHint();
		hint.firstColl = new YIPoint(0, 0);
		hint.collBall = -1;
		IBall cueBallSource = sourcePool.getBall(cueBallIndex);
		PoolBall cueBall = (PoolBall) ((PoolBall) cueBallSource).Copy();
		YIVector velocity = new YIVector(cueBallSource.getX() - cueDist.a,
				cueBallSource.getY() - cueDist.b);
		if (velocity.abs() == 0)
			return hint;
		velocity.versor();
		velocity.mul(PoolMath.intToYInt(20));
		cueBall.vel.setFrom(velocity);
		cueBall.sliding = true;
		cueBall.wX.set(0, 0);
		cueBall.uncolide();
		YRectangle playAreaBalls = (YRectangle) sourcePool
				.getProperty("PLAY_AREA_BALLS");
		Vector<IBall> balls = sourcePool.getBallInPlayArea();
		for (int tick = 0; tick < 600
				&& playAreaBalls.containsPoint(cueBall.a, cueBall.b); cueBall
				.nextPosition(), tick++) {
			int bestTime = PoolMath.n_1;
			IBall bestBall = null;
			for (int i = 0; i < balls.size(); i++) {
				IBall current = balls.elementAt(i);
				if (current == null || current.getIndex() == cueBallIndex
						|| current.inSlot())
					continue;
				int timeToBall = cueBall.timeToBall(current);
				if (timeToBall < bestTime && timeToBall > 0) {
					bestTime = timeToBall;
					bestBall = current;
				}
			}
			if (bestBall != null) {
				hint.firstColl.setCoords(cueBall.a, cueBall.b);
				YIVector step = cueBall.vel.je();
				step.mul(bestTime);
				hint.firstColl.add(step);
				if (playAreaBalls.containsPoint(hint.firstColl.a,
						hint.firstColl.b))
					hint.collBall = resolveCollisionHint(cueBallIndex,
							hint.firstColl, bestBall.getIndex());
				return hint;
			}
			cueBall.add(cueBall.vel);
		}
		return hint;
	}

	private Pool createSimulationPool() {
		Pool simPool = new Pool();
		SilentPoolHandler handler = new SilentPoolHandler();
		Hashtable<String, String> params = new Hashtable<String, String>();
		if (pool.training)
			params.put("training", "1");
		if (pool.getSetup() instanceof NineBallSetup)
			params.put("nineBallGame", "1");
		simPool.initializeProperties(params, handler);
		handler.setPool(simPool);
		simPool.assign(pool);
		return simPool;
	}

	private void runSimulation(Pool simPool) {
		for (int tick = 0; tick < TEST_SHOT_MAX_TICKS
				&& simPool.poolEngine.movingExist(); tick++)
			simPool.poolEngine.handleTimer(0L);
	}

	private boolean matchesPocketedSet(Pool simPool, TestShotTarget target) {
		if (!matchesFoulTarget(simPool, target))
			return false;
		if (!matchesEightPocketTarget(simPool, target))
			return false;
		if (!target.hasPocketTarget())
			return true;
		boolean[] seen = new boolean[target.exact.length];
		int solidCount = 0;
		int stripeCount = 0;
		for (int i = 0; i < simPool.turnPocketed.size(); i++) {
			IBall pocketed = simPool.turnPocketed.elementAt(i);
			int index = pocketed.getIndex();
			if (index < 0 || index >= target.exact.length)
				return false;
			if (target.exact[index]) {
				seen[index] = true;
				continue;
			}
			if (pocketed.getType() == 1024 && solidCount < target.solidCount) {
				solidCount++;
				continue;
			}
			if (pocketed.getType() == 2048 && stripeCount < target.stripeCount) {
				stripeCount++;
				continue;
			}
			return false;
		}
		for (int i = 0; i < target.exact.length; i++)
			if (target.exact[i] && !seen[i])
				return false;
		return solidCount == target.solidCount
				&& stripeCount == target.stripeCount;
	}

	private boolean matchesEightPocketTarget(Pool simPool,
			TestShotTarget target) {
		if (!target.eightCorrectPocket && !target.eightWrongPocket)
			return true;
		if (pool.selectedSlotIndex == -1)
			return false;
		IBall blackBall = simPool.getBall(TEST_SHOT_BLACK_BALL_INDEX);
		if (blackBall == null || !simPool.turnPocketed.contains(blackBall)
				|| !blackBall.inSlot())
			return false;
		boolean correctPocket = blackBall.getSlot() == pool.selectedSlotIndex;
		if (target.eightCorrectPocket && !correctPocket)
			return false;
		if (target.eightWrongPocket && correctPocket)
			return false;
		return true;
	}

	private boolean matchesFoulTarget(Pool simPool, TestShotTarget target) {
		boolean scratch = simPool.getSetup().whiteBallPocketed();
		boolean noHit = !simPool.turnCollided;
		boolean foul = simPool.getSetup().isFaul() || scratch;
		boolean wrongFirst = false;
		boolean opponentFirst = false;
		boolean eightFirst = false;
		if (simPool.turnCollided && simPool.firstCollidedBall != null) {
			int currentType = simPool.m_turn != 0 ? simPool.type1
					: simPool.type0;
			int hitType = simPool.firstCollidedBall.getType();
			eightFirst = hitType == 0 && simPool.firstCollidedBall.getIndex() != 0;
			if (currentType != 0 && hitType != currentType) {
				wrongFirst = true;
				opponentFirst = hitType != 0;
			}
		}
		if (target.ballInHand && !foul)
			return false;
		if (target.legal && foul)
			return false;
		if (target.scratch && !scratch)
			return false;
		if (target.noHit && !noHit)
			return false;
		if (target.wrongFirst && !wrongFirst)
			return false;
		if (target.opponentFirst && !opponentFirst)
			return false;
		if (target.eightFirst && !eightFirst)
			return false;
		return true;
	}

	private TestShotTarget parseTestShotSpec(String spec) {
		if (spec == null)
			return null;
		TestShotTarget target = new TestShotTarget(pool.getBall().length);
		String normalized = spec.toLowerCase();
		normalized = normalized.replace(';', ',');
		normalized = normalized.replace('+', ',');
		normalized = normalized.replaceAll("\\band\\b", ",");
		StringTokenizer tokenizer = new StringTokenizer(normalized, ",");
		boolean any = false;
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().trim();
			if (token.length() == 0)
				continue;
			if (parseFoulToken(target, token)) {
				any = true;
				continue;
			}
			if (parseEightPocketToken(target, token)) {
				any = true;
				continue;
			}
			if (token.equals("solid") || token.equals("solids")) {
				target.solidCount++;
				any = true;
				continue;
			}
			if (token.equals("stripe") || token.equals("stripes")
					|| token.equals("strip")) {
				target.stripeCount++;
				any = true;
				continue;
			}
			if (token.equals("own") || token.equals("mine")
					|| token.equals("my") || token.equals("myball")
					|| token.equals("my ball")) {
				if (!addCurrentTurnTypeTarget(target, true))
					return null;
				any = true;
				continue;
			}
			if (token.equals("opponent") || token.equals("opponentball")
					|| token.equals("opponent ball") || token.equals("theirs")
					|| token.equals("their") || token.equals("theirball")
					|| token.equals("their ball")) {
				if (!addCurrentTurnTypeTarget(target, false))
					return null;
				any = true;
				continue;
			}
			int index = parseExactTestShotToken(token);
			if (index < 0 || index >= target.exact.length
					|| pool.getBall(index).inSlot())
				return null;
			target.exact[index] = true;
			any = true;
		}
		return any ? target : null;
	}

	private boolean addCurrentTurnTypeTarget(TestShotTarget target,
			boolean ownType) {
		int currentType = pool.m_turn != 0 ? pool.type1 : pool.type0;
		if (currentType == 0)
			return false;
		int type = ownType ? currentType : currentType == 1024 ? 2048 : 1024;
		if (type == 1024)
			target.solidCount++;
		else if (type == 2048)
			target.stripeCount++;
		else
			return false;
		return true;
	}

	private int parseExactTestShotToken(String token) {
		if (token.equals("0") || token.indexOf("cue") != -1
				|| token.indexOf("queue") != -1
				|| token.indexOf("white") != -1)
			return 0;
		for (int i = 0; i < token.length(); i++) {
			if (!Character.isDigit(token.charAt(i)))
				continue;
			int j = i + 1;
			while (j < token.length() && Character.isDigit(token.charAt(j)))
				j++;
			try {
				return Integer.parseInt(token.substring(i, j));
			}
			catch (NumberFormatException _ex) {
				return -1;
			}
		}
		int colorIndex = parseColorBall(token);
		if (colorIndex != -1)
			return colorIndex;
		return -1;
	}

	private boolean parseFoulToken(TestShotTarget target, String token) {
		if (token.equals("foul") || token.equals("illegal")
				|| token.equals("ball in hand") || token.equals("ballinhand")
				|| token.equals("bih")) {
			target.ballInHand = true;
			return true;
		}
		if (token.equals("legal") || token.equals("clean")) {
			target.legal = true;
			return true;
		}
		if (token.equals("scratch")) {
			target.scratch = true;
			return true;
		}
		if (token.equals("nohit") || token.equals("no hit")
				|| token.equals("miss") || token.equals("whiff")) {
			target.noHit = true;
			target.ballInHand = true;
			return true;
		}
		if (token.equals("wrongfirst") || token.equals("wrong first")
				|| token.equals("wrongball") || token.equals("wrong ball")) {
			target.wrongFirst = true;
			target.ballInHand = true;
			return true;
		}
		if (token.equals("opponentfirst") || token.equals("opponent first")
				|| token.equals("opponentball")
				|| token.equals("opponent ball")) {
			target.opponentFirst = true;
			target.ballInHand = true;
			return true;
		}
		if (token.equals("eightfirst") || token.equals("8first")
				|| token.equals("8 first") || token.equals("blackfirst")
				|| token.equals("black first")) {
			target.eightFirst = true;
			target.ballInHand = true;
			return true;
		}
		return false;
	}

	private boolean parseEightPocketToken(TestShotTarget target, String token) {
		boolean eight = token.indexOf("eight") != -1
				|| token.indexOf("8") != -1 || token.indexOf("black") != -1;
		if (!eight)
			return false;
		if (token.indexOf("correct") != -1 || token.indexOf("right") != -1
				|| token.indexOf("called") != -1 || token.indexOf("call") != -1
				|| token.indexOf("win") != -1) {
			target.eightCorrectPocket = true;
			target.exact[TEST_SHOT_BLACK_BALL_INDEX] = true;
			return true;
		}
		if (token.indexOf("wrong") != -1 || token.indexOf("incorrect") != -1
				|| token.indexOf("bad") != -1) {
			target.eightWrongPocket = true;
			target.exact[TEST_SHOT_BLACK_BALL_INDEX] = true;
			return true;
		}
		return false;
	}

	private int parseColorBall(String token) {
		boolean stripe = token.indexOf("stripe") != -1;
		boolean solid = token.indexOf("solid") != -1;
		if (token.indexOf("yellow") != -1)
			return stripe ? 9 : 1;
		if (token.indexOf("blue") != -1)
			return stripe ? 10 : 3;
		if (token.indexOf("red") != -1)
			return stripe ? 4 : 11;
		if (token.indexOf("purple") != -1)
			return stripe ? 12 : 13;
		if (token.indexOf("orange") != -1)
			return stripe ? 2 : 7;
		if (token.indexOf("green") != -1)
			return stripe ? 15 : 5;
		if (token.indexOf("maroon") != -1 || token.indexOf("brown") != -1)
			return stripe ? 14 : 8;
		if (token.indexOf("black") != -1)
			return solid ? -1 : 6;
		return -1;
	}

	private static final class CollisionHint {
		YIPoint	firstColl;
		int		collBall;
	}

	private static final class TestShotTarget {
		boolean[]	exact;
		int			solidCount;
		int			stripeCount;
		boolean		ballInHand;
		boolean		legal;
		boolean		scratch;
		boolean		noHit;
		boolean		wrongFirst;
		boolean		opponentFirst;
		boolean		eightFirst;
		boolean		eightCorrectPocket;
		boolean		eightWrongPocket;

		TestShotTarget(int ballCount) {
			exact = new boolean[ballCount];
		}

		boolean hasPocketTarget() {
			if (solidCount != 0 || stripeCount != 0)
				return true;
			for (int i = 0; i < exact.length; i++)
				if (exact[i])
					return true;
			return false;
		}
	}

	private static final class TestShotResult {
		int		attempts;
		int		cueBallIndex;
		YIPoint	cueDist;
		YIPoint	englishDist;
		YIPoint	firstColl;
		int		collBall;
	}

	private static final class TestShotHelpDialog extends YahooDialog {

		private static final String[] HELP_LINES = {
				"Use commas to combine targets, for example: solid,stripe,cue",
				"Pocket targets",
				"solid, solids - pockets one non-8 solid of any color.",
				"stripe, stripes, strip - pockets one stripe of any color.",
				"cue, queue, white, 0 - pockets the cue ball.",
				"black - pockets the 8 ball.",
				"1..15 - pockets that exact internal ball number.",
				"Color targets",
				"yellow / solid yellow = 1; stripe yellow = 9.",
				"blue / solid blue = 3; stripe blue = 10.",
				"red / solid red = 11; stripe red = 4.",
				"purple / solid purple = 13; stripe purple = 12.",
				"orange / solid orange = 7; stripe orange = 2.",
				"green / solid green = 5; stripe green = 15.",
				"brown, maroon = 8; stripe brown, stripe maroon = 14.",
				"Group shortcuts",
				"own, mine, my, myball, my ball - pockets one of the shooter's group balls.",
				"opponent, theirs, their, theirball, their ball - pockets one opponent group ball.",
				"Legality filters",
				"legal, clean - requires the shot to avoid ball-in-hand fouls.",
				"foul, illegal, ball in hand, ballinhand, bih - finds any ball-in-hand foul.",
				"Foul filters",
				"scratch - requires the cue ball to be pocketed.",
				"nohit, no hit, miss, whiff - requires no object ball contact.",
				"wrongfirst, wrong first, wrongball, wrong ball - first legal-contact test fails.",
				"opponentfirst, opponent first - first contact is the opponent's group.",
				"eightfirst, 8first, 8 first, blackfirst, black first - 8 ball is hit first too early.",
				"8-ball called-pocket filters",
				"eight correct pocket, 8 called pocket, black right pocket - pockets the 8 in the called pocket.",
				"eight wrong pocket, 8 incorrect pocket, black bad pocket - pockets the 8 outside the called pocket.",
				"Examples",
				"solid,stripe,cue - pockets one solid, one stripe, and the cue ball only.",
				"legal,orange - pockets orange and requires a legal shot.",
				"legal,eight correct pocket - pockets the 8 in the called pocket without a ball-in-hand foul.",
				"eight wrong pocket - pockets the 8 in a pocket other than the called pocket.",
				"scratch,solid,stripe - pockets solid and stripe, and scratches.",
				"wrongfirst,own - pockets one own ball after an illegal first hit.",
				"eightfirst,cue - hits the 8 first too early and scratches." };

		YahooButton	btnOk;

		TestShotHelpDialog(YahooControl container) {
			super(container, "Test Shot Help");
			for (int i = 0; i < HELP_LINES.length; i++)
				addChildObject(new YahooLabel(HELP_LINES[i], YahooLabel.yl_b,
						690), 10, 2, 2, 1, 1, 0, i);
			btnOk = new YahooButton("OK");
			addChildObject(btnOk, 10, 2, 2, 1, 1, 0, HELP_LINES.length);
		}

		@Override
		public boolean eventActionEvent(Event event, Object obj) {
			if (event.target == btnOk) {
				close();
				return true;
			}
			return super.eventActionEvent(event, obj);
		}
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

	private static final class SilentPoolHandler implements PoolHandler {
		private Pool pool;

		void setPool(Pool pool) {
			this.pool = pool;
		}

		public void Ad() {
		}

		public void Bd(int i) {
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

		public YRectangle getInArea() {
			return (YRectangle) pool.getProperty("IN_AREA");
		}

		public int getLinearFriction() {
			return pool.getIntProperty("linearFriction");
		}

		public YRectangle getPlayArea() {
			return (YRectangle) pool.getProperty("PLAY_AREA");
		}

		public YRectangle getPocketArea() {
			return (YRectangle) pool.getProperty("OUT_OF_POCKET_AREA");
		}

		public int getRotationFriction() {
			return pool.getIntProperty("rotationFriction");
		}

		public int getSideRotationFriction() {
			return pool.getIntProperty("sideRotationFriction");
		}

		public void handleColl(int i) {
		}

		public void handleFirtsColl(IBall ball) {
		}

		public void handleIterate() {
		}

		public void handleSetPos(int i, int j, int k, int l,
				YIVector vector, Vel vel) {
		}

		public void handleShiftFromIntersect() {
		}

		public void handleStart() {
		}

		public void handleStartTick(int time) {
		}

		public void handleStop(YData data) {
		}

		public void handleStopMoving() {
		}

		public void handleStopTick() {
		}

		public void handleUpdateCue(int i) {
		}

		public void handleUpdateEnglish(int i) {
		}

		public void handleUpdateStatus(boolean flag) {
		}

		public void kd(int i) {
		}

		public void logState(String s) {
		}

		public void nd(YData data) {
		}

		public void qd(IBall ball) {
		}

		public void rd() {
		}

		public void Sc() {
		}

		public void selectSlot(int i) {
		}

		public void td() {
		}

		public String Xc(int i) {
			return null;
		}

		public void xd(int i) {
		}

		public void zd(int i, boolean flag) {
		}
	}

}
