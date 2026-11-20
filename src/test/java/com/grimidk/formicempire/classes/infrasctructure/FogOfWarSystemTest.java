package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.constants.dynasty.AiPersonality;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.CrossDynastyTradeProposal;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyAiService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyDiplomacyService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyIntegrationService;
import com.grimidk.formicempire.classes.entities.services.world.WorldHistoryEvent;
import com.grimidk.formicempire.classes.entities.services.world.WorldHistoryEventType;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.world.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

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

    @Test
    void undiscoveredDynastiesBlockDiplomacySpiesIntegrationAiAndMaskHistory() {
        Engine engine = engineWithoutSandboxCheats();
        World world = new World();
        world.setEngine(engine);

        Dynasty player = new Dynasty(1, "Alpha Empire", true, GameConstants.SPECIES_OMNI);
        Dynasty unknownAi = new Dynasty(2, "Shadow Swarm", false, GameConstants.SPECIES_FIRE);
        Dynasty discoveredAi = new Dynasty(3, "Sunny Swarm", false, GameConstants.SPECIES_LEAFCUTTER);
        unknownAi.setAiPersonality(AiPersonality.PACIFIST);
        discoveredAi.setAiPersonality(AiPersonality.MILITARIST);
        player.setOwningWorld(world);
        unknownAi.setOwningWorld(world);
        discoveredAi.setOwningWorld(world);
        world.getDynastys().add(player);
        world.getDynastys().add(unknownAi);
        world.getDynastys().add(discoveredAi);

        Hex playerHex = makeHex(0, 0, GameConstants.BIOME_PLAINS);
        Hex adjacentHex = makeHex(1, 0, GameConstants.BIOME_PLAINS);
        Hex farHex = makeHex(8, 8, GameConstants.BIOME_DESERT);
        playerHex.setSouthEast(adjacentHex);
        adjacentHex.setNorthWest(playerHex);
        world.getHexes().add(playerHex);
        world.getHexes().add(adjacentHex);
        world.getHexes().add(farHex);

        Colony playerCol = new Colony(1, "Alpha Capital", true);
        playerCol.setCapital(true);
        playerCol.setDynasty(player);
        player.addColony(playerCol);
        player.setCapital(playerCol);
        playerHex.setColony(playerCol);

        Colony discoveredCol = new Colony(2, "Sunny Capital", false);
        discoveredCol.setCapital(true);
        discoveredCol.setDynasty(discoveredAi);
        discoveredAi.addColony(discoveredCol);
        discoveredAi.setCapital(discoveredCol);
        adjacentHex.setColony(discoveredCol);

        Colony unknownCol = new Colony(3, "Shadow Capital", false);
        unknownCol.setCapital(true);
        unknownCol.setDynasty(unknownAi);
        unknownAi.addColony(unknownCol);
        unknownAi.setCapital(unknownCol);
        farHex.setColony(unknownCol);

        for (int i = 0; i < GameNumbers.WAR_DECLARATION_MIN_POPULATION; i++) {
            playerCol.getWorkers().add(new Ant(playerCol, GameConstants.CLASS_WORKER));
        }
        for (int i = 0; i < GameNumbers.GENETIC_EXCHANGE_DRONE_COST; i++) {
            Ant drone = new Ant(playerCol, GameConstants.CLASS_DRONE);
            playerCol.getDrones().add(drone);
        }
        for (int i = 0; i < 4; i++) {
            Ant princess = new Ant(playerCol, GameConstants.CLASS_PRINCESS);
            princess.setRole(GameConstants.ROLE_DIPLOMAT);
            playerCol.getPrincesses().add(princess);
        }
        Ant spyPrincess = new Ant(playerCol, GameConstants.CLASS_PRINCESS);
        spyPrincess.setRole(GameConstants.ROLE_SPY);
        playerCol.getPrincesses().add(spyPrincess);

        Ant soldier = new Ant(playerCol, GameConstants.CLASS_SOLDIER);
        soldier.setRole(GameConstants.ROLE_WARRIOR);
        playerCol.getSoldiers().add(soldier);
        discoveredCol.getWorkers().add(new Ant(discoveredCol, GameConstants.CLASS_WORKER));
        unknownCol.getWorkers().add(new Ant(unknownCol, GameConstants.CLASS_WORKER));

        player.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
        player.unlockUpgrade(GameUnlocks.ROLE_WARRIOR);
        player.unlockUpgrade(GameUnlocks.ROLE_DIPLOMAT);
        player.unlockUpgrade(GameUnlocks.ROLE_SPY);
        player.unlockUpgrade(GameUnlocks.ABILITY_TRADE);
        playerCol.setAssignedRoleCount(GameConstants.ROLE_DIPLOMAT, 4);
        playerCol.setAssignedRoleCount(GameConstants.ROLE_SPY, 1);
        playerCol.setAssignedRoleCount(GameConstants.ROLE_WARRIOR, 1);
        playerCol.setWarAssignedRoleCount(GameConstants.ROLE_WARRIOR, 1);

        player.discoverHexAndAdjacent(playerHex);
        unknownAi.discoverHexAndAdjacent(farHex);
        discoveredAi.discoverHexAndAdjacent(adjacentHex);

        player.setDiplomaticReputation(unknownAi.getId(), 100);
        unknownAi.setDiplomaticReputation(player.getId(), 100);
        player.setDiplomaticReputation(discoveredAi.getId(), 100);
        discoveredAi.setDiplomaticReputation(player.getId(), 100);

        DynastyDiplomacyService diplo = player.getDiplomacyService();
        assertFalse(diplo.canFormNonAggressionPact(unknownAi, world));
        assertFalse(diplo.canProposeCrossDynastyTrade(unknownAi, world, CrossDynastyTradeProposal.Kind.OFFER));
        assertFalse(diplo.canSendDiplomatsToDynasty(playerCol, unknownAi, world));
        assertFalse(diplo.canManageDynastyMissionDiplomats(unknownAi, world));
        assertFalse(diplo.canOfferGeneticExchange(unknownAi, world));
        assertFalse(diplo.canDeclareWar(unknownAi, world));
        assertFalse(player.getIntelligenceService().canManageSpiesToward(unknownAi, world));
        diplo.assignDynastyMissionDiplomats(unknownAi, 1, world);
        player.getIntelligenceService().assignSpiesToward(unknownAi, 1, world);
        assertEquals(0, diplo.countDynastyMissionDiplomatsToward(unknownAi));
        assertEquals(0, player.getIntelligenceService().countSpiesToward(unknownAi));

        assertTrue(diplo.canFormNonAggressionPact(discoveredAi, world));
        assertTrue(diplo.canDeclareWar(discoveredAi, world));
        assertTrue(diplo.canOfferGeneticExchange(discoveredAi, world));
        assertTrue(player.getIntelligenceService().canManageSpiesToward(discoveredAi, world));

        unknownAi.discoverHex(playerHex);
        unknownAi.getDiplomacyService().requestNonAggressionPact(player, world);
        assertFalse(player.hasPendingPactRequestFrom(unknownAi.getId()));

        new DynastyAiService().runDailyAi(unknownAi, world, new TradeManager());
        assertFalse(unknownAi.getDiplomacyService().hasNonAggressionPact(player));
        assertFalse(DynastyIntegrationService.canStartIntegration(player, unknownAi, world, new TradeManager()));

        WorldHistoryEvent hiddenEvent = new WorldHistoryEvent(
                1, 1, 1, 0, 0,
                WorldHistoryEventType.DYNASTY_FORMED,
                LanguageStrings.HISTORY_DYNASTY_FORMED_FMT,
                List.of(WorldHistoryEvent.dynastyArg(unknownAi.getId())),
                unknownAi.getId(), -1, -1);
        WorldHistoryEvent mixedWarEvent = new WorldHistoryEvent(
                1, 1, 2, 0, 0,
                WorldHistoryEventType.WAR_STARTED,
                LanguageStrings.HISTORY_WAR_STARTED_FMT,
                List.of(WorldHistoryEvent.dynastyArg(discoveredAi.getId()),
                        WorldHistoryEvent.dynastyArg(unknownAi.getId()),
                        WorldHistoryEvent.plainArg("1")),
                discoveredAi.getId(), -1, -1);
        assertFalse(hiddenEvent.isVisibleToViewer(world, player));
        assertTrue(mixedWarEvent.isVisibleToViewer(world, player));
        String maskedMsg = mixedWarEvent.formatMessage(world, player);
        assertTrue(maskedMsg.contains(LanguageStrings.get(LanguageStrings.DIPLO_UNKNOWN_DYNASTY)));
        assertFalse(maskedMsg.contains("Shadow Swarm"));

        player.discoverHex(farHex);
        assertTrue(diplo.canFormNonAggressionPact(unknownAi, world));
        assertTrue(diplo.canOfferGeneticExchange(unknownAi, world));
        assertTrue(player.getIntelligenceService().canManageSpiesToward(unknownAi, world));
        assertTrue(hiddenEvent.isVisibleToViewer(world, player));

        Hex secondUnknownHex = makeHex(10, 10, GameConstants.BIOME_DESERT);
        world.getHexes().add(secondUnknownHex);
        Colony hiddenOutpost = new Colony(4, "Hidden Outpost", false);
        hiddenOutpost.setDynasty(unknownAi);
        unknownAi.addColony(hiddenOutpost);
        secondUnknownHex.setColony(hiddenOutpost);
        player.setKnownHexKeys(Set.of(GameNumbers.packHexKey(0, 0), GameNumbers.packHexKey(1, 0)));
        assertFalse(diplo.canFormNonAggressionPact(unknownAi, world));
        engine.setCheatFogOfWarToggle(true);
        engine.setCheatRevealMap(true);
        assertTrue(diplo.canFormNonAggressionPact(unknownAi, world));
    }
}
