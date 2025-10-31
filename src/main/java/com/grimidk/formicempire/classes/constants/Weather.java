package com.grimidk.formicempire.classes.constants;

import javax.swing.ImageIcon;

public class Weather {
    private final int id;
    private String name;
    private int humidMod;
    private float tempMult;
    private ImageIcon icon;

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

    public void setName(String name) {
        this.name = name;
    }

    public int getHumidMult() {
        return humidMod;
    }

    public void setHumidMult(int humidMod) {
        this.humidMod = humidMod;
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
