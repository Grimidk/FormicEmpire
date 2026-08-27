package com.grimidk.formicempire.classes.interfaces.menu;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.grimidk.formicempire.classes.infrasctructure.diagnostics.SessionDiagnostics;
import com.grimidk.formicempire.classes.infrasctructure.diagnostics.SimulationDiagnostics;

class MenuChaoticDiagnosticsTest {

    private static final int FIELD_W = 1920;
    private static final int FIELD_H = 1080;
    private static final int FRAMES = 90;
    private static final float FRAME_DELTA_SEC = MenuChaoticCatalog.ANIMATION_FRAME_MS / 1000f;

    @BeforeEach
    void setUp() {
        SimulationDiagnostics.setEnabled(true);
        SimulationDiagnostics.reset();
    }

    @AfterEach
    void tearDown() {
        SimulationDiagnostics.reset();
        SimulationDiagnostics.setEnabled(false);
    }

    @Test
    @Timeout(120)
    void menuShowcasePaint_isContinuousWork_notIdle() {
        MenuChaoticDefinition heaviest = MenuChaoticCatalog.getScenarios().stream()
                .max((a, b) -> Integer.compare(a.antCount(), b.antCount()))
                .orElseThrow();
        assertTrue(heaviest.antCount() >= 480,
                "Menu showcase scenarios should stress-test with hundreds of ants");

        MenuChaoticPanel panel = new MenuChaoticPanel();
        panel.setSize(FIELD_W, FIELD_H);
        panel.setActive(true);

        BufferedImage buffer = new BufferedImage(FIELD_W, FIELD_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = buffer.createGraphics();
        try {
            for (int frame = 0; frame < FRAMES; frame++) {
                panel.advanceSimulationForDiagnostics(FRAME_DELTA_SEC, FIELD_W, FIELD_H);
                panel.paint(g2d);
            }
        } finally {
            g2d.dispose();
        }

        SimulationDiagnostics.Snapshot snapshot = SimulationDiagnostics.snapshot();
        System.out.println("[MenuChaoticDiagnosticsTest] scenario=" + heaviest.kind()
                + " ants=" + Math.min(heaviest.antCount(), MenuChaoticCatalog.MAX_ANTS)
                + " frames=" + FRAMES + " field=" + FIELD_W + "x" + FIELD_H);
        System.out.println(SimulationDiagnostics.formatReport(snapshot));
        System.out.println(SessionDiagnostics.formatSnapshot(SessionDiagnostics.capture(null)));

        var paintStats = snapshot.scopes()[SimulationDiagnostics.Scope.MENU_CHAOTIC_PAINT.ordinal()];
        var updateStats = snapshot.scopes()[SimulationDiagnostics.Scope.MENU_CHAOTIC_UPDATE.ordinal()];

        assertTrue(paintStats.calls() >= FRAMES);
        assertTrue(updateStats.calls() >= FRAMES);
        assertTrue(paintStats.avgNanos() > 0L);
        assertTrue(snapshot.counter(SimulationDiagnostics.Counter.MENU_CHAOTIC_ANTS_PAINTED) > 0L);

        double paintMsPerFrame = paintStats.avgNanos() / 1_000_000.0;
        System.out.println("[MenuChaoticDiagnosticsTest] avgPaintMsPerFrame=" + String.format("%.2f", paintMsPerFrame));
        assertTrue(paintMsPerFrame < 12.0,
                "Regression guard: menu paint avg should stay under 12ms/frame (baseline ~6ms after bg cache)");
    }
}
