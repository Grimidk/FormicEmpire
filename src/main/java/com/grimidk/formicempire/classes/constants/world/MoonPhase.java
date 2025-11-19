package com.grimidk.formicempire.classes.constants.world;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class MoonPhase extends Constant {
    private final float tideMult;

    public MoonPhase(int id, String name, float tideMult, ImageIcon icon) {
        super(id, name, icon);
        this.tideMult = tideMult;
    }

    public float getTideMult() {
        return tideMult;
    }
}
