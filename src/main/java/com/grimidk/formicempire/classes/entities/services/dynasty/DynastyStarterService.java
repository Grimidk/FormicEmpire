package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;

public class DynastyStarterService {
    
    public void initializeDynasty(Dynasty dynasty) {
        System.out.println("[DynastyStarterService] Initializing Dynasty: " + dynasty.getName());
        
        AntSpecies species = dynasty.getSpecies();
        if (species != null && species.getBaseUpgrades() != null) {
            for (Upgrade upgrade : species.getBaseUpgrades()) {
                dynasty.unlockUpgrade(upgrade);
            }
        }
        dynasty.ensureNativeAssimilationCompleted();
    }
}