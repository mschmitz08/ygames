package y.ycontrols;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import y.controls.YahooComponent;
import y.controls.YahooGraphics;
import y.po.PoolSettingsViewDialog;
import y.po.YahooPoolTable;
import y.yutils.YahooGamesTable;

public class PoolTopMessageOverlay extends YahooComponent {

	private static final Random	RANDOM	= new Random();
	private static final Color	BANNER_BG	= new Color(118, 157, 120);
	private static final Color	BANNER_TOP	= new Color(91, 129, 93);
	private static final Color	BANNER_BOTTOM	= new Color(61, 94, 64);
	private static final Color	CHIP_BG	= new Color(198, 214, 191);
	private static final Color	CHIP_BORDER	= new Color(57, 82, 61);
	private static final Color	TAB_BG	= new Color(154, 183, 148);
	private static final Color	TAB_ACTIVE_BG	= new Color(226, 235, 207);
	private static final Color	TEXT_FG	= new Color(20, 30, 22);
	private static final Color	MUTED_FG	= new Color(48, 70, 51);
	private static final int	SETTINGS_BUTTON_WIDTH	= 132;
	private static final int	SETTINGS_BUTTON_HEIGHT	= 18;
	private static final String[] TAB_NAMES = { "Rules", "Break", "Physics",
			"Shot", "Handicap", "More" };
	private static final String[] MESSAGES = {
			"Not an ad. Just extra cue room.",
			"This space is intentionally not for advertising.",
			"No ads up here. Just a little breathing room.",
			"This area is here for visibility, not advertising.",
			"Not a banner. Just room for the cue.",
			"No ad belongs here. This is cue space.",
			"This space is reserved for seeing the whole stick.",
			"Intentionally ad-free overhead clearance.",
			"No promotions. Just a better view of the cue.",
			"This top space is for comfort, not commerce.",
			"Not an ad zone. Just optional headroom.",
			"No advertising here. Just extra table space.",
			"This space stays empty on purpose.",
			"No banner here. That is intentional.",
			"This area exists for visibility, not marketing.",
			"Ad-free by design.",
			"Nothing is being sold up here.",
			"This is cue clearance, not ad space.",
			"No ads. Just room to aim.",
			"This space is intentionally unmonetized.",
			"Reserved for cue visibility.",
			"Not for ads. Just for a little extra room.",
			"This area is here to help the shot, not sell anything.",
			"No sponsor lives here.",
			"This strip is intentionally ad-free.",
			"Extra top room. Zero advertising.",
			"No ad goes here. Only space.",
			"This space is for the game, not for ads.",
			"Just a little room above the table. Nothing more.",
			"This area remains proudly ad-free.",
			"Not a billboard. Just better cue visibility.",
			"No ad slot here. Just overhead room.",
			"This space is intentionally left clear.",
			"No ads above the felt.",
			"This area is for seeing more of the table.",
			"Not an advertisement. Just some useful space.",
			"This room exists to help the cue fit on screen.",
			"No marketing. Just margin.",
			"No ad belongs in this space.",
			"This is optional headroom, not ad inventory.",
			"Nothing here but a little extra room.",
			"This strip is for visibility only.",
			"No ads, no popups, no nonsense.",
			"This space stays empty on purpose so the cue can breathe.",
			"Extra room for the cue. Not for ads.",
			"This area is intentionally quiet.",
			"No banner. No sponsorship. Just space.",
			"This space is here because the game needs it.",
			"Ad-free overhead for long cue angles.",
			"This area is not for advertising, and that is the point.",
			"No ad was invited into this space.",
			"This top area exists for the cue, not for clicks.",
			"Intentionally free of ads and distractions.",
			"This room is here to help the stick stay visible.",
			"No ad inventory above this table.",
			"No marketing up here. Just room.",
			"This area is blank on purpose.",
			"No ads. Just a little extra air above the table.",
			"This top strip is for the game only.",
			"Nothing commercial belongs up here.",
			"No ad space. Just cue space.",
			"This area exists to give the cue a little room.",
			"Not for selling. Just for seeing.",
			"This top space is part of the table, not a billboard.",
			"No advertisement was placed in this space.",
			"Ad-free overhead, exactly as intended.",
			"No banner here. Only headroom.",
			"This space is for visibility and nothing else.",
			"Nothing is promoted here but a better shot.",
			"No ad zone. Cue comfort zone.",
			"This area is intentionally clear for the sake of the game.",
			"Not a place for ads. A place for room.",
			"Extra top space with no sales pitch attached.",
			"No sponsors above the felt.",
			"This is where nothing is sold.",
			"This area is intentionally reserved for open space.",
			"No ads up here. The cue called dibs.",
			"This strip stays ad-free by choice.",
			"Nothing here but helpful space.",
			"This area is for cue clearance, not brand placement.",
			"No promotions. No products. Just room.",
			"This top space belongs to the game.",
			"Here for visibility, not visibility metrics.",
			"Not for advertising. Just for not clipping the cue.",
			"No ad above this rail.",
			"This space is intentionally unsponsored.",
			"No banners. No noise. Just room.",
			"This area exists because seeing the cue matters.",
			"Not ad space. Just table breathing room.",
			"This is not a marketing surface.",
			"No ads here, only useful emptiness.",
			"This strip is for the table, not advertisers.",
			"Nothing monetized in this direction.",
			"This top gap is a feature, not inventory.",
			"No ad belongs above this table.",
			"This area is intentionally left ad-free.",
			"Just enough room to see more of the cue.",
			"No marketing copy lives here.",
			"This space exists for aim, not ads.",
			"No ad slot. Just a little top-side mercy.",
			"This area stays clear so the cue can stay visible.",
			"There are no ads up here by design.",
			"This space is not for selling anything.",
			"It is okay for a space to just be useful.",
			"No advertisements were needed for this improvement.",
			"This strip exists for visibility, plain and simple.",
			"Ad-free overhead for players who want the extra room.",
			"No ad. Just optional space when you stretch upward.",
			"This area is intentionally not monetized.",
			"Nothing to buy above the table.",
			"No promotions up here, just pool.",
			"This top space is for the cue and the cue alone.",
			"This room is here because not everything needs an ad.",
			"No ad copy was placed in this area.",
			"This area remains clear for long-stick visibility.",
			"No banner, no pitch, no gimmick.",
			"This top strip exists to keep the cue in view.",
			"A little space, intentionally free of ads.",
			"This space is not here to monetize your resize.",
			"No ad content above this game.",
			"This is cue room, not campaign space.",
			"There is no sponsor message in this area.",
			"This space exists to help the table breathe.",
			"Nothing here but clean headroom.",
			"No ads here. That was a deliberate choice.",
			"This area is for better visibility, not better ad placement.",
			"No ad panel. Just room for the stick.",
			"This strip is empty because that is more useful.",
			"No banners above the break.",
			"This space is reserved for visual comfort.",
			"Not ad-supported. Cue-supported.",
			"This top margin is intentionally free of advertising.",
			"No ad space was harmed in the making of this gap.",
			"This area is just here to help the shot.",
			"No sponsor message. Just extra room.",
			"This strip remains proudly not for sale.",
			"No advertising. Just a cleaner view upward.",
			"This is for cue visibility, not brand visibility.",
			"No ad goes here because the cue needs the space more.",
			"This space was saved for the game.",
			"Just open room above the table. Nothing else.",
			"This area is intentionally free of clutter and ads.",
			"No promotions were added to this expansion.",
			"This top strip is here for players, not advertisers.",
			"No ad above the cloth.",
			"This space exists because the cue deserves room too.",
			"No marketing occupies this space.",
			"Just some useful air above the table.",
			"This area is here for clarity, not commerce.",
			"No ad. No interruption. Just space.",
			"This top area is intentionally quiet and ad-free.",
			"Nothing is sold here. It just helps the layout.",
			"No ad placement above this shot line.",
			"This strip is for space, not sponsorship.",
			"Here for cue visibility. Not for advertising.",
			"This area exists so you can see more when you want to.",
			"No ads. Just optional overhead room.",
			"This is not banner space. This is breathing room.",
			"No sponsored content in this section.",
			"This space is dedicated to not being an ad.",
			"No ad message. Just a little extra headroom.",
			"This strip was reserved for usefulness.",
			"This area is intentionally ad-free and low-drama.",
			"No banner was placed here because none was wanted.",
			"This room is for the cue, not the highest bidder.",
			"No ads above the table, by request and by design.",
			"This area remains clear for anyone who likes the extra space.",
			"No selling. Just seeing.",
			"This is the not-an-ad area.",
			"No ad belongs in this stretch zone.",
			"This top room is intentionally simple and ad-free.",
			"Nothing is marketed here.",
			"This space is here for visibility when the window grows.",
			"No banner up here. Just a little more sky.",
			"This area is for the cue, not commerce.",
			"No ad placement was approved for this strip.",
			"This space remains intentionally blank and useful.",
			"Ad-free overhead for those who want the room.",
			"This area is part of the table experience, not the ad stack.",
			"No banner belongs above this game.",
			"This strip is intentionally not an ad.",
			"No advertising here. Just optional space.",
			"This area exists to keep the cue visible near the top.",
			"No sponsor message. Only room.",
			"This space is not for promotions.",
			"No ads were invited into this overhead space.",
			"This top gap belongs to the game itself.",
			"Nothing here but a little extra room to breathe.",
			"No ad inventory. No missed opportunity. Just intention.",
			"This area is here because space can be useful.",
			"No banner. No branding. Just visibility.",
			"This top strip is intentionally free of ads and clutter.",
			"No marketing was placed here because the cue matters more.",
			"This room exists for cue comfort and nothing else.",
			"No ad here. Just room for longer sightlines.",
			"This area stays empty so the table can feel open.",
			"No promotions above this line.",
			"This strip is for extra room, not extra revenue.",
			"Ad-free space above the table, exactly as intended.",
			"This area is not for advertising. It is just here to give the cue some room." };

