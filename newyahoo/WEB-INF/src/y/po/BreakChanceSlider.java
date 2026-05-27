package y.po;

import java.awt.Color;
import java.awt.Event;

import y.controls.YahooComponent;
import y.controls.YahooGraphics;

class BreakChanceSlider extends YahooComponent {

	private static final int	KNOB_WIDTH	= 8;
	private static final int	TRACK_HEIGHT	= 8;
	private static final int	TRACK_LEFT	= 8;
	private static final int	TRACK_TOP	= 8;
	private static final int	TRACK_WIDTH	= 130;

	private final PoolTableCreatorDialog	owner;
	private int				value;

	BreakChanceSlider(PoolTableCreatorDialog owner) {
		super(true, 150, 24);
		this.owner = owner;
		value = 50;
	}

	@Override
	public boolean eventMouseDown(Event event, int x, int y) {
		updateValue(x);
		return true;
	}

	@Override
	public boolean eventMouseDrag(Event event, int x, int y) {
		updateValue(x);
		return true;
	}

	int getValue() {
		return value;
	}

	@Override
	public void paint(YahooGraphics graphics) {
		super.paint(graphics);
		graphics.setColor(Color.darkGray);
		graphics.fillRect(TRACK_LEFT, TRACK_TOP, TRACK_WIDTH, TRACK_HEIGHT);
		int fillWidth = valueToOffset(value);
		if (fillWidth > 0) {
			graphics.setColor(getFillColor());
			graphics.fillRect(TRACK_LEFT + 1, TRACK_TOP + 1, fillWidth,
					TRACK_HEIGHT - 1);
		}
		graphics.setColor(Color.black);
		graphics.drawRect(TRACK_LEFT, TRACK_TOP, TRACK_WIDTH, TRACK_HEIGHT);
		int knobLeft = TRACK_LEFT + fillWidth - KNOB_WIDTH / 2;
		if (knobLeft < TRACK_LEFT)
			knobLeft = TRACK_LEFT;
		if (knobLeft > TRACK_LEFT + TRACK_WIDTH - KNOB_WIDTH)
			knobLeft = TRACK_LEFT + TRACK_WIDTH - KNOB_WIDTH;
		graphics.setColor(getFillColor());
		graphics.fillRect(knobLeft, TRACK_TOP - 3, KNOB_WIDTH, TRACK_HEIGHT + 6);
		graphics.setColor(Color.black);
		graphics.drawRect(knobLeft, TRACK_TOP - 3, KNOB_WIDTH, TRACK_HEIGHT + 6);
	}

	void setValue(int value) {
		int clamped = clamp(value);
		if (this.value != clamped) {
			this.value = clamped;
			invalidate();
		}
	}

	private int clamp(int value) {
		if (value < 0)
			return 0;
		if (value > 100)
			return 100;
		return value;
	}

	private void updateValue(int x) {
		int relative = x - TRACK_LEFT;
		if (relative < 0)
			relative = 0;
		if (relative > TRACK_WIDTH)
			relative = TRACK_WIDTH;
		int updated = (relative * 100 + TRACK_WIDTH / 2) / TRACK_WIDTH;
		if (updated != value) {
			value = updated;
			invalidate();
			if (owner != null)
				owner.handleBreakChanceSliderChange(value);
		}
	}

	private int valueToOffset(int value) {
		return (value * TRACK_WIDTH) / 100;
	}

	private Color getFillColor() {
		if (value <= 50)
			return blend(new Color(45, 105, 210), new Color(235, 205, 55),
					value, 50);
		return blend(new Color(235, 205, 55), new Color(190, 35, 45),
				value - 50, 50);
	}

	private Color blend(Color low, Color high, int position, int range) {
		if (range <= 0)
			return high;
		if (position < 0)
			position = 0;
		if (position > range)
			position = range;
		int red = low.getRed()
				+ (high.getRed() - low.getRed()) * position / range;
		int green = low.getGreen()
				+ (high.getGreen() - low.getGreen()) * position / range;
		int blue = low.getBlue()
				+ (high.getBlue() - low.getBlue()) * position / range;
		return new Color(red, green, blue);
	}
}
