package com.antartida.ventisca.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StatsManagerTest {

    @Test
    void victoryBonusConstant() {
        assertEquals(850L, StatsManager.VICTORY_BONUS);
    }

    @Test
    void zeroSecondsYieldsNoPoints() {
        assertEquals(0L, StatsManager.computeProportionalPoints(0));
    }

    @Test
    void fullStormMatchesConfiguredPointsPerMinute() {
        // 120 s at 3.333.. pts/s == 200 pts/min * 2 min == 400 pts.
        assertEquals(400L, StatsManager.computeProportionalPoints(120));
    }

    @ParameterizedTest
    @CsvSource({
            "1, 3",
            "3, 10",
            "30, 100",
            "60, 200",
            "90, 300"
    })
    void proportionalPointsRoundToNearest(int seconds, long expected) {
        assertEquals(expected, StatsManager.computeProportionalPoints(seconds));
    }
}
