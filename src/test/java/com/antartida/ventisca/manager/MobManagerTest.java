package com.antartida.ventisca.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MobManagerTest {

    private static MobManager newManager() {
        return new MobManager(mock(JavaPlugin.class));
    }

    @ParameterizedTest
    @CsvSource({
            "1, 5",
            "2, 15",
            "3, 25",
            "4, 40"
    })
    void capScalesWithPhase(int phase, int expectedCap) {
        assertEquals(expectedCap, newManager().getMaxMobsForPhase(phase));
    }

    @Test
    void unknownPhasesFallBackToMaxCap() {
        MobManager manager = newManager();
        assertEquals(40, manager.getMaxMobsForPhase(0));
        assertEquals(40, manager.getMaxMobsForPhase(99));
        assertEquals(40, manager.getMaxMobsForPhase(-1));
    }
}
