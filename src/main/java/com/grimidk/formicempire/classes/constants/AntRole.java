package com.grimidk.formicempire.classes.constants;

import javax.swing.ImageIcon;

public class AntRole {
    private final int id;
    private final AntType antType;
    private final String name;
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

    public String getName() {
        return name;
    }

    public ImageIcon getIcon() {
        return icon;
    }
}
