package y.po;

import java.awt.Color;
import java.awt.Event;
import java.util.Hashtable;

import y.controls.YahooButton;
import y.controls.YahooCheckBox;
import y.controls.YahooControl;
import y.controls.YahooGraphics;
import y.controls.YahooLabel;
import y.dialogs.YahooDialog;

public class PoolSettingsViewDialog extends YahooDialog {

	private final Hashtable<String, String>	properties;
	private final YahooPoolTable				owner;
	private YahooButton					btnClose;
	private YahooButton					btnPocketHandicap;

	public PoolSettingsViewDialog(YahooControl container,
			Hashtable<String, String> properties) {
		this(null, container, properties);
	}

	public PoolSettingsViewDialog(YahooPoolTable owner, YahooControl container,
			Hashtable<String, String> properties) {
		super(container, "Table Settings");
		this.owner = owner;
		this.properties = properties;
		build();
		show();
	}

	private void build() {
		buildGameOptions();
		buildBreakOptions();
		buildPhysicsOptions();
		buildShotOptions();
		buildAnimationOptions();

		addChildObject(new ReadOnlyDescription(getValue("dc", "")), 2, 1, 0, 8);

		YahooControl buttons = new YahooControl(1);
		btnClose = new YahooButton("Close");
		buttons.addChildObject(btnClose, 0, 0, 2);
		addChildObject(buttons, 10, 0, 0, 2, 1, 1, 15);
	}

	private void buildGameOptions() {
		YahooCheckBox training8 = disabledCheckBox("8-Ball Training",
				has("training") && !has("nineBallGame"));
		addChildObject(training8, 17, 0, 0, 2, 1, 0, 0);
		YahooCheckBox training9 = disabledCheckBox("9-Ball Training",
				training8, has("training") && has("nineBallGame"));
		addChildObject(training9, 17, 0, 0, 2, 1, 0, 1);
		YahooCheckBox eightBall = disabledCheckBox("8-Ball", training8,
				!has("training") && !has("nineBallGame"));
		addChildObject(eightBall, 17, 0, 0, 2, 1, 0, 2);
		YahooCheckBox nineBall = disabledCheckBox("9-Ball", training8,
				!has("training") && has("nineBallGame"));
		addChildObject(nineBall, 17, 0, 0, 2, 1, 0, 3);

		YahooControl gameFlags = new YahooControl(0);
		gameFlags.addChildObject(disabledCheckBox("Rated game.", has("rd")), 17,
				0, 0, 2, 1, 0, 0);
		gameFlags.addChildObject(disabledCheckBox("", has("timer")), 17,
				0, 0, 1, 1, 0, 1);
		gameFlags.addChildObject(disabledCheckBox("Allow Force Forfeit",
				!has("ff")), 17, 0, 0, 2, 1, 0, 2);

		YahooControl timerPanel = new YahooControl(2);
		YahooControl timerRow = new YahooControl(0);
		timerRow.addChildObject(new YahooLabel("Time per move"), 17, 0, 0, 1,
				1, 0, 0);
		timerRow.addChildObject(new ReadOnlySmallTextValue(getValue("timer",
				"30")), 13, 0, 0, 1, 1, 1, 0);
		timerRow.addChildObject(new YahooLabel("sec."), 13, 0, 0, 1, 1, 2, 0);
		timerPanel.addChildObject(new YahooLabel("Shot timer"), 0);
		timerPanel.addChildObject(timerRow, 1);
		timerPanel.qo(has("timer") ? 1 : 0);
		gameFlags.addChildObject(timerPanel, 17, 0, 0, 1, 1, 1, 1);

		YahooControl flagWrapper = new YahooControl(2);
		flagWrapper.addChildObject(new YahooControl(), 0);
		flagWrapper.addChildObject(gameFlags, 1);
		flagWrapper.qo(1);
		addChildObject(flagWrapper, 17, 0, 0, 2, 1, 0, 4);
	}

	private void buildBreakOptions() {
		BreakBehaviorPanel panel = new BreakBehaviorPanel();
		YahooCheckBox deterministic = disabledCheckBox("Deterministic break",
				!has("breakPocketCap"));
		panel.addChildObject(deterministic, 12, 23, false);
		YahooCheckBox adaptive = disabledCheckBox("Non-deterministic break",
				deterministic, has("breakPocketCap"));
		panel.addChildObject(adaptive, 12, 44, false);

		YahooControl breakOptions = new YahooControl(2);
		YahooControl chanceControls = new YahooControl(0);
		chanceControls.addChildObject(new YahooLabel("Max pocket chance: "
				+ getValue("breakPocketCap", "0") + "%"), 17, 0, 0, 1, 1, 0,
				0);
		PoolPercentSlider slider = disabledSlider(null, 0, 100,
				getInt("breakPocketCap", 0));
		chanceControls.addChildObject(slider, 17, 0, 0, 1, 1, 0, 1);
		breakOptions.addChildObject(new YahooControl(), 0);
		breakOptions.addChildObject(chanceControls, 1);
		breakOptions.qo(has("breakPocketCap") ? 1 : 0);
		panel.addChildObject(breakOptions, 12, 68, false);

		addChildObject(panel, 17, 0, 0, 1, 4, 2, 0, 12, 0, 0, 0);
	}

