package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColonyAutomationService {

    private static final int MIN_NURSES = 4;
    private static final int MIN_FORAGERS = 4;
    private static final int MIN_FARMERS = 1;
    private static final int MIN_COURIERS_FOR_LOGISTICS = 1;
    private static final double PEACE_UNLOCKED_ROLE_SHARE = 0.05;
    private static final double SATELLITE_BREEDER_SHARE = 0.30;
    private static final double SATELLITE_DIPLOMAT_SHARE = 0.30;
    private static final double SATELLITE_RESEARCHER_SHARE = 0.30;
    private static final double SATELLITE_SKYTRANS_SHARE = 0.10;

    private static final AntRole[] PEACE_EXTRA_WORKER_ROLES = {
            GameConstants.ROLE_POTTER, GameConstants.ROLE_MILITIA
    };
    private static final AntRole[] PEACE_EXTRA_SOLDIER_ROLES = {
            GameConstants.ROLE_WARRIOR, GameConstants.ROLE_DEFENDER, GameConstants.ROLE_BOMBER
    };
    private static final AntRole[] PEACE_EXTRA_MAJOR_ROLES = {
            GameConstants.ROLE_ARTILLERY, GameConstants.ROLE_SIEGE
    };
    private static final AntRole[] PEACE_EXTRA_PRINCESS_ROLES = {
            GameConstants.ROLE_CAPTAIN, GameConstants.ROLE_AIR_SUPPORT, GameConstants.ROLE_AIR_BOMBER
    };
    private static final AntRole[] PEACE_EXTRA_QUEEN_ROLES = {
            GameConstants.ROLE_COMMANDER
    };
    private static final AntRole[] WAR_MILITARY_SOLDIER_ROLES = {
            GameConstants.ROLE_WARRIOR, GameConstants.ROLE_DEFENDER, GameConstants.ROLE_BOMBER
    };
    private static final AntRole[] WAR_MILITARY_MAJOR_ROLES = {
            GameConstants.ROLE_BRUTE, GameConstants.ROLE_ARTILLERY, GameConstants.ROLE_SIEGE
    };
    private static final AntRole[] WAR_MILITARY_PRINCESS_ROLES = {
            GameConstants.ROLE_CAPTAIN, GameConstants.ROLE_AIR_SUPPORT, GameConstants.ROLE_AIR_BOMBER
    };

    public void runAutomation(Colony colony) {
        runAutomation(colony, null, null);
    }

    public void runAutomation(Colony colony, Biome biome, Season season) {
        if (!colony.isAutomationEnabled()) return;

        Map<AntRole, Integer> roleQuotas = usesWarEconomy(colony)
                ? calculateWarEconomyQuotas(colony, biome, season)
                : calculateNeedsBasedQuotas(colony, biome, season);
        applyQuotas(colony, roleQuotas);
    }

    public void applyWarEconomyQuotas(Colony colony) {
        Map<AntRole, Integer> roleQuotas = calculateWarEconomyQuotas(colony, null, null);
        for (Map.Entry<AntRole, Integer> entry : roleQuotas.entrySet()) {
            colony.setWarAssignedRoleCount(entry.getKey(), entry.getValue());
        }
    }

    public Map<AntRole, Integer> calculateWarEconomyQuotas(Colony colony) {
        return calculateWarEconomyQuotas(colony, null, null);
    }

    public Map<AntRole, Integer> calculateWarEconomyQuotas(Colony colony, Biome biome, Season season) {
        Map<AntRole, Integer> targets = new HashMap<>();
        for (AntRole role : GameConstants.getAntRoles()) {
            targets.put(role, 0);
        }
        calculateWarWorkerQuotas(colony, targets);
        calculateWarSoldierQuotas(colony, targets, biome, season);
        calculateWarMajorQuotas(colony, targets);
        calculateWarPrincessQuotas(colony, targets);
        calculateWarQueenQuotas(colony, targets);
        return targets;
    }

    public void runDailyAutomation(Colony colony) {
        runDailyAutomation(colony, null);
    }

    public void runDailyAutomation(Colony colony, Hex currentHex) {
        if (!colony.isAutomationEnabled()) return;

        checkAndConstructBuildings(colony);
        checkAndStartTunnel(colony, currentHex);
    }

    public void runAutoBuild(Colony colony) {
        checkAndConstructBuildings(colony);
    }

    public void checkAndConstructBuildings(Colony colony) {
        if (colony.getCurrentBuildingProject() != null) return;

        List<Building> candidates = new ArrayList<>();
        Engine eng = colony.getDynasty() != null && colony.getDynasty().getOwningWorld() != null
                ? colony.getDynasty().getOwningWorld().getEngine()
                : null;
        boolean instant = eng != null && eng.isInstantBuildings();
        for (Building b : GameUnlocks.getBuildings()) {
            boolean notOwned = !colony.hasBuilding(b);
            boolean reqMet = (b.getRequirement() == null || colony.hasBuilding(b.getRequirement()));
            boolean tierMet = instant || b.isAvailableFor(colony.getDynasty());
            boolean unlockMet = GameUnlocks.meetsBuildingUnlockRequirement(colony, b);
            boolean canAfford = instant
                    || (colony.getMinerals() >= b.getMineralCost() && colony.getResins() >= b.getResinCost());

            if (notOwned && reqMet && tierMet && unlockMet && canAfford) {
                candidates.add(b);
            }
        }

        if (!candidates.isEmpty()) {
            candidates.sort(Comparator.comparingInt(b -> b.getMineralCost() + b.getResinCost()));
            Building target = candidates.get(0);

            colony.startBuildingProject(target);
            colony.logEvent(ColonyLogPrefixes.AUTOMATION + " "
                + String.format(LanguageStrings.get(LanguageStrings.LOG_AUTOMATION_BUILD_FMT), target.getName()));
        }
    }

    public void checkAndStartTunnel(Colony colony, Hex currentHex) {
        if (colony.getCurrentTunnelProject() != null) return;
        if (!colony.isAutomationEnabled()) return;
        if (!usesAggressiveTunnelAutomation(colony)) return;
        if (!colony.hasUpgrade(GameUnlocks.ABILITY_TUNNELS)) return;
        if (!colony.hasUpgrade(GameUnlocks.ROLE_BORER) && !colony.hasUpgrade(GameUnlocks.ROLE_ENGINEER)) return;

        int diggers = colony.getAssignedRoleCount(GameConstants.ROLE_BORER)
                + colony.getAssignedRoleCount(GameConstants.ROLE_ENGINEER);
        if (diggers <= 0) return;

        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null || currentHex == null) return;
        if (dynasty.hasIncompleteTunnelAt(currentHex)) return;

        Hex targetHex = findTunnelTarget(dynasty, currentHex);
        if (targetHex == null) return;

        Tunnel tunnel = new Tunnel(currentHex, targetHex, GameNumbers.TUNNEL_WORK_REQUIRED);
        dynasty.addTunnel(tunnel);
        colony.setCurrentTunnelProject(tunnel);
        colony.logEvent(ColonyLogPrefixes.AUTOMATION + " "
            + String.format(LanguageStrings.get(LanguageStrings.LOG_AUTOMATION_TUNNEL_FMT),
                targetHex.getColony() != null ? targetHex.getColony().getName() : "?"));
    }

    private Hex findTunnelTarget(Dynasty dynasty, Hex currentHex) {
        Hex sameDynastyTarget = null;

        for (Hex neighbor : currentHex.getAdjacentNeighbors()) {
            if (neighbor == null) continue;
            if (neighbor.getColony() == null || neighbor.getColony().getDynasty() != dynasty) continue;

            Tunnel existing = dynasty.getTunnelBetween(currentHex, neighbor);
            if (existing != null) continue;
            if (dynasty.hasIncompleteTunnelAt(neighbor)) continue;

            sameDynastyTarget = neighbor;
            break;
        }

        return sameDynastyTarget;
    }

    private Map<AntRole, Integer> calculateNeedsBasedQuotas(Colony colony, Biome biome, Season season) {
        Map<AntRole, Integer> targets = new HashMap<>();
        for (AntRole role : GameConstants.getAntRoles()) targets.put(role, 0);

        calculateWorkerQuotas(colony, targets);
        calculateSoldierQuotas(colony, targets, biome, season);
        calculateMajorQuotas(colony, targets);
        calculatePrincessQuotas(colony, targets);
        calculateQueenQuotas(colony, targets);

        return targets;
    }

    private void calculateWorkerQuotas(Colony colony, Map<AntRole, Integer> targets) {
        if (usesWarEconomy(colony)) {
            calculateWarWorkerQuotas(colony, targets);
            return;
        }
        int totalWorkers = colony.getWorkers().size();
        int remaining = totalWorkers;
        if (remaining == 0) return;

        remaining -= reserveMinimumShares(colony, targets, totalWorkers, PEACE_EXTRA_WORKER_ROLES);
        if (remaining == 0) return;

        ColonyStatsService stats = colony.getStatsService();

        int farmerTarget = Math.max(MIN_FARMERS, (int) (totalWorkers * 0.05));
        int farmers = assignMinimum(remaining, farmerTarget);
        remaining -= farmers;

        int foragers = assignMinimum(remaining, MIN_FORAGERS);
        remaining -= foragers;

        float nursingRate = stats.getNursingRate(colony);
        int maxBrood = stats.getEggsCapacity(colony) * 3;
        int nurseTarget = MIN_NURSES;
        if (nursingRate > 0) {
            nurseTarget = Math.max(MIN_NURSES, (int) Math.ceil(maxBrood / nursingRate));
        }
        int nurses = assignMinimum(remaining, nurseTarget);
        remaining -= nurses;

        int graverTarget = (int) (totalWorkers * 0.05);
        int gravers = assignMinimum(remaining, graverTarget);
        remaining -= gravers;

        int totalGraversNeeded = calculateGraverNeeds(colony, remaining + gravers, stats);
        if (totalGraversNeeded > gravers) {
            int extraGravers = Math.min(remaining, totalGraversNeeded - gravers);
            gravers += extraGravers;
            remaining -= extraGravers;
        }
        targets.put(GameConstants.ROLE_GRAVER, gravers);

        int extraNurses = calculateExtraNurseNeeds(colony, remaining, nurses, stats);
        nurses += extraNurses;
        remaining -= extraNurses;
        targets.put(GameConstants.ROLE_NURSE, nurses);

        int extraFarmers = calculateExtraFarmerNeeds(colony, remaining, farmers, stats);
        farmers += extraFarmers;
        remaining -= extraFarmers;
        targets.put(GameConstants.ROLE_FARMER, farmers);

        if (remaining > 0 && colony.hasUpgrade(GameUnlocks.ROLE_RANCHER) && colony.getAphids() > 0) {
            int rancherTarget = Math.max(1, (int) (totalWorkers * 0.05));
            int toAdd = Math.min(rancherTarget, remaining);
            targets.put(GameConstants.ROLE_RANCHER, toAdd);
            remaining -= toAdd;
        }

        if (remaining > 0 && colony.hasUpgrade(GameUnlocks.ROLE_SCOUT)) {
            int scoutTarget = Math.max(1, (int) (totalWorkers * 0.02));
            int toAdd = Math.min(scoutTarget, remaining);
            targets.put(GameConstants.ROLE_SCOUT, toAdd);
            remaining -= toAdd;
        }

        if (remaining > 0 && colony.hasUpgrade(GameUnlocks.ROLE_MINER)) {
            int minerTarget = (int) (totalWorkers * 0.15);
            int toAdd = Math.min(remaining, minerTarget);

            if (colony.getMinerals() >= colony.getMineralsCapacity()) {
                toAdd = Math.min(remaining, Math.max(1, (int) (minerTarget * 0.10)));
            }

            targets.put(GameConstants.ROLE_MINER, toAdd);
            remaining -= toAdd;
        }

        if (remaining > 0) {
            int foragerMin = (int) (totalWorkers * 0.10);
            if (foragers < foragerMin) {
                int toAdd = Math.min(remaining, foragerMin - foragers);
                foragers += toAdd;
                remaining -= toAdd;
            }
        }

        remaining = allocateTunnelEngineers(colony, targets, remaining);
        remaining = allocateCourierWorkers(colony, targets, remaining);

        if (remaining > 0 && colony.hasUpgrade(GameUnlocks.ROLE_BUILDER)) {
            if (colony.getCurrentBuildingProject() != null) {
                targets.put(GameConstants.ROLE_BUILDER, remaining);
                targets.put(GameConstants.ROLE_FORAGER, foragers);
            } else {
                targets.put(GameConstants.ROLE_BUILDER, 0);
                targets.put(GameConstants.ROLE_FORAGER, foragers + remaining);
            }
        } else {
            targets.put(GameConstants.ROLE_BUILDER, 0);
            targets.put(GameConstants.ROLE_FORAGER, foragers + remaining);
        }
    }

    private int allocateTunnelEngineers(Colony colony, Map<AntRole, Integer> targets, int remaining) {
        if (!colony.hasUpgrade(GameUnlocks.ABILITY_TUNNELS)) return remaining;
        if (!colony.hasUpgrade(GameUnlocks.ROLE_ENGINEER)) return remaining;
        if (remaining <= 0) return remaining;

        boolean aggressive = usesAggressiveTunnelAutomation(colony);
        boolean digging = colony.getCurrentTunnelProject() != null;
        boolean preparing = !digging && aggressive && needsTunnelDigging(colony);
        if (!digging && !preparing) return remaining;

        int engineerTarget = aggressive
                ? Math.min(remaining, Math.max(1, remaining / 3))
                : Math.min(remaining, Math.max(1, remaining / 5));
        targets.put(GameConstants.ROLE_ENGINEER, engineerTarget);
        return remaining - engineerTarget;
    }

    private boolean needsTunnelDigging(Colony colony) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_BORER) && !colony.hasUpgrade(GameUnlocks.ROLE_ENGINEER)) {
            return false;
        }
        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null || dynasty.getColonies().size() < 2) {
            return dynasty != null && dynasty.getTunnels().stream().anyMatch(t -> !t.isComplete());
        }
        return true;
    }

    private int allocateCourierWorkers(Colony colony, Map<AntRole, Integer> targets, int remaining) {
        if (remaining <= 0 || !colony.hasUpgrade(GameUnlocks.ROLE_COURIER)) return remaining;
        if (!needsLogisticsStaff(colony)) return remaining;

        int courierTarget = Math.min(remaining, Math.max(MIN_COURIERS_FOR_LOGISTICS, remaining / 10));
        targets.put(GameConstants.ROLE_COURIER, courierTarget);
        return remaining - courierTarget;
    }

    private boolean needsLogisticsStaff(Colony colony) {
        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null) return false;
        if (!dynasty.hasUpgrade(GameUnlocks.ABILITY_TRADE)) return false;
        if (dynasty.getColonies().size() >= 2) return true;
        if (dynasty.getTradeService() != null) {
            return !dynasty.getTradeService().getDynastyTrades().isEmpty();
        }
        return false;
    }

    private int assignMinimum(int available, int min) {
        return Math.min(min, available);
    }

    private int calculateGraverNeeds(Colony colony, int available, ColonyStatsService stats) {
        if (available <= 0) return 0;
        int deadBodies = colony.getDeadAnts().size();
        if (deadBodies == 0) return 0;

        float dailyCleaningRate = stats.getGravingRate(colony) * 24.0f;
        if (dailyCleaningRate <= 0) return 0;

        float daysToClear = 5.0f;
        if (deadBodies >= 400) daysToClear = 1.0f;
        if (deadBodies >= 1400) daysToClear = 0.5f;

        int needed = (int) Math.ceil(deadBodies / (dailyCleaningRate * daysToClear));
        int doubleNeeded = needed * 2;

        return Math.min(doubleNeeded, available);
    }

    private int calculateExtraNurseNeeds(Colony colony, int available, int currentNurses, ColonyStatsService stats) {
        if (available <= 0) return 0;
        int totalBrood = colony.getEggs().size() + colony.getLarvae().size() + colony.getPupae().size();
        float nursingRate = stats.getNursingRate(colony);
        if (nursingRate <= 0) return 0;

        int currentCapacity = (int) (currentNurses * nursingRate);
        if (currentCapacity < totalBrood) {
            int deficit = totalBrood - currentCapacity;
            int extraNeeded = (int) Math.ceil(deficit / nursingRate);
            return Math.min(extraNeeded, available);
        }
        return 0;
    }

    private int calculateExtraFarmerNeeds(Colony colony, int available, int currentFarmers, ColonyStatsService stats) {
        if (available <= 0) return 0;
        int totalConsumption = stats.getTotalConsumption(colony);
        float conversionRate = stats.getConversionRate(colony);
        double productionPerFarmer = conversionRate * 1440.0;

        if (productionPerFarmer <= 0) return 0;

        int currentProduction = (int) (currentFarmers * productionPerFarmer);
        boolean needsFood = currentProduction < totalConsumption || colony.getMushrooms() < stats.getMushroomsCapacity(colony) * 0.2;

        if (needsFood) {
            int deficit = totalConsumption - currentProduction;
            int baseNeeded = (int) Math.ceil(deficit / productionPerFarmer);
            if (colony.getMushrooms() < stats.getMushroomsCapacity(colony) * 0.1) baseNeeded++;

            int extraNeeded = baseNeeded * 3;
            return Math.min(extraNeeded, available);
        }
        return 0;
    }

    private void calculateSoldierQuotas(Colony colony, Map<AntRole, Integer> targets, Biome biome, Season season) {
        if (usesWarEconomy(colony)) {
            calculateWarSoldierQuotas(colony, targets, biome, season);
            return;
        }
        int totalSoldiers = colony.getSoldiers().size();
        int remainingSoldiers = totalSoldiers;
        if (remainingSoldiers == 0) return;

        remainingSoldiers -= reserveMinimumShares(colony, targets, totalSoldiers, PEACE_EXTRA_SOLDIER_ROLES);
        if (remainingSoldiers == 0) return;

        int assignedPolice = Math.min(remainingSoldiers, desiredPoliceCount(colony, biome, season, remainingSoldiers));
        targets.put(GameConstants.ROLE_POLICE, assignedPolice);
        remainingSoldiers -= assignedPolice;

        int assignedCatchers = Math.min(remainingSoldiers, desiredCatcherCount(colony, biome, season, remainingSoldiers));
        if (assignedCatchers > 0) {
            targets.put(GameConstants.ROLE_CATCHER, assignedCatchers);
            remainingSoldiers -= assignedCatchers;
        }

        int assignedEscorts = 0;
        if (remainingSoldiers > 0 && colony.hasUpgrade(GameUnlocks.ROLE_ESCORT)) {
            int couriers = targets.getOrDefault(GameConstants.ROLE_COURIER, 0);
            if (couriers > 0) {
                assignedEscorts = Math.min(remainingSoldiers, Math.max(1, couriers / 2));
                targets.put(GameConstants.ROLE_ESCORT, assignedEscorts);
                remainingSoldiers -= assignedEscorts;
            }
        }

        if (remainingSoldiers > 0 && colony.hasUpgrade(GameUnlocks.ROLE_HUNTER)) {
            targets.put(GameConstants.ROLE_HUNTER, remainingSoldiers);
        }
    }

    private int desiredPoliceCount(Colony colony, Biome biome, Season season, int availableSoldiers) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_POLICE) || availableSoldiers <= 0) {
            return 0;
        }
        int needed = 0;
        int parasiteAnts = colony.getParasiteAnts();
        if (parasiteAnts > 0) {
            needed = Math.max(1, parasiteAnts);
        }
        if (biome != null && season != null) {
            int prevent = colony.getPopulationService()
                    .requiredPoliceToPreventParasiteAntOutbreak(colony, biome, season);
            needed = Math.max(needed, prevent);
        }
        return Math.min(availableSoldiers, needed);
    }

    private int desiredCatcherCount(Colony colony, Biome biome, Season season, int availableSoldiers) {
        if (!colony.hasUpgrade(GameUnlocks.ROLE_CATCHER)
                || !colony.hasUpgrade(GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE)
                || availableSoldiers <= 0) {
            return 0;
        }
        int needed = 0;
        int parasiticMites = colony.getParasiticMites();
        if (parasiticMites > 0) {
            needed = Math.max(1, parasiticMites / GameNumbers.PARASITIC_MITES_PER_SLOWED_ANT);
        }
        if (biome != null && season != null) {
            ColonyCritterHandlingService bugs = colony.getBugHandlingService();
            int requiredMites = bugs.requiredSymbioticMitesToPreventOutbreak(colony, biome, season);
            int have = colony.getSymbioticMites();
            if (requiredMites > have) {
                int shortfall = requiredMites - have;
                int catchersForPrevention = Math.max(1,
                        (int) Math.ceil(shortfall / (double) GameNumbers.PET_CAPACITY_PER_TENDER));
                needed = Math.max(needed, catchersForPrevention);
            }
        }
        return Math.min(availableSoldiers, needed);
    }

    private void calculateMajorQuotas(Colony colony, Map<AntRole, Integer> targets) {
        if (usesWarEconomy(colony)) {
            calculateWarMajorQuotas(colony, targets);
            return;
        }
        int totalMajors = colony.getMajors().size();
        if (totalMajors == 0) return;

        int reserved = reserveMinimumShares(colony, targets, totalMajors, PEACE_EXTRA_MAJOR_ROLES);
        int remainingPool = totalMajors - reserved;
        if (remainingPool == 0) return;

        int assignedBorers = 0;
        if (colony.hasUpgrade(GameUnlocks.ROLE_BORER) && colony.hasUpgrade(GameUnlocks.ABILITY_TUNNELS)) {
            boolean digging = colony.getCurrentTunnelProject() != null;
            boolean aggressive = usesAggressiveTunnelAutomation(colony);
            boolean preparing = !digging && aggressive && needsTunnelDigging(colony);
            if (digging || preparing) {
                int borerShare = aggressive ? 2 : 3;
                assignedBorers = Math.min(remainingPool, digging
                        ? Math.max(1, remainingPool / borerShare)
                        : Math.max(1, remainingPool / 4));
                targets.put(GameConstants.ROLE_BORER, assignedBorers);
            }
        }

        int remainingMajors = remainingPool - assignedBorers;

        int assignedTransport = 0;
        if (remainingMajors > 0 && colony.hasUpgrade(GameUnlocks.ROLE_TRANSPORT)) {
            int couriers = targets.getOrDefault(GameConstants.ROLE_COURIER, 0);
            if (couriers > 0) {
                assignedTransport = Math.min(remainingMajors, Math.max(1, couriers / 3));
            }
        }

        if (colony.getCurrentBuildingProject() != null && colony.hasUpgrade(GameUnlocks.ROLE_CRANE)) {
            targets.put(GameConstants.ROLE_CRANE, remainingMajors - assignedTransport);
            targets.put(GameConstants.ROLE_TRANSPORT, assignedTransport);
            targets.put(GameConstants.ROLE_BRUTE, 0);
        } else if (assignedTransport > 0) {
            targets.put(GameConstants.ROLE_TRANSPORT, assignedTransport);
            targets.put(GameConstants.ROLE_CRANE, 0);
            targets.put(GameConstants.ROLE_BRUTE, 0);
        } else {
            targets.put(GameConstants.ROLE_CRANE, 0);
            targets.put(GameConstants.ROLE_TRANSPORT, 0);
            targets.put(GameConstants.ROLE_BRUTE, 0);
        }
    }

    private boolean usesWarEconomy(Colony colony) {
        Dynasty dynasty = colony.getDynasty();
        return dynasty != null && dynasty.isAtWar();
    }

    private void calculateWarWorkerQuotas(Colony colony, Map<AntRole, Integer> targets) {
        int totalWorkers = colony.getWorkers().size();
        int remaining = totalWorkers;
        if (remaining == 0) {
            return;
        }

        ColonyStatsService stats = colony.getStatsService();

        int farmers = assignMinimum(remaining, MIN_FARMERS);
        remaining -= farmers;
        targets.put(GameConstants.ROLE_FARMER, farmers);

        int foragers = assignMinimum(remaining, MIN_FORAGERS);
        remaining -= foragers;
        targets.put(GameConstants.ROLE_FORAGER, foragers);

        float nursingRate = stats.getNursingRate(colony);
        int maxBrood = stats.getEggsCapacity(colony) * 3;
        int nurseTarget = MIN_NURSES;
        if (nursingRate > 0) {
            nurseTarget = Math.max(MIN_NURSES, (int) Math.ceil(maxBrood / nursingRate));
        }
        int nurses = assignMinimum(remaining, nurseTarget);
        remaining -= nurses;
        targets.put(GameConstants.ROLE_NURSE, nurses);

        int extraNurses = calculateExtraNurseNeeds(colony, remaining, nurses, stats);
        nurses += extraNurses;
        remaining -= extraNurses;
        targets.put(GameConstants.ROLE_NURSE, nurses);

        int extraFarmers = calculateExtraFarmerNeeds(colony, remaining, farmers, stats);
        farmers += extraFarmers;
        remaining -= extraFarmers;
        targets.put(GameConstants.ROLE_FARMER, farmers);

        if (remaining > 0) {
            if (isAutomationRoleUnlocked(colony, GameConstants.ROLE_MILITIA)) {
                targets.put(GameConstants.ROLE_MILITIA, remaining);
            } else {
                targets.put(GameConstants.ROLE_FORAGER, foragers + remaining);
            }
        }
    }

    private void calculateWarSoldierQuotas(Colony colony, Map<AntRole, Integer> targets, Biome biome, Season season) {
        int remainingSoldiers = colony.getSoldiers().size();
        if (remainingSoldiers == 0) {
            return;
        }

        int warCap = Math.max(1, (int) (colony.getSoldiers().size() * 0.10));
        int assignedPolice = Math.min(warCap, desiredPoliceCount(colony, biome, season, remainingSoldiers));
        if (assignedPolice > 0) {
            targets.put(GameConstants.ROLE_POLICE, assignedPolice);
            remainingSoldiers -= assignedPolice;
        }

        int assignedCatchers = Math.min(
                Math.max(1, (int) (colony.getSoldiers().size() * 0.10)),
                desiredCatcherCount(colony, biome, season, remainingSoldiers));
        if (assignedCatchers > 0 && remainingSoldiers > 0) {
            assignedCatchers = Math.min(assignedCatchers, remainingSoldiers);
            targets.put(GameConstants.ROLE_CATCHER, assignedCatchers);
            remainingSoldiers -= assignedCatchers;
        }

        if (remainingSoldiers <= 0) {
            return;
        }

        int militaryAssigned = distributeAmongUnlockedRoles(
                colony, targets, remainingSoldiers, WAR_MILITARY_SOLDIER_ROLES);
        remainingSoldiers -= militaryAssigned;
        if (remainingSoldiers > 0 && colony.hasUpgrade(GameUnlocks.ROLE_HUNTER)) {
            targets.put(GameConstants.ROLE_HUNTER, remainingSoldiers);
        }
    }

    private void calculateWarMajorQuotas(Colony colony, Map<AntRole, Integer> targets) {
        int totalMajors = colony.getMajors().size();
        if (totalMajors == 0) {
            return;
        }
        distributeAmongUnlockedRoles(colony, targets, totalMajors, WAR_MILITARY_MAJOR_ROLES);
    }

    private void calculateWarPrincessQuotas(Colony colony, Map<AntRole, Integer> targets) {
        int totalPrincesses = colony.getPrincesses().size();
        if (totalPrincesses == 0) {
            return;
        }
        if (colony.getQueens().isEmpty() && colony.hasUpgrade(GameUnlocks.ROLE_BREEDER)) {
            targets.put(GameConstants.ROLE_BREEDER, totalPrincesses);
            return;
        }
        int militaryAssigned = distributeAmongUnlockedRoles(
                colony, targets, totalPrincesses, WAR_MILITARY_PRINCESS_ROLES);
        if (militaryAssigned < totalPrincesses && colony.hasUpgrade(GameUnlocks.ROLE_BREEDER)) {
            targets.put(GameConstants.ROLE_BREEDER, totalPrincesses - militaryAssigned);
        }
    }

    private void calculateWarQueenQuotas(Colony colony, Map<AntRole, Integer> targets) {
        int totalQueens = colony.getQueens().size();
        if (totalQueens == 0) {
            return;
        }
        int commanders = 0;
        if (isAutomationRoleUnlocked(colony, GameConstants.ROLE_COMMANDER) && totalQueens > 1) {
            commanders = Math.min(colony.getMaxAssignableCommanders(), totalQueens - 1);
        }
        targets.put(GameConstants.ROLE_COMMANDER, commanders);
        targets.put(GameConstants.ROLE_LAYER, totalQueens - commanders);
    }

    private void calculatePrincessQuotas(Colony colony, Map<AntRole, Integer> targets) {
        int totalPrincesses = colony.getPrincesses().size();
        if (totalPrincesses == 0) {
            return;
        }

        int reserved = reserveMinimumShares(colony, targets, totalPrincesses, PEACE_EXTRA_PRINCESS_ROLES);
        int remainingPrincesses = totalPrincesses - reserved;
        if (remainingPrincesses == 0) {
            return;
        }

        if (colony.getQueens().isEmpty() && colony.hasUpgrade(GameUnlocks.ROLE_BREEDER)) {
            targets.put(GameConstants.ROLE_BREEDER, remainingPrincesses);
            return;
        }

        if (!colony.isCapital()) {
            calculateSatellitePrincessQuotas(colony, targets, remainingPrincesses);
            return;
        }

        int skyTrans = 0;
        if (colony.hasUpgrade(GameUnlocks.ROLE_SKYTRANS)) {
            int couriers = targets.getOrDefault(GameConstants.ROLE_COURIER, 0);
            if (couriers >= 3) {
                skyTrans = Math.min(remainingPrincesses, 1);
            }
        }

        int diplomatReserve = 0;
        Dynasty dynasty = colony.getDynasty();
        if (dynasty != null
                && dynasty.hasUpgrade(GameUnlocks.ABILITY_AUTO_DIPLOMACY)
                && colony.hasUpgrade(GameUnlocks.ROLE_DIPLOMAT)) {
            diplomatReserve = Math.min(2, Math.max(0, remainingPrincesses - skyTrans));
        }
        int remaining = Math.max(0, remainingPrincesses - skyTrans - diplomatReserve);
        int assistantCount = (int) (remaining * 0.80);
        targets.put(GameConstants.ROLE_ASSISTANT, assistantCount);
        targets.put(GameConstants.ROLE_SKYTRANS, skyTrans);
        targets.put(GameConstants.ROLE_DIPLOMAT, diplomatReserve);
        targets.put(GameConstants.ROLE_BREEDER, remaining - assistantCount);
    }

    private void calculateSatellitePrincessQuotas(Colony colony, Map<AntRole, Integer> targets, int totalPrincesses) {
        int breeders = colony.hasUpgrade(GameUnlocks.ROLE_BREEDER)
                ? percentOf(totalPrincesses, SATELLITE_BREEDER_SHARE) : 0;
        int diplomats = colony.hasUpgrade(GameUnlocks.ROLE_DIPLOMAT)
                ? percentOf(totalPrincesses, SATELLITE_DIPLOMAT_SHARE) : 0;
        diplomats = Math.min(diplomats, GameNumbers.DIPLOMAT_MAX_PER_DYNASTY_MISSION);
        int skyTrans = colony.hasUpgrade(GameUnlocks.ROLE_SKYTRANS)
                ? percentOf(totalPrincesses, SATELLITE_SKYTRANS_SHARE) : 0;
        int assistants = Math.max(0, totalPrincesses - breeders - diplomats - skyTrans);

        targets.put(GameConstants.ROLE_BREEDER, breeders);
        targets.put(GameConstants.ROLE_DIPLOMAT, diplomats);
        targets.put(GameConstants.ROLE_SKYTRANS, skyTrans);
        targets.put(GameConstants.ROLE_ASSISTANT, assistants);
    }

    private static int percentOf(int total, double share) {
        return (int) Math.round(total * share);
    }

    private boolean usesAggressiveTunnelAutomation(Colony colony) {
        Dynasty dynasty = colony.getDynasty();
        return dynasty != null && dynasty.hasUpgrade(GameUnlocks.ABILITY_AUTO_TUNNELS) && colony.isAutoTunnelsEnabled();
    }

    private void calculateQueenQuotas(Colony colony, Map<AntRole, Integer> targets) {
        List<Ant> queens = colony.getQueens();
        int totalQueens = queens.size();
        if (totalQueens == 0) {
            return;
        }

        int reserved = reserveMinimumShares(colony, targets, totalQueens, PEACE_EXTRA_QUEEN_ROLES);
        int remainingQueens = totalQueens - reserved;
        if (remainingQueens == 0) {
            return;
        }

        if (!colony.isCapital()) {
            calculateSatelliteQueenQuotas(colony, targets, remainingQueens);
            return;
        }

        int waterCapacity = colony.getStatsService().getWaterCapacity(colony);
        int totalAnts = colony.getAntTotal();

        if (totalAnts >= waterCapacity && colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER) && colony.getAssignedRoleCount(GameConstants.ROLE_ASSISTANT) <= 25) {
            if (remainingQueens == 1) {
                targets.put(GameConstants.ROLE_RESEARCHER, 1);
            } else {
                int half = remainingQueens / 2;
                int researchers = half;
                targets.put(GameConstants.ROLE_RESEARCHER, researchers);
                targets.put(GameConstants.ROLE_LAYER, remainingQueens - researchers);
            }
        } else {
            targets.put(GameConstants.ROLE_LAYER, remainingQueens);
        }
    }

    private void calculateSatelliteQueenQuotas(Colony colony, Map<AntRole, Integer> targets, int totalQueens) {
        if (colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER)) {
            int researchers = Math.min(totalQueens, percentOf(totalQueens, SATELLITE_RESEARCHER_SHARE));
            targets.put(GameConstants.ROLE_RESEARCHER, researchers);
            targets.put(GameConstants.ROLE_LAYER, totalQueens - researchers);
        } else {
            targets.put(GameConstants.ROLE_LAYER, totalQueens);
        }
    }

    private void applyQuotas(Colony colony, Map<AntRole, Integer> quotas) {
        for (Map.Entry<AntRole, Integer> entry : quotas.entrySet()) {
            colony.setAssignedRoleCount(entry.getKey(), entry.getValue());
        }
    }

    private int reserveMinimumShares(Colony colony, Map<AntRole, Integer> targets, int casteTotal, AntRole... roles) {
        if (casteTotal <= 0 || roles == null || roles.length == 0) {
            return 0;
        }
        int reserved = 0;
        for (AntRole role : roles) {
            if (!isAutomationRoleUnlocked(colony, role)) {
                continue;
            }
            int share = Math.max(1, (int) Math.ceil(casteTotal * PEACE_UNLOCKED_ROLE_SHARE));
            if (role == GameConstants.ROLE_COMMANDER) {
                share = Math.min(share, colony.getMaxAssignableCommanders());
            }
            share = Math.min(share, casteTotal - reserved);
            if (share <= 0) {
                break;
            }
            targets.put(role, share);
            reserved += share;
        }
        return reserved;
    }

    private int distributeAmongUnlockedRoles(
            Colony colony, Map<AntRole, Integer> targets, int count, AntRole... candidates) {
        if (count <= 0 || candidates == null || candidates.length == 0) {
            return 0;
        }
        List<AntRole> unlocked = new ArrayList<>();
        for (AntRole role : candidates) {
            if (isAutomationRoleUnlocked(colony, role)) {
                unlocked.add(role);
            }
        }
        if (unlocked.isEmpty()) {
            return 0;
        }
        int assigned = 0;
        int base = count / unlocked.size();
        int rem = count % unlocked.size();
        for (int i = 0; i < unlocked.size(); i++) {
            AntRole role = unlocked.get(i);
            int share = base + (i < rem ? 1 : 0);
            if (role == GameConstants.ROLE_COMMANDER) {
                share = Math.min(share, colony.getMaxAssignableCommanders());
            }
            if (share <= 0) {
                continue;
            }
            targets.put(role, targets.getOrDefault(role, 0) + share);
            assigned += share;
        }
        return assigned;
    }

    private boolean isAutomationRoleUnlocked(Colony colony, AntRole role) {
        if (colony == null || role == null || !GameConstants.isObtainableRole(role)) {
            return false;
        }
        Upgrade upgrade = upgradeForRole(role);
        return upgrade != null && colony.hasUpgrade(upgrade);
    }

    private static Upgrade upgradeForRole(AntRole role) {
        if (role == GameConstants.ROLE_FORAGER) return GameUnlocks.ROLE_FORAGER;
        if (role == GameConstants.ROLE_SCOUT) return GameUnlocks.ROLE_SCOUT;
        if (role == GameConstants.ROLE_NURSE) return GameUnlocks.ROLE_NURSE;
        if (role == GameConstants.ROLE_FARMER) return GameUnlocks.ROLE_FARMER;
        if (role == GameConstants.ROLE_GRAVER) return GameUnlocks.ROLE_GRAVER;
        if (role == GameConstants.ROLE_HUNTER) return GameUnlocks.ROLE_HUNTER;
        if (role == GameConstants.ROLE_LAYER) return GameUnlocks.ROLE_LAYER;
        if (role == GameConstants.ROLE_RANCHER) return GameUnlocks.ROLE_RANCHER;
        if (role == GameConstants.ROLE_BUILDER) return GameUnlocks.ROLE_BUILDER;
        if (role == GameConstants.ROLE_BREEDER) return GameUnlocks.ROLE_BREEDER;
        if (role == GameConstants.ROLE_RESEARCHER) return GameUnlocks.ROLE_RESEARCHER;
        if (role == GameConstants.ROLE_ASSISTANT) return GameUnlocks.ROLE_ASSISTANT;
        if (role == GameConstants.ROLE_POLICE) return GameUnlocks.ROLE_POLICE;
        if (role == GameConstants.ROLE_MINER) return GameUnlocks.ROLE_MINER;
        if (role == GameConstants.ROLE_POTTER) return GameUnlocks.ROLE_POTTER;
        if (role == GameConstants.ROLE_MILITIA) return GameUnlocks.ROLE_MILITIA;
        if (role == GameConstants.ROLE_COURIER) return GameUnlocks.ROLE_COURIER;
        if (role == GameConstants.ROLE_ENGINEER) return GameUnlocks.ROLE_ENGINEER;
        if (role == GameConstants.ROLE_WARRIOR) return GameUnlocks.ROLE_WARRIOR;
        if (role == GameConstants.ROLE_DEFENDER) return GameUnlocks.ROLE_DEFENDER;
        if (role == GameConstants.ROLE_BOMBER) return GameUnlocks.ROLE_BOMBER;
        if (role == GameConstants.ROLE_CATCHER) return GameUnlocks.ROLE_CATCHER;
        if (role == GameConstants.ROLE_ESCORT) return GameUnlocks.ROLE_ESCORT;
        if (role == GameConstants.ROLE_BRUTE) return GameUnlocks.ROLE_BRUTE;
        if (role == GameConstants.ROLE_CARRIER) return GameUnlocks.ROLE_CARRIER;
        if (role == GameConstants.ROLE_ARTILLERY) return GameUnlocks.ROLE_ARTILLERY;
        if (role == GameConstants.ROLE_SIEGE) return GameUnlocks.ROLE_SIEGE;
        if (role == GameConstants.ROLE_BORER) return GameUnlocks.ROLE_BORER;
        if (role == GameConstants.ROLE_CRANE) return GameUnlocks.ROLE_CRANE;
        if (role == GameConstants.ROLE_TRANSPORT) return GameUnlocks.ROLE_TRANSPORT;
        if (role == GameConstants.ROLE_DIPLOMAT) return GameUnlocks.ROLE_DIPLOMAT;
        if (role == GameConstants.ROLE_SKYTRANS) return GameUnlocks.ROLE_SKYTRANS;
        if (role == GameConstants.ROLE_COMMANDER) return GameUnlocks.ROLE_COMMANDER;
        if (role == GameConstants.ROLE_CAPTAIN) return GameUnlocks.ROLE_CAPTAIN;
        if (role == GameConstants.ROLE_AIR_SUPPORT) return GameUnlocks.ROLE_AIR_SUPPORT;
        if (role == GameConstants.ROLE_AIR_BOMBER) return GameUnlocks.ROLE_AIR_BOMBER;
        return null;
    }
}
