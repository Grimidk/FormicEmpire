package com.grimidk.formicempire.classes.constants;

public class MoonPhase {
    
    private int id;
    private String name;    
    private float tideMult;

    public MoonPhase(int id, String name, float tideMult) {
        this.id = id;
        this.name = name;
        this.tideMult = tideMult;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getTideMult() {
        return tideMult;
    }

    public void setTideMult(float tideMult) {
        this.tideMult = tideMult;
    }
}
