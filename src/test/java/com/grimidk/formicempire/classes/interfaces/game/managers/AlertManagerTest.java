package com.grimidk.formicempire.classes.interfaces.game.managers;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.game.gamepanels.AlertPanel;
import com.grimidk.formicempire.classes.interfaces.game.gamepanels.AlertPanel.Alert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    void ingestSuccessShowsBuildingNameWithIconAndNoBuiltPrefix() {
        Colony colony = new Colony(1, "Prime", true);
        AlertManager alertManager = new AlertManager(colony, new AlertPanel());

        alertManager.ingestLogEvent(ColonyLogPrefixes.SUCCESS + " Nest Chamber");

        Alert alert = alertManager.findAlert("SUCC");
        assertNotNull(alert);
        assertEquals("Nest Chamber", alert.message);
        assertNotNull(alert.icon);
    }

    @Test
    void ingestDeathShortensToCountWithFullTooltip() {
        Colony colony = new Colony(1, "Prime", true);
        AlertManager alertManager = new AlertManager(colony, new AlertPanel());

        alertManager.ingestLogEvent(ColonyLogPrefixes.DEATH + " 1500 ants died of old age");

        Alert alert = alertManager.findAlert("DEATH");
        assertNotNull(alert);
        assertEquals("1.5k", alert.message);
        assertTrue(alert.tooltip.contains("1500 ants died of old age"));
        assertTrue(alert.tooltip.contains("Ants died") || alert.tooltip.contains("<html>"));
        assertNotNull(alert.icon);
    }

    @Test
    void hoveredAlertDoesNotExpireUntilHoverEnds() throws InterruptedException {
        Alert alert = new Alert("INFO", "Test", null, "detail", 30);
        alert.setHovered(true);
        Thread.sleep(50);
        assertFalse(alert.isExpired());
        alert.setHovered(false);
        Thread.sleep(50);
        assertTrue(alert.isExpired());
    }

    @Test
    void rebellionAlertShowsColonyNameWithRiskTooltip() {
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

        Alert alert = alertManager.findAlert("REBEL");
        assertNotNull(alert);
        assertEquals("Secundus", alert.message);
        assertTrue(alert.tooltip.contains("Secundus"));
        assertNotNull(alert.icon);
    }

    @Test
    void checkStatusConsumesEventsFromAllPlayerColoniesNotViewedForeignColony() {
        World world = new World();
        Dynasty player = new Dynasty(1, "Crystal Dynasty", true, GameConstants.SPECIES_OMNI);
        Colony capital = colonyOnHex(world, 1, "Prime", 0, 0);
        Colony satellite = colonyOnHex(world, 2, "Secundus", 2, 0);
        player.addColony(capital);
        player.addColony(satellite);
        player.setCapital(capital);
        world.getDynastys().add(player);

        Dynasty npc = new Dynasty(2, "Wild Dynasty", false, GameConstants.SPECIES_OMNI);
        Colony foreign = colonyOnHex(world, 3, "Foreign", 4, 0);
        npc.addColony(foreign);
        world.getDynastys().add(npc);

        capital.logEvent(ColonyLogPrefixes.SUCCESS + " Capital Nest");
        satellite.logEvent(ColonyLogPrefixes.INFO + " Satellite scouted");
        foreign.logEvent(ColonyLogPrefixes.WARNING + " Foreign should be ignored");

        AlertManager alertManager = new AlertManager(player, new AlertPanel());
        alertManager.checkStatus();

        assertTrue(alertManager.hasActiveAlert("SUCC"));
        assertTrue(alertManager.hasActiveAlert("INFO"));
        assertFalse(alertManager.hasActiveAlert("WARN"));
        assertEquals(1, foreign.consumeEvents().size());
    }

    @Test
    void checkStatusIgnoresNpcDynastyEvenWhenSeededFromNpcColony() {
        World world = new World();
        Dynasty npc = new Dynasty(1, "Wild Dynasty", false, GameConstants.SPECIES_OMNI);
        Colony foreign = colonyOnHex(world, 1, "Foreign", 0, 0);
        npc.addColony(foreign);
        world.getDynastys().add(npc);

        foreign.logEvent(ColonyLogPrefixes.WARNING + " Should never alert");

        AlertManager alertManager = new AlertManager(foreign, new AlertPanel());
        alertManager.checkStatus();

        assertFalse(alertManager.hasActiveAlert("WARN"));
        assertEquals(1, foreign.consumeEvents().size());
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
