package com.grimidk.formicempire.classes.entities.services.colony;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.grimidk.formicempire.classes.constants.critter.BugRole;
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
                int d6Target = computeHunterD6Target(skill, ant);
                int d6Roll = GameRandom.nextInt(6) + 1;
                if (d6Roll < d6Target) {
                    state.getNumericFeed().recordD6Roll(
                            state.getTickIndex(), true, d6Roll, d6Target, false, 0f, false,
                            skill.getName(), skill.getIcon(), null);
                    continue;
                }
                float damageMult = CritterSkillService.resolveDamageMult(skill, ant.getModProfile());
                float raw = damageMult * ant.getAttack();
                float dealt = GameNumbers.damageAfterDefense(raw, state.getBugDefense());
                state.applyBugDamage(dealt);
                state.getNumericFeed().recordD6Roll(
                        state.getTickIndex(), true, d6Roll, d6Target, false, dealt, !state.isBugAlive(),
                        skill.getName(), skill.getIcon(), null);
            }
        }
    }

    private static void resolveBugPhase(HuntBattleState state) {
        List<HuntBattleParticipant> living = state.livingFrontlineHunters();
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
                int unEvadedTarget = computeBugD6Target(skill, null);
                int d6Target = computeBugD6Target(skill, target);
                int d6Roll = GameRandom.nextInt(6) + 1;
                if (d6Roll < d6Target) {
                    boolean dodged = d6Roll >= unEvadedTarget;
                    state.getNumericFeed().recordD6Roll(
                            state.getTickIndex(), false, d6Roll, d6Target, dodged, 0f, false,
                            skill.getName(), skill.getIcon(), null);
                    continue;
                }
                float damageMult = skill.getDamageMult();
                float raw = damageMult * state.getBugAttack();
                Ant ant = target.getAnt();
                float dealt = GameNumbers.damageAfterDefense(raw, ant.getDefense());
                boolean killed = target.applyBattleDamage(dealt) <= 0f;
                if (killed) {
                    killHunter(state, target);
                }
                state.getNumericFeed().recordD6Roll(
                        state.getTickIndex(), false, d6Roll, d6Target, false, dealt, killed,
                        skill.getName(), skill.getIcon(), null);
            }
            state.rotateFocusTarget();
            living = state.livingFrontlineHunters();
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

    private static int computeHunterD6Target(Skill skill, Ant ant) {
        int baseTarget = skill != null ? skill.getD6Target() : 1;
        float accBonus = AntModService.combinedAccuracyBonus(ant.getModProfile());
        return GameNumbers.computeD6HitTarget(baseTarget, accBonus, 1f, false, 0f);
    }

    private static int computeBugD6Target(Skill skill, HuntBattleParticipant target) {
        int baseTarget = skill != null ? skill.getD6Target() : 1;
        float targetEvasion = target != null && target.getAnt() != null
                ? target.getAnt().getEvasionChance()
                : 0f;
        return GameNumbers.computeD6HitTarget(baseTarget, 0f, 1f, false, targetEvasion);
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
        if (species != null && species.hasBugRole(BugRole.INVASION)) {
            return GameNumbers.invasionEnemyMaxHealth(species);
        }
        if (species != null && species.hasBugRole(BugRole.HUNT)) {
            return GameNumbers.huntEnemyMaxHealth(species);
        }
        return GameNumbers.MILITARY_BASELINE_HEALTH;
    }

    public static float resolveBugAttack(Species species) {
        if (species != null && species.hasBugRole(BugRole.INVASION)) {
            return GameNumbers.invasionEnemyAttack(species);
        }
        if (species != null && species.hasBugRole(BugRole.HUNT)) {
            return GameNumbers.huntEnemyAttack(species);
        }
        return GameNumbers.MILITARY_BASELINE_ATTACK;
    }

    public static float resolveBugDefense(Species species) {
        if (species != null && species.hasBugRole(BugRole.INVASION)) {
            return GameNumbers.invasionEnemyDefense(species);
        }
        if (species != null && species.hasBugRole(BugRole.HUNT)) {
            return GameNumbers.huntEnemyDefense(species);
        }
        return GameNumbers.MILITARY_BASELINE_DEFENSE;
    }

    public static int resolveBugAttackSpeed(Species species) {
        if (species != null && species.hasBugRole(BugRole.INVASION)) {
            return GameNumbers.invasionEnemyAttackSpeed(species);
        }
        if (species != null && species.hasBugRole(BugRole.HUNT)) {
            return GameNumbers.huntEnemyAttackSpeed(species);
        }
        return GameNumbers.MILITARY_BASELINE_ATTACK_SPEED;
    }

    public static float estimateWinChance(Species species, List<Ant> party, Colony colony) {
        if (species == null || party == null || party.isEmpty()) {
            return 0f;
        }
        List<Ant> living = new ArrayList<>(party.size());
        for (Ant ant : party) {
            if (ant != null && ant.isAlive()) {
                living.add(ant);
            }
        }
        int n = living.size();
        if (n == 0) {
            return 0f;
        }

        float bugMaxHp = Math.max(1f, resolveBugMaxHealth(species));
        float bugAtk = Math.max(1f, resolveBugAttack(species));
        float bugDef = resolveBugDefense(species);
        int bugSpd = Math.max(1, resolveBugAttackSpeed(species));
        List<Skill> bugSkills = pickBugAttackSkills(species);
        int bugSkillCount = bugSkills.size();
        int[] bugBaseD6 = new int[bugSkillCount];
        float[] bugRawDmg = new float[bugSkillCount];
        int[] bugTargets = new int[bugSkillCount];
        for (int i = 0; i < bugSkillCount; i++) {
            Skill s = bugSkills.get(i);
            bugBaseD6[i] = s.getD6Target();
            bugRawDmg[i] = s.getDamageMult() * bugAtk;
            bugTargets[i] = Math.max(1, s.getTargetCount());
        }

        float[] baseHp = new float[n];
        float[] defPct = new float[n];
        boolean[] hasEvasion = new boolean[n];
        boolean[] isFrontline = new boolean[n];
        int[] actions = new int[n];
        int[][] hunterD6 = new int[n][];
        float[][] hunterDmg = new float[n][];
        double initTotalHp = 0.0;
        double totalAtk = 0.0;

        for (int i = 0; i < n; i++) {
            Ant ant = living.get(i);
            float maxHp = ant.getMaxHealth();
            float currentHp = ant.getHealth();
            if (maxHp <= 0f) {
                maxHp = GameNumbers.MILITARY_BASELINE_HEALTH;
                currentHp = maxHp;
            } else if (currentHp <= 0f) {
                currentHp = maxHp;
            }
            baseHp[i] = Math.min(currentHp, maxHp);
            defPct[i] = ant.getDefense();
            hasEvasion[i] = ant.getEvasionChance() >= 10f;
            isFrontline[i] = HuntBattleState.resolveBattleLine(ant) == GameConstants.BATTLE_LINE_INFANTRY;
            actions[i] = Math.max(1, Math.round(ant.getAttackSpeed()));

            float atk = Math.max(1f, ant.getAttack());
            initTotalHp += baseHp[i];
            totalAtk += atk;

            List<Skill> available = CritterSkillService.resolveAvailableSkills(ant, colony);
            List<Skill> attacks = new ArrayList<>();
            for (Skill sk : available) {
                if (sk != null && sk.isAttack() && !sk.isPassive()) {
                    attacks.add(sk);
                }
            }
            if (attacks.isEmpty()) {
                attacks.add(GameConstants.SKILL_BASIC_BITE);
            }
            int sc = attacks.size();
            hunterD6[i] = new int[sc];
            hunterDmg[i] = new float[sc];
            float accBonus = AntModService.combinedAccuracyBonus(ant.getModProfile());
            for (int k = 0; k < sc; k++) {
                Skill sk = attacks.get(k);
                hunterD6[i][k] = GameNumbers.computeD6HitTarget(sk.getD6Target(), accBonus, 1f, false, 0f);
                float dmgMult = CritterSkillService.resolveDamageMult(sk, ant.getModProfile());
                hunterDmg[i][k] = GameNumbers.damageAfterDefense(dmgMult * atk, bugDef);
            }
        }

        int unlockedSkillCount = colony != null && colony.getDynasty() != null
                ? colony.getDynasty().getUnlockedSkills().size()
                : 0;
        long seed = 1469598103934665603L;
        seed = (seed * 1099511628211L) ^ species.getId();
        seed = (seed * 1099511628211L) ^ n;
        seed = (seed * 1099511628211L) ^ Math.round(initTotalHp);
        seed = (seed * 1099511628211L) ^ Math.round(totalAtk);
        seed = (seed * 1099511628211L) ^ unlockedSkillCount;
        Random rng = new Random(seed);

        int trials = n > 250 ? 20 : 40;
        int wins = 0;
        double marginSum = 0.0;
        float[] hp = new float[n];
        int[] aliveIdx = new int[n];
        int[] targetableIdx = new int[n];
        int[] pool = new int[n];
        int[] scratch = new int[n];

        for (int trial = 0; trial < trials; trial++) {
            System.arraycopy(baseHp, 0, hp, 0, n);
            for (int i = 0; i < n; i++) {
                aliveIdx[i] = i;
            }
            int aliveCount = n;
            float bugHp = bugMaxHp;
            int focus = 0;
            int ticks = 0;

            while (bugHp > 0f && aliveCount > 0 && ticks < 400) {
                ticks++;
                for (int a = 0; a < aliveCount && bugHp > 0f; a++) {
                    int idx = aliveIdx[a];
                    int act = actions[idx];
                    int[] d6s = hunterD6[idx];
                    float[] dmgs = hunterDmg[idx];
                    int sc = d6s.length;
                    for (int k = 0; k < act && bugHp > 0f; k++) {
                        int pick = sc == 1 ? 0 : rng.nextInt(sc);
                        int roll = rng.nextInt(6) + 1;
                        if (roll >= d6s[pick]) {
                            bugHp -= dmgs[pick];
                        }
                    }
                }
                if (bugHp <= 0f) {
                    break;
                }

                for (int act = 0; act < bugSpd && aliveCount > 0; act++) {
                    int targetableCount = 0;
                    for (int i = 0; i < aliveCount; i++) {
                        int id = aliveIdx[i];
                        if (isFrontline[id]) {
                            targetableIdx[targetableCount++] = id;
                        }
                    }
                    if (targetableCount == 0) {
                        System.arraycopy(aliveIdx, 0, targetableIdx, 0, aliveCount);
                        targetableCount = aliveCount;
                    }

                    int skIdx = bugSkillCount == 1 ? 0 : rng.nextInt(bugSkillCount);
                    int targetCount = Math.min(bugTargets[skIdx], targetableCount);
                    int focusPos = focus % targetableCount;

                    int chosenCount = 0;
                    pool[chosenCount++] = targetableIdx[focusPos];
                    if (targetCount > 1) {
                        int remaining = 0;
                        for (int p = 0; p < targetableCount; p++) {
                            if (p != focusPos) {
                                scratch[remaining++] = targetableIdx[p];
                            }
                        }
                        for (int t = 1; t < targetCount && remaining > 0; t++) {
                            int r = rng.nextInt(remaining);
                            pool[chosenCount++] = scratch[r];
                            scratch[r] = scratch[--remaining];
                        }
                    }

                    int bD6 = bugBaseD6[skIdx];
                    float raw = bugRawDmg[skIdx];
                    boolean anyKilled = false;
                    for (int c = 0; c < chosenCount; c++) {
                        int antIdx = pool[c];
                        int targetD6 = hasEvasion[antIdx] ? Math.min(6, bD6 + 1) : bD6;
                        int roll = rng.nextInt(6) + 1;
                        if (roll >= targetD6) {
                            float dealt = GameNumbers.damageAfterDefense(raw, defPct[antIdx]);
                            hp[antIdx] -= dealt;
                            if (hp[antIdx] <= 0f) {
                                hp[antIdx] = 0f;
                                anyKilled = true;
                            }
                        }
                    }
                    if (anyKilled) {
                        int write = 0;
                        for (int i = 0; i < aliveCount; i++) {
                            int id = aliveIdx[i];
                            if (hp[id] > 0f) {
                                aliveIdx[write++] = id;
                            }
                        }
                        aliveCount = write;
                    }
                    focus++;
                }
            }

            if (bugHp <= 0f) {
                wins++;
                double remHp = 0.0;
                for (int i = 0; i < aliveCount; i++) {
                    remHp += hp[aliveIdx[i]];
                }
                marginSum += remHp / Math.max(1.0, initTotalHp);
            } else {
                marginSum -= Math.max(0f, bugHp) / Math.max(1.0, bugMaxHp);
            }
        }

        double emp = wins / (double) trials;
        double avgMargin = marginSum / trials;
        double logistic = 1.0 / (1.0 + Math.exp(-7.0 * avgMargin));
        double est;
        if (wins == 0) {
            est = Math.min(0.45, logistic);
        } else if (wins == trials) {
            est = Math.max(0.55, logistic);
        } else {
            est = 0.65 * emp + 0.35 * logistic;
        }
        return (float) Math.max(0.001, Math.min(0.999, est));
    }
}

