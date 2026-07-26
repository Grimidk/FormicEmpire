package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
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
        aggressor.unlockUpgrade(GameUnlocks.STAT_ACID);
        aggressor.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        defender.unlockUpgrade(GameUnlocks.STAT_ACID);
        defender.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        world = buildBorderWorld();
    }

    @Test
    void warBeginsAtHexDefenseWhenDefenderHasNoActiveMilitaryRoles() {
        Colony defenderColony = defender.getColonies().get(0);
        defenderColony.getWarAssignedRoleCounts().clear();
        ColonyMilitaryService.refreshColonyMilitaryPower(defenderColony);
        ColonyMilitaryService.refreshDynastyMilitaryPower(defender);

        War war = world.getWarService().beginWar(aggressor, defender);

        assertNotNull(war);
        assertEquals(GameConstants.WAR_STAGE_RESERVE_ASSAULT, war.getStagePhase());
        assertTrue(war.getDeployedReserveDefender() > 0);
        assertEquals(0, war.getDeployedActiveDefender());
    }

    @Test
    void beginWarInitializesCampaignAtFiftyPercent() {
        War war = world.getWarService().beginWar(aggressor, defender);

        assertNotNull(war);
        assertTrue(war.isCampaignInitialized());
        assertEquals(50f, war.getProgressPercent(), 0.01f);
        assertEquals(GameConstants.WAR_STAGE_ACTIVE_CLASH, war.getStagePhase());
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
    void withdrawToHexDefensePreservesArmiesAndOpensAssault() {
        War war = world.getWarService().beginWar(aggressor, defender);
        assertEquals(GameConstants.WAR_STAGE_ACTIVE_CLASH, war.getStagePhase());
        assertTrue(WarProgressService.canWithdrawToHexDefense(world, war, defender));
        assertFalse(WarProgressService.canWithdrawToHexDefense(world, war, aggressor));

        int defenderWarriors = defender.getColonies().get(0).getWarAssignedRoleCount(GameConstants.ROLE_WARRIOR);
        assertTrue(world.getWarService().withdrawToHexDefense(war, defender));

        assertEquals(GameConstants.WAR_STAGE_RESERVE_ASSAULT, war.getStagePhase());
        assertEquals(0, war.getDeployedActiveDefender());
        assertTrue(war.getDeployedActiveAttacker() > 0);
        assertTrue(war.getDeployedReserveDefender() > 0);
        assertEquals(defenderWarriors,
                defender.getColonies().get(0).getWarAssignedRoleCount(GameConstants.ROLE_WARRIOR));
        assertEquals(aggressor.getId(), war.getStageAttackerDynastyId());
        assertEquals(defender.getColonies().get(0).getId(), war.getContestedColonyId());
    }

    @Test
    void holdingHexFlipsStageAttackerForCounterattack() {
        World wideWorld = buildThreeHexWorld();
        War war = wideWorld.getWarService().beginWar(aggressor, defender);
        Colony mid = wideWorld.getHexes().get(1).getColony();
        assertNotNull(mid);
        assertEquals(mid.getId(), war.getContestedColonyId());

        assertTrue(wideWorld.getWarService().withdrawToHexDefense(war, defender));
        assertEquals(GameConstants.WAR_STAGE_RESERVE_ASSAULT, war.getStagePhase());

        int beforeDefenderStages = war.getDefenderStagesCaptured();
        assertTrue(WarProgressService.resolveHexDefenseHold(wideWorld, wideWorld.getWarService(), war));

        assertEquals(beforeDefenderStages + 1, war.getDefenderStagesCaptured());
        assertEquals(GameConstants.WAR_STAGE_REDEPLOYING, war.getStagePhase());
        assertEquals(defender.getId(), war.getStageAttackerDynastyId());
        assertEquals(aggressor.getCapital().getId(), war.getContestedColonyId());
    }

    @Test
    void aiHexBaitRejectedWhenAlreadyWinningBorderClash() {
        War war = world.getWarService().beginWar(aggressor, defender);
        Colony contested = defender.getColonies().get(0);
        war.setDeployedActiveAttacker(100);
        war.setDeployedActiveDefender(10_000);
        ColonyMilitaryService.refreshColonyMilitaryPower(contested);
        ColonyMilitaryService.refreshDynastyMilitaryPower(aggressor);

        assertFalse(WarProgressService.canAiHexBait(world, war, defender, aggressor, contested));
    }

    @Test
    void aiHexBaitAcceptedWhenHexAndCounterattackOddsFavorHold() {
        War war = world.getWarService().beginWar(aggressor, defender);
        Colony contested = defender.getColonies().get(0);
        Colony aggressorCapital = aggressor.getCapital();
        // Losing the border badly...
        war.setDeployedActiveAttacker(10_000);
        war.setDeployedActiveDefender(100);
        // Strong local hex defense for the bait.
        contested.getWarAssignedRoleCounts().put(GameConstants.ROLE_DEFENDER, 80);
        contested.getWarAssignedRoleCounts().put(GameConstants.ROLE_WARRIOR, 40);
        // Soft counterattack target: thin population defense, keep a small assault threat.
        aggressorCapital.getWorkers().clear();
        while (aggressorCapital.getSoldiers().size() > 5) {
            aggressorCapital.getSoldiers().remove(aggressorCapital.getSoldiers().size() - 1);
        }
        aggressorCapital.getWarAssignedRoleCounts().clear();
        aggressorCapital.getWarAssignedRoleCounts().put(GameConstants.ROLE_WARRIOR, 5);
        ColonyMilitaryService.refreshColonyMilitaryPower(contested);
        ColonyMilitaryService.refreshColonyMilitaryPower(aggressorCapital);
        ColonyMilitaryService.refreshDynastyMilitaryPower(aggressor);
        ColonyMilitaryService.refreshDynastyMilitaryPower(defender);

        assertTrue(ColonyMilitaryService.computeHexAssaultEffectiveAttackerPower(aggressor) > 0);
        assertTrue(WarProgressService.counterattackOddsLookFavorable(world, defender, aggressor));
        assertTrue(WarProgressService.canAiHexBait(world, war, defender, aggressor, contested));
    }

    @Test
    void aiHexBaitRejectedWhenCounterattackLooksHopeless() {
        War war = world.getWarService().beginWar(aggressor, defender);
        Colony contested = defender.getColonies().get(0);
        war.setDeployedActiveAttacker(10_000);
        war.setDeployedActiveDefender(100);
        contested.getWarAssignedRoleCounts().put(GameConstants.ROLE_DEFENDER, 80);
        contested.getWarAssignedRoleCounts().put(GameConstants.ROLE_WARRIOR, 40);
        // Strip defender dynasty assault power so a counterattack cannot follow the hold.
        for (Colony colony : defender.getColonies()) {
            colony.getWarAssignedRoleCounts().clear();
            colony.getWarAssignedRoleCounts().put(GameConstants.ROLE_DEFENDER, 80);
            ColonyMilitaryService.refreshColonyMilitaryPower(colony);
        }
        ColonyMilitaryService.refreshDynastyMilitaryPower(defender);
        ColonyMilitaryService.refreshDynastyMilitaryPower(aggressor);

        assertFalse(WarProgressService.counterattackOddsLookFavorable(world, defender, aggressor));
        assertFalse(WarProgressService.canAiHexBait(world, war, defender, aggressor, contested));
    }

    @Test
    void forfeitStageAdvancesOpponentWithoutBattle() {
        World wideWorld = buildThreeHexWorld();
        War war = wideWorld.getWarService().beginWar(aggressor, defender);
        int beforeCaptured = war.getAggressorStagesCaptured();

        assertTrue(wideWorld.getWarService().forfeitWarStage(war, defender));

        assertEquals(beforeCaptured + 1, war.getAggressorStagesCaptured());
        assertEquals(GameConstants.WAR_STAGE_REDEPLOYING, war.getStagePhase());
        assertEquals(GameNumbers.WAR_REDEPLOY_HOURS, war.getRedeployHoursRemaining());
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
        war.setStagePhase(GameConstants.WAR_STAGE_REDEPLOYING);
        war.setContestedColonyId(wideWorld.getHexes().get(1).getColony().getId());
        defender.setActiveMilitaryPower(9000);
        aggressor.setActiveMilitaryPower(1000);
        war.setStageStartActiveDefender(8000);

        assertFalse(WarProgressService.canAiFallback(wideWorld, war, defender, aggressor));
    }

    @Test
    void aiNeverFallbacksWithoutSpareHexes() {
        War war = world.getWarService().beginWar(aggressor, defender);
        war.setStagePhase(GameConstants.WAR_STAGE_REDEPLOYING);
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
        // Isolate from @BeforeEach border colonies on the shared dynasties.
        aggressor.getColonies().clear();
        defender.getColonies().clear();
        aggressor.setCapital(null);
        defender.setCapital(null);

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
        int needed = GameNumbers.WAR_DECLARATION_MIN_POPULATION
                - dynasty.getStatService().getTotalPopulation(dynasty);
        for (int i = 0; i < needed; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
        for (int i = 0; i < 20; i++) {
            colony.getSoldiers().add(new Ant(colony, GameConstants.TYPE_SOLDIER));
        }
        colony.getWarAssignedRoleCounts().put(GameConstants.ROLE_WARRIOR, 10);
        colony.getWarAssignedRoleCounts().put(GameConstants.ROLE_DEFENDER, 10);
        ColonyMilitaryService.refreshColonyMilitaryPower(colony);
        ColonyMilitaryService.refreshDynastyMilitaryPower(dynasty);
    }
}
