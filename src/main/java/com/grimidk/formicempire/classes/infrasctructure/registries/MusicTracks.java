package com.grimidk.formicempire.classes.infrasctructure.registries;

import com.grimidk.formicempire.classes.infrasctructure.audio.MusicTrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MusicTracks {
    private static final List<MusicTrack> TRACKS = new ArrayList<>();

    public static final MusicTrack FRESH_ONE = track("freshOne", "Fresh One", "GrimIDK");
    public static final MusicTrack HARD_WAR = track("hardWar", "Hard War", "GrimIDK");
    public static final MusicTrack MAIN_MENU = track("MainMenu", "Main Menu", "GrimIDK");

    private MusicTracks() {}

    public static List<MusicTrack> getTracks() {
        return Collections.unmodifiableList(TRACKS);
    }

    public static List<MusicTrack> getBaseTracks() {
        return getTracks();
    }

    public static List<MusicTrack> getPlayableBaseTracks() {
        return filterPlayable(TRACKS, false);
    }

    public static List<MusicTrack> getPlayableMenuTracks() {
        List<MusicTrack> out = new ArrayList<>();
        MusicTrack mainMenu = getById(MusicTrack.MAIN_MENU_ID);
        if (mainMenu != null && mainMenu.isResourcePresent()) {
            out.add(mainMenu);
        }
        return Collections.unmodifiableList(out);
    }

    public static List<MusicTrack> getPlayableSessionTracks() {
        return filterPlayable(TRACKS, true);
    }

    public static MusicTrack getById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        for (MusicTrack track : TRACKS) {
            if (track.getId().equals(id)) {
                return track;
            }
        }
        return null;
    }

    private static List<MusicTrack> filterPlayable(List<MusicTrack> source, boolean excludeMainMenu) {
        List<MusicTrack> out = new ArrayList<>();
        for (MusicTrack track : source) {
            if (excludeMainMenu && track.isMainMenuTrack()) {
                continue;
            }
            if (!track.isResourcePresent()) {
                continue;
            }
            out.add(track);
        }
        return Collections.unmodifiableList(out);
    }

    private static MusicTrack track(String id, String displayName, String author) {
        return register(new MusicTrack(id, displayName, author));
    }

    private static MusicTrack register(MusicTrack track) {
        for (MusicTrack existing : TRACKS) {
            if (existing.getId().equals(track.getId())) {
                throw new IllegalStateException("Duplicate music track: " + track.getId());
            }
        }
        TRACKS.add(track);
        return track;
    }
}