	private final YahooComponent	content;
	private final int				bannerHeight;
	private final YahooGamesTable	table;
	private final String			message;
	private int					selectedTab;

	public PoolTopMessageOverlay(YahooComponent content, int bannerHeight) {
		this(null, content, bannerHeight);
	}

	public PoolTopMessageOverlay(YahooGamesTable table, YahooComponent content,
			int bannerHeight) {
		super();
		this.table = table;
		this.content = content;
		this.bannerHeight = bannerHeight;
		message = buildMessage();
		selectedTab = 0;
	}

	@Override
	public int getWidth1() {
		return content.getWidth1();
	}

	@Override
	public int getHeight1() {
		return bannerHeight;
	}

	@Override
	public void m() {
		width = getWidth1();
		height = getHeight1();
		left = 0;
		top = 0;
	}

	@Override
	public void paint(YahooGraphics graphics) {
		int bannerWidth = Math.max(120, Math.min(getWidth(),
				Math.max(content.getWidth(), content.getWidth1())));
		int bannerHeight = getHeight();
		YahooGraphics overlay = null;
		try {
			overlay = graphics.create(0, 0, bannerWidth, bannerHeight);
			drawBannerBackground(overlay, bannerWidth, bannerHeight);
			if (table == null || table.getPropertyes() == null)
				drawFallbackMessage(overlay, bannerWidth, bannerHeight);
			else
				drawSettings(overlay, bannerWidth, bannerHeight);
		}
		finally {
			if (overlay != null)
				overlay.dispose();
		}
	}

