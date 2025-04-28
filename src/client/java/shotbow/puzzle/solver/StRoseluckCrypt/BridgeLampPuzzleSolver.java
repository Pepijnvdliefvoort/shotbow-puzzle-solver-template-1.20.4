package shotbow.puzzle.solver.StRoseluckCrypt;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import shotbow.puzzle.solver.IPuzzleSolver;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.HashMap;
import java.util.Map;

public class BridgeLampPuzzleSolver implements IPuzzleSolver {
    private static final int EFFECT_RADIUS = 32; // only affect blocks within 32 blocks of player

    // Lamp line
    private final int lampX = 59;
    private final int lampY = 48;

    // Bridge
    private final int bridgeStartX = 39;
    private final int bridgeEndX = 110;
    private final int bridgeY = 40;
    private final int edgeNorthZ = -62;
    private final int edgeSouthZ = -54;
    private final int slabZMin = -61;
    private final int slabZMax = -55;

    // Buttons
    private final BlockPos triggerButtonPos = new BlockPos(30, 41, -55);
    private final BlockPos clearButtonPos = new BlockPos(137, 22, -58);

    private boolean monitoringActive = false;
    private final Map<BlockPos, BlockState> originalBlocks = new HashMap<>();

    @Override
    public String getName() {
        return "BridgeLampPuzzle";
    }

    @Override
    public void activate(MinecraftClient client) {
        sendDebugMessage(client, "activated.");
        monitoringActive = false;
        restoreAll(client);
    }

    @Override
    public void deactivate(MinecraftClient client) {
        sendDebugMessage(client, "deactivated.");
        monitoringActive = false;
        restoreAll(client);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) {
            return;
        }

        BlockPos playerPos = client.player.getBlockPos();

        HitResult hit = client.crosshairTarget;
        if (hit instanceof BlockHitResult blockHit) {
            BlockPos hitPos = blockHit.getBlockPos();
            if (client.options.useKey.isPressed()) {
                if (hitPos.equals(triggerButtonPos)) {
                    restoreAll(client);
                    monitoringActive = true;
                }
                if (hitPos.equals(clearButtonPos)) {
                    restoreAll(client);
                    monitoringActive = false;
                }
            }
        }

        if (!monitoringActive) {
            return;
        }

        ClientWorld world = client.world;

        for (int edgeZ : new int[] { edgeNorthZ, edgeSouthZ }) {
            BlockPos lampPos = new BlockPos(lampX, lampY, edgeZ);
            BlockState lampState = world.getBlockState(lampPos);
            boolean isLit = lampState.isOf(Blocks.REDSTONE_LAMP)
                    && lampState.contains(Properties.LIT)
                    && lampState.get(Properties.LIT);

            for (int x = bridgeStartX; x <= bridgeEndX; x++) {
                BlockPos pos = new BlockPos(x, bridgeY, edgeZ);
                if (!isNearPlayer(pos, playerPos, EFFECT_RADIUS))
                    continue;

                BlockState current = world.getBlockState(pos);
                if (!originalBlocks.containsKey(pos)) {
                    originalBlocks.put(pos, current);
                }

                if (isLit) {
                    if (!current.isOf(Blocks.OXIDIZED_CUT_COPPER)) {
                        world.setBlockState(pos, Blocks.OXIDIZED_CUT_COPPER.getDefaultState());
                    }
                } else {
                    BlockState orig = originalBlocks.get(pos);
                    if (orig != null && !current.equals(orig)) {
                        world.setBlockState(pos, orig);
                    }
                }
            }
        }

        // Process slab rows
        for (int z = slabZMin; z <= slabZMax; z++) {
            BlockPos lampPos = new BlockPos(lampX, lampY, z);
            BlockState lampState = world.getBlockState(lampPos);
            boolean isLit = lampState.isOf(Blocks.REDSTONE_LAMP)
                    && lampState.contains(Properties.LIT)
                    && lampState.get(Properties.LIT);

            for (int x = bridgeStartX; x <= bridgeEndX; x++) {
                BlockPos pos = new BlockPos(x, bridgeY, z);
                if (!isNearPlayer(pos, playerPos, EFFECT_RADIUS))
                    continue;

                BlockState current = world.getBlockState(pos);
                if (!originalBlocks.containsKey(pos)) {
                    originalBlocks.put(pos, current);
                }

                if (isLit) {
                    if (!current.isOf(Blocks.OXIDIZED_CUT_COPPER_SLAB)) {
                        world.setBlockState(pos, Blocks.OXIDIZED_CUT_COPPER_SLAB.getDefaultState());
                    }
                } else {
                    BlockState orig = originalBlocks.get(pos);
                    if (orig != null && !current.equals(orig)) {
                        world.setBlockState(pos, orig);
                    }
                }
            }
        }
    }

    public void restoreAll(MinecraftClient client) {
        ClientWorld world = client.world;
        for (Map.Entry<BlockPos, BlockState> entry : originalBlocks.entrySet()) {
            world.setBlockState(entry.getKey(), entry.getValue());
        }
        originalBlocks.clear();
    }

    private boolean isNearPlayer(BlockPos pos, BlockPos playerPos, int radius) {
        return pos.getManhattanDistance(playerPos) <= radius;
    }
}
