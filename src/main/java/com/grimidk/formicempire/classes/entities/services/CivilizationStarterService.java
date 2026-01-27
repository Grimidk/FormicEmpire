package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.entities.Civilization;

public class CivilizationStarterService {
    
    public void initializeCivilization(Civilization civ) {
        // Apply species logic here in the future
        // Give starting tech here if needed beyond defaults
        System.out.println("Initialized Civ: " + civ.getName());
    }
}