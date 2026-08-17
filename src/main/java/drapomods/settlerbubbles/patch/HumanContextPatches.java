package drapomods.settlerbubbles.patch;

import drapomods.settlerbubbles.dialogue.DialogueManager;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.MobWasKilledEvent;
import necesse.entity.mobs.friendly.human.HumanMob;
import necesse.entity.mobs.job.EntityJobWorker;
import necesse.entity.mobs.job.JobTypeHandler;
import necesse.entity.mobs.job.LinkedListJobSequence;
import necesse.level.maps.levelData.jobs.AbstractLevelJob;
import necesse.level.maps.levelData.jobs.UseWorkstationLevelJob;
import necesse.level.maps.levelData.settlementData.SettlementWorkstationRecipe;
import net.bytebuddy.asm.Advice;

public class HumanContextPatches {
    @ModMethodPatch(target = UseWorkstationLevelJob.class, name = "getCraftActiveJob",
            arguments = {EntityJobWorker.class, JobTypeHandler.TypePriority.class,
                    SettlementWorkstationRecipe.class, LinkedListJobSequence.class})
    public static class WorkstationRecipePatch {
        @Advice.OnMethodEnter
        public static void enter(@Advice.This UseWorkstationLevelJob job,
                                 @Advice.Argument(0) EntityJobWorker worker,
                                 @Advice.Argument(2) SettlementWorkstationRecipe recipe) {
            DialogueManager.startedWorkstationJob(worker, job, recipe);
        }
    }

    @ModMethodPatch(target = HumanMob.class, name = "serverTick", arguments = {})
    public static class ServerTickPatch {
        @Advice.OnMethodExit
        public static void exit(@Advice.This HumanMob mob) {
            DialogueManager.periodicContext(mob);
        }
    }

    @ModMethodPatch(target = HumanMob.class, name = "onPerformedJob",
            arguments = {AbstractLevelJob.class, JobTypeHandler.TypePriority.class})
    public static class WorkPatch {
        @Advice.OnMethodExit
        public static void exit(@Advice.This HumanMob mob,
                                @Advice.Argument(0) AbstractLevelJob job) {
            DialogueManager.performedWork(mob, job);
        }
    }

    @ModMethodPatch(target = HumanMob.class, name = "doWasHitLogic",
            arguments = {MobWasHitEvent.class})
    public static class WasHitPatch {
        @Advice.OnMethodExit
        public static void exit(@Advice.This HumanMob mob,
                                @Advice.Argument(0) MobWasHitEvent event) {
            DialogueManager.wasHit(mob, event);
        }
    }

    @ModMethodPatch(target = HumanMob.class, name = "doHasKilledTarget",
            arguments = {MobWasKilledEvent.class})
    public static class KilledTargetPatch {
        @Advice.OnMethodExit
        public static void exit(@Advice.This HumanMob mob,
                                @Advice.Argument(0) MobWasKilledEvent event) {
            DialogueManager.killedTarget(mob, event);
        }
    }
}
