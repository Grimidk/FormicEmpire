package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiOptionPane;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.ZeroDialog;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class SettingsPanel extends JPanel {
    private static final String SECTION_GENERAL = "general";
    private static final String SECTION_VIDEO = "video";
    private static final String SECTION_AUDIO = "audio";
    private static final String SECTION_ROLES = "roles";

    private final MainFrame frame;
    private final Engine engine;
    private final boolean inDialog;

    private final CardLayout sectionLayout = new CardLayout();
    private final JPanel sectionCards = new JPanel(sectionLayout);
    private final JPanel sectionTabs = new JPanel();
    private JButton generalTabButton;
    private JButton videoTabButton;
    private JButton audioTabButton;
    private JButton rolesTabButton;
    private String selectedSection = SECTION_GENERAL;
    
    // --- General Tab ---
    private JComboBox<LanguageOption> languageCombo;
    private JComboBox<AutosaveOption> autosaveCombo;
    private JCheckBox turboCheck;
    private JCheckBox arachnophobiaCheck;
    private JCheckBox pauseFocusCheck;
    private JCheckBox confirmQuitCheck;
    private JCheckBox escapeKeyGameActionsCheck;
    private JCheckBox showTooltipsCheck;
    private JCheckBox fuzzParasiteAntsCheck;
    private JCheckBox showAuditMenuCheck;
    private JCheckBox overworldAutoRecenterCheck;
    
    // --- Video Tab ---
    private JComboBox<String> sizeCombo;
    private JCheckBox fullScreenCheck;
    private JCheckBox daylightColorOverlayCheck;
    private JCheckBox weatherColorOverlayCheck;
    private JCheckBox darkModeCheck;
    private JComboBox<FrameRateOption> frameRateCombo;

    // --- Audio Tab ---
    private JSlider masterVolSlider;
    private JSlider musicVolSlider;
    private JSlider sfxVolSlider;
    
    private JLabel langLabel, autoLabel, turboLabel, arachLabel, pauseFocusLabel, confirmQuitLabel, escapeKeyGameActionsLabel, tooltipsLabel, overworldAutoRecenterLabel, fuzzParasiteAntsLabel, showAuditMenuLabel;
    private JLabel sizeLabel, fsLabel, daylightColorOverlayLabel, weatherColorOverlayLabel, darkModeLabel, frameRateLabel;
    private JLabel masterLabel, musicLabel, sfxLabel;
    private JLabel defaultRoleWorkerLabel, defaultRoleSoldierLabel, defaultRoleMajorLabel, defaultRolePrincessLabel, defaultRoleQueenLabel;
    private JComboBox<AntRole> defaultRoleWorkerCombo, defaultRoleSoldierCombo, defaultRoleMajorCombo, defaultRolePrincessCombo, defaultRoleQueenCombo;
    
    private JButton saveButton;
    private JButton backButton;
    private JButton resetGeneralButton;
    private JButton resetVideoButton;
    private JButton resetAudioButton;
    private JButton resetRolesButton;
    private boolean loadedFullScreen;
    private String loadedScreenSize;

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

    private static class FrameRateOption {
        String label;
        int hz;
        public FrameRateOption(String label, int hz) {
            this.label = label;
            this.hz = hz;
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
        this.inDialog = isInDialog;
        setLayout(new BorderLayout());
        setBackground(AssetStyles.BACKGROUND_COLOR);

        sectionTabs.setLayout(new java.awt.GridLayout(1, 4, -1, 0));
        sectionTabs.setBackground(AssetStyles.BACKGROUND_DARK);
        sectionCards.setBackground(AssetStyles.BACKGROUND_COLOR);

        initUI();

        generalTabButton = createSectionTab(SECTION_GENERAL);
        videoTabButton = createSectionTab(SECTION_VIDEO);
        audioTabButton = createSectionTab(SECTION_AUDIO);
        rolesTabButton = createSectionTab(SECTION_ROLES);
        sectionTabs.add(generalTabButton);
        sectionTabs.add(videoTabButton);
        sectionTabs.add(audioTabButton);
        sectionTabs.add(rolesTabButton);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(AssetStyles.BACKGROUND_COLOR);
        body.add(sectionTabs, BorderLayout.NORTH);
        body.add(sectionCards, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        showSection(SECTION_GENERAL);
        
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
                this.frame.showCard(this.frame.getMenuReturnCard());
            }
        });
        setupNavigation(backButton);
        
        southPanel.add(saveButton);
        southPanel.add(backButton);
        add(southPanel, BorderLayout.SOUTH);
        
        refreshTranslations();

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
        sectionCards.removeAll();

        sectionCards.add(createGeneralTab(), SECTION_GENERAL);
        sectionCards.add(createVideoTab(), SECTION_VIDEO);
        sectionCards.add(createAudioTab(), SECTION_AUDIO);
        sectionCards.add(createRolesTab(), SECTION_ROLES);
    }

    private JButton createSectionTab(String sectionId) {
        JButton button = new JButton();
        AssetStyles.styleSectionTabButton(button);
        button.addActionListener(e -> showSection(sectionId));
        setupNavigation(button);
        return button;
    }

    private void showSection(String sectionId) {
        selectedSection = sectionId;
        sectionLayout.show(sectionCards, sectionId);
        styleSectionTab(generalTabButton, SECTION_GENERAL.equals(sectionId));
        styleSectionTab(videoTabButton, SECTION_VIDEO.equals(sectionId));
        styleSectionTab(audioTabButton, SECTION_AUDIO.equals(sectionId));
        styleSectionTab(rolesTabButton, SECTION_ROLES.equals(sectionId));
    }

    private void styleSectionTab(JButton button, boolean selected) {
        AssetStyles.applyTabSelection(button, selected);
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

        // Escape key: close dialogs / open menu
        c.gridy = 6; c.gridx = 0;
        escapeKeyGameActionsLabel = new JLabel();
        escapeKeyGameActionsLabel.setFont(AssetStyles.FONT_NORMAL);
        escapeKeyGameActionsLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(escapeKeyGameActionsLabel, c);

        escapeKeyGameActionsCheck = new JCheckBox();
        styleCheckBox(escapeKeyGameActionsCheck);
        c.gridx = 1; panel.add(escapeKeyGameActionsCheck, c);
        
        // Show Tooltips
        c.gridy = 7; c.gridx = 0;
        tooltipsLabel = new JLabel();
        tooltipsLabel.setFont(AssetStyles.FONT_NORMAL);
        tooltipsLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(tooltipsLabel, c);
        
        showTooltipsCheck = new JCheckBox();
        styleCheckBox(showTooltipsCheck);
        c.gridx = 1; panel.add(showTooltipsCheck, c);
        
        // Overworld auto-recenter
        c.gridy = 8; c.gridx = 0;
        overworldAutoRecenterLabel = new JLabel();
        overworldAutoRecenterLabel.setFont(AssetStyles.FONT_NORMAL);
        overworldAutoRecenterLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(overworldAutoRecenterLabel, c);

        overworldAutoRecenterCheck = new JCheckBox();
        styleCheckBox(overworldAutoRecenterCheck);
        c.gridx = 1; panel.add(overworldAutoRecenterCheck, c);

        // Fuzz parasite ants
        c.gridy = 9; c.gridx = 0;
        fuzzParasiteAntsLabel = new JLabel();
        fuzzParasiteAntsLabel.setFont(AssetStyles.FONT_NORMAL);
        fuzzParasiteAntsLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(fuzzParasiteAntsLabel, c);
        
        fuzzParasiteAntsCheck = new JCheckBox();
        styleCheckBox(fuzzParasiteAntsCheck);
        c.gridx = 1; panel.add(fuzzParasiteAntsCheck, c);

        c.gridy = 10; c.gridx = 0;
        showAuditMenuLabel = new JLabel();
        showAuditMenuLabel.setFont(AssetStyles.FONT_NORMAL);
        showAuditMenuLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(showAuditMenuLabel, c);

        showAuditMenuCheck = new JCheckBox();
        styleCheckBox(showAuditMenuCheck);
        c.gridx = 1; panel.add(showAuditMenuCheck, c);

        c.gridy = 11;
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(16, 15, 5, 15);
        resetGeneralButton = new JButton();
        styleButton(resetGeneralButton);
        resetGeneralButton.addActionListener(e -> resetGeneralTabToDefaults());
        setupNavigation(resetGeneralButton);
        panel.add(resetGeneralButton, c);
        
        return panel;
    }

    private JPanel createRolesTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 15, 5, 15);
        c.anchor = GridBagConstraints.WEST;

        defaultRoleWorkerLabel = new JLabel();
        defaultRoleWorkerLabel.setFont(AssetStyles.FONT_NORMAL);
        defaultRoleWorkerLabel.setForeground(AssetStyles.FONT_COLOR);
        c.gridy = 0;
        c.gridx = 0;
        panel.add(defaultRoleWorkerLabel, c);
        defaultRoleWorkerCombo = createAntRoleCombo(GameConstants.TYPE_WORKER);
        c.gridx = 1;
        panel.add(defaultRoleWorkerCombo, c);

        defaultRoleSoldierLabel = new JLabel();
        defaultRoleSoldierLabel.setFont(AssetStyles.FONT_NORMAL);
        defaultRoleSoldierLabel.setForeground(AssetStyles.FONT_COLOR);
        c.gridy = 1;
        c.gridx = 0;
        panel.add(defaultRoleSoldierLabel, c);
        defaultRoleSoldierCombo = createAntRoleCombo(GameConstants.TYPE_SOLDIER);
        c.gridx = 1;
        panel.add(defaultRoleSoldierCombo, c);

        defaultRoleMajorLabel = new JLabel();
        defaultRoleMajorLabel.setFont(AssetStyles.FONT_NORMAL);
        defaultRoleMajorLabel.setForeground(AssetStyles.FONT_COLOR);
        c.gridy = 2;
        c.gridx = 0;
        panel.add(defaultRoleMajorLabel, c);
        defaultRoleMajorCombo = createAntRoleCombo(GameConstants.TYPE_MAJOR);
        c.gridx = 1;
        panel.add(defaultRoleMajorCombo, c);

        defaultRolePrincessLabel = new JLabel();
        defaultRolePrincessLabel.setFont(AssetStyles.FONT_NORMAL);
        defaultRolePrincessLabel.setForeground(AssetStyles.FONT_COLOR);
        c.gridy = 3;
        c.gridx = 0;
        panel.add(defaultRolePrincessLabel, c);
        defaultRolePrincessCombo = createAntRoleCombo(GameConstants.TYPE_PRINCESS);
        c.gridx = 1;
        panel.add(defaultRolePrincessCombo, c);

        defaultRoleQueenLabel = new JLabel();
        defaultRoleQueenLabel.setFont(AssetStyles.FONT_NORMAL);
        defaultRoleQueenLabel.setForeground(AssetStyles.FONT_COLOR);
        c.gridy = 4;
        c.gridx = 0;
        panel.add(defaultRoleQueenLabel, c);
        defaultRoleQueenCombo = createAntRoleCombo(GameConstants.TYPE_QUEEN);
        c.gridx = 1;
        panel.add(defaultRoleQueenCombo, c);

        c.gridy = 5;
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(16, 15, 5, 15);
        resetRolesButton = new JButton();
        styleButton(resetRolesButton);
        resetRolesButton.addActionListener(e -> resetRolesTabToDefaults());
        setupNavigation(resetRolesButton);
        panel.add(resetRolesButton, c);

        return panel;
    }

    private JComboBox<AntRole> createAntRoleCombo(AntType type) {
        JComboBox<AntRole> combo = new JComboBox<>();
        for (AntRole r : Engine.antRolesForAntType(type)) {
            if (!GameConstants.isWarEconomyExclusiveRole(r) && GameConstants.isObtainableRole(r)) {
                combo.addItem(r);
            }
        }
        styleComboBox(combo);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof AntRole role) {
                    setText(role.getName());
                    setIcon(role.getIcon());
                    setIconTextGap(8);
                }
                return this;
            }
        });
        return combo;
    }

    private void applyDefaultRoleFromCombo(JComboBox<AntRole> combo, AntType type, int legacyFallbackId) {
        AntRole selected = (AntRole) combo.getSelectedItem();
        if (selected == null) {
            return;
        }
        int safe = Engine.sanitizeDefaultRoleId(type, selected.getId(), legacyFallbackId);
        if (type == GameConstants.TYPE_WORKER) {
            engine.setDefaultRoleWorker(safe);
        } else if (type == GameConstants.TYPE_SOLDIER) {
            engine.setDefaultRoleSoldier(safe);
        } else if (type == GameConstants.TYPE_MAJOR) {
            engine.setDefaultRoleMajor(safe);
        } else if (type == GameConstants.TYPE_PRINCESS) {
            engine.setDefaultRolePrincess(safe);
        } else if (type == GameConstants.TYPE_QUEEN) {
            engine.setDefaultRoleQueen(safe);
        }
    }

    private void selectLanguageByCode(String code) {
        if (code == null) {
            return;
        }
        for (int i = 0; i < languageCombo.getItemCount(); i++) {
            if (languageCombo.getItemAt(i).code.equals(code)) {
                languageCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectAutosaveByValue(int value) {
        for (int i = 0; i < autosaveCombo.getItemCount(); i++) {
            if (autosaveCombo.getItemAt(i).value == value) {
                autosaveCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectFrameRateByValue(int hz) {
        int safe = Engine.sanitizeFrameRateCap(hz);
        for (int i = 0; i < frameRateCombo.getItemCount(); i++) {
            if (frameRateCombo.getItemAt(i).hz == safe) {
                frameRateCombo.setSelectedIndex(i);
                return;
            }
        }
        if (frameRateCombo.getItemCount() > 0) {
            frameRateCombo.setSelectedIndex(0);
        }
    }

    private void resetGeneralTabToDefaults() {
        selectLanguageByCode("en");
        selectAutosaveByValue(1);
        turboCheck.setSelected(false);
        arachnophobiaCheck.setSelected(false);
        pauseFocusCheck.setSelected(true);
        confirmQuitCheck.setSelected(true);
        escapeKeyGameActionsCheck.setSelected(true);
        showTooltipsCheck.setSelected(true);
        overworldAutoRecenterCheck.setSelected(true);
        fuzzParasiteAntsCheck.setSelected(true);
        showAuditMenuCheck.setSelected(false);
    }

    private void resetVideoTabToDefaults() {
        sizeCombo.setSelectedItem("1000x700");
        if (sizeCombo.getSelectedItem() == null) {
            sizeCombo.setSelectedIndex(0);
        }
        fullScreenCheck.setSelected(true);
        daylightColorOverlayCheck.setSelected(true);
        weatherColorOverlayCheck.setSelected(true);
        darkModeCheck.setSelected(false);
        selectFrameRateByValue(0);
    }

    private void resetAudioTabToDefaults() {
        masterVolSlider.setValue(50);
        musicVolSlider.setValue(50);
        sfxVolSlider.setValue(50);
    }

    private void resetRolesTabToDefaults() {
        selectRoleCombo(defaultRoleWorkerCombo, GameConstants.TYPE_WORKER, GameConstants.ROLE_FORAGER.getId());
        selectRoleCombo(defaultRoleSoldierCombo, GameConstants.TYPE_SOLDIER, GameConstants.ROLE_HUNTER.getId());
        selectRoleCombo(defaultRoleMajorCombo, GameConstants.TYPE_MAJOR, GameConstants.ROLE_CRANE.getId());
        selectRoleCombo(defaultRolePrincessCombo, GameConstants.TYPE_PRINCESS, GameConstants.ROLE_BREEDER.getId());
        selectRoleCombo(defaultRoleQueenCombo, GameConstants.TYPE_QUEEN, GameConstants.ROLE_LAYER.getId());
    }

    private void selectRoleCombo(JComboBox<AntRole> combo, AntType type, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).getId() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        AntRole fallback = Engine.resolveDefaultRoleForAntType(type, engine);
        if (fallback != null) {
            for (int i = 0; i < combo.getItemCount(); i++) {
                if (combo.getItemAt(i).getId() == fallback.getId()) {
                    combo.setSelectedIndex(i);
                    return;
                }
            }
        }
        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
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

        // Daylight color overlay
        c.gridy = 2; c.gridx = 0;
        daylightColorOverlayLabel = new JLabel();
        daylightColorOverlayLabel.setFont(AssetStyles.FONT_NORMAL);
        daylightColorOverlayLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(daylightColorOverlayLabel, c);

        daylightColorOverlayCheck = new JCheckBox();
        styleCheckBox(daylightColorOverlayCheck);
        c.gridx = 1; panel.add(daylightColorOverlayCheck, c);

        // Weather color overlay
        c.gridy = 3; c.gridx = 0;
        weatherColorOverlayLabel = new JLabel();
        weatherColorOverlayLabel.setFont(AssetStyles.FONT_NORMAL);
        weatherColorOverlayLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(weatherColorOverlayLabel, c);

        weatherColorOverlayCheck = new JCheckBox();
        styleCheckBox(weatherColorOverlayCheck);
        c.gridx = 1; panel.add(weatherColorOverlayCheck, c);

        // Dark mode
        c.gridy = 4; c.gridx = 0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(10, 15, 10, 15);
        darkModeLabel = new JLabel();
        darkModeLabel.setFont(AssetStyles.FONT_NORMAL);
        darkModeLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(darkModeLabel, c);

        darkModeCheck = new JCheckBox();
        styleCheckBox(darkModeCheck);
        c.gridx = 1; panel.add(darkModeCheck, c);

        c.gridy = 5; c.gridx = 0;
        frameRateLabel = new JLabel();
        frameRateLabel.setFont(AssetStyles.FONT_NORMAL);
        frameRateLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(frameRateLabel, c);

        frameRateCombo = new JComboBox<>(buildFrameRateOptions());
        styleComboBox(frameRateCombo);
        c.gridx = 1; panel.add(frameRateCombo, c);

        c.gridy = 6;
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(16, 15, 5, 15);
        resetVideoButton = new JButton();
        styleButton(resetVideoButton);
        resetVideoButton.addActionListener(e -> resetVideoTabToDefaults());
        setupNavigation(resetVideoButton);
        panel.add(resetVideoButton, c);
        
        return panel;
    }

    private FrameRateOption[] buildFrameRateOptions() {
        return new FrameRateOption[]{
                new FrameRateOption(LanguageStrings.get(LanguageStrings.SETTINGS_FRAME_RATE_UNCAPPED), 0),
                new FrameRateOption("30 Hz", 30),
                new FrameRateOption("60 Hz", 60),
                new FrameRateOption("120 Hz", 120)
        };
    }
    
    private JPanel createAudioTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 15, 10, 15);
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;

        c.gridy = 0;
        c.gridx = 0;
        masterLabel = new JLabel();
        masterLabel.setFont(AssetStyles.FONT_NORMAL);
        masterLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(masterLabel, c);

        masterVolSlider = createVolumeSlider();
        c.gridx = 1;
        panel.add(masterVolSlider, c);

        c.gridy = 1;
        c.gridx = 0;
        musicLabel = new JLabel();
        musicLabel.setFont(AssetStyles.FONT_NORMAL);
        musicLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(musicLabel, c);

        musicVolSlider = createVolumeSlider();
        c.gridx = 1;
        panel.add(musicVolSlider, c);

        c.gridy = 2;
        c.gridx = 0;
        sfxLabel = new JLabel();
        sfxLabel.setFont(AssetStyles.FONT_NORMAL);
        sfxLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(sfxLabel, c);

        sfxVolSlider = createVolumeSlider();
        c.gridx = 1;
        panel.add(sfxVolSlider, c);

        c.gridy = 3;
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(16, 15, 5, 15);
        resetAudioButton = new JButton();
        styleButton(resetAudioButton);
        resetAudioButton.addActionListener(e -> resetAudioTabToDefaults());
        setupNavigation(resetAudioButton);
        panel.add(resetAudioButton, c);

        return panel;
    }

    private JSlider createVolumeSlider() {
        JSlider slider = new JSlider(0, 100, 50);
        slider.setMajorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setSnapToTicks(false);
        AssetStyles.styleSlider(slider);
        return slider;
    }
    
    public void refreshTranslations() {
        if (generalTabButton != null) {
            generalTabButton.setText(LanguageStrings.get(LanguageStrings.SETTINGS_TAB_GENERAL));
            videoTabButton.setText(LanguageStrings.get(LanguageStrings.SETTINGS_TAB_VIDEO));
            audioTabButton.setText(LanguageStrings.get(LanguageStrings.SETTINGS_TAB_AUDIO));
            rolesTabButton.setText(LanguageStrings.get(LanguageStrings.SETTINGS_TAB_ROLES));
            showSection(selectedSection);
        }
        
        langLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_LANGUAGE));
        autoLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_AUTOSAVE));
        turboLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_TURBO));
        arachLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_ARACHNOPHOBIA));
        pauseFocusLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_PAUSE_FOCUS));
        confirmQuitLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_CONFIRM_QUIT));
        escapeKeyGameActionsLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_ESCAPE_KEY_GAME_ACTIONS));
        tooltipsLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_SHOW_TOOLTIPS));
        overworldAutoRecenterLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_OVERWORLD_AUTO_RECENTER));
        fuzzParasiteAntsLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_FUZZ_PARASITE_ANTS));
        showAuditMenuLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_SHOW_AUDIT_MENU));
        
        sizeLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_SCREEN_SIZE));
        fsLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_FULLSCREEN));
        daylightColorOverlayLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_DAYLIGHT_COLOR_OVERLAY));
        weatherColorOverlayLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_WEATHER_COLOR_OVERLAY));
        darkModeLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_DARK_MODE));
        frameRateLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_FRAME_RATE));
        
        masterLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_MASTER_VOL));
        musicLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_MUSIC_VOL));
        sfxLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_SFX_VOL));

        defaultRoleWorkerLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_DEFAULT_ROLE_WORKER));
        defaultRoleSoldierLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_DEFAULT_ROLE_SOLDIER));
        defaultRoleMajorLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_DEFAULT_ROLE_MAJOR));
        defaultRolePrincessLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_DEFAULT_ROLE_PRINCESS));
        defaultRoleQueenLabel.setText(LanguageStrings.get(LanguageStrings.SETTINGS_DEFAULT_ROLE_QUEEN));
        
        applySettingTooltips();
        
        saveButton.setText(LanguageStrings.get(LanguageStrings.SETTINGS_SAVE_APPLY));
        backButton.setText(LanguageStrings.get(LanguageStrings.UI_BACK));
        resetGeneralButton.setText(LanguageStrings.get(LanguageStrings.SETTINGS_RESET_TAB));
        resetVideoButton.setText(LanguageStrings.get(LanguageStrings.SETTINGS_RESET_TAB));
        resetAudioButton.setText(LanguageStrings.get(LanguageStrings.SETTINGS_RESET_TAB));
        resetRolesButton.setText(LanguageStrings.get(LanguageStrings.SETTINGS_RESET_TAB));
        
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

        int currentFrameRate = (frameRateCombo.getSelectedItem() != null)
                ? ((FrameRateOption) frameRateCombo.getSelectedItem()).hz
                : 0;
        frameRateCombo.setModel(new DefaultComboBoxModel<>(buildFrameRateOptions()));
        selectFrameRateByValue(currentFrameRate);
    }
    
    private void styleComboBox(JComboBox<?> box) {
        AssetStyles.styleComboBox(box);
    }
    
    private void styleCheckBox(JCheckBox box) {
        AssetStyles.styleCheckBox(box);
    }
    
    private void styleButton(JButton btn) {
        AssetStyles.styleButton(btn);
    }

    private void applySettingTooltips() {
        setSettingTooltip(langLabel, languageCombo, LanguageStrings.SETTINGS_LANGUAGE_TT);
        setSettingTooltip(autoLabel, autosaveCombo, LanguageStrings.SETTINGS_AUTOSAVE_TT);
        setSettingTooltip(turboLabel, turboCheck, LanguageStrings.SETTINGS_TURBO_TT);
        setSettingTooltip(arachLabel, arachnophobiaCheck, LanguageStrings.SETTINGS_ARACHNOPHOBIA_TT);
        setSettingTooltip(pauseFocusLabel, pauseFocusCheck, LanguageStrings.SETTINGS_PAUSE_FOCUS_TT);
        setSettingTooltip(confirmQuitLabel, confirmQuitCheck, LanguageStrings.SETTINGS_CONFIRM_QUIT_TT);
        setSettingTooltip(escapeKeyGameActionsLabel, escapeKeyGameActionsCheck, LanguageStrings.SETTINGS_ESCAPE_KEY_GAME_ACTIONS_TT);
        setSettingTooltip(tooltipsLabel, showTooltipsCheck, LanguageStrings.SETTINGS_SHOW_TOOLTIPS_TT);
        setSettingTooltip(overworldAutoRecenterLabel, overworldAutoRecenterCheck, LanguageStrings.SETTINGS_OVERWORLD_AUTO_RECENTER_TT);
        setSettingTooltip(fuzzParasiteAntsLabel, fuzzParasiteAntsCheck, LanguageStrings.SETTINGS_FUZZ_PARASITE_ANTS_TT);
        setSettingTooltip(showAuditMenuLabel, showAuditMenuCheck, LanguageStrings.SETTINGS_SHOW_AUDIT_MENU_TT);
        setSettingTooltip(sizeLabel, sizeCombo, LanguageStrings.SETTINGS_SCREEN_SIZE_TT);
        setSettingTooltip(fsLabel, fullScreenCheck, LanguageStrings.SETTINGS_FULLSCREEN_TT);
        setSettingTooltip(daylightColorOverlayLabel, daylightColorOverlayCheck, LanguageStrings.SETTINGS_DAYLIGHT_COLOR_OVERLAY_TT);
        setSettingTooltip(weatherColorOverlayLabel, weatherColorOverlayCheck, LanguageStrings.SETTINGS_WEATHER_COLOR_OVERLAY_TT);
        setSettingTooltip(darkModeLabel, darkModeCheck, LanguageStrings.SETTINGS_DARK_MODE_TT);
        setSettingTooltip(frameRateLabel, frameRateCombo, LanguageStrings.SETTINGS_FRAME_RATE_TT);
        setSettingTooltip(masterLabel, masterVolSlider, LanguageStrings.SETTINGS_MASTER_VOL_TT);
        setSettingTooltip(musicLabel, musicVolSlider, LanguageStrings.SETTINGS_MUSIC_VOL_TT);
        setSettingTooltip(sfxLabel, sfxVolSlider, LanguageStrings.SETTINGS_SFX_VOL_TT);
        setSettingTooltip(defaultRoleWorkerLabel, defaultRoleWorkerCombo, LanguageStrings.SETTINGS_DEFAULT_ROLE_WORKER_TT);
        setSettingTooltip(defaultRoleSoldierLabel, defaultRoleSoldierCombo, LanguageStrings.SETTINGS_DEFAULT_ROLE_SOLDIER_TT);
        setSettingTooltip(defaultRoleMajorLabel, defaultRoleMajorCombo, LanguageStrings.SETTINGS_DEFAULT_ROLE_MAJOR_TT);
        setSettingTooltip(defaultRolePrincessLabel, defaultRolePrincessCombo, LanguageStrings.SETTINGS_DEFAULT_ROLE_PRINCESS_TT);
        setSettingTooltip(defaultRoleQueenLabel, defaultRoleQueenCombo, LanguageStrings.SETTINGS_DEFAULT_ROLE_QUEEN_TT);
    }

    private static void setSettingTooltip(JLabel label, JComponent control, String tooltipKey) {
        String text = LanguageStrings.get(tooltipKey);
        label.setToolTipText(text);
        control.setToolTipText(text);
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
        selectLanguageByCode(engine.getLanguage());
        selectAutosaveByValue(engine.getAutosaveFrequency());
        
        turboCheck.setSelected(engine.isAllowTurboMode());
        arachnophobiaCheck.setSelected(engine.isArachnophobiaMode());
        pauseFocusCheck.setSelected(engine.isPauseOnFocusLoss());
        confirmQuitCheck.setSelected(engine.isConfirmOnQuit());
        escapeKeyGameActionsCheck.setSelected(engine.isEscapeKeyGameActions());
        showTooltipsCheck.setSelected(engine.isShowTooltips());
        overworldAutoRecenterCheck.setSelected(engine.isOverworldAutoRecenter());
        fuzzParasiteAntsCheck.setSelected(engine.isFuzzParasiteAnts());
        showAuditMenuCheck.setSelected(engine.isShowAuditMenu());
        
        sizeCombo.setSelectedItem(engine.getScreenSize());
        if (sizeCombo.getSelectedItem() == null) {
            ensureScreenSizeInCombo(engine.getScreenSize());
            sizeCombo.setSelectedItem(engine.getScreenSize());
        }
        fullScreenCheck.setSelected(engine.isFullScreen());
        daylightColorOverlayCheck.setSelected(engine.isDaylightColorOverlayEnabled());
        weatherColorOverlayCheck.setSelected(engine.isWeatherColorOverlayEnabled());
        darkModeCheck.setSelected(engine.isDarkMode());
        selectFrameRateByValue(engine.getFrameRateCap());

        masterVolSlider.setValue(engine.getMasterVolume());
        musicVolSlider.setValue(engine.getMusicVolume());
        sfxVolSlider.setValue(engine.getSfxVolume());

        selectRoleCombo(defaultRoleWorkerCombo, GameConstants.TYPE_WORKER, engine.getDefaultRoleWorker());
        selectRoleCombo(defaultRoleSoldierCombo, GameConstants.TYPE_SOLDIER, engine.getDefaultRoleSoldier());
        selectRoleCombo(defaultRoleMajorCombo, GameConstants.TYPE_MAJOR, engine.getDefaultRoleMajor());
        selectRoleCombo(defaultRolePrincessCombo, GameConstants.TYPE_PRINCESS, engine.getDefaultRolePrincess());
        selectRoleCombo(defaultRoleQueenCombo, GameConstants.TYPE_QUEEN, engine.getDefaultRoleQueen());

        loadedFullScreen = engine.isFullScreen();
        loadedScreenSize = engine.getScreenSize();
    }

    private void ensureScreenSizeInCombo(String size) {
        if (size == null || size.isBlank()) {
            return;
        }
        for (int i = 0; i < sizeCombo.getItemCount(); i++) {
            if (size.equals(sizeCombo.getItemAt(i))) {
                return;
            }
        }
        sizeCombo.addItem(size);
    }

    private boolean windowChromeChanged() {
        if (fullScreenCheck.isSelected() != loadedFullScreen) {
            return true;
        }
        if (!fullScreenCheck.isSelected()) {
            return !resolveSelectedScreenSize().equals(loadedScreenSize);
        }
        return false;
    }

    private String resolveSelectedScreenSize() {
        Object selectedSize = sizeCombo.getSelectedItem();
        if (selectedSize instanceof String size && !size.isBlank()) {
            return size;
        }
        return engine.getScreenSize() != null ? engine.getScreenSize() : "1000x700";
    }

    private void showSavedMessage() {
        Component parent = inDialog ? frame : this;
        SwingUtilities.invokeLater(() -> UiOptionPane.showMessageDialog(parent,
                LanguageStrings.get(LanguageStrings.SETTINGS_SAVED_MSG),
                LanguageStrings.get(LanguageStrings.UI_SETTINGS),
                JOptionPane.INFORMATION_MESSAGE));
    }

    private void saveSettings() {
        LanguageOption selectedLang = (LanguageOption) languageCombo.getSelectedItem();
        if (selectedLang != null) {
            // Engine.setLanguage already updates LanguageStrings and notifies UI once.
            engine.setLanguage(selectedLang.code);
        }
        
        AutosaveOption selectedFreq = (AutosaveOption) autosaveCombo.getSelectedItem();
        if (selectedFreq != null) {
            engine.setAutosaveFrequency(selectedFreq.value);
        }
        
        engine.setAllowTurboMode(turboCheck.isSelected());
        engine.setArachnophobiaMode(arachnophobiaCheck.isSelected());
        engine.setPauseOnFocusLoss(pauseFocusCheck.isSelected());
        engine.setConfirmOnQuit(confirmQuitCheck.isSelected());
        engine.setEscapeKeyGameActions(escapeKeyGameActionsCheck.isSelected());
        engine.setShowTooltips(showTooltipsCheck.isSelected());
        engine.setOverworldAutoRecenter(overworldAutoRecenterCheck.isSelected());
        engine.setFuzzParasiteAnts(fuzzParasiteAntsCheck.isSelected());
        engine.setShowAuditMenu(showAuditMenuCheck.isSelected());
        
        engine.setScreenSize(resolveSelectedScreenSize());
        engine.setFullScreen(fullScreenCheck.isSelected());
        engine.setDaylightColorOverlayEnabled(daylightColorOverlayCheck.isSelected());
        engine.setWeatherColorOverlayEnabled(weatherColorOverlayCheck.isSelected());
        engine.setDarkMode(darkModeCheck.isSelected());
        FrameRateOption selectedFrameRate = (FrameRateOption) frameRateCombo.getSelectedItem();
        if (selectedFrameRate != null) {
            engine.setFrameRateCap(selectedFrameRate.hz);
        }

        engine.setMasterVolume(masterVolSlider.getValue());
        engine.setMusicVolume(musicVolSlider.getValue());
        engine.setSfxVolume(sfxVolSlider.getValue());

        applyDefaultRoleFromCombo(defaultRoleWorkerCombo, GameConstants.TYPE_WORKER, GameConstants.ROLE_FORAGER.getId());
        applyDefaultRoleFromCombo(defaultRoleSoldierCombo, GameConstants.TYPE_SOLDIER, GameConstants.ROLE_HUNTER.getId());
        applyDefaultRoleFromCombo(defaultRoleMajorCombo, GameConstants.TYPE_MAJOR, GameConstants.ROLE_CRANE.getId());
        applyDefaultRoleFromCombo(defaultRolePrincessCombo, GameConstants.TYPE_PRINCESS, GameConstants.ROLE_BREEDER.getId());
        applyDefaultRoleFromCombo(defaultRoleQueenCombo, GameConstants.TYPE_QUEEN, GameConstants.ROLE_LAYER.getId());

        engine.saveGlobalSettings();
        boolean chromeChanged = windowChromeChanged();

        if (inDialog) {
            frame.applyRuntimeSettings();
            if (chromeChanged) {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) {
                    window.dispose();
                }
                SwingUtilities.invokeLater(() -> {
                    frame.applyEngineSettings();
                    if (frame.getGamePanel() != null) {
                        frame.getGamePanel().applyOverworldRecenterSetting();
                    }
                    loadedFullScreen = engine.isFullScreen();
                    loadedScreenSize = engine.getScreenSize();
                    showSavedMessage();
                });
                return;
            }
        } else if (chromeChanged) {
            // Only tear down / recreate the window when size or fullscreen actually changed.
            // Language-only saves used to always dispose the frame, which re-fired IntroPanel
            // ancestor listeners and could yank the UI into Play → Load.
            frame.applyEngineSettings();
        } else {
            frame.applyRuntimeSettings();
        }
        if (frame.getGamePanel() != null) {
            frame.getGamePanel().applyOverworldRecenterSetting();
        }

        loadedFullScreen = engine.isFullScreen();
        loadedScreenSize = engine.getScreenSize();
        showSavedMessage();
    }

    public void refreshTheme() {
        setBackground(AssetStyles.BACKGROUND_COLOR);
        sectionTabs.setBackground(AssetStyles.BACKGROUND_DARK);
        sectionCards.setBackground(AssetStyles.BACKGROUND_COLOR);
        showSection(selectedSection);
        styleButton(saveButton);
        styleButton(backButton);
        styleButton(resetGeneralButton);
        styleButton(resetVideoButton);
        styleButton(resetAudioButton);
        styleButton(resetRolesButton);
        AssetStyles.applyThemeToContainer(this);
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

        @Override
        public void refreshTheme() {
            super.refreshTheme();
            settingsPanel.refreshTheme();
        }
    }
}
