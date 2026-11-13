package com.grimidk.formicempire.classes.interfaces.game.rendering;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grimidk.formicempire.classes.constants.world.DisasterType;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import org.junit.jupiter.api.Test;

class OverworldAtmosphereRendererTest {

    @Test
    void renderExecutesSafelyWhenPausedAndUnpaused() {
        BufferedImage img = new BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        World world = new World();
        Hex hex = new Hex();
        hex.setLocalWeather(GameConstants.WEATHER_RAIN);

        assertDoesNotThrow(() -> OverworldAtmosphereRenderer.render(g2d, 300, 200, world, hex, true, true, false));
        assertDoesNotThrow(() -> OverworldAtmosphereRenderer.render(g2d, 300, 200, world, hex, true, true, true));
        assertDoesNotThrow(() -> OverworldAtmosphereRenderer.render(null, 0, 0, null, null, false, false, true));

        g2d.dispose();
    }

    @Test
    void allDisasterIconsAndEnvironmentPlaceholdersLoadWithExpectedDimensions() {
        int totalSprites = 0;
        for (DisasterType disaster : GameConstants.getDisasterTypes()) {
            ImageIcon icon = disaster.getIcon();
            assertNotNull(icon);
            assertNotEquals(GameConstants.ICON_UNKNOWN, icon);
            assertEquals(16, icon.getIconWidth());
            assertEquals(16, icon.getIconHeight());

            assertFalse(disaster.getSpritePaths().isEmpty());
            for (String path : disaster.getSpritePaths()) {
                Image sprite = OverworldAtmosphereRenderer.loadDisasterSprite(path);
                assertNotNull(sprite, "Missing sprite: " + path);
                if (disaster == GameConstants.DISASTER_EARTHQUAKE) {
                    assertEquals(128, sprite.getWidth(null));
                    assertEquals(64, sprite.getHeight(null));
                } else if (disaster == GameConstants.DISASTER_WILDFIRE) {
                    assertEquals(64, sprite.getWidth(null));
                    assertEquals(64, sprite.getHeight(null));
                } else {
                    assertEquals(128, sprite.getWidth(null));
                    assertEquals(128, sprite.getHeight(null));
                }
                totalSprites++;
            }
        }
        assertEquals(22, totalSprites);
    }

    @Test
    void hasVisiblePixelsDetectsBlankVsDrawnImages() {
        BufferedImage blank = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        assertFalse(OverworldAtmosphereRenderer.hasVisiblePixels(blank));

        Image placeholder = OverworldAtmosphereRenderer.loadDisasterSprite("/sprites/environment/DisasterWildfire.png");
        assertNotNull(placeholder);
        assertTrue(OverworldAtmosphereRenderer.hasVisiblePixels(placeholder));

        BufferedImage drawn = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = drawn.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(4, 4, 2, 2);
        g.dispose();
        assertTrue(OverworldAtmosphereRenderer.hasVisiblePixels(drawn));
        assertFalse(OverworldAtmosphereRenderer.hasVisiblePixels(null));
    }

    @Test
    void renderExecutesSafelyForEveryDisasterType() {
        BufferedImage canvas = new BufferedImage(900, 700, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = canvas.createGraphics();
        World world = new World();
        Hex hex = new Hex();

        for (DisasterType disaster : GameConstants.getDisasterTypes()) {
            hex.recordDisaster(disaster);
            assertDoesNotThrow(() -> OverworldAtmosphereRenderer.render(
                    g2d, 900, 700, 175, 100, world, hex, false, false, false));
            assertDoesNotThrow(() -> OverworldAtmosphereRenderer.render(
                    g2d, 900, 700, 175, 100, world, hex, true, true, true));
        }

        g2d.dispose();
    }
}

