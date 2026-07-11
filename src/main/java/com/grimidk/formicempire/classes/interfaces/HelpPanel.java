package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntSubtype;
import com.grimidk.formicempire.classes.constants.ant.AntSubtypeSlot;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ColonyLoyalty;
import com.grimidk.formicempire.classes.constants.misc.ColonyLoyaltyModifier;
import com.grimidk.formicempire.classes.constants.misc.DiplomaticReputation;
import com.grimidk.formicempire.classes.constants.misc.TradeMethod;
import com.grimidk.formicempire.classes.constants.misc.BugType;
import com.grimidk.formicempire.classes.constants.misc.GameSpeed;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Humidity;
import com.grimidk.formicempire.classes.constants.world.MoonPhase;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.constants.world.Temperature;
import com.grimidk.formicempire.classes.constants.world.TimeOfDay;
import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiDialogUtils;
import com.grimidk.formicempire.classes.infrasctructure.assets.ClasspathTextFiles;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.constants.Constant;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HelpPanel extends JPanel {
    private final MainFrame frame;
    private final JTabbedPane mainTabs;
    private final JButton backButton;

    public HelpPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(AssetStyles.BACKGROUND_COLOR);

        mainTabs = new JTabbedPane();
        AssetStyles.styleTabbedPane(mainTabs);

        backButton = new JButton();
        
        initTabs();
        
        setupTabPaneNavigation(mainTabs);
        add(mainTabs, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setBackground(AssetStyles.BACKGROUND_COLOR);

        AssetStyles.styleButton(backButton);
        backButton.addActionListener(e -> this.frame.showCard(MainFrame.CARD_INIT));
        
        setupButtonNavigation(backButton);
        
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
    
    private void initTabs() {
        mainTabs.removeAll();

        // Add tabs
        mainTabs.addTab(LanguageStrings.get(LanguageStrings.HELP_TAB_TUTORIALS), createTutorialsPanel());
        mainTabs.addTab(LanguageStrings.get(LanguageStrings.HELP_TAB_SPECIES), createSpeciesPanel());
        mainTabs.addTab(LanguageStrings.get(LanguageStrings.HELP_TAB_TYPES), createAntTypesPanel());
        mainTabs.addTab(LanguageStrings.get(LanguageStrings.HELP_TAB_BUGS), createBugsPanel());
        mainTabs.addTab(LanguageStrings.get(LanguageStrings.HELP_TAB_ANT_ROLES), createAntRolesPanel());
        mainTabs.addTab(LanguageStrings.get(LanguageStrings.HELP_TAB_EMPIRE), createEmpirePanel());
        mainTabs.addTab(LanguageStrings.get(LanguageStrings.HELP_TAB_UPGRADES),
                createDictionaryPanel(new ArrayList<>(GameUnlocks.getUpgrades())));
        mainTabs.addTab(LanguageStrings.get(LanguageStrings.HELP_TAB_BUILDINGS),
                createDictionaryPanel(new ArrayList<>(GameUnlocks.getBuildings()), false));
        mainTabs.addTab(LanguageStrings.get(LanguageStrings.HELP_TAB_ASSIMILATIONS), createDictionaryPanel(new ArrayList<>(GameUnlocks.getAssimilations())));
        mainTabs.addTab(LanguageStrings.get(LanguageStrings.HELP_TAB_WORLD), createWorldPanel());
        mainTabs.addTab(LanguageStrings.get(LanguageStrings.HELP_TAB_UI), createUiControlsPanel());
    }
    
    public void refreshTranslations() {
        backButton.setText(LanguageStrings.get(LanguageStrings.UI_BACK));
        
        // Update tab titles
        String[] titles = {
            LanguageStrings.get(LanguageStrings.HELP_TAB_TUTORIALS),
            LanguageStrings.get(LanguageStrings.HELP_TAB_SPECIES),
            LanguageStrings.get(LanguageStrings.HELP_TAB_TYPES),
            LanguageStrings.get(LanguageStrings.HELP_TAB_BUGS),
            LanguageStrings.get(LanguageStrings.HELP_TAB_ANT_ROLES),
            LanguageStrings.get(LanguageStrings.HELP_TAB_EMPIRE),
            LanguageStrings.get(LanguageStrings.HELP_TAB_UPGRADES),
            LanguageStrings.get(LanguageStrings.HELP_TAB_BUILDINGS),
            LanguageStrings.get(LanguageStrings.HELP_TAB_ASSIMILATIONS),
            LanguageStrings.get(LanguageStrings.HELP_TAB_WORLD),
            LanguageStrings.get(LanguageStrings.HELP_TAB_UI)
        };
        
        for (int i = 0; i < titles.length && i < mainTabs.getTabCount(); i++) {
            mainTabs.setTitleAt(i, titles[i]);
        }
        
        // Fully re-init tabs to refresh internal content
        int selected = mainTabs.getSelectedIndex();
        initTabs();
        if (selected >= 0 && selected < mainTabs.getTabCount()) {
            mainTabs.setSelectedIndex(selected);
        }
    }

    public void refreshTheme() {
        setBackground(AssetStyles.BACKGROUND_COLOR);
        AssetStyles.styleTabbedPane(mainTabs);
        AssetStyles.styleButton(backButton);
        int selected = mainTabs.getSelectedIndex();
        initTabs();
        if (selected >= 0 && selected < mainTabs.getTabCount()) {
            mainTabs.setSelectedIndex(selected);
        }
        AssetStyles.applyThemeToContainer(this);
    }
    
    private void setupButtonNavigation(JButton button) {
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

    private void setupTabPaneNavigation(JTabbedPane tabs) {
        Set<AWTKeyStroke> forwardKeys = new HashSet<>(tabs.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        forwardKeys.add(KeyStroke.getKeyStroke("DOWN"));
        tabs.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);

        Set<AWTKeyStroke> backwardKeys = new HashSet<>(tabs.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        backwardKeys.add(KeyStroke.getKeyStroke("UP"));
        tabs.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardKeys);
    }

    private JComponent createTutorialsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        panel.add(wrapTutorialSection(LanguageStrings.HELP_TAB_WELCOME, createWelcomePanel()));
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(wrapTutorialSection(LanguageStrings.HELP_TAB_STARTED, createGettingStartedPanel()));
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(wrapTutorialSection(LanguageStrings.HELP_TAB_DYNASTY, createEmpireManagementPanel()));
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(wrapTutorialSection(LanguageStrings.HELP_TAB_HOTKEYS, buildHotkeysGrid()));

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JPanel wrapTutorialSection(String titleKey, JComponent content) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(AssetStyles.BACKGROUND_COLOR);
        section.setBorder(BorderFactory.createTitledBorder(
                AssetStyles.PANEL_BORDER,
                LanguageStrings.get(titleKey),
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                AssetStyles.FONT_BOLD,
                AssetStyles.FONT_COLOR_HEADER));
        section.add(content, BorderLayout.CENTER);
        return section;
    }

    private String tutorialHtml(String body) {
        return helpHtml("width:450px;font-size:12pt;", body);
    }

    private static String helpHtml(String extraStyle, String body) {
        return "<html><div style='" + extraStyle + "font-family:" + AssetStyles.themeFontFamilyCss()
                + ";color:" + AssetStyles.themeTextColorHtml() + ";'>" + body + "</div></html>";
    }

    private void styleTutorialLabel(JLabel label) {
        label.setFont(AssetStyles.FONT_NORMAL);
        label.setForeground(AssetStyles.FONT_COLOR);
    }

    private JComponent createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel(tutorialHtml(LanguageStrings.get("HELP_WELCOME_STORY")));
        styleTutorialLabel(label);
        panel.add(label);
        return panel;
    }

    private JComponent createGettingStartedPanel() {
        String body = LanguageStrings.get("HELP_START_INFO")
                + "<br><br>"
                + LanguageStrings.get("HELP_OVERWORLD_GATHERING");

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel(tutorialHtml(body));
        styleTutorialLabel(label);
        panel.add(label);
        return panel;
    }

    private JComponent createEmpireManagementPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel(tutorialHtml(LanguageStrings.get("HELP_DYNASTY_INFO")));
        styleTutorialLabel(label);
        panel.add(label);
        return panel;
    }

    private JComponent createSpeciesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (Species s : GameConstants.getSpecies()) {
            if (!GameConstants.hasAssimilatedDroneSprite(s)) {
                continue;
            }
            JPanel entry = new JPanel(new BorderLayout(10, 0));
            entry.setBackground(AssetStyles.BACKGROUND_COLOR);
            entry.setBorder(BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, s.getName(), 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
                javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                AssetStyles.FONT_BOLD, AssetStyles.FONT_COLOR_HEADER));

            ImageIcon sprite = GameConstants.getAntSprite(GameConstants.TYPE_WORKER, s);
            JLabel spriteLabel = new JLabel(sprite != null ? sprite : s.getIcon());
            spriteLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
            entry.add(spriteLabel, BorderLayout.WEST);

            String baseUpgrades = s.getBaseUpgrades().stream().map(Upgrade::getFlavorName).collect(Collectors.joining(", "));
            String info = helpHtml("width:350px;font-size:11pt;",
                    "<b>" + LanguageStrings.get("HELP_SPECIES_SCIENTIFIC") + "</b> <i>" + s.getScientific() + "</i><br>"
                            + "<b>" + LanguageStrings.get("HELP_SPECIES_TRAITS") + "</b> " + baseUpgrades);
            
            JLabel infoLabel = new JLabel(info);
            infoLabel.setFont(AssetStyles.FONT_NORMAL);
            infoLabel.setForeground(AssetStyles.FONT_COLOR);
            infoLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
            
            entry.add(infoLabel, BorderLayout.CENTER);
            panel.add(entry);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JPanel buildHotkeysGrid() {
        JPanel hotkeyPanel = new JPanel(new GridBagLayout());
        hotkeyPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        hotkeyPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 10, 5, 10);
        c.anchor = GridBagConstraints.WEST;

        class HotkeyRow {
            private int gridY = 0;
            void add(String key, String desc) {
                c.gridx = 0;
                c.gridy = gridY;
                JLabel keyLabel = new JLabel(key);
                keyLabel.setFont(AssetStyles.FONT_BOLD);
                keyLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
                hotkeyPanel.add(keyLabel, c);

                c.gridx = 1;
                JLabel descLabel = new JLabel(desc);
                descLabel.setFont(AssetStyles.FONT_NORMAL);
                descLabel.setForeground(AssetStyles.FONT_COLOR);
                hotkeyPanel.add(descLabel, c);
                gridY++;
            }
            void addSeparator() {
                c.gridx = 0;
                c.gridy = gridY;
                c.gridwidth = 2;
                c.fill = GridBagConstraints.HORIZONTAL;
                JSeparator sep = AssetStyles.createInternalSeparator();
                hotkeyPanel.add(sep, c);
                c.gridwidth = 1;
                c.fill = GridBagConstraints.NONE;
                gridY++;
            }
        }
        
        HotkeyRow row = new HotkeyRow();
        row.add("Spacebar", LanguageStrings.get("HOTKEY_PAUSE"));
        row.add("+ / -", LanguageStrings.get("HOTKEY_SPEED"));
        row.add("Z", LanguageStrings.get("HOTKEY_VIEW"));
        row.add("ESC", LanguageStrings.get("HOTKEY_ESC"));
        row.addSeparator();
        row.add("Q / W / E / R / T", LanguageStrings.get("HOTKEY_ROLES"));
        row.addSeparator();
        row.add("P", LanguageStrings.get("HOTKEY_P"));
        row.add("Y / U / I / O", LanguageStrings.get("HOTKEY_UPGRADES"));
        row.add("C", LanguageStrings.get("HOTKEY_ABILITIES"));
        row.add("A / S / D / F", LanguageStrings.get("HOTKEY_DYNASTY"));
        row.add("M", LanguageStrings.get("HOTKEY_M"));
        row.add("X", LanguageStrings.get("HOTKEY_X"));

        return hotkeyPanel;
    }

    private JComponent createAntTypesPanel() {
        JPanel split = new JPanel(new GridLayout(1, 2, 8, 0));
        split.setBackground(AssetStyles.BACKGROUND_COLOR);
        split.setBorder(new EmptyBorder(10, 10, 10, 10));
        split.add(wrapTutorialSection(LanguageStrings.HELP_TAB_TYPES, createAntTypesListPanel()));
        split.add(wrapTutorialSection(LanguageStrings.HELP_TAB_SUBTYPES, createAntSubtypesListPanel()));
        return split;
    }

    private JComponent createAntTypesListPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (AntType type : GameConstants.getAntTypes()) {
            if (type == GameConstants.TYPE_DEAD || type == GameConstants.TYPE_ZOMBIE) continue;

            JPanel entry = new JPanel(new BorderLayout(10, 0));
            entry.setBackground(AssetStyles.BACKGROUND_COLOR);
            entry.setBorder(BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, type.getName(), 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
                javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                AssetStyles.FONT_BOLD, AssetStyles.FONT_COLOR_HEADER));

            JLabel icon = new JLabel(type.getIcon());
            icon.setBorder(new EmptyBorder(5, 5, 5, 5));
            entry.add(icon, BorderLayout.WEST);

            String desc = "";
            if (type == GameConstants.TYPE_EGG) desc = LanguageStrings.get("HELP_TYPE_EGG_DESC");
            else if (type == GameConstants.TYPE_LARVA) desc = LanguageStrings.get("HELP_TYPE_LARVA_DESC");
            else if (type == GameConstants.TYPE_PUPA) desc = LanguageStrings.get("HELP_TYPE_PUPA_DESC");
            else if (type == GameConstants.TYPE_WORKER) desc = LanguageStrings.get("HELP_TYPE_WORKER_DESC");
            else if (type == GameConstants.TYPE_SOLDIER) desc = LanguageStrings.get("HELP_TYPE_SOLDIER_DESC");
            else if (type == GameConstants.TYPE_MAJOR) desc = LanguageStrings.get("HELP_TYPE_MAJOR_DESC");
            else if (type == GameConstants.TYPE_PRINCESS) desc = LanguageStrings.get("HELP_TYPE_PRINCESS_DESC");
            else if (type == GameConstants.TYPE_DRONE) desc = LanguageStrings.get("HELP_TYPE_DRONE_DESC");
            else if (type == GameConstants.TYPE_QUEEN) desc = LanguageStrings.get("HELP_TYPE_QUEEN_DESC");
            
            JTextArea descArea = new JTextArea(desc);
            descArea.setFont(AssetStyles.FONT_NORMAL);
            descArea.setForeground(AssetStyles.FONT_COLOR);
            descArea.setWrapStyleWord(true);
            descArea.setLineWrap(true);
            descArea.setEditable(false);
            descArea.setFocusable(false);
            descArea.setBackground(panel.getBackground());
            descArea.setBorder(new EmptyBorder(5, 5, 5, 5));
            
            entry.add(descArea, BorderLayout.CENTER);
            panel.add(entry);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        return scrollPane;
    }

    private JComponent createAntSubtypesListPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel intro = new JLabel(tutorialHtml(LanguageStrings.get(LanguageStrings.HELP_SUBTYPES_INTRO)));
        styleTutorialLabel(intro);
        intro.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.add(intro);

        for (AntSubtypeSlot slot : GameConstants.getConfigurableSubtypeSlots()) {
            String sectionKey = slot == AntSubtypeSlot.HEAD
                    ? LanguageStrings.HATCH_SUBTYPE_HEAD_SECTION
                    : LanguageStrings.HATCH_SUBTYPE_ABDOMEN_SECTION;
            JLabel sectionLabel = new JLabel(LanguageStrings.get(sectionKey));
            sectionLabel.setFont(AssetStyles.FONT_BOLD);
            sectionLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
            sectionLabel.setBorder(new EmptyBorder(8, 0, 4, 0));
            sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(sectionLabel);

            for (AntSubtype subtype : GameConstants.getSubtypesForSlot(slot)) {
                if (subtype.isNone()) {
                    continue;
                }
                panel.add(buildAntSubtypeEntry(panel, subtype));
                panel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        return scrollPane;
    }

    private JPanel buildAntSubtypeEntry(JPanel panel, AntSubtype subtype) {
        JPanel entry = new JPanel(new BorderLayout(10, 0));
        entry.setBackground(AssetStyles.BACKGROUND_COLOR);
        entry.setBorder(BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, subtype.getName(),
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                AssetStyles.FONT_BOLD, AssetStyles.FONT_COLOR_HEADER));
        entry.setAlignmentX(Component.LEFT_ALIGNMENT);

        ImageIcon icon = subtype.getIcon();
        if (icon != null) {
            JLabel iconLabel = new JLabel(icon);
            iconLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
            entry.add(iconLabel, BorderLayout.WEST);
        }

        JTextArea descArea = new JTextArea(subtype.getDesc());
        descArea.setFont(AssetStyles.FONT_NORMAL);
        descArea.setForeground(AssetStyles.FONT_COLOR);
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setFocusable(false);
        descArea.setBackground(panel.getBackground());
        descArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        entry.add(descArea, BorderLayout.CENTER);
        return entry;
    }

    private JComponent createBugsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (BugType type : GameConstants.getBugTypes()) {
            JPanel entry = new JPanel(new BorderLayout(10, 0));
            entry.setBackground(AssetStyles.BACKGROUND_COLOR);
            entry.setBorder(BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, type.getName(),
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                AssetStyles.FONT_BOLD, AssetStyles.FONT_COLOR_HEADER));

            ImageIcon sprite = type.getSprite();
            JLabel spriteLabel = new JLabel(sprite != null ? sprite : type.getIcon());
            spriteLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
            entry.add(spriteLabel, BorderLayout.WEST);

            String desc;
            if (type == GameConstants.TYPE_ANT) {
                desc = LanguageStrings.get(LanguageStrings.HELP_BUG_ANT_DESC);
            } else if (type == GameConstants.TYPE_APHID) {
                desc = LanguageStrings.get(LanguageStrings.HELP_BUG_APHID_DESC);
            } else if (type == GameConstants.TYPE_SYMBIOTIC_MITE) {
                desc = LanguageStrings.get(LanguageStrings.HELP_BUG_SYMBIOTIC_MITE_DESC);
            } else if (type == GameConstants.TYPE_DERMESTID) {
                desc = LanguageStrings.get(LanguageStrings.HELP_BUG_DERMESTID_DESC);
            } else if (type == GameConstants.TYPE_PARASITE_ANT) {
                desc = LanguageStrings.get(LanguageStrings.HELP_BUG_PARASITE_ANT_DESC);
            } else if (type == GameConstants.TYPE_PARASITIC_MITE) {
                desc = LanguageStrings.get(LanguageStrings.HELP_BUG_PARASITIC_MITE_DESC);
            } else {
                desc = "";
            }

            String info = helpHtml("width:350px;font-size:11pt;",
                    "<b>" + LanguageStrings.get(LanguageStrings.HELP_SPECIES_SCIENTIFIC) + "</b> <i>" + type.getScientificName()
                            + "</i><br><br>" + desc);

            JLabel infoLabel = new JLabel(info);
            infoLabel.setFont(AssetStyles.FONT_NORMAL);
            infoLabel.setForeground(AssetStyles.FONT_COLOR);
            entry.add(infoLabel, BorderLayout.CENTER);

            panel.add(entry);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private static String roleDescriptionKey(AntRole role) {
        return role.getNameKey() + "_DESC";
    }

    private JComponent createAntRolesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        List<AntRole> roles = new ArrayList<>(GameConstants.getAntRoles());
        Collections.sort(roles, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        for (AntRole role : roles) {
            JPanel entry = new JPanel(new BorderLayout(10, 0));
            entry.setBackground(AssetStyles.BACKGROUND_COLOR);
            entry.setBorder(BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, role.getName(),
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                AssetStyles.FONT_BOLD, AssetStyles.FONT_COLOR_HEADER));

            JLabel icon = new JLabel(role.getIcon());
            icon.setBorder(new EmptyBorder(5, 5, 5, 5));
            entry.add(icon, BorderLayout.WEST);

            AntType antType = role.getAntType();
            String typeLine = antType != null
                    ? LanguageStrings.format(LanguageStrings.HELP_ROLE_ANT_TYPE, antType.getName())
                    : "";
            String desc = LanguageStrings.get(roleDescriptionKey(role));
            String body = helpHtml("width:350px;font-size:11pt;",
                    (typeLine.isEmpty() ? "" : "<b>" + typeLine + "</b><br><br>") + desc);

            JLabel infoLabel = new JLabel(body);
            infoLabel.setFont(AssetStyles.FONT_NORMAL);
            infoLabel.setForeground(AssetStyles.FONT_COLOR);
            infoLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
            entry.add(infoLabel, BorderLayout.CENTER);

            panel.add(entry);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JComponent createEmpirePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        panel.add(wrapEmpireSection(LanguageStrings.HELP_EMPIRE_TRADE, createTradeMethodSection()));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(wrapEmpireSection(LanguageStrings.HELP_EMPIRE_LOYALTY, createLoyaltySection()));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(wrapEmpireSection(LanguageStrings.HELP_EMPIRE_MILITARY_POWER, createMilitaryPowerSection()));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(wrapEmpireSection(LanguageStrings.HELP_EMPIRE_REPUTATION, createReputationSection()));

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JPanel wrapEmpireSection(String titleKey, JComponent content) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(AssetStyles.BACKGROUND_COLOR);
        section.setBorder(BorderFactory.createTitledBorder(
                AssetStyles.PANEL_BORDER,
                LanguageStrings.get(titleKey),
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                AssetStyles.FONT_BOLD,
                AssetStyles.FONT_COLOR_HEADER));
        section.add(content, BorderLayout.CENTER);
        return section;
    }

    private JPanel createTradeMethodSection() {
        JPanel grid = new JPanel(new GridLayout(0, 1, 0, 8));
        grid.setBackground(AssetStyles.BACKGROUND_COLOR);
        grid.setBorder(new EmptyBorder(8, 8, 8, 8));

        for (TradeMethod method : GameConstants.getTradeMethods()) {
            String descKey = method.getNameKey() + "_DESC";
            String info = helpHtml("width:420px;font-size:11pt;",
                    "<b>" + method.getName() + "</b><br>" + LanguageStrings.get(descKey));
            JLabel row = new JLabel(info, method.getIcon(), SwingConstants.LEFT);
            row.setFont(AssetStyles.FONT_NORMAL);
            row.setForeground(AssetStyles.FONT_COLOR);
            row.setIconTextGap(10);
            grid.add(row);
        }
        return grid;
    }

    private JPanel createLoyaltySection() {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(AssetStyles.BACKGROUND_COLOR);
        list.setBorder(new EmptyBorder(8, 8, 8, 8));

        for (ColonyLoyalty loyalty : GameConstants.getColonyLoyalties()) {
            String label = loyalty.getName() + String.format(
                    LanguageStrings.get(LanguageStrings.HELP_TIER_MIN_SCORE), loyalty.getMinScore());
            JLabel item = new JLabel(label, loyalty.getIcon(), SwingConstants.LEFT);
            item.setFont(AssetStyles.FONT_NORMAL);
            item.setForeground(AssetStyles.FONT_COLOR);
            item.setIconTextGap(8);
            list.add(item);
        }

        list.add(Box.createRigidArea(new Dimension(0, 8)));
        JLabel modifiersTitle = new JLabel(LanguageStrings.get(LanguageStrings.HELP_LOYALTY_MODIFIERS_TITLE));
        modifiersTitle.setFont(AssetStyles.FONT_BOLD);
        modifiersTitle.setForeground(AssetStyles.FONT_COLOR_HEADER);
        modifiersTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        list.add(modifiersTitle);
        list.add(Box.createRigidArea(new Dimension(0, 4)));

        for (ColonyLoyaltyModifier modifier : GameConstants.getColonyLoyaltyModifiers()) {
            String line = String.format(
                    LanguageStrings.get(LanguageStrings.LOYALTY_MODIFIER_LINE),
                    modifier.getName(),
                    modifier.getLoyaltyDelta());
            JLabel item = new JLabel(line, GameConstants.ICON_STAT_LOYALTY, SwingConstants.LEFT);
            item.setFont(AssetStyles.FONT_NORMAL);
            item.setForeground(AssetStyles.FONT_COLOR);
            item.setIconTextGap(8);
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
            list.add(item);
        }
        JLabel militaryLine = new JLabel(
                LanguageStrings.get(LanguageStrings.HELP_LOYALTY_MODIFIER_MILITARY),
                GameConstants.ICON_STAT_MILITARY_POWER,
                SwingConstants.LEFT);
        militaryLine.setFont(AssetStyles.FONT_NORMAL);
        militaryLine.setForeground(AssetStyles.FONT_COLOR);
        militaryLine.setIconTextGap(8);
        militaryLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        list.add(militaryLine);
        JLabel distanceLine = new JLabel(
                LanguageStrings.get(LanguageStrings.HELP_LOYALTY_MODIFIER_DISTANCE),
                GameConstants.ICON_STAT_LOYALTY,
                SwingConstants.LEFT);
        distanceLine.setFont(AssetStyles.FONT_NORMAL);
        distanceLine.setForeground(AssetStyles.FONT_COLOR);
        distanceLine.setIconTextGap(8);
        distanceLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        list.add(distanceLine);
        return list;
    }

    private JPanel createMilitaryPowerSection() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        JLabel iconLabel = new JLabel(GameConstants.ICON_STAT_MILITARY_POWER);
        iconLabel.setVerticalAlignment(SwingConstants.TOP);
        panel.add(iconLabel, BorderLayout.WEST);
        JLabel body = new JLabel("<html>" + LanguageStrings.get(LanguageStrings.HELP_MILITARY_POWER_BODY) + "</html>");
        body.setFont(AssetStyles.FONT_NORMAL);
        body.setForeground(AssetStyles.FONT_COLOR);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReputationSection() {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(AssetStyles.BACKGROUND_COLOR);
        list.setBorder(new EmptyBorder(8, 8, 8, 8));

        for (DiplomaticReputation reputation : GameConstants.getDiplomaticReputations()) {
            String label = reputation.getName() + String.format(
                    LanguageStrings.get(LanguageStrings.HELP_TIER_MIN_SCORE), reputation.getMinScore());
            JLabel item = new JLabel(label, reputation.getIcon(), SwingConstants.LEFT);
            item.setFont(AssetStyles.FONT_NORMAL);
            item.setForeground(AssetStyles.FONT_COLOR);
            item.setIconTextGap(8);
            list.add(item);
        }
        return list;
    }

    private JPanel createSeasonListPanel(String title, List<Season> constants) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
        titleLabel.setFont(AssetStyles.FONT_BOLD);
        titleLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        panel.add(titleLabel);
        
        for (Season constant : constants) {
            JLabel item = new JLabel(constant.getName(), constant.getIcon(), SwingConstants.LEFT);
            item.setFont(AssetStyles.FONT_NORMAL);
            item.setForeground(AssetStyles.FONT_COLOR);
            panel.add(item);
        }
        return panel;
    }

    private JPanel createWeatherListPanel(String title, List<Weather> constants) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
        titleLabel.setFont(AssetStyles.FONT_BOLD);
        titleLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        panel.add(titleLabel);
        
        for (Weather constant : constants) {
            JLabel item = new JLabel(constant.getName(), constant.getIcon(), SwingConstants.LEFT);
            item.setFont(AssetStyles.FONT_NORMAL);
            item.setForeground(AssetStyles.FONT_COLOR);
            panel.add(item);
        }
        return panel;
    }

    private JPanel createTimeOfDayListPanel(String title, List<TimeOfDay> constants) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
        titleLabel.setFont(AssetStyles.FONT_BOLD);
        titleLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        panel.add(titleLabel);
        
        for (TimeOfDay constant : constants) {
            JLabel item = new JLabel(constant.getName(), constant.getIcon(), SwingConstants.LEFT);
            item.setFont(AssetStyles.FONT_NORMAL);
            item.setForeground(AssetStyles.FONT_COLOR);
            panel.add(item);
        }
        return panel;
    }

    private JPanel createMoonPhaseListPanel(String title, List<MoonPhase> constants) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
        titleLabel.setFont(AssetStyles.FONT_BOLD);
        titleLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        panel.add(titleLabel);
        
        for (MoonPhase constant : constants) {
            JLabel item = new JLabel(constant.getName(), constant.getIcon(), SwingConstants.LEFT);
            item.setFont(AssetStyles.FONT_NORMAL);
            item.setForeground(AssetStyles.FONT_COLOR);
            panel.add(item);
        }
        return panel;
    }

    private JPanel createTemperatureListPanel(String title, List<Temperature> constants) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
        titleLabel.setFont(AssetStyles.FONT_BOLD);
        titleLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        panel.add(titleLabel);
        
        for (Temperature constant : constants) {
            JLabel item = new JLabel(constant.getName() + " (Max " + constant.getMaxTemp() + "°C)", constant.getIcon(), SwingConstants.LEFT);
            item.setFont(AssetStyles.FONT_NORMAL);
            item.setForeground(AssetStyles.FONT_COLOR);
            panel.add(item);
        }
        return panel;
    }

    private JPanel createHumidityListPanel(String title, List<Humidity> constants) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
        titleLabel.setFont(AssetStyles.FONT_BOLD);
        titleLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        panel.add(titleLabel);
        
        for (Humidity constant : constants) {
            JLabel item = new JLabel(constant.getName(), constant.getIcon(), SwingConstants.LEFT);
            item.setFont(AssetStyles.FONT_NORMAL);
            item.setForeground(AssetStyles.FONT_COLOR);
            panel.add(item);
        }
        return panel;
    }

    private static ImageIcon sourceSpriteTier(ResourceType res, int tier) {
        switch (tier) {
            case 0:
                return res.getSourceSpriteSmall();
            case 1:
                return res.getSourceSpriteMedium();
            case 2:
                return res.getSourceSpriteBig();
            case 3:
                return res.getSourceSpriteHuge();
            default:
                return null;
        }
    }

    private JComponent createWorldPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Resources: one header row (icon + name per resource, like original), then rows of pile sizes (small..huge) aligned in columns
        JPanel resourcesPanel = new JPanel();
        resourcesPanel.setLayout(new BoxLayout(resourcesPanel, BoxLayout.Y_AXIS));
        resourcesPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        resourcesPanel.setBorder(BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, LanguageStrings.get("PANEL_RESOURCES"), 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
                javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                AssetStyles.FONT_BOLD, AssetStyles.FONT_COLOR_HEADER));

        List<ResourceType> resourceList = GameConstants.getResources();
        int n = resourceList.size();

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(AssetStyles.BACKGROUND_COLOR);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 8, 4, 8);
        gc.anchor = GridBagConstraints.CENTER;

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        JPanel corner = new JPanel();
        corner.setBackground(AssetStyles.BACKGROUND_COLOR);
        grid.add(corner, gc);

        for (int i = 0; i < n; i++) {
            ResourceType res = resourceList.get(i);
            gc.gridx = i + 1;
            gc.gridy = 0;
            gc.weightx = 1.0 / Math.max(1, n);
            gc.anchor = GridBagConstraints.WEST;
            JLabel hdr = new JLabel(res.getName(), res.getIcon(), SwingConstants.LEFT);
            hdr.setFont(AssetStyles.FONT_NORMAL);
            hdr.setForeground(AssetStyles.FONT_COLOR);
            grid.add(hdr, gc);
        }
        gc.anchor = GridBagConstraints.CENTER;

        String[] sizeKeys = {
                LanguageStrings.HELP_RESOURCE_SOURCE_SMALL,
                LanguageStrings.HELP_RESOURCE_SOURCE_MEDIUM,
                LanguageStrings.HELP_RESOURCE_SOURCE_BIG,
                LanguageStrings.HELP_RESOURCE_SOURCE_HUGE
        };
        for (int r = 0; r < 4; r++) {
            gc.gridx = 0;
            gc.gridy = r + 1;
            gc.weightx = 0;
            gc.anchor = GridBagConstraints.EAST;
            JLabel sizeLabel = new JLabel(LanguageStrings.get(sizeKeys[r]));
            sizeLabel.setFont(AssetStyles.FONT_SMALL);
            sizeLabel.setForeground(AssetStyles.FONT_COLOR);
            grid.add(sizeLabel, gc);
            gc.anchor = GridBagConstraints.CENTER;
            for (int i = 0; i < n; i++) {
                ResourceType res = resourceList.get(i);
                gc.gridx = i + 1;
                gc.weightx = 1.0 / Math.max(1, n);
                ImageIcon sp = sourceSpriteTier(res, r);
                grid.add(new JLabel(sp), gc);
            }
        }

        resourcesPanel.add(grid);
        panel.add(resourcesPanel);

        // Biomes
        JPanel biomesPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        biomesPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        biomesPanel.setBorder(BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, LanguageStrings.get("HELP_BIOMES_TITLE"), 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
                javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                AssetStyles.FONT_BOLD, AssetStyles.FONT_COLOR_HEADER));
        
        for (Biome b : GameConstants.getBiomes()) {
            String bInfo = helpHtml("width:120px;",
                    "<b>" + b.getName() + "</b><br>"
                            + LanguageStrings.get("HELP_BIOME_TEMP") + b.getTemperature() + "°C<br>"
                            + LanguageStrings.get("HELP_BIOME_HUMID") + b.isIsHumid() + "/5<br>"
                            + LanguageStrings.get("RESOURCE_PLANT") + ": " + b.getPlantAbundance() + "x<br>"
                            + LanguageStrings.get("RESOURCE_MEAT") + ": " + b.getAnimalAbundance() + "x<br>"
                            + LanguageStrings.get("RESOURCE_ROCK") + ": " + b.getMineralAbundance() + "x");
            JLabel bLabel = new JLabel(bInfo, b.getIcon(), SwingConstants.LEFT);
            bLabel.setFont(AssetStyles.FONT_SMALL);
            bLabel.setForeground(AssetStyles.FONT_COLOR);
            biomesPanel.add(bLabel);
        }
        panel.add(biomesPanel);

        // World Info (Seasons, Weather, etc.)
        JPanel worldInfoPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        worldInfoPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        worldInfoPanel.setBorder(BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, LanguageStrings.get("HELP_WORLD_INFO_TITLE"), 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
                javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                AssetStyles.FONT_BOLD, AssetStyles.FONT_COLOR_HEADER));
        
        worldInfoPanel.add(createSeasonListPanel(LanguageStrings.get("HELP_SEASONS"), GameConstants.getSeasons()));
        worldInfoPanel.add(createWeatherListPanel(LanguageStrings.get("HELP_WEATHER"), GameConstants.getWeathers()));
        worldInfoPanel.add(createTimeOfDayListPanel(LanguageStrings.get("HELP_TIME_DAY"), GameConstants.getTimesOfDay()));
        worldInfoPanel.add(createMoonPhaseListPanel(LanguageStrings.get("HELP_MOON"), GameConstants.getMoonPhases()));
        worldInfoPanel.add(createTemperatureListPanel(LanguageStrings.get("HELP_TEMP"), GameConstants.getTemperature()));
        worldInfoPanel.add(createHumidityListPanel(LanguageStrings.get("HELP_HUMID"), GameConstants.getHumidity()));

        panel.add(worldInfoPanel);
        
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JComponent createUiControlsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel intro = new JLabel(tutorialHtml(LanguageStrings.get(LanguageStrings.HELP_UI_INTRO)));
        styleTutorialLabel(intro);
        intro.setBorder(new EmptyBorder(0, 0, 10, 0));
        intro.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(intro);

        panel.add(createUiControlBarSection());
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createGameSpeedIconsSection());

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JPanel createUiControlBarSection() {
        JPanel section = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        section.setBackground(AssetStyles.BACKGROUND_COLOR);
        section.setBorder(BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, LanguageStrings.get(LanguageStrings.HELP_UI_BAR_TITLE),
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                AssetStyles.FONT_BOLD, AssetStyles.FONT_COLOR_HEADER));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        section.add(buildUiControlCell(GameConstants.ICON_SPEED_DOWN, LanguageStrings.UI_CONTROL_SPEED_DOWN_TT));
        section.add(buildUiControlCell(GameConstants.ICON_SPEED_UP, LanguageStrings.UI_CONTROL_SPEED_UP_TT));
        section.add(buildUiControlCell(GameConstants.SPEED_NORMAL.getIcon(), LanguageStrings.UI_CONTROL_SPEED_TT,
                GameConstants.SPEED_NORMAL.getName(), GameConstants.SPEED_NORMAL.getDelayMs()));
        section.add(buildUiControlCell(GameConstants.ICON_SPEED_PAUSE, LanguageStrings.UI_CONTROL_PAUSE_TT));
        section.add(buildUiControlCell(GameConstants.ICON_SPEED_PLAY, LanguageStrings.UI_CONTROL_PLAY_TT));
        section.add(buildUiMenuControlCell());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(AssetStyles.BACKGROUND_COLOR);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(section, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createGameSpeedIconsSection() {
        JPanel grid = new JPanel(new GridLayout(0, 3, 10, 10));
        grid.setBackground(AssetStyles.BACKGROUND_COLOR);
        grid.setBorder(BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, LanguageStrings.get(LanguageStrings.HELP_UI_SPEEDS_TITLE),
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                AssetStyles.FONT_BOLD, AssetStyles.FONT_COLOR_HEADER));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        grid.add(buildSpeedIconCell(LanguageStrings.get(LanguageStrings.HELP_UI_SPEED_PAUSED),
                GameConstants.ICON_SPEED_ZERO,
                LanguageStrings.get(LanguageStrings.UI_CONTROL_PAUSED_TT)));

        for (GameSpeed speed : GameConstants.getGameSpeeds()) {
            grid.add(buildSpeedIconCell(speed.getName(), speed.getIcon(),
                    LanguageStrings.format(LanguageStrings.HELP_UI_SPEED_MS,
                            AssetStyles.formatNumber(speed.getDelayMs()))));
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(AssetStyles.BACKGROUND_COLOR);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildUiControlCell(ImageIcon icon, String descKey) {
        return buildUiControlCell(icon, null, descKey, null, null);
    }

    private JPanel buildUiControlCell(ImageIcon icon, String descFormatKey, String speedName, int delayMs) {
        return buildUiControlCell(icon, descFormatKey, null, speedName, delayMs);
    }

    private JPanel buildUiControlCell(ImageIcon icon, String descFormatKey, String descKey,
            String speedName, Integer delayMs) {
        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setBackground(AssetStyles.BACKGROUND_COLOR);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLabel.setBorder(new EmptyBorder(0, 0, 4, 0));
        cell.add(iconLabel);

        String desc = descKey != null
                ? LanguageStrings.get(descKey)
                : LanguageStrings.format(descFormatKey, speedName, AssetStyles.formatNumber(delayMs));
        JLabel descLabel = new JLabel(helpHtml("width:110px;font-size:10pt;text-align:center;", desc));
        descLabel.setFont(AssetStyles.FONT_SMALL);
        descLabel.setForeground(AssetStyles.FONT_COLOR);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cell.add(descLabel);
        return cell;
    }

    private JPanel buildUiMenuControlCell() {
        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setBackground(AssetStyles.BACKGROUND_COLOR);

        JButton menuPreview = new JButton(LanguageStrings.get(LanguageStrings.UI_MENU));
        menuPreview.setEnabled(false);
        menuPreview.setFocusable(false);
        AssetStyles.styleButton(menuPreview);
        menuPreview.setAlignmentX(Component.CENTER_ALIGNMENT);
        cell.add(menuPreview);

        JLabel descLabel = new JLabel(helpHtml("width:110px;font-size:10pt;text-align:center;",
                LanguageStrings.get(LanguageStrings.UI_CONTROL_MENU_TT)));
        descLabel.setFont(AssetStyles.FONT_SMALL);
        descLabel.setForeground(AssetStyles.FONT_COLOR);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
        cell.add(descLabel);
        return cell;
    }

    private JLabel buildSpeedIconCell(String title, ImageIcon icon, String detail) {
        String info = helpHtml("width:120px;font-size:11pt;",
                "<b>" + title + "</b><br>" + detail);
        JLabel label = new JLabel(info, icon, SwingConstants.LEFT);
        label.setFont(AssetStyles.FONT_SMALL);
        label.setForeground(AssetStyles.FONT_COLOR);
        label.setIconTextGap(8);
        return label;
    }

    private JComponent createDictionaryPanel(List<Constant> items) {
        return createDictionaryPanel(items, true);
    }

    private JComponent createDictionaryPanel(List<Constant> items, boolean showIcons) {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JList<Constant> list = new JList<>();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBackground(AssetStyles.BACKGROUND_COLOR);
        list.setForeground(AssetStyles.FONT_COLOR);
        list.setSelectionBackground(AssetStyles.BACKGROUND_SECONDARY);
        list.setSelectionForeground(AssetStyles.FONT_COLOR_HEADER);
        list.setFont(AssetStyles.FONT_NORMAL);
        
        DefaultListModel<Constant> model = new DefaultListModel<>();
        
        List<Constant> sortedItems = new ArrayList<>(items);
        Collections.sort(sortedItems, (a, b) -> {
            String nameA = (a instanceof Upgrade) ? ((Upgrade) a).getFlavorName() : a.getName();
            String nameB = (b instanceof Upgrade) ? ((Upgrade) b).getFlavorName() : b.getName();
            return nameA.compareTo(nameB);
        });
        
        for (Constant item : sortedItems) {
            model.addElement(item);
        }
        
        list.setModel(model);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Constant) {
                    Constant c = (Constant) value;
                    setText((c instanceof Upgrade) ? ((Upgrade) c).getFlavorName() : c.getName());
                    setIcon(showIcons ? c.getIcon() : null);
                }
                return this;
            }
        });
        
        JScrollPane listScrollPane = new JScrollPane(list);
        listScrollPane.setMinimumSize(new Dimension(200, 100));
        splitPane.setLeftComponent(listScrollPane);

        JEditorPane descriptionArea = new JEditorPane();
        descriptionArea.setEditorKit(new HTMLEditorKit());
        descriptionArea.setEditable(false);
        descriptionArea.setFocusable(false);
        descriptionArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        descriptionArea.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JScrollPane textScrollPane = new JScrollPane(descriptionArea);
        textScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        splitPane.setRightComponent(textScrollPane);
        
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    Constant selected = list.getSelectedValue();
                    if (selected == null) {
                        descriptionArea.setText(helpHtml("font-size:11pt;", LanguageStrings.get("HELP_SELECT_ITEM")));
                        return;
                    }
                    
                    StringBuilder body = new StringBuilder();
                    
                    if (selected instanceof Upgrade) {
                        Upgrade u = (Upgrade) selected;
                        body.append("<b>").append(u.getFlavorName()).append("</b><br><br>");
                        body.append(u.getDescription()).append("<br><br>");
                        body.append("<b>").append(LanguageStrings.get("UI_COST")).append(":</b> ").append(AssetStyles.formatNumber(u.getCost())).append(" RP");
                        if (u.getRequirement() != null) {
                            body.append("<br><b>").append(LanguageStrings.get("UI_REQUIREMENTS")).append(":</b> ").append(u.getRequirement().getFlavorName());
                        }
                    } else if (selected instanceof Building) {
                        Building b = (Building) selected;
                        body.append("<b>").append(b.getName()).append("</b><br><br>");
                        body.append(b.getDescription());
                        if (b.getBuildTime() > 0) {
                             body.append("<br><br><b>").append(LanguageStrings.get("HELP_BUILD_BASE_COST")).append(":</b><br>");
                             body.append(AssetStyles.formatNumber(b.getMineralCost())).append(" ").append(LanguageStrings.get("RESOURCE_ROCK")).append(", ");
                             body.append(AssetStyles.formatNumber(b.getResinCost())).append(" ").append(LanguageStrings.get("RESOURCE_RESIN")).append(", ");
                             body.append(AssetStyles.formatNumber(b.getBuildTime())).append(" Hours");
                        }
                    } else if (selected instanceof Assimilation) {
                        Assimilation a = (Assimilation) selected;
                        body.append("<b>").append(a.getName()).append("</b><br><br>");
                        body.append(a.getDescription()).append("<br><br>");
                        body.append("<b>").append(LanguageStrings.get("UI_COST")).append(":</b> ").append(AssetStyles.formatNumber(a.getCost())).append(" RP");
                    }
                    
                    descriptionArea.setText(helpHtml("font-size:11pt;width:250px;", body.toString()));
                    descriptionArea.setCaretPosition(0);
                }
            }
        });
        
        splitPane.setDividerLocation(250);
        return splitPane;
    }

    public static void showTutorialDialog(Component parent) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(window, LanguageStrings.get("HELP_TUTORIAL_TITLE"), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());

        JPanel cardPanel = new JPanel(new CardLayout());
        cardPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        // --- Page 1: Story ---
        String story = helpHtml("width:350px;font-size:12pt;", "<p>" + LanguageStrings.get("HELP_WELCOME_STORY") + "</p>");
        JPanel page1 = new JPanel(new BorderLayout());
        page1.setBackground(AssetStyles.BACKGROUND_COLOR);
        page1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel storyLabel = new JLabel(story);
        storyLabel.setForeground(AssetStyles.FONT_COLOR);
        page1.add(storyLabel, BorderLayout.CENTER);

        // --- Page 2: Game Info ---
        String gameInfo = helpHtml("width:350px;font-size:11pt;", "<p>" + LanguageStrings.get("HELP_TUTORIAL_TIPS") + "</p>");
        JPanel page2 = new JPanel(new BorderLayout());
        page2.setBackground(AssetStyles.BACKGROUND_COLOR);
        page2.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel infoLabel = new JLabel(gameInfo);
        infoLabel.setForeground(AssetStyles.FONT_COLOR);
        page2.add(infoLabel, BorderLayout.CENTER);
        
        // --- Page 3: Threats & Mechanics ---
        String threatInfo = helpHtml("width:350px;font-size:11pt;", "<p>" + LanguageStrings.get("HELP_TUTORIAL_THREATS") + "</p>");
        JPanel page3 = new JPanel(new BorderLayout());
        page3.setBackground(AssetStyles.BACKGROUND_COLOR);
        page3.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel threatLabel = new JLabel(threatInfo);
        threatLabel.setForeground(AssetStyles.FONT_COLOR);
        page3.add(threatLabel, BorderLayout.CENTER);

        // --- Page 4: Dynasty Management ---
        String empireInfo = helpHtml("width:350px;font-size:11pt;", "<p>" + LanguageStrings.get("HELP_TUTORIAL_DYNASTY") + "</p>");
        JPanel page4 = new JPanel(new BorderLayout());
        page4.setBackground(AssetStyles.BACKGROUND_COLOR);
        page4.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel empireLabel = new JLabel(empireInfo);
        empireLabel.setForeground(AssetStyles.FONT_COLOR);
        page4.add(empireLabel, BorderLayout.CENTER);

        // --- Page 5: Hotkeys ---
        JPanel hotkeyPanel = new JPanel(new GridLayout(0, 2, 10, 5)); 
        hotkeyPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        hotkeyPanel.setBorder(BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, LanguageStrings.get("HELP_TAB_HOTKEYS"),
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                AssetStyles.FONT_BOLD, AssetStyles.FONT_COLOR_HEADER));

        Object[][] keysData = {
            {LanguageStrings.get("HOTKEY_PAUSE_LABEL"), "Spacebar"},
            {LanguageStrings.get("HOTKEY_ESC_LABEL"), "ESC"},
            {LanguageStrings.get("HOTKEY_VIEW_LABEL"), "Z"},
            {LanguageStrings.get("HOTKEY_ROLES_LABEL"), "Q-T"},
            {LanguageStrings.get("HOTKEY_UPGRADES_LABEL"), "Y-O"},
            {LanguageStrings.get("HOTKEY_P_LABEL"), "P"},
            {LanguageStrings.get("HOTKEY_ABILITIES_LABEL"), "C"},
            {LanguageStrings.get("HOTKEY_DYNASTY_LABEL"), "A / S / D / F"},
            {LanguageStrings.get("HOTKEY_M_LABEL"), "M"},
            {LanguageStrings.get("HOTKEY_X_LABEL"), "X"}
        };
        
        for (Object[] row : keysData) {
            JLabel k = new JLabel((String)row[0]);
            k.setFont(AssetStyles.FONT_BOLD);
            k.setForeground(AssetStyles.FONT_COLOR_HEADER);
            hotkeyPanel.add(k);
            
            JLabel v = new JLabel((String)row[1]);
            v.setFont(AssetStyles.FONT_NORMAL);
            v.setForeground(AssetStyles.FONT_COLOR);
            hotkeyPanel.add(v);
        }
        
        hotkeyPanel.add(AssetStyles.createInternalSeparator()); hotkeyPanel.add(AssetStyles.createInternalSeparator());
        
        JPanel page5 = new JPanel(new BorderLayout());
        page5.setBackground(AssetStyles.BACKGROUND_COLOR);
        page5.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        page5.add(hotkeyPanel, BorderLayout.CENTER);

        cardPanel.add(page1, "0");
        cardPanel.add(page2, "1");
        cardPanel.add(page3, "2");
        cardPanel.add(page4, "3");
        cardPanel.add(page5, "4");

        // --- Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JButton skipBtn = new JButton(LanguageStrings.get("HELP_SKIP_TUTORIAL"));
        JButton backBtn = new JButton("< " + LanguageStrings.get("UI_BACK"));
        JButton nextBtn = new JButton(LanguageStrings.get("UI_NEXT") + " >");
        JButton finishBtn = new JButton(LanguageStrings.get("UI_FINISH"));
        
        for (JButton btn : new JButton[]{skipBtn, backBtn, nextBtn, finishBtn}) {
            AssetStyles.styleButton(btn);
        }

        skipBtn.addActionListener(e -> dialog.dispose());
        finishBtn.addActionListener(e -> dialog.dispose());

        final int[] currentPage = {0};
        final int MAX_PAGES = 5;

        Runnable updateButtons = () -> {
            backBtn.setEnabled(currentPage[0] > 0);
            if (currentPage[0] == MAX_PAGES - 1) {
                nextBtn.setVisible(false);
                finishBtn.setVisible(true);
            } else {
                nextBtn.setVisible(true);
                finishBtn.setVisible(false);
            }
            CardLayout cl = (CardLayout) cardPanel.getLayout();
            cl.show(cardPanel, String.valueOf(currentPage[0]));
        };

        backBtn.addActionListener(e -> {
            if (currentPage[0] > 0) {
                currentPage[0]--;
                updateButtons.run();
            }
        });

        nextBtn.addActionListener(e -> {
            if (currentPage[0] < MAX_PAGES - 1) {
                currentPage[0]++;
                updateButtons.run();
            }
        });

        buttonPanel.add(skipBtn);
        buttonPanel.add(Box.createHorizontalStrut(20)); 
        buttonPanel.add(backBtn);
        buttonPanel.add(nextBtn);
        buttonPanel.add(finishBtn);

        updateButtons.run();

        dialog.add(cardPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        UiDialogUtils.show(dialog, parent);
    }

    public static void showRoadmapDialog(Component parent) {
        showTextFileDialog(parent, LanguageStrings.ROADMAP_TITLE,
                ClasspathTextFiles.load("/meta/roadmap.txt", LanguageStrings.ROADMAP_UNAVAILABLE));
    }

    public static void showCreditsDialog(Component parent) {
        showTextFileDialog(parent, LanguageStrings.CREDITS_TITLE,
                ClasspathTextFiles.load("/meta/credits.txt", LanguageStrings.CREDITS_UNAVAILABLE));
    }

    public static void showAuditDialog(Component parent) {
        showTextFileDialog(parent, LanguageStrings.AUDIT_TITLE,
                ClasspathTextFiles.load(AssetStyles.META_AUDIT, LanguageStrings.AUDIT_UNAVAILABLE));
    }

    private static void showTextFileDialog(Component parent, String titleKey, String text) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(window, LanguageStrings.get(titleKey), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setLineWrap(false);
        textArea.setFont(AssetStyles.FONT_MONOSPACED);
        textArea.setBackground(AssetStyles.BACKGROUND_COLOR);
        textArea.setForeground(AssetStyles.FONT_COLOR);
        textArea.setBorder(new EmptyBorder(12, 12, 12, 12));
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(720, 520));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        dialog.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(AssetStyles.BACKGROUND_COLOR);

        JButton closeButton = new JButton(LanguageStrings.get(LanguageStrings.UI_CLOSE));
        AssetStyles.styleButton(closeButton);
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        UiDialogUtils.show(dialog, parent);
    }
}
