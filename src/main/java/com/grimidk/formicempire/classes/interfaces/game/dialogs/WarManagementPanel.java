package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyDiplomacyService;
import com.grimidk.formicempire.classes.entities.services.world.WarService;
import com.grimidk.formicempire.classes.entities.services.world.WarStanding;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiTableStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiOptionPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WarManagementPanel extends JPanel implements DynastyManagementDialog.LiveUpdatePanel {

    public interface Callbacks {
        void goToOpponentCapital(Dynasty opponent);

        void openDiplomacy(Dynasty opponent);

        void openWarRoles();

        void viewBattle(War war);

        void onWarsChanged();
    }

    private static final int COL_NAME = 0;
    private static final int COL_DECLARED_BY = 1;
    private static final int COL_STARTED = 2;
    private static final int COL_DURATION = 3;
    private static final int COL_STANDING = 4;
    private static final int COL_PROGRESS = 5;
    private static final int COL_MILITARY = 6;
    private static final int COL_ACTIONS = 7;

    private static final int HIST_COL_NAME = 0;
    private static final int HIST_COL_STARTED = 1;
    private static final int HIST_COL_ENDED = 2;
    private static final int HIST_COL_DURATION = 3;
    private static final int HIST_COL_CONCLUSION = 4;
    private static final int HIST_COL_WINNER = 5;

    private final Dynasty dynasty;
    private final Engine engine;
    private final Callbacks callbacks;

    private final JTabbedPane tabbedPane;
    private final JLabel activeEmptyLabel = new JLabel();
    private final JLabel historyEmptyLabel = new JLabel();
    private final JTable activeTable;
    private final DefaultTableModel activeModel;
    private final JScrollPane activeScrollPane;
    private final JTable historyTable;
    private final DefaultTableModel historyModel;
    private final JScrollPane historyScrollPane;
    private final JCheckBox showAllActiveWarsCheck;
    private final JCheckBox showAllHistoricWarsCheck;
    private final List<ActiveWarRowData> displayedActiveWars = new ArrayList<>();

    public WarManagementPanel(Dynasty dynasty, Engine engine, Callbacks callbacks) {
        super(new BorderLayout());
        this.dynasty = dynasty;
        this.engine = engine;
        this.callbacks = callbacks;

        styleEmptyLabel(activeEmptyLabel, LanguageStrings.WAR_EMPTY);
        styleEmptyLabel(historyEmptyLabel, LanguageStrings.WAR_HISTORY_EMPTY);

        showAllActiveWarsCheck = new JCheckBox(LanguageStrings.get(LanguageStrings.WAR_SHOW_ALL_ACTIVE), false);
        showAllActiveWarsCheck.setToolTipText(LanguageStrings.get(LanguageStrings.WAR_SHOW_ALL_ACTIVE_TIP));
        AssetStyles.styleCheckBox(showAllActiveWarsCheck);
        showAllActiveWarsCheck.addActionListener(e -> updateActiveWars());

        showAllHistoricWarsCheck = new JCheckBox(LanguageStrings.get(LanguageStrings.WAR_SHOW_ALL_HISTORIC), false);
        showAllHistoricWarsCheck.setToolTipText(LanguageStrings.get(LanguageStrings.WAR_SHOW_ALL_HISTORIC_TIP));
        AssetStyles.styleCheckBox(showAllHistoricWarsCheck);
        showAllHistoricWarsCheck.addActionListener(e -> updateHistoricWars());

        activeModel = createActiveModel();
        activeTable = createActiveTable(activeModel);
        activeScrollPane = AssetStyles.wrapScrollableTable(activeTable);
        historyModel = createHistoryModel();
        historyTable = createHistoryTable(historyModel);
        historyScrollPane = AssetStyles.wrapScrollableTable(historyTable);

        JPanel activeHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        activeHeader.setOpaque(false);
        activeHeader.add(showAllActiveWarsCheck);

        JPanel activeNorth = new JPanel(new BorderLayout());
        activeNorth.setOpaque(false);
        activeNorth.add(activeHeader, BorderLayout.NORTH);
        activeNorth.add(activeEmptyLabel, BorderLayout.CENTER);

        JPanel activePanel = new JPanel(new BorderLayout());
        activePanel.setOpaque(false);
        activePanel.add(activeNorth, BorderLayout.NORTH);
        activePanel.add(activeScrollPane, BorderLayout.CENTER);

        JPanel historyHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        historyHeader.setOpaque(false);
        historyHeader.add(showAllHistoricWarsCheck);

        JPanel historyNorth = new JPanel(new BorderLayout());
        historyNorth.setOpaque(false);
        historyNorth.add(historyHeader, BorderLayout.NORTH);
        historyNorth.add(historyEmptyLabel, BorderLayout.CENTER);

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setOpaque(false);
        historyPanel.add(historyNorth, BorderLayout.NORTH);
        historyPanel.add(historyScrollPane, BorderLayout.CENTER);

        tabbedPane = new JTabbedPane();
        AssetStyles.styleTabbedPane(tabbedPane);
        tabbedPane.addTab(LanguageStrings.get(LanguageStrings.TAB_WAR_ACTIVE), activePanel);
        tabbedPane.addTab(LanguageStrings.get(LanguageStrings.TAB_WAR_HISTORY), historyPanel);
        add(tabbedPane, BorderLayout.CENTER);
        tabbedPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                fitActiveColumns();
                fitHistoryColumns();
            }
        });
        updateData();
    }

    @Override
    public void liveUpdate() {
        updateData();
    }

    public void updateData() {
        updateActiveWars();
        updateHistoricWars();
    }

    private void updateActiveWars() {
        if (activeTable.isEditing()) {
            return;
        }

        World world = engine != null ? engine.getWorld() : null;
        WarService warService = world != null ? world.getWarService() : null;
        boolean showAll = showAllActiveWarsCheck.isSelected();
        List<War> wars = List.of();
        if (warService != null) {
            wars = showAll ? warService.getActiveWars() : warService.getWarsForDynasty(dynasty.getId());
        }

        int selectedRow = activeTable.getSelectedRow();
        activeModel.setRowCount(0);
        displayedActiveWars.clear();

        activeEmptyLabel.setText(LanguageStrings.get(
                showAll ? LanguageStrings.WAR_ALL_EMPTY : LanguageStrings.WAR_EMPTY));

        if (warService == null || world == null) {
            activeTable.setVisible(false);
            activeEmptyLabel.setVisible(true);
            return;
        }

        int worldMonth = DynastyDiplomacyService.worldMonthIndex(world);
        String notApplicable = LanguageStrings.get(LanguageStrings.WORLD_NA);

        for (War war : wars) {
            if (war.involves(dynasty.getId())) {
                addInvolvedActiveWarRow(warService, world, worldMonth, war);
            } else if (showAll) {
                addSpectatorActiveWarRow(warService, world, worldMonth, war, notApplicable);
            }
        }

        boolean showTable = !displayedActiveWars.isEmpty();
        activeTable.setVisible(showTable);
        activeEmptyLabel.setVisible(!showTable);

        if (selectedRow >= 0 && selectedRow < activeTable.getRowCount()) {
            activeTable.setRowSelectionInterval(selectedRow, selectedRow);
        }
        if (showTable) {
            fitActiveColumns();
        }
    }

    private void addInvolvedActiveWarRow(WarService warService, World world, int worldMonth, War war) {
        Dynasty opponent = warService.resolveOpponent(war, dynasty);
        if (opponent == null || opponent.isDefeated()) {
            return;
        }
        Dynasty declarer = warService.resolveDeclarer(war);
        String declaredBy = declarer != null && declarer.getId() == dynasty.getId()
                ? LanguageStrings.get(LanguageStrings.WAR_DECLARED_BY_YOU)
                : (declarer != null ? declarer.getName() : "-");
        WarStanding standing = warService.getStandingForDynasty(war, dynasty);
        ActiveWarRowData rowData = new ActiveWarRowData(war, opponent, true);
        displayedActiveWars.add(rowData);
        activeModel.addRow(new Object[]{
                warService.formatWarNameForDisplay(war, dynasty),
                declaredBy,
                war.formatStartedDate(),
                war.getDurationMonths(worldMonth),
                warService.formatStanding(standing),
                warService.formatProgress(war),
                opponent.getMilitaryPower(),
                rowData
        });
    }

    private void addSpectatorActiveWarRow(WarService warService, World world, int worldMonth, War war,
            String notApplicable) {
        Dynasty dynastyA = world.findDynastyById(war.getDynastyIdA());
        Dynasty dynastyB = world.findDynastyById(war.getDynastyIdB());
        if (dynastyA == null || dynastyB == null || dynastyA.isDefeated() || dynastyB.isDefeated()) {
            return;
        }
        Dynasty declarer = warService.resolveDeclarer(war);
        String declaredBy = declarer != null ? declarer.getName() : notApplicable;
        String pairLabel = LanguageStrings.format(
                LanguageStrings.MAP_ACTIVE_WAR_PAIR_FMT, dynastyA.getName(), dynastyB.getName());
        String warName = war.getDisplayName();
        if (warName == null || warName.isEmpty()) {
            warName = pairLabel;
        }
        ActiveWarRowData rowData = new ActiveWarRowData(war, null, false);
        displayedActiveWars.add(rowData);
        activeModel.addRow(new Object[]{
                warName,
                declaredBy,
                war.formatStartedDate(),
                war.getDurationMonths(worldMonth),
                formatNeutralStanding(warService, war),
                warService.formatProgress(war),
                notApplicable,
                rowData
        });
    }

    private static String formatNeutralStanding(WarService warService, War war) {
        Dynasty leader = warService.getLeadingDynasty(war);
        if (leader == null) {
            return warService.formatStanding(WarStanding.EVEN);
        }
        return LanguageStrings.format(
                LanguageStrings.WAR_STANDING_LEADER_FMT,
                leader.getName(),
                warService.formatStanding(WarStanding.WINNING));
    }

    private void updateHistoricWars() {
        if (historyTable.isEditing()) {
            return;
        }

        World world = engine != null ? engine.getWorld() : null;
        WarService warService = world != null ? world.getWarService() : null;
        boolean showAll = showAllHistoricWarsCheck.isSelected();
        List<War> wars = List.of();
        if (warService != null) {
            wars = showAll ? warService.getHistoricWars() : warService.getHistoricWarsForDynasty(dynasty.getId());
        }

        historyModel.setRowCount(0);
        historyEmptyLabel.setText(LanguageStrings.get(
                showAll ? LanguageStrings.WAR_ALL_HISTORY_EMPTY : LanguageStrings.WAR_HISTORY_EMPTY));

        if (warService == null || world == null) {
            historyTable.setVisible(false);
            historyEmptyLabel.setVisible(true);
            return;
        }

        int worldMonth = DynastyDiplomacyService.worldMonthIndex(world);
        for (War war : wars) {
            if (!showAll && !war.involves(dynasty.getId())) {
                continue;
            }
            historyModel.addRow(buildHistoricWarRow(warService, world, war, worldMonth));
        }

        boolean showTable = historyModel.getRowCount() > 0;
        historyTable.setVisible(showTable);
        historyEmptyLabel.setVisible(!showTable);
        if (showTable) {
            fitHistoryColumns();
        }
    }

    private Object[] buildHistoricWarRow(WarService warService, World world, War war, int worldMonth) {
        return new Object[]{
                warService.formatWarNameForDisplay(war, dynasty),
                war.formatStartedDate(),
                war.formatEndedDate(),
                war.getDurationMonths(worldMonth),
                war.formatConclusion(),
                warService.resolveWinnerDisplayName(war, dynasty)
        };
    }

    private void performOfferPeace(ActiveWarRowData rowData) {
        World world = engine != null ? engine.getWorld() : null;
        WarService warService = world != null ? world.getWarService() : null;
        if (rowData == null || warService == null) {
            return;
        }
        if (!warService.canOfferPeace(rowData.war, dynasty)) {
            UiOptionPane.showMessageDialog(this,
                    LanguageStrings.get(LanguageStrings.WAR_ERROR_CANNOT_OFFER_PEACE),
                    LanguageStrings.get(LanguageStrings.WAR_ACTION_OFFER_PEACE),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = UiOptionPane.showConfirmDialog(this,
                LanguageStrings.format(LanguageStrings.WAR_OFFER_PEACE_CONFIRM_FMT, rowData.opponent.getName()),
                LanguageStrings.get(LanguageStrings.WAR_ACTION_OFFER_PEACE),
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        if (warService.offerPeace(rowData.war, dynasty)) {
            if (callbacks != null) {
                callbacks.onWarsChanged();
            }
            updateData();
        }
    }

    private void performAcceptPeace(ActiveWarRowData rowData) {
        World world = engine != null ? engine.getWorld() : null;
        WarService warService = world != null ? world.getWarService() : null;
        if (rowData == null || warService == null || !warService.canAcceptPeaceOffer(rowData.war, dynasty)) {
            return;
        }
        Dynasty offerer = world.findDynastyById(rowData.war.getPendingPeaceOfferFromDynastyId());
        String offererName = offerer != null ? offerer.getName() : "?";
        int confirm = UiOptionPane.showConfirmDialog(this,
                LanguageStrings.format(LanguageStrings.WAR_ACCEPT_PEACE_CONFIRM_FMT, offererName),
                LanguageStrings.get(LanguageStrings.WAR_ACTION_ACCEPT_PEACE),
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        warService.acceptPeaceOffer(rowData.war, dynasty);
        if (callbacks != null) {
            callbacks.onWarsChanged();
        }
        updateData();
    }

    private void performDeclinePeace(ActiveWarRowData rowData) {
        World world = engine != null ? engine.getWorld() : null;
        WarService warService = world != null ? world.getWarService() : null;
        if (rowData == null || warService == null) {
            return;
        }
        warService.declinePeaceOffer(rowData.war, dynasty);
        updateData();
    }

    private void performFallback(ActiveWarRowData rowData) {
        World world = engine != null ? engine.getWorld() : null;
        WarService warService = world != null ? world.getWarService() : null;
        if (rowData == null || warService == null || rowData.opponent == null) {
            return;
        }
        int confirm = UiOptionPane.showConfirmDialog(this,
                LanguageStrings.format(LanguageStrings.WAR_FALLBACK_CONFIRM_FMT, rowData.opponent.getName()),
                LanguageStrings.get(LanguageStrings.WAR_ACTION_FALLBACK),
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        if (warService.forfeitWarStage(rowData.war, dynasty)) {
            if (callbacks != null) {
                callbacks.onWarsChanged();
            }
            updateData();
        }
    }

    private void showWarActionsMenu(ActiveWarRowData rowData, JTable tableRef, int row, int column) {
        if (rowData == null || !rowData.war.isActive() || !rowData.war.isCampaignInitialized()) {
            return;
        }
        World world = engine != null ? engine.getWorld() : null;
        WarService warService = world != null ? world.getWarService() : null;
        Dynasty opponent = rowData.opponent;
        JPopupMenu menu = new JPopupMenu();

        JMenuItem battleItem = new JMenuItem(LanguageStrings.get(LanguageStrings.WAR_ACTION_VIEW_BATTLE));
        battleItem.addActionListener(e -> {
            if (callbacks != null) {
                callbacks.viewBattle(rowData.war);
            }
        });
        AssetStyles.styleMenuItem(battleItem);
        menu.add(battleItem);

        if (!rowData.playerInvolved || rowData.opponent == null) {
            UiTableStyles.showCellPopupMenu(menu, tableRef, row, column);
            return;
        }

        if (warService != null && warService.canAcceptPeaceOffer(rowData.war, dynasty)) {
            JMenuItem acceptItem = new JMenuItem(LanguageStrings.get(LanguageStrings.WAR_ACTION_ACCEPT_PEACE));
            acceptItem.addActionListener(e -> performAcceptPeace(rowData));
            AssetStyles.styleMenuItem(acceptItem);
            menu.add(acceptItem);

            JMenuItem declineItem = new JMenuItem(LanguageStrings.get(LanguageStrings.WAR_ACTION_DECLINE_PEACE));
            declineItem.addActionListener(e -> performDeclinePeace(rowData));
            AssetStyles.styleMenuItem(declineItem);
            menu.add(declineItem);
        }

        if (warService != null && warService.canOfferPeace(rowData.war, dynasty)) {
            JMenuItem peaceItem = new JMenuItem(LanguageStrings.get(LanguageStrings.WAR_ACTION_OFFER_PEACE));
            peaceItem.addActionListener(e -> performOfferPeace(rowData));
            AssetStyles.styleMenuItem(peaceItem);
            menu.add(peaceItem);
        }

        if (warService != null && warService.canForfeitStage(rowData.war, dynasty)) {
            JMenuItem fallbackItem = new JMenuItem(LanguageStrings.get(LanguageStrings.WAR_ACTION_FALLBACK));
            fallbackItem.addActionListener(e -> performFallback(rowData));
            AssetStyles.styleMenuItem(fallbackItem);
            menu.add(fallbackItem);
        }

        if (callbacks != null) {
            JMenuItem capitalItem = new JMenuItem(LanguageStrings.get(LanguageStrings.WAR_ACTION_GO_TO_CAPITAL));
            Colony capital = opponent.getCapital();
            if (capital != null) {
                capitalItem.addActionListener(e -> callbacks.goToOpponentCapital(opponent));
            } else {
                capitalItem.setEnabled(false);
            }
            AssetStyles.styleMenuItem(capitalItem);
            menu.add(capitalItem);

            JMenuItem diploItem = new JMenuItem(LanguageStrings.get(LanguageStrings.WAR_ACTION_DIPLOMACY));
            diploItem.addActionListener(e -> callbacks.openDiplomacy(opponent));
            AssetStyles.styleMenuItem(diploItem);
            menu.add(diploItem);

            JMenuItem rolesItem = new JMenuItem(LanguageStrings.get(LanguageStrings.WAR_ACTION_WAR_ROLES));
            rolesItem.addActionListener(e -> callbacks.openWarRoles());
            AssetStyles.styleMenuItem(rolesItem);
            menu.add(rolesItem);
        }

        UiTableStyles.showCellPopupMenu(menu, tableRef, row, column);
    }

    private DefaultTableModel createActiveModel() {
        return new DefaultTableModel(new String[]{
                LanguageStrings.get(LanguageStrings.WAR_COL_NAME),
                LanguageStrings.get(LanguageStrings.WAR_COL_DECLARED_BY),
                LanguageStrings.get(LanguageStrings.WAR_COL_STARTED),
                LanguageStrings.get(LanguageStrings.WAR_COL_DURATION),
                LanguageStrings.get(LanguageStrings.WAR_COL_STANDING),
                LanguageStrings.get(LanguageStrings.WAR_COL_PROGRESS),
                LanguageStrings.get(LanguageStrings.STAT_MILITARY_POWER),
                LanguageStrings.get(LanguageStrings.DYNASTY_ACTIONS)
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column != COL_ACTIONS) {
                    return false;
                }
                Object value = getValueAt(row, COL_ACTIONS);
                return value instanceof ActiveWarRowData rowData
                        && rowData.war.isActive()
                        && rowData.war.isCampaignInitialized();
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == COL_MILITARY || columnIndex == COL_DURATION) {
                    return Integer.class;
                }
                if (columnIndex == COL_ACTIONS) {
                    return ActiveWarRowData.class;
                }
                return String.class;
            }
        };
    }

    private JTable createActiveTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(45);
        table.setFocusable(false);
        AssetStyles.applyScrollableDialogTable(table);
        AssetStyles.applyTableColumnAlignment(table, COL_NAME, SwingConstants.LEFT);
        AssetStyles.applyTableColumnAlignment(table, COL_DECLARED_BY, SwingConstants.LEFT);
        AssetStyles.applyTableHeaderAlignment(table, COL_STARTED, SwingConstants.CENTER);
        AssetStyles.applyTableHeaderAlignment(table, COL_DURATION, SwingConstants.RIGHT);
        AssetStyles.applyTableHeaderAlignment(table, COL_STANDING, SwingConstants.CENTER);
        AssetStyles.applyTableHeaderAlignment(table, COL_PROGRESS, SwingConstants.CENTER);
        AssetStyles.applyTableHeaderAlignment(table, COL_MILITARY, SwingConstants.CENTER);
        AssetStyles.applyTableHeaderAlignment(table, COL_ACTIONS, SwingConstants.CENTER);
        table.getColumnModel().getColumn(COL_MILITARY).setCellRenderer(new MilitaryPowerRenderer());
        table.getColumnModel().getColumn(COL_STANDING).setCellRenderer(new StandingRenderer());
        table.getColumnModel().getColumn(COL_ACTIONS).setCellRenderer(new WarActionRenderer());
        table.getColumnModel().getColumn(COL_ACTIONS).setCellEditor(new WarActionEditor());
        return table;
    }

    private DefaultTableModel createHistoryModel() {
        return new DefaultTableModel(new String[]{
                LanguageStrings.get(LanguageStrings.WAR_COL_NAME),
                LanguageStrings.get(LanguageStrings.WAR_COL_STARTED),
                LanguageStrings.get(LanguageStrings.WAR_COL_ENDED),
                LanguageStrings.get(LanguageStrings.WAR_COL_DURATION),
                LanguageStrings.get(LanguageStrings.WAR_COL_CONCLUSION),
                LanguageStrings.get(LanguageStrings.WAR_COL_WINNER)
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == HIST_COL_DURATION) {
                    return Integer.class;
                }
                return String.class;
            }
        };
    }

    private JTable createHistoryTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFocusable(false);
        AssetStyles.applyScrollableDialogTable(table);
        AssetStyles.applyTableColumnAlignment(table, HIST_COL_NAME, SwingConstants.LEFT);
        AssetStyles.applyTableColumnAlignment(table, HIST_COL_STARTED, SwingConstants.CENTER);
        AssetStyles.applyTableColumnAlignment(table, HIST_COL_ENDED, SwingConstants.CENTER);
        AssetStyles.applyTableColumnAlignment(table, HIST_COL_DURATION, SwingConstants.RIGHT);
        AssetStyles.applyTableColumnAlignment(table, HIST_COL_CONCLUSION, SwingConstants.LEFT);
        AssetStyles.applyTableColumnAlignment(table, HIST_COL_WINNER, SwingConstants.LEFT);
        return table;
    }

    private void fitActiveColumns() {
        AssetStyles.relayoutTableInScrollPane(activeScrollPane,
                new boolean[]{true, true, false, false, false, false, false, false});
    }

    private void fitHistoryColumns() {
        AssetStyles.relayoutTableInScrollPane(historyScrollPane,
                new boolean[]{true, false, false, false, true, true});
    }

    private static void styleEmptyLabel(JLabel label, String textKey) {
        label.setText(LanguageStrings.get(textKey));
        label.setFont(AssetStyles.FONT_NORMAL);
        label.setForeground(AssetStyles.FONT_COLOR);
        label.setBorder(new EmptyBorder(12, 8, 8, 8));
        label.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private static final class ActiveWarRowData {
        final War war;
        final Dynasty opponent;
        final boolean playerInvolved;

        ActiveWarRowData(War war, Dynasty opponent, boolean playerInvolved) {
            this.war = war;
            this.opponent = opponent;
            this.playerInvolved = playerInvolved;
        }
    }

    private class StandingRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            String text = value != null ? value.toString() : "";
            if (LanguageStrings.get(LanguageStrings.WAR_STANDING_WINNING).equals(text)) {
                setForeground(AssetStyles.FONT_COLOR_SUCCESS);
            } else if (LanguageStrings.get(LanguageStrings.WAR_STANDING_LOSING).equals(text)) {
                setForeground(AssetStyles.FONT_COLOR_WARNING);
            } else {
                setForeground(isSelected ? getForeground() : AssetStyles.FONT_COLOR);
            }
            return this;
        }
    }

    private class MilitaryPowerRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            if (value instanceof Number number) {
                setText(AssetStyles.formatNumber(number.intValue()));
                setIcon(GameConstants.ICON_STAT_MILITARY_POWER);
            } else {
                setText(value != null ? value.toString() : "");
                setIcon(null);
            }
            setIconTextGap(6);
            return this;
        }
    }

    private class WarActionRenderer extends JPanel implements TableCellRenderer {
        private final JButton actionsBtn = new JButton(LanguageStrings.get(LanguageStrings.DYNASTY_ACTIONS));

        WarActionRenderer() {
            super(new FlowLayout(FlowLayout.CENTER, 0, 0));
            setOpaque(true);
            actionsBtn.setFocusable(false);
            AssetStyles.styleCompactButton(actionsBtn);
            add(actionsBtn);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            boolean enabled = value instanceof ActiveWarRowData rowData
                    && rowData.war.isActive()
                    && rowData.war.isCampaignInitialized();
            actionsBtn.setEnabled(enabled);
            actionsBtn.setVisible(enabled);
            return this;
        }
    }

    private class WarActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        private final JButton actionsBtn = new JButton(LanguageStrings.get(LanguageStrings.DYNASTY_ACTIONS));
        private ActiveWarRowData currentData;
        private JTable editingTable;
        private int editingRow;
        private int editingCol;

        WarActionEditor() {
            panel.setOpaque(true);
            actionsBtn.setFocusable(false);
            AssetStyles.styleCompactButton(actionsBtn);
            panel.add(actionsBtn);
            actionsBtn.addActionListener(e -> {
                JTable tableRef = editingTable;
                int row = editingRow;
                int col = editingCol;
                ActiveWarRowData data = currentData;
                fireEditingStopped();
                showWarActionsMenu(data, tableRef, row, col);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentData = (ActiveWarRowData) value;
            editingTable = table;
            editingRow = row;
            editingCol = column;
            panel.setBackground(table.getSelectionBackground());
            boolean enabled = currentData != null
                    && currentData.war.isActive()
                    && currentData.war.isCampaignInitialized();
            actionsBtn.setEnabled(enabled);
            actionsBtn.setVisible(enabled);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentData;
        }
    }
}
