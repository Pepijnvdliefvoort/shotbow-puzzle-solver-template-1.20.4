package shotbow.puzzle.solver.com.example;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import java.util.HashMap;
import java.util.Map;

public class ShotbowpuzzlesolverClient implements ClientModInitializer {
	// Default positions, can be changed by commands
	public BlockPos quartzWallStart = new BlockPos(12, 47, 62);
	public BlockPos buttonWallStart = new BlockPos(12, 46, 84);
	public BlockPos triggerButtonPos = new BlockPos(18, 48, 79);
	public BlockPos clearButtonPos = new BlockPos(11, 48, 83);

	private int pendingScanTicks = 0;
	private boolean pendingScanFromKey = false;

	// To track original blocks for restoration
	private final Map<BlockPos, BlockState> replacedBlocks = new HashMap<>();

	@Override
	public void onInitializeClient() {
		// Register client-only commands properly for Fabric v2
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
					ClientCommandManager.literal("puzzlesolver")
							.then(ClientCommandManager.literal("set")
									.then(ClientCommandManager.literal("quartz_start")
											.then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
													.then(ClientCommandManager
															.argument("y", IntegerArgumentType.integer())
															.then(ClientCommandManager
																	.argument("z", IntegerArgumentType.integer())
																	.executes(ctx -> setPos(ctx, "quartz_start"))))))
									.then(ClientCommandManager.literal("button_start")
											.then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
													.then(ClientCommandManager
															.argument("y", IntegerArgumentType.integer())
															.then(ClientCommandManager
																	.argument("z", IntegerArgumentType.integer())
																	.executes(ctx -> setPos(ctx, "button_start"))))))
									.then(ClientCommandManager.literal("trigger")
											.then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
													.then(ClientCommandManager
															.argument("y", IntegerArgumentType.integer())
															.then(ClientCommandManager
																	.argument("z", IntegerArgumentType.integer())
																	.executes(ctx -> setPos(ctx, "trigger"))))))
									.then(ClientCommandManager.literal("clear")
											.then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
													.then(ClientCommandManager
															.argument("y", IntegerArgumentType.integer())
															.then(ClientCommandManager
																	.argument("z", IntegerArgumentType.integer())
																	.executes(ctx -> setPos(ctx, "clear")))))))
							.then(ClientCommandManager.literal("show")
									.executes(ctx -> showAllPositions(ctx))));
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client == null || client.player == null || client.world == null) return;
		
			// Check for pending delayed scan
			if (pendingScanTicks > 0) {
				pendingScanTicks--;
				if (pendingScanTicks == 0) {
					scanAndReplaceWithObsidian(client);
					pendingScanFromKey = false;
				}
			}
		
			// In-world button logic
			HitResult hit = client.crosshairTarget;
			if (hit instanceof BlockHitResult blockHit) {
				BlockPos hitPos = blockHit.getBlockPos();
				if (client.options.useKey.isPressed()) {
					if (hitPos.equals(triggerButtonPos)) {
						clearPreviousBlocks(client);
						pendingScanTicks = 10; // 0.5 second delay
						pendingScanFromKey = false;
					}
				}
			}
		
			// Projectile clear logic (arrow hitting clear button block)
			client.world.getEntities().forEach(entity -> {
				if (entity.getType().getTranslationKey().contains("arrow") || entity.getType().getTranslationKey().contains("projectile")) {
					BlockPos arrowPos = entity.getBlockPos();
					if (arrowPos.equals(clearButtonPos)) {
						clearPreviousBlocks(client);
					}
				}
			});
		});
	}

	private int setPos(CommandContext<FabricClientCommandSource> ctx, String type) {
		int x = IntegerArgumentType.getInteger(ctx, "x");
		int y = IntegerArgumentType.getInteger(ctx, "y");
		int z = IntegerArgumentType.getInteger(ctx, "z");
		BlockPos pos = new BlockPos(x, y, z);

		switch (type) {
			case "quartz_start":
				this.quartzWallStart = pos;
				ctx.getSource().sendFeedback(Text.literal("[PuzzleSolver] Set quartz wall start to " + pos));
				break;
			case "button_start":
				this.buttonWallStart = pos;
				ctx.getSource().sendFeedback(Text.literal("[PuzzleSolver] Set button wall start to " + pos));
				break;
			case "trigger":
				this.triggerButtonPos = pos;
				ctx.getSource().sendFeedback(Text.literal("[PuzzleSolver] Set trigger button to " + pos));
				break;
			case "clear":
				this.clearButtonPos = pos;
				ctx.getSource().sendFeedback(Text.literal("[PuzzleSolver] Set clear button to " + pos));
				break;
		}
		return 1;
	}

	private int showAllPositions(CommandContext<FabricClientCommandSource> ctx) {
		ctx.getSource().sendFeedback(Text.literal("[PuzzleSolver] Current settings:"));
		ctx.getSource().sendFeedback(Text.literal(" - quartz_start: " + this.quartzWallStart));
		ctx.getSource().sendFeedback(Text.literal(" - button_start: " + this.buttonWallStart));
		ctx.getSource().sendFeedback(Text.literal(" - trigger: " + this.triggerButtonPos));
		ctx.getSource().sendFeedback(Text.literal(" - clear: " + this.clearButtonPos));
		return 1;
	}

	private void scanAndReplaceWithObsidian(MinecraftClient client) {
		ClientWorld world = client.world;

		// Get base positions from fields (settable by command!)
		int quartzStartX = quartzWallStart.getX();
		int quartzStartY = quartzWallStart.getY();
		int quartzZ = quartzWallStart.getZ();

		int buttonStartX = buttonWallStart.getX();
		int buttonStartY = buttonWallStart.getY();
		int buttonZ = buttonWallStart.getZ();

		for (int dx = 0; dx < 5; dx++) {
			for (int dy = 0; dy < 5; dy++) {
				BlockPos quartzPos = new BlockPos(quartzStartX + dx, quartzStartY + dy, quartzZ);
				if (world.getBlockState(quartzPos).isOf(Blocks.REDSTONE_BLOCK)) {
					BlockPos buttonPos = new BlockPos(buttonStartX + dx, buttonStartY + dy, buttonZ);

					BlockState original = world.getBlockState(buttonPos);
					replacedBlocks.put(buttonPos, original);
					world.setBlockState(buttonPos, Blocks.OBSIDIAN.getDefaultState());

				}
			}
		}

	}

	private void clearPreviousBlocks(MinecraftClient client) {
		ClientWorld world = client.world;

		for (Map.Entry<BlockPos, BlockState> entry : replacedBlocks.entrySet()) {
			world.setBlockState(entry.getKey(), entry.getValue());
		}
		replacedBlocks.clear();
	}
}
