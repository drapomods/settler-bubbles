package draporid.settlerbubbles.api;

import draporid.settlerbubbles.BubbleCategory;
import draporid.settlerbubbles.BubbleStyle;
import necesse.engine.localization.message.GameMessage;
import necesse.entity.mobs.Mob;

public final class BubbleRequest {
    private final Mob speaker;
    private final BubbleCategory category;
    private final BubbleStyle style;
    private final GameMessage message;
    private final int duration;
    private final int cooldown;

    private BubbleRequest(Builder builder) {
        speaker = builder.speaker;
        category = builder.category;
        style = builder.style == null ? BubbleStyle.forCategory(category) : builder.style;
        message = builder.message;
        duration = builder.duration;
        cooldown = builder.cooldown;
    }

    public static Builder builder(Mob speaker, GameMessage message) {
        return new Builder(speaker, message);
    }

    public Mob getSpeaker() {
        return speaker;
    }

    public BubbleCategory getCategory() {
        return category;
    }

    public BubbleStyle getStyle() {
        return style;
    }

    public GameMessage getMessage() {
        return message;
    }

    public int getDuration() {
        return duration;
    }

    public int getCooldown() {
        return cooldown;
    }

    public static final class Builder {
        private final Mob speaker;
        private final GameMessage message;
        private BubbleCategory category = BubbleCategory.SOCIAL;
        private BubbleStyle style;
        private int duration = 3500;
        private int cooldown = 3000;

        private Builder(Mob speaker, GameMessage message) {
            if (speaker == null || message == null) {
                throw new IllegalArgumentException("speaker and message are required");
            }
            this.speaker = speaker;
            this.message = message;
        }

        public Builder category(BubbleCategory category) {
            if (category == null) {
                throw new IllegalArgumentException("category cannot be null");
            }
            this.category = category;
            return this;
        }

        public Builder style(BubbleStyle style) {
            this.style = style;
            return this;
        }

        public Builder duration(int duration) {
            this.duration = Math.max(1500, Math.min(12000, duration));
            return this;
        }

        public Builder cooldown(int cooldown) {
            this.cooldown = Math.max(1000, Math.min(60000, cooldown));
            return this;
        }

        public BubbleRequest build() {
            return new BubbleRequest(this);
        }
    }
}

