package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.util.GamePaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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
        engine.setShowFpsCounter(true);
        engine.setMapEditorEnabled(true);
        engine.setAnimateWorldGen(false);
        engine.setAllowNonOmniAssimilation(true);
        assertFalse(engine.isAllowNonOmniAssimilation() && engine.hasAchievementTaintingSandbox());
        assertFalse(engine.hasAchievementTaintingSandbox());

        World world = new World();
        engine.setWorld(world);
        assertTrue(world.allowsAchievements());
    }

    @Test
    void cheatFogToggleControlsRevealMapTaint() {
        Engine engine = engineWithoutSandboxCheats();
        engine.setCheatFogOfWarToggle(false);
        engine.setCheatRevealMap(true);
        assertFalse(engine.isCheatRevealMap());
        assertFalse(engine.hasAchievementTaintingSandbox());

        engine.setCheatFogOfWarToggle(true);
        assertFalse(engine.isCheatRevealMap());
        assertFalse(engine.hasAchievementTaintingSandbox());

        engine.setCheatRevealMap(true);
        assertTrue(engine.isCheatRevealMap());
        assertTrue(engine.hasAchievementTaintingSandbox());

        engine.setCheatFogOfWarToggle(false);
        assertFalse(engine.isCheatRevealMap());
        assertFalse(engine.hasAchievementTaintingSandbox());
    }

    @Test
    void loadSettingsClearsPersistedCheatRevealMap(@TempDir Path tempDir) throws Exception {
        String previous = System.getProperty(GamePaths.SAVES_DIR_PROPERTY);
        System.setProperty(GamePaths.SAVES_DIR_PROPERTY, tempDir.toString());
        try {
            Path settingsFile = tempDir.resolve("settings.json");
            Files.writeString(settingsFile,
                    "{\n  \"cheatFogOfWarToggle\": false,\n  \"cheatRevealMap\": true\n}\n",
                    StandardCharsets.UTF_8);

            Engine engine = engineWithoutSandboxCheats();
            new SaveManager().loadSettings(engine);

            assertFalse(engine.isCheatRevealMap());
            assertFalse(engine.hasAchievementTaintingSandbox());
        } finally {
            if (previous == null) {
                System.clearProperty(GamePaths.SAVES_DIR_PROPERTY);
            } else {
                System.setProperty(GamePaths.SAVES_DIR_PROPERTY, previous);
            }
        }
    }

    @Test
    void allowNonOmniAssimilationDefaultsFalseAndPersistsInSettings(@TempDir Path tempDir) throws Exception {
        String previous = System.getProperty(GamePaths.SAVES_DIR_PROPERTY);
        System.setProperty(GamePaths.SAVES_DIR_PROPERTY, tempDir.toString());
        try {
            Engine engine = engineWithoutSandboxCheats();
            assertFalse(engine.isAllowNonOmniAssimilation());

            engine.setAllowNonOmniAssimilation(true);
            SaveManager manager = new SaveManager();
            manager.saveSettings(engine);

            Engine reloaded = engineWithoutSandboxCheats();
            manager.loadSettings(reloaded);
            assertTrue(reloaded.isAllowNonOmniAssimilation());
            assertFalse(reloaded.hasAchievementTaintingSandbox());
        } finally {
            if (previous == null) {
                System.clearProperty(GamePaths.SAVES_DIR_PROPERTY);
            } else {
                System.setProperty(GamePaths.SAVES_DIR_PROPERTY, previous);
            }
        }
    }
}
