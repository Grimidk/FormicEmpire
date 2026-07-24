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
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

/**
 * Creature-vs-creature war battle resolution (border clash and hex assault).
 */
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
        WarBattleSideState atk = buildBorderSide(stageAttacker, false);
        WarBattleSideState def = buildBorderSide(stageDefender, false);
        WarBattleState state = new WarBattleState(atk, def, false, war.getContestedColonyId());
        WAR_BATTLES.put(war.getId(), state);
        syncDeployedCounts(war, state);
        return state;
    }

    public static WarBattleState startHexBattle(War war, Dynasty stageAttacker, Colony contested) {
        if (war == null || stageAttacker == null || contested == null || contested.getDynasty() == null) {
            return null;
        }
        WarBattleSideState atk = buildHexAttackerSide(stageAttacker);
        WarBattleSideState def = buildHexDefenderSide(contested);
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
                p.restorePeaceRole();
            }
            for (WarBattleParticipant p : side.getReserve(line)) {
                p.restorePeaceRole();
            }
        }
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
        if (skills.isEmpty()) {
            skills = List.of(GameConstants.SKILL_BASIC_BITE);
        }
        Skill skill = skills.get(GameRandom.nextInt(skills.size()));
        if (!skill.isAttack()) {
            useSupportSkill(ant, colony, skill);
            return;
        }
        int targetCount = Math.max(1, skill.getTargetCount());
        ensureFocusTargets(actor, opposingSide, targetCount);
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
        float raw = damageMult * attackStat;
        float dealt = GameNumbers.damageAfterDefense(raw, defenseStat);
        if (dealt <= 0f) {
            return;
        }
        if (target.applyBattleDamage(dealt) <= 0f) {
            killParticipant(state, target, targetSide);
        }
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
        promoteReserves(side);
    }

    private static void clearFocusOn(WarBattleState state, WarBattleParticipant dead) {
        for (WarBattleParticipant p : state.getAttacker().allLivingActive()) {
            p.getFocusTargets().remove(dead);
        }
        for (WarBattleParticipant p : state.getDefender().allLivingActive()) {
            p.getFocusTargets().remove(dead);
        }
    }

    private static void ensureFocusTargets(WarBattleParticipant actor, WarBattleSideState enemy, int desired) {
        List<WarBattleParticipant> focus = actor.getFocusTargets();
        focus.removeIf(t -> t == null || !t.isAlive());
        while (focus.size() < desired) {
            WarBattleParticipant next = pickTarget(actor, enemy, focus);
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
            List<WarBattleParticipant> exclude) {
        List<WarBattleParticipant> pool = eligibleTargets(actor.getBattleLine(), enemy);
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
        // Prefer living defenders when any queen would otherwise be chosen.
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
            // Soft redirect: if random would be queen, force defender — implemented by removing queens when defenders exist.
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

    private static List<WarBattleParticipant> eligibleTargets(BattleLine attackerLine, WarBattleSideState enemy) {
        List<WarBattleParticipant> pool = new ArrayList<>();
        if (attackerLine == GameConstants.BATTLE_LINE_INFANTRY) {
            addLiving(pool, enemy.getActive(GameConstants.BATTLE_LINE_INFANTRY));
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

    private static WarBattleSideState buildBorderSide(Dynasty dynasty, boolean hexPriority) {
        WarBattleSideState side = new WarBattleSideState(dynasty);
        List<WarBattleParticipant> pool = new ArrayList<>();
        Map<Ant, Boolean> seen = new IdentityHashMap<>();
        for (Colony colony : dynasty.getColonies()) {
            claimBorderQuotas(colony, pool, seen);
        }
        seatParticipants(side, pool, dynasty.getCombatCapacity(), hexPriority);
        side.setStartingArmySize(side.livingArmySize());
        return side;
    }

    private static WarBattleSideState buildHexAttackerSide(Dynasty dynasty) {
        WarBattleSideState side = new WarBattleSideState(dynasty);
        List<WarBattleParticipant> pool = new ArrayList<>();
        Map<Ant, Boolean> seen = new IdentityHashMap<>();
        for (Colony colony : dynasty.getColonies()) {
            claimBorderQuotas(colony, pool, seen);
            claimRoleQuota(colony, GameConstants.ROLE_SIEGE, pool, seen);
        }
        seatParticipants(side, pool, dynasty.getCombatCapacity(), false);
        side.setStartingArmySize(side.livingArmySize());
        return side;
    }

    private static WarBattleSideState buildHexDefenderSide(Colony contested) {
        Dynasty dynasty = contested.getDynasty();
        WarBattleSideState side = new WarBattleSideState(dynasty);
        List<WarBattleParticipant> pool = new ArrayList<>();
        Map<Ant, Boolean> seen = new IdentityHashMap<>();
        // Apply war quotas onto ants first so Defender/Siege skills and always-active rules work.
        claimRoleQuota(contested, GameConstants.ROLE_DEFENDER, pool, seen);
        claimRoleQuota(contested, GameConstants.ROLE_SIEGE, pool, seen);
        for (AntRole role : GameConstants.getBorderBattleRoles()) {
            if (role != GameConstants.ROLE_DEFENDER) {
                claimRoleQuota(contested, role, pool, seen);
            }
        }
        collectRemainingColonyAnts(contested, pool, seen);
        seatParticipants(side, pool, dynasty.getCombatCapacity(), true);
        side.setStartingArmySize(side.livingArmySize());
        return side;
    }

    private static void claimBorderQuotas(Colony colony, List<WarBattleParticipant> pool,
            Map<Ant, Boolean> seen) {
        for (AntRole role : GameConstants.getBorderBattleRoles()) {
            claimRoleQuota(colony, role, pool, seen);
        }
    }

    /**
     * Claims up to the war-assigned quota of ants of the role's type. Temporarily sets
     * {@link Ant#setRole(AntRole)} so skills resolve; restored via {@link #clear(War)}.
     */
    private static void claimRoleQuota(Colony colony, AntRole role, List<WarBattleParticipant> pool,
            Map<Ant, Boolean> seen) {
        if (colony == null || role == null || role.getAntType() == null) {
            return;
        }
        int quota = colony.getWarAssignedRoleCount(role);
        if (quota <= 0) {
            return;
        }
        List<Ant> candidates = antsOfType(colony, role.getAntType());
        List<Ant> preferred = new ArrayList<>();
        List<Ant> fallback = new ArrayList<>();
        for (Ant ant : candidates) {
            if (ant == null || !ant.isAlive() || seen.containsKey(ant)) {
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
        takeClaims(fallback, role, pool, seen, quota - claimed);
    }

    private static int takeClaims(List<Ant> ants, AntRole role, List<WarBattleParticipant> pool,
            Map<Ant, Boolean> seen, int remaining) {
        int claimed = 0;
        for (Ant ant : ants) {
            if (claimed >= remaining) {
                break;
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
            if (ant == null || !ant.isAlive() || seen.containsKey(ant)) {
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

    /** Queens always active (commanders). Hex defense: Defenders + Siege too. Hex assault: Siege too. */
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