	private void buildPhysicsOptions() {
		PoolPhysicsPanel panel = new PoolPhysicsPanel();
		for (int p = 0; p < PoolTableCreatorDialog.PHYSICS_PROPERTY_KEYS.length; p++) {
			panel.addChildObject(new YahooLabel(
					PoolTableCreatorDialog.PHYSICS_PROPERTY_LABELS[p]), 12,
					24 + p * 23, false);
			String key = PoolTableCreatorDialog.PHYSICS_PROPERTY_KEYS[p];
			PoolPercentSlider slider = disabledSlider(null,
					PoolTableCreatorDialog.getPhysicsMinPercent(key),
					PoolTableCreatorDialog.getPhysicsMaxPercent(key),
					getPhysicsPct(key, 100));
			panel.addChildObject(slider, 130, 22 + p * 23, false);
			panel.addChildObject(valueLabel(getPhysicsPct(key, 100) + "%"), 247,
					24 + p * 23, false);
		}
		addChildObject(panel, 17, 0, 0, 1, 7, 2, 4, 12, 0, 0, 0);
	}

	private void buildShotOptions() {
		PoolShotControlPanel panel = new PoolShotControlPanel();
		for (int s = 0; s < PoolTableCreatorDialog.SHOT_PROPERTY_KEYS.length; s++) {
			panel.addChildObject(new YahooLabel(
					PoolTableCreatorDialog.SHOT_PROPERTY_LABELS[s]), 12,
					24 + s * 23, false);
			String key = PoolTableCreatorDialog.SHOT_PROPERTY_KEYS[s];
			int minPercent = PoolTableCreatorDialog.getShotMinPercent(key);
			int maxPercent = PoolTableCreatorDialog.getShotMaxPercent(key);
			PoolPercentSlider slider = disabledSlider(null, minPercent,
					maxPercent, getPhysicsPct(key, 100));
			panel.addChildObject(slider, 130, 22 + s * 23, false);
			panel.addChildObject(valueLabel(getPhysicsPct(key, 100) + "%"), 247,
					24 + s * 23, false);
		}
		btnPocketHandicap = new YahooButton("Pocket handicap...");
		panel.addChildObject(btnPocketHandicap, 12, 140, false);
		addChildObject(panel, 17, 0, 0, 1, 6, 0, 10, 0, 12, 12, 0);
	}

	private void buildAnimationOptions() {
		PoolAnimationPanel panel = new PoolAnimationPanel();
		panel.addChildObject(new YahooLabel("Animation speed:"), 12, 28, false);
		PoolPercentSlider slider = disabledSlider(null, 25, 300,
				getInt("animationSpeedPct", 100));
		panel.addChildObject(slider, 130, 26, false);
		panel.addChildObject(valueLabel(getInt("animationSpeedPct", 100) + "%"),
				247, 28, false);
		addChildObject(panel, 17, 0, 0, 1, 4, 2, 11, 12, 0, 0, 0);
	}

	private YahooLabel valueLabel(String value) {
		return new TransparentLabel(value);
	}

	private YahooCheckBox disabledCheckBox(String caption, boolean checked) {
		return disabledCheckBox(caption, null, checked);
	}

	private YahooCheckBox disabledCheckBox(String caption, YahooCheckBox group,
			boolean checked) {
		return new ReadOnlyCheckBox(caption, group, checked);
	}

	private PoolPercentSlider disabledSlider(PoolTableCreatorDialog owner,
			int min, int max, int value) {
		PoolPercentSlider slider = new PoolPercentSlider(owner, -99, true, min,
				max, value);
		slider.setEnabled(false);
		return slider;
	}

	private boolean has(String key) {
		return properties != null && properties.containsKey(key);
	}

	private String getValue(String key, String defaultValue) {
		if (properties == null)
			return defaultValue;
		String value = properties.get(key);
		return value != null && value.length() > 0 ? value : defaultValue;
	}

	private int getPhysicsPct(String key, int defaultValue) {
		return getInt("physics." + key + "Pct", defaultValue);
	}

