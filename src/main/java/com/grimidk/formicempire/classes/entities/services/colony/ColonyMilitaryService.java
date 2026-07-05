package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

import java.util.Map;

/**
 * Colony military power from adult ant type counts scaled by combat base stats.
 * During war, war-role assignments split power into active (front-line roles) and reserve (remaining adults).
 * Refreshed at most once per colony per world day ({@link Colony#runDailyJobs}).
 */
public final class ColonyMilitaryService {

    private ColonyMilitaryService() {
    }

    public static int computeTypePoints(int workers, int soldiers, int majors, int princesses, int queens) {
        return workers * GameConstants.MILITARY_WEIGHT_WORKER
                + soldiers * GameConstants.MILITARY_WEIGHT_SOLDIER
                + majors * GameConstants.MILITARY_WEIGHT_MAJOR
                + princesses * GameConstants.MILITARY_WEIGHT_PRINCESS
                + queens * GameConstants.MILITARY_WEIGHT_QUEEN;
    }

    public static float computeStatMultiplier(boolean hasSkeleton, boolean hasAcid) {
        int hp = hasSkeleton ? GameConstants.MILITARY_BASELINE_HEALTH : 0;
        int def = hasSkeleton ? GameConstants.MILITARY_BASELINE_DEFENSE : 0;
        int atk = hasAcid ? GameConstants.MILITARY_BASELINE_ATTACK : 0;
        int atkSpd = hasAcid ? GameConstants.MILITARY_BASELINE_ATTACK_SPEED : 0;
        return computeStatMultiplierFromBases(hp, atk, def, atkSpd);
    }

    public static float computeStatMultiplier(Colony colony) {
        if (colony == null || colony.getStatsService() == null) {
            return 0f;
        }
        ColonyStatsService stats = colony.getStatsService();
        return computeStatMultiplierFromBases(
                stats.getBaseHealth(colony),
                stats.getBaseAttack(colony),
                stats.getBaseDefense(colony),
                stats.getBaseAttackSpeed(colony));
    }

    public static float computeStatMultiplierFromBases(int baseHealth, int baseAttack, int baseDefense, int baseAttackSpeed) {
        float hpFactor = baseHealth / (float) GameConstants.MILITARY_BASELINE_HEALTH;
        float atkFactor = baseAttack / (float) GameConstants.MILITARY_BASELINE_ATTACK;
        float defFactor = baseDefense / (float) GameConstants.MILITARY_BASELINE_DEFENSE;
        float spdFactor = baseAttackSpeed / (float) GameConstants.MILITARY_BASELINE_ATTACK_SPEED;
        return (hpFactor + atkFactor + defFactor + spdFactor) / 4f;
    }

    /** Floor for battle view scaling when combat upgrades are missing. */
    public static float effectiveStatMultiplier(Colony colony) {
        return Math.max(0.01f, computeStatMultiplier(colony));
    }

    /** War campaign power uses a higher floor so stages are not resolved in a single hour. */
    public static float warStandingStatMultiplier(Colony colony) {
        return Math.max(0.35f, computeStatMultiplier(colony));
    }

    /**
     * Maps stronger:weaker military power ratio to a delta in {@code [0, MILITARY_STRENGTH_DELTA_MAX]}.
     * Ratio {@code <= 1} yields 0; ratio {@code >= MILITARY_STRENGTH_RATIO_MAX} yields max delta.
     */
    public static int getMilitaryStrengthDelta(int strongerPower, int weakerPower) {
        if (strongerPower <= 0 || weakerPower <= 0 || strongerPower <= weakerPower) {
            return 0;
        }
        float ratio = strongerPower / (float) weakerPower;
        if (ratio <= 1f) {
            return 0;
        }
        float normalized = (ratio - 1f) / (GameConstants.MILITARY_STRENGTH_RATIO_MAX - 1f);
        return Math.min(GameConstants.MILITARY_STRENGTH_DELTA_MAX,
                Math.max(0, Math.round(normalized * GameConstants.MILITARY_STRENGTH_DELTA_MAX)));
    }

