package com.grimidk.formicempire.classes.entities.invasion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.grimidk.formicempire.classes.entities.critter.Ant;

public final class InvasionDefense {

    private final int alertId;
    private final List<Ant> party;

    public InvasionDefense(int alertId, List<Ant> party) {
        this.alertId = alertId;
        this.party = new ArrayList<>(party);
    }

    public int getAlertId() {
        return alertId;
    }

    public List<Ant> getParty() {
        return Collections.unmodifiableList(party);
    }

    public static InvasionDefense restore(int alertId, List<Ant> party) {
        return new InvasionDefense(alertId, party);
    }
}
