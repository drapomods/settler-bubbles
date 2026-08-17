package drapomods.settlerbubbles.dialogue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import drapomods.settlerbubbles.BubbleCategory;
import drapomods.settlerbubbles.BubbleStyle;
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
import necesse.entity.mobs.job.EntityJobWorker;
import necesse.entity.mobs.job.JobWorkerChatter;
import necesse.entity.mobs.job.JobWorkerChatterHandler;
import necesse.inventory.item.Item;
import necesse.level.maps.Level;
import necesse.level.maps.levelData.jobs.AbstractLevelJob;
import necesse.level.maps.levelData.settlementData.SettlementWorkstationRecipe;
import necesse.level.maps.levelData.settlementData.settler.personalities.SettlerPersonality;

public final class DialogueManager {
    private static final int SPEAKER_HISTORY_SIZE = 8;
    private static final int SETTLEMENT_HISTORY_SIZE = 18;
    private static final int EVENT_REACTION_RANGE = 450;

    private static final Map<HumanMob, SpeakerState> SPEAKERS = new WeakHashMap<>();
    private static final Map<HumanMob, ConversationState> CONVERSATIONS = new WeakHashMap<>();
    private static final Map<HumanMob, WorkContext> WORK_CONTEXTS = new WeakHashMap<>();
    private static final Map<Level, SettlementState> SETTLEMENTS = new WeakHashMap<>();
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
        addMemoryGroups("social_person");
        addMemoryGroups("social_animal");
        addMemoryGroups("social_food");
        add("work", 4);
        add("work_farm", 3);
        add("work_fish", 3);
        add("work_craft", 3);
        add("work_wood", 3);
        add("work_mine", 3);
        add("work_build", 3);
        add("work_cook", 3);
        add("work_care", 3);
        add("work_haul", 3);
        add("work_guard", 3);
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
        add("visitor", 4);
        add("idle", 4);
        add("event_attack", 2);
        add("event_injury", 2);
        add("event_food_shortage", 2);
        add("event_rain", 2);
        add("event_strike", 2);
        add("event_victory", 2);

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
        int topicUniqueID = other.getChatterMob().getUniqueID();
        GameMessage topic = new StaticMessage(other.getChatterMob().getDisplayName());
        ConversationState conversation = startConversation(speaker, TopicKind.PERSON,
                Integer.toString(topicUniqueID), topic);
        LocalMessage message = openingLine(speaker, conversation, "social_person");
        message.addReplacement("other", topic);
        message.addReplacement("topic", topic);
        send(speaker, BubbleCategory.SOCIAL, message, 4300);
    }

    public static void socialAnimal(HumanMob speaker, String mobStringID) {
        if (!canSpeak(speaker) || mobStringID == null) {
            return;
        }
        GameMessage topic = MobRegistry.getLocalization(mobStringID);
        ConversationState conversation = startConversation(speaker, TopicKind.ANIMAL,
                mobStringID, topic);
        LocalMessage message = openingLine(speaker, conversation, "social_animal");
        message.addReplacement("animal", topic);
        message.addReplacement("topic", topic);
        send(speaker, BubbleCategory.SOCIAL, message, 4300);
    }

    public static void socialFood(HumanMob speaker, Item item) {
        if (!canSpeak(speaker) || item == null) {
            return;
        }
        GameMessage topic = ItemRegistry.getLocalization(item.getID());
        ConversationState conversation = startConversation(speaker, TopicKind.FOOD,
                item.getStringID(), topic);
        LocalMessage message = openingLine(speaker, conversation, "social_food");
        message.addReplacement("food", topic);
        message.addReplacement("topic", topic);
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
                Boolean rememberedOpinion = state(speaker).opinions.get(conversation.topicKey);
                conversation.positive = rememberedOpinion == null ? positive : rememberedOpinion;
                conversation.reactionKnown = true;
                state(speaker).opinions.put(conversation.topicKey, conversation.positive);
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
            if (conversation.reactionKnown) {
                rememberConversation(conversation);
            }
            finishConversation(conversation);
        }
    }

    public static void conversationEnded(HumanMob speaker, boolean completed, boolean positive) {
        ConversationState conversation = conversation(speaker);
        if (conversation == null || conversation.initiator != speaker) {
            return;
        }
        boolean effectivePositive = conversation.reactionKnown
                ? conversation.positive : positive;
        if (completed && !effectivePositive) {
            LocalMessage message = line(speaker, conversation.topicKind.prefix + "_followup_negative");
            message.addReplacement("topic", conversation.topic);
            message.addReplacement("other", new StaticMessage(conversation.listener.getDisplayName()));
            send(speaker, BubbleCategory.SOCIAL, message, 3700);
        }
        if (conversation.reactionKnown) {
            rememberConversation(conversation);
        }
        finishConversation(conversation);
    }

    public static void performedWork(HumanMob speaker, AbstractLevelJob job) {
        if (job == null) {
            return;
        }
        String id = job.getStringID().toLowerCase() + " "
                + job.getClass().getSimpleName().toLowerCase();
        if (speaker.currentActivity != null
                && speaker.currentActivity.getCurrentActivity() instanceof LocalMessage) {
            LocalMessage activity = (LocalMessage)speaker.currentActivity.getCurrentActivity();
            id += " " + (activity.category == null ? "" : activity.category.toLowerCase())
                    + " " + (activity.key == null ? "" : activity.key.toLowerCase());
        }
        WorkContext rememberedContext;
        synchronized (WORK_CONTEXTS) {
            rememberedContext = WORK_CONTEXTS.remove(speaker);
        }
        String group;
        if (rememberedContext != null && rememberedContext.job == job) {
            group = rememberedContext.group;
        } else if (containsAny(id, "mine", "mining", "ore")) {
            group = "work_mine";
        } else if (containsAny(id, "placeobject", "build", "construct", "repair")) {
            group = "work_build";
        } else if (containsAny(id, "cook", "meal", "foodrecipe")) {
            group = "work_cook";
        } else if (containsAny(id, "husband", "milk", "shear", "care", "heal", "medical")) {
            group = "work_care";
        } else if (containsAny(id, "haul", "shipping", "storepickup", "transport", "deliver")) {
            group = "work_haul";
        } else if (containsAny(id, "guard", "patrol", "defend")) {
            group = "work_guard";
        } else if (containsAny(id, "farm", "plant", "harvest", "fertil")) {
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

    public static void startedWorkstationJob(EntityJobWorker worker, AbstractLevelJob job,
                                             SettlementWorkstationRecipe recipe) {
        if (worker == null || job == null || recipe == null || recipe.recipe == null
                || recipe.recipe.resultItem == null
                || !(worker.getMobWorker() instanceof HumanMob)) {
            return;
        }
        HumanMob speaker = (HumanMob)worker.getMobWorker();
        if (recipe.recipe.resultItem.item.isFoodItem()) {
            synchronized (WORK_CONTEXTS) {
                WORK_CONTEXTS.put(speaker, new WorkContext(job, "work_cook"));
            }
        }
    }

    public static void periodicContext(HumanMob speaker) {
        if (!canSpeak(speaker)) {
            return;
        }
        SpeakerState state = state(speaker);
        processPending(speaker, state);
        long now = speaker.getTime();
        if (now < state.nextContextCheck) {
            return;
        }
        state.nextContextCheck = now + 10000L;
        if (speaker.getCurrentChatterHandler() != null
                && speaker.getCurrentChatterHandler().isInConversation()) {
            return;
        }
        handleRainState(speaker);

        if (speaker.getHealthPercent() > 0.50F) {
            state.injuryAnnounced = false;
        }
        if (!speaker.isOnStrike()) {
            state.strikeAnnounced = false;
        }
        if (speaker.getHungerLevel() > 0.45F) {
            state.hungerAnnounced = false;
        }

        if (speaker.getHealthPercent() <= 0.35F) {
            if (!state.injuryAnnounced) {
                state.injuryAnnounced = announceCondition(speaker, BubbleCategory.NEEDS,
                        "needs_injured", "event_injury", 3700);
                if (!state.injuryAnnounced) {
                    ambient(speaker, BubbleCategory.NEEDS, "needs_injured", 0.55F, 3600);
                }
            } else {
                ambient(speaker, BubbleCategory.NEEDS, "needs_injured", 0.55F, 3600);
            }
        } else if (speaker.isOnStrike()) {
            if (!state.strikeAnnounced) {
                state.strikeAnnounced = announceCondition(speaker, BubbleCategory.NEEDS,
                        "needs_strike", "event_strike", 3900);
                if (!state.strikeAnnounced) {
                    ambient(speaker, BubbleCategory.NEEDS, "needs_strike", 0.55F, 3900);
                }
            } else {
                ambient(speaker, BubbleCategory.NEEDS, "needs_strike", 0.55F, 3900);
            }
        } else if (speaker.getHungerLevel() <= 0.28F) {
            if (!state.hungerAnnounced && isFoodShortage(speaker)) {
                state.hungerAnnounced = announceCondition(speaker, BubbleCategory.NEEDS,
                        "needs_hungry", "event_food_shortage", 3700);
                if (!state.hungerAnnounced) {
                    ambient(speaker, BubbleCategory.NEEDS, "needs_hungry", 0.45F, 3600);
                }
            } else {
                ambient(speaker, BubbleCategory.NEEDS, "needs_hungry", 0.45F, 3600);
            }
        } else if (speaker.getRecreationLevel() <= 0.20F) {
            ambient(speaker, BubbleCategory.NEEDS, "needs_recreation", 0.38F, 3600);
        } else if (speaker.getSettlerHappiness() <= 30) {
            ambient(speaker, BubbleCategory.MOOD, "mood_low", 0.32F, 3800);
        } else if (speaker.getSettlerHappiness() >= 80) {
            ambient(speaker, BubbleCategory.MOOD, "mood_high", 0.28F, 3500);
        } else if (speaker.getLevel().weatherLayer.isRaining()) {
            ambient(speaker, BubbleCategory.MOOD, "weather_rain", 0.24F, 3500);
        } else if (speaker.hasCommandOrders() && speaker.commandGuardPoint != null) {
            ambient(speaker, BubbleCategory.WORK, "work_guard", 0.24F, 3500);
        } else if (speaker.isVisitor()) {
            ambient(speaker, BubbleCategory.MOOD, "visitor", 0.20F, 3700);
        } else if (speaker.getWorldEntity().isNight()) {
            ambient(speaker, BubbleCategory.MOOD, "weather_night", 0.18F, 3500);
        } else if (!speaker.hasActiveJob()) {
            ambient(speaker, BubbleCategory.MOOD, "idle", 0.12F, 3400);
        }
    }

    private static boolean isFoodShortage(HumanMob speaker) {
        return speaker.getLevel().entityManager.mobs.stream()
                .filter(mob -> mob instanceof HumanMob)
                .map(mob -> (HumanMob)mob)
                .filter(other -> !other.removed() && other.isSettler())
                .filter(other -> other.getHungerLevel() <= 0.28F)
                .limit(2)
                .count() >= 2;
    }

    public static void wasHit(HumanMob speaker, MobWasHitEvent event) {
        if (event != null && event.damage > 0 && speaker.getHealth() > 0) {
            if (ambient(speaker, BubbleCategory.COMBAT, "combat_hit", 0.50F, 2800)) {
                queueNearbyReaction(speaker, BubbleCategory.COMBAT,
                        BubbleStyle.SPEECH, "event_attack", 1450L);
            }
        }
    }

    public static void killedTarget(HumanMob speaker, MobWasKilledEvent event) {
        if (event != null && event.target != null && event.target != speaker) {
            if (ambient(speaker, BubbleCategory.COMBAT, "combat_kill", 0.75F, 3000)) {
                queueNearbyReaction(speaker, BubbleCategory.COMBAT,
                        BubbleStyle.SPEECH, "event_victory", 1550L);
            }
        }
    }

    private static boolean ambient(HumanMob speaker, BubbleCategory category, String group,
                                   float baseChance, int duration) {
        if (!canSpeak(speaker) || !SettlerBubblesSettings.isCategoryEnabled(category)) {
            return false;
        }
        JobWorkerChatterHandler chatter = speaker.getCurrentChatterHandler();
        if (chatter != null && chatter.isInConversation()) {
            return false;
        }
        SpeakerState state = state(speaker);
        long now = speaker.getTime();
        long processNow = System.nanoTime() / 1000000L;
        if (now < state.nextAmbient || processNow < nextGlobalAmbient) {
            return false;
        }
        float multiplier = SettlerBubblesSettings.frequency.multiplier;
        float chance = GameMath.limit(baseChance * multiplier, 0.0F, 0.95F);
        if (!GameRandom.globalRandom.getChance(chance)) {
            return false;
        }
        state.nextAmbient = now + (long)(45000L / multiplier);
        nextGlobalAmbient = processNow + 3000L;
        send(speaker, category, line(speaker, group), duration);
        return true;
    }

    private static void handleRainState(HumanMob speaker) {
        SettlementState state = settlement(speaker.getLevel());
        boolean raining = speaker.getLevel().weatherLayer.isRaining();
        if (raining && !state.raining) {
            state.raining = announceCondition(speaker, BubbleCategory.MOOD,
                    "weather_rain", "event_rain", 3600);
        } else if (!raining) {
            state.raining = false;
        }
    }

    private static boolean announceCondition(HumanMob speaker, BubbleCategory category,
                                             String openerGroup, String responseGroup,
                                             int duration) {
        if (!canSpeak(speaker) || !SettlerBubblesSettings.isCategoryEnabled(category)
                || !reserveEventSlot(speaker)) {
            return false;
        }
        SpeakerState speakerState = state(speaker);
        speakerState.nextAmbient = speaker.getTime() + 30000L;
        nextGlobalAmbient = System.nanoTime() / 1000000L + 3000L;
        send(speaker, category, line(speaker, openerGroup), duration);
        queueNearbyReactionReserved(speaker, category, BubbleStyle.SPEECH,
                responseGroup, 1500L);
        return true;
    }

    private static boolean queueNearbyReaction(HumanMob source, BubbleCategory category,
                                               BubbleStyle style, String group, long delay) {
        if (!reserveEventSlot(source)) {
            return false;
        }
        return queueNearbyReactionReserved(source, category, style, group, delay);
    }

    private static boolean queueNearbyReactionReserved(HumanMob source, BubbleCategory category,
                                                       BubbleStyle style, String group, long delay) {
        HumanMob witness = nearestAvailableSettler(source);
        if (witness == null) {
            return false;
        }
        LocalMessage message = line(witness, group);
        message.addReplacement("other", new StaticMessage(source.getDisplayName()));
        state(witness).pending.addLast(new PendingBubble(source.getLevel(), category, style,
                message, 3600, source.getTime() + delay));
        return true;
    }

    private static boolean reserveEventSlot(HumanMob source) {
        SettlementState settlement = settlement(source.getLevel());
        long now = source.getTime();
        if (now < settlement.nextEventReaction) {
            return false;
        }
        settlement.nextEventReaction = now + 6500L;
        return true;
    }

    private static HumanMob nearestAvailableSettler(HumanMob source) {
        if (source == null || source.getLevel() == null) {
            return null;
        }
        return source.getLevel().entityManager.mobs.stream()
                .filter(mob -> mob instanceof HumanMob)
                .map(mob -> (HumanMob)mob)
                .filter(other -> other != source && canSpeak(other))
                .filter(other -> other.getDistance(source) <= EVENT_REACTION_RANGE)
                .filter(other -> {
                    JobWorkerChatterHandler chatter = other.getCurrentChatterHandler();
                    return chatter == null || !chatter.isInConversation();
                })
                .min(Comparator.comparingDouble(other -> other.getDistance(source)))
                .orElse(null);
    }

    private static void processPending(HumanMob speaker, SpeakerState state) {
        long now = speaker.getTime();
        while (!state.pending.isEmpty() && state.pending.peekFirst().dueTime <= now) {
            PendingBubble pending = state.pending.removeFirst();
            if (speaker.getLevel() == pending.level && !speaker.removed()) {
                send(speaker, pending.category, pending.style,
                        pending.message, pending.duration);
            }
        }
    }

    private static void send(HumanMob speaker, BubbleCategory category, GameMessage message, int duration) {
        send(speaker, category, BubbleStyle.forCategory(category), message, duration);
    }

    private static void send(HumanMob speaker, BubbleCategory category, BubbleStyle style,
                             GameMessage message, int duration) {
        if (!SettlerBubblesSettings.isCategoryEnabled(category)
                || speaker.getServer() == null
                || speaker.getLevel() == null) {
            return;
        }
        speaker.getServer().network.sendToClientsAtEntireLevel(
                new SpeechBubblePacket(speaker.getUniqueID(), category, style, message, duration),
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
        SettlementState settlement = settlement(speaker.getLevel());
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

        List<String> preferred = filterRecent(candidates, state.recent, settlement.recent);
        if (preferred.isEmpty()) {
            preferred = filterRecent(candidates, state.recent, null);
        }
        if (preferred.isEmpty()) {
            preferred = candidates;
        }
        String chosen = preferred.get(GameRandom.globalRandom.nextInt(preferred.size()));
        state.recent.addLast(chosen);
        while (state.recent.size() > SPEAKER_HISTORY_SIZE) {
            state.recent.removeFirst();
        }
        settlement.recent.addLast(chosen);
        while (settlement.recent.size() > SETTLEMENT_HISTORY_SIZE) {
            settlement.recent.removeFirst();
        }
        return new LocalMessage("settlerbubbles", chosen);
    }

    private static List<String> filterRecent(List<String> candidates, Deque<String> speakerRecent,
                                             Deque<String> settlementRecent) {
        List<String> filtered = new ArrayList<>();
        for (String candidate : candidates) {
            if (!speakerRecent.contains(candidate)
                    && (settlementRecent == null || !settlementRecent.contains(candidate))) {
                filtered.add(candidate);
            }
        }
        return filtered;
    }

    private static LocalMessage openingLine(HumanMob speaker, ConversationState conversation,
                                            String defaultGroup) {
        if (conversation == null || conversation.previous == null) {
            return line(speaker, defaultGroup);
        }
        return line(speaker, defaultGroup + "_memory_"
                + (conversation.previous.positive ? "positive" : "negative"));
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

    private static ConversationState startConversation(HumanMob speaker, TopicKind topicKind,
                                                       String topicID, GameMessage topic) {
        JobWorkerChatterHandler handler = speaker.getCurrentChatterHandler();
        JobWorkerChatterHandler otherHandler = handler == null ? null : handler.getCurrentlyInteractingWith();
        if (otherHandler == null || !(otherHandler.getChatterMob() instanceof HumanMob)) {
            return null;
        }
        HumanMob listener = (HumanMob)otherHandler.getChatterMob();
        String topicKey = topicKind.prefix + ":" + topicID;
        ConversationMemory previous = state(speaker).conversations.get(
                conversationKey(listener, topicKey));
        ConversationState conversation = new ConversationState(speaker, listener,
                topicKind, topicKey, topic, previous);
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
        return conversation;
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

    private static void rememberConversation(ConversationState conversation) {
        rememberConversation(conversation.initiator, conversation.listener,
                conversation.topicKey, conversation.positive);
        rememberConversation(conversation.listener, conversation.initiator,
                conversation.topicKey, conversation.positive);
    }

    private static void rememberConversation(HumanMob speaker, HumanMob other,
                                             String topicKey, boolean positive) {
        SpeakerState state = state(speaker);
        state.conversations.put(conversationKey(other, topicKey),
                new ConversationMemory(positive));
        trimMap(state.conversations, 64);
        trimMap(state.opinions, 64);
    }

    private static String conversationKey(HumanMob other, String topicKey) {
        return other.getUniqueID() + "|" + topicKey;
    }

    private static <T> void trimMap(Map<String, T> map, int maximum) {
        while (map.size() > maximum) {
            map.remove(map.keySet().iterator().next());
        }
    }

    private static SettlementState settlement(Level level) {
        synchronized (SETTLEMENTS) {
            SettlementState state = SETTLEMENTS.get(level);
            if (state == null) {
                state = new SettlementState();
                SETTLEMENTS.put(level, state);
            }
            return state;
        }
    }

    public static String getDebugSummary() {
        int speakers;
        int opinions = 0;
        int rememberedConversations = 0;
        int pending = 0;
        synchronized (SPEAKERS) {
            speakers = SPEAKERS.size();
            for (SpeakerState state : SPEAKERS.values()) {
                opinions += state.opinions.size();
                rememberedConversations += state.conversations.size();
                pending += state.pending.size();
            }
        }
        synchronized (CONVERSATIONS) {
            return "speakers=" + speakers
                    + ", opinions=" + opinions
                    + ", memories=" + rememberedConversations
                    + ", activeConversations=" + (CONVERSATIONS.size() / 2)
                    + ", pendingReactions=" + pending;
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

    private static void addMemoryGroups(String prefix) {
        add(prefix + "_memory_positive", 2);
        add(prefix + "_memory_negative", 2);
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
        final String topicKey;
        final GameMessage topic;
        final ConversationMemory previous;
        boolean positive;
        boolean reactionKnown;

        ConversationState(HumanMob initiator, HumanMob listener, TopicKind topicKind,
                          String topicKey, GameMessage topic, ConversationMemory previous) {
            this.initiator = initiator;
            this.listener = listener;
            this.topicKind = topicKind;
            this.topicKey = topicKey;
            this.topic = topic;
            this.previous = previous;
        }
    }

    private static class ConversationMemory {
        final boolean positive;

        ConversationMemory(boolean positive) {
            this.positive = positive;
        }
    }

    private static class PendingBubble {
        final Level level;
        final BubbleCategory category;
        final BubbleStyle style;
        final GameMessage message;
        final int duration;
        final long dueTime;

        PendingBubble(Level level, BubbleCategory category, BubbleStyle style,
                      GameMessage message, int duration, long dueTime) {
            this.level = level;
            this.category = category;
            this.style = style;
            this.message = message;
            this.duration = duration;
            this.dueTime = dueTime;
        }
    }

    private static class SettlementState {
        final Deque<String> recent = new ArrayDeque<>();
        long nextEventReaction;
        boolean raining;
    }

    private static class WorkContext {
        final AbstractLevelJob job;
        final String group;

        WorkContext(AbstractLevelJob job, String group) {
            this.job = job;
            this.group = group;
        }
    }

    private static class SpeakerState {
        final Deque<String> recent = new ArrayDeque<>();
        final Map<String, Boolean> opinions = new LinkedHashMap<String, Boolean>(16, 0.75F, true);
        final Map<String, ConversationMemory> conversations =
                new LinkedHashMap<String, ConversationMemory>(16, 0.75F, true);
        final Deque<PendingBubble> pending = new ArrayDeque<>();
        long nextAmbient;
        long nextContextCheck;
        boolean injuryAnnounced;
        boolean hungerAnnounced;
        boolean strikeAnnounced;
    }
}
