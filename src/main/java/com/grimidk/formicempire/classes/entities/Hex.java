package com.grimidk.formicempire.classes.entities;

import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

public class Hex {
    private Biome biome;
    private Colony colony;
    private Hex north;
    private Hex northWest;
    private Hex northEast;
    private Hex south;
    private Hex southWest;
    private Hex southEast;
    
    private int q;
    private int r;
    
    private int timeOffset; 
    private Weather localWeather;
    private boolean isActive;

    /** Counts non-water overworld sources spawned on this hex (scouting); drives {@link #getResourceDepletionPercent()}. */
    private int nonWaterResourceSourcesGenerated;

    public Hex(Biome biome, Colony colony, Hex north, Hex northWest, Hex northEast, Hex south, Hex southWest, Hex southEast) {
        this.biome = biome;
        this.colony = colony;
        this.north = north;
        this.northWest = northWest;
        this.northEast = northEast;
        this.south = south;
        this.southWest = southWest;
        this.southEast = southEast;
        this.isActive = false;
    }
    
    public Hex(){
        this.isActive = false;
    }

    public Biome getBiome() { return biome; }

    public void setBiome(Biome biome) { this.biome = biome; }

    public Colony getColony() { return colony; }

    public void setColony(Colony colony) { this.colony = colony; }

    public Hex getNorth() { return north; }

    public void setNorth(Hex north) { this.north = north; }

    public Hex getNorthWest() { return northWest; }

    public void setNorthWest(Hex northWest) { this.northWest = northWest; }

    public Hex getNorthEast() { return northEast; }

    public void setNorthEast(Hex northEast) { this.northEast = northEast; }

    public Hex getSouth() { return south; }

    public void setSouth(Hex south) { this.south = south; }

    public Hex getSouthWest() { return southWest; }

    public void setSouthWest(Hex southWest) { this.southWest = southWest; }

    public Hex getSouthEast() { return southEast; }

    public void setSouthEast(Hex southEast) { this.southEast = southEast; }

    /** Six hex-grid neighbors (may contain nulls at map edges). */
    public Hex[] getAdjacentNeighbors() {
        return new Hex[] {
            north, northWest, northEast, south, southWest, southEast
        };
    }

    public int getQ() { return q; }

    public void setQ(int q) { this.q = q; }

    public int getR() { return r; }

    public void setR(int r) { this.r = r; }

    public int getTimeOffset() { return timeOffset;}

    public void setTimeOffset(int timeOffset) { this.timeOffset = timeOffset; }

    public Weather getLocalWeather() { return localWeather; }

    public void setLocalWeather(Weather localWeather) { this.localWeather = localWeather;}

    public boolean isActive() { return isActive; }
    
    public void setActive(boolean isActive) { this.isActive = isActive; }

    public int getNonWaterResourceSourcesGenerated() {
        return nonWaterResourceSourcesGenerated;
    }

    public void setNonWaterResourceSourcesGenerated(int count) {
        this.nonWaterResourceSourcesGenerated = Math.max(0, count);
    }

    /**
     * 0–100% depletion derived from non-water sources spawned; +1% per {@link GameConstants#HEX_RESOURCE_DEPLETION_SOURCES_PER_PERCENT} sources.
     */
    public int getResourceDepletionPercent() {
        return Math.min(100, nonWaterResourceSourcesGenerated / GameConstants.HEX_RESOURCE_DEPLETION_SOURCES_PER_PERCENT);
    }

    /**
     * Raw depletion clamped to {@code maxPercent} (e.g. {@link GameConstants#HEX_SUSTAIN_MAX_DEPLETION_PCT} with sustainability upgrade).
     */
    public int getResourceDepletionPercentCapped(int maxPercent) {
        int cap = Math.min(100, Math.max(0, maxPercent));
        return Math.min(getResourceDepletionPercent(), cap);
    }

    public void recordGeneratedResourceSource(ResourceType type) {
        if (type == GameConstants.RESOURCE_WATER) {
            return;
        }
        nonWaterResourceSourcesGenerated++;
    }
}