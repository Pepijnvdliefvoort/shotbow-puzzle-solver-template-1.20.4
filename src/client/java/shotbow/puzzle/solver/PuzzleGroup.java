package shotbow.puzzle.solver;

import net.minecraft.client.MinecraftClient;
import java.util.List;

public class PuzzleGroup {
    private final String name;
    private final List<IPuzzleSolver> solvers;

    public PuzzleGroup(String name, List<IPuzzleSolver> solvers) {
        this.name = name;
        this.solvers = solvers;
    }

    public String getName() {
        return name;
    }

    public List<IPuzzleSolver> getSolvers() {
        return solvers;
    }

    public void activate(MinecraftClient client) {
        for (IPuzzleSolver solver : solvers) {
            solver.activate(client);
        }
    }

    public void deactivate(MinecraftClient client) {
        for (IPuzzleSolver solver : solvers) {
            solver.deactivate(client);
        }
    }

    public void onTick(MinecraftClient client) {
        for (IPuzzleSolver solver : solvers) {
            solver.onTick(client);
        }
    }
}
