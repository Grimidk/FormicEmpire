package com.grimidk.formicempire.classes.constants.world;

import javax.swing.ImageIcon;

public class Weather {
    private final int id;
    private final String name;
    private final int humidMod;
    private final float tempMult;
    private final ImageIcon icon;

    public Weather(int id, String name, int humidMod, float tempMult, ImageIcon icon) {
        this.id = id;
        this.name = name;
        this.humidMod = humidMod;
        this.tempMult = tempMult;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public int getHumidMult() {
        return humidMod;
    }

    public float getTempMult() {
        return tempMult;
    }
    
    public ImageIcon getIcon() {
        return icon;
    }
}
