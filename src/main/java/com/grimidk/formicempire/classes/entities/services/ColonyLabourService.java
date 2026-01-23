package com.grimidk.formicempire.classes.entities.services;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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

    // --- Actual Ant Objects ---
    private List<Ant> getWorkingAnts(Colony colony, AntRole role) {
        List<Ant> workers = new ArrayList<>();
        List<Ant> allAdults = new ArrayList<>();
        allAdults.addAll(colony.getWorkers());
        allAdults.addAll(colony.getSoldiers());
        allAdults.addAll(colony.getMajors());
        allAdults.addAll(colony.getQueens());
        allAdults.addAll(colony.getPrincesses()); 
        allAdults.addAll(colony.getDrones());
        
        for (Ant ant : allAdults) {
            if (!ant.isAlive()) continue;
            if (ant.getRole() == role) {
                workers.add(ant);
            }
        }
        return workers;
    }

    private int countActiveAnts(Colony colony, AntRole role) {
        return getWorkingAnts(colony, role).size();
    }
    
    private int processGathering(Colony colony, List<ResourceSource> sources, int powerAvailable, ResourceType type, List<Ant> workers) {
        if (sources == null || sources.isEmpty() || workers.isEmpty()) return 0;
        
        ColonyStatsService stats = colony.getStatsService();
        ColonyLocationService locations = colony.getLocationService();
        
        int totalGathered = 0;
        int workerIndex = 0;

        for (ResourceSource source : sources) {
            if (powerAvailable <= 0) break;
        
            double current = 0;
            double max = 0;
            
            if (type == GameConstants.PLANT_RESOURCE) {
                current = colony.getPlantsPrecise(); max = stats.getPlantsCapacity(colony);
            } else if (type == GameConstants.WATER_RESOURCE) {
                current = colony.getWaterPrecise(); max = stats.getWaterCapacity(colony);
            } else if (type == GameConstants.MEAT_RESOURCE) {
                current = colony.getProteinPrecise(); max = stats.getProteinCapacity(colony);
            } else if (type == GameConstants.ROCK_RESOURCE) {
                current = colony.getMineralsPrecise(); max = stats.getMineralsCapacity(colony);
            }
            
            double space = max - current;
            if (space <= 0) break; 

            int gatherAmount = Math.min(powerAvailable, (int) Math.ceil(space));
            int actualGathered = locations.gatherFromSource(colony, source, gatherAmount);
            
            if (actualGathered > 0) {
                if (type == GameConstants.PLANT_RESOURCE) {
                    colony.setPlants(colony.getPlantsPrecise() + actualGathered); 
                    
                    for(int i = 0; i < actualGathered; i++) {
                        if (workerIndex >= workers.size()) workerIndex = 0;
                        Ant worker = workers.get(workerIndex);
                        worker.setCarrying(type);
                        
                        if (colony.hasUpgrade(GameUnlocks.ABILITY_RESIN)) {
                             if (random.nextInt(100) < 1) {
                                 double resinSpace = stats.getResinsCapacity(colony) - colony.getResinsPrecise();
                                 if (resinSpace > 0) {
                                     colony.setResins(colony.getResinsPrecise() + 1);
                                     worker.setCarryingSec(GameConstants.RESIN_RESOURCE);
                                 }
                             }
                        }
                        workerIndex++;
                    }
                    
                } else {
                    if (type == GameConstants.WATER_RESOURCE) colony.setWater(colony.getWaterPrecise() + actualGathered);
                    else if (type == GameConstants.MEAT_RESOURCE) colony.setProtein(colony.getProteinPrecise() + actualGathered);
                    else if (type == GameConstants.ROCK_RESOURCE) colony.setMinerals(colony.getMineralsPrecise() + actualGathered);
                    
                    for(Ant w : workers) {
                        w.setCarrying(type);
                        w.setCarryingSec(null);
                    }
                }
                
                powerAvailable -= actualGathered;
                totalGathered += actualGathered;
            }
        }
        return totalGathered;
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
        
        // --- Foragers ---
        if (colony.hasUpgrade(GameUnlocks.ROLE_FORAGER)) {
            List<Ant> foragers = getWorkingAnts(colony, GameConstants.ROLE_FORAGER);
            if (!foragers.isEmpty()) {
                for(Ant a : foragers) a.clearLoad();
                
                int totalPower = (int) (foragers.size() * stats.getCollectingRate(colony));
                List<ResourceSource> plantSources = locations.getSourcesByType(GameConstants.PLANT_RESOURCE);
                List<ResourceSource> waterSources = locations.getSourcesByType(GameConstants.WATER_RESOURCE);

                boolean canCollectPlants = !plantSources.isEmpty() && hasSpace(colony, GameConstants.PLANT_RESOURCE);
                boolean canCollectWater = !waterSources.isEmpty() && hasSpace(colony, GameConstants.WATER_RESOURCE);

                if (canCollectPlants && canCollectWater) {
                    int halfPower = totalPower / 2;
                    int remainingPower = totalPower - halfPower;
                    int plantsGathered = processGathering(colony, plantSources, halfPower, GameConstants.PLANT_RESOURCE, foragers);
                    int waterPower = remainingPower + (halfPower - plantsGathered);
                    
                    processGathering(colony, waterSources, waterPower, GameConstants.WATER_RESOURCE, foragers);
                } else if (canCollectPlants) {
                    processGathering(colony, plantSources, totalPower, GameConstants.PLANT_RESOURCE, foragers);
                } else if (canCollectWater) {
                    processGathering(colony, waterSources, totalPower, GameConstants.WATER_RESOURCE, foragers);
                }
            }
        }
        
        // --- Hunters ---
        if (colony.hasUpgrade(GameUnlocks.ROLE_HUNTER)) {
            List<Ant> hunters = getWorkingAnts(colony, GameConstants.ROLE_HUNTER);
            if (!hunters.isEmpty()) {
                for(Ant a : hunters) a.clearLoad();
                int totalPower = (int) (hunters.size() * stats.getCollectingRate(colony));
                List<ResourceSource> sources = locations.getSourcesByType(GameConstants.MEAT_RESOURCE);
                processGathering(colony, sources, totalPower, GameConstants.MEAT_RESOURCE, hunters);
            }
        }

        // --- Miners ---
        if (colony.hasUpgrade(GameUnlocks.ROLE_MINER)) {
            List<Ant> miners = getWorkingAnts(colony, GameConstants.ROLE_MINER);
            if (!miners.isEmpty()) {
                for(Ant a : miners) a.clearLoad();
                int totalPower = (int) (miners.size() * stats.getCollectingRate(colony));
                List<ResourceSource> sources = locations.getSourcesByType(GameConstants.ROCK_RESOURCE);
                processGathering(colony, sources, totalPower, GameConstants.ROCK_RESOURCE, miners);
            }
        }

        // --- Passive Water ---
        if (colony.hasBuilding(GameUnlocks.PASSIVE_WATER)) {
            double currentWater = colony.getWaterPrecise();
            double maxWater = stats.getWaterCapacity(colony);
            if (currentWater < maxWater) {
                double gain = (maxWater * 0.10);
                if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) {
                    gain = (maxWater * 0.20);
                }
                colony.setWater(Math.min(currentWater + gain, maxWater));
            }
        }
    }

    public void runConverting(Colony colony) {
        ColonyStatsService stats = colony.getStatsService();
        int farmerCount = countActiveAnts(colony, GameConstants.ROLE_FARMER);
        if (colony.hasBuilding(GameUnlocks.PASSIVE_FARM)) {
            if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) {
                farmerCount += 2;
            } else { farmerCount += 1; } 
        }
        
        if (colony.getMushrooms() >= stats.getMushroomsCapacity(colony)) return;
        
        if (Math.random() <= stats.getConversionRate(colony)) {
            if (colony.getPlantsPrecise() >= farmerCount) {
                colony.setPlants(colony.getPlantsPrecise() - farmerCount);
                colony.setMushrooms(Math.min(colony.getMushroomsPrecise() + farmerCount, (double)stats.getMushroomsCapacity(colony)));
            }
        }
        
        if (colony.getMushrooms() >= stats.getMushroomsCapacity(colony)) return;
        
        if (Math.random() <= stats.getConversionRate(colony)) {
            if (colony.getProteinPrecise() >= farmerCount) {
                colony.setProtein(colony.getProteinPrecise() - farmerCount);
                int mushroomGain = farmerCount * 2;
                colony.setMushrooms(Math.min(colony.getMushroomsPrecise() + mushroomGain, (double)stats.getMushroomsCapacity(colony)));
            }
        }
    }

    public void runRanching(Colony colony) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) return;
        
        colony.setAphids(colony.getAphids()); 
        
        ColonyStatsService stats = colony.getStatsService();
        int syrupGain = (int) (colony.getAphids()); 
        colony.setPlants(Math.max(0, colony.getPlantsPrecise() - syrupGain));
        colony.setSyrups(Math.min(colony.getSyrupsPrecise() + syrupGain, (double)stats.getSyrupsCapacity(colony)));
    }

    public void runHerding(Colony colony, Biome biome) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) return;
         
        ColonyStatsService stats = colony.getStatsService();
        int rancherCount = countActiveAnts(colony, GameConstants.ROLE_RANCHER);
        
        if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) {
            rancherCount += 2;
        } else { rancherCount += 1; }
        
        int maxSustainableAphids = stats.getAphidCapacity(colony) * rancherCount;
        
        if (biome != null && biome.getPlantAbundance() >= 0.5f) {
            if (colony.getAphids() < maxSustainableAphids) {
                colony.setAphids(Math.min(colony.getAphids() + rancherCount, maxSustainableAphids));
            } else if (colony.getAphids() > maxSustainableAphids) {
                colony.setAphids(maxSustainableAphids);
            }
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
            
            newEgg.setDimension(WorldSpaces.UNDERWORLD);            
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
        List<Ant> nurses = getWorkingAnts(colony, GameConstants.ROLE_NURSE);
        int nurseCount = nurses.size();

        if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) {
                nurseCount += 2;
        } else { nurseCount += 1; }
        
        int babyAntTotal = colony.getEggs().size() +  colony.getLarvae().size() +  colony.getPupae().size();
        float nursingRate = stats.getNursingRate(colony);
        
        for (Ant nurse : nurses) {
            nurse.clearLoad();
            if (babyAntTotal > 0 && Math.random() < 0.30) {
                double r = Math.random();
                if (r < 0.33) nurse.setCarryingAnt(GameConstants.TYPE_EGG);
                else if (r < 0.66) nurse.setCarryingAnt(GameConstants.TYPE_LARVA);
                else nurse.setCarryingAnt(GameConstants.TYPE_PUPA);
            }
        }

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
                    antToCull.goDie(colony, "Lack of Care");
                    colony.recordAntDeath(antToCull, "Lack of Care");
                    
                    list.remove(antToCull);
                    deathCount++;
                }
            }
        }
        
        if (deathCount > 0) {
            colony.logEvent("DEATH: " + deathCount + " Juveniles died (Lack of Care)");
        }
    }
    
    public void runSpreading(Colony colony, int potentialSatellites) { 
        if (potentialSatellites > 0) {
            colony.logEvent(potentialSatellites + " new satellite colonies will spawn in adjacent hexes.");
        }
    }

    public void runPolicing(Colony colony) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_POLICE)) return;
        
        int parasiteCount = colony.getParasites();
        if (parasiteCount == 0) return;
        
        List<Ant> police = getWorkingAnts(colony, GameConstants.ROLE_POLICE);
        if (police.isEmpty()) return;
        
        float detectionRate = colony.getStatsService().getParasiteDetection(colony);
        int parasitesKilled = 0;
        
        for (Ant officer : police) {
            if (parasitesKilled >= parasiteCount) break;
            
            if (random.nextFloat() < detectionRate) {
                parasitesKilled++;
                
                double currentProtein = colony.getProteinPrecise();
                double maxProtein = colony.getStatsService().getProteinCapacity(colony);
                colony.setProtein(Math.min(currentProtein + 4, maxProtein));
            }
        }
        
        if (parasitesKilled > 0) {
            colony.logEvent("Eliminated " + parasitesKilled + " parasites.");
            colony.setParasites(Math.max(0, colony.getParasites() - parasitesKilled));
        }
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
        
        int bufferMin = 150;
        int bufferMax = 450;
        int randomBuffer = bufferMin + random.nextInt(bufferMax - bufferMin);
        
        int sourceX, sourceY;
        int side = random.nextInt(4); // 0=Top, 1=Right, 2=Bottom, 3=Left
        
        if (side == 0) { // Top
             sourceX = random.nextInt(Math.max(1, gameW));
             sourceY = -randomBuffer;
        } else if (side == 1) { // Right
             sourceX = gameW + randomBuffer;
             sourceY = random.nextInt(Math.max(1, gameH));
        } else if (side == 2) { // Bottom
             sourceX = random.nextInt(Math.max(1, gameW));
             sourceY = gameH + randomBuffer;
        } else { // Left
             sourceX = -randomBuffer;
             sourceY = random.nextInt(Math.max(1, gameH));
        }
        
        ResourceSource source = new ResourceSource(selectedType, quantity, sourceX, sourceY); 
        colony.getLocationService().addSource(colony, source);
    }

    public void runNuptial(Colony colony) {
        if (!colony.hasUpgrade(GameUnlocks.TYPE_PRINCESS)) return;

        List<Ant> allDrones = new ArrayList<>(colony.getDrones());
        List<Ant> breederPrincesses = colony.getPrincesses().stream()
            .filter(p -> p.getRole() == GameConstants.ROLE_BREEDER)
            .collect(Collectors.toList());

        if (allDrones.isEmpty() || breederPrincesses.isEmpty()) return;
        colony.getDrones().clear(); 
        colony.getPrincesses().removeAll(breederPrincesses);

        int potentialQueens = Math.min(allDrones.size(), breederPrincesses.size());
        int currentQueens = colony.getQueens().size();
        int maxQueens = colony.getStatsService().getQueensCapacity(colony);
        int spaceAvailable = maxQueens - currentQueens;
        int queensToAdd = 0;
        int queensLeaving = potentialQueens;

        if (spaceAvailable > 0) {
            queensToAdd = Math.min(spaceAvailable, potentialQueens);
            queensLeaving = potentialQueens - queensToAdd;
        }

        for (int i = 0; i < queensToAdd; i++) {
            Ant newQueen = new Ant(colony, GameConstants.TYPE_QUEEN);
            newQueen.setDimension(WorldSpaces.UNDERWORLD); 
            
            Rectangle royal = colony.getPhysicsService().getRoomBounds(colony, WorldSpaces.ROYAL_CHAMBER);
            if (royal != null) {
                newQueen.setPosition(colony.getPhysicsService().getSpecificRoomPoint(colony, royal));
            }

            colony.getQueens().add(newQueen);
        }

        colony.logEvent("Nuptial Flight Occurred. " + queensToAdd + " new Queens joined.");
        
        if (queensLeaving > 0) {
            runSpreading(colony, queensLeaving);
        }
    }

    public void runGraveKeeping(Colony colony) {
        List<Ant> gravers = getWorkingAnts(colony, GameConstants.ROLE_GRAVER);
        int graverCount = gravers.size();

        if (colony.hasBuilding(GameUnlocks.PASSIVE_GRAVE)) {
            if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) {
                graverCount += 2;
            } else { 
                graverCount += 1; 
            }
        }
        
        boolean hasBodies = !colony.getDeadAnts().isEmpty();
        for (Ant graver : gravers) {
            graver.clearLoad();
            if (hasBodies && Math.random() < 0.5) {
                graver.setCarryingAnt(GameConstants.TYPE_DEAD);
            }
        }
        
        if (graverCount == 0 || !hasBodies) return;
        
        int canClean = graverCount * (int) colony.getStatsService().getGravingRate(colony);
        List<Ant> antsToRemove = new ArrayList<>();
        
        for (Ant deadAnt : colony.getDeadAnts()) {
            if (canClean <= 0) break;
            antsToRemove.add(deadAnt);
            canClean--;
        }
        colony.getDeadAnts().removeAll(antsToRemove);
    }
    
    public void runComposting(Colony colony) {
        if (!colony.hasBuilding(GameUnlocks.BUILDING_COMPOSTER)) return;
        
        List<Ant> deadAnts = colony.getDeadAnts();
        List<Ant> gravers = getWorkingAnts(colony, GameConstants.ROLE_GRAVER);
        int graverCount = gravers.size();
        int potentialCompost = (int) colony.getStatsService().getGravingRate(colony) * gravers.size();
        
        if (potentialCompost == 0 || deadAnts.isEmpty()) return;

        int actualToCompost = Math.min(potentialCompost, deadAnts.size());
        
        List<Ant> compostedAnts = new ArrayList<>();
        for(int i = 0; i < actualToCompost; i++) {
            compostedAnts.add(deadAnts.get(i));
        }
        deadAnts.removeAll(compostedAnts);

        int mushroomGain = actualToCompost * 4; 
        int capacity = colony.getStatsService().getMushroomsCapacity(colony);
        colony.setMushrooms(Math.min(colony.getMushroomsPrecise() + mushroomGain, (double)capacity));
        
        if (actualToCompost > 0) {
            colony.logEvent("COMPOST: Recycled " + actualToCompost + " bodies into mushroom matter.");
        }
    }

    public void runResearch(Colony colony) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER)) return;
        
        int researcherCount = countActiveAnts(colony, GameConstants.ROLE_RESEARCHER);        
        if (colony.hasBuilding(GameUnlocks.PASSIVE_LAB)) {
            if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) {
                researcherCount += 2;
            } else { 
                researcherCount += 1; 
            }
        }

        int assistantCount = countActiveAnts(colony, GameConstants.ROLE_ASSISTANT);        
        if (researcherCount > 0 || assistantCount > 0) {
            int speed = colony.getStatsService().getResearchSpeed(colony);
            
            int queenGain = researcherCount * speed;
            int assistantGain = (int) (assistantCount * (speed / 5.0));

            colony.setResearchPoints(colony.getResearchPoints() + queenGain + assistantGain);
        }
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