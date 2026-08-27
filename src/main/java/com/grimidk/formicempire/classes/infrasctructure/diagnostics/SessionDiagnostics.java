package com.grimidk.formicempire.classes.infrasctructure.diagnostics;

import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.assets.AntSpriteCompositor;
import com.grimidk.formicempire.classes.infrasctructure.assets.CritterSpriteCompositor;

public final class SessionDiagnostics {

    private SessionDiagnostics() {
    }

    public record Snapshot(
            boolean engineThreadAlive,
            boolean enginePaused,
            boolean worldAttached,
            int colonizedHexes,
            int dynastyCount,
            int minuteTickListeners,
            int hourTickListeners,
            int dayTickListeners,
            int monthTickListeners,
            int antCompositeCacheEntries,
            int antLayerCacheEntries,
            int critterCompositeCacheEntries) {

        public boolean idleSessionReady() {
            return !worldAttached && enginePaused;
        }
    }

    public static Snapshot capture(Engine engine) {
        if (engine == null) {
            return new Snapshot(false, true, false, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
        World world = engine.getWorld();
        int colonized = 0;
        int dynasties = 0;
        if (world != null) {
            dynasties = world.getDynastys() != null ? world.getDynastys().size() : 0;
            if (world.getHexes() != null) {
                for (var hex : world.getHexes()) {
                    if (hex != null && hex.getColony() != null) {
                        colonized++;
                    }
                }
            }
        }
        return new Snapshot(
                engine.isAlive(),
                engine.isPaused(),
                world != null,
                colonized,
                dynasties,
                engine.getMinuteTickListenerCount(),
                engine.getHourTickListenerCount(),
                engine.getDayTickListenerCount(),
                engine.getMonthTickListenerCount(),
                AntSpriteCompositor.compositeCacheSize(),
                AntSpriteCompositor.layerCacheSize(),
                CritterSpriteCompositor.compositeCacheSize());
    }

    public static String formatSnapshot(Snapshot snapshot) {
        if (snapshot == null) {
            return "SessionDiagnostics: (null)";
        }
        return String.format(
                "SessionDiagnostics{engineAlive=%s paused=%s worldAttached=%s colonizedHexes=%d dynasties=%d "
                        + "listeners(min/hour/day/month)=%d/%d/%d/%d spriteCaches(antComposite/antLayer/critter)=%d/%d/%d idleReady=%s}",
                snapshot.engineThreadAlive(),
                snapshot.enginePaused(),
                snapshot.worldAttached(),
                snapshot.colonizedHexes(),
                snapshot.dynastyCount(),
                snapshot.minuteTickListeners(),
                snapshot.hourTickListeners(),
                snapshot.dayTickListeners(),
                snapshot.monthTickListeners(),
                snapshot.antCompositeCacheEntries(),
                snapshot.antLayerCacheEntries(),
                snapshot.critterCompositeCacheEntries(),
                snapshot.idleSessionReady());
    }
}
