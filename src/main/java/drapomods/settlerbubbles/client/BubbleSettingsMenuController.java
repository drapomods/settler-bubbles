package drapomods.settlerbubbles.client;

import drapomods.settlerbubbles.SettlerBubblesSettings;
import necesse.engine.GlobalData;
import necesse.engine.Settings;
import necesse.engine.input.InputEvent;
import necesse.engine.network.client.Client;
import necesse.engine.state.MainGame;
import necesse.engine.state.State;
import necesse.engine.window.GameWindow;

public final class BubbleSettingsMenuController {
    private static BubbleSettingsForm currentForm;
    private static int lastHudWidth;
    private static int lastHudHeight;

    private BubbleSettingsMenuController() {
    }

    public static void frameTick(MainGame game, GameWindow window) {
        if (game == null || window == null || game.formManager == null) {
            return;
        }

        if (SettlerBubblesSettings.fontSize == 0) {
            SettlerBubblesSettings.ensureAutoFontSize(window);
            Settings.saveClientSettings();
        }

        if (currentForm != null && currentForm.isDisposed()) {
            currentForm = null;
        }
        if (currentForm != null
                && (lastHudWidth != window.getHudWidth() || lastHudHeight != window.getHudHeight())) {
            center(window);
        }

        if (BubbleControls.openSettings != null && BubbleControls.openSettings.isPressed()) {
            InputEvent event = BubbleControls.openSettings.getEvent();
            if (event != null) {
                event.use();
            }
            toggle(game, window);
        }
    }

    public static boolean open(Client client) {
        State state = GlobalData.getCurrentState();
        if (!(state instanceof MainGame)) {
            return false;
        }
        MainGame game = (MainGame)state;
        if (client != null && game.getClient() != client) {
            return false;
        }
        GameWindow window = necesse.engine.window.WindowManager.getWindow();
        if (window == null || game.formManager == null) {
            return false;
        }
        show(game, window);
        return true;
    }

    private static void toggle(MainGame game, GameWindow window) {
        if (currentForm != null && !currentForm.isDisposed()) {
            close(game);
        } else {
            show(game, window);
        }
    }

    private static void show(MainGame game, GameWindow window) {
        if (currentForm != null && !currentForm.isDisposed()) {
            currentForm.tryPutOnTop();
            return;
        }
        Client client = game.getClient();
        if (client == null || client.getPlayer() == null) {
            return;
        }
        SettlerBubblesSettings.ensureAutoFontSize(window);
        currentForm = new BubbleSettingsForm(client, window, () -> close(game));
        center(window);
        game.formManager.addComponent(currentForm);
        currentForm.tryPutOnTop();
    }

    private static void close(MainGame game) {
        if (currentForm == null) {
            return;
        }
        if (game != null && game.formManager != null) {
            game.formManager.removeComponent(currentForm);
        }
        currentForm.dispose();
        currentForm = null;
    }

    private static void center(GameWindow window) {
        if (currentForm != null) {
            currentForm.setPosMiddle(window.getHudWidth() / 2, window.getHudHeight() / 2);
            lastHudWidth = window.getHudWidth();
            lastHudHeight = window.getHudHeight();
        }
    }
}
