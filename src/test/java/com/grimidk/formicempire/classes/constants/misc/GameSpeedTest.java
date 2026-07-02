package com.grimidk.formicempire.classes.constants.misc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameSpeedTest {

    @Test
    void getNextStopsAtMaxWithoutTurbo() {
        assertEquals(GameSpeed.VERY_FAST,
                GameSpeed.getNext(GameSpeed.VERY_FAST, false));
    }

    @Test
    void getNextStopsAtTurboWhenEnabled() {
        assertEquals(GameSpeed.TURBO,
                GameSpeed.getNext(GameSpeed.TURBO, true));
    }

    @Test
    void getPreviousStopsAtVerySlow() {
        assertEquals(GameSpeed.VERY_SLOW,
                GameSpeed.getPrevious(GameSpeed.VERY_SLOW));
    }

    @Test
    void getNextStepsUpWhenBelowMax() {
        assertEquals(GameSpeed.FAST,
                GameSpeed.getNext(GameSpeed.NORMAL, false));
    }
}
