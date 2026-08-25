package com.grimidk.formicempire.classes.entities.services.world;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.grimidk.formicempire.classes.constants.critter.Skill;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.entities.services.shared.CritterSkillService;
import com.grimidk.formicempire.classes.entities.services.shared.WarCombatSkillService;
import com.grimidk.formicempire.classes.infrasctructure.registries.DeathCause;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

public final class WarCreatureCombatService {

    public enum TickOutcome {
        CONTINUE,
        ATTACKER_WINS,
        DEFENDER_WINS
    }

    private static final Map<Integer, WarBattleState> WAR_BATTLES = new java.util.concurrent.ConcurrentHashMap<>();

    private WarCreatureCombatService() {
    }

    public static WarBattleState getState(War war) {
        return war != null ? WAR_BATTLES.get(war.getId()) : null;
    }

    public static boolean hasLivingCombatants(War war) {
        WarBattleState state = getState(war);
        return state != null
                && (state.getAttacker().livingArmySize() > 0 || state.getDefender().livingArmySize() > 0);
    }

    public static WarBattleState startBorderBattle(War war, Dynasty stageAttacker, Dynasty stageDefender) {
        if (war == null || stageAttacker == null || stageDefender == null) {
            return null;
        }
        int attackerFronts = countBattlesInvolving(stageAttacker) + 1;
        int defenderFronts = countBattlesInvolving(stageDefender) + 1;
        WarBattleSideState atk = buildBorderSide(stageAttacker, false, attackerFronts);
        WarBattleSideState def = buildBorderSide(stageDefender, false, defenderFronts);
        WarBattleState state = new WarBattleState(atk, def, false, war.getContestedColonyId());
        WAR_BATTLES.put(war.getId(), state);
        syncDeployedCounts(war, state);
        return state;
    }

    public static WarBattleState startHexBattle(War war, Dynasty stageAttacker, Colony contested) {
        if (war == null || stageAttacker == null || contested == null || contested.getDynasty() == null) {
            return null;
        }
        int attackerFronts = countBattlesInvolving(stageAttacker) + 1;
        int defenderFronts = countBattlesInvolving(contested.getDynasty()) + 1;
        WarBattleSideState atk = buildHexAttackerSide(stageAttacker, attackerFronts);
        WarBattleSideState def = buildHexDefenderSide(contested, defenderFronts);
        WarCombatSkillService.armShieldingForEligibleDefenders(contested);
        WarBattleState state = new WarBattleState(atk, def, true, contested.getId());
        WAR_BATTLES.put(war.getId(), state);
        syncDeployedCounts(war, state);
        return state;
    }

    public static TickOutcome tick(War war) {
        if (war == null) {
            return TickOutcome.CONTINUE;
        }
        WarBattleState state = WAR_BATTLES.get(war.getId());
        if (state == null) {
            return TickOutcome.CONTINUE;
        }
        state.advanceTick();
        int tick = state.getTickIndex();

        resolveLinePhase(state, true, GameConstants.BATTLE_LINE_INFANTRY);
        resolveLinePhase(state, false, GameConstants.BATTLE_LINE_INFANTRY);
        if (tick % 2 == 0) {
            resolveLinePhase(state, true, GameConstants.BATTLE_LINE_ARTILLERY);
            resolveLinePhase(state, false, GameConstants.BATTLE_LINE_ARTILLERY);
        }
        if (tick % 3 == 0) {
            resolveLinePhase(state, true, GameConstants.BATTLE_LINE_AIR_SUPPORT);
            resolveLinePhase(state, false, GameConstants.BATTLE_LINE_AIR_SUPPORT);
        }

        promoteReserves(state.getAttacker());
        promoteReserves(state.getDefender());
        syncDeployedCounts(war, state);
        updateStageCasualtyProgress(war, state);

        return evaluateWinner(state);
    }

    public static void clear(War war) {
        if (war == null) {
            return;
        }
        WarBattleState state = WAR_BATTLES.remove(war.getId());
        if (state != null) {
            restorePeaceRoles(state.getAttacker());
            restorePeaceRoles(state.getDefender());
        }
    }

    private static void restorePeaceRoles(WarBattleSideState side) {
        if (side == null) {
            return;
        }
        for (BattleLine line : GameConstants.getBattleLines()) {
            for (WarBattleParticipant p : side.getActive(line)) {
                restorePeaceRoleIfUncommitted(p);
            }
            for (WarBattleParticipant p : side.getReserve(line)) {
                restorePeaceRoleIfUncommitted(p);
            }
        }
    }

