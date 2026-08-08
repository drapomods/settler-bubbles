package draporid.settlerbubbles.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import draporid.settlerbubbles.SettlerBubblesSettings;
import draporid.settlerbubbles.network.SpeechBubblePacket;
import necesse.engine.GameLog;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;

public final class SettlerBubblesAPI {
    public static final int API_VERSION = 1;

    private static final Map<String, DialogueProvider> PROVIDERS = new LinkedHashMap<>();
    private static final Map<Mob, Long> NEXT_BUBBLE = new WeakHashMap<>();
    private static long nextGlobalBubble;

    private SettlerBubblesAPI() {
    }

    public static RegistrationHandle registerProvider(String providerID, DialogueProvider provider) {
        if (providerID == null || !providerID.contains(":")) {
            throw new IllegalArgumentException("providerID must be namespaced, for example mymod:dialogue");
        }
        if (provider == null) {
            throw new IllegalArgumentException("provider cannot be null");
        }
        synchronized (PROVIDERS) {
            if (PROVIDERS.containsKey(providerID)) {
                throw new IllegalArgumentException("Dialogue provider already registered: " + providerID);
            }
            PROVIDERS.put(providerID, provider);
        }
        return new ProviderHandle(providerID, provider);
    }

    public static List<String> getRegisteredProviderIDs() {
        synchronized (PROVIDERS) {
            return Collections.unmodifiableList(new ArrayList<>(PROVIDERS.keySet()));
        }
    }

    public static boolean fireEvent(BubbleContext context) {
        if (context == null) {
            return false;
        }
        List<Map.Entry<String, DialogueProvider>> providers;
        synchronized (PROVIDERS) {
            providers = new ArrayList<>(PROVIDERS.entrySet());
        }

        List<BubbleLine> lines = new ArrayList<>();
        for (Map.Entry<String, DialogueProvider> entry : providers) {
            try {
                Collection<BubbleLine> provided = entry.getValue().getLines(context);
                if (provided != null) {
                    for (BubbleLine line : provided) {
                        if (line != null) {
                            lines.add(line);
                        }
                    }
                }
            } catch (Throwable error) {
                GameLog.warn.println("Settler Bubbles provider failed: " + entry.getKey());
                error.printStackTrace(GameLog.warn);
            }
        }
        if (lines.isEmpty()) {
            return false;
        }

        BubbleLine selected = weightedLine(lines);
        GameMessage message;
        try {
            message = selected.createMessage(context);
        } catch (Throwable error) {
            GameLog.warn.println("Settler Bubbles provider could not create a message for "
                    + context.getTriggerID());
            error.printStackTrace(GameLog.warn);
            return false;
        }
        if (message == null) {
            return false;
        }
        return showBubble(BubbleRequest.builder(context.getSpeaker(), message)
                .category(context.getCategory())
                .style(selected.getStyle() == null ? context.getStyle() : selected.getStyle())
                .duration(selected.getDuration())
                .build());
    }

    public static boolean showBubble(BubbleRequest request) {
        if (request == null || !SettlerBubblesSettings.isCategoryEnabled(request.getCategory())) {
            return false;
        }
        Mob speaker = request.getSpeaker();
        if (!speaker.isServer() || speaker.removed() || speaker.getServer() == null
                || speaker.getLevel() == null) {
            return false;
        }

        long now = System.nanoTime() / 1000000L;
        synchronized (NEXT_BUBBLE) {
            Long speakerNext = NEXT_BUBBLE.get(speaker);
            if ((speakerNext != null && now < speakerNext) || now < nextGlobalBubble) {
                return false;
            }
            NEXT_BUBBLE.put(speaker, now + request.getCooldown());
            nextGlobalBubble = now + 200L;
        }

        speaker.getServer().network.sendToClientsAtEntireLevel(
                new SpeechBubblePacket(speaker.getUniqueID(), request.getCategory(), request.getStyle(),
                        request.getMessage(), request.getDuration()),
                speaker.getLevel()
        );
        return true;
    }

    private static BubbleLine weightedLine(List<BubbleLine> lines) {
        int total = 0;
        for (BubbleLine line : lines) {
            total += line.getWeight();
        }
        int roll = GameRandom.globalRandom.nextInt(Math.max(1, total));
        for (BubbleLine line : lines) {
            roll -= line.getWeight();
            if (roll < 0) {
                return line;
            }
        }
        return lines.get(lines.size() - 1);
    }

    private static class ProviderHandle implements RegistrationHandle {
        private final String providerID;
        private final DialogueProvider provider;

        ProviderHandle(String providerID, DialogueProvider provider) {
            this.providerID = providerID;
            this.provider = provider;
        }

        @Override
        public boolean unregister() {
            synchronized (PROVIDERS) {
                if (PROVIDERS.get(providerID) == provider) {
                    PROVIDERS.remove(providerID);
                    return true;
                }
                return false;
            }
        }

        @Override
        public boolean isRegistered() {
            synchronized (PROVIDERS) {
                return PROVIDERS.get(providerID) == provider;
            }
        }
    }
}

