package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

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
    private JPanel synergyPanel;

    private final Map<Integer, Integer> tabIndexMap = new HashMap<>();
    
    private int targetTab = -1;

    public UpgradeDialog(JFrame owner, Colony colony) {
        super(owner, "Colony Upgrades", new Dimension(650, 600));
        this.colony = colony;

        tabbedPane = new JTabbedPane();
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

    @Override
    protected void refreshDialog() {
        tabbedPane.removeAll();
        tabIndexMap.clear();

        int currentIndex = 0;

        // --- Research Tab ---
        if (colony.hasUpgrade(GameUnlocks.ABILITY_RESEARCH)) {
            if (researchPanel == null) researchPanel = new ResearchPanel(colony);
            researchPanel.updateData();
            tabbedPane.addTab("Research", GameConstants.ROLE_RESEARCHER.getIcon(), researchPanel);
            tabIndexMap.put(TAB_RESEARCH, currentIndex++);
        }

        // --- Build Tab ---
        if (colony.hasUpgrade(GameUnlocks.ABILITY_BUILD)) {
            if (buildPanel == null) buildPanel = new BuildPanel(colony);
            buildPanel.updateData();
            tabbedPane.addTab("Construction", GameConstants.ROLE_BUILDER.getIcon(), buildPanel);
            tabIndexMap.put(TAB_BUILD, currentIndex++);
        }

        // --- Assimilations Tab ---
        if (false) { // To implement
            if (assimilationPanel == null) {
                assimilationPanel = createPlaceholderPanel("Assimilations - Coming Soon");
            }
            tabbedPane.addTab("Assimilations", null, assimilationPanel);
            tabIndexMap.put(TAB_ASSIMILATION, currentIndex++);
        }

        // --- Synergies Tab ---
        if (false) { // To implement
            if (synergyPanel == null) {
                synergyPanel = createPlaceholderPanel("Synergies - Coming Soon");
            }
            tabbedPane.addTab("Synergies", null, synergyPanel);
            tabIndexMap.put(TAB_SYNERGY, currentIndex++);
        }
        
        if (targetTab != -1 && tabIndexMap.containsKey(targetTab)) {
            tabbedPane.setSelectedIndex(tabIndexMap.get(targetTab));
            targetTab = -1; 
        }
    }

    private JPanel createPlaceholderPanel(String message) {
        JPanel p = new JPanel(new GridBagLayout());
        p.add(new JLabel(message));
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

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, 0), "switchToResearch");
        actionMap.put("switchToResearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tabIndexMap.containsKey(TAB_RESEARCH)) {
                    tabbedPane.setSelectedIndex(tabIndexMap.get(TAB_RESEARCH));
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0), "switchToBuild");
        actionMap.put("switchToBuild", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tabIndexMap.containsKey(TAB_BUILD)) {
                    tabbedPane.setSelectedIndex(tabIndexMap.get(TAB_BUILD));
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

            JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            northPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            researchPointsLabel = new JLabel();
            researchPointsLabel.setFont(researchPointsLabel.getFont().deriveFont(Font.BOLD));
            northPanel.add(researchPointsLabel);
            add(northPanel, BorderLayout.NORTH);

            listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            scrollPane = new JScrollPane(listPanel);
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
                listPanel.add(new JLabel("  No new research available at this time."));
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
            researchPointsLabel.setText("Available Research Points: " + colony.getResearchPoints());
        }

        private JPanel createUpgradePanel(Upgrade upgrade, int currentRP) {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(new TitledBorder(upgrade.getFlavorName()));

            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

            JTextArea descriptionArea = new JTextArea(upgrade.getDescription());
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setLineWrap(true);
            descriptionArea.setEditable(false);
            descriptionArea.setFocusable(false);
            descriptionArea.setBackground(panel.getBackground());
            descriptionArea.setFont(infoPanel.getFont());
            descriptionArea.setBorder(null);
            infoPanel.add(descriptionArea);
            panel.add(infoPanel, BorderLayout.CENTER);

            JPanel actionPanel = new JPanel();
            actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
            actionPanel.setBorder(new EmptyBorder(0, 0, 0, 5));

            JButton purchaseButton = new JButton("Buy");
            purchaseButton.setFocusable(false);
            if (currentRP < upgrade.getCost()) {
                purchaseButton.setEnabled(false);
                purchaseButton.setToolTipText("Not enough Research Points");
            }

            purchaseButton.addActionListener(e -> {
                if (colony.getResearchPoints() >= upgrade.getCost()) {
                    colony.setResearchPoints(colony.getResearchPoints() - upgrade.getCost());
                    colony.unlockUpgrade(upgrade);
                    updateData();
                }
            });

            JLabel costLabel = new JLabel(upgrade.getCost() + " RP");
            costLabel.setFont(costLabel.getFont().deriveFont(Font.BOLD));
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
                    button.setToolTipText("Not enough Research Points");
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
        private final Map<JButton, Building> buttonBuildingMap = new HashMap<>();

        public BuildPanel(Colony colony) {
            super(new BorderLayout());
            this.colony = colony;

            JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            northPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            mineralsLabel = new JLabel();
            mineralsLabel.setIcon(GameConstants.ROCK_RESOURCE.getIcon());
            resinLabel = new JLabel();
            resinLabel.setIcon(GameConstants.RESIN_RESOURCE.getIcon());
            buildersLabel = new JLabel();
            buildersLabel.setIcon(GameConstants.ROLE_BUILDER.getIcon());

            northPanel.add(mineralsLabel);
            northPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            northPanel.add(resinLabel);
            northPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            northPanel.add(buildersLabel);
            add(northPanel, BorderLayout.NORTH);

            listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            scrollPane = new JScrollPane(listPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            add(scrollPane, BorderLayout.CENTER);
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
                    listPanel.add(new JLabel("  No new constructions available at this time."));
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
            mineralsLabel.setText(colony.getMinerals() + "/" + colony.getMineralsCapacity());
            resinLabel.setText(colony.getResins() + "/" + colony.getResinsCapacity());
            buildersLabel.setText(colony.getAssignedRoleCount(GameConstants.ROLE_BUILDER) + " Builders");
        }

        private JPanel createBuildingPanel(Building building) {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(new TitledBorder(building.getName()));

            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

            JTextArea descriptionArea = new JTextArea(building.getDescription());
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setLineWrap(true);
            descriptionArea.setEditable(false);
            descriptionArea.setFocusable(false);
            descriptionArea.setBackground(panel.getBackground());
            descriptionArea.setFont(infoPanel.getFont());
            descriptionArea.setBorder(null);
            infoPanel.add(descriptionArea);
            panel.add(infoPanel, BorderLayout.CENTER);

            JPanel actionPanel = new JPanel();
            actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
            actionPanel.setBorder(new EmptyBorder(0, 0, 0, 5));

            JButton purchaseButton = new JButton("Build");
            purchaseButton.setFocusable(false);

            buttonBuildingMap.put(purchaseButton, building);
            updateBuildButtonState(purchaseButton, building);

            purchaseButton.addActionListener(e -> {
                if (colony.startBuildingProject(building)) {
                    updateData();
                }
            });

            String costString = String.format("<html>%d Minerals<br>%d Resin<br>%d Hours (base)</html>", building.getMineralCost(), building.getResinCost(), building.getBuildTime());

            JLabel costLabel = new JLabel(costString);
            costLabel.setFont(costLabel.getFont().deriveFont(Font.BOLD));
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
            panel.setBorder(new TitledBorder("Under Construction: " + project.getName()));

            int builderCount = colony.getAssignedRoleCount(GameConstants.ROLE_BUILDER);
            double efficiency = (builderCount > 0) ? (builderCount / 100.0) : 0.0;
            double requiredHours = (efficiency > 0) ? (project.getBuildTime() / efficiency) : Double.POSITIVE_INFINITY;
            double progressHours = colony.getBuildingProgressHours();

            int progressPercent = 0;
            if (requiredHours > 0 && !Double.isInfinite(requiredHours)) {
                progressPercent = (int) ((progressHours / requiredHours) * 100);
            }

            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setValue(progressPercent);
            progressBar.setStringPainted(true);
            progressBar.setString(String.format("%.1f / %.1f Hours", progressHours, requiredHours));

            panel.add(progressBar, BorderLayout.CENTER);

            JLabel buildersLabel = new JLabel(String.format("%d Builders (%.0f%% speed)", builderCount, efficiency * 100));
            buildersLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(buildersLabel, BorderLayout.SOUTH);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.setFocusable(false);
            cancelButton.addActionListener(e -> {
                colony.setMinerals(colony.getMinerals() + project.getMineralCost());
                colony.setResins(colony.getResins() + project.getResinCost());
                colony.setCurrentBuildingProject(null);
                colony.setBuildingProgressHours(0.0);
                updateData();
            });

            JPanel eastPanel = new JPanel(new GridBagLayout());
            eastPanel.add(cancelButton);
            panel.add(eastPanel, BorderLayout.EAST);

            return panel;
        }

        private void updateBuildButtonState(JButton button, Building building) {
            int builders = colony.getAssignedRoleCount(GameConstants.ROLE_BUILDER);
            int minerals = colony.getMinerals();
            int resin = colony.getResins();

            if (builders <= 0) {
                button.setEnabled(false);
                button.setToolTipText("You need at least 1 Builder assigned.");
            } else if (minerals < building.getMineralCost() || resin < building.getResinCost()) {
                button.setEnabled(false);
                button.setToolTipText("Not enough resources.");
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
                    if (b instanceof TitledBorder && ((TitledBorder) b).getTitle().contains(currentProject.getName())) {
                        JProgressBar progressBar = (JProgressBar) progressPanel.getComponent(0);
                        JLabel buildersLabel = (JLabel) progressPanel.getComponent(1);

                        int builderCount = colony.getAssignedRoleCount(GameConstants.ROLE_BUILDER);
                        double efficiency = (builderCount > 0) ? (builderCount / 100.0) : 0.0;
                        double requiredHours = (efficiency > 0) ? (currentProject.getBuildTime() / efficiency) : Double.POSITIVE_INFINITY;
                        double progressHours = colony.getBuildingProgressHours();

                        int progressPercent = 0;
                        if (requiredHours > 0 && !Double.isInfinite(requiredHours)) {
                            progressPercent = (int) ((progressHours / requiredHours) * 100);
                        }

                        progressBar.setValue(progressPercent);
                        progressBar.setString(String.format("%.1f / %.1f Hours", progressHours, requiredHours));
                        buildersLabel.setText(String.format("%d Builders (%.0f%% speed)", builderCount, efficiency * 100));
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
}