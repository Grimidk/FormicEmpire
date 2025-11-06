package com.grimidk.formicempire.classes.constants;

import javax.swing.ImageIcon;

public class AntStatus {
    private final int id;
    private final String name;
    private ImageIcon icon;

    public AntStatus(int id, String name) {
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

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
    
}
