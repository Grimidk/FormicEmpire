package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

import java.awt.Rectangle;
import java.util.List;

public class ColonyStarterService {

    private static final int STARTING_NURSES = 15;
    private static final int STARTING_FORAGERS = 12;
    private static final int STARTING_FARMERS = 3;
    private static final int STARTING_WORKERS = STARTING_NURSES + STARTING_FORAGERS + STARTING_FARMERS;
    private static final int MIN_SUSTAIN_WORKERS = STARTING_WORKERS;

    private static final ColonyStarterService SHARED = new ColonyStarterService();

    public static ColonyStarterService shared() {
        return SHARED;
    }

    public void initializeNewColony(Colony colony) {
        String type = colony.isPlayer() ? "Player" : "AI";
        
        if (colony.getDynasty() != null) {
            Dynasty d = colony.getDynasty();
            
            int index = d.getColonies().indexOf(colony);
            if (index == -1) {
                index = d.getColonies().size();
            }

            // Capitals may already be named by World; satellites always use city titles.
            if (index == 0) {
                if (colony.getName() == null || colony.getName().isEmpty()) {
                    colony.setName(d.generateColonyName(0));
                }
            } else {
                colony.setName(d.generateColonyName(index));
            }
            
            boolean isFirst = (index == 0);
            if (isFirst) {
                d.setCapital(colony);
                colony.setAge(7); 
            } else {
                colony.setCapital(false);
                colony.setAge(0);
                
                Colony capitalColony = d.getCapital();
                
                if (capitalColony != null) {
                    colony.setHatchRateWorker(capitalColony.getHatchRateWorker());
                    colony.setHatchRateSoldier(capitalColony.getHatchRateSoldier());
                    colony.setHatchRateMajor(capitalColony.getHatchRateMajor());
                    colony.setHatchRateDrone(capitalColony.getHatchRateDrone());
                    colony.setHatchRatePrincess(capitalColony.getHatchRatePrincess());
                    AntSubtypeService.copySubtypeRates(colony, capitalColony);
                }
                if (d.isDefaultAutomationEnabled() && d.hasUpgrade(GameUnlocks.ABILITY_AUTOMATION)) {
                    colony.setAutomationEnabled(true);
                }
                if (d.isDefaultAutoBuildEnabled() && d.hasUpgrade(GameUnlocks.ABILITY_MANAGEMENT)) {
                    colony.setAutoBuildEnabled(true);
                }
                if (d.isDefaultAutoTunnelsEnabled() && d.hasUpgrade(GameUnlocks.ABILITY_AUTO_TUNNELS)) {
                    colony.setAutoTunnelsEnabled(true);
                }
            }
        } else {
            colony.setCapital(true);
            colony.setAge(7);
        }

        System.out.println("[ColonyStarterService] Initializing new " + type + " colony: " + colony.getName());

        if (!colony.isPlayer()) {
            colony.setAutomationEnabled(true);
            System.out.println("[ColonyStarterService] Automation ENABLED for NPC colony.");
            Species species = colony.getDynasty() != null ? colony.getDynasty().getSpecies() : null;
            if (species != null) {
                AntSubtypeService.applyNaturalSpeciesSubtypeRates(colony, species);
            }
        }
        
        clearColonyLists(colony);

        try {
            Ant queen = new Ant(colony, GameConstants.TYPE_QUEEN);
            queen.setDimension(WorldSpaces.UNDERWORLD); 
            queen.setRole(GameConstants.ROLE_LAYER);
            colony.getQueens().add(queen);
        } catch (Exception e) {
            System.err.println("[ColonyStarterService] Error creating Queen: " + e.getMessage());
            e.printStackTrace();
        }

        if (colony.getAge() >= 7) {
            matureColony(colony);
        }

        if (!colony.isPlayer()) {
            AntSubtypeService.assignNaturalSubtypesToPopulation(colony);
        }
        
        System.out.println("[ColonyStarterService] Initialization complete for " + colony.getName() + " (ID: " + colony.getId() + "). Current Age: " + colony.getAge());
    }

