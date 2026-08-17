package drapomods.settlerbubbles.command;

import drapomods.settlerbubbles.SettlerBubblesSettings;
import drapomods.settlerbubbles.client.BubbleDiagnostics;
import drapomods.settlerbubbles.client.BubbleSettingsMenuController;
import drapomods.settlerbubbles.client.BubbleSmokeTest;
import necesse.engine.commands.CmdParameter;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.commands.parameterHandlers.PresetStringParameterHandler;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;

public class BubblesClientCommand extends ModularChatCommand {
    public BubblesClientCommand() {
        super("bubbles", "Control, inspect or test Settler Bubbles for this client.",
                PermissionLevel.USER, false,
                new CmdParameter("state",
                        new PresetStringParameterHandler("on", "off", "toggle",
                                "settings", "debug", "smoke"), true));
    }

    @Override
    public void runModular(Client client, Server server, ServerClient serverClient,
                           Object[] args, String[] errors, CommandLog commandLog) {
        String state = args[0] instanceof String ? (String)args[0] : "toggle";
        if ("settings".equalsIgnoreCase(state)) {
            if (!BubbleSettingsMenuController.open(client)) {
                commandLog.add("Settler Bubbles settings can only be opened while playing.");
            }
            return;
        } else if ("debug".equalsIgnoreCase(state)) {
            for (String line : BubbleDiagnostics.buildReport(client)) {
                commandLog.add(line);
            }
            return;
        } else if ("smoke".equalsIgnoreCase(state)) {
            commandLog.add(BubbleSmokeTest.toggle(client));
            return;
        } else if ("on".equalsIgnoreCase(state)) {
            SettlerBubblesSettings.enabled = true;
        } else if ("off".equalsIgnoreCase(state)) {
            SettlerBubblesSettings.enabled = false;
        } else {
            SettlerBubblesSettings.enabled = !SettlerBubblesSettings.enabled;
        }
        commandLog.add("Settler bubbles "
                + (SettlerBubblesSettings.enabled ? "enabled" : "disabled") + ".");
    }
}
