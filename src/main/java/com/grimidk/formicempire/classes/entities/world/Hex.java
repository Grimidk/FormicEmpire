package com.grimidk.formicempire.classes.entities.world;

import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.TimeOfDay;
import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

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
    private boolean island;
    private int elevation = 3;
    private int hydrology = 0;
    private int plateId = 0;

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

    public int getLocalHour(World world) {
        return world != null ? world.getLocalHour(this) : 0;
    }

    public TimeOfDay getLocalTimeOfDay(World world) {
        return world != null ? world.getTimeOfDay(this) : GameConstants.TIME_DAY;
    }

    public Weather getLocalWeather() { return localWeather; }

    public void setLocalWeather(Weather localWeather) { this.localWeather = localWeather;}

    public boolean isActive() { return isActive; }
    
    public void setActive(boolean isActive) { this.isActive = isActive; }

    public boolean isIsland() { return island; }

    public void setIsland(boolean island) { this.island = island; }

    public int getNonWaterResourceSourcesGenerated() {
        return nonWaterResourceSourcesGenerated;
    }

    public void setNonWaterResourceSourcesGenerated(int count) {
        this.nonWaterResourceSourcesGenerated = Math.max(0, count);
    }

    public int getResourceDepletionPercent() {
        return Math.min(100, nonWaterResourceSourcesGenerated / GameNumbers.HEX_RESOURCE_DEPLETION_SOURCES_PER_PERCENT);
    }

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

    private final boolean[] riverEdges = new boolean[6];
    private int continentId;
    private int waterDistance;
    private boolean coastal;

    public boolean hasRiverEdge(int direction) {
        if (direction < 0 || direction >= riverEdges.length) {
            return false;
        }
        return riverEdges[direction];
    }

    public void setRiverEdge(int direction, boolean river) {
        if (direction >= 0 && direction < riverEdges.length) {
            riverEdges[direction] = river;
        }
    }

    public boolean hasAnyRiver() {
        for (boolean r : riverEdges) {
            if (r) return true;
        }
        return false;
    }

    public boolean[] getRiverEdges() {
        return riverEdges;
    }

    public int directionTo(Hex other) {
        if (other == null) return -1;
        if (other == southEast) return 0;
        if (other == south) return 1;
        if (other == southWest) return 2;
        if (other == northWest) return 3;
        if (other == north) return 4;
        if (other == northEast) return 5;
        return -1;
    }

    public boolean hasRiverTo(Hex neighbor) {
        int dir = directionTo(neighbor);
        if (dir >= 0 && dir < 6) {
            return riverEdges[dir];
        }
        return false;
    }

    public Hex getNeighbor(int direction) {
        return switch (direction) {
            case 0 -> southEast;
            case 1 -> south;
            case 2 -> southWest;
            case 3 -> northWest;
            case 4 -> north;
            case 5 -> northEast;
            default -> null;
        };
    }

    public int getRiverEdgeCount() {
        int count = 0;
        for (boolean r : riverEdges) {
            if (r) count++;
        }
        return count;
    }

    public int getContinentId() {
        return continentId;
    }

    public void setContinentId(int continentId) {
        this.continentId = continentId;
    }

    public int getWaterDistance() {
        return waterDistance;
    }

    public void setWaterDistance(int waterDistance) {
        this.waterDistance = waterDistance;
    }

    public boolean isCoastal() {
        return coastal;
    }

    public void setCoastal(boolean coastal) {
        this.coastal = coastal;
    }

    public int getElevation() {
        return elevation;
    }

    public void setElevation(int elevation) {
        this.elevation = Math.max(0, Math.min(10, elevation));
    }

    public int getPlateId() {
        return plateId;
    }

    public void setPlateId(int plateId) {
        this.plateId = plateId;
    }

    public int getHydrology() {
        return hydrology;
    }

    public void setHydrology(int hydrology) {
        this.hydrology = hydrology;
    }
}