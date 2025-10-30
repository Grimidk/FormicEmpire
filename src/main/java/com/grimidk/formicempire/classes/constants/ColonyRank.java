package com.grimidk.formicempire.classes.constants;

public class ColonyRank {
    private final int id;
    private String name;
    private long population;

    public ColonyRank(int id, String name, long population) {
        this.id = id;
        this.name = name;
        this.population = population;
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
}
