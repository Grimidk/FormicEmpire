package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.world.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FogOfWarSystemTest {

    private static Hex makeHex(int q, int r, Biome biome) {
        Hex h = new Hex();
        h.setQ(q);
        h.setR(r);
        h.setBiome(biome);
        return h;
    }

    @Test
    void packAndUnpackHexKeyRoundTrip() {
        int[] qs = {0, 1, -1, 42, -50, 127, -128};
        int[] rs = {0, 1, -1, -33, 99, 127, -128};
        for (int q : qs) {
            for (int r : rs) {
                long key = GameNumbers.packHexKey(q, r);
                assertEquals(q, GameNumbers.unpackHexQ(key));
                assertEquals(r, GameNumbers.unpackHexR(key));
            }
        }
    }

    @Test
    void dynastyDiscoveryAndKnowledgeSharing() {
        Dynasty d1 = new Dynasty(1, "Dynasty 1", true, GameConstants.SPECIES_GHOST);
        Dynasty d2 = new Dynasty(2, "Dynasty 2", false, GameConstants.SPECIES_GHOST);

        Hex h1 = makeHex(0, 0, GameConstants.BIOME_PLAINS);
        d1.discoverHexAndAdjacent(h1);

        assertTrue(d1.isHexKnown(0, 0));
        assertTrue(d1.isHexKnown(1, 0));
        assertTrue(d1.isHexKnown(0, 1));
        assertTrue(d1.isHexKnown(-1, 1));
        assertFalse(d1.isHexKnown(5, 5));
        assertEquals(7, d1.getKnownHexCount());

        Hex h2 = makeHex(5, 5, GameConstants.BIOME_DESERT);
        d2.discoverHexAndAdjacent(h2);
        assertTrue(d2.isHexKnown(5, 5));
        assertFalse(d2.isHexKnown(0, 0));

        int learnedBy1 = d1.learnHexesFrom(d2);
        assertEquals(7, learnedBy1);
        assertTrue(d1.isHexKnown(5, 5));
        assertEquals(14, d1.getKnownHexCount());

        int learnedAgain = d1.learnHexesFrom(d2);
        assertEquals(0, learnedAgain);
    }

    @Test
    void hasDiscoveredDynastyCheck() {
        World world = new World();
        Dynasty player = new Dynasty(1, "Player", true, GameConstants.SPECIES_GHOST);
        Dynasty ai = new Dynasty(2, "AI", false, GameConstants.SPECIES_GHOST);
        world.getDynastys().add(player);
        world.getDynastys().add(ai);

        Hex playerHex = makeHex(0, 0, GameConstants.BIOME_PLAINS);
        Hex aiHex = makeHex(6, 6, GameConstants.BIOME_FOREST);
        world.getHexes().add(playerHex);
        world.getHexes().add(aiHex);

        Colony cPlayer = new Colony(1, "Player Colony", true);
        cPlayer.setDynasty(player);
        player.addColony(cPlayer);
        playerHex.setColony(cPlayer);

        Colony cAi = new Colony(2, "AI Colony", false);
        cAi.setDynasty(ai);
        ai.addColony(cAi);
        aiHex.setColony(cAi);

        player.discoverHexAndAdjacent(playerHex);
        ai.discoverHexAndAdjacent(aiHex);

        assertFalse(player.hasDiscoveredDynasty(ai, world));

        player.discoverHex(aiHex);
        assertTrue(player.hasDiscoveredDynasty(ai, world));
    }

    private static Engine engineWithoutSandboxCheats() {
        Engine engine = new Engine();
        engine.setFreeAbilities(false);
        engine.setInfiniteResearch(false);
        engine.setInstantBuildings(false);
        engine.setAssimilateAll(false);
        engine.setEasyConquering(false);
        engine.setInstantIntegration(false);
        engine.setCheatRevealMap(false);
        engine.setCheatFogOfWarToggle(false);
        return engine;
    }

    @Test
    void worldGenAnimationOnByDefault() {
        Engine engine = new Engine();
        assertTrue(engine.isAnimateWorldGen());
    }

    @Test
    void cheatRevealMapTaintsAchievements() {
        Engine engine = engineWithoutSandboxCheats();
        World world = new World();
        engine.setWorld(world);
        assertTrue(world.allowsAchievements());

        engine.setCheatFogOfWarToggle(true);
        assertFalse(engine.hasAchievementTaintingSandbox());

        engine.setCheatRevealMap(true);
        assertTrue(engine.hasAchievementTaintingSandbox());
        engine.applySandboxTaintToActiveWorld();
        assertFalse(world.allowsAchievements());
    }

    @Test
    void scoutsWanderAndDiscoverWhenSourcesAreFull() {
        World world = new World();
        Dynasty dynasty = new Dynasty(1, "Test Dynasty", true, GameConstants.SPECIES_GHOST);
        dynasty.setOwningWorld(world);
        world.getDynastys().add(dynasty);

        Hex homeHex = makeHex(0, 0, GameConstants.BIOME_PLAINS);
        Hex neighborHex = makeHex(1, 0, GameConstants.BIOME_PLAINS);
        world.getHexes().add(homeHex);
        world.getHexes().add(neighborHex);

        Colony colony = new Colony(1, "Home Colony", true);
        colony.setDynasty(dynasty);
        dynasty.addColony(colony);
        homeHex.setColony(colony);
        colony.unlockUpgrade(GameUnlocks.ROLE_SCOUT);
        colony.setAssignedRoleCount(GameConstants.ROLE_SCOUT, 5);

        dynasty.discoverHex(homeHex);
        assertFalse(dynasty.isHexKnown(neighborHex));

        for (int i = 0; i < 20; i++) {
            colony.getLocationService().addSource(colony,
                    new ResourceSource(GameConstants.RESOURCE_PLANT, 1000, 100, 100));
            colony.getLocationService().addSource(colony,
                    new ResourceSource(GameConstants.RESOURCE_WATER, 1000, 100, 100));
            colony.getLocationService().addSource(colony,
                    new ResourceSource(GameConstants.RESOURCE_MEAT, 1000, 100, 100));
            colony.getLocationService().addSource(colony,
                    new ResourceSource(GameConstants.RESOURCE_ROCK, 1000, 100, 100));
            colony.getLocationService().addSource(colony,
                    new ResourceSource(GameConstants.RESOURCE_FUNGI, 1000, 100, 100));
            colony.getLocationService().addSource(colony,
                    new ResourceSource(GameConstants.RESOURCE_RESIN, 1000, 100, 100));
        }

        for (int hour = 0; hour < 40; hour++) {
            colony.runScoutting(GameConstants.BIOME_PLAINS, homeHex);
            if (dynasty.isHexKnown(neighborHex)) {
                break;
            }
        }

        assertTrue(dynasty.isHexKnown(neighborHex));
    }

    @Test
    void standardScoutsCannotDiscoverOceanWithoutLandNeighbor() {
        World world = new World();
        Dynasty dynasty = new Dynasty(1, "Test Dynasty", true, GameConstants.SPECIES_GHOST);
        dynasty.setOwningWorld(world);
        world.getDynastys().add(dynasty);

        Hex homeHex = makeHex(0, 0, GameConstants.BIOME_PLAINS);
        Hex oceanHex = makeHex(2, 0, GameConstants.BIOME_DEEP_OCEAN);
        world.getHexes().add(homeHex);
        world.getHexes().add(oceanHex);

        Colony colony = new Colony(1, "Home Colony", true);
        colony.setDynasty(dynasty);
        dynasty.addColony(colony);
        homeHex.setColony(colony);
        colony.unlockUpgrade(GameUnlocks.ROLE_SCOUT);
        colony.setAssignedRoleCount(GameConstants.ROLE_SCOUT, 5);

        dynasty.discoverHex(homeHex);

        for (int i = 0; i < 20; i++) {
            colony.getLocationService().addSource(colony,
                    new ResourceSource(GameConstants.RESOURCE_PLANT, 1000, 100, 100));
            colony.getLocationService().addSource(colony,
                    new ResourceSource(GameConstants.RESOURCE_WATER, 1000, 100, 100));
            colony.getLocationService().addSource(colony,
                    new ResourceSource(GameConstants.RESOURCE_MEAT, 1000, 100, 100));
            colony.getLocationService().addSource(colony,
                    new ResourceSource(GameConstants.RESOURCE_ROCK, 1000, 100, 100));
            colony.getLocationService().addSource(colony,
                    new ResourceSource(GameConstants.RESOURCE_FUNGI, 1000, 100, 100));
            colony.getLocationService().addSource(colony,
                    new ResourceSource(GameConstants.RESOURCE_RESIN, 1000, 100, 100));
        }

        for (int hour = 0; hour < 50; hour++) {
            colony.runScoutting(GameConstants.BIOME_PLAINS, homeHex);
        }

        assertFalse(dynasty.isHexKnown(oceanHex));

        oceanHex.setSouth(homeHex);
        for (int hour = 0; hour < 50; hour++) {
            colony.runScoutting(GameConstants.BIOME_PLAINS, homeHex);
            if (dynasty.isHexKnown(oceanHex)) {
                break;
            }
        }
        assertTrue(dynasty.isHexKnown(oceanHex));
    }

    @Test
    void explorerPrincessCanDiscoverDeepOceanAndExtendedRange() {
        World world = new World();
        Dynasty dynasty = new Dynasty(1, "Test Dynasty", true, GameConstants.SPECIES_GHOST);
        dynasty.setOwningWorld(world);
        world.getDynastys().add(dynasty);

        Hex homeHex = makeHex(0, 0, GameConstants.BIOME_PLAINS);
        Hex deepOceanHex = makeHex(2, 0, GameConstants.BIOME_DEEP_OCEAN);
        Hex farHex = makeHex(8, 0, GameConstants.BIOME_DESERT);
        world.getHexes().add(homeHex);
        world.getHexes().add(deepOceanHex);
        world.getHexes().add(farHex);

        Colony colony = new Colony(1, "Home Colony", true);
        colony.setDynasty(dynasty);
        dynasty.addColony(colony);
        homeHex.setColony(colony);
        colony.unlockUpgrade(GameUnlocks.ROLE_EXPLORER);
        colony.setAssignedRoleCount(GameConstants.ROLE_EXPLORER, 1);

        dynasty.discoverHex(homeHex);

        for (int hour = 0; hour < 50; hour++) {
            colony.runScoutting(GameConstants.BIOME_PLAINS, homeHex);
            if (dynasty.isHexKnown(deepOceanHex)) {
                break;
            }
        }
        assertTrue(dynasty.isHexKnown(deepOceanHex));

        for (int hour = 0; hour < 60; hour++) {
            colony.runScoutting(GameConstants.BIOME_PLAINS, homeHex);
            if (dynasty.isHexKnown(farHex)) {
                break;
            }
        }
        assertTrue(dynasty.isHexKnown(farHex));
    }

    @Test
    void explorerTriggerRequiresPrincessAndTenExploredHexes() {
        World world = new World();
        Engine engine = new Engine();
        engine.setWorld(world);
        Dynasty dynasty = new Dynasty(1, "Player Dynasty", true, GameConstants.SPECIES_GHOST);
        dynasty.setOwningWorld(world);
        world.getDynastys().add(dynasty);

        Hex homeHex = makeHex(0, 0, GameConstants.BIOME_PLAINS);
        world.getHexes().add(homeHex);

        Colony colony = new Colony(1, "Player Colony", true);
        colony.setDynasty(dynasty);
        dynasty.addColony(colony);
        homeHex.setColony(colony);

        dynasty.setExploredHexCount(9);

        com.grimidk.formicempire.classes.infrasctructure.managers.TriggerManager tm =
                new com.grimidk.formicempire.classes.infrasctructure.managers.TriggerManager(world, colony, engine);
        tm.registerListeners();

        engine.notifyHourListeners();
        assertFalse(colony.hasUpgrade(GameUnlocks.ROLE_EXPLORER));

        dynasty.setExploredHexCount(10);
        engine.notifyHourListeners();
        assertFalse(colony.hasUpgrade(GameUnlocks.ROLE_EXPLORER));

        colony.unlockUpgrade(GameUnlocks.TYPE_PRINCESS);
        dynasty.setExploredHexCount(9);
        engine.notifyHourListeners();
        assertFalse(colony.hasUpgrade(GameUnlocks.ROLE_EXPLORER));

        dynasty.setExploredHexCount(10);
        engine.notifyHourListeners();
        assertTrue(colony.hasUpgrade(GameUnlocks.ROLE_EXPLORER));
        tm.unregisterListeners();
    }
}
