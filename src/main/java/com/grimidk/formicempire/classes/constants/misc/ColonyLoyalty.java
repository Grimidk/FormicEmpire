package com.grimidk.formicempire.classes.constants.misc;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class ColonyLoyalty extends Constant {
    private final int minScore;

    public ColonyLoyalty(int id, String nameKey, int minScore, ImageIcon icon) {
        super(id, nameKey, icon);
        this.minScore = minScore;
    }

    public int getMinScore() {
        return minScore;
    }
}
