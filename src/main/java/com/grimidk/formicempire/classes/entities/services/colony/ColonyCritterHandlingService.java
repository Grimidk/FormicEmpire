package com.grimidk.formicempire.classes.entities.services.colony;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.critter.BugRole;
import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.critter.Critter;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;

public class ColonyCritterHandlingService {

    public static List<Species> getPetTypes() {
        List<Species> pets = new ArrayList<>();
        for (Species type : GameConstants.getCritterSpecies()) {
            if (type != null && type.hasBugRole(BugRole.PET)) {
                pets.add(type);
            }
        }
        return Collections.unmodifiableList(pets);
    }

    public static boolean isPetBug(Species type) {
        return type != null && type.hasBugRole(BugRole.PET);
    }

    public boolean canCatchPetBug(Colony colony, Species type) {
        if (colony == null || type == null || !isPetBug(type)) {
            return false;
        }
        if (type == GameConstants.TYPE_APHID) {
            return colony.hasUpgrade(GameUnlocks.ROLE_RANCHER);
        }
        if (type == GameConstants.TYPE_DERMESTID) {
            return colony.hasUpgrade(GameUnlocks.ABILITY_CATCH_DERMESTID);
        }
        if (type == GameConstants.TYPE_SYMBIOTIC_MITE) {
            return colony.hasUpgrade(GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE);
        }
        return false;
    }

    public int getCount(Colony colony, Species type) {
        if (colony == null || type == null) {
            return 0;
        }
        if (type == GameConstants.TYPE_APHID) {
            return colony.getAphids();
        }
        if (type == GameConstants.TYPE_SYMBIOTIC_MITE) {
            return colony.getSymbioticMites();
        }
        if (type == GameConstants.TYPE_DERMESTID) {
            return colony.getDermestids();
        }
        return 0;
    }

    public int getTotalPetCount(Colony colony) {
        int total = 0;
        for (Species type : getPetTypes()) {
            total += getCount(colony, type);
        }
        return total;
    }

    public int getCatcherPoolPetCount(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int total = 0;
        for (Species type : getPetTypes()) {
            if (type != GameConstants.TYPE_APHID) {
                total += getCount(colony, type);
            }
        }
        return total;
    }

    public int getUnlockedPetCount(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int total = 0;
        for (Species type : getPetTypes()) {
            if (canCatchPetBug(colony, type)) {
                total += getCount(colony, type);
            }
        }
        return total;
    }

    public int getUnlockedCatcherPoolPetCount(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int total = 0;
        for (Species type : getPetTypes()) {
            if (type != GameConstants.TYPE_APHID && canCatchPetBug(colony, type)) {
                total += getCount(colony, type);
            }
        }
        return total;
    }

    public int getUnlockedPetCapacityMax(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int aphidCap = 0;
        if (canCatchPetBug(colony, GameConstants.TYPE_APHID)) {
            aphidCap = getSpeciesTenderCapacity(colony, GameConstants.TYPE_APHID);
        }
        return aphidCap + getUnlockedCatcherPoolCapacityMax(colony);
    }

    public int getUnlockedCatcherPoolCapacityMax(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int speciesTotal = 0;
        for (Species type : getPetTypes()) {
            if (type != GameConstants.TYPE_APHID && canCatchPetBug(colony, type)) {
                speciesTotal += getSpeciesTenderCapacity(colony, type);
            }
        }
        if (speciesTotal <= 0) {
            return 0;
        }
        int pool = getCatcherPoolCapacity(colony);
        if (pool > 0) {
            return Math.min(speciesTotal, pool);
        }
        return 0;
    }

    public void setCount(Colony colony, Species type, int count) {
        if (colony == null || type == null || !isPetBug(type)) {
            return;
        }
        int capped = Math.max(0, Math.min(count, getMaxCapacity(colony, type)));
        colony.applyPetBugCount(type, capped);
        if (colony.runsFullSimulation()) {
            syncPetBugEntities(colony, type, capped);
        }
    }

