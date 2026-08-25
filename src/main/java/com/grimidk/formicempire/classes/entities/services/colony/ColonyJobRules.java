package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.constants.world.Temperature;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.audio.SfxService;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.DeathCause;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.registries.SoundEffects;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public final class ColonyJobRules {

    private ColonyJobRules() {
    }

    private static int liteRoleCount(Colony colony, AntRole role) {
        return colony.getAssignedRoleCount(role);
    }

    public static void runHourlyLite(Colony colony, Biome biome) {
        applyHourlyProduction(colony);
        applyHourlyResearch(colony);
        applyHourlyBuilding(colony);
        applyHourlyLaying(colony);
        applyHourlyRanching(colony);
    }

    public static void runDailyLite(Colony colony, Temperature currentTemp) {
        runDailyLite(colony, currentTemp, null, null);
    }

    public static void runDailyLite(Colony colony, Temperature currentTemp, Biome biome) {
        runDailyLite(colony, currentTemp, biome, null);
    }

    public static void runDailyLite(Colony colony, Temperature currentTemp, Biome biome, Hex currentHex) {
        colony.rankUp();
        applyDailyNursing(colony);
        applyDailyEating(colony);
        colony.getPopulationService().runHatching(colony);
        colony.getPopulationService().runAging(colony);
        applyDailyComposting(colony);
        applyDailyGraveKeeping(colony);
        applyDailyPolicing(colony);
        colony.getBugHandlingService().runDaily(colony, biome);
        colony.runScoutting(biome, currentHex);
        colony.getPopulationService().runContamination(colony);
    }

    public static void runMonthlyLite(Colony colony, Biome biome, Season season) {
        colony.getPopulationService().runParasitation(colony, biome, season);
        colony.getBugHandlingService().runMonthlyParasiticMitesLite(colony, biome, season);
    }

    public static void applyHourlyProduction(Colony colony) {
        ColonyStatsService stats = colony.getStatsService();
        ColonySourceService sources = colony.getSourceService();
        ColonyResourceService resources = colony.getResourceService();

        int plantGain = probabilisticRound(stats.getPlantProductionHourly(colony) * parasiticMiteWorkEfficiency(colony));
        int waterGain = probabilisticRound(stats.getWaterProductionHourly(colony) * parasiticMiteWorkEfficiency(colony));
        int meatGain = probabilisticRound(stats.getProteinProductionHourly(colony) * parasiticMiteWorkEfficiency(colony));
        int rockGain = probabilisticRound(stats.getMineralProductionHourly(colony) * parasiticMiteWorkEfficiency(colony));

        if (sources.getTotalQuantityAvailable(GameConstants.RESOURCE_PLANT) <= 0) {
            plantGain = 0;
        }

        if (sources.getTotalQuantityAvailable(GameConstants.RESOURCE_MEAT) <= 0) {
            meatGain = 0;
        }
        if (sources.getTotalQuantityAvailable(GameConstants.RESOURCE_ROCK) <= 0) {
            rockGain = 0;
        }

        resources.addResource(colony, GameConstants.RESOURCE_PLANT, plantGain);
        resources.addResource(colony, GameConstants.RESOURCE_WATER, waterGain);
        resources.addResource(colony, GameConstants.RESOURCE_MEAT, meatGain);
        resources.addResource(colony, GameConstants.RESOURCE_ROCK, rockGain);

        if (colony.hasBuilding(GameUnlocks.PASSIVE_WEB)) {
            double maxProtein = stats.getProteinCapacity(colony);
            double webGain = (maxProtein * GameNumbers.WEB_BUILDING_PROTEIN_DAILY_FRACTION) / 24.0;
            resources.addResource(colony, GameConstants.RESOURCE_MEAT, webGain);
        }

        if (colony.hasUpgrade(GameUnlocks.STAT_SCOUTING_2)) {
            int fungiGain = probabilisticRound(stats.getFungiProductionHourly(colony) * parasiticMiteWorkEfficiency(colony));
            if (sources.getTotalQuantityAvailable(GameConstants.RESOURCE_FUNGI) <= 0) {
                fungiGain = 0;
            }
            if (fungiGain > 0) {
                resources.addResource(colony, GameConstants.RESOURCE_FUNGI, fungiGain);
            }
        }

        if (colony.hasUpgrade(GameUnlocks.STAT_SCOUTING_3) && colony.hasUpgrade(GameUnlocks.ABILITY_RESIN)) {
            int resinSourceGain = probabilisticRound(stats.getResinProductionHourly(colony) * parasiticMiteWorkEfficiency(colony));
            if (sources.getTotalQuantityAvailable(GameConstants.RESOURCE_RESIN) <= 0) {
                resinSourceGain = 0;
            }
            if (resinSourceGain > 0 && resources.hasCapacity(colony, GameConstants.RESOURCE_RESIN)) {
                resources.addResource(colony, GameConstants.RESOURCE_RESIN, resinSourceGain);
            }
        }

        if (plantGain > 0
                && colony.hasUpgrade(GameUnlocks.ABILITY_RESIN)
                && resources.hasCapacity(colony, GameConstants.RESOURCE_RESIN)) {
            int resinGain = probabilisticRound(plantGain * GameNumbers.RESIN_FORAGE_BONUS_CHANCE);
            if (resinGain > 0) {
                resources.addResource(colony, GameConstants.RESOURCE_RESIN, resinGain);
            }
        }

        if (resources.hasCapacity(colony, GameConstants.RESOURCE_FUNGI)) {
            int farmerCount = stats.getEffectiveFarmerCount(colony);
            float convertRate = stats.getConversionRate(colony);

            int maxConvert = probabilisticRound(farmerCount * convertRate * 60 * parasiticMiteWorkEfficiency(colony));

            if (maxConvert > 0) {
                int actualConverted = 0;

                double plantConvert = resources.consumeResource(colony, GameConstants.RESOURCE_PLANT, maxConvert);
                if (plantConvert > 0) {
                    actualConverted += plantConvert;
                    maxConvert -= (int) plantConvert;
                }

                if (maxConvert > 0) {
                    double meatConvert = resources.consumeResource(colony, GameConstants.RESOURCE_MEAT, maxConvert);
                    if (meatConvert > 0) {
                        actualConverted += (meatConvert * 2);
                    }
                }

                if (actualConverted > 0) {
                    resources.addResource(colony, GameConstants.RESOURCE_FUNGI, actualConverted);
                }
            }
        }
    }

    public static void applyHourlyRanching(Colony colony) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) {
            return;
        }

        ColonyResourceService resources = colony.getResourceService();
        int syrupGain = colony.getAphids();
        double plantConsumption = syrupGain * 0.10;

        double actualPlantsConsumed = resources.consumeResource(colony, GameConstants.RESOURCE_PLANT, plantConsumption);

        if (actualPlantsConsumed > 0) {
            double ratio = actualPlantsConsumed / plantConsumption;
            resources.addResource(colony, GameConstants.RESOURCE_SYRUP, syrupGain * ratio);
        }
    }

    public static void applyHourlyLaying(Colony colony) {
        int layerCount = liteRoleCount(colony, GameConstants.ROLE_LAYER);
        if (layerCount <= 0) {
            return;
        }

        int spaceAvailable = colony.getStatsService().getEggsCapacity(colony) - colony.getEggs().size();
        if (spaceAvailable <= 0) {
            return;
        }

        int toLay = probabilisticRound(layerCount * colony.getStatsService().getLayingRate(colony));
        toLay = Math.min(toLay, spaceAvailable);

        List<Ant> eggs = colony.getEggs();
        for (int i = 0; i < toLay; i++) {
            Ant newEgg = new Ant(colony, GameConstants.TYPE_EGG);
            newEgg.setDimension(WorldSpaces.UNDERWORLD);
            newEgg.setPosition(new Point(0, 0));
            eggs.add(newEgg);
        }
    }

    public static void applyDailyNursing(Colony colony) {
        int nurseCount = liteRoleCount(colony, GameConstants.ROLE_NURSE);
        if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) {
            nurseCount += 2;
        } else if (colony.hasBuilding(GameUnlocks.PASSIVE_LAB)) {
            nurseCount += 1;
        }

        float nursingRate = colony.getStatsService().getNursingRate(colony);
        int capacity = (int) (nurseCount * nursingRate);

        int totalBrood = colony.getEggs().size() + colony.getLarvae().size() + colony.getPupae().size();

        if (totalBrood > capacity) {
            int toCull = totalBrood - capacity;

            int killedEggs = cullBrood(colony, colony.getEggs(), toCull);
            toCull -= killedEggs;

            if (toCull > 0) {
                int killedLarvae = cullBrood(colony, colony.getLarvae(), toCull);
                toCull -= killedLarvae;
            }

            if (toCull > 0) {
                cullBrood(colony, colony.getPupae(), toCull);
            }
        }
    }

    public static void applyDailyEating(Colony colony) {
        ColonyStatsService stats = colony.getStatsService();
        ColonyResourceService resources = colony.getResourceService();

        int totalConsumption = stats.getTotalConsumption(colony);
        totalConsumption += colony.getParasiteAnts();

        double consumedFood = resources.consumeResource(colony, GameConstants.RESOURCE_FUNGI, totalConsumption);
        double foodDeficit = totalConsumption - consumedFood;

        if (foodDeficit > 0) {
            double consumedSyrup = resources.consumeResource(colony, GameConstants.RESOURCE_SYRUP, foodDeficit);
            foodDeficit -= consumedSyrup;

            if (foodDeficit > 0) {
                applyStarvation(colony, (int) foodDeficit, DeathCause.STARVATION);
            }
        }

        int waterDemand = stats.getWaterConsumption(colony);
        double consumedWater = resources.consumeResource(colony, GameConstants.RESOURCE_WATER, waterDemand);
        double waterDeficit = waterDemand - consumedWater;

        if (waterDeficit > 0) {
            double consumedSyrup = resources.consumeResource(colony, GameConstants.RESOURCE_SYRUP, waterDeficit);
            waterDeficit -= consumedSyrup;

            if (waterDeficit > 0) {
                applyStarvation(colony, (int) (waterDeficit / 2), DeathCause.DEHYDRATION);
            }
        }
    }

    public static void applyDailyComposting(Colony colony) {
        if (!colony.hasBuilding(GameUnlocks.BUILDING_COMPOSTER)) {
            return;
        }

        List<Ant> deadAnts = colony.getDeadAnts();
        int graverCount = liteRoleCount(colony, GameConstants.ROLE_GRAVER);
        int potentialCompost = (int) colony.getStatsService().getGravingRate(colony) * graverCount;

        if (potentialCompost == 0 || deadAnts.isEmpty()) {
            return;
        }

        int actualToCompost = Math.min(potentialCompost, deadAnts.size());

        for (int i = 0; i < actualToCompost; i++) {
            deadAnts.remove(deadAnts.size() - 1);
        }

        int mushroomGain = actualToCompost * 4;
        colony.getResourceService().addResource(colony, GameConstants.RESOURCE_FUNGI, mushroomGain);
    }

    public static void applyDailyGraveKeeping(Colony colony) {
        int graverCount = liteRoleCount(colony, GameConstants.ROLE_GRAVER);

        if (colony.hasBuilding(GameUnlocks.PASSIVE_GRAVE)) {
            if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) {
                graverCount += 2;
            } else {
                graverCount += 1;
            }
        }
        graverCount += colony.getBugHandlingService().getDermestidGraveBonus(colony);

        if (graverCount <= 0) {
            return;
        }

        float gravingRate = colony.getStatsService().getGravingRate(colony);
        int cleanCapacity = (int) (graverCount * gravingRate);

        if (cleanCapacity <= 0) {
            return;
        }

        List<Ant> deadAnts = colony.getDeadAnts();
        int removed = 0;

        for (int i = deadAnts.size() - 1; i >= 0; i--) {
            if (removed >= cleanCapacity) {
                break;
            }
            deadAnts.remove(i);
            removed++;
        }
    }

    public static void applyDailyPolicing(Colony colony) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_POLICE)) {
            return;
        }

        int parasiteCount = colony.getParasiteAnts();
        if (parasiteCount == 0) {
            return;
        }

        int policeCount = liteRoleCount(colony, GameConstants.ROLE_POLICE);
        if (policeCount == 0) {
            return;
        }

        float detectionRate = colony.getStatsService().getParasiteDetection(colony);
        int parasiteAntsKilled = 0;

        for (int i = 0; i < policeCount; i++) {
            if (parasiteAntsKilled >= parasiteCount) {
                break;
            }
            if (GameRandom.nextFloat() < detectionRate) {
                parasiteAntsKilled++;
                colony.getResourceService().addResource(colony, GameConstants.RESOURCE_MEAT, 4);
            }
        }

        if (parasiteAntsKilled > 0) {
            colony.setParasiteAnts(Math.max(0, colony.getParasiteAnts() - parasiteAntsKilled));
        }
    }

    public static void applyHourlyResearch(Colony colony) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER)) {
            return;
        }
        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null) {
            return;
        }

        int researcherCount = liteRoleCount(colony, GameConstants.ROLE_RESEARCHER);

        if (colony.hasBuilding(GameUnlocks.PASSIVE_LAB)) {
            if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) {
                researcherCount += 2;
            } else {
                researcherCount += 1;
            }
        }

        int assistantCount = liteRoleCount(colony, GameConstants.ROLE_ASSISTANT);
        if (researcherCount <= 0 && assistantCount <= 0) {
            return;
        }

        int speed = colony.getStatsService().getResearchSpeed(colony);

        if (dynasty.getCurrentAssimilation() != null) {
            double power = (researcherCount * speed + assistantCount * (speed / (double) GameNumbers.RESEARCH_ASSISTANT_EFFICIENCY_DIVISOR)) / 10.0;
            dynasty.addAssimilationProgress(power);

            if (dynasty.getAssimilationProgress() >= dynasty.getAssimilationTargetCost()) {
                Assimilation assimilation = dynasty.getCurrentAssimilation();
                dynasty.unlockUpgrade(assimilation.getReward());
                dynasty.completeAssimilation(assimilation);
                colony.logEvent(ColonyLogPrefixes.SUCCESS + " "
                        + String.format(LanguageStrings.get(LanguageStrings.LOG_SUCCESS_ASSIMILATION_FMT),
                                assimilation.getName(), assimilation.getReward().getFlavorName()));
                dynasty.setCurrentAssimilation(null);
                dynasty.setAssimilationProgress(0);
                if (colony.isPlayer()) {
                    SfxService.play(SoundEffects.ASSIMILATION);
                }
            }
        } else {
            int queenGain = researcherCount * speed;
            int assistantGain = (int) (assistantCount * (speed / (double) GameNumbers.RESEARCH_ASSISTANT_EFFICIENCY_DIVISOR));
            colony.addResearchPoints(queenGain + assistantGain);
        }
    }

    public static void applyHourlyBuilding(Colony colony) {
        if (colony.getCurrentBuildingProject() == null) {
            return;
        }

        double efficiency = colony.getConstructionEfficiency();
        if (efficiency <= 0) {
            return;
        }

        colony.setBuildingProgressHours(colony.getBuildingProgressHours() + 1.0);

        double required = colony.getStatsService().getEffectiveBuildTime(colony, colony.getCurrentBuildingProject())
                / efficiency;
        if (colony.getBuildingProgressHours() >= required) {
            colony.unlockBuilding(colony.getCurrentBuildingProject());
            if (colony.isPlayer()) {
                SfxService.play(SoundEffects.BUILDING_END);
            }
            colony.setCurrentBuildingProject(null);
            colony.setBuildingProgressHours(0.0);
        }
    }

    private static int probabilisticRound(double value) {
        int floor = (int) value;
        return (GameRandom.nextDouble() < (value - floor)) ? floor + 1 : floor;
    }

    private static void applyStarvation(Colony colony, int deficit, String cause) {
        int deaths = deficit / 5;

        if (deaths <= 0 && deficit > 0 && GameRandom.nextFloat() < 0.2) {
            deaths = 1;
        }

        if (deaths <= 0) {
            return;
        }

        List<Ant> candidates = new ArrayList<>();
        for (List<Ant> group : colony.getAntGroups().values()) {
            candidates.addAll(group);
        }

        List<Ant> victims = ColonyResourceDeathSelection.selectVictims(candidates, deaths, null);
        for (Ant victim : victims) {
            List<Ant> typeList = colony.getAntsByType(victim.getAntType());
            if (typeList != null) {
                typeList.remove(victim);
            }
            colony.recordAntDeath(victim, cause);
        }
    }

    private static int cullBrood(Colony colony, List<Ant> broodList, int amount) {
        int removed = 0;
        for (int i = broodList.size() - 1; i >= 0; i--) {
            if (removed >= amount) {
                break;
            }
            Ant victim = broodList.remove(i);
            colony.recordAntDeath(victim, DeathCause.LACK_OF_CARE);
            removed++;
        }
        return removed;
    }

    private static double parasiticMiteWorkEfficiency(Colony colony) {
        int antTotal = colony.getAntTotal();
        if (antTotal <= 0 || colony.getParasiticMites() <= 0) {
            return 1.0;
        }
        int slowed = colony.getParasiticMiteSlowedAntCount();
        return Math.max(0.0, 1.0 - (slowed * 0.5 / antTotal));
    }
}
