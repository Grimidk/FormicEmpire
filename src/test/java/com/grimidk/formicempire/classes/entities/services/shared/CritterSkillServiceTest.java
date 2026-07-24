package com.grimidk.formicempire.classes.entities.services.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.critter.Skill;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class CritterSkillServiceTest {

    @Test
    void antSpeciesBaseSkillIsBasicBite() {
        List<Skill> skills = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.standard());
        assertEquals(1, skills.size());
        assertEquals(GameConstants.SKILL_BASIC_BITE, skills.get(0));
    }

    @Test
    void dynastyStartsWithBasicBite() {
        Dynasty dynasty = new Dynasty(1, "Omni", true, GameConstants.SPECIES_OMNI);
        assertTrue(dynasty.hasSkill(GameConstants.SKILL_BASIC_BITE));
        assertEquals(1, dynasty.getUnlockedSkills().size());
    }

    @Test
    void trapjawReplacesBasicBiteWithPowerfulBiteOnAnt() {
        List<Skill> skills = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.of(2, 1, 1, 1));
        assertEquals(1, skills.size());
        assertEquals(GameConstants.SKILL_POWERFUL_BITE, skills.get(0));
        assertFalse(skills.contains(GameConstants.SKILL_BASIC_BITE));
    }

    @Test
    void trapjawAssimilationKeepsBasicBiteOnDynastyAndRequiresSubtype() {
        Dynasty dynasty = new Dynasty(1, "Trapjaw", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_TRAPJAW);
        assertTrue(dynasty.hasSkill(GameConstants.SKILL_POWERFUL_BITE));
        assertTrue(dynasty.hasSkill(GameConstants.SKILL_BASIC_BITE));

        Colony colony = new Colony(1, "Nest", true);
        colony.setDynasty(dynasty);

        List<Skill> withoutTrapjaw = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.standard(), colony, GameConstants.ROLE_WARRIOR);
        assertTrue(withoutTrapjaw.contains(GameConstants.SKILL_BASIC_BITE));
        assertFalse(withoutTrapjaw.contains(GameConstants.SKILL_POWERFUL_BITE));

        List<Skill> withTrapjaw = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.of(2, 1, 1, 1), colony, GameConstants.ROLE_WARRIOR);
        assertTrue(withTrapjaw.contains(GameConstants.SKILL_POWERFUL_BITE));
        assertFalse(withTrapjaw.contains(GameConstants.SKILL_BASIC_BITE));
    }

    @Test
    void stingerDoorheadHoneypotAddSkills() {
        List<Skill> skills = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.of(3, 1, 2, 1), null, GameConstants.ROLE_DEFENDER);
        assertTrue(skills.contains(GameConstants.SKILL_BASIC_BITE));
        assertTrue(skills.contains(GameConstants.SKILL_SHIELDING));
        assertTrue(skills.contains(GameConstants.SKILL_STINGING));
        assertEquals(3, skills.size());

        skills = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.of(1, 1, 3, 1), null, GameConstants.ROLE_POTTER);
        assertTrue(skills.contains(GameConstants.SKILL_BASIC_BITE));
        assertTrue(skills.contains(GameConstants.SKILL_BOOST_REGEN));
        assertEquals(2, skills.size());
    }

    @Test
    void shieldingAndBoostRegenRequireMatchingRoles() {
        List<Skill> doorheadWithoutRole = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.of(3, 1, 1, 1));
        assertFalse(doorheadWithoutRole.contains(GameConstants.SKILL_SHIELDING));

        List<Skill> honeypotWithoutRole = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.of(1, 1, 3, 1));
        assertFalse(honeypotWithoutRole.contains(GameConstants.SKILL_BOOST_REGEN));
    }

    @Test
    void honeypotAssimilationRequiresSubtypeAndPotterRole() {
        Dynasty dynasty = new Dynasty(1, "Honeypot", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_HONEYPOT);
        assertTrue(dynasty.hasSkill(GameConstants.SKILL_BOOST_REGEN));
        assertTrue(dynasty.hasSkill(GameConstants.SKILL_BASIC_BITE));

        Colony colony = new Colony(1, "Nest", true);
        colony.setDynasty(dynasty);

        List<Skill> potterWithoutSubtype = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.standard(), colony, GameConstants.ROLE_POTTER);
        assertFalse(potterWithoutSubtype.contains(GameConstants.SKILL_BOOST_REGEN));

        List<Skill> withHoneypot = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.of(1, 1, 3, 1), colony, GameConstants.ROLE_POTTER);
        assertTrue(withHoneypot.contains(GameConstants.SKILL_BOOST_REGEN));

        List<Skill> warriorWithHoneypot = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.of(1, 1, 3, 1), colony, GameConstants.ROLE_WARRIOR);
        assertFalse(warriorWithHoneypot.contains(GameConstants.SKILL_BOOST_REGEN));
    }

    @Test
    void trapjawWithStingerHasPowerfulBiteAndStinging() {
        List<Skill> skills = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.of(2, 1, 2, 1));
        assertTrue(skills.contains(GameConstants.SKILL_POWERFUL_BITE));
        assertTrue(skills.contains(GameConstants.SKILL_STINGING));
        assertFalse(skills.contains(GameConstants.SKILL_BASIC_BITE));
        assertEquals(2, skills.size());
    }

    @Test
    void nonAntSpeciesHaveNoBaseSkills() {
        List<Skill> skills = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_APHID, null);
        assertTrue(skills.isEmpty());
    }

    @Test
    void acidSpittingRequiresAssimilationOnDynasty() {
        List<Skill> without = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.standard(), null);
        assertFalse(without.contains(GameConstants.SKILL_ACID_SPITTING));

        Dynasty dynasty = new Dynasty(1, "Green", true, GameConstants.SPECIES_GREEN);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_ACIDSPIT);
        assertTrue(dynasty.hasSkill(GameConstants.SKILL_ACID_SPITTING));
        Colony colony = new Colony(1, "Nest", true);
        colony.setDynasty(dynasty);
        List<Skill> with = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.standard(), colony, GameConstants.ROLE_WARRIOR);
        assertTrue(with.contains(GameConstants.SKILL_BASIC_BITE));
        assertTrue(with.contains(GameConstants.SKILL_ACID_SPITTING));
    }

    @Test
    void skillsExposeAccuracyDamageRoleSubtypeAndBattleLine() {
        assertEquals(1f, GameConstants.SKILL_BASIC_BITE.getAccuracyMult(), 0.0001f);
        assertEquals(1f, GameConstants.SKILL_BASIC_BITE.getDamageMult(), 0.0001f);
        assertEquals(1f, GameConstants.SKILL_POWERFUL_BITE.getAccuracyMult(), 0.0001f);
        assertEquals(2f, GameConstants.SKILL_POWERFUL_BITE.getDamageMult(), 0.0001f);
        assertEquals(0.8f, GameConstants.SKILL_STINGING.getAccuracyMult(), 0.0001f);
        assertEquals(2.5f, GameConstants.SKILL_STINGING.getDamageMult(), 0.0001f);
        assertEquals(0.75f, GameConstants.SKILL_ACID_SPITTING.getAccuracyMult(), 0.0001f);
        assertEquals(2f, GameConstants.SKILL_ACID_SPITTING.getDamageMult(), 0.0001f);
        assertEquals(1f, GameConstants.SKILL_SHIELDING.getAccuracyMult(), 0.0001f);
        assertEquals(0f, GameConstants.SKILL_SHIELDING.getDamageMult(), 0.0001f);
        assertEquals(1f, GameConstants.SKILL_BOOST_REGEN.getAccuracyMult(), 0.0001f);
        assertEquals(0f, GameConstants.SKILL_BOOST_REGEN.getDamageMult(), 0.0001f);
        assertEquals(0.5f, GameConstants.SKILL_ACID_ARTILLERY.getAccuracyMult(), 0.0001f);
        assertEquals(5f, GameConstants.SKILL_ACID_ARTILLERY.getDamageMult(), 0.0001f);
        assertEquals(0.9f, GameConstants.SKILL_SELFDESTRUCT.getAccuracyMult(), 0.0001f);
        assertEquals(5f, GameConstants.SKILL_SELFDESTRUCT.getDamageMult(), 0.0001f);
        assertEquals(5, GameConstants.SKILL_SELFDESTRUCT.getTargetCount());
        assertTrue(GameConstants.SKILL_SELFDESTRUCT.sacrificesSelf());
        assertEquals(0.9f, GameConstants.SKILL_ACIDIC_SELFDESTRUCT.getAccuracyMult(), 0.0001f);
        assertEquals(10f, GameConstants.SKILL_ACIDIC_SELFDESTRUCT.getDamageMult(), 0.0001f);
        assertEquals(7, GameConstants.SKILL_ACIDIC_SELFDESTRUCT.getTargetCount());
        assertTrue(GameConstants.SKILL_ACIDIC_SELFDESTRUCT.sacrificesSelf());
        assertEquals(GameConstants.SKILL_SELFDESTRUCT, GameConstants.SKILL_ACIDIC_SELFDESTRUCT.getReplacesSkill());
        assertNull(GameConstants.SKILL_ACID_SPITTING.getRequiredRole());
        assertNull(GameConstants.SKILL_ACID_SPITTING.getRequiredSubtype());
        assertNull(GameConstants.SKILL_BASIC_BITE.getRequiredSubtype());
        assertEquals(GameConstants.ROLE_ARTILLERY, GameConstants.SKILL_ACID_ARTILLERY.getRequiredRole());
        assertEquals(GameConstants.ROLE_BOMBER, GameConstants.SKILL_SELFDESTRUCT.getRequiredRole());
        assertEquals(GameConstants.ROLE_BOMBER, GameConstants.SKILL_ACIDIC_SELFDESTRUCT.getRequiredRole());
        assertEquals(GameConstants.ROLE_DEFENDER, GameConstants.SKILL_SHIELDING.getRequiredRole());
        assertEquals(GameConstants.ROLE_POTTER, GameConstants.SKILL_BOOST_REGEN.getRequiredRole());
        assertEquals(GameConstants.SUBTYPE_HEAD_TRAPJAW, GameConstants.SKILL_POWERFUL_BITE.getRequiredSubtype());
        assertEquals(GameConstants.SUBTYPE_ABDOMEN_STINGER, GameConstants.SKILL_STINGING.getRequiredSubtype());
        assertEquals(GameConstants.SUBTYPE_HEAD_DOORHEAD, GameConstants.SKILL_SHIELDING.getRequiredSubtype());
        assertEquals(GameConstants.SUBTYPE_ABDOMEN_HONEYPOT, GameConstants.SKILL_BOOST_REGEN.getRequiredSubtype());
        assertEquals(GameConstants.BATTLE_LINE_INFANTRY, GameConstants.SKILL_BASIC_BITE.getBattleLine());
        assertEquals(GameConstants.BATTLE_LINE_INFANTRY, GameConstants.SKILL_POWERFUL_BITE.getBattleLine());
        assertEquals(GameConstants.BATTLE_LINE_INFANTRY, GameConstants.SKILL_STINGING.getBattleLine());
        assertEquals(GameConstants.BATTLE_LINE_INFANTRY, GameConstants.SKILL_SHIELDING.getBattleLine());
        assertEquals(GameConstants.BATTLE_LINE_INFANTRY, GameConstants.SKILL_BOOST_REGEN.getBattleLine());
        assertEquals(GameConstants.BATTLE_LINE_INFANTRY, GameConstants.SKILL_ACID_SPITTING.getBattleLine());
        assertEquals(GameConstants.BATTLE_LINE_ARTILLERY, GameConstants.SKILL_ACID_ARTILLERY.getBattleLine());
        assertEquals(GameConstants.BATTLE_LINE_INFANTRY, GameConstants.SKILL_SELFDESTRUCT.getBattleLine());
        assertEquals(GameConstants.BATTLE_LINE_INFANTRY, GameConstants.SKILL_ACIDIC_SELFDESTRUCT.getBattleLine());
        for (Skill skill : GameConstants.getSkills()) {
            assertTrue(skill.getBattleLine() != null, skill.getNameKey() + " missing battle line");
        }
    }

    @Test
    void selfdestructRequiresBomberRoleAndAssimilation() {
        Dynasty dynasty = new Dynasty(1, "Exploding", true, GameConstants.SPECIES_OMNI);
        Colony colony = new Colony(1, "Nest", true);
        colony.setDynasty(dynasty);

        List<Skill> without = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.standard(), colony, GameConstants.ROLE_BOMBER);
        assertFalse(without.contains(GameConstants.SKILL_SELFDESTRUCT));

        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_SELFDESTRUCT);
        assertTrue(dynasty.hasUpgrade(GameUnlocks.ROLE_BOMBER));
        assertTrue(dynasty.hasSkill(GameConstants.SKILL_SELFDESTRUCT));
        assertTrue(GameConstants.isObtainableRole(GameConstants.ROLE_BOMBER));

        List<Skill> withoutRole = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.standard(), colony, GameConstants.ROLE_WARRIOR);
        assertFalse(withoutRole.contains(GameConstants.SKILL_SELFDESTRUCT));

        List<Skill> withBomber = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.standard(), colony, GameConstants.ROLE_BOMBER);
        assertTrue(withBomber.contains(GameConstants.SKILL_SELFDESTRUCT));
        assertTrue(withBomber.contains(GameConstants.SKILL_BASIC_BITE));
    }

    @Test
    void corrosiveBombsReplacesSelfdestructWithAcidicSelfdestruct() {
        Dynasty dynasty = new Dynasty(1, "Corrosive", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_SELFDESTRUCT);
        dynasty.unlockUpgrade(GameUnlocks.SYNERGY_CORROSIVE_BOMBS);
        assertTrue(dynasty.hasSkill(GameConstants.SKILL_ACIDIC_SELFDESTRUCT));

        Colony colony = new Colony(1, "Nest", true);
        colony.setDynasty(dynasty);
        List<Skill> skills = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.standard(), colony, GameConstants.ROLE_BOMBER);
        assertTrue(skills.contains(GameConstants.SKILL_ACIDIC_SELFDESTRUCT));
        assertFalse(skills.contains(GameConstants.SKILL_SELFDESTRUCT));
    }

    @Test
    void farsightBoostsAccuracyOnAllSkillsCappedAtOne() {
        AntSubtypeProfile farsight = AntSubtypeProfile.of(4, 1, 1, 1);
        assertEquals(0.15f, GameConstants.SUBTYPE_HEAD_FARSIGHT.getAccuracyBonus(), 0.0001f);
        assertEquals(1f, CritterSkillService.resolveAccuracyMult(GameConstants.SKILL_BASIC_BITE, farsight), 0.0001f);
        assertEquals(1f, CritterSkillService.resolveAccuracyMult(GameConstants.SKILL_SELFDESTRUCT, farsight), 0.0001f);
        assertEquals(0.95f, CritterSkillService.resolveAccuracyMult(GameConstants.SKILL_STINGING, farsight), 0.0001f);
        assertEquals(0.65f, CritterSkillService.resolveAccuracyMult(GameConstants.SKILL_ACID_ARTILLERY, farsight), 0.0001f);
    }

    @Test
    void subtypeAttackBoostAppliesOnlyToInfantrySkills() {
        AntSubtypeProfile trapjaw = AntSubtypeProfile.of(2, 1, 1, 1);
        assertEquals(1.5f, CritterSkillService.resolveSubtypeAttackMult(GameConstants.SKILL_BASIC_BITE, trapjaw), 0.0001f);
        assertEquals(3f, CritterSkillService.resolveDamageMult(GameConstants.SKILL_POWERFUL_BITE, trapjaw), 0.0001f);
        assertEquals(1f, CritterSkillService.resolveSubtypeAttackMult(GameConstants.SKILL_ACID_ARTILLERY, trapjaw), 0.0001f);
        assertEquals(5f, CritterSkillService.resolveDamageMult(GameConstants.SKILL_ACID_ARTILLERY, trapjaw), 0.0001f);
    }

    @Test
    void acidArtilleryRequiresSynergyUnlockAndArtilleryRole() {
        Dynasty dynasty = new Dynasty(1, "Green", true, GameConstants.SPECIES_OMNI);
        Colony colony = new Colony(1, "Nest", true);
        colony.setDynasty(dynasty);

        List<Skill> withoutUnlock = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.standard(), colony, GameConstants.ROLE_ARTILLERY);
        assertFalse(withoutUnlock.contains(GameConstants.SKILL_ACID_ARTILLERY));

        dynasty.unlockUpgrade(GameUnlocks.ROLE_ARTILLERY);
        assertTrue(dynasty.hasSkill(GameConstants.SKILL_ACID_ARTILLERY));

        List<Skill> withoutRole = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.standard(), colony, null);
        assertFalse(withoutRole.contains(GameConstants.SKILL_ACID_ARTILLERY));

        List<Skill> withRole = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.standard(), colony, GameConstants.ROLE_ARTILLERY);
        assertTrue(withRole.contains(GameConstants.SKILL_ACID_ARTILLERY));
        assertFalse(withRole.contains(GameConstants.SKILL_BASIC_BITE));
    }

    @Test
    void infantryRoleKeepsInfantrySkillsAndExcludesArtillery() {
        List<Skill> skills = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.standard(), null, GameConstants.ROLE_WARRIOR);
        assertTrue(skills.contains(GameConstants.SKILL_BASIC_BITE));
        assertFalse(skills.contains(GameConstants.SKILL_ACID_ARTILLERY));
    }

    @Test
    void nonBattleLineRoleExcludesBattleLineSkills() {
        List<Skill> skills = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.standard(), null, GameConstants.ROLE_FORAGER);
        assertFalse(skills.contains(GameConstants.SKILL_BASIC_BITE));
        assertTrue(skills.isEmpty());
    }
}
