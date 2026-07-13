package com.antartida.ventisca.manager;

import com.antartida.ventisca.model.BlockCategory;
import com.antartida.ventisca.model.EventStatus;
import com.antartida.ventisca.model.ModifiedBlock;
import com.antartida.ventisca.model.ScannedBlock;
import com.antartida.ventisca.session.StormSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class TerrainManager {
    private static final int SCAN_RADIUS = 40;
    private static final int SCAN_BATCH_PER_TICK = 1500;
    private static final Set<Material> DEEP_CANDIDATES = Set.of(Material.STONE, Material.DIRT, Material.GRASS_BLOCK, Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.BIRCH_LEAVES, Material.JUNGLE_LEAVES, Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES, Material.WATER);
    private final JavaPlugin plugin;
    private final Random random = new Random();

    public TerrainManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void scanArea(Player player, StormSession session, Runnable onComplete) {
        World world = player.getWorld();
        int centerX = player.getLocation().getBlockX();
        int centerZ = player.getLocation().getBlockZ();
        ArrayList<int[]> columns = new ArrayList<int[]>();
        for (int dx = -40; dx <= 40; ++dx) {
            for (int dz = -40; dz <= 40; ++dz) {
                if (dx * dx + dz * dz > 1600) continue;
                columns.add(new int[]{centerX + dx, centerZ + dz});
            }
        }
        int[] index = new int[]{0};
        this.plugin.getServer().getScheduler().runTaskTimer((Plugin)this.plugin, task -> {
            if (session.getStatus() != EventStatus.ACTIVE && session.getStatus() != EventStatus.BUILDING) {
                task.cancel();
                return;
            }
            for (int processed = 0; index[0] < columns.size() && processed < 1500; ++processed) {
                int[] col = (int[])columns.get(index[0]);
                this.scanColumn(world, col[0], col[1], session);
                index[0] = index[0] + 1;
            }
            if (index[0] >= columns.size()) {
                task.cancel();
                session.setScanCompleted(true);
                onComplete.run();
            }
        }, 1L, 1L);
    }

    private void scanColumn(World world, int x, int z, StormSession session) {
        int highestY = world.getHighestBlockYAt(x, z);
        Block surfaceBlock = world.getBlockAt(x, highestY, z);
        if (surfaceBlock.getType().isSolid()) {
            Location loc = surfaceBlock.getLocation();
            Biome biome = world.getBiome(loc);
            session.getSurfaceBlocks().add(new ScannedBlock(loc, surfaceBlock.getType(), biome, BlockCategory.SURFACE));
        }
        for (int offset = 1; offset <= 4; ++offset) {
            Block deepBlock = world.getBlockAt(x, highestY - offset, z);
            Material type = deepBlock.getType();
            if (!DEEP_CANDIDATES.contains(type)) continue;
            Location loc = deepBlock.getLocation();
            Biome biome = world.getBiome(loc);
            session.getDeepBlocks().add(new ScannedBlock(loc, type, biome, BlockCategory.DEEP));
        }
    }

    public void degradeSurfaceTick(StormSession session, int samplesPerTick) {
        List<ScannedBlock> surface = session.getSurfaceBlocks();
        if (surface.isEmpty()) {
            return;
        }
        for (int i = 0; i < samplesPerTick; ++i) {
            ScannedBlock sample = surface.get(this.random.nextInt(surface.size()));
            Location above = sample.getLocation().clone().add(0.0, 1.0, 0.0);
            Block block = above.getBlock();
            if (block.getType() != Material.AIR) continue;
            session.getTerrainBlocks().add(new ModifiedBlock(above, Material.AIR, null));
            block.setType(Material.SNOW, false);
        }
    }

    public void glaciateCriticalTick(StormSession session, int biomeSamplesPerTick, int deepSamplesPerTick) {
        List<ScannedBlock> surface = session.getSurfaceBlocks();
        for (int i = 0; i < biomeSamplesPerTick && !surface.isEmpty(); ++i) {
            ScannedBlock sample = surface.get(this.random.nextInt(surface.size()));
            Location loc = sample.getLocation();
            World world = loc.getWorld();
            if (world == null || world.getBiome(loc) == Biome.SNOWY_PLAINS) continue;
            world.setBiome(loc, Biome.SNOWY_PLAINS);
        }
        List<ScannedBlock> deep = session.getDeepBlocks();
        for (int i = 0; i < deepSamplesPerTick && !deep.isEmpty(); ++i) {
            Material target;
            ScannedBlock sample = deep.get(this.random.nextInt(deep.size()));
            Block block = sample.getLocation().getBlock();
            Material current = block.getType();
            if (current != sample.getOriginalMaterial() || (target = this.resolveDeepTarget(sample.getOriginalMaterial())) == null || current == target) continue;
            World world = sample.getLocation().getWorld();
            Biome originalBiome = world != null ? world.getBiome(sample.getLocation()) : null;
            session.getTerrainBlocks().add(new ModifiedBlock(sample.getLocation(), current, originalBiome));
            block.setType(target, false);
            if (world == null) continue;
            world.setBiome(sample.getLocation(), Biome.SNOWY_PLAINS);
        }
    }

    private Material resolveDeepTarget(Material original) {
        String name = original.name();
        if (name.equals("WATER") || name.endsWith("_LEAVES")) {
            return Material.BLUE_ICE;
        }
        if (original == Material.DIRT || original == Material.STONE || original == Material.GRASS_BLOCK) {
            return Material.SNOW_BLOCK;
        }
        return null;
    }

    public int rollbackBatch(StormSession session, int maxPerTick) {
        List<ModifiedBlock> modified = session.getTerrainBlocks();
        for (int processed = 0; !modified.isEmpty() && processed < maxPerTick; ++processed) {
            World world;
            ModifiedBlock entry = modified.remove(modified.size() - 1);
            Block block = entry.getLocation().getBlock();
            block.setType(entry.getOriginalMaterial(), false);
            if (entry.getOriginalBiome() == null || (world = entry.getLocation().getWorld()) == null) continue;
            world.setBiome(entry.getLocation(), entry.getOriginalBiome());
        }
        return modified.size();
    }
}

