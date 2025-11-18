package com.grimidk.formicempire.classes.constants.ant;

import javax.swing.ImageIcon;

public class AntStatus {
    private final int id;
    private final String name;
    private final ImageIcon icon;

    public AntStatus(int id, String name, ImageIcon icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ImageIcon getIcon() {
        return icon;
    }
}
