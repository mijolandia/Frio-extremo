package com.antartida.ventisca.manager;

import com.antartida.ventisca.model.ModifiedBlock;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class WallManager {
    private static final int BLOCKS_PER_TICK_BUILD = 200;
    private static final int BLOCKS_PER_TICK_ROLLBACK = 35;
    private static final Material WALL_MATERIAL = Material.PACKED_ICE;
    private static final Material CEILING_MATERIAL = Material.BLUE_ICE;
    private static final Material FLOOR_MATERIAL = Material.ICE;
    private final JavaPlugin plugin;

    public WallManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void buildCage(Location anchor, List<ModifiedBlock> outputLog, Runnable onComplete) {
        BukkitTask[] taskHolder = new BukkitTask[1];
        World world = anchor.getWorld();
        int centerX = anchor.getBlockX();
        int centerY = anchor.getBlockY();
        int centerZ = anchor.getBlockZ();
        int radius = 32;
        int floorY = centerY + -10;
        int ceilY = centerY + 30;
        List<int[]> plan = this.buildPlan(centerX, centerZ, radius, floorY, ceilY);
        int[] cursor = new int[]{0};
        taskHolder[0] = this.plugin.getServer().getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            for (int processed = 0; processed < 200 && cursor[0] < plan.size(); ++processed) {
                int[] coords = (int[])plan.get(cursor[0]);
                this.placeBlock(world, coords[0], coords[1], coords[2], this.materialFor(coords[3]), outputLog);
                cursor[0] = cursor[0] + 1;
            }
            if (cursor[0] >= plan.size()) {
                taskHolder[0].cancel();
                onComplete.run();
            }
        }, 0L, 1L);
    }

    private Material materialFor(int layerType) {
        return switch (layerType) {
            case 0 -> WALL_MATERIAL;
            case 1 -> CEILING_MATERIAL;
            default -> FLOOR_MATERIAL;
        };
    }

    private List<int[]> buildPlan(int centerX, int centerZ, int radius, int floorY, int ceilY) {
        int z;
        int x;
        ArrayList<int[]> plan = new ArrayList<int[]>();
        for (x = -radius; x <= radius; ++x) {
            for (z = -radius; z <= radius; ++z) {
                boolean isPerimeter;
                boolean bl = isPerimeter = x == -radius || x == radius || z == -radius || z == radius;
                if (!isPerimeter) continue;
                int y = floorY;
                while (y <= ceilY) {
                    plan.add(new int[]{centerX + x, y++, centerZ + z, 0});
                }
            }
        }
        for (x = -radius; x <= radius; ++x) {
            for (z = -radius; z <= radius; ++z) {
                plan.add(new int[]{centerX + x, ceilY, centerZ + z, 1});
                plan.add(new int[]{centerX + x, floorY, centerZ + z, 2});
            }
        }
        return plan;
    }

    private void placeBlock(World world, int x, int y, int z, Material material, List<ModifiedBlock> outputLog) {
        Block block = world.getBlockAt(x, y, z);
        if (block.getType() == material) {
            return;
        }
        outputLog.add(new ModifiedBlock(block.getLocation(), block.getType(), null));
        block.setType(material, false);
    }

    public int rollbackBatch(List<ModifiedBlock> blocks, int maxBlocks) {
        int limit = Math.min(maxBlocks, blocks.size());
        for (int i = 0; i < limit; ++i) {
            ModifiedBlock modified = blocks.remove(blocks.size() - 1);
            Block block = modified.getLocation().getBlock();
            block.setType(modified.getOriginalMaterial(), false);
        }
        return blocks.size();
    }

    public int getDefaultRollbackBatchSize() {
        return 35;
    }
}

