package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

import java.util.List;
import java.util.Map;

public final class ColonyMilitaryService {

    private ColonyMilitaryService() {
    }

    public static int computeTypePoints(int workers, int soldiers, int majors, int princesses, int queens) {
        return workers * GameNumbers.MILITARY_WEIGHT_WORKER
                + soldiers * GameNumbers.MILITARY_WEIGHT_SOLDIER
                + majors * GameNumbers.MILITARY_WEIGHT_MAJOR
                + princesses * GameNumbers.MILITARY_WEIGHT_PRINCESS
                + queens * GameNumbers.MILITARY_WEIGHT_QUEEN;
    }

    public static float computeStatMultiplier(boolean hasSkeleton, boolean hasAcid) {
        int hp = hasSkeleton ? GameNumbers.MILITARY_BASELINE_HEALTH : 0;
        int def = hasSkeleton ? GameNumbers.MILITARY_BASELINE_DEFENSE : 0;
        int atk = hasAcid ? GameNumbers.MILITARY_BASELINE_ATTACK : 0;
        int atkSpd = hasAcid ? GameNumbers.MILITARY_BASELINE_ATTACK_SPEED : 0;
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
        float hpFactor = baseHealth / (float) GameNumbers.MILITARY_BASELINE_HEALTH;
        float atkFactor = baseAttack / (float) GameNumbers.MILITARY_BASELINE_ATTACK;
        float defFactor = baseDefense / (float) GameNumbers.MILITARY_BASELINE_DEFENSE;
        float spdFactor = baseAttackSpeed / (float) GameNumbers.MILITARY_BASELINE_ATTACK_SPEED;
        return (hpFactor + atkFactor + defFactor + spdFactor) / 4f;
    }

    public static float effectiveStatMultiplier(Colony colony) {
        return Math.max(0.01f, computeStatMultiplier(colony));
    }

    public static float warStandingStatMultiplier(Colony colony) {
        return Math.max(0.35f, computeStatMultiplier(colony));
    }

    public static int getMilitaryStrengthDelta(int strongerPower, int weakerPower) {
        if (strongerPower <= 0 || weakerPower <= 0 || strongerPower <= weakerPower) {
            return 0;
        }
        float ratio = strongerPower / (float) weakerPower;
        if (ratio <= 1f) {
            return 0;
        }
        float normalized = (ratio - 1f) / (GameNumbers.MILITARY_STRENGTH_RATIO_MAX - 1f);
        return Math.min(GameNumbers.MILITARY_STRENGTH_DELTA_MAX,
                Math.max(0, Math.round(normalized * GameNumbers.MILITARY_STRENGTH_DELTA_MAX)));
    }

    public static int getMilitaryReputationAdjustment(int viewerPower, int otherPower) {
        if (viewerPower > otherPower) {
            return -getMilitaryStrengthDelta(viewerPower, otherPower);
        }
        if (otherPower > viewerPower) {
            return getMilitaryStrengthDelta(otherPower, viewerPower);
        }
        return 0;
    }

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

    public static double computeAiWarDeclarationChance(int aiPower, int otherPower, int effectiveReputation) {
        if (otherPower <= 0 || aiPower <= 0) {
            return 0;
        }
        float maxRatio = GameNumbers.AI_DECLARE_WAR_MAX_TARGET_STRENGTH_RATIO;
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

        return GameNumbers.AI_DECLARE_WAR_CHANCE
                * repPressure
                * (0.40 + 0.60 * weaknessUrgency);
    }

    public static int computeMilitaryPower(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int total = computeMilitaryPowerFromPopulation(colony);
        Dynasty dynasty = colony.getDynasty();
        if (dynasty != null && dynasty.isAtWar()) {
            int active = computeActiveMilitaryPower(colony);
            return active + Math.max(0, total - active);
        }
        return total;
    }

    public static int computeMilitaryPowerFromPopulation(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int points = 0;
        points += sumMilitaryPointsForSubtypeCounts(colony, GameConstants.TYPE_WORKER,
                AntSubtypeService.aggregateSubtypeCounts(colony.getWorkers()));
        points += sumMilitaryPointsForSubtypeCounts(colony, GameConstants.TYPE_SOLDIER,
                AntSubtypeService.aggregateSubtypeCounts(colony.getSoldiers()));
        points += sumMilitaryPointsForSubtypeCounts(colony, GameConstants.TYPE_MAJOR,
                AntSubtypeService.aggregateSubtypeCounts(colony.getMajors()));
        points += sumMilitaryPointsForSubtypeCounts(colony, GameConstants.TYPE_PRINCESS,
                AntSubtypeService.aggregateSubtypeCounts(colony.getPrincesses()));
        points += sumMilitaryPointsForSubtypeCounts(colony, GameConstants.TYPE_QUEEN,
                AntSubtypeService.aggregateSubtypeCounts(colony.getQueens()));
        if (points > 0) {
            return points;
        }
        int typePoints = computeTypePoints(
                sizeOf(colony.getWorkers()),
                sizeOf(colony.getSoldiers()),
                sizeOf(colony.getMajors()),
                sizeOf(colony.getPrincesses()),
                sizeOf(colony.getQueens()));
        return Math.round(typePoints * computeStatMultiplier(colony));
    }

