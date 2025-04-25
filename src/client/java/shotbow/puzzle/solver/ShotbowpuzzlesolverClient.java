package shotbow.puzzle.solver;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import shotbow.puzzle.solver.StRoseluckCrypt.BossRoomPuzzleSolver;
import shotbow.puzzle.solver.StRoseluckCrypt.BridgeLampPuzzleSolver;

import java.util.HashMap;
import java.util.Map;

import com.mojang.brigadier.arguments.StringArgumentType;

public class ShotbowpuzzlesolverClient implements ClientModInitializer {

    private final Map<String, IPuzzleSolver> solvers = new HashMap<>();
    private IPuzzleSolver activeSolver;

    @Override
    public void onInitializeClient() {
        // Register solvers
        IPuzzleSolver crypt = new BossRoomPuzzleSolver();
        IPuzzleSolver bridge = new BridgeLampPuzzleSolver();

        solvers.put(crypt.getName().toLowerCase(), crypt);
        solvers.put(bridge.getName().toLowerCase(), bridge);

        activeSolver = crypt; // default

        // Command to switch active solver
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("puzzlesolver")
                            .then(ClientCommandManager.literal("use")
                                    .then(ClientCommandManager.argument("solver", StringArgumentType.word())
                                            .suggests((ctx, builder) -> {
                                                for (String name : solvers.keySet())
                                                    builder.suggest(name);
                                                return builder.buildFuture();
                                            })
                                            .executes(ctx -> {
                                                String solverName = StringArgumentType.getString(ctx, "solver")
                                                        .toLowerCase();
                                                IPuzzleSolver selected = solvers.get(solverName);
                                                if (selected != null) {
                                                    if (activeSolver != null)
                                                        activeSolver.deactivate(MinecraftClient.getInstance());
                                                    activeSolver = selected;
                                                    activeSolver.activate(MinecraftClient.getInstance());
                                                    ctx.getSource().sendFeedback(
                                                            Text.literal("[PuzzleSolver] Using solver: " + solverName));
                                                } else {
                                                    ctx.getSource().sendFeedback(Text
                                                            .literal("[PuzzleSolver] Solver not found: " + solverName));
                                                }
                                                return 1;
                                            })))
                            .then(ClientCommandManager.literal("list")
                                    .executes(ctx -> {
                                        ctx.getSource().sendFeedback(Text.literal("[PuzzleSolver] Available solvers:"));
                                        for (String name : solvers.keySet()) {
                                            boolean isActive = activeSolver != null
                                                    && activeSolver.getName().equalsIgnoreCase(name);
                                            String line = (isActive ? " * " : "   ") + name;
                                            ctx.getSource().sendFeedback(Text.literal(line));
                                        }
                                        return 1;
                                    })));
        });

        // Forward tick event to active solver
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (activeSolver != null) {
                activeSolver.onTick(client);
            }
        });
    }
}
