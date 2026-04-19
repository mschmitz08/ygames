package y.po;

import java.awt.Color;
import java.awt.Event;

import y.controls.YahooComponent;
import y.controls.YahooGraphics;

public class CueControlEditor extends YahooComponent {

	private static final int		POPUP_WIDTH		= 252;
	private static final int		POPUP_HEIGHT	= 150;
	private static final int		CLOSE_SIZE		= 14;
	private static final int		DEFAULT_LEFT	= 176;
	private static final int		DEFAULT_TOP		= 130;
	private static final int		DEFAULT_WIDTH	= 66;
	private static final int		DEFAULT_HEIGHT	= 14;
	private static final int		SLIDER_LEFT		= 70;
	private static final int		SLIDER_WIDTH	= 108;
	private static final int		SLIDER_HEIGHT	= 8;
	private static final int[]	ROW_Y			= { 34, 60, 86, 112 };
	private static final String[] ROW_LABELS		= { "Tap", "Max", "Delay",
			"Ramp" };
	private static final int[]	ROW_MIN			= { 1, 1, 0, 100 };
	private static final int[]	ROW_MAX			= { 40, 140, 3000, 5000 };
	private static final double	DIRECTION_STEP_UNIT	= 0.00025D;

	private final YahooPoolTable	owner;
	private final int[]			values;
	private int					activeRow;

	public CueControlEditor(YahooPoolTable owner) {
		super(true, POPUP_WIDTH, POPUP_HEIGHT);
		this.owner = owner;
		values = new int[] { YahooPoolTable.DEFAULT_CUE_TAP_UNITS,
				YahooPoolTable.DEFAULT_CUE_MAX_UNITS,
				YahooPoolTable.DEFAULT_CUE_ACCEL_DELAY_MS,
				YahooPoolTable.DEFAULT_CUE_ACCEL_RAMP_MS };
		activeRow = -1;
		visible = false;
		c = true;
	}

	@Override
	public boolean eventMouseDown(Event event, int x, int y) {
		if (!visible)
			return false;
		if (x >= POPUP_WIDTH - CLOSE_SIZE - 6 && x <= POPUP_WIDTH - 6
				&& y >= 6 && y <= 6 + CLOSE_SIZE) {
			activeRow = -1;
			owner.setCueControlPanelVisible(false);
			return true;
		}
		if (x >= DEFAULT_LEFT && x <= DEFAULT_LEFT + DEFAULT_WIDTH
				&& y >= DEFAULT_TOP && y <= DEFAULT_TOP + DEFAULT_HEIGHT) {
			activeRow = -1;
			resetToDefaults();
			return true;
		}
		activeRow = nearestRow(y);
		updateValue(activeRow, x);
		return true;
	}

	@Override
	public boolean eventMouseDrag(Event event, int x, int y) {
		if (!visible)
			return false;
		if (activeRow == -1)
			activeRow = nearestRow(y);
		updateValue(activeRow, x);
		return true;
	}

	@Override
	public boolean eventMouseUp(Event event, int x, int y) {
		if (!visible)
			return false;
		activeRow = -1;
		return true;
	}

	public int getAccelDelayMs() {
		return values[2];
	}

	public int getAccelRampMs() {
		return values[3];
	}

	public int getMaxUnits() {
		return values[1];
	}

	public int getTapUnits() {
		return values[0];
	}

	@Override
	public void paint(YahooGraphics graphics) {
		if (!visible)
			return;
		super.paint(graphics);
		graphics.setColor(new Color(0x88aeb2));
		graphics.fillRect(0, 0, getWidth(), getHeight());
		graphics.setColor(new Color(0xbfd7d9));
		graphics.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		graphics.setColor(Color.black);
		graphics.drawString("Cue Control", 76, 18);
		graphics.drawRect(POPUP_WIDTH - CLOSE_SIZE - 6, 6, CLOSE_SIZE,
				CLOSE_SIZE);
		graphics.drawLine(POPUP_WIDTH - CLOSE_SIZE - 3, 9, POPUP_WIDTH - 9,
				6 + CLOSE_SIZE - 3);
		graphics.drawLine(POPUP_WIDTH - 9, 9, POPUP_WIDTH - CLOSE_SIZE - 3,
				6 + CLOSE_SIZE - 3);
		for (int i = 0; i < ROW_LABELS.length; i++)
			paintRow(graphics, i);
		graphics.setColor(Color.black);
		graphics.drawString("Default = original", 14, 140);
		graphics.setColor(new Color(0xbfd7d9));
		graphics.fillRect(DEFAULT_LEFT, DEFAULT_TOP, DEFAULT_WIDTH,
				DEFAULT_HEIGHT);
		graphics.setColor(Color.black);
		graphics.drawRect(DEFAULT_LEFT, DEFAULT_TOP, DEFAULT_WIDTH,
				DEFAULT_HEIGHT);
		graphics.drawString("Default", DEFAULT_LEFT + 10, DEFAULT_TOP + 11);
	}