    private static void restorePeaceRoleIfUncommitted(WarBattleParticipant participant) {
        if (participant == null || participant.getAnt() == null) {
            return;
        }
        if (isAntCommittedToBattle(participant.getAnt())) {
            return;
        }
        participant.restorePeaceRole();
    }

    static boolean isAntCommittedToBattle(Ant ant) {
        if (ant == null) {
            return false;
        }
        for (WarBattleState state : WAR_BATTLES.values()) {
            if (stateContainsAnt(state, ant)) {
                return true;
            }
        }
        return false;
    }

    private static boolean stateContainsAnt(WarBattleState state, Ant ant) {
        return sideContainsAnt(state.getAttacker(), ant) || sideContainsAnt(state.getDefender(), ant);
    }

    private static boolean sideContainsAnt(WarBattleSideState side, Ant ant) {
        if (side == null || ant == null) {
            return false;
        }
        for (BattleLine line : GameConstants.getBattleLines()) {
            for (WarBattleParticipant p : side.getActive(line)) {
                if (p != null && p.getAnt() == ant) {
                    return true;
                }
            }
            for (WarBattleParticipant p : side.getReserve(line)) {
                if (p != null && p.getAnt() == ant) {
                    return true;
                }
            }
        }
        return false;
    }

    private static TickOutcome evaluateWinner(WarBattleState state) {
        if (state.isHexAssault()) {
            if (!state.getDefender().hasLivingQueen()) {
                return TickOutcome.ATTACKER_WINS;
            }
            if (state.getAttacker().livingArmySize() <= 0) {
                return TickOutcome.DEFENDER_WINS;
            }
            return TickOutcome.CONTINUE;
        }
        if (isArmyDefeated(state.getAttacker())) {
            return TickOutcome.DEFENDER_WINS;
        }
        if (isArmyDefeated(state.getDefender())) {
            return TickOutcome.ATTACKER_WINS;
        }
        return TickOutcome.CONTINUE;
    }

    private static boolean isArmyDefeated(WarBattleSideState side) {
        if (side.getStartingArmySize() <= 0) {
            return side.livingArmySize() <= 0;
        }
        return side.getDeadCount() >= Math.ceil(side.getStartingArmySize() * GameNumbers.WAR_BATTLE_ARMY_DEFEAT_RATIO);
    }

    private static void updateStageCasualtyProgress(War war, WarBattleState state) {
        int start = state.getAttacker().getStartingArmySize() + state.getDefender().getStartingArmySize();
        if (start <= 0) {
            war.setStageProgress(0f);
            return;
        }
        int dead = state.getAttacker().getDeadCount() + state.getDefender().getDeadCount();
        war.setStageProgress(Math.min(1f, dead / (float) start));
    }

    private static void syncDeployedCounts(War war, WarBattleState state) {
        war.setDeployedActiveAttacker(state.getAttacker().livingActiveSize());
        war.setDeployedActiveDefender(state.isHexAssault() ? 0 : state.getDefender().livingActiveSize());
        war.setDeployedReserveDefender(state.isHexAssault()
                ? state.getDefender().livingArmySize()
                : Math.max(0, state.getDefender().livingArmySize() - state.getDefender().livingActiveSize()));
    }

    private static void resolveLinePhase(WarBattleState state, boolean attackerActing, BattleLine line) {
        WarBattleSideState acting = attackerActing ? state.getAttacker() : state.getDefender();
        WarBattleSideState opposing = attackerActing ? state.getDefender() : state.getAttacker();
        List<WarBattleParticipant> actors = new ArrayList<>(acting.getActive(line));
        for (WarBattleParticipant actor : actors) {
            if (!actor.isAlive()) {
                continue;
            }
            int actions = Math.max(1, Math.round(actor.getAnt().getAttackSpeed()));
            for (int i = 0; i < actions; i++) {
                if (!actor.isAlive()) {
                    break;
                }
                performAction(state, actor, acting, opposing, attackerActing);
            }
        }
    }

