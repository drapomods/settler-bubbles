package drapomods.settlerbubbles.patch;

import drapomods.settlerbubbles.client.BubbleSmokeTest;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.client.Client;
import net.bytebuddy.asm.Advice;

public class ClientTickPatches {
    @ModMethodPatch(target = Client.class, name = "tick", arguments = {})
    public static class SmokeTestTickPatch {
        @Advice.OnMethodExit
        public static void exit(@Advice.This Client client) {
            BubbleSmokeTest.tick(client);
        }
    }
}
