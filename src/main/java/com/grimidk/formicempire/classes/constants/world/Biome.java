package com.grimidk.formicempire.classes.constants.world;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class Biome extends Constant{
    private final int temperature;
    private final int humidity; // 0 -> 5
    private final float plantAbundance;
    private final float animalAbundance;
    private final float mineralAbundance;
    private final ImageIcon background;

    public Biome(int id, String name, int temperature, int humidity, float plantAbundance, float animalAbundance, float mineralAbundance, ImageIcon icon, ImageIcon background) {
        super(id, name, icon);
        this.temperature = temperature;
        this.humidity = humidity;
        this.plantAbundance = plantAbundance;
        this.animalAbundance = animalAbundance;
        this.mineralAbundance = mineralAbundance;
        this.background = background;
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
