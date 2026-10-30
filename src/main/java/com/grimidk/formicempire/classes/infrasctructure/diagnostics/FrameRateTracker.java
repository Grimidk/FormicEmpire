package com.grimidk.formicempire.classes.infrasctructure.diagnostics;

public final class FrameRateTracker {

    private static final int SAMPLE_COUNT = 60;
    private static final long MIN_FRAME_GAP_NANOS = 250_000L;

    private final long[] frameGapNanos = new long[SAMPLE_COUNT];
    private int sampleIndex;
    private int sampleCount;
    private long lastFrameNanos;
    private double smoothedFps;
    private double smoothedFrameMs;

    public void recordFrame() {
        long now = System.nanoTime();
        if (lastFrameNanos > 0L) {
            long gap = now - lastFrameNanos;
            if (gap >= MIN_FRAME_GAP_NANOS) {
                frameGapNanos[sampleIndex] = gap;
                sampleIndex = (sampleIndex + 1) % SAMPLE_COUNT;
                if (sampleCount < SAMPLE_COUNT) {
                    sampleCount++;
                }
                updateSmoothed();
            }
        }
        lastFrameNanos = now;
    }

    private void updateSmoothed() {
        long sum = 0L;
        for (int i = 0; i < sampleCount; i++) {
            sum += frameGapNanos[i];
        }
        smoothedFrameMs = (sum / (double) sampleCount) / 1_000_000.0;
        smoothedFps = smoothedFrameMs > 0.0 ? 1000.0 / smoothedFrameMs : 0.0;
    }

    public double getSmoothedFps() {
        return smoothedFps;
    }

    public double getSmoothedFrameMs() {
        return smoothedFrameMs;
    }

    public boolean hasSamples() {
        return sampleCount > 0;
    }

    public void reset() {
        sampleIndex = 0;
        sampleCount = 0;
        lastFrameNanos = 0L;
        smoothedFps = 0.0;
        smoothedFrameMs = 0.0;
    }
}
