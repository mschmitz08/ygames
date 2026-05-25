// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) fieldsfirst

package y.po;

import java.awt.Color;
import java.awt.Event;

import y.controls.YahooButton;
import y.controls.YahooCheckBox;
import y.controls.YahooControl;
import y.controls.YahooLabel;
import y.controls.YahooNumericTextBox;
import y.dialogs.YahooDialog;
import y.ycontrols.TableCreator;
import y.ycontrols.TableDescription;

// Referenced classes of package y.po:
// _cls27, _cls56, _cls97, _cls3,
// _cls17, _cls0, PoolTableCreator, _cls137,
// _cls105, _cls168, _cls35, _cls133,
// _cls80, _cls79, _cls87, _cls136

class PoolTableCreatorDialog extends YahooDialog {

	TableCreator		tableCreator;
	YahooCheckBox		chkRated;
	YahooCheckBox		chkTraining;
	YahooCheckBox		chkNineBallTraining;
	YahooCheckBox		chkAutomat;
	YahooCheckBox		chkEightBallGame;
	YahooCheckBox		chkNineBallGame;
	YahooCheckBox		chkTimer;
	YahooCheckBox		chkForceForfeit;
	YahooCheckBox		chkBreakDeterministic;
	YahooCheckBox		chkBreakAdaptive;
	BreakChanceSlider	breakChanceSlider;
	YahooLabel		breakChanceLabel;
	YahooControl		breakOptions;
	BreakBehaviorPanel	breakPanel;
	PoolPhysicsPanel	physicsPanel;
	PoolPercentSlider	physicsSliders[];
	YahooLabel		physicsValueLabels[];
	PoolShotControlPanel	shotPanel;
	PoolPercentSlider	shotSliders[];
	YahooLabel		shotValueLabels[];
	PoolAnimationPanel	animationPanel;
	PoolPercentSlider	animationSpeedSlider;
	YahooLabel		animationSpeedValueLabel;
	YahooControl		h;
	YahooControl		i;
	YahooControl		j;
	YahooNumericTextBox	txtTimer;
	YahooButton			l;
	YahooButton			m;
	YahooButton			btnHelp;
	YahooControl		ptc_n;
	TableDescription	ptc_o;
	static final String	PHYSICS_PROPERTY_KEYS[] = { "linearFriction",
			"rotationFriction", "sideRotationFriction", "railBounce",
			"railSpinTransfer", "railSideSpin" };
	static final String	PHYSICS_PROPERTY_LABELS[] = { "Slide friction:",
			"Roll friction:", "Side spin friction:", "Rail bounce:",
			"Rail spin transfer:", "Rail side spin:" };
	static final String	SHOT_PROPERTY_KEYS[] = { "maxCuePower",
			"cueForce", "spinEffect", "ballRadius", "collisionEnergy" };
	static final String	SHOT_PROPERTY_LABELS[] = { "Max cue power:",
			"Cue force:", "Spin effectiveness:", "Ball size:",
			"Collision energy:" };
	static final String	ANIMATION_SPEED_PROPERTY = "animationSpeedPct";
	static final int	ANIMATION_SPEED_INDEX = -1;

