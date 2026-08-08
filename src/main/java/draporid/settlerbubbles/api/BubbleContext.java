package draporid.settlerbubbles.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import draporid.settlerbubbles.BubbleCategory;
import draporid.settlerbubbles.BubbleStyle;
import necesse.engine.localization.message.GameMessage;
import necesse.entity.mobs.Mob;

public final class BubbleContext {
    private final String triggerID;
    private final Mob speaker;
    private final Mob listener;
    private final BubbleCategory category;
    private final BubbleStyle style;
    private final Map<String, GameMessage> replacements;
    private final Map<String, String> attributes;

    private BubbleContext(Builder builder) {
        triggerID = builder.triggerID;
        speaker = builder.speaker;
        listener = builder.listener;
        category = builder.category;
        style = builder.style == null ? BubbleStyle.forCategory(category) : builder.style;
        replacements = Collections.unmodifiableMap(new LinkedHashMap<>(builder.replacements));
        attributes = Collections.unmodifiableMap(new LinkedHashMap<>(builder.attributes));
    }

    public static Builder builder(String triggerID, Mob speaker) {
        return new Builder(triggerID, speaker);
    }

    public String getTriggerID() {
        return triggerID;
    }

    public Mob getSpeaker() {
        return speaker;
    }

    public Mob getListener() {
        return listener;
    }

    public BubbleCategory getCategory() {
        return category;
    }

    public BubbleStyle getStyle() {
        return style;
    }

    public GameMessage getReplacement(String key) {
        return replacements.get(key);
    }

    public Map<String, GameMessage> getReplacements() {
        return replacements;
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public static final class Builder {
        private final String triggerID;
        private final Mob speaker;
        private Mob listener;
        private BubbleCategory category = BubbleCategory.SOCIAL;
        private BubbleStyle style;
        private final Map<String, GameMessage> replacements = new LinkedHashMap<>();
        private final Map<String, String> attributes = new LinkedHashMap<>();

        private Builder(String triggerID, Mob speaker) {
            if (triggerID == null || !triggerID.contains(":")) {
                throw new IllegalArgumentException("triggerID must be namespaced, for example mymod:event");
            }
            if (speaker == null) {
                throw new IllegalArgumentException("speaker cannot be null");
            }
            this.triggerID = triggerID;
            this.speaker = speaker;
        }

        public Builder listener(Mob listener) {
            this.listener = listener;
            return this;
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

        public Builder replacement(String key, GameMessage value) {
            if (key != null && value != null) {
                replacements.put(key, value);
            }
            return this;
        }

        public Builder attribute(String key, String value) {
            if (key != null && value != null) {
                attributes.put(key, value);
            }
            return this;
        }

        public BubbleContext build() {
            return new BubbleContext(this);
        }
    }
}

