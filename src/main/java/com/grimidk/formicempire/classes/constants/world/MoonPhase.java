package com.grimidk.formicempire.classes.constants.world;

import javax.swing.ImageIcon;

public class MoonPhase {
    private final int id;
    private final String name;    
    private final float tideMult;
    private final ImageIcon icon;

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

    public float getTideMult() {
        return tideMult;
    }

    public ImageIcon getIcon() {
        return icon;
    }
}
