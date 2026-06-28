package com.grimidk.formicempire.classes.entities.services;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.grimidk.formicempire.classes.infrasctructure.repositories.GameRandom;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.BugType;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Bug;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;

public class ColonyBugHandlingService {

    private static final List<BugType> PET_TYPES = List.of(
            GameConstants.TYPE_APHID,
            GameConstants.TYPE_SYMBIOTIC_MITE,
            GameConstants.TYPE_DERMESTID);

    public static List<BugType> getPetTypes() {
        return PET_TYPES;
    }

    public static boolean isPetBug(BugType type) {
        return type != null && PET_TYPES.contains(type);
    }

    public boolean canCatchPetBug(Colony colony, BugType type) {
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

    public int getCount(Colony colony, BugType type) {
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
        for (BugType type : PET_TYPES) {
            total += getCount(colony, type);
        }
        return total;
    }

    public int getUnlockedPetCount(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int total = 0;
        for (BugType type : PET_TYPES) {
            if (canCatchPetBug(colony, type)) {
                total += getCount(colony, type);
            }
        }
        return total;
    }

    /** Display cap: sum of tender capacity for unlocked pet species, limited by the shared catcher pool when present. */
    public int getUnlockedPetCapacityMax(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int speciesTotal = 0;
        for (BugType type : PET_TYPES) {
            if (canCatchPetBug(colony, type)) {
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
        return speciesTotal;
    }

    public void setCount(Colony colony, BugType type, int count) {
        if (colony == null || type == null || !isPetBug(type)) {
            return;
        }
        int capped = Math.max(0, Math.min(count, getMaxCapacity(colony, type)));
        colony.applyPetBugCount(type, capped);
        syncPetBugEntities(colony, type, capped);
    }

    public int getCatcherPoolCapacity(Colony colony) {
        if (colony == null) {
            return 0;
        }
        return colony.getAssignedRoleCount(GameConstants.ROLE_CATCHER) * GameConstants.PET_POOL_PER_CATCHER;
    }

    public int getSpeciesTenderCapacity(Colony colony, BugType type) {
        if (colony == null || type == null || !canCatchPetBug(colony, type)) {
            return 0;
        }
        if (type == GameConstants.TYPE_APHID) {
            return effectiveRancherCount(colony) * colony.getStatsService().getAphidCapacity(colony);
        }
        if (type == GameConstants.TYPE_DERMESTID) {
            return colony.getAssignedRoleCount(GameConstants.ROLE_GRAVER) * GameConstants.PET_CAPACITY_PER_TENDER;
        }
        if (type == GameConstants.TYPE_SYMBIOTIC_MITE) {
            return colony.getAssignedRoleCount(GameConstants.ROLE_CATCHER) * GameConstants.PET_CAPACITY_PER_TENDER;
        }
        return 0;
    }

    public int getMaxCapacity(Colony colony, BugType type) {
        if (colony == null || type == null || !isPetBug(type)) {
            return 0;
        }
        int speciesMax = getSpeciesTenderCapacity(colony, type);
        int poolMax = getCatcherPoolCapacity(colony);
        int others = getTotalPetCount(colony) - getCount(colony, type);
        int poolRoom = poolMax - others;
        if (speciesMax == Integer.MAX_VALUE) {
            return Math.max(0, poolRoom);
        }
        return Math.max(0, Math.min(speciesMax, poolRoom));
    }

    public void runDaily(Colony colony, Biome biome) {
        if (colony == null) {
            return;
        }
        if (colony.hasUpgrade(GameUnlocks.ROLE_CATCHER)) {
            runCatching(colony, biome);
        }
        runBreeding(colony);
        runSymbioticMitePredation(colony);
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
            if (GameRandom.nextFloat() > GameConstants.CATCH_BASE_CHANCE_PER_CATCHER) {
                continue;
            }
            BugType nativeType = pickRandomNative(colony, biome);
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
        for (BugType type : PET_TYPES) {
            if (!canCatchPetBug(colony, type)) {
                continue;
            }
            int count = getCount(colony, type);
            if (count < GameConstants.PET_BREED_MIN_COUNT) {
                continue;
            }
            int offspring = count / 2;
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
            return GameConstants.SYMBIOTIC_MITE_PARASITIC_MITE_KILL_UPGRADED;
        }
        return GameConstants.SYMBIOTIC_MITE_PARASITIC_MITE_KILL_PER_DAY;
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

    public void restorePetCountFromSave(Colony colony, BugType type, int count) {
        if (colony == null || type == null || !isPetBug(type)) {
            return;
        }
        int sane = Math.max(0, Math.min(count, petSaveLoadCap(colony, type)));
        colony.applyPetBugCount(type, sane);
        syncPetBugEntities(colony, type, sane);
    }

    private int petSaveLoadCap(Colony colony, BugType type) {
        int fromRoles = getMaxCapacity(colony, type);
        int fromScale = Math.max(
                GameConstants.PARASITIC_MITE_MIN_MONTHLY_SPAWN,
                colony.getAntTotal() * GameConstants.PET_CAPACITY_PER_TENDER);
        return Math.max(fromRoles, Math.min(GameConstants.PET_COUNT_SAVE_ABS_MAX, fromScale));
    }

    public int resolvePetCountForSave(Colony colony, BugType type) {
        if (colony == null || type == null) {
            return 0;
        }
        return getCount(colony, type);
    }

    public void syncPetBugEntities(Colony colony, BugType type, int targetCount) {
        if (colony == null || type == null) {
            return;
        }
        int entityCount = GameConstants.capPenNonAntSprites(targetCount);
        List<Bug> bugs = colony.getBugs();
        long current = bugs.stream().filter(b -> b.getBugType() == type).count();
        if (current < entityCount) {
            int diff = entityCount - (int) current;
            Rectangle pen = resolvePenBounds(colony, type);
            if (pen == null) {
                pen = new Rectangle(10, 10, 256, 256);
            }
            for (int i = 0; i < diff; i++) {
                Bug newBug = new Bug(type);
                newBug.setDimension(WorldSpaces.OVERWORLD);
                ColonyPhysicsService physics = colony.getPhysicsService();
                if (physics != null) {
                    Point spawnPos = physics.getSpecificRoomPoint(colony, pen);
                    newBug.setPosition(spawnPos);
                }
                bugs.add(newBug);
            }
        } else if (current > entityCount) {
            int diff = (int) current - entityCount;
            List<Bug> toRemove = new ArrayList<>();
            for (Bug b : bugs) {
                if (b.getBugType() == type) {
                    toRemove.add(b);
                    if (toRemove.size() == diff) {
                        break;
                    }
                }
            }
            bugs.removeAll(toRemove);
        }
    }

    public void syncParasiteAntEntities(Colony colony, int targetCount) {
        if (colony == null) {
            return;
        }
        List<Bug> bugs = colony.getBugs();
        long current = bugs.stream().filter(b -> b.getBugType() == GameConstants.TYPE_PARASITE_ANT).count();
        if (current < targetCount) {
            int diff = targetCount - (int) current;
            Rectangle spawnRoom = colony.getStorageBounds();
            if (spawnRoom == null) {
                spawnRoom = new Rectangle(0, 0, 256, 256);
            }
            ColonyPhysicsService physics = colony.getPhysicsService();
            for (int i = 0; i < diff; i++) {
                Bug newBug = new Bug(GameConstants.TYPE_PARASITE_ANT);
                newBug.setDimension(WorldSpaces.UNDERWORLD);
                if (physics != null) {
                    Point spawnPos = physics.getSpecificRoomPoint(colony, spawnRoom);
                    newBug.setPosition(spawnPos);
                }
                bugs.add(newBug);
            }
        } else if (current > targetCount) {
            int diff = (int) current - targetCount;
            List<Bug> toRemove = new ArrayList<>();
            for (Bug b : bugs) {
                if (b.getBugType() == GameConstants.TYPE_PARASITE_ANT) {
                    toRemove.add(b);
                    if (toRemove.size() == diff) {
                        break;
                    }
                }
            }
            bugs.removeAll(toRemove);
        }
    }

    private Rectangle resolvePenBounds(Colony colony, BugType type) {
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

    private BugType pickRandomNative(Colony colony, Biome biome) {
        List<BugType> natives = biome.getNativeBugs();
        if (natives.isEmpty()) {
            return null;
        }
        List<BugType> catchable = new ArrayList<>();
        for (BugType type : natives) {
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
        return stored >= GameConstants.PARASITIC_MITE_RESOURCE_THRESHOLD;
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
                GameConstants.PARASITIC_MITE_MIN_MONTHLY_SPAWN,
                antTotal * GameConstants.PARASITIC_MITE_PER_ANT);
        int spawnAmount = spawnFromAnts;
        if (existing > 0) {
            spawnAmount += (int) (existing * GameConstants.PARASITIC_MITE_SPREAD_FACTOR);
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
        return spawn * GameConstants.PARASITE_OUTBREAK_PREVENTION_MULTIPLIER;
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
        if (GameRandom.nextFloat() > GameConstants.PARASITE_OUTBREAK_CHANCE) {
            return;
        }

        setParasiticMiteCount(colony, colony.getParasiticMites() + spawnAmount);
        colony.logEvent(ColonyLogPrefixes.INFO + " "
                + String.format(LanguageStrings.get(LanguageStrings.LOG_PARASITIC_MITE_SPREAD_FMT), spawnAmount));
    }

    private int parasiticMiteCap(Colony colony) {
        int antTotal = colony.getAntTotal();
        int fromPopulation = antTotal * GameConstants.PARASITIC_MITES_PER_SLOWED_ANT;
        return Math.max(fromPopulation, GameConstants.PARASITIC_MITE_MIN_MONTHLY_SPAWN);
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
        return colony.getParasiticMites() / GameConstants.PARASITIC_MITES_PER_SLOWED_ANT;
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
