package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.ant.AntSubtype;
import com.grimidk.formicempire.classes.constants.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.ant.AntSubtypeSlot;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AntSubtypeService {

    private static final float NPC_NATURAL_SUBTYPE_RATE = 50f;

    private AntSubtypeService() {
    }

    public static boolean isEligibleType(AntType type) {
        return type == GameConstants.TYPE_WORKER
                || type == GameConstants.TYPE_SOLDIER
                || type == GameConstants.TYPE_MAJOR
                || type == GameConstants.TYPE_PRINCESS
                || type == GameConstants.TYPE_QUEEN;
    }

    public static Map<AntSubtypeSlot, Map<Integer, Float>> defaultSubtypeRates() {
        Map<AntSubtypeSlot, Map<Integer, Float>> rates = new EnumMap<>(AntSubtypeSlot.class);
        for (AntSubtypeSlot slot : AntSubtypeSlot.values()) {
            Map<Integer, Float> slotRates = new HashMap<>();
            slotRates.put(GameConstants.SUBTYPE_DIGIT_NONE, 100f);
            rates.put(slot, slotRates);
        }
        return rates;
    }

    public static void copySubtypeRates(Colony target, Colony source) {
        if (target == null || source == null) {
            return;
        }
        target.setSubtypeHatchRates(deepCopyRates(source.getSubtypeHatchRates()));
    }

    public static Map<AntSubtypeSlot, Map<Integer, Float>> deepCopyRates(
            Map<AntSubtypeSlot, Map<Integer, Float>> source) {
        Map<AntSubtypeSlot, Map<Integer, Float>> copy = new EnumMap<>(AntSubtypeSlot.class);
        if (source == null) {
            return defaultSubtypeRates();
        }
        for (Map.Entry<AntSubtypeSlot, Map<Integer, Float>> entry : source.entrySet()) {
            copy.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
        for (AntSubtypeSlot slot : AntSubtypeSlot.values()) {
            copy.putIfAbsent(slot, new HashMap<>(Map.of(GameConstants.SUBTYPE_DIGIT_NONE, 100f)));
        }
        return copy;
    }

    public static void applyNaturalSpeciesSubtypeRates(Colony colony, Species species) {
        if (colony == null || species == null) {
            return;
        }
        Map<AntSubtypeSlot, Map<Integer, Float>> rates = defaultSubtypeRates();
        for (Upgrade trait : species.getBaseUpgrades()) {
            AntSubtype subtype = findSubtypeForUpgrade(trait);
            if (subtype == null || subtype.isNone()) {
                continue;
            }
            Map<Integer, Float> slotRates = new HashMap<>();
            slotRates.put(GameConstants.SUBTYPE_DIGIT_NONE, 100f - NPC_NATURAL_SUBTYPE_RATE);
            slotRates.put(subtype.getDigit(), NPC_NATURAL_SUBTYPE_RATE);
            rates.put(subtype.getSlot(), slotRates);
        }
        colony.setSubtypeHatchRates(rates);
    }

    public static void assignNaturalSubtypesToPopulation(Colony colony) {
        if (colony == null) {
            return;
        }
        assignNaturalSubtypes(colony, colony.getWorkers());
        assignNaturalSubtypes(colony, colony.getSoldiers());
        assignNaturalSubtypes(colony, colony.getMajors());
        assignNaturalSubtypes(colony, colony.getPrincesses());
        assignNaturalSubtypes(colony, colony.getQueens());
    }

    private static void assignNaturalSubtypes(Colony colony, List<Ant> ants) {
        if (ants == null) {
            return;
        }
        for (Ant ant : ants) {
            if (ant == null || !isEligibleType(ant.getAntType())) {
                continue;
            }
            AntSubtypeProfile profile = rollProfile(colony);
            ant.setSubtypeProfile(profile);
            applySubtypeStats(ant, colony);
        }
    }

    private static AntSubtype findSubtypeForUpgrade(Upgrade upgrade) {
        if (upgrade == null) {
            return null;
        }
        for (AntSubtype subtype : GameConstants.getAntSubtypes()) {
            if (upgrade.equals(subtype.getRequiredUpgrade())) {
                return subtype;
            }
        }
        return null;
    }

    public static List<AntSubtype> getAvailableSubtypes(Colony colony, AntSubtypeSlot slot) {
        List<AntSubtype> available = new ArrayList<>();
        Dynasty dynasty = colony != null ? colony.getDynasty() : null;
        for (AntSubtype subtype : GameConstants.getSubtypesForSlot(slot)) {
            if (subtype.isNone()) {
                available.add(subtype);
                continue;
            }
            if (dynasty != null && dynasty.hasUpgrade(subtype.getRequiredUpgrade())) {
                available.add(subtype);
            }
        }
        return available;
    }

    public static AntSubtypeProfile rollProfile(Colony colony) {
        int head = rollSlotDigit(colony, AntSubtypeSlot.HEAD);
        int torso = GameConstants.SUBTYPE_DIGIT_NONE;
        int abdomen = rollSlotDigit(colony, AntSubtypeSlot.ABDOMEN);
        int other = GameConstants.SUBTYPE_DIGIT_NONE;
        return AntSubtypeProfile.of(head, torso, abdomen, other);
    }

    private static int rollSlotDigit(Colony colony, AntSubtypeSlot slot) {
        if (!GameConstants.getConfigurableSubtypeSlots().contains(slot)) {
            return GameConstants.SUBTYPE_DIGIT_NONE;
        }
        List<AntSubtype> options = getAvailableSubtypes(colony, slot);
        if (options.size() <= 1) {
            return GameConstants.SUBTYPE_DIGIT_NONE;
        }
        double rand = GameRandom.nextDouble() * 100.0;
        double cumulative = 0.0;
        for (AntSubtype subtype : options) {
            cumulative += colony.getSubtypeHatchRate(slot, subtype.getDigit());
            if (rand < cumulative) {
                return subtype.getDigit();
            }
        }
        return GameConstants.SUBTYPE_DIGIT_NONE;
    }

    public static void applySubtypeStats(Ant ant, Colony colony) {
        if (ant == null || colony == null) {
            return;
        }
        AntType type = ant.getAntType();
        AntSubtypeProfile profile = ant.getSubtypeProfile();
        if (profile == null) {
            profile = AntSubtypeProfile.standard();
            ant.setSubtypeProfile(profile);
        }

        ant.setMaxHealth((int) (colony.getBaseHealth() * type.getHealtMult() * combinedHealthMult(profile)));
        if (ant.getHealth() > ant.getMaxHealth()) {
            ant.setHealth(ant.getMaxHealth());
        }
        ant.setRegen((int) (colony.getBaseRegen() * type.getRegenMult()));
        ant.setConsumption(colony.getBaseConsumption() * type.getConsumptionMult());
        ant.setAttack((int) (colony.getBaseAttack() * type.getAttackMult() * combinedAttackMult(profile)));
        ant.setAttackSpeed((int) (colony.getBaseAttackSpeed() * type.getAttackSpeedMult()));
        ant.setDefense((int) (colony.getBaseDefense() * type.getDefenseMult() * combinedDefenseMult(profile)));
        ant.setSpeed(colony.getBaseSpeed() * type.getSpeedMult() * combinedSpeedMult(profile));
    }

    public static float combinedAttackMult(AntSubtypeProfile profile) {
        float multiplicative = 1f;
        float additiveBonus = 0f;
        for (AntSubtypeSlot slot : AntSubtypeSlot.values()) {
            AntSubtype subtype = profile.getSubtype(slot);
            if (subtype == null || subtype.isNone() || subtype.getAttackMult() == 1f) {
                continue;
            }
            if (subtype.isAttackAdditive()) {
                additiveBonus += subtype.getAttackMult() - 1f;
            } else {
                multiplicative *= subtype.getAttackMult();
            }
        }
        return multiplicative + additiveBonus;
    }

    public static float combinedDefenseMult(AntSubtypeProfile profile) {
        float mult = 1f;
        for (AntSubtypeSlot slot : AntSubtypeSlot.values()) {
            AntSubtype subtype = profile.getSubtype(slot);
            if (subtype != null && !subtype.isNone()) {
                mult *= subtype.getDefenseMult();
            }
        }
        return mult;
    }

    public static float combinedSpeedMult(AntSubtypeProfile profile) {
        float mult = 1f;
        for (AntSubtypeSlot slot : AntSubtypeSlot.values()) {
            AntSubtype subtype = profile.getSubtype(slot);
            if (subtype != null && !subtype.isNone()) {
                mult *= subtype.getSpeedMult();
            }
        }
        return mult;
    }

    private static float combinedHealthMult(AntSubtypeProfile profile) {
        return 1f;
    }

    /**
     * Combat stat multiplier for one ant profile, using the same bases as {@link #applySubtypeStats}.
     */
    public static float computeCombatStatMultiplier(AntType type, AntSubtypeProfile profile, Colony colony) {
        if (colony == null || type == null || !isEligibleType(type)) {
            return 0f;
        }
        ColonyStatsService stats = colony.getStatsService();
        if (stats == null) {
            return 0f;
        }
        AntSubtypeProfile resolved = profile != null ? profile : AntSubtypeProfile.standard();
        int hp = (int) (stats.getBaseHealth(colony) * type.getHealtMult() * combinedHealthMult(resolved));
        int atk = (int) (stats.getBaseAttack(colony) * type.getAttackMult() * combinedAttackMult(resolved));
        int def = (int) (stats.getBaseDefense(colony) * type.getDefenseMult() * combinedDefenseMult(resolved));
        int atkSpd = (int) (stats.getBaseAttackSpeed(colony) * type.getAttackSpeedMult());
        return ColonyMilitaryService.computeStatMultiplierFromBases(hp, atk, def, atkSpd);
    }

    public static float computeCombatStatMultiplier(Ant ant, Colony colony) {
        if (ant == null) {
            return 0f;
        }
        return computeCombatStatMultiplier(ant.getAntType(), ant.getSubtypeProfile(), colony);
    }

    public static float computeCombatStatMultiplierFromBases(
            AntType type, AntSubtypeProfile profile, int baseHealth, int baseAttack, int baseDefense,
            int baseAttackSpeed) {
        if (type == null || !isEligibleType(type)) {
            return 0f;
        }
        AntSubtypeProfile resolved = profile != null ? profile : AntSubtypeProfile.standard();
        int hp = (int) (baseHealth * type.getHealtMult() * combinedHealthMult(resolved));
        int atk = (int) (baseAttack * type.getAttackMult() * combinedAttackMult(resolved));
        int def = (int) (baseDefense * type.getDefenseMult() * combinedDefenseMult(resolved));
        int atkSpd = (int) (baseAttackSpeed * type.getAttackSpeedMult());
        return ColonyMilitaryService.computeStatMultiplierFromBases(hp, atk, def, atkSpd);
    }

    public static float computeSubtypeCombatFactor(AntType type, AntSubtypeProfile profile, Colony colony) {
        float standardMult = computeCombatStatMultiplier(type, AntSubtypeProfile.standard(), colony);
        float actualMult = computeCombatStatMultiplier(type, profile, colony);
        if (standardMult <= 0f) {
            return 1f;
        }
        return actualMult / standardMult;
    }

    public static float averageSubtypeCombatFactor(Colony colony, java.util.List<Ant> ants, AntType type) {
        if (colony == null || type == null || ants == null || ants.isEmpty()) {
            return 1f;
        }
        return weightedSubtypeCombatFactor(colony, type, aggregateSubtypeCounts(ants));
    }

    public static float forageMult(Ant ant) {
        if (ant == null) {
            return 1f;
        }
        AntSubtypeProfile profile = ant.getSubtypeProfile();
        if (profile == null) {
            return 1f;
        }
        float mult = 1f;
        for (AntSubtypeSlot slot : AntSubtypeSlot.values()) {
            AntSubtype subtype = profile.getSubtype(slot);
            if (subtype != null && !subtype.isNone()) {
                mult *= subtype.getForageMult();
            }
        }
        return mult;
    }

    public static int forageCarrySlots(Ant ant) {
        return Math.max(1, Math.round(forageMult(ant)));
    }

    public static int sumCollectingPower(Colony colony, List<Ant> workers) {
        if (colony == null || workers == null || workers.isEmpty()) {
            return 0;
        }
        float baseRate = colony.getStatsService().getCollectingRate(colony);
        if (baseRate <= 0f) {
            return 0;
        }
        int total = 0;
        for (Ant worker : workers) {
            total += Math.max(1, Math.round(baseRate * forageMult(worker)));
        }
        return total;
    }

    public static AntSubtypeProfile sampleProfileFromColony(Colony colony, AntType type) {
        if (colony == null || type == null) {
            return AntSubtypeProfile.standard();
        }
        List<Ant> ants = colony.getAntsByType(type);
        if (ants.isEmpty()) {
            return rollProfile(colony);
        }
        Ant picked = ants.get(GameRandom.nextInt(ants.size()));
        AntSubtypeProfile profile = picked.getSubtypeProfile();
        return profile != null ? profile : AntSubtypeProfile.standard();
    }

    public static AntSubtypeProfile sampleProfileFromDynasty(Dynasty dynasty, AntType type) {
        if (dynasty == null || type == null) {
            return AntSubtypeProfile.standard();
        }
        List<Ant> pool = new ArrayList<>();
        for (Colony colony : dynasty.getColonies()) {
            if (colony != null) {
                pool.addAll(colony.getAntsByType(type));
            }
        }
        if (pool.isEmpty()) {
            Colony capital = dynasty.getCapital();
            return capital != null ? rollProfile(capital) : AntSubtypeProfile.standard();
        }
        Ant picked = pool.get(GameRandom.nextInt(pool.size()));
        AntSubtypeProfile profile = picked.getSubtypeProfile();
        return profile != null ? profile : AntSubtypeProfile.standard();
    }

    public static Map<String, Integer> aggregateSubtypeCounts(List<Ant> ants) {
        Map<String, Integer> counts = new HashMap<>();
        if (ants == null) {
            return counts;
        }
        for (Ant ant : ants) {
            if (ant == null || !ant.isAlive()) {
                continue;
            }
            AntSubtypeProfile profile = ant.getSubtypeProfile();
            int code = profile != null ? profile.getCode() : AntSubtypeProfile.STANDARD_CODE;
            String key = String.valueOf(code);
            counts.merge(key, 1, Integer::sum);
        }
        return counts;
    }

    public static float weightedSubtypeCombatFactor(Colony colony, AntType type, Map<String, Integer> subtypeCounts) {
        if (colony == null || type == null || subtypeCounts == null || subtypeCounts.isEmpty()) {
            return 1f;
        }
        float weightedSum = 0f;
        int total = 0;
        for (Map.Entry<String, Integer> entry : subtypeCounts.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            AntSubtypeProfile profile = AntSubtypeProfile.fromCode(Integer.parseInt(entry.getKey()));
            weightedSum += entry.getValue() * computeSubtypeCombatFactor(type, profile, colony);
            total += entry.getValue();
        }
        return total > 0 ? weightedSum / total : 1f;
    }

    public static int sumMilitaryPointsForSubtypeCounts(Colony colony, AntType type, Map<String, Integer> subtypeCounts) {
        int weight = GameConstants.getMilitaryWeightForAntType(type);
        if (colony == null || weight == 0 || subtypeCounts == null || subtypeCounts.isEmpty()) {
            return 0;
        }
        float colonyMult = ColonyMilitaryService.computeStatMultiplier(colony);
        int total = 0;
        for (Map.Entry<String, Integer> entry : subtypeCounts.entrySet()) {
            int count = entry.getValue();
            if (count <= 0) {
                continue;
            }
            AntSubtypeProfile profile = AntSubtypeProfile.fromCode(Integer.parseInt(entry.getKey()));
            float subtypeFactor = computeSubtypeCombatFactor(type, profile, colony);
            total += Math.round(count * weight * colonyMult * subtypeFactor);
        }
        return total;
    }

    public static void populateAntsFromSubtypeCounts(Colony colony, List<Ant> list, AntType type,
            Map<String, Integer> subtypeCounts, int legacyCount) {
        list.clear();
        if (subtypeCounts != null && !subtypeCounts.isEmpty()) {
            for (Map.Entry<String, Integer> entry : subtypeCounts.entrySet()) {
                int code = Integer.parseInt(entry.getKey());
                AntSubtypeProfile profile = AntSubtypeProfile.fromCode(code);
                for (int i = 0; i < entry.getValue(); i++) {
                    list.add(createAnt(colony, type, profile));
                }
            }
            return;
        }
        for (int i = 0; i < legacyCount; i++) {
            list.add(createAnt(colony, type, AntSubtypeProfile.standard()));
        }
    }

    public static Ant createAnt(Colony colony, AntType type, AntSubtypeProfile profile) {
        Ant ant = new Ant(colony, type);
        if (isEligibleType(type) && profile != null) {
            ant.setSubtypeProfile(profile);
            applySubtypeStats(ant, colony);
        }
        if (type == GameConstants.TYPE_QUEEN) {
            ant.setDimension(com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces.UNDERWORLD);
        } else if (isEligibleType(type)) {
            ant.setDimension(com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces.OVERWORLD);
        }
        return ant;
    }

    public static Map<String, Double> flattenSubtypeRates(Map<AntSubtypeSlot, Map<Integer, Float>> rates) {
        Map<String, Double> flat = new HashMap<>();
        if (rates == null) {
            return flat;
        }
        for (Map.Entry<AntSubtypeSlot, Map<Integer, Float>> entry : rates.entrySet()) {
            for (Map.Entry<Integer, Float> rateEntry : entry.getValue().entrySet()) {
                flat.put(entry.getKey().name() + ":" + rateEntry.getKey(), rateEntry.getValue().doubleValue());
            }
        }
        return flat;
    }

    public static Map<AntSubtypeSlot, Map<Integer, Float>> unflattenSubtypeRates(Map<String, Double> flat) {
        Map<AntSubtypeSlot, Map<Integer, Float>> rates = defaultSubtypeRates();
        if (flat == null || flat.isEmpty()) {
            return rates;
        }
        for (Map.Entry<String, Double> entry : flat.entrySet()) {
            String[] parts = entry.getKey().split(":");
            if (parts.length != 2) {
                continue;
            }
            try {
                AntSubtypeSlot slot = AntSubtypeSlot.valueOf(parts[0]);
                int digit = Integer.parseInt(parts[1]);
                rates.computeIfAbsent(slot, ignored -> new HashMap<>())
                        .put(digit, entry.getValue().floatValue());
            } catch (Exception ignored) {
                // skip malformed legacy keys
            }
        }
        return rates;
    }

    public static void inheritSubtype(Ant source, Ant target, Colony colony) {
        if (source == null || target == null || colony == null) {
            return;
        }
        AntSubtypeProfile profile = source.getSubtypeProfile();
        if (profile == null) {
            profile = AntSubtypeProfile.standard();
        }
        target.setSubtypeProfile(profile);
        applySubtypeStats(target, colony);
    }
}
