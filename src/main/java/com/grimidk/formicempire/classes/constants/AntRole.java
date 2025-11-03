package com.grimidk.formicempire.classes.constants;

import javax.swing.ImageIcon;

public class AntRole {
    private final int id;
    private AntType antType;
    private String name;
    private ImageIcon icon;

    public AntRole(int id, AntType antType, String name) {
        this.id = id;
        this.antType = antType;
        this.name = name;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public AntType getAntType() {
        return antType;
    }

    public void setAntType(AntType antType) {
        this.antType = antType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
    
}
