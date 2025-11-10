package com.grimidk.formicempire.classes.interfaces.game;

import com.grimidk.formicempire.classes.constants.AntRole;
import com.grimidk.formicempire.classes.constants.AntType;
import com.grimidk.formicempire.classes.constants.Upgrade; 
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.GameUpgrades;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

public class RoleManagementDialog extends JDialog {

    private final Colony colony;
    private final JTabbedPane tabbedPane = new JTabbedPane();

    public RoleManagementDialog(JFrame owner, Colony colony) {
        super(owner, "Manage Ant Roles", true);
        this.colony = colony;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(550, 500));
        
        add(tabbedPane, BorderLayout.CENTER);
        add(createSouthPanel(), BorderLayout.SOUTH);
        
        initTabs(); 
        initKeyBindings();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                tabbedPane.requestFocusInWindow();
            }
        });
        
        pack();
        setLocationRelativeTo(owner);
    }
    
    private JPanel createSouthPanel() {
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(closeButton);
        return southPanel;
    }
    
    private void initTabs() {
        if (colony.hasUpgrade(GameUpgrades.TYPE_WORKER)) {
            tabbedPane.addTab(GameConstants.TYPE_WORKER.getName(), GameConstants.TYPE_WORKER.getIcon(), createRolePanel(GameConstants.TYPE_WORKER));
        }
        if (colony.hasUpgrade(GameUpgrades.TYPE_SOLDIER)) {
            tabbedPane.addTab(GameConstants.TYPE_SOLDIER.getName(), GameConstants.TYPE_SOLDIER.getIcon(), createRolePanel(GameConstants.TYPE_SOLDIER));
        }
        if (colony.hasUpgrade(GameUpgrades.TYPE_MAJOR)) {
            tabbedPane.addTab(GameConstants.TYPE_MAJOR.getName(), GameConstants.TYPE_MAJOR.getIcon(), createRolePanel(GameConstants.TYPE_MAJOR));
        }
        if (colony.hasUpgrade(GameUpgrades.TYPE_PRINCESS)) {
            tabbedPane.addTab(GameConstants.TYPE_PRINCESS.getName(), GameConstants.TYPE_PRINCESS.getIcon(), createRolePanel(GameConstants.TYPE_PRINCESS));
        }
        if (colony.hasUpgrade(GameUpgrades.TYPE_QUEEN)) {
            tabbedPane.addTab(GameConstants.TYPE_QUEEN.getName(), GameConstants.TYPE_QUEEN.getIcon(), createRolePanel(GameConstants.TYPE_QUEEN));
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

    private Upgrade getUpgradeForRole(AntRole role) {
        if (role == GameConstants.ROLE_FORAGER) return GameUpgrades.ROLE_FORAGER;
        if (role == GameConstants.ROLE_NURSE) return GameUpgrades.ROLE_NURSE;
        if (role == GameConstants.ROLE_FARMER) return GameUpgrades.ROLE_FARMER;
        if (role == GameConstants.ROLE_GRAVER) return GameUpgrades.ROLE_GRAVER;
        if (role == GameConstants.ROLE_HUNTER) return GameUpgrades.ROLE_HUNTER;
        if (role == GameConstants.ROLE_LAYER) return GameUpgrades.ROLE_LAYER;
        if (role == GameConstants.ROLE_RANCHER) return GameUpgrades.ROLE_RANCHER;
        if (role == GameConstants.ROLE_BUILDER) return GameUpgrades.ROLE_BUILDER;
        if (role == GameConstants.ROLE_BREEDER) return GameUpgrades.ROLE_BREEDER;
        if (role == GameConstants.ROLE_RESEARCHER) return GameUpgrades.ROLE_RESEARCHER;

        return null; 
    }

    private JPanel createRolePanel(AntType antType) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        int totalAnts = colony.getAntsByType(antType).size();
        
        JLabel totalLabel = new JLabel("Total " + antType.getName() + "s: " + totalAnts);
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
        JLabel assignedLabel = new JLabel("Total Assigned: 0");
        JLabel unassignedLabel = new JLabel("Unassigned: " + totalAnts);
        
        panel.add(totalLabel);
        panel.add(assignedLabel);
        panel.add(unassignedLabel);
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));
        
        Map<AntRole, JSpinner> spinnerMap = new HashMap<>();

        for (AntRole role : GameConstants.getAntRoles()) {
            if (role.getAntType() == antType) {
                
                Upgrade roleUpgrade = getUpgradeForRole(role);
                
                if (roleUpgrade != null && colony.hasUpgrade(roleUpgrade)) {
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

                        int newTotalAssigned = newValue + otherSpinnersTotal;
                        if (newTotalAssigned > totalAnts) {
                            int allowedValue = Math.max(0, totalAnts - otherSpinnersTotal);
                            SwingUtilities.invokeLater(() -> spinner.setValue(allowedValue));
                            newValue = allowedValue;
                        }
                        
                        colony.setAssignedRoleCount(role, newValue);
                        updateRolePanelTotals(totalAnts, assignedLabel, unassignedLabel, spinnerMap);
                    });
                    
                    disableSpinnerLetterInput(spinner);
                    
                    roleRow.add(spinner);
                    spinnerMap.put(role, spinner);
                    panel.add(roleRow);
                }
            }
        }
        
        updateRolePanelTotals(totalAnts, assignedLabel, unassignedLabel, spinnerMap);
        return panel;
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
    
    private void updateRolePanelTotals(int totalAnts, JLabel assignedLabel, JLabel unassignedLabel, Map<AntRole, JSpinner> spinnerMap) {
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
    
    public void showDialog(int tabIndex) {
        selectTab(tabIndex);
        setVisible(true);
    }
}