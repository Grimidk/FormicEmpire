package com.grimidk.formicempire.classes;

public class ResourceType {
    
    private int id;
    private String name;
    private boolean isEdible;
    private boolean isLiquid;

    public ResourceType(int id, String name, boolean isEdible, boolean isLiquid) {
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
