package com.grimidk.formicempire.classes.constants;

import javax.swing.ImageIcon;

public class Season {
    private final int id;
    private String name;    
    private float tempMult;
    private float humidityMult;
    private ImageIcon icon;

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

    public void setName(String name) {
        this.name = name;
    }

    public float getTempMult() {
        return tempMult;
    }

    public void setTempMult(float tempMult) {
        this.tempMult = tempMult;
    }

    public float getHumidityMult() {
        return humidityMult;
    }

    public void setHumidityMult(float humidityMult) {
        this.humidityMult = humidityMult;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
}