    private static void performAction(WarBattleState state, WarBattleParticipant actor, WarBattleSideState actingSide,
            WarBattleSideState opposingSide, boolean actorIsAttacker) {
        Ant ant = actor.getAnt();
        Colony colony = findColonyOf(ant, actingSide.getDynasty());
        List<Skill> skills = CritterSkillService.resolveAvailableSkills(ant, colony);
        List<Skill> actionable = new ArrayList<>();
        for (Skill candidate : skills) {
            if (candidate != null && !candidate.isPassive()) {
                actionable.add(candidate);
            }
        }
        if (actionable.isEmpty()) {
            actionable = List.of(GameConstants.SKILL_BASIC_BITE);
        }
        Skill skill = actionable.get(GameRandom.nextInt(actionable.size()));
        if (!skill.isAttack()) {
            useSupportSkill(ant, colony, skill);
            return;
        }
        int targetCount = Math.max(1, skill.getTargetCount());
        actor.getFocusTargets().clear();
        ensureFocusTargets(actor, opposingSide, actingSide.getDynasty(), skill, targetCount);
        List<WarBattleParticipant> targets = new ArrayList<>(actor.getFocusTargets());
        for (WarBattleParticipant target : targets) {
            if (!target.isAlive()) {
                continue;
            }
            if (!rollHit(skill, actor)) {
                continue;
            }
            applyDamage(state, actor, target, opposingSide, skill, actorIsAttacker);
        }
        if (skill.sacrificesSelf() && actor.isAlive()) {
            killParticipant(state, actor, actingSide);
        }
    }

    private static void useSupportSkill(Ant ant, Colony colony, Skill skill) {
        if (skill == GameConstants.SKILL_BOOST_REGEN) {
            WarCombatSkillService.useBoostRegen(ant, colony);
        } else if (skill == GameConstants.SKILL_SHIELDING) {
            WarCombatSkillService.useShielding(ant, colony);
        }
    }

    private static boolean rollHit(Skill skill, WarBattleParticipant actor) {
        float skillAcc = CritterSkillService.resolveAccuracyMult(skill, actor.getAnt().getSubtypeProfile());
        float lineAcc = actor.getBattleLine().getBaseAccuracyPercent() / 100f;
        float chance = Math.min(1f, Math.max(0f, skillAcc * lineAcc));
        return GameRandom.nextDouble() < chance;
    }

    private static void applyDamage(WarBattleState state, WarBattleParticipant attacker,
            WarBattleParticipant target, WarBattleSideState targetSide, Skill skill, boolean attackerIsAttackerSide) {
        Ant atkAnt = attacker.getAnt();
        Ant defAnt = target.getAnt();
        float attackStat = atkAnt.getAttack();
        float defenseStat = defAnt.getDefense();
        if (state.isHexAssault()) {
            attackStat = WarCombatSkillService.effectiveHexDefenseAttack(atkAnt, attackerIsAttackerSide);
            defenseStat = WarCombatSkillService.effectiveHexDefenseDefense(defAnt, !attackerIsAttackerSide);
        }
        float damageMult = CritterSkillService.resolveDamageMult(skill, atkAnt.getSubtypeProfile());
        WarBattleSideState actingSide = attackerIsAttackerSide ? state.getAttacker() : state.getDefender();
        if (jumpingBoostsMelee(actingSide.getDynasty(), skill)) {
            damageMult *= GameNumbers.ASSIMILATED_JUMPING_MELEE_DAMAGE_MULT;
        }
        float raw = damageMult * attackStat * laneDamageMultiplier(actingSide, attacker.getBattleLine());
        float dealt = GameNumbers.damageAfterDefense(raw, defenseStat);
        if (dealt <= 0f) {
            return;
        }
        if (target.applyBattleDamage(dealt) <= 0f) {
            killParticipant(state, target, targetSide);
        }
    }

    private static float laneDamageMultiplier(WarBattleSideState side, BattleLine line) {
        if (side == null || line == null) {
            return 1f;
        }
        float mult = 1f;
        if (line == GameConstants.BATTLE_LINE_ARTILLERY && sideHasLivingRole(side, GameConstants.ROLE_COMMANDER)) {
            mult += GameNumbers.COMMANDER_ARTILLERY_DAMAGE_BONUS;
        }
        if (line == GameConstants.BATTLE_LINE_INFANTRY && sideHasLivingRole(side, GameConstants.ROLE_CAPTAIN)) {
            mult += GameNumbers.CAPTAIN_INFANTRY_DAMAGE_BONUS;
        }
        return mult;
    }

    private static boolean sideHasLivingRole(WarBattleSideState side, AntRole role) {
        if (side == null || role == null) {
            return false;
        }
        for (WarBattleParticipant participant : side.allLivingActive()) {
            if (participant != null && participant.getAnt() != null && participant.getAnt().getRole() == role) {
                return true;
            }
        }
        return false;
    }

