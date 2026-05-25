// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) fieldsfirst

package y.po;

import java.util.Hashtable;

import y.utils.StringLookuper;
import y.ycontrols.CustomTableOptions;
import y.ycontrols.TableCreator;
import y.yutils.AbstractYahooApplet;
import y.yutils.YahooTable;

import common.po.Pool;
import common.yutils.Game;

// Referenced classes of package y.po:
// _cls19, _cls29, PoolTableCreator, _cls57,
// _cls14, _cls174, _cls95, _cls97

public class PoolTableOptions extends CustomTableOptions {
	AbstractYahooApplet	applet;

	public PoolTableOptions(AbstractYahooApplet applet) {
		this.applet = applet;
	}

	@Override
	public Game createGame(Hashtable<String, String> properties) {
		return new Pool();
	}

	@Override
	public TableCreator createTableCreator() {
		return new PoolTableCreator();
	}

	@Override
	public String getOptions(Hashtable<String, String> hashtable,
			StringLookuper _pcls14) {
		StringBuffer stringbuffer = new StringBuffer();
		if (Pool.isTraining(hashtable))
			return null;
		if (Pool.isNineBallGame(hashtable))
			stringbuffer.append(_pcls14.lookupString(0x665016b3));
		else
			stringbuffer.append(_pcls14.lookupString(0x665016b2));
		stringbuffer.append(" ");
		int i = Pool.getTimer(hashtable);
		if (i != -1)
			stringbuffer.append(String.valueOf(i)
					+ _pcls14.lookupString(0x6650132f));
		else
			stringbuffer.append(_pcls14.lookupString(0x6650132d));
		if (hashtable.containsKey("rd"))
			stringbuffer.append(_pcls14.lookupString(0x6650132e));
		if (hashtable.containsKey("breakDeterministic"))
			stringbuffer.append(" deterministic break");
		else if (hashtable.containsKey("breakPocketCap"))
			stringbuffer.append(" break<=" + hashtable.get("breakPocketCap")
					+ "%");
		appendPhysicsOption(stringbuffer, hashtable, "linearFriction", " slide");
		appendPhysicsOption(stringbuffer, hashtable, "rotationFriction", " roll");
		appendPhysicsOption(stringbuffer, hashtable, "sideRotationFriction",
				" spin");
		appendPhysicsOption(stringbuffer, hashtable, "railBounce", " rail");
		appendPhysicsOption(stringbuffer, hashtable, "railSpinTransfer",
				" rail-spin");
		appendPhysicsOption(stringbuffer, hashtable, "railSideSpin",
				" rail-side");
		appendPhysicsOption(stringbuffer, hashtable, "maxCuePower", " power");
		appendPhysicsOption(stringbuffer, hashtable, "cueForce", " force");
		appendPhysicsOption(stringbuffer, hashtable, "spinEffect", " english");
		appendPhysicsOption(stringbuffer, hashtable, "ballRadius", " size");
		appendPhysicsOption(stringbuffer, hashtable, "collisionEnergy",
				" collision");
		String speed = hashtable.get("animationSpeedPct");
		if (speed != null)
			stringbuffer.append(" animation=" + speed + "%");
		return new String(stringbuffer);
	}

	private void appendPhysicsOption(StringBuffer stringbuffer,
			Hashtable<String, String> hashtable, String key, String label) {
		String value = hashtable.get("physics." + key + "Pct");
		if (value != null)
			stringbuffer.append(label + "=" + value + "%");
	}

	@Override
	public int getSitCount() {
		return 2;
	}

	@Override
	public String getVersion() {
		return "i";// "e", "g", "h"
	}

	@Override
	public int isTraining(Hashtable<String, String> hashtable) {
		return hashtable.containsKey("training")
				|| hashtable.containsKey("automat") ? 1 : 2;
	}

	@Override
	public YahooTable newTable() {
		return new YahooPoolTable();
	}

	@Override
	public String sa(StringLookuper _pcls14, int i) {
		return "";
	}
}
