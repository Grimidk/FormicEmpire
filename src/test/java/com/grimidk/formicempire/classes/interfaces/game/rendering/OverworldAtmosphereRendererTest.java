package com.grimidk.formicempire.classes.interfaces.game.rendering;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

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
}

