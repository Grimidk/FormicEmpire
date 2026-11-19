package com.grimidk.formicempire.classes.constants.critter.ant;

import java.util.List;

public enum AntModSlot {
    HEAD(0),
    TORSO(1),
    ABDOMEN(2),
    OTHER(3);

    public static final List<AntModSlot> ALL = List.of(values());

    private final int index;

    AntModSlot(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
