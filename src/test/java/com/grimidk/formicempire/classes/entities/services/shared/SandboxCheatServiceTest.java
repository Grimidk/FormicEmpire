package com.grimidk.formicempire.classes.entities.services.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.Hex;
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
