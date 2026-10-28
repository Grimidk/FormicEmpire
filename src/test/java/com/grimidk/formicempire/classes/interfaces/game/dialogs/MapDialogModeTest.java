package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.world.FaultLine;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapDialogModeTest {

    @Test
    void toggleMapModesAndPaint() throws Exception {
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
                assertEquals(MapDialog.MapMode.BIOME, dialog.getMapMode());

                dialog.setSize(800, 600);
                dialog.doLayout();
                BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                dialog.paint(g);

                dialog.setMapMode(MapDialog.MapMode.HEIGHT);
                assertEquals(MapDialog.MapMode.HEIGHT, dialog.getMapMode());
                dialog.paint(g);

                dialog.setMapMode(MapDialog.MapMode.TECTONIC_PLATES);
                assertEquals(MapDialog.MapMode.TECTONIC_PLATES, dialog.getMapMode());
                dialog.paint(g);

                dialog.setMapMode(MapDialog.MapMode.BIOME);
                assertEquals(MapDialog.MapMode.BIOME, dialog.getMapMode());
                dialog.paint(g);

                g.dispose();
            } finally {
                dialog.dispose();
            }
        });
    }

    @Test
    void tectonicPlatesModeRendersFaultBorders() throws Exception {
        assertEquals(new Color(175, 65, 230), MapDialog.FAULT_CONVERGENT_COLOR);
        assertEquals(new Color(245, 215, 30), MapDialog.FAULT_DIVERGENT_COLOR);
        assertEquals(new Color(50, 150, 255), MapDialog.FAULT_TRANSFORM_COLOR);

        World world = new World();
        world.setWorldWidth(10);
        world.setWorldHeight(10);

        Map<String, Hex> hexMap = new HashMap<>();
        for (int r = -5; r < 5; r++) {
            for (int q = -5; q < 5; q++) {
                Hex h = new Hex();
                h.setQ(q);
                h.setR(r);
                h.setBiome(GameConstants.BIOME_PLAINS);
                h.setElevation(3);
                h.setPlateId(q < 0 ? 1 : 2);
                hexMap.put(q + "," + r, h);
            }
        }
        world.linkNeighbors(hexMap);
        world.setHexes(new ArrayList<>(hexMap.values()));

        FaultLine fault = new FaultLine(1, 2, FaultLine.FaultType.CONVERGENT);
        fault.addBoundaryPair(hexMap.get("-1,0"), hexMap.get("0,0"));
        world.addFaultLine(fault);

        assertEquals(FaultLine.FaultType.CONVERGENT, world.getFaultTypeBetweenPlates(1, 2));

        FaultLine divFault = new FaultLine(2, 3, FaultLine.FaultType.DIVERGENT, 2);
        world.addFaultLine(divFault);
        assertEquals(FaultLine.FaultType.DIVERGENT, world.getFaultTypeBetweenPlates(2, 3));
        assertEquals(2, world.getFaultIntensityBetweenPlates(2, 3));

        FaultLine transFault = new FaultLine(3, 4, FaultLine.FaultType.TRANSFORM, 1);
        world.addFaultLine(transFault);
        assertEquals(FaultLine.FaultType.TRANSFORM, world.getFaultTypeBetweenPlates(3, 4));
        assertEquals(1, world.getFaultIntensityBetweenPlates(3, 4));

        SwingUtilities.invokeAndWait(() -> {
            MapDialog dialog = new MapDialog(null, null, world, null, null);
            try {
                dialog.setSize(800, 600);
                dialog.doLayout();
                dialog.setMapMode(MapDialog.MapMode.TECTONIC_PLATES);
                assertEquals(MapDialog.MapMode.TECTONIC_PLATES, dialog.getMapMode());
                BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                dialog.paint(g);
                g.dispose();
            } finally {
                dialog.dispose();
            }
        });
    }
}
