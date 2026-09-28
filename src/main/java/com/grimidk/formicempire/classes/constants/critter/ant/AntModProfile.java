package com.grimidk.formicempire.classes.constants.critter.ant;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

public final class AntModProfile {
    public static final int STANDARD_CODE = 1111;

    private final int code;

    private AntModProfile(int code) {
        this.code = code;
    }

    public static AntModProfile standard() {
        return new AntModProfile(STANDARD_CODE);
    }

    public static AntModProfile fromCode(int code) {
        if (code < 1111 || code > 9999) {
            return standard();
        }
        return new AntModProfile(code);
    }

    public static AntModProfile of(int head, int torso, int abdomen, int other) {
        return fromCode(head * 1000 + torso * 100 + abdomen * 10 + other);
    }

    public int getCode() {
        return code;
    }

    public int getDigit(AntModSlot slot) {
        return switch (slot) {
            case HEAD -> code / 1000;
            case TORSO -> (code / 100) % 10;
            case ABDOMEN -> (code / 10) % 10;
            case OTHER -> code % 10;
        };
    }

    public AntMod getMod(AntModSlot slot) {
        return GameConstants.getAntModBySlotAndDigit(slot, getDigit(slot));
    }

    public boolean isStandard() {
        return code == STANDARD_CODE;
    }

    public int countActiveMods() {
        int count = 0;
        for (AntModSlot slot : AntModSlot.values()) {
            AntMod mod = getMod(slot);
            if (mod != null && !mod.isNone()) {
                count++;
            }
        }
        return count;
    }

    public String buildSpriteFolder() {
        if (isStandard()) {
            return null;
        }
        StringBuilder folder = new StringBuilder();
        for (AntModSlot slot : GameConstants.getConfigurableModSlots()) {
            AntMod mod = getMod(slot);
            if (mod != null && !mod.isNone() && mod.hasSprite()) {
                if (folder.length() > 0) {
                    folder.append('-');
                }
                folder.append(mod.getSpriteFolder());
            }
        }
        return folder.isEmpty() ? null : folder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AntModProfile other)) {
            return false;
        }
        return code == other.code;
    }

    @Override
    public int hashCode() {
        return code;
    }

    public AntSubtypeProfile toSubtypeProfile() {
        return AntSubtypeProfile.fromCode(code);
    }

    public static AntModProfile fromSubtypeProfile(AntSubtypeProfile profile) {
        return profile != null ? fromCode(profile.getCode()) : standard();
    }
}
