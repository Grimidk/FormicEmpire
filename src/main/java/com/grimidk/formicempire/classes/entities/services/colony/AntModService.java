package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
import com.grimidk.formicempire.classes.constants.critter.ant.AntMod;
import com.grimidk.formicempire.classes.constants.critter.ant.AntModProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntModSlot;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
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
        target.setModHatchRates(deepCopyRates(source.getModHatchRates()));
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
        if (colony == null || !hasModAssimilation(colony)) {
            return;
        }

        Map<AntClass, Map<AntModSlot, Map<Integer, Float>>> rates = defaultModRates();

        boolean trapjaw = colony.hasUpgrade(GameUnlocks.ASSIMILATED_TRAPJAW);
        boolean honeypot = colony.hasUpgrade(GameUnlocks.ASSIMILATED_HONEYPOT);
        boolean doorhead = colony.hasUpgrade(GameUnlocks.ASSIMILATED_DOORHEAD);
        boolean bullet = colony.hasUpgrade(GameUnlocks.ASSIMILATED_STINGING);
        boolean farsight = colony.hasUpgrade(GameUnlocks.ASSIMILATED_FARSIGHT);
        boolean leafcutter = colony.hasUpgrade(GameUnlocks.ASSIMILATED_FARMING);

        if (honeypot) {
            setAutomatedSlotRate(rates, GameConstants.CLASS_WORKER, AntModSlot.ABDOMEN, 3, 50f);
            setAutomatedSlotRate(rates, GameConstants.CLASS_PRINCESS, AntModSlot.ABDOMEN, 3, 10f);
        }
        if (trapjaw) {
            setAutomatedSlotRate(rates, GameConstants.CLASS_SOLDIER, AntModSlot.HEAD, 2, 100f);
            if (colony.hasUpgrade(GameUnlocks.TYPE_MAJOR)) {
                setAutomatedSlotRate(rates, GameConstants.CLASS_MAJOR, AntModSlot.HEAD, 2, 100f);
            }
        }
        if (doorhead) {
            setAutomatedSlotRate(rates, GameConstants.CLASS_WORKER, AntModSlot.HEAD, 3, 50f);
            if (!trapjaw && colony.hasUpgrade(GameUnlocks.TYPE_SOLDIER)) {
                setAutomatedSlotRate(rates, GameConstants.CLASS_SOLDIER, AntModSlot.HEAD, 3, 50f);
            }
        }
        if (leafcutter && !doorhead) {
            setAutomatedSlotRate(rates, GameConstants.CLASS_WORKER, AntModSlot.HEAD,
                    GameConstants.MOD_HEAD_LEAFCUTTER.getDigit(), 50f);
        }
        if (farsight && !trapjaw) {
            if (colony.hasUpgrade(GameUnlocks.TYPE_SOLDIER)) {
                setAutomatedSlotRate(rates, GameConstants.CLASS_SOLDIER, AntModSlot.HEAD,
                        GameConstants.MOD_HEAD_FARSIGHT.getDigit(), 50f);
            }
            if (colony.hasUpgrade(GameUnlocks.TYPE_MAJOR)) {
                setAutomatedSlotRate(rates, GameConstants.CLASS_MAJOR, AntModSlot.HEAD,
                        GameConstants.MOD_HEAD_FARSIGHT.getDigit(), 50f);
            }
        }
        if (bullet) {
            setAutomatedSlotRate(rates, GameConstants.CLASS_SOLDIER, AntModSlot.ABDOMEN, 2, 100f);
            if (colony.hasUpgrade(GameUnlocks.TYPE_MAJOR)) {
                setAutomatedSlotRate(rates, GameConstants.CLASS_MAJOR, AntModSlot.ABDOMEN, 2, 100f);
            }
        }

        colony.setModHatchRates(rates);
    }

    private static void setAutomatedSlotRate(
            Map<AntClass, Map<AntModSlot, Map<Integer, Float>>> rates,
            AntClass type, AntModSlot slot, int digit, float modPct) {
        if (rates == null || type == null || slot == null) {
            return;
        }
        float clamped = Math.min(100f, Math.max(0f, modPct));
        Map<Integer, Float> slotRates = rates.computeIfAbsent(type, ignored -> defaultRatesForClass()).get(slot);
        slotRates.clear();
        slotRates.put(digit, clamped);
        slotRates.put(AntMod.DIGIT_NONE, 100f - clamped);
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
        if (colony == null || species == null) {
            return;
        }
        Map<AntClass, Map<AntModSlot, Map<Integer, Float>>> rates = defaultModRates();
        for (Upgrade trait : species.getBaseUpgrades()) {
            AntMod mod = findModForUpgrade(trait);
            if (mod == null || mod.isNone()) {
                continue;
            }
            Map<Integer, Float> slotRates = new HashMap<>();
            slotRates.put(AntMod.DIGIT_NONE, 100f - NPC_NATURAL_MOD_RATE);
            slotRates.put(mod.getDigit(), NPC_NATURAL_MOD_RATE);
            for (AntClass type : getModRateClasses()) {
                rates.get(type).put(mod.getSlot(), slotRates);
            }
        }
        colony.setModHatchRates(rates);
    }

    public static void assignNaturalModsToPopulation(Colony colony) {
        if (colony == null) {
            return;
        }
        assignNaturalMods(colony, colony.getWorkers());
        assignNaturalMods(colony, colony.getSoldiers());
        assignNaturalMods(colony, colony.getMajors());
        assignNaturalMods(colony, colony.getPrincesses());
        assignNaturalMods(colony, colony.getQueens());
    }

    private static void assignNaturalMods(Colony colony, List<Ant> ants) {
        if (ants == null) {
            return;
        }
        for (Ant ant : ants) {
            if (ant == null || !isEligibleClass(ant.getAntClass())) {
                continue;
            }
            AntModProfile profile = rollProfile(colony, ant.getAntClass());
            ant.setModProfile(profile);
            applyModStats(ant, colony);
        }
    }

    private static AntMod findModForUpgrade(Upgrade upgrade) {
        if (upgrade == null) {
            return null;
        }
        for (AntMod mod : GameConstants.getAntMods()) {
            if (upgrade.equals(mod.getRequiredUpgrade())) {
                return mod;
            }
        }
        return null;
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
            cumulative += colony.getModHatchRate(type, slot, mod.getDigit()) * foodScale;
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
        AntClass type = ant.getAntClass();
        AntModProfile profile = ant.getModProfile();
        if (profile == null) {
            profile = AntModProfile.standard();
            ant.setModProfile(profile);
        }

        ant.setMaxHealth(Math.max(0, Math.round(colony.getBaseHealth() * type.getHealtMult() * combinedHealthMult(profile))));
        if (ant.getHealth() > ant.getMaxHealth()) {
            ant.setHealth(ant.getMaxHealth());
        }
        ant.setRegen(Math.round(colony.getBaseRegen() * type.getRegenMult() * combinedRegenMult(profile)));
        ant.setConsumption(colony.getBaseConsumption() * type.getConsumptionMult() * consumptionMult(profile));
        ant.setAttack((int) (colony.getBaseAttack() * type.getAttackMult()));
        ant.setAttackSpeed((int) (colony.getBaseAttackSpeed() * type.getAttackSpeedMult()));
        ant.setDefense(GameNumbers.clampDefensePercent(
                type.getDefenseMult() + colony.getBaseDefense() + combinedDefenseBonus(profile)));
        ant.setSpeed(colony.getBaseSpeed() * type.getSpeedMult() * combinedSpeedMult(profile));
        ant.setEvasionChance(colony.getBaseEvasionChance());
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

    private static float combinedHealthMult(AntModProfile profile) {
        return 1f;
    }

    public static float computeCombatStatMultiplier(AntClass type, AntModProfile profile, Colony colony) {
        if (colony == null || type == null || !isEligibleClass(type)) {
            return 0f;
        }
        ColonyStatsService stats = colony.getStatsService();
        if (stats == null) {
            return 0f;
        }
        AntModProfile resolved = profile != null ? profile : AntModProfile.standard();
        int hp = Math.max(0, Math.round(stats.getBaseHealth(colony) * type.getHealtMult() * combinedHealthMult(resolved)));
        int atk = (int) (stats.getBaseAttack(colony) * type.getAttackMult());
        int def = Math.round(GameNumbers.clampDefensePercent(
                type.getDefenseMult() + stats.getBaseDefense(colony) + combinedDefenseBonus(resolved)));
        int atkSpd = (int) (stats.getBaseAttackSpeed(colony) * type.getAttackSpeedMult());
        return ColonyMilitaryService.computeStatMultiplierFromBases(hp, atk, def, atkSpd);
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
        if (type == null || !isEligibleClass(type)) {
            return 0f;
        }
        AntModProfile resolved = profile != null ? profile : AntModProfile.standard();
        int hp = Math.max(0, Math.round(baseHealth * type.getHealtMult() * combinedHealthMult(resolved)));
        int atk = (int) (baseAttack * type.getAttackMult());
        int def = Math.round(GameNumbers.clampDefensePercent(
                type.getDefenseMult() + baseDefense + combinedDefenseBonus(resolved)));
        int atkSpd = (int) (baseAttackSpeed * type.getAttackSpeedMult());
        return ColonyMilitaryService.computeStatMultiplierFromBases(hp, atk, def, atkSpd);
    }

    public static float computeModCombatFactor(AntClass type, AntModProfile profile, Colony colony) {
        float standardMult = computeCombatStatMultiplier(type, AntModProfile.standard(), colony);
        float actualMult = computeCombatStatMultiplier(type, profile, colony);
        if (standardMult <= 0f) {
            return 1f;
        }
        return actualMult / standardMult;
    }

    public static float averageModCombatFactor(Colony colony, List<Ant> ants, AntClass type) {
        if (colony == null || type == null || ants == null || ants.isEmpty()) {
            return 1f;
        }
        return weightedModCombatFactor(colony, type, aggregateModCounts(ants));
    }

    public static float forageMult(Ant ant) {
        if (ant == null) {
            return 1f;
        }
        AntModProfile profile = ant.getModProfile();
        if (profile == null) {
            return 1f;
        }
        float mult = 1f;
        for (AntModSlot slot : AntModSlot.values()) {
            AntMod mod = profile.getMod(slot);
            if (mod != null && !mod.isNone()) {
                mult *= mod.getForageMult();
            }
        }
        return mult;
    }

    public static int forageCarrySlots(Ant ant) {
        return Math.max(1, Math.round(forageMult(ant)));
    }

    public static int sumCollectingPowerFromRoleCount(Colony colony, int workerCount) {
        if (colony == null || workerCount <= 0) {
            return 0;
        }
        float baseRate = colony.getStatsService().getCollectingRate(colony);
        if (baseRate <= 0f) {
            return 0;
        }
        return Math.max(workerCount, Math.round(workerCount * baseRate));
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

    public static AntModProfile sampleProfileFromColony(Colony colony, AntClass type) {
        if (colony == null || type == null) {
            return AntModProfile.standard();
        }
        List<Ant> ants = colony.getAntsByClass(type);
        if (ants.isEmpty()) {
            return rollProfile(colony, type);
        }
        Ant picked = ants.get(GameRandom.nextInt(ants.size()));
        AntModProfile profile = picked.getModProfile();
        return profile != null ? profile : AntModProfile.standard();
    }

    public static AntModProfile sampleProfileFromDynasty(Dynasty dynasty, AntClass type) {
        if (dynasty == null || type == null) {
            return AntModProfile.standard();
        }
        List<Ant> pool = new ArrayList<>();
        for (Colony colony : dynasty.getColonies()) {
            if (colony != null) {
                pool.addAll(colony.getAntsByClass(type));
            }
        }
        if (pool.isEmpty()) {
            Colony capital = dynasty.getCapital();
            return capital != null ? rollProfile(capital, type) : AntModProfile.standard();
        }
        Ant picked = pool.get(GameRandom.nextInt(pool.size()));
        AntModProfile profile = picked.getModProfile();
        return profile != null ? profile : AntModProfile.standard();
    }

    public static Map<String, Integer> aggregateModCounts(List<Ant> ants) {
        Map<String, Integer> counts = new HashMap<>();
        if (ants == null) {
            return counts;
        }
        for (Ant ant : ants) {
            if (ant == null || !ant.isAlive()) {
                continue;
            }
            AntModProfile profile = ant.getModProfile();
            int code = profile != null ? profile.getCode() : AntModProfile.STANDARD_CODE;
            String key = String.valueOf(code);
            counts.merge(key, 1, Integer::sum);
        }
        return counts;
    }

    public static float weightedModCombatFactor(Colony colony, AntClass type, Map<String, Integer> modCounts) {
        if (colony == null || type == null || modCounts == null || modCounts.isEmpty()) {
            return 1f;
        }
        float weightedSum = 0f;
        int total = 0;
        for (Map.Entry<String, Integer> entry : modCounts.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            AntModProfile profile = AntModProfile.fromCode(Integer.parseInt(entry.getKey()));
            weightedSum += entry.getValue() * computeModCombatFactor(type, profile, colony);
            total += entry.getValue();
        }
        return total > 0 ? weightedSum / total : 1f;
    }

    public static int sumMilitaryPointsForModCounts(Colony colony, AntClass type, Map<String, Integer> modCounts) {
        int weight = GameConstants.getMilitaryWeightForAntClass(type);
        if (colony == null || weight == 0 || modCounts == null || modCounts.isEmpty()) {
            return 0;
        }
        float colonyMult = ColonyMilitaryService.computeStatMultiplier(colony);
        int total = 0;
        for (Map.Entry<String, Integer> entry : modCounts.entrySet()) {
            int count = entry.getValue();
            if (count <= 0) {
                continue;
            }
            AntModProfile profile = AntModProfile.fromCode(Integer.parseInt(entry.getKey()));
            float modFactor = computeModCombatFactor(type, profile, colony);
            total += Math.round(count * weight * colonyMult * modFactor);
        }
        return total;
    }

    public static void populateAntsFromModCounts(Colony colony, List<Ant> list, AntClass type,
            Map<String, Integer> modCounts, int legacyCount) {
        list.clear();
        if (modCounts != null && !modCounts.isEmpty()) {
            for (Map.Entry<String, Integer> entry : modCounts.entrySet()) {
                int code = Integer.parseInt(entry.getKey());
                AntModProfile profile = AntModProfile.fromCode(code);
                for (int i = 0; i < entry.getValue(); i++) {
                    list.add(createAnt(colony, type, profile));
                }
            }
            return;
        }
        for (int i = 0; i < legacyCount; i++) {
            list.add(createAnt(colony, type, AntModProfile.standard()));
        }
    }

    public static Ant createAnt(Colony colony, AntClass type, AntModProfile profile) {
        Ant ant = new Ant(colony, type);
        if (isEligibleClass(type) && profile != null) {
            ant.setModProfile(profile);
            applyModStats(ant, colony);
        }
        if (type == GameConstants.CLASS_QUEEN) {
            ant.setDimension(WorldSpaces.UNDERWORLD);
        } else if (isEligibleClass(type)) {
            ant.setDimension(WorldSpaces.OVERWORLD);
        }
        return ant;
    }

    public static Map<String, Double> flattenModRates(
            Map<AntClass, Map<AntModSlot, Map<Integer, Float>>> rates) {
        Map<String, Double> flat = new HashMap<>();
        if (rates == null) {
            return flat;
        }
        for (Map.Entry<AntClass, Map<AntModSlot, Map<Integer, Float>>> typeEntry : rates.entrySet()) {
            AntClass type = typeEntry.getKey();
            if (type == null) {
                continue;
            }
            for (Map.Entry<AntModSlot, Map<Integer, Float>> slotEntry : typeEntry.getValue().entrySet()) {
                for (Map.Entry<Integer, Float> rateEntry : slotEntry.getValue().entrySet()) {
                    flat.put(type.getNameKey() + "|" + slotEntry.getKey().name() + "|" + rateEntry.getKey(),
                            rateEntry.getValue().doubleValue());
                }
            }
        }
        return flat;
    }

    public static Map<AntClass, Map<AntModSlot, Map<Integer, Float>>> unflattenModRates(Map<String, Double> flat) {
        Map<AntClass, Map<AntModSlot, Map<Integer, Float>>> rates = defaultModRates();
        if (flat == null || flat.isEmpty()) {
            return rates;
        }
        Map<AntModSlot, Map<Integer, Float>> legacyRates = new EnumMap<>(AntModSlot.class);
        for (Map.Entry<String, Double> entry : flat.entrySet()) {
            String key = entry.getKey();
            String[] parts = key.contains("|") ? key.split("\\|", -1) : key.split(":", -1);
            if (parts.length == 2) {
                try {
                    AntModSlot slot = AntModSlot.valueOf(parts[0]);
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
                AntClass type = resolveModRateClass(parts[0]);
                if (type == null) {
                    continue;
                }
                AntModSlot slot = AntModSlot.valueOf(parts[1]);
                int digit = Integer.parseInt(parts[2]);
                rates.computeIfAbsent(type, ignored -> defaultSlotRates())
                        .computeIfAbsent(slot, ignored -> new HashMap<>())
                        .put(digit, entry.getValue().floatValue());
            } catch (Exception ignored) {
            }
        }
        if (!legacyRates.isEmpty()) {
            for (AntClass type : getModRateClasses()) {
                for (Map.Entry<AntModSlot, Map<Integer, Float>> slotEntry : legacyRates.entrySet()) {
                    rates.get(type).put(slotEntry.getKey(), new HashMap<>(slotEntry.getValue()));
                }
            }
        }
        return rates;
    }

    private static AntClass resolveModRateClass(String key) {
        for (AntClass type : getModRateClasses()) {
            if (type.getNameKey().equals(key)) {
                return type;
            }
        }
        return null;
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
        if (ant == null) {
            return true;
        }
        AntModProfile profile = ant.getModProfile();
        return profile == null || profile.countActiveMods() == 0;
    }

    public static boolean isAntEligibleForRole(Ant ant, AntRole role, Set<Integer> allowedSpecialModIds) {
        if (ant == null || role == null) {
            return false;
        }
        AntModProfile profile = ant.getModProfile();
        if (profile == null) {
            profile = AntModProfile.standard();
        }

        for (AntMod required : role.getRequiredMods()) {
            if (!profileHasMod(profile, required)) {
                return false;
            }
        }

        if (profile.countActiveMods() == 0) {
            return !role.requiresMods();
        }

        Set<Integer> allowed = allowedSpecialModIds != null ? allowedSpecialModIds : Set.of();
        for (AntModSlot slot : GameConstants.getConfigurableModSlots()) {
            AntMod mod = profile.getMod(slot);
            if (mod == null || mod.isNone()) {
                continue;
            }
            if (role.isModForcedAllowed(mod)) {
                continue;
            }
            if (!allowed.contains(mod.getId())) {
                return false;
            }
        }
        return true;
    }

    public static Set<Integer> effectiveAllowedModIds(AntRole role, Set<Integer> storedAllowed) {
        Set<Integer> effective = new HashSet<>();
        if (storedAllowed != null) {
            effective.addAll(storedAllowed);
        }
        if (role != null) {
            for (AntMod forced : role.getForcedAllowedMods()) {
                effective.add(forced.getId());
            }
        }
        return effective;
    }

    public static List<AntMod> listUnlockedSpecialMods(Colony colony) {
        List<AntMod> unlocked = new ArrayList<>();
        if (colony == null) {
            return unlocked;
        }
        for (AntModSlot slot : GameConstants.getConfigurableModSlots()) {
            for (AntMod mod : getAvailableMods(colony, slot)) {
                if (!mod.isNone()) {
                    unlocked.add(mod);
                }
            }
        }
        return unlocked;
    }

    public static int countStandardAntsOfClass(Colony colony, AntClass type) {
        if (colony == null || type == null) {
            return 0;
        }
        int count = 0;
        for (Ant ant : colony.getAntsByClass(type)) {
            if (ant.isAlive() && isStandardMorph(ant)) {
                count++;
            }
        }
        return count;
    }

    public static int countAntsWithMod(Colony colony, AntClass type, AntMod mod) {
        if (colony == null || type == null || mod == null || mod.isNone()) {
            return 0;
        }
        int count = 0;
        for (Ant ant : colony.getAntsByClass(type)) {
            if (ant.isAlive() && antHasMod(ant, mod)) {
                count++;
            }
        }
        return count;
    }

    public static Map<String, Integer> flattenRoleDisallowedMods(Map<AntRole, Set<Integer>> disallowedByRole) {
        Map<String, Integer> flat = new HashMap<>();
        if (disallowedByRole == null) {
            return flat;
        }
        for (Map.Entry<AntRole, Set<Integer>> entry : disallowedByRole.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            for (Integer modId : entry.getValue()) {
                if (modId != null) {
                    flat.put(entry.getKey().getId() + "_" + modId, 1);
                }
            }
        }
        return flat;
    }

    public static Map<AntRole, Set<Integer>> unflattenRoleDisallowedMods(Map<String, Integer> flat) {
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
                int modId = Integer.parseInt(parts[1]);
                if (role != null && GameConstants.getAntModById(modId) != null) {
                    result.computeIfAbsent(role, ignored -> new HashSet<>()).add(modId);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    public static void inheritMod(Ant source, Ant target, Colony colony) {
        if (source == null || target == null || colony == null) {
            return;
        }
        AntModProfile profile = source.getModProfile();
        if (profile == null) {
            profile = AntModProfile.standard();
        }
        target.setModProfile(profile);
        applyModStats(target, colony);
    }
}
