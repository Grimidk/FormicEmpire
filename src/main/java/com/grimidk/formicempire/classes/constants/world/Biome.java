package com.grimidk.formicempire.classes.constants.world;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

import java.util.List;
import javax.swing.ImageIcon;

public class Biome extends Constant{
    private final int temperature;
    private final int humidity; // 0 -> 5
    private final float plantAbundance;
    private final float animalAbundance;
    private final float mineralAbundance;
    private final int difficulty;
    private final ImageIcon background;
    private List<Species> nativeBugs = List.of();
    private List<Species> nativeParasites = List.of();

    public Biome(int id, String name, int temperature, int humidity, float plantAbundance, float animalAbundance,
            float mineralAbundance, int difficulty, ImageIcon icon, ImageIcon background) {
        super(id, name, icon);
        this.temperature = temperature;
        this.humidity = humidity;
        this.plantAbundance = plantAbundance;
        this.animalAbundance = animalAbundance;
        this.mineralAbundance = mineralAbundance;
        this.difficulty = difficulty;
        this.background = background;
    }

    public void setNativeBugs(List<Species> nativeBugs) {
        this.nativeBugs = nativeBugs == null || nativeBugs.isEmpty()
                ? List.of()
                : List.copyOf(nativeBugs);
    }

    public List<Species> getNativeBugs() {
        return nativeBugs;
    }

    public void setNativeParasites(List<Species> nativeParasites) {
        this.nativeParasites = nativeParasites == null || nativeParasites.isEmpty()
                ? List.of()
                : List.copyOf(nativeParasites);
    }

    public List<Species> getNativeParasites() {
        return nativeParasites;
    }

    public boolean hasNativeParasite(Species type) {
        return type != null && nativeParasites.contains(type);
    }

    public boolean isDry() {
        return humidity <= GameNumbers.BIOME_DRY_HUMIDITY_MAX;
    }

    public boolean isCold() {
        return temperature <= GameNumbers.BIOME_COLD_TEMP_MAX;
    }

    public boolean isHot() {
        return temperature >= 28;
    }

    public int getDifficulty() {
        return difficulty;
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
