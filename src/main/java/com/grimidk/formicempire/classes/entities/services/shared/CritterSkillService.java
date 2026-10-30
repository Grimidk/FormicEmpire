package com.grimidk.formicempire.classes.entities.services.shared;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.grimidk.formicempire.classes.constants.critter.Skill;
import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntMod;
import com.grimidk.formicempire.classes.constants.critter.ant.AntModProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntModSlot;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.critter.Critter;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.colony.AntModService;
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
        AntModProfile profile = critter instanceof Ant ant ? ant.getModProfile() : null;
        return resolveAvailableSkills(critter.getSpecies(), profile, colony, role);
    }

    public static List<Skill> resolveAvailableSkills(Species species, AntModProfile profile) {
        return resolveAvailableSkills(species, profile, null, null);
    }

    public static List<Skill> resolveAvailableSkills(Species species, AntModProfile profile, Colony colony) {
        return resolveAvailableSkills(species, profile, colony, null);
    }

    public static List<Skill> resolveAvailableSkills(Species species, AntModProfile profile, Colony colony,
            AntRole role) {
        Set<Skill> skills = new LinkedHashSet<>();
        Dynasty dynasty = colony != null ? colony.getDynasty() : null;
        if (dynasty != null) {
            for (Skill skill : dynasty.getUnlockedSkills()) {
                if (allowsSkill(skill, role, profile)) {
                    skills.add(skill);
                }
            }
        } else {
            if (species != null) {
                for (Skill skill : species.getBaseSkills()) {
                    if (allowsSkill(skill, role, profile)) {
                        skills.add(skill);
                    }
                }
            }
            applyModGrantedSkills(skills, profile, role);
        }
        applyModReplaces(skills, profile);
        applySkillReplaces(skills);
        return List.copyOf(skills);
    }

    public static float resolveAccuracyMult(Skill skill, AntModProfile profile) {
        if (skill == null) {
            return 0f;
        }
        float accuracy = skill.getAccuracyMult() + AntModService.combinedAccuracyBonus(profile);
        return Math.min(1f, Math.max(0f, accuracy));
    }

    public static float resolveModAttackMult(Skill skill, AntModProfile profile) {
        if (skill == null || skill.getBattleLine() != GameConstants.BATTLE_LINE_INFANTRY) {
            return 1f;
        }
        return AntModService.combinedAttackMult(profile);
    }

    public static float resolveDamageMult(Skill skill, AntModProfile profile) {
        if (skill == null) {
            return 0f;
        }
        return skill.getDamageMult() * resolveModAttackMult(skill, profile);
    }

    private static void applyModGrantedSkills(Set<Skill> skills, AntModProfile profile, AntRole role) {
        if (profile == null || skills == null) {
            return;
        }
        for (AntModSlot slot : AntModSlot.values()) {
            AntMod subtype = profile.getMod(slot);
            if (subtype == null || subtype.isNone()) {
                continue;
            }
            Skill granted = subtype.getGrantedSkill();
            if (granted != null && allowsSkill(granted, role, profile)) {
                skills.add(granted);
            }
        }
    }

    private static void applyModReplaces(Set<Skill> skills, AntModProfile profile) {
        if (profile == null || skills == null) {
            return;
        }
        for (AntModSlot slot : AntModSlot.values()) {
            AntMod subtype = profile.getMod(slot);
            if (subtype == null || subtype.isNone()) {
                continue;
            }
            Skill replaces = subtype.getReplacesSkill();
            if (replaces != null) {
                skills.remove(replaces);
            }
        }
    }

    private static void applySkillReplaces(Set<Skill> skills) {
        if (skills == null || skills.isEmpty()) {
            return;
        }
        Set<Skill> toRemove = new LinkedHashSet<>();
        for (Skill skill : skills) {
            Skill replaces = skill.getReplacesSkill();
            if (replaces != null && skills.contains(skill)) {
                toRemove.add(replaces);
            }
        }
        skills.removeAll(toRemove);
    }

    private static boolean allowsSkill(Skill skill, AntRole role, AntModProfile profile) {
        if (skill == null) {
            return false;
        }
        AntRole requiredRole = skill.getRequiredRole();
        if (requiredRole != null && requiredRole != role) {
            return false;
        }
        AntMod requiredSubtype = skill.getRequiredMod();
        if (requiredSubtype != null && !profileHasMod(profile, requiredSubtype)) {
            return false;
        }
        return battleLineAllowsSkill(skill, role);
    }

    private static boolean profileHasMod(AntModProfile profile, AntMod required) {
        if (profile == null || required == null || required.getSlot() == null) {
            return false;
        }
        return profile.getMod(required.getSlot()) == required;
    }

    private static boolean battleLineAllowsSkill(Skill skill, AntRole role) {
        if (skill.getBattleLine() == null || role == null) {
            return true;
        }
        return skill.getBattleLine() == GameConstants.getBattleLineForRole(role);
    }
}
