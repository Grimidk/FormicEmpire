package com.grimidk.formicempire.classes;

public class Biome {
    
    private int id;
    private String name;
    private int temperature;
    private int humidity; // 0 -> 5

    public Biome(int id, String name, int temperature, int humidity) {
        this.id = id;
        this.name = name;
        this.temperature = temperature;
        this.humidity = humidity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
    
    
}
