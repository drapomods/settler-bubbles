package drapomods.settlerbubbles.client;

import necesse.engine.input.Control;
import necesse.engine.input.InputID;
import necesse.engine.localization.message.LocalMessage;

public final class BubbleControls {
    public static Control openSettings;

    private BubbleControls() {
    }

    public static void register() {
        if (openSettings == null) {
            openSettings = Control.addModControl(new Control(
                    InputID.KEY_UNKNOWN,
                    "opensettlerbubblessettings",
                    new LocalMessage("settlerbubbles", "control_settings")
            ));
        }
    }
}
