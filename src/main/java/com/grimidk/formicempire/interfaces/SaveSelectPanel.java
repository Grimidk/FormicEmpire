package com.grimidk.formicempire.interfaces;

import com.grimidk.formicempire.classes.Savefile;
import com.grimidk.formicempire.classes.SaveManager;
import com.grimidk.formicempire.classes.Engine;
import com.grimidk.formicempire.classes.World;
import com.grimidk.formicempire.classes.Colony;

import javax.swing.*;
import java.awt.*;

public class SaveSelectPanel extends JPanel {
    private final MainFrame frame;
    private final SaveManager saveManager;

    private final JButton[] slotButtons = new JButton[3];
    private final JButton[] deleteButtons = new JButton[3];
    private final JLabel[] slotLabels = new JLabel[3];

    public SaveSelectPanel(MainFrame frame) {
        this.frame = frame;
        this.saveManager = new SaveManager();
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8,8,8,8);

        for (int i = 0; i < 3; i++) {
            int slotId = i + 1;
            slotLabels[i] = new JLabel("Empty slot");
            slotButtons[i] = new JButton("Create");
            deleteButtons[i] = new JButton("Delete");
            deleteButtons[i].setVisible(false);
            int idx = i;
            slotButtons[i].addActionListener(e -> onCreateOrLoad(slotId, idx));
            deleteButtons[i].addActionListener(e -> onDelete(slotId, idx));
            c.gridx = 0; c.gridy = i; add(slotLabels[i], c);
            c.gridx = 1; add(slotButtons[i], c);
            c.gridx = 2; add(deleteButtons[i], c);
        }

        JButton back = new JButton("Back");
        back.addActionListener(e -> {
            frame.showCard(MainFrame.CARD_INIT);
        });

        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; add(back, c);

        refreshSlots();
    }

    public void refreshSlots() {
        for (int i = 0; i < 3; i++) {
            Savefile s = saveManager.loadSlot(i + 1);
            if (s == null) {
                slotLabels[i].setText("Empty slot");
                slotButtons[i].setText("Create");
                deleteButtons[i].setVisible(false);
            } else {
                int totalDays = s.getDay() + (s.getMonth() * 30) + (s.getYear() * 12 * 30);
                slotLabels[i].setText(s.getName() + " — " + totalDays + " days");
                slotButtons[i].setText("Load");
                deleteButtons[i].setVisible(true);
            }
        }
    }

    private void onCreateOrLoad(int slotId, int idx) {
        Savefile existing = saveManager.loadSlot(slotId);
        if (existing == null) {
            String name = JOptionPane.showInputDialog(this, "Enter save name:", "Create Save", JOptionPane.PLAIN_MESSAGE);
            if (name == null || name.trim().isEmpty()) return;
            Savefile save = new Savefile(slotId, name.trim());
            captureGameStateIntoSave(save);
            saveManager.saveUserSlotAsync(save, () -> {
                JOptionPane.showMessageDialog(this, "Save completed in slot " + slotId, "Saved", JOptionPane.INFORMATION_MESSAGE);
                refreshSlots();
            });
        } else {
            frame.openGameWithSave(existing);
        }
        refreshSlots();
    }

    private void onDelete(int slotId, int idx) {
        int res = JOptionPane.showConfirmDialog(this, "Delete save in slot " + slotId + "?", "Delete Save", JOptionPane.YES_NO_OPTION);
        if (res != JOptionPane.YES_OPTION) return;
        boolean ok = saveManager.deleteSlot(slotId);
        if (!ok) JOptionPane.showMessageDialog(this, "Failed to delete save (file may not exist).");
        refreshSlots();
    }

    private void captureGameStateIntoSave(Savefile save) {
        Engine eng = frame.getEngine();
        if (eng == null) return;
        World w = eng.getWorld();
        if (w != null) {
            save.setMinute(w.getMinute());
            save.setHour(w.getHour());
            save.setDay(w.getDay());
            save.setMonth(w.getMonth());
            save.setYear(w.getYear());
            if (w.getHexes() != null && !w.getHexes().isEmpty()) {
                Colony c = w.getSpawnHex().getColony();
                if (c != null) {
                    save.setTotalAnts(c.getAntTotal());
                    save.setWorkers(c.getWorkers() != null ? c.getWorkers().size() : 0);
                    save.setSoldiers(c.getSoldiers() != null ? c.getSoldiers().size() : 0);
                    save.setQueens(c.getQueens() != null ? c.getQueens().size() : 0);
                }
            }
        }
    }
}
