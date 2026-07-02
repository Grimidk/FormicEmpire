package com.grimidk.formicempire.classes.interfaces.ui.util;

/**
 * Compact metric-style display for user-facing counts (1k, 1.5m, …).
 * Values with magnitude below 1000 render without a suffix.
 */
public final class UiNumberFormat {
    private static final double COMPACT_THRESHOLD = 1000.0;
    private static final char[] SUFFIXES = {'k', 'm', 'b', 't', 'q', 'Q'};

    private UiNumberFormat() {
    }

    public static String format(int value) {
        return format((long) value);
    }

    public static String format(long value) {
        if (value == Long.MIN_VALUE) {
            return format((double) value);
        }
        return formatCompact(value);
    }

    public static String format(float value) {
        return format((double) value);
    }

    public static String format(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return String.valueOf(value);
        }
        return formatCompact(value);
    }

    public static String format(Number value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Long l) {
            return format(l.longValue());
        }
        if (value instanceof Integer i) {
            return format(i.intValue());
        }
        if (value instanceof Double d) {
            return format(d.doubleValue());
        }
        if (value instanceof Float f) {
            return format(f.floatValue());
        }
        if (value instanceof Short s) {
            return format(s.intValue());
        }
        if (value instanceof Byte b) {
            return format(b.intValue());
        }
        return format(value.doubleValue());
    }

    public static String formatSigned(int value) {
        if (value > 0) {
            return "+" + format(value);
        }
        return format(value);
    }

    public static String formatSigned(long value) {
        if (value > 0) {
            return "+" + format(value);
        }
        return format(value);
    }

    public static String formatRatio(long current, long max) {
        return format(current) + " / " + format(max);
    }

    public static String formatRatio(int current, int max) {
        return formatRatio((long) current, (long) max);
    }

    public static Object[] formatDisplayArgs(Object... args) {
        if (args == null || args.length == 0) {
            return args;
        }
        Object[] display = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            display[i] = formatDisplayArg(args[i]);
        }
        return display;
    }

    private static Object formatDisplayArg(Object arg) {
        if (arg instanceof Integer i) {
            return format(i.intValue());
        }
        if (arg instanceof Long l) {
            return format(l.longValue());
        }
        if (arg instanceof Short s) {
            return format(s.intValue());
        }
        if (arg instanceof Byte b) {
            return format(b.intValue());
        }
        if (arg instanceof Float f) {
            return format(f.floatValue());
        }
        if (arg instanceof Double d) {
            return format(d.doubleValue());
        }
        return arg;
    }

    private static String formatCompact(long value) {
        long abs = Math.abs(value);
        if (abs < (long) COMPACT_THRESHOLD) {
            return Long.toString(value);
        }
        return formatCompact(value, abs);
    }

    private static String formatCompact(double value) {
        double abs = Math.abs(value);
        if (abs < COMPACT_THRESHOLD) {
            return formatBelowThreshold(value);
        }
        return formatCompact(value, abs);
    }

    private static String formatCompact(double signedValue, double abs) {
        int tier = 0;
        double scaled = abs;
        while (scaled >= COMPACT_THRESHOLD && tier < SUFFIXES.length) {
            scaled /= 1000.0;
            tier++;
        }
        if (tier == 0) {
            return formatBelowThreshold(signedValue);
        }
        char suffix = SUFFIXES[tier - 1];
        boolean negative = signedValue < 0;
        return (negative ? "-" : "") + formatOneDecimal(scaled) + suffix;
    }

    private static String formatBelowThreshold(double value) {
        if (Math.rint(value) == value && !Double.isInfinite(value)) {
            return Long.toString((long) value);
        }
        return formatOneDecimal(value);
    }

    private static String formatOneDecimal(double abs) {
        double rounded = Math.round(abs * 10.0) / 10.0;
        if (Math.abs(rounded - Math.rint(rounded)) < 0.05) {
            return Long.toString(Math.round(rounded));
        }
        return String.format(java.util.Locale.US, "%.1f", rounded);
    }
}
