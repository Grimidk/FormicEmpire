package com.grimidk.formicempire.classes.infrasctructure.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public final class GameRandom {
    private static final ThreadLocal<Random> THREAD_RANDOM = ThreadLocal.withInitial(Random::new);
    private static final Deque<Double> TEST_DOUBLE_QUEUE = new ArrayDeque<>();
    private static volatile boolean hasTestDoubles = false;

    private GameRandom() {}

    public static void enqueueTestDoubles(double... values) {
        synchronized (TEST_DOUBLE_QUEUE) {
            for (double value : values) {
                TEST_DOUBLE_QUEUE.addLast(value);
            }
            hasTestDoubles = !TEST_DOUBLE_QUEUE.isEmpty();
        }
    }

    public static void clearTestDoubles() {
        synchronized (TEST_DOUBLE_QUEUE) {
            TEST_DOUBLE_QUEUE.clear();
            hasTestDoubles = false;
        }
    }

    public static double nextDouble() {
        if (hasTestDoubles) {
            synchronized (TEST_DOUBLE_QUEUE) {
                if (!TEST_DOUBLE_QUEUE.isEmpty()) {
                    double value = TEST_DOUBLE_QUEUE.removeFirst();
                    hasTestDoubles = !TEST_DOUBLE_QUEUE.isEmpty();
                    return value;
                }
            }
        }
        return THREAD_RANDOM.get().nextDouble();
    }

    public static float nextFloat() {
        return THREAD_RANDOM.get().nextFloat();
    }

    public static boolean nextBoolean() {
        return THREAD_RANDOM.get().nextBoolean();
    }

    public static int nextInt(int bound) {
        return THREAD_RANDOM.get().nextInt(bound);
    }

    public static Random getShuffleRandom() {
        return THREAD_RANDOM.get();
    }

    public static int seededNextInt(long seed, int bound) {
        if (bound <= 0) {
            return 0;
        }
        long mixed = mixSeed(seed);
        return (int) ((mixed >>> 32) % bound);
    }

    public static float seededNextFloat(long seed) {
        long mixed = mixSeed(seed);
        return ((mixed >>> 40) & 0xFFFFFF) / (float) (1 << 24);
    }

    private static long mixSeed(long seed) {
        return seed * 6364136223846793005L + 1442695040888963407L;
    }
}
