package com.antartida.ventisca.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.antartida.ventisca.model.EventStatus;
import java.util.UUID;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StormSessionTest {

    private static StormSession newSession() {
        return new StormSession(UUID.randomUUID());
    }

    @Test
    void defaultsMatchConstants() {
        StormSession session = newSession();
        assertEquals(EventStatus.BUILDING, session.getStatus());
        assertEquals(StormSession.TOTAL_TICKS, session.getTicksRemaining());
        assertEquals(120, session.getSecondsRemaining());
        assertEquals(0, session.getSecondsElapsed());
        assertEquals(1, session.getPhase());
    }

    @Test
    void secondsRemainingNeverNegative() {
        StormSession session = newSession();
        session.setTicksRemaining(-40);
        assertEquals(0, session.getSecondsRemaining());
    }

    @Test
    void secondsElapsedCountsFromTotal() {
        StormSession session = newSession();
        session.setTicksRemaining(StormSession.TOTAL_TICKS - 400);
        assertEquals(20, session.getSecondsElapsed());
    }

    @Test
    void secondsElapsedClampsWhenTimeExhausted() {
        StormSession session = newSession();
        session.setTicksRemaining(-100);
        assertEquals(120, session.getSecondsElapsed());
    }

    @ParameterizedTest
    @CsvSource({
            "2400, 1",
            "1820, 1",
            "1819, 2",
            "1800, 2",
            "1220, 2",
            "1219, 3",
            "1200, 3",
            "620, 3",
            "619, 4",
            "600, 4",
            "200, 4",
            "0, 4"
    })
    void phaseFollowsRemainingSeconds(int ticksRemaining, int expectedPhase) {
        StormSession session = newSession();
        session.setTicksRemaining(ticksRemaining);
        assertEquals(expectedPhase, session.getPhase());
    }

    @Test
    void cancelTasksCancelsAndClearsBothTimers() {
        StormSession session = newSession();
        BukkitTask fast = mock(BukkitTask.class);
        BukkitTask slow = mock(BukkitTask.class);
        session.setFastTask(fast);
        session.setSlowTask(slow);

        session.cancelTasks();

        verify(fast).cancel();
        verify(slow).cancel();
        assertNull(session.getFastTask());
        assertNull(session.getSlowTask());
    }

    @Test
    void cancelTasksIsSafeWhenNoTasksScheduled() {
        StormSession session = newSession();
        session.cancelTasks();
        assertNull(session.getFastTask());
        assertNull(session.getSlowTask());
    }

    @Test
    void collectionsStartEmpty() {
        StormSession session = newSession();
        assertEquals(0, session.getSurfaceBlocks().size());
        assertEquals(0, session.getDeepBlocks().size());
        assertEquals(0, session.getWallBlocks().size());
        assertEquals(0, session.getTerrainBlocks().size());
        assertEquals(0, session.getSpawnedMobs().size());
    }
}
