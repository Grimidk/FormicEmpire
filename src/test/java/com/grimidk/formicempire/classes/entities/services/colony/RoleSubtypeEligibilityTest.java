package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtype;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class RoleSubtypeEligibilityTest {

    @Test
    void standardAntsEligibleWhenNoSpecialsAllowedAndRoleHasNoRequirements() {
        Colony colony = new Colony(10, "Elig", true);
        Ant ant = new Ant(colony, GameConstants.TYPE_WORKER);
        ant.setSubtypeProfile(AntSubtypeProfile.standard());

        assertTrue(AntSubtypeService.isAntEligibleForRole(
                ant, GameConstants.ROLE_FORAGER, Set.of()));
    }

    @Test
    void standardAntsBlockedWhenRoleRequiresSubtype() {
        Colony colony = new Colony(11, "Elig", true);
        Ant ant = new Ant(colony, GameConstants.TYPE_WORKER);
        ant.setSubtypeProfile(AntSubtypeProfile.standard());

        assertFalse(AntSubtypeService.isAntEligibleForRole(
                ant, GameConstants.ROLE_POTTER, Set.of(GameConstants.SUBTYPE_ABDOMEN_HONEYPOT.getId())));
    }

    @Test
    void honeypotAntEligibleForPotter() {
        Colony colony = new Colony(12, "Elig", true);
        Ant ant = new Ant(colony, GameConstants.TYPE_WORKER);
        ant.setSubtypeProfile(AntSubtypeProfile.of(
                AntSubtype.DIGIT_NONE,
                AntSubtype.DIGIT_NONE,
                GameConstants.SUBTYPE_ABDOMEN_HONEYPOT.getDigit(),
                AntSubtype.DIGIT_NONE));

        assertTrue(AntSubtypeService.isAntEligibleForRole(
                ant, GameConstants.ROLE_POTTER, Set.of(GameConstants.SUBTYPE_ABDOMEN_HONEYPOT.getId())));
    }

    @Test
    void disallowedSpecialSubtypeBlocksAssignment() {
        Colony colony = new Colony(13, "Elig", true);
        Ant ant = new Ant(colony, GameConstants.TYPE_WORKER);
        ant.setSubtypeProfile(AntSubtypeProfile.of(
                GameConstants.SUBTYPE_HEAD_TRAPJAW.getDigit(),
                AntSubtype.DIGIT_NONE,
                AntSubtype.DIGIT_NONE,
                AntSubtype.DIGIT_NONE));

        assertFalse(AntSubtypeService.isAntEligibleForRole(
                ant, GameConstants.ROLE_FORAGER, Set.of()));
        assertTrue(AntSubtypeService.isAntEligibleForRole(
                ant, GameConstants.ROLE_FORAGER, Set.of(GameConstants.SUBTYPE_HEAD_TRAPJAW.getId())));
    }

    @Test
    void potterForcesHoneypotAllowedEvenWhenDisallowedInColony() {
        Colony colony = new Colony(1, "Test", true);
        colony.setPeaceRoleSubtypeAllowed(
                GameConstants.ROLE_POTTER, GameConstants.SUBTYPE_ABDOMEN_HONEYPOT, false);

        assertTrue(colony.isPeaceRoleSubtypeAllowed(
                GameConstants.ROLE_POTTER, GameConstants.SUBTYPE_ABDOMEN_HONEYPOT));
        assertTrue(GameConstants.ROLE_POTTER.isSubtypeForcedAllowed(GameConstants.SUBTYPE_ABDOMEN_HONEYPOT));
        assertTrue(GameConstants.ROLE_POTTER.isSubtypeRequired(GameConstants.SUBTYPE_ABDOMEN_HONEYPOT));
    }

    @Test
    void artilleryRoleExposesRequiredForcedHooksForFutureSubtype() {
        assertFalse(GameConstants.ROLE_ARTILLERY.requiresSubtypes());
        assertTrue(GameConstants.ROLE_ARTILLERY.getRequiredSubtypes().isEmpty());
        assertTrue(GameConstants.ROLE_ARTILLERY.getForcedAllowedSubtypes().isEmpty());
    }

    @Test
    void defenderRequiresDoorheadSubtype() {
        Colony colony = new Colony(14, "Elig", true);
        Ant ant = new Ant(colony, GameConstants.TYPE_SOLDIER);
        ant.setSubtypeProfile(AntSubtypeProfile.standard());

        assertFalse(AntSubtypeService.isAntEligibleForRole(
                ant, GameConstants.ROLE_DEFENDER, Set.of(GameConstants.SUBTYPE_HEAD_DOORHEAD.getId())));

        ant.setSubtypeProfile(AntSubtypeProfile.of(
                GameConstants.SUBTYPE_HEAD_DOORHEAD.getDigit(),
                AntSubtype.DIGIT_NONE,
                AntSubtype.DIGIT_NONE,
                AntSubtype.DIGIT_NONE));
        assertTrue(AntSubtypeService.isAntEligibleForRole(
                ant, GameConstants.ROLE_DEFENDER, Set.of(GameConstants.SUBTYPE_HEAD_DOORHEAD.getId())));
        assertTrue(GameConstants.ROLE_DEFENDER.isHexDefenseOnly());
    }

    @Test
    void copyPeaceRolesToWarCopiesSubtypeAllows() {
        Colony colony = new Colony(2, "Test", true);
        colony.setPeaceRoleSubtypeAllowed(
                GameConstants.ROLE_FORAGER, GameConstants.SUBTYPE_HEAD_TRAPJAW, false);

        colony.copyPeaceRolesToWar();

        assertFalse(colony.isWarRoleSubtypeAllowed(
                GameConstants.ROLE_FORAGER, GameConstants.SUBTYPE_HEAD_TRAPJAW));
        assertTrue(colony.isWarRoleSubtypeAllowed(
                GameConstants.ROLE_FORAGER, GameConstants.SUBTYPE_ABDOMEN_HONEYPOT));
    }

    @Test
    void saveAndLoadPreservesDisallowedSubtypes() {
        Colony source = new Colony(3, "Source", true);
        source.setPeaceRoleSubtypeAllowed(
                GameConstants.ROLE_FORAGER, GameConstants.SUBTYPE_HEAD_TRAPJAW, false);
        source.setWarRoleSubtypeAllowed(
                GameConstants.ROLE_WARRIOR, GameConstants.SUBTYPE_ABDOMEN_STINGER, false);

        Savefile.SavedColony saved = new Savefile.SavedColony();
        saved.id = 3;
        saved.name = "Source";
        saved.roleDisallowedSubtypesFlat = source.flattenPeaceRoleDisallowedSubtypes();
        saved.warRoleDisallowedSubtypesFlat = source.flattenWarRoleDisallowedSubtypes();

        Colony loaded = new Colony(saved);
        assertFalse(loaded.isPeaceRoleSubtypeAllowed(
                GameConstants.ROLE_FORAGER, GameConstants.SUBTYPE_HEAD_TRAPJAW));
        assertFalse(loaded.isWarRoleSubtypeAllowed(
                GameConstants.ROLE_WARRIOR, GameConstants.SUBTYPE_ABDOMEN_STINGER));
        assertTrue(loaded.isPeaceRoleSubtypeAllowed(
                GameConstants.ROLE_FORAGER, GameConstants.SUBTYPE_ABDOMEN_HONEYPOT));
    }

    @Test
    void countsStandardAndSpecialAnts() {
        Colony colony = new Colony(4, "Counts", true);
        Ant plain = new Ant(colony, GameConstants.TYPE_WORKER);
        plain.setSubtypeProfile(AntSubtypeProfile.standard());
        Ant trapjaw = new Ant(colony, GameConstants.TYPE_WORKER);
        trapjaw.setSubtypeProfile(AntSubtypeProfile.of(
                GameConstants.SUBTYPE_HEAD_TRAPJAW.getDigit(),
                AntSubtype.DIGIT_NONE,
                AntSubtype.DIGIT_NONE,
                AntSubtype.DIGIT_NONE));
        colony.getWorkers().add(plain);
        colony.getWorkers().add(trapjaw);

        assertEquals(1, AntSubtypeService.countStandardAntsOfType(colony, GameConstants.TYPE_WORKER));
        assertEquals(1, AntSubtypeService.countAntsWithSubtype(
                colony, GameConstants.TYPE_WORKER, GameConstants.SUBTYPE_HEAD_TRAPJAW));
    }
}
