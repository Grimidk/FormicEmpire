package com.grimidk.formicempire.classes.infrasctructure.managers;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
                    com.grimidk.formicempire.classes.constants.unlocks.Upgrade unlockedUpgrade,
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
}
