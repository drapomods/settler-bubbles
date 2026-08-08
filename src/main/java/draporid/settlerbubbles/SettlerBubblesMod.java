package draporid.settlerbubbles;

import draporid.settlerbubbles.command.BubblesClientCommand;
import draporid.settlerbubbles.network.SpeechBubblePacket;
import necesse.engine.commands.CommandsManager;
import necesse.engine.modLoader.ModSettings;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.registries.PacketRegistry;

@ModEntry
public class SettlerBubblesMod {
    public ModSettings initSettings() {
        return new SettlerBubblesSettings();
    }

    public void init() {
        PacketRegistry.registerPacket(SpeechBubblePacket.class);
    }

    public void postInit() {
        CommandsManager.registerClientCommand(new BubblesClientCommand());
    }
}
