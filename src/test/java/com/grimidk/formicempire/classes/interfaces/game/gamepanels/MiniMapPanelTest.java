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
}
