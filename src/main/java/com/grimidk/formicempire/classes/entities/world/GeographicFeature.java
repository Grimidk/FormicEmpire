package com.grimidk.formicempire.classes.entities.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

public class GeographicFeature implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum FeatureType {
        RIVER(LanguageStrings.GEOGRAPHIC_FEATURE_RIVER),
        MOUNTAIN_RANGE(LanguageStrings.GEOGRAPHIC_FEATURE_MOUNTAIN_RANGE),
        VALLEY(LanguageStrings.GEOGRAPHIC_FEATURE_VALLEY),
        LAKE_BASIN(LanguageStrings.GEOGRAPHIC_FEATURE_LAKE_BASIN),
        OCEAN(LanguageStrings.GEOGRAPHIC_FEATURE_OCEAN),
        PENINSULA(LanguageStrings.GEOGRAPHIC_FEATURE_PENINSULA),
        ARCHIPELAGO(LanguageStrings.GEOGRAPHIC_FEATURE_ARCHIPELAGO),
        ICE_CAP(LanguageStrings.GEOGRAPHIC_FEATURE_ICE_CAP),
        CONTINENT(LanguageStrings.GEOGRAPHIC_FEATURE_CONTINENT),
        TRENCH(LanguageStrings.GEOGRAPHIC_FEATURE_TRENCH);

        private final String labelKey;

        FeatureType(String labelKey) {
            this.labelKey = labelKey;
        }

        public String getLabelKey() {
            return labelKey;
        }
    }

    private final int id;
    private final FeatureType type;
    private String name;
    private String nameKey;
    private final List<Hex> hexes = new ArrayList<>();
    private final Set<Hex> hexSet = new HashSet<>();
    private double centroidQ;
    private double centroidR;

    public GeographicFeature(int id, FeatureType type, String name) {
        this(id, type, name, null);
    }

    public GeographicFeature(int id, FeatureType type, String name, String nameKey) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.nameKey = nameKey;
    }

    public int getId() {
        return id;
    }

    public FeatureType getType() {
        return type;
    }

    public String getName() {
        if (nameKey != null && !nameKey.isEmpty()) {
            return LanguageStrings.get(nameKey);
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameKey() {
        return nameKey;
    }

    public void setNameKey(String nameKey) {
        this.nameKey = nameKey;
    }

    public List<Hex> getHexes() {
        return Collections.unmodifiableList(hexes);
    }

    public void addHex(Hex hex) {
        if (hex != null && hexSet.add(hex)) {
            hexes.add(hex);
        }
    }

    public boolean containsHex(Hex hex) {
        return hex != null && hexSet.contains(hex);
    }

    public int size() {
        return hexes.size();
    }

    public double getCentroidQ() {
        return centroidQ;
    }

    public double getCentroidR() {
        return centroidR;
    }

    public void recalculateCentroid(int worldWidth) {
        if (hexes.isEmpty()) {
            this.centroidQ = 0;
            this.centroidR = 0;
            return;
        }
        double sumR = 0;
        double sumCol = 0;
        int refCol = hexes.get(0).getQ() + (hexes.get(0).getR() >> 1);

        for (Hex h : hexes) {
            int r = h.getR();
            int col = h.getQ() + (r >> 1);
            int diffCol = col - refCol;
            if (worldWidth > 0) {
                if (diffCol > worldWidth / 2) diffCol -= worldWidth;
                else if (diffCol < -worldWidth / 2) diffCol += worldWidth;
            }
            sumCol += (refCol + diffCol);
            sumR += r;
        }

        double avgCol = sumCol / hexes.size();
        this.centroidR = sumR / hexes.size();
        if (worldWidth > 0) {
            avgCol = ((avgCol % worldWidth) + worldWidth) % worldWidth;
            if (avgCol >= worldWidth / 2.0) {
                avgCol -= worldWidth;
            }
        }
        this.centroidQ = avgCol - (((int) Math.round(centroidR)) >> 1);
    }

    public Hex getCentroidHex() {
        if (hexes.isEmpty()) {
            return null;
        }
        Hex closest = null;
        int minDistance = Integer.MAX_VALUE;
        int targetQ = (int) Math.round(centroidQ);
        int targetR = (int) Math.round(centroidR);

        for (Hex h : hexes) {
            int dist = GameNumbers.axialHexDistance(h.getQ(), h.getR(), targetQ, targetR);
            if (dist < minDistance) {
                minDistance = dist;
                closest = h;
            }
        }
        return closest != null ? closest : hexes.get(0);
    }
}
