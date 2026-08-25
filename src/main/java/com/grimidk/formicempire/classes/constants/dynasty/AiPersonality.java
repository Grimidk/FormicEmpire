package com.grimidk.formicempire.classes.constants.dynasty;

import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

public enum AiPersonality {
    MILITARIST,
    PACIFIST;

    public static AiPersonality random() {
        return GameRandom.nextBoolean() ? MILITARIST : PACIFIST;
    }

    public static AiPersonality fromPersistenceKey(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        try {
            return AiPersonality.valueOf(key);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public String toPersistenceKey() {
        return name();
    }
}
