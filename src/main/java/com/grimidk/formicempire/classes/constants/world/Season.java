package com.grimidk.formicempire.classes.constants.world;

import javax.swing.ImageIcon;

public class Season {
    private final int id;
    private final String name;    
    private final float tempMult;
    private final float humidityMult;
    private final ImageIcon icon;

    public Season(int id, String name, float tempMult, float humidityMult, ImageIcon icon) {
        this.id = id;
        this.name = name;
        this.tempMult = tempMult;
        this.humidityMult = humidityMult;
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

    public float getHumidityMult() {
        return humidityMult;
    }

    public ImageIcon getIcon() {
        return icon;
    }
}
