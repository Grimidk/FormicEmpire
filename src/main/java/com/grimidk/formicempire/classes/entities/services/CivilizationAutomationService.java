package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Civilization;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CivilizationAutomationService {

    public void runDailyAutomation(Civilization civ) {
        if (civ.isPlayer()) return; // Player manages their own tech
        
        // Check global policy settings if you add them later
        // For now, NPCs always try to buy upgrades
        checkAndBuyUpgrades(civ);
    }

    private void checkAndBuyUpgrades(Civilization civ) {
        List<Upgrade> candidates = new ArrayList<>();
        
        for (Upgrade u : GameUnlocks.getUpgrades()) {
            boolean notOwned = !civ.hasUpgrade(u);
            boolean reqMet = (u.getRequirement() == null || civ.hasUpgrade(u.getRequirement()));
            boolean validCost = u.getCost() > 0;
            boolean canAfford = civ.getResearchPoints() >= u.getCost();

            if (notOwned && reqMet && validCost && canAfford) {
                candidates.add(u);
            }
        }
        
        if (!candidates.isEmpty()) {
            // Simple AI: Buy cheapest available
            candidates.sort(Comparator.comparingInt(Upgrade::getCost));
            Upgrade target = candidates.get(0);
            
            civ.setResearchPoints(civ.getResearchPoints() - target.getCost());
            civ.unlockUpgrade(target);
            
            // Log for the first colony to give some world feedback
            if (!civ.getColonies().isEmpty()) {
                civ.getColonies().get(0).logEvent("CIVILIZATION: Researched " + target.getName());
            }
        }
    }
}