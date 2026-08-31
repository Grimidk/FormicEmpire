package com.grimidk.formicempire.classes.entities.services.colony;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.grimidk.formicempire.classes.constants.critter.Skill;
import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.hunt.KnownHuntTarget;
import com.grimidk.formicempire.classes.entities.invasion.InvasionAlert;
import com.grimidk.formicempire.classes.entities.services.shared.CritterSkillService;
import com.grimidk.formicempire.classes.infrasctructure.registries.DeathCause;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

public final class HuntCreatureCombatService {

    public enum TickOutcome {
        CONTINUE,
        HUNTERS_WIN,
        BUG_WINS
    }

    private static final Map<Long, HuntBattleState> COLONY_BATTLES = new ConcurrentHashMap<>();

    private HuntCreatureCombatService() {
    }

    private static long battleKey(int colonyId, int contextId) {
        return ((long) colonyId << 32) | (contextId & 0xFFFFFFFFL);
    }

    static int invasionContextId(int alertId) {
        return -Math.max(1, alertId);
    }

    public static HuntBattleState getState(Colony colony, int contextId) {
        return colony != null ? COLONY_BATTLES.get(battleKey(colony.getId(), contextId)) : null;
    }

    public static HuntBattleState getInvasionState(Colony colony, int alertId) {
        return getState(colony, invasionContextId(alertId));
    }

    public static HuntBattleState startBattle(Colony colony, KnownHuntTarget target, List<Ant> party) {
        if (colony == null || target == null || party == null || party.isEmpty()) {
            return null;
        }
        Species species = target.getSpecies();
        if (species == null) {
            return null;
        }
        List<HuntBattleParticipant> hunters = new ArrayList<>();
        for (Ant ant : party) {
            if (ant != null && ant.isAlive()) {
                hunters.add(new HuntBattleParticipant(ant));
            }
        }
        if (hunters.isEmpty()) {
            return null;
        }
        HuntBattleState state = new HuntBattleState(
                colony,
                target.getId(),
                species,
                hunters,
                resolveBugMaxHealth(species),
                resolveBugAttack(species),
                resolveBugDefense(species),
                resolveBugAttackSpeed(species));
        COLONY_BATTLES.put(battleKey(colony.getId(), target.getId()), state);
        return state;
    }

    public static HuntBattleState startInvasionBattle(Colony colony, InvasionAlert alert, List<Ant> party) {
        if (colony == null || alert == null || party == null || party.isEmpty()) {
            return null;
        }
        Species species = alert.getSpecies();
        if (species == null) {
            species = GameConstants.TYPE_ANT_LION;
        }
        List<HuntBattleParticipant> defenders = new ArrayList<>();
        for (Ant ant : party) {
            if (ant != null && ant.isAlive()) {
                defenders.add(new HuntBattleParticipant(ant));
            }
        }
        if (defenders.isEmpty()) {
            return null;
        }
        int contextId = invasionContextId(alert.getId());
        HuntBattleState state = new HuntBattleState(
                colony,
                contextId,
                species,
                defenders,
                resolveBugMaxHealth(species),
                resolveBugAttack(species),
                resolveBugDefense(species),
                resolveBugAttackSpeed(species));
        COLONY_BATTLES.put(battleKey(colony.getId(), contextId), state);
        return state;
    }

    public static HuntBattleState restoreBattle(Colony colony, KnownHuntTarget target,
            List<HuntBattleParticipant> hunters, float bugHealth, int tickIndex, int focusTargetIndex) {
        if (colony == null || target == null || hunters == null || hunters.isEmpty()) {
            return null;
        }
        Species species = target.getSpecies();
        if (species == null) {
            return null;
        }
        float bugMaxHealth = resolveBugMaxHealth(species);
        HuntBattleState state = HuntBattleState.restore(
                colony,
                target.getId(),
                species,
                hunters,
                bugHealth,
                bugMaxHealth,
                resolveBugAttack(species),
                resolveBugDefense(species),
                resolveBugAttackSpeed(species),
                tickIndex,
                focusTargetIndex);
        COLONY_BATTLES.put(battleKey(colony.getId(), target.getId()), state);
        return state;
    }

