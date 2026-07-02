package com.grimidk.formicempire.classes.constants.misc;

import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

public enum GameSpeed {
    VERY_SLOW(LanguageStrings.UI_SPEED_VERY_SLOW, 1000),
    SLOW(LanguageStrings.UI_SPEED_SLOW, 500),
    NORMAL(LanguageStrings.UI_SPEED_NORMAL, 250),
    FAST(LanguageStrings.UI_SPEED_FAST, 100),
    VERY_FAST(LanguageStrings.UI_SPEED_VERY_FAST, 50),
    TURBO(LanguageStrings.UI_SPEED_TURBO, 1);

    private final String labelKey;
    private final int delayMs;

    GameSpeed(String labelKey, int delayMs) {
        this.labelKey = labelKey;
        this.delayMs = delayMs;
    }

    public String getLabel() { return LanguageStrings.get(labelKey); }
    public int getDelayMs() { return delayMs; }
    
    public static GameSpeed getMaxSpeed(boolean allowTurbo) {
        return allowTurbo ? TURBO : VERY_FAST;
    }

    public static GameSpeed getNext(GameSpeed current) {
        if (current == TURBO) {
            return TURBO;
        }
        return values()[current.ordinal() + 1];
    }

    /** Stops at max speed (turbo when allowed, otherwise very fast). */
    public static GameSpeed getNext(GameSpeed current, boolean allowTurbo) {
        GameSpeed max = getMaxSpeed(allowTurbo);
        if (current == max) {
            return current;
        }
        GameSpeed next = values()[current.ordinal() + 1];
        if (next == TURBO && !allowTurbo) {
            return VERY_FAST;
        }
        return next;
    }
    
    public static GameSpeed getPrevious(GameSpeed current) {
        if (current == VERY_SLOW) {
            return VERY_SLOW;
        }
        return values()[current.ordinal() - 1];
    }
}
