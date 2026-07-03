package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

/**
 * Colony military power from adult ant type counts (roles ignored) scaled by combat base stats.
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

    public static int computeMilitaryPower(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int typePoints = computeTypePoints(
                sizeOf(colony.getWorkers()),
                sizeOf(colony.getSoldiers()),
                sizeOf(colony.getMajors()),
                sizeOf(colony.getPrincesses()),
                sizeOf(colony.getQueens()));
        return Math.round(typePoints * computeStatMultiplier(colony));
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
        colony.setMilitaryPower(computeMilitaryPower(colony));
    }

    public static void refreshDynastyMilitaryPower(Dynasty dynasty) {
        if (dynasty == null) {
            return;
        }
        int total = 0;
        for (Colony colony : dynasty.getColonies()) {
            total += colony.getMilitaryPower();
        }
        dynasty.setMilitaryPower(total);
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

    private static int sizeOf(java.util.List<?> list) {
        return list != null ? list.size() : 0;
    }
}
