package com.grimidk.formicempire.classes.constants;

import javax.swing.ImageIcon;

public class MoonPhase {
    private final int id;
    private String name;    
    private float tideMult;
    private ImageIcon icon;

    public MoonPhase(int id, String name, float tideMult, ImageIcon icon) {
        this.id = id;
        this.name = name;
        this.tideMult = tideMult;
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

    public float getTideMult() {
        return tideMult;
    }

    public void setTideMult(float tideMult) {
        this.tideMult = tideMult;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
}