	private int getInt(String key, int defaultValue) {
		try {
			return Integer.parseInt(getValue(key, String.valueOf(defaultValue)));
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	private String formatHandicap(int value) {
		if (value > 0)
			return "+" + value;
		return String.valueOf(value);
	}

	@Override
	public boolean eventActionEvent(Event event, Object obj) {
		if (event.target == btnClose) {
			close();
			return true;
		}
		if (event.target == btnPocketHandicap) {
			new ReadOnlyPocketHandicapDialog(getContainer());
			return true;
		}
		return super.eventActionEvent(event, obj);
	}

	public void bringToFront() {
		YahooControl parent = getParent();
		if (parent == null)
			return;
		int dialogLeft = left;
		int dialogTop = top;
		parent.removeChildObject(this);
		parent.addChildObject(this, dialogLeft, dialogTop, true);
	}

	@Override
	public void close() {
		if (owner != null)
			owner.handlePoolSettingsViewDialogClosed(this);
		super.close();
	}

	private static final class ReadOnlyCheckBox extends YahooCheckBox {

		ReadOnlyCheckBox(String caption, YahooCheckBox group, boolean checked) {
			super(caption, group, checked);
		}

		@Override
		public boolean eventMouseDown(Event event, int x, int y) {
			return true;
		}

		@Override
		public boolean eventMouseUp(Event event, int x, int y) {
			return true;
		}
	}

	private static final class ReadOnlyDescription extends YahooControl {

		ReadOnlyDescription(String description) {
			addChildObject(new YahooLabel("Description:\n(optional)"), 1, 1, 0,
					0);
			addChildObject(new ReadOnlyTextValue(description), 11, 0, 0, 1, 1, 1,
					0);
		}
	}

	private static class ReadOnlyTextValue extends YahooLabel {

		ReadOnlyTextValue(String description) {
			super(description == null ? "" : description, YahooLabel.yl_b, 180);
		}

		@Override
		public int getHeight1() {
			return 22;
		}

		@Override
		public void paint(YahooGraphics graphics) {
			super.paint(graphics);
			graphics.setColor(Color.white);
			graphics.drawRect(0, 0, width - 1, height - 1);
			graphics.setColor(Color.black);
			graphics.drawLine(1, 1, width - 2, 1);
			graphics.drawLine(1, 1, 1, height - 2);
			graphics.setColor(Color.lightGray);
			graphics.drawLine(width - 2, 1, width - 2, height - 2);
			graphics.drawLine(1, height - 2, width - 2, height - 2);
		}
	}

	private static final class ReadOnlySmallTextValue extends ReadOnlyTextValue {

		ReadOnlySmallTextValue(String text) {
			super(text);
		}

		@Override
		public int getWidth1() {
			return 28;
		}
	}

	private static final class TransparentLabel extends YahooLabel {

		TransparentLabel(String text) {
			super(text);
			Sn(true);
		}
	}

	private final class ReadOnlyPocketHandicapDialog extends YahooDialog {

		YahooButton	btnOk;

		ReadOnlyPocketHandicapDialog(YahooControl container) {
			super(container, "Pocket Handicap");
			YahooControl body = new YahooControl(360, 100);
			int seat1 = getInt("pocketHandicap0", 0);
			int seat2 = getInt("pocketHandicap1", 0);
			body.addChildObject(new YahooLabel("Seat 1 pocket acceptance:"), 12,
					16, false);
			PoolPercentSlider slider1 = disabledSlider(null, -10, 10, seat1);
			body.addChildObject(slider1, 170, 14, false);
			body.addChildObject(new YahooLabel(formatHandicap(seat1),
					YahooLabel.yl_b, 34), 292, 16, false);
			body.addChildObject(new YahooLabel("Seat 2 pocket acceptance:"), 12,
					48, false);
			PoolPercentSlider slider2 = disabledSlider(null, -10, 10, seat2);
			body.addChildObject(slider2, 170, 46, false);
			body.addChildObject(new YahooLabel(formatHandicap(seat2),
					YahooLabel.yl_b, 34), 292, 48, false);
			body.addChildObject(new YahooLabel(
					"0 is classic. +10 is easier. -10 is harder."), 12, 78,
					false);
			addChildObject(body, 1, 1, 0, 0);
			btnOk = new YahooButton("OK");
			addChildObject(btnOk, 1, 1, 0, 1);
			show();
		}

		@Override
		public boolean eventActionEvent(Event event, Object obj) {
			if (event.target == btnOk) {
				close();
				return true;
			}
			return super.eventActionEvent(event, obj);
		}
	}
}
