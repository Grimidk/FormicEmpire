package com.grimidk.formicempire.classes.constants.misc;

import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public enum GameSpeed {
    VERY_SLOW(0, LanguageStrings.UI_SPEED_VERY_SLOW, 1000),
    SLOW(1, LanguageStrings.UI_SPEED_SLOW, 500),
    NORMAL(2, LanguageStrings.UI_SPEED_NORMAL, 250),
    FAST(3, LanguageStrings.UI_SPEED_FAST, 100),
    VERY_FAST(4, LanguageStrings.UI_SPEED_VERY_FAST, 50),
    TURBO(5, LanguageStrings.UI_SPEED_TURBO, 1);

    public static final int ID_VERY_SLOW = 0;
    public static final int ID_SLOW = 1;
    public static final int ID_NORMAL = 2;
    public static final int ID_FAST = 3;
    public static final int ID_VERY_FAST = 4;
    public static final int ID_TURBO = 5;

    public static final int MIN_PLAYABLE_ID = ID_VERY_SLOW;
    public static final int MAX_PLAYABLE_ID = ID_TURBO;
    public static final int MAX_PLAYABLE_ID_WITHOUT_TURBO = ID_VERY_FAST;

    private final int id;
    private final String labelKey;
    private final int delayMs;

    GameSpeed(int id, String labelKey, int delayMs) {
        this.id = id;
        this.labelKey = labelKey;
        this.delayMs = delayMs;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return LanguageStrings.get(labelKey);
    }

    public int getDelayMs() {
        return delayMs;
    }

    public static GameSpeed fromId(int id) {
        for (GameSpeed speed : values()) {
            if (speed.id == id) {
                return speed;
            }
        }
        return NORMAL;
    }

    public static int maxPlayableId(boolean allowTurbo) {
        return allowTurbo ? MAX_PLAYABLE_ID : MAX_PLAYABLE_ID_WITHOUT_TURBO;
    }

    /** Move one speed id up or down; clamps to playable range. Pause is not part of this ladder. */
    public static GameSpeed step(GameSpeed current, int delta, boolean allowTurbo) {
        GameSpeed base = current != null ? current : NORMAL;
        int minId = MIN_PLAYABLE_ID;
        int maxId = maxPlayableId(allowTurbo);
        int nextId = Math.max(minId, Math.min(maxId, base.id + delta));
        return fromId(nextId);
    }

    public static GameSpeed closestToDelayMs(int delayMs) {
        GameSpeed closest = NORMAL;
        int minDiff = Integer.MAX_VALUE;
        for (GameSpeed speed : values()) {
            int diff = Math.abs(speed.delayMs - delayMs);
            if (diff < minDiff) {
                minDiff = diff;
                closest = speed;
            }
        }
        return closest;
    }
}
