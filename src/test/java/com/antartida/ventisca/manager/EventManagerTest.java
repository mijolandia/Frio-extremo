package com.antartida.ventisca.manager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventManagerTest {

    private EventManager manager;

    @BeforeEach
    void setUp() {
        manager = new EventManager(
                mock(JavaPlugin.class),
                mock(WallManager.class),
                mock(TerrainManager.class),
                mock(MobManager.class),
                mock(StatsManager.class));
    }

    @Test
    void freshManagerHasNoGlobalEvent() {
        assertFalse(manager.isGlobalEventActive());
    }

    @Test
    void freshManagerHasNoSessions() {
        UUID id = UUID.randomUUID();
        assertFalse(manager.hasSession(id));
        assertNull(manager.getSession(id));
        assertTrue(manager.getPlayersWithSession().isEmpty());
    }

    @Test
    void commandLockIsFalseWithoutSession() {
        assertFalse(manager.isCommandLocked(UUID.randomUUID()));
    }

    @Test
    void debugFlagTogglesAndDefaultsOff() {
        assertFalse(manager.isDebugEnabled());
        manager.setDebugEnabled(true);
        assertTrue(manager.isDebugEnabled());
        manager.setDebugEnabled(false);
        assertFalse(manager.isDebugEnabled());
    }
}
