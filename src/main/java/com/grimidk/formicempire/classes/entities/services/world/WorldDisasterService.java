package com.grimidk.formicempire.classes.entities.services.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.world.DisasterType;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyStarterService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyUnassignedAntService;
import com.grimidk.formicempire.classes.entities.services.shared.HexWaterCrossing;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.world.ResourceSource;
import com.grimidk.formicempire.classes.entities.world.Tunnel;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.DeathCause;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

public class WorldDisasterService {

    private static final ColonyStarterService STARTER = ColonyStarterService.shared();

    public void processDaily(World world) {
        if (world == null) {
            return;
        }

        if (GameRandom.nextInt(GameNumbers.ECLIPSE_DAILY_CHANCE_DENOMINATOR) == 0) {
            triggerGlobalEclipse(world);
        }

        if (GameRandom.nextInt(GameNumbers.DISASTER_DAILY_CHANCE_DENOMINATOR) == 0) {
            triggerRandomDisaster(world);
        }
    }

    public void processHourly(World world) {
        if (world == null || world.getHexes() == null) {
            return;
        }
        for (Hex hex : world.getHexes()) {
            if (hex != null && hex.getDisasterType() != null) {
                hex.tickDisasterHour();
            }
        }
    }

    public void triggerGlobalEclipse(World world) {
        if (world == null) {
            return;
        }
        int dayOfMonth = world.getDay();
        boolean solar = dayOfMonth < 8 || dayOfMonth > 22;
        world.setTimeOfDay(solar ? GameConstants.TIME_SOLAR_ECLIPSE : GameConstants.TIME_LUNAR_ECLIPSE);

        for (Hex hex : world.getColonizedHexes()) {
            if (hex == null || hex.getColony() == null) {
                continue;
            }
            Colony colony = hex.getColony();
            if (colony.getDynasty() != null && !colony.getDynasty().isDefeated()) {
                colony.getLabourService().runNuptial(colony, world, hex);
                colony.logEvent(ColonyLogPrefixes.NUPTIAL + " "
                        + LanguageStrings.get(LanguageStrings.EVENT_ECLIPSE_NUPTIAL));
            }
        }

        String eventKey = solar ? LanguageStrings.TIME_SOLAR_ECLIPSE : LanguageStrings.TIME_LUNAR_ECLIPSE;
        world.getHistoryService().record(WorldHistoryEventType.NUPTIAL_FLIGHT,
                LanguageStrings.EVENT_ECLIPSE_NUPTIAL,
                WorldHistoryEvent.keyArg(eventKey));
    }

    public DisasterType triggerRandomDisaster(World world) {
        if (world == null || world.getHexes() == null || world.getHexes().isEmpty()) {
            return null;
        }
        List<DisasterType> pool = new ArrayList<>(GameConstants.getDisasterTypes());
        Collections.shuffle(pool, GameRandom.getShuffleRandom());
        for (DisasterType candidate : pool) {
            if (triggerDisaster(world, candidate, null)) {
                return candidate;
            }
        }
        return null;
    }

    public boolean triggerDisaster(World world, DisasterType type, Hex targetHex) {
        if (world == null || type == null) {
            return false;
        }
        if (type == GameConstants.DISASTER_EARTHQUAKE) {
            return triggerEarthquake(world, targetHex);
        } else if (type == GameConstants.DISASTER_TSUNAMI) {
            return triggerTsunami(world, targetHex);
        } else if (type == GameConstants.DISASTER_TORNADO) {
            return triggerTornado(world, targetHex);
        } else if (type == GameConstants.DISASTER_METEOR) {
            return triggerMeteor(world, targetHex);
        } else if (type == GameConstants.DISASTER_VOLCANO) {
            return triggerVolcano(world, targetHex);
        } else if (type == GameConstants.DISASTER_WILDFIRE) {
            return triggerWildfire(world, targetHex);
        } else if (type == GameConstants.DISASTER_ICE_COLLAPSE) {
            return triggerIceCollapse(world, targetHex);
        } else if (type == GameConstants.DISASTER_OCEAN_FREEZE) {
            return triggerOceanFreeze(world, targetHex);
        }
        return false;
    }

