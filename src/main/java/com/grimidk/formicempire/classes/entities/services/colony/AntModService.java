package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
import com.grimidk.formicempire.classes.constants.critter.ant.AntMod;
import com.grimidk.formicempire.classes.constants.critter.ant.AntModProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntModSlot;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtype;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
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

public final class AntModService {

    private static final float NPC_NATURAL_MOD_RATE = 50f;

    private AntModService() {
    }

    public static boolean isEligibleClass(AntClass type) {
        return type == GameConstants.CLASS_WORKER
                || type == GameConstants.CLASS_SOLDIER
                || type == GameConstants.CLASS_MAJOR
                || type == GameConstants.CLASS_PRINCESS
                || type == GameConstants.CLASS_QUEEN;
    }

    public static boolean isEligibleType(AntClass type) {
        return isEligibleClass(type);
    }

    public static List<AntClass> getModRateClasses() {
        return List.of(
                GameConstants.CLASS_WORKER,
                GameConstants.CLASS_SOLDIER,
                GameConstants.CLASS_MAJOR,
                GameConstants.CLASS_PRINCESS);
    }

    public static List<AntClass> getSubtypeRateTypes() {
        return getModRateClasses();
    }

    public static Map<AntClass, Map<AntModSlot, Map<Integer, Float>>> defaultModRates() {
        Map<AntClass, Map<AntModSlot, Map<Integer, Float>>> rates = new HashMap<>();
        for (AntClass type : getModRateClasses()) {
            rates.put(type, defaultSlotRates());
        }
        return rates;
    }

    public static Map<AntModSlot, Map<Integer, Float>> defaultRatesForClass() {
        return defaultSlotRates();
    }

    public static Map<AntModSlot, Map<Integer, Float>> defaultRatesForType() {
        return defaultSlotRates();
    }

    private static Map<AntModSlot, Map<Integer, Float>> defaultSlotRates() {
        Map<AntModSlot, Map<Integer, Float>> rates = new EnumMap<>(AntModSlot.class);
        for (AntModSlot slot : AntModSlot.values()) {
            Map<Integer, Float> slotRates = new HashMap<>();
            slotRates.put(AntMod.DIGIT_NONE, 100f);
            rates.put(slot, slotRates);
        }
        return rates;
    }

    public static void copyModRates(Colony target, Colony source) {
        if (target == null || source == null) {
            return;
        }
        AntSubtypeService.copySubtypeRates(target, source);
    }

