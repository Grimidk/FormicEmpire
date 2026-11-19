package com.grimidk.formicempire.classes.entities.services.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.grimidk.formicempire.classes.constants.critter.Skill;
import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
import com.grimidk.formicempire.classes.constants.critter.ant.AntModProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.entities.services.colony.AntModService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.entities.services.shared.CritterSkillService;
import com.grimidk.formicempire.classes.entities.services.shared.HexWaterCrossing;
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

    private static final Map<Integer, WarBattleState> WAR_BATTLES = new ConcurrentHashMap<>();
    private static final Object COMMIT_LOCK = new Object();
    private static final IdentityHashMap<Ant, WarBattleState> COMMITTED_ANT_STATES = new IdentityHashMap<>();
    private static final List<AntRole> SPLITTABLE_MILITARY_ROLES = buildSplittableMilitaryRoles();

    private record SkillCacheKey(int dynastyOrSpeciesKey, int roleId, int modCode) {
    }

    private static final class TickContext {
        final Map<SkillCacheKey, List<Skill>> skillCache = new HashMap<>();
        final Map<Skill, String> skillNameCache = new IdentityHashMap<>();
        final List<WarBattleParticipant> poolScratch = new ArrayList<>();
        final List<WarBattleParticipant> nonQueenScratch = new ArrayList<>();
        final Set<Colony> touchedColonies = Collections.newSetFromMap(new IdentityHashMap<>());
        boolean commanderCached;
        boolean hasCommander;
        boolean captainCached;
        boolean hasCaptain;
    }

    private static List<AntRole> buildSplittableMilitaryRoles() {
        List<AntRole> roles = new ArrayList<>();
        for (AntRole role : GameConstants.getBorderBattleRoles()) {
            roles.add(role);
        }
        if (!roles.contains(GameConstants.ROLE_SIEGE)) {
            roles.add(GameConstants.ROLE_SIEGE);
        }
        return List.copyOf(roles);
    }

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
        return startBorderBattle(war, stageAttacker, stageDefender, 0);
    }

    public static WarBattleState startBorderBattle(War war, Dynasty stageAttacker, Dynasty stageDefender,
            int worldDay) {
        if (war == null || stageAttacker == null || stageDefender == null) {
            return null;
        }
        if (WAR_BATTLES.containsKey(war.getId())) {
            clear(war);
        }
        int attackerFronts = countBattlesInvolving(stageAttacker) + 1;
        int defenderFronts = countBattlesInvolving(stageDefender) + 1;
        WarBattleSideState atk = buildBorderSide(stageAttacker, false, attackerFronts);
        WarBattleSideState def = buildBorderSide(stageDefender, false, defenderFronts);
        WarBattleState state = new WarBattleState(atk, def, false, war.getContestedColonyId());
        state.setWorldDay(worldDay);
        initializeInitialPhase(state);
        WarBattleState previous = WAR_BATTLES.put(war.getId(), state);
        if (previous != null) {
            unregisterSideAnts(previous, previous.getAttacker());
            unregisterSideAnts(previous, previous.getDefender());
        }
        registerSideAnts(state, atk);
        registerSideAnts(state, def);
        syncDeployedCounts(war, state);
        return state;
    }

    public static WarBattleState startHexBattle(War war, Dynasty stageAttacker, Colony contested) {
        return startHexBattle(war, stageAttacker, contested, 0);
    }

    public static WarBattleState startHexBattle(War war, Dynasty stageAttacker, Colony contested, int worldDay) {
        if (war == null || stageAttacker == null || contested == null || contested.getDynasty() == null) {
            return null;
        }
        if (WAR_BATTLES.containsKey(war.getId())) {
            clear(war);
        }
        int attackerFronts = countBattlesInvolving(stageAttacker) + 1;
        int defenderFronts = countBattlesInvolving(contested.getDynasty()) + 1;
        WarBattleSideState atk = buildHexAttackerSide(stageAttacker, attackerFronts);
        WarBattleSideState def = buildHexDefenderSide(contested, defenderFronts);
        WarCombatSkillService.armShieldingForEligibleDefenders(contested);
        WarBattleState state = new WarBattleState(atk, def, true, contested.getId());
        state.setWorldDay(worldDay);
        initializeInitialPhase(state);
        WarBattleState previous = WAR_BATTLES.put(war.getId(), state);
        if (previous != null) {
            unregisterSideAnts(previous, previous.getAttacker());
            unregisterSideAnts(previous, previous.getDefender());
        }
        registerSideAnts(state, atk);
        registerSideAnts(state, def);
        syncDeployedCounts(war, state);
        return state;
    }

    public static TickOutcome tick(War war) {
        return tick(war, 0);
    }

    public static TickOutcome tick(War war, int worldDay) {
        if (war == null) {
            return TickOutcome.CONTINUE;
        }
        WarBattleState state = WAR_BATTLES.get(war.getId());
        if (state == null) {
            return TickOutcome.CONTINUE;
        }
        state.setWorldDay(worldDay);
        state.advanceTick();

        TickContext ctx = new TickContext();
        int startPhase = state.getNextPhaseIndex();
        for (int offset = 0; offset < 6; offset++) {
            int phaseIdx = (startPhase + offset) % 6;
            boolean attackerActing = (phaseIdx % 2) == 0;
            BattleLine line = phaseLineForIndex(phaseIdx);
            WarBattleSideState acting = attackerActing ? state.getAttacker() : state.getDefender();
            WarBattleSideState opposing = attackerActing ? state.getDefender() : state.getAttacker();
            if (canPhaseAct(acting, opposing, line, ctx)) {
                state.setCurrentPhaseAttacker(attackerActing);
                state.setCurrentPhaseLine(line);
                state.setNextPhaseIndex((phaseIdx + 1) % 6);
                resolveLinePhase(state, attackerActing, line, ctx);
                break;
            }
        }

        if (!ctx.touchedColonies.isEmpty()) {
            Set<Dynasty> touchedDynasties = Collections.newSetFromMap(new IdentityHashMap<>());
            for (Colony touched : ctx.touchedColonies) {
                ColonyMilitaryService.refreshColonyMilitaryPower(touched);
                if (touched.getDynasty() != null) {
                    touchedDynasties.add(touched.getDynasty());
                }
            }
            for (Dynasty d : touchedDynasties) {
                ColonyMilitaryService.refreshDynastyMilitaryPower(d);
            }
        }

        removeDeadParticipants(state, state.getAttacker());
        removeDeadParticipants(state, state.getDefender());
        TickOutcome outcome = evaluateWinner(state);
        if (outcome == TickOutcome.CONTINUE) {
            promoteReserves(state.getAttacker(), worldDay);
            promoteReserves(state.getDefender(), worldDay);
        }
        syncDeployedCounts(war, state);
        updateStageCasualtyProgress(war, state);

        return outcome;
    }

    private static void initializeInitialPhase(WarBattleState state) {
        if (state == null) {
            return;
        }
        for (int phaseIdx = 0; phaseIdx < 6; phaseIdx++) {
            boolean attackerActing = (phaseIdx % 2) == 0;
            BattleLine line = phaseLineForIndex(phaseIdx);
            WarBattleSideState acting = attackerActing ? state.getAttacker() : state.getDefender();
            WarBattleSideState opposing = attackerActing ? state.getDefender() : state.getAttacker();
            if (canPhaseAct(acting, opposing, line)) {
                state.setCurrentPhaseAttacker(attackerActing);
                state.setCurrentPhaseLine(line);
                state.setNextPhaseIndex(phaseIdx);
                return;
            }
        }
        state.setCurrentPhaseAttacker(true);
        state.setCurrentPhaseLine(GameConstants.BATTLE_LINE_INFANTRY);
        state.setNextPhaseIndex(0);
    }

    private static BattleLine phaseLineForIndex(int phaseIdx) {
        return switch (phaseIdx / 2) {
            case 0 -> GameConstants.BATTLE_LINE_INFANTRY;
            case 1 -> GameConstants.BATTLE_LINE_ARTILLERY;
            default -> GameConstants.BATTLE_LINE_AIR_SUPPORT;
        };
    }

    private static boolean sideCanAttack(WarBattleSideState acting, WarBattleSideState opposing) {
        for (BattleLine line : GameConstants.getBattleLines()) {
            if (canPhaseAct(acting, opposing, line)) {
                return true;
            }
        }
        return false;
    }

    private static List<Skill> cachedAvailableSkills(Ant ant, Colony colony, TickContext ctx) {
        if (ant == null) {
            return List.of();
        }
        if (ctx == null) {
            return CritterSkillService.resolveAvailableSkills(ant, colony);
        }
        Dynasty dynasty = colony != null ? colony.getDynasty() : null;
        int ownerKey = dynasty != null
                ? dynasty.getId()
                : (ant.getSpecies() != null ? -(ant.getSpecies().getId() + 1) : Integer.MIN_VALUE);
        AntRole role = ant.getRole();
        int roleId = role != null ? role.getId() : -1;
        AntModProfile profile = ant.getModProfile();
        int modCode = profile != null ? profile.getCode() : AntModProfile.STANDARD_CODE;
        SkillCacheKey key = new SkillCacheKey(ownerKey, roleId, modCode);
        List<Skill> cached = ctx.skillCache.get(key);
        if (cached != null) {
            return cached;
        }
        List<Skill> resolved = CritterSkillService.resolveAvailableSkills(ant, colony);
        ctx.skillCache.put(key, resolved);
        return resolved;
    }

    private static boolean hasCombatEligibleTarget(WarBattleSideState enemy) {
        return enemy != null && enemy.hasAnyLivingActive();
    }

    private static boolean canPhaseAct(WarBattleSideState acting, WarBattleSideState opposing, BattleLine line) {
        return canPhaseAct(acting, opposing, line, null);
    }

    private static boolean canPhaseAct(WarBattleSideState acting, WarBattleSideState opposing, BattleLine line,
            TickContext ctx) {
        if (acting == null || opposing == null || line == null || !hasCombatEligibleTarget(opposing)) {
            return false;
        }
        for (WarBattleParticipant p : acting.getActive(line)) {
            if (p == null || !p.isAlive() || p.getAnt() == null) {
                continue;
            }
            Ant ant = p.getAnt();
            Colony colony = resolveColony(p, acting);
            List<Skill> skills = cachedAvailableSkills(ant, colony, ctx);
            boolean hasActionable = false;
            for (Skill candidate : skills) {
                if (candidate != null && !candidate.isPassive()) {
                    hasActionable = true;
                    if (candidate.isAttack()) {
                        return true;
                    }
                }
            }
            if (!hasActionable) {
                return true;
            }
        }
        return false;
    }

    private static Colony resolveColony(WarBattleParticipant participant, WarBattleSideState side) {
        if (participant != null && participant.getColony() != null) {
            return participant.getColony();
        }
        if (participant == null || participant.getAnt() == null || side == null) {
            return null;
        }
        return findColonyOf(participant.getAnt(), side.getDynasty());
    }

    private static void removeDeadParticipants(WarBattleState state, WarBattleSideState side) {
        if (side == null) {
            return;
        }
        synchronized (side) {
            for (BattleLine line : GameConstants.getBattleLines()) {
                List<WarBattleParticipant> active = side.getActive(line);
                for (int i = active.size() - 1; i >= 0; i--) {
                    WarBattleParticipant p = active.get(i);
                    if (p == null || !p.isAlive()) {
                        active.remove(i);
                        if (p != null) {
                            unregisterAnt(p.getAnt(), state);
                        }
                    }
                }
                List<WarBattleParticipant> reserve = side.getReserve(line);
                for (int i = reserve.size() - 1; i >= 0; i--) {
                    WarBattleParticipant p = reserve.get(i);
                    if (p == null || !p.isAlive()) {
                        reserve.remove(i);
                        if (p != null) {
                            unregisterAnt(p.getAnt(), state);
                        }
                    }
                }
            }
        }
    }

    public static void clear(War war) {
        if (war == null) {
            return;
        }
        WarBattleState state = WAR_BATTLES.remove(war.getId());
        if (state != null) {
            unregisterSideAnts(state, state.getAttacker());
            unregisterSideAnts(state, state.getDefender());
            restorePeaceRoles(state.getAttacker());
            restorePeaceRoles(state.getDefender());
        }
    }

    public static void clearAll() {
        synchronized (COMMIT_LOCK) {
            WAR_BATTLES.clear();
            COMMITTED_ANT_STATES.clear();
        }
    }

    private static void registerSideAnts(WarBattleState state, WarBattleSideState side) {
        if (state == null || side == null) {
            return;
        }
        synchronized (COMMIT_LOCK) {
            for (BattleLine line : GameConstants.getBattleLines()) {
                for (WarBattleParticipant p : side.getActive(line)) {
                    if (p != null && p.getAnt() != null) {
                        COMMITTED_ANT_STATES.put(p.getAnt(), state);
                    }
                }
                for (WarBattleParticipant p : side.getReserve(line)) {
                    if (p != null && p.getAnt() != null) {
                        COMMITTED_ANT_STATES.put(p.getAnt(), state);
                    }
                }
            }
        }
    }

    private static void unregisterSideAnts(WarBattleState state, WarBattleSideState side) {
        if (side == null) {
            return;
        }
        synchronized (COMMIT_LOCK) {
            for (BattleLine line : GameConstants.getBattleLines()) {
                for (WarBattleParticipant p : side.getActive(line)) {
                    if (p != null && p.getAnt() != null) {
                        WarBattleState owner = COMMITTED_ANT_STATES.get(p.getAnt());
                        if (owner == null || owner == state) {
                            COMMITTED_ANT_STATES.remove(p.getAnt());
                        }
                    }
                }
                for (WarBattleParticipant p : side.getReserve(line)) {
                    if (p != null && p.getAnt() != null) {
                        WarBattleState owner = COMMITTED_ANT_STATES.get(p.getAnt());
                        if (owner == null || owner == state) {
                            COMMITTED_ANT_STATES.remove(p.getAnt());
                        }
                    }
                }
            }
        }
    }

    private static void unregisterAnt(Ant ant, WarBattleState expectedState) {
        if (ant == null) {
            return;
        }
        synchronized (COMMIT_LOCK) {
            if (expectedState == null || COMMITTED_ANT_STATES.get(ant) == expectedState) {
                COMMITTED_ANT_STATES.remove(ant);
            }
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
        synchronized (COMMIT_LOCK) {
            return COMMITTED_ANT_STATES.containsKey(ant);
        }
    }

    private static TickOutcome evaluateWinner(WarBattleState state) {
        if (state.isHexAssault()) {
            if (!state.getDefender().hasLivingQueen() || state.getDefender().livingActiveSize() <= 0) {
                return TickOutcome.ATTACKER_WINS;
            }
            if (state.getAttacker().livingActiveSize() <= 0) {
                return TickOutcome.DEFENDER_WINS;
            }
        } else {
            if (isArmyDefeated(state.getAttacker())) {
                return TickOutcome.DEFENDER_WINS;
            }
            if (isArmyDefeated(state.getDefender())) {
                return TickOutcome.ATTACKER_WINS;
            }
        }
        boolean atkCanAttack = sideCanAttack(state.getAttacker(), state.getDefender());
        boolean defCanAttack = sideCanAttack(state.getDefender(), state.getAttacker());
        if (!atkCanAttack && !defCanAttack) {
            if (state.isHexAssault()) {
                return state.getDefender().hasLivingQueen() ? TickOutcome.DEFENDER_WINS : TickOutcome.ATTACKER_WINS;
            }
            return state.getAttacker().livingActiveSize() > state.getDefender().livingActiveSize()
                    ? TickOutcome.ATTACKER_WINS
                    : TickOutcome.DEFENDER_WINS;
        }
        if (!atkCanAttack) {
            return TickOutcome.DEFENDER_WINS;
        }
        if (!defCanAttack) {
            return TickOutcome.ATTACKER_WINS;
        }
        return TickOutcome.CONTINUE;
    }

    private static boolean isArmyDefeated(WarBattleSideState side) {
        if (side == null || side.livingActiveSize() <= 0) {
            return true;
        }
        if (side.getStartingArmySize() <= 0) {
            return false;
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

    private static void resolveLinePhase(WarBattleState state, boolean attackerActing, BattleLine line,
            TickContext ctx) {
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
                performAction(state, actor, acting, opposing, attackerActing, ctx);
            }
        }
    }

    private static void performAction(WarBattleState state, WarBattleParticipant actor, WarBattleSideState actingSide,
            WarBattleSideState opposingSide, boolean actorIsAttacker, TickContext ctx) {
        Ant ant = actor.getAnt();
        Colony colony = resolveColony(actor, actingSide);
        List<Skill> skills = cachedAvailableSkills(ant, colony, ctx);
        boolean enemyHasLiving = hasCombatEligibleTarget(opposingSide);
        List<Skill> attackWithTargets = new ArrayList<>();
        List<Skill> supportSkills = new ArrayList<>();
        boolean hasAnyNonPassive = false;
        for (Skill candidate : skills) {
            if (candidate == null || candidate.isPassive()) {
                continue;
            }
            hasAnyNonPassive = true;
            if (!candidate.isAttack()) {
                supportSkills.add(candidate);
            } else if (enemyHasLiving) {
                attackWithTargets.add(candidate);
            }
        }
        if (!hasAnyNonPassive && enemyHasLiving) {
            attackWithTargets.add(GameConstants.SKILL_BASIC_BITE);
        }
        if (attackWithTargets.isEmpty()) {
            return;
        }
        int totalChoices = attackWithTargets.size() + supportSkills.size();
        int choiceIdx = GameRandom.nextInt(totalChoices);
        Skill skill = choiceIdx < attackWithTargets.size()
                ? attackWithTargets.get(choiceIdx)
                : supportSkills.get(choiceIdx - attackWithTargets.size());
        if (!skill.isAttack()) {
            useSupportSkill(ant, colony, skill);
            return;
        }
        int targetCount = Math.max(1, skill.getTargetCount());
        actor.getFocusTargets().clear();
        ensureFocusTargets(actor, opposingSide, actingSide.getDynasty(), skill, targetCount, ctx);
        List<WarBattleParticipant> targets = actor.getFocusTargets();
        for (int tIdx = 0; tIdx < targets.size(); tIdx++) {
            WarBattleParticipant target = targets.get(tIdx);
            if (!target.isAlive()) {
                continue;
            }
            int unEvadedTarget = computeD6Target(state, skill, actor, null, actingSide);
            int d6Target = computeD6Target(state, skill, actor, target, actingSide);
            int d6Roll = GameRandom.nextInt(6) + 1;
            if (d6Roll < d6Target) {
                boolean dodged = d6Roll >= unEvadedTarget;
                String skillName = ctx.skillNameCache.computeIfAbsent(skill, Skill::getName);
                state.getNumericFeed().recordD6Roll(
                        state.getTickIndex(), actorIsAttacker, d6Roll, d6Target, dodged, 0f, false,
                        skillName, skill.getIcon(), actor.getBattleLine());
                continue;
            }
            applyDamage(state, actor, target, opposingSide, skill, actorIsAttacker, d6Target, d6Roll, ctx);
        }
        if (skill.sacrificesSelf() && actor.isAlive()) {
            AntRole sacrificedRole = actor.getAnt().getRole();
            killParticipant(state, actor, actingSide, ctx.touchedColonies);
            if (sacrificedRole == GameConstants.ROLE_COMMANDER) {
                ctx.commanderCached = false;
            } else if (sacrificedRole == GameConstants.ROLE_CAPTAIN) {
                ctx.captainCached = false;
            }
        }
    }

    private static void useSupportSkill(Ant ant, Colony colony, Skill skill) {
        if (skill == GameConstants.SKILL_BOOST_REGEN) {
            WarCombatSkillService.useBoostRegen(ant, colony);
        } else if (skill == GameConstants.SKILL_SHIELDING) {
            WarCombatSkillService.useShielding(ant, colony);
        }
    }

    private static int computeD6Target(WarBattleState state, Skill skill, WarBattleParticipant actor,
            WarBattleParticipant target, WarBattleSideState actingSide) {
        int baseTarget = skill != null ? skill.getD6Target() : 1;
        float accBonus = AntModService.combinedAccuracyBonus(actor.getAnt().getModProfile());
        float lineAcc = actor.getBattleLine().getBaseAccuracyPercent() / 100f;
        boolean riverPenalty = false;
        if (state != null && state.isRiverBattle()) {
            Dynasty d = actingSide != null ? actingSide.getDynasty() : null;
            riverPenalty = HexWaterCrossing.waterCrossRange(d) <= 0;
        }
        float targetEvasion = target != null && target.getAnt() != null
                ? target.getAnt().getEvasionChance()
                : 0f;
        return GameNumbers.computeD6HitTarget(baseTarget, accBonus, lineAcc, riverPenalty, targetEvasion);
    }

    private static void applyDamage(WarBattleState state, WarBattleParticipant attacker,
            WarBattleParticipant target, WarBattleSideState targetSide, Skill skill, boolean attackerIsAttackerSide,
            int d6Target, int d6Roll, TickContext ctx) {
        Ant atkAnt = attacker.getAnt();
        Ant defAnt = target.getAnt();
        float attackStat = atkAnt.getAttack();
        float defenseStat = defAnt.getDefense();
        if (state.isHexAssault()) {
            attackStat = WarCombatSkillService.effectiveHexDefenseAttack(atkAnt, attackerIsAttackerSide);
            defenseStat = WarCombatSkillService.effectiveHexDefenseDefense(defAnt, !attackerIsAttackerSide);
        }
        float damageMult = CritterSkillService.resolveDamageMult(skill, atkAnt.getModProfile());
        WarBattleSideState actingSide = attackerIsAttackerSide ? state.getAttacker() : state.getDefender();
        if (jumpingBoostsMelee(actingSide.getDynasty(), skill)) {
            damageMult *= GameNumbers.ASSIMILATED_JUMPING_MELEE_DAMAGE_MULT;
        }
        float raw = damageMult * attackStat * laneDamageMultiplier(actingSide, attacker.getBattleLine(), ctx);
        float dealt = GameNumbers.damageAfterDefense(raw, defenseStat);
        String skillName = ctx.skillNameCache.computeIfAbsent(skill, Skill::getName);
        if (dealt <= 0f) {
            state.getNumericFeed().recordD6Roll(
                    state.getTickIndex(), attackerIsAttackerSide, d6Roll, d6Target, false, 0f, false,
                    skillName, skill.getIcon(), attacker.getBattleLine());
            return;
        }
        boolean killed = target.applyBattleDamage(dealt) <= 0f;
        if (killed) {
            killParticipant(state, target, targetSide, ctx.touchedColonies);
        }
        state.getNumericFeed().recordD6Roll(
                state.getTickIndex(), attackerIsAttackerSide, d6Roll, d6Target, false, dealt, killed,
                skillName, skill.getIcon(), attacker.getBattleLine());
    }

    private static float laneDamageMultiplier(WarBattleSideState side, BattleLine line, TickContext ctx) {
        if (side == null || line == null) {
            return 1f;
        }
        float mult = 1f;
        if (line == GameConstants.BATTLE_LINE_ARTILLERY) {
            boolean hasCommander;
            if (ctx != null) {
                if (!ctx.commanderCached) {
                    ctx.hasCommander = side.hasLivingWithRole(GameConstants.ROLE_COMMANDER);
                    ctx.commanderCached = true;
                }
                hasCommander = ctx.hasCommander;
            } else {
                hasCommander = side.hasLivingWithRole(GameConstants.ROLE_COMMANDER);
            }
            if (hasCommander) {
                mult += GameNumbers.COMMANDER_ARTILLERY_DAMAGE_BONUS;
            }
        }
        if (line == GameConstants.BATTLE_LINE_INFANTRY) {
            boolean hasCaptain;
            if (ctx != null) {
                if (!ctx.captainCached) {
                    ctx.hasCaptain = side.hasLivingWithRole(GameConstants.ROLE_CAPTAIN);
                    ctx.captainCached = true;
                }
                hasCaptain = ctx.hasCaptain;
            } else {
                hasCaptain = side.hasLivingWithRole(GameConstants.ROLE_CAPTAIN);
            }
            if (hasCaptain) {
                mult += GameNumbers.CAPTAIN_INFANTRY_DAMAGE_BONUS;
            }
        }
        return mult;
    }

    private static void killParticipant(WarBattleState state, WarBattleParticipant victim, WarBattleSideState side,
            Set<Colony> touchedColonies) {
        if (victim == null || victim.getAnt() == null) {
            return;
        }
        Ant ant = victim.getAnt();
        if (!ant.isAlive() && ant.getAntClass() == GameConstants.CLASS_DEAD) {
            return;
        }
        ant.setHealth(0);
        Colony colony = resolveColony(victim, side);
        AntClass originalClass = ant.getAntClass();
        AntRole role = ant.getRole();
        if (colony != null) {
            ant.goDie(colony, DeathCause.CONFLICT);
            colony.recordAntDeath(ant, DeathCause.CONFLICT);
            if (role != null && role.isActiveMilitary()) {
                int count = colony.getWarAssignedRoleCount(role);
                colony.setWarAssignedRoleCount(role, Math.max(0, count - 1));
            }
            removeAntFromLists(colony, ant, originalClass);
            if (touchedColonies != null) {
                touchedColonies.add(colony);
            } else {
                ColonyMilitaryService.refreshColonyMilitaryPower(colony);
            }
        } else {
            ant.goDie();
        }
        side.incrementDead();
        unregisterAnt(ant, state);
        clearFocusOn(state, victim);
        detachAntFromOtherBattles(ant, state);
    }

    private static void detachAntFromOtherBattles(Ant ant, WarBattleState except) {
        if (ant == null) {
            return;
        }
        WarBattleState other;
        synchronized (COMMIT_LOCK) {
            other = COMMITTED_ANT_STATES.get(ant);
        }
        if (other == null || other == except) {
            return;
        }
        removeAntFromSide(other.getAttacker(), ant);
        removeAntFromSide(other.getDefender(), ant);
        clearFocusOnAnt(other, ant);
        unregisterAnt(ant, other);
    }

    private static void removeAntFromSide(WarBattleSideState side, Ant ant) {
        if (side == null || ant == null) {
            return;
        }
        synchronized (side) {
            for (BattleLine line : GameConstants.getBattleLines()) {
                side.getActive(line).removeIf(p -> p != null && p.getAnt() == ant);
                side.getReserve(line).removeIf(p -> p != null && p.getAnt() == ant);
            }
        }
    }

    private static void clearFocusOnAnt(WarBattleState state, Ant ant) {
        if (state == null || ant == null) {
            return;
        }
        clearFocusOnAntFromSide(state.getAttacker(), ant);
        clearFocusOnAntFromSide(state.getDefender(), ant);
    }

    private static void clearFocusOnAntFromSide(WarBattleSideState side, Ant ant) {
        if (side == null) {
            return;
        }
        for (BattleLine line : GameConstants.getBattleLines()) {
            for (WarBattleParticipant p : side.getActive(line)) {
                if (p != null && p.isAlive() && !p.getFocusTargets().isEmpty()) {
                    p.getFocusTargets().removeIf(t -> t != null && t.getAnt() == ant);
                }
            }
        }
    }

    private static void clearFocusOn(WarBattleState state, WarBattleParticipant dead) {
        if (state == null || dead == null) {
            return;
        }
        clearFocusOnFromSide(state.getAttacker(), dead);
        clearFocusOnFromSide(state.getDefender(), dead);
    }

    private static void clearFocusOnFromSide(WarBattleSideState side, WarBattleParticipant dead) {
        if (side == null) {
            return;
        }
        for (BattleLine line : GameConstants.getBattleLines()) {
            for (WarBattleParticipant p : side.getActive(line)) {
                if (p != null && p.isAlive() && !p.getFocusTargets().isEmpty()) {
                    p.getFocusTargets().remove(dead);
                }
            }
        }
    }

    private static void ensureFocusTargets(WarBattleParticipant actor, WarBattleSideState enemy, Dynasty actingDynasty,
            Skill skill, int desired, TickContext ctx) {
        List<WarBattleParticipant> focus = actor.getFocusTargets();
        focus.removeIf(t -> t == null || !t.isAlive());
        while (focus.size() < desired) {
            WarBattleParticipant next = pickTarget(actor, enemy, actingDynasty, skill, focus, ctx);
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
            Dynasty actingDynasty, Skill skill, List<WarBattleParticipant> exclude, TickContext ctx) {
        List<WarBattleParticipant> pool = ctx != null ? ctx.poolScratch : new ArrayList<>();
        pool.clear();
        fillCombatEligibleTargets(actor.getBattleLine(), enemy, actingDynasty, skill, pool);
        pool.removeIf(exclude::contains);
        if (pool.isEmpty()) {
            return null;
        }
        boolean hasLivingDefenders = enemy.hasLivingDefenders();
        boolean hasDefender = false;
        if (!hasLivingDefenders) {
            for (int i = 0; i < pool.size(); i++) {
                if (pool.get(i).isDefenderRole()) {
                    hasDefender = true;
                    break;
                }
            }
        }
        if (hasLivingDefenders || hasDefender) {
            List<WarBattleParticipant> nonQueens = ctx != null ? ctx.nonQueenScratch : new ArrayList<>();
            nonQueens.clear();
            for (int i = 0; i < pool.size(); i++) {
                WarBattleParticipant p = pool.get(i);
                if (!p.isQueen()) {
                    nonQueens.add(p);
                }
            }
            if (!nonQueens.isEmpty()) {
                pool = nonQueens;
            }
        }
        return pool.get(GameRandom.nextInt(pool.size()));
    }

    static List<WarBattleParticipant> eligibleTargetsForSkill(BattleLine attackerLine, WarBattleSideState enemy,
            Dynasty actingDynasty, Skill skill) {
        List<WarBattleParticipant> pool = new ArrayList<>();
        fillEligibleTargets(attackerLine, enemy, actingDynasty, skill, pool);
        return pool;
    }

    private static void fillCombatEligibleTargets(BattleLine attackerLine, WarBattleSideState enemy,
            Dynasty actingDynasty, Skill skill, List<WarBattleParticipant> pool) {
        fillEligibleTargets(attackerLine, enemy, actingDynasty, skill, pool);
        if (!pool.isEmpty() || enemy == null) {
            return;
        }
        addLiving(pool, enemy.getActive(GameConstants.BATTLE_LINE_INFANTRY));
        if (pool.isEmpty()) {
            addLiving(pool, enemy.getActive(GameConstants.BATTLE_LINE_ARTILLERY));
        }
        if (pool.isEmpty()) {
            addLiving(pool, enemy.getActive(GameConstants.BATTLE_LINE_AIR_SUPPORT));
        }
    }

    private static void fillEligibleTargets(BattleLine attackerLine, WarBattleSideState enemy,
            Dynasty actingDynasty, Skill skill, List<WarBattleParticipant> pool) {
        if (enemy == null) {
            return;
        }
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

    private static void promoteReserves(WarBattleSideState side, int worldDay) {
        if (side == null || side.getDynasty() == null) {
            return;
        }
        synchronized (side) {
            side.resetReinforcementDayIfNeeded(worldDay);
            Dynasty dynasty = side.getDynasty();
            int capacity = Math.max(1, dynasty.getCombatCapacity());
            int frontCount = Math.max(1, countBattlesInvolving(dynasty));
            int carrierShare = fairShare(totalWarQuota(dynasty, GameConstants.ROLE_CARRIER), frontCount);
            int allowancePerLine = GameNumbers.warDailyReinforcementAllowancePerLine(capacity, carrierShare);
            for (BattleLine line : GameConstants.getBattleLines()) {
                List<WarBattleParticipant> active = side.getActive(line);
                List<WarBattleParticipant> reserve = side.getReserve(line);
                for (int i = active.size() - 1; i >= 0; i--) {
                    WarBattleParticipant p = active.get(i);
                    if (p == null || !p.isAlive()) {
                        active.remove(i);
                        if (p != null) {
                            unregisterAnt(p.getAnt(), null);
                        }
                    }
                }
                for (int i = reserve.size() - 1; i >= 0; i--) {
                    WarBattleParticipant p = reserve.get(i);
                    if (p == null || !p.isAlive()) {
                        reserve.remove(i);
                        if (p != null) {
                            unregisterAnt(p.getAnt(), null);
                        }
                    }
                }
                while (active.size() < capacity
                        && side.getReinforcementPromotionsUsed(line) < allowancePerLine) {
                    WarBattleParticipant next = pollNextReinforcement(reserve);
                    if (next == null) {
                        break;
                    }
                    active.add(next);
                    side.recordReinforcementPromotion(line);
                }
            }
        }
    }

    private static WarBattleParticipant pollNextReinforcement(List<WarBattleParticipant> reserve) {
        for (int i = 0; i < reserve.size(); i++) {
            WarBattleParticipant candidate = reserve.get(i);
            if (isCarrierParticipant(candidate)) {
                continue;
            }
            return reserve.remove(i);
        }
        return null;
    }

    private static boolean isCarrierParticipant(WarBattleParticipant participant) {
        return participant != null
                && participant.getAnt() != null
                && participant.getAnt().getRole() == GameConstants.ROLE_CARRIER;
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
        claimDynastyRoleQuota(dynasty, GameConstants.ROLE_CARRIER, pool, seen, fronts);
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
                trimSideToFairShares(state, side, dynasty, frontCount);
                side.setStartingArmySize(side.livingArmySize() + side.getDeadCount());
                promoteReserves(side, battleWorldDay(state));
            }
        }
    }

    private static int battleWorldDay(WarBattleState state) {
        return state != null ? state.getWorldDay() : 0;
    }

    private static void trimSideToFairShares(WarBattleState state, WarBattleSideState side, Dynasty dynasty,
            int frontCount) {
        Map<AntRole, List<WarBattleParticipant>> reserveByRole = new HashMap<>();
        Map<AntRole, List<WarBattleParticipant>> activeNonQueenByRole = new HashMap<>();
        for (BattleLine line : GameConstants.getBattleLines()) {
            for (WarBattleParticipant p : side.getReserve(line)) {
                if (p != null && p.isAlive() && p.getAnt() != null && p.getAnt().getRole() != null) {
                    reserveByRole.computeIfAbsent(p.getAnt().getRole(), ignored -> new ArrayList<>()).add(p);
                }
            }
            for (WarBattleParticipant p : side.getActive(line)) {
                if (p != null && p.isAlive() && p.getAnt() != null && !p.isQueen() && p.getAnt().getRole() != null) {
                    activeNonQueenByRole.computeIfAbsent(p.getAnt().getRole(), ignored -> new ArrayList<>()).add(p);
                }
            }
        }
        Set<WarBattleParticipant> toRelease = Collections.newSetFromMap(new IdentityHashMap<>());
        for (AntRole role : SPLITTABLE_MILITARY_ROLES) {
            List<WarBattleParticipant> resList = reserveByRole.get(role);
            List<WarBattleParticipant> actList = activeNonQueenByRole.get(role);
            int total = (resList != null ? resList.size() : 0) + (actList != null ? actList.size() : 0);
            int share = fairShare(totalWarQuota(dynasty, role), frontCount);
            int toDrop = total - share;
            if (toDrop <= 0) {
                continue;
            }
            if (resList != null) {
                for (int i = resList.size() - 1; i >= 0 && toDrop > 0; i--, toDrop--) {
                    toRelease.add(resList.get(i));
                }
            }
            if (actList != null && toDrop > 0) {
                for (int i = actList.size() - 1; i >= 0 && toDrop > 0; i--, toDrop--) {
                    toRelease.add(actList.get(i));
                }
            }
        }
        if (toRelease.isEmpty()) {
            return;
        }
        for (BattleLine line : GameConstants.getBattleLines()) {
            side.getActive(line).removeIf(toRelease::contains);
            side.getReserve(line).removeIf(toRelease::contains);
        }
        for (WarBattleParticipant released : toRelease) {
            unregisterAnt(released.getAnt(), state);
            restorePeaceRoleIfUncommitted(released);
        }
    }

    private static int claimRoleQuota(Colony colony, AntRole role, List<WarBattleParticipant> pool,
            Map<Ant, Boolean> seen, int maxToClaim) {
        if (colony == null || role == null || role.getAntClass() == null || maxToClaim <= 0) {
            return 0;
        }
        int quota = Math.min(maxToClaim, colony.getWarAssignedRoleCount(role));
        if (quota <= 0) {
            return 0;
        }
        List<Ant> candidates = antsOfClass(colony, role.getAntClass());
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
        claimed += takeClaims(colony, preferred, role, pool, seen, quota - claimed);
        claimed += takeClaims(colony, fallback, role, pool, seen, quota - claimed);
        return claimed;
    }

    private static int takeClaims(Colony colony, List<Ant> ants, AntRole role, List<WarBattleParticipant> pool,
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
            pool.add(new WarBattleParticipant(ant, colony, line, previous, override));
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
            pool.add(new WarBattleParticipant(ant, colony, WarBattleSideState.lineForAnt(ant), ant.getRole(), false));
        }
    }

    private static List<Ant> antsOfClass(Colony colony, AntClass type) {
        List<Ant> ants = new ArrayList<>();
        if (colony == null || type == null) {
            return ants;
        }
        if (type == GameConstants.CLASS_WORKER) {
            addAll(ants, colony.getWorkers());
        } else if (type == GameConstants.CLASS_SOLDIER) {
            addAll(ants, colony.getSoldiers());
        } else if (type == GameConstants.CLASS_MAJOR) {
            addAll(ants, colony.getMajors());
        } else if (type == GameConstants.CLASS_PRINCESS) {
            addAll(ants, colony.getPrincesses());
        } else if (type == GameConstants.CLASS_QUEEN) {
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

    private static void removeAntFromLists(Colony colony, Ant ant, AntClass originalClass) {
        if (colony == null || ant == null) {
            return;
        }
        List<Ant> primary = originalClass != null ? colony.getAntsByClass(originalClass) : null;
        if (primary != null && primary.remove(ant)) {
            return;
        }
        if (colony.getWorkers() != null && colony.getWorkers().remove(ant)) {
            return;
        }
        if (colony.getSoldiers() != null && colony.getSoldiers().remove(ant)) {
            return;
        }
        if (colony.getMajors() != null && colony.getMajors().remove(ant)) {
            return;
        }
        if (colony.getPrincesses() != null && colony.getPrincesses().remove(ant)) {
            return;
        }
        if (colony.getQueens() != null) {
            colony.getQueens().remove(ant);
        }
    }
}
