package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;

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
            colony.setCapital(isFirst);
            
            if (isFirst) {
                colony.setAge(7); 
            } else {
                colony.setAge(0);
                
                Colony capitalColony = null;
                for (Colony c : d.getColonies()) {
                    if (c.isCapital() && c != colony) {
                        capitalColony = c;
                        break;
                    }
                }
                
                if (capitalColony != null) {
                    colony.setHatchRateWorker(capitalColony.getHatchRateWorker());
                    colony.setHatchRateSoldier(capitalColony.getHatchRateSoldier());
                    colony.setHatchRateMajor(capitalColony.getHatchRateMajor());
                    colony.setHatchRateDrone(capitalColony.getHatchRateDrone());
                    colony.setHatchRatePrincess(capitalColony.getHatchRatePrincess());
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
            colony.matureColony();
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
}