package y.po;

import java.awt.Color;

import y.controls.YahooControl;
import y.controls.YahooGraphics;

class BreakBehaviorPanel extends YahooControl {

	BreakBehaviorPanel() {
		super(215, 118);
	}

	@Override
	public void paint(YahooGraphics graphics) {
		paintTo(graphics);
		graphics.setColor(Color.darkGray);
		graphics.drawRect(0, 7, width - 1, height - 8);
		graphics.setColor(getBackColor());
		graphics.fillRect(10, 0, 120, 14);
		graphics.setColor(Color.black);
		graphics.drawString("Break shot behavior", 14, 11);
		lo(graphics, true);
	}
}
