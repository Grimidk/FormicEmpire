package com.grimidk.formicempire.classes.constants.world;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class Temperature extends Constant {
    private final int maxTemp;

    public Temperature(int id, String name, int maxTemp, ImageIcon icon) {
        super(id, name, icon);
        this.maxTemp = maxTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }
}
