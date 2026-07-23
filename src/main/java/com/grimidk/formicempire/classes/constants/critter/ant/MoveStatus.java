package com.grimidk.formicempire.classes.constants.critter.ant;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class MoveStatus extends Constant {
    private final float speedMult;
   
    public MoveStatus(int id, String name, float speedMult, ImageIcon icon) {
        super(id, name, icon);
        this.speedMult = speedMult;
    }

    public float getSpeedMult() {
        return speedMult;
    }
}