    private static void killParticipant(WarBattleState state, WarBattleParticipant victim, WarBattleSideState side) {
        if (victim == null || victim.getAnt() == null) {
            return;
        }
        Ant ant = victim.getAnt();
        if (!ant.isAlive() && ant.getAntType() == GameConstants.TYPE_DEAD) {
            return;
        }
        ant.setHealth(0);
        Colony colony = findColonyOf(ant, side.getDynasty());
        AntRole role = ant.getRole();
        if (colony != null) {
            ant.goDie(colony, DeathCause.CONFLICT);
            colony.recordAntDeath(ant, DeathCause.CONFLICT);
            if (role != null && role.isActiveMilitary()) {
                int count = colony.getWarAssignedRoleCount(role);
                colony.setWarAssignedRoleCount(role, Math.max(0, count - 1));
            }
            removeAntFromLists(colony, ant);
        } else {
            ant.goDie();
        }
        side.incrementDead();
        clearFocusOn(state, victim);
        detachAntFromOtherBattles(ant, state);
        promoteReserves(side);
    }

    private static void detachAntFromOtherBattles(Ant ant, WarBattleState except) {
        if (ant == null) {
            return;
        }
        for (WarBattleState other : WAR_BATTLES.values()) {
            if (other == null || other == except) {
                continue;
            }
            removeAntFromSide(other.getAttacker(), ant);
            removeAntFromSide(other.getDefender(), ant);
            clearFocusOnAnt(other, ant);
        }
    }

    private static void removeAntFromSide(WarBattleSideState side, Ant ant) {
        if (side == null || ant == null) {
            return;
        }
        for (BattleLine line : GameConstants.getBattleLines()) {
            side.getActive(line).removeIf(p -> p != null && p.getAnt() == ant);
            side.getReserve(line).removeIf(p -> p != null && p.getAnt() == ant);
        }
    }

    private static void clearFocusOnAnt(WarBattleState state, Ant ant) {
        if (state == null || ant == null) {
            return;
        }
        for (WarBattleParticipant p : state.getAttacker().allLivingActive()) {
            p.getFocusTargets().removeIf(t -> t != null && t.getAnt() == ant);
        }
        for (WarBattleParticipant p : state.getDefender().allLivingActive()) {
            p.getFocusTargets().removeIf(t -> t != null && t.getAnt() == ant);
        }
    }

    private static void clearFocusOn(WarBattleState state, WarBattleParticipant dead) {
        for (WarBattleParticipant p : state.getAttacker().allLivingActive()) {
            p.getFocusTargets().remove(dead);
        }
        for (WarBattleParticipant p : state.getDefender().allLivingActive()) {
            p.getFocusTargets().remove(dead);
        }
    }

    private static void ensureFocusTargets(WarBattleParticipant actor, WarBattleSideState enemy, Dynasty actingDynasty,
            Skill skill, int desired) {
        List<WarBattleParticipant> focus = actor.getFocusTargets();
        focus.removeIf(t -> t == null || !t.isAlive());
        while (focus.size() < desired) {
            WarBattleParticipant next = pickTarget(actor, enemy, actingDynasty, skill, focus);
            if (next == null) {
                break;
            }
            focus.add(next);
        }
        while (focus.size() > desired) {
            focus.remove(focus.size() - 1);
        }
    }

    private static WarBattleParticipant pickTarget(WarBattleParticipant actor, WarBattleSideState enemy,
            Dynasty actingDynasty, Skill skill, List<WarBattleParticipant> exclude) {
        List<WarBattleParticipant> pool = eligibleTargets(actor.getBattleLine(), enemy, actingDynasty, skill);
        pool.removeIf(exclude::contains);
        if (pool.isEmpty()) {
            return null;
        }
        if (enemy.hasLivingDefenders()) {
            List<WarBattleParticipant> nonQueens = new ArrayList<>();
            for (WarBattleParticipant p : pool) {
                if (!p.isQueen()) {
                    nonQueens.add(p);
                }
            }
            if (!nonQueens.isEmpty()) {
                pool = nonQueens;
            }
        }
        boolean hasDefender = false;
        for (WarBattleParticipant p : pool) {
            if (p.isDefenderRole()) {
                hasDefender = true;
                break;
            }
        }
        if (hasDefender) {
            List<WarBattleParticipant> defenders = new ArrayList<>();
            for (WarBattleParticipant p : pool) {
                if (p.isDefenderRole()) {
                    defenders.add(p);
                }
            }
            List<WarBattleParticipant> withoutQueens = new ArrayList<>();
            for (WarBattleParticipant p : pool) {
                if (!p.isQueen()) {
                    withoutQueens.add(p);
                }
            }
            if (!withoutQueens.isEmpty()) {
                pool = withoutQueens;
            }
        }
        return pool.get(GameRandom.nextInt(pool.size()));
    }

