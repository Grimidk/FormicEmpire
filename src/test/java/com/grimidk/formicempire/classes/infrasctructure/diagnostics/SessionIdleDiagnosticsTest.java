package com.grimidk.formicempire.classes.infrasctructure.diagnostics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.assets.AntSpriteCompositor;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

class SessionIdleDiagnosticsTest {

    private Engine engine;

    @BeforeEach
    void setUp() {
        engine = new Engine();
        engine.setAutosaveFrequency(0);
    }

    @AfterEach
    void tearDown() {
        GameRandom.clearTestDoubles();
        if (engine != null) {
            engine.setWorld(null);
            engine = null;
        }
    }

    @Test
    @Timeout(120)
    void afterSimTeardown_sessionIsIdleAndReportsListenerCounts() {
        World world = SimulationDiagnosticsSupport.buildBenchmarkWorld(engine);
        for (int h = 0; h < 24 * 30; h++) {
            world.runHour();
        }

        int antCacheAfterSim = AntSpriteCompositor.compositeCacheSize();
        SessionDiagnostics.Snapshot duringSim = SessionDiagnostics.capture(engine);
        assertTrue(duringSim.worldAttached());
        assertTrue(duringSim.colonizedHexes() >= 4);
        assertTrue(duringSim.hourTickListeners() >= 1, "TradeManager stays registered on hour ticks");

        engine.pauseEngine();
        engine.setWorld(null);

        SessionDiagnostics.Snapshot idle = SessionDiagnostics.capture(engine);
        System.out.println("[SessionIdleDiagnosticsTest] duringSim=" + SessionDiagnostics.formatSnapshot(duringSim));
        System.out.println("[SessionIdleDiagnosticsTest] idle=" + SessionDiagnostics.formatSnapshot(idle));
        System.out.println("[SessionIdleDiagnosticsTest] antCompositeCacheEntriesAfterSim=" + antCacheAfterSim);

        assertTrue(idle.idleSessionReady());
        assertFalse(idle.worldAttached());
        assertTrue(idle.enginePaused());
        assertTrue(idle.colonizedHexes() == 0);
        assertTrue(antCacheAfterSim > 0, "Gameplay/menu compositing should warm sprite caches");
    }
}
