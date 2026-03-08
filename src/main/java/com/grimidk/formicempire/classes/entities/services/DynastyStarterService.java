package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Dynasty;

public class DynastyStarterService {
    
    public void initializeDynasty(Dynasty dynasty) {
        System.out.println("[DynastyStarterService] Initializing Dynasty: " + dynasty.getName());
        
        Species species = dynasty.getSpecies();
        if (species != null && species.getBaseUpgrades() != null) {
            for (Upgrade upgrade : species.getBaseUpgrades()) {
                dynasty.unlockUpgrade(upgrade);
            }
        }
    }
}