package com.antartida.ventisca.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antartida.ventisca.model.ModifiedBlock;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

class WallManagerTest {

    private final WallManager manager = new WallManager(mock(JavaPlugin.class));

    private static ModifiedBlock modifiedBlock(Block block, Material original) {
        Location location = mock(Location.class);
        when(location.getBlock()).thenReturn(block);
        return new ModifiedBlock(location, original, null);
    }

    private static List<ModifiedBlock> blocks(List<Block> sink, int count) {
        List<ModifiedBlock> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Block block = mock(Block.class);
            sink.add(block);
            list.add(modifiedBlock(block, Material.STONE));
        }
        return list;
    }

    @Test
    void defaultRollbackBatchSize() {
        assertEquals(35, manager.getDefaultRollbackBatchSize());
    }

    @Test
    void rollbackBatchRestoresUpToLimitAndReportsRemaining() {
        List<Block> sink = new ArrayList<>();
        List<ModifiedBlock> log = blocks(sink, 10);

        int remaining = manager.rollbackBatch(log, 4);

        assertEquals(6, remaining);
        assertEquals(6, log.size());
        // The four most recently added (tail) blocks are the ones restored.
        for (int i = 6; i < 10; i++) {
            verify(sink.get(i)).setType(Material.STONE, false);
        }
    }

    @Test
    void rollbackBatchNeverProcessesMoreThanAvailable() {
        List<Block> sink = new ArrayList<>();
        List<ModifiedBlock> log = blocks(sink, 3);

        int remaining = manager.rollbackBatch(log, 100);

        assertEquals(0, remaining);
        assertTrue(log.isEmpty());
        for (Block block : sink) {
            verify(block, times(1)).setType(Material.STONE, false);
        }
    }

    @Test
    void rollbackBatchOnEmptyLogIsANoOp() {
        assertEquals(0, manager.rollbackBatch(new ArrayList<>(), 5));
    }
}
