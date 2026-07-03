package com.grimidk.formicempire.classes.infrasctructure.util;

import java.util.Collections;
import java.util.Random;

public final class GameRandom {
    private static final Random RNG = new Random();

    private GameRandom() {}

    public static double nextDouble() {
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

    /** Shared RNG for {@link Collections#shuffle} and similar APIs that require a {@link Random}. */
    public static Random getShuffleRandom() {
        return RNG;
    }

    /** Deterministic int in {@code [0, bound)} from a seed (no allocation). */
    public static int seededNextInt(long seed, int bound) {
        if (bound <= 0) {
            return 0;
        }
        long mixed = mixSeed(seed);
        return (int) ((mixed >>> 32) % bound);
    }

    /** Deterministic float in {@code [0, 1)} from a seed (no allocation). */
    public static float seededNextFloat(long seed) {
        long mixed = mixSeed(seed);
        return ((mixed >>> 40) & 0xFFFFFF) / (float) (1 << 24);
    }

    private static long mixSeed(long seed) {
        return seed * 6364136223846793005L + 1442695040888963407L;
    }
}
