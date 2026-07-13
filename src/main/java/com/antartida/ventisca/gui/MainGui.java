package com.antartida.ventisca.gui;

import com.antartida.ventisca.gui.AdminGuiHolder;
import com.antartida.ventisca.gui.PlayerGuiHolder;
import com.antartida.ventisca.manager.StatsManager;
import com.antartida.ventisca.model.PlayerStats;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public final class MainGui {
    private MainGui() {
    }

    public static void openPlayerGui(Player viewer, StatsManager statsManager) {
        PlayerGuiHolder holder = new PlayerGuiHolder();
        Inventory inventory = Bukkit.createInventory((InventoryHolder)holder, (int)27, (String)"\u00a7b\u00a7lVentisca Eterna - Tus Estadisticas");
        holder.setInventory(inventory);
        PlayerStats stats = statsManager.getOrCreate(viewer);
        inventory.setItem(4, MainGui.buildPlayerHead(viewer, stats));
        List<PlayerStats> top3 = statsManager.getTopThree();
        int[] podiumSlots = new int[]{11, 12, 13};
        String[] medals = new String[]{"\u00a76\u00a7lOro", "\u00a77\u00a7lPlata", "\u00a7c\u00a7lBronce"};
        for (int i = 0; i < podiumSlots.length; ++i) {
            if (i < top3.size()) {
                inventory.setItem(podiumSlots[i], MainGui.buildTopEntry(top3.get(i), medals[i], i + 1));
                continue;
            }
            inventory.setItem(podiumSlots[i], MainGui.buildEmptyPodium(medals[i], i + 1));
        }
        viewer.openInventory(inventory);
    }

    private static ItemStack buildPlayerHead(Player viewer, PlayerStats stats) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta itemMeta = head.getItemMeta();
        if (itemMeta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta)itemMeta;
            skullMeta.setOwningPlayer((OfflinePlayer)viewer);
            skullMeta.setDisplayName("\u00a7b\u00a7l" + viewer.getName());
            skullMeta.setLore(List.of("\u00a77Puntos historicos: \u00a7f" + stats.getTotalPoints(), "\u00a77Racha maxima: \u00a7f" + stats.getMaxStreak(), "\u00a77Racha actual: \u00a7f" + stats.getCurrentStreak(), "\u00a7a Tormentas ganadas: \u00a7f" + stats.getStormsWon(), "\u00a7c Tormentas falladas: \u00a7f" + stats.getStormsFailed()));
            head.setItemMeta((ItemMeta)skullMeta);
        }
        return head;
    }

    private static ItemStack buildTopEntry(PlayerStats stats, String medalLabel, int position) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta)itemMeta;
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer((UUID)stats.getPlayerId()));
            skullMeta.setDisplayName(medalLabel + " \u00a7f#" + position + " " + stats.getLastKnownName());
            skullMeta.setLore(List.of("\u00a77Puntos totales: \u00a7f" + stats.getTotalPoints(), "\u00a77Racha mas alta: \u00a7f" + stats.getMaxStreak()));
            item.setItemMeta((ItemMeta)skullMeta);
        }
        return item;
    }

    private static ItemStack buildEmptyPodium(String medalLabel, int position) {
        ItemStack item = new ItemStack(Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(medalLabel + " \u00a7f#" + position + " - Vacante");
            meta.setLore(List.of("\u00a77Nadie ha reclamado este puesto todavia."));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void openAdminGui(Player viewer, boolean debugEnabled) {
        AdminGuiHolder holder = new AdminGuiHolder();
        Inventory inventory = Bukkit.createInventory((InventoryHolder)holder, (int)27, (String)"\u00a73\u00a7lVentisca Eterna - Administracion");
        holder.setInventory(inventory);
        for (int i = 0; i < inventory.getSize(); ++i) {
            inventory.setItem(i, MainGui.namedItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ", List.of()));
        }
        inventory.setItem(10, MainGui.namedItem(Material.PACKED_ICE, "\u00a7b\u00a7lIniciar Glaciacion Global", List.of("\u00a77Encola a todos los jugadores elegibles", "\u00a77con cooldown dinamico entre activaciones.")));
        inventory.setItem(12, MainGui.namedItem(Material.BARRIER, "\u00a7c\u00a7lDetener / Abortar Todo", List.of("\u00a77Cancela la cola global y deshiela", "\u00a77de inmediato a todos los afectados.")));
        inventory.setItem(14, MainGui.namedItem(Material.IRON_BLOCK, "\u00a7e\u00a7lTestear Arena Local (2 chunks)", List.of("\u00a77Construye y desmantela una jaula", "\u00a77de prueba en tu posicion actual.")));
        inventory.setItem(16, MainGui.namedItem(Material.BOOK, "\u00a7a\u00a7lRecargar Configuracion", List.of("\u00a77Vuelve a leer config.yml sin reiniciar", "\u00a77el servidor.")));
        inventory.setItem(22, MainGui.namedItem(debugEnabled ? Material.LIME_DYE : Material.GRAY_DYE, "\u00a7d\u00a7lModo Debug: " + (debugEnabled ? "\u00a7aACTIVADO" : "\u00a77DESACTIVADO"), List.of("\u00a77Click para " + (debugEnabled ? "desactivar" : "activar") + " los logs detallados.")));
        viewer.openInventory(inventory);
    }

    private static ItemStack namedItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}

