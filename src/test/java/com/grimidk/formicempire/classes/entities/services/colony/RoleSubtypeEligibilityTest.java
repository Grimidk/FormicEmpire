package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.critter.ant.AntMod;
import com.grimidk.formicempire.classes.constants.critter.ant.AntModProfile;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class RoleSubtypeEligibilityTest {

    @Test
    void standardAntsEligibleWhenNoSpecialsAllowedAndRoleHasNoRequirements() {
        Colony colony = new Colony(10, "Elig", true);
        Ant ant = new Ant(colony, GameConstants.CLASS_WORKER);
        ant.setModProfile(AntModProfile.standard());

        assertTrue(AntModService.isAntEligibleForRole(
                ant, GameConstants.ROLE_FORAGER, Set.of()));
    }

    @Test
    void standardAntsBlockedWhenRoleRequiresSubtype() {
        Colony colony = new Colony(11, "Elig", true);
        Ant ant = new Ant(colony, GameConstants.CLASS_WORKER);
        ant.setModProfile(AntModProfile.standard());

        assertFalse(AntModService.isAntEligibleForRole(
                ant, GameConstants.ROLE_POTTER, Set.of(GameConstants.MOD_ABDOMEN_HONEYPOT.getId())));
    }

    @Test
    void honeypotAntEligibleForPotter() {
        Colony colony = new Colony(12, "Elig", true);
        Ant ant = new Ant(colony, GameConstants.CLASS_WORKER);
        ant.setModProfile(AntModProfile.of(
                AntMod.DIGIT_NONE,
                AntMod.DIGIT_NONE,
                GameConstants.MOD_ABDOMEN_HONEYPOT.getDigit(),
                AntMod.DIGIT_NONE));

        assertTrue(AntModService.isAntEligibleForRole(
                ant, GameConstants.ROLE_POTTER, Set.of(GameConstants.MOD_ABDOMEN_HONEYPOT.getId())));
    }

    @Test
    void disallowedSpecialSubtypeBlocksAssignment() {
        Colony colony = new Colony(13, "Elig", true);
        Ant ant = new Ant(colony, GameConstants.CLASS_WORKER);
        ant.setModProfile(AntModProfile.of(
                GameConstants.MOD_HEAD_TRAPJAW.getDigit(),
                AntMod.DIGIT_NONE,
                AntMod.DIGIT_NONE,
                AntMod.DIGIT_NONE));

        assertFalse(AntModService.isAntEligibleForRole(
                ant, GameConstants.ROLE_FORAGER, Set.of()));
        assertTrue(AntModService.isAntEligibleForRole(
                ant, GameConstants.ROLE_FORAGER, Set.of(GameConstants.MOD_HEAD_TRAPJAW.getId())));
    }

    @Test
    void potterForcesHoneypotAllowedEvenWhenDisallowedInColony() {
        Colony colony = new Colony(1, "Test", true);
        colony.setPeaceRoleModAllowed(
                GameConstants.ROLE_POTTER, GameConstants.MOD_ABDOMEN_HONEYPOT, false);

        assertTrue(colony.isPeaceRoleModAllowed(
                GameConstants.ROLE_POTTER, GameConstants.MOD_ABDOMEN_HONEYPOT));
        assertTrue(GameConstants.ROLE_POTTER.isModForcedAllowed(GameConstants.MOD_ABDOMEN_HONEYPOT));
        assertTrue(GameConstants.ROLE_POTTER.isModRequired(GameConstants.MOD_ABDOMEN_HONEYPOT));
    }

    @Test
    void artilleryRoleExposesRequiredForcedHooksForFutureSubtype() {
        assertFalse(GameConstants.ROLE_ARTILLERY.requiresMods());
        assertTrue(GameConstants.ROLE_ARTILLERY.getRequiredMods().isEmpty());
        assertTrue(GameConstants.ROLE_ARTILLERY.getForcedAllowedMods().isEmpty());
    }

    @Test
    void defenderRequiresDoorheadSubtype() {
        Colony colony = new Colony(14, "Elig", true);
        Ant ant = new Ant(colony, GameConstants.CLASS_SOLDIER);
        ant.setModProfile(AntModProfile.standard());

        assertFalse(AntModService.isAntEligibleForRole(
                ant, GameConstants.ROLE_DEFENDER, Set.of(GameConstants.MOD_HEAD_DOORHEAD.getId())));

        ant.setModProfile(AntModProfile.of(
                GameConstants.MOD_HEAD_DOORHEAD.getDigit(),
                AntMod.DIGIT_NONE,
                AntMod.DIGIT_NONE,
                AntMod.DIGIT_NONE));
        assertTrue(AntModService.isAntEligibleForRole(
                ant, GameConstants.ROLE_DEFENDER, Set.of(GameConstants.MOD_HEAD_DOORHEAD.getId())));
        assertTrue(GameConstants.ROLE_DEFENDER.isHexDefenseOnly());
    }

    @Test
    void copyPeaceRolesToWarCopiesSubtypeAllows() {
        Colony colony = new Colony(2, "Test", true);
        colony.setPeaceRoleModAllowed(
                GameConstants.ROLE_FORAGER, GameConstants.MOD_HEAD_TRAPJAW, false);

        colony.copyPeaceRolesToWar();

        assertFalse(colony.isWarRoleModAllowed(
                GameConstants.ROLE_FORAGER, GameConstants.MOD_HEAD_TRAPJAW));
        assertTrue(colony.isWarRoleModAllowed(
                GameConstants.ROLE_FORAGER, GameConstants.MOD_ABDOMEN_HONEYPOT));
    }

    @Test
    void saveAndLoadPreservesDisallowedSubtypes() {
        Colony source = new Colony(3, "Source", true);
        source.setPeaceRoleModAllowed(
                GameConstants.ROLE_FORAGER, GameConstants.MOD_HEAD_TRAPJAW, false);
        source.setWarRoleModAllowed(
                GameConstants.ROLE_WARRIOR, GameConstants.MOD_ABDOMEN_STINGER, false);

        Savefile.SavedColony saved = new Savefile.SavedColony();
        saved.id = 3;
        saved.name = "Source";
        saved.roleDisallowedModsFlat = source.flattenPeaceRoleDisallowedMods();
        saved.warRoleDisallowedModsFlat = source.flattenWarRoleDisallowedMods();

        Colony loaded = new Colony(saved);
        assertFalse(loaded.isPeaceRoleModAllowed(
                GameConstants.ROLE_FORAGER, GameConstants.MOD_HEAD_TRAPJAW));
        assertFalse(loaded.isWarRoleModAllowed(
                GameConstants.ROLE_WARRIOR, GameConstants.MOD_ABDOMEN_STINGER));
        assertTrue(loaded.isPeaceRoleModAllowed(
                GameConstants.ROLE_FORAGER, GameConstants.MOD_ABDOMEN_HONEYPOT));
    }

    @Test
    void countsStandardAndSpecialAnts() {
        Colony colony = new Colony(4, "Counts", true);
        Ant plain = new Ant(colony, GameConstants.CLASS_WORKER);
        plain.setModProfile(AntModProfile.standard());
        Ant trapjaw = new Ant(colony, GameConstants.CLASS_WORKER);
        trapjaw.setModProfile(AntModProfile.of(
                GameConstants.MOD_HEAD_TRAPJAW.getDigit(),
                AntMod.DIGIT_NONE,
                AntMod.DIGIT_NONE,
                AntMod.DIGIT_NONE));
        colony.getWorkers().add(plain);
        colony.getWorkers().add(trapjaw);

        assertEquals(1, AntModService.countStandardAntsOfClass(colony, GameConstants.CLASS_WORKER));
        assertEquals(1, AntModService.countAntsWithMod(
                colony, GameConstants.CLASS_WORKER, GameConstants.MOD_HEAD_TRAPJAW));
    }
}
