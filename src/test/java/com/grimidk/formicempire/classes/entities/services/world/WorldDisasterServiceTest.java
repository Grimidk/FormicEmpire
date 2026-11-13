package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.DisasterType;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyUnassignedAntService;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.world.ResourceSource;
import com.grimidk.formicempire.classes.entities.world.Tunnel;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorldDisasterServiceTest {

    private static Hex makeHex(int q, int r, Biome biome) {
        Hex hex = new Hex();
        hex.setQ(q);
        hex.setR(r);
        hex.setBiome(biome);
        hex.setElevation(biome == GameConstants.BIOME_OCEAN ? 1 : 4);
        return hex;
    }

    private static Colony attachColony(World world, Dynasty dynasty, Hex hex, int id, String name, boolean player) {
        Colony colony = new Colony(id, name, player);
        colony.setDynasty(dynasty);
        dynasty.addColony(colony);
        hex.setColony(colony);
        world.markColonizedHexIndexDirty();
        return colony;
    }

    @Test
    void earthquakeDamagesBelowTier3RoomsSeversTunnelsAndHitsOnlyFaultEdgeHexes() {
        World world = new World();
        Dynasty dynasty = new Dynasty(1, "QuakeDynasty", true, GameConstants.SPECIES_OMNI);
        world.getDynastys().add(dynasty);

        Hex faultA = makeHex(0, 0, GameConstants.BIOME_PLAINS);
        faultA.setPlateId(1);
        Hex faultB = makeHex(1, 0, GameConstants.BIOME_PLAINS);
        faultB.setPlateId(2);
        Hex safeNeighbor = makeHex(-1, 0, GameConstants.BIOME_PLAINS);
        safeNeighbor.setPlateId(1);

        faultA.setNorthEast(faultB);
        faultB.setSouthWest(faultA);
        faultA.setSouthWest(safeNeighbor);
        safeNeighbor.setNorthEast(faultA);

        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(faultA);
        hexes.add(faultB);
        hexes.add(safeNeighbor);
        world.setHexes(hexes);

        Colony colony = attachColony(world, dynasty, faultA, 1, "QuakeNest", true);
        colony.unlockBuilding(GameUnlocks.PLANT_CHAMBER_0);
        colony.unlockBuilding(GameUnlocks.PLANT_CHAMBER_1);
        colony.unlockBuilding(GameUnlocks.MUSHROOM_CHAMBER_0);
        colony.unlockBuilding(GameUnlocks.MUSHROOM_CHAMBER_1);
        colony.unlockBuilding(GameUnlocks.MUSHROOM_CHAMBER_2);
        colony.unlockBuilding(GameUnlocks.ROYAL_CHAMBER_0);
        colony.unlockBuilding(GameUnlocks.ROYAL_CHAMBER_1);
        colony.unlockBuilding(GameUnlocks.ROYAL_CHAMBER_2);
        colony.unlockBuilding(GameUnlocks.ROYAL_CHAMBER_3);

        Tunnel tunnel = new Tunnel(faultA, faultB, 100.0);
        tunnel.restoreState(100.0, true);
        dynasty.addTunnel(tunnel);

        WorldDisasterService service = new WorldDisasterService();
        assertTrue(service.triggerEarthquake(world, null));

        assertEquals(GameConstants.DISASTER_EARTHQUAKE, faultA.getDisasterType());
        assertEquals(GameConstants.DISASTER_EARTHQUAKE, faultB.getDisasterType());
        assertNull(safeNeighbor.getDisasterType());

        assertTrue(colony.hasBuilding(GameUnlocks.ROYAL_CHAMBER_3));
        assertTrue(!colony.hasBuilding(GameUnlocks.PLANT_CHAMBER_1) || !colony.hasBuilding(GameUnlocks.MUSHROOM_CHAMBER_2));
        assertFalse(tunnel.isComplete());
        assertEquals(50.0, tunnel.getProgress(), 0.001);
        assertTrue(colony.consumeEvents().stream().anyMatch(e -> e.startsWith(ColonyLogPrefixes.DISASTER)));
        assertTrue(world.getHistoryService().getEvents().stream()
                .anyMatch(e -> e.getType() == WorldHistoryEventType.DISASTER));
    }

    @Test
    void tsunamiClearsSourcesKillsOutsideWorkersAndPetsAndSparesIdleAnts() {
        World world = new World();
        Dynasty dynasty = new Dynasty(1, "CoastDynasty", true, GameConstants.SPECIES_OMNI);
        world.getDynastys().add(dynasty);

        Hex coast = makeHex(0, 0, GameConstants.BIOME_PLAINS);
        coast.setCoastal(true);
        Hex ocean = makeHex(1, 0, GameConstants.BIOME_OCEAN);
        coast.setNorthEast(ocean);
        ocean.setSouthWest(coast);

        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(coast);
        hexes.add(ocean);
        world.setHexes(hexes);

        Colony colony = attachColony(world, dynasty, coast, 1, "Harbor", true);
        colony.getLocationService().addSource(colony,
                new ResourceSource(GameConstants.RESOURCE_PLANT, 500, 500, 100, 100));
        assertFalse(colony.getLocationService().getDiscoveredSources().isEmpty());

        ListHolder lists = populateIdleAndWorkingAnts(colony, 6, 4);
        colony.setAphids(10);

        WorldDisasterService service = new WorldDisasterService();
        assertTrue(service.triggerTsunami(world, coast));

        assertEquals(GameConstants.DISASTER_TSUNAMI, coast.getDisasterType());
        assertTrue(colony.getLocationService().getDiscoveredSources().isEmpty());
        for (Ant idle : lists.idle) {
            assertTrue(idle.isAlive());
        }
        long aliveWorking = lists.working.stream().filter(Ant::isAlive).count();
        assertTrue(aliveWorking < lists.working.size());
        assertTrue(colony.getAphids() < 10);
    }

    @Test
    void tornadoHitsInlandFlatHexClearsSourcesAndSparesIdleAnts() {
        World world = new World();
        Dynasty dynasty = new Dynasty(1, "PrairieDynasty", true, GameConstants.SPECIES_OMNI);
        world.getDynastys().add(dynasty);

        Hex inlandFlat = makeHex(0, 0, GameConstants.BIOME_PRAIRIE);
        inlandFlat.setCoastal(false);
        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(inlandFlat);
        world.setHexes(hexes);

        Colony colony = attachColony(world, dynasty, inlandFlat, 1, "DustBowl", true);
        colony.getLocationService().addSource(colony,
                new ResourceSource(GameConstants.RESOURCE_PLANT, 300, 300, 100, 100));
        ListHolder lists = populateIdleAndWorkingAnts(colony, 5, 3);

        WorldDisasterService service = new WorldDisasterService();
        assertTrue(service.triggerTornado(world, null));

        assertEquals(GameConstants.DISASTER_TORNADO, inlandFlat.getDisasterType());
        assertTrue(colony.getLocationService().getDiscoveredSources().isEmpty());
        for (Ant idle : lists.idle) {
            assertTrue(idle.isAlive());
        }
        assertTrue(lists.working.stream().anyMatch(a -> !a.isAlive()));
    }

    @Test
    void meteorAddsMineralsKillsActiveSurfaceWorkersAndSparesIdleAnts() {
        World world = new World();
        Dynasty dynasty = new Dynasty(1, "StarDynasty", true, GameConstants.SPECIES_OMNI);
        world.getDynastys().add(dynasty);

        Hex hex = makeHex(0, 0, GameConstants.BIOME_DESERT);
        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(hex);
        world.setHexes(hexes);

        Colony colony = attachColony(world, dynasty, hex, 1, "Crater", true);
        double beforeMinerals = colony.getMineralsPrecise();
        ListHolder lists = populateIdleAndWorkingAnts(colony, 5, 4);

        WorldDisasterService service = new WorldDisasterService();
        assertTrue(service.triggerMeteor(world, hex));

        assertEquals(GameConstants.DISASTER_METEOR, hex.getDisasterType());
        assertEquals(GameConstants.BIOME_BARREN, hex.getBiome());
        assertEquals(beforeMinerals + GameNumbers.DISASTER_METEOR_MINERALS, colony.getMineralsPrecise(), 0.001);
        assertTrue(colony.getLocationService().getDiscoveredSources().stream()
                .anyMatch(s -> s.getResourceType() == GameConstants.RESOURCE_ROCK));
        for (Ant idle : lists.idle) {
            assertTrue(idle.isAlive());
        }
        assertTrue(lists.working.stream().anyMatch(a -> !a.isAlive()));
    }

    @Test
    void volcanoEruptionStrikesVolcanicBiomeAddsMineralsAndClearsSources() {
        World world = new World();
        Dynasty dynasty = new Dynasty(1, "VolcDynasty", true, GameConstants.SPECIES_OMNI);
        world.getDynastys().add(dynasty);

        Hex volcano = makeHex(0, 0, GameConstants.BIOME_VOLCANIC);
        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(volcano);
        world.setHexes(hexes);

        Colony colony = attachColony(world, dynasty, volcano, 1, "Caldera", true);
        colony.getLocationService().addSource(colony,
                new ResourceSource(GameConstants.RESOURCE_PLANT, 400, 400, 100, 100));
        double beforeMinerals = colony.getMineralsPrecise();
        ListHolder lists = populateIdleAndWorkingAnts(colony, 4, 4);

        WorldDisasterService service = new WorldDisasterService();
        assertTrue(service.triggerVolcano(world, null));

        assertEquals(GameConstants.DISASTER_VOLCANO, volcano.getDisasterType());
        assertEquals(beforeMinerals + GameNumbers.DISASTER_VOLCANO_MINERALS, colony.getMineralsPrecise(), 0.001);
        assertFalse(colony.getLocationService().getDiscoveredSources().stream()
                .anyMatch(s -> s.getResourceType() == GameConstants.RESOURCE_PLANT));
        assertTrue(colony.getLocationService().getDiscoveredSources().stream()
                .anyMatch(s -> s.getResourceType() == GameConstants.RESOURCE_ROCK));
        for (Ant idle : lists.idle) {
            assertTrue(idle.isAlive());
        }
    }

    @Test
    void wildfireBurnsForestIntoBurntForestAndClearsSources() {
        World world = new World();
        Dynasty dynasty = new Dynasty(1, "ForestDynasty", true, GameConstants.SPECIES_OMNI);
        world.getDynastys().add(dynasty);

        Hex forest = makeHex(0, 0, GameConstants.BIOME_FOREST);
        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(forest);
        world.setHexes(hexes);

        Colony colony = attachColony(world, dynasty, forest, 1, "Timber", true);
        colony.getLocationService().addSource(colony,
                new ResourceSource(GameConstants.RESOURCE_RESIN, 200, 200, 100, 100));
        ListHolder lists = populateIdleAndWorkingAnts(colony, 4, 3);

        WorldDisasterService service = new WorldDisasterService();
        assertTrue(service.triggerWildfire(world, null));

        assertEquals(GameConstants.DISASTER_WILDFIRE, forest.getDisasterType());
        assertEquals(GameConstants.BIOME_BURNT_FOREST, forest.getBiome());
        assertTrue(colony.getLocationService().getDiscoveredSources().isEmpty());
        for (Ant idle : lists.idle) {
            assertTrue(idle.isAlive());
        }
    }

    @Test
    void iceCollapseSinksCoastalIceColonyAndOceanFreezeFreezesAdjacentSea() {
        World world = new World();
        Dynasty dynasty = new Dynasty(1, "PolarDynasty", false, GameConstants.SPECIES_OMNI);
        world.getDynastys().add(dynasty);

        Hex iceHex = makeHex(0, 0, GameConstants.BIOME_ICE_FIELD);
        Hex oceanHex = makeHex(1, 0, GameConstants.BIOME_OCEAN);
        iceHex.setNorthEast(oceanHex);
        oceanHex.setSouthWest(iceHex);

        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(iceHex);
        hexes.add(oceanHex);
        world.setHexes(hexes);

        Colony colony = attachColony(world, dynasty, iceHex, 1, "Frost", false);
        Ant worker = new Ant(colony, GameConstants.CLASS_WORKER);
        colony.getWorkers().add(worker);

        WorldDisasterService service = new WorldDisasterService();
        assertTrue(service.triggerOceanFreeze(world, oceanHex));
        assertEquals(GameConstants.BIOME_ICE_FIELD, oceanHex.getBiome());
        assertEquals(GameConstants.DISASTER_OCEAN_FREEZE, oceanHex.getDisasterType());

        oceanHex.setBiome(GameConstants.BIOME_OCEAN);
        assertTrue(service.triggerIceCollapse(world, iceHex));
        assertEquals(GameConstants.BIOME_OCEAN, iceHex.getBiome());
        assertNull(iceHex.getColony());
        assertTrue(dynasty.isDefeated());
        assertFalse(worker.isAlive());
    }

    @Test
    void globalEclipseUsesLunarPhaseAndTriggersWorldNuptialEvent() {
        World world = new World();
        Hex hex = makeHex(0, 0, GameConstants.BIOME_PLAINS);
        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(hex);
        world.setHexes(hexes);

        WorldDisasterService service = new WorldDisasterService();
        world.setDay(2);
        service.triggerGlobalEclipse(world);
        assertEquals(GameConstants.TIME_SOLAR_ECLIPSE, world.getTimeOfDay());

        world.setDay(15);
        service.triggerGlobalEclipse(world);
        assertEquals(GameConstants.TIME_LUNAR_ECLIPSE, world.getTimeOfDay());
    }

    @Test
    void processHourlyExpiresOldDisasters() {
        World world = new World();
        Hex hex = makeHex(0, 0, GameConstants.BIOME_PLAINS);
        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(hex);
        world.setHexes(hexes);

        DisasterType tornado = GameConstants.DISASTER_TORNADO;
        hex.recordDisaster(tornado);
        assertEquals(tornado, hex.getDisasterType());
        assertEquals(0, hex.getDisasterAgeHours());

        WorldDisasterService service = new WorldDisasterService();
        for (int i = 0; i < GameNumbers.DISASTER_HEX_ICON_DURATION_HOURS; i++) {
            service.processHourly(world);
        }
        assertNull(hex.getDisasterType());
    }

    private static ListHolder populateIdleAndWorkingAnts(Colony colony, int workingCount, int idleCount) {
        ListHolder holder = new ListHolder();
        for (int i = 0; i < workingCount; i++) {
            Ant w = new Ant(colony, GameConstants.CLASS_WORKER);
            w.setRole(GameConstants.ROLE_SCOUT);
            w.setDimension(WorldSpaces.OVERWORLD);
            colony.getWorkers().add(w);
            holder.working.add(w);
            assertFalse(ColonyUnassignedAntService.isUnassigned(w, colony, null));
        }
        for (int i = 0; i < idleCount; i++) {
            Ant idle = new Ant(colony, GameConstants.CLASS_WORKER);
            idle.setRole(GameConstants.ROLE_FORAGER);
            idle.setDimension(WorldSpaces.OVERWORLD);
            colony.getWorkers().add(idle);
            holder.idle.add(idle);
            assertTrue(ColonyUnassignedAntService.isUnassigned(idle, colony, null));
        }
        return holder;
    }

    private static final class ListHolder {
        final List<Ant> working = new ArrayList<>();
        final List<Ant> idle = new ArrayList<>();
    }
}
