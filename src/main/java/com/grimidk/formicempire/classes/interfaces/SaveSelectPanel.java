package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

public class SaveSelectPanel extends JPanel {
    private final MainFrame frame;
    private final SaveManager saveManager;

    private final JButton[] slotButtons = new JButton[3];
    private final JButton[] deleteButtons = new JButton[3];
    private final JLabel[] slotLabels = new JLabel[3];
    
    private final Savefile[] cachedSaves = new Savefile[3];

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
            
            setupNavigation(slotButtons[i]);
            setupNavigation(deleteButtons[i]);

            int idx = i;
            slotButtons[i].addActionListener(e -> onCreateOrLoad(slotId, idx));
            deleteButtons[i].addActionListener(e -> onDelete(slotId, idx));
            c.gridx = 0; c.gridy = i; add(slotLabels[i], c);
            c.gridx = 1; add(slotButtons[i], c);
            c.gridx = 2; add(deleteButtons[i], c);
        }

        JButton back = new JButton("Back");
        setupNavigation(back);
        back.addActionListener(e -> {
            frame.showCard(MainFrame.CARD_INIT);
        });

        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; add(back, c);

        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                if (slotButtons[0] != null) {
                    slotButtons[0].requestFocusInWindow();
                }
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        });
    }
    
    private void setupNavigation(JButton button) {
        button.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "pressed");
        button.getActionMap().put("pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                button.doClick();
            }
        });

        Set<AWTKeyStroke> forwardKeys = new HashSet<>(button.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        forwardKeys.add(KeyStroke.getKeyStroke("DOWN"));
        forwardKeys.add(KeyStroke.getKeyStroke("RIGHT"));
        button.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);

        Set<AWTKeyStroke> backwardKeys = new HashSet<>(button.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        backwardKeys.add(KeyStroke.getKeyStroke("UP"));
        backwardKeys.add(KeyStroke.getKeyStroke("LEFT"));
        button.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardKeys);
    }

    public void refreshSlots() {
        for (int i = 0; i < 3; i++) {
            Savefile s = saveManager.loadSlot(i + 1);
            cachedSaves[i] = s;

            if (s == null) {
                slotLabels[i].setText("Empty slot");
                slotButtons[i].setText("Create");
                deleteButtons[i].setVisible(false);
            } else {
                int totalDays = (s.getDay() - 1) + ((s.getMonth() - 1) * 30) + (s.getYear() * 12 * 30);
                slotLabels[i].setText(s.getName() + " — " + totalDays + " days");
                slotButtons[i].setText("Load");
                deleteButtons[i].setVisible(true);
            }
        }
    }

    private void onCreateOrLoad(int slotId, int idx) {
        Savefile existing = cachedSaves[idx];
        
        if (existing == null) {
            String name = JOptionPane.showInputDialog(this, "Enter save name:", "Create Save", JOptionPane.PLAIN_MESSAGE);
            if (name == null || name.trim().isEmpty()) return;
            
            Savefile save = new Savefile(slotId, name.trim());            
            saveManager.saveUserSlotAsync(save, () -> {
                refreshSlots(); 

                Savefile newSave = cachedSaves[idx];
                if (newSave != null) {
                    HelpPanel.showTutorialDialog(frame);
                    frame.openGameWithSave(newSave); 
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to create new save file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        } else {
            frame.openGameWithSave(existing);
        }
    }

    private void onDelete(int slotId, int idx) {
        int res = JOptionPane.showConfirmDialog(this, "Delete save in slot " + slotId + "?", "Delete Save", JOptionPane.YES_NO_OPTION);
        if (res != JOptionPane.YES_OPTION) return;
        boolean ok = saveManager.deleteSlot(slotId);
        if (!ok) JOptionPane.showMessageDialog(this, "Failed to delete save (file may not exist).");
        refreshSlots();
    }
}