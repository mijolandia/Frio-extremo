package com.antartida.ventisca.manager;

import com.antartida.ventisca.model.PlayerStats;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class StatsManager {
    private static final double POINTS_PER_SECOND = 3.3333333333333335;
    public static final long VICTORY_BONUS = 850L;
    private final JavaPlugin plugin;
    private final File file;
    private final Map<UUID, PlayerStats> statsByPlayer = new HashMap<UUID, PlayerStats>();

    public StatsManager(JavaPlugin plugin) {
        this.plugin = plugin;
        File dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("No se pudo crear la carpeta data/ para las estadisticas.");
        }
        this.file = new File(dataFolder, "stats.yml");
    }

    public static long computeProportionalPoints(int secondsElapsed) {
        return Math.round((double)secondsElapsed * 3.3333333333333335);
    }

    public synchronized void load() {
        this.statsByPlayer.clear();
        if (!this.file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration((File)this.file);
        ConfigurationSection section = yaml.getConfigurationSection("players");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String path = "players." + key + ".";
                PlayerStats stats = new PlayerStats(uuid, yaml.getString(path + "name", "Desconocido"));
                stats.addPoints(yaml.getLong(path + "totalPoints", 0L));
                stats.restore(yaml.getInt(path + "currentStreak", 0), yaml.getInt(path + "maxStreak", 0), yaml.getInt(path + "stormsWon", 0), yaml.getInt(path + "stormsFailed", 0));
                this.statsByPlayer.put(uuid, stats);
            }
            catch (IllegalArgumentException ex) {
                this.plugin.getLogger().warning("UUID invalido en stats.yml: " + key);
            }
        }
    }

    public synchronized PlayerStats getOrCreate(Player player) {
        return this.statsByPlayer.computeIfAbsent(player.getUniqueId(), id -> new PlayerStats((UUID)id, player.getName()));
    }

    public synchronized void recordPartialFailure(Player player, int secondsElapsed) {
        PlayerStats stats = this.getOrCreate(player);
        stats.setLastKnownName(player.getName());
        stats.addPoints(StatsManager.computeProportionalPoints(secondsElapsed));
        stats.registerFailure();
        this.saveAsync();
    }

    public synchronized void recordVictory(Player player) {
        PlayerStats stats = this.getOrCreate(player);
        stats.setLastKnownName(player.getName());
        stats.addPoints(StatsManager.computeProportionalPoints(120) + 850L);
        stats.registerWin();
        this.saveAsync();
    }

    public synchronized List<PlayerStats> getTopThree() {
        ArrayList<PlayerStats> all = new ArrayList<PlayerStats>(this.statsByPlayer.values());
        all.sort(Comparator.comparingLong(PlayerStats::getTotalPoints).reversed());
        return all.subList(0, Math.min(3, all.size()));
    }

    private void saveAsync() {
        HashMap<UUID, PlayerStats> snapshot = new HashMap<UUID, PlayerStats>(this.statsByPlayer);
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> this.saveSnapshot(snapshot));
    }

    public synchronized void saveSync() {
        this.saveSnapshot(new HashMap<UUID, PlayerStats>(this.statsByPlayer));
    }

    private void saveSnapshot(Map<UUID, PlayerStats> snapshot) {
        YamlConfiguration yaml = new YamlConfiguration();
        for (PlayerStats stats : snapshot.values()) {
            String path = "players." + String.valueOf(stats.getPlayerId()) + ".";
            yaml.set(path + "name", (Object)stats.getLastKnownName());
            yaml.set(path + "totalPoints", (Object)stats.getTotalPoints());
            yaml.set(path + "maxStreak", (Object)stats.getMaxStreak());
            yaml.set(path + "currentStreak", (Object)stats.getCurrentStreak());
            yaml.set(path + "stormsWon", (Object)stats.getStormsWon());
            yaml.set(path + "stormsFailed", (Object)stats.getStormsFailed());
        }
        try {
            yaml.save(this.file);
        }
        catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "No se pudo guardar data/stats.yml", ex);
        }
    }
}

