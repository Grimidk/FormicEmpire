package com.grimidk.formicempire.classes.infrasctructure.util;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Random;

public final class GameRandom {
    private static final Random RNG = new Random();
    private static final Deque<Double> TEST_DOUBLE_QUEUE = new ArrayDeque<>();

    private GameRandom() {}

    public static void enqueueTestDoubles(double... values) {
        for (double value : values) {
            TEST_DOUBLE_QUEUE.addLast(value);
        }
    }

    public static void clearTestDoubles() {
        TEST_DOUBLE_QUEUE.clear();
    }

    public static double nextDouble() {
        if (!TEST_DOUBLE_QUEUE.isEmpty()) {
            return TEST_DOUBLE_QUEUE.removeFirst();
        }
        return RNG.nextDouble();
    }

    public static float nextFloat() {
        return RNG.nextFloat();
    }

    public static boolean nextBoolean() {
        return RNG.nextBoolean();
    }

    public static int nextInt(int bound) {
        return RNG.nextInt(bound);
    }

    public static Random getShuffleRandom() {
        return RNG;
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
