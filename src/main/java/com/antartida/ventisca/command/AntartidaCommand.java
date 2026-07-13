package com.antartida.ventisca.command;

import com.antartida.ventisca.gui.MainGui;
import com.antartida.ventisca.manager.EventManager;
import com.antartida.ventisca.manager.StatsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class AntartidaCommand
implements CommandExecutor {
    public static final String ADMIN_PERMISSION = "ventiscaeterna.admin";
    public static final String PLAYER_PERMISSION = "ventiscaeterna.player";
    private final EventManager eventManager;
    private final StatsManager statsManager;

    public AntartidaCommand(EventManager eventManager, StatsManager statsManager) {
        this.eventManager = eventManager;
        this.statsManager = statsManager;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean requestedAdminExplicitly;
        if (!(sender instanceof Player)) {
            sender.sendMessage((Component)Component.text((String)"Este comando solo puede ser usado por jugadores.", (TextColor)NamedTextColor.RED));
            return true;
        }
        Player player = (Player)sender;
        boolean isAdmin = player.hasPermission(ADMIN_PERMISSION);
        boolean bl = requestedAdminExplicitly = args.length > 0 && args[0].equalsIgnoreCase("admin");
        if (requestedAdminExplicitly && !isAdmin) {
            player.sendMessage((Component)Component.text((String)"No tienes permiso para abrir el panel de administracion.", (TextColor)NamedTextColor.RED));
            return true;
        }
        if (isAdmin) {
            MainGui.openAdminGui(player, this.eventManager.isDebugEnabled());
            return true;
        }
        if (!player.hasPermission(PLAYER_PERMISSION)) {
            player.sendMessage((Component)Component.text((String)"No tienes permiso para usar este comando.", (TextColor)NamedTextColor.RED));
            return true;
        }
        MainGui.openPlayerGui(player, this.statsManager);
        return true;
    }
}

