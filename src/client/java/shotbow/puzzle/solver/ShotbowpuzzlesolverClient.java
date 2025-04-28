package shotbow.puzzle.solver;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import shotbow.puzzle.solver.StRoseluckCrypt.BossRoomPuzzleSolver;
import shotbow.puzzle.solver.StRoseluckCrypt.BridgeLampPuzzleSolver;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShotbowpuzzlesolverClient implements ClientModInitializer {

    public static final Map<String, IPuzzleSolver> solvers = new HashMap<>();
    public static final Map<String, PuzzleGroup> groups = new HashMap<>();
    public static PuzzleGroup activeGroup = null;

    @Override
    public void onInitializeClient() {
        // Register solvers
        IPuzzleSolver crypt = new BossRoomPuzzleSolver();
        IPuzzleSolver bridge = new BridgeLampPuzzleSolver();

        solvers.put(crypt.getName().toLowerCase(), crypt);
        solvers.put(bridge.getName().toLowerCase(), bridge);

        // Group: St Roseluck Crypt
        PuzzleGroup stRoseluckGroup = new PuzzleGroup(
                "stroseluck",
                List.of(crypt, bridge));
        groups.put(stRoseluckGroup.getName().toLowerCase(), stRoseluckGroup);

        // Register commands in a separate file
        PuzzleSolverCommands.register();

        // Forward tick event to active group
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (activeGroup != null) {
            activeGroup.onTick(client);
            }
        });

    }
}
