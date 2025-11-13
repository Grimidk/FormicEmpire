package com.grimidk.formicempire.classes.constants;

import javax.swing.ImageIcon;

public class MoveStatus {
    private final int id;
    private final String name;
    private final float speedMult;
    private ImageIcon icon;
   
    public MoveStatus(int id, String name, float speedMult) {
        this.id = id;
        this.name = name;
        this.speedMult = speedMult;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getSpeedMult() {
        return speedMult;
    }

    public ImageIcon getIcon() {
        return icon;
    }
}
