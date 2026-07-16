package com.grimidk.formicempire.classes.entities.services.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.entities.services.shared.TriggerProgressService.TriggerProgress;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class TriggerProgressServiceTest {

    private Dynasty dynasty;
    private Colony colony;

    @BeforeEach
    void setUp() {
        dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "Prime", true);
        dynasty.addColony(colony);
        colony.setDynasty(dynasty);
    }

    @Test
    void showsPartialColonyProgressAndUnlockedEntries() {
        dynasty.addColony(new Colony(2, "Secundus", false));
        dynasty.addColony(new Colony(3, "Tertius", false));
        colony.unlockUpgrade(GameUnlocks.ABILITY_DYNASTY);
        colony.unlockUpgrade(GameUnlocks.ABILITY_TRADE);

        List<TriggerProgress> visible = TriggerProgressService.getVisible(colony, null);
        TriggerProgress dynastyProgress = find(visible, GameUnlocks.ABILITY_DYNASTY);
        TriggerProgress management = find(visible, GameUnlocks.ABILITY_MANAGEMENT);

        assertTrue(dynastyProgress != null && dynastyProgress.isUnlocked());
        assertTrue(management != null);
        assertFalse(management.isUnlocked());
        assertEquals(3, management.getCurrent());
        assertEquals(GameConstants.TRIGGER_MANAGEMENT_MIN_COLONIES, management.getRequired());
    }

    @Test
    void hidesCompletedUnlessRequested() {
        colony.unlockUpgrade(GameUnlocks.ABILITY_DYNASTY);
        dynasty.addColony(new Colony(2, "Secundus", false));
        dynasty.addColony(new Colony(3, "Tertius", false));

        List<TriggerProgress> hidden = TriggerProgressService.getVisible(colony, null, false);
        assertTrue(find(hidden, GameUnlocks.ABILITY_DYNASTY) == null);
        assertTrue(find(hidden, GameUnlocks.ABILITY_MANAGEMENT) != null);

        List<TriggerProgress> shown = TriggerProgressService.getVisible(colony, null, true);
        assertTrue(find(shown, GameUnlocks.ABILITY_DYNASTY) != null);
    }

    @Test
    void autoTunnelsHiddenUntilAutomationUnlocked() {
        Tunnel tunnel = new Tunnel(new Hex(), new Hex(), 100);
        tunnel.restoreState(100, true);
        dynasty.addTunnel(tunnel);

        List<TriggerProgress> before = TriggerProgressService.getVisible(colony, null);
        assertTrue(find(before, GameUnlocks.ABILITY_AUTO_TUNNELS) == null);

        colony.unlockUpgrade(GameUnlocks.ABILITY_AUTOMATION);
        List<TriggerProgress> after = TriggerProgressService.getVisible(colony, null);
        TriggerProgress tunnels = find(after, GameUnlocks.ABILITY_AUTO_TUNNELS);
        assertTrue(tunnels != null);
        assertEquals(1, tunnels.getCurrent());
        assertEquals(GameConstants.AUTO_UPGRADE_MIN_COMPLETE_TUNNELS, tunnels.getRequired());
    }

    private static TriggerProgress find(List<TriggerProgress> list, Object upgrade) {
        for (TriggerProgress progress : list) {
            if (progress.getUpgrade() == upgrade) {
                return progress;
            }
        }
        return null;
    }
}
