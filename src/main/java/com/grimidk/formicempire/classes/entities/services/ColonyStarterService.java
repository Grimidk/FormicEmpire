package com.grimidk.formicempire.classes.entities.services;

import java.util.List;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.Dimension;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;

public class ColonyStarterService {

    public void initializeNewColony(Colony colony) {
        System.out.println("[ColonyStarterService] Initializing new colony: " + colony.getName());
        clearColonyLists(colony);

        try {
            Ant queen = new Ant(colony, GameConstants.TYPE_QUEEN);
            queen.setDimension(WorldSpaces.UNDERWORLD); 
            queen.setRole(GameConstants.ROLE_LAYER);
            colony.getQueens().add(queen);
            System.out.println("[ColonyStarterService] Queen created and added.");
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
            System.out.println("[ColonyStarterService] 9 Workers created and added.");
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
                ResourceSource initialPlant = new ResourceSource(GameConstants.PLANT_RESOURCE, 10000, 0, 0);
                ResourceSource initialWater = new ResourceSource(GameConstants.WATER_RESOURCE, 10000, 0, 0);
                
                colony.getLocationService().addSource(colony, initialPlant);
                colony.getLocationService().addSource(colony, initialWater);
                System.out.println("[ColonyStarterService] Initial resources added.");
            }
        }
        
        if (colony.getPhysicsService() != null) {
            colony.getPhysicsService().randomizeAllAntPositions(colony);
        }
        
        System.out.println("[ColonyStarterService] Initialization complete. Final Ant Total: " + colony.getAntTotal());
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
}