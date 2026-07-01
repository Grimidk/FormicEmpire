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
    
    public static GameSpeed getNext(GameSpeed current) {
        int nextOrdinal = current.ordinal() + 1;
        if (nextOrdinal >= values().length) return values()[0];
        return values()[nextOrdinal];
    }

    /** Skips turbo when disabled; wraps VERY_FAST to VERY_SLOW instead of stalling on turbo. */
    public static GameSpeed getNext(GameSpeed current, boolean allowTurbo) {
        GameSpeed next = getNext(current);
        if (next == TURBO && !allowTurbo) {
            return VERY_SLOW;
        }
        return next;
    }
    
    public static GameSpeed getPrevious(GameSpeed current) {
        int prevOrdinal = current.ordinal() - 1;
        if (prevOrdinal < 0) return values()[values().length - 1];
        return values()[prevOrdinal];
    }
}
