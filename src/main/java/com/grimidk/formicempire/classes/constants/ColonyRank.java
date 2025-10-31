package com.grimidk.formicempire.classes.constants;

import javax.swing.ImageIcon;

public class ColonyRank {
    private final int id;
    private String name;
    private long population;
    private ImageIcon icon;

    public ColonyRank(int id, String name, long population) {
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

    public void setName(String name) {
        this.name = name;
    }

    public long getPopulation() {
        return population;
    }

    public void setPopulation(long population) {
        this.population = population;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
}
