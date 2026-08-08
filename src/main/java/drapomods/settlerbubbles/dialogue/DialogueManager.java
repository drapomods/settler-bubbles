package drapomods.settlerbubbles.dialogue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import drapomods.settlerbubbles.BubbleCategory;
import drapomods.settlerbubbles.SettlerBubblesSettings;
import drapomods.settlerbubbles.network.SpeechBubblePacket;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.localization.message.StaticMessage;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.registries.MobRegistry;
import necesse.engine.util.GameMath;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.MobWasKilledEvent;
import necesse.entity.mobs.friendly.human.HumanMob;
import necesse.entity.mobs.job.JobWorkerChatter;
import necesse.entity.mobs.job.JobWorkerChatterHandler;
import necesse.inventory.item.Item;
import necesse.level.maps.levelData.jobs.AbstractLevelJob;
import necesse.level.maps.levelData.settlementData.settler.personalities.SettlerPersonality;

public final class DialogueManager {
    private static final Map<HumanMob, SpeakerState> SPEAKERS = new WeakHashMap<>();
    private static final Map<HumanMob, ConversationState> CONVERSATIONS = new WeakHashMap<>();
    private static final Map<String, String[]> LINES = new HashMap<>();
    private static long nextGlobalAmbient;

    static {
        add("social_person", 4);
        add("social_animal", 4);
        add("social_food", 4);
        add("social_positive", 4);
        add("social_negative", 4);
        add("social_followup", 4);
        addConversationGroups("social_person");
        addConversationGroups("social_animal");
        addConversationGroups("social_food");
        add("work", 4);
        add("work_farm", 3);
        add("work_fish", 3);
        add("work_craft", 3);
        add("work_wood", 3);
        add("needs_hungry", 5);
        add("mood_low", 5);
        add("mood_high", 5);
        add("combat_hit", 5);
        add("combat_kill", 5);
        add("needs_injured", 4);
        add("needs_recreation", 4);
        add("needs_strike", 4);
        add("weather_rain", 4);
        add("weather_night", 4);
        add("activity_busy", 4);
        add("visitor", 4);
        add("idle", 4);

        addTone("social_person", Tone.ENERGETIC, 1);
        addTone("social_person", Tone.REFINED, 1);
        addTone("social_animal", Tone.GENTLE, 1);
        addTone("social_food", Tone.REFINED, 1);
        addTone("social_positive", Tone.ENERGETIC, 1);
        addTone("social_positive", Tone.GENTLE, 1);
        addTone("social_negative", Tone.TOUGH, 1);
        addTone("social_negative", Tone.STRESSED, 1);
        addTone("work", Tone.ENERGETIC, 1);
        addTone("work", Tone.TOUGH, 1);
        addTone("work", Tone.REFINED, 1);
        addTone("needs_hungry", Tone.STRESSED, 1);
        addTone("mood_low", Tone.GENTLE, 1);
        addTone("mood_low", Tone.STRESSED, 1);
        addTone("mood_high", Tone.ENERGETIC, 1);
        addTone("mood_high", Tone.REFINED, 1);
        addTone("combat_hit", Tone.GENTLE, 1);
        addTone("combat_hit", Tone.TOUGH, 1);
        addTone("combat_hit", Tone.STRESSED, 1);
        addTone("combat_kill", Tone.ENERGETIC, 1);
        addTone("combat_kill", Tone.TOUGH, 1);
    }

    private DialogueManager() {
    }

    public static void socialPerson(HumanMob speaker, JobWorkerChatter other) {
        if (!canSpeak(speaker) || other == null) {
            return;
        }
        GameMessage topic = new StaticMessage(other.getChatterMob().getDisplayName());
        startConversation(speaker, TopicKind.PERSON, topic);
        LocalMessage message = line(speaker, "social_person");
        message.addReplacement("other", topic);
        send(speaker, BubbleCategory.SOCIAL, message, 4300);
    }

    public static void socialAnimal(HumanMob speaker, String mobStringID) {
        if (!canSpeak(speaker) || mobStringID == null) {
            return;
        }
        GameMessage topic = MobRegistry.getLocalization(mobStringID);
        startConversation(speaker, TopicKind.ANIMAL, topic);
        LocalMessage message = line(speaker, "social_animal");
        message.addReplacement("animal", topic);
        send(speaker, BubbleCategory.SOCIAL, message, 4300);
    }

