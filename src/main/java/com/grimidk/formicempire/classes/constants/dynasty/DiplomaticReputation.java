package com.grimidk.formicempire.classes.constants.dynasty;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class DiplomaticReputation extends Constant {
    private final int minScore;

    public DiplomaticReputation(int id, String nameKey, int minScore, ImageIcon icon) {
        super(id, nameKey, icon);
        this.minScore = minScore;
    }

    public int getMinScore() {
        return minScore;
    }
}