    public static HuntBattleState restoreInvasionBattle(Colony colony, InvasionAlert alert,
            List<HuntBattleParticipant> defenders, float bugHealth, int tickIndex, int focusTargetIndex) {
        if (colony == null || alert == null || defenders == null || defenders.isEmpty()) {
            return null;
        }
        Species species = alert.getSpecies();
        if (species == null) {
            species = GameConstants.TYPE_ANT_LION;
        }
        float bugMaxHealth = resolveBugMaxHealth(species);
        int contextId = invasionContextId(alert.getId());
        HuntBattleState state = HuntBattleState.restore(
                colony,
                contextId,
                species,
                defenders,
                bugHealth,
                bugMaxHealth,
                resolveBugAttack(species),
                resolveBugDefense(species),
                resolveBugAttackSpeed(species),
                tickIndex,
                focusTargetIndex);
        COLONY_BATTLES.put(battleKey(colony.getId(), contextId), state);
        return state;
    }

    public static void clear(Colony colony, int contextId) {
        if (colony != null) {
            COLONY_BATTLES.remove(battleKey(colony.getId(), contextId));
        }
    }

    public static void clearInvasion(Colony colony, int alertId) {
        clear(colony, invasionContextId(alertId));
    }

    public static void clear(Colony colony) {
        if (colony == null) {
            return;
        }
        int colonyId = colony.getId();
        COLONY_BATTLES.keySet().removeIf(key -> (int) (key >> 32) == colonyId);
    }

    public static void clearAll() {
        COLONY_BATTLES.clear();
    }

    public static TickOutcome tick(Colony colony, int contextId) {
        HuntBattleState state = getState(colony, contextId);
        if (state == null) {
            return TickOutcome.CONTINUE;
        }
        state.advanceTick();
        resolveHunterPhase(state);
        resolveBugPhase(state);
        if (!state.isBugAlive()) {
            return TickOutcome.HUNTERS_WIN;
        }
        if (state.livingHunters().isEmpty()) {
            return TickOutcome.BUG_WINS;
        }
        return TickOutcome.CONTINUE;
    }

    public static TickOutcome tickInvasion(Colony colony, int alertId) {
        return tick(colony, invasionContextId(alertId));
    }

    private static void resolveHunterPhase(HuntBattleState state) {
        Colony colony = state.getColony();
        for (HuntBattleParticipant hunter : state.getHunters()) {
            if (!hunter.isAlive()) {
                continue;
            }
            Ant ant = hunter.getAnt();
            int actions = Math.max(1, Math.round(ant.getAttackSpeed()));
            for (int i = 0; i < actions; i++) {
                if (!state.isBugAlive()) {
                    break;
                }
                Skill skill = pickHunterAttackSkill(ant, colony);
                if (skill == null || !skill.isAttack()) {
                    skill = GameConstants.SKILL_BASIC_BITE;
                }
                if (!rollHit(skill, ant)) {
                    continue;
                }
                float damageMult = CritterSkillService.resolveDamageMult(skill, ant.getSubtypeProfile());
                float raw = damageMult * ant.getAttack();
                float dealt = GameNumbers.damageAfterDefense(raw, state.getBugDefense());
                state.applyBugDamage(dealt);
            }
        }
    }

    private static void resolveBugPhase(HuntBattleState state) {
        List<HuntBattleParticipant> living = state.livingHunters();
        if (living.isEmpty()) {
            return;
        }
        Species species = state.getSpecies();
        List<Skill> attackSkills = pickBugAttackSkills(species);
        int actions = Math.max(1, state.getBugAttackSpeed());
        for (int action = 0; action < actions; action++) {
            if (living.isEmpty()) {
                break;
            }
            Skill skill = attackSkills.get(GameRandom.nextInt(attackSkills.size()));
            int targetCount = Math.max(1, skill.getTargetCount());
            List<HuntBattleParticipant> targets = pickBugTargets(state, living, targetCount);
            for (HuntBattleParticipant target : targets) {
                if (!target.isAlive()) {
                    continue;
                }
                if (!rollBugHit(skill)) {
                    continue;
                }
                float damageMult = skill.getDamageMult();
                float raw = damageMult * state.getBugAttack();
                Ant ant = target.getAnt();
                float dealt = GameNumbers.damageAfterDefense(raw, ant.getDefense());
                if (target.applyBattleDamage(dealt) <= 0f) {
                    killHunter(state, target);
                }
            }
            state.rotateFocusTarget();
            living = state.livingHunters();
        }
    }

