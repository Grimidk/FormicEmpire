package com.grimidk.formicempire.classes.entities.dynasty;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NuptialFlightCostTest {

    @Test
    void forcedFlightCostIsOneFifthOfLegacyBaseScale() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        Colony capital = new Colony(1, "Capital", true);
        dynasty.addColony(capital);

        assertEquals(GameNumbers.FORCED_FLIGHT_BASE_COST * 2, capital.getNuptialFlightCost());
        assertEquals(200 * 2, capital.getNuptialFlightCost());
    }

    @Test
    void massFlightCostIsHalfOfPriorTenTimesMultiplier() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        Colony capital = new Colony(1, "Capital", true);
        dynasty.addColony(capital);

        int forced = capital.getNuptialFlightCost();
        assertEquals(forced * GameNumbers.MASS_FLIGHT_COST_MULTIPLIER, dynasty.getMassNuptialFlightCost());
        assertEquals(forced * 5, dynasty.getMassNuptialFlightCost());
    }

    @Test
    void forcedFlightScalesWithColonyCountSquared() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        Colony capital = new Colony(1, "Capital", true);
        dynasty.addColony(capital);
        dynasty.addColony(new Colony(2, "Second", false));
        dynasty.addColony(new Colony(3, "Third", false));

        int colonyCount = dynasty.getColonies().size();
        int expected = GameNumbers.FORCED_FLIGHT_BASE_COST * (1 + colonyCount * colonyCount);
        assertEquals(expected, capital.getNuptialFlightCost());
        assertEquals(expected * GameNumbers.MASS_FLIGHT_COST_MULTIPLIER, dynasty.getMassNuptialFlightCost());
    }
}