    public static void socialFood(HumanMob speaker, Item item) {
        if (!canSpeak(speaker) || item == null) {
            return;
        }
        GameMessage topic = ItemRegistry.getLocalization(item.getID());
        startConversation(speaker, TopicKind.FOOD, topic);
        LocalMessage message = line(speaker, "social_food");
        message.addReplacement("food", topic);
        send(speaker, BubbleCategory.SOCIAL, message, 4300);
    }

    public static void socialReaction(HumanMob speaker, boolean positive, boolean followup) {
        if (!canSpeak(speaker)) {
            return;
        }
        JobWorkerChatterHandler handler = speaker.getCurrentChatterHandler();
        if (handler == null || !handler.isInConversation()) {
            return;
        }
        ConversationState conversation = conversation(speaker);
        String group;
        if (conversation != null) {
            if (!followup) {
                conversation.positive = positive;
                conversation.reactionKnown = true;
            }
            boolean reaction = conversation.reactionKnown ? conversation.positive : positive;
            group = conversation.topicKind.prefix + (followup ? "_followup_" : "_")
                    + (reaction ? "positive" : "negative");
        } else {
            group = followup ? "social_followup" : positive ? "social_positive" : "social_negative";
        }
        LocalMessage message = line(speaker, group);
        JobWorkerChatterHandler other = handler.getCurrentlyInteractingWith();
        if (other != null) {
            message.addReplacement("other", new StaticMessage(other.getChatterMob().getDisplayName()));
        } else {
            message.addReplacement("other", new StaticMessage("friend"));
        }
        if (conversation != null) {
            message.addReplacement("topic", conversation.topic);
        }
        send(speaker, BubbleCategory.SOCIAL, message, 3700);
        if (followup && conversation != null) {
            finishConversation(conversation);
        }
    }

    public static void conversationEnded(HumanMob speaker, boolean completed, boolean positive) {
        ConversationState conversation = conversation(speaker);
        if (conversation == null || conversation.initiator != speaker) {
            return;
        }
        if (completed && !positive) {
            LocalMessage message = line(speaker, conversation.topicKind.prefix + "_followup_negative");
            message.addReplacement("topic", conversation.topic);
            message.addReplacement("other", new StaticMessage(conversation.listener.getDisplayName()));
            send(speaker, BubbleCategory.SOCIAL, message, 3700);
        }
        finishConversation(conversation);
    }

    public static void performedWork(HumanMob speaker, AbstractLevelJob job) {
        if (job == null) {
            return;
        }
        String id = job.getStringID().toLowerCase();
        String group;
        if (containsAny(id, "farm", "plant", "harvest", "fertil", "husband")) {
            group = "work_farm";
        } else if (containsAny(id, "fish")) {
            group = "work_fish";
        } else if (containsAny(id, "craft", "process", "cook")) {
            group = "work_craft";
        } else if (containsAny(id, "forest", "tree", "chop", "wood")) {
            group = "work_wood";
        } else {
            group = "work";
        }
        ambient(speaker, BubbleCategory.WORK, group, 0.35F, 3400);
    }

    public static void periodicContext(HumanMob speaker) {
        if (!canSpeak(speaker)) {
            return;
        }
        SpeakerState state = state(speaker);
        long now = speaker.getTime();
        if (now < state.nextContextCheck) {
            return;
        }
        state.nextContextCheck = now + 10000L;
        if (speaker.getCurrentChatterHandler() != null
                && speaker.getCurrentChatterHandler().isInConversation()) {
            return;
        }
        if (speaker.getHealthPercent() <= 0.35F) {
            ambient(speaker, BubbleCategory.NEEDS, "needs_injured", 0.55F, 3600);
        } else if (speaker.isOnStrike()) {
            ambient(speaker, BubbleCategory.NEEDS, "needs_strike", 0.55F, 3900);
        } else if (speaker.getHungerLevel() <= 0.28F) {
            ambient(speaker, BubbleCategory.NEEDS, "needs_hungry", 0.45F, 3600);
        } else if (speaker.getRecreationLevel() <= 0.20F) {
            ambient(speaker, BubbleCategory.NEEDS, "needs_recreation", 0.38F, 3600);
        } else if (speaker.getSettlerHappiness() <= 30) {
            ambient(speaker, BubbleCategory.MOOD, "mood_low", 0.32F, 3800);
        } else if (speaker.getSettlerHappiness() >= 80) {
            ambient(speaker, BubbleCategory.MOOD, "mood_high", 0.28F, 3500);
        } else if (speaker.getLevel().weatherLayer.isRaining()) {
            ambient(speaker, BubbleCategory.MOOD, "weather_rain", 0.24F, 3500);
        } else if (speaker.isVisitor()) {
            ambient(speaker, BubbleCategory.MOOD, "visitor", 0.20F, 3700);
        } else if (speaker.getWorldEntity().isNight()) {
            ambient(speaker, BubbleCategory.MOOD, "weather_night", 0.18F, 3500);
        } else if (speaker.hasActiveJob() && speaker.getCurrentActivity() != null) {
            LocalMessage message = line(speaker, "activity_busy");
            message.addReplacement("activity", speaker.getCurrentActivity());
            ambient(speaker, BubbleCategory.WORK, message, 0.20F, 3500);
        } else if (!speaker.hasActiveJob()) {
            ambient(speaker, BubbleCategory.MOOD, "idle", 0.12F, 3400);
        }
    }

