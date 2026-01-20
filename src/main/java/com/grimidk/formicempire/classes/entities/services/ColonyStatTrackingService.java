package com.grimidk.formicempire.classes.entities.services;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ColonyStatTrackingService {
    
    private final Map<String, Integer> deathCauses = new ConcurrentHashMap<>();
    private final Map<String, Integer> lifetimeProduction = new ConcurrentHashMap<>();
    private final Map<String, Integer> lifetimeConsumption = new ConcurrentHashMap<>();

    public ColonyStatTrackingService() {
        initializeCauses();
    }

    private void initializeCauses() {
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
        String key = "Other";
        if (cause != null) {
            if (cause.contains("Starvation") || cause.contains("Dehydration")) key = "Starvation"; 
            else if (cause.equalsIgnoreCase("Starvation")) key = "Starvation";
            else if (cause.equalsIgnoreCase("Dehydration")) key = "Dehydration";
            else if (cause.equalsIgnoreCase("Old Age")) key = "Old Age";
            else if (cause.contains("Contamination")) key = "Contamination";
            else if (cause.equalsIgnoreCase("Lack of Care")) key = "Lack of Care";
            else if (cause.contains("Parasite") || cause.equalsIgnoreCase("Illness")) key = "Illness";
            else if (cause.equalsIgnoreCase("Combat") || cause.equalsIgnoreCase("War")) key = "Conflict";
        }
        
        deathCauses.merge(key, 1, Integer::sum);
    }

    public Map<String, Integer> getDeathStatistics() {
        return Collections.unmodifiableMap(deathCauses);
    }
    
    public void loadStatistics(Map<String, Integer> savedStats) {
        if (savedStats != null) {
            this.deathCauses.putAll(savedStats);
        }
    }
    
    public int getDeathCount(String cause) {
        return deathCauses.getOrDefault(cause, 0);
    }

    public void reset() {
        deathCauses.clear();
        initializeCauses();
        lifetimeProduction.clear();
        lifetimeConsumption.clear();
    }
}