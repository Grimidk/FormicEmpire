package com.grimidk.formicempire.classes.entities.services.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.misc.GameSpeed;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class SandboxCheatServiceTest {

    @Test
    void cheatConquerTransfersForeignColony() {
        Engine engine = new Engine();
        engine.setEasyConquering(true);
        World world = new World();
        engine.setWorld(world);

        Hex home = new Hex();
        home.setQ(0);
        home.setR(0);
        Hex other = new Hex();
        other.setQ(1);
        other.setR(0);
        world.getHexes().add(home);
        world.getHexes().add(other);

        Dynasty player = new Dynasty(1, "Player", true, GameConstants.SPECIES_OMNI);
        Dynasty npc = new Dynasty(2, "Npc", false, GameConstants.SPECIES_OMNI);
        Colony playerColony = new Colony(1, "Home", true);
        Colony npcColony = new Colony(2, "Target", false);
        playerColony.setCapital(true);
        npcColony.setCapital(true);
        player.addColony(playerColony);
        npc.addColony(npcColony);
        home.setColony(playerColony);
        other.setColony(npcColony);
        world.registerDynasty(player);
        world.registerDynasty(npc);

        assertTrue(SandboxCheatService.isForeignColony(world, npcColony));
        assertFalse(SandboxCheatService.isForeignColony(world, playerColony));
        assertTrue(SandboxCheatService.cheatConquerColony(engine, world, npcColony));
        assertEquals(player.getId(), npcColony.getDynasty().getId());
        assertFalse(SandboxCheatService.isForeignColony(world, npcColony));
    }

    @Test
    void cheatConquerIgnoresOwnColony() {
        Engine engine = new Engine();
        engine.setEasyConquering(true);
        World world = new World();
        engine.setWorld(world);

        Hex home = new Hex();
        home.setQ(0);
        home.setR(0);
        world.getHexes().add(home);

        Dynasty player = new Dynasty(1, "Player", true, GameConstants.SPECIES_OMNI);
        Colony playerColony = new Colony(1, "Home", true);
        playerColony.setCapital(true);
        player.addColony(playerColony);
        home.setColony(playerColony);
        world.registerDynasty(player);

        assertFalse(SandboxCheatService.cheatConquerColony(engine, world, playerColony));
        assertEquals(player.getId(), playerColony.getDynasty().getId());
    }

    @Test
    void cheatConquerFoundsColonyOnEmptyLandHex() {
        Engine engine = new Engine();
        engine.setEasyConquering(true);
        World world = new World();
        engine.setWorld(world);

        Hex home = new Hex();
        home.setQ(0);
        home.setR(0);
        home.setBiome(GameConstants.BIOME_PLAINS);
        Hex empty = new Hex();
        empty.setQ(1);
        empty.setR(0);
        empty.setBiome(GameConstants.BIOME_PLAINS);
        world.getHexes().add(home);
        world.getHexes().add(empty);

        Dynasty player = new Dynasty(1, "Player", true, GameConstants.SPECIES_OMNI);
        Colony playerColony = new Colony(world.getNextColonyId(), "Home", true);
        playerColony.setCapital(true);
        player.addColony(playerColony);
        player.setCapital(playerColony);
        home.setColony(playerColony);
        world.registerDynasty(player);

        assertTrue(SandboxCheatService.canCheatConquerHex(world, empty));
        assertTrue(SandboxCheatService.cheatConquerHex(engine, world, empty));
        assertEquals(player.getId(), empty.getColony().getDynasty().getId());
    }

    @Test
    void cheatConquerRejectsOceanHex() {
        Engine engine = new Engine();
        engine.setEasyConquering(true);
        World world = new World();
        engine.setWorld(world);

        Hex ocean = new Hex();
        ocean.setQ(2);
        ocean.setR(0);
        ocean.setBiome(GameConstants.BIOME_OCEAN);
        world.getHexes().add(ocean);

        Dynasty player = new Dynasty(1, "Player", true, GameConstants.SPECIES_OMNI);
        world.registerDynasty(player);

        assertFalse(SandboxCheatService.canCheatConquerHex(world, ocean));
        assertFalse(SandboxCheatService.cheatConquerHex(engine, world, ocean));
        assertNull(ocean.getColony());
    }

    @Test
    void infiniteResearchUnlocksResearchTab() {
        Engine engine = engineWithoutSandboxCheats();
        engine.setInfiniteResearch(true);
        World world = new World();
        engine.setWorld(world);
        Dynasty player = new Dynasty(1, "Player", true, GameConstants.SPECIES_OMNI);
        world.registerDynasty(player);

        SandboxCheatService.applyEnabledCheats(engine);
        assertTrue(player.hasUpgrade(GameUnlocks.ABILITY_RESEARCH));
        assertTrue(player.hasUpgrade(GameUnlocks.ROLE_RESEARCHER));
    }

    @Test
    void instantBuildingsUnlocksConstructionTab() {
        Engine engine = engineWithoutSandboxCheats();
        engine.setInstantBuildings(true);
        World world = new World();
        engine.setWorld(world);
        Dynasty player = new Dynasty(1, "Player", true, GameConstants.SPECIES_OMNI);
        world.registerDynasty(player);

        SandboxCheatService.applyEnabledCheats(engine);
        assertTrue(player.hasUpgrade(GameUnlocks.ABILITY_BUILD));
        assertTrue(player.hasUpgrade(GameUnlocks.ROLE_BUILDER));
    }

    @Test
    void assimilateAllUnlocksAssimilationTab() {
        Engine engine = engineWithoutSandboxCheats();
        engine.setAssimilateAll(true);
        World world = new World();
        engine.setWorld(world);
        Dynasty player = new Dynasty(1, "Player", true, GameConstants.SPECIES_OMNI);
        world.registerDynasty(player);

        SandboxCheatService.applyEnabledCheats(engine);
        assertTrue(player.hasUpgrade(GameUnlocks.ABILITY_ASSIMILATION));
        assertTrue(GameUnlocks.shouldShowAssimilationUi(player));
    }

    @Test
    void possessHexTakesControlOfSpectatorColonyAndEndsSpectatorMode() {
        Engine engine = engineWithoutSandboxCheats();
        engine.setAllowTurboMode(false);
        World world = new World();
        world.setSpectatorMode(true);
        engine.setWorld(world);
        engine.setSpeed(GameSpeed.fromId(GameSpeed.ID_HYPER_TURBO));

        Hex hexA = new Hex();
        hexA.setQ(0);
        hexA.setR(0);
        Hex hexB = new Hex();
        hexB.setQ(1);
        hexB.setR(0);
        world.getHexes().add(hexA);
        world.getHexes().add(hexB);

        Dynasty npc = new Dynasty(2, "Rival", false, GameConstants.SPECIES_OMNI);
        Colony colA = new Colony(10, "Alpha", false);
        colA.getWorkers().add(new Ant(colA, GameConstants.CLASS_WORKER));
        Colony colB = new Colony(11, "Beta", false);
        colB.getWorkers().add(new Ant(colB, GameConstants.CLASS_WORKER));
        npc.addColony(colA);
        npc.addColony(colB);
        hexA.setColony(colA);
        hexB.setColony(colB);
        world.registerDynasty(npc);

        assertTrue(SandboxCheatService.canPossessHex(world, hexB));
        assertTrue(SandboxCheatService.possessHex(engine, world, hexB));

        assertFalse(world.isSpectatorMode());
        assertTrue(npc.isPlayer());
        assertTrue(colA.isPlayer());
        assertTrue(colB.isPlayer());
        assertEquals(hexB, world.getActiveHex());
        assertEquals(npc, world.findPlayerDynasty());
        assertTrue(engine.getSpeed().getId() <= GameSpeed.maxPlayableId(false, false));
    }

    @Test
    void canPossessHexRejectsNonSpectatorEmptyOrDefeatedTargets() {
        World world = new World();
        world.setSpectatorMode(true);
        Hex empty = new Hex();
        empty.setQ(0);
        empty.setR(0);
        world.getHexes().add(empty);

        assertFalse(SandboxCheatService.canPossessHex(world, empty));
        assertFalse(SandboxCheatService.possessHex(null, world, empty));

        Dynasty defeated = new Dynasty(3, "Fallen", false, GameConstants.SPECIES_OMNI);
        defeated.setDefeated(true);
        Colony deadColony = new Colony(12, "Ruins", false);
        defeated.addColony(deadColony);
        empty.setColony(deadColony);
        world.registerDynasty(defeated);

        assertFalse(SandboxCheatService.canPossessHex(world, empty));

        world.setSpectatorMode(false);
        assertFalse(SandboxCheatService.canPossessHex(world, empty));
    }

    @Test
    void depossessToSpectatorReleasesPlayerDynastyAndEntersSpectatorMode() {
        Engine engine = engineWithoutSandboxCheats();
        World world = new World();
        world.setSpectatorMode(false);
        engine.setWorld(world);

        Hex hexA = new Hex();
        hexA.setQ(0);
        hexA.setR(0);
        world.getHexes().add(hexA);

        Dynasty player = new Dynasty(1, "Player", true, GameConstants.SPECIES_OMNI);
        Colony colA = new Colony(10, "Alpha", true);
        colA.getWorkers().add(new Ant(colA, GameConstants.CLASS_WORKER));
        player.addColony(colA);
        hexA.setColony(colA);
        world.registerDynasty(player);
        world.changeActiveHex(hexA);

        assertTrue(SandboxCheatService.canDepossessToSpectator(world));
        assertTrue(SandboxCheatService.depossessToSpectator(engine, world));

        assertTrue(world.isSpectatorMode());
        assertFalse(player.isPlayer());
        assertFalse(colA.isPlayer());
        assertTrue(colA.isAutomationEnabled());
        assertNull(world.findPlayerDynasty());
        assertFalse(SandboxCheatService.canDepossessToSpectator(world));
        assertTrue(SandboxCheatService.canPossessHex(world, hexA));
    }

    @Test
    void triggerRandomBorderWarStartsWarBetweenBorderingDynasties() {
        World world = new World();
        Hex h0 = new Hex(); h0.setQ(0); h0.setR(0); h0.setBiome(GameConstants.BIOME_PLAINS);
        Hex h1 = new Hex(); h1.setQ(1); h1.setR(0); h1.setBiome(GameConstants.BIOME_PLAINS);
        h0.setSouthEast(h1);
        h1.setNorthWest(h0);
        world.getHexes().add(h0);
        world.getHexes().add(h1);

        Dynasty d1 = new Dynasty(1, "One", false, GameConstants.SPECIES_OMNI);
        Dynasty d2 = new Dynasty(2, "Two", false, GameConstants.SPECIES_OMNI);
        Colony c1 = new Colony(1, "Alpha", false);
        Colony c2 = new Colony(2, "Beta", false);
        d1.addColony(c1);
        d2.addColony(c2);
        h0.setColony(c1);
        h1.setColony(c2);
        world.registerDynasty(d1);
        world.registerDynasty(d2);

        War started = SandboxCheatService.triggerRandomBorderWar(world);
        assertEquals(1, world.getWarService().getActiveWars().size());
        assertEquals(started, world.getWarService().findActiveWar(d1.getId(), d2.getId()));
    }

    private static Engine engineWithoutSandboxCheats() {
        Engine engine = new Engine();
        engine.setFreeAbilities(false);
        engine.setInfiniteResearch(false);
        engine.setInstantBuildings(false);
        engine.setAssimilateAll(false);
        engine.setEasyConquering(false);
        engine.setInstantIntegration(false);
        return engine;
    }
}
