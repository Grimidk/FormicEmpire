package com.grimidk.formicempire.classes.constants.world;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class TimeOfDay extends Constant {
    private final float tempMult;

    public TimeOfDay(int id, String name, float tempMult, ImageIcon icon) {
        super(id, name, icon);
        this.tempMult = tempMult;
    }

    public float getTempMult() {
        return tempMult;
    }
}
