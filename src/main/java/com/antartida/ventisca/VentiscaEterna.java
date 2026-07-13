package com.antartida.ventisca;

import com.antartida.ventisca.command.AntartidaCommand;
import com.antartida.ventisca.listener.EventListeners;
import com.antartida.ventisca.manager.EventManager;
import com.antartida.ventisca.manager.MobManager;
import com.antartida.ventisca.manager.StatsManager;
import com.antartida.ventisca.manager.TerrainManager;
import com.antartida.ventisca.manager.WallManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class VentiscaEterna
extends JavaPlugin {
    private EventManager eventManager;
    private StatsManager statsManager;

    public void onEnable() {
        this.saveDefaultConfig();
        this.statsManager = new StatsManager(this);
        this.statsManager.load();
        WallManager wallManager = new WallManager(this);
        TerrainManager terrainManager = new TerrainManager(this);
        MobManager mobManager = new MobManager(this);
        this.eventManager = new EventManager(this, wallManager, terrainManager, mobManager, this.statsManager);
        AntartidaCommand antartidaCommand = new AntartidaCommand(this.eventManager, this.statsManager);
        PluginCommand command = this.getCommand("antartida");
        if (command != null) {
            command.setExecutor((CommandExecutor)antartidaCommand);
        } else {
            this.getLogger().severe("No se pudo registrar el comando /antartida. Revisa plugin.yml.");
        }
        this.getServer().getPluginManager().registerEvents((Listener)new EventListeners(this.eventManager, this.statsManager), (Plugin)this);
        this.getLogger().info("VentiscaEterna habilitado -- sistema profesional de glaciacion por fases listo.");
    }

    public void onDisable() {
        if (this.eventManager != null && this.eventManager.isGlobalEventActive()) {
            this.getLogger().info("Servidor deteniendose con una glaciacion en curso: las sesiones RAM-Only se descartan.");
        }
        if (this.statsManager != null) {
            this.statsManager.saveSync();
        }
    }

    public EventManager getEventManager() {
        return this.eventManager;
    }

    public StatsManager getStatsManager() {
        return this.statsManager;
    }
}

