package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.War;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

import java.util.HashMap;
import java.util.Map;

public final class WarBattleSceneBuilder {

    private WarBattleSceneBuilder() {
    }

    public static WarBattleScene build(World world, War war, WarService warService) {
        if (world == null || war == null || !war.isActive() || !war.isCampaignInitialized()) {
            return unavailable(war, warService, world);
        }

        Dynasty aggressor = world.findDynastyById(war.getAggressorDynastyId());
        Dynasty defender = world.findDynastyById(war.getDefenderDynastyId());
        if (aggressor == null || defender == null || aggressor.isDefeated() || defender.isDefeated()) {
            return unavailable(war, warService, world);
        }

        Colony contested = findColonyById(world, war.getContestedColonyId());
        if (contested == null) {
            return unavailable(war, warService, world);
        }

        Dynasty stageAttacker = world.findDynastyById(war.getStageAttackerDynastyId());
        Dynasty stageDefender = contested.getDynasty();
        if (stageAttacker == null || stageDefender == null) {
            return unavailable(war, warService, world);
        }

        Biome defenderBiome = biomeAtColony(world, contested);
        Biome attackerBiome = resolveAttackerApproachBiome(world, contested, stageAttacker, stageDefender);

        int attackerPower;
        int defenderPower;
        Map<AntType, Integer> attackerCounts;
        Map<AntType, Integer> defenderCounts;

        if (war.getStagePhase() == WarStagePhase.RESERVE_ASSAULT) {
            attackerPower = war.getDeployedActiveAttacker();
            defenderPower = GameConstants.warHexDefenseEffectivePower(war.getDeployedReserveDefender());
            attackerCounts = compositionForDynastyActive(stageAttacker, attackerPower);
            defenderCounts = compositionForColonyReserve(contested, war.getDeployedReserveDefender());
        } else {
            attackerPower = war.getDeployedActiveAttacker();
            defenderPower = war.getDeployedActiveDefender();
            attackerCounts = compositionForDynastyActive(stageAttacker, attackerPower);
            defenderCounts = compositionForDynastyActive(stageDefender, defenderPower);
        }

        float powerRatio = computePowerRatio(attackerPower, defenderPower);
        float stageProgress = war.getStageProgress();
        float frontline = 0.5f + (powerRatio - 0.5f) * (0.25f + 0.75f * stageProgress);

        String warName = warService != null
                ? warService.formatWarNameForDisplay(war, aggressor)
                : war.getDisplayName();
        String location = contested.getName();

        return new WarBattleScene(
                warName,
                location,
                attackerBiome,
                defenderBiome,
                new WarBattleScene.Side(stageAttacker.getId(), stageAttacker.getName(), stageAttacker.getSpecies(),
                        attackerCounts, attackerPower),
                new WarBattleScene.Side(stageDefender.getId(), stageDefender.getName(), stageDefender.getSpecies(),
                        defenderCounts, defenderPower),
                war.getStagePhase(),
                frontline,
                war.getStageProgress(),
                war.getProgressPercent(),
                war.getRedeployHoursRemaining(),
                war.getStagePhase() == WarStagePhase.RESERVE_ASSAULT,
                true);
    }

    private static WarBattleScene unavailable(War war, WarService warService, World world) {
        String warName = "";
        if (war != null && warService != null && world != null) {
            Dynasty any = world.findDynastyById(war.getDynastyIdA());
            warName = warService.formatWarNameForDisplay(war, any);
        } else if (war != null) {
            warName = war.getDisplayName();
        }
        return new WarBattleScene(
                warName,
                "",
                GameConstants.BIOME_PLAINS,
                GameConstants.BIOME_PLAINS,
                new WarBattleScene.Side(-1, "?", GameConstants.SPECIES_OMNI, Map.of(), 0),
                new WarBattleScene.Side(-1, "?", GameConstants.SPECIES_OMNI, Map.of(), 0),
                WarStagePhase.ACTIVE_CLASH,
                0.5f,
                0f,
                war != null ? war.getProgressPercent() : 50f,
                0,
                false,
                false);
    }

    private static float computePowerRatio(int attackerPower, int defenderPower) {
        if (attackerPower <= 0 && defenderPower <= 0) {
            return 0.5f;
        }
        return attackerPower / (float) (attackerPower + defenderPower);
    }

