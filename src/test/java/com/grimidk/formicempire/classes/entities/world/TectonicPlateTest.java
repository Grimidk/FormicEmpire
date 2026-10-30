package com.grimidk.formicempire.classes.entities.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TectonicPlateTest {

    @Test
    void plateInitializationAndGetters() {
        TectonicPlate plate = new TectonicPlate(1, 10, -5, Math.PI / 4, 1.5, true);

        assertEquals(1, plate.getId());
        assertEquals(10, plate.getCenterQ());
        assertEquals(-5, plate.getCenterR());
        assertEquals(Math.PI / 4, plate.getDriftAngle(), 1e-6);
        assertEquals(1.5, plate.getDriftSpeed(), 1e-6);
        assertTrue(plate.isOceanic());

        double expectedVx = Math.cos(Math.PI / 4) * 1.5;
        double expectedVy = Math.sin(Math.PI / 4) * 1.5;
        assertEquals(expectedVx, plate.getDriftVectorX(), 1e-6);
        assertEquals(expectedVy, plate.getDriftVectorY(), 1e-6);
    }

    @Test
    void plateHexMembership() {
        TectonicPlate plate = new TectonicPlate(2, 0, 0, 0.0, 1.0, false);
        assertFalse(plate.isOceanic());
        assertEquals(0, plate.getHexes().size());

        Hex h1 = new Hex();
        h1.setQ(0);
        h1.setR(0);
        Hex h2 = new Hex();
        h2.setQ(1);
        h2.setR(0);

        plate.addHex(h1);
        plate.addHex(h2);
        plate.addHex(h1);

        assertEquals(2, plate.getHexes().size());
        assertTrue(plate.getHexes().contains(h1));
        assertTrue(plate.getHexes().contains(h2));
        assertEquals(2, h1.getPlateId());
        assertEquals(2, h2.getPlateId());
    }
}
