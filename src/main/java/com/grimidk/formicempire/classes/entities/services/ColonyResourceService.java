package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.GameUnlocks;


public class ColonyResourceService {
    public void runCollecting(Colony colony) {
        ColonyStatsService stats = colony.getStatsService();

        int foragerCount = colony.getAssignedRoleCount(GameConstants.ROLE_FORAGER);
        
        int plantGain = 0;
        int waterGain = 0;
        for (int i = 0; i < foragerCount; i++) {
            if (Math.random() < 0.5) waterGain++;
            else plantGain++;
        }
        
        int effectivePlantGain = (int) (plantGain * stats.getCollectingRate(colony));
        int effectiveWaterGain = (int) (waterGain * stats.getCollectingRate(colony));
        int effectiveResinGain = (int) (plantGain * (stats.getCollectingRate(colony) / 100));
        
        colony.setPlants(Math.min(colony.getPlants() + effectivePlantGain, stats.getPlantsCapacity(colony)));
        colony.setWater(Math.min(colony.getWater() + effectiveWaterGain, stats.getWaterCapacity(colony)));
        
        if (colony.hasUpgrade(GameUnlocks.ABILITY_RESIN)) {
             colony.setResins(Math.min(colony.getResins() + effectiveResinGain, stats.getResinsCapacity(colony)));
        }
        
        if (colony.hasUpgrade(GameUnlocks.ROLE_HUNTER)) {
            int hunterCount = colony.getAssignedRoleCount(GameConstants.ROLE_HUNTER);
            int proteinGain = (int) (hunterCount * stats.getCollectingRate(colony));
            colony.setProtein(Math.min(colony.getProtein() + proteinGain, stats.getProteinCapacity(colony)));
        }
    }

    public void runConverting(Colony colony) {
        ColonyStatsService stats = colony.getStatsService();
        int farmerCount = colony.getAssignedRoleCount(GameConstants.ROLE_FARMER);
        
        if (colony.getMushrooms() >= stats.getMushroomsCapacity(colony)) return;
        
        if (Math.random() <= stats.getConversionRate(colony)) {
            if (colony.getPlants() >= farmerCount) {
                colony.setPlants(colony.getPlants() - farmerCount);
                colony.setMushrooms(Math.min(colony.getMushrooms() + farmerCount, stats.getMushroomsCapacity(colony)));
            }
        }
        
        if (colony.getMushrooms() >= stats.getMushroomsCapacity(colony)) return;
        
        if (Math.random() <= stats.getConversionRate(colony)) {
            if (colony.getProtein() >= farmerCount) {
                colony.setProtein(colony.getProtein() - farmerCount);
                int mushroomGain = farmerCount * 2;
                colony.setMushrooms(Math.min(colony.getMushrooms() + mushroomGain, stats.getMushroomsCapacity(colony)));
            }
        }
    }

    public void runRanching(Colony colony) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) return;
        
        ColonyStatsService stats = colony.getStatsService();
        int syrupGain = (int) (colony.getAphids()); 
        colony.setSyrups(Math.min(colony.getSyrups() + syrupGain, stats.getSyrupsCapacity(colony)));
    }

    public void runHerding(Colony colony) {
         if (!colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) return;
         
        ColonyStatsService stats = colony.getStatsService();
        int rancherCount = colony.getAssignedRoleCount(GameConstants.ROLE_RANCHER);
        int maxSustainableAphids = stats.getAphidCapacity(colony) * rancherCount;
        
        if (colony.getAphids() < maxSustainableAphids) {
            colony.setAphids(Math.min(colony.getAphids() + rancherCount, maxSustainableAphids));
        } else if (colony.getAphids() > maxSustainableAphids) {
            colony.setAphids(maxSustainableAphids);
        }
    }
}