package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.Test;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapDialogNavigationTest {

    @Test
    void navigationKeysAreRegisteredAndTriggerPanAndZoom() throws Exception {
        World world = new World();
        Colony colony = new Colony(1, "Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                colony,
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        SwingUtilities.invokeAndWait(() -> {
            MapDialog dialog = new MapDialog(null, null, world, null, null);
            try {
                InputMap inputMap = dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
                ActionMap actionMap = dialog.getRootPane().getActionMap();

                assertNotNull(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0)));
                assertNotNull(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0)));
                assertNotNull(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0)));
                assertNotNull(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0)));

                Action panLeft = actionMap.get(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0)));
                Action panRight = actionMap.get(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0)));
                Action zoomIn = actionMap.get(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0)));
                Action zoomOut = actionMap.get(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0)));

                assertNotNull(panLeft);
                assertNotNull(panRight);
                assertNotNull(zoomIn);
                assertNotNull(zoomOut);

                panLeft.actionPerformed(new ActionEvent(dialog, ActionEvent.ACTION_PERFORMED, "panLeft"));
                panRight.actionPerformed(new ActionEvent(dialog, ActionEvent.ACTION_PERFORMED, "panRight"));
                zoomIn.actionPerformed(new ActionEvent(dialog, ActionEvent.ACTION_PERFORMED, "zoomIn"));
                zoomOut.actionPerformed(new ActionEvent(dialog, ActionEvent.ACTION_PERFORMED, "zoomOut"));
            } finally {
                dialog.dispose();
            }
        });
    }

    @Test
    void mapPanelPaintsSmoothlyWithBakedCache() throws Exception {
        World world = new World();
        Colony colony = new Colony(1, "Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                colony,
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        SwingUtilities.invokeAndWait(() -> {
            MapDialog dialog = new MapDialog(null, null, world, null, null);
            try {
                dialog.setSize(1200, 800);
                dialog.doLayout();
                java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(1200, 800, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D g2d = img.createGraphics();
                dialog.paint(g2d);
                long t0 = System.currentTimeMillis();
                dialog.paint(g2d);
                long elapsed = System.currentTimeMillis() - t0;
                g2d.dispose();
                assertTrue(elapsed < 20, "Cached paint should complete in under 20ms, was " + elapsed);
            } finally {
                dialog.dispose();
            }
        });
    }
}
