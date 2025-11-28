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

    public void runCollecting(Colony colony) {
        ColonyStatsService stats = colony.getStatsService();
        ColonyLocationService locations = colony.getLocationService();
        
        // --- Foragers (Plants & Water) ---
        if (colony.hasUpgrade(GameUnlocks.ROLE_FORAGER)) {
            int foragerCount = countActiveAnts(colony, GameConstants.ROLE_FORAGER);
            if (foragerCount > 0) {
                float rate = stats.getCollectingRate(colony); 
                int totalGatherPower = (int) (foragerCount * rate);
                
                List<ResourceSource> plantSources = locations.getSourcesByType(GameConstants.PLANT_RESOURCE);
                boolean canGatherPlants = !plantSources.isEmpty() && (stats.getPlantsCapacity(colony) - colony.getPlants() > 0);
                
                List<ResourceSource> waterSources = locations.getSourcesByType(GameConstants.WATER_RESOURCE);
                boolean canGatherWater = !waterSources.isEmpty() && (stats.getWaterCapacity(colony) - colony.getWater() > 0);
                
                int plantPower = 0;
                int waterPower = 0;
                
                if (canGatherPlants && canGatherWater) {
                    for (int i = 0; i < totalGatherPower; i++) {
                        if (random.nextBoolean()) plantPower++;
                        else waterPower++;
                    }
                } else if (canGatherPlants) {
                    plantPower = totalGatherPower;
                } else if (canGatherWater) {
                    waterPower = totalGatherPower;
                }
                
                if (plantPower > 0) {
                    for (ResourceSource source : plantSources) {
                        if (plantPower <= 0) break;
                        
                        int spaceAvailable = stats.getPlantsCapacity(colony) - colony.getPlants();
                        if (spaceAvailable <= 0) break;

                        int gatherAmount = Math.min(plantPower, spaceAvailable);
                        int actualGathered = locations.gatherFromSource(colony, source, gatherAmount);
                        
                        colony.setPlants(colony.getPlants() + actualGathered);
                        plantPower -= actualGathered;
                        
                        // Resin Chance (Bonus)
                        if (colony.hasUpgrade(GameUnlocks.ABILITY_RESIN)) {
                            int resinSpace = stats.getResinsCapacity(colony) - colony.getResins();
                            if (resinSpace > 0 && Math.random() < 0.01) {
                                 colony.setResins(colony.getResins() + 1);
                            }
                        }
                    }
                }
                
                if (waterPower > 0) {
                    for (ResourceSource source : waterSources) {
                        if (waterPower <= 0) break;
                        
                        int spaceAvailable = stats.getWaterCapacity(colony) - colony.getWater();
                        if (spaceAvailable <= 0) break;
                        
                        int gatherAmount = Math.min(waterPower, spaceAvailable);
                        int actualGathered = locations.gatherFromSource(colony, source, gatherAmount);
                        
                        colony.setWater(colony.getWater() + actualGathered);
                        waterPower -= actualGathered;
                    }
                }
            }
        }
        
        // --- Meat/Protein (Hunters) ---
        if (colony.hasUpgrade(GameUnlocks.ROLE_HUNTER)) {
            int hunterCount = countActiveAnts(colony, GameConstants.ROLE_HUNTER);
            if (hunterCount > 0) {
                float rate = stats.getCollectingRate(colony); 
                int totalGatherPower = (int) (hunterCount * rate);
                
                List<ResourceSource> meatSources = locations.getSourcesByType(GameConstants.MEAT_RESOURCE);
                
                for (ResourceSource source : meatSources) {
                    if (totalGatherPower <= 0) break;
                    
                    int spaceAvailable = stats.getProteinCapacity(colony) - colony.getProtein();
                    if (spaceAvailable <= 0) break;

                    int gatherAmount = Math.min(totalGatherPower, spaceAvailable);
                    int actualGathered = locations.gatherFromSource(colony, source, gatherAmount);
                    
                    colony.setProtein(colony.getProtein() + actualGathered);
                    totalGatherPower -= actualGathered;
                }
            }
        }

        // --- Minerals (Miners) ---
        if (colony.hasUpgrade(GameUnlocks.ROLE_MINER)) {
            int minerCount = countActiveAnts(colony, GameConstants.ROLE_MINER);
            if (minerCount > 0) {
                float rate = stats.getCollectingRate(colony); 
                int totalGatherPower = (int) (minerCount * rate);
                
                List<ResourceSource> mineralSources = locations.getSourcesByType(GameConstants.ROCK_RESOURCE);
                
                for (ResourceSource source : mineralSources) {
                    if (totalGatherPower <= 0) break;
                    
                    int spaceAvailable = stats.getMineralsCapacity(colony) - colony.getMinerals();
                    if (spaceAvailable <= 0) break;

                    int gatherAmount = Math.min(totalGatherPower, spaceAvailable);
                    int actualGathered = locations.gatherFromSource(colony, source, gatherAmount);
                    
                    colony.setMinerals(colony.getMinerals() + actualGathered);
                    totalGatherPower -= actualGathered;
                }
            }
        }

        // Passive Water (Dew)
        if (colony.hasBuilding(GameUnlocks.PASSIVE_WATER)) {
            int gain = colony.getStatsService().getWaterCapacity(colony) / 10;
            colony.setWater(Math.min(colony.getWater() + gain, stats.getWaterCapacity(colony)));
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
        
        ResourceSource source = new ResourceSource(selectedType, quantity, 0, 0); 
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