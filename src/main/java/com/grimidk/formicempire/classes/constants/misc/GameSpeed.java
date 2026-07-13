package com.grimidk.formicempire.classes.constants.misc;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

public class GameSpeed extends Constant {

    public static final int ID_VERY_SLOW = 0;
    public static final int ID_SLOW = 1;
    public static final int ID_NORMAL = 2;
    public static final int ID_FAST = 3;
    public static final int ID_VERY_FAST = 4;
    public static final int ID_TURBO = 5;

    public static final int MIN_PLAYABLE_ID = ID_VERY_SLOW;
    public static final int MAX_PLAYABLE_ID = ID_TURBO;
    public static final int MAX_PLAYABLE_ID_WITHOUT_TURBO = ID_VERY_FAST;

    private final int delayMs;

    public GameSpeed(int id, String nameKey, int delayMs, ImageIcon icon) {
        super(id, nameKey, icon);
        this.delayMs = delayMs;
    }

    public int getDelayMs() {
        return delayMs;
    }

    public String getLabel() {
        return getName();
    }

    public static GameSpeed fromId(int id) {
        GameSpeed resolved = GameConstants.getGameSpeedById(id);
        return resolved != null ? resolved : GameConstants.SPEED_NORMAL;
    }

    public static int maxPlayableId(boolean allowTurbo) {
        return allowTurbo ? MAX_PLAYABLE_ID : MAX_PLAYABLE_ID_WITHOUT_TURBO;
    }

    public static GameSpeed step(GameSpeed current, int delta, boolean allowTurbo) {
        GameSpeed base = current != null ? current : GameConstants.SPEED_NORMAL;
        int minId = MIN_PLAYABLE_ID;
        int maxId = maxPlayableId(allowTurbo);
        int nextId = Math.max(minId, Math.min(maxId, base.getId() + delta));
        return fromId(nextId);
    }

    public static GameSpeed closestToDelayMs(int delayMs) {
        GameSpeed closest = GameConstants.SPEED_NORMAL;
        int minDiff = Integer.MAX_VALUE;
        for (GameSpeed speed : GameConstants.getGameSpeeds()) {
            int diff = Math.abs(speed.delayMs - delayMs);
            if (diff < minDiff) {
                minDiff = diff;
                closest = speed;
            }
        }
        return closest;
    }
}
