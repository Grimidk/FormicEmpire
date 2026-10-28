package com.grimidk.formicempire.classes.entities.world;

import com.grimidk.formicempire.classes.entities.services.shared.HexWaterCrossing;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class FaultLineTest {

    @Test
    void faultLineClassificationAndProperties() {
        FaultLine convergent = new FaultLine(1, 2, FaultLine.FaultType.CONVERGENT);
        assertEquals(1, convergent.getPlateAId());
        assertEquals(2, convergent.getPlateBId());
        assertEquals(FaultLine.FaultType.CONVERGENT, convergent.getType());

        FaultLine divergent = new FaultLine(3, 4, FaultLine.FaultType.DIVERGENT);
        assertEquals(FaultLine.FaultType.DIVERGENT, divergent.getType());

        FaultLine transform = new FaultLine(5, 6, FaultLine.FaultType.TRANSFORM);
        assertEquals(FaultLine.FaultType.TRANSFORM, transform.getType());

        FaultLine defaultIntensity = new FaultLine(7, 8, FaultLine.FaultType.CONVERGENT);
        assertEquals(1, defaultIntensity.getIntensity());

        FaultLine levelTwo = new FaultLine(9, 10, FaultLine.FaultType.CONVERGENT, 2);
        assertEquals(2, levelTwo.getIntensity());

        levelTwo.setIntensity(10);
        assertEquals(2, levelTwo.getIntensity());

        levelTwo.setIntensity(-5);
        assertEquals(1, levelTwo.getIntensity());
    }

    @Test
    void worldFaultIntensityBetweenPlates() {
        World world = new World();
        FaultLine fault1 = new FaultLine(1, 2, FaultLine.FaultType.CONVERGENT, 1);
        world.addFaultLine(fault1);
        assertEquals(1, world.getFaultIntensityBetweenPlates(1, 2));

        FaultLine fault2 = new FaultLine(3, 4, FaultLine.FaultType.DIVERGENT, 2);
        world.addFaultLine(fault2);
        assertEquals(2, world.getFaultIntensityBetweenPlates(3, 4));
        assertEquals(2, world.getFaultIntensityBetweenPlates(4, 3));
    }

    @Test
    void proceduralWorldGenerationAssignsFaultIntensity() {
        World world = new World();
        world.setWorldWidth(30);
        world.setWorldHeight(30);
        world.setTectonicPlateCount(10);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                null,
                "TestDynasty",
                "Dynasty");

        List<FaultLine> faults = world.getFaultLines();
        assertFalse(faults.isEmpty());
        for (FaultLine f : faults) {
            assertTrue(f.getIntensity() == 1 || f.getIntensity() == 2);
        }
    }

    @Test
    void boundaryPairAdditionAndDeduplication() {
        FaultLine fault = new FaultLine(1, 2, FaultLine.FaultType.CONVERGENT);
        Hex a1 = new Hex();
        a1.setQ(0);
        a1.setR(0);
        Hex b1 = new Hex();
        b1.setQ(1);
        b1.setR(0);
        Hex a2 = new Hex();
        a2.setQ(0);
        a2.setR(1);
        Hex b2 = new Hex();
        b2.setQ(1);
        b2.setR(1);

        fault.addBoundaryPair(a1, b1);
        fault.addBoundaryPair(a2, b2);
        fault.addBoundaryPair(a1, b1);

        assertEquals(2, fault.size());
        assertEquals(2, fault.getHexesA().size());
        assertEquals(2, fault.getHexesB().size());
        assertTrue(fault.getHexesA().contains(a1));
        assertTrue(fault.getHexesA().contains(a2));
        assertTrue(fault.getHexesB().contains(b1));
        assertTrue(fault.getHexesB().contains(b2));
    }

    @Test
    void continentsGeneratePeninsulasAlongFaultLines() {
        World world = new World();
        world.setWorldWidth(10);
        world.setWorldHeight(10);

        Map<String, Hex> hexMap = new HashMap<>();
        for (int r = -5; r < 5; r++) {
            for (int q = -5; q < 5; q++) {
                Hex h = new Hex();
                h.setQ(q);
                h.setR(r);
                h.setBiome(GameConstants.BIOME_OCEAN);
                h.setElevation(1);
                hexMap.put(q + "," + r, h);
            }
        }
        world.linkNeighbors(hexMap);
        world.setHexes(new ArrayList<>(hexMap.values()));

        Hex c0 = hexMap.get("0,0");
        c0.setBiome(GameConstants.BIOME_PLAINS);
        c0.setElevation(3);
        c0.setContinentId(1);

        Hex w1 = hexMap.get("1,0");
        Hex w2 = hexMap.get("2,0");
        Hex w3 = hexMap.get("3,0");
        Hex w4 = hexMap.get("4,0");

        FaultLine fault = new FaultLine(1, 2, FaultLine.FaultType.CONVERGENT);
        fault.addBoundaryPair(c0, w1);
        fault.addBoundaryPair(w1, w2);
        fault.addBoundaryPair(w2, w3);
        fault.addBoundaryPair(w3, w4);
        world.addFaultLine(fault);

        List<List<Hex>> peninsulas = world.generateFaultLinePeninsulas();
        assertFalse(peninsulas.isEmpty());
        for (List<Hex> pen : peninsulas) {
            assertTrue(pen.size() >= 2);
            for (Hex h : pen) {
                assertEquals(1, h.getContinentId());
                assertFalse(h.isIsland());
                assertTrue(h.isCoastal());
                assertTrue(HexWaterCrossing.isLandHex(h));
            }
        }
    }

    @Test
    void islandsAndIceCapsNeverGeneratePeninsulas() {
        World world = new World();
        world.setWorldWidth(10);
        world.setWorldHeight(10);

        Map<String, Hex> hexMap = new HashMap<>();
        for (int r = -5; r < 5; r++) {
            for (int q = -5; q < 5; q++) {
                Hex h = new Hex();
                h.setQ(q);
                h.setR(r);
                h.setBiome(GameConstants.BIOME_OCEAN);
                h.setElevation(1);
                hexMap.put(q + "," + r, h);
            }
        }
        world.linkNeighbors(hexMap);
        world.setHexes(new ArrayList<>(hexMap.values()));

        Hex isl = hexMap.get("0,0");
        isl.setBiome(GameConstants.BIOME_PLAINS);
        isl.setElevation(3);
        isl.setContinentId(1);
        isl.setIsland(true);

        Hex ice = hexMap.get("0,4");
        ice.setBiome(GameConstants.BIOME_ICE_FIELD);
        ice.setElevation(4);
        ice.setContinentId(2);

        FaultLine fault1 = new FaultLine(1, 2, FaultLine.FaultType.CONVERGENT);
        fault1.addBoundaryPair(isl, hexMap.get("1,0"));
        fault1.addBoundaryPair(hexMap.get("1,0"), hexMap.get("2,0"));
        fault1.addBoundaryPair(hexMap.get("2,0"), hexMap.get("3,0"));
        world.addFaultLine(fault1);

        FaultLine fault2 = new FaultLine(3, 4, FaultLine.FaultType.CONVERGENT);
        fault2.addBoundaryPair(ice, hexMap.get("1,4"));
        fault2.addBoundaryPair(hexMap.get("1,4"), hexMap.get("2,4"));
        fault2.addBoundaryPair(hexMap.get("2,4"), hexMap.get("3,4"));
        world.addFaultLine(fault2);

        List<List<Hex>> peninsulas = world.generateFaultLinePeninsulas();
        assertTrue(peninsulas.isEmpty());
    }

    @Test
    void peninsulasDoNotBridgeToOtherContinents() {
        World world = new World();
        world.setWorldWidth(10);
        world.setWorldHeight(10);

        Map<String, Hex> hexMap = new HashMap<>();
        for (int r = -5; r < 5; r++) {
            for (int q = -5; q < 5; q++) {
                Hex h = new Hex();
                h.setQ(q);
                h.setR(r);
                h.setBiome(GameConstants.BIOME_OCEAN);
                h.setElevation(1);
                hexMap.put(q + "," + r, h);
            }
        }
        world.linkNeighbors(hexMap);
        world.setHexes(new ArrayList<>(hexMap.values()));

        Hex c1 = hexMap.get("0,0");
        c1.setBiome(GameConstants.BIOME_PLAINS);
        c1.setElevation(3);
        c1.setContinentId(1);

        Hex c2 = hexMap.get("2,0");
        c2.setBiome(GameConstants.BIOME_FOREST);
        c2.setElevation(4);
        c2.setContinentId(2);

        Hex w1 = hexMap.get("1,0");
        FaultLine fault = new FaultLine(1, 2, FaultLine.FaultType.CONVERGENT);
        fault.addBoundaryPair(c1, w1);
        world.addFaultLine(fault);

        List<List<Hex>> peninsulas = world.generateFaultLinePeninsulas();
        assertTrue(peninsulas.isEmpty());
    }
}
