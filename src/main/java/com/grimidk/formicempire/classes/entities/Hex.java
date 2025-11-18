package com.grimidk.formicempire.classes.entities;

import com.grimidk.formicempire.classes.constants.world.Biome;

public class Hex {
    
    private Biome biome;
    private Colony colony;
    private Hex north;
    private Hex northWest;
    private Hex northEast;
    private Hex south;
    private Hex southWest;
    private Hex southEast;

    public Hex(Biome biome, Colony colony, Hex north, Hex northWest, Hex northEast, Hex south, Hex southWest, Hex southEast) {
        this.biome = biome;
        this.colony = colony;
        this.north = north;
        this.northWest = northWest;
        this.northEast = northEast;
        this.south = south;
        this.southWest = southWest;
        this.southEast = southEast;
    }
    
    public Hex(){
        
    }

    public Biome getBiome() {
        return biome;
    }

    public void setBiome(Biome biome) {
        this.biome = biome;
    }

    public Colony getColony() {
        return colony;
    }

    public void setColony(Colony colony) {
        this.colony = colony;
    }

    public Hex getNorth() {
        return north;
    }

    public void setNorth(Hex north) {
        this.north = north;
    }

    public Hex getNorthWest() {
        return northWest;
    }

    public void setNorthWest(Hex northWest) {
        this.northWest = northWest;
    }

    public Hex getNorthEast() {
        return northEast;
    }

    public void setNorthEast(Hex northEast) {
        this.northEast = northEast;
    }

    public Hex getSouth() {
        return south;
    }

    public void setSouth(Hex south) {
        this.south = south;
    }

    public Hex getSouthWest() {
        return southWest;
    }

    public void setSouthWest(Hex southWest) {
        this.southWest = southWest;
    }

    public Hex getSouthEast() {
        return southEast;
    }

    public void setSouthEast(Hex southEast) {
        this.southEast = southEast;
    }
}
