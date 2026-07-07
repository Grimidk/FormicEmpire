package com.grimidk.formicempire.classes.interfaces.game.managers;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.game.gamepanels.AlertPanel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertManagerTest {

    @Test
    void checkRebellionRiskShowsAlertForRebelliousSatelliteWhileViewingCapital() {
        World world = new World();
        Dynasty parent = new Dynasty(1, "Crystal Dynasty", true, GameConstants.SPECIES_OMNI);
        Colony capital = colonyOnHex(world, 1, "Prime", 0, 0);
        Colony rebel = colonyOnHex(world, 2, "Secundus", 2, 0);
        rebel.setLoyalty(5);
        parent.addColony(capital);
        parent.addColony(rebel);
        parent.setCapital(capital);
        world.getDynastys().add(parent);

        AlertManager alertManager = new AlertManager(capital, new AlertPanel());
        alertManager.checkRebellionRisk(world, null);

        assertTrue(alertManager.hasActiveAlert("REBEL"));
    }

    @Test
    void checkRebellionRiskHidesAlertWhenNoRebelliousColonies() {
        World world = new World();
        Dynasty parent = new Dynasty(1, "Crystal Dynasty", true, GameConstants.SPECIES_OMNI);
        Colony capital = colonyOnHex(world, 1, "Prime", 0, 0);
        Colony loyal = colonyOnHex(world, 2, "Secundus", 2, 0);
        loyal.setLoyalty(80);
        parent.addColony(capital);
        parent.addColony(loyal);
        parent.setCapital(capital);
        world.getDynastys().add(parent);

        AlertManager alertManager = new AlertManager(capital, new AlertPanel());
        alertManager.checkRebellionRisk(world, null);

        assertFalse(alertManager.hasActiveAlert("REBEL"));
    }

    @Test
    void checkRebellionRiskIgnoresNpcDynasties() {
        World world = new World();
        Dynasty npc = new Dynasty(1, "Wild Dynasty", false, GameConstants.SPECIES_OMNI);
        Colony colony = colonyOnHex(world, 1, "Wild", 0, 0);
        colony.setLoyalty(5);
        npc.addColony(colony);
        world.getDynastys().add(npc);

        AlertManager alertManager = new AlertManager(colony, new AlertPanel());
        alertManager.checkRebellionRisk(world, null);

        assertFalse(alertManager.hasActiveAlert("REBEL"));
    }

    private static Colony colonyOnHex(World world, int id, String name, int q, int r) {
        Hex hex = new Hex();
        hex.setQ(q);
        hex.setR(r);
        Colony colony = new Colony(id, name, false);
        hex.setColony(colony);
        if (world.getHexes() == null) {
            world.setHexes(new ArrayList<>());
        }
        world.getHexes().add(hex);
        return colony;
    }
}
