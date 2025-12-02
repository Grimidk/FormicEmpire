package com.grimidk.formicempire.classes.entities.services;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;


public class ColonyLabourService {
    
    private final Random random = new Random();

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

    // --- Accepts ResourceType Object ---
    private void processGathering(Colony colony, List<ResourceSource> sources, int powerAvailable, ResourceType type) {
        if (sources == null || sources.isEmpty()) return;
        
        ColonyStatsService stats = colony.getStatsService();
        ColonyLocationService locations = colony.getLocationService();
        
        for (ResourceSource source : sources) {
            if (powerAvailable <= 0) break;
        
            int current = 0;
            int max = 0;
            
            if (type == GameConstants.PLANT_RESOURCE) {
                current = colony.getPlants(); max = stats.getPlantsCapacity(colony);
            } else if (type == GameConstants.WATER_RESOURCE) {
                current = colony.getWater(); max = stats.getWaterCapacity(colony);
            } else if (type == GameConstants.MEAT_RESOURCE) {
                current = colony.getProtein(); max = stats.getProteinCapacity(colony);
            } else if (type == GameConstants.ROCK_RESOURCE) {
                current = colony.getMinerals(); max = stats.getMineralsCapacity(colony);
            }
            
            int space = max - current;
            if (space <= 0) break; 

            int gatherAmount = Math.min(powerAvailable, space);
            int actualGathered = locations.gatherFromSource(colony, source, gatherAmount);
            
            if (actualGathered > 0) {
                if (type == GameConstants.PLANT_RESOURCE) {
                    colony.setPlants(colony.getPlants() + actualGathered); 
                    if (colony.hasUpgrade(GameUnlocks.ABILITY_RESIN)) {
                        calculateResinBonus(colony, actualGathered);
                    }
                } else if (type == GameConstants.WATER_RESOURCE) {
                    colony.setWater(colony.getWater() + actualGathered);
                } else if (type == GameConstants.MEAT_RESOURCE) {
                    colony.setProtein(colony.getProtein() + actualGathered);
                } else if (type == GameConstants.ROCK_RESOURCE) {
                    colony.setMinerals(colony.getMinerals() + actualGathered);
                }
                
                powerAvailable -= actualGathered;
            }
        }
    }

    private void calculateResinBonus(Colony colony, int plantsGathered) {
        int guaranteed = plantsGathered / 100;
        int remainder = plantsGathered % 100;
        
        int totalGain = guaranteed;
        if (remainder > 0 && random.nextInt(100) < remainder) {
            totalGain++;
        }
        
        if (totalGain > 0) {
            int space = colony.getStatsService().getResinsCapacity(colony) - colony.getResins();
            colony.setResins(colony.getResins() + Math.min(totalGain, space));
        }
    }

    private int splitGatherPower(int totalPower) {
        if (totalPower < 20) {
            int split = 0;
            for (int i = 0; i < totalPower; i++) {
                if (random.nextBoolean()) split++;
            }
            return split;
        }
        int base = totalPower / 2;
        int variance = (int)(totalPower * 0.15f); 
        if (variance > 0) base += (random.nextInt(variance * 2) - variance);
        return Math.max(0, Math.min(totalPower, base));
    }

    private boolean hasSpace(Colony colony, ResourceType type) {
        ColonyStatsService stats = colony.getStatsService();
        if (type == GameConstants.PLANT_RESOURCE) return stats.getPlantsCapacity(colony) > colony.getPlants();
        if (type == GameConstants.WATER_RESOURCE) return stats.getWaterCapacity(colony) > colony.getWater();
        return false;
    }

    public void runCollecting(Colony colony) {
        ColonyStatsService stats = colony.getStatsService();
        ColonyLocationService locations = colony.getLocationService();
        
        if (colony.hasUpgrade(GameUnlocks.ROLE_FORAGER)) {
            int foragerCount = countActiveAnts(colony, GameConstants.ROLE_FORAGER);
            if (foragerCount > 0) {
                int totalPower = (int) (foragerCount * stats.getCollectingRate(colony));
                
                boolean needPlants = hasSpace(colony, GameConstants.PLANT_RESOURCE);
                boolean needWater = hasSpace(colony, GameConstants.WATER_RESOURCE);
                
                List<ResourceSource> plantSources = locations.getSourcesByType(GameConstants.PLANT_RESOURCE);
                List<ResourceSource> waterSources = locations.getSourcesByType(GameConstants.WATER_RESOURCE);
                
                int plantPower = 0;
                int waterPower = 0;
                
                if (needPlants && !plantSources.isEmpty() && needWater && !waterSources.isEmpty()) {
                    plantPower = splitGatherPower(totalPower); 
                    waterPower = totalPower - plantPower;
                } else if (needPlants && !plantSources.isEmpty()) {
                    plantPower = totalPower;
                } else if (needWater && !waterSources.isEmpty()) {
                    waterPower = totalPower;
                }
                
                if (plantPower > 0) processGathering(colony, plantSources, plantPower, GameConstants.PLANT_RESOURCE);
                if (waterPower > 0) processGathering(colony, waterSources, waterPower, GameConstants.WATER_RESOURCE);
            }
        }
        
        if (colony.hasUpgrade(GameUnlocks.ROLE_HUNTER)) {
            int hunterCount = countActiveAnts(colony, GameConstants.ROLE_HUNTER);
            if (hunterCount > 0) {
                int totalPower = (int) (hunterCount * stats.getCollectingRate(colony));
                List<ResourceSource> sources = locations.getSourcesByType(GameConstants.MEAT_RESOURCE);
                processGathering(colony, sources, totalPower, GameConstants.MEAT_RESOURCE);
            }
        }

        if (colony.hasUpgrade(GameUnlocks.ROLE_MINER)) {
            int minerCount = countActiveAnts(colony, GameConstants.ROLE_MINER);
            if (minerCount > 0) {
                int totalPower = (int) (minerCount * stats.getCollectingRate(colony));
                List<ResourceSource> sources = locations.getSourcesByType(GameConstants.ROCK_RESOURCE);
                processGathering(colony, sources, totalPower, GameConstants.ROCK_RESOURCE);
            }
        }

        if (colony.hasBuilding(GameUnlocks.PASSIVE_WATER)) {
            int currentWater = colony.getWater();
            int maxWater = stats.getWaterCapacity(colony);
            if (currentWater < maxWater) {
                int gain = Math.max(1, maxWater / 100); 
                colony.setWater(Math.min(currentWater + gain, maxWater));
            }
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
            
            newEgg.setDimension(WorldSpaces.UNDERWORLD.getId());            
            Rectangle nursery = colony.getPhysicsService().getRoomBounds(colony, WorldSpaces.NURSERY);
            
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

    public void runScoutting(Colony colony, Biome biome) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_SCOUT)) return;

