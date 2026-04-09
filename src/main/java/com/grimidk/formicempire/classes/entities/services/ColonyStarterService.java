package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.repositories.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;
import java.util.List;
import java.util.Random;

public class ColonyStarterService {
    
    private String formatName(String name) {
        if (name == null || name.trim().isEmpty()) return "Player";
        name = name.trim();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public void initializeNewColony(Colony colony) {
        String type = colony.isPlayer() ? "Player" : "AI";
        
        if (colony.getDynasty() != null) {
            Dynasty d = colony.getDynasty();
            
            String baseName = d.getName();
            if (baseName != null && baseName.endsWith(" Dynasty")) {
                baseName = baseName.substring(0, baseName.length() - 8);
            }
            baseName = formatName(baseName);
            
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
        Random random = new Random();

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

        colony.setAssignedRoleCount(GameConstants.ROLE_LAYER, 1);
        colony.setAssignedRoleCount(GameConstants.ROLE_NURSE, 3);
        colony.setAssignedRoleCount(GameConstants.ROLE_FARMER, 1);
        colony.setAssignedRoleCount(GameConstants.ROLE_FORAGER, 5);

        if (colony.getLocationService() != null) {
            if (colony.getLocationService().getDiscoveredSources().isEmpty()) {
                int range = 300;
                
                int centerX = ColonyLocationService.ANCHOR_CENTER_X; 
                int centerY = ColonyLocationService.ANCHOR_HEIGHT / 2;
                
                int pX = centerX + random.nextInt((range * 2) + 1) - range;
                int pY = centerY + random.nextInt((range * 2) + 1) - range;
                pX = Math.max(50, pX);
                pY = Math.max(50, pY);
                ResourceSource initialPlant = new ResourceSource(GameConstants.RESOURCE_PLANT, 10000, pX, pY);
                
                int wX = centerX + random.nextInt((range * 2) + 1) - range;
                int wY = centerY + random.nextInt((range * 2) + 1) - range;
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
}