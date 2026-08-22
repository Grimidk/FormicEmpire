package com.grimidk.formicempire.classes.interfaces.game.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Trade;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class HexMapOverlaysTest {

    @Test
    void tradeLegProgressMovesFromOriginTowardDestination() {
        Hex origin = new Hex();
        origin.setQ(0);
        origin.setR(0);
        Hex destination = new Hex();
        destination.setQ(1);
        destination.setR(0);
        Trade trade = new Trade(
                origin,
                destination,
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                false,
                false,
                GameConstants.METHOD_LAND);
        trade.restoreTripState(10, 10, false);
        assertEquals(0.0, HexMapOverlays.tradeLegProgress(trade), 1e-9);
        trade.restoreTripState(10, 5, false);
        assertEquals(0.5, HexMapOverlays.tradeLegProgress(trade), 1e-9);
        trade.restoreTripState(10, 0, false);
        assertEquals(1.0, HexMapOverlays.tradeLegProgress(trade), 1e-9);
    }

    @Test
    void tradeLegProgressAdvancesWithinCurrentHour() {
        Hex origin = new Hex();
        origin.setQ(0);
        origin.setR(0);
        Hex destination = new Hex();
        destination.setQ(1);
        destination.setR(0);
        Trade trade = new Trade(
                origin,
                destination,
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                false,
                false,
                GameConstants.METHOD_LAND);
        trade.restoreTripState(10, 5, false);
        assertEquals(0.5, HexMapOverlays.tradeLegProgress(trade, 0), 1e-9);
        assertEquals(0.55, HexMapOverlays.tradeLegProgress(trade, 30), 1e-9);
        assertEquals(0.5 + (59.0 / 60.0) / 10.0, HexMapOverlays.tradeLegProgress(trade, 59), 1e-9);
    }

    @Test
    void areNeighborsMatchesFlatTopAdjacency() {
        Hex a = new Hex();
        a.setQ(0);
        a.setR(0);
        Hex neighbor = new Hex();
        neighbor.setQ(1);
        neighbor.setR(0);
        Hex far = new Hex();
        far.setQ(2);
        far.setR(0);
        assertTrue(HexMapOverlays.areNeighbors(a, neighbor));
        assertFalse(HexMapOverlays.areNeighbors(a, far));
    }
}
