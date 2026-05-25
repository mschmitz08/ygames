package y.po;

import java.awt.Color;

import y.controls.YahooControl;
import y.controls.YahooGraphics;

class PoolPhysicsPanel extends YahooControl {

	PoolPhysicsPanel() {
		super(285, 176);
	}

	@Override
	public void paint(YahooGraphics graphics) {
		paintTo(graphics);
		graphics.setColor(Color.darkGray);
		graphics.drawRect(0, 7, width - 1, height - 8);
		graphics.setColor(getBackColor());
		graphics.fillRect(10, 0, 142, 14);
		graphics.setColor(Color.black);
		graphics.drawString("Ball & table physics", 14, 11);
		lo(graphics, true);
	}
}
