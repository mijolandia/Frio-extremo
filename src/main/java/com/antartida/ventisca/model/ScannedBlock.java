package com.antartida.ventisca.model;

import com.antartida.ventisca.model.BlockCategory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;

public final class ScannedBlock {
    private final Location location;
    private final Material originalMaterial;
    private final Biome originalBiome;
    private final BlockCategory category;

    public ScannedBlock(Location location, Material originalMaterial, Biome originalBiome, BlockCategory category) {
        this.location = location;
        this.originalMaterial = originalMaterial;
        this.originalBiome = originalBiome;
        this.category = category;
    }

    public Location getLocation() {
        return this.location;
    }

    public Material getOriginalMaterial() {
        return this.originalMaterial;
    }

    public Biome getOriginalBiome() {
        return this.originalBiome;
    }

    public BlockCategory getCategory() {
        return this.category;
    }
}

