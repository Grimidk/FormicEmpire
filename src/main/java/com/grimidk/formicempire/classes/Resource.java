/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.grimidk.formicempire.classes;

/**
 *
 * @author juanmendezl
 */
public class Resource {
    
    private int id;
    private String name;
    private int amount;
    private boolean isEdible;
    private boolean isLiquid;

    public Resource(int id, String name, boolean isEdible, boolean isLiquid) {
        this.id = id;
        this.name = name;
        this.isEdible = isEdible;
        this.isLiquid = isLiquid;
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

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isIsEdible() {
        return isEdible;
    }

    public void setIsEdible(boolean isEdible) {
        this.isEdible = isEdible;
    }

    public boolean isIsLiquid() {
        return isLiquid;
    }

    public void setIsLiquid(boolean isLiquid) {
        this.isLiquid = isLiquid;
    }
    
    
    
}
