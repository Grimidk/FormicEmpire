package com.grimidk.formicempire.classes.infrasctructure.diagnostics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrameRateTrackerTest {

    @Test
    void recordsSmoothedFrameRateFromGaps() {
        FrameRateTracker tracker = new FrameRateTracker();
        for (int i = 0; i < 30; i++) {
            tracker.recordFrame();
            sleepMillis(16);
            tracker.recordFrame();
        }
        assertTrue(tracker.hasSamples());
        assertTrue(tracker.getSmoothedFps() > 30.0);
        assertTrue(tracker.getSmoothedFps() < 80.0);
        assertTrue(tracker.getSmoothedFrameMs() > 12.0);
        assertTrue(tracker.getSmoothedFrameMs() < 33.0);
    }

    @Test
    void resetClearsSamples() {
        FrameRateTracker tracker = new FrameRateTracker();
        tracker.recordFrame();
        sleepMillis(20);
        tracker.recordFrame();
        assertTrue(tracker.hasSamples());
        tracker.reset();
        assertFalse(tracker.hasSamples());
        assertEquals(0.0, tracker.getSmoothedFps(), 1e-9);
        assertEquals(0.0, tracker.getSmoothedFrameMs(), 1e-9);
    }

    private static void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
