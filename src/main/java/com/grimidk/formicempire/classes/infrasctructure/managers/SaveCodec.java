package com.grimidk.formicempire.classes.infrasctructure.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

final class SaveCodec {
    private static final Gson GSON = new GsonBuilder().create();
    private static final String META_SUFFIX = ".meta.json";

    static class SaveMetadata {
        int id;
        String name;
        String version;
        long timestamp;
        int playTime;
        int minute;
        int hour;
        int day;
        int month;
        int year;
        int worldRadius;
        int continentCoreRadius;
        int worldWidth;
        int worldHeight;
        int playerDynastyTitleId;
        String playerDynastyTitleKey;
        int starterBiomeId;
        int starterDietId;
        int colonyId;
        String colonyName;
        int totalAnts;
        int deadAnts;
        int workers;
        int queens;
        boolean allowsAchievements = true;
        boolean spectatorMode = false;

        static SaveMetadata fromSavefile(Savefile s) {
            SaveMetadata m = new SaveMetadata();
            m.id = s.getId();
            m.name = s.getName();
            m.version = s.getVersion();
            m.timestamp = s.getTimestamp();
            m.playTime = s.getPlayTime();
            m.minute = s.getMinute();
            m.hour = s.getHour();
            m.day = s.getDay();
            m.month = s.getMonth();
            m.year = s.getYear();
            m.worldRadius = s.getWorldRadius();
            m.continentCoreRadius = s.getContinentCoreRadius();
            m.worldWidth = s.getWorldWidth();
            m.worldHeight = s.getWorldHeight();
            m.playerDynastyTitleId = s.getPlayerDynastyTitleId();
            m.playerDynastyTitleKey = s.getPlayerDynastyTitleKey();
            m.starterBiomeId = s.getStarterBiomeId();
            m.starterDietId = s.getStarterDietId();
            m.colonyId = s.getColonyId();
            m.colonyName = s.getColonyName();
            m.totalAnts = s.getTotalAnts();
            m.deadAnts = s.getDeadAnts();
            m.workers = s.getWorkers();
            m.queens = s.getQueens();
            m.allowsAchievements = s.allowsAchievements();
            m.spectatorMode = s.isSpectatorMode();
            return m;
        }

        Savefile toPreviewSavefile() {
            Savefile s = new Savefile(id, name != null ? name : "");
            s.setVersion(version);
            s.setTimestamp(timestamp);
            s.setPlayTime(playTime);
            s.setMinute(minute);
            s.setHour(hour);
            s.setDay(day);
            s.setMonth(month);
            s.setYear(year);
            s.setWorldRadius(worldRadius);
            s.setContinentCoreRadius(continentCoreRadius);
            s.setWorldWidth(worldWidth);
            s.setWorldHeight(worldHeight);
            s.setPlayerDynastyTitleId(playerDynastyTitleId);
            s.setPlayerDynastyTitleKey(playerDynastyTitleKey);
            s.setStarterBiomeId(starterBiomeId);
            s.setStarterDietId(starterDietId);
            s.setColonyId(colonyId);
            s.setColonyName(colonyName);
            s.setTotalAnts(totalAnts);
            s.setDeadAnts(deadAnts);
            s.setWorkers(workers);
            s.setQueens(queens);
            s.setAllowsAchievements(allowsAchievements);
            s.setSpectatorMode(spectatorMode);
            return s;
        }
    }

    private SaveCodec() {}

    static File getMetadataFile(File saveFile) {
        return new File(saveFile.getAbsolutePath() + META_SUFFIX);
    }

