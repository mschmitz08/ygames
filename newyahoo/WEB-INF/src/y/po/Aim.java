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
	Image			a_f;
	int				a_g;
	PoolAreaHandler	table;
	Vector<YLine>	line;
	Vector<YPoint>	path;

	Aim(int j, int k, int l, PoolAreaHandler _pcls29) {
		super(j, k);
		line = new Vector<YLine>();
		path = new Vector<YPoint>();
		a_e = new Color(232, 204, 73);
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
			yahooGraphics.setColor(a_e);
			for (int i = 0; i < line.size(); i++) {
				YLine b = line.elementAt(i);
				yahooGraphics.drawLine(b.a, b.b, b.c, b.d);
				yahooGraphics.drawLine(b.a + 1, b.b, b.c + 1, b.d);
			}
			for (int i = 0; i < path.size(); i++) {
				YPoint h = path.elementAt(i);
				int lineIndex = Math.min(i * 3, line.size() - 1);
				YLine guideLine = lineIndex >= 0 ? line.elementAt(lineIndex) : null;
				int markerSize = 16;
				int imageSize = 20;
				if (guideLine != null) {
					double dx = guideLine.c - guideLine.a;
					double dy = guideLine.d - guideLine.b;
					double guideLength = Math.sqrt(dx * dx + dy * dy);
					double shrink = Math.min(1.0D, guideLength / 220D);
					markerSize = Math.max(8, (int) Math.round(16D - 8D * shrink));
					imageSize = markerSize + 4;
				}
				int markerRadius = markerSize / 2;
				int imageRadius = imageSize / 2;
				yahooGraphics.drawOval((int) h.x - markerRadius, (int) h.y - markerRadius,
						markerSize, markerSize);
				yahooGraphics.drawImage(a_f, (int) h.x - imageRadius,
						(int) h.y - imageRadius, imageSize, imageSize, null);
			}
		}
	}
}
