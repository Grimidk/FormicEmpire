package com.grimidk.formicempire.classes.constants.world;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

import java.awt.Color;
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
    private final String mapColorHex;
    private final Color mapColor;
    private List<Species> nativeBugs = List.of();
    private List<Species> nativeParasites = List.of();

    public Biome(int id, String name, int temperature, int humidity, float plantAbundance, float animalAbundance,
            float mineralAbundance, int difficulty, String mapColorHex, ImageIcon icon, ImageIcon background) {
        super(id, name, icon);
        this.temperature = temperature;
        this.humidity = humidity;
        this.plantAbundance = plantAbundance;
        this.animalAbundance = animalAbundance;
        this.mineralAbundance = mineralAbundance;
        this.difficulty = difficulty;
        this.mapColorHex = normalizeMapColorHex(mapColorHex);
        this.mapColor = new Color(Integer.parseInt(this.mapColorHex, 16));
        this.background = background;
    }

    public static String normalizeMapColorHex(String hex) {
        if (hex == null || hex.isBlank()) {
            return "808080";
        }
        String h = hex.trim();
        if (h.startsWith("#") || h.startsWith("0x") || h.startsWith("0X")) {
            h = h.startsWith("#") ? h.substring(1) : h.substring(2);
        }
        if (h.length() != 6) {
            throw new IllegalArgumentException("Expected RRGGBB hex color, got: " + hex);
        }
        return h.toLowerCase();
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

    public String getMapColorHex() {
        return mapColorHex;
    }

    public Color getMapColor() {
        return mapColor;
    }

    public int getTemperature() { return temperature; }
    public int isIsHumid() { return humidity; }
    public float getPlantAbundance() { return plantAbundance; }
    public float getAnimalAbundance() { return animalAbundance; }
    public float getMineralAbundance() { return mineralAbundance; }
}
