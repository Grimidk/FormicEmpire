package com.grimidk.formicempire.classes.infrasctructure.audio;

import com.grimidk.formicempire.classes.infrasctructure.registries.MusicTracks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MusicTracksTest {

    @Test
    void registryIncludesPlaylistSampleAndMainMenu() {
        assertFalse(MusicTracks.getTracks().isEmpty());
        MusicTrack sample = MusicTracks.getById("sample1");
        assertNotNull(sample);
        assertEquals("Sample 1", sample.getDisplayName());
        assertTrue(sample.isResourcePresent());
        assertEquals("/audio/music/base/sample1.mp3", sample.getResourcePath());
        assertFalse(MusicTracks.getPlayableSessionTracks().isEmpty());

        MusicTrack mainMenu = MusicTracks.getById(MusicTrack.MAIN_MENU_ID);
        assertNotNull(mainMenu);
        assertTrue(mainMenu.isMainMenuTrack());
        assertFalse(mainMenu.isResourcePresent());
        assertTrue(MusicTracks.getPlayableMenuTracks().isEmpty());
    }

    @Test
    void musicTrackBuildsPlaylistClasspathPath() {
        MusicTrack track = new MusicTrack("EmpireTitle", "Empire Title", "GrimIDK");
        assertEquals("EmpireTitle", track.getId());
        assertEquals("Empire Title", track.getDisplayName());
        assertEquals("GrimIDK", track.getAuthor());
        assertEquals("Empire Title — GrimIDK", track.getLabel());
        assertEquals("EmpireTitle.mp3", track.getFileName());
        assertEquals("/audio/music/base/EmpireTitle.mp3", track.getResourcePath());
        assertFalse(track.isResourcePresent());
    }

    @Test
    void musicTrackRejectsBlankFields() {
        assertThrows(IllegalArgumentException.class,
                () -> new MusicTrack(" ", "Title", "Author"));
        assertThrows(IllegalArgumentException.class,
                () -> new MusicTrack("Id", " ", "Author"));
        assertThrows(IllegalArgumentException.class,
                () -> new MusicTrack("Id", "Title", " "));
    }

    @Test
    void linearGain_respectsMuteAndVolumes() {
        assertEquals(0f, MusicService.linearGain(100, 100, true));
        assertEquals(1f, MusicService.linearGain(100, 100, false));
        assertEquals(0.25f, MusicService.linearGain(50, 50, false), 0.0001f);
    }

    @Test
    void applySoftwareGain_scalesOrSilencesSamples() {
        byte[] silent = new byte[] {10, 0, 20, 0};
        MusicService.applySoftwareGain(silent, 0, silent.length, 0f);
        assertEquals(0, silent[0]);
        assertEquals(0, silent[1]);

        byte[] half = new byte[] {(byte) 0x00, (byte) 0x40, (byte) 0x00, (byte) 0x00};
        MusicService.applySoftwareGain(half, 0, 2, 0.5f);
        short scaled = (short) ((half[0] & 0xff) | (half[1] << 8));
        assertEquals(8192, scaled);
    }
}
