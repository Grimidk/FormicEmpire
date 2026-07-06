package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Synergy;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastySynergyService;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpgradeDialog extends ZeroDialog {

    public static final int TAB_RESEARCH = 0;
    public static final int TAB_BUILD = 1;
    public static final int TAB_ASSIMILATION = 2;
    public static final int TAB_SYNERGY = 3;

    private final Colony colony;
    private final JTabbedPane tabbedPane;
    
    private ResearchPanel researchPanel;
    private BuildPanel buildPanel;
    private JPanel assimilationPanel;
    private SynergyPanel synergyPanel;

    private final Map<Integer, Integer> tabIndexMap = new HashMap<>();
    
    private int targetTab = -1;

    public UpgradeDialog(JFrame owner, Colony colony) {
        super(owner, LanguageStrings.DIALOG_UPGRADES_TITLE, AssetStyles.DEFAULT_DIALOG_SIZE);
        this.colony = colony;

        tabbedPane = new JTabbedPane();
        AssetStyles.styleTabbedPane(tabbedPane);
        add(tabbedPane, BorderLayout.CENTER);

        initKeyBindings();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                tabbedPane.requestFocusInWindow();
            }
        });
    }

    public void showDialog(int preferredType) {
        this.targetTab = preferredType;
        super.showDialog();
    }

    public void setTab(int tabType) {
        if (tabIndexMap.containsKey(tabType)) {
            tabbedPane.setSelectedIndex(tabIndexMap.get(tabType));
        }
    }
    
    public boolean isTabOpen(int tabType) {
        if (!isShowing()) return false;
        Integer index = tabIndexMap.get(tabType);
        return index != null && tabbedPane.getSelectedIndex() == index;
    }

    @Override
    public void refreshTheme() {
        super.refreshTheme();
        AssetStyles.styleTabbedPane(tabbedPane);
        tabbedPane.updateUI();
    }

    @Override
    protected void refreshDialog() {
        tabbedPane.removeAll();
        tabIndexMap.clear();

        int currentIndex = 0;

        // --- Research Tab ---
        if (colony.hasUpgrade(GameUnlocks.ABILITY_RESEARCH)) {
            if (researchPanel == null) researchPanel = new ResearchPanel(colony);
            researchPanel.updateData();
            tabbedPane.addTab(LanguageStrings.get(LanguageStrings.TAB_RESEARCH), GameConstants.ROLE_RESEARCHER.getIcon(), researchPanel);
            tabIndexMap.put(TAB_RESEARCH, currentIndex++);
        }

        // --- Build Tab ---
        if (colony.hasUpgrade(GameUnlocks.ROLE_BUILDER)) {
            if (buildPanel == null) buildPanel = new BuildPanel(colony);
            buildPanel.updateData();
            tabbedPane.addTab(LanguageStrings.get(LanguageStrings.TAB_CONSTRUCTION), GameConstants.ROLE_BUILDER.getIcon(), buildPanel);
            tabIndexMap.put(TAB_BUILD, currentIndex++);
        }

        // --- Assimilations Tab ---
        if (colony.hasUpgrade(GameUnlocks.ABILITY_ASSIMILATION)) { 
            if (assimilationPanel == null) {
                assimilationPanel = new AssimilationPanel(colony);
            }
            ((AssimilationPanel) assimilationPanel).updateData();
            tabbedPane.addTab(LanguageStrings.get(LanguageStrings.TAB_ASSIMILATIONS), null, assimilationPanel);
            tabIndexMap.put(TAB_ASSIMILATION, currentIndex++);
        }

        // --- Synergies Tab ---
        if (colony.getDynasty() != null && DynastySynergyService.hasAnyUnlocked(colony.getDynasty())) {
            if (synergyPanel == null) {
                synergyPanel = new SynergyPanel(colony);
            }
            synergyPanel.updateData();
            tabbedPane.addTab(LanguageStrings.get(LanguageStrings.TAB_SYNERGIES), null, synergyPanel);
            tabIndexMap.put(TAB_SYNERGY, currentIndex++);
        }
        
        if (targetTab != -1 && tabIndexMap.containsKey(targetTab)) {
            tabbedPane.setSelectedIndex(tabIndexMap.get(targetTab));
            targetTab = -1; 
        }
    }

    private JPanel createPlaceholderPanel(String message) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(AssetStyles.BACKGROUND_COLOR);
        JLabel label = new JLabel(message);
        label.setForeground(AssetStyles.FONT_COLOR);
        p.add(label);
        return p;
    }

    @Override
    public void liveUpdate() {
        if (!isShowing()) return;

        Component selected = tabbedPane.getSelectedComponent();
        if (selected instanceof LiveUpdatePanel) {
            ((LiveUpdatePanel) selected).liveUpdate();
        }
    }

    private void initKeyBindings() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, 0), "toggleResearch");
        actionMap.put("toggleResearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isTabOpen(TAB_RESEARCH)) {
                    dispose();
                } else if (tabIndexMap.containsKey(TAB_RESEARCH)) {
                    tabbedPane.setSelectedIndex(tabIndexMap.get(TAB_RESEARCH));
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0), "toggleBuild");
        actionMap.put("toggleBuild", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isTabOpen(TAB_BUILD)) {
                    dispose();
                } else if (tabIndexMap.containsKey(TAB_BUILD)) {
                    tabbedPane.setSelectedIndex(tabIndexMap.get(TAB_BUILD));
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0), "toggleAssimilation");
        actionMap.put("toggleAssimilation", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isTabOpen(TAB_ASSIMILATION)) {
                    dispose();
                } else if (tabIndexMap.containsKey(TAB_ASSIMILATION)) {
                    tabbedPane.setSelectedIndex(tabIndexMap.get(TAB_ASSIMILATION));
                }
            }
        });

    }

    interface LiveUpdatePanel {
        void liveUpdate();
        void updateData();
    }

    private class ResearchPanel extends JPanel implements LiveUpdatePanel {
        private final Colony colony;
        private final JPanel listPanel;
        private final JLabel researchPointsLabel;
        private final JScrollPane scrollPane;
        private final Map<JButton, Upgrade> buttonUpgradeMap = new HashMap<>();

        public ResearchPanel(Colony colony) {
            super(new BorderLayout());
            this.colony = colony;
            setBackground(AssetStyles.BACKGROUND_COLOR);

            JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            northPanel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            northPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            researchPointsLabel = new JLabel();
            researchPointsLabel.setFont(AssetStyles.FONT_BOLD);
            researchPointsLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
            northPanel.add(researchPointsLabel);
            add(northPanel, BorderLayout.NORTH);

            listPanel = new JPanel();
            listPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            scrollPane = new JScrollPane(listPanel);
            scrollPane.getViewport().setBackground(AssetStyles.BACKGROUND_COLOR);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            add(scrollPane, BorderLayout.CENTER);
        }

        @Override
        public void updateData() {
            listPanel.removeAll();
            buttonUpgradeMap.clear();

            updateResearchPointsLabel();

            int currentRP = colony.getResearchPoints();
            List<Upgrade> allUpgrades = GameUnlocks.getUpgrades();
            List<Upgrade> availableUpgrades = new ArrayList<>();

            for (Upgrade upgrade : allUpgrades) {
                boolean owned = colony.hasUpgrade(upgrade);
                boolean reqMet = (upgrade.getRequirement() == null || colony.hasUpgrade(upgrade.getRequirement()));

                if (!owned && reqMet && upgrade.getCost() > 0) {
                    availableUpgrades.add(upgrade);
                }
            }

            availableUpgrades.sort((u1, u2) -> Integer.compare(u1.getCost(), u2.getCost()));

            if (availableUpgrades.isEmpty()) {
                JLabel emptyLabel = new JLabel(LanguageStrings.get(LanguageStrings.UPGRADE_NO_RESEARCH));
                emptyLabel.setForeground(AssetStyles.FONT_COLOR);
                listPanel.add(emptyLabel);
            } else {
                for (Upgrade upgrade : availableUpgrades) {
                    listPanel.add(createUpgradePanel(upgrade, currentRP));
                    listPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                }
            }

            listPanel.revalidate();
            listPanel.repaint();
            
            SwingUtilities.invokeLater(() -> scrollPane.getViewport().setViewPosition(new Point(0, 0)));
        }

        private void updateResearchPointsLabel() {
            researchPointsLabel.setText(LanguageStrings.format(LanguageStrings.UPGRADE_RESEARCH_AVAILABLE, colony.getResearchPoints()));
        }

        private JPanel createUpgradePanel(Upgrade upgrade, int currentRP) {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            
            TitledBorder border = new TitledBorder(AssetStyles.PANEL_BORDER, upgrade.getFlavorName());
            border.setTitleColor(AssetStyles.FONT_COLOR_HEADER);
            border.setTitleFont(AssetStyles.FONT_BOLD);
            panel.setBorder(border);

            JPanel infoPanel = new JPanel();
            infoPanel.setOpaque(false);
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

            JTextArea descriptionArea = new JTextArea(upgrade.getDescription());
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setLineWrap(true);
            descriptionArea.setEditable(false);
            descriptionArea.setFocusable(false);
            descriptionArea.setBackground(panel.getBackground());
            descriptionArea.setForeground(AssetStyles.FONT_COLOR);
            descriptionArea.setFont(AssetStyles.FONT_NORMAL);
            descriptionArea.setBorder(null);
            infoPanel.add(descriptionArea);
            panel.add(infoPanel, BorderLayout.CENTER);

            JPanel actionPanel = new JPanel();
            actionPanel.setOpaque(false);
            actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
            actionPanel.setBorder(new EmptyBorder(0, 0, 0, 5));

            JButton purchaseButton = new JButton(LanguageStrings.get(LanguageStrings.UI_BUY));
            purchaseButton.setFocusable(false);
            AssetStyles.styleButton(purchaseButton);
            if (currentRP < upgrade.getCost()) {
                purchaseButton.setEnabled(false);
                purchaseButton.setToolTipText(LanguageStrings.get(LanguageStrings.UPGRADE_NOT_ENOUGH_RP));
            }

            purchaseButton.addActionListener(e -> {
                if (colony.getResearchPoints() >= upgrade.getCost()) {
                    colony.setResearchPoints(colony.getResearchPoints() - upgrade.getCost());
                    colony.unlockUpgrade(upgrade);
                    
                    UpgradeDialog.this.refreshDialog();
                }
            });

            JLabel costLabel = new JLabel(LanguageStrings.format(LanguageStrings.UPGRADE_COST_RP, upgrade.getCost()));
            costLabel.setFont(AssetStyles.FONT_BOLD);
            costLabel.setForeground(AssetStyles.FONT_COLOR_VALUE);
            costLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            purchaseButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            actionPanel.add(costLabel);
            actionPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            actionPanel.add(purchaseButton);
            panel.add(actionPanel, BorderLayout.EAST);

            buttonUpgradeMap.put(purchaseButton, upgrade);

            return panel;
        }

        @Override
        public void liveUpdate() {
            updateResearchPointsLabel();
            int currentRP = colony.getResearchPoints();
            for (Map.Entry<JButton, Upgrade> entry : buttonUpgradeMap.entrySet()) {
                JButton button = entry.getKey();
                Upgrade upgrade = entry.getValue();

                if (currentRP < upgrade.getCost()) {
                    button.setEnabled(false);
                    button.setToolTipText(LanguageStrings.get(LanguageStrings.UPGRADE_NOT_ENOUGH_RP));
                } else {
                    button.setEnabled(true);
                    button.setToolTipText(null);
                }
            }
        }
    }

    private class BuildPanel extends JPanel implements LiveUpdatePanel {
        private final Colony colony;
        private final JPanel listPanel;
        private final JScrollPane scrollPane;
        private final JLabel mineralsLabel;
        private final JLabel resinLabel;
        private final JLabel buildersLabel;
        private final JLabel cranesLabel;
        private final Map<JButton, Building> buttonBuildingMap = new HashMap<>();

        public BuildPanel(Colony colony) {
            super(new BorderLayout());
            this.colony = colony;
            setBackground(AssetStyles.BACKGROUND_COLOR);

            JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            northPanel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            northPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            
            mineralsLabel = createStatusLabel(GameConstants.RESOURCE_ROCK.getIcon());
            resinLabel = createStatusLabel(GameConstants.RESOURCE_RESIN.getIcon());
            buildersLabel = createStatusLabel(GameConstants.ROLE_BUILDER.getIcon());
            cranesLabel = createStatusLabel(GameConstants.ROLE_CRANE.getIcon());

            northPanel.add(mineralsLabel);
            northPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            northPanel.add(resinLabel);
            northPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            northPanel.add(buildersLabel);
            northPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            northPanel.add(cranesLabel);
            add(northPanel, BorderLayout.NORTH);

            listPanel = new JPanel();
            listPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            scrollPane = new JScrollPane(listPanel);
            scrollPane.getViewport().setBackground(AssetStyles.BACKGROUND_COLOR);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            add(scrollPane, BorderLayout.CENTER);
        }
        
        private JLabel createStatusLabel(Icon icon) {
            JLabel label = new JLabel(icon);
            label.setFont(AssetStyles.FONT_NORMAL);
            label.setForeground(AssetStyles.FONT_COLOR);
            return label;
        }

        @Override
        public void updateData() {
            listPanel.removeAll();
            buttonBuildingMap.clear();

            updateResourceLabels();

            Building currentProject = colony.getCurrentBuildingProject();

            if (currentProject != null) {
                listPanel.add(createProgressPanel(currentProject));
            } else {
                List<Building> allBuildings = GameUnlocks.getBuildings();
                List<Building> availableBuildings = new ArrayList<>();

                for (Building building : allBuildings) {
                    boolean owned = colony.hasBuilding(building);
                    boolean reqMet = (building.getRequirement() == null || colony.hasBuilding(building.getRequirement()));

                    if (!owned && reqMet) {
                        availableBuildings.add(building);
                    }
                }

                availableBuildings.sort((b1, b2) -> Integer.compare(b1.getBuildTime(), b2.getBuildTime()));

                if (availableBuildings.isEmpty()) {
                    JLabel emptyLabel = new JLabel(LanguageStrings.get(LanguageStrings.BUILD_NO_CONSTRUCTIONS));
                    emptyLabel.setForeground(AssetStyles.FONT_COLOR);
                    listPanel.add(emptyLabel);
                } else {
                    for (Building building : availableBuildings) {
                        listPanel.add(createBuildingPanel(building));
                        listPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                    }
                }
            }

            listPanel.revalidate();
            listPanel.repaint();

            SwingUtilities.invokeLater(() -> scrollPane.getViewport().setViewPosition(new Point(0, 0)));
        }

        private void updateResourceLabels() {
            mineralsLabel.setText(AssetStyles.formatRatio(colony.getMinerals(), colony.getMineralsCapacity()));
            resinLabel.setText(AssetStyles.formatRatio(colony.getResins(), colony.getResinsCapacity()));
            buildersLabel.setText(LanguageStrings.format(LanguageStrings.BUILD_STATUS_BUILDERS, colony.getAssignedRoleCount(GameConstants.ROLE_BUILDER)));
            
            if (colony.hasUpgrade(GameUnlocks.ROLE_CRANE)) {
                cranesLabel.setVisible(true);
                cranesLabel.setText(LanguageStrings.format(LanguageStrings.BUILD_STATUS_CRANES, colony.getAssignedRoleCount(GameConstants.ROLE_CRANE)));
            } else {
                cranesLabel.setVisible(false);
            }
        }

        private JPanel createBuildingPanel(Building building) {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            
            TitledBorder border = new TitledBorder(AssetStyles.PANEL_BORDER, building.getName());
            border.setTitleColor(AssetStyles.FONT_COLOR_HEADER);
            border.setTitleFont(AssetStyles.FONT_BOLD);
            panel.setBorder(border);

            JPanel infoPanel = new JPanel();
            infoPanel.setOpaque(false);
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

            JTextArea descriptionArea = new JTextArea(building.getDescription());
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setLineWrap(true);
            descriptionArea.setEditable(false);
            descriptionArea.setFocusable(false);
            descriptionArea.setBackground(panel.getBackground());
            descriptionArea.setForeground(AssetStyles.FONT_COLOR);
            descriptionArea.setFont(AssetStyles.FONT_NORMAL);
            descriptionArea.setBorder(null);
            infoPanel.add(descriptionArea);
            panel.add(infoPanel, BorderLayout.CENTER);

            JPanel actionPanel = new JPanel();
            actionPanel.setOpaque(false);
            actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
            actionPanel.setBorder(new EmptyBorder(0, 0, 0, 5));

            JButton purchaseButton = new JButton(LanguageStrings.get(LanguageStrings.UI_BUILD));
            purchaseButton.setFocusable(false);
            AssetStyles.styleButton(purchaseButton);

            buttonBuildingMap.put(purchaseButton, building);
            updateBuildButtonState(purchaseButton, building);

            purchaseButton.addActionListener(e -> {
                if (colony.startBuildingProject(building)) {
                    updateData();
                }
            });

            String costString = LanguageStrings.format(LanguageStrings.BUILD_COST_FORMAT, building.getMineralCost(), building.getResinCost(), building.getBuildTime());

            JLabel costLabel = new JLabel(costString);
            costLabel.setFont(AssetStyles.FONT_BOLD);
            costLabel.setForeground(AssetStyles.FONT_COLOR_VALUE);
            costLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            purchaseButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            actionPanel.add(costLabel);
            actionPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            actionPanel.add(purchaseButton);
            panel.add(actionPanel, BorderLayout.EAST);

            return panel;
        }

        private JPanel createProgressPanel(Building project) {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            
            TitledBorder border = new TitledBorder(AssetStyles.PANEL_BORDER, LanguageStrings.format(LanguageStrings.BUILD_UNDER_CONSTRUCTION, project.getName()));
            border.setTitleColor(AssetStyles.FONT_COLOR_HEADER);
            border.setTitleFont(AssetStyles.FONT_BOLD);
            panel.setBorder(border);

            int builderCount = colony.getAssignedRoleCount(GameConstants.ROLE_BUILDER);
            int craneCount = colony.getAssignedRoleCount(GameConstants.ROLE_CRANE);
            double efficiency = colony.getConstructionEfficiency();
            double requiredHours = (efficiency > 0) ? (project.getBuildTime() / efficiency) : Double.POSITIVE_INFINITY;
            double progressHours = colony.getBuildingProgressHours();

            int progressPercent = 0;
            if (requiredHours > 0 && !Double.isInfinite(requiredHours)) {
                progressPercent = (int) ((progressHours / requiredHours) * 100);
            }

            JProgressBar progressBar = new JProgressBar(0, 100);
            AssetStyles.styleProgressBar(progressBar);
            progressBar.setValue(progressPercent);
            progressBar.setStringPainted(true);
            progressBar.setString(LanguageStrings.format(LanguageStrings.BUILD_PROGRESS_HOURS, progressHours, requiredHours));

            panel.add(progressBar, BorderLayout.CENTER);

            String buildersStr = LanguageStrings.format(LanguageStrings.BUILD_STATUS_BUILDERS, builderCount);
            if (colony.hasUpgrade(GameUnlocks.ROLE_CRANE)) {
                buildersStr += " & " + LanguageStrings.format(LanguageStrings.BUILD_STATUS_CRANES, craneCount);
            }
            JLabel progressLabel = new JLabel(LanguageStrings.format(LanguageStrings.BUILD_STATUS_SPEED, buildersStr, efficiency * 100));
            progressLabel.setForeground(AssetStyles.FONT_COLOR);
            progressLabel.setFont(AssetStyles.FONT_NORMAL);
            progressLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(progressLabel, BorderLayout.SOUTH);

            JButton cancelButton = new JButton(LanguageStrings.get(LanguageStrings.UI_CANCEL));
            cancelButton.setFocusable(false);
            AssetStyles.styleButton(cancelButton);
            cancelButton.addActionListener(e -> {
                colony.setMinerals(colony.getMinerals() + project.getMineralCost());
                colony.setResins(colony.getResins() + project.getResinCost());
                colony.setCurrentBuildingProject(null);
                colony.setBuildingProgressHours(0.0);
                updateData();
            });

            JPanel eastPanel = new JPanel(new GridBagLayout());
            eastPanel.setOpaque(false);
            eastPanel.add(cancelButton);
            panel.add(eastPanel, BorderLayout.EAST);

            return panel;
        }

        private void updateBuildButtonState(JButton button, Building building) {
            int builders = colony.getAssignedRoleCount(GameConstants.ROLE_BUILDER);
            int cranes = colony.getAssignedRoleCount(GameConstants.ROLE_CRANE);
            int minerals = colony.getMinerals();
            int resin = colony.getResins();

            if (builders <= 0 && cranes <= 0) {
                button.setEnabled(false);
                button.setToolTipText(LanguageStrings.get(LanguageStrings.BUILD_REQUIREMENT_ERROR));
            } else if (minerals < building.getMineralCost() || resin < building.getResinCost()) {
                button.setEnabled(false);
                button.setToolTipText(LanguageStrings.get(LanguageStrings.BUILD_RESOURCES_ERROR));
            } else {
                button.setEnabled(true);
                button.setToolTipText(null);
            }
        }

        @Override
        public void liveUpdate() {
            updateResourceLabels();
            Building currentProject = colony.getCurrentBuildingProject();
            if (currentProject != null) {
                if (listPanel.getComponentCount() > 0 && listPanel.getComponent(0) instanceof JPanel) {
                    JPanel progressPanel = (JPanel) listPanel.getComponent(0);
                    Border b = progressPanel.getBorder();
                    
                    if (b instanceof TitledBorder && ((TitledBorder) b).getTitle().contains(currentProject.getName())
                            && progressPanel.getComponentCount() > 0 
                            && progressPanel.getComponent(0) instanceof JProgressBar) {
                        
                        JProgressBar progressBar = (JProgressBar) progressPanel.getComponent(0);
                        JLabel progressLabel = (JLabel) progressPanel.getComponent(1);

                        int builderCount = colony.getAssignedRoleCount(GameConstants.ROLE_BUILDER);
                        int craneCount = colony.getAssignedRoleCount(GameConstants.ROLE_CRANE);
                        double efficiency = colony.getConstructionEfficiency();
                        double requiredHours = (efficiency > 0) ? (currentProject.getBuildTime() / efficiency) : Double.POSITIVE_INFINITY;
                        double progressHours = colony.getBuildingProgressHours();

                        int progressPercent = 0;
                        if (requiredHours > 0 && !Double.isInfinite(requiredHours)) {
                            progressPercent = (int) ((progressHours / requiredHours) * 100);
                        }

                        progressBar.setValue(progressPercent);
                        progressBar.setString(LanguageStrings.format(LanguageStrings.BUILD_PROGRESS_HOURS, progressHours, requiredHours));
                        
                        String buildersStr = LanguageStrings.format(LanguageStrings.BUILD_STATUS_BUILDERS, builderCount);
                        if (colony.hasUpgrade(GameUnlocks.ROLE_CRANE)) {
                            buildersStr += " & " + LanguageStrings.format(LanguageStrings.BUILD_STATUS_CRANES, craneCount);
                        }
                        progressLabel.setText(LanguageStrings.format(LanguageStrings.BUILD_STATUS_SPEED, buildersStr, efficiency * 100));
                    } else {
                        updateData();
                    }
                } else {
                    updateData();
                }
            } else {
                if (!buttonBuildingMap.isEmpty()) {
                    for (Map.Entry<JButton, Building> entry : buttonBuildingMap.entrySet()) {
                        updateBuildButtonState(entry.getKey(), entry.getValue());
                    }
                } else if (listPanel.getComponentCount() > 0 && listPanel.getComponent(0) instanceof JPanel) {
                    updateData();
                }
            }
        }
    }

    private class AssimilationPanel extends JPanel implements LiveUpdatePanel {
        private final Colony colony;
        private final JPanel listPanel;
        private final JScrollPane scrollPane;
        private final JLabel statusLabel;
        private final JLabel geneticIntegrityLabel;
        private final Map<JButton, Assimilation> buttonMap = new HashMap<>();

        public AssimilationPanel(Colony colony) {
            super(new BorderLayout());
            this.colony = colony;
            setBackground(AssetStyles.BACKGROUND_COLOR);

            JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
            northPanel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            statusLabel = new JLabel(LanguageStrings.format(LanguageStrings.ASSIMILATION_CURRENT, LanguageStrings.get(LanguageStrings.ASSIMILATION_NONE)));
            statusLabel.setFont(AssetStyles.FONT_BOLD);
            statusLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
            northPanel.add(statusLabel);

            geneticIntegrityLabel = new JLabel();
            geneticIntegrityLabel.setFont(AssetStyles.FONT_BOLD);
            geneticIntegrityLabel.setForeground(AssetStyles.FONT_COLOR_VALUE);
            geneticIntegrityLabel.setIcon(GameConstants.ICON_STAT_GENETIC_INTEGRITY);
            geneticIntegrityLabel.setIconTextGap(8);
            northPanel.add(geneticIntegrityLabel);
            add(northPanel, BorderLayout.NORTH);

            listPanel = new JPanel();
            listPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            scrollPane = new JScrollPane(listPanel);
            scrollPane.getViewport().setBackground(AssetStyles.BACKGROUND_COLOR);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            add(scrollPane, BorderLayout.CENTER);
        }

        @Override
        public void updateData() {
            listPanel.removeAll();
            buttonMap.clear();

            Dynasty dynasty = colony.getDynasty();
            if (dynasty == null) return;

            updateStatusLabel();

            if (dynasty.getCurrentAssimilation() != null) {
                listPanel.add(createProgressPanel(dynasty.getCurrentAssimilation()));
            } else {
                List<Assimilation> all = GameUnlocks.getAssimilations();
                List<Assimilation> available = new ArrayList<>();

                for (Assimilation a : all) {
                    int speciesId = -1;
                    for (Species s : GameConstants.getSpecies()) {
                        if (s.getAssimilation() == a) {
                            speciesId = s.getId();
                            break;
                        }
                    }

                    boolean defeated = (speciesId != -1 && dynasty.getDefeatedSpeciesIds().contains(speciesId));
                    boolean completed = dynasty.isAssimilationCompleted(a);

                    if (defeated && !completed) {
                        available.add(a);
                    }
                }

                if (available.isEmpty()) {
                    JLabel emptyLabel = new JLabel(LanguageStrings.get(LanguageStrings.ASSIMILATION_NO_GENOMES));
                    emptyLabel.setForeground(AssetStyles.FONT_COLOR);
                    listPanel.add(emptyLabel);
                } else {
                    for (Assimilation a : available) {
                        listPanel.add(createAssimilationCard(a));
                        listPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                    }
                }
            }

            listPanel.revalidate();
            listPanel.repaint();
        }

        private void updateStatusLabel() {
            Dynasty d = colony.getDynasty();
            if (d == null) return;
            if (d.getCurrentAssimilation() != null) {
                statusLabel.setText(LanguageStrings.format(LanguageStrings.ASSIMILATION_CURRENT, d.getCurrentAssimilation().getName()));
            } else {
                statusLabel.setText(LanguageStrings.format(LanguageStrings.ASSIMILATION_CURRENT, LanguageStrings.get(LanguageStrings.ASSIMILATION_NONE)));
            }
            geneticIntegrityLabel.setText(String.format("%s: %.1f%%",
                    LanguageStrings.get(LanguageStrings.STAT_GENETIC_INTEGRITY),
                    d.getGeneticIntegrity()));
        }

        private JPanel createAssimilationCard(Assimilation a) {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            
            TitledBorder border = new TitledBorder(AssetStyles.PANEL_BORDER, a.getName());
            border.setTitleColor(AssetStyles.FONT_COLOR_HEADER);
            border.setTitleFont(AssetStyles.FONT_BOLD);
            panel.setBorder(border);

            JTextArea desc = new JTextArea(a.getDescription());
            desc.setWrapStyleWord(true);
            desc.setLineWrap(true);
            desc.setEditable(false);
            desc.setForeground(AssetStyles.FONT_COLOR);
            desc.setFont(AssetStyles.FONT_NORMAL);
            desc.setBackground(panel.getBackground());
            panel.add(desc, BorderLayout.CENTER);

            JPanel east = new JPanel();
            east.setOpaque(false);
            east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));
            
            JLabel cost = new JLabel(LanguageStrings.format(LanguageStrings.ASSIMILATION_TARGET, a.getCost()));
            cost.setForeground(AssetStyles.FONT_COLOR_VALUE);
            cost.setFont(AssetStyles.FONT_BOLD);
            cost.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JButton btn = new JButton(LanguageStrings.get(LanguageStrings.UI_BEGIN));
            btn.setFocusable(false);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            AssetStyles.styleButton(btn);
            btn.addActionListener(e -> {
                colony.getDynasty().setCurrentAssimilation(a);
                colony.getDynasty().setAssimilationProgress(0);
                updateData();
            });

            east.add(cost);
            east.add(Box.createRigidArea(new Dimension(0, 5)));
            east.add(btn);
            panel.add(east, BorderLayout.EAST);

            return panel;
        }

        private JPanel createProgressPanel(Assimilation a) {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            
            TitledBorder border = new TitledBorder(AssetStyles.PANEL_BORDER, LanguageStrings.format(LanguageStrings.ASSIMILATION_ACTIVE, a.getName()));
            border.setTitleColor(AssetStyles.FONT_COLOR_HEADER);
            border.setTitleFont(AssetStyles.FONT_BOLD);
            panel.setBorder(border);

            double prog = colony.getDynasty().getAssimilationProgress();
            int percent = (int)((prog / a.getCost()) * 100);

            JProgressBar bar = new JProgressBar(0, 100);
            AssetStyles.styleProgressBar(bar);
            bar.setValue(percent);
            bar.setStringPainted(true);
            bar.setString(LanguageStrings.format(LanguageStrings.ASSIMILATION_PROGRESS, prog, a.getCost(), percent));
            panel.add(bar, BorderLayout.CENTER);

            JButton cancel = new JButton(LanguageStrings.get(LanguageStrings.UI_CANCEL));
            cancel.setFocusable(false);
            AssetStyles.styleButton(cancel);
            cancel.addActionListener(e -> {
                colony.getDynasty().setCurrentAssimilation(null);
                colony.getDynasty().setAssimilationProgress(0);
                updateData();
            });
            
            JPanel eastPanel = new JPanel(new GridBagLayout());
            eastPanel.setOpaque(false);
            eastPanel.add(cancel);
            panel.add(eastPanel, BorderLayout.EAST);

            JLabel info = new JLabel(LanguageStrings.get(LanguageStrings.ASSIMILATION_INFO));
            info.setForeground(AssetStyles.FONT_COLOR);
            info.setFont(AssetStyles.FONT_NORMAL);
            info.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(info, BorderLayout.SOUTH);

            return panel;
        }

        @Override
        public void liveUpdate() {
            Dynasty d = colony.getDynasty();
            if (d == null) {
                return;
            }
            geneticIntegrityLabel.setText(String.format("%s: %.1f%%",
                    LanguageStrings.get(LanguageStrings.STAT_GENETIC_INTEGRITY),
                    d.getGeneticIntegrity()));
            if (d.getCurrentAssimilation() == null) {
                if (listPanel.getComponentCount() > 0 && listPanel.getComponent(0) instanceof JPanel) {
                    JPanel p = (JPanel) listPanel.getComponent(0);
                    if (p.getBorder() instanceof TitledBorder && ((TitledBorder)p.getBorder()).getTitle().contains("Assimilating:")) {
                        updateData();
                    }
                }
                return;
            }

            Assimilation a = d.getCurrentAssimilation();
            if (listPanel.getComponentCount() > 0 && listPanel.getComponent(0) instanceof JPanel) {
                JPanel p = (JPanel) listPanel.getComponent(0);
                if (p.getComponentCount() > 0 && p.getComponent(0) instanceof JProgressBar) {
                    JProgressBar bar = (JProgressBar) p.getComponent(0);
                    double prog = d.getAssimilationProgress();
                    int percent = (int)((prog / a.getCost()) * 100);
                    bar.setValue(percent);
                    bar.setString(LanguageStrings.format(LanguageStrings.ASSIMILATION_PROGRESS, prog, a.getCost(), percent));
                }
            }
        }
    }

    private class SynergyPanel extends JPanel implements LiveUpdatePanel {
        private final Colony colony;
        private final JPanel listPanel;
        private final JScrollPane scrollPane;

        public SynergyPanel(Colony colony) {
            super(new BorderLayout());
            this.colony = colony;
            setBackground(AssetStyles.BACKGROUND_COLOR);

            listPanel = new JPanel();
            listPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            scrollPane = new JScrollPane(listPanel);
            scrollPane.getViewport().setBackground(AssetStyles.BACKGROUND_COLOR);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            add(scrollPane, BorderLayout.CENTER);
        }

        @Override
        public void updateData() {
            listPanel.removeAll();
            Dynasty dynasty = colony.getDynasty();
            if (dynasty == null) {
                listPanel.revalidate();
                listPanel.repaint();
                return;
            }

            List<Synergy> unlocked = new ArrayList<>();
            for (Synergy synergy : GameUnlocks.getSynergies()) {
                if (DynastySynergyService.isUnlocked(dynasty, synergy)) {
                    unlocked.add(synergy);
                }
            }

            for (Synergy synergy : unlocked) {
                listPanel.add(createSynergyCard(synergy));
                listPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }

            listPanel.revalidate();
            listPanel.repaint();
            SwingUtilities.invokeLater(() -> scrollPane.getViewport().setViewPosition(new Point(0, 0)));
        }

        private JPanel createSynergyCard(Synergy synergy) {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBackground(AssetStyles.BACKGROUND_SECONDARY);

            TitledBorder border = new TitledBorder(AssetStyles.PANEL_BORDER, synergy.getName());
            border.setTitleColor(AssetStyles.FONT_COLOR_HEADER);
            border.setTitleFont(AssetStyles.FONT_BOLD);
            panel.setBorder(border);

            JPanel infoPanel = new JPanel();
            infoPanel.setOpaque(false);
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

            JLabel requirements = new JLabel(LanguageStrings.format(
                    LanguageStrings.SYNERGY_REQUIREMENTS_FMT,
                    synergy.getRequirement1().getFlavorName(),
                    synergy.getRequirement2().getFlavorName()));
            requirements.setForeground(AssetStyles.FONT_COLOR_VALUE);
            requirements.setFont(AssetStyles.FONT_NORMAL);
            requirements.setAlignmentX(Component.LEFT_ALIGNMENT);
            infoPanel.add(requirements);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 6)));

            JTextArea descriptionArea = new JTextArea(synergy.getDescription());
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setLineWrap(true);
            descriptionArea.setEditable(false);
            descriptionArea.setFocusable(false);
            descriptionArea.setBackground(panel.getBackground());
            descriptionArea.setForeground(AssetStyles.FONT_COLOR);
            descriptionArea.setFont(AssetStyles.FONT_NORMAL);
            descriptionArea.setBorder(null);
            infoPanel.add(descriptionArea);
            panel.add(infoPanel, BorderLayout.CENTER);

            return panel;
        }

        @Override
        public void liveUpdate() {
        }
    }
}
