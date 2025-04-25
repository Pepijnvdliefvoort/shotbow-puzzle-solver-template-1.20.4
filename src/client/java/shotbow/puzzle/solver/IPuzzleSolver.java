package shotbow.puzzle.solver;

import net.minecraft.client.MinecraftClient;

public interface IPuzzleSolver {
    String getName();

    void activate(MinecraftClient client);

    void deactivate(MinecraftClient client);

    void onTick(MinecraftClient client);
}