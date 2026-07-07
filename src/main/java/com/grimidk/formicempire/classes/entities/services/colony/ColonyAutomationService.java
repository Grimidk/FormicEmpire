package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
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
    private static final int WORKER_SURPLUS_FOR_TUNNEL = 5;
    private static final int MIN_COURIERS_FOR_LOGISTICS = 1;

    public void runAutomation(Colony colony) {
        if (!colony.isAutomationEnabled()) return;

        Map<AntRole, Integer> roleQuotas = usesWarEconomy(colony)
                ? calculateWarEconomyQuotas(colony)
                : calculateNeedsBasedQuotas(colony);
        applyQuotas(colony, roleQuotas);
    }

    public void applyWarEconomyQuotas(Colony colony) {
        Map<AntRole, Integer> roleQuotas = calculateWarEconomyQuotas(colony);
        for (Map.Entry<AntRole, Integer> entry : roleQuotas.entrySet()) {
            colony.setWarAssignedRoleCount(entry.getKey(), entry.getValue());
        }
    }

    public Map<AntRole, Integer> calculateWarEconomyQuotas(Colony colony) {
        Map<AntRole, Integer> targets = new HashMap<>();
        for (AntRole role : GameConstants.getAntRoles()) {
            targets.put(role, 0);
        }
        calculateWarWorkerQuotas(colony, targets);
        calculateWarSoldierQuotas(colony, targets);
        calculateWarMajorQuotas(colony, targets);
        calculateMinimalPrincessQuotas(colony, targets);
        calculateMinimalQueenQuotas(colony, targets);
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
        for (Building b : GameUnlocks.getBuildings()) {
            boolean notOwned = !colony.hasBuilding(b);
            boolean reqMet = (b.getRequirement() == null || colony.hasBuilding(b.getRequirement()));
            boolean canAfford = colony.getMinerals() >= b.getMineralCost() && colony.getResins() >= b.getResinCost();

            if (notOwned && reqMet && canAfford) {
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
        if (!colony.hasUpgrade(GameUnlocks.ABILITY_TUNNELS)) return;
        if (!colony.hasUpgrade(GameUnlocks.ROLE_BORER) && !colony.hasUpgrade(GameUnlocks.ROLE_ENGINEER)) return;

        int diggers = colony.getAssignedRoleCount(GameConstants.ROLE_BORER)
                + colony.getAssignedRoleCount(GameConstants.ROLE_ENGINEER);
        if (diggers <= 0) return;

        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null || currentHex == null) return;

        Hex targetHex = findTunnelTarget(colony, dynasty, currentHex);
        if (targetHex == null) return;

        Tunnel tunnel = new Tunnel(currentHex, targetHex, GameConstants.TUNNEL_WORK_REQUIRED);
        dynasty.addTunnel(tunnel);
        colony.setCurrentTunnelProject(tunnel);
        colony.logEvent(ColonyLogPrefixes.AUTOMATION + " "
            + String.format(LanguageStrings.get(LanguageStrings.LOG_AUTOMATION_TUNNEL_FMT),
                targetHex.getColony() != null ? targetHex.getColony().getName() : "?"));
    }

    private Hex findTunnelTarget(Colony colony, Dynasty dynasty, Hex currentHex) {
        Hex sameDynastyTarget = null;
        Hex anyTarget = null;

        for (Hex neighbor : currentHex.getAdjacentNeighbors()) {
            if (neighbor == null) continue;

            Tunnel existing = dynasty.getTunnelBetween(currentHex, neighbor);
            if (existing != null) continue;

            if (neighbor.getColony() != null && neighbor.getColony().getDynasty() == dynasty) {
                sameDynastyTarget = neighbor;
            } else if (anyTarget == null) {
                anyTarget = neighbor;
            }
        }

        return sameDynastyTarget != null ? sameDynastyTarget : anyTarget;
    }

    private Map<AntRole, Integer> calculateNeedsBasedQuotas(Colony colony) {
        Map<AntRole, Integer> targets = new HashMap<>();
        for (AntRole role : GameConstants.getAntRoles()) targets.put(role, 0);

        calculateWorkerQuotas(colony, targets);
        calculateSoldierQuotas(colony, targets);
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
        if (!aggressive && remaining < WORKER_SURPLUS_FOR_TUNNEL) return remaining;

        boolean digging = colony.getCurrentTunnelProject() != null;
        boolean preparing = !digging && needsTunnelDigging(colony);
        if (!digging && !preparing) return remaining;

        int engineerTarget = aggressive
                ? Math.min(remaining, Math.max(1, remaining / 3))
                : Math.min(remaining, digging ? Math.max(1, remaining / 5) : 1);
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

    private void calculateSoldierQuotas(Colony colony, Map<AntRole, Integer> targets) {
        if (usesWarEconomy(colony)) {
            calculateWarSoldierQuotas(colony, targets);
            return;
        }
        int remainingSoldiers = colony.getSoldiers().size();
        if (remainingSoldiers == 0) return;

        int assignedPolice = 0;
        if (colony.getParasiteAnts() > 0 && colony.hasUpgrade(GameUnlocks.ROLE_POLICE)) {
            int maxPolice = (int) (colony.getSoldiers().size() * 0.20);
            assignedPolice = Math.min(maxPolice, remainingSoldiers);

            if (assignedPolice == 0 && maxPolice > 0 && remainingSoldiers > 0) assignedPolice = 1;
        }
        targets.put(GameConstants.ROLE_POLICE, assignedPolice);
        remainingSoldiers -= assignedPolice;

        int assignedCatchers = 0;
        if (colony.getParasiticMites() > 0 && colony.hasUpgrade(GameUnlocks.ROLE_CATCHER)) {
            assignedCatchers = Math.min(remainingSoldiers, Math.max(1, colony.getParasiticMites() / 10));
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

    private void calculateMajorQuotas(Colony colony, Map<AntRole, Integer> targets) {
        if (usesWarEconomy(colony)) {
            calculateWarMajorQuotas(colony, targets);
            return;
        }
        int totalMajors = colony.getMajors().size();
        if (totalMajors == 0) return;

        int assignedBorers = 0;
        if (colony.hasUpgrade(GameUnlocks.ROLE_BORER) && colony.hasUpgrade(GameUnlocks.ABILITY_TUNNELS)) {
            boolean digging = colony.getCurrentTunnelProject() != null;
            boolean preparing = !digging && needsTunnelDigging(colony);
            if (digging || preparing) {
                boolean aggressive = usesAggressiveTunnelAutomation(colony);
                int borerShare = aggressive ? 2 : 3;
                assignedBorers = Math.min(totalMajors, digging
                        ? Math.max(1, totalMajors / borerShare)
                        : (aggressive ? Math.max(1, totalMajors / 4) : 1));
                targets.put(GameConstants.ROLE_BORER, assignedBorers);
            }
        }

        int remainingMajors = totalMajors - assignedBorers;

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
            if (isAtWar(colony)) {
                targets.put(GameConstants.ROLE_BRUTE, remainingMajors - assignedTransport);
            } else {
                targets.put(GameConstants.ROLE_BRUTE, 0);
            }
        } else {
            targets.put(GameConstants.ROLE_CRANE, 0);
            targets.put(GameConstants.ROLE_TRANSPORT, 0);
            if (isAtWar(colony)) {
                targets.put(GameConstants.ROLE_BRUTE, remainingMajors);
            } else {
                targets.put(GameConstants.ROLE_BRUTE, 0);
            }
        }
    }

    private boolean isAtWar(Colony colony) {
        return usesWarEconomy(colony);
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

        if (remaining > 0 && colony.hasUpgrade(GameUnlocks.ROLE_MILITIA)) {
            targets.put(GameConstants.ROLE_MILITIA, remaining);
        }
    }

    private void calculateWarSoldierQuotas(Colony colony, Map<AntRole, Integer> targets) {
        int remainingSoldiers = colony.getSoldiers().size();
        if (remainingSoldiers == 0) {
            return;
        }

        int assignedPolice = 0;
        if (colony.getParasiteAnts() > 0 && colony.hasUpgrade(GameUnlocks.ROLE_POLICE)) {
            int maxPolice = Math.max(1, (int) (colony.getSoldiers().size() * 0.10));
            assignedPolice = Math.min(maxPolice, remainingSoldiers);
            targets.put(GameConstants.ROLE_POLICE, assignedPolice);
            remainingSoldiers -= assignedPolice;
        }

        if (remainingSoldiers <= 0) {
            return;
        }

        if (colony.hasUpgrade(GameUnlocks.ROLE_WARRIOR)) {
            int defenders = 0;
            if (colony.hasUpgrade(GameUnlocks.ROLE_DEFENDER)) {
                defenders = Math.max(1, remainingSoldiers / 4);
                defenders = Math.min(defenders, remainingSoldiers);
                targets.put(GameConstants.ROLE_DEFENDER, defenders);
            }
            targets.put(GameConstants.ROLE_WARRIOR, remainingSoldiers - defenders);
        } else if (colony.hasUpgrade(GameUnlocks.ROLE_HUNTER)) {
            targets.put(GameConstants.ROLE_HUNTER, remainingSoldiers);
        }
    }

    private void calculateWarMajorQuotas(Colony colony, Map<AntRole, Integer> targets) {
        int totalMajors = colony.getMajors().size();
        if (totalMajors == 0) {
            return;
        }

        if (colony.hasUpgrade(GameUnlocks.ROLE_BRUTE)) {
            targets.put(GameConstants.ROLE_BRUTE, totalMajors);
        }
    }

    private void calculateMinimalPrincessQuotas(Colony colony, Map<AntRole, Integer> targets) {
        int totalPrincesses = colony.getPrincesses().size();
        if (totalPrincesses == 0) {
            return;
        }
        targets.put(GameConstants.ROLE_BREEDER, totalPrincesses);
    }

    private void calculateMinimalQueenQuotas(Colony colony, Map<AntRole, Integer> targets) {
        int totalQueens = colony.getQueens().size();
        if (totalQueens == 0) {
            return;
        }
        targets.put(GameConstants.ROLE_LAYER, totalQueens);
    }

    private void calculatePrincessQuotas(Colony colony, Map<AntRole, Integer> targets) {
        int totalPrincesses = colony.getPrincesses().size();
        if (totalPrincesses == 0) {
            return;
        }

        if (colony.getQueens().isEmpty() && colony.hasUpgrade(GameUnlocks.ROLE_BREEDER)) {
            targets.put(GameConstants.ROLE_BREEDER, totalPrincesses);
            return;
        }

        int skyTrans = 0;
        if (colony.hasUpgrade(GameUnlocks.ROLE_SKYTRANS)) {
            int couriers = targets.getOrDefault(GameConstants.ROLE_COURIER, 0);
            if (couriers >= 3) {
                skyTrans = Math.min(totalPrincesses, 1);
            }
        }

        int assistantCount = (int) ((totalPrincesses - skyTrans) * 0.80);
        targets.put(GameConstants.ROLE_ASSISTANT, assistantCount);
        targets.put(GameConstants.ROLE_SKYTRANS, skyTrans);

        int breederCount = totalPrincesses - assistantCount - skyTrans;
        targets.put(GameConstants.ROLE_BREEDER, breederCount);
        assignAutomatedDiplomatQuotas(colony, targets);
    }

    private void assignAutomatedDiplomatQuotas(Colony colony, Map<AntRole, Integer> targets) {
        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null || !dynasty.hasUpgrade(GameUnlocks.ABILITY_AUTO_DIPLOMACY)) {
            return;
        }
        if (!colony.hasUpgrade(GameUnlocks.ROLE_DIPLOMAT) || colony.getPrincesses().isEmpty()) {
            return;
        }
        boolean isSource = colony.isCapital()
                || colony.getLoyalty() >= GameConstants.LOYALTY_MILITANT.getMinScore();
        if (!isSource) {
            return;
        }
        int princesses = colony.getPrincesses().size();
        int assignedElsewhere = targets.values().stream().mapToInt(Integer::intValue).sum();
        int available = Math.max(0, princesses - assignedElsewhere);
        if (available <= 0) {
            return;
        }
        int diplomatTarget = colony.isCapital() ? Math.min(2, available) : Math.min(1, available);
        targets.put(GameConstants.ROLE_DIPLOMAT,
                targets.getOrDefault(GameConstants.ROLE_DIPLOMAT, 0) + diplomatTarget);
    }

    private boolean usesAggressiveTunnelAutomation(Colony colony) {
        Dynasty dynasty = colony.getDynasty();
        return dynasty != null && dynasty.hasUpgrade(GameUnlocks.ABILITY_AUTO_TUNNELS) && colony.isAutoTunnelsEnabled();
    }

    private void calculateQueenQuotas(Colony colony, Map<AntRole, Integer> targets) {
        List<Ant> queens = colony.getQueens();
        int totalQueens = queens.size();
        if (totalQueens == 0) return;

        int waterCapacity = colony.getStatsService().getWaterCapacity(colony);
        int totalAnts = colony.getAntTotal();

        if (totalAnts >= waterCapacity && colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER) && colony.getAssignedRoleCount(GameConstants.ROLE_ASSISTANT) <= 25) {
            if (totalQueens == 1) {
                targets.put(GameConstants.ROLE_RESEARCHER, 1);
            } else {
                int half = totalQueens / 2;
                int researchers = half;
                targets.put(GameConstants.ROLE_RESEARCHER, researchers);
                targets.put(GameConstants.ROLE_LAYER, totalQueens - researchers);
            }
        } else {
            targets.put(GameConstants.ROLE_LAYER, totalQueens);
        }
    }

    private void applyQuotas(Colony colony, Map<AntRole, Integer> quotas) {
        for (Map.Entry<AntRole, Integer> entry : quotas.entrySet()) {
            colony.setAssignedRoleCount(entry.getKey(), entry.getValue());
        }
    }
}