    private static Map<AntType, Integer> aggregateActiveTypeCounts(Dynasty dynasty) {
        Map<AntType, Integer> byType = new HashMap<>();
        if (dynasty == null) {
            return byType;
        }
        for (Colony colony : dynasty.getColonies()) {
            Map<AntRole, Integer> roleCounts = colony.getWarAssignedRoleCounts();
            for (AntRole role : GameConstants.getActiveMilitaryRoles()) {
                int count = roleCounts.getOrDefault(role, 0);
                if (count > 0 && role.getAntType() != null) {
                    byType.merge(role.getAntType(), count, Integer::sum);
                }
            }
        }
        return byType;
    }

    private static Map<AntType, Integer> reserveTypeCounts(Colony colony) {
        Map<AntType, Integer> byType = new HashMap<>();
        if (colony == null) {
            return byType;
        }
        Map<AntRole, Integer> warCounts = colony.getWarAssignedRoleCounts();
        int militia = warCounts.getOrDefault(GameConstants.ROLE_MILITIA, 0);
        int activeSoldiers = sumActiveRoleCountsForType(warCounts, GameConstants.TYPE_SOLDIER);
        int activeMajors = sumActiveRoleCountsForType(warCounts, GameConstants.TYPE_MAJOR);

        int reserveWorkers = Math.max(0, sizeOf(colony.getWorkers()) - militia);
        int reserveSoldiers = Math.max(0, sizeOf(colony.getSoldiers()) - activeSoldiers);
        int reserveMajors = Math.max(0, sizeOf(colony.getMajors()) - activeMajors);

        putIfPositive(byType, GameConstants.TYPE_WORKER, reserveWorkers);
        putIfPositive(byType, GameConstants.TYPE_SOLDIER, reserveSoldiers);
        putIfPositive(byType, GameConstants.TYPE_MAJOR, reserveMajors);
        putIfPositive(byType, GameConstants.TYPE_PRINCESS, sizeOf(colony.getPrincesses()));
        putIfPositive(byType, GameConstants.TYPE_QUEEN, sizeOf(colony.getQueens()));
        return byType;
    }

    private static Map<AntType, Integer> compositionForDynastyActive(Dynasty dynasty, int targetPower) {
        Map<AntType, Integer> raw = aggregateActiveTypeCounts(dynasty);
        return scaleTypeCountsToPower(raw, dynasty, targetPower);
    }

    private static Map<AntType, Integer> compositionForColonyReserve(Colony colony, int targetPower) {
        Map<AntType, Integer> raw = reserveTypeCounts(colony);
        return scaleTypeCountsToPower(raw, colony, targetPower);
    }

    private static Map<AntType, Integer> scaleTypeCountsToPower(
            Map<AntType, Integer> rawCounts, Dynasty dynasty, int targetPower) {
        if (dynasty == null || targetPower <= 0 || rawCounts == null || rawCounts.isEmpty()) {
            return Map.of();
        }
        float multiplier = averageStatMultiplier(dynasty);
        int currentPoints = 0;
        for (Map.Entry<AntType, Integer> entry : rawCounts.entrySet()) {
            currentPoints += entry.getValue() * GameConstants.getMilitaryWeightForAntType(entry.getKey());
        }
        currentPoints = Math.round(currentPoints * multiplier);
        if (currentPoints <= 0) {
            return rawCounts;
        }
        float scale = targetPower / (float) currentPoints;
        Map<AntType, Integer> scaled = new HashMap<>();
        for (Map.Entry<AntType, Integer> entry : rawCounts.entrySet()) {
            int scaledCount = Math.max(0, Math.round(entry.getValue() * scale));
            putIfPositive(scaled, entry.getKey(), scaledCount);
        }
        return scaled;
    }

