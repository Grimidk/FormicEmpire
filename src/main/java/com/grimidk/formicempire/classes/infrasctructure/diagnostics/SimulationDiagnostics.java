package com.grimidk.formicempire.classes.infrasctructure.diagnostics;

import java.util.Locale;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

public final class SimulationDiagnostics {

    public enum Scope {
        WORLD_MINUTE,
        WORLD_HOUR,
        WORLD_DAY,
        COLONY_MINUTELY_TOTAL,
        COLONY_HOURLY_TOTAL,
        COLONY_DAILY_TOTAL,
        DYNASTY_DAILY_TOTAL,
        MENU_CHAOTIC_TICK,
        MENU_CHAOTIC_PAINT,
        MENU_CHAOTIC_UPDATE,
        ENGINE_MINUTE_NOTIFY,
        PHYSICS_DRAIN,
        GUI_MINUTE_UPDATE
    }

    public enum Counter {
        COLONIES_MINUTE,
        COLONIES_HOUR,
        COLONIES_DAY,
        DYNASTIES_DAY,
        HEXES_SCANNED_MINUTE,
        HEXES_SCANNED_HOUR,
        HEXES_SCANNED_DAY,
        MENU_CHAOTIC_ANTS_PAINTED
    }

    private static volatile boolean enabled = Boolean.getBoolean("formic.diagnostics");

    private static final LongAdder[] TOTAL_NANOS = new LongAdder[Scope.values().length];
    private static final LongAdder[] CALL_COUNTS = new LongAdder[Scope.values().length];
    private static final LongAccumulator[] MAX_NANOS = new LongAccumulator[Scope.values().length];
    private static final LongAdder[] COUNTERS = new LongAdder[Counter.values().length];

    static {
        for (int i = 0; i < TOTAL_NANOS.length; i++) {
            TOTAL_NANOS[i] = new LongAdder();
            CALL_COUNTS[i] = new LongAdder();
            MAX_NANOS[i] = new LongAccumulator(Long::max, 0L);
        }
        for (int i = 0; i < COUNTERS.length; i++) {
            COUNTERS[i] = new LongAdder();
        }
    }

    private SimulationDiagnostics() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static void reset() {
        for (LongAdder adder : TOTAL_NANOS) {
            adder.reset();
        }
        for (LongAdder adder : CALL_COUNTS) {
            adder.reset();
        }
        for (LongAccumulator acc : MAX_NANOS) {
            acc.reset();
        }
        for (LongAdder counter : COUNTERS) {
            counter.reset();
        }
    }

    public static TimedSection start(Scope scope) {
        if (!enabled) {
            return TimedSection.NOOP;
        }
        return new TimedSection(scope);
    }

    public static void record(Scope scope, long nanos) {
        if (!enabled || scope == null || nanos < 0L) {
            return;
        }
        int index = scope.ordinal();
        TOTAL_NANOS[index].add(nanos);
        CALL_COUNTS[index].increment();
        MAX_NANOS[index].accumulate(nanos);
    }

    public static void addCounter(Counter counter, long delta) {
        if (!enabled || counter == null || delta == 0L) {
            return;
        }
        COUNTERS[counter.ordinal()].add(delta);
    }

    public static Snapshot snapshot() {
        ScopeStats[] scopes = new ScopeStats[Scope.values().length];
        for (Scope scope : Scope.values()) {
            int index = scope.ordinal();
            long calls = CALL_COUNTS[index].sum();
            long totalNanos = TOTAL_NANOS[index].sum();
            long maxNanos = calls > 0L ? MAX_NANOS[index].get() : 0L;
            scopes[index] = new ScopeStats(scope, calls, totalNanos, maxNanos);
        }
        long[] counterValues = new long[Counter.values().length];
        for (Counter counter : Counter.values()) {
            counterValues[counter.ordinal()] = COUNTERS[counter.ordinal()].sum();
        }
        return new Snapshot(scopes, counterValues);
    }

    public static String formatReport(Snapshot snapshot) {
        if (snapshot == null) {
            return "=== SimulationDiagnostics (empty) ===";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("=== SimulationDiagnostics ===").append(System.lineSeparator());
        for (ScopeStats stats : snapshot.scopes()) {
            if (stats.calls() <= 0L) {
                continue;
            }
            sb.append(String.format(Locale.ROOT,
                    "  %-24s calls=%6d total=%8.2fms avg=%8.3fms max=%8.3fms%n",
                    stats.scope().name(),
                    stats.calls(),
                    stats.totalNanos() / 1_000_000.0,
                    stats.avgNanos() / 1_000_000.0,
                    stats.maxNanos() / 1_000_000.0));
        }
        sb.append("Counters:").append(System.lineSeparator());
        for (Counter counter : Counter.values()) {
            long value = snapshot.counter(counter);
            if (value <= 0L) {
                continue;
            }
            sb.append(String.format(Locale.ROOT, "  %-24s %d%n", counter.name(), value));
        }
        return sb.toString().trim();
    }

    public static void printReport() {
        System.out.println(formatReport(snapshot()));
    }

    public record ScopeStats(Scope scope, long calls, long totalNanos, long maxNanos) {
        public double avgNanos() {
            return calls > 0L ? (double) totalNanos / (double) calls : 0.0;
        }
    }

    public record Snapshot(ScopeStats[] scopes, long[] counters) {
        public long counter(Counter counter) {
            return counters[counter.ordinal()];
        }
    }

    public static final class TimedSection implements AutoCloseable {
        static final TimedSection NOOP = new TimedSection(null, 0L);

        private final Scope scope;
        private final long startNanos;

        private TimedSection(Scope scope, long startNanos) {
            this.scope = scope;
            this.startNanos = startNanos;
        }

        TimedSection(Scope scope) {
            this(scope, System.nanoTime());
        }

        @Override
        public void close() {
            if (scope != null) {
                record(scope, System.nanoTime() - startNanos);
            }
        }
    }
}
