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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.io.BufferedReader;
import java.io.BufferedWriter;

public class SaveManager {

    private final File savesDir;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "save-worker"));

    public SaveManager() {
        this.savesDir = new File("saves");
        if (!savesDir.exists()) {
            savesDir.mkdirs();
        }
    }

    private File getSlotFile(int slotId, String type) {
        String fileName;
        switch (type) {
            case "manual":
                fileName = "slot" + slotId + ".manual.json";
                break;
            case "autosave":
                fileName = (slotId > 0) ? "slot" + slotId + ".autosave.json" : "autosave.json";
                break;
            case "legacy":
                fileName = "slot" + slotId + ".json";
                break;
            default:
                fileName = "slot" + slotId + "." + type + ".json";
        }
        return new File(savesDir, fileName);
    }

    private void writeSaveToFile(Savefile save, File targetFile) throws IOException {
        File tmp = new File(targetFile.getAbsolutePath() + ".tmp");
        try (BufferedWriter bw = Files.newBufferedWriter(tmp.toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writeSaveToWriter(save, bw);
            bw.flush();
        } catch (IOException e) {
            try { if (tmp.exists()) tmp.delete(); } catch (Exception ignore) {}
            throw e; 
        }

        try {
            Files.move(tmp.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException amnse) {
            Files.move(tmp.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            try { if (tmp.exists()) tmp.delete(); } catch (Exception ignore) {}
            throw e; 
        }
    }

    private void writeManualSave(Savefile save) throws IOException {
        File f = getSlotFile(save.getId(), "manual");
        if (save.getPlayTime() == 0) {
            save.setPlayTime(computePlayTimeFromSave(save));
        }
        writeSaveToFile(save, f); 
        System.out.println("[SaveManager] Wrote manual save for slot " + save.getId() + " -> " + f.getName());
    }

    public void saveAutosave(World w) {
        if (w == null) return;
        int slotId = 0;
        try {
            slotId = w.getSaveSlotId();
        } catch (Exception ignore) {
            slotId = 0; 
        }
        saveWorldToSlot(w, slotId);
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
        File[] filesToDelete = {
            getSlotFile(slotId, "manual"),
            getSlotFile(slotId, "autosave"),
            getSlotFile(slotId, "legacy")
        };
        
        for (File f : filesToDelete) {
            if (f.exists()) {
                try {
                    f.delete();
                    removed = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return removed;
    }

    public void saveWorldToSlot(World w, int slotId) {
        if (w == null) return;

        File f = getSlotFile(slotId, "autosave");
        Savefile save = new Savefile(slotId, "autosave");
        populateSavefileFromWorld(save, w);

        try {
            if (slotId > 0) {
                File manual = getSlotFile(slotId, "manual");
                if (manual.exists() && manual.length() > 0) {
                    try (BufferedReader r = Files.newBufferedReader(manual.toPath(), StandardCharsets.UTF_8)) {
                        Savefile existing = readSaveFromReader(r);
                        if (existing != null) {
                            int existingPlay = existing.getPlayTime() != 0 ? existing.getPlayTime() : computePlayTimeFromSave(existing);
                            if (existingPlay > save.getPlayTime()) {
                                System.out.println("[SaveManager] Manual save for slot " + slotId + " is newer than autosave - skipping autosave");
                                return;
                            }
                        }
                    } catch (Exception ignore) {
                    }
                }
            }
            
            writeSaveToFile(save, f);
            
            System.out.println("[SaveManager] Autosaved world to " + f.getAbsolutePath() + " (Day " + save.getDay() + " H" + save.getHour() + ")");
        
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Savefile loadSlot(int slotId) {
        File manual = getSlotFile(slotId, "manual");
        File autos = getSlotFile(slotId, "autosave");
        File legacy = getSlotFile(slotId, "legacy");

        File f = null;
        if (manual.exists()) f = manual;
        else if (autos.exists()) f = autos;
        else if (legacy.exists()) f = legacy;

        if (f == null) return null;
        if (f.length() == 0) {
            System.out.println("[SaveManager] Found empty save file for slot " + slotId + " - ignoring");
            return null;
        }
        
        try (BufferedReader r = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
            return readSaveFromReader(r);
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
        } catch (Exception ignore) {
        }
        
        saveWorldToSlotUser(w, slotId, slotName);
    }
    
    public void saveWorldToSlotUser(World w, int slotId, String name) throws IOException {
        if (w == null) return;
        
        String saveName = (name != null && !name.isEmpty()) ? name : ("Save " + slotId);
        Savefile save = new Savefile(slotId, saveName);
        populateSavefileFromWorld(save, w);
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
                saveWorldToSlotUser(w, slotId, name); 
                if (onComplete != null) SwingUtilities.invokeLater(onComplete);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private int computePlayTime(int minute, int hour, int day, int month, int year) {
        int days = day + (month * 30) + (year * 12 * 30);
        return (days * 24 * 60) + (hour * 60) + minute;
    }

    private int computePlayTime(World w) {
        if (w == null) return 0;
        return computePlayTime(w.getMinute(), w.getHour(), w.getDay(), w.getMonth(), w.getYear());
    }
    
    private int computePlayTimeFromSave(Savefile s) {
        if (s == null) return 0;
        return computePlayTime(s.getMinute(), s.getHour(), s.getDay(), s.getMonth(), s.getYear());
    }

    public void saveAutosaveAsync(World w, Runnable onComplete) {
        executor.submit(() -> {
            saveAutosave(w); 
            if (onComplete != null) SwingUtilities.invokeLater(onComplete);
        });
    }
    
    private void populateSavefileFromWorld(Savefile save, World w) {
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
                    save.setEggs(c.getEggs() != null ? c.getEggs().size() : 0);
                    save.setWorkers(c.getWorkers() != null ? c.getWorkers().size() : 0);
                    save.setSoldiers(c.getSoldiers() != null ? c.getSoldiers().size() : 0);
                    save.setQueens(c.getQueens() != null ? c.getQueens().size() : 0);
                    save.setMushrooms(c.getMushrooms());
                    save.setMushroomsCapacity(c.getMushroomsCapacity());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void writeSaveToWriter(Savefile s, BufferedWriter w) throws IOException {
        Map<String, String> m = new HashMap<>();
        m.put("id", Integer.toString(s.getId()));
        m.put("name", s.getName() != null ? URLEncoder.encode(s.getName(), StandardCharsets.UTF_8.name()) : "");
        m.put("progress", Float.toString(s.getProgress()));
        m.put("playTime", Integer.toString(s.getPlayTime()));
        m.put("minute", Integer.toString(s.getMinute()));
        m.put("hour", Integer.toString(s.getHour()));
        m.put("day", Integer.toString(s.getDay()));
        m.put("month", Integer.toString(s.getMonth()));
        m.put("year", Integer.toString(s.getYear()));
        m.put("totalAnts", Integer.toString(s.getTotalAnts()));
        m.put("eggs", Integer.toString(s.getEggs()));
        m.put("workers", Integer.toString(s.getWorkers()));
        m.put("soldiers", Integer.toString(s.getSoldiers()));
        m.put("queens", Integer.toString(s.getQueens()));
        m.put("mushrooms", Integer.toString(s.getMushrooms()));
        m.put("mushroomsCapacity", Integer.toString(s.getMushroomsCapacity()));
        
        for (Map.Entry<String,String> e : m.entrySet()) {
            w.write(e.getKey());
            w.write('=');
            w.write(e.getValue() != null ? e.getValue() : "");
            w.newLine();
        }
    }

    private Savefile readSaveFromReader(BufferedReader r) throws IOException {
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
            try { 
                name = URLDecoder.decode(name, StandardCharsets.UTF_8.name()); 
            } catch (Exception ignore) {}
            
            s = new Savefile(id, name);
            s.setProgress(Float.parseFloat(m.getOrDefault("progress", "0")));
            s.setPlayTime(Integer.parseInt(m.getOrDefault("playTime", "0")));
            s.setMinute(Integer.parseInt(m.getOrDefault("minute", "0")));
            s.setHour(Integer.parseInt(m.getOrDefault("hour", "0")));
            s.setDay(Integer.parseInt(m.getOrDefault("day", "0")));
            s.setMonth(Integer.parseInt(m.getOrDefault("month", "0")));
            s.setYear(Integer.parseInt(m.getOrDefault("year", "0")));
            s.setTotalAnts(Integer.parseInt(m.getOrDefault("totalAnts", "0")));
            s.setEggs(Integer.parseInt(m.getOrDefault("eggs", "0")));
            s.setWorkers(Integer.parseInt(m.getOrDefault("workers", "0")));
            s.setSoldiers(Integer.parseInt(m.getOrDefault("soldiers", "0")));
            s.setQueens(Integer.parseInt(m.getOrDefault("queens", "0")));
            s.setMushrooms(Integer.parseInt(m.getOrDefault("mushrooms", "0")));
            s.setMushroomsCapacity(Integer.parseInt(m.getOrDefault("mushroomsCapacity", "0")));
        }
        return s;
    }
}