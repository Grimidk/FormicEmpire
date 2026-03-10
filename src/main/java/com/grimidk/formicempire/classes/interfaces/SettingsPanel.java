package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.repositories.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

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
        setBackground(AssetStyles.BACKGROUND_COLOR);
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;

        // Language
        c.gridy = 0; c.gridx = 0; 
        JLabel langLabel = new JLabel(LanguageStrings.SETTINGS_LANGUAGE);
        langLabel.setFont(AssetStyles.FONT_NORMAL);
        langLabel.setForeground(AssetStyles.FONT_COLOR);
        add(langLabel, c);
        
        languageCombo = new JComboBox<>(new String[]{"en", "es", "de"});
        styleComboBox(languageCombo);
        c.gridx = 1; add(languageCombo, c);

        // Screen Size
        c.gridy = 1; c.gridx = 0; 
        JLabel sizeLabel = new JLabel(LanguageStrings.SETTINGS_SCREEN_SIZE);
        sizeLabel.setFont(AssetStyles.FONT_NORMAL);
        sizeLabel.setForeground(AssetStyles.FONT_COLOR);
        add(sizeLabel, c);
        
        sizeCombo = new JComboBox<>(new String[]{"1000x700", "1280x720", "1600x900", "1920x1000"});
        styleComboBox(sizeCombo);
        sizeCombo.setEditable(true);
        c.gridx = 1; add(sizeCombo, c);

        // Full Screen
        c.gridy = 2; c.gridx = 0; 
        JLabel fsLabel = new JLabel(LanguageStrings.SETTINGS_FULLSCREEN);
        fsLabel.setFont(AssetStyles.FONT_NORMAL);
        fsLabel.setForeground(AssetStyles.FONT_COLOR);
        add(fsLabel, c);
        
        fullScreenCheck = new JCheckBox();
        styleCheckBox(fullScreenCheck);
        setupNavigation(fullScreenCheck);
        c.gridx = 1; add(fullScreenCheck, c);

        // Autosave Frequency
        c.gridy = 3; c.gridx = 0; 
        JLabel autoLabel = new JLabel(LanguageStrings.SETTINGS_AUTOSAVE);
        autoLabel.setFont(AssetStyles.FONT_NORMAL);
        autoLabel.setForeground(AssetStyles.FONT_COLOR);
        add(autoLabel, c);
        
        autosaveCombo = new JComboBox<>(new AutosaveOption[]{
                new AutosaveOption(LanguageStrings.SETTINGS_EVERY_MONTH, 1),
                new AutosaveOption(LanguageStrings.SETTINGS_EVERY_3_MONTHS, 3),
                new AutosaveOption(LanguageStrings.SETTINGS_EVERY_6_MONTHS, 6),
                new AutosaveOption(LanguageStrings.SETTINGS_EVERY_YEAR, 12),
                new AutosaveOption(LanguageStrings.UI_DISABLED, 0)
        });
        styleComboBox(autosaveCombo);
        c.gridx = 1; add(autosaveCombo, c);
        
        // Turbo Mode
        c.gridy = 4; c.gridx = 0; 
        JLabel turboLabel = new JLabel(LanguageStrings.SETTINGS_TURBO);
        turboLabel.setFont(AssetStyles.FONT_NORMAL);
        turboLabel.setForeground(AssetStyles.FONT_COLOR);
        add(turboLabel, c);
        
        turboCheck = new JCheckBox();
        styleCheckBox(turboCheck);
        setupNavigation(turboCheck);
        c.gridx = 1; add(turboCheck, c);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JButton saveButton = new JButton(LanguageStrings.SETTINGS_SAVE_APPLY);
        styleButton(saveButton);
        saveButton.addActionListener(e -> saveSettings());
        setupNavigation(saveButton);

        JButton backButton = new JButton(LanguageStrings.UI_BACK);
        styleButton(backButton);
        backButton.addActionListener(e -> this.frame.showCard(MainFrame.CARD_INIT));
        setupNavigation(backButton);
        
        buttonPanel.add(saveButton);
        buttonPanel.add(backButton);

        c.gridy = 5; c.gridx = 0; c.gridwidth = 2; c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, c);
        
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                backButton.requestFocusInWindow();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        });
    }
    
    private void styleComboBox(JComboBox<?> box) {
        box.setFont(AssetStyles.FONT_NORMAL);
        box.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        box.setForeground(AssetStyles.FONT_COLOR);
    }
    
    private void styleCheckBox(JCheckBox box) {
        box.setBackground(AssetStyles.BACKGROUND_COLOR);
        box.setForeground(AssetStyles.FONT_COLOR);
        box.setFont(AssetStyles.FONT_NORMAL);
    }
    
    private void styleButton(JButton btn) {
        btn.setFont(AssetStyles.FONT_BOLD);
        btn.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        btn.setForeground(AssetStyles.FONT_COLOR);
    }
    
    private void setupNavigation(JComponent component) {
        if (component instanceof JButton) {
            component.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "pressed");
            component.getActionMap().put("pressed", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ((JButton)component).doClick();
                }
            });
        } else if (component instanceof JCheckBox) {
            component.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "pressed");
            component.getActionMap().put("pressed", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ((JCheckBox)component).doClick();
                }
            });
        }

        Set<AWTKeyStroke> forwardKeys = new HashSet<>(component.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        forwardKeys.add(KeyStroke.getKeyStroke("DOWN"));
        forwardKeys.add(KeyStroke.getKeyStroke("RIGHT"));
        component.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);

        Set<AWTKeyStroke> backwardKeys = new HashSet<>(component.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        backwardKeys.add(KeyStroke.getKeyStroke("UP"));
        backwardKeys.add(KeyStroke.getKeyStroke("LEFT"));
        component.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardKeys);
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
        
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, LanguageStrings.SETTINGS_SAVED_MSG, LanguageStrings.UI_SETTINGS, JOptionPane.INFORMATION_MESSAGE);
        });
    }
}