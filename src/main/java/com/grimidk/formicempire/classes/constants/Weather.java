package com.grimidk.formicempire.classes.constants;

public class Weather {
    
    private int id;
    private String name;
    private int humidMod;
    private float tempMult;

    public Weather(int id, String name, int humidMod, float tempMult) {
        this.id = id;
        this.name = name;
        this.humidMod = humidMod;
        this.tempMult = tempMult;
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
    
}