    private static Map<AntType, Integer> scaleTypeCountsToPower(
            Map<AntType, Integer> rawCounts, Colony colony, int targetPower) {
        if (colony == null || targetPower <= 0 || rawCounts == null || rawCounts.isEmpty()) {
            return Map.of();
        }
        float multiplier = ColonyMilitaryService.effectiveStatMultiplier(colony);
        int currentPoints = 0;
        for (Map.Entry<AntType, Integer> entry : rawCounts.entrySet()) {
            currentPoints += entry.getValue() * GameConstants.getMilitaryWeightForAntType(entry.getKey());
        }
        currentPoints = Math.round(currentPoints * multiplier);
        if (currentPoints <= 0) {
            return rawCounts;
        }
        float scale = targetPower / (float) currentPoints;
        Map<AntType, Integer> scaled = new HashMap<>();
        for (Map.Entry<AntType, Integer> entry : rawCounts.entrySet()) {
            int scaledCount = Math.max(0, Math.round(entry.getValue() * scale));
            putIfPositive(scaled, entry.getKey(), scaledCount);
        }
        return scaled;
    }

    private static float averageStatMultiplier(Dynasty dynasty) {
        float weighted = 0f;
        int totalActive = 0;
        for (Colony colony : dynasty.getColonies()) {
            int active = colony.getActiveMilitaryPower();
            if (active <= 0) {
                continue;
            }
            weighted += ColonyMilitaryService.effectiveStatMultiplier(colony) * active;
            totalActive += active;
        }
        if (totalActive <= 0) {
            Colony capital = dynasty.getCapital();
            return capital != null ? ColonyMilitaryService.effectiveStatMultiplier(capital) : 1f;
        }
        return weighted / totalActive;
    }

    private static Biome biomeAtColony(World world, Colony colony) {
        Hex hex = world.getHexOfColony(colony);
        if (hex != null && hex.getBiome() != null) {
            return hex.getBiome();
        }
        return GameConstants.BIOME_PLAINS;
    }

    private static Biome resolveAttackerApproachBiome(World world, Colony contested, Dynasty stageAttacker,
            Dynasty stageDefender) {
        Hex contestedHex = world.getHexOfColony(contested);
        if (contestedHex != null) {
            for (Hex neighbor : contestedHex.getAdjacentNeighbors()) {
                if (neighbor == null || neighbor.getColony() == null) {
                    continue;
                }
                Dynasty owner = neighbor.getColony().getDynasty();
                if (owner == stageAttacker && neighbor.getBiome() != null) {
                    return neighbor.getBiome();
                }
            }
        }
        Colony border = findBorderColonyFacing(world, stageAttacker, stageDefender);
        if (border != null) {
            Biome biome = biomeAtColony(world, border);
            if (biome != null) {
                return biome;
            }
        }
        Colony capital = stageAttacker.getCapital();
        if (capital != null) {
            Biome biome = biomeAtColony(world, capital);
            if (biome != null) {
                return biome;
            }
        }
        return biomeAtColony(world, contested);
    }

    private static Colony findBorderColonyFacing(World world, Dynasty owner, Dynasty facing) {
        Colony best = null;
        int bestDist = Integer.MAX_VALUE;
        Colony facingCapital = facing.getCapital();
        for (Colony colony : owner.getColonies()) {
            Hex hex = world.getHexOfColony(colony);
            if (hex == null || !hexAdjacentToDynasty(hex, facing, world)) {
                continue;
            }
            int dist = facingCapital != null ? world.colonyHexDistance(colony, facingCapital) : colony.getId();
            if (dist < bestDist || (dist == bestDist && (best == null || colony.getId() < best.getId()))) {
                bestDist = dist;
                best = colony;
            }
        }
        return best;
    }

    private static boolean hexAdjacentToDynasty(Hex hex, Dynasty dynasty, World world) {
        if (hex == null || dynasty == null) {
            return false;
        }
        for (Hex neighbor : hex.getAdjacentNeighbors()) {
            if (neighbor != null && neighbor.getColony() != null
                    && neighbor.getColony().getDynasty() == dynasty) {
                return true;
            }
        }
        return false;
    }

    private static Colony findColonyById(World world, int colonyId) {
        if (world == null || colonyId <= 0 || world.getHexes() == null) {
            return null;
        }
        for (Hex hex : world.getHexes()) {
            if (hex.getColony() != null && hex.getColony().getId() == colonyId) {
                return hex.getColony();
            }
        }
        return null;
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

    private static void putIfPositive(Map<AntType, Integer> map, AntType type, int count) {
        if (count > 0) {
            map.put(type, count);
        }
    }

    private static int sizeOf(java.util.List<?> list) {
        return list != null ? list.size() : 0;
    }
}