    public boolean triggerEarthquake(World world, Hex targetHex) {
        Hex primary = targetHex;
        Hex secondary = null;
        if (primary == null) {
            List<Hex[]> faultEdges = findFaultEdges(world);
            if (faultEdges.isEmpty()) {
                return false;
            }
            Hex[] picked = faultEdges.get(GameRandom.nextInt(faultEdges.size()));
            primary = picked[0];
            secondary = picked[1];
        } else {
            for (Hex n : primary.getAdjacentNeighbors()) {
                if (n != null && n.getPlateId() > 0 && n.getPlateId() != primary.getPlateId()) {
                    secondary = n;
                    break;
                }
            }
        }

        List<Hex> affected = new ArrayList<>();
        if (primary != null) {
            affected.add(primary);
        }
        if (secondary != null) {
            affected.add(secondary);
        }
        if (affected.isEmpty()) {
            return false;
        }

        for (Hex hex : affected) {
            hex.recordDisaster(GameConstants.DISASTER_EARTHQUAKE);
            int severedTunnels = severTunnelsConnectedTo(world, hex);
            int damagedRooms = 0;
            Colony colony = hex.getColony();
            if (colony != null) {
                List<Building> downgraded = colony.downgradeEligibleBuildings(GameNumbers.DISASTER_EARTHQUAKE_MAX_DOWNGRADES);
                damagedRooms = downgraded.size();
                colony.logEvent(ColonyLogPrefixes.DISASTER + " "
                        + String.format(LanguageStrings.get(LanguageStrings.LOG_DISASTER_EARTHQUAKE_FMT),
                                damagedRooms, severedTunnels));
            }
            recordDisasterHistory(world, GameConstants.DISASTER_EARTHQUAKE, hex,
                    damagedRooms + " rooms damaged, " + severedTunnels + " tunnels severed");
        }
        return true;
    }

    public boolean triggerTsunami(World world, Hex targetHex) {
        Hex target = targetHex != null ? targetHex : pickRandomCoastalLandHex(world);
        if (target == null) {
            return false;
        }
        target.recordDisaster(GameConstants.DISASTER_TSUNAMI);
        Colony colony = target.getColony();
        int deadAnts = 0;
        int deadPets = 0;
        if (colony != null) {
            deadAnts = killOutsideWorkingAnts(colony, randomCasualtyFraction());
            deadPets = colony.getBugHandlingService().killPetsOutside(colony, GameNumbers.DISASTER_PET_LOSS_FRACTION);
            colony.getLocationService().clearAllSources(colony);
            colony.logEvent(ColonyLogPrefixes.DISASTER + " "
                    + String.format(LanguageStrings.get(LanguageStrings.LOG_DISASTER_TSUNAMI_FMT),
                            deadAnts, deadPets));
        }
        recordDisasterHistory(world, GameConstants.DISASTER_TSUNAMI, target,
                deadAnts + " ants, " + deadPets + " pets lost");
        return true;
    }

    public boolean triggerTornado(World world, Hex targetHex) {
        Hex target = targetHex != null ? targetHex : pickRandomInlandFlatHex(world);
        if (target == null) {
            return false;
        }
        target.recordDisaster(GameConstants.DISASTER_TORNADO);
        Colony colony = target.getColony();
        int deadAnts = 0;
        int deadPets = 0;
        if (colony != null) {
            deadAnts = killOutsideWorkingAnts(colony, randomCasualtyFraction());
            deadPets = colony.getBugHandlingService().killPetsOutside(colony, GameNumbers.DISASTER_PET_LOSS_FRACTION);
            colony.getLocationService().clearAllSources(colony);
            colony.logEvent(ColonyLogPrefixes.DISASTER + " "
                    + String.format(LanguageStrings.get(LanguageStrings.LOG_DISASTER_TORNADO_FMT),
                            deadAnts, deadPets));
        }
        recordDisasterHistory(world, GameConstants.DISASTER_TORNADO, target,
                deadAnts + " ants, " + deadPets + " pets lost");
        return true;
    }

