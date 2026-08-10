package drapomods.settlerbubbles.patch;

import drapomods.settlerbubbles.client.BubbleSettingsMenuController;
import net.bytebuddy.asm.Advice;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.state.MainGame;
import necesse.engine.window.GameWindow;

@ModMethodPatch(target = MainGame.class, name = "frameTick",
        arguments = {TickManager.class, GameWindow.class})
public class MainGameInputPatch {
    @Advice.OnMethodExit(suppress = Throwable.class)
    public static void exit(@Advice.This MainGame game,
                            @Advice.Argument(1) GameWindow window) {
        BubbleSettingsMenuController.frameTick(game, window);
    }
}
