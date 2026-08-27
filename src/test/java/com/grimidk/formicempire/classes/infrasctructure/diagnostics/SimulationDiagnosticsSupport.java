package com.grimidk.formicempire.classes.infrasctructure.diagnostics;

import java.util.HashMap;
import java.util.Map;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.dynasty.AiPersonality;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyStarterService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class SimulationDiagnosticsSupport {

    public static final int BENCHMARK_MAP_RADIUS = 2;

    private SimulationDiagnosticsSupport() {
    }

    public static World buildBenchmarkWorld(Engine engine) {
        World world = new World();
        world.setEngine(engine);
        engine.setWorld(world);
        world.setWorldRadius(BENCHMARK_MAP_RADIUS);
        world.setContinentCoreRadius(BENCHMARK_MAP_RADIUS);

        Map<String, Hex> hexMap = new HashMap<>();
        for (int q = -BENCHMARK_MAP_RADIUS; q <= BENCHMARK_MAP_RADIUS; q++) {
            for (int r = -BENCHMARK_MAP_RADIUS; r <= BENCHMARK_MAP_RADIUS; r++) {
                int dist = (Math.abs(q) + Math.abs(q + r) + Math.abs(r)) / 2;
                if (dist > BENCHMARK_MAP_RADIUS) {
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
        linkNeighbors(hexMap);
        assertTrue(hexMap.size() >= 19);

        ColonyStarterService starter = ColonyStarterService.shared();
        placeAdvancedNpc(world, hexMap.get("0,0"), "Bench Dynasty A", GameConstants.SPECIES_OMNI,
                AiPersonality.MILITARIST, starter);
        placeAdvancedNpc(world, hexMap.get("2,0"), "Bench Dynasty B", GameConstants.SPECIES_LEAFCUTTER,
                AiPersonality.MILITARIST, starter);
        placeAdvancedNpc(world, hexMap.get("-2,0"), "Bench Dynasty C", GameConstants.SPECIES_TURTLE,
                AiPersonality.PACIFIST, starter);
        placeAdvancedNpc(world, hexMap.get("0,2"), "Bench Dynasty D", GameConstants.SPECIES_CARPENTER,
                AiPersonality.PACIFIST, starter);

        world.bindDynastyTradeServices();
        world.markColonizedHexIndexDirty();
        return world;
    }

    public static int colonizedHexCount(World world) {
        if (world == null || world.getHexes() == null) {
            return 0;
        }
        int count = 0;
        for (Hex hex : world.getHexes()) {
            if (hex != null && hex.getColony() != null) {
                count++;
            }
        }
        return count;
    }

    private static void linkNeighbors(Map<String, Hex> hexMap) {
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
    }

    private static Dynasty placeAdvancedNpc(
            World world,
            Hex hex,
            String name,
            AntSpecies species,
            AiPersonality personality,
            ColonyStarterService starter) {
        Dynasty dynasty = new Dynasty(world.allocateDynastyId(), name, false, species);
        dynasty.setAiPersonality(personality);
        dynasty.getStarterService().initializeDynasty(dynasty);
        unlockAdvancedProgression(dynasty);

        Colony capital = new Colony(world.getNextColonyId(), name + " Prime", false);
        dynasty.addColony(capital);
        dynasty.setCapital(capital);
        starter.initializeNewColony(capital);
        boostColony(capital);
        hex.setColony(capital);
        capital.setActive(false);
        world.registerDynasty(dynasty);
        return dynasty;
    }

    private static void unlockAdvancedProgression(Dynasty dynasty) {
        Upgrade[] upgrades = {
                GameUnlocks.ABILITY_RESEARCH,
                GameUnlocks.ABILITY_BUILD,
                GameUnlocks.ROLE_BUILDER,
                GameUnlocks.ROLE_NURSE,
                GameUnlocks.ROLE_FORAGER,
                GameUnlocks.ROLE_FARMER,
                GameUnlocks.ROLE_HUNTER,
                GameUnlocks.ROLE_LAYER,
                GameUnlocks.ROLE_BREEDER
        };
        for (Upgrade upgrade : upgrades) {
            dynasty.unlockUpgrade(upgrade);
        }
    }

    private static void boostColony(Colony colony) {
        colony.setAge(7);
        for (int i = colony.getWorkers().size(); i < 40; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
        colony.setMushrooms(5000);
        colony.setPlants(5000);
        colony.setWater(5000);
        colony.setAssignedRoleCount(GameConstants.ROLE_NURSE, 10);
        colony.setAssignedRoleCount(GameConstants.ROLE_FORAGER, 8);
        colony.setAssignedRoleCount(GameConstants.ROLE_FARMER, 4);
    }
}
