package com.grimidk.formicempire.classes;

public class Season {
    
    private int id;
    private String name;    
    private float tempMult;
    private float humidityMult;

    public Season(int id, String name, float tempMult, float humidityMult) {
        this.id = id;
        this.name = name;
        this.tempMult = tempMult;
        this.humidityMult = humidityMult;
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


}
