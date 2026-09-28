package com.grimidk.formicempire.classes.constants.critter.ant;

import javax.swing.ImageIcon;

public class AntType extends AntClass {

    public AntType(int id, String name, float healtMult, float attackMult, float regenMult, float consumptionMult,
            float attackSpeedMult, float defenseMult, float speedMult, ImageIcon icon, String spriteName) {
        super(id, name, healtMult, attackMult, regenMult, consumptionMult, attackSpeedMult, defenseMult, speedMult,
                0, icon, spriteName);
    }

    public AntType(int id, String name, float healtMult, float attackMult, float regenMult, float consumptionMult,
            float attackSpeedMult, float defenseMult, float speedMult, int militaryWeight, ImageIcon icon,
            String spriteName) {
        super(id, name, healtMult, attackMult, regenMult, consumptionMult, attackSpeedMult, defenseMult, speedMult,
                militaryWeight, icon, spriteName);
    }
}
