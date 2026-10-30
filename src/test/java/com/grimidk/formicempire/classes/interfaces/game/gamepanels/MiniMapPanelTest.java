package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MiniMapPanelTest {

    @Test
    void miniMapPaintsFastWithBakedCache() throws Exception {
        Engine engine = new Engine();
        World world = new World();
        Colony colony = new Colony(1, "Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                colony,
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);
        engine.setWorld(world);

        SwingUtilities.invokeAndWait(() -> {
            MiniMapPanel panel = new MiniMapPanel(engine, () -> {});
            panel.setSize(250, 180);
            panel.doLayout();
            BufferedImage img = new BufferedImage(250, 180, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            try {
                panel.paint(g2d);
                long t0 = System.currentTimeMillis();
                for (int i = 0; i < 50; i++) {
                    panel.paint(g2d);
                }
                long elapsed = System.currentTimeMillis() - t0;
                assertTrue(elapsed < 100, "50 cached minimap repaints should complete under 100ms, was " + elapsed);
            } finally {
                g2d.dispose();
            }
        });
    }

    @Test
    void invalidateMapCacheClearsAndRepaints() throws Exception {
        Engine engine = new Engine();
        World world = new World();
        Colony colony = new Colony(1, "Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                colony,
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);
        engine.setWorld(world);

        SwingUtilities.invokeAndWait(() -> {
            MiniMapPanel panel = new MiniMapPanel(engine, () -> {});
            panel.setSize(250, 180);
            panel.doLayout();
            BufferedImage img = new BufferedImage(250, 180, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            try {
                panel.paint(g2d);
                panel.invalidateMapCache();
                panel.paint(g2d);
                panel.refreshTheme();
                panel.paint(g2d);
                panel.reset();
                panel.paint(g2d);
            } finally {
                g2d.dispose();
            }
        });
    }

    @Test
    void multiMapGalleryWorldGenerationAndPaint() throws Exception {
        World world = new World();
        world.generateRectangularWorld(null, null, "World 1", null);
        SwingUtilities.invokeAndWait(() -> {
            MiniMapPanel panel = new MiniMapPanel((World) null);
            panel.setSize(280, 190);
            panel.doLayout();
            panel.setExplicitWorld(world);
            BufferedImage img = new BufferedImage(280, 190, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            try {
                panel.paint(g2d);
            } finally {
                g2d.dispose();
            }
        });
    }

    @Test
    void miniMapBiomeColorsMatchBigMapFadedColors() throws Exception {
        MiniMapPanel panel = new MiniMapPanel((World) null);
        java.lang.reflect.Method m = MiniMapPanel.class.getDeclaredMethod("getBiomeColor", com.grimidk.formicempire.classes.constants.world.Biome.class);
        m.setAccessible(true);
        for (com.grimidk.formicempire.classes.constants.world.Biome biome : GameConstants.getBiomes()) {
            java.awt.Color miniColor = (java.awt.Color) m.invoke(panel, biome);
            java.awt.Color expected = com.grimidk.formicempire.classes.interfaces.ui.AssetStyles.fadeTowardBackground(
                    biome.getMapColor(),
                    com.grimidk.formicempire.classes.interfaces.ui.AssetStyles.mapInactiveHexFade());
            org.junit.jupiter.api.Assertions.assertEquals(expected, miniColor,
                    "Minimap color for biome " + biome.getId() + " must match big map faded color");
        }
    }
}