    public int getCatcherPoolCapacity(Colony colony) {
        if (colony == null) {
            return 0;
        }
        return colony.getAssignedRoleCount(GameConstants.ROLE_CATCHER) * GameNumbers.PET_POOL_PER_CATCHER;
    }

    public int getSpeciesTenderCapacity(Colony colony, Species type) {
        if (colony == null || type == null || !canCatchPetBug(colony, type)) {
            return 0;
        }
        if (type == GameConstants.TYPE_APHID) {
            return effectiveRancherCount(colony) * colony.getStatsService().getAphidCapacity(colony);
        }
        if (type == GameConstants.TYPE_DERMESTID) {
            return colony.getAssignedRoleCount(GameConstants.ROLE_GRAVER) * GameNumbers.PET_CAPACITY_PER_TENDER;
        }
        if (type == GameConstants.TYPE_SYMBIOTIC_MITE) {
            return colony.getAssignedRoleCount(GameConstants.ROLE_CATCHER) * GameNumbers.PET_CAPACITY_PER_TENDER;
        }
        return 0;
    }

    public int getMaxCapacity(Colony colony, Species type) {
        if (colony == null || type == null || !isPetBug(type)) {
            return 0;
        }
        int speciesMax = getSpeciesTenderCapacity(colony, type);
        if (type == GameConstants.TYPE_APHID) {
            return Math.max(0, speciesMax);
        }
        int poolMax = getCatcherPoolCapacity(colony);
        int others = getCatcherPoolPetCount(colony) - getCount(colony, type);
        int poolRoom = poolMax - others;
        return Math.max(0, Math.min(speciesMax, poolRoom));
    }

    public void runDaily(Colony colony, Biome biome) {
        if (colony == null) {
            return;
        }
        runEscapes(colony);
        if (colony.hasUpgrade(GameUnlocks.ROLE_CATCHER)) {
            runCatching(colony, biome);
        }
        runBreeding(colony);
        runSymbioticMitePredation(colony);
    }

    public void runEscapes(Colony colony) {
        if (colony == null) {
            return;
        }
        int escapedTotal = 0;
        escapedTotal += escapeAphids(colony);
        escapedTotal += escapeCatcherPoolPets(colony);
        if (escapedTotal > 0) {
            colony.logEvent(ColonyLogPrefixes.INFO + " "
                    + String.format(LanguageStrings.get(LanguageStrings.LOG_PET_BUGS_ESCAPED_FMT), escapedTotal));
        }
    }

    private int escapeAphids(Colony colony) {
        if (colony.hasBuilding(GameUnlocks.PASSIVE_APHID)) {
            return 0;
        }
        int count = getCount(colony, GameConstants.TYPE_APHID);
        int max = getMaxCapacity(colony, GameConstants.TYPE_APHID);
        if (count <= max) {
            return 0;
        }
        int escaped = count - max;
        applyPetCountUnchecked(colony, GameConstants.TYPE_APHID, max);
        return escaped;
    }

    private int escapeCatcherPoolPets(Colony colony) {
        List<Species> poolTypes = new ArrayList<>();
        for (Species type : getPetTypes()) {
            if (type != GameConstants.TYPE_APHID) {
                poolTypes.add(type);
            }
        }
        if (poolTypes.isEmpty()) {
            return 0;
        }

        int escaped = 0;
        int[] targets = new int[poolTypes.size()];
        for (int i = 0; i < poolTypes.size(); i++) {
            Species type = poolTypes.get(i);
            int count = getCount(colony, type);
            int speciesMax = getSpeciesTenderCapacity(colony, type);
            targets[i] = Math.min(count, speciesMax);
            if (count > targets[i]) {
                escaped += count - targets[i];
            }
        }

        int poolMax = getCatcherPoolCapacity(colony);
        int sum = 0;
        for (int target : targets) {
            sum += target;
        }
        if (sum > poolMax) {
            int overflow = sum - poolMax;
            escaped += overflow;
            while (overflow > 0) {
                int richest = 0;
                for (int i = 1; i < targets.length; i++) {
                    if (targets[i] > targets[richest]) {
                        richest = i;
                    }
                }
                if (targets[richest] <= 0) {
                    break;
                }
                targets[richest]--;
                overflow--;
            }
        }

        for (int i = 0; i < poolTypes.size(); i++) {
            Species type = poolTypes.get(i);
            if (getCount(colony, type) != targets[i]) {
                applyPetCountUnchecked(colony, type, targets[i]);
            }
        }
        return escaped;
    }

