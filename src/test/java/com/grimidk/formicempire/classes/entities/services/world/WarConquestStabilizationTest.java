package com.grimidk.formicempire.classes.entities.services.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.War;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyStarterService;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class WarConquestStabilizationTest {

    @Test
    void captureStabilizesQueenlessDepletedColony() {
        Dynasty aggressor = new Dynasty(1, "Aggressor Dynasty", true, GameConstants.SPECIES_OMNI);
        Dynasty defender = new Dynasty(2, "Defender Dynasty", false, GameConstants.SPECIES_OMNI);

        Colony aggressorCapital = new Colony(10, "Aggressor Prime", true);
        Colony defenderColony = new Colony(11, "Defender Prime", false);
        defenderColony.setAge(10);
        aggressor.addColony(aggressorCapital);
        defender.addColony(defenderColony);
        aggressor.setCapital(aggressorCapital);
        defender.setCapital(defenderColony);
        aggressorCapital.setDynasty(aggressor);
        defenderColony.setDynasty(defender);

        Hex aggressorHex = new Hex();
        Hex defenderHex = new Hex();
        aggressorHex.setQ(0);
        aggressorHex.setR(0);
        defenderHex.setQ(1);
        defenderHex.setR(0);
        aggressorHex.setNorthEast(defenderHex);
        defenderHex.setSouthWest(aggressorHex);
        aggressorHex.setColony(aggressorCapital);
        defenderHex.setColony(defenderColony);

        World world = new World();
        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(aggressorHex);
        hexes.add(defenderHex);
        world.setHexes(hexes);
        world.getDynastys().add(aggressor);
        world.getDynastys().add(defender);

        War war = world.getWarService().beginWar(aggressor, defender);
        WarProgressService.captureColony(world, world.getWarService(), war,
                defenderColony, aggressor, defender, true);

        assertEquals(aggressor, defenderColony.getDynasty());
        assertFalse(defenderColony.getQueens().isEmpty());
        assertTrue(defenderColony.getWorkers().size() >= 9);
        assertEquals(0, defenderColony.getDaysWithoutQueen());
        assertEquals(GameConstants.RECENTLY_CONQUERED_LOYALTY_MONTHS,
                defenderColony.getRecentlyConqueredMonthsRemaining());
    }

    @Test
    void recentlyConqueredAppliesLoyaltyPenalty() {
        Colony colony = new Colony(1, "Frontier", true);
        colony.setLoyalty(50);
        colony.setRecentlyConqueredMonthsRemaining(GameConstants.RECENTLY_CONQUERED_LOYALTY_MONTHS);

        int effective = colony.getEffectiveLoyalty(null, null);

        assertEquals(45, effective);
    }

    @Test
    void stabilizeConqueredColonyRefillsWorkersWithoutReplacingExistingQueen() {
        Dynasty victor = new Dynasty(1, "Victor", true, GameConstants.SPECIES_OMNI);
        Colony capital = new Colony(10, "Capital", true);
        capital.setAge(10);
        victor.addColony(capital);
        victor.setCapital(capital);

        Colony captured = new Colony(11, "Captured", false);
        captured.setAge(10);
        captured.getQueens().add(new Ant(captured, GameConstants.TYPE_QUEEN));
        ColonyStarterService.shared().matureColony(captured);
        captured.getWorkers().clear();
        captured.getSoldiers().clear();

        ColonyStarterService.shared().stabilizeConqueredColony(victor, captured);

        assertEquals(1, captured.getQueens().size());
        assertTrue(captured.getWorkers().size() >= 9);
        assertEquals(GameConstants.RECENTLY_CONQUERED_LOYALTY_MONTHS,
                captured.getRecentlyConqueredMonthsRemaining());
    }
}