	PoolTableCreatorDialog(TableCreator _pcls97, YahooControl _pcls79) {
		super(_pcls79, _pcls97.getApplet().lookupString(0x66501402));
		txtTimer = new YahooNumericTextBox(_pcls97.applet.getTimerHandler(),
				"30", 20);
		ptc_n = _pcls79;
		l = new YahooButton(_pcls97.getApplet().lookupString(0x66501404));
		m = new YahooButton(_pcls97.getApplet().lookupString(0x66501400));
		tableCreator = _pcls97;
		String category = _pcls97.getApplet().getParameter("category");
		chkTraining = new YahooCheckBox("8-Ball Training", null, category != null
				&& !category.equals("social"));
		addChildObject(chkTraining, 17, 0, 0, 2, 1, 0, 0);
		chkTraining.setChecked(false);
		chkNineBallTraining = new YahooCheckBox("9-Ball Training",
				chkTraining, category != null && !category.equals("social"));
		if (_pcls97.getApplet().lookupString(0x6650179d).equals("y"))
			addChildObject(chkNineBallTraining, 17, 0, 0, 2, 1, 0, 1);
		chkNineBallTraining.setChecked(false);
		chkEightBallGame = new YahooCheckBox("8-Ball", chkTraining, category != null && !category.equals("social"));
		addChildObject(chkEightBallGame, 17, 0, 0, 2, 1, 0, 2);
		chkEightBallGame.setChecked(true);
		chkNineBallGame = new YahooCheckBox("9-Ball", chkTraining,
				category != null && !category.equals("social"));
		if (_pcls97.getApplet().lookupString(0x6650179d).equals("y"))
			addChildObject(chkNineBallGame, 17, 0, 0, 2, 1, 0, 3);
		else
			addChildObject(chkNineBallGame, 17, 0, 0, 2, 1, 0, 1);
		chkNineBallGame.setChecked(false);
		chkAutomat = new YahooCheckBox("Automatic", null, category != null
				&& !category.equals("social"));
		chkAutomat.setChecked(false);
		h = new YahooControl(2);
		i = new YahooControl(0);
		chkRated = new YahooCheckBox(_pcls97.getApplet().lookupString(
				0x66501403), null, category != null
				&& !category.equals("social"));
		i.addChildObject(chkRated, 17, 0, 0, 2, 1, 0, 0);
		chkRated.setChecked(true);
		chkTimer = new YahooCheckBox("");
		i.addChildObject(chkTimer, 17, 0, 0, 1, 1, 0, 1);
		chkForceForfeit = new YahooCheckBox("Allow Force Forfeit");
		chkForceForfeit.setChecked(true);
		i.addChildObject(chkForceForfeit, 17, 0, 0, 2, 1, 0, 2);
		j = new YahooControl(2);
		YahooControl _lcls79 = new YahooControl(0);
		_lcls79.addChildObject(new YahooLabel(_pcls97.getApplet().lookupString(
				0x665013fc)), 17, 0, 0, 1, 1, 0, 0);
		_lcls79.addChildObject(txtTimer, 13, 0, 0, 1, 1, 1, 0);
		_lcls79.addChildObject(new YahooLabel(_pcls97.getApplet().lookupString(
				0x6650140a)), 13, 0, 0, 1, 1, 2, 0);
		txtTimer.setEnabled(true);
		j.addChildObject(new YahooLabel("Shot timer"), 0);
		j.addChildObject(_lcls79, 1);
		j.qo(0);
		i.addChildObject(j, 17, 0, 0, 1, 1, 1, 1);
		h.addChildObject(new YahooControl(), 0);
		h.addChildObject(i, 1);
		addChildObject(h, 17, 0, 0, 2, 1, 0, 4);
		h.qo(1);
		chkBreakDeterministic = new YahooCheckBox("Deterministic break", null,
				true);
		chkBreakAdaptive = new YahooCheckBox("Non-deterministic break",
				chkBreakDeterministic, false);
		breakPanel = new BreakBehaviorPanel();
		breakPanel.addChildObject(chkBreakDeterministic, 12, 23, false);
		breakPanel.addChildObject(chkBreakAdaptive, 12, 44, false);
		breakOptions = new YahooControl(2);
		breakChanceSlider = new BreakChanceSlider(this);
		breakChanceLabel = new YahooLabel("Max pocket chance: 50%");
		YahooControl breakChanceControls = new YahooControl(0);
		breakChanceControls.addChildObject(breakChanceLabel, 17, 0, 0, 1, 1, 0,
				0);
		breakChanceControls.addChildObject(breakChanceSlider, 17, 0, 0, 1, 1,
				0, 1);
		breakOptions.addChildObject(new YahooControl(), 0);
		breakOptions.addChildObject(breakChanceControls, 1);
		breakPanel.addChildObject(breakOptions, 12, 68, false);
		breakOptions.qo(0);
		addChildObject(breakPanel, 17, 0, 0, 1, 4, 2, 0, 12, 0, 0, 0);
		physicsPanel = new PoolPhysicsPanel();
		physicsSliders = new PoolPercentSlider[PHYSICS_PROPERTY_KEYS.length];
		physicsValueLabels = new YahooLabel[PHYSICS_PROPERTY_KEYS.length];
		for (int p = 0; p < PHYSICS_PROPERTY_KEYS.length; p++) {
			physicsPanel.addChildObject(new YahooLabel(PHYSICS_PROPERTY_LABELS[p]),
					12, 24 + p * 23, false);
			physicsSliders[p] = new PoolPercentSlider(this, p, 0, 200, 100);
			physicsPanel.addChildObject(physicsSliders[p], 130, 22 + p * 23,
					false);
			physicsValueLabels[p] = new YahooLabel("100%");
			physicsPanel.addChildObject(physicsValueLabels[p], 247,
					24 + p * 23, false);
		}
		addChildObject(physicsPanel, 17, 0, 0, 1, 7, 2, 4, 12, 0, 0, 0);
		shotPanel = new PoolShotControlPanel();
		shotSliders = new PoolPercentSlider[SHOT_PROPERTY_KEYS.length];
		shotValueLabels = new YahooLabel[SHOT_PROPERTY_KEYS.length];
		for (int s = 0; s < SHOT_PROPERTY_KEYS.length; s++) {
			shotPanel.addChildObject(new YahooLabel(SHOT_PROPERTY_LABELS[s]), 12,
					24 + s * 23, false);
			int minPercent = "ballRadius".equals(SHOT_PROPERTY_KEYS[s]) ? 10 : 0;
			shotSliders[s] = new PoolPercentSlider(this, s, true, minPercent, 200,
					100);
			shotPanel.addChildObject(shotSliders[s], 130, 22 + s * 23, false);
			shotValueLabels[s] = new YahooLabel("100%");
			shotPanel.addChildObject(shotValueLabels[s], 247, 24 + s * 23,
					false);
		}
		addChildObject(shotPanel, 17, 0, 0, 1, 6, 0, 10, 0, 12, 12, 0);
		animationPanel = new PoolAnimationPanel();
		animationPanel.addChildObject(new YahooLabel("Animation speed:"), 12, 28,
				false);
		animationSpeedSlider = new PoolPercentSlider(this, ANIMATION_SPEED_INDEX,
				true, 5, 500, 100);
		animationPanel.addChildObject(animationSpeedSlider, 130, 26, false);
		animationSpeedValueLabel = new YahooLabel("100%");
		animationPanel.addChildObject(animationSpeedValueLabel, 247, 28, false);
		addChildObject(animationPanel, 17, 0, 0, 1, 4, 2, 11, 12, 0, 0, 0);
		ptc_o = new TableDescription(_pcls97.getApplet().getTimerHandler(),
				_pcls97.getApplet());
		addChildObject(ptc_o, 2, 1, 0, 8);
		YahooControl _lcls79_1 = new YahooControl(1);
		addChildObject(_lcls79_1, 10, 0, 0, 2, 1, 1, 15);
		_lcls79_1.addChildObject(l, 0, 0, 2);
		_lcls79_1.addChildObject(m, 1, 0, 2);
		btnHelp = new YahooButton("Help");
		_lcls79_1.addChildObject(btnHelp, 2, 0, 2);
		show();
	}

