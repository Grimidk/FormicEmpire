package com.grimidk.formicempire.classes.infrasctructure.audio;

import java.util.Objects;

public final class MusicTrack {
    public static final String MUSIC_ROOT = "/audio/music";
    public static final String BASE_DIR = MUSIC_ROOT + "/base";
    public static final String MAIN_MENU_ID = "MainMenu";

    private final String id;
    private final String displayName;
    private final String author;
    private final String fileName;
    private final String resourcePath;

    public MusicTrack(String id, String displayName, String author) {
        this.id = Objects.requireNonNull(id, "id").trim();
        if (this.id.isEmpty()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        this.displayName = Objects.requireNonNull(displayName, "displayName").trim();
        if (this.displayName.isEmpty()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        this.author = Objects.requireNonNull(author, "author").trim();
        if (this.author.isEmpty()) {
            throw new IllegalArgumentException("author must not be blank");
        }
        this.fileName = this.id + ".mp3";
        this.resourcePath = BASE_DIR + "/" + this.fileName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAuthor() {
        return author;
    }

    public String getFileName() {
        return fileName;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public String getLabel() {
        return displayName + " — " + author;
    }

    public boolean isMainMenuTrack() {
        return MAIN_MENU_ID.equals(id);
    }

    public boolean isResourcePresent() {
        return MusicTrack.class.getResource(resourcePath) != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MusicTrack other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
