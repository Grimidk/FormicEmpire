package com.grimidk.formicempire.classes.constants.misc;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameSpeedTest {

    @Test
    void stepUpMovesOneIdWithoutTurbo() {
        assertEquals(GameConstants.SPEED_FAST, GameSpeed.step(GameConstants.SPEED_NORMAL, 1, false));
        assertEquals(GameConstants.SPEED_VERY_FAST, GameSpeed.step(GameConstants.SPEED_VERY_FAST, 1, false));
    }

    @Test
    void stepDownMovesOneId() {
        assertEquals(GameConstants.SPEED_SLOW, GameSpeed.step(GameConstants.SPEED_NORMAL, -1, true));
        assertEquals(GameConstants.SPEED_VERY_SLOW, GameSpeed.step(GameConstants.SPEED_VERY_SLOW, -1, true));
    }

    @Test
    void stepUpReachesTurboWhenAllowed() {
        assertEquals(GameConstants.SPEED_TURBO, GameSpeed.step(GameConstants.SPEED_VERY_FAST, 1, true));
    }

    @Test
    void fromIdMapsEveryConstant() {
        assertEquals(GameConstants.SPEED_VERY_SLOW, GameSpeed.fromId(GameSpeed.ID_VERY_SLOW));
        assertEquals(GameConstants.SPEED_TURBO, GameSpeed.fromId(GameSpeed.ID_TURBO));
        assertEquals(GameConstants.SPEED_NORMAL, GameSpeed.fromId(99));
    }
}
