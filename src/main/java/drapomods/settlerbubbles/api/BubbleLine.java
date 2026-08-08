package drapomods.settlerbubbles.api;

import drapomods.settlerbubbles.BubbleStyle;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;

public final class BubbleLine {
    @FunctionalInterface
    public interface MessageFactory {
        GameMessage create(BubbleContext context);
    }

    private final MessageFactory messageFactory;
    private final int weight;
    private final int duration;
    private final BubbleStyle style;

    private BubbleLine(Builder builder) {
        messageFactory = builder.messageFactory;
        weight = builder.weight;
        duration = builder.duration;
        style = builder.style;
    }

    public static Builder builder(MessageFactory messageFactory) {
        return new Builder(messageFactory);
    }

    public static Builder message(GameMessage message) {
        return builder(context -> message);
    }

    public static Builder local(String category, String key) {
        return builder(context -> new LocalMessage(category, key));
    }

    GameMessage createMessage(BubbleContext context) {
        return messageFactory.create(context);
    }

    int getWeight() {
        return weight;
    }

    int getDuration() {
        return duration;
    }

    BubbleStyle getStyle() {
        return style;
    }

    public static final class Builder {
        private final MessageFactory messageFactory;
        private int weight = 10;
        private int duration = 3500;
        private BubbleStyle style;

        private Builder(MessageFactory messageFactory) {
            if (messageFactory == null) {
                throw new IllegalArgumentException("messageFactory cannot be null");
            }
            this.messageFactory = messageFactory;
        }

        public Builder weight(int weight) {
            this.weight = Math.max(1, Math.min(10000, weight));
            return this;
        }

        public Builder duration(int duration) {
            this.duration = Math.max(1500, Math.min(12000, duration));
            return this;
        }

        public Builder style(BubbleStyle style) {
            this.style = style;
            return this;
        }

        public BubbleLine build() {
            return new BubbleLine(this);
        }
    }
}
