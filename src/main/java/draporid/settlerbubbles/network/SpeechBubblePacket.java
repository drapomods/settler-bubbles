package draporid.settlerbubbles.network;

import draporid.settlerbubbles.BubbleCategory;
import draporid.settlerbubbles.BubbleStyle;
import draporid.settlerbubbles.SettlerBubblesSettings;
import draporid.settlerbubbles.client.BubbleClientManager;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.Mob;
import necesse.level.maps.Level;

public class SpeechBubblePacket extends Packet {
    public final int mobUniqueID;
    public final BubbleCategory category;
    public final BubbleStyle style;
    public final GameMessage message;
    public final int durationHint;

    public SpeechBubblePacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);
        mobUniqueID = reader.getNextInt();
        category = BubbleCategory.fromID(reader.getNextByteUnsigned());
        style = BubbleStyle.fromID(reader.getNextByteUnsigned());
        message = GameMessage.fromContentPacket(reader.getNextContentPacket());
        durationHint = reader.getNextShortUnsigned();
    }

    public SpeechBubblePacket(int mobUniqueID, BubbleCategory category, GameMessage message, int durationHint) {
        this(mobUniqueID, category, BubbleStyle.forCategory(category), message, durationHint);
    }

    public SpeechBubblePacket(int mobUniqueID, BubbleCategory category, BubbleStyle style,
                              GameMessage message, int durationHint) {
        this.mobUniqueID = mobUniqueID;
        this.category = category;
        this.style = style == null ? BubbleStyle.forCategory(category) : style;
        this.message = message;
        this.durationHint = Math.max(2000, Math.min(8000, durationHint));
        PacketWriter writer = new PacketWriter(this);
        writer.putNextInt(mobUniqueID);
        writer.putNextByteUnsigned(category.ordinal());
        writer.putNextByteUnsigned(this.style.ordinal());
        writer.putNextContentPacket(message.getContentPacket());
        writer.putNextShortUnsigned(this.durationHint);
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        if (!SettlerBubblesSettings.enabled || !SettlerBubblesSettings.isCategoryEnabled(category)) {
            return;
        }
        Level level = client.getLevel();
        if (level == null || category == null || style == null || message == null) {
            return;
        }
        Mob mob = GameUtils.getLevelMob(mobUniqueID, level);
        if (mob == null || mob.removed()) {
            return;
        }
        String translated = message.translate();
        if (translated == null || translated.trim().isEmpty()) {
            return;
        }
        int textDuration = Math.max(durationHint, 2200 + Math.min(3600, translated.length() * 55));
        int duration = (int)(textDuration * SettlerBubblesSettings.durationScale);
        BubbleClientManager.show(level, mob, category, style, translated, duration);
    }
}
