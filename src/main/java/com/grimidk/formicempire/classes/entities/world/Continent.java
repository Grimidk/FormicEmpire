package com.grimidk.formicempire.classes.entities.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class Continent implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private String name;
    private String nameKey;
    private boolean archipelago;
    private boolean iceCap;
    private final List<Hex> hexes = new ArrayList<>();
    private final Set<Hex> hexSet = new HashSet<>();
    private final List<Hex> populatedHexes = new ArrayList<>();

    public Continent(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Continent(int id, String name, String nameKey) {
        this.id = id;
        this.name = name;
        this.nameKey = nameKey;
    }

    public int getId() {
        return id;
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

    public boolean isArchipelago() {
        return archipelago;
    }

    public void setArchipelago(boolean archipelago) {
        this.archipelago = archipelago;
    }

    public boolean isIceCap() {
        return iceCap;
    }

    public void setIceCap(boolean iceCap) {
        this.iceCap = iceCap;
    }

    public List<Hex> getHexes() {
        return Collections.unmodifiableList(hexes);
    }

    public boolean containsHex(Hex hex) {
        return hex != null && hexSet.contains(hex);
    }

    public void addHex(Hex hex) {
        if (hex != null && hexSet.add(hex)) {
            hexes.add(hex);
            if (!this.archipelago && !this.iceCap) {
                hex.setContinentId(this.id);
            }
        }
    }

    public void removeHex(Hex hex) {
        if (hex != null && hexSet.remove(hex)) {
            hexes.remove(hex);
        }
    }

    public void clearHexes() {
        hexes.clear();
        hexSet.clear();
    }

    public int getHexCount() {
        return hexes.size();
    }

    public List<Colony> getColonies() {
        List<Colony> colonies = new ArrayList<>();
        for (Hex h : hexes) {
            if (h != null && h.getColony() != null) {
                colonies.add(h.getColony());
            }
        }
        return colonies;
    }

    public List<Colony> getCapitals() {
        List<Colony> capitals = new ArrayList<>();
        for (Hex h : hexes) {
            if (h != null && h.getColony() != null && h.getColony().isCapital()) {
                capitals.add(h.getColony());
            }
        }
        return capitals;
    }

    public List<Hex> getPopulatedHexes() {
        return Collections.unmodifiableList(populatedHexes);
    }

    public boolean hasPopulatedHexes() {
        return !populatedHexes.isEmpty();
    }

    public void refreshPopulatedHexes() {
        populatedHexes.clear();
        for (Hex h : hexes) {
            if (h != null && h.getColony() != null) {
                populatedHexes.add(h);
            }
        }
    }
}
