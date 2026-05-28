package y.po;

import java.awt.Color;
import java.awt.Event;

import y.controls.YahooComponent;
import y.controls.YahooGraphics;

class PoolPercentSlider extends YahooComponent {

	private static final int	KNOB_WIDTH	= 8;
	private static final int	TRACK_HEIGHT	= 8;
	private static final int	TRACK_LEFT	= 4;
	private static final int	TRACK_TOP	= 5;
	private static final int	TRACK_WIDTH	= 105;

	private final PoolTableCreatorDialog	owner;
	private final int				index;
	private final boolean				shotControl;
	private final int				min;
	private final int				max;
	private int					value;
	private boolean					focused;
	private boolean					enabled;

	PoolPercentSlider(PoolTableCreatorDialog owner, int index, int min, int max,
			int value) {
		super(true, 116, 18);
		this.owner = owner;
		this.index = index;
		shotControl = false;
		this.min = min;
		this.max = max;
		this.value = clamp(value);
		focused = false;
		enabled = true;
	}

	PoolPercentSlider(PoolTableCreatorDialog owner, int index,
			boolean shotControl, int min, int max, int value) {
		super(true, 116, 18);
		this.owner = owner;
		this.index = index;
		this.shotControl = shotControl;
		this.min = min;
		this.max = max;
		this.value = clamp(value);
		focused = false;
		enabled = true;
	}

	@Override
	public boolean eventMouseDown(Event event, int x, int y) {
		if (!enabled)
			return true;
		Gn(true);
		updateValue(x);
		return true;
	}

	@Override
	public boolean eventMouseDrag(Event event, int x, int y) {
		if (!enabled)
			return true;
		updateValue(x);
		return true;
	}

	int getValue() {
		return value;
	}

	@Override
	public boolean eventKeyPress(Event event, int key) {
		if (!enabled)
			return true;
		if (key == Event.LEFT || key == Event.DOWN) {
			setValue(value - getKeyStep(event));
			return true;
		}
		if (key == Event.RIGHT || key == Event.UP) {
			setValue(value + getKeyStep(event));
			return true;
		}
		if (key == Event.HOME) {
			setValue(min);
			return true;
		}
		if (key == Event.END) {
			setValue(max);
			return true;
		}
		return false;
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
		if (focused) {
			graphics.setColor(Color.white);
			graphics.drawRect(0, 0, width - 1, height - 1);
			graphics.setColor(Color.black);
			graphics.drawRect(1, 1, width - 3, height - 3);
		}
	}

	@Override
	public boolean processEvent(Event event) {
		if (event.id == Event.GOT_FOCUS) {
			focused = true;
			invalidate();
			return true;
		}
		if (event.id == Event.LOST_FOCUS) {
			focused = false;
			invalidate();
			return true;
		}
		return super.processEvent(event);
	}

	private int clamp(int value) {
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	void setEnabled(boolean enabled) {
		this.enabled = enabled;
		invalidate();
	}

	private int getKeyStep(Event event) {
		return (event.modifiers & Event.SHIFT_MASK) == Event.SHIFT_MASK ? 10 : 1;
	}

	private void setValue(int updated) {
		updated = clamp(updated);
		if (updated != value) {
			value = updated;
			invalidate();
			if (owner != null)
				if (shotControl)
					owner.handleShotSliderChange(index, value);
				else
					owner.handlePhysicsSliderChange(index, value);
		}
	}

	private void updateValue(int x) {
		int relative = x - TRACK_LEFT;
		if (relative < 0)
			relative = 0;
		if (relative > TRACK_WIDTH)
			relative = TRACK_WIDTH;
		int updated = min + (relative * (max - min) + TRACK_WIDTH / 2)
				/ TRACK_WIDTH;
		setValue(updated);
	}

	private int valueToOffset(int value) {
		return ((value - min) * TRACK_WIDTH) / (max - min);
	}

	private Color getFillColor() {
		int midpoint = min + (max - min) / 2;
		if (value <= midpoint)
			return blend(new Color(45, 105, 210), new Color(235, 205, 55),
					value - min, midpoint - min);
		return blend(new Color(235, 205, 55), new Color(190, 35, 45),
				value - midpoint, max - midpoint);
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