    static List<WarBattleParticipant> eligibleTargetsForSkill(BattleLine attackerLine, WarBattleSideState enemy,
            Dynasty actingDynasty, Skill skill) {
        return eligibleTargets(attackerLine, enemy, actingDynasty, skill);
    }

    private static List<WarBattleParticipant> eligibleTargets(BattleLine attackerLine, WarBattleSideState enemy,
            Dynasty actingDynasty, Skill skill) {
        List<WarBattleParticipant> pool = new ArrayList<>();
        if (attackerLine == GameConstants.BATTLE_LINE_INFANTRY) {
            addLiving(pool, enemy.getActive(GameConstants.BATTLE_LINE_INFANTRY));
            if (jumpingBoostsMelee(actingDynasty, skill)) {
                addLiving(pool, enemy.getActive(GameConstants.BATTLE_LINE_ARTILLERY));
            }
        } else if (attackerLine == GameConstants.BATTLE_LINE_ARTILLERY) {
            addLiving(pool, enemy.getActive(GameConstants.BATTLE_LINE_INFANTRY));
            addLiving(pool, enemy.getActive(GameConstants.BATTLE_LINE_ARTILLERY));
        } else {
            addLiving(pool, enemy.getActive(GameConstants.BATTLE_LINE_INFANTRY));
            addLiving(pool, enemy.getActive(GameConstants.BATTLE_LINE_ARTILLERY));
            addLiving(pool, enemy.getActive(GameConstants.BATTLE_LINE_AIR_SUPPORT));
        }
        return pool;
    }

    private static boolean hasJumpingAbility(Dynasty dynasty) {
        return dynasty != null && (dynasty.hasUpgrade(GameUnlocks.ABILITY_JUMPING)
                || dynasty.hasUpgrade(GameUnlocks.ASSIMILATED_JUMPING));
    }

    private static boolean jumpingBoostsMelee(Dynasty dynasty, Skill skill) {
        return hasJumpingAbility(dynasty) && isInfantryMeleeSkill(skill);
    }

    private static boolean isInfantryMeleeSkill(Skill skill) {
        return skill != null && skill.isAttack()
                && skill.getBattleLine() == GameConstants.BATTLE_LINE_INFANTRY;
    }

    private static void addLiving(List<WarBattleParticipant> pool, List<WarBattleParticipant> source) {
        for (WarBattleParticipant p : source) {
            if (p.isAlive()) {
                pool.add(p);
            }
        }
    }

    private static void promoteReserves(WarBattleSideState side) {
        if (side == null || side.getDynasty() == null) {
            return;
        }
        int capacity = Math.max(1, side.getDynasty().getCombatCapacity());
        for (BattleLine line : GameConstants.getBattleLines()) {
            List<WarBattleParticipant> active = side.getActive(line);
            List<WarBattleParticipant> reserve = side.getReserve(line);
            active.removeIf(p -> !p.isAlive());
            reserve.removeIf(p -> !p.isAlive());
            while (active.size() < capacity && !reserve.isEmpty()) {
                active.add(reserve.remove(0));
            }
        }
    }

    private static WarBattleSideState buildBorderSide(Dynasty dynasty, boolean hexPriority, int frontCount) {
        int fronts = Math.max(1, frontCount);
        rebalanceCommittedForces(dynasty, fronts);
        WarBattleSideState side = new WarBattleSideState(dynasty);
        List<WarBattleParticipant> pool = new ArrayList<>();
        Map<Ant, Boolean> seen = new IdentityHashMap<>();
        for (AntRole role : GameConstants.getBorderBattleRoles()) {
            claimDynastyRoleQuota(dynasty, role, pool, seen, fronts);
        }
        seatParticipants(side, pool, dynasty.getCombatCapacity(), hexPriority);
        side.setStartingArmySize(side.livingArmySize());
        return side;
    }

