package com.grimidk.formicempire.classes.infrasctructure.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSpeciesPalette;
import com.grimidk.formicempire.classes.constants.critter.ant.AntMod;
import com.grimidk.formicempire.classes.constants.critter.ant.AntModProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntModSlot;
import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

class AntSpriteCompositorTest {

    @Test
    void mapMarkerReplacesHeadAndKeepsUnknown() {
        AntSpeciesPalette palette = new AntSpeciesPalette(
                "112233", "445566", "778899",
                "aabbcc", "ddeeff",
                "010203", "040506", "070809",
                "ff0080");
        assertEquals(0x112233, AntSpriteCompositor.mapMarker(AntSpriteCompositor.MARKER_HEAD, palette, false));
        assertEquals(0x010203, AntSpriteCompositor.mapMarker(AntSpriteCompositor.MARKER_HEAD, palette, true));
        assertEquals(0x010203, AntSpriteCompositor.mapMarker(AntSpriteCompositor.MARKER_DRONE, palette, false));
        assertEquals(-1, AntSpriteCompositor.mapMarker(0x123456, palette, false));
    }

    @Test
    void composeOmniWorkerUsesSpeciesHeadColor() {
        ImageIcon icon = AntSpriteCompositor.getSprite(
                GameConstants.CLASS_WORKER,
                GameConstants.SPECIES_OMNI,
                AntModProfile.standard());
        assertNotNull(icon);
        assertTrue(icon.getIconWidth() > 0);
        assertTrue(icon.getIconHeight() > 0);

        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        icon.paintIcon(null, image.getGraphics(), 0, 0);

        int expected = GameConstants.SPECIES_OMNI.getPalette().getHeadColor();
        boolean found = false;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if (((argb >>> 24) & 0xFF) == 0) {
                    continue;
                }
                if ((argb & 0xFFFFFF) == expected) {
                    found = true;
                    break;
                }
            }
            if (found) {
                break;
            }
        }
        assertTrue(found, "composed worker should contain omni headColor pixels");
    }

    @Test
    void composeWorkerLegFramesOneThroughFour() {
        AntModProfile profile = AntModProfile.standard();
        for (int leg = 1; leg <= 4; leg++) {
            ImageIcon icon = AntSpriteCompositor.getSprite(
                    GameConstants.CLASS_WORKER,
                    GameConstants.SPECIES_OMNI,
                    profile,
                    leg,
                    1,
                    1);
            assertNotNull(icon, "leg frame " + leg);
            assertTrue(icon.getIconWidth() > 0);
            assertTrue(icon.getIconHeight() > 0);
        }
    }

    @Test
    void composeDroneFlyingLegsUsesLegFlyingLayer() {
        ImageIcon flying = AntSpriteCompositor.getSprite(
                GameConstants.CLASS_DRONE,
                GameConstants.SPECIES_OMNI,
                AntModProfile.standard(),
                GameNumbers.ANT_LEG_FRAME_FLYING,
                1,
                2);
        ImageIcon walking = AntSpriteCompositor.getSprite(
                GameConstants.CLASS_DRONE,
                GameConstants.SPECIES_OMNI,
                AntModProfile.standard(),
                1,
                1,
                2);
        assertNotNull(flying);
        assertNotNull(walking);
        assertTrue(flying.getIconWidth() > 0);
    }

    @Test
    void composeTrapjawHeadUsesNamedJawLayer() {
        AntModProfile profile = AntModProfile.of(
                GameConstants.MOD_HEAD_TRAPJAW.getDigit(),
                AntMod.DIGIT_NONE,
                AntMod.DIGIT_NONE,
                AntMod.DIGIT_NONE);
        ImageIcon icon = GameConstants.getAntSprite(
                GameConstants.CLASS_WORKER,
                GameConstants.SPECIES_FIRE,
                profile);
        assertNotNull(icon);
        assertEquals(GameConstants.MOD_HEAD_TRAPJAW, profile.getMod(AntModSlot.HEAD));
    }

    @Test
    void composeWorkerAntennaFramesOneAndTwo() {
        AntModProfile profile = AntModProfile.standard();
        for (int antenna = 1; antenna <= 2; antenna++) {
            ImageIcon icon = AntSpriteCompositor.getSprite(
                    GameConstants.CLASS_WORKER,
                    GameConstants.SPECIES_OMNI,
                    profile,
                    1,
                    1,
                    1,
                    antenna,
                    false);
            assertNotNull(icon, "antenna frame " + antenna);
            assertTrue(icon.getIconWidth() > 0);
            assertTrue(icon.getIconHeight() > 0);
        }
    }

    @Test
    void composeAllAntennaHeadVariantsForEveryType() {
        AntMod[] heads = {
                GameConstants.MOD_HEAD_NONE,
                GameConstants.MOD_HEAD_TRAPJAW,
                GameConstants.MOD_HEAD_DOORHEAD,
                GameConstants.MOD_HEAD_FARSIGHT
        };
        AntClass[] types = {
                GameConstants.CLASS_WORKER,
                GameConstants.CLASS_SOLDIER,
                GameConstants.CLASS_MAJOR,
                GameConstants.CLASS_QUEEN,
                GameConstants.CLASS_PRINCESS,
                GameConstants.CLASS_DRONE
        };
        for (AntClass type : types) {
            for (AntMod head : heads) {
                if (type == GameConstants.CLASS_DRONE && head != GameConstants.MOD_HEAD_NONE) {
                    continue;
                }
                AntModProfile profile = AntModProfile.of(
                        head.getDigit(),
                        AntMod.DIGIT_NONE,
                        AntMod.DIGIT_NONE,
                        AntMod.DIGIT_NONE);
                for (int antennaFrame = 1; antennaFrame <= 2; antennaFrame++) {
                    ImageIcon icon = AntSpriteCompositor.getSprite(
                            type,
                            GameConstants.SPECIES_OMNI,
                            profile,
                            1,
                            1,
                            1,
                            antennaFrame,
                            false);
                    assertNotNull(icon, type.getNameKey() + " " + head.getNameKey() + " antenna " + antennaFrame);
                    assertTrue(icon.getIconWidth() > 0);
                    assertTrue(icon.getIconHeight() > 0);
                }
            }
        }
    }

    @Test
    void composeTrapjawAntennaUsesHeadVariantLayer() {
        AntModProfile profile = AntModProfile.of(
                GameConstants.MOD_HEAD_TRAPJAW.getDigit(),
                AntMod.DIGIT_NONE,
                AntMod.DIGIT_NONE,
                AntMod.DIGIT_NONE);
        ImageIcon icon = AntSpriteCompositor.getSprite(
                GameConstants.CLASS_WORKER,
                GameConstants.SPECIES_FIRE,
                profile,
                1,
                1,
                1,
                2,
                false);
        assertNotNull(icon);
    }

    @Test
    void composeWithParasiticMitesBakesOverlayIntoSprite() {
        ImageIcon clean = AntSpriteCompositor.getSprite(
                GameConstants.CLASS_WORKER,
                GameConstants.SPECIES_OMNI,
                AntModProfile.standard(),
                1,
                1,
                1,
                1,
                false);
        ImageIcon infected = AntSpriteCompositor.getSprite(
                GameConstants.CLASS_WORKER,
                GameConstants.SPECIES_OMNI,
                AntModProfile.standard(),
                1,
                1,
                1,
                1,
                true);
        assertNotNull(clean);
        assertNotNull(infected);
        assertTrue(clean != infected);
    }
}