	void handleBreakChanceSliderChange(int value) {
		if (breakChanceLabel != null)
			breakChanceLabel.setCaption("Max pocket chance: " + value + "%");
	}

	private void updateBreakOptionsVisibility() {
		if (breakOptions != null)
			breakOptions.qo(chkBreakAdaptive.isChecked() ? 1 : 0);
	}

	void handlePhysicsSliderChange(int index, int value) {
		if (physicsValueLabels != null && index >= 0
				&& index < physicsValueLabels.length
				&& physicsValueLabels[index] != null)
			physicsValueLabels[index].setCaption(value + "%");
	}

	void handleShotSliderChange(int index, int value) {
		if (index == ANIMATION_SPEED_INDEX) {
			if (animationSpeedValueLabel != null)
				animationSpeedValueLabel.setCaption(value + "%");
			return;
		}
		if (shotValueLabels != null && index >= 0 && index < shotValueLabels.length
				&& shotValueLabels[index] != null)
			shotValueLabels[index].setCaption(value + "%");
	}

	@Override
	public boolean eventActionEvent(Event event, Object obj) {
		if (event.target == l) {
			boolean flag = false;
			if (chkRated.isChecked())
				tableCreator.addProperty("rd", "");
			if (!chkForceForfeit.isChecked())
				tableCreator.addProperty("ff", "");
			if (chkTraining.isChecked() || chkNineBallTraining.isChecked())
				tableCreator.addProperty("training", "");
			if (chkAutomat.isChecked())
				tableCreator.addProperty("automat", "");
			if (chkEightBallGame.isChecked()) {
				tableCreator.addProperty("eightBallGame", "");
				if (chkTimer.isChecked())
					tableCreator.addProperty("timer", txtTimer.getText());
			}
			if (chkNineBallGame.isChecked()
					|| chkNineBallTraining.isChecked()) {
				tableCreator.addProperty("nineBallGame", "");
				flag = true;
				if (chkNineBallGame.isChecked() && chkTimer.isChecked())
					tableCreator.addProperty("timer", txtTimer.getText());
			}
			if (chkBreakAdaptive.isChecked())
				tableCreator.addProperty("breakPocketCap", String
						.valueOf(breakChanceSlider.getValue()));
			else
				tableCreator.addProperty("breakDeterministic", "");
			for (int p = 0; p < PHYSICS_PROPERTY_KEYS.length; p++)
				if (physicsSliders[p].getValue() != 100)
					tableCreator.addProperty("physics."
							+ PHYSICS_PROPERTY_KEYS[p] + "Pct", String
							.valueOf(physicsSliders[p].getValue()));
			for (int s = 0; s < SHOT_PROPERTY_KEYS.length; s++)
				if (shotSliders[s].getValue() != 100)
					tableCreator.addProperty("physics." + SHOT_PROPERTY_KEYS[s]
							+ "Pct", String.valueOf(shotSliders[s].getValue()));
			if (animationSpeedSlider.getValue() != 100)
				tableCreator.addProperty(ANIMATION_SPEED_PROPERTY, String
						.valueOf(animationSpeedSlider.getValue()));
			if (ptc_o != null)
				ptc_o.Qa(tableCreator);
			tableCreator.makeTable();
			close();
			return true;
		}
		if (event.target != chkRated)
			if (event.target == chkBreakAdaptive
					|| event.target == chkBreakDeterministic) {
				updateBreakOptionsVisibility();
			}
			else if (event.target == chkTimer) {
				if (chkTimer.isChecked())
					j.qo(1);
				else
					j.qo(0);
			}
			else if (event.target instanceof YahooCheckBox) {
				if (chkEightBallGame.isChecked() || chkNineBallGame.isChecked()) {
					h.qo(1);
				}
				else {
					h.qo(0);
					chkRated.setChecked(false);
				}
			}
			else if (event.target == m) {
				tableCreator.cancel();
				return true;
			}
			else if (event.target == btnHelp) {
				new PoolOptionsHelpDialog(ptc_n);
				return true;
			}
		return false;
	}

