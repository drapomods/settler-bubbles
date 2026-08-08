package drapomods.settlerbubbles.client;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import drapomods.settlerbubbles.BubbleCategory;
import drapomods.settlerbubbles.BubbleStyle;
import necesse.entity.mobs.Mob;
import necesse.level.maps.Level;

public final class BubbleClientManager {
    private static final int MAX_VISIBLE_BUBBLES = 8;

    private BubbleClientManager() {
    }

    public static void show(Level level, Mob mob, BubbleCategory category, BubbleStyle style,
                            String message, int duration) {
        level.hudManager.removeElements(element -> element instanceof SpeechBubbleHud
                && ((SpeechBubbleHud)element).isFor(mob));

        List<SpeechBubbleHud> active = level.hudManager.streamElements()
                .filter(element -> element instanceof SpeechBubbleHud && !element.isRemoved())
                .map(element -> (SpeechBubbleHud)element)
                .sorted(Comparator.comparingLong(SpeechBubbleHud::getCreatedAt))
                .collect(Collectors.toList());

        int toRemove = active.size() - MAX_VISIBLE_BUBBLES + 1;
        for (int i = 0; i < toRemove; i++) {
            active.get(i).remove();
        }
        level.hudManager.addElement(new SpeechBubbleHud(mob, category, style, message, duration));
    }
}
