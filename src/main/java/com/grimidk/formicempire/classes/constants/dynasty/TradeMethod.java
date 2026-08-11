package com.grimidk.formicempire.classes.constants.dynasty;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class TradeMethod extends Constant {
    
    private final float speedMult;
    private final float capacityMult;
    private final float dangerFactor;

    public TradeMethod(int id, String name, float speedMult, float capacityMult, float dangerFactor, ImageIcon icon) {
        super(id, name, icon);
        this.speedMult = speedMult;
        this.capacityMult = capacityMult;
        this.dangerFactor = dangerFactor;
    }

    public float getSpeedMult() {
        return speedMult;
    }

    public float getCapacityMult() {
        return capacityMult;
    }

    public float getDangerFactor() {
        return dangerFactor;
    }
}
