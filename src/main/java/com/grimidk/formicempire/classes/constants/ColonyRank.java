package com.grimidk.formicempire.classes.constants;

import javax.swing.ImageIcon;

public class ColonyRank {
    private final int id;
    private final String name;
    private final long population;
    private final ImageIcon icon;

    public ColonyRank(int id, String name, long population, ImageIcon icon) {
        this.id = id;
        this.name = name;
        this.population = population;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getPopulation() {
        return population;
    }
    
    public ImageIcon getIcon() {
        return icon;
    }
}
