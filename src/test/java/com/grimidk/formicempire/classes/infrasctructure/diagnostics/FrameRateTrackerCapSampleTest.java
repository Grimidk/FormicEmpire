package com.grimidk.formicempire.classes.infrasctructure.diagnostics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FrameRateTrackerCapSampleTest {

    @Test
    void sixtyHertzTicksReportNearSixtyFps() throws InterruptedException {
        FrameRateTracker tracker = new FrameRateTracker();
        long intervalNs = 16_000_000L;
        long start = System.nanoTime();
        for (int i = 0; i < 45; i++) {
            long target = start + i * intervalNs;
            long waitMs = Math.max(0L, (target - System.nanoTime()) / 1_000_000L);
            if (waitMs > 0L) {
                Thread.sleep(waitMs);
            }
            tracker.recordFrame();
        }
        assertTrue(tracker.hasSamples());
        assertTrue(tracker.getSmoothedFps() > 50.0, "fps=" + tracker.getSmoothedFps());
        assertTrue(tracker.getSmoothedFps() < 70.0, "fps=" + tracker.getSmoothedFps());
    }
}
