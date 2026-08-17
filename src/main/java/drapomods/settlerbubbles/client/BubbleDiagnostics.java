package drapomods.settlerbubbles.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import drapomods.settlerbubbles.SettlerBubblesMod;
import drapomods.settlerbubbles.SettlerBubblesSettings;
import drapomods.settlerbubbles.dialogue.DialogueManager;
import necesse.engine.localization.Language;
import necesse.engine.localization.Localization;
import necesse.engine.modLoader.LoadedMod;
import necesse.engine.modLoader.ModLoader;
import necesse.engine.network.client.Client;

public final class BubbleDiagnostics {
    private BubbleDiagnostics() {
    }

    public static List<String> buildReport(Client client) {
        List<String> report = new ArrayList<>();
        Language gameLanguage = Localization.getCurrentLang();
        String gameLanguageID = gameLanguage == null ? "unknown" : gameLanguage.stringID;
        String connection = client == null ? "no client"
                : client.isSingleplayer() ? "singleplayer"
                : client.getLocalServer() != null ? "hosted multiplayer" : "remote multiplayer";

        report.add("--- Settler Bubbles diagnostics ---");
        report.add("Mod=" + SettlerBubblesMod.VERSION
                + ", targetNecesse=" + SettlerBubblesMod.GAME_VERSION
                + ", mode=" + connection);
        report.add("Enabled=" + SettlerBubblesSettings.enabled
                + ", frequency=" + SettlerBubblesSettings.frequency
                + ", duration=" + Math.round(SettlerBubblesSettings.durationScale * 100.0F) + "%"
                + ", distance=" + SettlerBubblesSettings.maxDistance);
        report.add("Font=" + SettlerBubblesSettings.fontMode + "/"
                + SettlerBubblesSettings.fontSize + "px, density="
                + SettlerBubblesSettings.maxVisibleBubbles
                + ", active=" + BubbleClientManager.getActiveCount(
                        client == null ? null : client.getLevel()));
        report.add("Languages: game=" + gameLanguageID
                + ", bubbles=" + SettlerBubblesSettings.language.name());
        report.add("Categories: social=" + SettlerBubblesSettings.social
                + ", work=" + SettlerBubblesSettings.work
                + ", needs=" + SettlerBubblesSettings.needs
                + ", mood=" + SettlerBubblesSettings.mood
                + ", combat=" + SettlerBubblesSettings.combat);
        if (client != null && !client.isSingleplayer() && client.getLocalServer() == null) {
            report.add("Session memory: server-side counters are unavailable on a remote client");
        } else {
            report.add("Session memory: " + DialogueManager.getDebugSummary());
        }

        List<LoadedMod> enabledMods = ModLoader.getEnabledMods();
        report.add("Enabled mods=" + enabledMods.size());
        List<String> possibleChatterMods = enabledMods.stream()
                .filter(BubbleDiagnostics::mayRenderChatter)
                .map(mod -> mod.name + " (" + mod.id + " " + mod.version + ")")
                .collect(Collectors.toList());
        if (possibleChatterMods.isEmpty()) {
            report.add("Other possible chatter mods: none detected");
        } else {
            report.add("Other possible chatter mods: " + String.join(", ", possibleChatterMods));
        }
        report.add("Use /bubbles smoke for the visual test sequence.");
        return report;
    }

    private static boolean mayRenderChatter(LoadedMod mod) {
        if (mod == null || SettlerBubblesMod.MOD_ID.equals(mod.id)) {
            return false;
        }
        String text = (mod.id + " " + mod.name).toLowerCase(Locale.ROOT);
        return text.contains("bubble") || text.contains("chatter")
                || text.contains("chat") || text.contains("talk")
                || text.contains("lively") || text.contains("living settlement")
                || text.contains("safe haven");
    }
}
