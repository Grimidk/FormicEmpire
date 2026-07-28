package com.grimidk.formicempire.classes.entities.services.shared;

import java.util.ArrayList;
import java.util.List;

import com.grimidk.formicempire.classes.constants.critter.Skill;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

public final class WarCombatSkillService {

    private WarCombatSkillService() {
    }

    public static boolean useBoostRegen(Ant ant, Colony colony) {
        if (ant == null || !canUseSkill(ant, colony, GameConstants.SKILL_BOOST_REGEN)) {
            return false;
        }
        ant.setBoostRegenPending(true);
        return true;
    }

    public static boolean useShielding(Ant ant, Colony colony) {
        if (ant == null || !canUseSkill(ant, colony, GameConstants.SKILL_SHIELDING)) {
            return false;
        }
        ant.setShieldingActive(true);
        return true;
    }

    public static boolean canUseSkill(Ant ant, Colony colony, Skill skill) {
        if (ant == null || skill == null) {
            return false;
        }
        return CritterSkillService.resolveAvailableSkills(ant, colony).contains(skill);
    }

    public static void applyRedeployRegen(Dynasty dynasty) {
        if (dynasty == null) {
            return;
        }
        for (Colony colony : dynasty.getColonies()) {
            armBoostRegenForEligiblePotters(colony);
            healColonyOnRedeploy(colony);
        }
    }

    public static void armBoostRegenForEligiblePotters(Colony colony) {
        if (colony == null) {
            return;
        }
        for (Ant ant : allLiveAnts(colony)) {
            if (ant.getRole() == GameConstants.ROLE_POTTER
                    && canUseSkill(ant, colony, GameConstants.SKILL_BOOST_REGEN)) {
                ant.setBoostRegenPending(true);
            }
        }
    }

    public static void armShieldingForEligibleDefenders(Colony colony) {
        if (colony == null) {
            return;
        }
        for (Ant ant : allLiveAnts(colony)) {
            if (ant.getRole() == GameConstants.ROLE_DEFENDER
                    && canUseSkill(ant, colony, GameConstants.SKILL_SHIELDING)) {
                ant.setShieldingActive(true);
            }
        }
    }

    public static void healColonyOnRedeploy(Colony colony) {
        if (colony == null) {
            return;
        }
        for (Ant ant : allLiveAnts(colony)) {
            healAntOnRedeploy(ant);
        }
    }

    public static void healAntOnRedeploy(Ant ant) {
        if (ant == null || ant.getMaxHealth() <= 0) {
            return;
        }
        float regenPercent = ant.getRegen();
        if (ant.isBoostRegenPending()) {
            regenPercent *= GameNumbers.BOOST_REGEN_NEXT_REDEPLOY_MULT;
            ant.setBoostRegenPending(false);
        }
        float healed = GameNumbers.regenAmountFromPercent(ant.getMaxHealth(), regenPercent);
        if (healed <= 0f) {
            return;
        }
        int newHealth = Math.min(ant.getMaxHealth(), Math.round(ant.getHealth() + healed));
        ant.setHealth(newHealth);
    }

    public static int sacrificeShieldingDefenders(Colony colony, int maxToSacrifice) {
        if (colony == null || maxToSacrifice <= 0) {
            return 0;
        }
        List<Ant> shields = new ArrayList<>();
        for (Ant ant : allLiveAnts(colony)) {
            if (ant.isShieldingActive() && ant.getRole() == GameConstants.ROLE_DEFENDER) {
                shields.add(ant);
            }
        }
        int sacrificed = 0;
        for (Ant ant : shields) {
            if (sacrificed >= maxToSacrifice) {
                break;
            }
            removeAntFromColony(colony, ant);
            sacrificed++;
        }
        if (sacrificed > 0) {
            int defenderCount = colony.getWarAssignedRoleCount(GameConstants.ROLE_DEFENDER);
            colony.setWarAssignedRoleCount(GameConstants.ROLE_DEFENDER,
                    Math.max(0, defenderCount - sacrificed));
        }
        return sacrificed;
    }

    public static int countShieldingDefenders(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int count = 0;
        for (Ant ant : allLiveAnts(colony)) {
            if (ant.isShieldingActive() && ant.getRole() == GameConstants.ROLE_DEFENDER) {
                count++;
            }
        }
        return count;
    }

    public static float hexDefenseStatMultiplier(Ant ant, boolean attacking) {
        AntRole role = ant != null ? ant.getRole() : null;
        return hexDefenseStatMultiplier(role, attacking);
    }

    public static float hexDefenseStatMultiplier(AntRole role, boolean attacking) {
        if (attacking) {
            return role == GameConstants.ROLE_SIEGE
                    ? GameNumbers.WAR_HEX_SIEGE_ATTACKER_STAT_MULT
                    : GameNumbers.WAR_HEX_ATTACKER_STAT_MULT;
        }
        return role == GameConstants.ROLE_DEFENDER
                ? GameNumbers.WAR_HEX_DEFENDER_ROLE_STAT_MULT
                : GameNumbers.WAR_HEX_DEFENDING_STAT_MULT;
    }

    public static float effectiveHexDefenseAttack(Ant ant, boolean attacking) {
        if (ant == null) {
            return 0f;
        }
        return GameNumbers.applyHexDefenseAttack(ant.getAttack(), hexDefenseStatMultiplier(ant, attacking));
    }

    public static float effectiveHexDefenseDefense(Ant ant, boolean attacking) {
        if (ant == null) {
            return 0f;
        }
        return GameNumbers.applyHexDefenseDefense(ant.getDefense(), hexDefenseStatMultiplier(ant, attacking));
    }

    private static void removeAntFromColony(Colony colony, Ant ant) {
        if (colony == null || ant == null) {
            return;
        }
        if (colony.getWorkers() != null) {
            colony.getWorkers().remove(ant);
        }
        if (colony.getSoldiers() != null) {
            colony.getSoldiers().remove(ant);
        }
        if (colony.getMajors() != null) {
            colony.getMajors().remove(ant);
        }
        if (colony.getPrincesses() != null) {
            colony.getPrincesses().remove(ant);
        }
        if (colony.getQueens() != null) {
            colony.getQueens().remove(ant);
        }
    }

    private static List<Ant> allLiveAnts(Colony colony) {
        List<Ant> ants = new ArrayList<>();
        addAll(ants, colony.getWorkers());
        addAll(ants, colony.getSoldiers());
        addAll(ants, colony.getMajors());
        addAll(ants, colony.getPrincesses());
        addAll(ants, colony.getQueens());
        return ants;
    }

    private static void addAll(List<Ant> target, List<Ant> source) {
        if (source != null) {
            target.addAll(source);
        }
    }
}
