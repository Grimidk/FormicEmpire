package com.grimidk.formicempire.classes.constants.misc;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class ColonyRank extends Constant {
    private final long population;

    public ColonyRank(int id, String name, long population, ImageIcon icon) {
        super(id, name, icon);
        this.population = population;
    }

    public long getPopulation() {
        return population;
    }
}
