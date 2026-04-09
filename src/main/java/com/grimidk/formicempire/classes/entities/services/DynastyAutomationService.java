package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.repositories.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DynastyAutomationService {

    public void runDailyAutomation(Dynasty dynasty) {
        if (dynasty.isPlayer()) return;
        
        checkAndBuyUpgrades(dynasty);
    }

    private void checkAndBuyUpgrades(Dynasty dynasty) {
        List<Upgrade> candidates = new ArrayList<>();
        
        for (Upgrade u : GameUnlocks.getUpgrades()) {
            boolean notOwned = !dynasty.hasUpgrade(u);
            boolean reqMet = (u.getRequirement() == null || dynasty.hasUpgrade(u.getRequirement()));
            boolean validCost = u.getCost() > 0;
            boolean canAfford = dynasty.getResearchPoints() >= u.getCost();

            if (notOwned && reqMet && validCost && canAfford) {
                candidates.add(u);
            }
        }
        
        if (!candidates.isEmpty()) {
            candidates.sort(Comparator.comparingInt(Upgrade::getCost));
            Upgrade target = candidates.get(0);
            
            dynasty.setResearchPoints(dynasty.getResearchPoints() - target.getCost());
            dynasty.unlockUpgrade(target);
            
            if (!dynasty.getColonies().isEmpty()) {
                dynasty.getColonies().get(0).logEvent(ColonyLogPrefixes.DYNASTY + " "
                    + String.format(LanguageStrings.get(LanguageStrings.LOG_DYNASTY_RESEARCHED_FMT), target.getName()));
            }
        }
    }
}