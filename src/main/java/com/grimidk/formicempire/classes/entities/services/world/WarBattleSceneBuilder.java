package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

import java.util.HashMap;
import java.util.List;
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

        WarBattleState battle = WarCreatureCombatService.getState(war);
        SideComposition attackerComp;
        SideComposition defenderComp;
        if (battle != null && hasLivingCombatants(battle)) {
            attackerComp = compositionFromBattleSide(battle.getAttacker());
            defenderComp = compositionFromBattleSide(battle.getDefender());
        } else if (war.getStagePhase() == GameConstants.WAR_STAGE_RESERVE_ASSAULT) {
            attackerComp = compositionFromRoleQuotasHexAssault(stageAttacker);
            defenderComp = compositionFromColonyPopulation(contested, stageDefender);
        } else {
            attackerComp = compositionFromRoleQuotasBorder(stageAttacker);
            defenderComp = compositionFromRoleQuotasBorder(stageDefender);
        }

        int attackerPower = Math.max(attackerComp.livingArmy(), war.getDeployedActiveAttacker());
        int defenderPower = Math.max(defenderComp.livingArmy(),
                war.getStagePhase() == GameConstants.WAR_STAGE_RESERVE_ASSAULT
                        ? war.getDeployedReserveDefender()
                        : war.getDeployedActiveDefender());
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
                        attackerComp.activeByLine(), attackerComp.reserveByLine(),
                        attackerComp.livingArmy(), attackerComp.livingActive()),
                new WarBattleScene.Side(stageDefender.getId(), stageDefender.getName(), stageDefender.getSpecies(),
                        defenderComp.activeByLine(), defenderComp.reserveByLine(),
                        defenderComp.livingArmy(), defenderComp.livingActive()),
                war.getStagePhase(),
                frontline,
                war.getStageProgress(),
                war.getProgressPercent(),
                war.getRedeployHoursRemaining(),
                war.getStagePhase() == GameConstants.WAR_STAGE_RESERVE_ASSAULT,
                true);
    }

    private static boolean hasLivingCombatants(WarBattleState battle) {
        return battle != null
                && (battle.getAttacker().livingArmySize() > 0 || battle.getDefender().livingArmySize() > 0);
    }

    private record SideComposition(Map<BattleLine, Map<AntType, Integer>> activeByLine,
            Map<BattleLine, Map<AntType, Integer>> reserveByLine, int livingArmy, int livingActive) {
    }

    private static SideComposition compositionFromBattleSide(WarBattleSideState side) {
        if (side == null) {
            return emptyComposition();
        }
        Map<BattleLine, Map<AntType, Integer>> active = emptyLineMaps();
        Map<BattleLine, Map<AntType, Integer>> reserve = emptyLineMaps();
        for (BattleLine line : GameConstants.getBattleLines()) {
            tallyParticipants(active.get(line), side.getActive(line));
            tallyParticipants(reserve.get(line), side.getReserve(line));
        }
        return new SideComposition(active, reserve, side.livingArmySize(), side.livingActiveSize());
    }

    private static void tallyParticipants(Map<AntType, Integer> counts, java.util.List<WarBattleParticipant> list) {
        if (counts == null || list == null) {
            return;
        }
        for (WarBattleParticipant p : list) {
            if (p == null || !p.isAlive() || p.getAnt() == null || p.getAnt().getAntType() == null) {
                continue;
            }
            counts.merge(p.getAnt().getAntType(), 1, Integer::sum);
        }
    }

    private static SideComposition compositionFromRoleQuotasBorder(Dynasty dynasty) {
        Map<BattleLine, Map<AntType, Integer>> pooled = emptyLineMaps();
        if (dynasty == null) {
            return emptyComposition();
        }
        int capacity = Math.max(1, dynasty.getCombatCapacity());
        for (Colony colony : dynasty.getColonies()) {
            for (AntRole role : GameConstants.getBorderBattleRoles()) {
                addRoleQuota(pooled, colony, role);
            }
        }
        SideComposition seated = seatByCapacity(pooled, capacity, false);
        if (seated.livingArmy() > 0) {
            return seated;
        }
        return compositionFromPhysicalMilitary(dynasty, capacity);
    }

    private static SideComposition compositionFromRoleQuotasHexAssault(Dynasty dynasty) {
        Map<BattleLine, Map<AntType, Integer>> pooled = emptyLineMaps();
        if (dynasty == null) {
            return emptyComposition();
        }
        int capacity = Math.max(1, dynasty.getCombatCapacity());
        for (Colony colony : dynasty.getColonies()) {
            for (AntRole role : GameConstants.getBorderBattleRoles()) {
                addRoleQuota(pooled, colony, role);
            }
            addRoleQuota(pooled, colony, GameConstants.ROLE_SIEGE);
        }
        SideComposition seated = seatByCapacity(pooled, capacity, false);
        if (seated.livingArmy() > 0) {
            return seated;
        }
        return compositionFromPhysicalMilitary(dynasty, capacity);
    }

    private static SideComposition compositionFromPhysicalMilitary(Dynasty dynasty, int capacity) {
        Map<BattleLine, Map<AntType, Integer>> pooled = emptyLineMaps();
        if (dynasty == null) {
            return emptyComposition();
        }
        for (Colony colony : dynasty.getColonies()) {
            putType(pooled, GameConstants.BATTLE_LINE_INFANTRY, GameConstants.TYPE_SOLDIER,
                    countLivingOfType(colony, GameConstants.TYPE_SOLDIER));
            putType(pooled, GameConstants.BATTLE_LINE_INFANTRY, GameConstants.TYPE_MAJOR,
                    countLivingOfType(colony, GameConstants.TYPE_MAJOR));
        }
        return seatByCapacity(pooled, capacity, false);
    }

    private static SideComposition compositionFromColonyPopulation(Colony contested, Dynasty dynasty) {
        Map<BattleLine, Map<AntType, Integer>> pooled = emptyLineMaps();
        if (contested == null) {
            return emptyComposition();
        }
        int capacity = Math.max(1, dynasty != null ? dynasty.getCombatCapacity() : 1);
        putType(pooled, GameConstants.BATTLE_LINE_INFANTRY, GameConstants.TYPE_WORKER, sizeOf(contested.getWorkers()));
        putType(pooled, GameConstants.BATTLE_LINE_INFANTRY, GameConstants.TYPE_SOLDIER, sizeOf(contested.getSoldiers()));
        putType(pooled, GameConstants.BATTLE_LINE_INFANTRY, GameConstants.TYPE_MAJOR, sizeOf(contested.getMajors()));
        putType(pooled, GameConstants.BATTLE_LINE_INFANTRY, GameConstants.TYPE_PRINCESS, sizeOf(contested.getPrincesses()));
        putType(pooled, GameConstants.BATTLE_LINE_INFANTRY, GameConstants.TYPE_QUEEN, sizeOf(contested.getQueens()));
        for (AntRole role : GameConstants.getActiveMilitaryRoles()) {
            int count = contested.getWarAssignedRoleCount(role);
            if (count <= 0 || role.getAntType() == null) {
                continue;
            }
            BattleLine line = GameConstants.getBattleLineForRole(role);
            if (line == null) {
                line = GameConstants.BATTLE_LINE_INFANTRY;
            }
            Map<AntType, Integer> infantry = pooled.get(GameConstants.BATTLE_LINE_INFANTRY);
            int available = infantry.getOrDefault(role.getAntType(), 0);
            int move = Math.min(count, available);
            if (move > 0) {
                infantry.put(role.getAntType(), available - move);
                putType(pooled, line, role.getAntType(), move);
            }
        }
        return seatByCapacity(pooled, capacity, true);
    }

    private static void addRoleQuota(Map<BattleLine, Map<AntType, Integer>> pooled, Colony colony, AntRole role) {
        if (colony == null || role == null || role.getAntType() == null) {
            return;
        }
        int count = Math.max(colony.getWarAssignedRoleCount(role), countLivingWithRole(colony, role));
        if (count <= 0) {
            return;
        }
        int available = countLivingOfType(colony, role.getAntType());
        if (available > 0) {
            count = Math.min(count, available);
        }
        BattleLine line = GameConstants.getBattleLineForRole(role);
        if (line == null) {
            line = GameConstants.BATTLE_LINE_INFANTRY;
        }
        putType(pooled, line, role.getAntType(), count);
    }

    private static int countLivingWithRole(Colony colony, AntRole role) {
        if (colony == null || role == null || role.getAntType() == null) {
            return 0;
        }
        int n = 0;
        for (Ant ant : antsOfTypeList(colony, role.getAntType())) {
            if (ant != null && ant.isAlive() && ant.getRole() == role) {
                n++;
            }
        }
        return n;
    }

    private static int countLivingOfType(Colony colony, AntType type) {
        int n = 0;
        for (Ant ant : antsOfTypeList(colony, type)) {
            if (ant != null && ant.isAlive()) {
                n++;
            }
        }
        return n;
    }

    private static List<Ant> antsOfTypeList(Colony colony, AntType type) {
        if (colony == null || type == null) {
            return List.of();
        }
        List<Ant> list = colony.getAntsByType(type);
        return list != null ? list : List.of();
    }

    private static SideComposition seatByCapacity(Map<BattleLine, Map<AntType, Integer>> pooled, int capacity,
            boolean hexHomeAlwaysActive) {
        Map<BattleLine, Map<AntType, Integer>> active = emptyLineMaps();
        Map<BattleLine, Map<AntType, Integer>> reserve = emptyLineMaps();
        int livingArmy = 0;
        int livingActive = 0;
        for (BattleLine line : GameConstants.getBattleLines()) {
            Map<AntType, Integer> linePool = pooled.getOrDefault(line, Map.of());
            int seated = 0;
            for (Map.Entry<AntType, Integer> e : linePool.entrySet()) {
                AntType type = e.getKey();
                int count = e.getValue() != null ? e.getValue() : 0;
                if (type == null || count <= 0) {
                    continue;
                }
                livingArmy += count;
                boolean forceActive = type == GameConstants.TYPE_QUEEN
                        || (hexHomeAlwaysActive && (type == GameConstants.TYPE_SOLDIER || type == GameConstants.TYPE_MAJOR));
                if (forceActive) {
                    putType(active, line, type, count);
                    livingActive += count;
                    continue;
                }
                int room = Math.max(0, capacity - seated);
                int toActive = Math.min(count, room);
                int toReserve = count - toActive;
                if (toActive > 0) {
                    putType(active, line, type, toActive);
                    seated += toActive;
                    livingActive += toActive;
                }
                if (toReserve > 0) {
                    putType(reserve, line, type, toReserve);
                }
            }
        }
        return new SideComposition(active, reserve, livingArmy, livingActive);
    }

    private static void putType(Map<BattleLine, Map<AntType, Integer>> byLine, BattleLine line, AntType type, int count) {
        if (byLine == null || line == null || type == null || count <= 0) {
            return;
        }
        byLine.computeIfAbsent(line, k -> new HashMap<>()).merge(type, count, Integer::sum);
    }

    private static Map<BattleLine, Map<AntType, Integer>> emptyLineMaps() {
        Map<BattleLine, Map<AntType, Integer>> maps = new HashMap<>();
        for (BattleLine line : GameConstants.getBattleLines()) {
            maps.put(line, new HashMap<>());
        }
        return maps;
    }

    private static SideComposition emptyComposition() {
        return new SideComposition(emptyLineMaps(), emptyLineMaps(), 0, 0);
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
                new WarBattleScene.Side(-1, "?", GameConstants.SPECIES_OMNI, Map.of(), Map.of(), 0, 0),
                new WarBattleScene.Side(-1, "?", GameConstants.SPECIES_OMNI, Map.of(), Map.of(), 0, 0),
                GameConstants.WAR_STAGE_ACTIVE_CLASH,
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

    private static int sizeOf(java.util.List<?> list) {
        return list != null ? list.size() : 0;
    }
}
