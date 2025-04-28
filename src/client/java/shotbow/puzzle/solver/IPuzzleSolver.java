package shotbow.puzzle.solver;

import net.minecraft.client.MinecraftClient;

public interface IPuzzleSolver {
    String getName();

    void activate(MinecraftClient client);

    void deactivate(MinecraftClient client);

    void onTick(MinecraftClient client);

    default void sendDebugMessage(MinecraftClient client, String message) {
        if (client != null && client.player != null) {
            client.player.sendMessage(net.minecraft.text.Text.literal("[" + getName() + "] " + message), false);
        }
    }
}