package com.grimidk.formicempire.classes.entities.services;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.grimidk.formicempire.classes.entities.Colony;

public class ColonyDeathService {

    private final Map<String, Integer> deathCauses = new ConcurrentHashMap<>();

    public ColonyDeathService() {
        initializeDeathCauses();
    }

    private void initializeDeathCauses() {
        deathCauses.put("Old Age", 0);
        deathCauses.put("Starvation", 0);
        deathCauses.put("Dehydration", 0);
        deathCauses.put("Contamination", 0);
        deathCauses.put("Conflict", 0);
        deathCauses.put("Illness", 0);
        deathCauses.put("Lack of Care", 0);
        deathCauses.put("Other", 0);
    }

    public void recordDeath(String cause) {
        recordDeath(cause, null);
    }

    public void recordDeath(String cause, Colony colony) {
        String key = "Other";
        if (cause != null) {
            if (deathCauses.containsKey(cause)) {
                key = cause;
            } else if (cause.equalsIgnoreCase("Combat")) {
                key = "Conflict";
            }
        }
        deathCauses.merge(key, 1, Integer::sum);

        if (colony != null && colony.getDynasty() != null) {
            colony.getDynasty().recordDeath(key);
        }
    }

    public Map<String, Integer> getDeathStatistics() {
        return Collections.unmodifiableMap(deathCauses);
    }

    public void loadDeathStatistics(Map<String, Integer> savedStats) {
        if (savedStats != null) {
            this.deathCauses.putAll(savedStats);
        }
    }

    public void resetDeathStatistics() {
        deathCauses.clear();
        initializeDeathCauses();
    }
}
