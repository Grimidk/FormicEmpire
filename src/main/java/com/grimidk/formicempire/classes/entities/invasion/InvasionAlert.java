package com.grimidk.formicempire.classes.entities.invasion;

import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

public final class InvasionAlert {

    public enum Scope {
        COLONY,
        DYNASTY
    }

    private final int id;
    private final int speciesId;
    private final Scope scope;
    private final int targetColonyId;
    private final int deadlineAbsoluteHour;
    private boolean defenseDispatched;

    public InvasionAlert(int id, int speciesId, Scope scope, int targetColonyId, int deadlineAbsoluteHour) {
        this.id = id;
        this.speciesId = speciesId;
        this.scope = scope != null ? scope : Scope.COLONY;
        this.targetColonyId = targetColonyId;
        this.deadlineAbsoluteHour = deadlineAbsoluteHour;
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

    public Scope getScope() {
        return scope;
    }

    public int getTargetColonyId() {
        return targetColonyId;
    }

    public int getDeadlineAbsoluteHour() {
        return deadlineAbsoluteHour;
    }

    public boolean isDefenseDispatched() {
        return defenseDispatched;
    }

    public void setDefenseDispatched(boolean defenseDispatched) {
        this.defenseDispatched = defenseDispatched;
    }

    public int hoursRemaining(int worldDay, int worldHour) {
        return Math.max(0, deadlineAbsoluteHour - toAbsoluteHour(worldDay, worldHour));
    }

    public boolean isExpired(int worldDay, int worldHour) {
        return toAbsoluteHour(worldDay, worldHour) >= deadlineAbsoluteHour;
    }

    public static int toAbsoluteHour(int worldDay, int worldHour) {
        return Math.max(0, worldDay) * 24 + Math.max(0, Math.min(23, worldHour));
    }
}
