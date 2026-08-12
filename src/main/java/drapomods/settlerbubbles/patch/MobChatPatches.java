package drapomods.settlerbubbles.patch;

import drapomods.settlerbubbles.BubbleCategory;
import drapomods.settlerbubbles.BubbleStyle;
import drapomods.settlerbubbles.SettlerBubblesSettings;
import drapomods.settlerbubbles.client.BubbleClientManager;
import necesse.engine.network.NetworkPacket;
import necesse.engine.network.client.Client;
import necesse.engine.network.packet.PacketMobChat;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.friendly.human.HumanMob;
import necesse.level.maps.Level;
import net.bytebuddy.asm.Advice;

public class MobChatPatches {
    @ModMethodPatch(target = PacketMobChat.class, name = "processClient",
            arguments = {NetworkPacket.class, Client.class})
    public static class SettlerChatPatch {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean enter(@Advice.This PacketMobChat packet,
                                    @Advice.Argument(1) Client client) {
            if (!SettlerBubblesSettings.enabled
                    || !SettlerBubblesSettings.isCategoryEnabled(BubbleCategory.SOCIAL)
                    || packet.isFloatTextElement
                    || packet.message == null
                    || client == null) {
                return false;
            }

            Level level = client.getLevel();
            Mob mob = level == null ? null : GameUtils.getLevelMob(packet.mobUniqueID, level);
            if (!(mob instanceof HumanMob)) {
                return false;
            }
            HumanMob human = (HumanMob)mob;
            if (!human.isSettler() && !human.isVisitor()) {
                return false;
            }

            String translated = SettlerBubblesSettings.language.translate(packet.message);
            if (translated == null || translated.trim().isEmpty()) {
                return false;
            }
            int duration = Math.max(3000, Math.min(6000, translated.length() * 100));
            BubbleClientManager.show(level, client.getPlayer(), human,
                    BubbleCategory.SOCIAL, BubbleStyle.SPEECH, translated, duration);
            return true;
        }
    }
}
