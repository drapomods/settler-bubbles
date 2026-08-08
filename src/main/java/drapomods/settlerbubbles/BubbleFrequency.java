package drapomods.settlerbubbles;

public enum BubbleFrequency {
    LOW(0.55F),
    NORMAL(1.0F),
    HIGH(1.65F);

    public final float multiplier;

    BubbleFrequency(float multiplier) {
        this.multiplier = multiplier;
    }
}
