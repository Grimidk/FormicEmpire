package com.grimidk.formicempire.classes.constants;

import javax.swing.ImageIcon;

public class Biome {
    private final int id;
    private String name;
    private int temperature;
    private int humidity; // 0 -> 5
    private ImageIcon icon;

    public Biome(int id, String name, int temperature, int humidity) {
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

    public void setName(String name) {
        this.name = name;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int isIsHumid() {
        return humidity;
    }

    public void setIsHumid(int humidity) {
        this.humidity = humidity;
    }
    
    
    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
}
