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

	PoolPercentSlider(PoolTableCreatorDialog owner, int index, int min, int max,
			int value) {
		super(true, 116, 18);
		this.owner = owner;
		this.index = index;
		shotControl = false;
		this.min = min;
		this.max = max;
		this.value = clamp(value);
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
			graphics.setColor(new Color(176, 34, 48));
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
		graphics.setColor(Color.white);
		graphics.fillRect(knobLeft, TRACK_TOP - 3, KNOB_WIDTH, TRACK_HEIGHT + 6);
		graphics.setColor(Color.black);
		graphics.drawRect(knobLeft, TRACK_TOP - 3, KNOB_WIDTH, TRACK_HEIGHT + 6);
	}

	private int clamp(int value) {
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	private void updateValue(int x) {
		int relative = x - TRACK_LEFT;
		if (relative < 0)
			relative = 0;
		if (relative > TRACK_WIDTH)
			relative = TRACK_WIDTH;
		int updated = min + (relative * (max - min) + TRACK_WIDTH / 2)
				/ TRACK_WIDTH;
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

	private int valueToOffset(int value) {
		return ((value - min) * TRACK_WIDTH) / (max - min);
	}
}
