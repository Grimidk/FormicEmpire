package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarCapitalCaptureTest {

    @Test
    void capturingEnemyCapitalDoesNotCrownItForVictor() {
        Dynasty aggressor = new Dynasty(1, "Aggressor Dynasty", true, GameConstants.SPECIES_OMNI);
        Dynasty defender = new Dynasty(2, "Defender Dynasty", false, GameConstants.SPECIES_OMNI);

        Colony aggressorCapital = new Colony(10, "Aggressor Prime", true);
        Colony defenderCapital = new Colony(11, "Defender Prime", false);
        Colony defenderSatellite = new Colony(12, "Defender Secundus", false);
        aggressor.addColony(aggressorCapital);
        defender.addColony(defenderCapital);
        defender.addColony(defenderSatellite);
        aggressor.setCapital(aggressorCapital);
        defender.setCapital(defenderCapital);
        aggressorCapital.setDynasty(aggressor);
        defenderCapital.setDynasty(defender);
        defenderSatellite.setDynasty(defender);

        Hex aggressorHex = new Hex();
        Hex defenderHex = new Hex();
        aggressorHex.setQ(0);
        aggressorHex.setR(0);
        defenderHex.setQ(1);
        defenderHex.setR(0);
        aggressorHex.setNorthEast(defenderHex);
        defenderHex.setSouthWest(aggressorHex);
        aggressorHex.setColony(aggressorCapital);
        defenderHex.setColony(defenderCapital);

        seedMilitary(aggressor, aggressorCapital);
        seedMilitary(defender, defenderCapital);
        seedMilitary(defender, defenderSatellite);

        World world = new World();
        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(aggressorHex);
        hexes.add(defenderHex);
        world.setHexes(hexes);
        world.getDynastys().add(aggressor);
        world.getDynastys().add(defender);

        War war = world.getWarService().beginWar(aggressor, defender);

        WarProgressService.captureColony(world, world.getWarService(), war,
                defenderCapital, aggressor, defender, true);

        assertEquals(aggressor, defenderCapital.getDynasty());
        assertFalse(defenderCapital.isCapital(), "Captured colony must not remain capital");
        assertEquals(aggressorCapital, aggressor.getCapital(), "Victor capital must not change");
        assertNotNull(defender.getCapital(), "Defender must crown a new capital");
        assertEquals(defenderSatellite, defender.getCapital());
    }

    @Test
    void resolveCapitalFromColoniesRepairsForeignCapturedPrime() {
        Dynasty dynasty = new Dynasty(1, "Grim Dynasty", true, GameConstants.SPECIES_OMNI);
        Colony founding = new Colony(1, "Grim Prime", true);
        Colony captured = new Colony(39, "Wind Prime", false);
        dynasty.addColony(founding);
        dynasty.addColony(captured);
        dynasty.setCapital(captured);

        dynasty.resolveCapitalFromColonies();

        assertEquals(founding, dynasty.getCapital());
        assertTrue(founding.isCapital());
        assertFalse(captured.isCapital());
    }

    @Test
    void absoluteVictoryTransfersAllRemainingLoserColonies() {
        Dynasty aggressor = new Dynasty(1, "Aggressor Dynasty", true, GameConstants.SPECIES_OMNI);
        Dynasty defender = new Dynasty(2, "Defender Dynasty", false, GameConstants.SPECIES_OMNI);

        Colony aggressorCapital = new Colony(10, "Aggressor Prime", true);
        Colony defenderCapital = new Colony(11, "Defender Prime", false);
        Colony defenderRemote = new Colony(12, "Defender Remote", false);
        aggressor.addColony(aggressorCapital);
        defender.addColony(defenderCapital);
        defender.addColony(defenderRemote);
        aggressor.setCapital(aggressorCapital);
        defender.setCapital(defenderCapital);
        aggressorCapital.setDynasty(aggressor);
        defenderCapital.setDynasty(defender);
        defenderRemote.setDynasty(defender);

        Hex aggressorHex = new Hex();
        Hex defenderHex = new Hex();
        Hex remoteHex = new Hex();
        aggressorHex.setQ(0);
        aggressorHex.setR(0);
        defenderHex.setQ(1);
        defenderHex.setR(0);
        remoteHex.setQ(3);
        remoteHex.setR(0);
        aggressorHex.setNorthEast(defenderHex);
        defenderHex.setSouthWest(aggressorHex);
        aggressorHex.setColony(aggressorCapital);
        defenderHex.setColony(defenderCapital);
        remoteHex.setColony(defenderRemote);

        seedMilitary(aggressor, aggressorCapital);
        seedMilitary(defender, defenderCapital);
        seedMilitary(defender, defenderRemote);

        World world = new World();
        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(aggressorHex);
        hexes.add(defenderHex);
        hexes.add(remoteHex);
        world.setHexes(hexes);
        world.getDynastys().add(aggressor);
        world.getDynastys().add(defender);

        War war = world.getWarService().beginWar(aggressor, defender);
        world.getWarService().concludeWar(war, aggressor.getId(), LanguageStrings.WAR_CONCLUSION_ABSOLUTE_VICTORY);

        assertTrue(defender.isDefeated());
        assertTrue(defender.getColonies().isEmpty());
        assertEquals(aggressor, defenderCapital.getDynasty());
        assertEquals(aggressor, defenderRemote.getDynasty());
        assertFalse(defenderCapital.isCapital());
        assertFalse(defenderRemote.isCapital());
        assertEquals(2, war.getColoniesCapturedBy(aggressor.getId()).size());
    }

    private static void seedMilitary(Dynasty dynasty, Colony colony) {
        for (int i = 0; i < GameNumbers.WAR_DECLARATION_MIN_POPULATION; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
        for (int i = 0; i < 20; i++) {
            colony.getSoldiers().add(new Ant(colony, GameConstants.TYPE_SOLDIER));
        }
        ColonyMilitaryService.refreshColonyMilitaryPower(colony);
        ColonyMilitaryService.refreshDynastyMilitaryPower(dynasty);
    }
}
