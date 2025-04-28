package shotbow.puzzle.solver;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class PuzzleSolverCommands {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("puzzlesolver")
                            .then(ClientCommandManager.literal("group")
                                    .then(ClientCommandManager.literal("use")
                                            .then(ClientCommandManager.argument("group", StringArgumentType.word())
                                                    .suggests((ctx, builder) -> {
                                                        for (String name : ShotbowpuzzlesolverClient.groups.keySet())
                                                            builder.suggest(name);
                                                        return builder.buildFuture();
                                                    })
                                                    .executes(ctx -> {
                                                         String groupName = StringArgumentType.getString(ctx, "group")
                                                                .toLowerCase();
                                                        PuzzleGroup selectedGroup = ShotbowpuzzlesolverClient.groups
                                                                .get(groupName);
                                                        if (selectedGroup != null) {
                                                            if (ShotbowpuzzlesolverClient.activeGroup != null)
                                                                ShotbowpuzzlesolverClient.activeGroup
                                                                        .deactivate(MinecraftClient.getInstance());
                                                            ShotbowpuzzlesolverClient.activeGroup = selectedGroup;
                                                            ShotbowpuzzlesolverClient.activeGroup
                                                                    .activate(MinecraftClient.getInstance());
                                                            ctx.getSource().sendFeedback(Text.literal(
                                                                    "[PuzzleSolver] Activated group: " + groupName));
                                                        } else {
                                                            ctx.getSource().sendFeedback(Text.literal(
                                                                    "[PuzzleSolver] Group not found: " + groupName));
                                                        }
                                                        return 1;
                                                    })))
                                    .then(ClientCommandManager.literal("disable")
                                            .executes(ctx -> {
                                                if (ShotbowpuzzlesolverClient.activeGroup != null) {
                                                    ShotbowpuzzlesolverClient.activeGroup
                                                            .deactivate(MinecraftClient.getInstance());
                                                    String name = ShotbowpuzzlesolverClient.activeGroup.getName();
                                                    ShotbowpuzzlesolverClient.activeGroup = null;
                                                    ctx.getSource().sendFeedback(
                                                            Text.literal("[PuzzleSolver] Disabled group: " + name));
                                                } else {
                                                    ctx.getSource().sendFeedback(Text
                                                            .literal("[PuzzleSolver] No group is currently active."));
                                                }
                                                return 1;
                                            }))
                                    .then(ClientCommandManager.literal("list")
                                            .executes(ctx -> {
                                                ctx.getSource()
                                                        .sendFeedback(Text.literal("[PuzzleSolver] Available groups:"));
                                                for (String name : ShotbowpuzzlesolverClient.groups.keySet()) {
                                                    boolean isActive = ShotbowpuzzlesolverClient.activeGroup != null
                                                            && ShotbowpuzzlesolverClient.activeGroup.getName()
                                                                    .equalsIgnoreCase(name);
                                                    String line = (isActive ? " * " : "   ") + name;
                                                    ctx.getSource().sendFeedback(Text.literal(line));
                                                }
                                                return 1;
                                            }))));
        });
    }
}
