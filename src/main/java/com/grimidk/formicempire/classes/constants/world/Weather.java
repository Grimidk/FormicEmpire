package com.grimidk.formicempire.classes.constants.world;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class Weather extends Constant {
    private final int humidMod;
    private final float tempMult;

    public Weather(int id, String name, int humidMod, float tempMult, ImageIcon icon) {
        super(id, name, icon);
        this.humidMod = humidMod;
        this.tempMult = tempMult;
    }

    public int getHumidMult() {
        return humidMod;
    }

    public float getTempMult() {
        return tempMult;
    }
}