	private void drawBannerBackground(YahooGraphics graphics, int width,
			int height) {
		graphics.setColor(getBannerBg());
		graphics.fillRect(0, 0, width, height);
		graphics.setColor(getBannerTop());
		graphics.fillRect(0, 0, width, 7);
		graphics.setColor(getBannerBottom());
		graphics.fillRect(0, height - 4, width, 4);
		graphics.setColor(getBannerLine());
		graphics.drawLine(0, 7, width, 7);
	}

	private void drawFallbackMessage(YahooGraphics graphics, int bannerWidth,
			int bannerHeight) {
		Font font = pickFont(bannerWidth, bannerHeight);
		graphics.setFont(font);
		FontMetrics metrics = getFontMetrics(font);
		ArrayList<String> lines = wrapMessage(metrics, bannerWidth - 40);
		int totalHeight = lines.size() * metrics.getHeight();
		int y = Math.max(metrics.getAscent() + 6,
				(bannerHeight - totalHeight) / 2 + metrics.getAscent());
		graphics.setColor(getBannerTextFg());
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			int x = Math.max(20, (bannerWidth - metrics.stringWidth(line)) / 2);
			graphics.drawString(line, x, y + i * metrics.getHeight());
		}
	}

	private void drawSettings(YahooGraphics graphics, int bannerWidth,
			int bannerHeight) {
		Font titleFont = new Font(YahooComponent.defaultFont.getName(), Font.BOLD,
				12);
		Font chipFont = new Font(YahooComponent.defaultFont.getName(), Font.PLAIN,
				10);
		graphics.setFont(titleFont);
		graphics.setColor(getBannerTextFg());
		graphics.drawString("Table settings", 10, 14);
		graphics.setFont(chipFont);
		FontMetrics metrics = getFontMetrics(chipFont);
		drawTabs(graphics, metrics, bannerWidth);
		drawSettingsButton(graphics, metrics, bannerWidth);
		ArrayList<String> settings = buildSettings(selectedTab);
		int x = 10;
		int y = 40;
		int rowHeight = Math.max(16, metrics.getHeight() + 4);
		for (int i = 0; i < settings.size(); i++) {
			String setting = settings.get(i);
			int chipWidth = metrics.stringWidth(setting) + 12;
			if (x + chipWidth > bannerWidth - 10) {
				x = 10;
				y += rowHeight;
			}
			if (y + rowHeight > bannerHeight - 5)
				break;
			drawSettingChip(graphics, setting, x, y, chipWidth, rowHeight,
					metrics);
			x += chipWidth + 4;
		}
	}

	private void drawTabs(YahooGraphics graphics, FontMetrics metrics,
			int bannerWidth) {
		int x = 108;
		int y = 6;
		int height = 18;
		for (int i = 0; i < TAB_NAMES.length; i++) {
			int width = getTabWidth(metrics, TAB_NAMES[i]);
			if (x + width > getSettingsButtonLeft(bannerWidth) - 6)
				break;
			graphics.setColor(i == selectedTab ? getTabActiveBg() : getTabBg());
			graphics.fillRect(x, y, width, height);
			graphics.setColor(getChipBorder());
			graphics.drawRect(x, y, width, height);
			graphics.setColor(i == selectedTab ? getTextFg() : getMutedFg());
			graphics.drawString(TAB_NAMES[i], x + 7, y + (height
					+ metrics.getAscent()) / 2 - 1);
			x += width + 3;
		}
	}

	private void drawSettingsButton(YahooGraphics graphics, FontMetrics metrics,
			int bannerWidth) {
		int x = getSettingsButtonLeft(bannerWidth);
		int y = 6;
		graphics.setColor(getTabActiveBg());
		graphics.fillRect(x, y, SETTINGS_BUTTON_WIDTH, SETTINGS_BUTTON_HEIGHT);
		graphics.setColor(getChipBorder());
		graphics.drawRect(x, y, SETTINGS_BUTTON_WIDTH, SETTINGS_BUTTON_HEIGHT);
		graphics.setColor(getTextFg());
		String label = "Show Settings Window";
		graphics.drawString(label, x + (SETTINGS_BUTTON_WIDTH
				- metrics.stringWidth(label)) / 2, y
				+ (SETTINGS_BUTTON_HEIGHT + metrics.getAscent()) / 2 - 1);
	}

	private void drawSettingChip(YahooGraphics graphics, String setting, int x,
			int y, int width, int height, FontMetrics metrics) {
		graphics.setColor(getChipBg());
		graphics.fillRoundRect(x, y, width, height - 2, 8, 8);
		graphics.setColor(getChipBorder());
		graphics.drawRect(x, y, width, height - 2);
		graphics.setColor(getMutedFg());
		graphics.drawString(setting, x + 6, y + (height - 2
				+ metrics.getAscent()) / 2 - 1);
	}

	private Color getTableColor() {
		if (table instanceof YahooPoolTable)
			return ((YahooPoolTable) table).getCurrentTableColor();
		return null;
	}

	private Color getBannerBg() {
		Color color = getTableColor();
		return color == null ? BANNER_BG : blend(color, Color.white, 0.35F);
	}

	private Color getBannerTop() {
		Color color = getTableColor();
		return color == null ? BANNER_TOP : blend(color, Color.white, 0.20F);
	}

	private Color getBannerBottom() {
		Color color = getTableColor();
		return color == null ? BANNER_BOTTOM : blend(color, Color.black, 0.28F);
	}

	private Color getBannerLine() {
		Color color = getTableColor();
		return color == null ? new Color(147, 182, 145) : blend(color,
				Color.white, 0.50F);
	}

	private Color getChipBg() {
		Color color = getTableColor();
		return color == null ? CHIP_BG : blend(color, Color.white, 0.70F);
	}

	private Color getChipBorder() {
		Color color = getTableColor();
		return color == null ? CHIP_BORDER : blend(color, Color.black, 0.55F);
	}

	private Color getTabBg() {
		Color color = getTableColor();
		return color == null ? TAB_BG : blend(color, Color.white, 0.52F);
	}

	private Color getTabActiveBg() {
		Color color = getTableColor();
		return color == null ? TAB_ACTIVE_BG : blend(color, Color.white, 0.82F);
	}

	private Color getTextFg() {
		Color color = getTableColor();
		return color == null ? TEXT_FG : blend(color, Color.black, 0.78F);
	}

	private Color getMutedFg() {
		Color color = getTableColor();
		return color == null ? MUTED_FG : blend(color, Color.black, 0.58F);
	}

	private Color getBannerTextFg() {
		Color background = getBannerBg();
		return getBrightness(background) < 95 ? Color.white : getTextFg();
	}

	private int getBrightness(Color color) {
		return (color.getRed() * 299 + color.getGreen() * 587 + color.getBlue()
				* 114) / 1000;
	}

	private Color blend(Color color, Color color1, float value) {
		float amount = value;
		if (amount < 0.0F)
			amount = 0.0F;
		if (amount > 1.0F)
			amount = 1.0F;
		int red = (int) (color.getRed() * (1.0F - amount) + color1.getRed()
				* amount);
		int green = (int) (color.getGreen() * (1.0F - amount) + color1
				.getGreen() * amount);
		int blue = (int) (color.getBlue() * (1.0F - amount) + color1.getBlue()
				* amount);
		return new Color(red, green, blue);
	}

	@Override
	public boolean eventMouseDown(java.awt.Event event, int x, int y) {
		if (table == null || table.getPropertyes() == null)
			return false;
		int bannerWidth = Math.max(120, Math.min(getWidth(),
				Math.max(content.getWidth(), content.getWidth1())));
		int buttonLeft = getSettingsButtonLeft(bannerWidth);
		if (x >= buttonLeft && x <= buttonLeft + SETTINGS_BUTTON_WIDTH && y >= 6
				&& y <= 6 + SETTINGS_BUTTON_HEIGHT) {
			if (table instanceof YahooPoolTable)
				((YahooPoolTable) table).showPoolSettingsViewDialog(getContainer());
			else
				new PoolSettingsViewDialog(getContainer(), table.getPropertyes());
			return true;
		}
		Font chipFont = new Font(YahooComponent.defaultFont.getName(), Font.PLAIN,
				10);
		FontMetrics metrics = getFontMetrics(chipFont);
		int tabX = 108;
		int tabY = 6;
		int tabHeight = 18;
		for (int i = 0; i < TAB_NAMES.length; i++) {
			int tabWidth = getTabWidth(metrics, TAB_NAMES[i]);
			if (x >= tabX && x <= tabX + tabWidth && y >= tabY
					&& y <= tabY + tabHeight) {
				if (selectedTab != i) {
					selectedTab = i;
					invalidate();
				}
				return true;
			}
			tabX += tabWidth + 3;
		}
		return false;
	}

	private int getSettingsButtonLeft(int bannerWidth) {
		return Math.max(108, bannerWidth - SETTINGS_BUTTON_WIDTH - 10);
	}

	private int getTabWidth(FontMetrics metrics, String label) {
		return metrics.stringWidth(label) + 16;
	}

	private Font pickFont(int bannerWidth, int bannerHeight) {
		int size = Math.min(24, Math.max(14, bannerHeight / 3));
		Font font = new Font(YahooComponent.defaultFont.getName(), Font.BOLD,
				size);
		while (size > 14) {
			FontMetrics metrics = getFontMetrics(font);
			if (fits(metrics, bannerWidth - 40, bannerHeight - 12))
				break;
			size--;
			font = new Font(YahooComponent.defaultFont.getName(), Font.BOLD,
					size);
		}
		return font;
	}

	private boolean fits(FontMetrics metrics, int maxWidth, int maxHeight) {
		ArrayList<String> lines = wrapMessage(metrics, maxWidth);
		return lines.size() * metrics.getHeight() <= maxHeight;
	}

	private ArrayList<String> wrapMessage(FontMetrics metrics, int maxWidth) {
		ArrayList<String> lines = new ArrayList<String>();
		String[] words = message.split(" ");
		String line = "";
		for (int i = 0; i < words.length; i++) {
			String candidate = line.length() == 0 ? words[i] : line + " "
					+ words[i];
			if (line.length() > 0 && metrics.stringWidth(candidate) > maxWidth) {
				lines.add(line);
				line = words[i];
			}
			else {
				line = candidate;
			}
		}
		if (line.length() > 0)
			lines.add(line);
		if (lines.size() == 0)
			lines.add(message);
		return lines;
	}

	private String buildMessage() {
		if (table == null || table.getPropertyes() == null)
			return MESSAGES[RANDOM.nextInt(MESSAGES.length)];
		Hashtable<String, String> properties = table.getPropertyes();
		StringBuffer text = new StringBuffer();
		if (properties.containsKey("nineBallGame")
				|| properties.containsKey("nineBallTraining"))
			text.append("9-Ball");
		else
			text.append("8-Ball");
		if (properties.containsKey("training"))
			text.append(" Training");
		if (properties.containsKey("rd"))
			text.append(" | Rated");
		else
			text.append(" | Unrated");
		if (properties.containsKey("timer"))
			text.append(" | Timer " + properties.get("timer") + "s");
		if (properties.containsKey("ff"))
			text.append(" | No force forfeit");
		else
			text.append(" | Force forfeit allowed");
		if (properties.containsKey("breakPocketCap"))
			text.append(" | Break random <= "
					+ properties.get("breakPocketCap") + "%");
		else
			text.append(" | Deterministic break");
		appendPct(text, properties, "linearFriction", "Slide");
		appendPct(text, properties, "rotationFriction", "Roll");
		appendPct(text, properties, "sideRotationFriction", "Spin friction");
		appendPct(text, properties, "railBounce", "Rail bounce");
		appendPct(text, properties, "railSpinTransfer", "Rail spin");
		appendPct(text, properties, "railSideSpin", "Rail side");
		appendPct(text, properties, "maxCuePower", "Max power");
		appendPct(text, properties, "cueForce", "Cue force");
		appendPct(text, properties, "spinEffect", "Spin effect");
		appendPct(text, properties, "ballRadius", "Ball size");
		appendPct(text, properties, "collisionEnergy", "Collision");
		if (properties.containsKey("animationSpeedPct"))
			text.append(" | Animation " + properties.get("animationSpeedPct")
					+ "%");
		if (properties.containsKey("pocketHandicap0"))
			text.append(" | Seat 1 pocket " + properties.get("pocketHandicap0"));
		if (properties.containsKey("pocketHandicap1"))
			text.append(" | Seat 2 pocket " + properties.get("pocketHandicap1"));
		return new String(text);
	}

	private ArrayList<String> buildSettings(int tab) {
		ArrayList<String> result = new ArrayList<String>();
		if (table == null || table.getPropertyes() == null)
			return result;
		Hashtable<String, String> properties = table.getPropertyes();
		if (tab == 0)
			buildRuleSettings(result, properties);
		else if (tab == 1)
			buildBreakSettings(result, properties);
		else if (tab == 2)
			buildPhysicsSettings(result, properties);
		else if (tab == 3)
			buildShotSettings(result, properties);
		else if (tab == 4)
			buildPocketSettings(result, properties);
		else
			buildMoreSettings(result, properties);
		return result;
	}

	private void buildRuleSettings(ArrayList<String> result,
			Hashtable<String, String> properties) {
		if (properties.containsKey("nineBallGame")
				|| properties.containsKey("nineBallTraining"))
			result.add("Game 9-Ball");
		else
			result.add("Game 8-Ball");
		result.add(properties.containsKey("training") ? "Mode Training"
				: "Mode Standard");
		result.add(properties.containsKey("automat") ? "Automatic"
				: "Manual start");
		result.add(properties.containsKey("rd") ? "Rated" : "Unrated");
		if (properties.containsKey("timer"))
			result.add("Timer " + properties.get("timer") + "s");
		else
			result.add("No timer");
		result.add(properties.containsKey("ff") ? "No force forfeit"
				: "Force forfeit");
	}

	private void buildBreakSettings(ArrayList<String> result,
			Hashtable<String, String> properties) {
		if (properties.containsKey("breakPocketCap")) {
			result.add("Behavior Non-deterministic");
			result.add("Max pocket " + getPropertyValue(properties,
					"breakPocketCap", "0") + "%");
		}
		else
			result.add("Behavior Deterministic");
	}

	private void buildPhysicsSettings(ArrayList<String> result,
			Hashtable<String, String> properties) {
		appendSettingPct(result, properties, "linearFriction", "Slide");
		appendSettingPct(result, properties, "rotationFriction", "Roll");
		appendSettingPct(result, properties, "sideRotationFriction", "Side spin");
		appendSettingPct(result, properties, "railBounce", "Rail bounce");
		appendSettingPct(result, properties, "railSpinTransfer", "Rail spin");
		appendSettingPct(result, properties, "railSideSpin", "Rail side");
	}

	private void buildShotSettings(ArrayList<String> result,
			Hashtable<String, String> properties) {
		appendSettingPct(result, properties, "maxCuePower", "Max cue power");
		appendSettingPct(result, properties, "cueForce", "Cue force");
		appendSettingPct(result, properties, "spinEffect", "English");
		appendSettingPct(result, properties, "ballRadius", "Ball size");
		appendSettingPct(result, properties, "collisionEnergy", "Collision energy");
	}

	private void buildPocketSettings(ArrayList<String> result,
			Hashtable<String, String> properties) {
		result.add("Seat 1 pocket handicap " + formatSigned(getPropertyValue(
				properties, "pocketHandicap0", "0")));
		result.add("Seat 2 pocket handicap " + formatSigned(getPropertyValue(
				properties, "pocketHandicap1", "0")));
		result.add("+ easier");
		result.add("- harder");
	}

	private void buildMoreSettings(ArrayList<String> result,
			Hashtable<String, String> properties) {
		result.add("Animation "
				+ getPropertyValue(properties, "animationSpeedPct", "100") + "%");
		if (properties.get("dc") != null)
			result.add("Desc " + truncate(properties.get("dc"), 20));
		else
			result.add("No description");
	}

	private String formatSigned(String value) {
		if (value != null && value.length() > 0 && value.charAt(0) != '-'
				&& !"0".equals(value))
			return "+" + value;
		return value;
	}

	private void appendSettingPct(ArrayList<String> result,
			Hashtable<String, String> properties, String key, String label) {
		String value = getPropertyValue(properties, "physics." + key + "Pct",
				"100");
		result.add(label + " " + value + "%");
	}

	private String getPropertyValue(Hashtable<String, String> properties,
			String key, String defaultValue) {
		String value = properties.get(key);
		return value != null ? value : defaultValue;
	}

	private String truncate(String value, int maxLength) {
		if (value == null || value.length() <= maxLength)
			return value;
		return value.substring(0, maxLength - 3) + "...";
	}

	private void appendPct(StringBuffer text, Hashtable<String, String> properties,
			String key, String label) {
		String value = properties.get("physics." + key + "Pct");
		if (value != null)
			text.append(" | " + label + " " + value + "%");
	}
}
