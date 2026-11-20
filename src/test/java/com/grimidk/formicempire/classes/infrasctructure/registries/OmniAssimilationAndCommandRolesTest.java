package com.grimidk.formicempire.classes.infrasctructure.registries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyAiService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import org.junit.jupiter.api.Test;

class OmniAssimilationAndCommandRolesTest {

    @Test
    void omniAssimilationGatesForeignSpeciesAssimilations() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        assertFalse(GameUnlocks.canAssimilateForeignSpecies(dynasty));

        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_ASSIMILATION);
        assertTrue(GameUnlocks.canAssimilateForeignSpecies(dynasty));
    }

    @Test
    void omniStartsWithNativeAssimilationButNoAssimilationUi() {
        Dynasty dynasty = new Dynasty(1, "Omni", true, GameConstants.SPECIES_OMNI);
        dynasty.getStarterService().initializeDynasty(dynasty);

        assertTrue(dynasty.isAssimilationCompleted(GameUnlocks.ASSIMILATION_OMNI));
        assertTrue(dynasty.hasUpgrade(GameUnlocks.ASSIMILATED_ASSIMILATION));
        assertTrue(GameUnlocks.canAssimilateForeignSpecies(dynasty));
        assertFalse(dynasty.hasUpgrade(GameUnlocks.ABILITY_ASSIMILATION));
        assertFalse(GameUnlocks.shouldShowAssimilationUi(dynasty));
    }

    @Test
    void assimilationUiAppearsOnlyWhenAbilityUnlockedAndGenomeAvailable() {
        Dynasty dynasty = new Dynasty(1, "Omni", true, GameConstants.SPECIES_OMNI);
        dynasty.getStarterService().initializeDynasty(dynasty);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_ASSIMILATION);
        assertFalse(GameUnlocks.shouldShowAssimilationUi(dynasty));

        dynasty.absorbSpecies(GameConstants.SPECIES_LEAFCUTTER.getId());
        assertTrue(GameUnlocks.isAssimilationAvailable(dynasty, GameUnlocks.ASSIMILATION_LEAFCUTTER));
        assertTrue(GameUnlocks.shouldShowAssimilationUi(dynasty));
    }

    @Test
    void assimilationUiStaysVisibleAfterMoreThanTwoCompletedAssimilations() {
        Dynasty dynasty = new Dynasty(1, "Omni", true, GameConstants.SPECIES_OMNI);
        dynasty.getStarterService().initializeDynasty(dynasty);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_ASSIMILATION);

        dynasty.completeAssimilation(GameUnlocks.ASSIMILATION_LEAFCUTTER);
        assertEquals(2, dynasty.getCompletedAssimilations().size());
        assertFalse(GameUnlocks.hasAssimilationUiContent(dynasty));
        assertFalse(GameUnlocks.shouldShowAssimilationUi(dynasty));

        dynasty.completeAssimilation(GameUnlocks.ASSIMILATION_PHARAOH);
        assertTrue(dynasty.getCompletedAssimilations().size() > 2);
        assertFalse(GameUnlocks.hasAssimilationUiContent(dynasty));
        assertTrue(GameUnlocks.shouldShowAssimilationUi(dynasty));
    }

    @Test
    void leafcutterStartsWithNativeAssimilationCompleted() {
        Dynasty dynasty = new Dynasty(2, "Leaf", false, GameConstants.SPECIES_LEAFCUTTER);
        dynasty.getStarterService().initializeDynasty(dynasty);

        assertTrue(dynasty.isAssimilationCompleted(GameUnlocks.ASSIMILATION_LEAFCUTTER));
        assertTrue(dynasty.hasUpgrade(GameUnlocks.ASSIMILATED_FARMING));
        assertFalse(GameUnlocks.canAssimilateForeignSpecies(dynasty));
        assertFalse(GameUnlocks.shouldShowAssimilationUi(dynasty));
    }

    @Test
    void commanderUnlockAlsoUnlocksCaptainAndPassiveSkills() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_COMMANDER);

        assertTrue(dynasty.hasUpgrade(GameUnlocks.ROLE_COMMANDER));
        assertTrue(dynasty.hasUpgrade(GameUnlocks.ROLE_CAPTAIN));
        assertTrue(dynasty.hasSkill(GameConstants.SKILL_ARTILLERY_LEADER));
        assertTrue(dynasty.hasSkill(GameConstants.SKILL_INFANTRY_LEADER));
    }

    @Test
    void preciseCommandsAndInfantryCommandArePassiveLaneAuras() {
        assertTrue(GameConstants.SKILL_ARTILLERY_LEADER.isPassive());
        assertFalse(GameConstants.SKILL_ARTILLERY_LEADER.isAttack());
        assertEquals(GameNumbers.COMMANDER_ARTILLERY_DAMAGE_BONUS,
                GameConstants.SKILL_ARTILLERY_LEADER.getLaneDamageBonus(), 0.0001f);
        assertEquals(GameConstants.BATTLE_LINE_ARTILLERY, GameConstants.SKILL_ARTILLERY_LEADER.getBattleLine());
        assertEquals(GameConstants.ROLE_COMMANDER, GameConstants.SKILL_ARTILLERY_LEADER.getRequiredRole());

        assertTrue(GameConstants.SKILL_INFANTRY_LEADER.isPassive());
        assertFalse(GameConstants.SKILL_INFANTRY_LEADER.isAttack());
        assertEquals(GameNumbers.CAPTAIN_INFANTRY_DAMAGE_BONUS,
                GameConstants.SKILL_INFANTRY_LEADER.getLaneDamageBonus(), 0.0001f);
        assertEquals(GameConstants.BATTLE_LINE_INFANTRY, GameConstants.SKILL_INFANTRY_LEADER.getBattleLine());
        assertEquals(GameConstants.ROLE_CAPTAIN, GameConstants.SKILL_INFANTRY_LEADER.getRequiredRole());
    }

    @Test
    void captainIsInfantryAndCommanderIsArtillery() {
        assertEquals(GameConstants.BATTLE_LINE_INFANTRY,
                GameConstants.getBattleLineForRole(GameConstants.ROLE_CAPTAIN));
        assertEquals(GameConstants.BATTLE_LINE_ARTILLERY,
                GameConstants.getBattleLineForRole(GameConstants.ROLE_COMMANDER));
        assertTrue(GameConstants.BATTLE_LINE_INFANTRY.getAllowedRoles().contains(GameConstants.ROLE_CAPTAIN));
        assertTrue(GameConstants.BATTLE_LINE_ARTILLERY.getAllowedRoles().contains(GameConstants.ROLE_COMMANDER));
    }

    @Test
    void onlyDuchyAndEmpireRanksUnlockSpecificUpgrades() {
        assertEquals(GameUnlocks.ROLE_POLICE, GameConstants.RANK_DUCHY.getUnlockOnAnnounce());
        assertEquals(GameUnlocks.ROLE_AIR_SUPPORT, GameConstants.RANK_KINGDOM.getUnlockOnAnnounce());
        assertEquals(GameUnlocks.ABILITY_CLONING, GameConstants.RANK_EMPIRE.getUnlockOnAnnounce());
        assertEquals(null, GameConstants.RANK_ANT.getUnlockOnAnnounce());
        assertEquals(null, GameConstants.RANK_COUNTY.getUnlockOnAnnounce());
        assertEquals(null, GameConstants.RANK_SUPER.getUnlockOnAnnounce());
        assertEquals(null, GameConstants.RANK_GIGA.getUnlockOnAnnounce());
    }

    @Test
    void sandboxAllowsNonOmniSpeciesAssimilationWithoutOmniBridge() {
        Engine engine = new Engine();
        engine.setAllowNonOmniAssimilation(false);
        World world = new World();
        world.setEngine(engine);

        Dynasty nonOmni = new Dynasty(1, "Leaf", true, GameConstants.SPECIES_LEAFCUTTER);
        nonOmni.setOwningWorld(world);
        nonOmni.getStarterService().initializeDynasty(nonOmni);
        nonOmni.unlockUpgrade(GameUnlocks.ABILITY_ASSIMILATION);
        nonOmni.absorbSpecies(GameConstants.SPECIES_FIRE.getId());

        assertFalse(GameUnlocks.canAssimilateForeignSpecies(nonOmni));
        assertFalse(GameUnlocks.isAssimilationAvailable(nonOmni, GameUnlocks.ASSIMILATION_FIREVENOM));
        assertFalse(GameUnlocks.shouldShowAssimilationUi(nonOmni));

        engine.setAllowNonOmniAssimilation(true);

        assertTrue(GameUnlocks.canAssimilateForeignSpecies(nonOmni));
        assertTrue(GameUnlocks.isAssimilationAvailable(nonOmni, GameUnlocks.ASSIMILATION_FIREVENOM));
        assertTrue(GameUnlocks.shouldShowAssimilationUi(nonOmni));
        assertFalse(nonOmni.hasUpgrade(GameUnlocks.ASSIMILATED_ASSIMILATION));
        assertFalse(nonOmni.isAssimilationCompleted(GameUnlocks.ASSIMILATION_OMNI));
    }

    @Test
    void aiNonOmniStartsForeignAssimilationWhenSandboxEnabled() {
        Engine engine = new Engine();
        engine.setAllowNonOmniAssimilation(true);
        World world = new World();
        world.setEngine(engine);

        Dynasty aiNonOmni = new Dynasty(2, "Leaf AI", false, GameConstants.SPECIES_LEAFCUTTER);
        aiNonOmni.setOwningWorld(world);
        aiNonOmni.getStarterService().initializeDynasty(aiNonOmni);
        aiNonOmni.unlockUpgrade(GameUnlocks.ABILITY_ASSIMILATION);
        aiNonOmni.unlockUpgrade(GameUnlocks.ROLE_RESEARCHER);
        aiNonOmni.absorbSpecies(GameConstants.SPECIES_FIRE.getId());
        Colony capital = new Colony(1, "Capital", false);
        capital.setCapital(true);
        aiNonOmni.addColony(capital);
        world.getDynastys().add(aiNonOmni);

        assertNull(aiNonOmni.getCurrentAssimilation());
        new DynastyAiService().runDailyAi(aiNonOmni, world, new TradeManager());
        assertEquals(GameUnlocks.ASSIMILATION_FIREVENOM, aiNonOmni.getCurrentAssimilation());
    }
}
