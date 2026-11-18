package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

public class DynastyStarterService {
    
    public void initializeDynasty(Dynasty dynasty) {
        System.out.println("[DynastyStarterService] Initializing Dynasty: " + dynasty.getName());
        
        AntSpecies species = dynasty.getSpecies();
        if (species != null && species.getBaseUpgrades() != null) {
            for (Upgrade upgrade : species.getBaseUpgrades()) {
                if (upgrade == GameUnlocks.ROLE_FARMER
                        && dynasty.getStartingDiet() == GameConstants.DIET_CARNIVOROUS) {
                    continue;
                }
                dynasty.unlockUpgrade(upgrade);
            }
        }
        if (dynasty.getStartingDiet() == GameConstants.DIET_CARNIVOROUS) {
            dynasty.revokeUpgrade(GameUnlocks.ROLE_FARMER);
            dynasty.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
            dynasty.unlockUpgrade(GameUnlocks.ROLE_HUNTER);
        } else {
            dynasty.unlockUpgrade(GameUnlocks.ROLE_FARMER);
        }
        dynasty.unlockUpgrade(GameUnlocks.STAT_LONGEVITY);
        dynasty.ensureNativeAssimilationCompleted();
    }
}