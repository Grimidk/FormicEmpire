package com.grimidk.formicempire.classes.constants.world;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class Season extends Constant {
    private final float tempMult;
    private final float humidityMult;

    public Season(int id, String name, float tempMult, float humidityMult, ImageIcon icon) {
        super(id, name, icon);
        this.tempMult = tempMult;
        this.humidityMult = humidityMult;
    }

    public float getTempMult() {
        return tempMult;
    }

    public float getHumidityMult() {
        return humidityMult;
    }
}
