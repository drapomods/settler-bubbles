package draporid.settlerbubbles;

import necesse.engine.modLoader.ModSettings;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

public class SettlerBubblesSettings extends ModSettings {
    public static boolean enabled = true;
    public static BubbleFrequency frequency = BubbleFrequency.NORMAL;
    public static float durationScale = 1.0F;
    public static int maxDistance = 700;
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
        social = load.getBoolean("social", social);
        work = load.getBoolean("work", work);
        needs = load.getBoolean("needs", needs);
        mood = load.getBoolean("mood", mood);
        combat = load.getBoolean("combat", combat);
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
