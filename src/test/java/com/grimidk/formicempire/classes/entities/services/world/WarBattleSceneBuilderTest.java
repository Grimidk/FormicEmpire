package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.War;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarBattleSceneBuilderTest {

    private Dynasty player;
    private Dynasty neighbor;
    private World world;

    @BeforeEach
    void setUp() {
        player = new Dynasty(1, "Meat Dynasty", true, GameConstants.SPECIES_OMNI);
        neighbor = new Dynasty(2, "Crystal Dynasty", false, GameConstants.SPECIES_OMNI);
        world = buildBorderWorld();
    }

    @Test
    void buildsSceneForActiveWarWithTypeCounts() {
        player.getDiplomacyService().applyWar(neighbor, null, world);
        War war = world.getWarService().findActiveWar(player.getId(), neighbor.getId());
        assertNotNull(war);

        WarBattleScene scene = WarBattleSceneBuilder.build(world, war, world.getWarService());
        assertTrue(scene.isAvailable());
        assertNotNull(scene.getAttackerBiome());
        assertNotNull(scene.getDefenderBiome());
        assertTrue(scene.getFrontlineRatio() >= 0.05f && scene.getFrontlineRatio() <= 0.95f);
        assertFalse(scene.getAttacker().typeCounts().isEmpty());
        assertFalse(scene.getDefender().typeCounts().isEmpty());
    }

    @Test
    void unavailableWhenCampaignNotInitialized() {
        War war = new War(99, player.getId(), neighbor.getId(), 0, player.getId(),
                "Test War", 100, 100);

        WarBattleScene scene = WarBattleSceneBuilder.build(world, war, world.getWarService());
        assertFalse(scene.isAvailable());
    }

    private World buildBorderWorld() {
        World world = new World();
        Hex playerHex = new Hex();
        Hex neighborHex = new Hex();
        playerHex.setQ(0);
        playerHex.setR(0);
        neighborHex.setQ(1);
        neighborHex.setR(0);
        playerHex.setBiome(GameConstants.BIOME_PLAINS);
        neighborHex.setBiome(GameConstants.BIOME_FOREST);
        playerHex.setNorthEast(neighborHex);
        neighborHex.setSouthWest(playerHex);

        Colony playerColony = new Colony(10, "Capital", true);
        Colony neighborColony = new Colony(11, "Border", false);
        player.addColony(playerColony);
        neighbor.addColony(neighborColony);
        player.setCapital(playerColony);
        neighbor.setCapital(neighborColony);
        playerColony.setDynasty(player);
        neighborColony.setDynasty(neighbor);
        playerHex.setColony(playerColony);
        neighborHex.setColony(neighborColony);
        seedWarPopulation(player, playerColony);
        seedWarPopulation(neighbor, neighborColony);

        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(playerHex);
        hexes.add(neighborHex);
        world.setHexes(hexes);
        world.getDynastys().add(player);
        world.getDynastys().add(neighbor);
        return world;
    }

    private static void seedWarPopulation(Dynasty dynasty, Colony colony) {
        dynasty.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
        int needed = GameNumbers.WAR_DECLARATION_MIN_POPULATION
                - dynasty.getStatService().getTotalPopulation(dynasty);
        for (int i = 0; i < needed; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
        for (int i = 0; i < 20; i++) {
            colony.getSoldiers().add(new Ant(colony, GameConstants.TYPE_SOLDIER));
        }
        colony.getWarAssignedRoleCounts().put(GameConstants.ROLE_WARRIOR, 10);
        colony.getWarAssignedRoleCounts().put(GameConstants.ROLE_DEFENDER, 10);
        ColonyMilitaryService.refreshColonyMilitaryPower(colony);
        ColonyMilitaryService.refreshDynastyMilitaryPower(dynasty);
    }
}
