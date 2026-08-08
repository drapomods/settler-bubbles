package drapomods.settlerbubbles.command;

import drapomods.settlerbubbles.SettlerBubblesSettings;
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
        super("bubbles", "Toggle settler speech bubbles for this client.",
                PermissionLevel.USER, false,
                new CmdParameter("state",
                        new PresetStringParameterHandler(true, "on", "off", "toggle"), true));
    }

    @Override
    public void runModular(Client client, Server server, ServerClient serverClient,
                           Object[] args, String[] errors, CommandLog commandLog) {
        String state = args[0] instanceof String ? (String)args[0] : "toggle";
        if ("on".equalsIgnoreCase(state)) {
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
