package com.grimidk.formicempire.classes.interfaces.game;

import com.grimidk.formicempire.classes.constants.Building;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.GameUpgrades;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class BuildDialog extends JDialog {

    private final Colony colony;
    private final JPanel listPanel;
    private final JLabel mineralsLabel;
    private final JLabel resinLabel;
    private final JLabel buildersLabel;
    private JButton buildButton; 
    private Building buildingToBuild;

    public BuildDialog(JFrame owner, Colony colony) {
        super(owner, "Colony Construction", true);
        this.colony = colony;
        
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(600, 500));

        // Header Panel (shows current resources)
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

        // Center Panel (list of buildings or current project)
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // South Panel (Close button)
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        southPanel.add(closeButton);
        add(southPanel, BorderLayout.SOUTH);
        
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        pack();
        setLocationRelativeTo(owner);
    }

    private void refreshDialog() {
        listPanel.removeAll();
        buildButton = null;
        buildingToBuild = null;
        
        updateResourceLabels();

        Building currentProject = colony.getCurrentBuildingProject();

        if (currentProject != null) {
            // --- Show Current Project Progress ---
            listPanel.add(createProgressPanel(currentProject));
        } else {
            // --- Show Available Buildings ---
            List<Building> allBuildings = GameUpgrades.getBuildings();
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
    }
    
    private void updateResourceLabels() {
        mineralsLabel.setText(colony.getMinerals() + "/" + colony.getMineralsCapacity());
        resinLabel.setText(colony.getResins() + "/" + colony.getResinsCapacity());
        buildersLabel.setText(colony.getAssignedRoleCount(GameConstants.ROLE_BUILDER) + " Builders");
    }

    private JPanel createBuildingPanel(Building building) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new TitledBorder(building.getName()));

        // Info Panel (Description)
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

        // Action Panel (Button, Cost)
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBorder(new EmptyBorder(0, 0, 0, 5));
        
        JButton purchaseButton = new JButton("Build");
        purchaseButton.setFocusable(false);
        
        this.buildButton = purchaseButton; 
        this.buildingToBuild = building; 
        updateBuildButtonState(purchaseButton, building);

        purchaseButton.addActionListener(e -> {
            if (colony.startBuildingProject(building)) {
                refreshDialog();
            }
        });

        // Cost Labels
        String costString = String.format("<html>%d Minerals<br>%d Resin<br>%d Hours (base)</html>",
            building.getMineralCost(), building.getResinCost(), building.getBuildTime());
        
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

    public void liveUpdate() {
        if (!isShowing()) {
            return;
        }

        updateResourceLabels();
        
        Building currentProject = colony.getCurrentBuildingProject();
        if (currentProject != null) {
            if (!(listPanel.getComponent(0) instanceof JPanel) || 
                !(((TitledBorder)((JPanel)listPanel.getComponent(0)).getBorder()).getTitle().contains(currentProject.getName()))) {
                refreshDialog();
            } else {
                JPanel progressPanel = (JPanel) listPanel.getComponent(0);
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
            }
        } else {
            if (buildButton != null && buildingToBuild != null) {
                updateBuildButtonState(buildButton, buildingToBuild);
            } else if (listPanel.getComponentCount() > 0 && listPanel.getComponent(0) instanceof JPanel) {
                refreshDialog();
            }
        }
    }

    public void showDialog() {
        refreshDialog();
        setVisible(true);
    }
}