    private static int sumMilitaryPointsForSubtypeCounts(Colony colony, AntType type, Map<String, Integer> subtypeCounts) {
        return AntSubtypeService.sumMilitaryPointsForSubtypeCounts(colony, type, subtypeCounts);
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
        float colonyMult = computeStatMultiplier(colony);
        float points = 0f;
        int typePoints = 0;
        for (AntRole role : GameConstants.getActiveMilitaryRoles()) {
            int count = warCounts.getOrDefault(role, 0);
            if (count <= 0 || role.getAntType() == null) {
                continue;
            }
            int roleWeight = GameConstants.getActiveMilitaryRoleWeight(role);
            typePoints += count * roleWeight;
            float avgSubtypeFactor = AntSubtypeService.weightedSubtypeCombatFactor(colony,
                    role.getAntType(), AntSubtypeService.aggregateSubtypeCounts(colony.getAntsByType(role.getAntType())));
            points += count * roleWeight * colonyMult * avgSubtypeFactor;
        }
        return withWarStandingFloor(colony, typePoints, Math.round(points));
    }

    public static int computeReserveMilitaryPower(Colony colony) {
        if (colony == null) {
            return 0;
        }
        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null || !dynasty.isAtWar()) {
            return computeMilitaryPowerFromPopulation(colony);
        }
        int total = computeMilitaryPowerFromPopulation(colony);
        int active = computeActiveMilitaryPower(colony);
        return Math.max(0, total - active);
    }

    public static int computeFromSavedColony(Savefile.SavedColony savedColony, Dynasty dynasty) {
        if (savedColony == null) {
            return 0;
        }
        boolean hasSkeleton = dynasty != null && dynasty.hasUpgrade(GameUnlocks.STAT_SKELETON);
        boolean hasAcid = dynasty != null && dynasty.hasUpgrade(GameUnlocks.STAT_ACID);
        int baseHealth = hasSkeleton ? GameNumbers.MILITARY_BASELINE_HEALTH : 0;
        int baseDefense = hasSkeleton ? GameNumbers.MILITARY_BASELINE_DEFENSE : 0;
        int baseAttack = hasAcid
                ? Math.round(GameNumbers.MILITARY_BASELINE_ATTACK * ColonyStatsService.getAssimilatedDamageMultiplier(dynasty))
                : 0;
        int baseAttackSpeed = hasAcid
                ? Math.round(GameNumbers.MILITARY_BASELINE_ATTACK_SPEED
                        * ColonyStatsService.getAssimilatedAttackSpeedMultiplier(dynasty))
                : 0;

        int points = 0;
        points += sumSavedMilitaryPoints(savedColony.workerSubtypes, savedColony.workers, GameConstants.TYPE_WORKER,
                baseHealth, baseAttack, baseDefense, baseAttackSpeed);
        points += sumSavedMilitaryPoints(savedColony.soldierSubtypes, savedColony.soldiers, GameConstants.TYPE_SOLDIER,
                baseHealth, baseAttack, baseDefense, baseAttackSpeed);
        points += sumSavedMilitaryPoints(savedColony.majorSubtypes, savedColony.majors, GameConstants.TYPE_MAJOR,
                baseHealth, baseAttack, baseDefense, baseAttackSpeed);
        points += sumSavedMilitaryPoints(savedColony.princessSubtypes, savedColony.princesses, GameConstants.TYPE_PRINCESS,
                baseHealth, baseAttack, baseDefense, baseAttackSpeed);
        points += sumSavedMilitaryPoints(savedColony.queenSubtypes, savedColony.queens, GameConstants.TYPE_QUEEN,
                baseHealth, baseAttack, baseDefense, baseAttackSpeed);
        if (points > 0) {
            return points;
        }

        int typePoints = computeTypePoints(
                savedColony.workers,
                savedColony.soldiers,
                savedColony.majors,
                savedColony.princesses,
                savedColony.queens);
        return Math.round(typePoints * computeStatMultiplierFromBases(baseHealth, baseAttack, baseDefense, baseAttackSpeed));
    }

    private static int sumSavedMilitaryPoints(Map<String, Integer> subtypeCounts, int legacyCount, AntType type,
            int baseHealth, int baseAttack, int baseDefense, int baseAttackSpeed) {
        int weight = GameConstants.getMilitaryWeightForAntType(type);
        if (weight == 0) {
            return 0;
        }
        float colonyMult = computeStatMultiplierFromBases(baseHealth, baseAttack, baseDefense, baseAttackSpeed);
        if (subtypeCounts != null && !subtypeCounts.isEmpty()) {
            int total = 0;
            for (Map.Entry<String, Integer> entry : subtypeCounts.entrySet()) {
                int count = entry.getValue();
                if (count <= 0) {
                    continue;
                }
                AntSubtypeProfile profile = AntSubtypeProfile.fromCode(Integer.parseInt(entry.getKey()));
                float standardMult = AntSubtypeService.computeCombatStatMultiplierFromBases(
                        type, AntSubtypeProfile.standard(), baseHealth, baseAttack, baseDefense, baseAttackSpeed);
                float actualMult = AntSubtypeService.computeCombatStatMultiplierFromBases(
                        type, profile, baseHealth, baseAttack, baseDefense, baseAttackSpeed);
                float subtypeFactor = standardMult > 0f ? actualMult / standardMult : 1f;
                total += Math.round(count * weight * colonyMult * subtypeFactor);
            }
            return total;
        }
        if (legacyCount <= 0) {
            return 0;
        }
        return Math.round(legacyCount * weight * colonyMult);
    }

    private static int withWarStandingFloor(Colony colony, int typePoints, int perAntPoints) {
        if (perAntPoints > 0) {
            return perAntPoints;
        }
        return Math.round(typePoints * warStandingStatMultiplier(colony));
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

    private static int sizeOf(List<?> list) {
        return list != null ? list.size() : 0;
    }
}
