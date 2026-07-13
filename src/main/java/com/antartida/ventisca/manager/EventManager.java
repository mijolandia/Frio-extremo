package com.antartida.ventisca.manager;

import com.antartida.ventisca.manager.MobManager;
import com.antartida.ventisca.manager.StatsManager;
import com.antartida.ventisca.manager.TerrainManager;
import com.antartida.ventisca.manager.WallManager;
import com.antartida.ventisca.model.EventStatus;
import com.antartida.ventisca.model.ModifiedBlock;
import com.antartida.ventisca.session.StormSession;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public final class EventManager {
    private static final double SPAWN_EXCLUSION_RADIUS = 80.0;
    private static final int BASE_COOLDOWN_TICKS = 25;
    private static final int COOLDOWN_PER_PLAYER_TICKS = 5;
    private static final int MAX_COOLDOWN_TICKS = 180;
    private final JavaPlugin plugin;
    private final WallManager wallManager;
    private final TerrainManager terrainManager;
    private final MobManager mobManager;
    private final StatsManager statsManager;
    private final Deque<UUID> queue = new ArrayDeque<UUID>();
    private final Map<UUID, StormSession> activeSessions = new HashMap<UUID, StormSession>();
    private boolean globalEventActive = false;
    private int cooldownTicksConfigured = 25;
    private int cooldownCounter = 0;
    private BukkitTask queueTask;
    private boolean debugEnabled = false;

    public EventManager(JavaPlugin plugin, WallManager wallManager, TerrainManager terrainManager, MobManager mobManager, StatsManager statsManager) {
        this.plugin = plugin;
        this.wallManager = wallManager;
        this.terrainManager = terrainManager;
        this.mobManager = mobManager;
        this.statsManager = statsManager;
    }

    public boolean isGlobalEventActive() {
        return this.globalEventActive;
    }

    public boolean hasSession(UUID playerId) {
        return this.activeSessions.containsKey(playerId);
    }

    public boolean isCommandLocked(UUID playerId) {
        StormSession session = this.activeSessions.get(playerId);
        return session != null && session.getStatus() != EventStatus.ENDING;
    }

    public boolean isDebugEnabled() {
        return this.debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    private void debug(String message) {
        if (this.debugEnabled) {
            this.plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    public void startGlobalSequence(CommandSender initiator) {
        if (this.globalEventActive) {
            initiator.sendMessage((Component)Component.text((String)"Ya hay una secuencia global en curso.", (TextColor)NamedTextColor.RED));
            return;
        }
        World world = (World)Bukkit.getWorlds().get(0);
        Location spawn = world.getSpawnLocation();
        this.queue.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean sameWorld = player.getWorld().equals((Object)world);
            double d = sameWorld ? player.getLocation().distance(spawn) : Double.MAX_VALUE;
            double distance = d;
            if (!(distance > 80.0)) continue;
            this.queue.add(player.getUniqueId());
        }
        if (this.queue.isEmpty()) {
            initiator.sendMessage((Component)Component.text((String)"No hay jugadores elegibles (a mas de 80 bloques del spawn).", (TextColor)NamedTextColor.YELLOW));
            return;
        }
        int totalQueued = this.queue.size();
        this.cooldownTicksConfigured = Math.min(180, 25 + totalQueued * 5);
        this.cooldownCounter = 0;
        this.globalEventActive = true;
        initiator.sendMessage((Component)Component.text((String)("Glaciacion Global iniciada. " + totalQueued + " jugador(es) en cola. Cooldown: " + this.cooldownTicksConfigured + " ticks."), (TextColor)NamedTextColor.AQUA));
        this.queueTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, this::tickQueue, 0L, 1L);
    }

    public void abortGlobalSequence(CommandSender initiator) {
        if (!this.globalEventActive && this.activeSessions.isEmpty()) {
            initiator.sendMessage((Component)Component.text((String)"No hay ninguna glaciacion activa para abortar.", (TextColor)NamedTextColor.RED));
            return;
        }
        this.queue.clear();
        this.globalEventActive = false;
        if (this.queueTask != null) {
            this.queueTask.cancel();
            this.queueTask = null;
        }
        ArrayList<StormSession> snapshot = new ArrayList<StormSession>(this.activeSessions.values());
        for (StormSession session : snapshot) {
            this.endStorm(session, Outcome.ABORTED);
        }
        initiator.sendMessage((Component)Component.text((String)"Glaciacion Total abortada. Restaurando ecosistema...", (TextColor)NamedTextColor.GREEN));
    }

    private void tickQueue() {
        this.updateQueueActionBars();
        if (this.cooldownCounter > 0) {
            --this.cooldownCounter;
        }
        if (this.cooldownCounter <= 0 && !this.queue.isEmpty()) {
            UUID nextId = this.queue.poll();
            Player player = Bukkit.getPlayer((UUID)nextId);
            if (player != null && player.isOnline()) {
                this.startStormForPlayer(player);
            }
            this.cooldownCounter = this.cooldownTicksConfigured;
        }
        if (this.queue.isEmpty() && this.activeSessions.isEmpty()) {
            this.globalEventActive = false;
            if (this.queueTask != null) {
                this.queueTask.cancel();
                this.queueTask = null;
            }
        }
    }

    private void updateQueueActionBars() {
        int position = 1;
        for (UUID id : this.queue) {
            Player queuedPlayer = Bukkit.getPlayer((UUID)id);
            if (queuedPlayer != null && queuedPlayer.isOnline()) {
                queuedPlayer.sendActionBar((Component)Component.text((String)("Posicion en cola: " + position + " | Preparate para el frio extremo!"), (TextColor)NamedTextColor.AQUA));
            }
            ++position;
        }
    }

    private void startStormForPlayer(Player player) {
        StormSession session = new StormSession(player.getUniqueId());
        session.setStatus(EventStatus.BUILDING);
        session.setCageAnchor(player.getLocation().clone());
        this.activeSessions.put(player.getUniqueId(), session);
        player.sendActionBar((Component)Component.text((String)"TU TURNO LLEGO! Sellando el cubo de hielo...", (TextColor)NamedTextColor.RED));
        this.prepareBuilding(player, session);
        this.debug("Iniciando construccion de jaula para " + player.getName());
        this.wallManager.buildCage(session.getCageAnchor(), session.getWallBlocks(), () -> {
            if (!player.isOnline() || session.getStatus() != EventStatus.BUILDING) {
                return;
            }
            session.setCageReady(true);
            this.unfreezeAndStartPhases(player, session);
        });
        this.terrainManager.scanArea(player, session, () -> this.debug("Pre-scan de terreno completado para " + player.getName()));
    }

    private void prepareBuilding(Player player, StormSession session) {
        ItemStack currentHelmet = player.getInventory().getHelmet();
        session.setOriginalHelmet(currentHelmet != null ? currentHelmet.clone() : null);
        ItemStack frozenHead = new ItemStack(Material.BLUE_ICE);
        ItemMeta meta = frozenHead.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("\u00a7b\u00a7lCABEZA CONGELADA");
            frozenHead.setItemMeta(meta);
        }
        player.getInventory().setHelmet(frozenHead);
        session.setHelmetSwapped(true);
        player.setVelocity(new Vector(0, 0, 0));
    }

    private void unfreezeAndStartPhases(Player player, StormSession session) {
        session.setStatus(EventStatus.ACTIVE);
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 1.5f, 0.6f);
        player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, SoundCategory.AMBIENT, 1.0f, 0.7f);
        player.showTitle(Title.title((Component)Component.text((String)"LA JAULA ESTA SELLADA", (TextColor)NamedTextColor.DARK_AQUA), (Component)Component.text((String)"No hay escape posible...", (TextColor)NamedTextColor.AQUA), (Title.Times)Title.Times.times((Duration)Duration.ofMillis(500L), (Duration)Duration.ofSeconds(2L), (Duration)Duration.ofMillis(500L))));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 2400, 5, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 2400, 2, false, false, true));
        session.setFastTask(Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> this.fastTick(player, session), 5L, 5L));
        session.setSlowTask(Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> this.slowTick(player, session), 20L, 20L));
        this.debug("Jaula lista, Fase 1 iniciada para " + player.getName());
    }

    private void fastTick(Player player, StormSession session) {
        if (!player.isOnline()) {
            this.endStorm(session, Outcome.ABORTED);
            return;
        }
        int phase = session.getPhase();
        player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0.0, 1.0, 0.0), phase * 6, 0.6, 0.8, 0.6, 0.01);
        if (phase == 2 || phase == 3) {
            this.terrainManager.degradeSurfaceTick(session, 20 + phase * 10);
        }
        if (phase == 3 || phase == 4) {
            this.terrainManager.glaciateCriticalTick(session, 10, 6);
        }
        long tickCount = 2400 - session.getTicksRemaining();
        int heartbeatInterval = phase == 4 ? 2 : phase >= 2 ? 4 : 10;
        if (tickCount % (long)heartbeatInterval == 0L) {
            if (phase == 4) {
                player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, SoundCategory.HOSTILE, 1.0f, 1.0f);
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, SoundCategory.AMBIENT, 1.0f, 0.6f);
            }
        }
        if (phase == 1 && tickCount % 30L == 0L) {
            player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, SoundCategory.HOSTILE, 0.8f, 0.4f);
        }
    }

    private void slowTick(Player player, StormSession session) {
        if (!player.isOnline()) {
            this.endStorm(session, Outcome.ABORTED);
            return;
        }
        session.setTicksRemaining(session.getTicksRemaining() - 20);
        int secondsRemaining = session.getSecondsRemaining();
        int phase = session.getPhase();
        if (phase != session.getLastAnnouncedPhase()) {
            this.announcePhaseTransition(player, session, phase);
        }
        this.mobManager.maintainPopulation(player, session, phase);
        player.sendActionBar((Component)Component.text((String)("FASE " + phase + " | Tiempo restante: " + secondsRemaining + "s"), (TextColor)this.phaseColor(phase)));
        if (session.getTicksRemaining() <= 0) {
            this.endStorm(session, Outcome.VICTORY);
        }
    }

    private void announcePhaseTransition(Player player, StormSession session, int phase) {
        session.setLastAnnouncedPhase(phase);
        this.debug(player.getName() + " avanzo a la Fase " + phase);
        switch (phase) {
            case 2: {
                player.showTitle(Title.title((Component)Component.text((String)"TORMENTA FIRME", (TextColor)NamedTextColor.BLUE), (Component)Component.text((String)"El frio se intensifica...", (TextColor)NamedTextColor.AQUA), (Title.Times)Title.Times.times((Duration)Duration.ofMillis(300L), (Duration)Duration.ofSeconds(2L), (Duration)Duration.ofMillis(300L))));
                break;
            }
            case 3: {
                player.showTitle(Title.title((Component)Component.text((String)"GLACIACION AVANZADA", (TextColor)NamedTextColor.DARK_BLUE), (Component)Component.text((String)"El suelo se congela bajo tus pies...", (TextColor)NamedTextColor.BLUE), (Title.Times)Title.Times.times((Duration)Duration.ofMillis(300L), (Duration)Duration.ofSeconds(2L), (Duration)Duration.ofMillis(300L))));
                break;
            }
            case 4: {
                player.showTitle(Title.title((Component)Component.text((String)"PANICO ARTICO", (TextColor)NamedTextColor.DARK_PURPLE), (Component)Component.text((String)"Sobrevive los ultimos 30 segundos...", (TextColor)NamedTextColor.RED), (Title.Times)Title.Times.times((Duration)Duration.ofMillis(300L), (Duration)Duration.ofSeconds(2L), (Duration)Duration.ofMillis(300L))));
                player.playSound(player.getLocation(), Sound.MUSIC_DISC_11, SoundCategory.RECORDS, 1.0f, 1.0f);
                break;
            }
        }
    }

    private NamedTextColor phaseColor(int phase) {
        return switch (phase) {
            case 1 -> NamedTextColor.AQUA;
            case 2 -> NamedTextColor.BLUE;
            case 3 -> NamedTextColor.DARK_BLUE;
            default -> NamedTextColor.DARK_PURPLE;
        };
    }

    public void handlePlayerDeath(Player player, StormSession session) {
        if (session.isDeathHandled()) {
            return;
        }
        session.setDeathHandled(true);
        int secondsElapsed = session.getStatus() == EventStatus.ACTIVE ? session.getSecondsElapsed() : 0;
        this.statsManager.recordPartialFailure(player, secondsElapsed);
        this.endStorm(session, Outcome.DEATH);
    }

    public void endStorm(StormSession session, Outcome outcome) {
        if (session.getStatus() == EventStatus.ENDING) {
            return;
        }
        session.setStatus(EventStatus.ENDING);
        session.cancelTasks();
        Player player = Bukkit.getPlayer((UUID)session.getPlayerId());
        if (outcome == Outcome.VICTORY && player != null) {
            this.statsManager.recordVictory(player);
            long total = StatsManager.computeProportionalPoints(120) + 850L;
            Bukkit.broadcast((Component)Component.text((String)("HAZANA LEGENDARIA! " + player.getName() + " sobrevivio la Ventisca Eterna completa (+" + total + " puntos)!"), (TextColor)NamedTextColor.GOLD));
        } else if (outcome == Outcome.ABORTED && player != null && !session.isDeathHandled()) {
            int secondsElapsed = session.getStatus() == EventStatus.ENDING ? session.getSecondsElapsed() : 0;
            this.statsManager.recordPartialFailure(player, secondsElapsed);
        }
        if (player != null && player.isOnline()) {
            player.removePotionEffect(PotionEffectType.SLOWNESS);
            player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
            if (session.isHelmetSwapped()) {
                player.getInventory().setHelmet(session.getOriginalHelmet());
            }
            player.showTitle(Title.title((Component)Component.text((String)"DESHIELO", (TextColor)NamedTextColor.WHITE), (Component)Component.text((String)"Restaurando ecosistema...", (TextColor)NamedTextColor.GRAY), (Title.Times)Title.Times.times((Duration)Duration.ofMillis(500L), (Duration)Duration.ofSeconds(2L), (Duration)Duration.ofMillis(500L))));
        }
        this.mobManager.removeAllTrackedMobs(session);
        this.activeSessions.remove(session.getPlayerId());
        this.scheduleRollback(session);
        if (this.queue.isEmpty() && this.activeSessions.isEmpty()) {
            this.globalEventActive = false;
            if (this.queueTask != null) {
                this.queueTask.cancel();
                this.queueTask = null;
            }
        }
    }

    private void scheduleRollback(StormSession session) {
        BukkitTask[] taskHolder = new BukkitTask[1];
        int batchSize = this.wallManager.getDefaultRollbackBatchSize();
        taskHolder[0] = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            int wallRemaining = this.wallManager.rollbackBatch(session.getWallBlocks(), batchSize);
            int terrainRemaining = this.terrainManager.rollbackBatch(session, batchSize);
            if (wallRemaining <= 0 && terrainRemaining <= 0) {
                taskHolder[0].cancel();
                this.debug("Rollback completado para la sesion de " + String.valueOf(session.getPlayerId()));
            }
        }, 1L, 1L);
    }

    public void testLocalArena(Player admin) {
        Location anchor = admin.getLocation().clone();
        ArrayList<ModifiedBlock> testLog = new ArrayList<ModifiedBlock>();
        long start = System.currentTimeMillis();
        admin.sendMessage((Component)Component.text((String)"Construyendo arena de prueba (2 chunks)...", (TextColor)NamedTextColor.YELLOW));
        this.wallManager.buildCage(anchor, testLog, () -> {
            long elapsed = System.currentTimeMillis() - start;
            admin.sendMessage((Component)Component.text((String)("Arena de prueba lista en " + elapsed + " ms (" + testLog.size() + " bloques). Desmantelando en 15s..."), (TextColor)NamedTextColor.GREEN));
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                BukkitTask[] rollbackHolder = new BukkitTask[1];
                int batchSize = this.wallManager.getDefaultRollbackBatchSize();
                rollbackHolder[0] = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
                    int remaining = this.wallManager.rollbackBatch(testLog, batchSize);
                    if (remaining <= 0) {
                        rollbackHolder[0].cancel();
                        admin.sendMessage((Component)Component.text((String)"Arena de prueba desmantelada.", (TextColor)NamedTextColor.GRAY));
                    }
                }, 1L, 1L);
            }, 300L);
        });
    }

    public Set<UUID> getPlayersWithSession() {
        return this.activeSessions.keySet();
    }

    public StormSession getSession(UUID playerId) {
        return this.activeSessions.get(playerId);
    }

    public static enum Outcome {
        VICTORY,
        DEATH,
        ABORTED;

    }
}

