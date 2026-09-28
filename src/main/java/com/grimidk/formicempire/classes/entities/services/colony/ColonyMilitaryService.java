package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
import com.grimidk.formicempire.classes.constants.critter.ant.AntModProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

import java.util.List;
import java.util.Map;

public final class ColonyMilitaryService {

    private ColonyMilitaryService() {
    }

    public static int computeClassPoints(int workers, int soldiers, int majors, int princesses, int queens) {
        return workers * GameConstants.CLASS_WORKER.getMilitaryWeight()
                + soldiers * GameConstants.CLASS_SOLDIER.getMilitaryWeight()
                + majors * GameConstants.CLASS_MAJOR.getMilitaryWeight()
                + princesses * GameConstants.CLASS_PRINCESS.getMilitaryWeight()
                + queens * GameConstants.CLASS_QUEEN.getMilitaryWeight();
    }

    public static int computeTypePoints(int workers, int soldiers, int majors, int princesses, int queens) {
        return computeClassPoints(workers, soldiers, majors, princesses, queens);
    }

    public static float computeStatMultiplier(boolean hasSkeleton, boolean hasAcid) {
        int hp = hasSkeleton ? GameNumbers.MILITARY_BASELINE_HEALTH : 0;
        int atk = hasAcid ? GameNumbers.MILITARY_BASELINE_ATTACK : 0;
        int atkSpd = hasAcid ? GameNumbers.MILITARY_BASELINE_ATTACK_SPEED : 0;
        return computeColonyStatMultiplierFromBases(hp, atk, atkSpd);
    }

    public static float computeStatMultiplier(Colony colony) {
        if (colony == null || colony.getStatsService() == null) {
            return 0f;
        }
        ColonyStatsService stats = colony.getStatsService();
        return computeColonyStatMultiplierFromBases(
                stats.getBaseHealth(colony),
                stats.getBaseAttack(colony),
                stats.getBaseAttackSpeed(colony));
    }

    public static float computeColonyStatMultiplierFromBases(int baseHealth, int baseAttack, int baseAttackSpeed) {
        float hpFactor = baseHealth / (float) GameNumbers.MILITARY_BASELINE_HEALTH;
        float atkFactor = baseAttack / (float) GameNumbers.MILITARY_BASELINE_ATTACK;
        float spdFactor = baseAttackSpeed / (float) GameNumbers.MILITARY_BASELINE_ATTACK_SPEED;
        return (hpFactor + atkFactor + spdFactor) / 3f;
    }

    public static float computeStatMultiplierFromBases(int baseHealth, int baseAttack, int baseDefense, int baseAttackSpeed) {
        float hpFactor = baseHealth / (float) GameNumbers.MILITARY_BASELINE_HEALTH;
        float atkFactor = baseAttack / (float) GameNumbers.MILITARY_BASELINE_ATTACK;
        float defenseBaseline = GameConstants.CLASS_MAJOR.getDefenseMult();
        float defFactor = defenseBaseline <= 0f ? 0f : baseDefense / defenseBaseline;
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
        points += sumMilitaryPointsForModCounts(colony, GameConstants.CLASS_WORKER,
                AntModService.aggregateModCounts(colony.getWorkers()));
        points += sumMilitaryPointsForModCounts(colony, GameConstants.CLASS_SOLDIER,
                AntModService.aggregateModCounts(colony.getSoldiers()));
        points += sumMilitaryPointsForModCounts(colony, GameConstants.CLASS_MAJOR,
                AntModService.aggregateModCounts(colony.getMajors()));
        points += sumMilitaryPointsForModCounts(colony, GameConstants.CLASS_PRINCESS,
                AntModService.aggregateModCounts(colony.getPrincesses()));
        points += sumMilitaryPointsForModCounts(colony, GameConstants.CLASS_QUEEN,
                AntModService.aggregateModCounts(colony.getQueens()));
        if (points > 0) {
            return points;
        }
        int typePoints = computeClassPoints(
                sizeOf(colony.getWorkers()),
                sizeOf(colony.getSoldiers()),
                sizeOf(colony.getMajors()),
                sizeOf(colony.getPrincesses()),
                sizeOf(colony.getQueens()));
        return Math.round(typePoints * computeStatMultiplier(colony));
    }

