package com.antartida.ventisca.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlayerStatsTest {

    private static PlayerStats newStats() {
        return new PlayerStats(UUID.randomUUID(), "Steve");
    }

    @Test
    void newStatsStartAtZero() {
        PlayerStats stats = newStats();
        assertEquals(0L, stats.getTotalPoints());
        assertEquals(0, stats.getCurrentStreak());
        assertEquals(0, stats.getMaxStreak());
        assertEquals(0, stats.getStormsWon());
        assertEquals(0, stats.getStormsFailed());
        assertEquals("Steve", stats.getLastKnownName());
    }

    @Test
    void constructorKeepsProvidedIdentity() {
        UUID id = UUID.randomUUID();
        PlayerStats stats = new PlayerStats(id, "Alex");
        assertSame(id, stats.getPlayerId());
        assertEquals("Alex", stats.getLastKnownName());
    }

    @Test
    void setLastKnownNameUpdatesName() {
        PlayerStats stats = newStats();
        stats.setLastKnownName("Renamed");
        assertEquals("Renamed", stats.getLastKnownName());
    }

    @Test
    void addPointsAccumulates() {
        PlayerStats stats = newStats();
        stats.addPoints(100L);
        stats.addPoints(50L);
        assertEquals(150L, stats.getTotalPoints());
    }

    @Test
    void addPointsIsClampedAtZero() {
        PlayerStats stats = newStats();
        stats.addPoints(30L);
        stats.addPoints(-100L);
        assertEquals(0L, stats.getTotalPoints());
    }

    @Test
    void registerWinIncrementsWinsAndStreak() {
        PlayerStats stats = newStats();
        stats.registerWin();
        stats.registerWin();
        assertEquals(2, stats.getStormsWon());
        assertEquals(2, stats.getCurrentStreak());
        assertEquals(2, stats.getMaxStreak());
    }

    @Test
    void registerFailureResetsCurrentStreakButKeepsMax() {
        PlayerStats stats = newStats();
        stats.registerWin();
        stats.registerWin();
        stats.registerWin();
        stats.registerFailure();
        assertEquals(3, stats.getMaxStreak());
        assertEquals(0, stats.getCurrentStreak());
        assertEquals(1, stats.getStormsFailed());
    }

    @Test
    void maxStreakTracksHighestRun() {
        PlayerStats stats = newStats();
        stats.registerWin();
        stats.registerWin();
        stats.registerFailure();
        stats.registerWin();
        assertEquals(1, stats.getCurrentStreak());
        assertEquals(2, stats.getMaxStreak());
    }

    @Test
    void restoreOverwritesCountersVerbatim() {
        PlayerStats stats = newStats();
        stats.restore(4, 9, 12, 7);
        assertEquals(4, stats.getCurrentStreak());
        assertEquals(9, stats.getMaxStreak());
        assertEquals(12, stats.getStormsWon());
        assertEquals(7, stats.getStormsFailed());
    }
}