    private void applyPetCountUnchecked(Colony colony, Species type, int count) {
        int sane = Math.max(0, count);
        colony.applyPetBugCount(type, sane);
        if (colony.runsFullSimulation()) {
            syncPetBugEntities(colony, type, sane);
        }
    }

    private void runCatching(Colony colony, Biome biome) {
        if (biome == null || biome.getNativeBugs().isEmpty()) {
            return;
        }
        int catcherCount = colony.getAssignedRoleCount(GameConstants.ROLE_CATCHER);
        if (catcherCount <= 0) {
            return;
        }

        int caughtTotal = 0;
        for (int i = 0; i < catcherCount; i++) {
            if (GameRandom.nextFloat() > GameNumbers.CATCH_BASE_CHANCE_PER_CATCHER) {
                continue;
            }
            Species nativeType = pickRandomNative(colony, biome);
            if (nativeType == null) {
                continue;
            }
            int current = getCount(colony, nativeType);
            int max = getMaxCapacity(colony, nativeType);
            if (current >= max) {
                continue;
            }
            setCount(colony, nativeType, current + 1);
            caughtTotal++;
        }

        if (caughtTotal > 0) {
            colony.logEvent(ColonyLogPrefixes.INFO + " "
                    + String.format(LanguageStrings.get(LanguageStrings.LOG_CAUGHT_BUGS_SUMMARY_FMT), caughtTotal));
        }
    }

    private void runBreeding(Colony colony) {
        for (Species type : getPetTypes()) {
            if (!canCatchPetBug(colony, type)) {
                continue;
            }
            int count = getCount(colony, type);
            if (count < GameNumbers.PET_BREED_MIN_COUNT) {
                continue;
            }
            double expected = count / (double) GameNumbers.PET_BREED_DIVISOR;
            int offspring = (int) expected;
            if (GameRandom.nextFloat() < (expected - offspring)) {
                offspring++;
            }
            int max = getMaxCapacity(colony, type);
            int room = max - count;
            if (offspring <= 0 || room <= 0) {
                continue;
            }
            int added = Math.min(offspring, room);
            setCount(colony, type, count + added);
            if (added > 0) {
                colony.logEvent(ColonyLogPrefixes.INFO + " "
                        + String.format(LanguageStrings.get(LanguageStrings.LOG_CAUGHT_BUG_BRED_FMT),
                                added, type.getName()));
            }
        }
    }

    private void runSymbioticMitePredation(Colony colony) {
        int symbioticMites = getCount(colony, GameConstants.TYPE_SYMBIOTIC_MITE);
        int parasiticMites = colony.getParasiticMites();
        if (symbioticMites <= 0 || parasiticMites <= 0) {
            return;
        }
        int killPerSymbioticMite = getSymbioticMiteParasiticMiteKillPerDay(colony);
        int eliminated = Math.min(parasiticMites, symbioticMites * killPerSymbioticMite);
        if (eliminated > 0) {
            setParasiticMiteCount(colony, parasiticMites - eliminated);
            colony.logEvent(ColonyLogPrefixes.INFO + " "
                    + String.format(LanguageStrings.get(LanguageStrings.LOG_SYMBIOTIC_MITES_PREDATION_FMT), eliminated));
        }
    }

    public int getSymbioticMiteParasiticMiteKillPerDay(Colony colony) {
        if (colony != null && colony.hasUpgrade(GameUnlocks.STAT_SYMBIOTIC_MITE_1)) {
            return GameNumbers.SYMBIOTIC_MITE_PARASITIC_MITE_KILL_UPGRADED;
        }
        return GameNumbers.SYMBIOTIC_MITE_PARASITIC_MITE_KILL_PER_DAY;
    }