        int scoutCount = countActiveAnts(colony, GameConstants.ROLE_SCOUT);
        if (scoutCount == 0) return;

        float chancePerScout = colony.getStatsService().getScoutingRate(colony); 
        float totalChance = scoutCount * chancePerScout;

        while (totalChance > 0) {
            boolean found = false;
            
            if (totalChance >= 1.0f) {
                found = true;
                totalChance -= 1.0f;
            } else {
                if (random.nextFloat() < totalChance) {
                    found = true;
                }
                totalChance = 0; 
            }

            if (found) {
                generateAndAddSource(colony, biome);
            }
        }
    }

    private void generateAndAddSource(Colony colony, Biome biome) {
        ColonyLocationService locations = colony.getLocationService();
        List<ResourceType> possibleTypes = new ArrayList<>();
        
        if (colony.hasUpgrade(GameUnlocks.ROLE_FORAGER) && !locations.isSourceFull(colony, GameConstants.PLANT_RESOURCE)) {
            possibleTypes.add(GameConstants.PLANT_RESOURCE);
        }
        if (colony.hasUpgrade(GameUnlocks.ROLE_HUNTER) && !locations.isSourceFull(colony, GameConstants.MEAT_RESOURCE)) {
            possibleTypes.add(GameConstants.MEAT_RESOURCE);
        }
        if (colony.hasUpgrade(GameUnlocks.ROLE_MINER) && !locations.isSourceFull(colony, GameConstants.ROCK_RESOURCE)) {
            possibleTypes.add(GameConstants.ROCK_RESOURCE);
        }
        if (!locations.isSourceFull(colony, GameConstants.WATER_RESOURCE)) {
            possibleTypes.add(GameConstants.WATER_RESOURCE); 
        }

        if (possibleTypes.isEmpty()) return;
        ResourceType selectedType = possibleTypes.get(random.nextInt(possibleTypes.size()));
        
        float abundance = 0f;
        
        if (biome != null) {
            if (selectedType == GameConstants.PLANT_RESOURCE) abundance = biome.getPlantAbundance();
            else if (selectedType == GameConstants.MEAT_RESOURCE) abundance = biome.getAnimalAbundance();
            else if (selectedType == GameConstants.ROCK_RESOURCE) abundance = biome.getMineralAbundance();
            else if (selectedType == GameConstants.WATER_RESOURCE) {
                int h = biome.isIsHumid();
                if (h >= 5) abundance = 0.9f;     
                else if (h == 4) abundance = 0.7f;
                else if (h == 3) abundance = 0.5f; 
                else if (h == 2) abundance = 0.3f;
                else if (h == 1) abundance = 0.1f; 
                else abundance = 0.0f;    
            }
        } else {
            abundance = 0.5f; 
        }

        // None (0)
        if (abundance <= 0) return; 
        
        int quantity = 100; // Small
        float roll = random.nextFloat();
        
        if (roll < abundance) {
            quantity = 100000; // Huge
        } else {
            float subRoll = random.nextFloat();
            if (subRoll < 0.33f) {
                quantity = 10000; // Big
            } else if (subRoll < 0.66f) {
                quantity = 1000; // Medium
            } else {
                quantity = 100; // Small
            }
        }

        int gameW = colony.getGameAreaWidth();
        int gameH = colony.getGameAreaHeight();
        int buffer = 300;
        
        int sourceX, sourceY;
        int side = random.nextInt(4); // 0=Top, 1=Right, 2=Bottom, 3=Left
        
        if (side == 0) { // Top
             sourceX = random.nextInt(Math.max(1, gameW));
             sourceY = -buffer;
        } else if (side == 1) { // Right
             sourceX = gameW + buffer;
             sourceY = random.nextInt(Math.max(1, gameH));
        } else if (side == 2) { // Bottom
             sourceX = random.nextInt(Math.max(1, gameW));
             sourceY = gameH + buffer;
        } else { // Left
             sourceX = -buffer;
             sourceY = random.nextInt(Math.max(1, gameH));
        }
        
        ResourceSource source = new ResourceSource(selectedType, quantity, sourceX, sourceY); 
        colony.getLocationService().addSource(colony, source);
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