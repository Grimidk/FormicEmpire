package com.grimidk.formicempire.classes.infrasctructure.registries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;

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
}
