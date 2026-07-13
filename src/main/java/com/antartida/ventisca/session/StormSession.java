package com.antartida.ventisca.session;

import com.antartida.ventisca.model.EventStatus;
import com.antartida.ventisca.model.ModifiedBlock;
import com.antartida.ventisca.model.ScannedBlock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public final class StormSession {
    public static final int TOTAL_TICKS = 2400;
    public static final int CAGE_RADIUS = 32;
    public static final int CEILING_OFFSET = 30;
    public static final int FLOOR_OFFSET = -10;
    private final UUID playerId;
    private EventStatus status = EventStatus.BUILDING;
    private int ticksRemaining = 2400;
    private boolean scanCompleted = false;
    private boolean cageReady = false;
    private boolean deathHandled = false;
    private int lastAnnouncedPhase = 0;
    private Location cageAnchor;
    private ItemStack originalHelmet;
    private boolean helmetSwapped = false;
    private final List<ScannedBlock> surfaceBlocks = new ArrayList<ScannedBlock>();
    private final List<ScannedBlock> deepBlocks = new ArrayList<ScannedBlock>();
    private final List<ModifiedBlock> wallBlocks = new ArrayList<ModifiedBlock>();
    private final List<ModifiedBlock> terrainBlocks = new ArrayList<ModifiedBlock>();
    private final Set<UUID> spawnedMobs = new HashSet<UUID>();
    private BukkitTask fastTask;
    private BukkitTask slowTask;

    public StormSession(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public EventStatus getStatus() {
        return this.status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public int getTicksRemaining() {
        return this.ticksRemaining;
    }

    public void setTicksRemaining(int ticksRemaining) {
        this.ticksRemaining = ticksRemaining;
    }

    public int getSecondsRemaining() {
        return Math.max(0, this.ticksRemaining / 20);
    }

    public int getSecondsElapsed() {
        return (2400 - Math.max(0, this.ticksRemaining)) / 20;
    }

    public boolean isScanCompleted() {
        return this.scanCompleted;
    }

    public void setScanCompleted(boolean scanCompleted) {
        this.scanCompleted = scanCompleted;
    }

    public boolean isCageReady() {
        return this.cageReady;
    }

    public void setCageReady(boolean cageReady) {
        this.cageReady = cageReady;
    }

    public boolean isDeathHandled() {
        return this.deathHandled;
    }

    public void setDeathHandled(boolean deathHandled) {
        this.deathHandled = deathHandled;
    }

    public int getLastAnnouncedPhase() {
        return this.lastAnnouncedPhase;
    }

    public void setLastAnnouncedPhase(int lastAnnouncedPhase) {
        this.lastAnnouncedPhase = lastAnnouncedPhase;
    }

    public Location getCageAnchor() {
        return this.cageAnchor;
    }

    public void setCageAnchor(Location cageAnchor) {
        this.cageAnchor = cageAnchor;
    }

    public ItemStack getOriginalHelmet() {
        return this.originalHelmet;
    }

    public void setOriginalHelmet(ItemStack originalHelmet) {
        this.originalHelmet = originalHelmet;
    }

    public boolean isHelmetSwapped() {
        return this.helmetSwapped;
    }

    public void setHelmetSwapped(boolean helmetSwapped) {
        this.helmetSwapped = helmetSwapped;
    }

    public List<ScannedBlock> getSurfaceBlocks() {
        return this.surfaceBlocks;
    }

    public List<ScannedBlock> getDeepBlocks() {
        return this.deepBlocks;
    }

    public List<ModifiedBlock> getWallBlocks() {
        return this.wallBlocks;
    }

    public List<ModifiedBlock> getTerrainBlocks() {
        return this.terrainBlocks;
    }

    public Set<UUID> getSpawnedMobs() {
        return this.spawnedMobs;
    }

    public BukkitTask getFastTask() {
        return this.fastTask;
    }

    public void setFastTask(BukkitTask fastTask) {
        this.fastTask = fastTask;
    }

    public BukkitTask getSlowTask() {
        return this.slowTask;
    }

    public void setSlowTask(BukkitTask slowTask) {
        this.slowTask = slowTask;
    }

    public int getPhase() {
        int seconds = this.getSecondsRemaining();
        if (seconds > 90) {
            return 1;
        }
        if (seconds > 60) {
            return 2;
        }
        if (seconds > 30) {
            return 3;
        }
        return 4;
    }

    public void cancelTasks() {
        if (this.fastTask != null) {
            this.fastTask.cancel();
            this.fastTask = null;
        }
        if (this.slowTask != null) {
            this.slowTask.cancel();
            this.slowTask = null;
        }
    }
}