    public int getDermestidGraveBonus(Colony colony) {
        if (colony == null || !colony.hasUpgrade(GameUnlocks.STAT_DERMESTID_1)) {
            return 0;
        }
        return getCount(colony, GameConstants.TYPE_DERMESTID);
    }

    public void setParasiteAntCount(Colony colony, int count) {
        if (colony == null) {
            return;
        }
        int capped = Math.max(0, count);
        colony.applyParasiteAntCount(capped);
        syncParasiteAntEntities(colony, capped);
    }

    public void restorePetCountsFromSave(Colony colony, int aphids, int symbioticMites, int dermestids) {
        if (colony == null) {
            return;
        }
        grantLegacyPetCatchUnlocks(colony, symbioticMites, dermestids);
        restorePetCountFromSave(colony, GameConstants.TYPE_APHID, aphids);
        restorePetCountFromSave(colony, GameConstants.TYPE_SYMBIOTIC_MITE, symbioticMites);
        restorePetCountFromSave(colony, GameConstants.TYPE_DERMESTID, dermestids);
    }

    public void restorePetCountFromSave(Colony colony, Species type, int count) {
        if (colony == null || type == null || !isPetBug(type)) {
            return;
        }
        int sane = Math.max(0, Math.min(count, petSaveLoadCap(colony, type)));
        colony.applyPetBugCount(type, sane);
        if (colony.runsFullSimulation()) {
            syncPetBugEntities(colony, type, sane);
        }
    }

    private int petSaveLoadCap(Colony colony, Species type) {
        int fromRoles = type == GameConstants.TYPE_APHID
                ? getSpeciesTenderCapacity(colony, type)
                : getMaxCapacity(colony, type);
        int fromScale = Math.max(
                GameNumbers.PARASITIC_MITE_MIN_MONTHLY_SPAWN,
                colony.getAntTotal() * GameNumbers.PET_CAPACITY_PER_TENDER);
        return Math.max(fromRoles, Math.min(GameNumbers.PET_COUNT_SAVE_ABS_MAX, fromScale));
    }

    public int resolvePetCountForSave(Colony colony, Species type) {
        if (colony == null || type == null) {
            return 0;
        }
        int count = getCount(colony, type);
        if (type == GameConstants.TYPE_APHID && colony.hasBuilding(GameUnlocks.PASSIVE_APHID)) {
            return count;
        }
        int cap = getMaxCapacity(colony, type);
        if (cap > 0) {
            return Math.min(count, cap);
        }
        return count;
    }

    public void syncPetBugEntities(Colony colony, Species type, int targetCount) {
        if (colony == null || type == null) {
            return;
        }
        int entityCount = GameNumbers.capPenNonAntSprites(targetCount);
        List<Critter> critters = colony.getCritters();
        long current = critters.stream().filter(b -> b.getSpecies() == type).count();
        if (current < entityCount) {
            int diff = entityCount - (int) current;
            Rectangle pen = resolvePenBounds(colony, type);
            if (pen == null) {
                pen = new Rectangle(10, 10, 256, 256);
            }
            for (int i = 0; i < diff; i++) {
                Critter newBug = new Critter(type);
                newBug.setDimension(WorldSpaces.OVERWORLD);
                ColonyPhysicsService physics = colony.getPhysicsService();
                if (physics != null) {
                    Point spawnPos = physics.getSpecificRoomPoint(colony, pen);
                    newBug.setPosition(spawnPos);
                }
                critters.add(newBug);
            }
        } else if (current > entityCount) {
            int diff = (int) current - entityCount;
            List<Critter> toRemove = new ArrayList<>();
            for (Critter b : critters) {
                if (b.getSpecies() == type) {
                    toRemove.add(b);
                    if (toRemove.size() == diff) {
                        break;
                    }
                }
            }
            critters.removeAll(toRemove);
        }
    }

