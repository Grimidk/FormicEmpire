package com.grimidk.formicempire.classes.constants.misc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameSpeedTest {

    @Test
    void stepUpMovesOneIdWithoutTurbo() {
        assertEquals(GameSpeed.FAST, GameSpeed.step(GameSpeed.NORMAL, 1, false));
        assertEquals(GameSpeed.VERY_FAST, GameSpeed.step(GameSpeed.VERY_FAST, 1, false));
    }

    @Test
    void stepDownMovesOneId() {
        assertEquals(GameSpeed.SLOW, GameSpeed.step(GameSpeed.NORMAL, -1, true));
        assertEquals(GameSpeed.VERY_SLOW, GameSpeed.step(GameSpeed.VERY_SLOW, -1, true));
    }

    @Test
    void stepUpReachesTurboWhenAllowed() {
        assertEquals(GameSpeed.TURBO, GameSpeed.step(GameSpeed.VERY_FAST, 1, true));
    }

    @Test
    void fromIdMapsEveryConstant() {
        assertEquals(GameSpeed.VERY_SLOW, GameSpeed.fromId(GameSpeed.ID_VERY_SLOW));
        assertEquals(GameSpeed.TURBO, GameSpeed.fromId(GameSpeed.ID_TURBO));
        assertEquals(GameSpeed.NORMAL, GameSpeed.fromId(99));
    }
}
