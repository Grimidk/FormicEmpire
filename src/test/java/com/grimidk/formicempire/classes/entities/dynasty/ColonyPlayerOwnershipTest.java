package com.grimidk.formicempire.classes.entities.dynasty;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class ColonyPlayerOwnershipTest {

    @Test
    void setDynastySyncsIsPlayerFromPlayerDynasty() {
        Dynasty player = new Dynasty(1, "Player", true, GameConstants.SPECIES_OMNI);
        Colony conquered = new Colony(10, "Captured", false);

        assertFalse(conquered.isPlayer());
        conquered.setDynasty(player);

        assertTrue(conquered.isPlayer());
        assertTrue(conquered.belongsToPlayerDynasty());
        assertTrue(player.getColonies().contains(conquered));
    }

    @Test
    void setDynastyClearsIsPlayerWhenTakenByNpc() {
        Dynasty player = new Dynasty(1, "Player", true, GameConstants.SPECIES_OMNI);
        Dynasty npc = new Dynasty(2, "NPC", false, GameConstants.SPECIES_OMNI);
        Colony colony = new Colony(10, "Outpost", true);
        colony.setDynasty(player);

        colony.setDynasty(npc);

        assertFalse(colony.isPlayer());
        assertFalse(colony.belongsToPlayerDynasty());
    }

    @Test
    void belongsToPlayerDynastyUsesDynastyFlag() {
        Dynasty player = new Dynasty(1, "Player", true, GameConstants.SPECIES_OMNI);
        Colony satellite = new Colony(11, "Satellite", true);
        player.addColony(satellite);

        assertTrue(satellite.belongsToPlayerDynasty());
    }
}
