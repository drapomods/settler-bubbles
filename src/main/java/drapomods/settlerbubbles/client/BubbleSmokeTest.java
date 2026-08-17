package drapomods.settlerbubbles.client;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import drapomods.settlerbubbles.BubbleCategory;
import drapomods.settlerbubbles.BubbleStyle;
import drapomods.settlerbubbles.SettlerBubblesSettings;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.localization.message.StaticMessage;
import necesse.engine.network.client.Client;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.friendly.human.HumanMob;
import necesse.level.maps.Level;

public final class BubbleSmokeTest {
    private static final long STEP_INTERVAL_MS = 3500L;
    private static final int BUBBLE_DURATION_MS = 3100;
    private static final Map<Client, TestState> ACTIVE = new WeakHashMap<>();
    private static final List<SmokeStep> STEPS = Arrays.asList(
            step("Social dialogue", BubbleCategory.SOCIAL, BubbleStyle.SPEECH, "social_person1"),
            step("Remembered agreement", BubbleCategory.SOCIAL, BubbleStyle.SPEECH,
                    "social_person_memory_positive1"),
            step("Remembered disagreement", BubbleCategory.SOCIAL, BubbleStyle.SPEECH,
                    "social_person_memory_negative1"),
            step("Mining work", BubbleCategory.WORK, BubbleStyle.SPEECH, "work_mine1"),
            step("Building work", BubbleCategory.WORK, BubbleStyle.SPEECH, "work_build1"),
            step("Cooking work", BubbleCategory.WORK, BubbleStyle.SPEECH, "work_cook1"),
            step("Care work", BubbleCategory.WORK, BubbleStyle.SPEECH, "work_care1"),
            step("Hauling work", BubbleCategory.WORK, BubbleStyle.SPEECH, "work_haul1"),
            step("Guard work", BubbleCategory.WORK, BubbleStyle.SPEECH, "work_guard1"),
            step("Hunger thought", BubbleCategory.NEEDS, BubbleStyle.THOUGHT, "needs_hungry1"),
            step("Rain reaction", BubbleCategory.MOOD, BubbleStyle.SPEECH, "event_rain1"),
            step("Positive mood", BubbleCategory.MOOD, BubbleStyle.THOUGHT, "mood_high1"),
            step("Combat shout", BubbleCategory.COMBAT, BubbleStyle.SHOUT, "combat_hit1"),
            step("Victory reaction", BubbleCategory.COMBAT, BubbleStyle.SPEECH, "event_victory1")
    );

    private BubbleSmokeTest() {
    }

    public static String toggle(Client client) {
        if (client == null || client.getLevel() == null || client.getPlayer() == null) {
            return "The smoke test can only run while playing.";
        }
        synchronized (ACTIVE) {
            if (ACTIVE.remove(client) != null) {
                return "Settler Bubbles smoke test cancelled.";
            }
            Level level = client.getLevel();
            List<Mob> speakers = level.entityManager.mobs.stream()
                    .filter(mob -> mob instanceof HumanMob)
                    .map(mob -> (HumanMob)mob)
                    .filter(mob -> !mob.removed() && (mob.isSettler() || mob.isVisitor()))
                    .sorted(Comparator.comparingDouble(mob -> mob.getDistance(client.getPlayer())))
                    .limit(6)
                    .collect(Collectors.toCollection(ArrayList::new));
            boolean usingPlayer = speakers.isEmpty();
            if (usingPlayer) {
                speakers.add(client.getPlayer());
            }
            ACTIVE.put(client, new TestState(level, speakers));
            return "Started a " + STEPS.size() + "-step visual smoke test (about "
                    + Math.round(STEPS.size() * STEP_INTERVAL_MS / 1000.0F)
                    + " seconds). Run /bubbles smoke again to cancel."
                    + (usingPlayer ? " No settler was nearby, so bubbles will use the player." : "");
        }
    }

    public static void tick(Client client) {
        TestState state;
        synchronized (ACTIVE) {
            state = ACTIVE.get(client);
        }
        if (state == null) {
            return;
        }
        if (client.getLevel() != state.level || client.getPlayer() == null) {
            synchronized (ACTIVE) {
                ACTIVE.remove(client);
            }
            return;
        }

        long now = System.nanoTime() / 1000000L;
        if (now < state.nextStepAt) {
            return;
        }
        if (state.step >= STEPS.size()) {
            synchronized (ACTIVE) {
                ACTIVE.remove(client);
            }
            client.setMessage("Settler Bubbles smoke test complete: "
                    + STEPS.size() + "/" + STEPS.size() + " steps shown.", Color.GREEN, 5.0F);
            return;
        }

        SmokeStep step = STEPS.get(state.step);
        Mob speaker = state.speakers.get(state.step % state.speakers.size());
        Mob other = state.speakers.get((state.step + 1) % state.speakers.size());
        LocalMessage message = new LocalMessage("settlerbubbles", step.key);
        StaticMessage subject = new StaticMessage(other.getDisplayName());
        message.addReplacement("other", subject);
        message.addReplacement("topic", subject);
        String translated = SettlerBubblesSettings.language.translate(message);
        BubbleClientManager.show(state.level, client.getPlayer(), speaker,
                step.category, step.style, translated, BUBBLE_DURATION_MS);

        int displayStep = state.step + 1;
        client.setMessage("Settler Bubbles smoke " + displayStep + "/" + STEPS.size()
                + ": " + step.label, Color.WHITE, 2.0F);
        state.step++;
        state.nextStepAt = now + STEP_INTERVAL_MS;
    }

    public static boolean isRunning(Client client) {
        synchronized (ACTIVE) {
            return ACTIVE.containsKey(client);
        }
    }

    private static SmokeStep step(String label, BubbleCategory category,
                                  BubbleStyle style, String key) {
        return new SmokeStep(label, category, style, key);
    }

    private static class SmokeStep {
        final String label;
        final BubbleCategory category;
        final BubbleStyle style;
        final String key;

        SmokeStep(String label, BubbleCategory category, BubbleStyle style, String key) {
            this.label = label;
            this.category = category;
            this.style = style;
            this.key = key;
        }
    }

    private static class TestState {
        final Level level;
        final List<Mob> speakers;
        int step;
        long nextStepAt;

        TestState(Level level, List<Mob> speakers) {
            this.level = level;
            this.speakers = speakers;
        }
    }
}
