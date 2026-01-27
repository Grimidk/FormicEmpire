package com.grimidk.formicempire.classes.entities.services;

import java.util.List;
import java.util.Random;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.Dimension;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;

public class ColonyStarterService {

    private final Random random = new Random();

    public void initializeNewColony(Colony colony) {
        String type = colony.isPlayer() ? "Player" : "AI";
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

        List<Ant> workerList = colony.getWorkers();
        if (workerList != null) {
            for (int i = 0; i < 9; i++) {
                Ant worker = new Ant(colony, GameConstants.TYPE_WORKER);
                workerList.add(worker);
            }
        } else {
            System.err.println("[ColonyStarterService] Critical Error: Worker list is null.");
        }

        configureWorker(colony, 0, GameConstants.ROLE_NURSE, WorldSpaces.UNDERWORLD);
        configureWorker(colony, 1, GameConstants.ROLE_NURSE, WorldSpaces.UNDERWORLD);
        configureWorker(colony, 2, GameConstants.ROLE_FARMER, WorldSpaces.UNDERWORLD);
        configureWorker(colony, 3, GameConstants.ROLE_NURSE, WorldSpaces.UNDERWORLD);
        configureWorker(colony, 4, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);
        configureWorker(colony, 5, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);
        configureWorker(colony, 6, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);
        configureWorker(colony, 7, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);
        configureWorker(colony, 8, GameConstants.ROLE_FORAGER, WorldSpaces.OVERWORLD);

        colony.setAssignedRoleCount(GameConstants.ROLE_LAYER, 1);
        colony.setAssignedRoleCount(GameConstants.ROLE_NURSE, 3);
        colony.setAssignedRoleCount(GameConstants.ROLE_FARMER, 1);
        colony.setAssignedRoleCount(GameConstants.ROLE_FORAGER, 5);

        if (colony.getLocationService() != null) {
            if (colony.getLocationService().getDiscoveredSources().isEmpty()) {
                int range = 300;
                
                int pX = random.nextInt((range * 2) + 1) - range;
                int pY = random.nextInt((range * 2) + 1) - range;
                ResourceSource initialPlant = new ResourceSource(GameConstants.PLANT_RESOURCE, 10000, pX, pY);
                
                int wX = random.nextInt((range * 2) + 1) - range;
                int wY = random.nextInt((range * 2) + 1) - range;
                ResourceSource initialWater = new ResourceSource(GameConstants.WATER_RESOURCE, 10000, wX, wY);
                
                colony.getLocationService().addSource(colony, initialPlant);
                colony.getLocationService().addSource(colony, initialWater);
            }
        }
        
        if (colony.getPhysicsService() != null) {
            colony.getPhysicsService().randomizeAllAntPositions(colony);
        }
        
        System.out.println("[ColonyStarterService] Initialization complete for " + colony.getName() + " (ID: " + colony.getId() + "). Total Ants: " + colony.getAntTotal());
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

    private void configureWorker(Colony colony, int index, AntRole role, Dimension dim) {
        if (colony.getWorkers() != null && index < colony.getWorkers().size()) {
            Ant worker = colony.getWorkers().get(index);
            worker.setRole(role);
            worker.setDimension(dim);
        }
    }

    public void dismantleColony(Hex hex) {
        if (hex == null || hex.getColony() == null) return;
        Colony colony = hex.getColony();

        if (colony.isPlayer()) return;

        System.out.println("[ColonyStarterService] Dismantling dead NPC colony: " + colony.getName() + " at Hex (" + hex.getQ() + ", " + hex.getR() + ")");

        colony.setActive(false);
        colony.setAutomationEnabled(false);

        clearColonyLists(colony);
        if (colony.getDeadAnts() != null) colony.getDeadAnts().clear();
        if (colony.getBugs() != null) colony.getBugs().clear();

        hex.setColony(null);
    }
}