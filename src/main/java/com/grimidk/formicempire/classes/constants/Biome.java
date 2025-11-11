package com.grimidk.formicempire.classes.constants;

import javax.swing.ImageIcon;

public class Biome {
    private final int id;
    private final String name;
    private final int temperature;
    private final int humidity; // 0 -> 5
    private final ImageIcon icon;

    public Biome(int id, String name, int temperature, int humidity, ImageIcon icon) {
        this.id = id;
        this.name = name;
        this.temperature = temperature;
        this.humidity = humidity;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTemperature() {
        return temperature;
    }

    public int isIsHumid() {
        return humidity;
    }

    public ImageIcon getIcon() {
        return icon;
    }
}