    public static void wasHit(HumanMob speaker, MobWasHitEvent event) {
        if (event != null && event.damage > 0 && speaker.getHealth() > 0) {
            ambient(speaker, BubbleCategory.COMBAT, "combat_hit", 0.50F, 2800);
        }
    }

    public static void killedTarget(HumanMob speaker, MobWasKilledEvent event) {
        if (event != null && event.target != null && event.target != speaker) {
            ambient(speaker, BubbleCategory.COMBAT, "combat_kill", 0.75F, 3000);
        }
    }

    private static void ambient(HumanMob speaker, BubbleCategory category, String group,
                                float baseChance, int duration) {
        ambient(speaker, category, line(speaker, group), baseChance, duration);
    }

    private static void ambient(HumanMob speaker, BubbleCategory category, LocalMessage message,
                                float baseChance, int duration) {
        if (!canSpeak(speaker) || !SettlerBubblesSettings.isCategoryEnabled(category)) {
            return;
        }
        JobWorkerChatterHandler chatter = speaker.getCurrentChatterHandler();
        if (chatter != null && chatter.isInConversation()) {
            return;
        }
        SpeakerState state = state(speaker);
        long now = speaker.getTime();
        long processNow = System.nanoTime() / 1000000L;
        if (now < state.nextAmbient || processNow < nextGlobalAmbient) {
            return;
        }
        float multiplier = SettlerBubblesSettings.frequency.multiplier;
        float chance = GameMath.limit(baseChance * multiplier, 0.0F, 0.95F);
        if (!GameRandom.globalRandom.getChance(chance)) {
            return;
        }
        state.nextAmbient = now + (long)(45000L / multiplier);
        nextGlobalAmbient = processNow + 3000L;
        send(speaker, category, message, duration);
    }

    private static void send(HumanMob speaker, BubbleCategory category, GameMessage message, int duration) {
        if (!SettlerBubblesSettings.isCategoryEnabled(category)
                || speaker.getServer() == null
                || speaker.getLevel() == null) {
            return;
        }
        speaker.getServer().network.sendToClientsAtEntireLevel(
                new SpeechBubblePacket(speaker.getUniqueID(), category, message, duration),
                speaker.getLevel()
        );
    }

    private static boolean canSpeak(HumanMob speaker) {
        return speaker != null
                && speaker.isServer()
                && !speaker.removed()
                && (speaker.isSettler() || speaker.isVisitor());
    }

    private static LocalMessage line(HumanMob speaker, String group) {
        SpeakerState state = state(speaker);
        List<String> candidates = new ArrayList<>();
        String[] base = LINES.get(group);
        if (base != null) {
            candidates.addAll(Arrays.asList(base));
        }
        String[] flavored = LINES.get(group + "." + tone(speaker).name().toLowerCase());
        if (flavored != null) {
            candidates.addAll(Arrays.asList(flavored));
            candidates.addAll(Arrays.asList(flavored));
        }
        if (candidates.isEmpty()) {
            candidates.add("work1");
        }

        String chosen = candidates.get(GameRandom.globalRandom.nextInt(candidates.size()));
        for (int i = 0; i < 8 && state.recent.contains(chosen) && candidates.size() > 1; i++) {
            chosen = candidates.get(GameRandom.globalRandom.nextInt(candidates.size()));
        }
        state.recent.addLast(chosen);
        while (state.recent.size() > 5) {
            state.recent.removeFirst();
        }
        return new LocalMessage("settlerbubbles", chosen);
    }

