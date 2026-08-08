package draporid.settlerbubbles.api;

import java.util.Collection;

@FunctionalInterface
public interface DialogueProvider {
    Collection<BubbleLine> getLines(BubbleContext context);
}

