package com.grimidk.formicempire.classes.infrasctructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EngineFrameRateCapTest {

    private Engine engine;

    @BeforeEach
    void setUp() {
        engine = new Engine();
    }

    @Test
    void uncappedMeansOneMsVisualInterval() {
        engine.setFrameRateCap(0);
        assertEquals(0, engine.getFrameRateCap());
        assertEquals(1, engine.getVisualFrameIntervalMs());
    }

    @Test
    void sanitizeMapsToSupportedCaps() {
        assertEquals(0, Engine.sanitizeFrameRateCap(0));
        assertEquals(0, Engine.sanitizeFrameRateCap(-5));
        assertEquals(30, Engine.sanitizeFrameRateCap(15));
        assertEquals(30, Engine.sanitizeFrameRateCap(30));
        assertEquals(60, Engine.sanitizeFrameRateCap(45));
        assertEquals(60, Engine.sanitizeFrameRateCap(60));
        assertEquals(120, Engine.sanitizeFrameRateCap(90));
        assertEquals(120, Engine.sanitizeFrameRateCap(240));
    }

    @Test
    void visualIntervalMatchesCap() {
        engine.setFrameRateCap(30);
        assertEquals(30, engine.getFrameRateCap());
        assertEquals(33, engine.getVisualFrameIntervalMs());

        engine.setFrameRateCap(60);
        assertEquals(16, engine.getVisualFrameIntervalMs());

        engine.setFrameRateCap(120);
        assertEquals(8, engine.getVisualFrameIntervalMs());

        engine.setFrameRateCap(0);
        assertEquals(1, engine.getVisualFrameIntervalMs());
    }
}
