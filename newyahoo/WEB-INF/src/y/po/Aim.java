// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) fieldsfirst

package y.po;

import java.awt.Color;
import java.awt.Image;
import java.util.Vector;

import y.controls.YahooComponent;
import y.controls.YahooGraphics;

import common.po.YLine;
import common.po.YPoint;

// Referenced classes of package y.po:
// _cls78, _cls29, _cls45, _cls151,
// _cls169, _cls116

public class Aim extends YahooComponent {
	Color			a_e;
	Color			a_h;
	Image			a_f;
	int				a_g;
	PoolAreaHandler	table;
	Vector<YLine>	line;
	Vector<YPoint>	path;

	Aim(int j, int k, int l, PoolAreaHandler _pcls29) {
		super(j, k);
		line = new Vector<YLine>();
		path = new Vector<YPoint>();
		a_e = new Color(214, 224, 170);
		a_h = new Color(166, 185, 104);
		a_f = YahooPoolImageList.loadImages().q;
		a_g = l;
		table = _pcls29;
		Sn(true);
		setCoords(0, 0);
	}

	public void add(YLine line1, YLine line2, YLine line3, YPoint path) {
		line.add(line1);
		line.add(line2);
		line.add(line3);
		this.path.add(path);
		setCoords(0, 0);
	}

	public void clear() {
		line.clear();
		path.clear();
	}

	@Override
	public synchronized void paint(YahooGraphics yahooGraphics) {
		super.paint(yahooGraphics);
		if (table.cueActive()) {
			for (int i = 0; i < line.size(); i++) {
				YLine b = line.elementAt(i);
				if (i % 3 == 0)
					yahooGraphics.setColor(a_e);
				else
					yahooGraphics.setColor(a_h);
				yahooGraphics.drawLine(b.a, b.b, b.c, b.d);
				yahooGraphics.drawLine(b.a + 1, b.b, b.c + 1, b.d);
			}
			for (int i = 0; i < path.size(); i++) {
				YPoint h = path.elementAt(i);
				yahooGraphics.drawImage(a_f, (int) h.x - 10, (int) h.y - 10, null);
			}
		}
	}
}
