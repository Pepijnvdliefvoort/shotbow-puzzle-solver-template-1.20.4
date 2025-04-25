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
    // Lamp line
    private final int lampX = 56;
    private final int lampY = 48;
    private final int lampZMin = -62;
    private final int lampZMax = -54;

    // Bridge
    private final int bridgeStartX = 39;
    private final int bridgeEndX = 110; // 72 blocks: 39..110 inclusive
    private final int bridgeY = 40;
    private final int edgeNorthZ = -62;
    private final int edgeSouthZ = -54;
    private final int slabZMin = -61;
    private final int slabZMax = -55;

    // Buttons
    private final BlockPos triggerButtonPos = new BlockPos(31, 41, -55);
    private final BlockPos clearButtonPos = new BlockPos(137, 22, -55);

    private boolean monitoringActive = false;
    private final Map<BlockPos, BlockState> originalBlocks = new HashMap<>();

    @Override
    public String getName() {
        return "BridgeLampPuzzle";
    }

    @Override
    public void activate(MinecraftClient client) {
        monitoringActive = false;
        restoreAll(client);
    }

    @Override
    public void deactivate(MinecraftClient client) {
        monitoringActive = false;
        restoreAll(client);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) return;

        // Check for trigger/clear press
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

        if (!monitoringActive) return;

        ClientWorld world = client.world;

        // --- NORTH EDGE (Z = -62) and SOUTH EDGE (Z = -54) ---
        for (int edgeZ : new int[] {edgeNorthZ, edgeSouthZ}) {
            // Check corresponding lamp
            BlockPos lampPos = new BlockPos(lampX, lampY, edgeZ);
            BlockState lampState = world.getBlockState(lampPos);
            boolean isLit = lampState.isOf(Blocks.REDSTONE_LAMP)
                    && lampState.contains(Properties.LIT)
                    && lampState.get(Properties.LIT);

            for (int x = bridgeStartX; x <= bridgeEndX; x++) {
                BlockPos pos = new BlockPos(x, bridgeY, edgeZ);
                BlockState current = world.getBlockState(pos);

                // Save for restoration
                if (!originalBlocks.containsKey(pos)) {
                    originalBlocks.put(pos, current);
                }

                // Only replace if current is cobblestone, copper, or red nether bricks
                if (current.isOf(Blocks.COBBLESTONE)
                 || current.isOf(Blocks.OXIDIZED_CUT_COPPER)
                 || current.isOf(Blocks.RED_NETHER_BRICKS)) {
                    BlockState newState = isLit ? Blocks.OXIDIZED_CUT_COPPER.getDefaultState()
                                                : Blocks.RED_NETHER_BRICKS.getDefaultState();
                    if (!current.isOf(newState.getBlock())) {
                        world.setBlockState(pos, newState);
                    }
                }
            }
        }

        // --- MIDDLE (SLABS, Z = -61 to -55) ---
        for (int z = slabZMin; z <= slabZMax; z++) {
            // Check corresponding lamp
            BlockPos lampPos = new BlockPos(lampX, lampY, z);
            BlockState lampState = world.getBlockState(lampPos);
            boolean isLit = lampState.isOf(Blocks.REDSTONE_LAMP)
                    && lampState.contains(Properties.LIT)
                    && lampState.get(Properties.LIT);

            for (int x = bridgeStartX; x <= bridgeEndX; x++) {
                BlockPos pos = new BlockPos(x, bridgeY, z);
                BlockState current = world.getBlockState(pos);

                if (!originalBlocks.containsKey(pos)) {
                    originalBlocks.put(pos, current);
                }

                // Only replace slabs of certain types
                if (current.isOf(Blocks.NETHER_BRICK_SLAB)
                 || current.isOf(Blocks.STONE_BRICK_SLAB)
                 || current.isOf(Blocks.RED_NETHER_BRICK_SLAB)
                 || current.isOf(Blocks.OXIDIZED_CUT_COPPER_SLAB)) {
                    BlockState newState = isLit ? Blocks.OXIDIZED_CUT_COPPER_SLAB.getDefaultState()
                                                : Blocks.RED_NETHER_BRICK_SLAB.getDefaultState();
                    if (!current.isOf(newState.getBlock())) {
                        world.setBlockState(pos, newState);
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
}
