package com.grimidk.formicempire.classes.entities.services;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;


public class ColonyLabourService {
    
    // --- Job Counter ---
    private int countActiveAnts(Colony colony, AntRole role) {
        int count = 0;
        List<Ant> allAdults = new ArrayList<>();
        allAdults.addAll(colony.getWorkers());
        allAdults.addAll(colony.getSoldiers());
        allAdults.addAll(colony.getMajors());
        allAdults.addAll(colony.getQueens()); 
        
        for (Ant ant : allAdults) {
            if (!ant.isAlive()) continue;
            if (ant.getRole() != role) continue;
            
            count++;
        }
        return count;
    }

    public void runCollecting(Colony colony) {
        ColonyStatsService stats = colony.getStatsService();
        int foragerCount = countActiveAnts(colony, GameConstants.ROLE_FORAGER);
        
        int plantGain = 0;
        int waterGain = 0;
        for (int i = 0; i < foragerCount; i++) {
            if (Math.random() < 0.5) waterGain++;
            else plantGain++;
        }
        
        int effectivePlantGain = (int) (plantGain * stats.getCollectingRate(colony));
        int effectiveWaterGain = (int) (waterGain * stats.getCollectingRate(colony));
        if (colony.hasBuilding(GameUnlocks.PASSIVE_WATER)) {
            effectiveWaterGain += colony.getWaterCapacity() / 10;
        }
        int effectiveResinGain = (int) (plantGain * (stats.getCollectingRate(colony) / 100));
        
        colony.setPlants(Math.min(colony.getPlants() + effectivePlantGain, stats.getPlantsCapacity(colony)));
        colony.setWater(Math.min(colony.getWater() + effectiveWaterGain, stats.getWaterCapacity(colony)));
        
        if (colony.hasUpgrade(GameUnlocks.ABILITY_RESIN)) {
             colony.setResins(Math.min(colony.getResins() + effectiveResinGain, stats.getResinsCapacity(colony)));
        }
        
        if (colony.hasUpgrade(GameUnlocks.ROLE_HUNTER)) {
            int hunterCount = countActiveAnts(colony, GameConstants.ROLE_HUNTER);
            int proteinGain = (int) (hunterCount * stats.getCollectingRate(colony));
            colony.setProtein(Math.min(colony.getProtein() + proteinGain, stats.getProteinCapacity(colony)));
        }
    }

    public void runConverting(Colony colony) {
        ColonyStatsService stats = colony.getStatsService();
        int farmerCount = countActiveAnts(colony, GameConstants.ROLE_FARMER);
        
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
        int rancherCount = countActiveAnts(colony, GameConstants.ROLE_RANCHER);
        
        if(colony.hasBuilding(GameUnlocks.PASSIVE_APHID)) {
            rancherCount += 1;
        }
        int maxSustainableAphids = stats.getAphidCapacity(colony) * rancherCount;
        
        if (colony.getAphids() < maxSustainableAphids) {
            colony.setAphids(Math.min(colony.getAphids() + rancherCount, maxSustainableAphids));
        } else if (colony.getAphids() > maxSustainableAphids) {
            colony.setAphids(maxSustainableAphids);
        }
    }

    public void runLaying(Colony colony) {
        int layerCount = countActiveAnts(colony, GameConstants.ROLE_LAYER);
        
        List<Ant> eggList = colony.getEggs();
        
        int spaceAvailable = colony.getStatsService().getEggsCapacity(colony) - eggList.size();
        if (spaceAvailable <= 0) return;   
        
        int toLay = Math.min(layerCount * (int) colony.getStatsService().getLayingRate(colony), spaceAvailable);
        for (int i = 0; i < toLay; i++) {
            Ant newEgg = new Ant(colony, GameConstants.TYPE_EGG);
            
            newEgg.setDimension(1);
            Rectangle nursery = colony.getNurseryBounds();
            if (nursery != null) {
                Point spawnPos = colony.getPhysicsService().getSpecificRoomPoint(colony, nursery);
                newEgg.setPosition(spawnPos);
            } else {
                newEgg.setPosition(new Point(0, 0));
            }

            eggList.add(newEgg);
        }
    }

