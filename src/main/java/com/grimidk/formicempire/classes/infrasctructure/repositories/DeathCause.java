package com.grimidk.formicempire.classes.infrasctructure.repositories;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class DeathCause {

    public static final String OLD_AGE = "death_old_age";
    public static final String STARVATION = "death_starvation";
    public static final String DEHYDRATION = "death_dehydration";
    public static final String CONTAMINATION = "death_contamination";
    public static final String CONFLICT = "death_conflict";
    public static final String ILLNESS = "death_illness";
    public static final String LACK_OF_CARE = "death_lack_of_care";
    public static final String OTHER = "death_other";

    private static final Set<String> KNOWN = Set.of(
            OLD_AGE, STARVATION, DEHYDRATION, CONTAMINATION, CONFLICT, ILLNESS, LACK_OF_CARE, OTHER);

    private static final Map<String, String> LEGACY = Map.ofEntries(
            Map.entry("Old Age", OLD_AGE),
            Map.entry("Starvation", STARVATION),
            Map.entry("Dehydration", DEHYDRATION),
            Map.entry("Contamination", CONTAMINATION),
            Map.entry("Conflict", CONFLICT),
            Map.entry("Illness", ILLNESS),
            Map.entry("Lack of Care", LACK_OF_CARE),
            Map.entry("Other", OTHER),
            Map.entry("Combat", CONFLICT),
            Map.entry("Unknown", OTHER));

    private DeathCause() {
    }

    public static String normalize(String cause) {
        if (cause == null) {
            return OTHER;
        }
        if (KNOWN.contains(cause)) {
            return cause;
        }
        String legacy = LEGACY.get(cause);
        if (legacy != null) {
            return legacy;
        }
        if ("Combat".equalsIgnoreCase(cause)) {
            return CONFLICT;
        }
        return OTHER;
    }

    public static Map<String, Integer> migrateStatistics(Map<String, Integer> saved) {
        if (saved == null) {
            return null;
        }
        Map<String, Integer> migrated = new HashMap<>();
        for (Map.Entry<String, Integer> entry : saved.entrySet()) {
            String key = normalize(entry.getKey());
            migrated.merge(key, entry.getValue(), Integer::sum);
        }
        return migrated;
    }
}
