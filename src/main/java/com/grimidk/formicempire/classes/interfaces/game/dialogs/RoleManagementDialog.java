package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RoleManagementDialog extends ZeroDialog {

    private final Colony colony;
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final List<RolePanel> rolePanels = new ArrayList<>();
    private final Set<AntType> initializedTypes = new HashSet<>();

    public RoleManagementDialog(JFrame owner, Colony colony) {
        super(owner, "Manage Ant Roles", new Dimension(550, 500));
        this.colony = colony;

        add(tabbedPane, BorderLayout.CENTER);
        
        initTabs(); 
        initKeyBindings();
        initListeners(); 
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                tabbedPane.requestFocusInWindow();
            }
        });
    }

    @Override
    protected void refreshDialog() {
        initTabs();
        for (RolePanel panel : rolePanels) {
            panel.updateData();
        }
    }
    
    public void showDialog(int tabIndex) {
        refreshDialog(); 
        selectTab(tabIndex);
        super.showDialog();
    }
    
    private void initTabs() {
        addRoleTab(GameConstants.TYPE_WORKER, GameUnlocks.TYPE_WORKER);
        addRoleTab(GameConstants.TYPE_SOLDIER, GameUnlocks.TYPE_SOLDIER);
        addRoleTab(GameConstants.TYPE_MAJOR, GameUnlocks.TYPE_MAJOR);
        addRoleTab(GameConstants.TYPE_PRINCESS, GameUnlocks.TYPE_PRINCESS);
        addRoleTab(GameConstants.TYPE_QUEEN, GameUnlocks.TYPE_QUEEN);
    }
    
    private void initListeners() {
        tabbedPane.addChangeListener(e -> {
            Component selected = tabbedPane.getSelectedComponent();
            if (selected instanceof RolePanel) {
                ((RolePanel) selected).updateData();
            }
        });
    }
    
    private void addRoleTab(AntType type, Upgrade requiredUpgrade) {
        if (initializedTypes.contains(type)) {
            return;
        }

        if (colony.hasUpgrade(requiredUpgrade)) {
            RolePanel panel = new RolePanel(colony, type);
            rolePanels.add(panel);
            tabbedPane.addTab(type.getName(), type.getIcon(), panel);
            initializedTypes.add(type);
        }
    }
    
    public void selectTab(int tabIndex) {
        if (tabIndex >= 0 && tabIndex < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(tabIndex);
        }
    }

    private void initKeyBindings() {
        InputMap inputMap = tabbedPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = tabbedPane.getActionMap();
        
        addTabSwitchAction(inputMap, actionMap, "selectTab1", KeyEvent.VK_Q, 0);
        addTabSwitchAction(inputMap, actionMap, "selectTab2", KeyEvent.VK_W, 1);
        addTabSwitchAction(inputMap, actionMap, "selectTab3", KeyEvent.VK_E, 2);
        addTabSwitchAction(inputMap, actionMap, "selectTab4", KeyEvent.VK_R, 3);
        addTabSwitchAction(inputMap, actionMap, "selectTab5", KeyEvent.VK_T, 4);
    }
    
    private void addTabSwitchAction(InputMap im, ActionMap am, String name, int key, int index) {
        im.put(KeyStroke.getKeyStroke(key, 0), name);
        am.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tabbedPane.getTabCount() > index) {
                    tabbedPane.setSelectedIndex(index);
                }
            }
        });
    }

    private static AntRole getDefaultRoleForType(AntType type) {
        if (type == GameConstants.TYPE_WORKER) return GameConstants.ROLE_FORAGER;
        else if (type == GameConstants.TYPE_SOLDIER) return GameConstants.ROLE_HUNTER;
        else if (type == GameConstants.TYPE_MAJOR) return GameConstants.ROLE_BRUTE; 
        else if (type == GameConstants.TYPE_PRINCESS) return GameConstants.ROLE_BREEDER;
        else if (type == GameConstants.TYPE_DRONE) return GameConstants.ROLE_DRONE;
        else if (type == GameConstants.TYPE_QUEEN) return GameConstants.ROLE_LAYER;
        return null; 
    }

    private static Upgrade getUpgradeForRole(AntRole role) {
        if (role == GameConstants.ROLE_FORAGER) return GameUnlocks.ROLE_FORAGER;
        if (role == GameConstants.ROLE_SCOUT) return GameUnlocks.ROLE_SCOUT;
        if (role == GameConstants.ROLE_NURSE) return GameUnlocks.ROLE_NURSE;
        if (role == GameConstants.ROLE_FARMER) return GameUnlocks.ROLE_FARMER;
        if (role == GameConstants.ROLE_GRAVER) return GameUnlocks.ROLE_GRAVER;
        if (role == GameConstants.ROLE_HUNTER) return GameUnlocks.ROLE_HUNTER;
        if (role == GameConstants.ROLE_LAYER) return GameUnlocks.ROLE_LAYER;
        if (role == GameConstants.ROLE_RANCHER) return GameUnlocks.ROLE_RANCHER;
        if (role == GameConstants.ROLE_BUILDER) return GameUnlocks.ROLE_BUILDER;
        if (role == GameConstants.ROLE_BREEDER) return GameUnlocks.ROLE_BREEDER;
        if (role == GameConstants.ROLE_RESEARCHER) return GameUnlocks.ROLE_RESEARCHER;
        if (role == GameConstants.ROLE_ASSISTANT) return GameUnlocks.ROLE_ASSISTANT;
        if (role == GameConstants.ROLE_POLICE) return GameUnlocks.ROLE_POLICE;

        return null; 
    }

    @Override
    public void liveUpdate() {
        if (!isShowing()) {
            return;
        }
        initTabs();
        for (RolePanel panel : rolePanels) {
            panel.updateData();
        }
    }
    
    private static class RolePanel extends JPanel {
        private final Colony colony;
        private final AntType antType;
        private final JLabel totalLabel;
        private final JLabel assignedLabel;
        private final JLabel unassignedLabel;
        private final Map<AntRole, JSpinner> spinnerMap = new HashMap<>();
        
        private final Set<AntRole> displayedRoles = new HashSet<>();

        RolePanel(Colony colony, AntType antType) {
            this.colony = colony;
            this.antType = antType;

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            int totalAnts = colony.getAntsByType(antType).size();
            
            totalLabel = new JLabel("Total " + antType.getName() + "s: " + totalAnts);
            totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
            assignedLabel = new JLabel("Total Assigned: 0");
            unassignedLabel = new JLabel("Unassigned: " + totalAnts);
            
            add(totalLabel);
            add(assignedLabel);
            add(unassignedLabel);
            add(new JSeparator(SwingConstants.HORIZONTAL));

            checkAndAddRoles();
            
            updateData();
        }
        
        private void checkAndAddRoles() {
            boolean addedAny = false;
            for (AntRole role : GameConstants.getAntRoles()) {
                if (role.getAntType() == antType) {
                    if (displayedRoles.contains(role)) continue;

                    Upgrade roleUpgrade = getUpgradeForRole(role);
                    
                    if (roleUpgrade != null && colony.hasUpgrade(roleUpgrade)) {
                        addRoleRow(role);
                        displayedRoles.add(role);
                        addedAny = true;
                    }
                }
            }
            if (addedAny) {
                revalidate();
                repaint();
            }
        }

        private void addRoleRow(AntRole role) {
            JPanel roleRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            roleRow.add(new JLabel(role.getName() + ":"));
            
            int currentAssigned = colony.getAssignedRoleCount(role);
            SpinnerModel model = new SpinnerNumberModel(currentAssigned, 0, Integer.MAX_VALUE, 1); 
            JSpinner spinner = new JSpinner(model);
            spinner.setPreferredSize(new Dimension(80, 25));

            spinner.addChangeListener(e -> {
                int newValue = (Integer) spinner.getValue();
                
                int otherSpinnersTotal = 0;
                for (Map.Entry<AntRole, JSpinner> entry : spinnerMap.entrySet()) {
                    if (entry.getValue() != spinner) {
                        otherSpinnersTotal += (Integer) entry.getValue().getValue();
                    }
                }

                int currentTotalAnts = RolePanel.this.colony.getAntsByType(RolePanel.this.antType).size();
                int newTotalAssigned = newValue + otherSpinnersTotal;

                if (newTotalAssigned > currentTotalAnts) {

                    AntRole defaultRole = getDefaultRoleForType(antType);
                    
                    if (defaultRole != null && !role.equals(defaultRole) && spinnerMap.containsKey(defaultRole)) {
                        
                        int deficit = newTotalAssigned - currentTotalAnts;
                        JSpinner defaultSpinner = spinnerMap.get(defaultRole);
                        int defaultCount = (Integer) defaultSpinner.getValue();
                        
                        if (defaultCount >= deficit) {
                            int newDefaultCount = defaultCount - deficit;
                            defaultSpinner.setValue(newDefaultCount);
                            colony.setAssignedRoleCount(defaultRole, newDefaultCount);
                            
                            colony.setAssignedRoleCount(role, newValue);
                            updateData();
                            return; 
                        }
                    }
                    
                    int allowedValue = Math.max(0, currentTotalAnts - otherSpinnersTotal);
                    
                    final int finalAllowed = allowedValue;
                    SwingUtilities.invokeLater(() -> spinner.setValue(finalAllowed));
                    
                    newValue = allowedValue;
                }
                
                colony.setAssignedRoleCount(role, newValue);
                updateData();
            });
            
            disableSpinnerLetterInput(spinner);
            
            roleRow.add(spinner);
            spinnerMap.put(role, spinner);
            add(roleRow);
        }
        
        void updateData() {
            checkAndAddRoles();
            
            for (Map.Entry<AntRole, JSpinner> entry : spinnerMap.entrySet()) {
                int colonyValue = colony.getAssignedRoleCount(entry.getKey());
                if ((Integer)entry.getValue().getValue() != colonyValue) {
                    entry.getValue().setValue(colonyValue);
                }
            }
            
            int totalAnts = colony.getAntsByType(antType).size();
            totalLabel.setText("Total " + antType.getName() + "s: " + totalAnts);

            int totalAssigned = 0;
            for (JSpinner s : spinnerMap.values()) {
                totalAssigned += (Integer) s.getValue();
            }
            
            int unassigned = totalAnts - totalAssigned;
            
            assignedLabel.setText("Total Assigned: " + totalAssigned);
            unassignedLabel.setText("Unassigned: " + unassigned);

            if (totalAssigned > totalAnts) {
                assignedLabel.setForeground(Color.RED);
                assignedLabel.setToolTipText("You have assigned more roles than you have ants.");
                unassignedLabel.setForeground(Color.RED);
                unassignedLabel.setToolTipText("You have assigned more roles than you have ants.");
            } else {
                assignedLabel.setForeground(Color.BLACK);
                assignedLabel.setToolTipText(null);
                unassignedLabel.setForeground(Color.BLACK);
                unassignedLabel.setToolTipText(null);
            }
        }
        
        private void disableSpinnerLetterInput(JSpinner spinner) {
            if (spinner.getEditor() instanceof JSpinner.DefaultEditor) {
                JFormattedTextField textField = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
                textField.addKeyListener(new java.awt.event.KeyAdapter() {
                    public void keyTyped(java.awt.event.KeyEvent e) {
                        if (Character.isLetter(e.getKeyChar())) {
                            e.consume(); 
                        }
                    }
                });
            }
        }
    }
}