    private static int sumMilitaryPointsForModCounts(Colony colony, AntClass antClass, Map<String, Integer> modCounts) {
        return AntModService.sumMilitaryPointsForModCounts(colony, antClass, modCounts);
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
        return computeRolePowerFromWarCounts(colony, warCounts, true, false);
    }

    public static int computeHexDefenseOnlyPower(Colony colony) {
        if (colony == null) {
            return 0;
        }
        Dynasty dynasty = colony.getDynasty();
        if (dynasty != null && dynasty.isAtWar()) {
            return computeRolePowerFromWarCounts(colony, colony.getWarAssignedRoleCounts(), false, true);
        }
        return 0;
    }

    public static int computeHexDefenseOnlyPower(Dynasty dynasty) {
        if (dynasty == null) {
            return 0;
        }
        int total = 0;
        for (Colony colony : dynasty.getColonies()) {
            total += computeHexDefenseOnlyPower(colony);
        }
        return total;
    }

    public static int computeHexDefenseMilitaryPower(Colony colony) {
        if (colony == null) {
            return 0;
        }
        return computeMilitaryPowerFromPopulation(colony);
    }

    public static int computeHexAssaultAttackerPower(Dynasty dynasty) {
        if (dynasty == null) {
            return 0;
        }
        return Math.max(0, powerForWarStanding(dynasty)) + computeSiegeAssaultPower(dynasty);
    }

    public static int computeHexAssaultEffectiveAttackerPower(Dynasty dynasty) {
        if (dynasty == null) {
            return 0;
        }
        int siege = computeSiegeAssaultPower(dynasty);
        int nonSiege = Math.max(0, powerForWarStanding(dynasty));
        return GameNumbers.warHexAssaultEffectiveAttackerPower(nonSiege, siege);
    }

    public static int computeHexDefenseEffectivePower(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int base = computeHexDefenseMilitaryPower(colony);
        int defenderRole = Math.min(base, computeDefenderRolePower(colony));
        int standard = Math.max(0, base - defenderRole);
        return GameNumbers.warHexDefenseEffectiveDefenderPower(standard, defenderRole);
    }