	private static final class PoolOptionsHelpDialog extends YahooDialog {

		private static final String[] HELP_LINES = {
				"Break shot behavior",
				"Deterministic break: uses the exact shot selected by the player.",
				"Non-deterministic break: opening breaks may be adjusted if they are likely to pocket a ball.",
				"Max pocket chance: target cap for simulated jittered opening breaks.",
				"0% tries to choose an adjusted opening break that pockets no ball.",
				"Ball and table physics",
				"Slide friction: higher values make sliding balls lose speed faster.",
				"Roll friction: higher values make rolling balls slow down faster.",
				"Side spin friction: higher values make english wear off faster.",
				"Rail bounce: controls speed loss at rail contact.",
				"Lower rail bounce is livelier; 0% does not make the ball stick to the rail.",
				"Rail spin transfer: changes how much roll/spin interacts with rail hits.",
				"Rail side spin: changes how side spin affects the rebound off rails.",
				"Shot power and control",
				"Max cue power: caps the highest selectable shot power.",
				"Cue force: scales the force applied by the same cue pullback.",
				"Spin effectiveness: scales how strongly english affects the cue ball.",
				"Ball size: changes physics radius and visual ball size; minimum is 10%.",
				"Collision energy: higher values retain more speed after ball-to-ball contact.",
				"Animation speed: changes how fast shot animations play out, from 5% to 500%.",
				"It does not change the shot chosen by the player.",
				"All percent values are relative to the classic default table at 100%." };

		YahooButton	btnOk;

		PoolOptionsHelpDialog(YahooControl container) {
			super(container, "Pool Option Help");
			YahooControl body = new PoolOptionsHelpPanel(HELP_LINES);
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

	private static final class PoolOptionsHelpPanel extends YahooControl {

		private final String[]	lines;

		PoolOptionsHelpPanel(String[] lines) {
			super(610, 390);
			this.lines = lines;
		}

		@Override
		public void paint(y.controls.YahooGraphics graphics) {
			graphics.setColor(new Color(192, 192, 192));
			graphics.fillRect(0, 0, width, height);
			graphics.setColor(Color.darkGray);
			graphics.drawRect(0, 0, width - 1, height - 1);
			graphics.setColor(Color.black);
			int y = 18;
			for (int i = 0; i < lines.length; i++) {
				if (isHeading(lines[i]))
					graphics.setColor(new Color(0, 0, 128));
				else
					graphics.setColor(Color.black);
				graphics.drawString(lines[i], 12, y);
				y += isHeading(lines[i]) ? 19 : 17;
			}
		}

		private boolean isHeading(String text) {
			return "Break shot behavior".equals(text)
					|| "Ball and table physics".equals(text)
					|| "Shot power and control".equals(text);
		}
	}
}
