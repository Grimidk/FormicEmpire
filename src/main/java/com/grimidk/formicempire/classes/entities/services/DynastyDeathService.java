package com.grimidk.formicempire.classes.entities.services;

import java.util.ArrayList;
import java.util.List;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.repositories.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

public class DynastyDeathService {

    private static final ColonyStarterService STARTER = ColonyStarterService.shared();

    public void processDynastyDeaths(World world) {
        for (Dynasty dynasty : world.getDynastys()) {
            if (dynasty.isDefeated()) continue;
            
            List<Colony> deadColonies = new ArrayList<>();
            boolean capitalDied = false;
            
            for (Colony c : dynasty.getColonies()) {
                boolean deadFromZeroAnts = c.getAntTotal() == 0 && c.getAge() >= 7;
                boolean deadFromNoQueens = c.getDaysWithoutQueen() >= 7;
                
                if (deadFromZeroAnts || deadFromNoQueens) {
                    if (deadFromNoQueens && c.getAntTotal() > 0) {
                        System.out.println("[DynastyDeathService] Colony collapsed due to 7 days without a Queen: " + c.getName());
                    }
                    deadColonies.add(c);
                    if (c.isCapital()) {
                        capitalDied = true;
                    }
                }
            }
            
            if (deadColonies.isEmpty()) continue;
            
            if (deadColonies.size() == dynasty.getColonies().size()) {
                Colony remnant = deadColonies.stream().filter(Colony::isCapital).findFirst().orElse(deadColonies.get(0));
                
                for (Colony dead : deadColonies) {
                    if (dead == remnant) {
                        dead.setActive(false);
                        dead.setAutomationEnabled(false);
                        dead.logEvent(ColonyLogPrefixes.INFO + " "
                            + LanguageStrings.get(LanguageStrings.LOG_LAST_COLONY_FALLEN));
                    } else {
                        Hex hex = findHexForColony(world, dead);
                        if (hex != null) STARTER.dismantleColony(hex);
                    }
                }
                
                dynasty.setDefeated(true);
                System.out.println("[DynastyDeathService] Dynasty Defeated: " + dynasty.getName());
                
            } else {
                for (Colony dead : deadColonies) {
                    Hex hex = findHexForColony(world, dead);
                    if (hex != null) STARTER.dismantleColony(hex);
                    dynasty.removeColony(dead);
                }
                
                if (capitalDied) {
                    Colony newCapital = null;
                    int maxAnts = -1;
                    
                    for (Colony c : dynasty.getColonies()) {
                        if (c.getAntTotal() > maxAnts) {
                            maxAnts = c.getAntTotal();
                            newCapital = c;
                        }
                    }
                    
                    if (newCapital != null) {
                        newCapital.setCapital(true);
                        newCapital.logEvent(ColonyLogPrefixes.PROMOTION + " "
                            + String.format(LanguageStrings.get(LanguageStrings.LOG_PROMOTION_CAPITAL_FMT),
                                dynasty.getName()));
                        System.out.println("[DynastyDeathService] New capital crowned for " + dynasty.getName() + ": " + newCapital.getName());
                    }
                }
            }
        }
    }
    
    private Hex findHexForColony(World world, Colony colony) {
        for (Hex h : world.getHexes()) {
            if (h.getColony() == colony) return h;
        }
        return null;
    }
}