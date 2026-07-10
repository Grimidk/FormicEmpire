package com.grimidk.formicempire.classes.constants.ant;

public enum AntSubtypeSlot {
    HEAD(0),
    TORSO(1),
    ABDOMEN(2),
    OTHER(3);

    private final int index;

    AntSubtypeSlot(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
