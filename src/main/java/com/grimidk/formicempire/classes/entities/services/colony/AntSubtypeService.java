package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtype;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeSlot;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static List<AntType> getSubtypeRateTypes() {
        return List.of(
                GameConstants.TYPE_WORKER,
                GameConstants.TYPE_SOLDIER,
                GameConstants.TYPE_MAJOR,
                GameConstants.TYPE_PRINCESS);
    }

    public static Map<AntType, Map<AntSubtypeSlot, Map<Integer, Float>>> defaultSubtypeRates() {
        Map<AntType, Map<AntSubtypeSlot, Map<Integer, Float>>> rates = new HashMap<>();
        for (AntType type : getSubtypeRateTypes()) {
            rates.put(type, defaultSlotRates());
        }
        return rates;
    }

    public static Map<AntSubtypeSlot, Map<Integer, Float>> defaultRatesForType() {
        return defaultSlotRates();
    }

    private static Map<AntSubtypeSlot, Map<Integer, Float>> defaultSlotRates() {
        Map<AntSubtypeSlot, Map<Integer, Float>> rates = new EnumMap<>(AntSubtypeSlot.class);
        for (AntSubtypeSlot slot : AntSubtypeSlot.values()) {
            Map<Integer, Float> slotRates = new HashMap<>();
            slotRates.put(AntSubtype.DIGIT_NONE, 100f);
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

    public static boolean hasSubtypeAssimilation(Colony colony) {
        if (colony == null) {
            return false;
        }
        return colony.hasUpgrade(GameUnlocks.ASSIMILATED_TRAPJAW)
                || colony.hasUpgrade(GameUnlocks.ASSIMILATED_HONEYPOT)
                || colony.hasUpgrade(GameUnlocks.ASSIMILATED_DOORHEAD)
                || colony.hasUpgrade(GameUnlocks.ASSIMILATED_STINGING);
    }

    public static float consumptionMult(AntSubtypeProfile profile) {
        if (profile == null || profile.isStandard()) {
            return 1f;
        }
        int active = profile.countActiveSubtypes();
        if (active <= 0) {
            return 1f;
        }
        return 1f + active * GameNumbers.SUBTYPE_FOOD_CONSUMPTION_ADD_PER_TRAIT;
    }

    public static float subtypeAutomationFoodScale(Colony colony) {
        if (colony == null) {
            return 1f;
        }
        double food = colony.getMushroomsPrecise();
        double water = colony.getWaterPrecise();
        if (food >= water) {
            return 1f;
        }
        if (food >= water * 0.5) {
            return 0.5f;
        }
        return 0f;
    }

    public static void applyAutomatedSubtypeRates(Colony colony) {
        if (colony == null || !hasSubtypeAssimilation(colony)) {
            return;
        }

        Map<AntType, Map<AntSubtypeSlot, Map<Integer, Float>>> rates = defaultSubtypeRates();
        float scale = subtypeAutomationFoodScale(colony);
        if (scale <= 0f) {
            colony.setSubtypeHatchRates(rates);
            return;
        }

        boolean trapjaw = colony.hasUpgrade(GameUnlocks.ASSIMILATED_TRAPJAW);
        boolean honeypot = colony.hasUpgrade(GameUnlocks.ASSIMILATED_HONEYPOT);
        boolean doorhead = colony.hasUpgrade(GameUnlocks.ASSIMILATED_DOORHEAD);
        boolean bullet = colony.hasUpgrade(GameUnlocks.ASSIMILATED_STINGING);

        if (honeypot) {
            setAutomatedSlotRate(rates, GameConstants.TYPE_WORKER, AntSubtypeSlot.ABDOMEN, 3, 50f * scale);
            setAutomatedSlotRate(rates, GameConstants.TYPE_PRINCESS, AntSubtypeSlot.ABDOMEN, 3, 10f * scale);
        }
        if (trapjaw) {
            setAutomatedSlotRate(rates, GameConstants.TYPE_SOLDIER, AntSubtypeSlot.HEAD, 2, 100f * scale);
            if (colony.hasUpgrade(GameUnlocks.TYPE_MAJOR)) {
                setAutomatedSlotRate(rates, GameConstants.TYPE_MAJOR, AntSubtypeSlot.HEAD, 2, 100f * scale);
            }
        }
        if (doorhead) {
            setAutomatedSlotRate(rates, GameConstants.TYPE_WORKER, AntSubtypeSlot.HEAD, 3, 50f * scale);
            if (!trapjaw && colony.hasUpgrade(GameUnlocks.TYPE_SOLDIER)) {
                setAutomatedSlotRate(rates, GameConstants.TYPE_SOLDIER, AntSubtypeSlot.HEAD, 3, 50f * scale);
            }
        }
        if (bullet) {
            setAutomatedSlotRate(rates, GameConstants.TYPE_SOLDIER, AntSubtypeSlot.ABDOMEN, 2, 100f * scale);
            if (colony.hasUpgrade(GameUnlocks.TYPE_MAJOR)) {
                setAutomatedSlotRate(rates, GameConstants.TYPE_MAJOR, AntSubtypeSlot.ABDOMEN, 2, 100f * scale);
            }
        }

        colony.setSubtypeHatchRates(rates);
    }

    private static void setAutomatedSlotRate(
            Map<AntType, Map<AntSubtypeSlot, Map<Integer, Float>>> rates,
            AntType type, AntSubtypeSlot slot, int digit, float subtypePct) {
        if (rates == null || type == null || slot == null) {
            return;
        }
        float clamped = Math.min(100f, Math.max(0f, subtypePct));
        Map<Integer, Float> slotRates = rates.computeIfAbsent(type, ignored -> defaultRatesForType()).get(slot);
        slotRates.clear();
        slotRates.put(digit, clamped);
        slotRates.put(AntSubtype.DIGIT_NONE, 100f - clamped);
    }

    public static Map<AntType, Map<AntSubtypeSlot, Map<Integer, Float>>> deepCopyRates(
            Map<AntType, Map<AntSubtypeSlot, Map<Integer, Float>>> source) {
        Map<AntType, Map<AntSubtypeSlot, Map<Integer, Float>>> copy = new HashMap<>();
        if (source == null) {
            return defaultSubtypeRates();
        }
        for (Map.Entry<AntType, Map<AntSubtypeSlot, Map<Integer, Float>>> entry : source.entrySet()) {
            Map<AntSubtypeSlot, Map<Integer, Float>> slotCopy = new EnumMap<>(AntSubtypeSlot.class);
            for (Map.Entry<AntSubtypeSlot, Map<Integer, Float>> slotEntry : entry.getValue().entrySet()) {
                slotCopy.put(slotEntry.getKey(), new HashMap<>(slotEntry.getValue()));
            }
            for (AntSubtypeSlot slot : AntSubtypeSlot.values()) {
                slotCopy.putIfAbsent(slot, new HashMap<>(Map.of(AntSubtype.DIGIT_NONE, 100f)));
            }
            copy.put(entry.getKey(), slotCopy);
        }
        for (AntType type : getSubtypeRateTypes()) {
            copy.putIfAbsent(type, defaultSlotRates());
        }
        return copy;
    }

    public static void applyNaturalSpeciesSubtypeRates(Colony colony, AntSpecies species) {
        if (colony == null || species == null) {
            return;
        }
        Map<AntType, Map<AntSubtypeSlot, Map<Integer, Float>>> rates = defaultSubtypeRates();
        for (Upgrade trait : species.getBaseUpgrades()) {
            AntSubtype subtype = findSubtypeForUpgrade(trait);
            if (subtype == null || subtype.isNone()) {
                continue;
            }
            Map<Integer, Float> slotRates = new HashMap<>();
            slotRates.put(AntSubtype.DIGIT_NONE, 100f - NPC_NATURAL_SUBTYPE_RATE);
            slotRates.put(subtype.getDigit(), NPC_NATURAL_SUBTYPE_RATE);
            for (AntType type : getSubtypeRateTypes()) {
                rates.get(type).put(subtype.getSlot(), slotRates);
            }
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
            AntSubtypeProfile profile = rollProfile(colony, ant.getAntType());
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

    public static AntSubtypeProfile rollProfile(Colony colony, AntType type) {
        int head = rollSlotDigit(colony, type, AntSubtypeSlot.HEAD);
        int torso = AntSubtype.DIGIT_NONE;
        int abdomen = rollSlotDigit(colony, type, AntSubtypeSlot.ABDOMEN);
        int other = AntSubtype.DIGIT_NONE;
        return AntSubtypeProfile.of(head, torso, abdomen, other);
    }

    private static int rollSlotDigit(Colony colony, AntType type, AntSubtypeSlot slot) {
        if (!GameConstants.getConfigurableSubtypeSlots().contains(slot)) {
            return AntSubtype.DIGIT_NONE;
        }
        List<AntSubtype> options = getAvailableSubtypes(colony, slot);
        if (options.size() <= 1) {
            return AntSubtype.DIGIT_NONE;
        }
        double rand = GameRandom.nextDouble() * 100.0;
        double cumulative = 0.0;
        for (AntSubtype subtype : options) {
            cumulative += colony.getSubtypeHatchRate(type, slot, subtype.getDigit());
            if (rand < cumulative) {
                return subtype.getDigit();
            }
        }
        return AntSubtype.DIGIT_NONE;
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

        ant.setMaxHealth(Math.max(0, Math.round(colony.getBaseHealth() * type.getHealtMult() * combinedHealthMult(profile))));
        if (ant.getHealth() > ant.getMaxHealth()) {
            ant.setHealth(ant.getMaxHealth());
        }
        ant.setRegen(Math.round(colony.getBaseRegen() * type.getRegenMult() * combinedRegenMult(profile)));
        ant.setConsumption(colony.getBaseConsumption() * type.getConsumptionMult() * consumptionMult(profile));
        // Subtype attack boosts apply to infantry skills only (see CritterSkillService.resolveSubtypeAttackMult).
        ant.setAttack((int) (colony.getBaseAttack() * type.getAttackMult()));
        ant.setAttackSpeed((int) (colony.getBaseAttackSpeed() * type.getAttackSpeedMult()));
        ant.setDefense(GameNumbers.clampDefensePercent(
                type.getDefenseMult() + combinedDefenseBonus(profile)));
        ant.setSpeed(colony.getBaseSpeed() * type.getSpeedMult() * combinedSpeedMult(profile));
    }

    public static float combinedAttackMult(AntSubtypeProfile profile) {
        float total = 0f;
        boolean hasAttackSubtype = false;
        for (AntSubtypeSlot slot : AntSubtypeSlot.values()) {
            AntSubtype subtype = profile.getSubtype(slot);
            if (subtype == null || subtype.isNone() || subtype.getAttackMult() == 1f) {
                continue;
            }
            total += subtype.getAttackMult();
            hasAttackSubtype = true;
        }
        return hasAttackSubtype ? total : 1f;
    }

    /** Additive accuracy bonus from subtypes (e.g. Farsight +0.15). */
    public static float combinedAccuracyBonus(AntSubtypeProfile profile) {
        float total = 0f;
        if (profile == null) {
            return total;
        }
        for (AntSubtypeSlot slot : AntSubtypeSlot.values()) {
            AntSubtype subtype = profile.getSubtype(slot);
            if (subtype == null || subtype.isNone()) {
                continue;
            }
            total += subtype.getAccuracyBonus();
        }
        return total;
    }

    /** Extra defense percent from subtypes (e.g. Doorhead +20). Neutral subtypes contribute 0. */
    public static float combinedDefenseBonus(AntSubtypeProfile profile) {
        float total = 0f;
        for (AntSubtypeSlot slot : AntSubtypeSlot.values()) {
            AntSubtype subtype = profile.getSubtype(slot);
            if (subtype == null || subtype.isNone() || subtype.getDefenseMult() == 1f) {
                continue;
            }
            total += subtype.getDefenseMult();
        }
        return total;
    }

    public static float combinedRegenMult(AntSubtypeProfile profile) {
        float mult = 1f;
        for (AntSubtypeSlot slot : AntSubtypeSlot.values()) {
            AntSubtype subtype = profile.getSubtype(slot);
            if (subtype != null && !subtype.isNone()) {
                mult *= subtype.getRegenMult();
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

    public static float computeCombatStatMultiplier(AntType type, AntSubtypeProfile profile, Colony colony) {
        if (colony == null || type == null || !isEligibleType(type)) {
            return 0f;
        }
        ColonyStatsService stats = colony.getStatsService();
        if (stats == null) {
            return 0f;
        }
        AntSubtypeProfile resolved = profile != null ? profile : AntSubtypeProfile.standard();
        int hp = Math.max(0, Math.round(stats.getBaseHealth(colony) * type.getHealtMult() * combinedHealthMult(resolved)));
        int atk = (int) (stats.getBaseAttack(colony) * type.getAttackMult());
        int def = Math.round(GameNumbers.clampDefensePercent(
                type.getDefenseMult() + combinedDefenseBonus(resolved)));
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
        int hp = Math.max(0, Math.round(baseHealth * type.getHealtMult() * combinedHealthMult(resolved)));
        int atk = (int) (baseAttack * type.getAttackMult());
        int def = Math.round(GameNumbers.clampDefensePercent(
                type.getDefenseMult() + combinedDefenseBonus(resolved)));
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

    public static float averageSubtypeCombatFactor(Colony colony, List<Ant> ants, AntType type) {
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
            return rollProfile(colony, type);
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
            return capital != null ? rollProfile(capital, type) : AntSubtypeProfile.standard();
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
            ant.setDimension(WorldSpaces.UNDERWORLD);
        } else if (isEligibleType(type)) {
            ant.setDimension(WorldSpaces.OVERWORLD);
        }
        return ant;
    }

    public static Map<String, Double> flattenSubtypeRates(
            Map<AntType, Map<AntSubtypeSlot, Map<Integer, Float>>> rates) {
        Map<String, Double> flat = new HashMap<>();
        if (rates == null) {
            return flat;
        }
        for (Map.Entry<AntType, Map<AntSubtypeSlot, Map<Integer, Float>>> typeEntry : rates.entrySet()) {
            AntType type = typeEntry.getKey();
            if (type == null) {
                continue;
            }
            for (Map.Entry<AntSubtypeSlot, Map<Integer, Float>> slotEntry : typeEntry.getValue().entrySet()) {
                for (Map.Entry<Integer, Float> rateEntry : slotEntry.getValue().entrySet()) {
                    flat.put(type.getNameKey() + ":" + slotEntry.getKey().name() + ":" + rateEntry.getKey(),
                            rateEntry.getValue().doubleValue());
                }
            }
        }
        return flat;
    }

    public static Map<AntType, Map<AntSubtypeSlot, Map<Integer, Float>>> unflattenSubtypeRates(Map<String, Double> flat) {
        Map<AntType, Map<AntSubtypeSlot, Map<Integer, Float>>> rates = defaultSubtypeRates();
        if (flat == null || flat.isEmpty()) {
            return rates;
        }
        Map<AntSubtypeSlot, Map<Integer, Float>> legacyRates = new EnumMap<>(AntSubtypeSlot.class);
        for (Map.Entry<String, Double> entry : flat.entrySet()) {
            String[] parts = entry.getKey().split(":");
            if (parts.length == 2) {
                try {
                    AntSubtypeSlot slot = AntSubtypeSlot.valueOf(parts[0]);
                    int digit = Integer.parseInt(parts[1]);
                    legacyRates.computeIfAbsent(slot, ignored -> new HashMap<>())
                            .put(digit, entry.getValue().floatValue());
                } catch (Exception ignored) {
                }
                continue;
            }
            if (parts.length != 3) {
                continue;
            }
            try {
                AntType type = resolveSubtypeRateType(parts[0]);
                if (type == null) {
                    continue;
                }
                AntSubtypeSlot slot = AntSubtypeSlot.valueOf(parts[1]);
                int digit = Integer.parseInt(parts[2]);
                rates.computeIfAbsent(type, ignored -> defaultSlotRates())
                        .computeIfAbsent(slot, ignored -> new HashMap<>())
                        .put(digit, entry.getValue().floatValue());
            } catch (Exception ignored) {
            }
        }
        if (!legacyRates.isEmpty()) {
            for (AntType type : getSubtypeRateTypes()) {
                for (Map.Entry<AntSubtypeSlot, Map<Integer, Float>> slotEntry : legacyRates.entrySet()) {
                    rates.get(type).put(slotEntry.getKey(), new HashMap<>(slotEntry.getValue()));
                }
            }
        }
        return rates;
    }

    private static AntType resolveSubtypeRateType(String key) {
        for (AntType type : getSubtypeRateTypes()) {
            if (type.getNameKey().equals(key)) {
                return type;
            }
        }
        return null;
    }

    public static boolean profileHasSubtype(AntSubtypeProfile profile, AntSubtype subtype) {
        if (profile == null || subtype == null || subtype.isNone()) {
            return false;
        }
        AntSubtype present = profile.getSubtype(subtype.getSlot());
        return present != null && present.getId() == subtype.getId();
    }

    public static boolean antHasSubtype(Ant ant, AntSubtype subtype) {
        return ant != null && profileHasSubtype(ant.getSubtypeProfile(), subtype);
    }

    public static boolean isStandardMorph(Ant ant) {
        if (ant == null) {
            return true;
        }
        AntSubtypeProfile profile = ant.getSubtypeProfile();
        return profile == null || profile.countActiveSubtypes() == 0;
    }

    /**
     * Whether {@code ant} may fill {@code role} given the colony's allowed special subtypes.
     * Standard ("nothing") ants are eligible only when the role has no required subtypes.
     * Special ants need every active special trait allowed (forced-allowed always counts as allowed).
     */
    public static boolean isAntEligibleForRole(Ant ant, AntRole role, Set<Integer> allowedSpecialSubtypeIds) {
        if (ant == null || role == null) {
            return false;
        }
        AntSubtypeProfile profile = ant.getSubtypeProfile();
        if (profile == null) {
            profile = AntSubtypeProfile.standard();
        }

        for (AntSubtype required : role.getRequiredSubtypes()) {
            if (!profileHasSubtype(profile, required)) {
                return false;
            }
        }

        if (profile.countActiveSubtypes() == 0) {
            return !role.requiresSubtypes();
        }

        Set<Integer> allowed = allowedSpecialSubtypeIds != null ? allowedSpecialSubtypeIds : Set.of();
        for (AntSubtypeSlot slot : GameConstants.getConfigurableSubtypeSlots()) {
            AntSubtype subtype = profile.getSubtype(slot);
            if (subtype == null || subtype.isNone()) {
                continue;
            }
            if (role.isSubtypeForcedAllowed(subtype)) {
                continue;
            }
            if (!allowed.contains(subtype.getId())) {
                return false;
            }
        }
        return true;
    }

    public static Set<Integer> effectiveAllowedSubtypeIds(AntRole role, Set<Integer> storedAllowed) {
        Set<Integer> effective = new HashSet<>();
        if (storedAllowed != null) {
            effective.addAll(storedAllowed);
        }
        if (role != null) {
            for (AntSubtype forced : role.getForcedAllowedSubtypes()) {
                effective.add(forced.getId());
            }
        }
        return effective;
    }

    public static List<AntSubtype> listUnlockedSpecialSubtypes(Colony colony) {
        List<AntSubtype> unlocked = new ArrayList<>();
        if (colony == null) {
            return unlocked;
        }
        for (AntSubtypeSlot slot : GameConstants.getConfigurableSubtypeSlots()) {
            for (AntSubtype subtype : getAvailableSubtypes(colony, slot)) {
                if (!subtype.isNone()) {
                    unlocked.add(subtype);
                }
            }
        }
        return unlocked;
    }

    public static int countStandardAntsOfType(Colony colony, AntType type) {
        if (colony == null || type == null) {
            return 0;
        }
        int count = 0;
        for (Ant ant : colony.getAntsByType(type)) {
            if (ant.isAlive() && isStandardMorph(ant)) {
                count++;
            }
        }
        return count;
    }

    public static int countAntsWithSubtype(Colony colony, AntType type, AntSubtype subtype) {
        if (colony == null || type == null || subtype == null || subtype.isNone()) {
            return 0;
        }
        int count = 0;
        for (Ant ant : colony.getAntsByType(type)) {
            if (ant.isAlive() && antHasSubtype(ant, subtype)) {
                count++;
            }
        }
        return count;
    }

    public static Map<String, Integer> flattenRoleDisallowedSubtypes(Map<AntRole, Set<Integer>> disallowedByRole) {
        Map<String, Integer> flat = new HashMap<>();
        if (disallowedByRole == null) {
            return flat;
        }
        for (Map.Entry<AntRole, Set<Integer>> entry : disallowedByRole.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            for (Integer subtypeId : entry.getValue()) {
                if (subtypeId != null) {
                    flat.put(entry.getKey().getId() + "_" + subtypeId, 1);
                }
            }
        }
        return flat;
    }

    public static Map<AntRole, Set<Integer>> unflattenRoleDisallowedSubtypes(Map<String, Integer> flat) {
        Map<AntRole, Set<Integer>> result = new HashMap<>();
        if (flat == null || flat.isEmpty()) {
            return result;
        }
        for (Map.Entry<String, Integer> entry : flat.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            String[] parts = entry.getKey().split("_", 2);
            if (parts.length != 2) {
                continue;
            }
            try {
                AntRole role = GameConstants.getAntRoleById(Integer.parseInt(parts[0]));
                int subtypeId = Integer.parseInt(parts[1]);
                if (role != null && GameConstants.getAntSubtypeById(subtypeId) != null) {
                    result.computeIfAbsent(role, ignored -> new HashSet<>()).add(subtypeId);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
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