    public void syncPetPenPositionsFromBounds(Colony colony) {
        if (colony == null) {
            return;
        }
        for (Species type : getPetTypes()) {
            int count = getCount(colony, type);
            if (count <= 0) {
                continue;
            }
            Rectangle pen = resolvePenBounds(colony, type);
            if (pen == null || pen.width <= 0 || pen.height <= 0) {
                continue;
            }

            long entityCount = colony.getCritters().stream()
                    .filter(b -> b.getSpecies() == type && b.getDimension() == WorldSpaces.OVERWORLD)
                    .count();
            int targetEntities = GameNumbers.capPenNonAntSprites(count);
            if (entityCount != targetEntities) {
                syncPetBugEntities(colony, type, count);
            }

            ColonyPhysicsService physics = colony.getPhysicsService();
            ImageIcon sprite = type.getSprite();
            int halfW = sprite != null ? Math.max(1, sprite.getIconWidth()) / 2 : 1;
            int halfH = sprite != null ? Math.max(1, sprite.getIconHeight()) / 2 : 1;
            for (Critter bug : colony.getCritters()) {
                if (bug.getSpecies() != type || bug.getDimension() != WorldSpaces.OVERWORLD) {
                    continue;
                }
                int cx = bug.getX() + halfW;
                int cy = bug.getY() + halfH;
                if (pen.contains(cx, cy)) {
                    continue;
                }
                if (physics != null) {
                    bug.setPosition(physics.getSpecificRoomPoint(colony, pen));
                }
            }
        }
    }

    public void syncParasiteAntEntities(Colony colony, int targetCount) {
        if (colony == null) {
            return;
        }
        List<Critter> critters = colony.getCritters();
        long current = critters.stream().filter(b -> b.getSpecies() == GameConstants.TYPE_PARASITE_ANT).count();
        if (current < targetCount) {
            int diff = targetCount - (int) current;
            Rectangle spawnRoom = colony.getStorageBounds();
            if (spawnRoom == null) {
                spawnRoom = new Rectangle(0, 0, 256, 256);
            }
            ColonyPhysicsService physics = colony.getPhysicsService();
            for (int i = 0; i < diff; i++) {
                Critter newBug = new Critter(GameConstants.TYPE_PARASITE_ANT);
                newBug.setDimension(WorldSpaces.UNDERWORLD);
                if (physics != null) {
                    Point spawnPos = physics.getSpecificRoomPoint(colony, spawnRoom);
                    newBug.setPosition(spawnPos);
                }
                critters.add(newBug);
            }
        } else if (current > targetCount) {
            int diff = (int) current - targetCount;
            List<Critter> toRemove = new ArrayList<>();
            for (Critter b : critters) {
                if (b.getSpecies() == GameConstants.TYPE_PARASITE_ANT) {
                    toRemove.add(b);
                    if (toRemove.size() == diff) {
                        break;
                    }
                }
            }
            critters.removeAll(toRemove);
        }
    }

    private Rectangle resolvePenBounds(Colony colony, Species type) {
        if (type == GameConstants.TYPE_APHID) {
            Rectangle rancher = colony.getRancherBounds();
            return rancher != null ? rancher : colony.getInsectPenBounds();
        }
        if (type == GameConstants.TYPE_DERMESTID) {
            Rectangle grave = colony.getGraverBounds();
            return grave != null ? grave : colony.getInsectPenBounds();
        }
        if (type == GameConstants.TYPE_SYMBIOTIC_MITE) {
            return colony.getInsectPenBounds();
        }
        return colony.getInsectPenBounds();
    }

    private void grantLegacyPetCatchUnlocks(Colony colony, int symbioticMites, int dermestids) {
        if (colony.getDynasty() == null) {
            return;
        }
        if (symbioticMites > 0 && !colony.hasUpgrade(GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE)) {
            colony.getDynasty().unlockUpgrade(GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE);
        }
        if (dermestids > 0 && !colony.hasUpgrade(GameUnlocks.ABILITY_CATCH_DERMESTID)) {
            colony.getDynasty().unlockUpgrade(GameUnlocks.ABILITY_CATCH_DERMESTID);
        }
    }

