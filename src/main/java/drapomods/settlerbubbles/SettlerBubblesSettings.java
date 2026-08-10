package drapomods.settlerbubbles;

import necesse.engine.modLoader.ModSettings;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.window.GameWindow;

public class SettlerBubblesSettings extends ModSettings {
    public static final int DEFAULT_MAX_VISIBLE_BUBBLES = 5;

    public static boolean enabled = true;
    public static BubbleFrequency frequency = BubbleFrequency.NORMAL;
    public static float durationScale = 1.0F;
    public static int maxDistance = 700;
    public static BubbleFontMode fontMode = BubbleFontMode.GAME_DEFAULT;
    public static int fontSize;
    public static int maxVisibleBubbles = DEFAULT_MAX_VISIBLE_BUBBLES;
    public static BubbleLanguage language = BubbleLanguage.GAME_LANGUAGE;
    public static boolean social = true;
    public static boolean work = true;
    public static boolean needs = true;
    public static boolean mood = true;
    public static boolean combat = true;

    @Override
    public void addSaveData(SaveData save) {
        save.addEnum("frequency", frequency);
        save.addFloat("durationScale", durationScale);
        save.addInt("maxDistance", maxDistance);
        save.addEnum("fontMode", fontMode);
        save.addInt("fontSize", fontSize);
        save.addInt("maxVisibleBubbles", maxVisibleBubbles);
        save.addEnum("language", language);
        save.addBoolean("social", social);
        save.addBoolean("work", work);
        save.addBoolean("needs", needs);
        save.addBoolean("mood", mood);
        save.addBoolean("combat", combat);
    }

    @Override
    public void applyLoadData(LoadData load) {
        // Speech bubbles always start enabled. The client command can still hide
        // them temporarily for the current game session.
        enabled = true;
        frequency = load.getEnum(BubbleFrequency.class, "frequency", frequency);
        durationScale = load.getFloat("durationScale", durationScale, 0.5F, 2.0F);
        maxDistance = load.getInt("maxDistance", maxDistance, 200, 1600);
        if (load.hasLoadDataByName("fontMode")) {
            fontMode = load.getEnum(BubbleFontMode.class, "fontMode", fontMode);
        }
        if (load.hasLoadDataByName("fontSize")) {
            fontSize = normalizeFontSize(load.getInt("fontSize", fontSize, 0, 22));
        }
        if (load.hasLoadDataByName("maxVisibleBubbles")) {
            maxVisibleBubbles = load.getInt("maxVisibleBubbles",
                    DEFAULT_MAX_VISIBLE_BUBBLES, 3, 10);
        }
        if (load.hasLoadDataByName("language")) {
            language = load.getEnum(BubbleLanguage.class, "language", language);
        }
        social = load.getBoolean("social", social);
        work = load.getBoolean("work", work);
        needs = load.getBoolean("needs", needs);
        mood = load.getBoolean("mood", mood);
        combat = load.getBoolean("combat", combat);
    }

    public static void ensureAutoFontSize(GameWindow window) {
        if (fontSize == 0) {
            fontSize = suggestFontSize(window);
        }
    }

    public static int suggestFontSize(GameWindow window) {
        // Use the actual window height, not the internally scaled scene height.
        // Otherwise a 1440p display can still look like a 720p scene here and
        // receive a font that is much too small.
        int height = window == null ? 1080 : window.getHeight();
        if (height <= 900) {
            return 14;
        }
        if (height <= 1200) {
            return 16;
        }
        if (height <= 1800) {
            return 18;
        }
        if (height <= 2600) {
            return 20;
        }
        return 22;
    }

    public static int normalizeFontSize(int size) {
        if (size == 0) {
            return 0;
        }
        int[] sizes = {14, 16, 18, 20, 22};
        int nearest = sizes[0];
        for (int option : sizes) {
            if (Math.abs(option - size) < Math.abs(nearest - size)) {
                nearest = option;
            }
        }
        return nearest;
    }

    public static boolean isCategoryEnabled(BubbleCategory category) {
        if (category == null) {
            return false;
        }
        switch (category) {
            case SOCIAL:
                return social;
            case WORK:
                return work;
            case NEEDS:
                return needs;
            case MOOD:
                return mood;
            case COMBAT:
                return combat;
            default:
                return false;
        }
    }
}
