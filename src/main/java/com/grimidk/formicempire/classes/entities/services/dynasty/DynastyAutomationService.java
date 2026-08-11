package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

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
            boolean tierMet = u.isAvailableFor(dynasty);
            boolean validCost = u.getCost() > 0;
            boolean canAfford = dynasty.getResearchPoints() >= u.getCost();

            if (notOwned && reqMet && tierMet && validCost && canAfford) {
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
                    + LanguageStrings.format(LanguageStrings.LOG_DYNASTY_RESEARCHED_FMT, target.getName()));
            }
        }
    }
}