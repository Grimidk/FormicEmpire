package com.grimidk.formicempire.classes.constants.world;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class Humidity extends Constant {
    private final int humidityLevel;

    public Humidity(int id, String name, int humidityLevel, ImageIcon icon) {
        super(id, name, null);
        this.humidityLevel = humidityLevel;
    }
    
    public int getHumidityLevel() {
        return humidityLevel;
    }
}
