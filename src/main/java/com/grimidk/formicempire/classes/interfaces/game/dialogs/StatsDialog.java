package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.dynasty.colony.ColonyLoyalty;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyCritterHandlingService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyStatService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyLocationService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyStatsService;
import com.grimidk.formicempire.classes.entities.services.world.WorldHistoryEvent;
import com.grimidk.formicempire.classes.entities.services.world.WorldHistoryEventType;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogTexts;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.grimidk.formicempire.classes.interfaces.ui.styles.UiTableStyles;

public class StatsDialog extends ZeroDialog {

    private final Colony colony;
    private final Engine engine;
    private final DynastyStatService dynastyStatsService; 
    
    private final JTabbedPane tabbedPane;
    private final JPanel topPanel;
    private JCheckBox dynastyModeToggle;
    
    private JTable generalTable;
    private JTable dynastyTable;
    private JTable resourcesTable;
    private JTable populationTable;
    private JTable localHexTable;
    private JTable ratesTable;
    private JTable deathTable;
    private JTable worldHistoryTable;
    private JComboBox<HistoryFilterOption> worldHistoryFilter;
    private JCheckBox worldHistoryOwnOnly;

    private JScrollPane generalScrollPane;
    private JScrollPane dynastyScrollPane;
    private JScrollPane worldHistoryScrollPane;
    private JScrollPane resourcesScrollPane;
    private JScrollPane populationScrollPane;
    private JScrollPane localHexScrollPane;
    private JScrollPane ratesScrollPane;
    private JScrollPane deathScrollPane;

