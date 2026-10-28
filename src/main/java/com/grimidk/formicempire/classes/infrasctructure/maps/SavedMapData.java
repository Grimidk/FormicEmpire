package com.grimidk.formicempire.classes.infrasctructure.maps;

import java.util.ArrayList;
import java.util.List;

public class SavedMapData {
    public String mapName;
    public long createdAt;
    public String version;
    public int worldWidth;
    public int worldHeight;
    public boolean hasIceCaps;
    public double iceCapPolarLimit;
    public double continentalLandPercentage;
    public int targetContinentCount;
    public int minIslandCount;
    public int tectonicPlateCount;
    public int continentCount;
    public int islandCount;
    public List<SavedMapHex> hexes = new ArrayList<>();
    public List<SavedMapPlate> tectonicPlates = new ArrayList<>();
    public List<SavedMapFault> faultLines = new ArrayList<>();
    public List<SavedMapContinent> continents = new ArrayList<>();

    public static class SavedMapHex {
        public int q;
        public int r;
        public int biomeId;
        public int elevation;
        public int hydrology;
        public int timeOffset;
        public int weatherId;
        public int continentId;
        public boolean island;
        public int waterDistance;
        public boolean coastal;
        public boolean[] riverEdges = new boolean[6];
        public int nonWaterResourceSourcesGenerated;
        public int plateId;
    }

    public static class SavedMapPlate {
        public int id;
        public int centerQ;
        public int centerR;
        public double driftAngle;
        public double driftSpeed;
        public boolean oceanic;
        public List<int[]> hexCoords = new ArrayList<>();
    }

    public static class SavedMapFault {
        public String type;
        public int intensity = 1;
        public double upliftFactor;
        public List<int[]> hexesA = new ArrayList<>();
        public List<int[]> hexesB = new ArrayList<>();
    }

    public static class SavedMapContinent {
        public int id;
        public String name;
        public int coreRadius;
    }
}
