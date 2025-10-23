package com.grimidk.formicempire.classes;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import java.util.HashMap;
import java.util.Map;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class SaveManager {

    private final File savesDir;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "save-worker"));

    public SaveManager() {
        this.savesDir = new File("saves");
        if (!savesDir.exists()) {
            savesDir.mkdirs();
        }
        // Legacy/trash migration and cleanup removed per request.
    }

    private void writeManualSave(Savefile save) throws IOException {
        File f = new File(savesDir, "slot" + save.getId() + ".manual.json");
        File tmp = new File(savesDir, "slot" + save.getId() + ".manual.json.tmp");
        if (save.getPlayTime() == 0) save.setPlayTime(computePlayTimeFromSave(save));
        try (java.io.BufferedWriter bw = java.nio.file.Files.newBufferedWriter(tmp.toPath(), StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING)) {
            writeSaveToWriter(save, bw);
            bw.flush();
        }
        try {
            java.nio.file.Files.move(tmp.toPath(), f.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException amnse) {
            java.nio.file.Files.move(tmp.toPath(), f.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("[SaveManager] Wrote manual save for slot " + save.getId() + " -> " + f.getName());
    }

    public void saveAutosave(World w) {
        if (w == null) return;
        int slotId = 0;
        try { slotId = w.getSaveSlotId(); } catch (Exception ignore) { slotId = 0; }
        File f;
        if (slotId > 0) f = new File(savesDir, "slot" + slotId + ".autosave.json");
        else f = new File(savesDir, "autosave.json");
        Savefile save = new Savefile(slotId, "autosave");
        save.setMinute(w.getMinute());
        save.setHour(w.getHour());
        save.setDay(w.getDay());
        save.setMonth(w.getMonth());
        save.setYear(w.getYear());
        int playTime = computePlayTime(w);
        save.setPlayTime(playTime);
        if (w.getHexes() != null && !w.getHexes().isEmpty()) {
            try {
                Colony c = w.getSpawnHex().getColony();
                if (c != null) {
                    save.setTotalAnts(c.getAntTotal());
                    save.setWorkers(c.getWorkers() != null ? c.getWorkers().size() : 0);
                    save.setSoldiers(c.getSoldiers() != null ? c.getSoldiers().size() : 0);
                    save.setQueens(c.getQueens() != null ? c.getQueens().size() : 0);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
        File tmp = new File(f.getAbsolutePath() + ".tmp");
        try {
            File manual = slotId > 0 ? new File(savesDir, "slot" + slotId + ".manual.json") : null;
            if (manual != null && manual.exists()) {
                try (java.io.BufferedReader r = java.nio.file.Files.newBufferedReader(manual.toPath(), StandardCharsets.UTF_8)) {
                    Savefile existing = readSaveFromReader(r);
                    if (existing != null) {
                        int existingPlay = existing.getPlayTime() != 0 ? existing.getPlayTime() : computePlayTimeFromSave(existing);
                        if (existingPlay > save.getPlayTime()) {
                            System.out.println("[SaveManager] Manual save for slot " + slotId + " is newer than autosave - skipping autosave");
                            return;
                        }
                    }
                } catch (Exception ignore) {}
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        try (java.io.BufferedWriter bw = java.nio.file.Files.newBufferedWriter(tmp.toPath(), StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING)) {
            writeSaveToWriter(save, bw);
            bw.flush();
            try {
                java.nio.file.Files.move(tmp.toPath(), f.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException amnse) {
                java.nio.file.Files.move(tmp.toPath(), f.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("[SaveManager] Autosaved world to " + f.getAbsolutePath() + " (Day " + save.getDay() + " H" + save.getHour() + ")");
        } catch (IOException ex) {
            ex.printStackTrace();
            try { if (tmp.exists()) tmp.delete(); } catch (Exception ignore) {}
        }
    }

    public void saveUserSlot(Savefile save) throws IOException {
        writeManualSave(save);
    }

    public void saveUserSlotAsync(Savefile save, Runnable onComplete) {
        executor.submit(() -> {
            try {
                writeManualSave(save);
                if (onComplete != null) SwingUtilities.invokeLater(onComplete);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public boolean deleteSlot(int slotId) {
        boolean removed = false;
        String[] names = new String[] {"slot" + slotId + ".manual.json", "slot" + slotId + ".autosave.json", "slot" + slotId + ".json"};
        for (String n : names) {
            File f = new File(savesDir, n);
            if (f.exists()) {
                try { f.delete(); removed = true; } catch (Exception ex) { ex.printStackTrace(); }
            }
        }
        return removed;
    }

    public void saveWorldToSlot(World w, int slotId) {
        if (w == null) return;
        Savefile save = new Savefile(slotId, "autosave");
        save.setMinute(w.getMinute());
        save.setHour(w.getHour());
        save.setDay(w.getDay());
        save.setMonth(w.getMonth());
        save.setYear(w.getYear());
        save.setPlayTime(computePlayTime(w));
        if (w.getHexes() != null && !w.getHexes().isEmpty()) {
            try {
                Colony c = w.getSpawnHex().getColony();
                if (c != null) {
                    save.setTotalAnts(c.getAntTotal());
                    save.setWorkers(c.getWorkers() != null ? c.getWorkers().size() : 0);
                    save.setSoldiers(c.getSoldiers() != null ? c.getSoldiers().size() : 0);
                    save.setQueens(c.getQueens() != null ? c.getQueens().size() : 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File f = slotId > 0 ? new File(savesDir, "slot" + slotId + ".autosave.json") : new File(savesDir, "autosave.json");
        File tmp = new File(f.getAbsolutePath() + ".tmp");
        try {
            if (slotId > 0) {
                File manual = new File(savesDir, "slot" + slotId + ".manual.json");
                if (manual.exists()) {
                    try (java.io.BufferedReader r = java.nio.file.Files.newBufferedReader(manual.toPath(), StandardCharsets.UTF_8)) {
                        Savefile existing = readSaveFromReader(r);
                        if (existing != null) {
                            int existingPlay = existing.getPlayTime() != 0 ? existing.getPlayTime() : computePlayTimeFromSave(existing);
                            if (existingPlay > save.getPlayTime()) {
                                System.out.println("[SaveManager] Manual save for slot " + slotId + " is newer than autosave - skipping autosave");
                                return;
                            }
                        }
                    } catch (Exception ignore) {}
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        try (java.io.BufferedWriter bw = java.nio.file.Files.newBufferedWriter(tmp.toPath(), StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING)) {
            writeSaveToWriter(save, bw);
            bw.flush();
            try {
                java.nio.file.Files.move(tmp.toPath(), f.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException amnse) {
                java.nio.file.Files.move(tmp.toPath(), f.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("[SaveManager] Autosaved world to " + f.getAbsolutePath() + " (Day " + save.getDay() + " H" + save.getHour() + ")");
        } catch (IOException ex) {
            ex.printStackTrace();
            try { if (tmp.exists()) tmp.delete(); } catch (Exception ignore) {}
        }
    }

    public Savefile loadSlot(int slotId) {
        File manual = new File(savesDir, "slot" + slotId + ".manual.json");
        File autos = new File(savesDir, "slot" + slotId + ".autosave.json");
        File legacy = new File(savesDir, "slot" + slotId + ".json");
        File f = null;
        if (manual.exists()) f = manual;
        else if (autos.exists()) f = autos;
        else if (legacy.exists()) f = legacy;
        if (f == null) return null;
        if (f.length() == 0) {
            System.out.println("[SaveManager] Found empty save file for slot " + slotId + " - ignoring");
            return null;
        }
        try (java.io.BufferedReader r = java.nio.file.Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
            Savefile s = readSaveFromReader(r);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveWorldToSlotUser(World w, int slotId) throws IOException {
        if (w == null) return;
        String slotName = "Save " + slotId;
        try {
            Savefile existing = loadSlot(slotId);
            if (existing != null && existing.getName() != null && !existing.getName().isEmpty()) {
                slotName = existing.getName();
            }
        } catch (Exception ignore) {}
        Savefile save = new Savefile(slotId, slotName);
        save.setMinute(w.getMinute());
        save.setHour(w.getHour());
        save.setDay(w.getDay());
        save.setMonth(w.getMonth());
        save.setYear(w.getYear());
        save.setPlayTime(computePlayTime(w));
        if (w.getHexes() != null && !w.getHexes().isEmpty()) {
            try {
                Colony c = w.getSpawnHex().getColony();
                if (c != null) {
                    save.setTotalAnts(c.getAntTotal());
                    save.setWorkers(c.getWorkers() != null ? c.getWorkers().size() : 0);
                    save.setSoldiers(c.getSoldiers() != null ? c.getSoldiers().size() : 0);
                    save.setQueens(c.getQueens() != null ? c.getQueens().size() : 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        writeManualSave(save);
    }

    public void saveWorldToSlotUserAsync(World w, int slotId, Runnable onComplete) {
        executor.submit(() -> {
            try {
                saveWorldToSlotUser(w, slotId);
                if (onComplete != null) SwingUtilities.invokeLater(onComplete);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void saveWorldToSlotUserAsync(World w, int slotId, String name, Runnable onComplete) {
        executor.submit(() -> {
            try {
                if (w == null) {
                    if (onComplete != null) SwingUtilities.invokeLater(onComplete);
                    return;
                }
                Savefile save = new Savefile(slotId, name != null && !name.isEmpty() ? name : ("Save " + slotId));
                save.setMinute(w.getMinute());
                save.setHour(w.getHour());
                save.setDay(w.getDay());
                save.setMonth(w.getMonth());
                save.setYear(w.getYear());
                save.setPlayTime(computePlayTime(w));
                if (w.getHexes() != null && !w.getHexes().isEmpty()) {
                    try {
                        Colony c = w.getSpawnHex().getColony();
                        if (c != null) {
                            save.setTotalAnts(c.getAntTotal());
                            save.setWorkers(c.getWorkers() != null ? c.getWorkers().size() : 0);
                            save.setSoldiers(c.getSoldiers() != null ? c.getSoldiers().size() : 0);
                            save.setQueens(c.getQueens() != null ? c.getQueens().size() : 0);
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
                writeManualSave(save);
                if (onComplete != null) SwingUtilities.invokeLater(onComplete);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private int computePlayTime(World w) {
        if (w == null) return 0;
        int days = w.getDay() + (w.getMonth() * 30) + (w.getYear() * 12 * 30);
        int totalMinutes = days * 24 * 60 + w.getHour() * 60 + w.getMinute();
        return totalMinutes;
    }
    
    private int computePlayTimeFromSave(Savefile s) {
        if (s == null) return 0;
        int days = s.getDay() + (s.getMonth() * 30) + (s.getYear() * 12 * 30);
        int totalMinutes = days * 24 * 60 + s.getHour() * 60 + s.getMinute();
        return totalMinutes;
    }

    public void saveSlotAsync(Savefile save, Runnable onComplete) {
        executor.submit(() -> {
            try {
                saveUserSlot(save);
                if (onComplete != null) SwingUtilities.invokeLater(onComplete);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void saveAutosaveAsync(World w, Runnable onComplete) {
        executor.submit(() -> {
            saveAutosave(w);
            if (onComplete != null) SwingUtilities.invokeLater(onComplete);
        });
    }

    private void writeSaveToWriter(Savefile s, java.io.BufferedWriter w) throws IOException {
        Map<String, String> m = new HashMap<>();
        m.put("id", Integer.toString(s.getId()));
        m.put("name", s.getName() != null ? URLEncoder.encode(s.getName(), StandardCharsets.UTF_8) : "");
        m.put("progress", Float.toString(s.getProgress()));
        m.put("playTime", Integer.toString(s.getPlayTime()));
        m.put("minute", Integer.toString(s.getMinute()));
        m.put("hour", Integer.toString(s.getHour()));
        m.put("day", Integer.toString(s.getDay()));
        m.put("month", Integer.toString(s.getMonth()));
        m.put("year", Integer.toString(s.getYear()));
        m.put("totalAnts", Integer.toString(s.getTotalAnts()));
        m.put("workers", Integer.toString(s.getWorkers()));
        m.put("soldiers", Integer.toString(s.getSoldiers()));
        m.put("queens", Integer.toString(s.getQueens()));
        for (Map.Entry<String,String> e : m.entrySet()) {
            w.write(e.getKey());
            w.write('=');
            w.write(e.getValue() != null ? e.getValue() : "");
            w.newLine();
        }
    }

    private Savefile readSaveFromReader(java.io.BufferedReader r) throws IOException {
        Savefile s = null;
        String line;
        Map<String,String> m = new HashMap<>();
        while ((line = r.readLine()) != null) {
            int idx = line.indexOf('=');
            if (idx <= 0) continue;
            String k = line.substring(0, idx);
            String v = line.substring(idx+1);
            m.put(k, v);
        }
        if (m.containsKey("id") && m.containsKey("name")) {
            int id = Integer.parseInt(m.getOrDefault("id", "0"));
            String name = m.getOrDefault("name", "");
            try { name = URLDecoder.decode(name, StandardCharsets.UTF_8); } catch (Exception ignore) {}
            s = new Savefile(id, name);
            s.setProgress(Float.parseFloat(m.getOrDefault("progress", "0")));
            s.setPlayTime(Integer.parseInt(m.getOrDefault("playTime", "0")));
            s.setMinute(Integer.parseInt(m.getOrDefault("minute", "0")));
            s.setHour(Integer.parseInt(m.getOrDefault("hour", "0")));
            s.setDay(Integer.parseInt(m.getOrDefault("day", "0")));
            s.setMonth(Integer.parseInt(m.getOrDefault("month", "0")));
            s.setYear(Integer.parseInt(m.getOrDefault("year", "0")));
            s.setTotalAnts(Integer.parseInt(m.getOrDefault("totalAnts", "0")));
            s.setWorkers(Integer.parseInt(m.getOrDefault("workers", "0")));
            s.setSoldiers(Integer.parseInt(m.getOrDefault("soldiers", "0")));
            s.setQueens(Integer.parseInt(m.getOrDefault("queens", "0")));
        }
        return s;
    }
}
