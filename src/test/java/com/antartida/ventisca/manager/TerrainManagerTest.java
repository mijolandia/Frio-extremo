package com.antartida.ventisca.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antartida.ventisca.model.ModifiedBlock;
import com.antartida.ventisca.session.StormSession;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

class TerrainManagerTest {

    private final TerrainManager manager = new TerrainManager(mock(JavaPlugin.class));

    private static ModifiedBlock terrainBlock(Block block, World world) {
        Location location = mock(Location.class);
        when(location.getBlock()).thenReturn(block);
        when(location.getWorld()).thenReturn(world);
        return new ModifiedBlock(location, Material.STONE, null);
    }

    @Test
    void rollbackBatchRestoresMaterialAndReportsRemaining() {
        StormSession session = new StormSession(UUID.randomUUID());
        Block first = mock(Block.class);
        Block second = mock(Block.class);
        Block third = mock(Block.class);
        session.getTerrainBlocks().add(terrainBlock(first, null));
        session.getTerrainBlocks().add(terrainBlock(second, null));
        session.getTerrainBlocks().add(terrainBlock(third, null));

        int remaining = manager.rollbackBatch(session, 2);

        assertEquals(1, remaining);
        verify(third).setType(Material.STONE, false);
        verify(second).setType(Material.STONE, false);
        verify(first, never()).setType(Material.STONE, false);
    }

    // NOTE: org.bukkit.block.Biome is a registry-backed type that cannot be
    // instantiated or mocked outside a running server, so the biome-restoring
    // branch is only exercised for its "no tracked biome" path below.

    @Test
    void rollbackBatchSkipsBiomeWhenNotTracked() {
        StormSession session = new StormSession(UUID.randomUUID());
        Block block = mock(Block.class);
        World world = mock(World.class);
        session.getTerrainBlocks().add(terrainBlock(block, world));

        manager.rollbackBatch(session, 10);

        verify(world, never()).setBiome(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rollbackBatchDrainsEntireLogWhenBudgetIsLarge() {
        StormSession session = new StormSession(UUID.randomUUID());
        for (int i = 0; i < 5; i++) {
            session.getTerrainBlocks().add(terrainBlock(mock(Block.class), null));
        }

        int remaining = manager.rollbackBatch(session, 100);

        assertEquals(0, remaining);
        assertTrue(session.getTerrainBlocks().isEmpty());
    }
}