    static void writeCompressed(Savefile save, File targetFile) throws IOException {
        if (save == null) {
            return;
        }
        save.setFormatVersion(Savefile.CURRENT_FORMAT_VERSION);
        pruneZeroEntries(save);

        File tmp = new File(targetFile.getAbsolutePath() + ".tmp");
        try (GZIPOutputStream gzos = new GZIPOutputStream(Files.newOutputStream(tmp.toPath()));
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(gzos, StandardCharsets.UTF_8))) {
            GSON.toJson(save, bw);
            bw.flush();
        } catch (IOException e) {
            try { if (tmp.exists()) tmp.delete(); } catch (Exception ignore) {}
            throw e;
        }
        moveAtomically(tmp, targetFile);
        writeMetadataSidecar(save, targetFile);
    }

    static void writeMetadataSidecar(Savefile save, File saveFile) {
        if (save == null || saveFile == null) {
            return;
        }
        File metaFile = getMetadataFile(saveFile);
        File tmp = new File(metaFile.getAbsolutePath() + ".tmp");
        try (BufferedWriter bw = Files.newBufferedWriter(tmp.toPath(), StandardCharsets.UTF_8)) {
            GSON.toJson(SaveMetadata.fromSavefile(save), bw);
            bw.flush();
        } catch (IOException e) {
            try { if (tmp.exists()) tmp.delete(); } catch (Exception ignore) {}
            return;
        }
        try {
            moveAtomically(tmp, metaFile);
        } catch (IOException e) {
            try { if (tmp.exists()) tmp.delete(); } catch (Exception ignore) {}
        }
    }

    static Savefile readMetadata(File saveFile) {
        if (saveFile == null || !saveFile.exists() || saveFile.length() == 0) {
            return null;
        }
        File metaFile = getMetadataFile(saveFile);
        if (!metaFile.exists() || metaFile.length() == 0 || metaFile.lastModified() < saveFile.lastModified()) {
            return null;
        }
        try (BufferedReader br = Files.newBufferedReader(metaFile.toPath(), StandardCharsets.UTF_8)) {
            SaveMetadata meta = GSON.fromJson(br, SaveMetadata.class);
            return meta != null ? meta.toPreviewSavefile() : null;
        } catch (Exception e) {
            return null;
        }
    }

    static String toJson(Object value) {
        return GSON.toJson(value);
    }

    static <T> T fromJson(String json, Class<T> type) {
        return GSON.fromJson(json, type);
    }

    static Savefile read(File file) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
            if (isGzipStream(bis)) {
                try (GZIPInputStream gzis = new GZIPInputStream(bis);
                     BufferedReader br = new BufferedReader(new InputStreamReader(gzis, StandardCharsets.UTF_8))) {
                    return ensureDefaults(GSON.fromJson(br, Savefile.class));
                }
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(bis, StandardCharsets.UTF_8))) {
                return ensureDefaults(GSON.fromJson(br, Savefile.class));
            }
        }
    }

    private static boolean isGzipStream(BufferedInputStream bis) throws IOException {
        bis.mark(2);
        int b1 = bis.read();
        int b2 = bis.read();
        bis.reset();
        return b1 == 0x1f && b2 == 0x8b;
    }

    static void pruneZeroEntries(Savefile save) {
        if (save == null) {
            return;
        }
        if (save.getColonies() != null) {
            for (Savefile.SavedColony c : save.getColonies()) {
                if (c == null) continue;
                pruneZeroIntMap(c.assignedRoleCounts);
                pruneZeroIntMap(c.warAssignedRoleCounts);
                pruneZeroIntMap(c.localDeathStatistics);
            }
        }
        if (save.getDynastys() != null) {
            for (Savefile.SavedDynasty d : save.getDynastys()) {
                if (d == null) continue;
                pruneZeroIntMap(d.deathStatistics);
            }
        }
    }

    private static void pruneZeroIntMap(Map<?, Integer> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        map.entrySet().removeIf(e -> e.getValue() == null || e.getValue() == 0);
    }

    static Savefile ensureDefaults(Savefile save) {
        if (save == null) {
            return null;
        }
        if (save.getWorldHexes() == null) save.setWorldHexes(new ArrayList<>());
        if (save.getColonies() == null) save.setColonies(new ArrayList<>());
        if (save.getDynastys() == null) save.setDynastys(new ArrayList<>());
        if (save.getTrades() == null) save.setTrades(new ArrayList<>());
        if (save.getWars() == null) save.setWars(new ArrayList<>());
        if (save.getWorldHistory() == null) save.setWorldHistory(new ArrayList<>());

        for (Savefile.SavedColony c : save.getColonies()) {
            if (c == null) continue;
            if (c.assignedRoleCounts == null) c.assignedRoleCounts = new HashMap<>();
            if (c.warAssignedRoleCounts == null) c.warAssignedRoleCounts = new HashMap<>();
            if (c.modHatchRatesFlat == null) c.modHatchRatesFlat = new HashMap<>();
            if (c.localDeathStatistics == null) c.localDeathStatistics = new HashMap<>();
            if (c.unlockedBuildingIds == null) c.unlockedBuildingIds = new ArrayList<>();
            if (c.savedResourceSources == null) c.savedResourceSources = new ArrayList<>();
            if (c.knownHuntTargets == null) c.knownHuntTargets = new ArrayList<>();
            if (c.activeHuntExpeditions == null) c.activeHuntExpeditions = new ArrayList<>();
            if (c.invasionAlerts == null) c.invasionAlerts = new ArrayList<>();
            if (c.activeInvasionDefenses == null) c.activeInvasionDefenses = new ArrayList<>();
        }
        for (Savefile.SavedDynasty d : save.getDynastys()) {
            if (d == null) continue;
            if (d.unlockedUpgradeIds == null) d.unlockedUpgradeIds = new ArrayList<>();
            if (d.unlockedSkillIds == null) d.unlockedSkillIds = new ArrayList<>();
            if (d.announcedRankIds == null) d.announcedRankIds = new ArrayList<>();
            if (d.absorbedDynastyIds == null) d.absorbedDynastyIds = new ArrayList<>();
            if (d.defeatedSpeciesIds == null) d.defeatedSpeciesIds = new ArrayList<>();
            if (d.completedAssimilationIds == null) d.completedAssimilationIds = new ArrayList<>();
            if (d.deathStatistics == null) d.deathStatistics = new HashMap<>();
            if (d.knownHexKeys == null) d.knownHexKeys = new ArrayList<>();
            if (d.tunnels == null) d.tunnels = new ArrayList<>();
            if (d.bridges == null) d.bridges = new ArrayList<>();
            if (d.pendingTradeProposals == null) d.pendingTradeProposals = new ArrayList<>();
            if (d.wasAtWarPeacedAtWorldMonth == null) d.wasAtWarPeacedAtWorldMonth = new HashMap<>();
            if (d.geneticExchangeGrantedAtWorldMonth == null) d.geneticExchangeGrantedAtWorldMonth = new HashMap<>();
        }
        for (Savefile.SavedTrade t : save.getTrades()) {
            if (t == null) continue;
            if (t.load == null) t.load = new HashMap<>();
            if (t.returnLoad == null) t.returnLoad = new HashMap<>();
            if (t.transport == null) t.transport = new HashMap<>();
            if (t.pendingLoad == null) t.pendingLoad = new HashMap<>();
            if (t.pendingReturnLoad == null) t.pendingReturnLoad = new HashMap<>();
            if (t.pendingTransport == null) t.pendingTransport = new HashMap<>();
        }
        for (Savefile.SavedWar w : save.getWars()) {
            if (w == null) continue;
            if (w.capturedColonyIds == null) w.capturedColonyIds = "";
            if (w.capturedByDynastyIds == null) w.capturedByDynastyIds = "";
        }
        for (Savefile.SavedWorldHistoryEvent ev : save.getWorldHistory()) {
            if (ev == null) continue;
            if (ev.args == null) ev.args = new ArrayList<>();
        }
        return migrate(save);
    }

    static Savefile migrate(Savefile save) {
        if (save == null) {
            return null;
        }
        int v = save.getFormatVersion();
        if (v < Savefile.CURRENT_FORMAT_VERSION) {
            save.setFormatVersion(Savefile.CURRENT_FORMAT_VERSION);
        }
        return save;
    }

    private static void moveAtomically(File tmp, File targetFile) throws IOException {
        try {
            Files.move(tmp.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException amnse) {
            Files.move(tmp.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            try { if (tmp.exists()) tmp.delete(); } catch (Exception ignore) {}
            throw e;
        }
    }
}
