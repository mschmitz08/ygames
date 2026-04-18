// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) fieldsfirst

package y.controls;

import java.awt.Color;
import java.awt.Event;
import java.awt.FontMetrics;

// Referenced classes of package y.po:
// _cls78, _cls77, _cls113, _cls116

public class _cls92 extends YahooComponent {

	private static final int	SCROLLBAR_WIDTH	= 12;

	public YahooComboBox	b92;
	public int				c92;
	public int				d92;
	public FontMetrics		e92;
	private boolean			f92;

	public _cls92(YahooComboBox _pcls77, int j, int k) {
		b92 = _pcls77;
		c92 = j;
		d92 = k;
		setBackColor(((YahooComponent) _pcls77).backColor);
		f92 = false;
	}

	void cp(int j) {
		b92.ycc_d = -1;
		if (j >= 0) {
			int k = b92.ycc_e + j / e92.getHeight();
			if (k < b92.ycc_a.size())
				b92.ycc_d = k;
		}
	}

	private int getVisibleCount() {
		return b92.getPopupVisibleCount();
	}

	private int getListWidth() {
		return c92 - (b92.hasPopupScrollBar() ? SCROLLBAR_WIDTH : 0);
	}

	private boolean isScrollBarHit(int x) {
		return b92.hasPopupScrollBar() && x >= getListWidth();
	}

	private void scrollTo(int y) {
		if (!b92.hasPopupScrollBar())
			return;
		int visibleCount = getVisibleCount();
		int maxOffset = b92.ycc_a.size() - visibleCount;
		if (maxOffset <= 0) {
			b92.ycc_e = 0;
			return;
		}
		int thumbHeight = Math.max(16, (d92 * visibleCount) / b92.ycc_a.size());
		int trackHeight = d92 - thumbHeight;
		if (trackHeight <= 0) {
			b92.ycc_e = 0;
			return;
		}
		int thumbTop = y - thumbHeight / 2;
		if (thumbTop < 0)
			thumbTop = 0;
		if (thumbTop > trackHeight)
			thumbTop = trackHeight;
		b92.ycc_e = (thumbTop * maxOffset + trackHeight / 2) / trackHeight;
	}

	@Override
	public boolean eventMouseDown(Event event, int j, int k) {
		if (isScrollBarHit(j)) {
			f92 = true;
			scrollTo(k);
			invalidate();
			return true;
		}
		cp(k);
		Event event1 = b92.Zm();
		if (event1 != null)
			b92.doEvent(event1);
		return true;
	}

	@Override
	public boolean eventMouseDrag(Event event, int j, int k) {
		if (f92) {
			scrollTo(k);
			invalidate();
			return true;
		}
		cp(k);
		invalidate();
		return true;
	}

	@Override
	public boolean eventMouseMove(Event event, int j, int k) {
		if (isScrollBarHit(j))
			b92.ycc_d = -1;
		else
		cp(k);
		invalidate();
		return true;
	}

	@Override
	public boolean eventMouseUp(Event event, int j, int k) {
		f92 = false;
		return true;
	}

	@Override
	public int getHeight1() {
		return d92;
	}

	@Override
	public int getWidth1() {
		return c92;
	}

	@Override
	public void paint(YahooGraphics yahooGraphics) {
		paintTo(yahooGraphics);
		yahooGraphics.setColor(Color.black);
		yahooGraphics.drawRect(0, 0, c92, d92);
		yahooGraphics.setFont(YahooComponent.defaultFont);
		int itemHeight = b92.ycc_g.getHeight();
		int visibleCount = getVisibleCount();
		int listWidth = getListWidth();
		for (int j = 0; j < visibleCount; j++) {
			int index = b92.ycc_e + j;
			if (index >= b92.ycc_a.size())
				break;
			int k = j * itemHeight;
			yahooGraphics.setColor(Color.black);
			if (index == b92.ycc_d) {
				yahooGraphics.setColor(YahooComponent.defaultColor);
				yahooGraphics.fillRect(0, k, listWidth, itemHeight + 2);
				yahooGraphics.setColor(Color.white);
			}
			String s = b92.ycc_a.elementAt(index);
			yahooGraphics.drawString(s, 2, k + b92.ycc_g.getAscent() + 2);
		}
		if (b92.hasPopupScrollBar()) {
			int barLeft = listWidth;
			yahooGraphics.setColor(new Color(230, 230, 230));
			yahooGraphics.fillRect(barLeft, 1, SCROLLBAR_WIDTH - 1, d92 - 1);
			yahooGraphics.setColor(Color.gray);
			yahooGraphics.drawLine(barLeft, 0, barLeft, d92);
			int thumbHeight = Math.max(16, (d92 * visibleCount) / b92.ycc_a.size());
			int maxOffset = b92.ycc_a.size() - visibleCount;
			int trackHeight = d92 - thumbHeight;
			int thumbTop = maxOffset <= 0 ? 0 : (b92.ycc_e * trackHeight) / maxOffset;
			yahooGraphics.setColor(new Color(140, 140, 140));
			yahooGraphics.fillRect(barLeft + 2, thumbTop + 2, SCROLLBAR_WIDTH - 5,
					thumbHeight - 4);
			yahooGraphics.setColor(Color.darkGray);
			yahooGraphics.drawRect(barLeft + 1, thumbTop + 1, SCROLLBAR_WIDTH - 4,
					thumbHeight - 2);
		}
	}

	@Override
	public boolean processEvent(Event event) {
		if (event.target == this && event.id == Event.LOST_FOCUS) {
			if (b92.h != null)
				b92._n();
			return true;
		}
		return super.processEvent(event);
	}

	@Override
	public void realingChilds() {
		super.realingChilds();
		e92 = getFontMetrics(YahooComponent.defaultFont);
	}
}
