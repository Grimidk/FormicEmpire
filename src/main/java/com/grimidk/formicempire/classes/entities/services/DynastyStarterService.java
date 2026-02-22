package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

public class DynastyStarterService {
    
    public void initializeDynasty(Dynasty civ) {
        System.out.println("Initialized Civ: " + civ.getName());
        
        civ.unlockUpgrade(GameUnlocks.TYPE_EGG);
        civ.unlockUpgrade(GameUnlocks.TYPE_QUEEN);
        civ.unlockUpgrade(GameUnlocks.TYPE_WORKER);
        civ.unlockUpgrade(GameUnlocks.ROLE_FORAGER);
        civ.unlockUpgrade(GameUnlocks.ROLE_FARMER);
        civ.unlockUpgrade(GameUnlocks.ROLE_NURSE);
        civ.unlockUpgrade(GameUnlocks.ROLE_LAYER);
        civ.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        civ.unlockUpgrade(GameUnlocks.STAT_ACID);
        civ.unlockUpgrade(GameUnlocks.STAT_LONGEVITY);
        
        if (civ.getSpecies() != null) {
        }
    }
}