package com.grimidk.formicempire.classes.constants.ant;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

public final class AntSubtypeProfile {
    public static final int STANDARD_CODE = 1111;

    private final int code;

    private AntSubtypeProfile(int code) {
        this.code = code;
    }

    public static AntSubtypeProfile standard() {
        return new AntSubtypeProfile(STANDARD_CODE);
    }

    public static AntSubtypeProfile fromCode(int code) {
        if (code < 1111 || code > 9999) {
            return standard();
        }
        return new AntSubtypeProfile(code);
    }

    public static AntSubtypeProfile of(int head, int torso, int abdomen, int other) {
        return fromCode(head * 1000 + torso * 100 + abdomen * 10 + other);
    }

    public int getCode() {
        return code;
    }

    public int getDigit(AntSubtypeSlot slot) {
        return switch (slot) {
            case HEAD -> code / 1000;
            case TORSO -> (code / 100) % 10;
            case ABDOMEN -> (code / 10) % 10;
            case OTHER -> code % 10;
        };
    }

    public AntSubtype getSubtype(AntSubtypeSlot slot) {
        return GameConstants.getAntSubtypeBySlotAndDigit(slot, getDigit(slot));
    }

    public boolean isStandard() {
        return code == STANDARD_CODE;
    }

    public int countActiveSubtypes() {
        int count = 0;
        for (AntSubtypeSlot slot : AntSubtypeSlot.values()) {
            AntSubtype subtype = getSubtype(slot);
            if (subtype != null && !subtype.isNone()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AntSubtypeProfile other)) {
            return false;
        }
        return code == other.code;
    }

    @Override
    public int hashCode() {
        return code;
    }
}
