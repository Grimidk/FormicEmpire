package com.grimidk.formicempire.classes.infrasctructure.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.ImageIcon;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

class CritterSpriteCompositorTest {

    @Test
    void animatedCrittersComposeDistinctLegFrames() {
        ImageIcon leg1 = CritterSpriteCompositor.getSprite(GameConstants.TYPE_APHID, 1);
        ImageIcon leg2 = CritterSpriteCompositor.getSprite(GameConstants.TYPE_APHID, 2);
        assertNotNull(leg1);
        assertNotNull(leg2);
        assertNotSame(leg1.getImage(), leg2.getImage());
        assertEquals(40, leg1.getIconWidth());
        assertEquals(40, leg1.getIconHeight());
        assertTrue(GameConstants.TYPE_APHID.hasLegWalkCycle());
        assertTrue(GameConstants.TYPE_DERMESTID.hasLegWalkCycle());
        assertTrue(GameConstants.TYPE_SYMBIOTIC_MITE.hasLegWalkCycle());
        assertTrue(GameConstants.TYPE_PARASITE_ANT.hasLegWalkCycle());
    }

    @Test
    void parasiticMiteHasNoLegWalkCycle() {
        assertFalse(GameConstants.TYPE_PARASITIC_MITE.hasLegWalkCycle());
        ImageIcon a = CritterSpriteCompositor.getSprite(GameConstants.TYPE_PARASITIC_MITE, 1);
        ImageIcon b = CritterSpriteCompositor.getSprite(GameConstants.TYPE_PARASITIC_MITE, GameNumbers.ANT_LEG_FRAME_COUNT);
        assertNotNull(a);
        assertNotNull(b);
        assertEquals(a.getImage(), b.getImage());
    }

    @Test
    void parasiticAntUsesParasiticAntAssets() {
        ImageIcon icon = CritterSpriteCompositor.getSprite(GameConstants.TYPE_PARASITE_ANT, 1);
        assertNotNull(icon);
        assertEquals("otherAnts", GameConstants.TYPE_PARASITE_ANT.getSpriteFolder());
        assertEquals("ParasiticAnt.png", GameConstants.TYPE_PARASITE_ANT.getBodySpriteFile());
    }
}
