package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.entities.world.ResourceSourcePlacement;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.entities.spatial.NeoPoint;
import com.grimidk.formicempire.classes.entities.services.world.WorldHistoryEvent;
import com.grimidk.formicempire.classes.entities.services.world.WorldHistoryEventType;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.DeathCause;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;

public class ColonyLabourService {

    private final List<Ant> workingAntsScratch = new ArrayList<>();

    // --- Actual Ant Objects ---
    private List<Ant> getWorkingAnts(Colony colony, AntRole role) {
        workingAntsScratch.clear();
        if (role == null) {
            return workingAntsScratch;
        }
        List<Ant> group = colony.getAntsByType(role.getAntType());
        if (group == null || group.isEmpty()) {
            return workingAntsScratch;
        }
        for (Ant ant : group) {
            if (ant.isAlive() && !ant.isOnTrade() && ant.getRole() == role) {
                workingAntsScratch.add(ant);
            }
        }
        return workingAntsScratch;
    }
    
    private int processGathering(Colony colony, List<ResourceSource> sources, int powerAvailable, ResourceType type, List<Ant> workers) {
        if (sources == null || sources.isEmpty() || workers.isEmpty()) return 0;
        
        ColonyStatsService stats = colony.getStatsService();
        ColonyLocationService locations = colony.getLocationService();
        ColonyResourceService resources = colony.getResourceService();
        
        int totalGathered = 0;
        int workerIndex = 0;

        for (ResourceSource source : sources) {
            if (powerAvailable <= 0) break;
        
            double current = 0;
            double max = 0;
            
            if (type == GameConstants.RESOURCE_PLANT) {
                current = colony.getPlantsPrecise(); max = stats.getPlantsCapacity(colony);
            } else if (type == GameConstants.RESOURCE_WATER) {
                current = colony.getWaterPrecise(); max = stats.getWaterCapacity(colony);
            } else if (type == GameConstants.RESOURCE_MEAT) {
                current = colony.getProteinPrecise(); max = stats.getProteinCapacity(colony);
            } else if (type == GameConstants.RESOURCE_ROCK) {
                current = colony.getMineralsPrecise(); max = stats.getMineralsCapacity(colony);
            }
            
            double space = max - current;
            if (space <= 0) break; 

            int gatherAmount = Math.min(powerAvailable, (int) Math.ceil(space));
            double efficiency = locations.computeGatherEfficiency(colony, source, workers);
            int cappedRequest = (int) Math.floor(gatherAmount * efficiency);
            if (cappedRequest <= 0) {
                continue;
            }
            int actualGathered = locations.gatherFromSource(colony, source, cappedRequest);
            
            if (actualGathered > 0) {
                resources.addResource(colony, type, actualGathered);
                
                if (type == GameConstants.RESOURCE_PLANT) {
                    for (int i = 0; i < actualGathered; ) {
                        if (workerIndex >= workers.size()) {
                            workerIndex = 0;
                        }
                        Ant worker = workers.get(workerIndex);
                        worker.setCarrying(type);

                        if (colony.hasUpgrade(GameUnlocks.ABILITY_RESIN)) {
                            if (GameRandom.nextDouble() < GameNumbers.RESIN_FORAGE_BONUS_CHANCE) {
                                double addedResin = resources.addResource(colony, GameConstants.RESOURCE_RESIN, 1);
                                if (addedResin > 0) {
                                    worker.setCarryingSec(GameConstants.RESOURCE_RESIN);
                                }
                            }
                        }
                        i += AntSubtypeService.forageCarrySlots(worker);
                        workerIndex++;
                    }
                    
                } else {
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

    public void runCollecting(Colony colony) {
        ColonyStatsService stats = colony.getStatsService();
        ColonyLocationService locations = colony.getLocationService();
        ColonyResourceService resources = colony.getResourceService();
        
        // --- Foragers ---
        if (colony.hasUpgrade(GameUnlocks.ROLE_FORAGER)) {
            List<Ant> foragers = getWorkingAnts(colony, GameConstants.ROLE_FORAGER);
            if (!foragers.isEmpty()) {
                for(Ant a : foragers) a.clearLoad();
                
                int totalPower = AntSubtypeService.sumCollectingPower(colony, foragers);
                List<ResourceSource> plantSources = locations.getSourcesByType(GameConstants.RESOURCE_PLANT);
                List<ResourceSource> waterSources = locations.getSourcesByType(GameConstants.RESOURCE_WATER);
                List<ResourceSource> fungiSources = colony.hasUpgrade(GameUnlocks.STAT_SCOUTING_2)
                        ? locations.getSourcesByType(GameConstants.RESOURCE_FUNGI) : List.of();
                List<ResourceSource> resinSources = colony.hasUpgrade(GameUnlocks.STAT_SCOUTING_3)
                        ? locations.getSourcesByType(GameConstants.RESOURCE_RESIN) : List.of();

                boolean canCollectPlants = !plantSources.isEmpty() && resources.hasCapacity(colony, GameConstants.RESOURCE_PLANT);
                boolean canCollectWater = !waterSources.isEmpty() && resources.hasCapacity(colony, GameConstants.RESOURCE_WATER);
                boolean canCollectFungi = !fungiSources.isEmpty() && resources.hasCapacity(colony, GameConstants.RESOURCE_FUNGI);
                boolean canCollectResin = !resinSources.isEmpty() && colony.hasUpgrade(GameUnlocks.ABILITY_RESIN)
                        && resources.hasCapacity(colony, GameConstants.RESOURCE_RESIN);

                int activeTypes = (canCollectPlants ? 1 : 0) + (canCollectWater ? 1 : 0)
                        + (canCollectFungi ? 1 : 0) + (canCollectResin ? 1 : 0);
                if (activeTypes > 0) {
                    int share = totalPower / activeTypes;
                    int remainder = totalPower;
                    if (canCollectPlants) {
                        int gathered = processGathering(colony, plantSources, share, GameConstants.RESOURCE_PLANT, foragers);
                        remainder = remainder - share + (share - gathered);
                    }
                    if (canCollectWater) {
                        int power = activeTypes == 1 ? remainder : share;
                        int gathered = processGathering(colony, waterSources, power, GameConstants.RESOURCE_WATER, foragers);
                        remainder = remainder - power + (power - gathered);
                    }
                    if (canCollectFungi) {
                        int power = (canCollectPlants || canCollectWater) ? share : remainder;
                        processGathering(colony, fungiSources, power, GameConstants.RESOURCE_FUNGI, foragers);
                    }
                    if (canCollectResin) {
                        int power = activeTypes == 1 ? totalPower : share;
                        processGathering(colony, resinSources, power, GameConstants.RESOURCE_RESIN, foragers);
                    }
                }
            }
        }
        
        // --- Hunters ---
        if (colony.hasUpgrade(GameUnlocks.ROLE_HUNTER)) {
            List<Ant> hunters = getWorkingAnts(colony, GameConstants.ROLE_HUNTER);
            if (!hunters.isEmpty()) {
                for(Ant a : hunters) a.clearLoad();
                int totalPower = (int) (hunters.size() * stats.getCollectingRate(colony));
                List<ResourceSource> sources = locations.getSourcesByType(GameConstants.RESOURCE_MEAT);
                processGathering(colony, sources, totalPower, GameConstants.RESOURCE_MEAT, hunters);
            }
        }

        // --- Miners ---
        if (colony.hasUpgrade(GameUnlocks.ROLE_MINER)) {
            List<Ant> miners = getWorkingAnts(colony, GameConstants.ROLE_MINER);
            if (!miners.isEmpty()) {
                for(Ant a : miners) a.clearLoad();
                if (GameRandom.nextDouble() < GameNumbers.MINING_GATHER_SUCCESS_CHANCE) {
                    int totalPower = (int) (miners.size() * stats.getCollectingRate(colony));
                    List<ResourceSource> sources = locations.getSourcesByType(GameConstants.RESOURCE_ROCK);
                    processGathering(colony, sources, totalPower, GameConstants.RESOURCE_ROCK, miners);
                }
            }
        }

        // --- Passive Water ---
        if (colony.hasBuilding(GameUnlocks.PASSIVE_WATER)) {
            double maxWater = stats.getWaterCapacity(colony);
            double dailyPct = colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1) ? 0.20 : 0.10;
            double gain = (maxWater * dailyPct) / 24.0;
            resources.addResource(colony, GameConstants.RESOURCE_WATER, gain);
        }
    }

    public void runConverting(Colony colony) {
        ColonyStatsService stats = colony.getStatsService();
        ColonyResourceService resources = colony.getResourceService();
        
        int farmerCount = colony.getActiveRoleCount(GameConstants.ROLE_FARMER);
        if (colony.hasBuilding(GameUnlocks.PASSIVE_FARM)) {
            if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) {
                farmerCount += 2;
            } else { farmerCount += 1; } 
        }
        
        if (farmerCount <= 0 || !resources.hasCapacity(colony, GameConstants.RESOURCE_FUNGI)) return;
        
        double scale = 1.0;
        double chance = stats.getConversionRate(colony);
        
        if (GameRandom.nextDouble() <= chance * scale || chance * scale >= 1.0) {
            double amount = farmerCount * (chance * scale >= 1.0 ? chance * scale : 1.0);
            
            double consumedPlants = resources.consumeResource(colony, GameConstants.RESOURCE_PLANT, amount);
            if (consumedPlants > 0) {
                double yield = consumedPlants;
                if (colony.hasUpgrade(GameUnlocks.ASSIMILATED_FARMING)) {
                    yield += consumedPlants;
                }
                resources.addResource(colony, GameConstants.RESOURCE_FUNGI, yield);
            }
            
            double consumedMeat = resources.consumeResource(colony, GameConstants.RESOURCE_MEAT, amount);
            if (consumedMeat > 0) {
                double yield = consumedMeat * 2;
                if (colony.hasUpgrade(GameUnlocks.ASSIMILATED_FARMING)) {
                    yield += consumedMeat;
                }
                resources.addResource(colony, GameConstants.RESOURCE_FUNGI, yield);
            }
        }
    }

    public void runRanching(Colony colony) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) return;
                
        ColonyResourceService resources = colony.getResourceService();
        int syrupGain = (int) (colony.getAphids()); 
        double plantConsumption = syrupGain * 0.10; 

        double actualPlantsConsumed = resources.consumeResource(colony, GameConstants.RESOURCE_PLANT, plantConsumption);
        
        if (actualPlantsConsumed > 0) {
            double ratio = actualPlantsConsumed / plantConsumption;
            resources.addResource(colony, GameConstants.RESOURCE_SYRUP, syrupGain * ratio);
        }
    }