    private static WarBattleSideState buildHexAttackerSide(Dynasty dynasty, int frontCount) {
        int fronts = Math.max(1, frontCount);
        rebalanceCommittedForces(dynasty, fronts);
        WarBattleSideState side = new WarBattleSideState(dynasty);
        List<WarBattleParticipant> pool = new ArrayList<>();
        Map<Ant, Boolean> seen = new IdentityHashMap<>();
        for (AntRole role : GameConstants.getBorderBattleRoles()) {
            claimDynastyRoleQuota(dynasty, role, pool, seen, fronts);
        }
        claimDynastyRoleQuota(dynasty, GameConstants.ROLE_SIEGE, pool, seen, fronts);
        seatParticipants(side, pool, dynasty.getCombatCapacity(), false);
        side.setStartingArmySize(side.livingArmySize());
        return side;
    }

    private static WarBattleSideState buildHexDefenderSide(Colony contested, int frontCount) {
        Dynasty dynasty = contested.getDynasty();
        int fronts = Math.max(1, frontCount);
        rebalanceCommittedForces(dynasty, fronts);
        WarBattleSideState side = new WarBattleSideState(dynasty);
        List<WarBattleParticipant> pool = new ArrayList<>();
        Map<Ant, Boolean> seen = new IdentityHashMap<>();
        int defenderShare = fairShare(contested.getWarAssignedRoleCount(GameConstants.ROLE_DEFENDER), fronts);
        claimRoleQuota(contested, GameConstants.ROLE_DEFENDER, pool, seen, defenderShare);
        int siegeShare = fairShare(contested.getWarAssignedRoleCount(GameConstants.ROLE_SIEGE), fronts);
        claimRoleQuota(contested, GameConstants.ROLE_SIEGE, pool, seen, siegeShare);
        for (AntRole role : GameConstants.getBorderBattleRoles()) {
            if (role == GameConstants.ROLE_DEFENDER) {
                continue;
            }
            int share = fairShare(contested.getWarAssignedRoleCount(role), fronts);
            claimRoleQuota(contested, role, pool, seen, share);
        }
        collectRemainingColonyAnts(contested, pool, seen);
        seatParticipants(side, pool, dynasty.getCombatCapacity(), true);
        side.setStartingArmySize(side.livingArmySize());
        return side;
    }

    private static void claimDynastyRoleQuota(Dynasty dynasty, AntRole role, List<WarBattleParticipant> pool,
            Map<Ant, Boolean> seen, int frontCount) {
        if (dynasty == null || role == null) {
            return;
        }
        int remaining = fairShare(totalWarQuota(dynasty, role), frontCount);
        for (Colony colony : dynasty.getColonies()) {
            if (remaining <= 0) {
                break;
            }
            remaining -= claimRoleQuota(colony, role, pool, seen, remaining);
        }
    }

    private static int totalWarQuota(Dynasty dynasty, AntRole role) {
        if (dynasty == null || role == null) {
            return 0;
        }
        int total = 0;
        for (Colony colony : dynasty.getColonies()) {
            total += Math.max(0, colony.getWarAssignedRoleCount(role));
        }
        return total;
    }

    private static int fairShare(int totalQuota, int frontCount) {
        int fronts = Math.max(1, frontCount);
        if (totalQuota <= 0) {
            return 0;
        }
        return (totalQuota + fronts - 1) / fronts;
    }

    private static int countBattlesInvolving(Dynasty dynasty) {
        if (dynasty == null) {
            return 0;
        }
        int count = 0;
        for (WarBattleState state : WAR_BATTLES.values()) {
            if (sideForDynasty(state, dynasty) != null) {
                count++;
            }
        }
        return count;
    }

    private static WarBattleSideState sideForDynasty(WarBattleState state, Dynasty dynasty) {
        if (state == null || dynasty == null) {
            return null;
        }
        if (state.getAttacker() != null && state.getAttacker().getDynasty() == dynasty) {
            return state.getAttacker();
        }
        if (state.getDefender() != null && state.getDefender().getDynasty() == dynasty) {
            return state.getDefender();
        }
        return null;
    }

    private static void rebalanceCommittedForces(Dynasty dynasty, int frontCount) {
        if (dynasty == null || frontCount <= 1) {
            return;
        }
        for (WarBattleState state : WAR_BATTLES.values()) {
            WarBattleSideState side = sideForDynasty(state, dynasty);
            if (side != null) {
                trimSideToFairShares(side, dynasty, frontCount);
                side.setStartingArmySize(side.livingArmySize() + side.getDeadCount());
                promoteReserves(side);
            }
        }
    }

