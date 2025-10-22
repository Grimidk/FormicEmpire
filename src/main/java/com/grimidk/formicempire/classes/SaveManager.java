package com.grimidk.formicempire.classes;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import java.util.HashMap;
import java.util.Map;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.StandardOpenOption;

public class SaveManager {

    private final File savesDir;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "save-worker"));

    public SaveManager() {
        this.savesDir = new File("saves");
        if (!savesDir.exists()) {
            savesDir.mkdirs();
        }
        // ensure trash folder exists for safe deletes
        File trash = new File(savesDir, "trash");
        if (!trash.exists()) trash.mkdirs();
    }

    private File lastSlotFile() {
        return new File(savesDir, "lastslot.txt");
    }

    public int getLastSlot() {
        File f = lastSlotFile();
        if (!f.exists()) return 0;
        try (BufferedReader r = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))) {
            String line = r.readLine();
            if (line == null) return 0;
            return Integer.parseInt(line.trim());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void setLastSlot(int slot) {
        File f = lastSlotFile();
        try (BufferedWriter w = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8))) {
            w.write(Integer.toString(slot));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Internal helper that performs the actual slot save. If allowOverwrite is false, this method will
    // refuse to overwrite any existing non-empty slot file (even if incoming save is newer) to be extra-safe.
    private void doSaveSlot(Savefile save, boolean allowOverwrite) throws IOException {
        File f = new File(savesDir, "slot" + save.getId() + ".json");
        File tmp = new File(savesDir, "slot" + save.getId() + ".json.tmp");
        if (save.getPlayTime() == 0) {
            int computed = computePlayTimeFromSave(save);
            save.setPlayTime(computed);
        }
        // If there is an existing non-empty slot file, attempt to read it and ensure we don't overwrite a
        // more-played save. If allowOverwrite is false, we refuse to overwrite non-empty files regardless.
        if (f.exists() && f.length() > 0) {
            try (java.io.BufferedReader r = java.nio.file.Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
                Savefile existing = readSaveFromReader(r);
                if (existing != null) {
                    int existingPlay = existing.getPlayTime() != 0 ? existing.getPlayTime() : computePlayTimeFromSave(existing);
                    if (!allowOverwrite) {
                        System.out.println("[SaveManager] Refusing to overwrite slot " + save.getId() + " because allowOverwrite=false");
                        return; // skip save
                    }
                    if (existingPlay > save.getPlayTime()) {
                        System.out.println("[SaveManager] Not overwriting slot " + save.getId() + " because existing save is newer (" + existingPlay + " > " + save.getPlayTime() + ")");
                        return; // skip save
                    }
                } else {
                    // couldn't parse existing save but file is non-empty; do not overwrite to be safe
                    System.out.println("[SaveManager] Existing slot " + save.getId() + " is non-empty but unreadable; skipping overwrite to avoid data loss");
                    return;
                }
            } catch (Exception readEx) {
                // If we can't read the existing file for any reason, skip overwrite to avoid data loss
                System.out.println("[SaveManager] Error reading existing slot " + save.getId() + " - skipping overwrite");
                readEx.printStackTrace();
                return;
            }
        }
        // check for recent trash entries: if the slot was just deleted, refuse to overwrite for safety
        // However, user-initiated saves (allowOverwrite==true) should be able to recreate a slot immediately
        try {
            if (!allowOverwrite) {
                File trashDir = new File(savesDir, "trash");
                if (trashDir.exists() && trashDir.isDirectory()) {
                    File[] files = trashDir.listFiles((d, name) -> name.startsWith("slot" + save.getId() + ".json.deleted."));
                    if (files != null && files.length > 0) {
                        long now = System.currentTimeMillis();
                        for (File tf : files) {
                            String[] parts = tf.getName().split("\\.");
                            try {
                                long ts = Long.parseLong(parts[parts.length-1]);
                                // if deleted within last 5 minutes, refuse overwrite
                                if (now - ts < 5L * 60L * 1000L) {
                                    System.out.println("[SaveManager] Recent deletion found for slot " + save.getId() + " (" + tf.getName() + ") - refusing overwrite");
                                    return;
                                }
                            } catch (Exception ignore) {}
                        }
                    }
                }
            } else {
                // allowOverwrite==true -> user-initiated save: skip recent-deletion guard
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // write using simple key=value format to avoid external JSON deps
        try (java.io.BufferedWriter bw = java.nio.file.Files.newBufferedWriter(tmp.toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writeSaveToWriter(save, bw);
            bw.flush();
        }
        try {
            // if target exists and is non-empty, back it up first
            if (f.exists() && f.length() > 0) {
                String bakName = "slot" + save.getId() + ".json.bak." + System.currentTimeMillis();
                File bak = new File(savesDir, bakName);
                java.nio.file.Files.move(f.toPath(), bak.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[SaveManager] Backed up existing slot " + save.getId() + " to " + bak.getName());
            }
            java.nio.file.Files.move(tmp.toPath(), f.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException amnse) {
            java.nio.file.Files.move(tmp.toPath(), f.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("[SaveManager] Saved slot " + save.getId() + " to " + f.getAbsolutePath());
    }

    // Public API for user-initiated saves: these are allowed to overwrite existing slot files when appropriate.
    public void saveUserSlot(Savefile save) throws IOException {
        doSaveSlot(save, true);
    }

    public void saveUserSlotAsync(Savefile save, Runnable onComplete) {
        executor.submit(() -> {
            try {
                doSaveSlot(save, true);
                if (onComplete != null) SwingUtilities.invokeLater(onComplete);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public boolean deleteSlot(int slotId) {
        File f = new File(savesDir, "slot" + slotId + ".json");
        if (f.exists()) {
            try {
                File trashDir = new File(savesDir, "trash");
                if (!trashDir.exists()) trashDir.mkdirs();
                String name = "slot" + slotId + ".json.deleted." + System.currentTimeMillis();
                File dest = new File(trashDir, name);
                java.nio.file.Files.move(f.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[SaveManager] Moved slot " + slotId + " to trash: " + dest.getAbsolutePath());
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("[SaveManager] Failed to move slot " + slotId + " to trash");
                return false;
            }
        }
        return false;
    }

    public void saveAutosave(World w) {
        if (w == null) return;
    File f = new File(savesDir, "autosave.json");
        Savefile save = new Savefile(0, "autosave");
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File tmp = new File(savesDir, "autosave.json.tmp");
        try (java.io.BufferedWriter bw = java.nio.file.Files.newBufferedWriter(tmp.toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
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
        // Note: we intentionally do NOT copy the autosave into the player's last-used slot automatically
        // to avoid accidentally overwriting a manual save with an autosave. The autosave remains in
        // 'autosave.json' and can be restored manually by the player from the save UI if desired.
    }

    public Savefile loadSlot(int slotId) {
        File f = new File(savesDir, "slot" + slotId + ".json");
        if (!f.exists()) return null;
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
        try {
            // programmatic world->slot saves should not overwrite existing user saves unless explicitly allowed
            doSaveSlot(save, false);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // User-initiated world->slot save: allow overwriting existing slot (creates backup)
    public void saveWorldToSlotUser(World w, int slotId) throws IOException {
        if (w == null) return;
        // Preserve existing slot name if present, otherwise use a default name
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
        doSaveSlot(save, true);
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

    // Save with an explicit name (useful when creating a new slot with user-provided name)
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
                doSaveSlot(save, true);
                if (onComplete != null) SwingUtilities.invokeLater(onComplete);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void saveWorldToLastSlotUserAsync(World w, Runnable onComplete) {
        int last = getLastSlot();
        if (last <= 0) {
            if (onComplete != null) SwingUtilities.invokeLater(onComplete);
            return;
        }
        saveWorldToSlotUserAsync(w, last, onComplete);
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

    public void saveWorldToLastSlot(World w) {
        int last = getLastSlot();
        if (last <= 0) return;
        saveWorldToSlot(w, last);
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

    // Simple serializer: key=value per line, URL-encoded values to be safe
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
