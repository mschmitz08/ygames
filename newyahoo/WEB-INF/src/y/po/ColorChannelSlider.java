package y.po;

import java.awt.Color;
import java.awt.Event;

import y.controls.YahooComponent;
import y.controls.YahooGraphics;

public class ColorChannelSlider extends YahooComponent {

	private static final int TRACK_LEFT = 8;
	private static final int TRACK_TOP = 8;
	private static final int TRACK_WIDTH = 130;
	private static final int TRACK_HEIGHT = 8;
	private static final int KNOB_WIDTH = 8;

	private final YahooPoolTable owner;
	private final char channel;
	private final Color accent;
	private int value;

	public ColorChannelSlider(YahooPoolTable owner, char channel, Color accent) {
		super(true, 150, 24);
		this.owner = owner;
		this.channel = channel;
		this.accent = accent;
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

	public int getValue() {
		return value;
	}

	@Override
	public void paint(YahooGraphics graphics) {
		super.paint(graphics);
		graphics.setColor(new Color(45, 45, 45));
		graphics.fillRect(TRACK_LEFT, TRACK_TOP, TRACK_WIDTH, TRACK_HEIGHT);
		graphics.setColor(new Color(210, 210, 210));
		graphics.drawRect(TRACK_LEFT, TRACK_TOP, TRACK_WIDTH, TRACK_HEIGHT);
		int fillWidth = valueToOffset(value);
		if (fillWidth > 0) {
			graphics.setColor(accent);
			graphics.fillRect(TRACK_LEFT + 1, TRACK_TOP + 1, fillWidth,
					TRACK_HEIGHT - 1);
		}
		int knobLeft = TRACK_LEFT + valueToOffset(value) - KNOB_WIDTH / 2;
		if (knobLeft < TRACK_LEFT)
			knobLeft = TRACK_LEFT;
		if (knobLeft > TRACK_LEFT + TRACK_WIDTH - KNOB_WIDTH)
			knobLeft = TRACK_LEFT + TRACK_WIDTH - KNOB_WIDTH;
		graphics.setColor(Color.white);
		graphics.fillRect(knobLeft, TRACK_TOP - 3, KNOB_WIDTH, TRACK_HEIGHT + 6);
		graphics.setColor(Color.black);
		graphics.drawRect(knobLeft, TRACK_TOP - 3, KNOB_WIDTH, TRACK_HEIGHT + 6);
	}

	public void setValue(int value) {
		int clamped = clamp(value);
		if (this.value != clamped) {
			this.value = clamped;
			invalidate();
		}
	}

	private int clamp(int value) {
		if (value < 0)
			return 0;
		if (value > 255)
			return 255;
		return value;
	}

	private void notifyOwner() {
		if (owner != null)
			owner.handleTableColorSliderChange(channel, value);
	}

	private void updateValue(int x) {
		int relative = x - TRACK_LEFT;
		if (relative < 0)
			relative = 0;
		if (relative > TRACK_WIDTH)
			relative = TRACK_WIDTH;
		int updated = (relative * 255 + TRACK_WIDTH / 2) / TRACK_WIDTH;
		if (updated != value) {
			value = updated;
			invalidate();
			notifyOwner();
		}
	}

	private int valueToOffset(int value) {
		return (value * TRACK_WIDTH) / 255;
	}
}
