package com.grimidk.formicempire.classes.constants.critter.ant;

public enum AntModSlot {
    HEAD(0),
    TORSO(1),
    ABDOMEN(2),
    OTHER(3);

    private final int index;

    AntModSlot(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public AntSubtypeSlot toSubtypeSlot() {
        return switch (this) {
            case HEAD -> AntSubtypeSlot.HEAD;
            case TORSO -> AntSubtypeSlot.TORSO;
            case ABDOMEN -> AntSubtypeSlot.ABDOMEN;
            case OTHER -> AntSubtypeSlot.OTHER;
        };
    }

    public static AntModSlot fromSubtypeSlot(AntSubtypeSlot slot) {
        if (slot == null) {
            return null;
        }
        return switch (slot) {
            case HEAD -> HEAD;
            case TORSO -> TORSO;
            case ABDOMEN -> ABDOMEN;
            case OTHER -> OTHER;
        };
    }
}