    private static void trimSideToFairShares(WarBattleSideState side, Dynasty dynasty, int frontCount) {
        for (AntRole role : splittableMilitaryRoles()) {
            int share = fairShare(totalWarQuota(dynasty, role), frontCount);
            List<WarBattleParticipant> ofRole = livingParticipantsWithRole(side, role);
            while (ofRole.size() > share) {
                WarBattleParticipant excess = takeExcessParticipant(side, ofRole);
                if (excess == null) {
                    break;
                }
                ofRole.remove(excess);
                releaseParticipant(side, excess);
            }
        }
    }

    private static List<AntRole> splittableMilitaryRoles() {
        List<AntRole> roles = new ArrayList<>();
        for (AntRole role : GameConstants.getBorderBattleRoles()) {
            roles.add(role);
        }
        if (!roles.contains(GameConstants.ROLE_SIEGE)) {
            roles.add(GameConstants.ROLE_SIEGE);
        }
        return roles;
    }

    private static List<WarBattleParticipant> livingParticipantsWithRole(WarBattleSideState side, AntRole role) {
        List<WarBattleParticipant> found = new ArrayList<>();
        if (side == null || role == null) {
            return found;
        }
        for (BattleLine line : GameConstants.getBattleLines()) {
            for (WarBattleParticipant p : side.getReserve(line)) {
                if (p != null && p.isAlive() && p.getAnt() != null && p.getAnt().getRole() == role) {
                    found.add(p);
                }
            }
            for (WarBattleParticipant p : side.getActive(line)) {
                if (p != null && p.isAlive() && p.getAnt() != null && p.getAnt().getRole() == role
                        && !p.isQueen()) {
                    found.add(p);
                }
            }
        }
        return found;
    }

    private static WarBattleParticipant takeExcessParticipant(WarBattleSideState side,
            List<WarBattleParticipant> ofRole) {
        if (ofRole.isEmpty()) {
            return null;
        }
        for (int i = ofRole.size() - 1; i >= 0; i--) {
            WarBattleParticipant candidate = ofRole.get(i);
            for (BattleLine line : GameConstants.getBattleLines()) {
                if (side.getReserve(line).contains(candidate)) {
                    return candidate;
                }
            }
        }
        return ofRole.get(ofRole.size() - 1);
    }

    private static void releaseParticipant(WarBattleSideState side, WarBattleParticipant participant) {
        if (side == null || participant == null) {
            return;
        }
        for (BattleLine line : GameConstants.getBattleLines()) {
            side.getActive(line).remove(participant);
            side.getReserve(line).remove(participant);
        }
        restorePeaceRoleIfUncommitted(participant);
    }

    private static int claimRoleQuota(Colony colony, AntRole role, List<WarBattleParticipant> pool,
            Map<Ant, Boolean> seen, int maxToClaim) {
        if (colony == null || role == null || role.getAntType() == null || maxToClaim <= 0) {
            return 0;
        }
        int quota = Math.min(maxToClaim, colony.getWarAssignedRoleCount(role));
        if (quota <= 0) {
            return 0;
        }
        List<Ant> candidates = antsOfType(colony, role.getAntType());
        List<Ant> preferred = new ArrayList<>();
        List<Ant> fallback = new ArrayList<>();
        for (Ant ant : candidates) {
            if (ant == null || !ant.isAlive() || seen.containsKey(ant) || isAntCommittedToBattle(ant)) {
                continue;
            }
            if (ant.getRole() == role) {
                preferred.add(ant);
            } else {
                fallback.add(ant);
            }
        }
        int claimed = 0;
        claimed += takeClaims(preferred, role, pool, seen, quota - claimed);
        claimed += takeClaims(fallback, role, pool, seen, quota - claimed);
        return claimed;
    }

    private static int takeClaims(List<Ant> ants, AntRole role, List<WarBattleParticipant> pool,
            Map<Ant, Boolean> seen, int remaining) {
        int claimed = 0;
        for (Ant ant : ants) {
            if (claimed >= remaining) {
                break;
            }
            if (ant == null || !ant.isAlive() || seen.containsKey(ant) || isAntCommittedToBattle(ant)) {
                continue;
            }
            AntRole previous = ant.getRole();
            boolean override = previous != role;
            if (override) {
                ant.setRole(role);
            }
            seen.put(ant, Boolean.TRUE);
            BattleLine line = GameConstants.getBattleLineForRole(role);
            if (line == null) {
                line = WarBattleSideState.lineForAnt(ant);
            }
            pool.add(new WarBattleParticipant(ant, line, previous, override));
            claimed++;
        }
        return claimed;
    }

