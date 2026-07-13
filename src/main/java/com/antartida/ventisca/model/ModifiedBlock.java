package com.antartida.ventisca.model;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;

public final class ModifiedBlock {
    private final Location location;
    private final Material originalMaterial;
    private final Biome originalBiome;

    public ModifiedBlock(Location location, Material originalMaterial, Biome originalBiome) {
        this.location = location;
        this.originalMaterial = originalMaterial;
        this.originalBiome = originalBiome;
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
}