    public boolean triggerMeteor(World world, Hex targetHex) {
        Hex target = targetHex != null ? targetHex : pickRandomLandHex(world);
        if (target == null) {
            return false;
        }
        target.recordDisaster(GameConstants.DISASTER_METEOR);
        Colony colony = target.getColony();
        int deadAnts = 0;
        int minerals = GameNumbers.DISASTER_METEOR_MINERALS;
        if (colony != null) {
            deadAnts = killOutsideWorkingAnts(colony, 0.40);
            colony.setMinerals(colony.getMineralsPrecise() + minerals);
            ResourceSource richSource = new ResourceSource(GameConstants.RESOURCE_ROCK, minerals, minerals, 200, 200);
            colony.getLocationService().addSource(colony, richSource);
            colony.logEvent(ColonyLogPrefixes.DISASTER + " "
                    + String.format(LanguageStrings.get(LanguageStrings.LOG_DISASTER_METEOR_FMT),
                            deadAnts, minerals));
        }
        world.getBiomeTransformationService().applyMeteorTransformation(world, target);
        recordDisasterHistory(world, GameConstants.DISASTER_METEOR, target,
                deadAnts + " ants lost, +" + minerals + " minerals");
        return true;
    }

    public boolean triggerVolcano(World world, Hex targetHex) {
        Hex target = targetHex != null ? targetHex : pickRandomHexByBiome(world, GameConstants.BIOME_VOLCANIC);
        if (target == null) {
            return false;
        }
        target.recordDisaster(GameConstants.DISASTER_VOLCANO);
        Colony colony = target.getColony();
        int deadAnts = 0;
        int deadPets = 0;
        int minerals = GameNumbers.DISASTER_VOLCANO_MINERALS;
        if (colony != null) {
            deadAnts = killOutsideWorkingAnts(colony, randomCasualtyFraction());
            deadPets = colony.getBugHandlingService().killPetsOutside(colony, GameNumbers.DISASTER_PET_LOSS_FRACTION);
            colony.getLocationService().clearAllSources(colony);
            colony.setMinerals(colony.getMineralsPrecise() + minerals);
            ResourceSource richSource = new ResourceSource(GameConstants.RESOURCE_ROCK, minerals, minerals, 200, 200);
            colony.getLocationService().addSource(colony, richSource);
            colony.logEvent(ColonyLogPrefixes.DISASTER + " "
                    + String.format(LanguageStrings.get(LanguageStrings.LOG_DISASTER_VOLCANO_FMT),
                            deadAnts, deadPets, minerals));
        }
        for (Hex neighbor : WorldBiomeTransformationService.getNeighbors(target)) {
            if (neighbor != null && (neighbor.getBiome() == GameConstants.BIOME_FOREST || neighbor.getBiome() == GameConstants.BIOME_JUNGLE)) {
                world.getBiomeTransformationService().applyWildfireTransformation(world, neighbor);
                break;
            }
        }
        recordDisasterHistory(world, GameConstants.DISASTER_VOLCANO, target,
                deadAnts + " ants, " + deadPets + " pets lost, +" + minerals + " minerals");
        return true;
    }

