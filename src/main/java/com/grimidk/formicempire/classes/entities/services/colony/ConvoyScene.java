package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.misc.TradeMethod;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ConvoyScene {

    public static final int MAX_VISUAL_ANTS = 120;

    public enum BackgroundKind {
        LAND_BIOME,
        SEA,
        SKY,
        TUNNEL
    }

    private final String originName;
    private final String destinationName;
    private final TradeMethod method;
    private final BackgroundKind backgroundKind;
    private final Biome landBiome;
    private final Species species;
    private final Map<AntType, Integer> typeCounts;
    private final boolean returning;
    private final float legProgress;
    private final int remainingHours;
    private final int totalHours;
    private final boolean available;

    public ConvoyScene(String originName, String destinationName, TradeMethod method, BackgroundKind backgroundKind,
            Biome landBiome, Species species, Map<AntType, Integer> typeCounts, boolean returning, float legProgress,
            int remainingHours, int totalHours, boolean available) {
        this.originName = originName != null ? originName : "";
        this.destinationName = destinationName != null ? destinationName : "";
        this.method = method;
        this.backgroundKind = backgroundKind != null ? backgroundKind : BackgroundKind.LAND_BIOME;
        this.landBiome = landBiome;
        this.species = species != null ? species : GameConstants.SPECIES_OMNI;
        this.typeCounts = typeCounts == null
                ? Map.of()
                : Collections.unmodifiableMap(new HashMap<>(typeCounts));
        this.returning = returning;
        this.legProgress = Math.max(0f, Math.min(1f, legProgress));
        this.remainingHours = Math.max(0, remainingHours);
        this.totalHours = Math.max(1, totalHours);
        this.available = available;
    }

    public String getOriginName() {
        return originName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public TradeMethod getMethod() {
        return method;
    }

    public BackgroundKind getBackgroundKind() {
        return backgroundKind;
    }

    public Biome getLandBiome() {
        return landBiome;
    }

    public Species getSpecies() {
        return species;
    }

    public Map<AntType, Integer> typeCounts() {
        return typeCounts;
    }

    public boolean isReturning() {
        return returning;
    }

    public float getLegProgress() {
        return legProgress;
    }

    public int getRemainingHours() {
        return remainingHours;
    }

    public int getTotalHours() {
        return totalHours;
    }

    public boolean isAvailable() {
        return available;
    }
}
