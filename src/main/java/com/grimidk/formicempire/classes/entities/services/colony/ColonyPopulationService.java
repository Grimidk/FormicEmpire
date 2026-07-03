package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogTexts;
import com.grimidk.formicempire.classes.infrasctructure.registries.DeathCause;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.world.Temperature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ColonyPopulationService {

    // --- Role Management ---
    private void assignRolesForType(Colony colony, List<Ant> ants, AntType type, Engine engine) {
        AntRole defaultRole = Engine.resolveDefaultRoleForAntType(type, engine);
        if (defaultRole == null) return;
        
        List<Ant> tradeAnts = new ArrayList<>();
        List<Ant> availableAnts = new ArrayList<>();
        
        for (Ant ant : ants) {
            if (ant.isOnTrade()) {
                tradeAnts.add(ant);
            } else {
                ant.setRole(defaultRole);
                availableAnts.add(ant);
            }
        }
        
        Map<AntRole, Integer> assignedRoleCounts = colony.getAssignedRoleCounts();
        
        for (Map.Entry<AntRole, Integer> entry : assignedRoleCounts.entrySet()) {
            AntRole role = entry.getKey();
            if (role.getAntType() != type) continue;
            if (role.equals(defaultRole)) continue; 
            
            int desiredCount = entry.getValue();
            
            int currentlyOnTradeWithThisRole = 0;
            for (Ant ta : tradeAnts) {
                if (ta.getRole() != null && ta.getRole().equals(role)) {
                    currentlyOnTradeWithThisRole++;
                }
            }
            
            int neededCount = Math.max(0, desiredCount - currentlyOnTradeWithThisRole);
            int assignedCount = 0;
            Iterator<Ant> antIterator = availableAnts.iterator();
            
            while (assignedCount < neededCount && antIterator.hasNext()) {
                Ant antToAssign = antIterator.next();
                antToAssign.setRole(role); 
                antIterator.remove();
                assignedCount++;
            }
        }
    }

    public void runRoleAssignment(Colony colony, Engine engine) {
        assignRolesForType(colony, colony.getWorkers(), GameConstants.TYPE_WORKER, engine);
        assignRolesForType(colony, colony.getSoldiers(), GameConstants.TYPE_SOLDIER, engine);
        assignRolesForType(colony, colony.getMajors(), GameConstants.TYPE_MAJOR, engine);
        assignRolesForType(colony, colony.getPrincesses(), GameConstants.TYPE_PRINCESS, engine);
        assignRolesForType(colony, colony.getQueens(), GameConstants.TYPE_QUEEN, engine);
    }

    // --- Hatching & Lifecycle ---
    private AntType determineHatchType(Colony colony) {
        double rand = GameRandom.nextDouble() * 100.0;
        double cumulative = 0.0;
        
        cumulative += colony.getHatchRateWorker();
        if (rand < cumulative) return GameConstants.TYPE_WORKER;
        
        if (colony.hasUpgrade(GameUnlocks.TYPE_SOLDIER)) {
            cumulative += colony.getHatchRateSoldier();
            if (rand < cumulative) return GameConstants.TYPE_SOLDIER;
        }
        if (colony.hasUpgrade(GameUnlocks.TYPE_MAJOR)) {
            cumulative += colony.getHatchRateMajor();
            if (rand < cumulative) return GameConstants.TYPE_MAJOR;
        }
        if (colony.hasUpgrade(GameUnlocks.TYPE_PRINCESS)) {
            cumulative += colony.getHatchRateDrone();
            if (rand < cumulative) return GameConstants.TYPE_DRONE;
            cumulative += colony.getHatchRatePrincess();
            if (rand < cumulative) return GameConstants.TYPE_PRINCESS;
        }
        return GameConstants.TYPE_WORKER;
    }

    private void evolveAnts(Colony colony, List<Ant> sourceList, List<Ant> destList, AntType newType) {
        List<Ant> antsToEvolve = new ArrayList<>();
        int growthTime = colony.getStatsService().getGrowthTime(colony);
        
        for (Ant ant : sourceList) {
            if (ant.getAge() >= growthTime) {
                antsToEvolve.add(ant);
            }
        }
        for (Ant ant : antsToEvolve) {
            ant.transform(colony, newType); 
            destList.add(ant);
        }
        sourceList.removeAll(antsToEvolve);
    }

    private void hatchPupae(Colony colony) {
        List<Ant> pupaeToHatch = new ArrayList<>();
        int growthTime = colony.getStatsService().getGrowthTime(colony);
        
        for (Ant pupa : colony.getPupae()) {
            if (pupa.getAge() >= growthTime) {
                pupaeToHatch.add(pupa);
            }
        }
        for (Ant pupa : pupaeToHatch) {
            AntType newType = determineHatchType(colony);
            pupa.transform(colony, newType);
            colony.getAntsByType(newType).add(pupa);
        }
        colony.getPupae().removeAll(pupaeToHatch);
    }

    private void applyAutomatedHatchRates(Colony colony) {
        float s = colony.hasUpgrade(GameUnlocks.TYPE_SOLDIER) ? 15.0f : 0f;
        float m = colony.hasUpgrade(GameUnlocks.TYPE_MAJOR) ? 5.0f : 0f;
        float p = 0f;
        float d = 0f;
        
        if (colony.hasUpgrade(GameUnlocks.TYPE_PRINCESS)) {
            p = 4.0f;
            d = 1.0f;
        }

        float w = 100.0f - (s + m + p + d);

        colony.setHatchRate(GameConstants.TYPE_WORKER, w);
        colony.setHatchRate(GameConstants.TYPE_SOLDIER, s);
        colony.setHatchRate(GameConstants.TYPE_MAJOR, m);
        colony.setHatchRate(GameConstants.TYPE_PRINCESS, p);
        colony.setHatchRate(GameConstants.TYPE_DRONE, d);
    }

    public void runHatching(Colony colony){
        if (colony.isAutomationEnabled() || (!colony.isPlayer() && colony.isCapital())) {
            applyAutomatedHatchRates(colony);
        }

        hatchPupae(colony);
        evolveAnts(colony, colony.getLarvae(), colony.getPupae(), GameConstants.TYPE_PUPA);
        evolveAnts(colony, colony.getEggs(), colony.getLarvae(), GameConstants.TYPE_LARVA);
    }

    public void runAging(Colony colony){
        List<Ant> antsToKill = new ArrayList<>();

        for (List<Ant> antList : colony.getAntGroups().values()) {
            for (Ant ant : antList) {
                ant.setAge(ant.getAge() + 1);
            }
        }
        
        int agedDeaths = 0;
        for (Ant ant : antsToKill) {
             if (ant.isAlive()) { 
                AntType originalType = ant.getAntType(); 
                ant.goDie(colony, DeathCause.OLD_AGE);
                colony.recordAntDeath(ant, DeathCause.OLD_AGE);
                
                List<Ant> antList = colony.getAntsByType(originalType);
                if (antList != null) antList.remove(ant);
                agedDeaths++;
            }
        }
        
        if (agedDeaths > 0) {
            colony.logEvent(ColonyLogPrefixes.DEATH + " "
                + String.format(LanguageStrings.get(LanguageStrings.LOG_DEATH_OLD_AGE_FMT), agedDeaths));
        }
    }
    
    public void runEating(Colony colony, Temperature currentTemp) {
        ColonyStatsService stats = colony.getStatsService();
        ColonyResourceService resources = colony.getResourceService();
        
        List<AntType> adultDrinkOrder = Arrays.asList(
            GameConstants.TYPE_DRONE, GameConstants.TYPE_PRINCESS, GameConstants.TYPE_MAJOR,
            GameConstants.TYPE_SOLDIER, GameConstants.TYPE_WORKER, GameConstants.TYPE_QUEEN
        );
        
        int resistanceChance = stats.getThirstResistance(colony, currentTemp);
        List<Ant> thirstyCandidates = new ArrayList<>();
        
        for (AntType type : adultDrinkOrder) {
            List<Ant> list = colony.getAntsByType(type);
            for (Ant ant : list) {
                if (GameRandom.nextInt(100) >= resistanceChance) {
                    thirstyCandidates.add(ant);
                }
            }
        }
        
        int waterNeeded = thirstyCandidates.size();
        double consumedWater = resources.consumeResource(colony, GameConstants.RESOURCE_WATER, waterNeeded);
        double waterDeficit = waterNeeded - consumedWater;
        
        if (waterDeficit > 0 && colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) {
            double syrupConsumed = resources.consumeResource(colony, GameConstants.RESOURCE_SYRUP, waterDeficit);
            waterDeficit -= syrupConsumed;
        }

        List<Ant> doomedThirsty = new ArrayList<>();
        for (int i = 0; i < (int) waterDeficit; i++) {
            if (i < thirstyCandidates.size()) {
                doomedThirsty.add(thirstyCandidates.get(i));
            }
        }

        List<AntType> eatOrder = Arrays.asList(
            GameConstants.TYPE_DRONE, GameConstants.TYPE_PRINCESS, GameConstants.TYPE_MAJOR,
            GameConstants.TYPE_SOLDIER, GameConstants.TYPE_LARVA, GameConstants.TYPE_WORKER, GameConstants.TYPE_QUEEN
        );
        
        List<Ant> hungryCandidates = new ArrayList<>();
        int foodNeeded = 0;
        int baseConsumption = stats.getBaseConsumption(colony);

        for (AntType type : eatOrder) {
            int consumptionPerAnt = (int) (type.getConsumptionMult() * baseConsumption);
            if (consumptionPerAnt <= 0) consumptionPerAnt = 1; 
            if (type == GameConstants.TYPE_EGG || type == GameConstants.TYPE_PUPA) continue;
            
            List<Ant> list = colony.getAntsByType(type);
            for (Ant ant : list) {
                foodNeeded += consumptionPerAnt;
                hungryCandidates.add(ant); 
            }
        }

        int parasiteCount = colony.getParasiteAnts();
        if (parasiteCount > 0) {
            foodNeeded += parasiteCount; 
        }

        double consumedFood = resources.consumeResource(colony, GameConstants.RESOURCE_FUNGI, foodNeeded);
        double foodDeficit = foodNeeded - consumedFood;

        if (foodDeficit > 0 && colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) {
            double syrupConsumed = resources.consumeResource(colony, GameConstants.RESOURCE_SYRUP, foodDeficit);
            foodDeficit -= syrupConsumed;
        }

        List<Ant> doomedHungry = new ArrayList<>();
        int approxAntsToKill = (int) (foodDeficit / (baseConsumption <= 0 ? 1 : baseConsumption));
        
        int added = 0;
        for (int i = 0; i < hungryCandidates.size() && added < approxAntsToKill; i++) {
            Ant potentialVictim = hungryCandidates.get(i);
            if (!doomedThirsty.contains(potentialVictim)) {
                doomedHungry.add(potentialVictim);
                added++;
            }
        }

        processDeaths(colony, doomedThirsty, DeathCause.DEHYDRATION);
        processDeaths(colony, doomedHungry, DeathCause.STARVATION);
    }

    private void processDeaths(Colony colony, List<Ant> ants, String cause) {
        int count = 0;
        for (Ant ant : ants) {
            if (ant.isAlive()) {
                AntType originalType = ant.getAntType();
                ant.goDie(colony, cause);
                colony.recordAntDeath(ant, cause);
                
                List<Ant> antList = colony.getAntsByType(originalType);
                if (antList != null) antList.remove(ant);
                count++;
            }
        }
        if (count > 0) {
            colony.logEvent(ColonyLogPrefixes.DEATH + " "
                + String.format(LanguageStrings.get(LanguageStrings.LOG_DEATH_COUNT_CAUSE_FMT),
                    count, ColonyLogTexts.localizedDeathCause(cause)));
        }
    }
    
    public void runContamination(Colony colony) { 
        int deadBodyCount = colony.getDeadAnts().size();
        int baseDeaths = 0;
        String contaminationLevel = "";

        if (deadBodyCount >= 5000) {
            baseDeaths = 100;
            contaminationLevel = "Massive";
        } else if (deadBodyCount >= 1500) {
            baseDeaths = 25;
            contaminationLevel = "Medium";
        } else if (deadBodyCount >= 500) {
            baseDeaths = 5;
            contaminationLevel = "Small";
        } else {
            return;
        }

        float mitigation = colony.getStatsService().getContaminationMitigation(colony);
        int finalDeaths = (int) (baseDeaths * mitigation);
        
        if (finalDeaths <= 0) return;

        List<Ant> victims = new ArrayList<>();
        List<AntType> killableTypes = Arrays.asList(GameConstants.TYPE_WORKER, GameConstants.TYPE_SOLDIER, GameConstants.TYPE_MAJOR);
        
        int killed = 0;
        for (AntType type : killableTypes) {
            if (killed >= finalDeaths) break;
            List<Ant> population = colony.getAntsByType(type);
            
            for (Ant ant : population) {
                if (killed >= finalDeaths) break;
                if (GameRandom.nextFloat() < 0.1) {
                    victims.add(ant);
                    killed++;
                }
            }
        }

        processDeaths(colony, victims, DeathCause.CONTAMINATION);
        if (killed > 0) {
            String levelLabel = switch (contaminationLevel) {
                case "Small" -> LanguageStrings.get(LanguageStrings.LOG_CONTAM_LEVEL_SMALL);
                case "Medium" -> LanguageStrings.get(LanguageStrings.LOG_CONTAM_LEVEL_MEDIUM);
                case "Massive" -> LanguageStrings.get(LanguageStrings.LOG_CONTAM_LEVEL_MASSIVE);
                default -> contaminationLevel;
            };
            colony.logEvent(ColonyLogPrefixes.INFO + " "
                + String.format(LanguageStrings.get(LanguageStrings.LOG_CONTAMINATION_LEVEL_FMT), levelLabel));
        }
    }

    public boolean isParasiteAntOutbreakEligible(Colony colony, Biome biome, Season season) {
        if (colony == null || biome == null || season == null) {
            return false;
        }
        if (!GameConstants.isParasiticAntSeason(season)) {
            return false;
        }
        if (!biome.hasNativeParasite(GameConstants.TYPE_PARASITE_ANT)) {
            return false;
        }
        return colony.getAntTotal() >= 1000;
    }

    public int calculateParasiteAntSpawnAmount(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int spawnAmount = Math.max(10, (int) (colony.getAntTotal() * 0.01));
        int existingParasiteAnts = colony.getParasiteAnts();
        if (existingParasiteAnts > 0) {
            spawnAmount += (int) (existingParasiteAnts * 0.50);
        }
        return Math.max(0, spawnAmount);
    }

    public int projectParasiteAntMonthlySpawn(Colony colony, Biome biome, Season season) {
        if (!isParasiteAntOutbreakEligible(colony, biome, season)) {
            return 0;
        }
        return calculateParasiteAntSpawnAmount(colony);
    }

    public int requiredPoliceToPreventParasiteAntOutbreak(Colony colony, Biome biome, Season season) {
        int spawn = projectParasiteAntMonthlySpawn(colony, biome, season);
        if (spawn <= 0) {
            return 0;
        }
        return spawn * GameConstants.PARASITE_OUTBREAK_PREVENTION_MULTIPLIER;
    }

    public boolean isParasiteAntOutbreakPrevented(Colony colony, Biome biome, Season season) {
        int required = requiredPoliceToPreventParasiteAntOutbreak(colony, biome, season);
        if (required <= 0) {
            return false;
        }
        return colony.getAssignedRoleCount(GameConstants.ROLE_POLICE) >= required;
    }

    public void runParasitation(Colony colony, Biome biome, Season season) {
        if (!isParasiteAntOutbreakEligible(colony, biome, season)) {
            return;
        }

        int spawnAmount = calculateParasiteAntSpawnAmount(colony);
        if (spawnAmount <= 0) {
            return;
        }
        if (isParasiteAntOutbreakPrevented(colony, biome, season)) {
            return;
        }
        if (GameRandom.nextFloat() > GameConstants.PARASITE_OUTBREAK_CHANCE) {
            return;
        }

        colony.setParasiteAnts(colony.getParasiteAnts() + spawnAmount);
        colony.logEvent(ColonyLogPrefixes.INFO + " "
            + String.format(LanguageStrings.get(LanguageStrings.LOG_PARASITE_ANT_SPREAD_FMT), spawnAmount));
    }

    public void rankUp(Colony colony) {
        int total = colony.getAntTotal();
        if (total >= GameConstants.RANK_GIGA.getPopulation()) colony.setRank(GameConstants.RANK_GIGA);
        else if (total >= GameConstants.RANK_SUPREME.getPopulation()) colony.setRank(GameConstants.RANK_SUPREME);
        else if (total >= GameConstants.RANK_ULTIMATE.getPopulation()) colony.setRank(GameConstants.RANK_ULTIMATE);
        else if (total >= GameConstants.RANK_MEGA.getPopulation()) colony.setRank(GameConstants.RANK_MEGA);
        else if (total >= GameConstants.RANK_HYPER.getPopulation()) colony.setRank(GameConstants.RANK_HYPER);
        else if (total >= GameConstants.RANK_ULTRA.getPopulation()) colony.setRank(GameConstants.RANK_ULTRA);
        else if (total >= GameConstants.RANK_SUPER.getPopulation()) colony.setRank(GameConstants.RANK_SUPER);
        else if (total >= GameConstants.RANK_EMPIRE.getPopulation()) colony.setRank(GameConstants.RANK_EMPIRE);
        else if (total >= GameConstants.RANK_KINGDOM.getPopulation()) colony.setRank(GameConstants.RANK_KINGDOM);
        else if (total >= GameConstants.RANK_DUCHY.getPopulation()) colony.setRank(GameConstants.RANK_DUCHY);
        else if (total >= GameConstants.RANK_COUNTY.getPopulation()) colony.setRank(GameConstants.RANK_COUNTY);
        else if (total >= GameConstants.RANK_COLONY.getPopulation()) colony.setRank(GameConstants.RANK_COLONY);
        else colony.setRank(GameConstants.RANK_ANT);
    }
}