    /** Signed adjustment from this dynasty's perspective toward {@code other} (negative if stronger). */
    public static int getMilitaryReputationAdjustment(int viewerPower, int otherPower) {
        if (viewerPower > otherPower) {
            return -getMilitaryStrengthDelta(viewerPower, otherPower);
        }
        if (otherPower > viewerPower) {
            return getMilitaryStrengthDelta(otherPower, viewerPower);
        }
        return 0;
    }

    /** Signed loyalty adjustment vs dynasty capital (negative if this colony is stronger). */
    public static int getMilitaryLoyaltyAdjustment(int colonyPower, int capitalPower, boolean isCapital) {
        if (isCapital || capitalPower <= 0) {
            return 0;
        }
        if (colonyPower > capitalPower) {
            return -getMilitaryStrengthDelta(colonyPower, capitalPower);
        }
        if (capitalPower > colonyPower) {
            return getMilitaryStrengthDelta(capitalPower, colonyPower);
        }
        return 0;
    }

    /**
     * Daily AI war probability toward a bordering target. Returns 0 when the target exceeds
     * {@link GameConstants#AI_DECLARE_WAR_MAX_TARGET_STRENGTH_RATIO} times the AI's military power.
     * Chance rises as effective reputation falls and as the AI becomes relatively weaker (up to that cap).
     */
    public static double computeAiWarDeclarationChance(int aiPower, int otherPower, int effectiveReputation) {
        if (otherPower <= 0 || aiPower <= 0) {
            return 0;
        }
        float maxRatio = GameConstants.AI_DECLARE_WAR_MAX_TARGET_STRENGTH_RATIO;
        if (otherPower > aiPower * maxRatio) {
            return 0;
        }

        double strengthRatio = Math.min(maxRatio, otherPower / (double) aiPower);
        double weaknessUrgency = strengthRatio <= 1.0
                ? 0.0
                : (strengthRatio - 1.0) / (maxRatio - 1.0);

        int neutralMin = GameConstants.REPUTATION_NEUTRAL.getMinScore();
        double repPressure;
        if (effectiveReputation >= neutralMin) {
            repPressure = 0.35 * weaknessUrgency;
        } else {
            repPressure = 0.35 + 0.65 * ((neutralMin - effectiveReputation) / (double) neutralMin);
        }
        if (repPressure <= 0) {
            return 0;
        }

        return GameConstants.AI_DECLARE_WAR_CHANCE
                * repPressure
                * (0.40 + 0.60 * weaknessUrgency);
    }

    public static int computeMilitaryPower(Colony colony) {
        if (colony == null) {
            return 0;
        }
        Dynasty dynasty = colony.getDynasty();
        if (dynasty != null && dynasty.isAtWar()) {
            return computeActiveMilitaryPower(colony) + computeReserveMilitaryPower(colony);
        }
        int typePoints = computeTypePoints(
                sizeOf(colony.getWorkers()),
                sizeOf(colony.getSoldiers()),
                sizeOf(colony.getMajors()),
                sizeOf(colony.getPrincesses()),
                sizeOf(colony.getQueens()));
        return Math.round(typePoints * computeStatMultiplier(colony));
    }

    public static int computeActiveMilitaryPower(Colony colony) {
        if (colony == null) {
            return 0;
        }
        Dynasty dynasty = colony.getDynasty();
        if (dynasty != null && dynasty.isAtWar()) {
            return computeActiveMilitaryPowerFromWarCounts(colony, colony.getWarAssignedRoleCounts());
        }
        return 0;
    }

    public static int computeActiveMilitaryPowerFromWarCounts(Colony colony, Map<AntRole, Integer> warCounts) {
        if (colony == null || warCounts == null) {
            return 0;
        }
        int points = 0;
        for (AntRole role : GameConstants.getActiveMilitaryRoles()) {
            int count = warCounts.getOrDefault(role, 0);
            if (count > 0) {
                points += count * GameConstants.getActiveMilitaryRoleWeight(role);
            }
        }
        return Math.round(points * warStandingStatMultiplier(colony));
    }

