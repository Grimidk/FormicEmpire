package com.grimidk.formicempire.classes.constants;

import javax.swing.ImageIcon;

public class TimeOfDay {
    private final int id;
    private String name;
    private float tempMult;
    private ImageIcon icon;

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

    public void setName(String name) {
        this.name = name;
    }

    public float getTempMult() {
        return tempMult;
    }

    public void setTempMult(float tempMult) {
        this.tempMult = tempMult;
    }
    
    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

}