    private Species pickRandomNative(Colony colony, Biome biome) {
        List<Species> natives = biome.getNativeBugs();
        if (natives.isEmpty()) {
            return null;
        }
        List<Species> catchable = new ArrayList<>();
        for (Species type : natives) {
            if (canCatchPetBug(colony, type)) {
                catchable.add(type);
            }
        }
        if (catchable.isEmpty()) {
            return null;
        }
        return catchable.get(GameRandom.nextInt(catchable.size()));
    }

    private int effectiveRancherCount(Colony colony) {
        int rancherCount = colony.getAssignedRoleCount(GameConstants.ROLE_RANCHER);
        if (colony.hasBuilding(GameUnlocks.PASSIVE_APHID)) {
            if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) {
                rancherCount += 2;
            } else {
                rancherCount += 1;
            }
        }
        return rancherCount;
    }

    public boolean isParasiticMiteOutbreakEligible(Colony colony, Biome biome, Season season) {
        if (colony == null || biome == null || season == null) {
            return false;
        }
        if (!GameConstants.isParasiticMiteSeason(season)) {
            return false;
        }
        if (!biome.hasNativeParasite(GameConstants.TYPE_PARASITIC_MITE)) {
            return false;
        }
        long stored = colony.getResourceService().getStoredResourceTotal(colony);
        return stored >= GameNumbers.PARASITIC_MITE_RESOURCE_THRESHOLD;
    }

    public int calculateParasiticMiteSpawnAmount(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int existing = colony.getParasiticMites();
        int cap = parasiticMiteCap(colony);
        int room = cap - existing;
        if (room <= 0) {
            return 0;
        }

        int antTotal = colony.getAntTotal();
        int spawnFromAnts = Math.max(
                GameNumbers.PARASITIC_MITE_MIN_MONTHLY_SPAWN,
                antTotal * GameNumbers.PARASITIC_MITE_PER_ANT);
        int spawnAmount = spawnFromAnts;
        if (existing > 0) {
            spawnAmount += (int) (existing * GameNumbers.PARASITIC_MITE_SPREAD_FACTOR);
        }
        return Math.min(spawnAmount, room);
    }

    public int projectParasiticMiteMonthlySpawn(Colony colony, Biome biome, Season season) {
        if (!isParasiticMiteOutbreakEligible(colony, biome, season)) {
            return 0;
        }
        return calculateParasiticMiteSpawnAmount(colony);
    }

    public int requiredSymbioticMitesToPreventOutbreak(Colony colony, Biome biome, Season season) {
        int spawn = projectParasiticMiteMonthlySpawn(colony, biome, season);
        if (spawn <= 0) {
            return 0;
        }
        return spawn * GameNumbers.PARASITE_OUTBREAK_PREVENTION_MULTIPLIER;
    }

    public boolean isParasiticMiteOutbreakPrevented(Colony colony, Biome biome, Season season) {
        int required = requiredSymbioticMitesToPreventOutbreak(colony, biome, season);
        if (required <= 0) {
            return false;
        }
        return colony.getSymbioticMites() >= required;
    }

    public void runMonthlyParasiticMites(Colony colony, Biome biome, Season season) {
        if (!isParasiticMiteOutbreakEligible(colony, biome, season)) {
            return;
        }

        int spawnAmount = calculateParasiticMiteSpawnAmount(colony);
        if (spawnAmount <= 0) {
            return;
        }
        if (isParasiticMiteOutbreakPrevented(colony, biome, season)) {
            return;
        }
        if (GameRandom.nextFloat() > GameNumbers.PARASITE_OUTBREAK_CHANCE) {
            return;
        }

        setParasiticMiteCount(colony, colony.getParasiticMites() + spawnAmount);
        colony.logEvent(ColonyLogPrefixes.INFO + " "
                + String.format(LanguageStrings.get(LanguageStrings.LOG_PARASITIC_MITE_SPREAD_FMT), spawnAmount));
    }

    public void runMonthlyParasiticMitesLite(Colony colony, Biome biome, Season season) {
        if (!isParasiticMiteOutbreakEligible(colony, biome, season)) {
            return;
        }

        int spawnAmount = calculateParasiticMiteSpawnAmount(colony);
        if (spawnAmount <= 0) {
            return;
        }
        if (isParasiticMiteOutbreakPrevented(colony, biome, season)) {
            return;
        }
        if (GameRandom.nextFloat() > GameNumbers.PARASITE_OUTBREAK_CHANCE) {
            return;
        }

        applyParasiticMiteCountLite(colony, colony.getParasiticMites() + spawnAmount);
        colony.logEvent(ColonyLogPrefixes.INFO + " "
                + String.format(LanguageStrings.get(LanguageStrings.LOG_PARASITIC_MITE_SPREAD_FMT), spawnAmount));
    }

    public void runSymbioticMitePredationLite(Colony colony) {
        if (colony == null) {
            return;
        }
        int symbioticMites = getCount(colony, GameConstants.TYPE_SYMBIOTIC_MITE);
        int parasiticMites = colony.getParasiticMites();
        if (symbioticMites <= 0 || parasiticMites <= 0) {
            return;
        }
        int killPerSymbioticMite = getSymbioticMiteParasiticMiteKillPerDay(colony);
        int eliminated = Math.min(parasiticMites, symbioticMites * killPerSymbioticMite);
        if (eliminated > 0) {
            applyParasiticMiteCountLite(colony, parasiticMites - eliminated);
            colony.logEvent(ColonyLogPrefixes.INFO + " "
                    + String.format(LanguageStrings.get(LanguageStrings.LOG_SYMBIOTIC_MITES_PREDATION_FMT), eliminated));
        }
    }

    public void applyParasiticMiteCountLite(Colony colony, int count) {
        if (colony == null) {
            return;
        }
        int capped = Math.min(Math.max(0, count), parasiticMiteCap(colony));
        colony.applyParasiticMiteCount(capped);
    }

    private int parasiticMiteCap(Colony colony) {
        int antTotal = colony.getAntTotal();
        int fromPopulation = antTotal * GameNumbers.PARASITIC_MITES_PER_SLOWED_ANT;
        return Math.max(fromPopulation, GameNumbers.PARASITIC_MITE_MIN_MONTHLY_SPAWN);
    }

    public void setParasiticMiteCount(Colony colony, int count) {
        if (colony == null) {
            return;
        }
        int capped = Math.min(Math.max(0, count), parasiticMiteCap(colony));
        colony.applyParasiticMiteCount(capped);
        syncParasiticMiteInfections(colony);
    }

    public int getParasiticMiteSlowedAntCount(Colony colony) {
        if (colony == null) {
            return 0;
        }
        return colony.getParasiticMites() / GameNumbers.PARASITIC_MITES_PER_SLOWED_ANT;
    }

    public void syncParasiticMiteInfections(Colony colony) {
        if (colony == null) {
            return;
        }
        int targetInfected = getParasiticMiteSlowedAntCount(colony);
        List<Ant> candidates = collectParasiticMiteCandidates(colony);
        for (Ant ant : candidates) {
            ant.setParasiticMiteInfected(false);
        }
        if (targetInfected <= 0 || candidates.isEmpty()) {
            return;
        }
        Collections.shuffle(candidates, GameRandom.getShuffleRandom());
        int toInfect = Math.min(targetInfected, candidates.size());
        for (int i = 0; i < toInfect; i++) {
            candidates.get(i).setParasiticMiteInfected(true);
        }
    }

    private List<Ant> collectParasiticMiteCandidates(Colony colony) {
        List<Ant> candidates = new ArrayList<>();
        for (AntType type : List.of(
                GameConstants.TYPE_WORKER,
                GameConstants.TYPE_SOLDIER,
                GameConstants.TYPE_MAJOR,
                GameConstants.TYPE_DRONE,
                GameConstants.TYPE_PRINCESS,
                GameConstants.TYPE_QUEEN)) {
            List<Ant> ants = colony.getAntsByType(type);
            if (ants == null) {
                continue;
            }
            for (Ant ant : ants) {
                if (ant.isAlive()) {
                    candidates.add(ant);
                }
            }
        }
        return candidates;
    }
}
