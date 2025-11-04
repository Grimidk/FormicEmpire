package com.grimidk.formicempire.classes.infrasctructure;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;

import com.grimidk.formicempire.classes.constants.AntRole;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.World;

import java.util.HashMap;
import java.util.Map;
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
            default:
                fileName = "slot" + slotId + "." + type + ".json";
        }
        return new File(savesDir, fileName);
    }

    private File getSettingsFile() {
        return new File(savesDir, "settings.json");
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

    public void saveAutosave(World w, Engine e) {
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
        
        if (slotId == 0) {
            System.out.println("[SaveManager] Skipping autosave for new game (Slot ID 0).");
            return;
        }

        String nameToUse = "Autosave"; 
        File manualFile = getSlotFile(slotId, "manual");
        if (manualFile.exists() && manualFile.length() > 0) {
            try (BufferedReader r = Files.newBufferedReader(manualFile.toPath(), StandardCharsets.UTF_8)) {
                Savefile existingManual = readSaveFromReader(r);
                if (existingManual != null && existingManual.getName() != null && !existingManual.getName().trim().isEmpty()) {
                    nameToUse = existingManual.getName(); 
                }
            } catch (Exception ignore) {}
        }

        File f = getSlotFile(slotId, "autosave");
        Savefile save = new Savefile(slotId, nameToUse);
        populateSavefileFromGame(save, w);

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
            
            System.out.println("[SaveManager] Autosaved world to " + f.getAbsolutePath());
        
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Savefile loadSlot(int slotId) {
        File manual = getSlotFile(slotId, "manual");
        File autos = getSlotFile(slotId, "autosave");

        File f = null;
        if (manual.exists()) f = manual;
        else if (autos.exists()) f = autos;

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
        populateSavefileFromGame(save, w);
        writeManualSave(save);
    }

    public void saveWorldToSlotUserAsync(World w, Engine e, int slotId, Runnable onComplete) {
        executor.submit(() -> {
            try {
                saveWorldToSlotUser(w, slotId); 
                if (onComplete != null) SwingUtilities.invokeLater(onComplete);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void saveWorldToSlotUserAsync(World w, Engine e, int slotId, String name, Runnable onComplete) {
        executor.submit(() -> {
            try {
                saveWorldToSlotUser(w, slotId, name); 
                if (onComplete != null) SwingUtilities.invokeLater(onComplete);
            } catch (Exception err) {
                err.printStackTrace();
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

    public void saveAutosaveAsync(World w, Engine e, Runnable onComplete) {
        executor.submit(() -> {
            saveAutosave(w, e); 
            if (onComplete != null) SwingUtilities.invokeLater(onComplete);
        });
    }
    
    private void populateSavefileFromGame(Savefile save, World w) {
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
                    save.setDeadAnts(c.getDeadAnts() != null ? c.getDeadAnts().size() : 0);
                    save.setEggs(c.getEggs() != null ? c.getEggs().size() : 0);
                    save.setLarvae(c.getLarvae() != null ? c.getLarvae().size() : 0);
                    save.setPupae(c.getPupae() != null ? c.getPupae().size() : 0);
                    save.setWorkers(c.getWorkers() != null ? c.getWorkers().size() : 0);
                    save.setSoldiers(c.getSoldiers() != null ? c.getSoldiers().size() : 0);
                    save.setMajors(c.getMajors() != null ? c.getMajors().size() : 0);
                    save.setDrones(c.getDrones() != null ? c.getDrones().size() : 0);
                    save.setPrincesses(c.getPrincesses() != null ? c.getPrincesses().size() : 0);
                    save.setQueens(c.getQueens() != null ? c.getQueens().size() : 0);
                    save.setMushrooms(c.getMushrooms() );
                    save.setPlants(c.getPlants());
                    save.setProtein(c.getProtein());
                    save.setWater(c.getWater());
                    save.setSyrups(c.getSyrups());
                    save.setResins(c.getResins());
                    save.setMinerals(c.getMinerals());

                    Map<String, Integer> rolesToSave = new HashMap<>();
                    for (Map.Entry<AntRole, Integer> entry : c.getAssignedRoleCounts().entrySet()) {
                        rolesToSave.put(entry.getKey().getName(), entry.getValue());
                    }
                    save.setAssignedRoleCounts(rolesToSave);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void writeJsonLine(BufferedWriter w, String key, Object value, boolean last) throws IOException {
        w.write("  \"");
        w.write(escapeJsonString(key));
        w.write("\": ");

        if (value instanceof String) {
            w.write("\"");
            w.write(escapeJsonString((String) value));
            w.write("\"");
        } else if (value instanceof Number) {
            w.write(value.toString());
        } else if (value instanceof Boolean) {
            w.write(value.toString());
        } else {
            w.write("null");
        }

        if (!last) {
            w.write(",");
        }
        w.newLine();
    }

    private void writeSaveToWriter(Savefile s, BufferedWriter w) throws IOException {
        w.write("{");
        w.newLine();

        // General
        writeJsonLine(w, "id", s.getId(), false);
        writeJsonLine(w, "name", s.getName() != null ? s.getName() : "", false);
        writeJsonLine(w, "progress", s.getProgress(), false);
        
        // Time
        writeJsonLine(w, "playTime", s.getPlayTime(), false);
        writeJsonLine(w, "minute", s.getMinute(), false);
        writeJsonLine(w, "hour", s.getHour(), false);
        writeJsonLine(w, "day", s.getDay(), false);
        writeJsonLine(w, "month", s.getMonth(), false);
        writeJsonLine(w, "year", s.getYear(), false);

        // Ant Counts
        writeJsonLine(w, "totalAnts", s.getTotalAnts(), false);
        writeJsonLine(w, "deadAnts", s.getDeadAnts(), false);
        writeJsonLine(w, "eggs", s.getEggs(), false);
        writeJsonLine(w, "larvae", s.getLarvae(), false);
        writeJsonLine(w, "pupae", s.getPupae(), false);
        writeJsonLine(w, "workers", s.getWorkers(), false);
        writeJsonLine(w, "soldiers", s.getSoldiers(), false);
        writeJsonLine(w, "majors", s.getMajors(), false);
        writeJsonLine(w, "drones", s.getDrones(), false);
        writeJsonLine(w, "princesses", s.getPrincesses(), false);
        writeJsonLine(w, "queens", s.getQueens(), false);

        // Resources
        writeJsonLine(w, "plants", s.getPlants(), false);
        writeJsonLine(w, "mushrooms", s.getMushrooms(), false);
        writeJsonLine(w, "protein", s.getProtein(), false);
        writeJsonLine(w, "water", s.getWater(), false);
        writeJsonLine(w, "syrups", s.getSyrups(), false);
        writeJsonLine(w, "resins", s.getResins(), false);
        writeJsonLine(w, "minerals", s.getMinerals(), false);

        // Settings removed

        // Roles
        w.write("  \"assignedRoleCounts\": ");
        w.write(serializeMapToJson(s.getAssignedRoleCounts()));
        w.newLine();

        w.write("}");
        w.newLine();
    }

    private Savefile readSaveFromReader(BufferedReader r) throws IOException {
        Savefile s = null;
        String line;
        Map<String,String> m = new HashMap<>();
        
        while ((line = r.readLine()) != null) {
            line = line.trim();
            
            if (line.equals("{") || line.equals("}") || line.isEmpty()) {
                continue;
            }

            int idx = line.indexOf(':');
            if (idx <= 0) continue;
            
            String k = line.substring(0, idx).trim();
            if (k.startsWith("\"")) k = k.substring(1);
            if (k.endsWith("\"")) k = k.substring(0, k.length() - 1);

            String v = line.substring(idx + 1).trim();
            if (v.endsWith(",")) {
                v = v.substring(0, v.length() - 1);
            }
            
            if (v.startsWith("\"")) {
                v = v.substring(1);
                if (v.endsWith("\"")) {
                    v = v.substring(0, v.length() - 1);
                }
                if (k.equals("name")) {
                     v = unescapeJsonString(v);
                }
            } else if (v.equals("null")) {
                v = ""; 
            }
            
            m.put(k, v);
        }

        if (m.containsKey("id") && m.containsKey("name")) {
            int id = Integer.parseInt(m.getOrDefault("id", "0"));
            String name = m.getOrDefault("name", "");

            s = new Savefile(id, name);
            s.setProgress(Float.parseFloat(m.getOrDefault("progress", "0")));
            s.setPlayTime(Integer.parseInt(m.getOrDefault("playTime", "0")));
            s.setMinute(Integer.parseInt(m.getOrDefault("minute", "0")));
            s.setHour(Integer.parseInt(m.getOrDefault("hour", "0")));
            s.setDay(Integer.parseInt(m.getOrDefault("day", "0")));
            s.setMonth(Integer.parseInt(m.getOrDefault("month", "0")));
            s.setYear(Integer.parseInt(m.getOrDefault("year", "0")));
            s.setTotalAnts(Integer.parseInt(m.getOrDefault("totalAnts", "0")));
            s.setDeadAnts(Integer.parseInt(m.getOrDefault("deadAnts", "0")));
            s.setEggs(Integer.parseInt(m.getOrDefault("eggs", "0")));
            s.setLarvae(Integer.parseInt(m.getOrDefault("larvae", "0")));
            s.setPupae(Integer.parseInt(m.getOrDefault("pupae", "0")));
            s.setWorkers(Integer.parseInt(m.getOrDefault("workers", "0")));
            s.setSoldiers(Integer.parseInt(m.getOrDefault("soldiers", "0")));
            s.setMajors(Integer.parseInt(m.getOrDefault("majors", "0")));
            s.setDrones(Integer.parseInt(m.getOrDefault("drones", "0")));
            s.setPrincesses(Integer.parseInt(m.getOrDefault("princesses", "0")));
            s.setQueens(Integer.parseInt(m.getOrDefault("queens", "0")));
            s.setMushrooms(Integer.parseInt(m.getOrDefault("mushrooms", "0")));
            s.setPlants(Integer.parseInt(m.getOrDefault("plants", "0")));
            s.setProtein(Integer.parseInt(m.getOrDefault("protein", "0")));
            s.setWater(Integer.parseInt(m.getOrDefault("water", "0")));
            s.setSyrups(Integer.parseInt(m.getOrDefault("syrups", "0")));
            s.setResins(Integer.parseInt(m.getOrDefault("resins", "0")));
            s.setMinerals(Integer.parseInt(m.getOrDefault("minerals", "0")));

            // Settings removed
            
            String rolesJson = m.getOrDefault("assignedRoleCounts", "{}");
            if (rolesJson.startsWith("\"")) {
                rolesJson = unescapeJsonString(rolesJson.substring(1, rolesJson.length() - 1));
            }
            s.setAssignedRoleCounts(deserializeJsonToMap(rolesJson));
        }
        return s;
    }

    private String serializeMapToJson(Map<String, Integer> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int i = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            sb.append("\"");
            sb.append(escapeJsonString(entry.getKey()));
            sb.append("\":");
            sb.append(entry.getValue());
            if (i < map.size() - 1) {
                sb.append(",");
            }
            i++;
        }
        sb.append("}");
        return sb.toString();
    }
    
    private static final Pattern JSON_PAIR_PATTERN = Pattern.compile("\"([^\"]*)\":([0-9]+)");

    private Map<String, Integer> deserializeJsonToMap(String json) {
        Map<String, Integer> map = new HashMap<>();
        if (json == null || json.length() <= 2) {
            return map;
        }
        Matcher m = JSON_PAIR_PATTERN.matcher(json);
        while (m.find()) {
            try {
                String key = unescapeJsonString(m.group(1));
                int value = Integer.parseInt(m.group(2));
                map.put(key, value);
            } catch (Exception e) {
                System.err.println("Error parsing role map pair: " + m.group(0));
            }
        }
        return map;
    }


    private String escapeJsonString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private String unescapeJsonString(String str) {
        if (str == null) return null;
        return str.replace("\\\"", "\"")
                  .replace("\\\\", "\\");
    }

    // --- Settings Management ---

    public void saveSettings(Engine engine) {
        File f = getSettingsFile();
        File tmp = new File(f.getAbsolutePath() + ".tmp");

        try (BufferedWriter w = Files.newBufferedWriter(tmp.toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            w.write("{");
            w.newLine();
            writeJsonLine(w, "language", engine.getLanguage(), false);
            writeJsonLine(w, "allowTurboMode", engine.isAllowTurboMode(), false);
            writeJsonLine(w, "screenSize", engine.getScreenSize(), false);
            writeJsonLine(w, "fullScreen", engine.isFullScreen(), false);
            writeJsonLine(w, "autosaveFrequency", engine.getAutosaveFrequency(), true);
            w.write("}");
            w.newLine();
            w.flush();
        } catch (IOException e) {
            try { if (tmp.exists()) tmp.delete(); } catch (Exception ignore) {}
            e.printStackTrace();
            return;
        }

        try {
            Files.move(tmp.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException amnse) {
            try {
                Files.move(tmp.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            try { if (tmp.exists()) tmp.delete(); } catch (Exception ignore) {}
            e.printStackTrace();
        }
        System.out.println("[SaveManager] Saved global settings.");
    }

    public void loadSettings(Engine engine) {
        File f = getSettingsFile();
        if (!f.exists() || f.length() == 0) {
            System.out.println("[SaveManager] settings.json not found, using defaults.");
            return;
        }

        Map<String,String> m = new HashMap<>();
        try (BufferedReader r = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.equals("{") || line.equals("}") || line.isEmpty()) continue;

                int idx = line.indexOf(':');
                if (idx <= 0) continue;
                
                String k = line.substring(0, idx).trim();
                if (k.startsWith("\"")) k = k.substring(1);
                if (k.endsWith("\"")) k = k.substring(0, k.length() - 1);

                String v = line.substring(idx + 1).trim();
                if (v.endsWith(",")) v = v.substring(0, v.length() - 1);
                
                if (v.startsWith("\"")) {
                    v = v.substring(1);
                    if (v.endsWith("\"")) v = v.substring(0, v.length() - 1);
                } else if (v.equals("null")) {
                    v = ""; 
                }
                m.put(k, v);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            engine.setLanguage(m.getOrDefault("language", engine.getLanguage()));
            engine.setAllowTurboMode(Boolean.parseBoolean(m.getOrDefault("allowTurboMode", String.valueOf(engine.isAllowTurboMode()))));
            engine.setScreenSize(m.getOrDefault("screenSize", engine.getScreenSize()));
            engine.setFullScreen(Boolean.parseBoolean(m.getOrDefault("fullScreen", String.valueOf(engine.isFullScreen()))));
            engine.setAutosaveFrequency(Integer.parseInt(m.getOrDefault("autosaveFrequency", String.valueOf(engine.getAutosaveFrequency()))));
            System.out.println("[SaveManager] Global settings loaded.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[SaveManager] Error parsing settings.json, using defaults.");
        }
    }
}