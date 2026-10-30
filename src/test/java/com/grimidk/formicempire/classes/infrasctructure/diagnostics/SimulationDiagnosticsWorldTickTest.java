package com.grimidk.formicempire.classes.infrasctructure.diagnostics;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

class SimulationDiagnosticsWorldTickTest {

    private Engine engine;

    @BeforeEach
    void setUp() {
        SimulationDiagnostics.setEnabled(true);
        SimulationDiagnostics.reset();
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
        SimulationDiagnostics.reset();
        SimulationDiagnostics.setEnabled(false);
    }

    @Test
    @Timeout(180)
    void benchmarkWorldTicks_reportsMinuteHourDayBreakdown() {
        World world = SimulationDiagnosticsSupport.buildBenchmarkWorld(engine);
        assertTrue(SimulationDiagnosticsSupport.colonizedHexCount(world) >= 4);

        int hours = 24 * 14;
        for (int h = 0; h < hours; h++) {
            world.runHour();
        }

        SimulationDiagnostics.Snapshot snapshot = SimulationDiagnostics.snapshot();
        System.out.println("[SimulationDiagnosticsWorldTickTest] colonizedHexes="
                + SimulationDiagnosticsSupport.colonizedHexCount(world)
                + " dynasties=" + world.getDynastys().size());
        System.out.println(SimulationDiagnostics.formatReport(snapshot));
        System.out.println(SessionDiagnostics.formatSnapshot(SessionDiagnostics.capture(engine)));

        assertTrue(snapshot.counter(SimulationDiagnostics.Counter.COLONIES_HOUR) > 0L);
        assertTrue(snapshot.scopes()[SimulationDiagnostics.Scope.WORLD_HOUR.ordinal()].calls() >= hours);
        assertTrue(snapshot.scopes()[SimulationDiagnostics.Scope.COLONY_HOURLY_TOTAL.ordinal()].totalNanos() > 0L);
    }
}
