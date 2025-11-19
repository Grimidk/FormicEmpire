package com.grimidk.formicempire.classes.constants.misc;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class ResourceType extends Constant {
    private final boolean isEdible;
    private final boolean isLiquid;

    public ResourceType(int id, String name, boolean isEdible, boolean isLiquid, ImageIcon icon) {
        super(id, name, icon);
        this.isEdible = isEdible;
        this.isLiquid = isLiquid;
    }

    public boolean isIsEdible() {
        return isEdible;
    }

    public boolean isIsLiquid() {
        return isLiquid;
    }
}
