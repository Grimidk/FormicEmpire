package com.grimidk.formicempire.classes.constants.world;

import java.util.List;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.constants.misc.BugType;

public class Biome extends Constant{
    private final int temperature;
    private final int humidity; // 0 -> 5
    private final float plantAbundance;
    private final float animalAbundance;
    private final float mineralAbundance;
    private final ImageIcon background;
    private List<BugType> nativeBugs = List.of();

    public Biome(int id, String name, int temperature, int humidity, float plantAbundance, float animalAbundance, float mineralAbundance, ImageIcon icon, ImageIcon background) {
        super(id, name, icon);
        this.temperature = temperature;
        this.humidity = humidity;
        this.plantAbundance = plantAbundance;
        this.animalAbundance = animalAbundance;
        this.mineralAbundance = mineralAbundance;
        this.background = background;
    }

    public void setNativeBugs(List<BugType> nativeBugs) {
        this.nativeBugs = nativeBugs == null || nativeBugs.isEmpty()
                ? List.of()
                : List.copyOf(nativeBugs);
    }

    public List<BugType> getNativeBugs() {
        return nativeBugs;
    }

    public boolean isDry() {
        return humidity <= 1;
    }

    public boolean isCold() {
        return temperature <= 15;
    }

    public ImageIcon getBackground() {
        return background;
    }

    public int getTemperature() { return temperature; }
    public int isIsHumid() { return humidity; }
    public float getPlantAbundance() { return plantAbundance; }
    public float getAnimalAbundance() { return animalAbundance; }
    public float getMineralAbundance() { return mineralAbundance; }
}