    public static int computeAssignedRolePower(Colony colony, AntRole role) {
        if (colony == null || role == null || !role.isActiveMilitary()) {
            return 0;
        }
        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null || !dynasty.isAtWar()) {
            return 0;
        }
        Map<AntRole, Integer> warCounts = colony.getWarAssignedRoleCounts();
        int count = warCounts.getOrDefault(role, 0);
        if (count <= 0 || role.getAntClass() == null) {
            return 0;
        }
        float colonyMult = computeStatMultiplier(colony);
        int roleWeight = GameConstants.getActiveMilitaryRoleWeight(role);
        int typePoints = count * roleWeight;
        float avgModFactor = AntModService.weightedModCombatFactor(colony,
                role.getAntClass(), AntModService.aggregateModCounts(colony.getAntsByClass(role.getAntClass())));
        float points = count * roleWeight * colonyMult * avgModFactor;
        return withWarStandingFloor(colony, typePoints, Math.round(points));
    }

    public static int computeDefenderRolePower(Colony colony) {
        return computeAssignedRolePower(colony, GameConstants.ROLE_DEFENDER);
    }

    public static int computeSiegeAssaultPower(Dynasty dynasty) {
        if (dynasty == null) {
            return 0;
        }
        int total = 0;
        for (Colony colony : dynasty.getColonies()) {
            total += computeAssignedRolePower(colony, GameConstants.ROLE_SIEGE);
        }
        return total;
    }

    private static int computeRolePowerFromWarCounts(Colony colony, Map<AntRole, Integer> warCounts,
            boolean includeBorder, boolean includeHexDefenseOnly) {
        if (colony == null || warCounts == null) {
            return 0;
        }
        float colonyMult = computeStatMultiplier(colony);
        float points = 0f;
        int typePoints = 0;
        for (AntRole role : GameConstants.getActiveMilitaryRoles()) {
            if (role.isHexDefenseOnly()) {
                if (!includeHexDefenseOnly) {
                    continue;
                }
            } else if (!includeBorder) {
                continue;
            }
            int count = warCounts.getOrDefault(role, 0);
            if (count <= 0 || role.getAntClass() == null) {
                continue;
            }
            int roleWeight = GameConstants.getActiveMilitaryRoleWeight(role);
            typePoints += count * roleWeight;
            float avgModFactor = AntModService.weightedModCombatFactor(colony,
                    role.getAntClass(), AntModService.aggregateModCounts(colony.getAntsByClass(role.getAntClass())));
            points += count * roleWeight * colonyMult * avgModFactor;
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
        int assignedBorder = computeRolePowerFromWarCounts(colony, colony.getWarAssignedRoleCounts(), true, false);
        return Math.max(0, total - assignedBorder);
    }

    public static int computeFromSavedColony(Savefile.SavedColony savedColony, Dynasty dynasty) {
        if (savedColony == null) {
            return 0;
        }
        int baseHealth = ColonyStatsService.resolveBaseHealth(dynasty);
        int baseDefense = ColonyStatsService.resolveBaseDefense(dynasty);
        int baseAttack = ColonyStatsService.resolveBaseAttack(dynasty);
        int baseAttackSpeed = ColonyStatsService.resolveBaseAttackSpeed(dynasty);

        int points = 0;
        points += sumSavedMilitaryPoints(savedColony.workerMods, savedColony.workers, GameConstants.CLASS_WORKER,
                baseHealth, baseAttack, baseDefense, baseAttackSpeed);
        points += sumSavedMilitaryPoints(savedColony.soldierMods, savedColony.soldiers, GameConstants.CLASS_SOLDIER,
                baseHealth, baseAttack, baseDefense, baseAttackSpeed);
        points += sumSavedMilitaryPoints(savedColony.majorMods, savedColony.majors, GameConstants.CLASS_MAJOR,
                baseHealth, baseAttack, baseDefense, baseAttackSpeed);
        points += sumSavedMilitaryPoints(savedColony.princessMods, savedColony.princesses, GameConstants.CLASS_PRINCESS,
                baseHealth, baseAttack, baseDefense, baseAttackSpeed);
        points += sumSavedMilitaryPoints(savedColony.queenMods, savedColony.queens, GameConstants.CLASS_QUEEN,
                baseHealth, baseAttack, baseDefense, baseAttackSpeed);
        if (points > 0) {
            return points;
        }

        int typePoints = computeClassPoints(
                savedColony.workers,
                savedColony.soldiers,
                savedColony.majors,
                savedColony.princesses,
                savedColony.queens);
        return Math.round(typePoints * computeColonyStatMultiplierFromBases(baseHealth, baseAttack, baseAttackSpeed));
    }

    private static int sumSavedMilitaryPoints(Map<String, Integer> modCounts, int legacyCount, AntClass antClass,
            int baseHealth, int baseAttack, int baseDefense, int baseAttackSpeed) {
        int weight = GameConstants.getMilitaryWeightForAntClass(antClass);
        if (weight == 0) {
            return 0;
        }
        float colonyMult = computeColonyStatMultiplierFromBases(baseHealth, baseAttack, baseAttackSpeed);
        if (modCounts != null && !modCounts.isEmpty()) {
            int total = 0;
            for (Map.Entry<String, Integer> entry : modCounts.entrySet()) {
                int count = entry.getValue();
                if (count <= 0) {
                    continue;
                }
                AntModProfile profile = AntModProfile.fromCode(Integer.parseInt(entry.getKey()));
                float standardMult = AntModService.computeCombatStatMultiplierFromBases(
                        antClass, AntModProfile.standard(), baseHealth, baseAttack, baseDefense, baseAttackSpeed);
                float actualMult = AntModService.computeCombatStatMultiplierFromBases(
                        antClass, profile, baseHealth, baseAttack, baseDefense, baseAttackSpeed);
                float modFactor = standardMult > 0f ? actualMult / standardMult : 1f;
                total += Math.round(count * weight * colonyMult * modFactor);
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
