package com.grimidk.formicempire.classes.entities.hunt;

import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

public final class KnownHuntTarget {
    private final int id;
    private final int speciesId;
    private final int overworldX;
    private final int overworldY;
    private final int discoveredWorldDay;
    private final int escapeWorldDay;

    public KnownHuntTarget(int id, int speciesId, int overworldX, int overworldY, int discoveredWorldDay,
            int escapeWorldDay) {
        this.id = id;
        this.speciesId = speciesId;
        this.overworldX = overworldX;
        this.overworldY = overworldY;
        this.discoveredWorldDay = discoveredWorldDay;
        this.escapeWorldDay = escapeWorldDay;
    }

    public int getId() {
        return id;
    }

    public int getSpeciesId() {
        return speciesId;
    }

    public Species getSpecies() {
        return GameConstants.getCritterSpeciesById(speciesId);
    }

    public int getOverworldX() {
        return overworldX;
    }

    public int getOverworldY() {
        return overworldY;
    }

    public int getDiscoveredWorldDay() {
        return discoveredWorldDay;
    }

    public int getEscapeWorldDay() {
        return escapeWorldDay;
    }

    public boolean hasEscaped(int worldDay) {
        return worldDay >= escapeWorldDay;
    }
}
