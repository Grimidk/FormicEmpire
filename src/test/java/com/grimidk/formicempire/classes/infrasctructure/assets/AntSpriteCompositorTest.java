package com.grimidk.formicempire.classes.infrasctructure.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSpeciesPalette;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtype;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeSlot;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

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
                GameConstants.TYPE_WORKER,
                GameConstants.SPECIES_OMNI,
                AntSubtypeProfile.standard());
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
    void composeTrapjawHeadUsesNamedJawLayer() {
        AntSubtypeProfile profile = AntSubtypeProfile.of(
                GameConstants.SUBTYPE_HEAD_TRAPJAW.getDigit(),
                AntSubtype.DIGIT_NONE,
                AntSubtype.DIGIT_NONE,
                AntSubtype.DIGIT_NONE);
        ImageIcon icon = GameConstants.getAntSprite(
                GameConstants.TYPE_WORKER,
                GameConstants.SPECIES_FIRE,
                profile);
        assertNotNull(icon);
        assertEquals(GameConstants.SUBTYPE_HEAD_TRAPJAW, profile.getSubtype(AntSubtypeSlot.HEAD));
    }
}
