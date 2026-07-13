package com.antartida.ventisca.model;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class BlockHolderTest {

    // NOTE: org.bukkit.block.Biome is a registry-backed type that cannot be
    // instantiated or mocked outside a running server, so these tests exercise
    // the holders with a null biome (a valid, supported value in the code).

    @Test
    void modifiedBlockExposesConstructorArguments() {
        Location location = mock(Location.class);
        ModifiedBlock block = new ModifiedBlock(location, Material.STONE, null);
        assertSame(location, block.getLocation());
        assertSame(Material.STONE, block.getOriginalMaterial());
        assertNull(block.getOriginalBiome());
    }

    @Test
    void scannedBlockExposesConstructorArguments() {
        Location location = mock(Location.class);
        ScannedBlock block = new ScannedBlock(location, Material.GRASS_BLOCK, null, BlockCategory.SURFACE);
        assertSame(location, block.getLocation());
        assertSame(Material.GRASS_BLOCK, block.getOriginalMaterial());
        assertNull(block.getOriginalBiome());
        assertSame(BlockCategory.SURFACE, block.getCategory());
    }
}
