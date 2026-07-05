package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.War;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarProgressServiceTest {

    private Dynasty aggressor;
    private Dynasty defender;
    private World world;

    @BeforeEach
    void setUp() {
        aggressor = new Dynasty(1, "Aggressor Dynasty", true, GameConstants.SPECIES_OMNI);
        defender = new Dynasty(2, "Defender Dynasty", false, GameConstants.SPECIES_OMNI);
        world = buildBorderWorld();
    }

    @Test
    void beginWarInitializesCampaignAtFiftyPercent() {
        War war = world.getWarService().beginWar(aggressor, defender);

        assertNotNull(war);
        assertTrue(war.isCampaignInitialized());
        assertEquals(50f, war.getProgressPercent(), 0.01f);
        assertEquals(WarStagePhase.ACTIVE_CLASH, war.getStagePhase());
        assertEquals(aggressor.getId(), war.getStageAttackerDynastyId());
        assertEquals(defender.getColonies().get(0).getId(), war.getContestedColonyId());
    }

    @Test
    void progressMovesTowardAggressorWhenStageCaptured() {
        War war = world.getWarService().beginWar(aggressor, defender);
        war.setTotalStages(2);
        war.setAggressorStagesCaptured(1);
        war.setStageProgress(0f);
        war.recomputeProgressPercent();

        assertEquals(75f, war.getProgressPercent(), 0.01f);
    }

    @Test
    void forfeitStageAdvancesOpponentWithoutBattle() {
        World wideWorld = buildThreeHexWorld();
        War war = wideWorld.getWarService().beginWar(aggressor, defender);
        int beforeCaptured = war.getAggressorStagesCaptured();

        assertTrue(wideWorld.getWarService().forfeitWarStage(war, defender));

        assertEquals(beforeCaptured + 1, war.getAggressorStagesCaptured());
        assertEquals(WarStagePhase.REDEPLOYING, war.getStagePhase());
        assertEquals(GameConstants.WAR_REDEPLOY_HOURS, war.getRedeployHoursRemaining());
    }

    @Test
    void cannotForfeitCapitalHex() {
        War war = world.getWarService().beginWar(aggressor, defender);
        war.setContestedColonyId(defender.getCapital().getId());

        assertFalse(WarProgressService.canForfeitStage(world, war, defender));
        assertFalse(world.getWarService().forfeitWarStage(war, defender));
    }

    @Test
    void aiNeverFallbacksWhenStronger() {
        World wideWorld = buildThreeHexWorld();
        War war = wideWorld.getWarService().beginWar(aggressor, defender);
        war.setStagePhase(WarStagePhase.REDEPLOYING);
        war.setContestedColonyId(wideWorld.getHexes().get(1).getColony().getId());
        defender.setActiveMilitaryPower(9000);
        aggressor.setActiveMilitaryPower(1000);
        war.setStageStartActiveDefender(8000);

        assertFalse(WarProgressService.canAiFallback(wideWorld, war, defender, aggressor));
    }

    @Test
    void aiNeverFallbacksWithoutSpareHexes() {
        War war = world.getWarService().beginWar(aggressor, defender);
        war.setStagePhase(WarStagePhase.REDEPLOYING);
        defender.setActiveMilitaryPower(1000);
        aggressor.setActiveMilitaryPower(2000);
        war.setStageStartActiveDefender(900);

        assertFalse(WarProgressService.canAiFallback(world, war, defender, aggressor));
    }

    @Test
    void saveRoundTripPreservesCampaignFields() {
        War war = world.getWarService().beginWar(aggressor, defender);
        assertNotNull(war);

        List<Savefile.SavedWar> saved = world.getWarService().toSavedWars();
        World reloaded = new World();
        reloaded.getDynastys().add(aggressor);
        reloaded.getDynastys().add(defender);
        reloaded.setHexes(world.getHexes());
        reloaded.getWarService().loadFromSave(saved);

        War loaded = reloaded.getWarService().findActiveWar(aggressor.getId(), defender.getId());
        assertNotNull(loaded);
        assertEquals(war.getProgressPercent(), loaded.getProgressPercent(), 0.01f);
        assertEquals(war.getTotalStages(), loaded.getTotalStages());
        assertEquals(war.getContestedColonyId(), loaded.getContestedColonyId());
        assertEquals(war.getStagePhase(), loaded.getStagePhase());
    }

    private World buildBorderWorld() {
        World world = new World();
        Hex aggressorHex = new Hex();
        Hex defenderHex = new Hex();
        aggressorHex.setQ(0);
        aggressorHex.setR(0);
        defenderHex.setQ(1);
        defenderHex.setR(0);
        aggressorHex.setNorthEast(defenderHex);
        defenderHex.setSouthWest(aggressorHex);

        Colony aggressorColony = new Colony(10, "Aggressor Capital", true);
        Colony defenderColony = new Colony(11, "Defender Border", false);
        aggressor.addColony(aggressorColony);
        defender.addColony(defenderColony);
        aggressor.setCapital(aggressorColony);
        defender.setCapital(defenderColony);
        aggressorColony.setDynasty(aggressor);
        defenderColony.setDynasty(defender);
        aggressorHex.setColony(aggressorColony);
        defenderHex.setColony(defenderColony);
        seedPopulation(aggressor, aggressorColony);
        seedPopulation(defender, defenderColony);
        aggressor.setActiveMilitaryPower(5000);
        aggressor.setMilitaryPower(5000);
        defender.setActiveMilitaryPower(5000);
        defender.setMilitaryPower(5000);
        aggressorColony.setActiveMilitaryPower(5000);
        aggressorColony.setMilitaryPower(5000);
        defenderColony.setActiveMilitaryPower(5000);
        defenderColony.setMilitaryPower(5000);

        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(aggressorHex);
        hexes.add(defenderHex);
        world.setHexes(hexes);
        world.getDynastys().add(aggressor);
        world.getDynastys().add(defender);
        return world;
    }

    private World buildThreeHexWorld() {
        World world = new World();
        Hex h0 = new Hex();
        Hex h1 = new Hex();
        Hex h2 = new Hex();
        h0.setQ(0);
        h0.setR(0);
        h1.setQ(1);
        h1.setR(0);
        h2.setQ(2);
        h2.setR(0);
        h0.setNorthEast(h1);
        h1.setSouthWest(h0);
        h1.setNorthEast(h2);
        h2.setSouthWest(h1);

        Colony aggressorColony = new Colony(20, "Aggressor Capital", true);
        Colony midColony = new Colony(21, "Defender Mid", false);
        Colony defenderColony = new Colony(22, "Defender Capital", false);
        aggressor.addColony(aggressorColony);
        defender.addColony(midColony);
        defender.addColony(defenderColony);
        aggressor.setCapital(aggressorColony);
        defender.setCapital(defenderColony);
        aggressorColony.setDynasty(aggressor);
        midColony.setDynasty(defender);
        defenderColony.setDynasty(defender);
        h0.setColony(aggressorColony);
        h1.setColony(midColony);
        h2.setColony(defenderColony);
        seedPopulation(aggressor, aggressorColony);
        seedPopulation(defender, midColony);
        seedPopulation(defender, defenderColony);
        aggressor.setActiveMilitaryPower(5000);
        aggressor.setMilitaryPower(5000);
        defender.setActiveMilitaryPower(5000);
        defender.setMilitaryPower(5000);

        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(h0);
        hexes.add(h1);
        hexes.add(h2);
        world.setHexes(hexes);
        world.getDynastys().add(aggressor);
        world.getDynastys().add(defender);
        return world;
    }

    private static void seedPopulation(Dynasty dynasty, Colony colony) {
        int needed = GameConstants.WAR_DECLARATION_MIN_POPULATION
                - dynasty.getStatService().getTotalPopulation(dynasty);
        for (int i = 0; i < needed; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
    }
}
