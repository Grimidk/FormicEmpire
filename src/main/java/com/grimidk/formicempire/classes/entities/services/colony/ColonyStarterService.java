package com.grimidk.formicempire.classes.entities.services.colony;

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

    private static final int MIN_SUSTAIN_WORKERS = 9;

    private static final ColonyStarterService SHARED = new ColonyStarterService();

    public static ColonyStarterService shared() {
        return SHARED;
    }
    
    private String colonyStarterBaseName(String dynastyName) {
        String base = LanguageStrings.dynastyThemeBase(dynastyName);
        return base.isEmpty() ? "Player" : base;
    }

    public void initializeNewColony(Colony colony) {
        String type = colony.isPlayer() ? "Player" : "AI";
        
        if (colony.getDynasty() != null) {
            Dynasty d = colony.getDynasty();
            
            String baseName = colonyStarterBaseName(d.getName());
            
            int index = d.getColonies().indexOf(colony);
            if (index == -1) index = d.getColonies().size(); 
            
            String newName;
            if (index == 0) newName = baseName + " Prime";
            else if (index == 1) newName = "New " + baseName;
            else if (index == 2) newName = baseName + " Secundus";
            else if (index == 3) newName = baseName + " Tertius";
            else if (index == 4) newName = baseName + " Quartus";
            else newName = baseName + " " + (index + 1);
            
            colony.setName(newName);
            
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

    public void matureColony(Colony colony) {
        System.out.println("[ColonyStarterService] Maturation complete. Spawning workforce for " + colony.getName());

        List<Ant> workerList = colony.getWorkers();
        for (int i = 0; i < 9; i++) {
            Ant worker = new Ant(colony, GameConstants.TYPE_WORKER);
            workerList.add(worker);
        }

        colony.configureWorker(0, GameConstants.ROLE_NURSE, WorldSpaces.UNDERWORLD);
        colony.configureWorker(1, GameConstants.ROLE_NURSE, WorldSpaces.UNDERWORLD);
        colony.configureWorker(2, GameConstants.ROLE_FARMER, WorldSpaces.UNDERWORLD);
        colony.configureWorker(3, GameConstants.ROLE_NURSE, WorldSpaces.UNDERWORLD);
        colony.configureWorker(4, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);
        colony.configureWorker(5, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);
        colony.configureWorker(6, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);
        colony.configureWorker(7, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);
        colony.configureWorker(8, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);

        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_LAYER, 1);
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_NURSE, 3);
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_FARMER, 1);
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_FORAGER, 5);

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

    /**
     * Rebuilds a colony immediately after conquest so battle losses do not leave it queenless or depopulated.
     */
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

    /**
     * Seeds workforce, peace roles, resources, and food so a queenless captured colony can sustain after war.
     */
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
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_LAYER,
                Math.max(colony.getQueens().size(), colony.getPeaceAssignedRoleCount(GameConstants.ROLE_LAYER)));
        if (colony.getPeaceAssignedRoleCount(GameConstants.ROLE_NURSE) < 3) {
            colony.setPeaceAssignedRoleCount(GameConstants.ROLE_NURSE, 3);
        }
        if (colony.getPeaceAssignedRoleCount(GameConstants.ROLE_FARMER) < 1) {
            colony.setPeaceAssignedRoleCount(GameConstants.ROLE_FARMER, 1);
        }
        if (colony.getPeaceAssignedRoleCount(GameConstants.ROLE_FORAGER) < 5) {
            colony.setPeaceAssignedRoleCount(GameConstants.ROLE_FORAGER, 5);
        }

        List<Ant> workers = colony.getWorkers();
        if (workers.size() < MIN_SUSTAIN_WORKERS) {
            return;
        }
        colony.configureWorker(0, GameConstants.ROLE_NURSE, WorldSpaces.UNDERWORLD);
        colony.configureWorker(1, GameConstants.ROLE_NURSE, WorldSpaces.UNDERWORLD);
        colony.configureWorker(2, GameConstants.ROLE_FARMER, WorldSpaces.UNDERWORLD);
        colony.configureWorker(3, GameConstants.ROLE_NURSE, WorldSpaces.UNDERWORLD);
        colony.configureWorker(4, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);
        colony.configureWorker(5, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);
        colony.configureWorker(6, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);
        colony.configureWorker(7, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);
        colony.configureWorker(8, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);
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