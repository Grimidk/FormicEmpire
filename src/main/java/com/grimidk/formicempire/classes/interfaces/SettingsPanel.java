package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.repositories.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.ZeroDialog;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class SettingsPanel extends JPanel {
    private final MainFrame frame;
    private final Engine engine;

    private JTabbedPane tabbedPane;
    
    // --- General Tab ---
    private JComboBox<LanguageOption> languageCombo;
    private JComboBox<AutosaveOption> autosaveCombo;
    private JCheckBox turboCheck;
    private JCheckBox arachnophobiaCheck;
    private JCheckBox pauseFocusCheck;
    private JCheckBox confirmQuitCheck;
    private JCheckBox showTooltipsCheck;
    private JCheckBox fuzzParasitesCheck;
    
    // --- Video Tab ---
    private JComboBox<String> sizeCombo;
    private JCheckBox fullScreenCheck;
    private JCheckBox visualFiltersCheck;
    
    // --- Audio Tab ---
    private JSlider masterVolSlider;
    private JSlider musicVolSlider;
    private JSlider sfxVolSlider;
    
    private JLabel langLabel, autoLabel, turboLabel, arachLabel, pauseFocusLabel, confirmQuitLabel, tooltipsLabel, fuzzParasitesLabel;
    private JLabel sizeLabel, fsLabel, visualFiltersLabel;
    private JLabel masterLabel, musicLabel, sfxLabel;
    
    private JButton saveButton;
    private JButton backButton;

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
    
    private static class LanguageOption {
        String code;
        String name;
        public LanguageOption(String code, String name) {
            this.code = code;
            this.name = name;
        }
        @Override
        public String toString() { return name; }
    }

    public SettingsPanel(MainFrame frame) {
        this(frame, false);
    }

    public SettingsPanel(MainFrame frame, boolean isInDialog) {
        this.frame = frame;
        this.engine = frame.getEngine();
        setLayout(new BorderLayout());
        setBackground(AssetStyles.BACKGROUND_COLOR);
        
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(AssetStyles.FONT_BOLD);
        tabbedPane.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        tabbedPane.setForeground(AssetStyles.FONT_COLOR);
        
        initUI();
        
        add(tabbedPane, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        saveButton = new JButton();
        styleButton(saveButton);
        saveButton.addActionListener(e -> saveSettings());
        setupNavigation(saveButton);

        backButton = new JButton();
        styleButton(backButton);
        backButton.addActionListener(e -> {
            if (isInDialog) {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window instanceof JDialog) {
                    window.dispose();
                }
            } else {
                this.frame.showCard(MainFrame.CARD_INIT);
            }
        });
        setupNavigation(backButton);
        
        southPanel.add(saveButton);
        southPanel.add(backButton);
        add(southPanel, BorderLayout.SOUTH);
        
        refreshTranslations();
        
        LanguageStrings.addListener(this::refreshTranslations);

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
    
    private void initUI() {
        tabbedPane.removeAll();
        
        tabbedPane.addTab("General", createGeneralTab());
        tabbedPane.addTab("Video", createVideoTab());
        tabbedPane.addTab("Audio", createAudioTab());
    }
    
    private JPanel createGeneralTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 15, 5, 15);
        c.anchor = GridBagConstraints.WEST;

        // Language
        c.gridy = 0; c.gridx = 0; 
        langLabel = new JLabel();
        langLabel.setFont(AssetStyles.FONT_NORMAL);
        langLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(langLabel, c);
        
        List<String> codes = LanguageStrings.getAvailableLanguageCodes();
        LanguageOption[] options = new LanguageOption[codes.size()];
        for (int i = 0; i < codes.size(); i++) {
            options[i] = new LanguageOption(codes.get(i), LanguageStrings.getLanguageName(codes.get(i)));
        }
        languageCombo = new JComboBox<>(options);
        styleComboBox(languageCombo);
        c.gridx = 1; panel.add(languageCombo, c);

        // Autosave Frequency
        c.gridy = 1; c.gridx = 0; 
        autoLabel = new JLabel();
        autoLabel.setFont(AssetStyles.FONT_NORMAL);
        autoLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(autoLabel, c);
        
        autosaveCombo = new JComboBox<>();
        styleComboBox(autosaveCombo);
        c.gridx = 1; panel.add(autosaveCombo, c);
        
        // Turbo Mode
        c.gridy = 2; c.gridx = 0; 
        turboLabel = new JLabel();
        turboLabel.setFont(AssetStyles.FONT_NORMAL);
        turboLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(turboLabel, c);
        
        turboCheck = new JCheckBox();
        styleCheckBox(turboCheck);
        c.gridx = 1; panel.add(turboCheck, c);
        
        // Arachnophobia
        c.gridy = 3; c.gridx = 0;
        arachLabel = new JLabel();
        arachLabel.setFont(AssetStyles.FONT_NORMAL);
        arachLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(arachLabel, c);
        
        arachnophobiaCheck = new JCheckBox();
        styleCheckBox(arachnophobiaCheck);
        c.gridx = 1; panel.add(arachnophobiaCheck, c);
        
        // Pause on Focus Loss
        c.gridy = 4; c.gridx = 0;
        pauseFocusLabel = new JLabel();
        pauseFocusLabel.setFont(AssetStyles.FONT_NORMAL);
        pauseFocusLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(pauseFocusLabel, c);
        
        pauseFocusCheck = new JCheckBox();
        styleCheckBox(pauseFocusCheck);
        c.gridx = 1; panel.add(pauseFocusCheck, c);
        
        // Confirm on Quit
        c.gridy = 5; c.gridx = 0;
        confirmQuitLabel = new JLabel();
        confirmQuitLabel.setFont(AssetStyles.FONT_NORMAL);
        confirmQuitLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(confirmQuitLabel, c);
        
        confirmQuitCheck = new JCheckBox();
        styleCheckBox(confirmQuitCheck);
        c.gridx = 1; panel.add(confirmQuitCheck, c);
        
        // Show Tooltips
        c.gridy = 6; c.gridx = 0;
        tooltipsLabel = new JLabel();
        tooltipsLabel.setFont(AssetStyles.FONT_NORMAL);
        tooltipsLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(tooltipsLabel, c);
        
        showTooltipsCheck = new JCheckBox();
        styleCheckBox(showTooltipsCheck);
        c.gridx = 1; panel.add(showTooltipsCheck, c);
        
        // Fuzz Parasites
        c.gridy = 7; c.gridx = 0;
        fuzzParasitesLabel = new JLabel();
        fuzzParasitesLabel.setFont(AssetStyles.FONT_NORMAL);
        fuzzParasitesLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(fuzzParasitesLabel, c);
        
        fuzzParasitesCheck = new JCheckBox();
        styleCheckBox(fuzzParasitesCheck);
        c.gridx = 1; panel.add(fuzzParasitesCheck, c);
        
        return panel;
    }
    
    private JPanel createVideoTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 15, 10, 15);
        c.anchor = GridBagConstraints.WEST;

        // Screen Size
        c.gridy = 0; c.gridx = 0; 
        sizeLabel = new JLabel();
        sizeLabel.setFont(AssetStyles.FONT_NORMAL);
        sizeLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(sizeLabel, c);
        
        String[] commonResolutions = {"1000x700", "1280x720", "1366x768", "1440x900", "1600x900", "1920x1080", "2560x1440"};
        sizeCombo = new JComboBox<>(commonResolutions);
        styleComboBox(sizeCombo);
        sizeCombo.setEditable(false);
        c.gridx = 1; panel.add(sizeCombo, c);

        // Full Screen
        c.gridy = 1; c.gridx = 0; 
        fsLabel = new JLabel();
        fsLabel.setFont(AssetStyles.FONT_NORMAL);
        fsLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(fsLabel, c);
        
        fullScreenCheck = new JCheckBox();
        styleCheckBox(fullScreenCheck);
        c.gridx = 1; panel.add(fullScreenCheck, c);

        // Visual Filters
        c.gridy = 2; c.gridx = 0;
        visualFiltersLabel = new JLabel();
        visualFiltersLabel.setFont(AssetStyles.FONT_NORMAL);
        visualFiltersLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(visualFiltersLabel, c);
        
        visualFiltersCheck = new JCheckBox();
        styleCheckBox(visualFiltersCheck);
        c.gridx = 1; panel.add(visualFiltersCheck, c);
        
        return panel;
    }
    
    private JPanel createAudioTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 15, 10, 15);
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;

        // Master Volume
        c.gridy = 0; c.gridx = 0; 
        masterLabel = new JLabel();
        masterLabel.setFont(AssetStyles.FONT_NORMAL);
        masterLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(masterLabel, c);
        
        masterVolSlider = createVolumeSlider();
        c.gridx = 1; panel.add(masterVolSlider, c);

        // Music Volume
        c.gridy = 1; c.gridx = 0; 
        musicLabel = new JLabel();
        musicLabel.setFont(AssetStyles.FONT_NORMAL);
        musicLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(musicLabel, c);
        
        musicVolSlider = createVolumeSlider();
        c.gridx = 1; panel.add(musicVolSlider, c);

        // SFX Volume
        c.gridy = 2; c.gridx = 0; 
        sfxLabel = new JLabel();
        sfxLabel.setFont(AssetStyles.FONT_NORMAL);
        sfxLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(sfxLabel, c);
        
        sfxVolSlider = createVolumeSlider();
        c.gridx = 1; panel.add(sfxVolSlider, c);
        
        return panel;
    }
    
    private JSlider createVolumeSlider() {
        JSlider slider = new JSlider(0, 100);
        slider.setBackground(AssetStyles.BACKGROUND_COLOR);
        slider.setForeground(AssetStyles.FONT_COLOR_HEADER);
        slider.setMajorTickSpacing(20);
        slider.setPaintTicks(true);
        return slider;
    }
    
    public void refreshTranslations() {
        tabbedPane.setTitleAt(0, LanguageStrings.get(LanguageStrings.SETTINGS_TAB_GENERAL));
        tabbedPane.setTitleAt(1, LanguageStrings.get(LanguageStrings.SETTINGS_TAB_VIDEO));
        tabbedPane.setTitleAt(2, LanguageStrings.get(LanguageStrings.SETTINGS_TAB_AUDIO));
        
        langLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_LANGUAGE));
        autoLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_AUTOSAVE));
        turboLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_TURBO));
        arachLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_ARACHNOPHOBIA));
        pauseFocusLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_PAUSE_FOCUS));
        confirmQuitLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_CONFIRM_QUIT));
        tooltipsLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_SHOW_TOOLTIPS));
        fuzzParasitesLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_FUZZ_PARASITES));
        
        sizeLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_SCREEN_SIZE));
        fsLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_FULLSCREEN));
        visualFiltersLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_VISUAL_FILTERS));
        
        masterLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_MASTER_VOL));
        musicLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_MUSIC_VOL));
        sfxLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_SFX_VOL));
        
        saveButton.setText(LanguageStrings.get(LanguageStrings.SETTINGS_SAVE_APPLY));
        backButton.setText(LanguageStrings.get(LanguageStrings.UI_BACK));
        
        // Refresh autosave combo options
        int currentFreq = (autosaveCombo.getSelectedItem() != null) ? ((AutosaveOption)autosaveCombo.getSelectedItem()).value : 0;
        autosaveCombo.setModel(new DefaultComboBoxModel<>(new AutosaveOption[]{
                new AutosaveOption(LanguageStrings.get(LanguageStrings.SETTINGS_EVERY_MONTH), 1),
                new AutosaveOption(LanguageStrings.get(LanguageStrings.SETTINGS_EVERY_3_MONTHS), 3),
                new AutosaveOption(LanguageStrings.get(LanguageStrings.SETTINGS_EVERY_6_MONTHS), 6),
                new AutosaveOption(LanguageStrings.get(LanguageStrings.SETTINGS_EVERY_YEAR), 12),
                new AutosaveOption(LanguageStrings.get(LanguageStrings.UI_DISABLED), 0)
        }));
        for (int i = 0; i < autosaveCombo.getItemCount(); i++) {
            if (autosaveCombo.getItemAt(i).value == currentFreq) {
                autosaveCombo.setSelectedIndex(i);
                break;
            }
        }
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
        String currentLang = engine.getLanguage();
        for (int i = 0; i < languageCombo.getItemCount(); i++) {
            if (languageCombo.getItemAt(i).code.equals(currentLang)) {
                languageCombo.setSelectedIndex(i);
                break;
            }
        }
        
        int freq = engine.getAutosaveFrequency();
        for (int i = 0; i < autosaveCombo.getItemCount(); i++) {
            if (autosaveCombo.getItemAt(i).value == freq) {
                autosaveCombo.setSelectedIndex(i);
                break;
            }
        }
        
        turboCheck.setSelected(engine.isAllowTurboMode());
        arachnophobiaCheck.setSelected(engine.isArachnophobiaMode());
        pauseFocusCheck.setSelected(engine.isPauseOnFocusLoss());
        confirmQuitCheck.setSelected(engine.isConfirmOnQuit());
        showTooltipsCheck.setSelected(engine.isShowTooltips());
        fuzzParasitesCheck.setSelected(engine.isFuzzParasites());
        
        sizeCombo.setSelectedItem(engine.getScreenSize());
        fullScreenCheck.setSelected(engine.isFullScreen());
        visualFiltersCheck.setSelected(engine.isVisualFiltersEnabled());
        
        masterVolSlider.setValue(engine.getMasterVolume());
        musicVolSlider.setValue(engine.getMusicVolume());
        sfxVolSlider.setValue(engine.getSfxVolume());
    }

    private void saveSettings() {
        LanguageOption selectedLang = (LanguageOption) languageCombo.getSelectedItem();
        if (selectedLang != null) {
            engine.setLanguage(selectedLang.code);
            LanguageStrings.setLanguage(selectedLang.code); 
        }
        
        AutosaveOption selectedFreq = (AutosaveOption) autosaveCombo.getSelectedItem();
        if (selectedFreq != null) {
            engine.setAutosaveFrequency(selectedFreq.value);
        }
        
        engine.setAllowTurboMode(turboCheck.isSelected());
        engine.setArachnophobiaMode(arachnophobiaCheck.isSelected());
        engine.setPauseOnFocusLoss(pauseFocusCheck.isSelected());
        engine.setConfirmOnQuit(confirmQuitCheck.isSelected());
        engine.setShowTooltips(showTooltipsCheck.isSelected());
        engine.setFuzzParasites(fuzzParasitesCheck.isSelected());
        
        engine.setScreenSize((String) sizeCombo.getSelectedItem());
        engine.setFullScreen(fullScreenCheck.isSelected());
        engine.setVisualFiltersEnabled(visualFiltersCheck.isSelected());
        
        engine.setMasterVolume(masterVolSlider.getValue());
        engine.setMusicVolume(musicVolSlider.getValue());
        engine.setSfxVolume(sfxVolSlider.getValue());

        engine.saveGlobalSettings(); 
        frame.applyEngineSettings();
        
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, LanguageStrings.get(LanguageStrings.SETTINGS_SAVED_MSG), LanguageStrings.get(LanguageStrings.UI_SETTINGS), JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public static class SettingsDialog extends ZeroDialog {
        private final SettingsPanel settingsPanel;

        public SettingsDialog(MainFrame frame, Engine engine) {
            super(frame, LanguageStrings.UI_SETTINGS, AssetStyles.DEFAULT_DIALOG_SIZE);
            
            setLayout(new BorderLayout());
            
            settingsPanel = new SettingsPanel(frame, true);
            settingsPanel.loadSettings();
            
            add(settingsPanel, BorderLayout.CENTER);
        }

        @Override
        protected void refreshDialog() {
            settingsPanel.refreshTranslations();
        }
    }
}
