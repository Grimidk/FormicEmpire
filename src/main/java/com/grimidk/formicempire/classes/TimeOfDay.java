/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.grimidk.formicempire.classes;

/**
 *
 * @author juanmendezl
 */
public class TimeOfDay {
    
    private int id;
    private String name;
    private float tempMult;

    public TimeOfDay(int id, String name, float tempMult) {
        this.id = id;
        this.name = name;
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

    public float getTempMult() {
        return tempMult;
    }

    public void setTempMult(float tempMult) {
        this.tempMult = tempMult;
    }
    
    

}
