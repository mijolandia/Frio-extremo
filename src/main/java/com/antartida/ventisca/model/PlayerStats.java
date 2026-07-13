package com.antartida.ventisca.model;

import java.util.UUID;

public final class PlayerStats {
    private final UUID playerId;
    private String lastKnownName;
    private long totalPoints;
    private int currentStreak;
    private int maxStreak;
    private int stormsWon;
    private int stormsFailed;

    public PlayerStats(UUID playerId, String lastKnownName) {
        this.playerId = playerId;
        this.lastKnownName = lastKnownName;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public String getLastKnownName() {
        return this.lastKnownName;
    }

    public void setLastKnownName(String lastKnownName) {
        this.lastKnownName = lastKnownName;
    }

    public long getTotalPoints() {
        return this.totalPoints;
    }

    public void addPoints(long points) {
        this.totalPoints = Math.max(0L, this.totalPoints + points);
    }

    public int getCurrentStreak() {
        return this.currentStreak;
    }

    public int getMaxStreak() {
        return this.maxStreak;
    }

    public int getStormsWon() {
        return this.stormsWon;
    }

    public int getStormsFailed() {
        return this.stormsFailed;
    }

    public void registerWin() {
        ++this.stormsWon;
        ++this.currentStreak;
        this.maxStreak = Math.max(this.maxStreak, this.currentStreak);
    }

    public void registerFailure() {
        ++this.stormsFailed;
        this.currentStreak = 0;
    }

    public void restore(int currentStreak, int maxStreak, int stormsWon, int stormsFailed) {
        this.currentStreak = currentStreak;
        this.maxStreak = maxStreak;
        this.stormsWon = stormsWon;
        this.stormsFailed = stormsFailed;
    }
}

