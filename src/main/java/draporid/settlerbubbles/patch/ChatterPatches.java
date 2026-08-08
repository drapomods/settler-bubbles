package draporid.settlerbubbles.patch;

import draporid.settlerbubbles.dialogue.DialogueManager;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.friendly.human.HumanMob;
import necesse.entity.mobs.friendly.human.HumanMobChatterHandler;
import necesse.entity.mobs.job.JobWorkerChatter;
import necesse.inventory.item.Item;
import net.bytebuddy.asm.Advice;

public class ChatterPatches {
    @ModMethodPatch(target = HumanMobChatterHandler.class, name = "runAndSendOtherChatterThought",
            arguments = {JobWorkerChatter.class, int.class})
    public static class OtherSettlerPatch {
        @Advice.OnMethodExit
        public static void exit(@Advice.This HumanMobChatterHandler handler,
                                @Advice.Argument(0) JobWorkerChatter other) {
            DialogueManager.socialPerson(handler.humanMob, other);
        }
    }

    @ModMethodPatch(target = HumanMobChatterHandler.class, name = "runAndSendChatterMobThought",
            arguments = {String.class, int.class})
    public static class AnimalPatch {
        @Advice.OnMethodExit
        public static void exit(@Advice.This HumanMobChatterHandler handler,
                                @Advice.Argument(0) String mobStringID) {
            DialogueManager.socialAnimal(handler.humanMob, mobStringID);
        }
    }

    @ModMethodPatch(target = HumanMobChatterHandler.class, name = "runAndSendChatterItemThought",
            arguments = {Item.class, int.class})
    public static class FoodPatch {
        @Advice.OnMethodExit
        public static void exit(@Advice.This HumanMobChatterHandler handler,
                                @Advice.Argument(0) Item item) {
            DialogueManager.socialFood(handler.humanMob, item);
        }
    }

    @ModMethodPatch(target = HumanMobChatterHandler.class, name = "runAndSendLoveThought",
            arguments = {int.class})
    public static class PositivePatch {
        @Advice.OnMethodExit
        public static void exit(@Advice.This HumanMobChatterHandler handler) {
            DialogueManager.socialReaction(handler.humanMob, true, false);
        }
    }

    @ModMethodPatch(target = HumanMobChatterHandler.class, name = "runAndSendDisagreeThought",
            arguments = {int.class})
    public static class NegativePatch {
        @Advice.OnMethodExit
        public static void exit(@Advice.This HumanMobChatterHandler handler) {
            DialogueManager.socialReaction(handler.humanMob, false, false);
        }
    }

    @ModMethodPatch(target = HumanMobChatterHandler.class, name = "runAndSendExcitedThought",
            arguments = {int.class})
    public static class FollowupPatch {
        @Advice.OnMethodExit
        public static void exit(@Advice.This HumanMobChatterHandler handler) {
            DialogueManager.socialReaction(handler.humanMob, true, true);
        }
    }

    @ModMethodPatch(target = HumanMobChatterHandler.class, name = "endConversation",
            arguments = {boolean.class, boolean.class})
    public static class EndConversationPatch {
        @Advice.OnMethodEnter
        public static void enter(@Advice.This HumanMobChatterHandler handler,
                                 @Advice.Argument(0) boolean completed,
                                 @Advice.Argument(1) boolean positive) {
            DialogueManager.conversationEnded(handler.humanMob, completed, positive);
        }
    }
}
