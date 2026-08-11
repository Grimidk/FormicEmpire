package com.grimidk.formicempire.classes.infrasctructure.managers;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TriggerManagerTest {

    @Test
    void registerListenersDoesNotClearListeners() throws Exception {
        Engine engine = new Engine();
        World world = new World();
        Colony colony = new Colony(1, "Test Prime", true);
        TriggerManager manager = new TriggerManager(world, colony, engine);
        manager.addListener(new TriggerManager.TriggerListener() {
            @Override
            public void onUpgradeTriggered(
                    Upgrade unlockedUpgrade,
                    String title,
                    String message) {
            }

            @Override
            public void onColonyDeath() {
            }
        });

        manager.registerListeners();

        Field listenersField = TriggerManager.class.getDeclaredField("listeners");
        listenersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<TriggerManager.TriggerListener> listeners =
                (List<TriggerManager.TriggerListener>) listenersField.get(manager);
        assertEquals(1, listeners.size());
    }

    @Test
    void parasiticMiteOutbreakUnlocksFromSisterColonyWithoutBiomeGate() throws Exception {
        Engine engine = new Engine();
        World world = new World();
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);

        Colony watched = new Colony(1, "Capital", true);
        Colony satellite = new Colony(2, "Outpost", true);
        dynasty.addColony(watched);
        dynasty.addColony(satellite);
        watched.setDynasty(dynasty);
        satellite.setDynasty(dynasty);
        satellite.setParasiticMites(2_500);

        TriggerManager manager = new TriggerManager(world, watched, engine);
        AtomicReference<Upgrade> unlocked = new AtomicReference<>();
        manager.addListener(new TriggerManager.TriggerListener() {
            @Override
            public void onUpgradeTriggered(Upgrade upgrade, String title, String message) {
                unlocked.set(upgrade);
            }

            @Override
            public void onColonyDeath() {
            }
        });

        Method check = TriggerManager.class.getDeclaredMethod("checkParasiticMiteOutbreak");
        check.setAccessible(true);
        check.invoke(manager);
        SwingUtilities.invokeAndWait(() -> { });

        assertTrue(watched.hasUpgrade(GameUnlocks.ABILITY_PARASITIC_MITE_ALERT));
        assertTrue(watched.hasUpgrade(GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE));
        assertEquals(GameUnlocks.ABILITY_PARASITIC_MITE_ALERT, unlocked.get());
    }

    @Test
    void parasiticMiteOutbreakUnlocksNpcSilentlyAndGrantsCatcher() throws Exception {
        Engine engine = new Engine();
        World world = new World();
        Dynasty dynasty = new Dynasty(2, "NPC", false, GameConstants.SPECIES_OMNI);

        Colony npc = new Colony(3, "NPC Nest", false);
        dynasty.addColony(npc);
        npc.setDynasty(dynasty);
        npc.unlockUpgrade(GameUnlocks.ROLE_HUNTER);
        npc.setParasiticMites(1_000);

        TriggerManager manager = new TriggerManager(world, new Colony(1, "Player", true), engine);
        AtomicReference<Upgrade> unlocked = new AtomicReference<>();
        manager.addListener(new TriggerManager.TriggerListener() {
            @Override
            public void onUpgradeTriggered(Upgrade upgrade, String title, String message) {
                unlocked.set(upgrade);
            }

            @Override
            public void onColonyDeath() {
            }
        });

        Method check = TriggerManager.class.getDeclaredMethod("applyParasiticMiteUnlockIfEligible",
                Colony.class, boolean.class);
        check.setAccessible(true);
        check.invoke(manager, npc, false);
        SwingUtilities.invokeAndWait(() -> { });

        assertTrue(npc.hasUpgrade(GameUnlocks.ABILITY_PARASITIC_MITE_ALERT));
        assertTrue(npc.hasUpgrade(GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE));
        assertTrue(npc.hasUpgrade(GameUnlocks.ROLE_CATCHER));
        assertEquals(null, unlocked.get());
    }
}
