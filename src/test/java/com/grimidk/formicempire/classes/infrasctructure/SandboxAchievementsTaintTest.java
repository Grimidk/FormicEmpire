package com.grimidk.formicempire.classes.infrasctructure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SandboxAchievementsTaintTest {

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

    @Test
    void taintingFlagsDisableAchievementsOnWorldAndSavefile() {
        Engine engine = engineWithoutSandboxCheats();
        World world = new World();
        engine.setWorld(world);
        assertTrue(world.allowsAchievements());

        engine.setFreeAbilities(true);
        engine.applySandboxTaintToActiveWorld();
        assertFalse(world.allowsAchievements());

        world.setAllowsAchievements(true);
        assertFalse(world.allowsAchievements());

        Savefile save = new Savefile(1, "Test");
        assertTrue(save.allowsAchievements());
        save.disableAchievements();
        save.setAllowsAchievements(true);
        assertFalse(save.allowsAchievements());
    }

    @Test
    void nonTaintingSandboxDoesNotTaint() {
        Engine engine = engineWithoutSandboxCheats();
        engine.setAllowTurboMode(true);
        engine.setShowAuditMenu(true);
        assertFalse(engine.hasAchievementTaintingSandbox());

        World world = new World();
        engine.setWorld(world);
        assertTrue(world.allowsAchievements());
    }
}