    public void runCaughtBugs(Colony colony, Biome biome) {
        colony.getBugHandlingService().runDaily(colony, biome);
    }

    public void runLaying(Colony colony) {
        int layerCount = colony.getActiveRoleCount(GameConstants.ROLE_LAYER);
        
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
        } else if (colony.hasBuilding(GameUnlocks.PASSIVE_LAB)) { 
            nurseCount += 1; 
        }
        
        int babyAntTotal = colony.getEggs().size() +  colony.getLarvae().size() +  colony.getPupae().size();
        float nursingRate = stats.getNursingRate(colony);
        
        for (Ant nurse : nurses) {
            nurse.clearLoad();
            if (babyAntTotal > 0 && GameRandom.nextDouble() < 0.30) {
                double r = GameRandom.nextDouble();
                if (r < 0.33) nurse.setCarryingAnt(GameConstants.TYPE_EGG);
                else if (r < 0.66) nurse.setCarryingAnt(GameConstants.TYPE_LARVA);
                else nurse.setCarryingAnt(GameConstants.TYPE_PUPA);
            }
        }

        int capacity = (int) (nurseCount * nursingRate);
        if (babyAntTotal <= capacity) {
            return;
        }

        int deficit = babyAntTotal - capacity;
        List<AntType> killOrder = List.of(GameConstants.TYPE_EGG, GameConstants.TYPE_LARVA, GameConstants.TYPE_PUPA);
        int deathCount = 0;

