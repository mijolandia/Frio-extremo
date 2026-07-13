package com.antartida.ventisca.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class AdminGuiHolder
implements InventoryHolder {
    public static final int START_GLOBAL_SLOT = 10;
    public static final int ABORT_ALL_SLOT = 12;
    public static final int TEST_ARENA_SLOT = 14;
    public static final int RELOAD_CONFIG_SLOT = 16;
    public static final int TOGGLE_DEBUG_SLOT = 22;
    private Inventory inventory;

    public Inventory getInventory() {
        return this.inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}

