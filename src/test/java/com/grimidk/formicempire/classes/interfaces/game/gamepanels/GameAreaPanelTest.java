package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;

import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class GameAreaPanelTest {

    @Test
    void gameAreaPanelRendersUnderworldAndOverworld() throws Exception {
        Engine engine = new Engine();
        World world = new World();
        Colony colony = new Colony(1, "Capital", true);
        world.startWorld(GameConstants.BIOME_PLAINS, colony, "Player");
        engine.setWorld(world);

        SwingUtilities.invokeAndWait(() -> {
            assertDoesNotThrow(() -> {
                GameAreaPanel panel = new GameAreaPanel();
                panel.setEngine(engine);
                panel.setColony(colony);
                panel.setSize(800, 600);
                panel.doLayout();

                BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = img.createGraphics();
                try {
                    panel.paint(g2d);
                    panel.toggleDimension();
                    panel.paint(g2d);
                    panel.toggleDimension();
                    panel.paint(g2d);
                    panel.resetView();
                    panel.paint(g2d);
                } finally {
                    g2d.dispose();
                }
            });
        });
    }
}