    private static void collectRemainingColonyAnts(Colony colony, List<WarBattleParticipant> pool,
            Map<Ant, Boolean> seen) {
        for (Ant ant : allAnts(colony)) {
            if (ant == null || !ant.isAlive() || seen.containsKey(ant) || isAntCommittedToBattle(ant)) {
                continue;
            }
            seen.put(ant, Boolean.TRUE);
            pool.add(new WarBattleParticipant(ant, WarBattleSideState.lineForAnt(ant), ant.getRole(), false));
        }
    }

    private static List<Ant> antsOfType(Colony colony, AntType type) {
        List<Ant> ants = new ArrayList<>();
        if (colony == null || type == null) {
            return ants;
        }
        if (type == GameConstants.TYPE_WORKER) {
            addAll(ants, colony.getWorkers());
        } else if (type == GameConstants.TYPE_SOLDIER) {
            addAll(ants, colony.getSoldiers());
        } else if (type == GameConstants.TYPE_MAJOR) {
            addAll(ants, colony.getMajors());
        } else if (type == GameConstants.TYPE_PRINCESS) {
            addAll(ants, colony.getPrincesses());
        } else if (type == GameConstants.TYPE_QUEEN) {
            addAll(ants, colony.getQueens());
        }
        return ants;
    }

    private static void seatParticipants(WarBattleSideState side, List<WarBattleParticipant> pool,
            int capacityPerLine, boolean hexHomePriority) {
        int capacity = Math.max(1, capacityPerLine);
        Map<BattleLine, List<WarBattleParticipant>> byLine = new java.util.HashMap<>();
        for (BattleLine line : GameConstants.getBattleLines()) {
            byLine.put(line, new ArrayList<>());
        }
        for (WarBattleParticipant p : pool) {
            byLine.computeIfAbsent(p.getBattleLine(), k -> new ArrayList<>()).add(p);
        }
        for (BattleLine line : GameConstants.getBattleLines()) {
            List<WarBattleParticipant> linePool = byLine.getOrDefault(line, List.of());
            List<WarBattleParticipant> priority = new ArrayList<>();
            List<WarBattleParticipant> normal = new ArrayList<>();
            for (WarBattleParticipant p : linePool) {
                if (forcesAlwaysActive(p, hexHomePriority)) {
                    priority.add(p);
                } else {
                    normal.add(p);
                }
            }
            List<WarBattleParticipant> ordered = new ArrayList<>(priority);
            ordered.addAll(normal);
            List<WarBattleParticipant> active = side.getActive(line);
            List<WarBattleParticipant> reserve = side.getReserve(line);
            for (WarBattleParticipant p : ordered) {
                if (forcesAlwaysActive(p, hexHomePriority)) {
                    active.add(p);
                } else if (active.size() < capacity) {
                    active.add(p);
                } else {
                    reserve.add(p);
                }
            }
        }
    }

    private static boolean forcesAlwaysActive(WarBattleParticipant p, boolean hexDefenderSide) {
        if (p.isQueen()) {
            return true;
        }
        if (p.getAnt() != null && p.getAnt().getRole() == GameConstants.ROLE_SIEGE) {
            return true;
        }
        return hexDefenderSide && p.isDefenderRole();
    }

    private static List<Ant> allAnts(Colony colony) {
        List<Ant> ants = new ArrayList<>();
        if (colony == null) {
            return ants;
        }
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

    private static Colony findColonyOf(Ant ant, Dynasty dynasty) {
        if (ant == null || dynasty == null) {
            return null;
        }
        for (Colony colony : dynasty.getColonies()) {
            if (listContains(colony.getWorkers(), ant)
                    || listContains(colony.getSoldiers(), ant)
                    || listContains(colony.getMajors(), ant)
                    || listContains(colony.getPrincesses(), ant)
                    || listContains(colony.getQueens(), ant)) {
                return colony;
            }
        }
        return null;
    }

    private static boolean listContains(List<Ant> list, Ant ant) {
        return list != null && list.contains(ant);
    }

    private static void removeAntFromLists(Colony colony, Ant ant) {
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
        ColonyMilitaryService.refreshColonyMilitaryPower(colony);
    }
}
