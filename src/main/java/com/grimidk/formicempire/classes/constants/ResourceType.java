package com.grimidk.formicempire.classes.constants;

import javax.swing.ImageIcon;

public class ResourceType {
    private final int id;
    private final String name;
    private final boolean isEdible;
    private final boolean isLiquid;
    private final ImageIcon icon;

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

    public boolean isIsEdible() {
        return isEdible;
    }

    public boolean isIsLiquid() {
        return isLiquid;
    }

    public ImageIcon getIcon() {
        return icon;
    }
}