    private static List<Skill> pickBugAttackSkills(Species species) {
        List<Skill> attacks = new ArrayList<>();
        if (species != null) {
            for (Skill skill : species.getBaseSkills()) {
                if (skill != null && skill.isAttack()) {
                    attacks.add(skill);
                }
            }
        }
        if (attacks.isEmpty()) {
            attacks.add(GameConstants.SKILL_BASIC_BITE);
        }
        return attacks;
    }

    private static List<HuntBattleParticipant> pickBugTargets(HuntBattleState state,
            List<HuntBattleParticipant> living, int targetCount) {
        if (living.isEmpty()) {
            return List.of();
        }
        List<HuntBattleParticipant> pool = new ArrayList<>(living);
        List<HuntBattleParticipant> picked = new ArrayList<>();
        HuntBattleParticipant focus = state.getFocusTarget();
        if (focus != null && focus.isAlive() && pool.remove(focus)) {
            picked.add(focus);
        }
        int count = Math.min(targetCount, pool.size() + picked.size());
        while (picked.size() < count && !pool.isEmpty()) {
            int index = GameRandom.nextInt(pool.size());
            picked.add(pool.remove(index));
        }
        return picked;
    }

    private static Skill pickHunterAttackSkill(Ant ant, Colony colony) {
        List<Skill> skills = CritterSkillService.resolveAvailableSkills(ant, colony);
        List<Skill> attacks = new ArrayList<>();
        for (Skill skill : skills) {
            if (skill != null && skill.isAttack() && !skill.isPassive()) {
                attacks.add(skill);
            }
        }
        if (attacks.isEmpty()) {
            return GameConstants.SKILL_BASIC_BITE;
        }
        return attacks.get(GameRandom.nextInt(attacks.size()));
    }

    private static boolean rollHit(Skill skill, Ant ant) {
        float skillAcc = CritterSkillService.resolveAccuracyMult(skill, ant.getSubtypeProfile());
        float chance = Math.min(1f, Math.max(0f, skillAcc));
        return GameRandom.nextDouble() < chance;
    }

    private static boolean rollBugHit(Skill skill) {
        float chance = Math.min(1f, Math.max(0f, skill.getAccuracyMult()));
        return GameRandom.nextDouble() < chance;
    }

    private static void killHunter(HuntBattleState state, HuntBattleParticipant victim) {
        if (victim == null || victim.getAnt() == null) {
            return;
        }
        Ant ant = victim.getAnt();
        float bodyRotation = GameRandom.nextFloat() * 360f;
        state.recordFallenBody(victim, bodyRotation);
        ant.setHealth(0);
        Colony colony = state.getColony();
        if (colony != null) {
            ant.goDie(colony, DeathCause.CONFLICT);
            colony.recordAntDeath(ant, DeathCause.CONFLICT);
            removeAntFromColonyLists(colony, ant);
        } else {
            ant.goDie();
        }
    }

    private static void removeAntFromColonyLists(Colony colony, Ant ant) {
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
    }

    public static float resolveBugMaxHealth(Species species) {
        if (species == GameConstants.TYPE_COCKROACH) {
            return GameNumbers.HUNT_COCKROACH_HP;
        }
        if (species == GameConstants.TYPE_ANT_LION) {
            return GameNumbers.INVASION_ANT_LION_HP;
        }
        return GameNumbers.MILITARY_BASELINE_HEALTH;
    }

    public static float resolveBugAttack(Species species) {
        if (species == GameConstants.TYPE_COCKROACH) {
            return GameNumbers.HUNT_COCKROACH_ATTACK;
        }
        if (species == GameConstants.TYPE_ANT_LION) {
            return GameNumbers.INVASION_ANT_LION_ATTACK;
        }
        return GameNumbers.MILITARY_BASELINE_ATTACK;
    }

    public static float resolveBugDefense(Species species) {
        if (species == GameConstants.TYPE_COCKROACH) {
            return GameNumbers.HUNT_COCKROACH_DEFENSE;
        }
        if (species == GameConstants.TYPE_ANT_LION) {
            return GameNumbers.INVASION_ANT_LION_DEFENSE;
        }
        return GameNumbers.MILITARY_BASELINE_DEFENSE;
    }

    public static int resolveBugAttackSpeed(Species species) {
        if (species == GameConstants.TYPE_COCKROACH) {
            return GameNumbers.HUNT_COCKROACH_ATTACK_SPEED;
        }
        if (species == GameConstants.TYPE_ANT_LION) {
            return GameNumbers.INVASION_ANT_LION_ATTACK_SPEED;
        }
        return 1;
    }
}

