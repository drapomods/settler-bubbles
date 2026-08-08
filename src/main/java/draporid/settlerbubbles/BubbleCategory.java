package draporid.settlerbubbles;

public enum BubbleCategory {
    SOCIAL,
    WORK,
    NEEDS,
    MOOD,
    COMBAT;

    public static BubbleCategory fromID(int id) {
        BubbleCategory[] values = values();
        return id >= 0 && id < values.length ? values[id] : null;
    }
}