        for (AntType typeToKill : killOrder) {
            if (deficit <= 0) break;
            List<Ant> list = colony.getAntsByType(typeToKill);
            int toCullCount = Math.min(deficit, list.size());
            
            List<Ant> antsToCull = new ArrayList<>();
            for (int i = 0; i < toCullCount; i++) {
                antsToCull.add(list.get(list.size() - 1 - i));
            }
            
            for (Ant antToCull : antsToCull) {
                if (antToCull.isAlive()) {
                    AntType originalType = antToCull.getAntType();
                    AntRole formerRole = antToCull.getRole();
                    boolean wasOnTrade = antToCull.isOnTrade();
                    antToCull.goDie(colony, DeathCause.LACK_OF_CARE);
                    colony.recordAntDeath(antToCull, DeathCause.LACK_OF_CARE);
                    colony.handleAntCasualtyAftermath(antToCull, originalType, formerRole, wasOnTrade);
                    
                    list.remove(antToCull);
                    deathCount++;
                    deficit--;
                }
            }
        }
        
        if (deathCount > 0) {
            colony.logEvent(ColonyLogPrefixes.DEATH + " "
                + String.format(LanguageStrings.get(LanguageStrings.LOG_DEATH_JUVENILES_LACK_CARE_FMT), deathCount));
        }
    }
    
    public void runSpreading(Colony colony, int potentialSatellites, World world, Hex currentHex) { 
        if (potentialSatellites <= 0 || world == null || currentHex == null) return;
        
        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null) return;

        int satellitesToSpawn = Math.min(potentialSatellites, colony.getStatsService().getSpreadingLimit(colony));

        List<Hex> neighbors = new ArrayList<>();
        neighbors.add(currentHex.getNorth());
        neighbors.add(currentHex.getNorthEast());
        neighbors.add(currentHex.getSouthEast());
        neighbors.add(currentHex.getSouth());
        neighbors.add(currentHex.getSouthWest());
        neighbors.add(currentHex.getNorthWest());
        
        neighbors.removeIf(h -> h == null);
        Collections.shuffle(neighbors);
        
        int satellitesSpawned = 0;
        ColonyStarterService starter = ColonyStarterService.shared();
        
        for (Hex neighbor : neighbors) {
            if (satellitesSpawned >= satellitesToSpawn) break; 
            
            if (neighbor.getBiome() == GameConstants.BIOME_OCEAN || neighbor.getBiome() == GameConstants.BIOME_LAKE) {
                continue;
            }
            if (neighbor.isIsland()) {
                continue;
            }
            
            Colony existingColony = neighbor.getColony();
            boolean reclaimable = ColonyStarterService.isReclaimableDeadColony(existingColony);
            
            if (existingColony == null || reclaimable) {
                
                if (reclaimable) {
                    starter.reclaimDeadColonyForSpread(neighbor, dynasty, colony);
                }

                double integrity = dynasty.getGeneticIntegrity();
                double failureChance = 0.0;
                if (integrity <= 0) failureChance = 0.80;
                else if (integrity <= 20) failureChance = 0.40;
                else if (integrity <= 40) failureChance = 0.20;
                else if (integrity <= 60) failureChance = 0.10;
                else if (integrity <= 80) failureChance = 0.05;

                if (GameRandom.nextDouble() < failureChance) {
                    colony.logEvent(ColonyLogPrefixes.FAILURE + " " + LanguageStrings.get(LanguageStrings.LOG_FAILURE_SATELLITE));
                    continue;
                }

                int newId = world.getNextColonyId();

                Colony satellite = new Colony(newId, "", colony.isPlayer());
                satellite.setDynasty(dynasty); 
                satellite.setActive(false); 
                satellite.setAutomationEnabled(!colony.isPlayer()); 
                satellite.setRank(GameConstants.RANK_COLONY);
                
                starter.initializeNewColony(satellite);

                Colony primaryColony = null;
                for (Colony c : dynasty.getColonies()) {
                    if (c.isCapital()) {
                        primaryColony = c;
                        break;
                    }
                }

                if (primaryColony != null) {
                    satellite.setHatchRateWorker(primaryColony.getHatchRateWorker());
                    satellite.setHatchRateSoldier(primaryColony.getHatchRateSoldier());
                    satellite.setHatchRateMajor(primaryColony.getHatchRateMajor());
                    satellite.setHatchRateDrone(primaryColony.getHatchRateDrone());
                    satellite.setHatchRatePrincess(primaryColony.getHatchRatePrincess());
                    AntSubtypeService.copySubtypeRates(satellite, primaryColony);
                } else {
                    satellite.setHatchRateWorker(colony.getHatchRateWorker());
                    satellite.setHatchRateSoldier(colony.getHatchRateSoldier());
                    satellite.setHatchRateMajor(colony.getHatchRateMajor());
                    satellite.setHatchRateDrone(colony.getHatchRateDrone());
                    satellite.setHatchRatePrincess(colony.getHatchRatePrincess());
                    AntSubtypeService.copySubtypeRates(satellite, colony);
                }
                
                neighbor.setColony(satellite);
                
                satellitesSpawned++;
                if (world != null && dynasty != null) {
                    world.getHistoryService().record(WorldHistoryEventType.COLONY_FOUNDED,
                            LanguageStrings.HISTORY_COLONY_FOUNDED_FMT,
                            dynasty.getId(), satellite.getId(), -1,
                            WorldHistoryEvent.colonyArg(satellite.getId()),
                            WorldHistoryEvent.dynastyArg(dynasty.getId()));
                }
                colony.logEvent(ColonyLogPrefixes.INFO + " "
                    + String.format(LanguageStrings.get(LanguageStrings.LOG_SATELLITE_AT_FMT), neighbor.getQ(), neighbor.getR()));
            }
        }
        
        if (satellitesSpawned > 0) {
            colony.logEvent(ColonyLogPrefixes.INFO + " "
                + String.format(LanguageStrings.get(LanguageStrings.LOG_SATELLITES_ESTABLISHED_FMT), satellitesSpawned));
        } else {
            colony.logEvent(ColonyLogPrefixes.INFO + " " + LanguageStrings.get(LanguageStrings.LOG_SPREADING_FAILED));
        }
    }

    public void runPolicing(Colony colony) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_POLICE)) return;
        
        int parasiteCount = colony.getParasiteAnts();
        if (parasiteCount == 0) return;
        
        List<Ant> police = getWorkingAnts(colony, GameConstants.ROLE_POLICE);
        if (police.isEmpty()) return;
        
        float detectionRate = colony.getStatsService().getParasiteDetection(colony);
        int parasiteAntsKilled = 0;
        
        for (Ant officer : police) {
            if (parasiteAntsKilled >= parasiteCount) break;
            
            if (GameRandom.nextFloat() < detectionRate) {
                parasiteAntsKilled++;
                colony.getResourceService().addResource(colony, GameConstants.RESOURCE_MEAT, 4);
            }
        }
        
        if (parasiteAntsKilled > 0) {
            colony.logEvent(ColonyLogPrefixes.INFO + " "
                + String.format(LanguageStrings.get(LanguageStrings.LOG_PARASITE_ANTS_ELIMINATED_FMT), parasiteAntsKilled));
            colony.setParasiteAnts(Math.max(0, colony.getParasiteAnts() - parasiteAntsKilled));
        }
    }

    public void runScoutting(Colony colony, Biome biome, Hex currentHex) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_SCOUT)) return;

        int scoutCount = colony.runsFullSimulation()
                ? colony.getActiveRoleCount(GameConstants.ROLE_SCOUT)
                : colony.getAssignedRoleCount(GameConstants.ROLE_SCOUT);
        if (scoutCount == 0) return;

        float chancePerScout = colony.getStatsService().getScoutingRate(colony); 
        float totalChance = scoutCount * chancePerScout;

        while (totalChance > 0) {
            boolean found = false;
            
            if (totalChance >= 1.0f) {
                found = true;
                totalChance -= 1.0f;
            } else {
                if (GameRandom.nextFloat() < totalChance) {
                    found = true;
                }
                totalChance = 0; 
            }

            if (found) {
                generateAndAddSource(colony, biome, currentHex);
            }
        }
    }

    private void generateAndAddSource(Colony colony, Biome biome, Hex currentHex) {
        ColonyLocationService locations = colony.getLocationService();
        List<ResourceType> possibleTypes = new ArrayList<>();
        
        if (colony.hasUpgrade(GameUnlocks.ROLE_FORAGER) && !locations.isSourceFull(colony, GameConstants.RESOURCE_PLANT)) {
            possibleTypes.add(GameConstants.RESOURCE_PLANT);
        }
        if (colony.hasUpgrade(GameUnlocks.ROLE_HUNTER) && !locations.isSourceFull(colony, GameConstants.RESOURCE_MEAT)) {
            possibleTypes.add(GameConstants.RESOURCE_MEAT);
        }
        if (colony.hasUpgrade(GameUnlocks.ROLE_MINER) && !locations.isSourceFull(colony, GameConstants.RESOURCE_ROCK)
                && biome != null && biome.getMineralAbundance() > 0) {
            possibleTypes.add(GameConstants.RESOURCE_ROCK);
        }
        if (colony.hasUpgrade(GameUnlocks.STAT_SCOUTING_2) && !locations.isSourceFull(colony, GameConstants.RESOURCE_FUNGI)
                && biome != null && biome.getMushroomAbundance() > 0) {
            possibleTypes.add(GameConstants.RESOURCE_FUNGI);
        }
        if (colony.hasUpgrade(GameUnlocks.STAT_SCOUTING_3) && !locations.isSourceFull(colony, GameConstants.RESOURCE_RESIN)
                && biome != null && biome.getResinAbundance() > 0) {
            possibleTypes.add(GameConstants.RESOURCE_RESIN);
        }
        if (!locations.isSourceFull(colony, GameConstants.RESOURCE_WATER)) {
            possibleTypes.add(GameConstants.RESOURCE_WATER); 
        }

        if (possibleTypes.isEmpty()) return;
        ResourceType selectedType = possibleTypes.get(GameRandom.nextInt(possibleTypes.size()));
        
        float abundance = 0f;
        
        if (biome != null) {
            if (selectedType == GameConstants.RESOURCE_PLANT) abundance = biome.getPlantAbundance();
            else if (selectedType == GameConstants.RESOURCE_MEAT) abundance = biome.getAnimalAbundance();
            else if (selectedType == GameConstants.RESOURCE_ROCK) abundance = biome.getMineralAbundance();
            else if (selectedType == GameConstants.RESOURCE_FUNGI) abundance = biome.getMushroomAbundance();
            else if (selectedType == GameConstants.RESOURCE_RESIN) abundance = biome.getResinAbundance();
            else if (selectedType == GameConstants.RESOURCE_WATER) {
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
        float roll = GameRandom.nextFloat();
        
        if (roll < abundance) {
            quantity = 100000; // Huge
        } else {
            float subRoll = GameRandom.nextFloat();
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

        if (gameW <= 100) gameW = 2560;
        if (gameH <= 100) gameH = 1440;
        
        int maxDepl = colony.hasUpgrade(GameUnlocks.STAT_HEX_SUSTAIN)
                ? GameNumbers.HEX_SUSTAIN_MAX_DEPLETION_PCT
                : 100;
        int depletionPct = currentHex != null ? currentHex.getResourceDepletionPercentCapped(maxDepl) : 0;
        int depletionExtra = (int) Math.round(
            (depletionPct / 100.0) * GameNumbers.HEX_DEPLETION_SPAWN_BUFFER_EXTRA_MAX);
        depletionExtra = Math.min(depletionExtra, GameNumbers.RESOURCE_SPAWN_BUFFER_EXTRA_CAP);

        int extraMin = GameNumbers.RESOURCE_SPAWN_EXTRA_DISTANCE_MIN + depletionExtra;
        int extraMax = GameNumbers.RESOURCE_SPAWN_EXTRA_DISTANCE_MAX + depletionExtra;
        int extraDistance = extraMin + GameRandom.nextInt(Math.max(1, extraMax - extraMin));

        int displayPx = selectedType.getDisplaySizeForSourceQuantity(quantity);
        NeoPoint entrance = colony.getLocationService().getColonyEntrance(colony);
        Point center = ResourceSourcePlacement.pickSpawnCenter(
                entrance, gameW, gameH, displayPx, extraDistance);
        Point topLeft = ResourceSourcePlacement.topLeftFromCenter(center.x, center.y, displayPx);

        ResourceSource source = new ResourceSource(selectedType, quantity, topLeft.x, topLeft.y);
        if (colony.getLocationService().addSource(colony, source) && currentHex != null) {
            currentHex.recordGeneratedResourceSource(selectedType);
        }
    }

    public void runNuptial(Colony colony, World world, Hex currentHex) {
        if (!colony.hasUpgrade(GameUnlocks.TYPE_PRINCESS)) {
            return;
        }

        colony.runRoleAssignment(null);

        List<Ant> allDrones = collectAvailableDrones(colony);
        List<Ant> breederPrincesses = collectBreederPrincesses(colony);
        if (allDrones.isEmpty() || breederPrincesses.isEmpty()) {
            return;
        }

        int potentialQueens = Math.min(allDrones.size(), breederPrincesses.size());
        int currentQueens = colony.getQueens().size();
        int maxQueens = colony.getStatsService().getQueensCapacity(colony);
        int spaceAvailable = maxQueens - currentQueens;
        int queensToAdd = 0;
        int queensLeaving = 0;
        boolean queenless = currentQueens == 0;

        if (spaceAvailable > 0) {
            queensToAdd = Math.min(spaceAvailable, potentialQueens);
            if (!queenless) {
                queensLeaving = potentialQueens - queensToAdd;
            }
        } else if (!queenless) {
            queensLeaving = potentialQueens;
        }

        if (queenless && queensToAdd <= 0) {
            return;
        }

        int pairsToFly = queenless ? queensToAdd : potentialQueens;
        for (int i = 0; i < pairsToFly; i++) {
            allDrones.get(i).setNuptial(true);
            breederPrincesses.get(i).setNuptial(true);
        }

        for (int i = 0; i < queensToAdd; i++) {
            Ant breeder = breederPrincesses.get(i);
            Ant newQueen = new Ant(colony, GameConstants.TYPE_QUEEN);
            AntSubtypeService.inheritSubtype(breeder, newQueen, colony);
            newQueen.setDimension(WorldSpaces.UNDERWORLD);

            Rectangle royal = colony.getPhysicsService().getRoomBounds(colony, WorldSpaces.ROYAL_CHAMBER);
            if (royal != null) {
                newQueen.setPosition(colony.getPhysicsService().getSpecificRoomPoint(colony, royal));
            }

            colony.getQueens().add(newQueen);
        }

        if (queensToAdd > 0) {
            colony.setDaysWithoutQueen(0);
        }

        colony.logEvent(ColonyLogPrefixes.NUPTIAL + " "
            + String.format(LanguageStrings.get(LanguageStrings.LOG_NUPTIAL_QUEENS_FMT), queensToAdd));

        if (colony.getDynasty() != null) {
            colony.getDynasty().incrementNuptialFlights();
        }
        if (world != null) {
            Dynasty dynasty = colony.getDynasty();
            world.getHistoryService().record(WorldHistoryEventType.NUPTIAL_FLIGHT,
                    LanguageStrings.HISTORY_NUPTIAL_FLIGHT_FMT,
                    dynasty != null ? dynasty.getId() : -1, colony.getId(), -1,
                    WorldHistoryEvent.colonyArg(colony.getId()),
                    WorldHistoryEvent.plainArg(queensToAdd),
                    WorldHistoryEvent.plainArg(queensLeaving));
        }

        if (queensLeaving > 0) {
            runSpreading(colony, queensLeaving, world, currentHex);
        }
    }

    public static boolean meetsNuptialRequirements(Colony colony) {
        if (colony == null || !colony.hasUpgrade(GameUnlocks.TYPE_PRINCESS)) {
            return false;
        }
        colony.runRoleAssignment(null);
        return !collectAvailableDrones(colony).isEmpty()
                && !collectBreederPrincesses(colony).isEmpty();
    }

    private static List<Ant> collectAvailableDrones(Colony colony) {
        List<Ant> drones = new ArrayList<>();
        for (Ant drone : colony.getDrones()) {
            if (!drone.isOnTrade() && !drone.isNuptial()) {
                drones.add(drone);
            }
        }
        return drones;
    }

    private static List<Ant> collectBreederPrincesses(Colony colony) {
        List<Ant> breeders = new ArrayList<>();
        for (Ant princess : colony.getPrincesses()) {
            if (princess.getRole() == GameConstants.ROLE_BREEDER
                    && !princess.isOnTrade() && !princess.isNuptial()) {
                breeders.add(princess);
            }
        }
        if (!breeders.isEmpty()) {
            return breeders;
        }

        int quota = colony.getAssignedRoleCount(GameConstants.ROLE_BREEDER);
        if (quota <= 0 || !colony.hasUpgrade(GameUnlocks.ROLE_BREEDER)) {
            return breeders;
        }

        for (Ant princess : colony.getPrincesses()) {
            if (princess.isOnTrade() || princess.isNuptial()) {
                continue;
            }
            princess.setRole(GameConstants.ROLE_BREEDER);
            breeders.add(princess);
            if (breeders.size() >= quota) {
                break;
            }
        }
        return breeders;
    }

    public static boolean establishQueenFromBreederPair(Colony capital, Colony target) {
        if (capital == null || target == null || capital == target) {
            return false;
        }
        if (!capital.hasUpgrade(GameUnlocks.TYPE_PRINCESS) || !capital.hasUpgrade(GameUnlocks.ROLE_BREEDER)) {
            return false;
        }

        Ant drone = null;
        for (Ant candidate : capital.getDrones()) {
            if (!candidate.isOnTrade() && !candidate.isNuptial()) {
                drone = candidate;
                break;
            }
        }
        Ant breeder = null;
        for (Ant candidate : capital.getPrincesses()) {
            if (candidate.getRole() == GameConstants.ROLE_BREEDER
                    && !candidate.isOnTrade() && !candidate.isNuptial()) {
                breeder = candidate;
                break;
            }
        }
        if (drone == null || breeder == null) {
            return false;
        }

        capital.getDrones().remove(drone);
        capital.getPrincesses().remove(breeder);

        Ant queen = new Ant(target, GameConstants.TYPE_QUEEN);
        queen.setDimension(WorldSpaces.UNDERWORLD);
        queen.setRole(GameConstants.ROLE_LAYER);
        Rectangle royal = target.getPhysicsService().getRoomBounds(target, WorldSpaces.ROYAL_CHAMBER);
        if (royal != null) {
            queen.setPosition(target.getPhysicsService().getSpecificRoomPoint(target, royal));
        }
        target.getQueens().add(queen);
        target.setDaysWithoutQueen(0);
        target.setPeaceAssignedRoleCount(GameConstants.ROLE_LAYER,
                target.getPeaceAssignedRoleCount(GameConstants.ROLE_LAYER) + 1);
        ColonyStarterService.shared().reestablishCapturedColony(capital, target);
        ColonyMilitaryService.refreshColonyMilitaryPower(capital);
        return true;
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
        graverCount += colony.getBugHandlingService().getDermestidGraveBonus(colony);
        
        boolean hasBodies = !colony.getDeadAnts().isEmpty();
        for (Ant graver : gravers) {
            graver.clearLoad();
            if (hasBodies && GameRandom.nextDouble() < 0.5) {
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
        int potentialCompost = (int) colony.getStatsService().getGravingRate(colony) * gravers.size();
        
        if (potentialCompost == 0 || deadAnts.isEmpty()) return;

        int actualToCompost = Math.min(potentialCompost, deadAnts.size());
        
        List<Ant> compostedAnts = new ArrayList<>();
        for(int i = 0; i < actualToCompost; i++) {
            compostedAnts.add(deadAnts.get(i));
        }
        deadAnts.removeAll(compostedAnts);

        int mushroomGain = actualToCompost * 4; 
        colony.getResourceService().addResource(colony, GameConstants.RESOURCE_FUNGI, mushroomGain);
        
        if (actualToCompost > 0) {
            colony.logEvent(ColonyLogPrefixes.COMPOST + " "
                + String.format(LanguageStrings.get(LanguageStrings.LOG_COMPOST_RECYCLED_FMT), actualToCompost));
        }
    }

    public void runResearch(Colony colony) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER)) return;
        
        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null) return;

        int researcherCount = colony.getActiveRoleCount(GameConstants.ROLE_RESEARCHER);        
        if (colony.hasBuilding(GameUnlocks.PASSIVE_LAB)) {
            if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) {
                researcherCount += 2;
            } else { 
                researcherCount += 1; 
            }
        }

        int assistantCount = colony.getActiveRoleCount(GameConstants.ROLE_ASSISTANT);        
        if (researcherCount > 0 || assistantCount > 0) {
            int speed = colony.getStatsService().getResearchSpeed(colony);
            
            if (dynasty.getCurrentAssimilation() != null) {
                double power = (researcherCount * speed + assistantCount * (speed / (double) GameNumbers.RESEARCH_ASSISTANT_EFFICIENCY_DIVISOR)) / 10.0;
                dynasty.addAssimilationProgress(power);
                
                if (dynasty.getAssimilationProgress() >= dynasty.getAssimilationTargetCost()) {
                    Assimilation a = dynasty.getCurrentAssimilation();
                    dynasty.unlockUpgrade(a.getReward());
                    dynasty.completeAssimilation(a);
                    colony.logEvent(ColonyLogPrefixes.SUCCESS + " "
                        + String.format(LanguageStrings.get(LanguageStrings.LOG_SUCCESS_ASSIMILATION_FMT),
                            a.getName(), a.getReward().getFlavorName()));
                    
                    dynasty.setCurrentAssimilation(null);
                    dynasty.setAssimilationProgress(0);
                }
            } else {
                int queenGain = researcherCount * speed;
                int assistantGain = (int) (assistantCount * (speed / (double) GameNumbers.RESEARCH_ASSISTANT_EFFICIENCY_DIVISOR));
                colony.addResearchPoints(queenGain + assistantGain);
            }
        }
    }

    public void runBuilding(Colony colony) {
        if (colony.getCurrentBuildingProject() == null) return;

        double efficiency = colony.getStatsService().getConstructionEfficiency(colony);
        if (efficiency <= 0) return;

        colony.setBuildingProgressHours(colony.getBuildingProgressHours() + 1.0);
        double requiredHours = colony.getCurrentBuildingProject().getBuildTime() / efficiency;

        if (colony.getBuildingProgressHours() >= requiredHours) {
            colony.unlockBuilding(colony.getCurrentBuildingProject());
            colony.logEvent(ColonyLogPrefixes.SUCCESS + " " + colony.getCurrentBuildingProject().getName());
            colony.setCurrentBuildingProject(null);
            colony.setBuildingProgressHours(0.0);
        }
    }

    public void runTunnelConstruction(Colony colony) {
        Tunnel tunnel = colony.getCurrentTunnelProject();
        if (tunnel == null || tunnel.isComplete()) {
            colony.setCurrentTunnelProject(null);
            return;
        }

        int engineers = colony.getAssignedRoleCount(GameConstants.ROLE_ENGINEER);
        int borers = colony.getAssignedRoleCount(GameConstants.ROLE_BORER);

        if (engineers <= 0 && borers <= 0) return;

        double hourlyProgress = (engineers * 1.0) + (borers * 100.0);
        tunnel.addProgress(hourlyProgress);

        if (tunnel.isComplete()) {
            colony.logEvent(ColonyLogPrefixes.SUCCESS + " " + LanguageStrings.get(LanguageStrings.LOG_SUCCESS_TUNNEL));
            colony.setCurrentTunnelProject(null);
        }
    }
}
