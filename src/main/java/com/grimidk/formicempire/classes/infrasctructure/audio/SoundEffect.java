package com.grimidk.formicempire.classes.infrasctructure.audio;

import java.util.Objects;

public final class SoundEffect {
    public static final String EFFECTS_DIR = "/audio/effects";

    private final String id;
    private final String displayName;
    private final String fileName;
    private final String resourcePath;

    public SoundEffect(String id, String displayName) {
        this.id = Objects.requireNonNull(id, "id").trim();
        if (this.id.isEmpty()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        this.displayName = Objects.requireNonNull(displayName, "displayName").trim();
        if (this.displayName.isEmpty()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        this.fileName = this.id + ".mp3";
        this.resourcePath = EFFECTS_DIR + "/" + this.fileName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public boolean isResourcePresent() {
        return SoundEffect.class.getResource(resourcePath) != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SoundEffect other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
