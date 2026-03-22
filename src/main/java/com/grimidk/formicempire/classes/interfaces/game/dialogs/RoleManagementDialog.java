package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

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

    public static final int TAB_WORKER = 0;
    public static final int TAB_SOLDIER = 1;
    public static final int TAB_MAJOR = 2;
    public static final int TAB_PRINCESS = 3;
    public static final int TAB_QUEEN = 4;

    private final Colony colony;
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final List<RolePanel> rolePanels = new ArrayList<>();
    private final Set<AntType> initializedTypes = new HashSet<>();
    private final Map<Integer, Integer> tabIndexMap = new HashMap<>();

    public RoleManagementDialog(JFrame owner, Colony colony) {
        super(owner, LanguageStrings.get(LanguageStrings.DIALOG_ROLES_TITLE), AssetStyles.DEFAULT_DIALOG_SIZE);
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

    public Colony getColony() {
        return colony;
    }

    @Override
    protected void refreshDialog() {
        initTabs();
        for (RolePanel panel : rolePanels) {
            panel.updateData();
        }
    }
    
    public void showDialog(int tabType) {
        refreshDialog(); 
        selectTab(tabType);
        super.showDialog();
    }
    
    public boolean isTabOpen(int tabType) {
        if (!isShowing()) return false;
        Integer index = tabIndexMap.get(tabType);
        return index != null && tabbedPane.getSelectedIndex() == index;
    }
    
    private void initTabs() {
        int previousSelectedIndex = tabbedPane.getSelectedIndex();
        int currentIndex = 0;
        tabIndexMap.clear();
        
        if (addRoleTab(GameConstants.TYPE_WORKER, GameUnlocks.TYPE_WORKER, currentIndex)) {
            tabIndexMap.put(TAB_WORKER, currentIndex++);
        }
        if (addRoleTab(GameConstants.TYPE_SOLDIER, GameUnlocks.TYPE_SOLDIER, currentIndex)) {
            tabIndexMap.put(TAB_SOLDIER, currentIndex++);
        }
        if (addRoleTab(GameConstants.TYPE_MAJOR, GameUnlocks.TYPE_MAJOR, currentIndex)) {
            tabIndexMap.put(TAB_MAJOR, currentIndex++);
        }
        if (addRoleTab(GameConstants.TYPE_PRINCESS, GameUnlocks.TYPE_PRINCESS, currentIndex)) {
            tabIndexMap.put(TAB_PRINCESS, currentIndex++);
        }
        if (addRoleTab(GameConstants.TYPE_QUEEN, GameUnlocks.TYPE_QUEEN, currentIndex)) {
            tabIndexMap.put(TAB_QUEEN, currentIndex++);
        }

        if (previousSelectedIndex >= 0 && previousSelectedIndex < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(previousSelectedIndex);
        }
    }
    
    private void initListeners() {
        tabbedPane.addChangeListener(e -> {
            Component selected = tabbedPane.getSelectedComponent();
            if (selected instanceof RolePanel) {
                ((RolePanel) selected).updateData();
            }
        });
    }
    
    private boolean addRoleTab(AntType type, Upgrade requiredUpgrade, int expectedIndex) {
        if (!colony.hasUpgrade(requiredUpgrade)) {
            return false;
        }

        if (initializedTypes.contains(type)) {
            return true;
        }

        RolePanel panel = new RolePanel(colony, type);
        rolePanels.add(panel);
        tabbedPane.insertTab(type.getName(), type.getIcon(), panel, null, expectedIndex);
        initializedTypes.add(type);
        return true;
    }
    
    public void selectTab(int tabType) {
        if (tabIndexMap.containsKey(tabType)) {
            tabbedPane.setSelectedIndex(tabIndexMap.get(tabType));
        }
    }

    private void initKeyBindings() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();
        
        addTabSwitchAction(inputMap, actionMap, "toggleTab1", KeyEvent.VK_Q, TAB_WORKER);
        addTabSwitchAction(inputMap, actionMap, "toggleTab2", KeyEvent.VK_W, TAB_SOLDIER);
        addTabSwitchAction(inputMap, actionMap, "toggleTab3", KeyEvent.VK_E, TAB_MAJOR);
        addTabSwitchAction(inputMap, actionMap, "toggleTab4", KeyEvent.VK_R, TAB_PRINCESS);
        addTabSwitchAction(inputMap, actionMap, "toggleTab5", KeyEvent.VK_T, TAB_QUEEN);
    }
    
    private void addTabSwitchAction(InputMap im, ActionMap am, String name, int key, int tabType) {
        im.put(KeyStroke.getKeyStroke(key, 0), name);
        am.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isTabOpen(tabType)) {
                    dispose();
                } else if (tabIndexMap.containsKey(tabType)) {
                    tabbedPane.setSelectedIndex(tabIndexMap.get(tabType));
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
        if (role == GameConstants.ROLE_MINER) return GameUnlocks.ROLE_MINER;
        if (role == GameConstants.ROLE_POTTER) return GameUnlocks.ROLE_POTTER;
        if (role == GameConstants.ROLE_MILITIA) return GameUnlocks.ROLE_MILITIA;
        if (role == GameConstants.ROLE_COURIER) return GameUnlocks.ROLE_COURIER;
        if (role == GameConstants.ROLE_ENGINEER) return GameUnlocks.ROLE_ENGINEER;
        if (role == GameConstants.ROLE_GUARD) return GameUnlocks.ROLE_GUARD;
        if (role == GameConstants.ROLE_WARRIOR) return GameUnlocks.ROLE_WARRIOR;
        if (role == GameConstants.ROLE_DEFENDER) return GameUnlocks.ROLE_DEFENDER;
        if (role == GameConstants.ROLE_BOMBER) return GameUnlocks.ROLE_BOMBER;
        if (role == GameConstants.ROLE_CATCHER) return GameUnlocks.ROLE_CATCHER;
        if (role == GameConstants.ROLE_ESCORT) return GameUnlocks.ROLE_ESCORT;
        if (role == GameConstants.ROLE_BRUTE) return GameUnlocks.ROLE_BRUTE;
        if (role == GameConstants.ROLE_CARRIER) return GameUnlocks.ROLE_CARRIER;
        if (role == GameConstants.ROLE_ARTILLERY) return GameUnlocks.ROLE_ARTILLERY;
        if (role == GameConstants.ROLE_SIEGE) return GameUnlocks.ROLE_SIEGE;
        if (role == GameConstants.ROLE_BORER) return GameUnlocks.ROLE_BORER;
        if (role == GameConstants.ROLE_CRANE) return GameUnlocks.ROLE_CRANE;
        if (role == GameConstants.ROLE_TRANSPORT) return GameUnlocks.ROLE_TRANSPORT;
        if (role == GameConstants.ROLE_DIPLOMAT) return GameUnlocks.ROLE_DIPLOMAT;
        if (role == GameConstants.ROLE_SKYTRANS) return GameUnlocks.ROLE_SKYTRANS;

        return null; 
    }

    @Override
    public void liveUpdate() {
        if (!isShowing()) {
            return;
        }
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
        private boolean isUpdating = false;

        RolePanel(Colony colony, AntType antType) {
            this.colony = colony;
            this.antType = antType;

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setBackground(AssetStyles.BACKGROUND_COLOR);

            int totalAnts = colony.getAntsByType(antType).size();
            
            totalLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.ROLE_TOTAL_PREFIX), antType.getName(), totalAnts));
            totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
            totalLabel.setForeground(AssetStyles.FONT_COLOR);
            
            assignedLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.ROLE_ASSIGNED_PREFIX), 0));
            assignedLabel.setForeground(AssetStyles.FONT_COLOR);
            
            unassignedLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.ROLE_UNASSIGNED_PREFIX), totalAnts));
            unassignedLabel.setForeground(AssetStyles.FONT_COLOR);
            
            add(totalLabel);
            add(assignedLabel);
            add(unassignedLabel);
            add(AssetStyles.createInternalSeparator());

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
            roleRow.setOpaque(false);
            JLabel label = new JLabel(role.getName() + ":");
            label.setForeground(AssetStyles.FONT_COLOR);
            roleRow.add(label);
            
            int currentAssigned = colony.getAssignedRoleCount(role);
            SpinnerModel model = new SpinnerNumberModel(currentAssigned, 0, Integer.MAX_VALUE, 1); 
            JSpinner spinner = new JSpinner(model);
            spinner.setPreferredSize(new Dimension(80, 25));

            spinner.addChangeListener(e -> {
                if (isUpdating) return;
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
                            isUpdating = true;
                            try {
                                defaultSpinner.setValue(newDefaultCount);
                            } finally {
                                isUpdating = false;
                            }
                            colony.setAssignedRoleCount(defaultRole, newDefaultCount);
                            
                            colony.setAssignedRoleCount(role, newValue);
                            updateData();
                            return; 
                        }
                    }
                    
                    int allowedValue = Math.max(0, currentTotalAnts - otherSpinnersTotal);
                    
                    final int finalAllowed = allowedValue;
                    isUpdating = true;
                    try {
                        spinner.setValue(finalAllowed);
                    } finally {
                        isUpdating = false;
                    }
                    
                    newValue = finalAllowed;
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
            if (isUpdating) return;
            isUpdating = true;
            try {
                checkAndAddRoles();
                
                for (Map.Entry<AntRole, JSpinner> entry : spinnerMap.entrySet()) {
                    int colonyValue = colony.getAssignedRoleCount(entry.getKey());
                    if ((Integer)entry.getValue().getValue() != colonyValue) {
                        entry.getValue().setValue(colonyValue);
                    }
                }
                
                int totalAnts = colony.getAntsByType(antType).size();
                totalLabel.setText(String.format(LanguageStrings.get(LanguageStrings.ROLE_TOTAL_PREFIX), antType.getName(), totalAnts));

                int totalAssigned = 0;
                for (JSpinner s : spinnerMap.values()) {
                    totalAssigned += (Integer) s.getValue();
                }
                
                int unassigned = totalAnts - totalAssigned;
                
                assignedLabel.setText(String.format(LanguageStrings.get(LanguageStrings.ROLE_ASSIGNED_PREFIX), totalAssigned));
                unassignedLabel.setText(String.format(LanguageStrings.get(LanguageStrings.ROLE_UNASSIGNED_PREFIX), unassigned));

                if (totalAssigned > totalAnts) {
                    assignedLabel.setForeground(AssetStyles.FONT_COLOR_ERROR);
                    assignedLabel.setToolTipText(LanguageStrings.get(LanguageStrings.ROLE_ERROR_OVER_ASSIGNED));
                    unassignedLabel.setForeground(AssetStyles.FONT_COLOR_ERROR);
                    unassignedLabel.setToolTipText(LanguageStrings.get(LanguageStrings.ROLE_ERROR_OVER_ASSIGNED));
                } else {
                    assignedLabel.setForeground(AssetStyles.FONT_COLOR);
                    assignedLabel.setToolTipText(null);
                    unassignedLabel.setForeground(AssetStyles.FONT_COLOR);
                    unassignedLabel.setToolTipText(null);
                }
            } finally {
                isUpdating = false;
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
