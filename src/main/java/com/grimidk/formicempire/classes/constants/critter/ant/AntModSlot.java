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
}
