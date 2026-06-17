package com.grimidk.formicempire.classes.entities.services;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.DeathCause;

public class ColonyDeathService {

    private final Map<String, Integer> deathCauses = new ConcurrentHashMap<>();

    public ColonyDeathService() {
        initializeDeathCauses();
    }

    private void initializeDeathCauses() {
        deathCauses.put(DeathCause.OLD_AGE, 0);
        deathCauses.put(DeathCause.STARVATION, 0);
        deathCauses.put(DeathCause.DEHYDRATION, 0);
        deathCauses.put(DeathCause.CONTAMINATION, 0);
        deathCauses.put(DeathCause.CONFLICT, 0);
        deathCauses.put(DeathCause.ILLNESS, 0);
        deathCauses.put(DeathCause.LACK_OF_CARE, 0);
        deathCauses.put(DeathCause.OTHER, 0);
    }

    public void recordDeath(String cause) {
        recordDeath(cause, null);
    }

    public void recordDeath(String cause, Colony colony) {
        String key = DeathCause.normalize(cause);
        deathCauses.merge(key, 1, Integer::sum);

        if (colony != null && colony.getDynasty() != null) {
            colony.getDynasty().recordDeath(key);
        }
    }

    public Map<String, Integer> getDeathStatistics() {
        return Collections.unmodifiableMap(deathCauses);
    }

    public void loadDeathStatistics(Map<String, Integer> savedStats) {
        resetDeathStatistics();
        Map<String, Integer> migrated = DeathCause.migrateStatistics(savedStats);
        if (migrated != null) {
            for (Map.Entry<String, Integer> entry : migrated.entrySet()) {
                deathCauses.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }
    }

    public void resetDeathStatistics() {
        deathCauses.clear();
        initializeDeathCauses();
    }
}