	public void setValues(int tapUnits, int maxUnits, int accelDelayMs,
			int accelRampMs) {
		values[0] = clamp(tapUnits, ROW_MIN[0], ROW_MAX[0]);
		values[1] = clamp(maxUnits, ROW_MIN[1], ROW_MAX[1]);
		values[2] = clamp(accelDelayMs, ROW_MIN[2], ROW_MAX[2]);
		values[3] = clamp(accelRampMs, ROW_MIN[3], ROW_MAX[3]);
		invalidate();
	}

	private int clamp(int value, int min, int max) {
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	private void notifyOwner() {
		if (owner != null)
			owner.handleCueControlChange(values[0], values[1], values[2],
					values[3]);
	}

	private void paintRow(YahooGraphics graphics, int row) {
		int y = ROW_Y[row];
		graphics.setColor(Color.black);
		graphics.drawString(ROW_LABELS[row], 8, y + 8);
		graphics.setColor(new Color(45, 45, 45));
		graphics.fillRect(SLIDER_LEFT, y, SLIDER_WIDTH, SLIDER_HEIGHT);
		graphics.setColor(Color.lightGray);
		graphics.drawRect(SLIDER_LEFT, y, SLIDER_WIDTH, SLIDER_HEIGHT);
		int fillWidth = valueToOffset(row);
		if (fillWidth > 0) {
			graphics.setColor(new Color(96, 132, 214));
			graphics.fillRect(SLIDER_LEFT + 1, y + 1, fillWidth,
					SLIDER_HEIGHT - 1);
		}
		int knobLeft = SLIDER_LEFT + fillWidth - 4;
		if (knobLeft < SLIDER_LEFT)
			knobLeft = SLIDER_LEFT;
		if (knobLeft > SLIDER_LEFT + SLIDER_WIDTH - 8)
			knobLeft = SLIDER_LEFT + SLIDER_WIDTH - 8;
		graphics.setColor(Color.white);
		graphics.fillRect(knobLeft, y - 3, 8, SLIDER_HEIGHT + 6);
		graphics.setColor(Color.black);
		graphics.drawRect(knobLeft, y - 3, 8, SLIDER_HEIGHT + 6);
		graphics.drawString(formatValue(row), 184, y + 8);
	}

	private void resetToDefaults() {
		values[0] = YahooPoolTable.DEFAULT_CUE_TAP_UNITS;
		values[1] = YahooPoolTable.DEFAULT_CUE_MAX_UNITS;
		values[2] = YahooPoolTable.DEFAULT_CUE_ACCEL_DELAY_MS;
		values[3] = YahooPoolTable.DEFAULT_CUE_ACCEL_RAMP_MS;
		invalidate();
		notifyOwner();
	}

	private int nearestRow(int y) {
		int bestRow = 0;
		int bestDistance = Math.abs(y - ROW_Y[0]);
		for (int i = 1; i < ROW_Y.length; i++) {
			int distance = Math.abs(y - ROW_Y[i]);
			if (distance < bestDistance) {
				bestDistance = distance;
				bestRow = i;
			}
		}
		return bestRow;
	}

	private String formatValue(int row) {
		if (row == 2 || row == 3)
			return formatSeconds(values[row]);
		return formatDegrees(values[row]);
	}

	private String formatDegrees(int units) {
		double degrees = units * DIRECTION_STEP_UNIT * 180D / Math.PI;
		int thousandths = (int) Math.round(degrees * 1000D);
		return Integer.toString(thousandths / 1000) + "."
				+ threeDigits(thousandths % 1000) + "d";
	}

	private String formatSeconds(int millis) {
		int hundredths = (int) Math.round(millis / 10D);
		return Integer.toString(hundredths / 100) + "."
				+ twoDigits(hundredths % 100) + "s";
	}

	private String twoDigits(int value) {
		if (value < 0)
			value = -value;
		return value < 10 ? "0" + value : Integer.toString(value);
	}

	private String threeDigits(int value) {
		if (value < 0)
			value = -value;
		if (value < 10)
			return "00" + value;
		if (value < 100)
			return "0" + value;
		return Integer.toString(value);
	}

	private void updateValue(int row, int x) {
		int relative = x - SLIDER_LEFT;
		if (relative < 0)
			relative = 0;
		if (relative > SLIDER_WIDTH)
			relative = SLIDER_WIDTH;
		int range = ROW_MAX[row] - ROW_MIN[row];
		int value = ROW_MIN[row]
				+ (relative * range + SLIDER_WIDTH / 2) / SLIDER_WIDTH;
		value = clamp(value, ROW_MIN[row], ROW_MAX[row]);
		if (values[row] != value) {
			values[row] = value;
			invalidate();
			notifyOwner();
		}
	}

	private int valueToOffset(int row) {
		int range = ROW_MAX[row] - ROW_MIN[row];
		if (range <= 0)
			return 0;
		return ((values[row] - ROW_MIN[row]) * SLIDER_WIDTH) / range;
	}
}
