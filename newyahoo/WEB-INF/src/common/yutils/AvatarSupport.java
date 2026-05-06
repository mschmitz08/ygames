package common.yutils;

public final class AvatarSupport {

	public static final int BUILTIN_AVATAR_COUNT = 45;
	public static final int CUSTOM_AVATAR_INDEX = 127;
	public static final int AVATAR_WIDTH = 34;
	public static final int AVATAR_HEIGHT = 23;

	public static boolean isCustomAvatar(int avatar) {
		return avatar == CUSTOM_AVATAR_INDEX;
	}

	public static boolean isValidAvatarIndex(int avatar) {
		return avatar >= 0 && avatar < BUILTIN_AVATAR_COUNT
				|| isCustomAvatar(avatar);
	}

	private AvatarSupport() {
	}
}
