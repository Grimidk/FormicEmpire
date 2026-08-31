package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.critter.BugRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.colony.HuntCreatureCombatService;
import com.grimidk.formicempire.classes.entities.invasion.InvasionAlert;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

class ColonyInvasionServiceTest {

    private Colony colony;

    @BeforeEach
    void setUp() {
        Dynasty dynasty = new Dynasty(1, "Player", true, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "Prime", true);
        dynasty.addColony(colony);
        colony.setDynasty(dynasty);
        dynasty.setCapital(colony);
    }

    @Test
    void antLionUsesInvasionRole() {
        assertTrue(GameConstants.TYPE_ANT_LION.hasBugRole(BugRole.INVASION));
    }

    @Test
    void dispatchStartsBattleAndResolvesOnTick() {
        InvasionAlert alert = new InvasionAlert(
                1,
                GameConstants.TYPE_ANT_LION.getId(),
                InvasionAlert.Scope.COLONY,
                colony.getId(),
                InvasionAlert.toAbsoluteHour(0, 0) + 48);
        colony.getInvasionAlerts().add(alert);

        for (int i = 0; i < 30; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
        colony.runRoleAssignment(null);

        Map<AntType, Integer> counts = new HashMap<>();
        counts.put(GameConstants.TYPE_WORKER, 30);
        assertTrue(ColonyInvasionService.dispatchDefense(colony, 1, counts, 0, 0));
        assertEquals(1, colony.getActiveInvasionDefenses().size());
        assertNotNull(HuntCreatureCombatService.getInvasionState(colony, 1));

        for (int hour = 0; hour < 5000 && !colony.getActiveInvasionDefenses().isEmpty(); hour++) {
            ColonyInvasionService.tickInvasions(colony, hour / 24, hour % 24);
        }
        assertTrue(colony.getActiveInvasionDefenses().isEmpty());
        assertTrue(colony.getInvasionAlerts().isEmpty());
    }

    @Test
    void expiredAlertRemovedAfterRaidTick() {
        InvasionAlert alert = new InvasionAlert(
                1,
                GameConstants.TYPE_ANT_LION.getId(),
                InvasionAlert.Scope.COLONY,
                colony.getId(),
                InvasionAlert.toAbsoluteHour(1, 0));
        colony.getInvasionAlerts().add(alert);
        colony.setMushrooms(1000);
        colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        colony.runRoleAssignment(null);

        ColonyInvasionService.tickInvasions(colony, 1, 0);
        assertTrue(colony.getInvasionAlerts().isEmpty());
        assertTrue(colony.getMushrooms() < 1000);
    }

    @Test
    void winChanceScalesWithPartySize() {
        InvasionAlert alert = new InvasionAlert(
                1,
                GameConstants.TYPE_ANT_LION.getId(),
                InvasionAlert.Scope.COLONY,
                colony.getId(),
                InvasionAlert.toAbsoluteHour(0, 0) + GameNumbers.INVASION_RESPONSE_HOURS);
        Map<AntType, Integer> small = new HashMap<>();
        small.put(GameConstants.TYPE_SOLDIER, 1);
        colony.getSoldiers().add(new Ant(colony, GameConstants.TYPE_SOLDIER));
        colony.runRoleAssignment(null);
        ColonyInvasionService.InvasionDispatchPreview smallPreview =
                ColonyInvasionService.previewDefense(colony, alert, small, 0, 0);

        for (int i = 0; i < 40; i++) {
            colony.getSoldiers().add(new Ant(colony, GameConstants.TYPE_SOLDIER));
        }
        colony.runRoleAssignment(null);
        Map<AntType, Integer> large = new HashMap<>();
        large.put(GameConstants.TYPE_SOLDIER, 40);
        ColonyInvasionService.InvasionDispatchPreview largePreview =
                ColonyInvasionService.previewDefense(colony, alert, large, 0, 0);

        assertTrue(largePreview.winChance >= smallPreview.winChance);
        assertFalse(largePreview.winChance <= 0f);
    }
}
