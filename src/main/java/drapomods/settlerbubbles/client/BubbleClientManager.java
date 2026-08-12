package drapomods.settlerbubbles.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import drapomods.settlerbubbles.BubbleCategory;
import drapomods.settlerbubbles.BubbleStyle;
import drapomods.settlerbubbles.SettlerBubblesSettings;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.level.maps.Level;

public final class BubbleClientManager {
    private BubbleClientManager() {
    }

    public static void show(Level level, PlayerMob perspective, Mob mob,
                            BubbleCategory category, BubbleStyle style,
                            String message, int duration) {
        List<SpeechBubbleHud> active = level.hudManager.streamElements()
                .filter(element -> element instanceof SpeechBubbleHud && !element.isRemoved())
                .map(element -> (SpeechBubbleHud)element)
                .collect(Collectors.toList());

        int incomingPriority = priority(category);
        for (SpeechBubbleHud bubble : active) {
            if (bubble.isFor(mob) && priority(bubble.getCategory()) > incomingPriority) {
                return;
            }
        }
        for (SpeechBubbleHud bubble : active) {
            if (bubble.isFor(mob)) {
                bubble.remove();
            }
        }
        active.removeIf(bubble -> bubble.isFor(mob));

        IncomingBubble incoming = new IncomingBubble(mob, category);
        List<RankedBubble> ranked = new ArrayList<>();
        for (SpeechBubbleHud bubble : active) {
            ranked.add(RankedBubble.active(bubble, perspective));
        }
        ranked.add(RankedBubble.incoming(incoming, perspective));
        ranked.sort(BEST_FIRST);

        int limit = SettlerBubblesSettings.maxVisibleBubbles;
        boolean keepIncoming = false;
        for (int i = 0; i < ranked.size(); i++) {
            RankedBubble rankedBubble = ranked.get(i);
            if (i < limit) {
                keepIncoming |= rankedBubble.incoming != null;
            } else if (rankedBubble.active != null) {
                rankedBubble.active.remove();
            }
        }
        if (keepIncoming) {
            level.hudManager.addElement(new SpeechBubbleHud(mob, category, style, message, duration));
        }
    }

    public static void trim(Level level, PlayerMob perspective) {
        if (level == null) {
            return;
        }
        List<RankedBubble> ranked = level.hudManager.streamElements()
                .filter(element -> element instanceof SpeechBubbleHud && !element.isRemoved())
                .map(element -> RankedBubble.active((SpeechBubbleHud)element, perspective))
                .sorted(BEST_FIRST)
                .collect(Collectors.toList());
        for (int i = SettlerBubblesSettings.maxVisibleBubbles; i < ranked.size(); i++) {
            ranked.get(i).active.remove();
        }
    }

    private static int priority(BubbleCategory category) {
        if (category == null) {
            return 0;
        }
        switch (category) {
            case COMBAT:
                return 4;
            case NEEDS:
                return 3;
            case MOOD:
                return 2;
            case WORK:
            case SOCIAL:
            default:
                return 1;
        }
    }

    private static double distance(PlayerMob perspective, Mob mob) {
        return perspective == null || mob == null ? Double.MAX_VALUE : perspective.getDistance(mob);
    }

    private static final Comparator<RankedBubble> BEST_FIRST =
            Comparator.comparingInt((RankedBubble bubble) -> bubble.priority).reversed()
                    .thenComparingDouble(bubble -> bubble.distance)
                    .thenComparing(Comparator.comparingLong(
                            (RankedBubble bubble) -> bubble.createdAt).reversed());

    private static class IncomingBubble {
        final Mob mob;
        final BubbleCategory category;

        IncomingBubble(Mob mob, BubbleCategory category) {
            this.mob = mob;
            this.category = category;
        }
    }

    private static class RankedBubble {
        final SpeechBubbleHud active;
        final IncomingBubble incoming;
        final int priority;
        final double distance;
        final long createdAt;

        private RankedBubble(SpeechBubbleHud active, IncomingBubble incoming,
                             int priority, double distance, long createdAt) {
            this.active = active;
            this.incoming = incoming;
            this.priority = priority;
            this.distance = distance;
            this.createdAt = createdAt;
        }

        static RankedBubble active(SpeechBubbleHud bubble, PlayerMob perspective) {
            return new RankedBubble(bubble, null, priority(bubble.getCategory()),
                    distance(perspective, bubble.getMob()), bubble.getCreatedAt());
        }

        static RankedBubble incoming(IncomingBubble bubble, PlayerMob perspective) {
            return new RankedBubble(null, bubble, priority(bubble.category),
                    distance(perspective, bubble.mob), System.nanoTime());
        }
    }
}
