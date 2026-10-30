package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.interfaces.game.gamepanels.MiniMapPanel;
import org.junit.jupiter.api.Test;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorldGenStepListenerTest {

    @Test
    public void testWorldGenStepListenerNotifiesAllStepsInOrder() {
        World world = new World();
        List<Integer> steps = new ArrayList<>();
        List<String> stepNames = new ArrayList<>();

        world.setWorldGenStepListener((stepNumber, stepName, w) -> {
            steps.add(stepNumber);
            stepNames.add(stepName);
        });

        world.generateRectangularWorld(null, null, "TestWorld", null, 12, 6);

        assertEquals(16, steps.size());
        for (int i = 0; i < 16; i++) {
            assertEquals(i + 1, steps.get(i));
            assertNotNull(stepNames.get(i));
            assertFalse(stepNames.get(i).isEmpty());
        }
        assertEquals("Initialize World", stepNames.get(0));
        assertEquals("Finalize World", stepNames.get(15));
    }

    @Test
    public void testStandaloneWorldGenerationWithoutGameSession() {
        World world = new World();
        world.generateRectangularWorld(null, null, "GalleryWorld", null, 14, 7);

        assertNotNull(world.getHexes());
        assertFalse(world.getHexes().isEmpty());
        assertTrue(world.getWorldWidth() > 0);
        assertTrue(world.getWorldHeight() > 0);

        MiniMapPanel miniMap = new MiniMapPanel(world);
        miniMap.setSize(new Dimension(280, 180));

        BufferedImage img = new BufferedImage(280, 180, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            miniMap.paint(g2);
        } finally {
            g2.dispose();
        }
    }

    @Test
    public void testEngineAnimateWorldGenSetting() {
        Engine engine = new Engine();
        assertFalse(engine.isAnimateWorldGen());

        engine.setAnimateWorldGen(true);
        assertTrue(engine.isAnimateWorldGen());

        List<Integer> steps = new ArrayList<>();
        engine.setWorldGenStepListener((stepNumber, stepName, w) -> steps.add(stepNumber));

        World world = new World();
        engine.setWorld(world);
        assertEquals(engine.getWorldGenStepListener(), world.getWorldGenStepListener());
    }
}
