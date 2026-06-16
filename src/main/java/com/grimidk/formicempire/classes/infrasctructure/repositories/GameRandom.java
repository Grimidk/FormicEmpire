package com.grimidk.formicempire.classes.infrasctructure.repositories;

import java.util.Random;

public final class GameRandom {
    private static final Random RNG = new Random();

    private GameRandom() {}

    public static double nextDouble() {
        return RNG.nextDouble();
    }

    public static boolean nextBoolean() {
        return RNG.nextBoolean();
    }

    public static int nextInt(int bound) {
        return RNG.nextInt(bound);
    }
}
