package com.grimidk.formicempire.classes.constants;

import javax.swing.ImageIcon;

public class TimeOfDay {
    private final int id;
    private final String name;
    private final float tempMult;
    private final ImageIcon icon;

    public TimeOfDay(int id, String name, float tempMult, ImageIcon icon) {
        this.id = id;
        this.name = name;
        this.tempMult = tempMult;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getTempMult() {
        return tempMult;
    }

    public ImageIcon getIcon() {
        return icon;
    }
}
