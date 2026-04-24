package y.ycontrols;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.Random;

import y.controls.YahooComponent;
import y.controls.YahooGraphics;

public class PoolTopMessageOverlay extends YahooComponent {

	private static final Random	RANDOM	= new Random();
	private static final Color	TEXT_FG	= new Color(90, 84, 42);
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
	private final String			message;

	public PoolTopMessageOverlay(YahooComponent content, int bannerHeight) {
		super();
		this.content = content;
		this.bannerHeight = bannerHeight;
		message = MESSAGES[RANDOM.nextInt(MESSAGES.length)];
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
			Font font = pickFont(bannerWidth, bannerHeight);
			overlay.setFont(font);
			FontMetrics metrics = getFontMetrics(font);
			ArrayList<String> lines = wrapMessage(metrics, bannerWidth - 40);
			int totalHeight = lines.size() * metrics.getHeight();
			int y = Math.max(metrics.getAscent() + 6,
					(bannerHeight - totalHeight) / 2 + metrics.getAscent());
			overlay.setColor(TEXT_FG);
			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				int x = Math.max(20, (bannerWidth - metrics.stringWidth(line)) / 2);
				overlay.drawString(line, x, y + i * metrics.getHeight());
			}
		}
		finally {
			if (overlay != null)
				overlay.dispose();
		}
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
}