    public boolean triggerWildfire(World world, Hex targetHex) {
        Hex target = targetHex;
        if (target == null) {
            List<Hex> woodland = new ArrayList<>();
            for (Hex h : world.getHexes()) {
                if (h != null && (h.getBiome() == GameConstants.BIOME_FOREST || h.getBiome() == GameConstants.BIOME_JUNGLE)) {
                    woodland.add(h);
                }
            }
            if (!woodland.isEmpty()) {
                target = woodland.get(GameRandom.nextInt(woodland.size()));
            }
        }
        if (target == null) {
            return false;
        }
        target.recordDisaster(GameConstants.DISASTER_WILDFIRE);
        world.getBiomeTransformationService().applyWildfireTransformation(world, target);
        Colony colony = target.getColony();
        int deadAnts = 0;
        int deadPets = 0;
        if (colony != null) {
            deadAnts = killOutsideWorkingAnts(colony, randomCasualtyFraction());
            deadPets = colony.getBugHandlingService().killPetsOutside(colony, GameNumbers.DISASTER_PET_LOSS_FRACTION);
            colony.getLocationService().clearAllSources(colony);
            colony.logEvent(ColonyLogPrefixes.DISASTER + " "
                    + String.format(LanguageStrings.get(LanguageStrings.LOG_DISASTER_WILDFIRE_FMT),
                            deadAnts, deadPets));
        }
        recordDisasterHistory(world, GameConstants.DISASTER_WILDFIRE, target,
                "burned to Burnt Forest, " + deadAnts + " ants lost");
        return true;
    }

    public boolean triggerIceCollapse(World world, Hex targetHex) {
        Hex target = targetHex;
        if (target == null) {
            List<Hex> shelf = new ArrayList<>();
            for (Hex h : world.getHexes()) {
                if (h != null && h.getBiome() == GameConstants.BIOME_ICE_FIELD && bordersOcean(h)) {
                    shelf.add(h);
                }
            }
            if (!shelf.isEmpty()) {
                target = shelf.get(GameRandom.nextInt(shelf.size()));
            }
        }
        if (target == null) {
            return false;
        }

        target.recordDisaster(GameConstants.DISASTER_ICE_COLLAPSE);
        world.getBiomeTransformationService().applyIceCollapseTransformation(world, target);

        Colony colony = target.getColony();
        if (colony != null) {
            Dynasty dynasty = colony.getDynasty();
            colony.logEvent(ColonyLogPrefixes.DISASTER + " "
                    + LanguageStrings.get(LanguageStrings.LOG_DISASTER_ICE_COLLAPSE_FMT));
            killAllAntsInColony(colony);
            STARTER.dismantleColony(target);
            if (dynasty != null) {
                dynasty.removeColony(colony);
                if (dynasty.getColonies().isEmpty()) {
                    dynasty.setDefeated(true);
                    world.getWarService().endWarsInvolving(dynasty);
                } else if (colony.isCapital()) {
                    dynasty.promoteNewCapital();
                }
            }
            world.getHistoryService().record(WorldHistoryEventType.COLONY_DIED,
                    LanguageStrings.HISTORY_COLONY_DIED_FMT,
                    dynasty != null ? dynasty.getId() : -1, colony.getId(), -1,
                    WorldHistoryEvent.colonyArg(colony.getId()),
                    WorldHistoryEvent.dynastyArg(dynasty != null ? dynasty.getId() : -1),
                    WorldHistoryEvent.keyArg(LanguageStrings.DISASTER_ICE_COLLAPSE));
        }

        recordDisasterHistory(world, GameConstants.DISASTER_ICE_COLLAPSE, target,
                "ice shelf collapsed into ocean");
        return true;
    }

    public boolean triggerOceanFreeze(World world, Hex targetHex) {
        Hex target = targetHex;
        if (target == null) {
            List<Hex> oceanEdge = new ArrayList<>();
            for (Hex h : world.getHexes()) {
                if (h != null && HexWaterCrossing.isOceanHex(h) && bordersIceField(h)) {
                    oceanEdge.add(h);
                }
            }
            if (!oceanEdge.isEmpty()) {
                target = oceanEdge.get(GameRandom.nextInt(oceanEdge.size()));
            }
        }
        if (target == null) {
            return false;
        }

        target.recordDisaster(GameConstants.DISASTER_OCEAN_FREEZE);
        world.getBiomeTransformationService().applyOceanFreezeTransformation(world, target);
        recordDisasterHistory(world, GameConstants.DISASTER_OCEAN_FREEZE, target,
                "ocean froze into ice field");
        return true;
    }

