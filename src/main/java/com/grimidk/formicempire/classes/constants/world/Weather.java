package com.grimidk.formicempire.classes.constants.world;

import java.awt.Color;
import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class Weather extends Constant {
    private final int humidMod;
    private final float tempMult;
    private final Color overlayColor;

    public Weather(int id, String name, int humidMod, float tempMult, Color overlayColor, ImageIcon icon) {
        super(id, name, icon);
        this.humidMod = humidMod;
        this.tempMult = tempMult;
        this.overlayColor = overlayColor;
    }

    public int getHumidMult() {
        return humidMod;
    }

    public float getTempMult() {
        return tempMult;
    }

    public Color getOverlayColor() {
        return overlayColor;
    }
}
