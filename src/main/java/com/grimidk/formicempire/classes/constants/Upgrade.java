package com.grimidk.formicempire.classes.constants;

public class Upgrade {
    private final int id;
    private final String name;
    private final String flavorName;
    private final String description;
    private final Upgrade requirement;
    private final int cost;

    public Upgrade(int id, String name, String flavorName, String description, Upgrade requirement, int cost) {
        this.id = id;
        this.name = name;
        this.flavorName = flavorName;
        this.description = description;
        this.requirement = requirement;
        this.cost = cost;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFlavorName() {
        return flavorName;
    }

    public String getDescription() {
        return description;
    }

    public Upgrade getRequirement() {
        return requirement;
    }

    public int getCost() {
        return cost;
    }
}