    private void clearColonyLists(Colony colony) {
        if (colony.getWorkers() != null) colony.getWorkers().clear();
        if (colony.getQueens() != null) colony.getQueens().clear();
        if (colony.getEggs() != null) colony.getEggs().clear();
        if (colony.getLarvae() != null) colony.getLarvae().clear();
        if (colony.getPupae() != null) colony.getPupae().clear();
        if (colony.getSoldiers() != null) colony.getSoldiers().clear();
        if (colony.getMajors() != null) colony.getMajors().clear();
        if (colony.getDrones() != null) colony.getDrones().clear();
        if (colony.getPrincesses() != null) colony.getPrincesses().clear();
    }

    public void dismantleColony(Hex hex) {
        if (hex == null || hex.getColony() == null) return;
        Colony colony = hex.getColony();

        System.out.println("[ColonyStarterService] Dismantling dead colony: " + colony.getName() + " at Hex (" + hex.getQ() + ", " + hex.getR() + ")");

        colony.setActive(false);
        colony.setAutomationEnabled(false);

        clearColonyLists(colony);
        if (colony.getDeadAnts() != null) colony.getDeadAnts().clear();
        if (colony.getBugs() != null) colony.getBugs().clear();

        hex.setColony(null);
    }

    public static boolean isReclaimableDeadColony(Colony colony) {
        if (colony == null || colony.getAntTotal() > 0) {
            return false;
        }
        if (colony.getAge() >= 7 || colony.getDaysWithoutQueen() >= 7) {
            return true;
        }
        Dynasty dynasty = colony.getDynasty();
        if (dynasty != null && dynasty.isDefeated()) {
            return true;
        }
        return colony.getDynasty() == null;
    }

    public void finalizeDeadRemnant(Colony colony) {
        if (colony == null) {
            return;
        }
        colony.setActive(false);
        colony.setAutomationEnabled(false);
        clearColonyLists(colony);
        if (colony.getDeadAnts() != null) {
            colony.getDeadAnts().clear();
        }
        if (colony.getBugs() != null) {
            colony.getBugs().clear();
        }
    }

    public void reclaimDeadColonyForSpread(Hex hex, Dynasty absorbingDynasty, Colony logColony) {
        if (hex == null || !isReclaimableDeadColony(hex.getColony())) {
            return;
        }
        Colony dead = hex.getColony();
        Dynasty oldDynasty = dead.getDynasty();
        if (oldDynasty != null && absorbingDynasty != null && oldDynasty != absorbingDynasty) {
            absorbingDynasty.inheritAssimilationsFrom(oldDynasty);
            if (logColony != null) {
                logColony.logEvent(ColonyLogPrefixes.DYNASTY + " "
                        + String.format(LanguageStrings.get(LanguageStrings.LOG_DYNASTY_ABSORBED_FMT),
                                oldDynasty.getName()));
            }
            oldDynasty.removeColony(dead);
        }
        dismantleColony(hex);
    }

    public void matureColony(Colony colony) {
        System.out.println("[ColonyStarterService] Maturation complete. Spawning workforce for " + colony.getName());

        applyStarterWorkforce(colony);

        if (colony.getLocationService() != null) {
            if (colony.getLocationService().getDiscoveredSources().isEmpty()) {
                int range = 300;
                
                int centerX = ColonySpatialLayout.ANCHOR_CENTER_X; 
                int centerY = ColonySpatialLayout.ANCHOR_HEIGHT / 2;
                
                int pX = centerX + GameRandom.nextInt((range * 2) + 1) - range;
                int pY = centerY + GameRandom.nextInt((range * 2) + 1) - range;
                pX = Math.max(50, pX);
                pY = Math.max(50, pY);
                ResourceSource initialPlant = new ResourceSource(GameConstants.RESOURCE_PLANT, 10000, pX, pY);
                
                int wX = centerX + GameRandom.nextInt((range * 2) + 1) - range;
                int wY = centerY + GameRandom.nextInt((range * 2) + 1) - range;
                wX = Math.max(50, wX);
                wY = Math.max(50, wY);
                ResourceSource initialWater = new ResourceSource(GameConstants.RESOURCE_WATER, 10000, wX, wY);
                
                colony.getLocationService().addSource(colony, initialPlant);
                colony.getLocationService().addSource(colony, initialWater);
            }
        }
        
        if (colony.getPhysicsService() != null) {
            colony.getPhysicsService().randomizeAllAntPositions(colony);
        }

        colony.logEvent(ColonyLogPrefixes.INFO + " " + LanguageStrings.get(LanguageStrings.LOG_MATURATION_COMPLETE));
    }

