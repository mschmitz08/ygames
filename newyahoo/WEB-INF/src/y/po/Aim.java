// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) fieldsfirst

package y.po;

import java.awt.Color;
import java.awt.Image;
import java.util.Date;
import java.util.Vector;

import y.controls.YahooComponent;
import y.controls.YahooGraphics;

import common.po.YLine;
import common.po.YPoint;

// Referenced classes of package y.po:
// _cls78, _cls29, _cls45, _cls151,
// _cls169, _cls116

public class Aim extends YahooComponent {
	public static final int	STYLE_AUTO			= 0;
	public static final int	STYLE_OUTLINE		= 1;
	public static final int	STYLE_GREEN_OUTLINE	= 2;
	public static final int	STYLE_RED_OUTLINE	= 3;
	public static final int	STYLE_GHOST_IMAGE	= 4;

	Color			a_e;
	Color			a_h;
	Image			a_f;
	int				a_g;
	int				markerStyle;
	PoolAreaHandler	table;
	Vector<YLine>	line;
	Vector<YPoint>	path;

	Aim(int j, int k, int l, PoolAreaHandler _pcls29) {
		super(j, k);
		line = new Vector<YLine>();
		path = new Vector<YPoint>();
		a_e = new Color(99, 142, 38);
		a_h = new Color(0, 220, 0);
		a_f = YahooPoolImageList.loadImages().q;
		a_g = 20;
		markerStyle = STYLE_GHOST_IMAGE;
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

	public int getMarkerStyle() {
		return markerStyle;
	}

	private int getResolvedMarkerStyle() {
		if (markerStyle != STYLE_AUTO)
			return markerStyle;
		Date date = new Date();
		int month = date.getMonth();
		int day = date.getDate();
		// The original jar swaps in special ghost art on these calendar dates.
		// This source tree only has the generic fantom marker asset, so Auto
		// falls back to that image on the original special days and otherwise
		// keeps the default outlined marker.
		if (month == 1 && day == 14 || month == 5 && day == 3
				|| month == 9 && day == 31 || month == 0 && day == 1)
			return STYLE_GHOST_IMAGE;
		return STYLE_OUTLINE;
	}

	private Color getMarkerColor(int style) {
		switch (style) {
		case STYLE_RED_OUTLINE:
			return Color.red;

		case STYLE_GREEN_OUTLINE:
			return a_h;

		default:
			return Color.white;
		}
	}

	public void setMarkerStyle(int style) {
		markerStyle = style;
		invalidate();
	}

	@Override
	public synchronized void paint(YahooGraphics yahooGraphics) {
		super.paint(yahooGraphics);
		if (table.cueActive()) {
			for (int i = 0; i < line.size(); i++) {
				YLine b = line.elementAt(i);
				yahooGraphics.setColor(a_e);
				yahooGraphics.drawLine(b.a, b.b, b.c, b.d);
			}
			for (int i = 0; i < path.size(); i++) {
				YPoint h = path.elementAt(i);
				int style = getResolvedMarkerStyle();
				if (style == STYLE_GHOST_IMAGE && a_f != null)
					yahooGraphics.drawImage(a_f, (int) h.x - a_g / 2,
							(int) h.y - a_g / 2, a_g, a_g, null);
				else {
					yahooGraphics.setColor(getMarkerColor(style));
					yahooGraphics.drawOval((int) h.x - a_g / 2,
							(int) h.y - a_g / 2, a_g, a_g);
					if (style == STYLE_GREEN_OUTLINE)
						yahooGraphics.drawOval((int) h.x - (a_g - 2) / 2,
								(int) h.y - (a_g - 2) / 2, a_g - 2, a_g - 2);
				}
			}
		}
	}
}
