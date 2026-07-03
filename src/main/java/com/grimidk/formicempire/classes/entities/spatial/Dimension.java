package com.grimidk.formicempire.classes.entities.spatial;

public class Dimension {
    private final int id;
    private final String name;
    private final boolean isActiveAble;

    public Dimension(int id, String name, boolean isActiveAble) {
        this.id = id;
        this.name = name;
        this.isActiveAble = isActiveAble;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public boolean isActiveAble() { return isActiveAble; }
}
