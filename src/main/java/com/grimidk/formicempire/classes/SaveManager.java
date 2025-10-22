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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SaveManager {

    private final File savesDir;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "save-worker"));
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public SaveManager() {
        this.savesDir = new File("saves");
        if (!savesDir.exists()) {
            savesDir.mkdirs();
        }
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

    public void saveSlot(Savefile save) throws IOException {
        File f = new File(savesDir, "slot" + save.getId() + ".json");
        try (FileWriter fw = new FileWriter(f, StandardCharsets.UTF_8)) {
            gson.toJson(save, fw);
            System.out.println("[SaveManager] Saved slot " + save.getId() + " to " + f.getAbsolutePath());
        }
    }

    public boolean deleteSlot(int slotId) {
        File f = new File(savesDir, "slot" + slotId + ".json");
        if (f.exists()) {
            boolean ok = f.delete();
            if (ok) System.out.println("[SaveManager] Deleted save slot " + slotId + " (" + f.getAbsolutePath() + ")");
            else System.out.println("[SaveManager] Failed to delete save slot " + slotId + " (" + f.getAbsolutePath() + ")");
            return ok;
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
        try (FileWriter fw = new FileWriter(f, StandardCharsets.UTF_8)) {
            gson.toJson(save, fw);
            System.out.println("[SaveManager] Autosaved world to " + f.getAbsolutePath() + " (Day " + save.getDay() + " H" + save.getHour() + ")");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // If the player has a last-used slot, also persist this autosave into that slot so it's immediately accessible
        try {
            int last = getLastSlot();
            if (last > 0) {
                saveWorldToSlot(w, last);
                System.out.println("[SaveManager] Also copied autosave to slot " + last);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Savefile loadSlot(int slotId) {
        File f = new File(savesDir, "slot" + slotId + ".json");
        if (!f.exists()) return null;
        try (FileReader r = new FileReader(f, StandardCharsets.UTF_8)) {
            Savefile s = gson.fromJson(r, Savefile.class);
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
            saveSlot(save);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void saveWorldToLastSlot(World w) {
        int last = getLastSlot();
        if (last <= 0) return;
        saveWorldToSlot(w, last);
    }

    // Asynchronous helpers
    public void saveSlotAsync(Savefile save, Runnable onComplete) {
        executor.submit(() -> {
            try {
                saveSlot(save);
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
}
