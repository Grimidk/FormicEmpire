package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ColonyForcedFlightSpreadingTest {

    private World world;
    private Engine engine;
    private Dynasty dynasty;
    private Colony capitalColony;
    private Hex centerHex;

    @BeforeEach
    public void setup() {
        GameRandom.enqueueTestDoubles(0.99, 0.99, 0.99, 0.99, 0.99, 0.99, 0.99, 0.99, 0.99, 0.99);
        engine = new Engine();
        world = new World();
        world.setEngine(engine);
        engine.setWorld(world);

        Map<String, Hex> hexMap = new HashMap<>();
        for (int q = -2; q <= 2; q++) {
            for (int r = -2; r <= 2; r++) {
                int dist = (Math.abs(q) + Math.abs(q + r) + Math.abs(r)) / 2;
                if (dist > 2) {
                    continue;
                }
                Hex hex = new Hex();
                hex.setQ(q);
                hex.setR(r);
                hex.setBiome(GameConstants.BIOME_PLAINS);
                hex.setLocalWeather(GameConstants.WEATHER_CLEAR);
                hex.setActive(false);
                hex.setIsland(false);
                hexMap.put(q + "," + r, hex);
                world.getHexes().add(hex);
            }
        }
        for (Hex hex : hexMap.values()) {
            int q = hex.getQ();
            int r = hex.getR();
            hex.setNorth(hexMap.get(q + "," + (r - 1)));
            hex.setNorthEast(hexMap.get((q + 1) + "," + (r - 1)));
            hex.setSouthEast(hexMap.get((q + 1) + "," + r));
            hex.setSouth(hexMap.get(q + "," + (r + 1)));
            hex.setSouthWest(hexMap.get((q - 1) + "," + (r + 1)));
            hex.setNorthWest(hexMap.get((q - 1) + "," + r));
        }

        dynasty = new Dynasty(1, "Player Dynasty", true, GameConstants.SPECIES_OMNI);
        world.getDynastys().add(dynasty);
        dynasty.setOwningWorld(world);

        centerHex = hexMap.get("0,0");
        capitalColony = new Colony(1, "Player Prime", true);
        centerHex.setColony(capitalColony);
        dynasty.addColony(capitalColony);
        ColonyStarterService.shared().initializeNewColony(capitalColony);

        capitalColony.unlockUpgrade(GameUnlocks.TYPE_PRINCESS);
        capitalColony.unlockUpgrade(GameUnlocks.ABILITY_ABILITY);
        capitalColony.unlockUpgrade(GameUnlocks.ABILITY_FORCED_FLIGHT);
        capitalColony.unlockUpgrade(GameUnlocks.ABILITY_SPREAD_2);

        capitalColony.addResearchPoints(10000);

        for (int i = 0; i < 6; i++) {
            Ant drone = new Ant(capitalColony, GameConstants.CLASS_DRONE);
            capitalColony.getDrones().add(drone);

            Ant princess = new Ant(capitalColony, GameConstants.CLASS_PRINCESS);
            princess.setRole(GameConstants.ROLE_BREEDER);
            capitalColony.getPrincesses().add(princess);
        }
    }

    @AfterEach
    public void tearDown() {
        GameRandom.clearTestDoubles();
    }

    @Test
    public void testForceNuptialFlightSpawnsSixNewColoniesWithoutCrash() {
        assertEquals(1, dynasty.getColonies().size());
        assertEquals(1, capitalColony.getQueens().size());

        capitalColony.forceNuptialFlight(world, centerHex);

        assertEquals(7, dynasty.getColonies().size());

        Hex[] adjacent = centerHex.getAdjacentNeighbors();
        assertEquals(6, adjacent.length);
        for (Hex neighbor : adjacent) {
            assertNotNull(neighbor.getColony());
            assertEquals(dynasty, neighbor.getColony().getDynasty());
            assertNotNull(neighbor.getColony().getName());
            assertFalse(neighbor.getColony().getName().isEmpty());
            assertFalse(neighbor.getColony().getQueens().isEmpty());
        }

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 60; i++) {
                world.runMinute();
            }
            world.runHour();
            world.runDay();
            world.runMonth();
        });
    }
}