    public static int killOutsideWorkingAnts(Colony colony, double casualtyFraction) {
        if (colony == null || casualtyFraction <= 0.0) {
            return 0;
        }
        Engine engine = ColonyUnassignedAntService.resolveEngine(colony);
        List<Ant> victims = new ArrayList<>();

        for (AntClass ac : GameConstants.getAntClasses()) {
            List<Ant> group = colony.getAntsByClass(ac);
            if (group == null) {
                continue;
            }
            for (Ant ant : group) {
                if (ant == null || !ant.isAlive() || ant.isOnTrade()) {
                    continue;
                }
                if (ant.getDimension() != WorldSpaces.OVERWORLD) {
                    continue;
                }
                if (ColonyUnassignedAntService.isUnassigned(ant, colony, engine)) {
                    continue;
                }
                victims.add(ant);
            }
        }

        if (victims.isEmpty()) {
            return 0;
        }

        Collections.shuffle(victims, GameRandom.getShuffleRandom());
        int killCount = Math.max(1, (int) Math.round(victims.size() * casualtyFraction));
        killCount = Math.min(killCount, victims.size());

        for (int i = 0; i < killCount; i++) {
            Ant victim = victims.get(i);
            AntClass originalType = victim.getAntClass();
            AntRole formerRole = victim.getRole();
            boolean wasOnTrade = victim.isOnTrade();

            victim.goDie(colony, DeathCause.DISASTER);
            colony.recordAntDeath(victim, DeathCause.DISASTER);
            colony.handleAntCasualtyAftermath(victim, originalType, formerRole, wasOnTrade);

            List<Ant> antList = colony.getAntsByClass(originalType);
            if (antList != null) {
                antList.remove(victim);
            }
        }
        return killCount;
    }

    private static void killAllAntsInColony(Colony colony) {
        if (colony == null) {
            return;
        }
        for (AntClass ac : GameConstants.getAntClasses()) {
            List<Ant> group = colony.getAntsByClass(ac);
            if (group == null) {
                continue;
            }
            for (Ant ant : new ArrayList<>(group)) {
                if (ant != null && ant.isAlive()) {
                    ant.goDie(colony, DeathCause.DISASTER);
                    colony.recordAntDeath(ant, DeathCause.DISASTER);
                }
            }
            group.clear();
        }
    }

    private static double randomCasualtyFraction() {
        return GameNumbers.DISASTER_ANT_CASUALTY_MIN_FRACTION
                + GameRandom.nextDouble() * (GameNumbers.DISASTER_ANT_CASUALTY_MAX_FRACTION - GameNumbers.DISASTER_ANT_CASUALTY_MIN_FRACTION);
    }

    private static boolean bordersOcean(Hex hex) {
        if (hex == null) {
            return false;
        }
        for (Hex n : hex.getAdjacentNeighbors()) {
            if (n != null && HexWaterCrossing.isOceanHex(n)) {
                return true;
            }
        }
        return false;
    }

    private static boolean bordersIceField(Hex hex) {
        if (hex == null) {
            return false;
        }
        for (Hex n : hex.getAdjacentNeighbors()) {
            if (n != null && n.getBiome() == GameConstants.BIOME_ICE_FIELD) {
                return true;
            }
        }
        return false;
    }

