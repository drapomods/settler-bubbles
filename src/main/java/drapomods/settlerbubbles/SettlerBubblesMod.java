package drapomods.settlerbubbles;

import drapomods.settlerbubbles.command.BubblesClientCommand;
import drapomods.settlerbubbles.client.BubbleControls;
import drapomods.settlerbubbles.network.SpeechBubblePacket;
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
        BubbleControls.register();
    }

    public void postInit() {
        CommandsManager.registerClientCommand(new BubblesClientCommand());
    }
}
