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
    public static final String MOD_ID = "drapomods.settlerbubbles";
    public static final String VERSION = "1.2.0";
    public static final String GAME_VERSION = "1.3.2";

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
