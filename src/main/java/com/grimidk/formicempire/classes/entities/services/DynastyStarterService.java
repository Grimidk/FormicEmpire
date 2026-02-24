package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

public class DynastyStarterService {
    
    public void initializeDynasty(Dynasty dynasty) {
        System.out.println("[DynastyStarterService] Initializing Dynasty: " + dynasty.getName());
        
        dynasty.unlockUpgrade(GameUnlocks.TYPE_EGG);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_QUEEN);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_WORKER);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_FORAGER);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_FARMER);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_NURSE);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_LAYER);
        dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
        dynasty.unlockUpgrade(GameUnlocks.STAT_LONGEVITY);
        
        if (dynasty.getSpecies() != null) {
        }
    }
}