    public StatsDialog(JFrame owner, Colony colony, Engine engine) {
        super(owner, LanguageStrings.DIALOG_STATS_TITLE, AssetStyles.DEFAULT_DIALOG_SIZE);
        this.colony = colony;
        this.engine = engine;
        this.dynastyStatsService = colony.getDynasty().getStatService();
        
        setLayout(new BorderLayout());
        
        // --- Top Toggle Panel ---
        topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(AssetStyles.UI_BG_SECONDARY);
        dynastyModeToggle = new JCheckBox(LanguageStrings.get(LanguageStrings.STATS_DYNASTY_MODE));
        AssetStyles.styleCheckBox(dynastyModeToggle);
        dynastyModeToggle.setOpaque(false);
        dynastyModeToggle.setFont(AssetStyles.FONT_BOLD);
        dynastyModeToggle.setForeground(AssetStyles.TEXT_HEADER);
        dynastyModeToggle.setFocusable(false);
        dynastyModeToggle.addActionListener(e -> {
            updateTabTitles();
            refreshDialog();
        });
        topPanel.add(dynastyModeToggle);
        add(topPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        AssetStyles.styleTabbedPane(tabbedPane);
        add(tabbedPane, BorderLayout.CENTER);
        
        initGeneralTab();
        initDynastyTab();
        initWorldHistoryTab();
        initResourceTab();
        initPopulationTab();
        initLocalHexTab();
        initRatesTab();
        initDeathTab();
        
        updateTabTitles();
        refreshDialog();

        tabbedPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutAllTables();
            }
        });
        SwingUtilities.invokeLater(this::layoutAllTables);

        registerCloseKey(KeyEvent.VK_X);
    }

    @Override
    public void refreshTheme() {
        super.refreshTheme();
        topPanel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        dynastyModeToggle.setForeground(AssetStyles.FONT_COLOR_HEADER);
        if (worldHistoryFilter != null) {
            AssetStyles.styleComboBox(worldHistoryFilter);
        }
        if (worldHistoryOwnOnly != null) {
            AssetStyles.styleCheckBox(worldHistoryOwnOnly);
            worldHistoryOwnOnly.setForeground(AssetStyles.FONT_COLOR_HEADER);
        }
        AssetStyles.styleTabbedPane(tabbedPane);
        tabbedPane.updateUI();
    }

    private void updateTabTitles() {
        tabbedPane.setTitleAt(0, LanguageStrings.get(LanguageStrings.STATS_TAB_GENERAL));
        tabbedPane.setTitleAt(1, LanguageStrings.get(LanguageStrings.STATS_TAB_DYNASTY));
        tabbedPane.setTitleAt(2, LanguageStrings.get(LanguageStrings.STATS_TAB_WORLD_HISTORY));
        tabbedPane.setTitleAt(3, LanguageStrings.get(LanguageStrings.STATS_TAB_ECONOMY));
        tabbedPane.setTitleAt(4, LanguageStrings.get(LanguageStrings.STATS_TAB_POPULATION));
        tabbedPane.setTitleAt(5, LanguageStrings.get(LanguageStrings.STATS_TAB_LOCAL_HEX));
        tabbedPane.setTitleAt(6, LanguageStrings.get(LanguageStrings.STATS_TAB_RATES));
        tabbedPane.setTitleAt(7, LanguageStrings.get(LanguageStrings.STATS_TAB_MORTALITY));
        if (worldHistoryFilter != null) {
            refreshWorldHistoryFilterLabels();
        }
        if (worldHistoryOwnOnly != null) {
            worldHistoryOwnOnly.setText(LanguageStrings.get(LanguageStrings.HISTORY_FILTER_OWN_ONLY));
        }
    }

    public void liveUpdate() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::refreshDialog);
        } else {
            refreshDialog();
        }
    }

    private DefaultTableModel createIconModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                for (int row = 0; row < getRowCount(); row++) {
                    Object val = getValueAt(row, columnIndex);
                    if (val instanceof Icon) {
                        return Icon.class;
                    }
                    if (val instanceof Integer) {
                        return Integer.class;
                    }
                    if (val instanceof Boolean) {
                        return Boolean.class;
                    }
                    if (val instanceof String) {
                        return String.class;
                    }
                }
                return Object.class;
            }
        };
    }

    private static void applyIconColumnRenderer(JTable table, int columnIndex) {
        if (table == null || columnIndex < 0 || columnIndex >= table.getColumnCount()) {
            return;
        }
        table.getColumnModel().getColumn(columnIndex).setCellRenderer(new IconOrTextCellRenderer());
        AssetStyles.applyTableColumnAlignment(table, columnIndex, SwingConstants.CENTER);
    }

    private static void applyTextColumnRenderer(JTable table, int columnIndex, int alignment) {
        if (table == null || columnIndex < 0 || columnIndex >= table.getColumnCount()) {
            return;
        }
        table.getColumnModel().getColumn(columnIndex).setCellRenderer(UiTableStyles.createTextCellRenderer(alignment));
        AssetStyles.applyTableColumnAlignment(table, columnIndex, alignment);
    }

    private static void applyNumericColumnRenderer(JTable table, int columnIndex) {
        if (table == null || columnIndex < 0 || columnIndex >= table.getColumnCount()) {
            return;
        }
        table.getColumnModel().getColumn(columnIndex).setCellRenderer(UiTableStyles.createNumericCellRenderer());
        AssetStyles.applyTableColumnAlignment(table, columnIndex, SwingConstants.RIGHT);
    }

    private static void applyFormattedValueColumnRenderer(JTable table, int columnIndex, int alignment) {
        if (table == null || columnIndex < 0 || columnIndex >= table.getColumnCount()) {
            return;
        }
        table.getColumnModel().getColumn(columnIndex).setCellRenderer(new FormattedValueCellRenderer(alignment));
        AssetStyles.applyTableColumnAlignment(table, columnIndex, alignment);
    }

    private static final class FormattedValueCellRenderer extends UiTableStyles.TooltipCellRenderer {
        private final int alignment;

        private FormattedValueCellRenderer(int alignment) {
            this.alignment = alignment;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(alignment);
            if (value instanceof Number number) {
                setText(AssetStyles.formatNumber(number));
            } else {
                setText(value != null ? value.toString() : "");
            }
            updateTruncationTooltip(table, column);
            return this;
        }
    }

    private static void applyGeneralValueRenderer(JTable table) {
        if (table == null || table.getColumnCount() < 3) {
            return;
        }
        table.getColumnModel().getColumn(2).setCellRenderer(new GeneralValueCellRenderer());
        AssetStyles.applyTableColumnAlignment(table, 0, SwingConstants.LEFT);
        AssetStyles.applyTableColumnAlignment(table, 1, SwingConstants.LEFT);
        AssetStyles.applyTableColumnAlignment(table, 2, SwingConstants.LEFT);
    }

    private static final class GeneralValueCellRenderer extends UiTableStyles.TooltipCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Object property = table.getModel().getValueAt(row, 1);
            if (LanguageStrings.get(LanguageStrings.STAT_LOYALTY).equals(property) && value instanceof Integer score) {
                ColonyLoyalty tier = GameConstants.getColonyLoyaltyLevel(score);
                setText(LanguageStrings.format(
                        LanguageStrings.SCORE_TIER_FORMAT,
                        score,
                        tier.getName()));
                setIcon(tier.getIcon());
                setIconTextGap(6);
            } else if (LanguageStrings.get(LanguageStrings.STAT_MILITARY_POWER).equals(property) && value instanceof Integer power) {
                setText(AssetStyles.formatNumber(power));
                setIcon(GameConstants.ICON_STAT_MILITARY_POWER);
                setIconTextGap(6);
                setToolTipText(LanguageStrings.get(LanguageStrings.STAT_MILITARY_POWER_DESC));
            } else if (LanguageStrings.get(LanguageStrings.STAT_ACTIVE_MILITARY_POWER).equals(property) && value instanceof Integer power) {
                setText(AssetStyles.formatNumber(power));
                setIcon(GameConstants.ICON_STAT_MILITARY_POWER);
                setIconTextGap(6);
                setToolTipText(LanguageStrings.get(LanguageStrings.STAT_ACTIVE_MILITARY_POWER_DESC));
            } else if (LanguageStrings.get(LanguageStrings.STAT_RESERVE_MILITARY_POWER).equals(property) && value instanceof Integer power) {
                setText(AssetStyles.formatNumber(power));
                setIcon(GameConstants.ICON_STAT_MILITARY_POWER);
                setIconTextGap(6);
                setToolTipText(LanguageStrings.get(LanguageStrings.STAT_RESERVE_MILITARY_POWER_DESC));
            } else if (LanguageStrings.get(LanguageStrings.STAT_COMBAT_CAPACITY).equals(property) && value instanceof Integer capacity) {
                setText(AssetStyles.formatNumber(capacity));
                setIcon(GameConstants.ICON_STAT_COMBAT_CAPACITY);
                setIconTextGap(6);
                setToolTipText(LanguageStrings.get(LanguageStrings.STAT_COMBAT_CAPACITY_DESC));
            } else if (LanguageStrings.get(LanguageStrings.STAT_ASSIGNED_CARRIERS).equals(property) && value instanceof Integer count) {
                setText(AssetStyles.formatNumber(count));
                setIcon(GameConstants.ROLE_CARRIER.getIcon());
                setIconTextGap(6);
                setToolTipText(LanguageStrings.get(LanguageStrings.STAT_ASSIGNED_CARRIERS_DESC));
            } else if (LanguageStrings.get(LanguageStrings.STAT_DAILY_REINFORCEMENT_RATE).equals(property)) {
                setIcon(null);
                setText(value != null ? value.toString() : "");
                setToolTipText(LanguageStrings.get(LanguageStrings.STAT_DAILY_REINFORCEMENT_RATE_DESC));
            } else if (LanguageStrings.get(LanguageStrings.STAT_DAILY_REINFORCEMENT_ALLOWANCE).equals(property) && value instanceof Integer allowance) {
                setText(AssetStyles.formatNumber(allowance));
                setIcon(GameConstants.ICON_STAT_COMBAT_CAPACITY);
                setIconTextGap(6);
                setToolTipText(LanguageStrings.get(LanguageStrings.STAT_DAILY_REINFORCEMENT_ALLOWANCE_DESC));
            } else if (value instanceof Number number) {
                setIcon(null);
                setText(AssetStyles.formatNumber(number));
                setToolTipText(null);
            } else {
                setIcon(null);
                setText(value != null ? value.toString() : "");
                setToolTipText(null);
            }
            updateTruncationTooltip(table, column);
            return this;
        }
    }

    private static final class IconOrTextCellRenderer extends UiTableStyles.TooltipCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Icon icon) {
                setIcon(icon);
                setText("");
                setHorizontalAlignment(SwingConstants.CENTER);
                setToolTipText(null);
            } else {
                setIcon(null);
                setText(value != null ? value.toString() : "");
                setHorizontalAlignment(SwingConstants.LEFT);
                updateTruncationTooltip(table, column);
            }
            return this;
        }
    }

    private JScrollPane createTablePane(JTable table) {
        table.setRowHeight(24);
        table.setSelectionForeground(AssetStyles.TEXT_HEADER);
        JScrollPane scrollPane = AssetStyles.wrapScrollableTable(table);
        return scrollPane;
    }

    private int viewportWidth(JScrollPane scrollPane) {
        if (scrollPane != null && scrollPane.getViewport().getWidth() > 0) {
            return scrollPane.getViewport().getWidth();
        }
        return AssetStyles.DEFAULT_DIALOG_SIZE.width - 56;
    }

    private void layoutAllTables() {
        AssetStyles.layoutTableColumnsForViewport(generalTable, viewportWidth(generalScrollPane),
                new boolean[]{false, true, true});
        AssetStyles.layoutTableColumnsForViewport(dynastyTable, viewportWidth(dynastyScrollPane),
                new boolean[]{false, false, true, true});
        if (worldHistoryTable != null) {
            AssetStyles.layoutTableColumnsForViewport(worldHistoryTable, viewportWidth(worldHistoryScrollPane),
                    new boolean[]{false, false, true});
        }
        AssetStyles.layoutTableColumnsForViewport(resourcesTable, viewportWidth(resourcesScrollPane),
                new boolean[]{false, true, true, true, true, true, true, true});
        AssetStyles.layoutTableColumnsForViewport(populationTable, viewportWidth(populationScrollPane),
                new boolean[]{false, true, true, false, false});
        AssetStyles.layoutTableColumnsForViewport(localHexTable, viewportWidth(localHexScrollPane),
                new boolean[]{false, true, true});
        AssetStyles.layoutTableColumnsForViewport(ratesTable, viewportWidth(ratesScrollPane),
                new boolean[]{true, false, false, false});
        AssetStyles.layoutTableColumnsForViewport(deathTable, viewportWidth(deathScrollPane),
                new boolean[]{true, false});
    }

    // --- Tab Initialization ---

    private void initGeneralTab() {
        String[] columns = {LanguageStrings.get(LanguageStrings.COL_CATEGORY), LanguageStrings.get(LanguageStrings.COL_PROPERTY), LanguageStrings.get(LanguageStrings.COL_VALUE)};
        generalTable = new JTable(createIconModel(columns));
        applyGeneralValueRenderer(generalTable);
        generalTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = generalTable.rowAtPoint(e.getPoint());
                int col = generalTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col >= 0
                        && LanguageStrings.get(LanguageStrings.STAT_LOYALTY)
                                .equals(generalTable.getModel().getValueAt(row, 1))
                        && !dynastyModeToggle.isSelected()) {
                    generalTable.setToolTipText(colony.buildLoyaltyModifierTooltip(
                            engine != null ? engine.getTradeManager() : null,
                            engine != null ? engine.getWorld() : null));
                    return;
                }
                generalTable.setToolTipText(null);
            }
        });
        generalScrollPane = createTablePane(generalTable);
        tabbedPane.addTab(LanguageStrings.get(LanguageStrings.STATS_TAB_GENERAL), generalScrollPane);
    }
    
    private void initDynastyTab() {
        String[] columns = {"", LanguageStrings.get(LanguageStrings.COL_SCOPE), LanguageStrings.get(LanguageStrings.COL_METRIC), LanguageStrings.get(LanguageStrings.COL_VALUE)};
        dynastyTable = new JTable(createIconModel(columns));
        
        applyIconColumnRenderer(dynastyTable, 0);
        applyTextColumnRenderer(dynastyTable, 1, SwingConstants.LEFT);
        applyTextColumnRenderer(dynastyTable, 2, SwingConstants.LEFT);
        applyFormattedValueColumnRenderer(dynastyTable, 3, SwingConstants.LEFT);
        
        dynastyScrollPane = createTablePane(dynastyTable);
        tabbedPane.addTab(LanguageStrings.get(LanguageStrings.STATS_TAB_DYNASTY), dynastyScrollPane);
    }

    private void initWorldHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterRow.setOpaque(false);
        worldHistoryFilter = new JComboBox<>();
        for (HistoryFilterOption option : HistoryFilterOption.values()) {
            worldHistoryFilter.addItem(option);
        }
        AssetStyles.styleComboBox(worldHistoryFilter);
        worldHistoryFilter.addActionListener(e -> updateWorldHistoryData());
        filterRow.add(worldHistoryFilter);

        worldHistoryOwnOnly = new JCheckBox(LanguageStrings.get(LanguageStrings.HISTORY_FILTER_OWN_ONLY));
        AssetStyles.styleCheckBox(worldHistoryOwnOnly);
        worldHistoryOwnOnly.setOpaque(false);
        worldHistoryOwnOnly.setForeground(AssetStyles.FONT_COLOR_HEADER);
        worldHistoryOwnOnly.setFocusable(false);
        worldHistoryOwnOnly.addActionListener(e -> updateWorldHistoryData());
        filterRow.add(worldHistoryOwnOnly);

        panel.add(filterRow, BorderLayout.NORTH);

        String[] columns = {
                LanguageStrings.get(LanguageStrings.HISTORY_COL_DATE),
                LanguageStrings.get(LanguageStrings.HISTORY_COL_CATEGORY),
                LanguageStrings.get(LanguageStrings.HISTORY_COL_EVENT)
        };
        worldHistoryTable = new JTable(createIconModel(columns));
        applyTextColumnRenderer(worldHistoryTable, 0, SwingConstants.LEFT);
        applyTextColumnRenderer(worldHistoryTable, 1, SwingConstants.LEFT);
        applyTextColumnRenderer(worldHistoryTable, 2, SwingConstants.LEFT);
        worldHistoryScrollPane = createTablePane(worldHistoryTable);
        panel.add(worldHistoryScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab(LanguageStrings.get(LanguageStrings.STATS_TAB_WORLD_HISTORY), panel);
    }

    private void initResourceTab() {
        String[] columns = {"", LanguageStrings.get(LanguageStrings.COL_RESOURCE), LanguageStrings.get(LanguageStrings.COL_CURRENT), LanguageStrings.get(LanguageStrings.COL_CAPACITY), LanguageStrings.get(LanguageStrings.COL_SOURCES), LanguageStrings.get(LanguageStrings.COL_PROD_DAY), LanguageStrings.get(LanguageStrings.COL_CONS_DAY), LanguageStrings.get(LanguageStrings.COL_NET)};
        resourcesTable = new JTable(createIconModel(columns));
        
        applyIconColumnRenderer(resourcesTable, 0);
        applyTextColumnRenderer(resourcesTable, 1, SwingConstants.LEFT);
        for (int col = 2; col <= 7; col++) {
            applyNumericColumnRenderer(resourcesTable, col);
        }
        
        resourcesScrollPane = createTablePane(resourcesTable);
        tabbedPane.addTab(LanguageStrings.get(LanguageStrings.STATS_TAB_ECONOMY), resourcesScrollPane);
    }

    private void initPopulationTab() {
        String[] columns = {
                "",
                LanguageStrings.get(LanguageStrings.COL_TYPE),
                LanguageStrings.get(LanguageStrings.COL_ROLE),
                LanguageStrings.get(LanguageStrings.COL_COUNT),
                LanguageStrings.get(LanguageStrings.COL_CAPACITY)
        };
        populationTable = new JTable(createIconModel(columns));
        
        applyIconColumnRenderer(populationTable, 0);
        applyTextColumnRenderer(populationTable, 1, SwingConstants.LEFT);
        applyTextColumnRenderer(populationTable, 2, SwingConstants.LEFT);
        applyNumericColumnRenderer(populationTable, 3);
        applyFormattedValueColumnRenderer(populationTable, 4, SwingConstants.LEFT);

        populationScrollPane = createTablePane(populationTable);
        tabbedPane.addTab(LanguageStrings.get(LanguageStrings.STATS_TAB_POPULATION), populationScrollPane);
    }

    private void initLocalHexTab() {
        String[] columns = {LanguageStrings.get(LanguageStrings.COL_CATEGORY), LanguageStrings.get(LanguageStrings.COL_PROPERTY), LanguageStrings.get(LanguageStrings.COL_VALUE)};
        localHexTable = new JTable(createIconModel(columns));
        applyTextColumnRenderer(localHexTable, 0, SwingConstants.LEFT);
        applyTextColumnRenderer(localHexTable, 1, SwingConstants.LEFT);
        applyFormattedValueColumnRenderer(localHexTable, 2, SwingConstants.LEFT);
        localHexScrollPane = createTablePane(localHexTable);
        tabbedPane.addTab(LanguageStrings.get(LanguageStrings.STATS_TAB_LOCAL_HEX), localHexScrollPane);
    }

    private void initRatesTab() {
        String[] columns = {LanguageStrings.get(LanguageStrings.COL_ACTIVITY), LanguageStrings.get(LanguageStrings.COL_ASSIGNED), LanguageStrings.get(LanguageStrings.COL_RATE_CAP), LanguageStrings.get(LanguageStrings.COL_COV_OUT)};
        ratesTable = new JTable(createIconModel(columns));
        applyTextColumnRenderer(ratesTable, 0, SwingConstants.LEFT);
        for (int col = 1; col <= 3; col++) {
            applyNumericColumnRenderer(ratesTable, col);
        }
        ratesScrollPane = createTablePane(ratesTable);
        tabbedPane.addTab(LanguageStrings.get(LanguageStrings.STATS_TAB_RATES), ratesScrollPane);
    }

    private void initDeathTab() {
        String[] columns = {LanguageStrings.get(LanguageStrings.COL_CAUSE), LanguageStrings.get(LanguageStrings.COL_TOTAL)};
        deathTable = new JTable(createIconModel(columns));
        applyTextColumnRenderer(deathTable, 0, SwingConstants.LEFT);
        applyNumericColumnRenderer(deathTable, 1);
        deathScrollPane = createTablePane(deathTable);
        tabbedPane.addTab(LanguageStrings.get(LanguageStrings.STATS_TAB_MORTALITY), deathScrollPane);
    }

    // --- Data Updates ---

    @Override
    protected void refreshDialog() {
        updateGeneralData();
        updateDynastyData();
        updateWorldHistoryData();
        updateResourceData();
        updatePopulationData();
        updateLocalHexData();
        updateRatesData();
        updateDeathData();
        layoutAllTables();
    }

    private void updateLocalHexData() {
        DefaultTableModel model = (DefaultTableModel) localHexTable.getModel();
        model.setRowCount(0);
        String sep = LanguageStrings.get(LanguageStrings.STAT_TABLE_SEPARATOR);

        World world = engine != null ? engine.getWorld() : null;
        if (world == null) {
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.PANEL_WORLD), LanguageStrings.get(LanguageStrings.COL_VALUE), LanguageStrings.get(LanguageStrings.STAT_STATUS_NO_HEX)});
            return;
        }
        Hex hex = world.getHexOfColony(colony);
        if (hex == null) {
            hex = world.getActiveHex();
        }
        if (hex == null) {
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.PANEL_WORLD), LanguageStrings.get(LanguageStrings.COL_VALUE), LanguageStrings.get(LanguageStrings.STAT_STATUS_NO_HEX)});
            return;
        }
        Biome biome = hex.getBiome();

        if (dynastyModeToggle.isSelected()) {
            model.addRow(new Object[]{
                LanguageStrings.get(LanguageStrings.STAT_LOCAL_HEX_NOTE_CAT),
                "",
                LanguageStrings.get(LanguageStrings.STAT_LOCAL_HEX_DYNASTY_HINT)
            });
        }

        // Hex Coordinates & Basic Info
        model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.PANEL_WORLD), LanguageStrings.get(LanguageStrings.STAT_COORDS), hex.getQ() + ", " + hex.getR()});
        
        Weather localWeather = hex.getLocalWeather();
        model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.PANEL_WORLD), LanguageStrings.get(LanguageStrings.STAT_LOCAL_WEATHER), localWeather != null ? localWeather.getName() : LanguageStrings.get(LanguageStrings.STAT_USING_GLOBAL)});

        if (biome != null) {
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_ENVIRONMENT), LanguageStrings.get(LanguageStrings.COL_VALUE), biome.getName()});
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_ENVIRONMENT), LanguageStrings.get(LanguageStrings.STAT_BASE_TEMP), biome.getTemperature() + "°C"});
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_ENVIRONMENT), LanguageStrings.get(LanguageStrings.STAT_HUMIDITY_LEVEL), biome.isIsHumid() + " / 5"});
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_ABUNDANCES), LanguageStrings.get(LanguageStrings.RESOURCE_PLANT), String.format("%.2f", biome.getPlantAbundance())});
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_ABUNDANCES), LanguageStrings.get(LanguageStrings.RESOURCE_MEAT), String.format("%.2f", biome.getAnimalAbundance())});
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_ABUNDANCES), LanguageStrings.get(LanguageStrings.RESOURCE_ROCK), String.format("%.2f", biome.getMineralAbundance())});
        }

        model.addRow(new Object[]{null, null, sep, sep});
        int maxDepl = colony.hasUpgrade(GameUnlocks.STAT_HEX_SUSTAIN)
                ? GameNumbers.HEX_SUSTAIN_MAX_DEPLETION_PCT
                : 100;
        int depletionPct = hex.getResourceDepletionPercentCapped(maxDepl);
        ColonyLocationService locations = colony.getLocationService();
        int sourcesFound = locations != null ? locations.getDiscoveredSources().size() : 0;
        List<Ant> sampleWorker = new ArrayList<>();
        if (colony.getWorkers() != null) {
            for (Ant w : colony.getWorkers()) {
                if (w.getAntType() == GameConstants.TYPE_WORKER) {
                    sampleWorker.add(w);
                    break;
                }
            }
        }
        float maxEffDistance = (locations == null || sampleWorker.isEmpty())
                ? GameNumbers.GATHER_FULL_EFFICIENCY_RADIUS_BASE
                : locations.computeFullEfficiencyRadius(colony, sampleWorker);
        model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_CAT_OVERWORLD), LanguageStrings.get(LanguageStrings.STAT_HEX_DEPLETION), depletionPct + "%"});
        model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_CAT_OVERWORLD), LanguageStrings.get(LanguageStrings.STAT_HEX_SOURCES_FOUND), sourcesFound});
        model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_CAT_OVERWORLD), LanguageStrings.get(LanguageStrings.STAT_HEX_MAX_EFFICIENCY_DISTANCE), AssetStyles.formatNumber(maxEffDistance)});

        // Neighbors
        model.addRow(new Object[]{null, null, sep, sep});
        model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_NEIGHBORS), LanguageStrings.get(LanguageStrings.STAT_NEIGHBOR_NORTH), getHexSummary(hex.getNorth())});
        model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_NEIGHBORS), LanguageStrings.get(LanguageStrings.STAT_NEIGHBOR_NORTH_WEST), getHexSummary(hex.getNorthWest())});
        model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_NEIGHBORS), LanguageStrings.get(LanguageStrings.STAT_NEIGHBOR_NORTH_EAST), getHexSummary(hex.getNorthEast())});
        model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_NEIGHBORS), LanguageStrings.get(LanguageStrings.STAT_NEIGHBOR_SOUTH), getHexSummary(hex.getSouth())});
        model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_NEIGHBORS), LanguageStrings.get(LanguageStrings.STAT_NEIGHBOR_SOUTH_WEST), getHexSummary(hex.getSouthWest())});
        model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_NEIGHBORS), LanguageStrings.get(LanguageStrings.STAT_NEIGHBOR_SOUTH_EAST), getHexSummary(hex.getSouthEast())});
    }

    private String getHexSummary(Hex neighbor) {
        if (neighbor == null) return LanguageStrings.get(LanguageStrings.STAT_EDGE_WORLD);
        String summary = (neighbor.getBiome() != null ? neighbor.getBiome().getName() : LanguageStrings.get(LanguageStrings.STAT_UNKNOWN));
        if (neighbor.getColony() != null) {
            summary += LanguageStrings.format(LanguageStrings.STAT_HEX_COLONY_FMT, neighbor.getColony().getName());
        }
        return summary;
    }

    private void appendDynastyMilitaryRows(DefaultTableModel model, String category, Dynasty dynasty) {
        World world = engine != null ? engine.getWorld() : null;
        model.addRow(new Object[]{
                GameConstants.ICON_STAT_MILITARY_POWER,
                category,
                LanguageStrings.get(LanguageStrings.STAT_MILITARY_POWER),
                dynasty.getMilitaryPower()
        });
        model.addRow(new Object[]{
                GameConstants.ICON_STAT_MILITARY_POWER,
                category,
                LanguageStrings.get(LanguageStrings.STAT_ACTIVE_MILITARY_POWER),
                dynastyStatsService.getActiveMilitaryPower(dynasty)
        });
        model.addRow(new Object[]{
                GameConstants.ICON_STAT_MILITARY_POWER,
                category,
                LanguageStrings.get(LanguageStrings.STAT_RESERVE_MILITARY_POWER),
                dynastyStatsService.getReserveMilitaryPower(dynasty)
        });
        model.addRow(new Object[]{
                GameConstants.ICON_STAT_COMBAT_CAPACITY,
                category,
                LanguageStrings.get(LanguageStrings.STAT_COMBAT_CAPACITY),
                dynastyStatsService.getCombatCapacity(dynasty)
        });
        model.addRow(new Object[]{
                GameConstants.ROLE_CARRIER.getIcon(),
                category,
                LanguageStrings.get(LanguageStrings.STAT_ASSIGNED_CARRIERS),
                dynastyStatsService.getTotalAssignedCarriers(dynasty)
        });
        float reinforcementRate = dynastyStatsService.getDailyReinforcementRate(dynasty, world);
        model.addRow(new Object[]{
                null,
                category,
                LanguageStrings.get(LanguageStrings.STAT_DAILY_REINFORCEMENT_RATE),
                String.format("%.0f%%", reinforcementRate * 100f)
        });
        model.addRow(new Object[]{
                GameConstants.ICON_STAT_COMBAT_CAPACITY,
                category,
                LanguageStrings.get(LanguageStrings.STAT_DAILY_REINFORCEMENT_ALLOWANCE),
                dynastyStatsService.getDailyReinforcementAllowancePerLine(dynasty, world)
        });
    }

    private void appendColonyMilitaryRows(DefaultTableModel model, String category, Colony targetColony) {
        model.addRow(new Object[]{
                GameConstants.ICON_STAT_MILITARY_POWER,
                category,
                LanguageStrings.get(LanguageStrings.STAT_MILITARY_POWER),
                targetColony.getMilitaryPower()
        });
        model.addRow(new Object[]{
                GameConstants.ICON_STAT_MILITARY_POWER,
                category,
                LanguageStrings.get(LanguageStrings.STAT_ACTIVE_MILITARY_POWER),
                targetColony.getActiveMilitaryPower()
        });
        model.addRow(new Object[]{
                GameConstants.ICON_STAT_MILITARY_POWER,
                category,
                LanguageStrings.get(LanguageStrings.STAT_RESERVE_MILITARY_POWER),
                targetColony.getReserveMilitaryPower()
        });
        model.addRow(new Object[]{
                GameConstants.ROLE_CARRIER.getIcon(),
                category,
                LanguageStrings.get(LanguageStrings.STAT_ASSIGNED_CARRIERS),
                dynastyStatsService.getAssignedCarriers(targetColony)
        });
    }

    private void updateGeneralData() {
        DefaultTableModel model = (DefaultTableModel) generalTable.getModel();
        model.setRowCount(0);

        if (dynastyModeToggle.isSelected()) {
            Dynasty dynasty = colony.getDynasty();
            if (dynasty != null) {
                model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_DYNASTY), LanguageStrings.get(LanguageStrings.COL_VALUE), dynasty.getName()});
                model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_DYNASTY), LanguageStrings.get(LanguageStrings.COL_VALUE), dynasty.getRank().getName()});
                model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_DYNASTY), LanguageStrings.get(LanguageStrings.COL_VALUE), dynasty.getSpecies() != null ? dynasty.getSpecies().getName() : LanguageStrings.get(LanguageStrings.STAT_UNKNOWN)});
                model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_DYNASTY), LanguageStrings.get(LanguageStrings.STAT_TOTAL_COLONIES), dynasty.getColonies().size()});
                model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_DYNASTY), LanguageStrings.get(LanguageStrings.STAT_GLOBAL_POP), dynastyStatsService.getTotalPopulation(dynasty)});
                model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_DYNASTY), LanguageStrings.get(LanguageStrings.STAT_GLOBAL_QUEENS), dynastyStatsService.getTotalQueens(dynasty)});
                appendDynastyMilitaryRows(model, LanguageStrings.get(LanguageStrings.STAT_DYNASTY), dynasty);
            }
        } else {
            // Colony Info
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.PANEL_COLONY), LanguageStrings.get(LanguageStrings.COL_VALUE), colony.getName()});
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.PANEL_COLONY), LanguageStrings.get(LanguageStrings.COL_VALUE), colony.getRank().getName()});
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.PANEL_COLONY), LanguageStrings.get(LanguageStrings.COL_VALUE), colony.getSpecies() != null ? colony.getSpecies().getName() : LanguageStrings.get(LanguageStrings.STAT_UNKNOWN)});
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.PANEL_COLONY), LanguageStrings.get(LanguageStrings.STAT_SPECIES_SCIENTIFIC), colony.getSpecies() != null ? colony.getSpecies().getScientific() : LanguageStrings.get(LanguageStrings.STAT_UNKNOWN)});
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.PANEL_COLONY), LanguageStrings.get(LanguageStrings.STAT_LABEL_ID), String.valueOf(colony.getId())});
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.PANEL_COLONY), LanguageStrings.get(LanguageStrings.STAT_AGE), AssetStyles.formatNumber(colony.getAge()) + LanguageStrings.get(LanguageStrings.STAT_DAYS_SUFFIX)});

            int effectiveLoyalty = colony.getEffectiveLoyalty(engine.getTradeManager(), engine.getWorld());
            model.addRow(new Object[]{
                    LanguageStrings.get(LanguageStrings.PANEL_COLONY),
                    LanguageStrings.get(LanguageStrings.STAT_LOYALTY),
                    effectiveLoyalty
            });
            appendColonyMilitaryRows(model, LanguageStrings.get(LanguageStrings.PANEL_COLONY), colony);
            
            if (colony.getQueens().isEmpty()) {
                model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.PANEL_COLONY), LanguageStrings.get(LanguageStrings.STAT_QUEEN_STATUS), LanguageStrings.get(LanguageStrings.UI_MISSING) + " (" + AssetStyles.formatNumber(colony.getDaysWithoutQueen()) + LanguageStrings.get(LanguageStrings.STAT_DAYS_SUFFIX) + ")"});
            } else {
                model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.PANEL_COLONY), LanguageStrings.get(LanguageStrings.STAT_QUEEN_STATUS), LanguageStrings.get(LanguageStrings.UI_HEALTHY) + " (" + AssetStyles.formatNumber(colony.getQueens().size()) + " " + LanguageStrings.get(LanguageStrings.UI_TOTAL) + ")"});
            }

            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.PANEL_COLONY), LanguageStrings.get(LanguageStrings.STAT_AUTOMATION), colony.isAutomationEnabled() ? LanguageStrings.get(LanguageStrings.UI_ENABLED) : LanguageStrings.get(LanguageStrings.UI_DISABLED)});
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.PANEL_COLONY), LanguageStrings.get(LanguageStrings.STAT_AUTO_BUILD), colony.isAutoBuildEnabled() ? LanguageStrings.get(LanguageStrings.UI_ENABLED) : LanguageStrings.get(LanguageStrings.UI_DISABLED)});
        }

        // World Info
        World world = engine != null ? engine.getWorld() : null;
        if (world != null) {
            String dateTime = String.format("%02d:%02d %02d/%02d/%04d",
                world.getHour(), world.getMinute(), world.getDay(), world.getMonth(), world.getYear());
            
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.PANEL_WORLD), LanguageStrings.get(LanguageStrings.STAT_DATE_TIME), dateTime});
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.PANEL_WORLD), LanguageStrings.get(LanguageStrings.STAT_TIME_DAY), world.getTimeOfDay().getName()});
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.PANEL_WORLD), LanguageStrings.get(LanguageStrings.STAT_MOON_PHASE), world.getMoonPhase().getName()});
            
            Season season = world.getSeason();
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_ENVIRONMENT), LanguageStrings.get(LanguageStrings.SEASON_SPRING), season.getName()});
            
            Weather weather = world.getWeather();
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_ENVIRONMENT), LanguageStrings.get(LanguageStrings.PANEL_WORLD), weather.getName()});
            
            if (world.getActiveHex() != null && world.getActiveHex().getBiome() != null) {
                model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_ENVIRONMENT), LanguageStrings.get(LanguageStrings.WORLD_BIOME_PREFIX), world.getActiveHex().getBiome().getName()});
            }
            
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_ENVIRONMENT), LanguageStrings.get(LanguageStrings.WORLD_TEMP_PREFIX), world.getTemperature() + "°C"});
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_ENVIRONMENT), LanguageStrings.get(LanguageStrings.WORLD_HUMIDITY_PREFIX), world.getHumidity()});
        }
    }

    private void updateDynastyData() {
        DefaultTableModel model = (DefaultTableModel) dynastyTable.getModel();
        model.setRowCount(0);
        
        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null) {
            model.addRow(new Object[]{null, LanguageStrings.get(LanguageStrings.STAT_LABEL_ERROR), LanguageStrings.get(LanguageStrings.COL_VALUE), LanguageStrings.get(LanguageStrings.STAT_NO_DYNASTY)});
            return;
        }

        // Basic Info
        model.addRow(new Object[]{null, LanguageStrings.get(LanguageStrings.STAT_DYNASTY), LanguageStrings.get(LanguageStrings.COL_VALUE), dynasty.getName()});
        model.addRow(new Object[]{dynasty.getRank().getIcon(), LanguageStrings.get(LanguageStrings.STAT_DYNASTY), LanguageStrings.get(LanguageStrings.COLONY_RANK), dynasty.getRank().getName()}); 
        model.addRow(new Object[]{null, LanguageStrings.get(LanguageStrings.STAT_DYNASTY), LanguageStrings.get(LanguageStrings.COL_VALUE), dynasty.getSpecies() != null ? dynasty.getSpecies().getName() : LanguageStrings.get(LanguageStrings.SPECIES_OMNI)});
        
        Colony capital = dynasty.getCapital();
        model.addRow(new Object[]{null, LanguageStrings.get(LanguageStrings.STAT_DYNASTY), LanguageStrings.get(LanguageStrings.STAT_CAPITAL), (capital != null ? capital.getName() : LanguageStrings.get(LanguageStrings.ASSIMILATION_NONE))});
        
        model.addRow(new Object[]{null, LanguageStrings.get(LanguageStrings.STAT_DYNASTY), LanguageStrings.get(LanguageStrings.STAT_TOTAL_COLONIES), dynastyStatsService.getTotalColonies(dynasty)});
        model.addRow(new Object[]{GameConstants.ICON_STAT_POPULATION, LanguageStrings.get(LanguageStrings.STAT_DYNASTY), LanguageStrings.get(LanguageStrings.STAT_GLOBAL_POP), dynastyStatsService.getTotalPopulation(dynasty)});
        model.addRow(new Object[]{null, LanguageStrings.get(LanguageStrings.STAT_DYNASTY), LanguageStrings.get(LanguageStrings.STAT_GLOBAL_QUEENS), dynastyStatsService.getTotalQueens(dynasty)});
        model.addRow(new Object[]{null, LanguageStrings.get(LanguageStrings.STAT_DYNASTY), LanguageStrings.get(LanguageStrings.STAT_BIRTH_RATE), LanguageStrings.format(LanguageStrings.STAT_RATE_EGGS_DAY, dynastyStatsService.getGlobalBirthRateDaily(dynasty))});
        appendDynastyMilitaryRows(model, LanguageStrings.get(LanguageStrings.STAT_DYNASTY), dynasty);
        model.addRow(new Object[]{null, LanguageStrings.get(LanguageStrings.STAT_DYNASTY), LanguageStrings.get(LanguageStrings.STAT_NUPTIAL_FLIGHTS), dynasty.getTotalNuptialFlights()});
        model.addRow(new Object[]{GameConstants.ICON_STAT_GENETIC_INTEGRITY, LanguageStrings.get(LanguageStrings.STAT_DYNASTY), LanguageStrings.get(LanguageStrings.STAT_GENETIC_INTEGRITY), String.format("%.1f%%", dynasty.getGeneticIntegrity())});

        // Conquest & Expansion
        model.addRow(new Object[]{null, null, LanguageStrings.get(LanguageStrings.STAT_TABLE_SEPARATOR), LanguageStrings.get(LanguageStrings.STAT_TABLE_SEPARATOR)});
        model.addRow(new Object[]{null, LanguageStrings.get(LanguageStrings.STAT_DYNASTY), LanguageStrings.get(LanguageStrings.STAT_DEFEATED_SPECIES), dynasty.getDefeatedSpeciesIds().size()});
        
        // Unlocks
        model.addRow(new Object[]{null, null, LanguageStrings.get(LanguageStrings.STAT_TABLE_SEPARATOR), LanguageStrings.get(LanguageStrings.STAT_TABLE_SEPARATOR)});
        model.addRow(new Object[]{null, LanguageStrings.get(LanguageStrings.STAT_LABEL_PROGRESS), LanguageStrings.get(LanguageStrings.STAT_UPGRADES_RES), dynasty.getUnlockedUpgrades().size()});
        model.addRow(new Object[]{null, LanguageStrings.get(LanguageStrings.STAT_LABEL_PROGRESS), LanguageStrings.get(LanguageStrings.STAT_ASSIM_COMP), dynasty.getCompletedAssimilations().size()});
        
        int totalBuildings = 0;
        for (Colony c : dynasty.getColonies()) {
            totalBuildings += c.getUnlockedBuildings().size();
        }
        model.addRow(new Object[]{null, LanguageStrings.get(LanguageStrings.STAT_LABEL_PROGRESS), LanguageStrings.get(LanguageStrings.STAT_BUILDINGS_BUILT), totalBuildings});

        // Research
        model.addRow(new Object[]{null, null, LanguageStrings.get(LanguageStrings.STAT_TABLE_SEPARATOR), LanguageStrings.get(LanguageStrings.STAT_TABLE_SEPARATOR)});
        model.addRow(new Object[]{GameConstants.ICON_RESEARCH, LanguageStrings.get(LanguageStrings.TAB_RESEARCH), LanguageStrings.get(LanguageStrings.STAT_STORED_POINTS), dynasty.getResearchPoints()});
        model.addRow(new Object[]{GameConstants.ROLE_RESEARCHER.getIcon(), LanguageStrings.get(LanguageStrings.TAB_RESEARCH), LanguageStrings.get(LanguageStrings.STAT_GLOBAL_RATE), LanguageStrings.format(LanguageStrings.STAT_PTS_DAY_FORMAT, dynastyStatsService.getGlobalResearchRateDaily(dynasty))});

        // Global Resources
        model.addRow(new Object[]{null, null, LanguageStrings.get(LanguageStrings.STAT_TABLE_SEPARATOR), LanguageStrings.get(LanguageStrings.STAT_TABLE_SEPARATOR)});
        Map<ResourceType, Integer> resources = dynastyStatsService.getGlobalResources(dynasty);
        
        model.addRow(new Object[]{GameConstants.RESOURCE_PLANT.getIcon(), LanguageStrings.get(LanguageStrings.PANEL_RESOURCES), LanguageStrings.format(LanguageStrings.STAT_GLOBAL_RESOURCE_FMT, LanguageStrings.get(LanguageStrings.RESOURCE_PLANT)), resources.get(GameConstants.RESOURCE_PLANT)});
        model.addRow(new Object[]{GameConstants.RESOURCE_FUNGI.getIcon(), LanguageStrings.get(LanguageStrings.PANEL_RESOURCES), LanguageStrings.format(LanguageStrings.STAT_GLOBAL_RESOURCE_FMT, LanguageStrings.get(LanguageStrings.RESOURCE_FUNGI)), resources.get(GameConstants.RESOURCE_FUNGI)});
        model.addRow(new Object[]{GameConstants.RESOURCE_MEAT.getIcon(), LanguageStrings.get(LanguageStrings.PANEL_RESOURCES), LanguageStrings.format(LanguageStrings.STAT_GLOBAL_RESOURCE_FMT, LanguageStrings.get(LanguageStrings.RESOURCE_MEAT)), resources.get(GameConstants.RESOURCE_MEAT)});
        model.addRow(new Object[]{GameConstants.RESOURCE_WATER.getIcon(), LanguageStrings.get(LanguageStrings.PANEL_RESOURCES), LanguageStrings.format(LanguageStrings.STAT_GLOBAL_RESOURCE_FMT, LanguageStrings.get(LanguageStrings.RESOURCE_WATER)), resources.get(GameConstants.RESOURCE_WATER)});
        model.addRow(new Object[]{GameConstants.RESOURCE_SYRUP.getIcon(), LanguageStrings.get(LanguageStrings.PANEL_RESOURCES), LanguageStrings.format(LanguageStrings.STAT_GLOBAL_RESOURCE_FMT, LanguageStrings.get(LanguageStrings.RESOURCE_SYRUP)), resources.getOrDefault(GameConstants.RESOURCE_SYRUP, 0)});
        model.addRow(new Object[]{GameConstants.RESOURCE_RESIN.getIcon(), LanguageStrings.get(LanguageStrings.PANEL_RESOURCES), LanguageStrings.format(LanguageStrings.STAT_GLOBAL_RESOURCE_FMT, LanguageStrings.get(LanguageStrings.RESOURCE_RESIN)), resources.getOrDefault(GameConstants.RESOURCE_RESIN, 0)});
        model.addRow(new Object[]{GameConstants.RESOURCE_ROCK.getIcon(), LanguageStrings.get(LanguageStrings.PANEL_RESOURCES), LanguageStrings.format(LanguageStrings.STAT_GLOBAL_RESOURCE_FMT, LanguageStrings.get(LanguageStrings.RESOURCE_ROCK)), resources.getOrDefault(GameConstants.RESOURCE_ROCK, 0)});
        
        // Global Deaths
        model.addRow(new Object[]{null, null, LanguageStrings.get(LanguageStrings.STAT_TABLE_SEPARATOR), LanguageStrings.get(LanguageStrings.STAT_TABLE_SEPARATOR)});
        int totalDeaths = 0;
        if (dynasty.getGlobalDeathStatistics() != null) {
            totalDeaths = dynasty.getGlobalDeathStatistics().values().stream().mapToInt(Integer::intValue).sum();
        }
        model.addRow(new Object[]{GameConstants.STATUS_DEAD.getIcon(), LanguageStrings.get(LanguageStrings.STAT_MORTALITY), LanguageStrings.get(LanguageStrings.STAT_GLOBAL_DEATHS), totalDeaths});
    }

    private void updateWorldHistoryData() {
        if (worldHistoryTable == null) {
            return;
        }
        DefaultTableModel model = (DefaultTableModel) worldHistoryTable.getModel();
        model.setRowCount(0);
        World world = engine != null ? engine.getWorld() : null;
        if (world == null || world.getHistoryService() == null) {
            model.addRow(new Object[]{"—", "—", LanguageStrings.get(LanguageStrings.HISTORY_EMPTY)});
            return;
        }
        HistoryFilterOption filter = worldHistoryFilter != null
                ? (HistoryFilterOption) worldHistoryFilter.getSelectedItem()
                : HistoryFilterOption.ALL;
        List<WorldHistoryEvent> events = filter == null || filter.category == null
                ? world.getHistoryService().getEvents()
                : world.getHistoryService().getEvents(filter.category);
        boolean ownOnly = worldHistoryOwnOnly != null && worldHistoryOwnOnly.isSelected();
        int ownDynastyId = -1;
        if (ownOnly) {
            Dynasty own = colony != null ? colony.getDynasty() : null;
            if (own == null) {
                for (Dynasty d : world.getDynastys()) {
                    if (d.isPlayer()) {
                        own = d;
                        break;
                    }
                }
            }
            ownDynastyId = own != null ? own.getId() : -1;
        }
        int shown = 0;
        for (WorldHistoryEvent event : events) {
            if (ownOnly && (ownDynastyId <= 0 || !event.involvesDynasty(world, ownDynastyId))) {
                continue;
            }
            model.addRow(new Object[]{
                    event.formatDateTime(),
                    event.formatCategoryLabel(),
                    event.formatMessage(world)
            });
            shown++;
        }
        if (shown == 0) {
            model.addRow(new Object[]{"—", "—", LanguageStrings.get(LanguageStrings.HISTORY_EMPTY)});
        }
    }

    private void refreshWorldHistoryFilterLabels() {
        HistoryFilterOption selected = (HistoryFilterOption) worldHistoryFilter.getSelectedItem();
        worldHistoryFilter.removeAllItems();
        for (HistoryFilterOption option : HistoryFilterOption.values()) {
            worldHistoryFilter.addItem(option);
        }
        if (selected != null) {
            worldHistoryFilter.setSelectedItem(selected);
        }
    }

    private enum HistoryFilterOption {
        ALL(null, LanguageStrings.HISTORY_FILTER_ALL),
        COLONY(WorldHistoryEventType.WorldHistoryCategory.COLONY, LanguageStrings.HISTORY_FILTER_COLONY),
        DYNASTY(WorldHistoryEventType.WorldHistoryCategory.DYNASTY, LanguageStrings.HISTORY_FILTER_DYNASTY),
        WAR(WorldHistoryEventType.WorldHistoryCategory.WAR, LanguageStrings.HISTORY_FILTER_WAR),
        BATTLE(WorldHistoryEventType.WorldHistoryCategory.BATTLE, LanguageStrings.HISTORY_FILTER_BATTLE),
        REBELLION(WorldHistoryEventType.WorldHistoryCategory.REBELLION, LanguageStrings.HISTORY_FILTER_REBELLION),
        NUPTIAL(WorldHistoryEventType.WorldHistoryCategory.NUPTIAL, LanguageStrings.HISTORY_FILTER_NUPTIAL),
        ASSIMILATION(WorldHistoryEventType.WorldHistoryCategory.ASSIMILATION, LanguageStrings.HISTORY_FILTER_ASSIMILATION);

        private final WorldHistoryEventType.WorldHistoryCategory category;
        private final String labelKey;

        HistoryFilterOption(WorldHistoryEventType.WorldHistoryCategory category, String labelKey) {
            this.category = category;
            this.labelKey = labelKey;
        }

        @Override
        public String toString() {
            return LanguageStrings.get(labelKey);
        }
    }

    private void updateResourceData() {
        DefaultTableModel model = (DefaultTableModel) resourcesTable.getModel();
        model.setRowCount(0);

        List<Colony> coloniesToCount = new ArrayList<>();
        if (dynastyModeToggle.isSelected() && colony.getDynasty() != null) {
            coloniesToCount.addAll(colony.getDynasty().getColonies());
        } else {
            coloniesToCount.add(colony);
        }

        long totalPlants = 0, totalMushrooms = 0, totalProtein = 0, totalWater = 0, totalSyrups = 0, totalResins = 0, totalMinerals = 0;
        int capPlants = 0, capMushrooms = 0, capProtein = 0, capWater = 0, capSyrups = 0, capResins = 0, capMinerals = 0;
        int sourcesPlants = 0, sourcesProtein = 0, sourcesWater = 0, sourcesMinerals = 0;
        int maxSources = 0;
        int prodPlants = 0, prodMushrooms = 0, prodProtein = 0, prodWater = 0, prodSyrups = 0, prodResins = 0, prodMinerals = 0;
        int consPlants = 0, consMushrooms = 0, consProtein = 0, consWater = 0, consMinerals = 0;
        int subtypeFoodOverhead = 0;

        for (Colony c : coloniesToCount) {
            ColonyStatsService cs = c.getStatsService();
            ColonyLocationService ls = c.getLocationService();

            totalPlants += c.getPlants();
            totalMushrooms += c.getMushrooms();
            totalProtein += c.getProtein();
            totalWater += c.getWater();
            totalSyrups += c.getSyrups();
            totalResins += c.getResins();
            totalMinerals += c.getMinerals();

            capPlants += cs.getPlantsCapacity(c);
            capMushrooms += cs.getMushroomsCapacity(c);
            capProtein += cs.getProteinCapacity(c);
            capWater += cs.getWaterCapacity(c);
            capSyrups += cs.getSyrupsCapacity(c);
            capResins += cs.getResinsCapacity(c);
            capMinerals += cs.getMineralsCapacity(c);

            if (ls != null) {
                sourcesPlants += ls.getSourcesByType(GameConstants.RESOURCE_PLANT).size();
                sourcesProtein += ls.getSourcesByType(GameConstants.RESOURCE_MEAT).size();
                sourcesWater += ls.getSourcesByType(GameConstants.RESOURCE_WATER).size();
                sourcesMinerals += ls.getSourcesByType(GameConstants.RESOURCE_ROCK).size();
            }
            maxSources += cs.getSourceCapacity(c);

            prodPlants += cs.getPlantProduction(c);
            prodMushrooms += cs.getTotalProduction(c);
            prodProtein += cs.getProteinProduction(c);
            prodWater += cs.getWaterProduction(c);
            prodMinerals += cs.getMineralProduction(c);
            prodSyrups += c.getAphids();
            prodResins += (int)(cs.getPlantProduction(c) * 0.01);

            consPlants += cs.getPlantConsumption(c);
            consMushrooms += cs.getTotalConsumption(c);
            subtypeFoodOverhead += cs.getSubtypeFoodOverhead(c);
            consProtein += cs.getProteinConsumption(c);
            consWater += cs.getWaterConsumption(c);
        }

        addResourceRow(model, GameConstants.RESOURCE_PLANT.getIcon(), LanguageStrings.get(LanguageStrings.RESOURCE_PLANT), (int)totalPlants, capPlants, sourcesPlants, maxSources, prodPlants, consPlants);
        addResourceRow(model, GameConstants.RESOURCE_FUNGI.getIcon(), LanguageStrings.get(LanguageStrings.RESOURCE_FUNGI), (int)totalMushrooms, capMushrooms, 0, 0, prodMushrooms, consMushrooms);
        addResourceRow(model, GameConstants.RESOURCE_MEAT.getIcon(), LanguageStrings.get(LanguageStrings.RESOURCE_MEAT), (int)totalProtein, capProtein, sourcesProtein, maxSources, prodProtein, consProtein);
        addResourceRow(model, GameConstants.RESOURCE_WATER.getIcon(), LanguageStrings.get(LanguageStrings.RESOURCE_WATER), (int)totalWater, capWater, sourcesWater, maxSources, prodWater, consWater);
        addResourceRow(model, GameConstants.RESOURCE_ROCK.getIcon(), LanguageStrings.get(LanguageStrings.RESOURCE_ROCK), (int)totalMinerals, capMinerals, sourcesMinerals, maxSources, prodMinerals, consMinerals);
        addResourceRow(model, GameConstants.RESOURCE_SYRUP.getIcon(), LanguageStrings.get(LanguageStrings.RESOURCE_SYRUP), (int)totalSyrups, capSyrups, 0, 0, prodSyrups, 0);
        addResourceRow(model, GameConstants.RESOURCE_RESIN.getIcon(), LanguageStrings.get(LanguageStrings.RESOURCE_RESIN), (int)totalResins, capResins, 0, 0, prodResins, 0);

        model.addRow(new Object[]{null, LanguageStrings.get(LanguageStrings.STAT_TABLE_SEPARATOR), "---", "---", "---", "---", "---", "---"});
        int netFood = prodMushrooms - consMushrooms;
        model.addRow(new Object[]{null, LanguageStrings.get(LanguageStrings.STAT_TOTAL_FOOD), "---", "---", "---", prodMushrooms, consMushrooms, AssetStyles.formatSignedNumber(netFood)});
        if (subtypeFoodOverhead > 0) {
            model.addRow(new Object[]{GameConstants.ICON_STAT_FOOD_CONSUMPTION, LanguageStrings.get(LanguageStrings.STAT_SUBTYPE_FOOD_OVERHEAD),
                    "---", "---", "---", "---", subtypeFoodOverhead, AssetStyles.formatSignedNumber(subtypeFoodOverhead)});
        }
    }

    private void addResourceRow(DefaultTableModel model, ImageIcon icon, String name, int current, int cap, int sources, int maxSources, int production, int consumption) {
        String sourceStr = (maxSources > 0) ? AssetStyles.formatRatio(sources, maxSources) : LanguageStrings.get(LanguageStrings.WORLD_NA);
        String prodStr = AssetStyles.formatNumber(production);
        String consStr = AssetStyles.formatNumber(consumption);
        
        int net = production - consumption;
        String netStr = AssetStyles.formatSignedNumber(net);
        
        if (name.equals(LanguageStrings.get(LanguageStrings.RESOURCE_SYRUP)) || name.equals(LanguageStrings.get(LanguageStrings.RESOURCE_RESIN))) {
            if (production == 0 && consumption == 0) {
                prodStr = "---"; consStr = "---"; netStr = "---";
            }
        }
        model.addRow(new Object[]{icon, name, current, cap, sourceStr, prodStr, consStr, netStr});
    }

    private void updatePopulationData() {
        DefaultTableModel model = (DefaultTableModel) populationTable.getModel();
        model.setRowCount(0);

        List<Colony> coloniesToCount = new ArrayList<>();
        if (dynastyModeToggle.isSelected() && colony.getDynasty() != null) {
            coloniesToCount.addAll(colony.getDynasty().getColonies());
        } else {
            coloniesToCount.add(colony);
        }

        Map<AntType, Integer> typeTotals = new HashMap<>();
        Map<AntRole, Integer> roleTotals = new HashMap<>();
        int totalJuvenile = 0;
        int totalAdult = 0;
        int grandTotal = 0;

        for (Colony c : coloniesToCount) {
            grandTotal += c.getAntTotal();
            for (AntType type : GameConstants.getAntTypes()) {
                int count = c.getAntsByType(type).size();
                if (count > 0) {
                    typeTotals.merge(type, count, Integer::sum);
                    if (type == GameConstants.TYPE_EGG || type == GameConstants.TYPE_LARVA || type == GameConstants.TYPE_PUPA) {
                        totalJuvenile += count;
                    } else if (type != GameConstants.TYPE_DEAD) {
                        totalAdult += count;
                    }
                }
            }
            for (AntRole role : GameConstants.getAntRoles()) {
                int count = c.getAssignedRoleCount(role);
                if (count > 0) {
                    roleTotals.merge(role, count, Integer::sum);
                }
            }
        }

        for (AntType type : GameConstants.getAntTypes()) {
            Integer count = typeTotals.get(type);
            if (count == null || count == 0) continue;

            model.addRow(new Object[]{type.getIcon(), type.getName(), LanguageStrings.get(LanguageStrings.UI_TOTAL), count, "—"});

            if (type != GameConstants.TYPE_EGG && type != GameConstants.TYPE_LARVA && type != GameConstants.TYPE_PUPA && type != GameConstants.TYPE_DEAD) {
                for (AntRole role : GameConstants.getAntRoles()) {
                    if (role.getAntType() == type) {
                        Integer rCount = roleTotals.get(role);
                        if (rCount != null && rCount > 0) {
                            model.addRow(new Object[]{null, "", role.getName(), rCount, "—"});
                        }
                    }
                }
            }
        }
        
        String sep = LanguageStrings.get(LanguageStrings.STAT_TABLE_SEPARATOR);
        model.addRow(new Object[]{null, sep, sep, sep, sep});
        model.addRow(new Object[]{GameConstants.TYPE_WORKER.getIcon(), LanguageStrings.get(LanguageStrings.UI_SUMMARY), LanguageStrings.get(LanguageStrings.STAT_ADULTS), totalAdult, "—"});
        model.addRow(new Object[]{GameConstants.TYPE_EGG.getIcon(), LanguageStrings.get(LanguageStrings.UI_SUMMARY), LanguageStrings.get(LanguageStrings.STAT_JUVENILES), totalJuvenile, "—"});
        model.addRow(new Object[]{GameConstants.ICON_STAT_POPULATION, LanguageStrings.get(LanguageStrings.UI_SUMMARY), dynastyModeToggle.isSelected() ? LanguageStrings.get(LanguageStrings.STAT_DYNASTY_TOTAL) : LanguageStrings.get(LanguageStrings.STAT_COLONY_TOTAL), grandTotal, "—"});

        int militaryTotal = 0;
        for (Colony c : coloniesToCount) {
            militaryTotal += c.getMilitaryPower();
        }
        model.addRow(new Object[]{
                GameConstants.ICON_STAT_MILITARY_POWER,
                LanguageStrings.get(LanguageStrings.UI_SUMMARY),
                LanguageStrings.get(LanguageStrings.STAT_MILITARY_POWER),
                militaryTotal,
                "—"
        });

        appendInsectsToPopulation(model, coloniesToCount);
    }

    private void appendInsectsToPopulation(DefaultTableModel model, List<Colony> coloniesToCount) {
        int aphids = 0, aphidCap = 0, ranchers = 0;
        int symbioticMites = 0, symbioticMiteCap = 0, catchers = 0;
        int dermestids = 0, dermestidCap = 0, gravers = 0;
        int poolUsed = 0, poolMax = 0;
        int parasiticMites = 0, slowedAnts = 0, parasiticKillPerDay = 0;
        int projectedParasiticMiteSpawn = 0;
        int requiredSymbioticMites = 0;
        int symbioticMitesForPrevention = 0;
        int parasiteAnts = 0;
        boolean showAphids = false;
        boolean showSymbioticMites = false;
        boolean showDermestids = false;
        boolean showPool = false;
        boolean showParasitic = false;
        boolean showParasiteAnts = false;

        World world = engine != null ? engine.getWorld() : null;
        Season season = world != null ? world.getSeason() : null;

        for (Colony c : coloniesToCount) {
            ColonyCritterHandlingService bugs = c.getBugHandlingService();
            Biome biome = resolveColonyBiome(c, world);

            if (c.hasUpgrade(GameUnlocks.ROLE_RANCHER) || c.getAphids() > 0) {
                showAphids = true;
                aphids += c.getAphids();
                aphidCap += bugs.getMaxCapacity(c, GameConstants.TYPE_APHID);
                ranchers += c.getAssignedRoleCount(GameConstants.ROLE_RANCHER);
            }
            if (c.hasUpgrade(GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE) || c.getSymbioticMites() > 0) {
                showSymbioticMites = true;
                symbioticMites += c.getSymbioticMites();
                symbioticMiteCap += bugs.getMaxCapacity(c, GameConstants.TYPE_SYMBIOTIC_MITE);
            }
            if (c.hasUpgrade(GameUnlocks.ABILITY_CATCH_DERMESTID) || c.getDermestids() > 0) {
                showDermestids = true;
                dermestids += c.getDermestids();
                dermestidCap += bugs.getMaxCapacity(c, GameConstants.TYPE_DERMESTID);
            }
            if (c.hasUpgrade(GameUnlocks.ROLE_CATCHER)) {
                showPool = true;
                catchers += c.getAssignedRoleCount(GameConstants.ROLE_CATCHER);
                poolUsed += bugs.getUnlockedCatcherPoolPetCount(c);
                poolMax += bugs.getUnlockedCatcherPoolCapacityMax(c);
            }
            if (c.hasUpgrade(GameUnlocks.ROLE_GRAVER)) {
                gravers += c.getAssignedRoleCount(GameConstants.ROLE_GRAVER);
            }

            if (c.hasUpgrade(GameUnlocks.ABILITY_PARASITIC_MITE_ALERT)
                    || c.hasUpgrade(GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE)
                    || c.getParasiticMites() > 0) {
                showParasitic = true;
            }
            parasiticMites += c.getParasiticMites();
            slowedAnts += c.getParasiticMiteSlowedAntCount();
            if (c.hasUpgrade(GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE) || c.getSymbioticMites() > 0) {
                parasiticKillPerDay += c.getSymbioticMites() * bugs.getSymbioticMiteParasiticMiteKillPerDay(c);
                symbioticMitesForPrevention += c.getSymbioticMites();
            }
            if (biome != null && season != null) {
                projectedParasiticMiteSpawn += bugs.projectParasiticMiteMonthlySpawn(c, biome, season);
                requiredSymbioticMites += bugs.requiredSymbioticMitesToPreventOutbreak(c, biome, season);
            }
            if (c.hasUpgrade(GameUnlocks.ROLE_POLICE)) {
                showParasiteAnts = true;
                parasiteAnts += c.getParasiteAnts();
            }
        }

        if (!showAphids && !showSymbioticMites && !showDermestids && !showPool
                && !showParasitic && !showParasiteAnts) {
            return;
        }

        String sep = LanguageStrings.get(LanguageStrings.STAT_TABLE_SEPARATOR);
        model.addRow(new Object[]{null, sep, sep, sep, sep});
        model.addRow(new Object[]{
                null,
                LanguageStrings.get(LanguageStrings.STATS_TAB_INSECTS),
                LanguageStrings.get(LanguageStrings.UI_SUMMARY),
                "—",
                "—"
        });

        if (showAphids) {
            model.addRow(new Object[]{
                GameConstants.ICON_APHID,
                LanguageStrings.get(LanguageStrings.BUG_APHID),
                LanguageStrings.format(LanguageStrings.STAT_RATE_RANCHERS_FMT, ranchers),
                aphids,
                aphidCap
            });
        }
        if (showSymbioticMites) {
            model.addRow(new Object[]{
                GameConstants.ICON_SYMBIOTIC_MITE,
                LanguageStrings.get(LanguageStrings.BUG_SYMBIOTIC_MITE),
                LanguageStrings.format(LanguageStrings.STAT_INSECT_CATCHERS_FMT, catchers),
                symbioticMites,
                symbioticMiteCap
            });
        }
        if (showDermestids) {
            model.addRow(new Object[]{
                GameConstants.ICON_DERMESTID,
                LanguageStrings.get(LanguageStrings.BUG_DERMESTID),
                LanguageStrings.format(LanguageStrings.STAT_RATE_GRAVERS_FMT, gravers),
                dermestids,
                dermestidCap
            });
        }
        if (showPool) {
            model.addRow(new Object[]{
                null,
                LanguageStrings.get(LanguageStrings.STAT_INSECT_POOL),
                LanguageStrings.format(LanguageStrings.STAT_INSECT_CATCHERS_FMT, catchers),
                poolUsed,
                poolMax
            });
        }

        if (showParasitic || showParasiteAnts) {
            if (showAphids || showSymbioticMites || showDermestids || showPool) {
                model.addRow(new Object[]{null, sep, sep, sep, sep});
            }
            if (showParasitic) {
                String rateCol = LanguageStrings.format(
                        LanguageStrings.STAT_INSECT_PARASITIC_KILL_FMT,
                        slowedAnts, parasiticKillPerDay);
                if (projectedParasiticMiteSpawn > 0) {
                    String prevention = LanguageStrings.format(
                            LanguageStrings.STAT_OUTBREAK_PREV_FMT,
                            symbioticMitesForPrevention, requiredSymbioticMites, projectedParasiticMiteSpawn);
                    if (symbioticMitesForPrevention >= requiredSymbioticMites) {
                        prevention = LanguageStrings.get(LanguageStrings.STAT_OUTBREAK_PREV_BLOCKED) + " — " + prevention;
                    }
                    rateCol = rateCol + " | " + prevention;
                }
                model.addRow(new Object[]{
                    GameConstants.ICON_PARASITIC_MITE,
                    LanguageStrings.get(LanguageStrings.BUG_PARASITIC_MITE),
                    rateCol,
                    parasiticMites,
                    projectedParasiticMiteSpawn > 0
                            ? LanguageStrings.format(LanguageStrings.STAT_OUTBREAK_PREV_PROJECTED_FMT, projectedParasiticMiteSpawn)
                            : LanguageStrings.format(LanguageStrings.STAT_INSECT_PARASITIC_CAP_FMT,
                                    GameNumbers.PARASITIC_MITES_PER_SLOWED_ANT)
                });
            }
            if (showParasiteAnts) {
                model.addRow(new Object[]{
                    GameConstants.TYPE_PARASITE_ANT.getIcon(),
                    LanguageStrings.get(LanguageStrings.BUG_PARASITE_ANT),
                    LanguageStrings.get(LanguageStrings.ROLE_POLICE),
                    parasiteAnts,
                    LanguageStrings.get(LanguageStrings.WORLD_NA)
                });
            }
        }
    }

    private void updateRatesData() {
        DefaultTableModel model = (DefaultTableModel) ratesTable.getModel();
        model.setRowCount(0);

        List<Colony> coloniesToCount = new ArrayList<>();
        if (dynastyModeToggle.isSelected() && colony.getDynasty() != null) {
            coloniesToCount.addAll(colony.getDynasty().getColonies());
        } else {
            coloniesToCount.add(colony);
        }

        int foragers = 0, dailyForage = 0;
        int farmers = 0, dailyFarm = 0;
        int hunters = 0, dailyHunt = 0;
        int miners = 0, dailyMine = 0;
        int scouts = 0;
        int researchers = 0, assistants = 0, dailyRP = 0;
        int layers = 0, dailyEggs = 0;
        int nurses = 0, babies = 0, nurseCap = 0;
        int gravers = 0, deadAnts = 0, graveCap = 0;
        int ranchers = 0, aphidCap = 0, aphids = 0;
        int police = 0, parasiteAnts = 0, dailyDetect = 0;
        int projectedParasiteAntSpawn = 0;
        int requiredPoliceForPrevention = 0;

        World world = engine != null ? engine.getWorld() : null;
        Season season = world != null ? world.getSeason() : null;

        for (Colony c : coloniesToCount) {
            ColonyStatsService cs = c.getStatsService();
            Biome biome = resolveColonyBiome(c, world);
            
            foragers += c.getAssignedRoleCount(GameConstants.ROLE_FORAGER);
            dailyForage += cs.getPlantProduction(c) + cs.getWaterProduction(c);

            farmers += cs.getEffectiveFarmerCount(c);
            dailyFarm += (int)(cs.getEffectiveFarmerCount(c) * cs.getConversionRate(c) * 1440);

            hunters += c.getAssignedRoleCount(GameConstants.ROLE_HUNTER);
            dailyHunt += cs.getProteinProduction(c);

            miners += c.getAssignedRoleCount(GameConstants.ROLE_MINER);
            dailyMine += cs.getMineralProduction(c);

            scouts += c.getAssignedRoleCount(GameConstants.ROLE_SCOUT);

            researchers += c.getAssignedRoleCount(GameConstants.ROLE_RESEARCHER);
            assistants += c.getAssignedRoleCount(GameConstants.ROLE_ASSISTANT);
            dailyRP += cs.getDailyResearchPoints(c);

            layers += c.getAssignedRoleCount(GameConstants.ROLE_LAYER);
            dailyEggs += (int)(c.getAssignedRoleCount(GameConstants.ROLE_LAYER) * cs.getLayingRate(c) * 24);

            nurses += c.getAssignedRoleCount(GameConstants.ROLE_NURSE);
            babies += c.getEggs().size() + c.getLarvae().size() + c.getPupae().size();
            nurseCap += (int)(c.getAssignedRoleCount(GameConstants.ROLE_NURSE) * cs.getNursingRate(c));

            gravers += c.getAssignedRoleCount(GameConstants.ROLE_GRAVER);
            deadAnts += c.getDeadAnts().size();
            graveCap += (int)(c.getAssignedRoleCount(GameConstants.ROLE_GRAVER) * cs.getGravingRate(c));

            ranchers += c.getAssignedRoleCount(GameConstants.ROLE_RANCHER);
            aphids += c.getAphids();
            aphidCap += c.getBugHandlingService().getMaxCapacity(c, GameConstants.TYPE_APHID);

            police += c.getAssignedRoleCount(GameConstants.ROLE_POLICE);
            parasiteAnts += c.getParasiteAnts();
            dailyDetect += Math.round(c.getAssignedRoleCount(GameConstants.ROLE_POLICE) * cs.getParasiteDetection(c));
            if (biome != null && season != null) {
                projectedParasiteAntSpawn += c.getPopulationService().projectParasiteAntMonthlySpawn(c, biome, season);
                requiredPoliceForPrevention += c.getPopulationService().requiredPoliceToPreventParasiteAntOutbreak(c, biome, season);
            }
        }

        if (foragers > 0) model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.ROLE_FORAGER), AssetStyles.formatNumber(foragers) + " " + LanguageStrings.get(LanguageStrings.ROLE_FORAGER), LanguageStrings.get(LanguageStrings.UI_COMBINED), LanguageStrings.format(LanguageStrings.STAT_RATE_RES_DAY, dailyForage)});
        if (farmers > 0) model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_JOB_FARMING), LanguageStrings.format(LanguageStrings.STAT_JOB_FARMERS_EFF_FMT, farmers), LanguageStrings.get(LanguageStrings.UI_COMBINED), LanguageStrings.format(LanguageStrings.STAT_RATE_CONVERT_DAY, dailyFarm)});
        if (hunters > 0) model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.ROLE_HUNTER), AssetStyles.formatNumber(hunters) + " " + LanguageStrings.get(LanguageStrings.ROLE_HUNTER), LanguageStrings.get(LanguageStrings.UI_COMBINED), LanguageStrings.format(LanguageStrings.STAT_RATE_PWR_DAY, dailyHunt)});
        if (miners > 0) model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.ROLE_MINER), AssetStyles.formatNumber(miners) + " " + LanguageStrings.get(LanguageStrings.ROLE_MINER), LanguageStrings.get(LanguageStrings.UI_COMBINED), LanguageStrings.format(LanguageStrings.STAT_RATE_PWR_DAY, dailyMine)});
        if (scouts > 0) model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.ROLE_SCOUT), AssetStyles.formatNumber(scouts) + " " + LanguageStrings.get(LanguageStrings.ROLE_SCOUT), "---", LanguageStrings.get(LanguageStrings.STAT_RATE_SCOUT_STATUS)});
        if (researchers + assistants > 0 || dailyRP > 0) model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.TAB_RESEARCH), LanguageStrings.format(LanguageStrings.STAT_RATE_RESEARCH_ASST_FMT, researchers, assistants), LanguageStrings.get(LanguageStrings.UI_COMBINED), LanguageStrings.format(LanguageStrings.STAT_RATE_PTS_DAY_FMT, dailyRP)});
        if (layers > 0) model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_JOB_EGG_LAYING), LanguageStrings.format(LanguageStrings.STAT_RATE_LAYERS_FMT, layers), LanguageStrings.get(LanguageStrings.UI_COMBINED), LanguageStrings.format(LanguageStrings.STAT_RATE_EGGS_DAY, dailyEggs)});
        if (nurses > 0) model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.ROLE_NURSE), LanguageStrings.format(LanguageStrings.STAT_RATE_NURSES_FMT, nurses), LanguageStrings.format(LanguageStrings.STAT_RATE_CAP_SHORT, nurseCap), LanguageStrings.format(LanguageStrings.STAT_RATE_LOAD_FMT, babies, nurseCap)});
        if (gravers > 0) model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.ROLE_GRAVER), LanguageStrings.format(LanguageStrings.STAT_RATE_GRAVERS_FMT, gravers), LanguageStrings.format(LanguageStrings.STAT_RATE_CAP_SHORT, graveCap), LanguageStrings.format(LanguageStrings.STAT_RATE_LOAD_FMT, deadAnts, graveCap)});
        if (ranchers > 0) model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.ROLE_RANCHER), LanguageStrings.format(LanguageStrings.STAT_RATE_RANCHERS_FMT, ranchers), LanguageStrings.format(LanguageStrings.STAT_RATE_CAP_SHORT, aphidCap), LanguageStrings.format(LanguageStrings.STAT_RATE_APHIDS_FMT, aphids, aphidCap)});
        if (police > 0 || projectedParasiteAntSpawn > 0 || parasiteAnts > 0) {
            String loadCol = projectedParasiteAntSpawn > 0
                    ? LanguageStrings.format(LanguageStrings.STAT_OUTBREAK_PREV_PROJECTED_FMT, projectedParasiteAntSpawn)
                    : LanguageStrings.format(LanguageStrings.STAT_RATE_PARASITE_ANTS_FMT, parasiteAnts);
            String rateCol = LanguageStrings.format(LanguageStrings.STAT_RATE_DET_DAY, dailyDetect);
            if (projectedParasiteAntSpawn > 0) {
                String prevention = LanguageStrings.format(
                        LanguageStrings.STAT_OUTBREAK_PREV_FMT,
                        police, requiredPoliceForPrevention, projectedParasiteAntSpawn);
                if (police >= requiredPoliceForPrevention) {
                    prevention = LanguageStrings.get(LanguageStrings.STAT_OUTBREAK_PREV_BLOCKED) + " — " + prevention;
                }
                rateCol = prevention + " | " + rateCol;
            }
            model.addRow(new Object[]{
                    LanguageStrings.get(LanguageStrings.ROLE_POLICE),
                    LanguageStrings.format(LanguageStrings.STAT_RATE_POLICE_FMT, police),
                    loadCol,
                    rateCol
            });
        }
    }

    private Biome resolveColonyBiome(Colony c, World world) {
        if (c == null || world == null) {
            return null;
        }
        Hex hex = world.getHexOfColony(c);
        return hex != null ? hex.getBiome() : null;
    }

    private void updateDeathData() {
        DefaultTableModel model = (DefaultTableModel) deathTable.getModel();
        model.setRowCount(0);

        List<Colony> coloniesToCount = new ArrayList<>();
        if (dynastyModeToggle.isSelected() && colony.getDynasty() != null) {
            coloniesToCount.addAll(colony.getDynasty().getColonies());
        } else {
            coloniesToCount.add(colony);
        }

        Map<String, Integer> aggregateDeaths = new HashMap<>();
        int totalDeaths = 0;

        for (Colony c : coloniesToCount) {
            if (c.getPopulationService() != null) {
                Map<String, Integer> stats = c.getDeathService().getDeathStatistics();
                for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                    aggregateDeaths.merge(entry.getKey(), entry.getValue(), Integer::sum);
                }
                totalDeaths += c.getTotalDeaths();
            }
        }
        
        if (aggregateDeaths.isEmpty()) {
            model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_NO_DEATHS), 0});
        } else {
            for (Map.Entry<String, Integer> entry : aggregateDeaths.entrySet()) {
                model.addRow(new Object[]{ColonyLogTexts.localizedDeathCause(entry.getKey()), entry.getValue()});
            }
        }
        
        model.addRow(new Object[]{LanguageStrings.get(LanguageStrings.STAT_TABLE_SEPARATOR), LanguageStrings.get(LanguageStrings.STAT_TABLE_SEPARATOR)});
        model.addRow(new Object[]{dynastyModeToggle.isSelected() ? LanguageStrings.get(LanguageStrings.STAT_DYNASTY_TOTAL) : LanguageStrings.get(LanguageStrings.STAT_COLONY_TOTAL), totalDeaths});
    }
}