    public void runNursing(Colony colony) {
        ColonyStatsService stats = colony.getStatsService();
        int nurseCount = countActiveAnts(colony, GameConstants.ROLE_NURSE);
        
        int babyAntTotal = colony.getEggs().size() +  colony.getLarvae().size() +  colony.getPupae().size();
        float nursingRate = stats.getNursingRate(colony);

        if (babyAntTotal <= nurseCount * nursingRate) {
            return;
        }

        int deficit = babyAntTotal - (nurseCount * (int) nursingRate);
        List<AntType> killOrder = List.of(GameConstants.TYPE_LARVA, GameConstants.TYPE_EGG, GameConstants.TYPE_PUPA);
        int deathCount = 0;

        for (AntType typeToKill : killOrder) {
            if (deficit <= 0) break;
            List<Ant> list = colony.getAntsByType(typeToKill);
            List<Ant> antsToCull = new ArrayList<>();
            
            for (Ant ant : list) {
                if (deficit <= 0) break;
                if (Math.random() > 0.5) { 
                    antsToCull.add(ant);
                    deficit--;
                } 
            }
            
            for (Ant antToCull : antsToCull) {
                if (antToCull.isAlive()) {
                    antToCull.goDie(); 
                    colony.getDeadAnts().add(antToCull);
                    list.remove(antToCull);
                    deathCount++;
                }
            }
        }
        
        if (deathCount > 0) {
            colony.logEvent("WARNING: " + deathCount + " Juveniles Died (Nursing)");
        }
    }
    
    public void runSpreading(Colony colony, List<Ant> princesses) { 
        // Placeholder
    }

    public void runPolicing(Colony colony) {
        // Placeholder
    }

    public void runScoutting(Colony colony) {
        // Placeholder
    }

    public void runNuptial(Colony colony) {
        List<Ant> princesses = colony.getPrincesses();
        List<Ant> drones = colony.getDrones();

        if (!colony.hasUpgrade(GameUnlocks.TYPE_PRINCESS)) return;

        List<Ant> princessesToEvolve = new ArrayList<>();
        boolean flightOccurred = false;
        int queenCapacity = colony.getStatsService().getQueensCapacity(colony);
        
        for (Ant princess : princesses) {
            boolean hasQueenSpace = (colony.getQueens().size() + princessesToEvolve.size()) < queenCapacity;
            boolean hasDrones = !drones.isEmpty();     
            
            if (hasQueenSpace && hasDrones) {
                princessesToEvolve.add(princess);
                Ant deadDrone = drones.remove(drones.size() - 1); 
                deadDrone.goDie();
                colony.getDeadAnts().add(deadDrone);
                flightOccurred = true;
            } 
        }

        for (Ant princess : princessesToEvolve) {
            princess.transform(colony, GameConstants.TYPE_QUEEN);
            princess.setDimension(1); 
            colony.getQueens().add(princess);
        }
        
        princesses.removeAll(princessesToEvolve);
        runSpreading(colony, princesses); 
        
        if (flightOccurred) {
            colony.logEvent("CRITICAL: Nuptial Flight Occurred");
        }
    }

    public void runGraveKeeping(Colony colony) {
        int graverCount = colony.getAssignedRoleCount(GameConstants.ROLE_GRAVER);
        
        if (graverCount == 0 || colony.getDeadAnts().isEmpty()) return;
        
        int canClean = graverCount * (int) colony.getStatsService().getGravingRate(colony);
        List<Ant> antsToRemove = new ArrayList<>();
        
        for (Ant deadAnt : colony.getDeadAnts()) {
            if (canClean <= 0) break;
            antsToRemove.add(deadAnt);
            canClean--;
        }
        colony.getDeadAnts().removeAll(antsToRemove);
    }

    public void runResearch(Colony colony) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER)) return;
        int researcherCount = countActiveAnts(colony, GameConstants.ROLE_RESEARCHER);
        
        if (colony.hasBuilding(GameUnlocks.PASSIVE_LAB)) {
            researcherCount += 1; 
        }
        
        colony.setResearchPoints(colony.getResearchPoints() + (researcherCount * colony.getStatsService().getResearchSpeed(colony)));
    }

    public void runBuilding(Colony colony) {
        if (colony.getCurrentBuildingProject() == null) return;
        
        int builderCount = countActiveAnts(colony, GameConstants.ROLE_BUILDER);        
        if (builderCount <= 0) return;
        
        double efficiency = builderCount / 100.0;
        if (efficiency <= 0) return;

        colony.setBuildingProgressHours(colony.getBuildingProgressHours() + 1.0);
        double requiredHours = colony.getCurrentBuildingProject().getBuildTime() / efficiency;
        
        if (colony.getBuildingProgressHours() >= requiredHours) {
            colony.unlockBuilding(colony.getCurrentBuildingProject());
            colony.logEvent("SUCCESS: Built " + colony.getCurrentBuildingProject().getName());
            colony.setCurrentBuildingProject(null);
            colony.setBuildingProgressHours(0.0);
        }
    }
}