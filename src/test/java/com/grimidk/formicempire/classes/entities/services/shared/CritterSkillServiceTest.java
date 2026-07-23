package com.grimidk.formicempire.classes.entities.services.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void trapjawReplacesBasicBiteWithPowerfulBite() {
        List<Skill> skills = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.of(2, 1, 1, 1));
        assertEquals(1, skills.size());
        assertEquals(GameConstants.SKILL_POWERFUL_BITE, skills.get(0));
        assertFalse(skills.contains(GameConstants.SKILL_BASIC_BITE));
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
    void acidSpittingRequiresAssimilationUpgrade() {
        List<Skill> without = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.standard(), null);
        assertFalse(without.contains(GameConstants.SKILL_ACID_SPITTING));

        Dynasty dynasty = new Dynasty(1, "Green", true, GameConstants.SPECIES_GREEN);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_ACIDSPIT);
        Colony colony = new Colony(1, "Nest", true);
        colony.setDynasty(dynasty);
        List<Skill> with = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.standard(), colony);
        assertTrue(with.contains(GameConstants.SKILL_BASIC_BITE));
        assertTrue(with.contains(GameConstants.SKILL_ACID_SPITTING));
    }

    @Test
    void skillsExposeAccuracyAndDamageMultipliers() {
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
        assertEquals(GameUnlocks.ASSIMILATED_ACIDSPIT, GameConstants.SKILL_ACID_SPITTING.getRequiredUpgrade());
        assertEquals(GameConstants.ROLE_ARTILLERY, GameConstants.SKILL_ACID_ARTILLERY.getRequiredRole());
        assertEquals(GameConstants.ROLE_DEFENDER, GameConstants.SKILL_SHIELDING.getRequiredRole());
        assertEquals(GameConstants.ROLE_POTTER, GameConstants.SKILL_BOOST_REGEN.getRequiredRole());
    }

    @Test
    void acidArtilleryRequiresArtilleryRole() {
        List<Skill> withoutRole = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.standard(), null, null);
        assertFalse(withoutRole.contains(GameConstants.SKILL_ACID_ARTILLERY));

        List<Skill> withRole = CritterSkillService.resolveAvailableSkills(
                GameConstants.TYPE_ANT, AntSubtypeProfile.standard(), null, GameConstants.ROLE_ARTILLERY);
        assertTrue(withRole.contains(GameConstants.SKILL_ACID_ARTILLERY));
        assertTrue(withRole.contains(GameConstants.SKILL_BASIC_BITE));
    }
}