    public void stabilizeConqueredColony(Dynasty victor, Colony colony) {
        if (victor == null || colony == null) {
            return;
        }

        colony.setRecentlyConqueredMonthsRemaining(GameConstants.RECENTLY_CONQUERED_LOYALTY_MONTHS);
        Colony capital = victor.getCapital();

        if (colony.getQueens().isEmpty()) {
            boolean established = capital != null
                    && ColonyLabourService.establishQueenFromBreederPair(capital, colony);
            if (!established) {
                spawnOccupationQueen(colony);
                reestablishCapturedColony(capital != null ? capital : colony, colony);
            }
        } else {
            colony.setDaysWithoutQueen(0);
            reestablishCapturedColony(capital != null ? capital : colony, colony);
        }
    }

    private void spawnOccupationQueen(Colony colony) {
        Ant queen = new Ant(colony, GameConstants.TYPE_QUEEN);
        queen.setDimension(WorldSpaces.UNDERWORLD);
        queen.setRole(GameConstants.ROLE_LAYER);
        if (colony.getPhysicsService() != null) {
            Rectangle royal = colony.getPhysicsService().getRoomBounds(colony, WorldSpaces.ROYAL_CHAMBER);
            if (royal != null) {
                queen.setPosition(colony.getPhysicsService().getSpecificRoomPoint(colony, royal));
            }
        }
        colony.getQueens().add(queen);
        colony.setDaysWithoutQueen(0);
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_LAYER,
                colony.getPeaceAssignedRoleCount(GameConstants.ROLE_LAYER) + 1);
    }

    public void inheritIntegratedColonyFromOverlord(Dynasty overlord, Colony colony) {
        if (overlord == null || colony == null) {
            return;
        }
        Colony capital = overlord.getCapital();
        if (capital != null) {
            for (Building building : capital.getUnlockedBuildings()) {
                if (!colony.hasBuilding(building)) {
                    colony.unlockBuilding(building);
                }
            }
            colony.setHatchRateWorker(capital.getHatchRateWorker());
            colony.setHatchRateSoldier(capital.getHatchRateSoldier());
            colony.setHatchRateMajor(capital.getHatchRateMajor());
            colony.setHatchRateDrone(capital.getHatchRateDrone());
            colony.setHatchRatePrincess(capital.getHatchRatePrincess());
            AntSubtypeService.copySubtypeRates(colony, capital);
        }
        if (colony.isAutomationEnabled()) {
            if (overlord.isDefaultAutomationEnabled() && overlord.hasUpgrade(GameUnlocks.ABILITY_AUTOMATION)) {
                colony.setAutomationEnabled(true);
            }
            if (overlord.isDefaultAutoBuildEnabled() && overlord.hasUpgrade(GameUnlocks.ABILITY_MANAGEMENT)) {
                colony.setAutoBuildEnabled(true);
            }
            if (overlord.isDefaultAutoTunnelsEnabled() && overlord.hasUpgrade(GameUnlocks.ABILITY_AUTO_TUNNELS)) {
                colony.setAutoTunnelsEnabled(true);
            }
            colony.invalidateActiveRoleCountCache();
            colony.runRoleAssignment(null);
        }
        colony.refreshAntStats();
    }

    public void reestablishCapturedColony(Colony capital, Colony target) {
        if (capital == null || target == null || target.getQueens().isEmpty()) {
            return;
        }

        target.setDaysWithoutQueen(0);
        if (target.getAge() < 7) {
            target.setAge(7);
        }

        target.setHatchRateWorker(capital.getHatchRateWorker());
        target.setHatchRateSoldier(capital.getHatchRateSoldier());
        target.setHatchRateMajor(capital.getHatchRateMajor());
        target.setHatchRateDrone(capital.getHatchRateDrone());
        target.setHatchRatePrincess(capital.getHatchRatePrincess());
        AntSubtypeService.copySubtypeRates(target, capital);

        Dynasty dynasty = target.getDynasty();
        if (dynasty != null) {
            if (dynasty.isDefaultAutomationEnabled() && dynasty.hasUpgrade(GameUnlocks.ABILITY_AUTOMATION)) {
                target.setAutomationEnabled(true);
            }
            if (dynasty.isDefaultAutoBuildEnabled() && dynasty.hasUpgrade(GameUnlocks.ABILITY_MANAGEMENT)) {
                target.setAutoBuildEnabled(true);
            }
            if (dynasty.isDefaultAutoTunnelsEnabled() && dynasty.hasUpgrade(GameUnlocks.ABILITY_AUTO_TUNNELS)) {
                target.setAutoTunnelsEnabled(true);
            }
        }

        int workerDeficit = MIN_SUSTAIN_WORKERS - target.getWorkers().size();
        for (int i = 0; i < workerDeficit; i++) {
            target.getWorkers().add(new Ant(target, GameConstants.TYPE_WORKER));
        }

        ensurePeaceEconomyRoles(target);
        ensureStarterResourceSources(target);

        int consumption = Math.max(1, target.getTotalConsumption());
        if (target.getMushrooms() < consumption * 48) {
            target.getResourceService().addResource(target, GameConstants.RESOURCE_FUNGI, consumption * 48);
        }

        if (target.getPhysicsService() != null) {
            target.getPhysicsService().randomizeAllAntPositions(target);
        }
        target.invalidateActiveRoleCountCache();
        target.runRoleAssignment(null);
        ColonyMilitaryService.refreshColonyMilitaryPower(target);
    }

    private void ensurePeaceEconomyRoles(Colony colony) {
        applyStarterWorkforce(colony);
    }

    private void applyStarterWorkforce(Colony colony) {
        List<Ant> workers = colony.getWorkers();
        while (workers.size() < STARTING_WORKERS) {
            workers.add(new Ant(colony, GameConstants.TYPE_WORKER));
        }

        int index = 0;
        for (int i = 0; i < STARTING_NURSES; i++) {
            colony.configureWorker(index++, GameConstants.ROLE_NURSE, WorldSpaces.UNDERWORLD);
        }
        for (int i = 0; i < STARTING_FARMERS; i++) {
            colony.configureWorker(index++, GameConstants.ROLE_FARMER, WorldSpaces.UNDERWORLD);
        }
        for (int i = 0; i < STARTING_FORAGERS; i++) {
            colony.configureWorker(index++, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);
        }

        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_LAYER,
                Math.max(colony.getQueens().size(), Math.max(1, colony.getPeaceAssignedRoleCount(GameConstants.ROLE_LAYER))));
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_NURSE,
                Math.max(STARTING_NURSES, colony.getPeaceAssignedRoleCount(GameConstants.ROLE_NURSE)));
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_FARMER,
                Math.max(STARTING_FARMERS, colony.getPeaceAssignedRoleCount(GameConstants.ROLE_FARMER)));
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_FORAGER,
                Math.max(STARTING_FORAGERS, colony.getPeaceAssignedRoleCount(GameConstants.ROLE_FORAGER)));
    }

    private void ensureStarterResourceSources(Colony colony) {
        if (colony.getLocationService() == null
                || !colony.getLocationService().getDiscoveredSources().isEmpty()) {
            return;
        }
        int range = 300;
        int centerX = ColonySpatialLayout.ANCHOR_CENTER_X;
        int centerY = ColonySpatialLayout.ANCHOR_HEIGHT / 2;

        int pX = centerX + GameRandom.nextInt((range * 2) + 1) - range;
        int pY = centerY + GameRandom.nextInt((range * 2) + 1) - range;
        pX = Math.max(50, pX);
        pY = Math.max(50, pY);
        ResourceSource initialPlant = new ResourceSource(GameConstants.RESOURCE_PLANT, 10000, pX, pY);

        int wX = centerX + GameRandom.nextInt((range * 2) + 1) - range;
        int wY = centerY + GameRandom.nextInt((range * 2) + 1) - range;
        wX = Math.max(50, wX);
        wY = Math.max(50, wY);
        ResourceSource initialWater = new ResourceSource(GameConstants.RESOURCE_WATER, 10000, wX, wY);

        colony.getLocationService().addSource(colony, initialPlant);
        colony.getLocationService().addSource(colony, initialWater);
    }
}