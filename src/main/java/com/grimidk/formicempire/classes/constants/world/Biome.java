package com.grimidk.formicempire.classes.constants.world;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class Biome extends Constant{
    private final int temperature;
    private final int humidity; // 0 -> 5

    public Biome(int id, String name, int temperature, int humidity, ImageIcon icon) {
        super(id, name, icon);
        this.temperature = temperature;
        this.humidity = humidity;
    }

    public int getTemperature() {
        return temperature;
    }

    public int isIsHumid() {
        return humidity;
    }
}
