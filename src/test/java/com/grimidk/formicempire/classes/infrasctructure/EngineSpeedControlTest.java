package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EngineSpeedControlTest {

    private Engine engine;

    @BeforeEach
    void setUp() {
        engine = new Engine();
        engine.setSpeed(GameConstants.SPEED_NORMAL);
        engine.pauseEngine();
    }

    @Test
    void adjustSpeedStepUpWhilePausedOnlyResumes() {
        engine.adjustSpeedStep(1);

        assertFalse(engine.isPaused());
        assertEquals(GameConstants.SPEED_NORMAL.getId(), engine.getSpeed().getId());
    }

    @Test
    void adjustSpeedStepUpWhileRunningStepsSpeed() {
        engine.resumeEngine();

        engine.adjustSpeedStep(1);

        assertFalse(engine.isPaused());
        assertEquals(GameConstants.SPEED_FAST.getId(), engine.getSpeed().getId());
    }

    @Test
    void adjustSpeedStepUpFromVerySlowPauseResumesAtVerySlow() {
        engine.setSpeed(GameConstants.SPEED_VERY_SLOW);
        engine.pauseEngine();

        engine.adjustSpeedStep(1);

        assertFalse(engine.isPaused());
        assertEquals(GameConstants.SPEED_VERY_SLOW.getId(), engine.getSpeed().getId());
    }

    @Test
    void adjustSpeedStepDownWhileRunningStepsSpeed() {
        engine.setSpeed(GameConstants.SPEED_SLOW);
        engine.resumeEngine();

        engine.adjustSpeedStep(-1);

        assertFalse(engine.isPaused());
        assertEquals(GameConstants.SPEED_VERY_SLOW.getId(), engine.getSpeed().getId());
    }

    @Test
    void adjustSpeedStepDownFromNormalChangesOneTierOnly() {
        engine.setSpeed(GameConstants.SPEED_NORMAL);
        engine.resumeEngine();

        engine.adjustSpeedStep(-1);

        assertEquals(GameConstants.SPEED_SLOW.getId(), engine.getSpeed().getId());
    }

    @Test
    void adjustSpeedStepDownFromVerySlowPauses() {
        engine.setSpeed(GameConstants.SPEED_VERY_SLOW);
        engine.resumeEngine();

        engine.adjustSpeedStep(-1);

        assertTrue(engine.isPaused());
        assertEquals(GameConstants.SPEED_VERY_SLOW.getId(), engine.getSpeed().getId());
    }

    @Test
    void adjustSpeedStepDownCoalescesDuplicateCalls() {
        engine.setSpeed(GameConstants.SPEED_NORMAL);
        engine.resumeEngine();

        engine.adjustSpeedStep(-1);
        engine.adjustSpeedStep(-1);

        assertEquals(GameConstants.SPEED_SLOW.getId(), engine.getSpeed().getId());
    }

    @Test
    void adjustSpeedStepDownWhilePausedIsNoOp() {
        engine.adjustSpeedStep(-1);

        assertTrue(engine.isPaused());
        assertEquals(GameConstants.SPEED_NORMAL.getId(), engine.getSpeed().getId());
    }
}
