package y.po;

import java.awt.Color;
import java.awt.Event;

import y.controls.YahooComponent;
import y.controls.YahooGraphics;

public class TableColorEditor extends YahooComponent {

	private static final int POPUP_WIDTH = 236;
	private static final int POPUP_HEIGHT = 128;
	private static final int CLOSE_SIZE = 14;
	private static final int SLIDER_LEFT = 54;
	private static final int SLIDER_WIDTH = 124;
	private static final int SLIDER_HEIGHT = 8;
	private static final int[] ROW_Y = { 34, 60, 86 };
	private static final String[] ROW_LABELS = { "Red", "Green", "Blue" };

	private final YahooPoolTable owner;
	private final int[] values;
	private int activeRow;

	public TableColorEditor(YahooPoolTable owner) {
		super(true, POPUP_WIDTH, POPUP_HEIGHT);
		this.owner = owner;
		values = new int[] { 45, 109, 43 };
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
			owner.setCustomTableColorPanelVisible(false);
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

	public Color getColor() {
		return new Color(values[0], values[1], values[2]);
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
		graphics.drawString("Custom RGB", 76, 18);
		graphics.drawRect(POPUP_WIDTH - CLOSE_SIZE - 6, 6, CLOSE_SIZE, CLOSE_SIZE);
		graphics.drawLine(POPUP_WIDTH - CLOSE_SIZE - 3, 9, POPUP_WIDTH - 9,
				6 + CLOSE_SIZE - 3);
		graphics.drawLine(POPUP_WIDTH - 9, 9, POPUP_WIDTH - CLOSE_SIZE - 3,
				6 + CLOSE_SIZE - 3);
		for (int i = 0; i < ROW_LABELS.length; i++)
			paintRow(graphics, i);
		Color preview = getColor();
		graphics.setColor(preview);
		graphics.fillRect(144, 102, 78, 18);
		graphics.setColor(Color.white);
		graphics.drawRect(144, 102, 78, 18);
		graphics.drawString(toHex(preview), 150, 116);
	}

	public void setColor(Color color) {
		if (color == null)
			return;
		values[0] = color.getRed();
		values[1] = color.getGreen();
		values[2] = color.getBlue();
		invalidate();
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
			owner.handleCustomTableColorChange(getColor());
	}

	private void paintRow(YahooGraphics graphics, int row) {
		int y = ROW_Y[row];
		graphics.setColor(Color.black);
		graphics.drawString(ROW_LABELS[row], 8, y + 8);
		graphics.setColor(new Color(45, 45, 45));
		graphics.fillRect(SLIDER_LEFT, y, SLIDER_WIDTH, SLIDER_HEIGHT);
		graphics.setColor(Color.lightGray);
		graphics.drawRect(SLIDER_LEFT, y, SLIDER_WIDTH, SLIDER_HEIGHT);
		int fillWidth = (values[row] * SLIDER_WIDTH) / 255;
		if (fillWidth > 0) {
			graphics.setColor(row == 0 ? new Color(204, 60, 60)
					: row == 1 ? new Color(74, 170, 82) : new Color(66, 132, 224));
			graphics.fillRect(SLIDER_LEFT + 1, y + 1, fillWidth, SLIDER_HEIGHT - 1);
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
		graphics.drawString(Integer.toString(values[row]), 188, y + 8);
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

	private String toHex(Color color) {
		return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(),
				color.getBlue());
	}

	private void updateValue(int row, int x) {
		int relative = x - SLIDER_LEFT;
		if (relative < 0)
			relative = 0;
		if (relative > SLIDER_WIDTH)
			relative = SLIDER_WIDTH;
		int value = clamp((relative * 255 + SLIDER_WIDTH / 2) / SLIDER_WIDTH);
		if (values[row] != value) {
			values[row] = value;
			invalidate();
			notifyOwner();
		}
	}
}