    private static List<Hex[]> findFaultEdges(World world) {
        List<Hex[]> pairs = new ArrayList<>();
        if (world == null || world.getHexes() == null) {
            return pairs;
        }
        for (Hex h : world.getHexes()) {
            if (h == null || h.getPlateId() <= 0) {
                continue;
            }
            for (Hex n : h.getAdjacentNeighbors()) {
                if (n != null && n.getPlateId() > 0 && n.getPlateId() != h.getPlateId()) {
                    if (h.getQ() < n.getQ() || (h.getQ() == n.getQ() && h.getR() < n.getR())) {
                        pairs.add(new Hex[] {h, n});
                    }
                }
            }
        }
        return pairs;
    }

    private static int severTunnelsConnectedTo(World world, Hex hex) {
        if (world == null || world.getDynastys() == null || hex == null) {
            return 0;
        }
        int count = 0;
        for (Dynasty dynasty : world.getDynastys()) {
            if (dynasty == null || dynasty.getTunnels() == null) {
                continue;
            }
            for (Tunnel tunnel : dynasty.getTunnels()) {
                if (tunnel != null && tunnel.isComplete() && (tunnel.getHexA() == hex || tunnel.getHexB() == hex)) {
                    tunnel.restoreState(tunnel.getTotalCost() * 0.5, false);
                    count++;
                }
            }
        }
        return count;
    }

    private static Hex pickRandomCoastalLandHex(World world) {
        List<Hex> coastal = new ArrayList<>();
        for (Hex h : world.getHexes()) {
            if (h != null && HexWaterCrossing.isLandHex(h) && (h.isCoastal() || bordersOcean(h))) {
                coastal.add(h);
            }
        }
        if (coastal.isEmpty()) {
            return null;
        }
        return coastal.get(GameRandom.nextInt(coastal.size()));
    }

    private static Hex pickRandomInlandFlatHex(World world) {
        List<Hex> inlandFlat = new ArrayList<>();
        for (Hex h : world.getHexes()) {
            if (h != null && HexWaterCrossing.isLandHex(h) && !h.isCoastal()
                    && (h.getBiome() == GameConstants.BIOME_PLAINS || h.getBiome() == GameConstants.BIOME_PRAIRIE)) {
                inlandFlat.add(h);
            }
        }
        if (inlandFlat.isEmpty()) {
            return null;
        }
        return inlandFlat.get(GameRandom.nextInt(inlandFlat.size()));
    }

    private static Hex pickRandomLandHex(World world) {
        List<Hex> land = new ArrayList<>();
        for (Hex h : world.getHexes()) {
            if (h != null && HexWaterCrossing.isLandHex(h)) {
                land.add(h);
            }
        }
        if (land.isEmpty()) {
            return null;
        }
        return land.get(GameRandom.nextInt(land.size()));
    }

    private static Hex pickRandomHexByBiome(World world, com.grimidk.formicempire.classes.constants.world.Biome biome) {
        List<Hex> matches = new ArrayList<>();
        for (Hex h : world.getHexes()) {
            if (h != null && h.getBiome() == biome) {
                matches.add(h);
            }
        }
        if (matches.isEmpty()) {
            return null;
        }
        return matches.get(GameRandom.nextInt(matches.size()));
    }

    private static void recordDisasterHistory(World world, DisasterType type, Hex hex, String details) {
        if (world == null || type == null || hex == null) {
            return;
        }
        String locationArg = hex.getColony() != null
                ? WorldHistoryEvent.colonyArg(hex.getColony().getId())
                : WorldHistoryEvent.plainArg("(" + hex.getQ() + ", " + hex.getR() + ")");
        world.getHistoryService().record(WorldHistoryEventType.DISASTER,
                LanguageStrings.HISTORY_DISASTER_FMT,
                hex.getColony() != null && hex.getColony().getDynasty() != null ? hex.getColony().getDynasty().getId() : -1,
                hex.getColony() != null ? hex.getColony().getId() : -1,
                -1,
                WorldHistoryEvent.keyArg(type.getNameKey()),
                locationArg,
                WorldHistoryEvent.plainArg(details));
    }
}
