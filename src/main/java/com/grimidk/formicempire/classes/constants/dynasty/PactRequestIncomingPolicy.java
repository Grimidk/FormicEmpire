package com.grimidk.formicempire.classes.constants.dynasty;

public enum PactRequestIncomingPolicy {
    MANUAL("DIPLO_PACT_INCOMING_MANUAL"),
    AUTO_ACCEPT("DIPLO_PACT_INCOMING_AUTO_ACCEPT"),
    AUTO_DECLINE("DIPLO_PACT_INCOMING_AUTO_DECLINE");

    private final String nameKey;

    PactRequestIncomingPolicy(String nameKey) {
        this.nameKey = nameKey;
    }

    public String getNameKey() {
        return nameKey;
    }

    public static PactRequestIncomingPolicy fromPersistenceKey(String key) {
        if (key == null || key.isBlank()) {
            return MANUAL;
        }
        try {
            return valueOf(key);
        } catch (IllegalArgumentException ignored) {
            return MANUAL;
        }
    }
}
