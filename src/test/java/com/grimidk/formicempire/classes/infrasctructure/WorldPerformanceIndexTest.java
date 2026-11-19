package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.world.WorldDisasterService;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldPerformanceIndexTest {

    @Test
    void getHexOfColonyTracksHostHexAcrossPlacementsAndMoves() {
        World world = new World();
        Colony c1 = new Colony(1, "Alpha", false);
        Colony c2 = new Colony(2, "Beta", false);
        Hex h1 = new Hex(GameConstants.BIOME_PLAINS, c1, null, null, null, null, null, null);
        h1.setQ(0);
        h1.setR(0);
        Hex h2 = new Hex();
        h2.setBiome(GameConstants.BIOME_PLAINS);
        h2.setQ(1);
        h2.setR(0);

        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(h1);
        hexes.add(h2);
        world.setHexes(hexes);

        assertSame(h1, c1.getHostHex());
        assertSame(h1, world.getHexOfColony(c1));
        assertNull(world.getHexOfColony(c2));

        h2.setColony(c1);
        h1.setColony(null);
        assertSame(h2, world.getHexOfColony(c1));
        assertSame(h2, c1.getHostHex());

        h2.setColony(c2);
        assertNull(c1.getHostHex());
        assertNull(world.getHexOfColony(c1));
        assertSame(h2, world.getHexOfColony(c2));

        World emptyWorld = new World();
        assertNull(emptyWorld.getHexOfColony(c2));

        World otherWorld = new World();
        Hex foreignHex = new Hex();
        foreignHex.setBiome(GameConstants.BIOME_PLAINS);
        otherWorld.setHexes(new ArrayList<>(List.of(foreignHex)));
        assertNull(otherWorld.getHexOfColony(c2));
    }

    @Test
    void processHourlyExpiresDirectAndServiceDisastersViaEpochSync() {
        World world = new World();
        Hex h1 = new Hex();
        h1.setQ(0);
        h1.setR(0);
        h1.setBiome(GameConstants.BIOME_PLAINS);
        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(h1);
        world.setHexes(hexes);

        h1.recordDisaster(GameConstants.DISASTER_TORNADO);
        WorldDisasterService service = new WorldDisasterService();
        for (int i = 0; i < GameNumbers.DISASTER_HEX_ICON_DURATION_HOURS - 1; i++) {
            service.processHourly(world);
        }
        assertEquals(GameConstants.DISASTER_TORNADO, h1.getDisasterType());

        service.processHourly(world);
        assertNull(h1.getDisasterType());

        h1.setDisasterType(GameConstants.DISASTER_WILDFIRE);
        h1.setDisasterAgeHours(0);
        for (int i = 0; i < GameNumbers.DISASTER_HEX_ICON_DURATION_HOURS; i++) {
            service.processHourly(world);
        }
        assertNull(h1.getDisasterType());
    }

    @Test
    void npcDynastyCountScalesWithMapHeightAndHonorsTargetOverride() {
        int speciesCount = GameConstants.getWorldSpawnableNpcSpecies().size();

        World tiny = new World();
        tiny.setSpectatorMode(true);
        tiny.generateRectangularWorld(GameConstants.BIOME_PLAINS, null, "Spec",
                GameConstants.DYNASTY_TITLE_DYNASTY.getNameKey(), 32, 16, true);
        assertEquals(speciesCount, tiny.getDynastys().size());

        World small = new World();
        small.setSpectatorMode(true);
        small.generateRectangularWorld(GameConstants.BIOME_PLAINS, null, "Spec",
                GameConstants.DYNASTY_TITLE_DYNASTY.getNameKey(), 64, 32, true);
        assertEquals(speciesCount * 2, small.getDynastys().size());

        World override = new World();
        override.setSpectatorMode(true);
        override.setTargetRivalDynastyCount(10);
        override.generateRectangularWorld(GameConstants.BIOME_PLAINS, null, "Spec",
                GameConstants.DYNASTY_TITLE_DYNASTY.getNameKey(), 32, 16, true);
        assertEquals(10, override.getDynastys().size());
    }

    @Test
    void scoutRingBfsDiscoversNearbyLinkedHexWithoutExceedingMaxRange() {
        World world = new World();
        ArrayList<Hex> chain = new ArrayList<>();
        for (int q = 0; q <= 6; q++) {
            Hex h = new Hex();
            h.setQ(q);
            h.setR(0);
            h.setBiome(GameConstants.BIOME_PLAINS);
            chain.add(h);
        }
        for (int q = 0; q < 6; q++) {
            chain.get(q).setSouthEast(chain.get(q + 1));
            chain.get(q + 1).setNorthWest(chain.get(q));
        }
        world.setHexes(chain);

        Dynasty dynasty = new Dynasty(1, "Scouts", false, GameConstants.SPECIES_OMNI);
        dynasty.setOwningWorld(world);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_EXPLORER);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_SCOUT);
        Colony colony = new Colony(1, "Base", false);
        colony.setDynasty(dynasty);
        colony.unlockBuilding(GameUnlocks.ROYAL_CHAMBER_0);
        chain.get(0).setColony(colony);
        dynasty.discoverHex(chain.get(0));
        dynasty.discoverHex(chain.get(1));

        for (int i = 0; i < 4; i++) {
            Ant scout = new Ant(colony, GameConstants.CLASS_WORKER);
            scout.setRole(GameConstants.ROLE_SCOUT);
            colony.getWorkers().add(scout);
        }
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_EXPLORER, 1);

        boolean discoveredRing2 = false;
        for (int hour = 0; hour < 60 && !discoveredRing2; hour++) {
            colony.getLabourService().runScoutting(colony, chain.get(0).getBiome(), chain.get(0));
            discoveredRing2 = dynasty.isHexKnown(2, 0);
        }
        assertTrue(discoveredRing2);
        List<Hex> colonized = world.getColonizedHexes();
        assertEquals(1, colonized.size());
    }
}
