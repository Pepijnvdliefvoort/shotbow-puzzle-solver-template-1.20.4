package shotbow.puzzle.solver.StRoseluckCrypt;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import shotbow.puzzle.solver.IPuzzleSolver;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import java.util.HashMap;
import java.util.Map;

public class BossRoomPuzzleSolver implements IPuzzleSolver {
    public BlockPos quartzWallStart = new BlockPos(12, 47, 62);
    public BlockPos buttonWallStart = new BlockPos(12, 46, 84);
    public BlockPos triggerButtonPos = new BlockPos(18, 48, 79);
    public BlockPos clearButtonPos = new BlockPos(11, 48, 83);

    private boolean monitoringActive = false;
    private final Map<BlockPos, BlockState> replacedBlocks = new HashMap<>();

    @Override
    public String getName() {
        return "BossRoom";
    }

    @Override
    public void activate(MinecraftClient client) {
        monitoringActive = false;
    }

    @Override
    public void deactivate(MinecraftClient client) {
        clearPreviousBlocks(client);
        monitoringActive = false;
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null)
            return;

        // --- Hand Press Monitoring Start ---
        HitResult hit = client.crosshairTarget;
        if (hit instanceof BlockHitResult blockHit) {
            BlockPos hitPos = blockHit.getBlockPos();
            if (client.options.useKey.isPressed()) {
                if (hitPos.equals(triggerButtonPos)) {
                    clearPreviousBlocks(client);
                    monitoringActive = true;
                }
                if (hitPos.equals(clearButtonPos)) {
                    clearPreviousBlocks(client);
                    monitoringActive = false;
                }
            }
        }

        // --- Projectile hit for "clear" button ---
        client.world.getEntities().forEach(entity -> {
            String key = entity.getType().getTranslationKey();
            if (key.contains("arrow") || key.contains("projectile")) {
                BlockPos arrowPos = entity.getBlockPos();
                if (arrowPos.equals(clearButtonPos)) {
                    clearPreviousBlocks(client);
                    monitoringActive = false;
                }
            }
        });

        // --- Continuous Monitoring ---
        if (monitoringActive) {
            monitorAndReflectQuartzPattern(client);
        }
    }

    private void monitorAndReflectQuartzPattern(MinecraftClient client) {
        ClientWorld world = client.world;

        int quartzStartX = quartzWallStart.getX();
        int quartzStartY = quartzWallStart.getY();
        int quartzZ = quartzWallStart.getZ();

        int buttonStartX = buttonWallStart.getX();
        int buttonStartY = buttonWallStart.getY();
        int buttonZ = buttonWallStart.getZ();

        for (int dx = 0; dx < 5; dx++) {
            for (int dy = 0; dy < 5; dy++) {
                BlockPos quartzPos = new BlockPos(quartzStartX + dx, quartzStartY + dy, quartzZ);
                BlockPos buttonPos = new BlockPos(buttonStartX + dx, buttonStartY + dy, buttonZ);

                BlockState targetState;
                if (world.getBlockState(quartzPos).isOf(Blocks.REDSTONE_BLOCK)) {
                    targetState = Blocks.LIME_CONCRETE.getDefaultState();
                } else {
                    targetState = Blocks.CHISELED_STONE_BRICKS.getDefaultState();
                }

                // Save original block if not already saved
                if (!replacedBlocks.containsKey(buttonPos)) {
                    replacedBlocks.put(buttonPos, world.getBlockState(buttonPos));
                }

                // Only update if needed
                if (!world.getBlockState(buttonPos).isOf(targetState.getBlock())) {
                    world.setBlockState(buttonPos, targetState);
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
