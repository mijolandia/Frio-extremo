package com.antartida.ventisca.listener;

import com.antartida.ventisca.gui.AdminGuiHolder;
import com.antartida.ventisca.gui.MainGui;
import com.antartida.ventisca.gui.PlayerGuiHolder;
import com.antartida.ventisca.manager.EventManager;
import com.antartida.ventisca.manager.StatsManager;
import com.antartida.ventisca.model.EventStatus;
import com.antartida.ventisca.session.StormSession;
import java.util.Locale;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;

public final class EventListeners
implements Listener {
    private static final Set<String> ESCAPE_COMMANDS = Set.of("spawn", "tpa", "tpahere", "tp", "home", "warp", "call");
    private final EventManager eventManager;
    private final StatsManager statsManager;

    public EventListeners(EventManager eventManager, StatsManager statsManager) {
        this.eventManager = eventManager;
        this.statsManager = statsManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        boolean moved;
        StormSession session = this.eventManager.getSession(event.getPlayer().getUniqueId());
        if (session == null || session.getStatus() != EventStatus.BUILDING) {
            return;
        }
        if (event.getTo() == null) {
            return;
        }
        boolean bl = moved = event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ();
        if (moved) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!this.eventManager.isCommandLocked(player.getUniqueId())) {
            return;
        }
        String rawCommand = event.getMessage().substring(1);
        String baseCommand = rawCommand.split(" ")[0].toLowerCase(Locale.ROOT);
        if (baseCommand.contains(":")) {
            baseCommand = baseCommand.substring(baseCommand.indexOf(58) + 1);
        }
        event.setCancelled(true);
        if (ESCAPE_COMMANDS.contains(baseCommand)) {
            player.sendMessage((Component)Component.text((String)"ESCAPE DENEGADO! El frio artico interfiere con la teletransportacion.", (TextColor)NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_BREAK_BLOCK, 1.0f, 1.0f);
        } else {
            player.sendMessage((Component)Component.text((String)"Tus dedos estan demasiado congelados para escribir...", (TextColor)NamedTextColor.GRAY));
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        StormSession session = this.eventManager.getSession(player.getUniqueId());
        if (session == null) {
            return;
        }
        this.eventManager.handlePlayerDeath(player, session);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof PlayerGuiHolder) {
            event.setCancelled(true);
            return;
        }
        if (!(holder instanceof AdminGuiHolder)) {
            return;
        }
        event.setCancelled(true);
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        if (!player.hasPermission("ventiscaeterna.admin")) {
            return;
        }
        int slot = event.getRawSlot();
        if (slot == 10) {
            player.closeInventory();
            this.eventManager.startGlobalSequence((CommandSender)player);
        } else if (slot == 12) {
            player.closeInventory();
            this.eventManager.abortGlobalSequence((CommandSender)player);
        } else if (slot == 14) {
            player.closeInventory();
            this.eventManager.testLocalArena(player);
        } else if (slot == 16) {
            player.closeInventory();
            player.getServer().getPluginManager().getPlugin("VentiscaEterna").reloadConfig();
            player.sendMessage((Component)Component.text((String)"Configuracion recargada.", (TextColor)NamedTextColor.GREEN));
        } else if (slot == 22) {
            this.eventManager.setDebugEnabled(!this.eventManager.isDebugEnabled());
            MainGui.openAdminGui(player, this.eventManager.isDebugEnabled());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
    }
}

