package com.grimidk.formicempire.classes.constants;

import javax.swing.ImageIcon;

public class ResourceType {
    private final int id;
    private String name;
    private boolean isEdible;
    private boolean isLiquid;
    private ImageIcon icon;

    public ResourceType(int id, String name, boolean isEdible, boolean isLiquid, ImageIcon icon) {
        this.id = id;
        this.name = name;
        this.isEdible = isEdible;
        this.isLiquid = isLiquid;
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
    
    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
}
