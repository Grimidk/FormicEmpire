package com.grimidk.formicempire.classes.constants;

public class AntRoles {
    
    private final int id;
    private AntType antType;
    private String name;

    public AntRoles(int id, AntType antType, String name) {
        this.id = id;
        this.antType = antType;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public AntType getAntType() {
        return antType;
    }

    public void setAntType(AntType antType) {
        this.antType = antType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
