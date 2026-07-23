package com.grimidk.formicempire.classes.entities.services.shared;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Rectangle;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class ViewportPhysicsLodTest {

    @Test
    void antIntersectsViewport_nullViewport_treatsAsVisible() {
        assertTrue(ViewportPhysicsLod.antIntersectsViewport(null, 0, 0, 32, 32));
    }

    @Test
    void antIntersectsViewport_inside() {
        Rectangle vp = new Rectangle(0, 0, 400, 400);
        assertTrue(ViewportPhysicsLod.antIntersectsViewport(vp, 100, 100, 32, 32));
    }

    @Test
    void antIntersectsViewport_outside() {
        Rectangle vp = new Rectangle(0, 0, 100, 100);
        assertFalse(ViewportPhysicsLod.antIntersectsViewport(vp, 5000, 5000, 32, 32));
    }

    @Test
    void shouldRunOffViewportAi_eventuallyTrue() {
        Colony colony = new Colony(1, "c", true);
        Dynasty d = new Dynasty(1, "d", true, GameConstants.SPECIES_OMNI);
        colony.setDynasty(d);
        Ant ant = new Ant(colony, GameConstants.TYPE_WORKER);
        boolean any = false;
        for (long step = 1; step <= ViewportPhysicsLod.OFF_VIEWPORT_AI_PERIOD * 2; step++) {
            if (ViewportPhysicsLod.shouldRunOffViewportAi(step, ant)) {
                any = true;
                break;
            }
        }
        assertTrue(any);
    }
}
