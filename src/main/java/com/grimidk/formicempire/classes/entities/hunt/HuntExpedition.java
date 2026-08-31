package com.grimidk.formicempire.classes.entities.hunt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.grimidk.formicempire.classes.entities.critter.Ant;

public final class HuntExpedition {
    public enum Phase {
        TRAVELING,
        FIGHTING,
        RESOLVED
    }

    private final int targetId;
    private Phase phase;
    private final List<Ant> party;
    private float travelHoursRemaining;
    private float travelHoursTotal;

    public HuntExpedition(int targetId, List<Ant> party, float travelHoursTotal) {
        this.targetId = targetId;
        this.party = new ArrayList<>(party);
        this.phase = Phase.TRAVELING;
        this.travelHoursTotal = Math.max(1f, travelHoursTotal);
        this.travelHoursRemaining = this.travelHoursTotal;
    }

    public int getTargetId() {
        return targetId;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public List<Ant> getParty() {
        return Collections.unmodifiableList(party);
    }

    public float getTravelHoursRemaining() {
        return travelHoursRemaining;
    }

    public float getTravelHoursTotal() {
        return travelHoursTotal;
    }

    public float getTravelProgressRatio() {
        if (travelHoursTotal <= 0f) {
            return 1f;
        }
        return 1f - (travelHoursRemaining / travelHoursTotal);
    }

    public void advanceTravelOneHour() {
        if (phase == Phase.TRAVELING) {
            travelHoursRemaining = Math.max(0f, travelHoursRemaining - 1f);
        }
    }

    public static HuntExpedition restore(int targetId, Phase phase, List<Ant> party, float travelHoursRemaining,
            float travelHoursTotal) {
        HuntExpedition expedition = new HuntExpedition(targetId, party, travelHoursTotal);
        expedition.phase = phase;
        expedition.travelHoursRemaining = Math.max(0f, travelHoursRemaining);
        return expedition;
    }

    public boolean isTravelComplete() {
        return phase == Phase.TRAVELING && travelHoursRemaining <= 0f;
    }
}
