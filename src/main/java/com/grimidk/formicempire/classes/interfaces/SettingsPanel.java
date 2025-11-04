package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.infrasctructure.Engine;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private final MainFrame frame;
    private final Engine engine;

    private JComboBox<String> languageCombo;
    private JCheckBox turboCheck;
    private JComboBox<String> sizeCombo;
    private JCheckBox fullScreenCheck;
    private JComboBox<AutosaveOption> autosaveCombo;

    private static class AutosaveOption {
        String label;
        int value;
        public AutosaveOption(String label, int value) {
            this.label = label;
            this.value = value;
        }
        @Override
        public String toString() { return label; }
    }

    public SettingsPanel(MainFrame frame) {
        this.frame = frame;
        this.engine = frame.getEngine();
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;

        // Language
        c.gridy = 0; c.gridx = 0; add(new JLabel("Language:"), c);
        languageCombo = new JComboBox<>(new String[]{"en", "es", "de"});
        c.gridx = 1; add(languageCombo, c);

        // Screen Size
        c.gridy = 1; c.gridx = 0; add(new JLabel("Screen Size:"), c);
        sizeCombo = new JComboBox<>(new String[]{"1000x700", "1280x720", "1600x900", "1920x1000"});
        sizeCombo.setEditable(true);
        c.gridx = 1; add(sizeCombo, c);

        // Full Screen
        c.gridy = 2; c.gridx = 0; add(new JLabel("Full Screen:"), c);
        fullScreenCheck = new JCheckBox();
        c.gridx = 1; add(fullScreenCheck, c);

        // Autosave Frequency
        c.gridy = 3; c.gridx = 0; add(new JLabel("Autosave Frequency:"), c);
        autosaveCombo = new JComboBox<>(new AutosaveOption[]{
                new AutosaveOption("Every Month", 1),
                new AutosaveOption("Every 3 Months", 3),
                new AutosaveOption("Every 6 Months", 6),
                new AutosaveOption("Every Year (12 Months)", 12),
                new AutosaveOption("Disabled", 0)
        });
        c.gridx = 1; add(autosaveCombo, c);
        
        // Turbo Mode
        c.gridy = 4; c.gridx = 0; add(new JLabel("Allow Turbo Mode:"), c);
        turboCheck = new JCheckBox();
        c.gridx = 1; add(turboCheck, c);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save & Apply");
        saveButton.addActionListener(e -> saveSettings());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> this.frame.showCard(MainFrame.CARD_INIT));
        
        buttonPanel.add(saveButton);
        buttonPanel.add(backButton);

        c.gridy = 5; c.gridx = 0; c.gridwidth = 2; c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, c);
    }

    public void loadSettings() {
        languageCombo.setSelectedItem(engine.getLanguage());
        turboCheck.setSelected(engine.isAllowTurboMode());
        sizeCombo.setSelectedItem(engine.getScreenSize());
        fullScreenCheck.setSelected(engine.isFullScreen());

        int freq = engine.getAutosaveFrequency();
        for (int i = 0; i < autosaveCombo.getItemCount(); i++) {
            if (autosaveCombo.getItemAt(i).value == freq) {
                autosaveCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void saveSettings() {
        engine.setLanguage((String) languageCombo.getSelectedItem());
        engine.setAllowTurboMode(turboCheck.isSelected());
        engine.setScreenSize((String) sizeCombo.getSelectedItem());
        engine.setFullScreen(fullScreenCheck.isSelected());
        
        AutosaveOption selectedFreq = (AutosaveOption) autosaveCombo.getSelectedItem();
        if (selectedFreq != null) {
            engine.setAutosaveFrequency(selectedFreq.value);
        }

        engine.saveGlobalSettings(); 
        frame.applyEngineSettings();
        JOptionPane.showMessageDialog(this, "Settings saved and applied.", "Settings", JOptionPane.INFORMATION_MESSAGE);
    }
}