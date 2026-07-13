package com.antartida.ventisca.manager;

import com.antartida.ventisca.session.StormSession;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class MobManager {
    private static final EntityType[] EARLY_TYPES = new EntityType[]{EntityType.ZOMBIE, EntityType.HUSK};
    private static final EntityType[] LATE_TYPES = new EntityType[]{EntityType.ZOMBIE, EntityType.HUSK, EntityType.SKELETON, EntityType.STRAY};
    private final JavaPlugin plugin;
    private final Random random = new Random();

    public MobManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public int getMaxMobsForPhase(int phase) {
        return switch (phase) {
            case 1 -> 5;
            case 2 -> 15;
            case 3 -> 25;
            default -> 40;
        };
    }

    public void maintainPopulation(Player player, StormSession session, int phase) {
        session.getSpawnedMobs().removeIf(uuid -> Bukkit.getEntity((UUID)uuid) == null);
        int cap = this.getMaxMobsForPhase(phase);
        int missing = cap - session.getSpawnedMobs().size();
        if (missing <= 0) {
            return;
        }
        int toSpawn = Math.min(missing, 4);
        for (int i = 0; i < toSpawn; ++i) {
            this.spawnHostile(player, session, phase);
        }
    }

    private void spawnHostile(Player player, StormSession session, int phase) {
        EntityType[] pool;
        EntityType type;
        World world = player.getWorld();
        int dx = this.random.nextInt(21) - 10;
        int dz = this.random.nextInt(21) - 10;
        int x = player.getLocation().getBlockX() + dx;
        int z = player.getLocation().getBlockZ() + dz;
        int groundY = world.getHighestBlockYAt(x, z);
        int y = phase >= 4 ? groundY + 1 : Math.max(groundY + 1, player.getLocation().getBlockY());
        Location spawnLocation = new Location(world, (double)x + 0.5, (double)y, (double)z + 0.5);
        Entity entity = world.spawnEntity(spawnLocation, type = (pool = phase >= 3 ? LATE_TYPES : EARLY_TYPES)[this.random.nextInt(pool.length)]);
        if (!(entity instanceof LivingEntity)) {
            entity.remove();
            return;
        }
        LivingEntity livingEntity = (LivingEntity)entity;
        if (phase >= 4) {
            livingEntity.setGlowing(true);
            AttributeInstance maxHealthAttribute = livingEntity.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealthAttribute != null) {
                double doubled = maxHealthAttribute.getBaseValue() * 2.0;
                maxHealthAttribute.setBaseValue(doubled);
                livingEntity.setHealth(doubled);
            }
        }
        session.getSpawnedMobs().add(entity.getUniqueId());
    }

    public void removeAllTrackedMobs(StormSession session) {
        for (UUID uuid : session.getSpawnedMobs()) {
            Entity entity = Bukkit.getEntity((UUID)uuid);
            if (entity == null) continue;
            entity.remove();
        }
        session.getSpawnedMobs().clear();
    }
}

