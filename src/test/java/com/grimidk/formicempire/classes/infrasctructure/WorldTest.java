package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class WorldTest {

    @Test
    public void testTunnelProjectResumedOnLoad() {
        // 1. Setup Savefile
        Savefile savefile = new Savefile(1, "TestSave");
        savefile.setWorldRadius(2);
        
        // Create Hexes (q,r): (0,0) and (0,1)
        List<Savefile.SavedHex> hexes = new ArrayList<>();
        hexes.add(new Savefile.SavedHex(0, 0, GameConstants.BIOME_PLAINS.getId(), true, 0, 1));
        hexes.add(new Savefile.SavedHex(0, 1, GameConstants.BIOME_PLAINS.getId(), true, 0, 1));
        savefile.setWorldHexes(hexes);

        // Create Colonies
        List<Savefile.SavedColony> colonies = new ArrayList<>();
        
        // Colony A at 0,0
        Savefile.SavedColony colA = new Savefile.SavedColony();
        colA.id = 1; colA.dynastyId = 1; colA.name = "Colony A"; colA.q = 0; colA.r = 0;
        colA.isPlayer = true;
        colonies.add(colA);

        // Colony B at 0,1
        Savefile.SavedColony colB = new Savefile.SavedColony();
        colB.id = 2; colB.dynastyId = 1; colB.name = "Colony B"; colB.q = 0; colB.r = 1;
        colB.isPlayer = true;
        colonies.add(colB);
        
        savefile.setColonies(colonies);

        // Create Dynasty
        List<Savefile.SavedDynasty> dynastys = new ArrayList<>();
        Savefile.SavedDynasty dynasty = new Savefile.SavedDynasty();
        dynasty.id = 1;
        dynasty.name = "Test Dynasty";
        dynasty.isPlayer = true;
        
        // Create Incomplete Tunnel between 0,0 and 0,1
        Savefile.SavedTunnel tunnel = new Savefile.SavedTunnel();
        tunnel.qA = 0; tunnel.rA = 0;
        tunnel.qB = 0; tunnel.rB = 1;
        tunnel.progress = 50.0;
        tunnel.totalCost = 100.0;
        tunnel.isComplete = false;
        
        dynasty.tunnels.add(tunnel);
        dynastys.add(dynasty);
        
        savefile.setDynastys(dynastys);

        // 2. Load World
        World world = new World();
        world.setEngine(null);
        world.loadWorld(savefile);

        // 3. Verify
        Hex hA = world.getHexAt(0, 0);
        Hex hB = world.getHexAt(0, 1);
        
        assertNotNull(hA);
        assertNotNull(hB);
        
        Colony cA = hA.getColony();
        Colony cB = hB.getColony();
        
        assertNotNull(cA);
        assertNotNull(cB);

        // Incomplete tunnels resume on a single sponsor (hex A preferred) so both
        // endpoints do not dig the same project and progress without diggers.
        Tunnel tA = cA.getCurrentTunnelProject();
        Tunnel tB = cB.getCurrentTunnelProject();

        assertNotNull(tA, "Colony A should have resumed tunnel project");
        assertNull(tB, "Colony B should not also sponsor the same tunnel");

        assertEquals(50.0, tA.getProgress(), 0.001);
        assertFalse(tA.isComplete());
    }

    @Test
    void generateWorldIncludesEachNonOmniSpecies() {
        World world = new World();
        Colony colony = new Colony(1, "Test Prime", true);
        world.generateWorld(
                GameConstants.BIOME_PLAINS,
                7,
                colony,
                "Test",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        Set<Integer> npcSpeciesIds = new HashSet<>();
        for (Dynasty dynasty : world.getDynastys()) {
            if (!dynasty.isPlayer() && dynasty.getSpecies() != null) {
                npcSpeciesIds.add(dynasty.getSpecies().getId());
            }
        }

        for (var species : GameConstants.getWorldSpawnableNpcSpecies()) {
            assertTrue(npcSpeciesIds.contains(species.getId()),
                    "Missing NPC dynasty for species id " + species.getId());
        }
    }

    @Test
    void minDynastyHexDistanceUsesClosestColonyPair() {
        World world = new World();
        ArrayList<Hex> hexes = new ArrayList<>();

        Dynasty dynastyA = new Dynasty(1, "Alpha", true, GameConstants.SPECIES_OMNI);
        Dynasty dynastyB = new Dynasty(2, "Beta", false, GameConstants.SPECIES_OMNI);

        Colony aFar = new Colony(1, "A Far", true);
        Colony aNear = new Colony(2, "A Near", true);
        Colony bOnly = new Colony(3, "B Only", false);
        aFar.setDynasty(dynastyA);
        aNear.setDynasty(dynastyA);
        bOnly.setDynasty(dynastyB);

        Hex hexAFar = new Hex();
        hexAFar.setQ(0);
        hexAFar.setR(0);
        hexAFar.setColony(aFar);

        Hex hexANear = new Hex();
        hexANear.setQ(3);
        hexANear.setR(0);
        hexANear.setColony(aNear);

        Hex hexB = new Hex();
        hexB.setQ(4);
        hexB.setR(0);
        hexB.setColony(bOnly);

        hexes.add(hexAFar);
        hexes.add(hexANear);
        hexes.add(hexB);
        world.setHexes(hexes);

        assertEquals(1, world.minDynastyHexDistance(dynastyA, dynastyB));
        assertEquals(Integer.MAX_VALUE, world.minDynastyHexDistance(dynastyA, null));
    }

    @Test
    void generateWorldUsesContinentCorePlusCoastalAndOuterOceanRings() {
        World world = new World();
        Colony colony = new Colony(1, "Test Prime", true);
        int core = GameNumbers.WORLD_DEFAULT_CONTINENT_CORE_RADIUS;
        world.generateWorld(
                GameConstants.BIOME_PLAINS,
                core,
                colony,
                "Test",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        assertEquals(core, world.getContinentCoreRadius());
        assertEquals(GameNumbers.worldRadiusForContinentCore(core), world.getWorldRadius());
        assertTrue(world.getContinentCount() >= 1);
        assertTrue(world.getIslandCount() >= GameNumbers.WORLD_MIN_ISLAND_COUNT,
                "Expected at least " + GameNumbers.WORLD_MIN_ISLAND_COUNT + " islands, got " + world.getIslandCount());
        assertBiomeMinimums(world);

        int outerDist = world.getWorldRadius();
        for (Hex hex : world.getHexes()) {
            int dist = (Math.abs(hex.getQ()) + Math.abs(hex.getQ() + hex.getR()) + Math.abs(hex.getR())) / 2;
            if (dist == outerDist) {
                assertEquals(GameConstants.BIOME_OCEAN, hex.getBiome(),
                        "Outer ring must be pure ocean at " + hex.getQ() + "," + hex.getR());
            }
            if (hex.isIsland()) {
                assertNull(hex.getColony(), "Islands must not spawn colonies");
                assertNotEquals(GameConstants.BIOME_OCEAN, hex.getBiome());
                assertNotEquals(GameConstants.BIOME_LAKE, hex.getBiome());
            }
        }
    }

    @Test
    void generateWorldEnsuresMinimumIslandsForSmallCore() {
        World world = new World();
        Colony colony = new Colony(1, "Test Prime", true);
        world.generateWorld(
                GameConstants.BIOME_PLAINS,
                0,
                colony,
                "Test",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        assertTrue(world.getIslandCount() >= GameNumbers.WORLD_MIN_ISLAND_COUNT,
                "Expected at least " + GameNumbers.WORLD_MIN_ISLAND_COUNT + " islands, got " + world.getIslandCount());
        for (Hex hex : world.getHexes()) {
            if (hex.isIsland()) {
                assertNull(hex.getColony(), "Islands must not spawn colonies");
            }
        }
    }

    private static void assertBiomeMinimums(World world) {
        Map<Biome, Integer> counts = new HashMap<>();
        for (Hex hex : world.getHexes()) {
            counts.merge(hex.getBiome(), 1, Integer::sum);
        }
        for (Biome biome : GameConstants.getBiomes()) {
            int count = counts.getOrDefault(biome, 0);
            assertTrue(count >= GameNumbers.WORLD_MIN_HEXES_PER_BIOME,
                    "Expected at least " + GameNumbers.WORLD_MIN_HEXES_PER_BIOME
                            + " hexes of " + biome.getId() + ", got " + count);
        }
    }

    @Test
    void classifyLandmassesMarksDisconnectedLandAsIslands() {
        World world = new World();
        world.setContinentCoreRadius(1);

        Hex core = new Hex();
        core.setQ(0);
        core.setR(0);
        core.setBiome(GameConstants.BIOME_PLAINS);

        Hex island = new Hex();
        island.setQ(3);
        island.setR(0);
        island.setBiome(GameConstants.BIOME_PLAINS);

        Hex ocean = new Hex();
        ocean.setQ(1);
        ocean.setR(0);
        ocean.setBiome(GameConstants.BIOME_OCEAN);

        core.setSouthEast(ocean);
        ocean.setNorthWest(core);
        ocean.setSouthEast(island);
        island.setNorthWest(ocean);

        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(core);
        hexes.add(ocean);
        hexes.add(island);
        world.setHexes(hexes);

        world.classifyLandmasses();

        assertEquals(1, world.getContinentCount());
        assertEquals(1, world.getIslandCount());
        assertFalse(core.isIsland());
        assertTrue(island.isIsland());
    }

    @Test
    void convertEnclosedOceansToLakesLeavesOpenOcean() {
        World world = new World();

        Hex inlandOcean = new Hex();
        inlandOcean.setQ(0);
        inlandOcean.setR(0);
        inlandOcean.setBiome(GameConstants.BIOME_OCEAN);

        Hex[] ring = new Hex[6];
        for (int i = 0; i < 6; i++) {
            Hex land = new Hex();
            land.setQ(i + 1);
            land.setR(0);
            land.setBiome(GameConstants.BIOME_PLAINS);
            ring[i] = land;
        }
        inlandOcean.setNorth(ring[0]);
        inlandOcean.setNorthEast(ring[1]);
        inlandOcean.setSouthEast(ring[2]);
        inlandOcean.setSouth(ring[3]);
        inlandOcean.setSouthWest(ring[4]);
        inlandOcean.setNorthWest(ring[5]);
        ring[0].setSouth(inlandOcean);
        ring[1].setSouthWest(inlandOcean);
        ring[2].setNorthWest(inlandOcean);
        ring[3].setNorth(inlandOcean);
        ring[4].setNorthEast(inlandOcean);
        ring[5].setSouthEast(inlandOcean);

        Hex openOcean = new Hex();
        openOcean.setQ(10);
        openOcean.setR(0);
        openOcean.setBiome(GameConstants.BIOME_OCEAN);

        Hex channelOcean = new Hex();
        channelOcean.setQ(11);
        channelOcean.setR(0);
        channelOcean.setBiome(GameConstants.BIOME_OCEAN);
        openOcean.setSouthEast(channelOcean);
        channelOcean.setNorthWest(openOcean);

        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(inlandOcean);
        for (Hex land : ring) {
            hexes.add(land);
        }
        hexes.add(openOcean);
        hexes.add(channelOcean);
        world.setHexes(hexes);

        world.convertEnclosedOceansToLakes();

        assertEquals(GameConstants.BIOME_LAKE, inlandOcean.getBiome());
        assertEquals(GameConstants.BIOME_OCEAN, openOcean.getBiome());
        assertEquals(GameConstants.BIOME_OCEAN, channelOcean.getBiome());
    }

    @Test
    void convertLakesTouchingOceanToOceanCascadesThroughConnectedLakes() {
        World world = new World();

        Hex ocean = new Hex();
        ocean.setQ(0);
        ocean.setR(0);
        ocean.setBiome(GameConstants.BIOME_OCEAN);

        Hex lakeTouching = new Hex();
        lakeTouching.setQ(1);
        lakeTouching.setR(0);
        lakeTouching.setBiome(GameConstants.BIOME_LAKE);

        Hex lakeBehind = new Hex();
        lakeBehind.setQ(2);
        lakeBehind.setR(0);
        lakeBehind.setBiome(GameConstants.BIOME_LAKE);

        Hex inlandLake = new Hex();
        inlandLake.setQ(5);
        inlandLake.setR(0);
        inlandLake.setBiome(GameConstants.BIOME_LAKE);

        ocean.setSouthEast(lakeTouching);
        lakeTouching.setNorthWest(ocean);
        lakeTouching.setSouthEast(lakeBehind);
        lakeBehind.setNorthWest(lakeTouching);

        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(ocean);
        hexes.add(lakeTouching);
        hexes.add(lakeBehind);
        hexes.add(inlandLake);
        world.setHexes(hexes);

        world.convertLakesTouchingOceanToOcean();

        assertEquals(GameConstants.BIOME_OCEAN, ocean.getBiome());
        assertEquals(GameConstants.BIOME_OCEAN, lakeTouching.getBiome());
        assertEquals(GameConstants.BIOME_OCEAN, lakeBehind.getBiome());
        assertEquals(GameConstants.BIOME_LAKE, inlandLake.getBiome());
    }
}