    public static int computeReserveMilitaryPower(Colony colony) {
        if (colony == null) {
            return 0;
        }
        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null || !dynasty.isAtWar()) {
            return computeMilitaryPower(colony);
        }

        int workers = sizeOf(colony.getWorkers());
        int soldiers = sizeOf(colony.getSoldiers());
        int majors = sizeOf(colony.getMajors());
        int princesses = sizeOf(colony.getPrincesses());
        int queens = sizeOf(colony.getQueens());

        Map<AntRole, Integer> warCounts = colony.getWarAssignedRoleCounts();
        int militia = warCounts.getOrDefault(GameConstants.ROLE_MILITIA, 0);
        int activeSoldiers = sumActiveRoleCountsForType(warCounts, GameConstants.TYPE_SOLDIER);
        int activeMajors = sumActiveRoleCountsForType(warCounts, GameConstants.TYPE_MAJOR);

        int reserveWorkers = Math.max(0, workers - militia);
        int reserveSoldiers = Math.max(0, soldiers - activeSoldiers);
        int reserveMajors = Math.max(0, majors - activeMajors);

        int points = computeTypePoints(reserveWorkers, reserveSoldiers, reserveMajors, princesses, queens);
        return Math.round(points * warStandingStatMultiplier(colony));
    }

    /** Recompute from persisted colony counts and dynasty combat upgrades (for saves / inactive colonies). */
    public static int computeFromSavedColony(Savefile.SavedColony savedColony, Dynasty dynasty) {
        if (savedColony == null) {
            return 0;
        }
        boolean hasSkeleton = dynasty != null && dynasty.hasUpgrade(GameUnlocks.STAT_SKELETON);
        boolean hasAcid = dynasty != null && dynasty.hasUpgrade(GameUnlocks.STAT_ACID);
        int typePoints = computeTypePoints(
                savedColony.workers,
                savedColony.soldiers,
                savedColony.majors,
                savedColony.princesses,
                savedColony.queens);
        return Math.round(typePoints * computeStatMultiplier(hasSkeleton, hasAcid));
    }

    public static void refreshColonyMilitaryPower(Colony colony) {
        if (colony == null) {
            return;
        }
        int active = computeActiveMilitaryPower(colony);
        int reserve = computeReserveMilitaryPower(colony);
        colony.setActiveMilitaryPower(active);
        colony.setReserveMilitaryPower(reserve);
        colony.setMilitaryPower(active + reserve);
    }

    public static void refreshDynastyMilitaryPower(Dynasty dynasty) {
        if (dynasty == null) {
            return;
        }
        int total = 0;
        int activeTotal = 0;
        int reserveTotal = 0;
        for (Colony colony : dynasty.getColonies()) {
            total += colony.getMilitaryPower();
            activeTotal += colony.getActiveMilitaryPower();
            reserveTotal += colony.getReserveMilitaryPower();
        }
        dynasty.setMilitaryPower(total);
        dynasty.setActiveMilitaryPower(activeTotal);
        dynasty.setReserveMilitaryPower(reserveTotal);
    }

    public static void refreshAllMilitaryPower(Iterable<Dynasty> dynasties) {
        if (dynasties == null) {
            return;
        }
        for (Dynasty dynasty : dynasties) {
            for (Colony colony : dynasty.getColonies()) {
                refreshColonyMilitaryPower(colony);
            }
            refreshDynastyMilitaryPower(dynasty);
        }
    }

    public static int powerForWarStanding(Dynasty dynasty) {
        if (dynasty == null) {
            return 0;
        }
        return dynasty.isAtWar() ? dynasty.getActiveMilitaryPower() : dynasty.getMilitaryPower();
    }

    private static int sumActiveRoleCountsForType(Map<AntRole, Integer> warCounts, AntType type) {
        int sum = 0;
        for (AntRole role : GameConstants.getActiveMilitaryRoles()) {
            if (role.getAntType() == type) {
                sum += warCounts.getOrDefault(role, 0);
            }
        }
        return sum;
    }

    private static int sizeOf(java.util.List<?> list) {
        return list != null ? list.size() : 0;
    }
}
