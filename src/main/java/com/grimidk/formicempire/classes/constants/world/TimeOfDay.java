package com.grimidk.formicempire.classes.constants.world;

import java.awt.Color;
import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class TimeOfDay extends Constant {
    private final float tempMult;
    private final Color overlayColor;

    public TimeOfDay(int id, String name, float tempMult, Color overlayColor, ImageIcon icon) {
        super(id, name, icon);
        this.tempMult = tempMult;
        this.overlayColor = overlayColor;
    }

    public float getTempMult() {
        return tempMult;
    }

    public Color getOverlayColor() {
        return overlayColor;
    }
}