    public static boolean hasModAssimilation(Colony colony) {
        if (colony == null) {
            return false;
        }
        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null) {
            return false;
        }
        for (AntMod mod : GameConstants.getAntMods()) {
            if (mod == null || mod.isNone() || mod.getRequiredUpgrade() == null) {
                continue;
            }
            if (dynasty.hasUpgrade(mod.getRequiredUpgrade())) {
                return true;
            }
        }
        return false;
    }

    public static float consumptionMult(AntModProfile profile) {
        if (profile == null || profile.isStandard()) {
            return 1f;
        }
        int active = profile.countActiveMods();
        if (active <= 0) {
            return 1f;
        }
        return 1f + active * GameNumbers.SUBTYPE_FOOD_CONSUMPTION_ADD_PER_TRAIT;
    }

    public static float modAutomationFoodScale(Colony colony) {
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

    public static void applyAutomatedModRates(Colony colony) {
        AntSubtypeService.applyAutomatedSubtypeRates(colony);
    }

    public static Map<AntClass, Map<AntModSlot, Map<Integer, Float>>> deepCopyRates(
            Map<AntClass, Map<AntModSlot, Map<Integer, Float>>> source) {
        Map<AntClass, Map<AntModSlot, Map<Integer, Float>>> copy = new HashMap<>();
        if (source == null) {
            return defaultModRates();
        }
        for (Map.Entry<AntClass, Map<AntModSlot, Map<Integer, Float>>> entry : source.entrySet()) {
            Map<AntModSlot, Map<Integer, Float>> slotCopy = new EnumMap<>(AntModSlot.class);
            for (Map.Entry<AntModSlot, Map<Integer, Float>> slotEntry : entry.getValue().entrySet()) {
                slotCopy.put(slotEntry.getKey(), new HashMap<>(slotEntry.getValue()));
            }
            for (AntModSlot slot : AntModSlot.values()) {
                slotCopy.putIfAbsent(slot, new HashMap<>(Map.of(AntMod.DIGIT_NONE, 100f)));
            }
            copy.put(entry.getKey(), slotCopy);
        }
        for (AntClass type : getModRateClasses()) {
            copy.putIfAbsent(type, defaultSlotRates());
        }
        return copy;
    }

    public static void applyNaturalSpeciesModRates(Colony colony, AntSpecies species) {
        AntSubtypeService.applyNaturalSpeciesSubtypeRates(colony, species);
    }

    public static void assignNaturalModsToPopulation(Colony colony) {
        AntSubtypeService.assignNaturalSubtypesToPopulation(colony);
    }

    public static List<AntMod> getAvailableMods(Colony colony, AntModSlot slot) {
        List<AntMod> available = new ArrayList<>();
        Dynasty dynasty = colony != null ? colony.getDynasty() : null;
        for (AntMod mod : GameConstants.getModsForSlot(slot)) {
            if (mod.isNone()) {
                available.add(mod);
                continue;
            }
            if (dynasty != null && dynasty.hasUpgrade(mod.getRequiredUpgrade())) {
                available.add(mod);
            }
        }
        return available;
    }

    public static AntModProfile rollProfile(Colony colony, AntClass type) {
        int head = rollSlotDigit(colony, type, AntModSlot.HEAD);
        int torso = rollSlotDigit(colony, type, AntModSlot.TORSO);
        int abdomen = rollSlotDigit(colony, type, AntModSlot.ABDOMEN);
        int other = AntMod.DIGIT_NONE;
        return AntModProfile.of(head, torso, abdomen, other);
    }

    private static int rollSlotDigit(Colony colony, AntClass type, AntModSlot slot) {
        if (!GameConstants.getConfigurableModSlots().contains(slot)) {
            return AntMod.DIGIT_NONE;
        }
        List<AntMod> options = getAvailableMods(colony, slot);
        if (options.size() <= 1) {
            return AntMod.DIGIT_NONE;
        }
        float foodScale = 1f;
        if (colony != null && colony.isAutomationEnabled()) {
            foodScale = modAutomationFoodScale(colony);
            if (foodScale <= 0f) {
                return AntMod.DIGIT_NONE;
            }
        }
        double rand = GameRandom.nextDouble() * 100.0;
        double cumulative = 0.0;
        for (AntMod mod : options) {
            cumulative += colony.getSubtypeHatchRate(type instanceof AntType t ? t : null, slot.toSubtypeSlot(), mod.getDigit()) * foodScale;
            if (rand < cumulative) {
                return mod.getDigit();
            }
        }
        return AntMod.DIGIT_NONE;
    }

    public static void applyModStats(Ant ant, Colony colony) {
        if (ant == null || colony == null) {
            return;
        }
        AntSubtypeService.applySubtypeStats(ant, colony);
    }

    public static float combinedAttackMult(AntModProfile profile) {
        float total = 0f;
        boolean hasAttackMod = false;
        for (AntModSlot slot : AntModSlot.values()) {
            AntMod mod = profile != null ? profile.getMod(slot) : null;
            if (mod == null || mod.isNone() || mod.getAttackMult() == 1f) {
                continue;
            }
            total += mod.getAttackMult();
            hasAttackMod = true;
        }
        return hasAttackMod ? total : 1f;
    }

    public static float combinedAccuracyBonus(AntModProfile profile) {
        float total = 0f;
        if (profile == null) {
            return total;
        }
        for (AntModSlot slot : AntModSlot.values()) {
            AntMod mod = profile.getMod(slot);
            if (mod == null || mod.isNone()) {
                continue;
            }
            total += mod.getAccuracyBonus();
        }
        return total;
    }

    public static float combinedDefenseBonus(AntModProfile profile) {
        float total = 0f;
        for (AntModSlot slot : AntModSlot.values()) {
            AntMod mod = profile != null ? profile.getMod(slot) : null;
            if (mod == null || mod.isNone() || mod.getDefenseMult() == 1f) {
                continue;
            }
            total += mod.getDefenseMult();
        }
        return total;
    }

    public static float combinedRegenMult(AntModProfile profile) {
        float mult = 1f;
        for (AntModSlot slot : AntModSlot.values()) {
            AntMod mod = profile != null ? profile.getMod(slot) : null;
            if (mod != null && !mod.isNone()) {
                mult *= mod.getRegenMult();
            }
        }
        return mult;
    }

    public static float combinedSpeedMult(AntModProfile profile) {
        float mult = 1f;
        for (AntModSlot slot : AntModSlot.values()) {
            AntMod mod = profile != null ? profile.getMod(slot) : null;
            if (mod != null && !mod.isNone()) {
                mult *= mod.getSpeedMult();
            }
        }
        return mult;
    }

    public static float computeCombatStatMultiplier(AntClass type, AntModProfile profile, Colony colony) {
        return AntSubtypeService.computeCombatStatMultiplier(type instanceof AntType t ? t : null,
                profile != null ? profile.toSubtypeProfile() : null, colony);
    }

    public static float computeCombatStatMultiplier(Ant ant, Colony colony) {
        if (ant == null) {
            return 0f;
        }
        return computeCombatStatMultiplier(ant.getAntClass(), ant.getModProfile(), colony);
    }

    public static float computeCombatStatMultiplierFromBases(
            AntClass type, AntModProfile profile, int baseHealth, int baseAttack, int baseDefense,
            int baseAttackSpeed) {
        return AntSubtypeService.computeCombatStatMultiplierFromBases(
                type instanceof AntType t ? t : null, profile != null ? profile.toSubtypeProfile() : null, baseHealth, baseAttack, baseDefense, baseAttackSpeed);
    }

    public static float computeModCombatFactor(AntClass type, AntModProfile profile, Colony colony) {
        return AntSubtypeService.computeSubtypeCombatFactor(
                type instanceof AntType t ? t : null, profile != null ? profile.toSubtypeProfile() : null, colony);
    }

    public static float averageModCombatFactor(Colony colony, List<Ant> ants, AntClass type) {
        return AntSubtypeService.averageSubtypeCombatFactor(colony, ants, type instanceof AntType t ? t : null);
    }

    public static float forageMult(Ant ant) {
        return AntSubtypeService.forageMult(ant);
    }

    public static int forageCarrySlots(Ant ant) {
        return AntSubtypeService.forageCarrySlots(ant);
    }

    public static int sumCollectingPowerFromRoleCount(Colony colony, int workerCount) {
        return AntSubtypeService.sumCollectingPowerFromRoleCount(colony, workerCount);
    }

    public static int sumCollectingPower(Colony colony, List<Ant> workers) {
        return AntSubtypeService.sumCollectingPower(colony, workers);
    }

    public static AntModProfile sampleProfileFromColony(Colony colony, AntClass type) {
        return AntSubtypeService.sampleProfileFromColony(colony, type instanceof AntType t ? t : null).toModProfile();
    }

    public static AntModProfile sampleProfileFromDynasty(Dynasty dynasty, AntClass type) {
        return AntSubtypeService.sampleProfileFromDynasty(dynasty, type instanceof AntType t ? t : null).toModProfile();
    }

    public static Map<String, Integer> aggregateModCounts(List<Ant> ants) {
        return AntSubtypeService.aggregateSubtypeCounts(ants);
    }

    public static float weightedModCombatFactor(Colony colony, AntClass type, Map<String, Integer> modCounts) {
        return AntSubtypeService.weightedSubtypeCombatFactor(colony, type instanceof AntType t ? t : null, modCounts);
    }

    public static int sumMilitaryPointsForModCounts(Colony colony, AntClass type, Map<String, Integer> modCounts) {
        return AntSubtypeService.sumMilitaryPointsForSubtypeCounts(colony, type instanceof AntType t ? t : null, modCounts);
    }

    public static void populateAntsFromModCounts(Colony colony, List<Ant> list, AntClass type,
            Map<String, Integer> modCounts, int legacyCount) {
        AntSubtypeService.populateAntsFromSubtypeCounts(colony, list, type instanceof AntType t ? t : null, modCounts, legacyCount);
    }

    public static Ant createAnt(Colony colony, AntClass type, AntModProfile profile) {
        return AntSubtypeService.createAnt(colony, type instanceof AntType t ? t : null, profile != null ? profile.toSubtypeProfile() : null);
    }

    public static boolean profileHasMod(AntModProfile profile, AntMod mod) {
        if (profile == null || mod == null || mod.isNone()) {
            return false;
        }
        AntMod present = profile.getMod(mod.getSlot());
        return present != null && present.getId() == mod.getId();
    }

    public static boolean antHasMod(Ant ant, AntMod mod) {
        return ant != null && profileHasMod(ant.getModProfile(), mod);
    }

    public static boolean isStandardMorph(Ant ant) {
        return AntSubtypeService.isStandardMorph(ant);
    }

    public static boolean isAntEligibleForRole(Ant ant, AntRole role, Set<Integer> allowedSpecialModIds) {
        return AntSubtypeService.isAntEligibleForRole(ant, role, allowedSpecialModIds);
    }

    public static Set<Integer> effectiveAllowedModIds(AntRole role, Set<Integer> storedAllowed) {
        return AntSubtypeService.effectiveAllowedSubtypeIds(role, storedAllowed);
    }

    public static List<AntMod> listUnlockedSpecialMods(Colony colony) {
        List<AntSubtype> list = AntSubtypeService.listUnlockedSpecialSubtypes(colony);
        List<AntMod> mods = new ArrayList<>();
        for (AntSubtype s : list) {
            mods.add(s.toMod());
        }
        return mods;
    }

    public static int countStandardAntsOfClass(Colony colony, AntClass type) {
        return AntSubtypeService.countStandardAntsOfType(colony, type instanceof AntType t ? t : null);
    }

    public static int countAntsWithMod(Colony colony, AntClass type, AntMod mod) {
        return AntSubtypeService.countAntsWithSubtype(colony, type instanceof AntType t ? t : null, mod != null ? mod.toSubtype() : null);
    }

    public static Map<String, Integer> flattenRoleDisallowedMods(Map<AntRole, Set<Integer>> disallowedByRole) {
        return AntSubtypeService.flattenRoleDisallowedSubtypes(disallowedByRole);
    }

    public static Map<AntRole, Set<Integer>> unflattenRoleDisallowedMods(Map<String, Integer> flat) {
        return AntSubtypeService.unflattenRoleDisallowedSubtypes(flat);
    }

    public static void inheritMod(Ant source, Ant target, Colony colony) {
        AntSubtypeService.inheritSubtype(source, target, colony);
    }
}
