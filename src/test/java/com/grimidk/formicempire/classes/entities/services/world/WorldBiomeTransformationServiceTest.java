package com.grimidk.formicempire.classes.entities.services.world;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.shared.HexWaterCrossing;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.world.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class WorldBiomeTransformationServiceTest {

    private static Hex makeHex(int q, int r, Biome biome) {
        Hex h = new Hex();
        h.setQ(q);
        h.setR(r);
        h.setBiome(biome);
        h.setElevation(HexWaterCrossing.isWaterBiome(biome) ? 1 : 4);
        return h;
    }

    private static Colony attachColony(World world, Dynasty dynasty, Hex hex, int id, String name) {
        Colony colony = new Colony(id, name, true);
        colony.setDynasty(dynasty);
        dynasty.addColony(colony);
        hex.setColony(colony);
        world.markColonizedHexIndexDirty();
        return colony;
    }

    @Test
    void wildfireTransformsWoodlandsToBurntForest() {
        World world = new World();
        Hex forestHex = makeHex(0, 0, GameConstants.BIOME_FOREST);
        Hex plainsHex = makeHex(1, 0, GameConstants.BIOME_PLAINS);

        WorldBiomeTransformationService service = world.getBiomeTransformationService();
        assertTrue(service.applyWildfireTransformation(world, forestHex));
        assertEquals(GameConstants.BIOME_BURNT_FOREST, forestHex.getBiome());

        assertFalse(service.applyWildfireTransformation(world, plainsHex));
        assertEquals(GameConstants.BIOME_PLAINS, plainsHex.getBiome());
    }

    @Test
    void meteorTransformsLandHexToBarren() {
        World world = new World();
        Hex plainsHex = makeHex(0, 0, GameConstants.BIOME_PLAINS);
        Hex oceanHex = makeHex(1, 0, GameConstants.BIOME_OCEAN);

        WorldBiomeTransformationService service = world.getBiomeTransformationService();
        assertTrue(service.applyMeteorTransformation(world, plainsHex));
        assertEquals(GameConstants.BIOME_BARREN, plainsHex.getBiome());

        assertFalse(service.applyMeteorTransformation(world, oceanHex));
        assertEquals(GameConstants.BIOME_OCEAN, oceanHex.getBiome());
    }

    @Test
    void iceCollapseAndOceanFreezeUpdateCoastalTopology() {
        World world = new World();
        Hex iceHex = makeHex(0, 0, GameConstants.BIOME_ICE_FIELD);
        Hex oceanHex = makeHex(1, 0, GameConstants.BIOME_OCEAN);
        Hex landNeighbor = makeHex(-1, 0, GameConstants.BIOME_PLAINS);

        iceHex.setNorthEast(oceanHex);
        oceanHex.setSouthWest(iceHex);
        iceHex.setSouthWest(landNeighbor);
        landNeighbor.setNorthEast(iceHex);

        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(iceHex);
        hexes.add(oceanHex);
        hexes.add(landNeighbor);
        world.setHexes(hexes);

        WorldBiomeTransformationService service = world.getBiomeTransformationService();

        assertTrue(service.applyIceCollapseTransformation(world, iceHex));
        assertEquals(GameConstants.BIOME_OCEAN, iceHex.getBiome());
        assertFalse(iceHex.isCoastal());
        assertTrue(landNeighbor.isCoastal());

        assertTrue(service.applyOceanFreezeTransformation(world, iceHex));
        assertEquals(GameConstants.BIOME_ICE_FIELD, iceHex.getBiome());
        assertTrue(iceHex.isCoastal());
    }

    @Test
    void overharvestDeforestsWoodlandsIntoPlainsAndDepletesGrasslandIntoBarren() {
        World world = new World();
        Dynasty dynasty = new Dynasty(1, "TestDynasty", true, GameConstants.SPECIES_OMNI);
        world.getDynastys().add(dynasty);

        Hex jungleHex = makeHex(0, 0, GameConstants.BIOME_JUNGLE);
        Colony colony = attachColony(world, dynasty, jungleHex, 1, "JungleColony");
        colony.getLocationService().addSource(colony,
                new ResourceSource(GameConstants.RESOURCE_PLANT, 100, 100, 50, 50));

        WorldBiomeTransformationService service = world.getBiomeTransformationService();

        jungleHex.setNonWaterResourceSourcesGenerated(100);
        assertFalse(service.checkOverharvestDepletion(world, colony, jungleHex));
        assertEquals(GameConstants.BIOME_JUNGLE, jungleHex.getBiome());

        jungleHex.setNonWaterResourceSourcesGenerated(2000);
        assertEquals(100, jungleHex.getResourceDepletionPercent());
        assertTrue(service.checkOverharvestDepletion(world, colony, jungleHex));
        assertEquals(GameConstants.BIOME_PLAINS, jungleHex.getBiome());
        assertEquals(0, jungleHex.getNonWaterResourceSourcesGenerated());

        jungleHex.setNonWaterResourceSourcesGenerated(2000);
        assertTrue(service.checkOverharvestDepletion(world, colony, jungleHex));
        assertEquals(GameConstants.BIOME_BARREN, jungleHex.getBiome());
    }

    @Test
    void sustainUpgradeProtectsFromOverharvestDegradation() {
        World world = new World();
        Dynasty dynasty = new Dynasty(1, "SustainDynasty", true, GameConstants.SPECIES_OMNI);
        world.getDynastys().add(dynasty);

        Hex forestHex = makeHex(0, 0, GameConstants.BIOME_FOREST);
        Colony colony = attachColony(world, dynasty, forestHex, 1, "SustainColony");
        colony.unlockUpgrade(GameUnlocks.STAT_HEX_SUSTAIN);

        forestHex.setNonWaterResourceSourcesGenerated(3000);
        WorldBiomeTransformationService service = world.getBiomeTransformationService();
        assertFalse(service.checkOverharvestDepletion(world, colony, forestHex));
        assertEquals(GameConstants.BIOME_FOREST, forestHex.getBiome());
    }

    @Test
    void nonPlantRichBiomesDoNotDegradeFromOverharvesting() {
        World world = new World();
        Dynasty dynasty = new Dynasty(1, "DesertDynasty", true, GameConstants.SPECIES_OMNI);
        world.getDynastys().add(dynasty);

        Hex desertHex = makeHex(0, 0, GameConstants.BIOME_DESERT);
        Colony colony = attachColony(world, dynasty, desertHex, 1, "DesertColony");
        desertHex.setNonWaterResourceSourcesGenerated(3000);

        WorldBiomeTransformationService service = world.getBiomeTransformationService();
        assertFalse(service.checkOverharvestDepletion(world, colony, desertHex));
        assertEquals(GameConstants.BIOME_DESERT, desertHex.getBiome());
    }

    @Test
    void ecologicalRecoveryRestoresBurntForestAndBarrenToPlains() {
        World world = new World();
        Hex burntHex = makeHex(0, 0, GameConstants.BIOME_BURNT_FOREST);
        Hex barrenHex = makeHex(1, 0, GameConstants.BIOME_BARREN);

        WorldBiomeTransformationService service = world.getBiomeTransformationService();
        assertTrue(service.applyEcologicalRecovery(world, burntHex));
        assertEquals(GameConstants.BIOME_PLAINS, burntHex.getBiome());

        assertTrue(service.applyEcologicalRecovery(world, barrenHex));
        assertEquals(GameConstants.BIOME_PLAINS, barrenHex.getBiome());

        burntHex.recordDisaster(GameConstants.DISASTER_WILDFIRE);
        burntHex.setBiome(GameConstants.BIOME_BURNT_FOREST);
        assertFalse(service.applyEcologicalRecovery(world, burntHex));
        assertEquals(GameConstants.BIOME_BURNT_FOREST, burntHex.getBiome());
    }

    @Test
    void processDailyExecutesSafely() {
        World world = new World();
        Hex h1 = makeHex(0, 0, GameConstants.BIOME_BURNT_FOREST);
        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(h1);
        world.setHexes(hexes);

        WorldBiomeTransformationService service = world.getBiomeTransformationService();
        assertDoesNotThrow(() -> service.processDaily(world));
        assertDoesNotThrow(() -> service.processDaily(null));
    }
}
