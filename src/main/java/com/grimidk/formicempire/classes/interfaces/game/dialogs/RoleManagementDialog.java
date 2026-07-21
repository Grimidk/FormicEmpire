package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntSubtype;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.services.colony.AntSubtypeService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyDiplomacyService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.interfaces.MainFrame;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
    private final Engine engine;
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final JCheckBox warEconomyCheck;
    private final JButton copyPeaceToWarButton;
    private boolean editingWarRoles;
    private final List<RolePanel> rolePanels = new ArrayList<>();
    private final Set<AntType> initializedTypes = new HashSet<>();
    private final Map<Integer, Integer> tabIndexMap = new HashMap<>();

    public RoleManagementDialog(JFrame owner, Colony colony) {
        this(owner, colony, owner instanceof MainFrame ? ((MainFrame) owner).getEngine() : null);
    }

    public RoleManagementDialog(JFrame owner, Colony colony, Engine engine) {
        super(owner, LanguageStrings.get(LanguageStrings.DIALOG_ROLES_TITLE), AssetStyles.ROLE_DIALOG_SIZE);
        this.colony = colony;
        this.engine = engine;

        warEconomyCheck = new JCheckBox(
                LanguageStrings.get(LanguageStrings.ROLE_WAR_ECONOMY_TOGGLE),
                false);
        warEconomyCheck.setToolTipText(LanguageStrings.get(LanguageStrings.ROLE_WAR_ECONOMY_TOGGLE_TIP));
        AssetStyles.styleCheckBox(warEconomyCheck);
        warEconomyCheck.addActionListener(e -> onWarEconomyViewToggle());

        copyPeaceToWarButton = new JButton(LanguageStrings.get(LanguageStrings.ROLE_COPY_PEACE_TO_WAR));
        AssetStyles.styleButton(copyPeaceToWarButton);
        copyPeaceToWarButton.setEnabled(false);
        copyPeaceToWarButton.addActionListener(e -> onCopyPeaceToWar());

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        headerPanel.add(warEconomyCheck);
        headerPanel.add(copyPeaceToWarButton);
        // Keep header height stable when war-economy controls hide/show.
        headerPanel.setPreferredSize(new Dimension(
                AssetStyles.ROLE_DIALOG_SIZE.width,
                Math.max(AssetStyles.MIN_CONTROL_HIT_SIZE + 16, copyPeaceToWarButton.getPreferredSize().height + 12)));
        headerPanel.setMinimumSize(headerPanel.getPreferredSize());

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        contentPanel.add(headerPanel, BorderLayout.NORTH);

        AssetStyles.styleTabbedPane(tabbedPane);
        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
        
        initTabs(); 
        initKeyBindings();
        initListeners();
        updateWarEconomyControlsVisibility();
        
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
    public void refreshTheme() {
        super.refreshTheme();
        AssetStyles.styleCheckBox(warEconomyCheck);
        AssetStyles.styleButton(copyPeaceToWarButton);
        AssetStyles.styleTabbedPane(tabbedPane);
        tabbedPane.updateUI();
    }

    private void onWarEconomyViewToggle() {
        editingWarRoles = warEconomyCheck.isSelected();
        copyPeaceToWarButton.setEnabled(editingWarRoles && isWarEconomyUnlocked());
        for (RolePanel panel : rolePanels) {
            panel.updateData();
        }
    }

    private boolean isWarEconomyUnlocked() {
        Dynasty dynasty = colony.getDynasty();
        return DynastyDiplomacyService.meetsWarDeclarationPopulationRequirement(dynasty);
    }

    private void updateWarEconomyControlsVisibility() {
        boolean unlocked = isWarEconomyUnlocked();
        warEconomyCheck.setVisible(unlocked);
        copyPeaceToWarButton.setVisible(unlocked);
        if (!unlocked) {
            if (warEconomyCheck.isSelected()) {
                warEconomyCheck.setSelected(false);
            }
            editingWarRoles = false;
            copyPeaceToWarButton.setEnabled(false);
            return;
        }
        copyPeaceToWarButton.setEnabled(editingWarRoles);
    }

    private void onCopyPeaceToWar() {
        colony.copyPeaceRolesToWar();
        for (RolePanel panel : rolePanels) {
            panel.updateData();
        }
    }

    boolean isEditingWarRoles() {
        return editingWarRoles;
    }

    @Override
    protected void refreshDialog() {
        initTabs();
        updateWarEconomyControlsVisibility();
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

        RolePanel panel = new RolePanel(colony, type, engine, this);
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
        updateWarEconomyControlsVisibility();
        for (RolePanel panel : rolePanels) {
            panel.updateData();
        }
    }
    
    private static class RolePanel extends JPanel {
        private final Colony colony;
        private final AntType antType;
        private final Engine engine;
        private final RoleManagementDialog owner;
        private final JLabel totalLabel;
        private final JLabel assignedLabel;
        private final JLabel unassignedLabel;
        private final JPanel availableSubtypesPanel;
        private final JPanel rolesGrid;
        private final Map<AntRole, JSpinner> spinnerMap = new HashMap<>();
        private final Map<AntRole, Map<AntSubtype, JCheckBox>> subtypeAllowMap = new HashMap<>();
        private final Map<AntRole, JPanel> subtypeAllowPanels = new HashMap<>();
        private final Set<AntRole> displayedRoles = new HashSet<>();
        private boolean isUpdating = false;

        RolePanel(Colony colony, AntType antType, Engine engine, RoleManagementDialog owner) {
            this.colony = colony;
            this.antType = antType;
            this.engine = engine;
            this.owner = owner;

            setLayout(new BorderLayout(0, 8));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setBackground(AssetStyles.BACKGROUND_COLOR);

            JPanel summaryPanel = new JPanel();
            summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
            summaryPanel.setOpaque(false);

            int totalAnts = colony.getAntsByType(antType).size();
            
            totalLabel = new JLabel(LanguageStrings.format(LanguageStrings.ROLE_TOTAL_PREFIX, antType.getName(), totalAnts));
            totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
            totalLabel.setForeground(AssetStyles.FONT_COLOR);
            totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            assignedLabel = new JLabel(LanguageStrings.format(LanguageStrings.ROLE_ASSIGNED_PREFIX, 0));
            assignedLabel.setForeground(AssetStyles.FONT_COLOR);
            assignedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            unassignedLabel = new JLabel(LanguageStrings.format(LanguageStrings.ROLE_UNASSIGNED_PREFIX, totalAnts));
            unassignedLabel.setForeground(AssetStyles.FONT_COLOR);
            unassignedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            availableSubtypesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
            availableSubtypesPanel.setOpaque(false);
            availableSubtypesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            summaryPanel.add(totalLabel);
            summaryPanel.add(assignedLabel);
            summaryPanel.add(unassignedLabel);
            summaryPanel.add(Box.createVerticalStrut(4));
            summaryPanel.add(availableSubtypesPanel);
            summaryPanel.add(AssetStyles.createInternalSeparator());

            rolesGrid = new JPanel(new GridBagLayout());
            rolesGrid.setOpaque(false);

            JPanel rolesHost = new JPanel(new BorderLayout());
            rolesHost.setOpaque(false);
            rolesHost.add(rolesGrid, BorderLayout.NORTH);

            JScrollPane scrollPane = new JScrollPane(rolesHost);
            scrollPane.setBorder(null);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            AssetStyles.styleScrollPane(scrollPane);

            add(summaryPanel, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);

            checkAndAddRoles();
            updateData();
        }
        
        private void checkAndAddRoles() {
            boolean addedAny = false;
            for (AntRole role : GameConstants.getAntRoles()) {
                if (role.getAntType() != antType) {
                    continue;
                }
                if (!GameConstants.isObtainableRole(role)) {
                    removeRoleRow(role);
                    continue;
                }

                Upgrade roleUpgrade = getUpgradeForRole(role);
                boolean unlocked = roleUpgrade != null && colony.hasUpgrade(roleUpgrade);

                if (GameConstants.isWarEconomyExclusiveRole(role)) {
                    // War-only roles must still appear on their type tab once unlocked.
                    // Quotas always read/write the war distribution (see usesWarRoleCounts).
                    if (!unlocked) {
                        removeRoleRow(role);
                        continue;
                    }
                    if (!displayedRoles.contains(role)) {
                        // Must mark displayed before addRoleRow — rebuildRolesGrid skips unmarked roles.
                        displayedRoles.add(role);
                        addRoleRow(role);
                        addedAny = true;
                    }
                    continue;
                }
                if (displayedRoles.contains(role)) {
                    continue;
                }

                if (unlocked) {
                    // Must mark displayed before addRoleRow — rebuildRolesGrid skips unmarked roles.
                    displayedRoles.add(role);
                    addRoleRow(role);
                    addedAny = true;
                }
            }
            if (addedAny) {
                revalidate();
                repaint();
            }
        }

        private void removeRoleRow(AntRole role) {
            if (!displayedRoles.contains(role)) {
                return;
            }
            spinnerMap.remove(role);
            subtypeAllowMap.remove(role);
            subtypeAllowPanels.remove(role);
            displayedRoles.remove(role);
            rebuildRolesGrid();
            revalidate();
            repaint();
        }

        private void addRoleRow(AntRole role) {
            int currentAssigned = getRoleCount(role);
            SpinnerModel model = new SpinnerNumberModel(currentAssigned, 0, Integer.MAX_VALUE, 1); 
            JSpinner spinner = new JSpinner(model);
            AssetStyles.styleSpinner(spinner);
            Dimension spinnerSize = AssetStyles.preferredSpinnerSize(80);
            spinner.setPreferredSize(spinnerSize);
            spinner.setMinimumSize(spinnerSize);
            spinner.setMaximumSize(spinnerSize);

            spinner.addChangeListener(e -> {
                if (isUpdating) return;
                int newValue = (Integer) spinner.getValue();
                boolean warCounts = usesWarRoleCounts(role);
                
                int otherSpinnersTotal = 0;
                for (Map.Entry<AntRole, JSpinner> entry : spinnerMap.entrySet()) {
                    if (entry.getValue() == spinner) {
                        continue;
                    }
                    if (usesWarRoleCounts(entry.getKey()) != warCounts) {
                        continue;
                    }
                    otherSpinnersTotal += (Integer) entry.getValue().getValue();
                }

                int currentTotalAnts = RolePanel.this.colony.getAntsByType(RolePanel.this.antType).size();
                int newTotalAssigned = newValue + otherSpinnersTotal;

                if (newTotalAssigned > currentTotalAnts) {

                    AntRole defaultRole = Engine.resolveDefaultRoleForAntType(antType, engine);
                    
                    if (defaultRole != null && !role.equals(defaultRole) && spinnerMap.containsKey(defaultRole)
                            && usesWarRoleCounts(defaultRole) == warCounts) {
                        
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
                            setRoleCount(defaultRole, newDefaultCount);
                            
                            setRoleCount(role, newValue);
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
                
                setRoleCount(role, newValue);
                updateData();
            });
            
            disableSpinnerLetterInput(spinner);

            JPanel subtypeAllows = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            subtypeAllows.setOpaque(false);
            Map<AntSubtype, JCheckBox> checks = new LinkedHashMap<>();
            subtypeAllowMap.put(role, checks);
            subtypeAllowPanels.put(role, subtypeAllows);
            spinnerMap.put(role, spinner);
            rebuildSubtypeAllowControls(role);
            rebuildRolesGrid();
        }

        private void rebuildRolesGrid() {
            rolesGrid.removeAll();

            GridBagConstraints labelGbc = new GridBagConstraints();
            labelGbc.gridx = 0;
            labelGbc.weightx = 1.0;
            labelGbc.fill = GridBagConstraints.HORIZONTAL;
            labelGbc.anchor = GridBagConstraints.WEST;
            labelGbc.insets = new Insets(2, 4, 2, 8);

            GridBagConstraints spinnerGbc = new GridBagConstraints();
            spinnerGbc.gridx = 1;
            spinnerGbc.weightx = 0;
            spinnerGbc.fill = GridBagConstraints.NONE;
            spinnerGbc.anchor = GridBagConstraints.EAST;
            spinnerGbc.insets = new Insets(2, 4, 2, 8);

            GridBagConstraints subtypeGbc = new GridBagConstraints();
            subtypeGbc.gridx = 2;
            subtypeGbc.weightx = 0;
            subtypeGbc.fill = GridBagConstraints.NONE;
            subtypeGbc.anchor = GridBagConstraints.WEST;
            subtypeGbc.insets = new Insets(2, 4, 2, 4);

            int row = 0;
            boolean firstRole = true;
            for (AntRole role : GameConstants.getAntRoles()) {
                if (!displayedRoles.contains(role)) {
                    continue;
                }
                JSpinner spinner = spinnerMap.get(role);
                JPanel subtypeAllows = subtypeAllowPanels.get(role);
                if (spinner == null || subtypeAllows == null) {
                    continue;
                }

                if (!firstRole) {
                    GridBagConstraints sepGbc = new GridBagConstraints();
                    sepGbc.gridx = 0;
                    sepGbc.gridy = row++;
                    sepGbc.gridwidth = 3;
                    sepGbc.weightx = 1.0;
                    sepGbc.fill = GridBagConstraints.HORIZONTAL;
                    sepGbc.insets = new Insets(2, 0, 2, 0);
                    rolesGrid.add(AssetStyles.createInternalSeparator(), sepGbc);
                }
                firstRole = false;

                JLabel label = new JLabel(role.getName() + ":", role.getIcon(), SwingConstants.LEFT);
                label.setIconTextGap(6);
                label.setForeground(AssetStyles.FONT_COLOR);

                labelGbc.gridy = row;
                spinnerGbc.gridy = row;
                subtypeGbc.gridy = row;
                rolesGrid.add(label, labelGbc);
                rolesGrid.add(spinner, spinnerGbc);
                rolesGrid.add(subtypeAllows, subtypeGbc);
                row++;
            }
            rolesGrid.revalidate();
            rolesGrid.repaint();
        }

        private void rebuildSubtypeAllowControls(AntRole role) {
            JPanel host = subtypeAllowPanels.get(role);
            Map<AntSubtype, JCheckBox> checks = subtypeAllowMap.get(role);
            if (host == null || checks == null) {
                return;
            }
            host.removeAll();
            checks.clear();

            List<AntSubtype> unlocked = AntSubtypeService.listUnlockedSpecialSubtypes(colony);
            for (AntSubtype subtype : unlocked) {
                JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
                cell.setOpaque(false);

                JLabel iconLabel = new JLabel(subtype.getIcon());
                iconLabel.setToolTipText(subtype.getName());
                cell.add(iconLabel);

                boolean allowed = isSubtypeAllowed(role, subtype);
                boolean forced = role.isSubtypeForcedAllowed(subtype);
                JCheckBox check = new JCheckBox();
                check.setSelected(allowed || forced);
                check.setEnabled(!forced);
                check.setOpaque(false);
                check.setToolTipText(LanguageStrings.format(LanguageStrings.ROLE_SUBTYPE_ALLOW_TIP, subtype.getName()));
                AssetStyles.styleCheckBox(check);
                if (!forced) {
                    check.addActionListener(e -> {
                        if (isUpdating) {
                            return;
                        }
                        setSubtypeAllowed(role, subtype, check.isSelected());
                    });
                }
                checks.put(subtype, check);
                cell.add(check);
                host.add(cell);
            }
            host.revalidate();
            host.repaint();
        }

        private void syncSubtypeAllowControls(AntRole role) {
            Map<AntSubtype, JCheckBox> checks = subtypeAllowMap.get(role);
            List<AntSubtype> unlocked = AntSubtypeService.listUnlockedSpecialSubtypes(colony);
            if (checks == null || checks.size() != unlocked.size() || !checks.keySet().containsAll(unlocked)) {
                rebuildSubtypeAllowControls(role);
                return;
            }
            for (AntSubtype subtype : unlocked) {
                JCheckBox check = checks.get(subtype);
                if (check == null) {
                    rebuildSubtypeAllowControls(role);
                    return;
                }
                boolean forced = role.isSubtypeForcedAllowed(subtype);
                boolean allowed = isSubtypeAllowed(role, subtype) || forced;
                if (check.isSelected() != allowed) {
                    check.setSelected(allowed);
                }
                if (check.isEnabled() == forced) {
                    check.setEnabled(!forced);
                }
            }
        }

        private void refreshAvailableSubtypesSummary() {
            availableSubtypesPanel.removeAll();

            List<AntSubtype> unlocked = AntSubtypeService.listUnlockedSpecialSubtypes(colony);
            if (unlocked.isEmpty()) {
                availableSubtypesPanel.setVisible(false);
                availableSubtypesPanel.revalidate();
                availableSubtypesPanel.repaint();
                return;
            }

            availableSubtypesPanel.setVisible(true);

            JLabel prefix = new JLabel(LanguageStrings.get(LanguageStrings.ROLE_AVAILABLE_SUBTYPES_PREFIX));
            prefix.setForeground(AssetStyles.FONT_COLOR);
            prefix.setFont(AssetStyles.FONT_BOLD);
            availableSubtypesPanel.add(prefix);

            int nothingCount = AntSubtypeService.countStandardAntsOfType(colony, antType);
            availableSubtypesPanel.add(buildAvailableSubtypeChip(
                    GameConstants.SUBTYPE_HEAD_NONE.getIcon(),
                    LanguageStrings.get(LanguageStrings.SUBTYPE_NOTHING),
                    nothingCount));

            for (AntSubtype subtype : unlocked) {
                int count = AntSubtypeService.countAntsWithSubtype(colony, antType, subtype);
                availableSubtypesPanel.add(buildAvailableSubtypeChip(subtype.getIcon(), subtype.getName(), count));
            }

            availableSubtypesPanel.revalidate();
            availableSubtypesPanel.repaint();
        }

        private JPanel buildAvailableSubtypeChip(Icon icon, String name, int count) {
            JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            chip.setOpaque(false);
            JLabel iconLabel = new JLabel(icon);
            iconLabel.setToolTipText(name);
            chip.add(iconLabel);
            JLabel text = new JLabel(name + ": " + AssetStyles.formatNumber(count));
            text.setForeground(AssetStyles.FONT_COLOR);
            chip.add(text);
            return chip;
        }

        private boolean usesWarRoleCounts(AntRole role) {
            return owner.isEditingWarRoles() || GameConstants.isWarEconomyExclusiveRole(role);
        }

        private boolean isSubtypeAllowed(AntRole role, AntSubtype subtype) {
            return usesWarRoleCounts(role)
                    ? colony.isWarRoleSubtypeAllowed(role, subtype)
                    : colony.isPeaceRoleSubtypeAllowed(role, subtype);
        }

        private void setSubtypeAllowed(AntRole role, AntSubtype subtype, boolean allowed) {
            if (usesWarRoleCounts(role)) {
                colony.setWarRoleSubtypeAllowed(role, subtype, allowed);
            } else {
                colony.setPeaceRoleSubtypeAllowed(role, subtype, allowed);
            }
        }

        private int getRoleCount(AntRole role) {
            return usesWarRoleCounts(role)
                    ? colony.getWarAssignedRoleCount(role)
                    : colony.getPeaceAssignedRoleCount(role);
        }

        private void setRoleCount(AntRole role, int count) {
            if (usesWarRoleCounts(role)) {
                colony.setWarAssignedRoleCount(role, count);
            } else {
                colony.setPeaceAssignedRoleCount(role, count);
            }
        }
        
        void updateData() {
            if (isUpdating) return;
            isUpdating = true;
            try {
                checkAndAddRoles();
                
                for (Map.Entry<AntRole, JSpinner> entry : spinnerMap.entrySet()) {
                    int colonyValue = getRoleCount(entry.getKey());
                    if ((Integer)entry.getValue().getValue() != colonyValue) {
                        entry.getValue().setValue(colonyValue);
                    }
                    syncSubtypeAllowControls(entry.getKey());
                }
                
                int totalAnts = colony.getAntsByType(antType).size();
                totalLabel.setText(LanguageStrings.format(LanguageStrings.ROLE_TOTAL_PREFIX, antType.getName(), totalAnts));

                int totalAssigned = 0;
                for (Map.Entry<AntRole, JSpinner> entry : spinnerMap.entrySet()) {
                    // In peace view, war-exclusive rows edit war prep quotas — exclude from peace totals.
                    if (!owner.isEditingWarRoles()
                            && GameConstants.isWarEconomyExclusiveRole(entry.getKey())) {
                        continue;
                    }
                    totalAssigned += (Integer) entry.getValue().getValue();
                }
                
                int unassigned = totalAnts - totalAssigned;
                
                assignedLabel.setText(LanguageStrings.format(LanguageStrings.ROLE_ASSIGNED_PREFIX, totalAssigned));
                unassignedLabel.setText(LanguageStrings.format(LanguageStrings.ROLE_UNASSIGNED_PREFIX, unassigned));
                refreshAvailableSubtypesSummary();

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
