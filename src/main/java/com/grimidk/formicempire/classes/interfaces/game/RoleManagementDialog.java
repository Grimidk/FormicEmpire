package com.grimidk.formicempire.classes.interfaces.game;

import com.grimidk.formicempire.classes.constants.AntRole;
import com.grimidk.formicempire.classes.constants.AntType;
import com.grimidk.formicempire.classes.constants.Upgrade; 
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.GameUnlocks;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoleManagementDialog extends JDialog {

    private final Colony colony;
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final List<RolePanel> rolePanels = new ArrayList<>();

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
        
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        
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
        addRoleTab(GameConstants.TYPE_WORKER, GameUnlocks.TYPE_WORKER);
        addRoleTab(GameConstants.TYPE_SOLDIER, GameUnlocks.TYPE_SOLDIER);
        addRoleTab(GameConstants.TYPE_MAJOR, GameUnlocks.TYPE_MAJOR);
        addRoleTab(GameConstants.TYPE_PRINCESS, GameUnlocks.TYPE_PRINCESS);
        addRoleTab(GameConstants.TYPE_QUEEN, GameUnlocks.TYPE_QUEEN);
    }
    
    private void addRoleTab(AntType type, Upgrade requiredUpgrade) {
        if (colony.hasUpgrade(requiredUpgrade)) {
            RolePanel panel = new RolePanel(colony, type);
            rolePanels.add(panel);
            tabbedPane.addTab(type.getName(), type.getIcon(), panel);
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

    private static Upgrade getUpgradeForRole(AntRole role) {
        if (role == GameConstants.ROLE_FORAGER) return GameUnlocks.ROLE_FORAGER;
        if (role == GameConstants.ROLE_NURSE) return GameUnlocks.ROLE_NURSE;
        if (role == GameConstants.ROLE_FARMER) return GameUnlocks.ROLE_FARMER;
        if (role == GameConstants.ROLE_GRAVER) return GameUnlocks.ROLE_GRAVER;
        if (role == GameConstants.ROLE_HUNTER) return GameUnlocks.ROLE_HUNTER;
        if (role == GameConstants.ROLE_LAYER) return GameUnlocks.ROLE_LAYER;
        if (role == GameConstants.ROLE_RANCHER) return GameUnlocks.ROLE_RANCHER;
        if (role == GameConstants.ROLE_BUILDER) return GameUnlocks.ROLE_BUILDER;
        if (role == GameConstants.ROLE_BREEDER) return GameUnlocks.ROLE_BREEDER;
        if (role == GameConstants.ROLE_RESEARCHER) return GameUnlocks.ROLE_RESEARCHER;

        return null; 
    }

    public void liveUpdate() {
        if (!isShowing()) {
            return;
        }
        for (RolePanel panel : rolePanels) {
            panel.updateData();
        }
    }
    
    public void showDialog(int tabIndex) {
        selectTab(tabIndex);
        setVisible(true);
    }
    
    private static class RolePanel extends JPanel {
        private final Colony colony;
        private final AntType antType;
        private final JLabel totalLabel;
        private final JLabel assignedLabel;
        private final JLabel unassignedLabel;
        private final Map<AntRole, JSpinner> spinnerMap = new HashMap<>();

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

                            int currentTotalAnts = RolePanel.this.colony.getAntsByType(RolePanel.this.antType).size();
                            int newTotalAssigned = newValue + otherSpinnersTotal;

                            if (newTotalAssigned > currentTotalAnts) {
                                int allowedValue = Math.max(0, currentTotalAnts - otherSpinnersTotal);
                                SwingUtilities.invokeLater(() -> spinner.setValue(allowedValue));
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
                }
            }
            updateData();
        }
        
        void updateData() {
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