    private static Tone tone(HumanMob speaker) {
        for (SettlerPersonality personality : speaker.getPersonalities()) {
            String id = personality.getStringID().toLowerCase();
            if (containsAny(id, "fastworker", "jogger", "adventurer", "persistent", "inspiring")) {
                return Tone.ENERGETIC;
            }
            if (containsAny(id, "pacifist", "ecologist", "gardener", "roomie", "friendly")) {
                return Tone.GENTLE;
            }
            if (containsAny(id, "bloodthirsty", "vengeful", "warrior", "ranger", "lumberjack")) {
                return Tone.TOUGH;
            }
            if (containsAny(id, "artconnoisseur", "audiophile", "fashionista", "collector", "orderly")) {
                return Tone.REFINED;
            }
            if (containsAny(id, "stressed", "indoorsy", "clumsy")) {
                return Tone.STRESSED;
            }
        }
        return Tone.DEFAULT;
    }

    private static SpeakerState state(HumanMob speaker) {
        synchronized (SPEAKERS) {
            SpeakerState state = SPEAKERS.get(speaker);
            if (state == null) {
                state = new SpeakerState();
                SPEAKERS.put(speaker, state);
            }
            return state;
        }
    }

    private static void startConversation(HumanMob speaker, TopicKind topicKind, GameMessage topic) {
        JobWorkerChatterHandler handler = speaker.getCurrentChatterHandler();
        JobWorkerChatterHandler otherHandler = handler == null ? null : handler.getCurrentlyInteractingWith();
        if (otherHandler == null || !(otherHandler.getChatterMob() instanceof HumanMob)) {
            return;
        }
        HumanMob listener = (HumanMob)otherHandler.getChatterMob();
        ConversationState conversation = new ConversationState(speaker, listener, topicKind, topic);
        synchronized (CONVERSATIONS) {
            ConversationState previousSpeaker = CONVERSATIONS.get(speaker);
            ConversationState previousListener = CONVERSATIONS.get(listener);
            if (previousSpeaker != null) {
                finishConversation(previousSpeaker);
            }
            if (previousListener != null && previousListener != previousSpeaker) {
                finishConversation(previousListener);
            }
            CONVERSATIONS.put(speaker, conversation);
            CONVERSATIONS.put(listener, conversation);
        }
    }

    private static ConversationState conversation(HumanMob speaker) {
        synchronized (CONVERSATIONS) {
            return CONVERSATIONS.get(speaker);
        }
    }

    private static void finishConversation(ConversationState conversation) {
        synchronized (CONVERSATIONS) {
            if (CONVERSATIONS.get(conversation.initiator) == conversation) {
                CONVERSATIONS.remove(conversation.initiator);
            }
            if (CONVERSATIONS.get(conversation.listener) == conversation) {
                CONVERSATIONS.remove(conversation.listener);
            }
        }
    }

    private static void add(String group, int count) {
        String[] keys = new String[count];
        for (int i = 0; i < count; i++) {
            keys[i] = group + (i + 1);
        }
        LINES.put(group, keys);
    }

    private static void addTone(String group, Tone tone, int count) {
        String[] keys = new String[count];
        for (int i = 0; i < count; i++) {
            keys[i] = group + "_" + tone.name().toLowerCase() + (i + 1);
        }
        LINES.put(group + "." + tone.name().toLowerCase(), keys);
    }

    private static void addConversationGroups(String prefix) {
        add(prefix + "_positive", 4);
        add(prefix + "_negative", 4);
        add(prefix + "_followup_positive", 3);
        add(prefix + "_followup_negative", 3);
    }

    private static boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private enum Tone {
        DEFAULT,
        ENERGETIC,
        GENTLE,
        TOUGH,
        REFINED,
        STRESSED
    }

    private enum TopicKind {
        PERSON("social_person"),
        ANIMAL("social_animal"),
        FOOD("social_food");

        final String prefix;

        TopicKind(String prefix) {
            this.prefix = prefix;
        }
    }

    private static class ConversationState {
        final HumanMob initiator;
        final HumanMob listener;
        final TopicKind topicKind;
        final GameMessage topic;
        boolean positive;
        boolean reactionKnown;

        ConversationState(HumanMob initiator, HumanMob listener, TopicKind topicKind, GameMessage topic) {
            this.initiator = initiator;
            this.listener = listener;
            this.topicKind = topicKind;
            this.topic = topic;
        }
    }

    private static class SpeakerState {
        long nextAmbient;
        long nextContextCheck;
        final Deque<String> recent = new ArrayDeque<>();
    }
}
