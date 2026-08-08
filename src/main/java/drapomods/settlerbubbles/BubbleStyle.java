package drapomods.settlerbubbles;

public enum BubbleStyle {
    SPEECH,
    THOUGHT,
    SHOUT;

    public static BubbleStyle fromID(int id) {
        BubbleStyle[] values = values();
        return id >= 0 && id < values.length ? values[id] : null;
    }

    public static BubbleStyle forCategory(BubbleCategory category) {
        return category == BubbleCategory.NEEDS || category == BubbleCategory.MOOD
                ? THOUGHT
                : category == BubbleCategory.COMBAT ? SHOUT : SPEECH;
    }
}
