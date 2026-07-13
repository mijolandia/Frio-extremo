package com.antartida.ventisca.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.antartida.ventisca.manager.EventManager;
import org.junit.jupiter.api.Test;

class EnumsTest {

    @Test
    void blockCategoryValues() {
        assertArrayEquals(new BlockCategory[]{BlockCategory.SURFACE, BlockCategory.DEEP}, BlockCategory.values());
        assertEquals(BlockCategory.SURFACE, BlockCategory.valueOf("SURFACE"));
    }

    @Test
    void eventStatusValues() {
        assertArrayEquals(
                new EventStatus[]{EventStatus.BUILDING, EventStatus.ACTIVE, EventStatus.ENDING},
                EventStatus.values());
        assertEquals(EventStatus.ACTIVE, EventStatus.valueOf("ACTIVE"));
    }

    @Test
    void outcomeValues() {
        assertArrayEquals(
                new EventManager.Outcome[]{
                        EventManager.Outcome.VICTORY,
                        EventManager.Outcome.DEATH,
                        EventManager.Outcome.ABORTED},
                EventManager.Outcome.values());
        assertEquals(EventManager.Outcome.DEATH, EventManager.Outcome.valueOf("DEATH"));
    }
}
