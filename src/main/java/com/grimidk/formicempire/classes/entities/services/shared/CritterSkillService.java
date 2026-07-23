package com.grimidk.formicempire.classes.entities.services.shared;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.grimidk.formicempire.classes.constants.critter.Skill;
import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtype;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeSlot;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.critter.Critter;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

public final class CritterSkillService {

    private CritterSkillService() {
    }

    public static List<Skill> resolveAvailableSkills(Critter critter) {
        return resolveAvailableSkills(critter, null);
    }

    public static List<Skill> resolveAvailableSkills(Critter critter, Colony colony) {
        if (critter == null) {
            return List.of();
        }
        AntRole role = critter instanceof Ant ant ? ant.getRole() : null;
        AntSubtypeProfile profile = critter instanceof Ant ant ? ant.getSubtypeProfile() : null;
        return resolveAvailableSkills(critter.getSpecies(), profile, colony, role);
    }

    public static List<Skill> resolveAvailableSkills(Species species, AntSubtypeProfile profile) {
        return resolveAvailableSkills(species, profile, null, null);
    }

    public static List<Skill> resolveAvailableSkills(Species species, AntSubtypeProfile profile, Colony colony) {
        return resolveAvailableSkills(species, profile, colony, null);
    }

    public static List<Skill> resolveAvailableSkills(Species species, AntSubtypeProfile profile, Colony colony,
            AntRole role) {
        Set<Skill> skills = new LinkedHashSet<>();
        if (species != null) {
            skills.addAll(species.getBaseSkills());
        }
        applySubtypeSkills(skills, profile, role);
        applyConditionalSkills(skills, colony, role);
        return List.copyOf(skills);
    }

    private static void applySubtypeSkills(Set<Skill> skills, AntSubtypeProfile profile, AntRole role) {
        if (profile == null || skills == null) {
            return;
        }
        for (AntSubtypeSlot slot : AntSubtypeSlot.values()) {
            AntSubtype subtype = profile.getSubtype(slot);
            if (subtype == null || subtype.isNone()) {
                continue;
            }
            Skill replaces = subtype.getReplacesSkill();
            if (replaces != null) {
                skills.remove(replaces);
            }
            Skill granted = subtype.getGrantedSkill();
            if (granted != null && roleAllowsSkill(granted, role)) {
                skills.add(granted);
            }
        }
    }

    private static void applyConditionalSkills(Set<Skill> skills, Colony colony, AntRole role) {
        if (skills == null) {
            return;
        }
        for (Skill skill : GameConstants.getSkills()) {
            if (isSubtypeGrantedSkill(skill)) {
                continue;
            }
            Upgrade requiredUpgrade = skill.getRequiredUpgrade();
            AntRole requiredRole = skill.getRequiredRole();
            if (requiredUpgrade == null && requiredRole == null) {
                continue;
            }
            if (requiredUpgrade != null && (colony == null || !colony.hasUpgrade(requiredUpgrade))) {
                continue;
            }
            if (!roleAllowsSkill(skill, role)) {
                continue;
            }
            skills.add(skill);
        }
    }

    private static boolean roleAllowsSkill(Skill skill, AntRole role) {
        AntRole requiredRole = skill.getRequiredRole();
        return requiredRole == null || requiredRole == role;
    }

    private static boolean isSubtypeGrantedSkill(Skill skill) {
        if (skill == null) {
            return false;
        }
        for (AntSubtype subtype : GameConstants.getAntSubtypes()) {
            if (subtype.getGrantedSkill() == skill) {
                return true;
            }
        }
        return false;
    }
}
