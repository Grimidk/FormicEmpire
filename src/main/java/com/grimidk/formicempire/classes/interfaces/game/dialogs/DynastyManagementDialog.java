package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.dynasty.colony.ColonyLoyalty;
import com.grimidk.formicempire.classes.constants.dynasty.DiplomaticReputation;
import com.grimidk.formicempire.classes.constants.dynasty.PactRequestIncomingPolicy;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.dynasty.TradeMethod;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Trade;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.entities.dynasty.CrossDynastyTradeProposal;
import com.grimidk.formicempire.classes.entities.services.colony.ConvoySceneBuilder;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyTradeAutomation;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyDiplomacyService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyIntegrationService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.DynastyColorSwatch;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiTableStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiDialogUtils;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiNameSearchBar;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiOptionPane;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DynastyManagementDialog extends ZeroDialog {

    public static final int TAB_OVERVIEW = 0;
    public static final int TAB_TRADE = 1;
    public static final int TAB_DIPLOMACY = 2;
    public static final int TAB_WARS = 3;

    private final Dynasty dynasty;
    private final Engine engine;
    private final Consumer<Colony> onGoToColony;
    private final Runnable onOpenWarRoles;
    private final Consumer<War> onViewBattle;
    private final Consumer<Trade> onViewConvoy;

    private final JTabbedPane tabbedPane;
    private final Map<Integer, Integer> tabIndexMap = new HashMap<>();
    
    private OverviewPanel overviewPanel;
    private TradePanel tradePanel;
    private DiplomacyPanel diplomacyPanel;
    private WarManagementPanel warsPanel;
    private TradeCreationDialog tradeCreationDialog;

    public DynastyManagementDialog(JFrame owner, Dynasty dynasty, Engine engine, Consumer<Colony> onGoToColony,
            Runnable onOpenWarRoles, Consumer<War> onViewBattle, Consumer<Trade> onViewConvoy) {
        super(owner, LanguageStrings.DIALOG_DYNASTY_TITLE, AssetStyles.DYNASTY_DIALOG_SIZE);
        this.dynasty = dynasty;
        this.engine = engine;
        this.onGoToColony = onGoToColony;
        this.onOpenWarRoles = onOpenWarRoles;
        this.onViewBattle = onViewBattle;
        this.onViewConvoy = onViewConvoy;
        dynasty.bindTradeManager(engine.getTradeManager());

        tabbedPane = new JTabbedPane();
        AssetStyles.styleTabbedPane(tabbedPane);
        tabbedPane.setFocusable(false);
        tabbedPane.addChangeListener(e -> {
            Component selected = tabbedPane.getSelectedComponent();
            if (selected instanceof LiveUpdatePanel) {
                ((LiveUpdatePanel) selected).updateData();
            }
        });
        add(tabbedPane, BorderLayout.CENTER);

        refreshDialog();
        initKeyBindings();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                tabbedPane.requestFocusInWindow();
            }
        });
        
        setLocationRelativeTo(owner);
    }

    public void showDialog(int tabIndex) {
        setTab(tabIndex);
        super.showDialog();
    }
    
    public boolean isTabOpen(int tabIndex) {
        if (!isShowing()) return false;
        Integer index = tabIndexMap.get(tabIndex);
        return index != null && tabbedPane.getSelectedIndex() == index;
    }

    public void setTab(int tabIndex) {
        if (tabIndexMap.containsKey(tabIndex)) {
            tabbedPane.setSelectedIndex(tabIndexMap.get(tabIndex));
        }
    }

    @Override
    public void dispose() {
        disposeTradeCreationDialog();
        super.dispose();
    }

    private void disposeTradeCreationDialog() {
        if (tradeCreationDialog != null) {
            tradeCreationDialog.dispose();
            tradeCreationDialog = null;
        }
    }

    public void liveUpdate() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::liveUpdate);
            return;
        }

        if (!isShowing()) return;

        boolean hasTrade = dynasty.hasUpgrade(GameUnlocks.ABILITY_TRADE);
        int expectedTabs = 3 + (hasTrade ? 1 : 0);
        boolean currentAuto = dynasty.hasUpgrade(GameUnlocks.ABILITY_AUTOMATION);
        boolean currentAutoBuild = dynasty.hasUpgrade(GameUnlocks.ABILITY_MANAGEMENT);
        boolean currentAutoTunnels = dynasty.hasUpgrade(GameUnlocks.ABILITY_AUTO_TUNNELS);
        boolean currentAutoDiplomacy = dynasty.hasUpgrade(GameUnlocks.ABILITY_AUTO_DIPLOMACY);
        
        boolean panelAuto = (overviewPanel != null) && overviewPanel.isShowAutomation();
        boolean panelAutoBuild = (overviewPanel != null) && overviewPanel.isShowAutoBuild();
        boolean panelAutoTunnels = (overviewPanel != null) && overviewPanel.isShowAutoTunnels();
        boolean panelAutoDiplomacy = (overviewPanel != null) && overviewPanel.isShowAutoDiplomacy();

        if (tabbedPane.getTabCount() != expectedTabs || currentAuto != panelAuto || currentAutoBuild != panelAutoBuild
                || currentAutoTunnels != panelAutoTunnels || currentAutoDiplomacy != panelAutoDiplomacy) {
            refreshDialog();
        } else {
            Component selected = tabbedPane.getSelectedComponent();
            if (selected instanceof LiveUpdatePanel) {
                ((LiveUpdatePanel) selected).liveUpdate();
            }
        }
    }

    @Override
    public void refreshTheme() {
        super.refreshTheme();
        AssetStyles.styleTabbedPane(tabbedPane);
        tabbedPane.updateUI();
    }

    @Override
    protected void refreshDialog() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex < 0) selectedIndex = 0;

        tabbedPane.removeAll();
        tabIndexMap.clear();
        int currentIndex = 0;

        boolean currentAuto = dynasty.hasUpgrade(GameUnlocks.ABILITY_AUTOMATION);
        boolean currentAutoBuild = dynasty.hasUpgrade(GameUnlocks.ABILITY_MANAGEMENT);
        boolean currentAutoTunnels = dynasty.hasUpgrade(GameUnlocks.ABILITY_AUTO_TUNNELS);
        boolean currentAutoDiplomacy = dynasty.hasUpgrade(GameUnlocks.ABILITY_AUTO_DIPLOMACY);
        
        if (overviewPanel == null || overviewPanel.isShowAutomation() != currentAuto
                || overviewPanel.isShowAutoBuild() != currentAutoBuild
                || overviewPanel.isShowAutoTunnels() != currentAutoTunnels
                || overviewPanel.isShowAutoDiplomacy() != currentAutoDiplomacy) {
            overviewPanel = new OverviewPanel(currentAutoBuild, currentAuto, currentAutoTunnels, currentAutoDiplomacy);
        }
        overviewPanel.updateData();
        tabbedPane.addTab(LanguageStrings.get(LanguageStrings.TAB_OVERVIEW), overviewPanel);
        tabIndexMap.put(TAB_OVERVIEW, currentIndex++);

        if (dynasty.hasUpgrade(GameUnlocks.ABILITY_TRADE)) {
            if (tradePanel == null) {
                tradePanel = new TradePanel();
            }
            tabbedPane.addTab(LanguageStrings.get(LanguageStrings.TAB_LOGISTICS), tradePanel);
            tabIndexMap.put(TAB_TRADE, currentIndex++);
        }

        if (diplomacyPanel == null) {
            diplomacyPanel = new DiplomacyPanel();
        }
        diplomacyPanel.updateData();
        tabbedPane.addTab(LanguageStrings.get(LanguageStrings.TAB_DIPLOMACY), diplomacyPanel);
        tabIndexMap.put(TAB_DIPLOMACY, currentIndex++);

        if (warsPanel == null) {
            warsPanel = new WarManagementPanel(dynasty, engine, createWarCallbacks());
        }
        warsPanel.updateData();
        tabbedPane.addTab(LanguageStrings.get(LanguageStrings.TAB_WARS), warsPanel);
        tabIndexMap.put(TAB_WARS, currentIndex++);

        if (selectedIndex < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(selectedIndex);
        }
    }

    private void initKeyBindings() {
        ActionMap actionMap = getRootPane().getActionMap();
        InputMap windowMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        InputMap tabMap = tabbedPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        registerTabKeyBinding(windowMap, tabMap, actionMap, KeyEvent.VK_A, TAB_OVERVIEW, "toggleOverview");
        registerTabKeyBinding(windowMap, tabMap, actionMap, KeyEvent.VK_S, TAB_TRADE, "toggleTrade");
        registerTabKeyBinding(windowMap, tabMap, actionMap, KeyEvent.VK_D, TAB_DIPLOMACY, "toggleDiplomacy");
        registerTabKeyBinding(windowMap, tabMap, actionMap, KeyEvent.VK_F, TAB_WARS, "toggleWars");
    }

    private void registerTabKeyBinding(InputMap windowMap, InputMap tabMap, ActionMap actionMap,
            int keyCode, int tabIndex, String actionId) {
        KeyStroke stroke = KeyStroke.getKeyStroke(keyCode, 0);
        windowMap.put(stroke, actionId);
        tabMap.put(stroke, actionId);
        actionMap.put(actionId, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchTabOrClose(tabIndex);
            }
        });
    }

    private void switchTabOrClose(int tabIndex) {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focusOwner instanceof JTextComponent) {
            return;
        }
        if (isTabOpen(tabIndex)) {
            dispose();
        } else if (tabIndexMap.containsKey(tabIndex)) {
            tabbedPane.setSelectedIndex(tabIndexMap.get(tabIndex));
        }
    }

    private WarManagementPanel.Callbacks createWarCallbacks() {
        return new WarManagementPanel.Callbacks() {
            @Override
            public void goToOpponentCapital(Dynasty opponent) {
                if (opponent != null && opponent.getCapital() != null) {
                    onGoToColony.accept(opponent.getCapital());
                }
            }

            @Override
            public void openDiplomacy(Dynasty opponent) {
                setTab(TAB_DIPLOMACY);
            }

            @Override
            public void openWarRoles() {
                if (onOpenWarRoles != null) {
                    onOpenWarRoles.run();
                }
            }

            @Override
            public void viewBattle(War war) {
                if (onViewBattle != null && war != null) {
                    onViewBattle.accept(war);
                }
            }

            @Override
            public void onWarsChanged() {
                liveUpdate();
            }
        };
    }

    interface LiveUpdatePanel {
        void liveUpdate();
        void updateData();
    }

    private Colony getActivePlayerColony() {
        World world = engine.getWorld();
        if (world == null) {
            return null;
        }
        Hex mapActive = world.getActiveHex();
        if (mapActive != null && mapActive.getColony() != null && mapActive.getColony().isPlayer()) {
            return mapActive.getColony();
        }
        return null;
    }

    private void openTradeDialog(Colony origin, Colony target, Trade existingTrade) {
        disposeTradeCreationDialog();
        tradeCreationDialog = new TradeCreationDialog(
                SwingUtilities.getWindowAncestor(this), origin, target, engine, existingTrade);
        tradeCreationDialog.setVisible(true);
    }


    private JPopupMenu buildTradeRowActionsMenu(TradePanel panel, TradePanel.TradeRowData data) {
        JPopupMenu menu = new JPopupMenu();
        if (data == null || data.neighbor == null || panel.activeColony == null) {
            return menu;
        }
        Colony colony = panel.activeColony;
        Trade outgoing = data.outgoingTrade;
        Trade incoming = data.incomingTrade;
        boolean labelRoutes = outgoing != null && incoming != null
                && !panel.isTradeBilateral(outgoing) && !panel.isTradeBilateral(incoming);

        Trade convoyTrade = resolveConvoyTrade(outgoing, incoming);
        if (convoyTrade != null) {
            JMenuItem convoy = new JMenuItem(LanguageStrings.get(LanguageStrings.CONVOY_ACTION_VIEW));
            convoy.addActionListener(e -> {
                if (onViewConvoy != null) {
                    onViewConvoy.accept(convoyTrade);
                }
            });
            AssetStyles.styleMenuItem(convoy);
            menu.add(convoy);
        }

        if (outgoing == null) {
            JMenuItem establish = new JMenuItem(LanguageStrings.get(LanguageStrings.UI_ESTABLISH));
            establish.addActionListener(e -> panel.establishTrade(data.neighbor));
            AssetStyles.styleMenuItem(establish);
            menu.add(establish);
        }

        if (outgoing != null) {
            appendTradeRouteMenuItems(menu, colony, data.neighbor, outgoing, labelRoutes);
        }
        if (incoming != null && (outgoing == null || !panel.isTradeBilateral(outgoing))) {
            appendTradeRouteMenuItems(menu, data.neighbor, colony, incoming, labelRoutes);
        }
        if (panel.shouldShowTwoWayMenuItem(data)) {
            JMenuItem twoWay = new JMenuItem(LanguageStrings.get(LanguageStrings.TRADE_MAKE_TWO_WAY));
            if (panel.canMakeTwoWayTrade(data)) {
                twoWay.addActionListener(e -> panel.makeTwoWayTrade(data));
            } else {
                twoWay.setEnabled(false);
                twoWay.setToolTipText(LanguageStrings.get(LanguageStrings.TRADE_TWO_WAY_REQUIRES_INCOMING));
            }
            AssetStyles.styleMenuItem(twoWay);
            menu.add(twoWay);
        }
        return menu;
    }

    private static Trade resolveConvoyTrade(Trade outgoing, Trade incoming) {
        if (ConvoySceneBuilder.isInTransit(outgoing)) {
            return outgoing;
        }
        if (ConvoySceneBuilder.isInTransit(incoming)) {
            return incoming;
        }
        return null;
    }

    private void appendTradeRouteMenuItems(JPopupMenu menu, Colony origin, Colony target, Trade trade,
            boolean labelRoute) {
        if (trade == null) {
            return;
        }

        String routeLabel = LanguageStrings.format(LanguageStrings.CONVOY_ROUTE_FMT, origin.getName(), target.getName());
        String modifyText = labelRoute
                ? LanguageStrings.get(LanguageStrings.DYNASTY_MODIFY) + " — " + routeLabel
                : LanguageStrings.get(LanguageStrings.DYNASTY_MODIFY);
        JMenuItem modify = new JMenuItem(modifyText);
        modify.addActionListener(e -> openTradeDialog(origin, target, trade));
        AssetStyles.styleMenuItem(modify);
        menu.add(modify);

        JMenuItem cancel = new JMenuItem(LanguageStrings.get(LanguageStrings.DYNASTY_CANCEL_ROUTE));
        cancel.addActionListener(e -> cancelTradeRoute(trade));
        AssetStyles.styleMenuItem(cancel);
        menu.add(cancel);
    }

    private void cancelTradeRoute(Trade trade) {
        if (trade == null || engine.getTradeManager() == null) {
            return;
        }
        trade.cancel();
        engine.getTradeManager().removeTrade(trade);
        if (tradePanel != null) {
            tradePanel.updateData();
        }
    }

    private void showTradeRowActionsMenu(TradePanel.TradeRowData data, JTable table, int row, int column) {
        JPopupMenu menu = buildTradeRowActionsMenu(tradePanel, data);
        if (menu.getComponentCount() == 0) {
            return;
        }
        UiTableStyles.showCellPopupMenu(menu, table, row, column);
    }

    private void showTradeRowActionsMenu(TradePanel.TradeRowData data, Component invoker) {
        JPopupMenu menu = buildTradeRowActionsMenu(tradePanel, data);
        if (menu.getComponentCount() == 0) {
            return;
        }
        UiTableStyles.showComponentPopupMenu(menu, invoker);
    }

    private class TradePanel extends JPanel implements LiveUpdatePanel {
        private JTable table;
        private DefaultTableModel model;
        private JScrollPane tableScrollPane;
        private Colony activeColony;
        private JLabel activeColonyLabel;
        private boolean tunnelsVisible = true;

        public TradePanel() {
            super(new BorderLayout());
            initUI();
        }

        private void initUI() {
            removeAll();
            tunnelsVisible = dynasty.hasUpgrade(GameUnlocks.ABILITY_TUNNELS);
            
            List<String> cols = new ArrayList<>(List.of(LanguageStrings.get(LanguageStrings.DYNASTY_DIRECTION), LanguageStrings.get(LanguageStrings.DYNASTY_NEIGHBOR)));
            if (tunnelsVisible) cols.add(LanguageStrings.get(LanguageStrings.DYNASTY_TUNNEL_STATUS));
            cols.addAll(List.of(LanguageStrings.get(LanguageStrings.DYNASTY_OUTGOING), LanguageStrings.get(LanguageStrings.DYNASTY_INCOMING), LanguageStrings.get(LanguageStrings.DYNASTY_ACTIONS)));

            model = new DefaultTableModel(cols.toArray(), 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    int last = getColumnCount() - 1;
                    if (tunnelsVisible) return column == last || column == 2;
                    return column == last;
                }
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    int last = getColumnCount() - 1;
                    int outCol = tunnelsVisible ? 3 : 2;
                    int inCol = tunnelsVisible ? 4 : 3;
                    if (columnIndex == outCol || columnIndex == inCol) {
                        return TradeRouteStatus.class;
                    }
                    if (tunnelsVisible) {
                        if (columnIndex == last || columnIndex == 2) return TradeRowData.class;
                    } else {
                        if (columnIndex == last) return TradeRowData.class;
                    }
                    return String.class;
                }
            };

            table = new JTable(model);
            table.setRowHeight(45);
            table.setFocusable(false);
            AssetStyles.applyScrollableDialogTable(table);
            AssetStyles.applyTableColumnAlignment(table, 0, SwingConstants.LEFT);
            AssetStyles.applyTableColumnAlignment(table, 1, SwingConstants.LEFT);

            TradeRouteStatusRenderer routeStatusRenderer = new TradeRouteStatusRenderer();
            int outCol = tunnelsVisible ? 3 : 2;
            int inCol = tunnelsVisible ? 4 : 3;
            AssetStyles.applyTableHeaderAlignment(table, outCol, SwingConstants.CENTER);
            AssetStyles.applyTableHeaderAlignment(table, inCol, SwingConstants.CENTER);
            table.getColumnModel().getColumn(outCol).setCellRenderer(routeStatusRenderer);
            table.getColumnModel().getColumn(inCol).setCellRenderer(routeStatusRenderer);
            
            if (tunnelsVisible) {
                AssetStyles.applyTableHeaderAlignment(table, 2, SwingConstants.CENTER);
                table.getColumnModel().getColumn(2).setCellRenderer(new TunnelCellRenderer());
                table.getColumnModel().getColumn(2).setCellEditor(new TunnelCellEditor());
                table.getColumnModel().getColumn(5).setCellRenderer(new TradeActionRenderer());
                table.getColumnModel().getColumn(5).setCellEditor(new TradeActionEditor());
                AssetStyles.applyTableHeaderAlignment(table, 5, SwingConstants.CENTER);
                configureTradeActionColumn(5);
            } else {
                table.getColumnModel().getColumn(4).setCellRenderer(new TradeActionRenderer());
                table.getColumnModel().getColumn(4).setCellEditor(new TradeActionEditor());
                AssetStyles.applyTableHeaderAlignment(table, 4, SwingConstants.CENTER);
                configureTradeActionColumn(4);
            }

            tableScrollPane = AssetStyles.wrapScrollableTable(table);
            add(tableScrollPane, BorderLayout.CENTER);
            
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            
            JLabel label = new JLabel(LanguageStrings.get(LanguageStrings.DYNASTY_MANAGE_LOGISTICS));
            label.setForeground(AssetStyles.FONT_COLOR);
            topPanel.add(label);
            
            activeColonyLabel = new JLabel(LanguageStrings.get(LanguageStrings.ASSIMILATION_NONE));
            activeColonyLabel.setForeground(AssetStyles.FONT_COLOR);
            activeColonyLabel.setFont(AssetStyles.FONT_BOLD);
            topPanel.add(activeColonyLabel);
            
            add(topPanel, BorderLayout.NORTH);
            revalidate();
            repaint();
        }

        @Override
        public void liveUpdate() {
            updateData();
        }

        @Override
        public void updateData() {
            if (tunnelsVisible != dynasty.hasUpgrade(GameUnlocks.ABILITY_TUNNELS)) {
                initUI();
            }

            World world = engine.getWorld();
            if (world == null) return;
            
            Hex mapActive = world.getActiveHex();
            activeColony = (mapActive != null && mapActive.getColony() != null && mapActive.getColony().isPlayer()) 
                ? mapActive.getColony() 
                : null;

            if (activeColony == null) {
                activeColonyLabel.setText(LanguageStrings.get(LanguageStrings.WORLD_NA));
                model.setRowCount(0);
                return;
            }
            
            activeColonyLabel.setText(activeColony.getName());

            Hex currentHex = world.getHexOfColony(activeColony);
            int selectedRow = table.getSelectedRow();
            model.setRowCount(0);

            Hex[] adjacent = {
                currentHex.getNorth(), currentHex.getNorthEast(), currentHex.getSouthEast(),
                currentHex.getSouth(), currentHex.getSouthWest(), currentHex.getNorthWest()
            };
            String[] dirNames = {
                LanguageStrings.get(LanguageStrings.STAT_NEIGHBOR_NORTH),
                LanguageStrings.get(LanguageStrings.STAT_NEIGHBOR_NORTH_EAST),
                LanguageStrings.get(LanguageStrings.STAT_NEIGHBOR_SOUTH_EAST),
                LanguageStrings.get(LanguageStrings.STAT_NEIGHBOR_SOUTH),
                LanguageStrings.get(LanguageStrings.STAT_NEIGHBOR_SOUTH_WEST),
                LanguageStrings.get(LanguageStrings.STAT_NEIGHBOR_NORTH_WEST)
            };

            for (int i = 0; i < adjacent.length; i++) {
                Hex neighborHex = adjacent[i];
                if (neighborHex == null) continue;
                
                Colony neighborColony = neighborHex.getColony();
                if (neighborColony == null) continue;
                if (neighborColony.getDynasty() != activeColony.getDynasty()) continue;

                String neighborName = neighborColony.getName();
                Tunnel tunnel = dynasty.getTunnelBetween(currentHex, neighborHex);
                
                Trade outgoing = dynasty.getTradeService().findTrade(activeColony, neighborColony);
                Trade incoming = dynasty.getTradeService().findTrade(neighborColony, activeColony);

                String bilateralOut = null;
                String bilateralIn = null;
                if (outgoing == null && incoming != null && (incoming.hasPendingUpdate() ? incoming.isPendingBilateral() : incoming.isBilateral())) {
                    bilateralOut = LanguageStrings.get(LanguageStrings.UI_BILATERAL);
                }
                if (incoming == null && outgoing != null && (outgoing.hasPendingUpdate() ? outgoing.isPendingBilateral() : outgoing.isBilateral())) {
                    bilateralIn = LanguageStrings.get(LanguageStrings.UI_BILATERAL);
                }

                TradeRouteStatus outStatus = tradeRouteStatus(outgoing, bilateralOut);
                TradeRouteStatus inStatus = tradeRouteStatus(incoming, bilateralIn);

                TradeRowData rowData = new TradeRowData(neighborColony, neighborHex, outgoing, incoming, tunnel);

                if (tunnelsVisible) {
                    model.addRow(new Object[]{dirNames[i], neighborName, rowData, outStatus, inStatus, rowData});
                } else {
                    model.addRow(new Object[]{dirNames[i], neighborName, outStatus, inStatus, rowData});
                }
            }
            
            if (selectedRow >= 0 && selectedRow < table.getRowCount()) {
                table.setRowSelectionInterval(selectedRow, selectedRow);
            }
            if (tunnelsVisible) {
                AssetStyles.relayoutTableInScrollPane(tableScrollPane,
                        new boolean[]{false, true, false, false, false, false});
            } else {
                AssetStyles.relayoutTableInScrollPane(tableScrollPane,
                        new boolean[]{false, true, false, false, false});
            }
        }

        private Trade findIncomingTrade(Colony origin, Colony neighbor) {
            return findTrade(neighbor, origin);
        }

        private static boolean isTradeBilateral(Trade trade) {
            if (trade == null) {
                return false;
            }
            return trade.hasPendingUpdate() ? trade.isPendingBilateral() : trade.isBilateral();
        }

        private boolean canMakeTwoWayTrade(TradeRowData data) {
            if (data == null || data.neighbor == null || activeColony == null) {
                return false;
            }
            if (!activeColony.hasUpgrade(GameUnlocks.ABILITY_BILATERAL_TRADE)) {
                return false;
            }
            Trade outgoing = data.outgoingTrade;
            if (outgoing == null || isTradeBilateral(outgoing)) {
                return false;
            }
            Trade incoming = findIncomingTrade(activeColony, data.neighbor);
            return incoming != null && !isTradeBilateral(incoming);
        }

        private boolean shouldShowTwoWayMenuItem(TradeRowData data) {
            return shouldShowTwoWayButton(data);
        }

        private boolean shouldShowTwoWayButton(TradeRowData data) {
            if (data == null || data.neighbor == null || activeColony == null) {
                return false;
            }
            if (!activeColony.hasUpgrade(GameUnlocks.ABILITY_BILATERAL_TRADE)) {
                return false;
            }
            Trade outgoing = data.outgoingTrade;
            return outgoing != null && !isTradeBilateral(outgoing);
        }

        private void configureTradeActionColumn(int actionCol) {
            TableColumn column = table.getColumnModel().getColumn(actionCol);
            column.setMinWidth(110);
            column.setPreferredWidth(110);
        }

        private boolean mergeTradesIntoBilateral(Trade outgoing, Trade incoming) {
            if (outgoing == null || incoming == null || engine.getTradeManager() == null) {
                return false;
            }
            Map<ResourceType, Double> load = outgoing.hasPendingUpdate() && outgoing.getPendingLoad() != null
                    ? new HashMap<>(outgoing.getPendingLoad())
                    : new HashMap<>(outgoing.getLoad());
            Map<ResourceType, Double> returnLoad = incoming.hasPendingUpdate() && incoming.getPendingLoad() != null
                    ? new HashMap<>(incoming.getPendingLoad())
                    : new HashMap<>(incoming.getLoad());
            Map<AntType, Integer> transport = outgoing.hasPendingUpdate() && outgoing.getPendingTransport() != null
                    ? new HashMap<>(outgoing.getPendingTransport())
                    : new HashMap<>(outgoing.getTransport());
            boolean recurrent = outgoing.hasPendingUpdate() ? outgoing.isPendingRecurrent() : outgoing.isRecurrent();
            TradeMethod method = outgoing.hasPendingUpdate() ? outgoing.getPendingMethod() : outgoing.getMethod();
            outgoing.setPendingUpdate(load, returnLoad, transport, recurrent, true, method);
            incoming.cancel();
            engine.getTradeManager().removeTrade(incoming);
            return true;
        }

        private void makeTwoWayTrade(TradeRowData data) {
            if (data == null || data.neighbor == null || data.outgoingTrade == null) {
                return;
            }
            Trade incoming = findIncomingTrade(activeColony, data.neighbor);
            if (incoming == null) {
                return;
            }
            int res = UiOptionPane.showConfirmDialog(this,
                    LanguageStrings.format(LanguageStrings.TRADE_OPTIMIZE_MSG, data.neighbor.getName()),
                    LanguageStrings.get(LanguageStrings.TRADE_MAKE_TWO_WAY),
                    JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION && mergeTradesIntoBilateral(data.outgoingTrade, incoming)) {
                updateData();
            }
        }

        private Trade findTrade(Colony origin, Colony destination) {
            if (dynasty.getTradeService() == null) {
                return null;
            }
            return dynasty.getTradeService().findTrade(origin, destination);
        }

        private String formatTradeStatus(Trade trade) {
            if (trade == null) return LanguageStrings.get(LanguageStrings.ASSIMILATION_NONE);
            String transit = trade.isReturning() ? LanguageStrings.get(LanguageStrings.UI_RETURNING) : LanguageStrings.get(LanguageStrings.UI_TRANSIT);
            String pending = trade.hasPendingUpdate() ? LanguageStrings.get(LanguageStrings.UI_MODIFIED) : "";
            return LanguageStrings.format(LanguageStrings.DYNASTY_TRANSIT_FORMAT, transit, trade.getRemainingHours(), pending);
        }

        private TradeRouteStatus tradeRouteStatus(Trade trade, String textOverride) {
            if (textOverride != null) {
                return new TradeRouteStatus(textOverride, null);
            }
            if (trade == null) {
                return new TradeRouteStatus(LanguageStrings.get(LanguageStrings.ASSIMILATION_NONE), null);
            }
            TradeMethod method = trade.hasPendingUpdate() ? trade.getPendingMethod() : trade.getMethod();
            ImageIcon icon = method != null ? method.getIcon() : null;
            return new TradeRouteStatus(formatTradeStatus(trade), icon);
        }

        private static final class TradeRouteStatus {
            private final String text;
            private final ImageIcon icon;

            TradeRouteStatus(String text, ImageIcon icon) {
                this.text = text;
                this.icon = icon;
            }
        }

        private static class TradeRouteStatusRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (value instanceof TradeRouteStatus status) {
                    setText(status.text);
                    setIcon(status.icon);
                    setIconTextGap(8);
                } else {
                    setIcon(null);
                }
                return this;
            }
        }

        private boolean shouldShowTunnelProgress(Tunnel tunnel) {
            return tunnel != null && !tunnel.isComplete() && tunnel.getProgress() > 0;
        }

        private boolean canStartTunnel(TradeRowData data) {
            if (data == null || data.neighbor == null) {
                return false;
            }
            if (data.neighbor.getDynasty() != activeColony.getDynasty()) {
                return false;
            }
            if (activeColony.getCurrentTunnelProject() != null) {
                return false;
            }
            if (data.tunnel != null && data.tunnel.isComplete()) {
                return false;
            }
            World world = engine.getWorld();
            if (world != null) {
                Hex originHex = world.getHexOfColony(activeColony);
                if (originHex != null && dynasty.hasIncompleteTunnelAt(originHex)
                        && (data.tunnel == null || data.tunnel.isComplete())) {
                    return false;
                }
            }
            return true;
        }

        private class TunnelCellRenderer extends JPanel implements TableCellRenderer {
            private final JProgressBar progressBar = new JProgressBar(0, 100);
            private final JLabel label = new JLabel();
            private final JButton buildBtn = new JButton(LanguageStrings.get(LanguageStrings.DYNASTY_BUILD_TUNNEL));
            public TunnelCellRenderer() {
                setLayout(new BorderLayout(5, 5));
                setOpaque(true);
                progressBar.setStringPainted(true);
                label.setHorizontalAlignment(JLabel.CENTER);
                buildBtn.setFocusable(false);
                AssetStyles.styleCompactButton(buildBtn);
                AssetStyles.styleProgressBar(progressBar);
            }
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setEnabled(true);
                setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                removeAll();
                if (value instanceof TradeRowData) {
                    TradeRowData data = (TradeRowData) value;
                    Tunnel tunnel = data.tunnel;
                    if (tunnel != null && tunnel.isComplete()) {
                        label.setText(LanguageStrings.get(LanguageStrings.DYNASTY_BUILT));
                        label.setForeground(AssetStyles.FONT_COLOR_SUCCESS);
                        add(label, BorderLayout.CENTER);
                    } else if (shouldShowTunnelProgress(tunnel)) {
                        double pct = (tunnel.getProgress() / tunnel.getTotalCost()) * 100;
                        progressBar.setValue((int) pct);
                        progressBar.setString(LanguageStrings.format(LanguageStrings.DYNASTY_PROGRESS_PERCENT, pct));

                        int engineers = activeColony.getAssignedRoleCount(GameConstants.ROLE_ENGINEER);
                        int borers = activeColony.getAssignedRoleCount(GameConstants.ROLE_BORER);
                        if (engineers <= 0 && borers <= 0) {
                            progressBar.setForeground(AssetStyles.FONT_COLOR_ERROR);
                            progressBar.setToolTipText(LanguageStrings.get(LanguageStrings.DYNASTY_ERROR_NO_ENGINEERS));
                        } else {
                            progressBar.setForeground(UIManager.getColor("ProgressBar.foreground"));
                            progressBar.setToolTipText(null);
                        }

                        add(progressBar, BorderLayout.CENTER);
                    } else {
                        boolean sameDynasty = data.neighbor != null && data.neighbor.getDynasty() == activeColony.getDynasty();
                        if (sameDynasty) {
                            boolean alreadyBuilding = activeColony.getCurrentTunnelProject() != null;
                            buildBtn.setEnabled(!alreadyBuilding);
                            AssetStyles.styleCompactButton(buildBtn);
                            buildBtn.setToolTipText(alreadyBuilding ? LanguageStrings.get(LanguageStrings.DYNASTY_TUNNEL_SPONSORING) : buildBtn.getText());
                            add(buildBtn, BorderLayout.CENTER);
                        } else {
                            label.setText(LanguageStrings.get(LanguageStrings.WORLD_NA));
                            label.setForeground(AssetStyles.COLOR_LIGHT_GRAY);
                            add(label, BorderLayout.CENTER);
                        }
                    }
                }
                return this;
            }
        }

        private class TunnelCellEditor extends AbstractCellEditor implements TableCellEditor {
            private final JPanel panel = new JPanel(new BorderLayout());
            private final JButton buildBtn = new JButton(LanguageStrings.get(LanguageStrings.DYNASTY_BUILD_TUNNEL));
            private TradeRowData currentData;
            public TunnelCellEditor() {
                buildBtn.setFocusable(false);
                AssetStyles.styleCompactButton(buildBtn);
                panel.add(buildBtn, BorderLayout.CENTER);
                buildBtn.addActionListener(e -> {
                    if (currentData != null && canStartTunnel(currentData)) {
                        int engineers = activeColony.getAssignedRoleCount(GameConstants.ROLE_ENGINEER);
                        int borers = activeColony.getAssignedRoleCount(GameConstants.ROLE_BORER);

                        if (engineers <= 0 && borers <= 0) {
                            UiOptionPane.showMessageDialog(panel,
                                LanguageStrings.get(LanguageStrings.DYNASTY_ERROR_ASSIGN_BORERS),
                                LanguageStrings.get(LanguageStrings.DYNASTY_ERROR_LABOR_REQUIRED), JOptionPane.WARNING_MESSAGE);
                        } else {
                            startTunnel(currentData.neighborHex);
                        }
                    }
                    fireEditingStopped();
                });
            }
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                currentData = (TradeRowData) value;
                panel.setBackground(table.getSelectionBackground());
                AssetStyles.styleCompactButton(buildBtn);
                return panel;
            }
            @Override
            public Object getCellEditorValue() { return currentData; }
        }

        private class TradeRowData {
            final Colony neighbor;
            final Hex neighborHex;
            final Trade outgoingTrade;
            final Trade incomingTrade;
            final Tunnel tunnel;

            TradeRowData(Colony neighbor, Hex neighborHex, Trade outgoingTrade, Trade incomingTrade, Tunnel tunnel) {
                this.neighbor = neighbor;
                this.neighborHex = neighborHex;
                this.outgoingTrade = outgoingTrade;
                this.incomingTrade = incomingTrade;
                this.tunnel = tunnel;
            }
        }

        private class TradeActionRenderer extends JPanel implements TableCellRenderer {
            private final JButton actionBtn = new JButton();

            TradeActionRenderer() {
                setLayout(new FlowLayout(FlowLayout.CENTER, 4, 2));
                actionBtn.setFocusable(false);
                AssetStyles.styleCompactButton(actionBtn);
                add(actionBtn);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                if (value instanceof TradeRowData data) {
                    if (data.neighbor == null) {
                        actionBtn.setText(LanguageStrings.get(LanguageStrings.UI_ESTABLISH));
                        actionBtn.setEnabled(false);
                    } else {
                        actionBtn.setText(LanguageStrings.get(LanguageStrings.UI_MANAGE));
                        actionBtn.setEnabled(true);
                    }
                }
                AssetStyles.styleCompactButton(actionBtn);
                setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                return this;
            }
        }

        private class TradeActionEditor extends AbstractCellEditor implements TableCellEditor {
            private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
            private final JButton actionBtn = new JButton();
            private TradeRowData currentData;
            private JTable editingTable;
            private int editingRow = -1;
            private int editingCol = -1;

            TradeActionEditor() {
                actionBtn.setFocusable(false);
                AssetStyles.styleCompactButton(actionBtn);
                panel.add(actionBtn);
                actionBtn.addActionListener(e -> {
                    JTable tableRef = editingTable;
                    int row = editingRow;
                    int col = editingCol;
                    TradeRowData data = currentData;
                    fireEditingStopped();
                    if (data == null || data.neighbor == null) {
                        return;
                    }
                    showTradeRowActionsMenu(data, tableRef, row, col);
                });
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                    int column) {
                currentData = (TradeRowData) value;
                editingTable = table;
                editingRow = row;
                editingCol = column;
                panel.setBackground(table.getSelectionBackground());
                if (currentData != null) {
                    if (currentData.neighbor == null) {
                        actionBtn.setText(LanguageStrings.get(LanguageStrings.UI_ESTABLISH));
                        actionBtn.setEnabled(false);
                    } else {
                        actionBtn.setText(LanguageStrings.get(LanguageStrings.UI_MANAGE));
                        actionBtn.setEnabled(true);
                    }
                }
                AssetStyles.styleCompactButton(actionBtn);
                return panel;
            }

            @Override
            public Object getCellEditorValue() {
                return currentData;
            }
        }

        private void establishTrade(Colony target) {
            openTradeDialog(activeColony, target, null);
            updateData();
        }

        private void showActionsMenuForNeighbor(Colony neighbor, Component invoker) {
            World world = engine.getWorld();
            activeColony = DynastyManagementDialog.this.getActivePlayerColony();
            if (world == null || activeColony == null || neighbor == null) {
                return;
            }
            Hex currentHex = world.getHexOfColony(activeColony);
            Hex neighborHex = world.getHexOfColony(neighbor);
            Trade outgoing = findTrade(activeColony, neighbor);
            Trade incoming = findIncomingTrade(activeColony, neighbor);
            Tunnel tunnel = currentHex != null && neighborHex != null
                    ? dynasty.getTunnelBetween(currentHex, neighborHex)
                    : null;
            TradeRowData data = new TradeRowData(neighbor, neighborHex, outgoing, incoming, tunnel);
            showTradeRowActionsMenu(data, invoker);
        }

        private void startTunnel(Hex targetHex) {
            Hex originHex = engine.getWorld().getHexOfColony(activeColony);
            if (originHex == null || targetHex == null) {
                return;
            }
            Tunnel existing = dynasty.getTunnelBetween(originHex, targetHex);
            if (existing != null && !existing.isComplete()) {
                activeColony.setCurrentTunnelProject(existing);
            } else if (existing == null) {
                if (activeColony.getCurrentTunnelProject() != null || dynasty.hasIncompleteTunnelAt(originHex)) {
                    return;
                }
                Tunnel tunnel = new Tunnel(originHex, targetHex, GameNumbers.TUNNEL_WORK_REQUIRED);
                dynasty.addTunnel(tunnel);
                activeColony.setCurrentTunnelProject(tunnel);
            }
            updateData();
        }
    }

    private static class TradeCreationDialog extends JDialog {
        private final Colony origin;
        private final Colony target;
        private final Engine engine;
        private final Trade existingTrade;
        private final boolean crossDynasty;
        private final Map<ResourceType, JSpinner> resourceSpinners = new HashMap<>();
        private final Map<ResourceType, JSpinner> returnResourceSpinners = new HashMap<>();
        private final Map<AntType, JSpinner> antSpinners = new HashMap<>();
        private final JComboBox<TradeMethod> methodCombo;
        private final JCheckBox recurrentCheck;
        private final JCheckBox bilateralCheck;
        private final JButton createBtn;
        private final JButton optimizeBtn;
        private final List<JButton> compactButtons = new ArrayList<>();
        
        private final JLabel capLabel = new JLabel(LanguageStrings.format(LanguageStrings.TRADE_CAPACITY_FORMAT, 0.0, 0.0));
        private final JLabel speedLabel = new JLabel(LanguageStrings.format(LanguageStrings.TRADE_SPEED_FORMAT, 0.0));
        private final JLabel timeLabel = new JLabel(LanguageStrings.format(LanguageStrings.TRADE_TIME_FORMAT, 0));
        private final JLabel dangerLabel = new JLabel(LanguageStrings.format(LanguageStrings.TRADE_SECURITY_FORMAT, 0.0));

        private double totalTransportCapacity = 0.0;

        public TradeCreationDialog(Window owner, Colony origin, Colony target, Engine engine, Trade existingTrade) {
            super(owner, (existingTrade == null ? LanguageStrings.get(LanguageStrings.UI_ESTABLISH) : LanguageStrings.get(LanguageStrings.DYNASTY_MODIFY)) + " " + LanguageStrings.get(LanguageStrings.PANEL_TRADE), ModalityType.APPLICATION_MODAL); // generic trade key used
            this.origin = origin;
            this.target = target;
            this.engine = engine;
            this.existingTrade = existingTrade;
            this.crossDynasty = DynastyDiplomacyService.isCrossDynastyTrade(origin, target);
            setLayout(new BorderLayout());
            setSize(1000, 800);
            setLocationRelativeTo(owner);

            JPanel headerPanel = new JPanel(new GridLayout(1, 4, 20, 20));
            headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            headerPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
            
            capLabel.setFont(AssetStyles.FONT_BOLD.deriveFont(14f));
            speedLabel.setFont(AssetStyles.FONT_BOLD.deriveFont(14f));
            timeLabel.setFont(AssetStyles.FONT_BOLD.deriveFont(14f));
            dangerLabel.setFont(AssetStyles.FONT_BOLD.deriveFont(14f));
            capLabel.setForeground(AssetStyles.FONT_COLOR);
            speedLabel.setForeground(AssetStyles.FONT_COLOR);
            timeLabel.setForeground(AssetStyles.FONT_COLOR);
            dangerLabel.setForeground(AssetStyles.FONT_COLOR);
            
            headerPanel.add(capLabel);
            headerPanel.add(speedLabel);
            headerPanel.add(timeLabel);
            headerPanel.add(dangerLabel);
            add(headerPanel, BorderLayout.NORTH);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            mainPanel.setBackground(AssetStyles.BACKGROUND_COLOR);

            JLabel routeLabel = new JLabel(LanguageStrings.format(LanguageStrings.TRADE_ROUTE_PREFIX, origin.getName(), target.getName()));
            routeLabel.setFont(AssetStyles.FONT_BOLD);
            routeLabel.setForeground(AssetStyles.FONT_COLOR);
            mainPanel.add(routeLabel);
            mainPanel.add(Box.createVerticalStrut(15));

            JPanel resGrid = new JPanel(new GridLayout(0, 2, 15, 8));
            resGrid.setBorder(BorderFactory.createTitledBorder(LanguageStrings.get(LanguageStrings.TRADE_CARGO_LOAD)));
            resGrid.setBackground(AssetStyles.BACKGROUND_COLOR);
            for (ResourceType rt : GameConstants.getResources()) {
                if (rt.isIsLiquid()) continue;

                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
                p.setOpaque(false);
                JLabel label = new JLabel(rt.getName(), rt.getIcon(), JLabel.LEFT);
                label.setPreferredSize(new Dimension(160, 25));
                label.setForeground(AssetStyles.FONT_COLOR);
                p.add(label);
                
                double available = getColonyResource(rt);
                double initialVal = 0.0;
                if (existingTrade != null) {
                    if (existingTrade.hasPendingUpdate() && existingTrade.getPendingLoad() != null) {
                        initialVal = existingTrade.getPendingLoad().getOrDefault(rt, 0.0);
                    } else {
                        initialVal = existingTrade.getLoad().getOrDefault(rt, 0.0);
                    }
                }
                JSpinner s = new JSpinner(new SpinnerNumberModel(initialVal, 0.0, available, 10.0));
                s.setFocusable(false);
                AssetStyles.styleSpinner(s);
                s.addChangeListener(e -> updateStats());
                resourceSpinners.put(rt, s);
                p.add(s);

                JButton maxBtn = new JButton(LanguageStrings.get(LanguageStrings.UI_MAX));
                maxBtn.setFocusable(false);
                compactButtons.add(maxBtn);
                maxBtn.addActionListener(e -> setMaxResource(rt));
                p.add(maxBtn);

                resGrid.add(p);
            }
            mainPanel.add(resGrid);
            mainPanel.add(Box.createVerticalStrut(15));

            JPanel bilateralPanel = new JPanel(new GridLayout(0, 2, 15, 8));
            bilateralPanel.setBorder(BorderFactory.createTitledBorder(LanguageStrings.get(LanguageStrings.TRADE_CARGO_RETURN)));
            bilateralPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
            for (ResourceType rt : GameConstants.getResources()) {
                if (rt.isIsLiquid()) continue;
                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
                p.setOpaque(false);
                JLabel label = new JLabel(rt.getName(), rt.getIcon(), JLabel.LEFT);
                label.setPreferredSize(new Dimension(160, 25));
                label.setForeground(AssetStyles.FONT_COLOR);
                p.add(label);
                
                double initialVal = 0.0;
                if (existingTrade != null) {
                    if (existingTrade.hasPendingUpdate() && existingTrade.getPendingReturnLoad() != null) {
                        initialVal = existingTrade.getPendingReturnLoad().getOrDefault(rt, 0.0);
                    } else {
                        initialVal = existingTrade.getReturnLoad().getOrDefault(rt, 0.0);
                    }
                }
                JSpinner s = new JSpinner(new SpinnerNumberModel(initialVal, 0.0, 1000000.0, 10.0));
                s.setFocusable(false);
                AssetStyles.styleSpinner(s);
                s.addChangeListener(e -> updateStats());
                returnResourceSpinners.put(rt, s);
                p.add(s);

                JButton maxBtn = new JButton(LanguageStrings.get(LanguageStrings.UI_MAX));
                maxBtn.setFocusable(false);
                compactButtons.add(maxBtn);
                maxBtn.addActionListener(e -> setMaxResource(rt, true));
                p.add(maxBtn);

                bilateralPanel.add(p);
            }
            boolean isBilateral = (existingTrade != null) ? (existingTrade.hasPendingUpdate() ? existingTrade.isPendingBilateral() : existingTrade.isBilateral()) : false;
            bilateralPanel.setVisible(isBilateral);
            mainPanel.add(bilateralPanel);
            mainPanel.add(Box.createVerticalStrut(15));

            JPanel antGrid = new JPanel(new GridLayout(0, 2, 15, 8));
            antGrid.setBorder(BorderFactory.createTitledBorder(LanguageStrings.get(LanguageStrings.TRADE_PERSONNEL)));
            antGrid.setBackground(AssetStyles.BACKGROUND_COLOR);
            
            Map<AntType, Integer> availableRoles = new HashMap<>();
            availableRoles.put(GameConstants.TYPE_WORKER, origin.getAssignedRoleCount(GameConstants.ROLE_COURIER));
            availableRoles.put(GameConstants.TYPE_MAJOR, origin.getAssignedRoleCount(GameConstants.ROLE_TRANSPORT));
            availableRoles.put(GameConstants.TYPE_SOLDIER, origin.getAssignedRoleCount(GameConstants.ROLE_ESCORT));
            availableRoles.put(GameConstants.TYPE_PRINCESS, origin.getAssignedRoleCount(GameConstants.ROLE_SKYTRANS));

            if (engine.getTradeManager() != null) {
                for (Trade t : engine.getTradeManager().getActiveTrades()) {
                    if (t == existingTrade || !t.isActive()) continue;
                    if (t.getOrigin().getColony() == origin) {
                        Map<AntType, Integer> trans = t.hasPendingUpdate() ? t.getPendingTransport() : t.getTransport();
                        for (Map.Entry<AntType, Integer> entry : trans.entrySet()) {
                            int current = availableRoles.getOrDefault(entry.getKey(), 0);
                            availableRoles.put(entry.getKey(), Math.max(0, current - entry.getValue()));
                        }
                    }
                }
            }

            List<AntType> tradeAnts = List.of(GameConstants.TYPE_WORKER, GameConstants.TYPE_SOLDIER, GameConstants.TYPE_MAJOR, GameConstants.TYPE_PRINCESS);
            for (AntType at : tradeAnts) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
                p.setOpaque(false);
                String labelName = at.getName();
                if (at == GameConstants.TYPE_WORKER) labelName = LanguageStrings.get(LanguageStrings.TRADE_COURIERS);
                else if (at == GameConstants.TYPE_MAJOR) labelName = LanguageStrings.get(LanguageStrings.TRADE_TRANSPORTS);
                else if (at == GameConstants.TYPE_SOLDIER) labelName = LanguageStrings.get(LanguageStrings.TRADE_ESCORTS);
                else if (at == GameConstants.TYPE_PRINCESS) labelName = LanguageStrings.get(LanguageStrings.TRADE_FLYERS);
                
                int available = availableRoles.getOrDefault(at, 0);
                JLabel label = new JLabel(labelName + " (" + available + ")", at.getIcon(), JLabel.LEFT);
                label.setPreferredSize(new Dimension(200, 25));
                label.setForeground(AssetStyles.FONT_COLOR);
                p.add(label);
                
                int initialVal = 0;
                if (existingTrade != null) {
                    if (existingTrade.hasPendingUpdate() && existingTrade.getPendingTransport() != null) {
                        initialVal = existingTrade.getPendingTransport().getOrDefault(at, 0);
                    } else {
                        initialVal = existingTrade.getTransport().getOrDefault(at, 0);
                    }
                }
                JSpinner s = new JSpinner(new SpinnerNumberModel(initialVal, 0, available, 1));
                s.setFocusable(false);
                AssetStyles.styleSpinner(s);
                s.addChangeListener(e -> {
                    updateAvailableMethods();
                    updateStats();
                });
                antSpinners.put(at, s);
                p.add(s);
                antGrid.add(p);
            }
            mainPanel.add(antGrid);
            mainPanel.add(Box.createVerticalStrut(15));

            JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
            configPanel.setBorder(BorderFactory.createTitledBorder(LanguageStrings.get(LanguageStrings.TAB_LOGISTICS)));
            configPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
            
            methodCombo = new JComboBox<>();
            methodCombo.setFocusable(false);
            AssetStyles.styleComboBox(methodCombo);
            updateAvailableMethods();

            if (existingTrade != null) {
                TradeMethod m = existingTrade.hasPendingUpdate() ? existingTrade.getPendingMethod() : existingTrade.getMethod();
                methodCombo.setSelectedItem(m);
            }
            methodCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof TradeMethod method) {
                        setText(method.getName());
                        setIcon(method.getIcon());
                        setIconTextGap(8);
                    }
                    return this;
                }
            });
            methodCombo.addActionListener(e -> updateStats());
            JLabel mLabel = new JLabel(LanguageStrings.get(LanguageStrings.TRADE_METHOD));
            mLabel.setForeground(AssetStyles.FONT_COLOR);
            configPanel.add(mLabel);
            configPanel.add(methodCombo);
            
            boolean initialRecurrent = (existingTrade != null) ? (existingTrade.hasPendingUpdate() ? existingTrade.isPendingRecurrent() : existingTrade.isRecurrent()) : true;
            recurrentCheck = new JCheckBox(LanguageStrings.get(LanguageStrings.TRADE_RECURRENT), initialRecurrent);
            AssetStyles.styleCheckBox(recurrentCheck);
            recurrentCheck.setFocusable(false);
            recurrentCheck.setOpaque(false);
            configPanel.add(Box.createHorizontalStrut(20));
            configPanel.add(recurrentCheck);
            
            boolean initialBilateral = crossDynasty ? false
                    : ((existingTrade != null) ? (existingTrade.hasPendingUpdate() ? existingTrade.isPendingBilateral() : existingTrade.isBilateral()) : false);
            bilateralCheck = new JCheckBox(LanguageStrings.get(LanguageStrings.TRADE_BILATERAL), initialBilateral);
            AssetStyles.styleCheckBox(bilateralCheck);
            bilateralCheck.setFocusable(false);
            bilateralCheck.setOpaque(false);
            bilateralCheck.setVisible(!crossDynasty && origin.hasUpgrade(GameUnlocks.ABILITY_BILATERAL_TRADE));
            bilateralCheck.addActionListener(e -> {
                bilateralPanel.setVisible(bilateralCheck.isSelected());
                revalidate();
                repaint();
                updateStats();
            });
            configPanel.add(Box.createHorizontalStrut(20));
            configPanel.add(bilateralCheck);
            mainPanel.add(configPanel);

            if (crossDynasty) {
                bilateralPanel.setVisible(false);
            }

            add(new JScrollPane(mainPanel), BorderLayout.CENTER);

            JPanel footerPanel = new JPanel(new GridLayout(1, 0, 10, 0));
            footerPanel.setPreferredSize(new Dimension(0, 60));
            footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            footerPanel.setBackground(AssetStyles.BACKGROUND_SECONDARY);

            createBtn = new JButton(existingTrade == null ? LanguageStrings.get(LanguageStrings.TRADE_CONFIRM) : LanguageStrings.get(LanguageStrings.TRADE_UPDATE));
            createBtn.setFocusable(false);
            AssetStyles.styleCompactButton(createBtn);
            createBtn.addActionListener(e -> attemptCreate());
            
            optimizeBtn = new JButton(LanguageStrings.get(LanguageStrings.TRADE_OPTIMIZE));
            optimizeBtn.setFocusable(false);
            AssetStyles.styleCompactButton(optimizeBtn);
            boolean showOptimize = origin.hasUpgrade(GameUnlocks.ABILITY_BILATERAL_TRADE) && !crossDynasty;
            optimizeBtn.setVisible(showOptimize);
            optimizeBtn.setEnabled(false);
            optimizeBtn.addActionListener(e -> performOptimization());
            
            JButton cancelBtn = new JButton(LanguageStrings.get(LanguageStrings.UI_CANCEL));
            cancelBtn.setFocusable(false);
            AssetStyles.styleCompactButton(cancelBtn);
            cancelBtn.addActionListener(e -> dispose());
            
            footerPanel.add(createBtn);
            footerPanel.add(optimizeBtn);
            footerPanel.add(cancelBtn);
            add(footerPanel, BorderLayout.SOUTH);
            
            UiDialogUtils.prepareDialog(this, owner);
            updateStats();
        }

        void refreshTheme() {
            getContentPane().setBackground(AssetStyles.UI_BG_PRIMARY);
            UiDialogUtils.prepareDialog(this, getOwner());
        }

        private double getColonyResource(ResourceType rt) {
            if (rt == GameConstants.RESOURCE_PLANT) return origin.getPlantsPrecise();
            if (rt == GameConstants.RESOURCE_FUNGI) return origin.getMushroomsPrecise();
            if (rt == GameConstants.RESOURCE_MEAT) return origin.getProteinPrecise();
            if (rt == GameConstants.RESOURCE_ROCK) return origin.getMineralsPrecise();
            if (rt == GameConstants.RESOURCE_SYRUP) return origin.getSyrupsPrecise();
            if (rt == GameConstants.RESOURCE_RESIN) return origin.getResinsPrecise();
            return 0.0;
        }

        private double getTargetColonyResource(ResourceType rt) {
            if (rt == GameConstants.RESOURCE_PLANT) return target.getPlantsPrecise();
            if (rt == GameConstants.RESOURCE_FUNGI) return target.getMushroomsPrecise();
            if (rt == GameConstants.RESOURCE_MEAT) return target.getProteinPrecise();
            if (rt == GameConstants.RESOURCE_ROCK) return target.getMineralsPrecise();
            if (rt == GameConstants.RESOURCE_SYRUP) return target.getSyrupsPrecise();
            if (rt == GameConstants.RESOURCE_RESIN) return target.getResinsPrecise();
            return 0.0;
        }

        private void setMaxResource(ResourceType rt) {
            setMaxResource(rt, false);
        }

        private void setMaxResource(ResourceType rt, boolean isReturn) {
            Map<ResourceType, JSpinner> spinners = isReturn ? returnResourceSpinners : resourceSpinners;
            double currentLoad = 0.0;
            for (Map.Entry<ResourceType, JSpinner> entry : spinners.entrySet()) {
                if (entry.getKey() != rt) {
                    currentLoad += (Double) entry.getValue().getValue();
                }
            }
            double remainingCap = Math.max(0.0, totalTransportCapacity - currentLoad);
            double available = isReturn ? getTargetColonyResource(rt) : getColonyResource(rt);
            double maxToSet = Math.min(available, remainingCap);
            spinners.get(rt).setValue(maxToSet);
        }

        private void updateAvailableMethods() {
            TradeMethod current = (TradeMethod) methodCombo.getSelectedItem();
            methodCombo.removeAllItems();
            
            boolean princessesOnly = true;
            boolean anyPrincesses = false;
            for (Map.Entry<AntType, JSpinner> entry : antSpinners.entrySet()) {
                int count = (Integer) entry.getValue().getValue();
                if (count > 0) {
                    if (entry.getKey() != GameConstants.TYPE_PRINCESS) { princessesOnly = false; }
                    else { anyPrincesses = true; }
                }
            }
            if (!anyPrincesses) princessesOnly = false;

            World world = engine.getWorld();
            Hex hA = world.getHexOfColony(origin);
            Hex hB = world.getHexOfColony(target);
            Tunnel tunnel = origin.getDynasty().getTunnelBetween(hA, hB);

            for (TradeMethod m : GameConstants.getTradeMethods()) {
                if (m == GameConstants.METHOD_SEA) continue;
                if (crossDynasty && m == GameConstants.METHOD_TUNNEL) continue;
                if (m == GameConstants.METHOD_TUNNEL) {
                    if (tunnel == null || !tunnel.isComplete()) continue;
                }
                if (m == GameConstants.METHOD_AIR) {
                    if (!princessesOnly) continue; 
                }
                methodCombo.addItem(m);
            }
            
            if (current != null) {
                for (int i = 0; i < methodCombo.getItemCount(); i++) {
                    if (methodCombo.getItemAt(i) == current) {
                        methodCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }

        private void updateStats() {
            TradeMethod method = (TradeMethod) methodCombo.getSelectedItem();
            if (method == null) return;

            double totalLoad = resourceSpinners.values().stream().mapToDouble(s -> (Double) s.getValue()).sum();
            double totalReturnLoad = returnResourceSpinners.values().stream().mapToDouble(s -> (Double) s.getValue()).sum();
            
            double baseCap = origin.getStatsService().getBaseTradeCapacity(origin);
            double baseSec = origin.getStatsService().getBaseTradeSecurity(origin);
            
            double totalCap = 0;
            double totalSec = 0;
            float totalAntSpeed = 0;
            int totalAnts = 0;
            
            for (Map.Entry<AntType, JSpinner> entry : antSpinners.entrySet()) {
                int count = (Integer) entry.getValue().getValue();
                if (count <= 0) continue;

                AntType type = entry.getKey();
                totalAnts += count;
                totalAntSpeed += type.getSpeedMult() * count;

                if (type == GameConstants.TYPE_WORKER) { 
                    totalCap += count * baseCap * 1.0;
                    totalSec += count * baseSec * 1.0;
                } else if (type == GameConstants.TYPE_MAJOR) { 
                    totalCap += count * baseCap * 5.0;
                    totalSec += count * baseSec * 2.5;
                } else if (type == GameConstants.TYPE_SOLDIER) { 
                    totalSec += count * baseSec * 10.0;
                } else if (type == GameConstants.TYPE_PRINCESS) {
                    totalCap += count * baseCap * 1.0;
                    totalSec += count * baseSec * 1.0;
                }
            }
            
            totalCap *= method.getCapacityMult();
            this.totalTransportCapacity = totalCap;
            
            float methodSpeed = method.getSpeedMult();
            float workerSpeed = GameConstants.TYPE_WORKER.getSpeedMult();
            float avgAntSpeed = (totalAnts > 0) ? (totalAntSpeed / totalAnts) : workerSpeed;
            float speedFactor = methodSpeed * (avgAntSpeed / workerSpeed);
            
            World world = engine.getWorld();
            Tunnel tunnel = origin.getDynasty().getTunnelBetween(world.getHexOfColony(origin), world.getHexOfColony(target));
            if (tunnel != null && tunnel.isComplete()) speedFactor *= 1.5f;

            int hours = Math.max(1, Math.round(168f / speedFactor));
            
            double dangerFactor = method.getDangerFactor();
            double mitigationPercent = 100.0;
            if (dangerFactor > 0) {
                mitigationPercent = Math.min(100.0, (totalSec / (10.0 + dangerFactor * 50.0)) * 100.0);
            }

            boolean overCap = totalLoad > totalCap || (bilateralCheck.isSelected() && totalReturnLoad > totalCap);
            boolean noLoad = totalLoad <= 0 && (!bilateralCheck.isSelected() || totalReturnLoad <= 0);
            boolean noAnts = totalAnts <= 0;

            capLabel.setText(LanguageStrings.format(LanguageStrings.TRADE_CAPACITY_FORMAT, Math.max(totalLoad, bilateralCheck.isSelected() ? totalReturnLoad : 0), totalCap));
            capLabel.setForeground(overCap ? AssetStyles.FONT_COLOR_ERROR : AssetStyles.FONT_COLOR);
            
            speedLabel.setText(LanguageStrings.format(LanguageStrings.TRADE_SPEED_FORMAT, speedFactor));
            timeLabel.setText(LanguageStrings.format(LanguageStrings.TRADE_TIME_FORMAT, hours));
            dangerLabel.setText(LanguageStrings.format(LanguageStrings.TRADE_SECURITY_FORMAT, mitigationPercent));
            dangerLabel.setForeground(mitigationPercent < 100 ? AssetStyles.FONT_COLOR_WARNING : AssetStyles.FONT_COLOR_SUCCESS);

            createBtn.setEnabled(!overCap && !noAnts && !noLoad);
            if (overCap) createBtn.setToolTipText(LanguageStrings.get(LanguageStrings.TRADE_ERROR_OVER_CAP));
            else if (noAnts) createBtn.setToolTipText(LanguageStrings.get(LanguageStrings.TRADE_ERROR_NO_ANTS));
            else if (noLoad) createBtn.setToolTipText(LanguageStrings.get(LanguageStrings.TRADE_ERROR_NO_LOAD));
            else createBtn.setToolTipText(null);

            if (origin.hasUpgrade(GameUnlocks.ABILITY_BILATERAL_TRADE) && !crossDynasty) {
                Trade incoming = findIncomingTrade();
                boolean canOptimize = !bilateralCheck.isSelected() && incoming != null;
                optimizeBtn.setVisible(true);
                optimizeBtn.setEnabled(canOptimize);
                optimizeBtn.setToolTipText(canOptimize ? null
                        : LanguageStrings.get(LanguageStrings.TRADE_OPTIMIZE_REQUIRES_INCOMING));
            } else {
                optimizeBtn.setVisible(false);
                optimizeBtn.setEnabled(false);
                optimizeBtn.setToolTipText(null);
            }
            AssetStyles.styleCompactButton(optimizeBtn);
            AssetStyles.styleCompactButton(createBtn);
        }

        private Trade findIncomingTrade() {
            if (engine.getTradeManager() == null) return null;
            for (Trade t : engine.getTradeManager().getActiveTrades()) {
                if (t.getOrigin().getColony() == target && t.getDestination().getColony() == origin) {
                    return t;
                }
            }
            return null;
        }

        private void performOptimization() {
            Trade incoming = findIncomingTrade();
            if (incoming == null) return;

            int res = UiOptionPane.showConfirmDialog(this, 
                LanguageStrings.format(LanguageStrings.TRADE_OPTIMIZE_MSG, target.getName()), 
                LanguageStrings.get(LanguageStrings.TRADE_OPTIMIZE), JOptionPane.YES_NO_OPTION);
            
            if (res == JOptionPane.YES_OPTION) {
                bilateralCheck.setSelected(true);
                bilateralCheck.getActionListeners()[0].actionPerformed(new ActionEvent(bilateralCheck, ActionEvent.ACTION_PERFORMED, null));

                for (Map.Entry<ResourceType, Double> entry : incoming.getLoad().entrySet()) {
                    JSpinner s = returnResourceSpinners.get(entry.getKey());
                    if (s != null) s.setValue(entry.getValue());
                }

                incoming.cancel();
                engine.getTradeManager().removeTrade(incoming);
                updateStats();
            }
        }

        private void attemptCreate() {
            Map<ResourceType, Double> load = new HashMap<>();
            for (Map.Entry<ResourceType, JSpinner> entry : resourceSpinners.entrySet()) {
                double val = (Double) entry.getValue().getValue();
                if (val > 0) load.put(entry.getKey(), val);
            }

            Map<ResourceType, Double> returnLoad = new HashMap<>();
            if (bilateralCheck.isSelected()) {
                for (Map.Entry<ResourceType, JSpinner> entry : returnResourceSpinners.entrySet()) {
                    double val = (Double) entry.getValue().getValue();
                    if (val > 0) returnLoad.put(entry.getKey(), val);
                }
            }

            Map<AntType, Integer> transport = new HashMap<>();
            for (Map.Entry<AntType, JSpinner> entry : antSpinners.entrySet()) {
                int val = (Integer) entry.getValue().getValue();
                if (val > 0) transport.put(entry.getKey(), val);
            }

            if (load.isEmpty() && returnLoad.isEmpty()) {
                UiOptionPane.showMessageDialog(this, LanguageStrings.get(LanguageStrings.TRADE_ERROR_EMPTY));
                return;
            }
            if (transport.isEmpty()) {
                UiOptionPane.showMessageDialog(this, LanguageStrings.get(LanguageStrings.TRADE_ERROR_NO_PERSONNEL));
                return;
            }

            TradeMethod method = (TradeMethod) methodCombo.getSelectedItem();
            if (method == null) {
                UiOptionPane.showMessageDialog(this, LanguageStrings.get(LanguageStrings.TRADE_ERROR_NO_METHOD));
                return;
            }

            if (existingTrade != null) {
                existingTrade.setPendingUpdate(load, returnLoad, transport, recurrentCheck.isSelected(), bilateralCheck.isSelected(), method);
                UiOptionPane.showMessageDialog(this, LanguageStrings.get(LanguageStrings.TRADE_QUEUED_MSG));
            } else {
                World world = engine.getWorld();
                Hex originHex = world.getHexOfColony(origin);
                Hex targetHex = world.getHexOfColony(target);
                
                if (originHex == null || targetHex == null) {
                    UiOptionPane.showMessageDialog(this, LanguageStrings.get(LanguageStrings.DYNASTY_ERROR_LOCATE));
                    return;
                }

                Trade trade = new Trade(originHex, targetHex, load, returnLoad, transport, recurrentCheck.isSelected(), bilateralCheck.isSelected(), method);
                boolean started = trade.startTrip();
                
                if (started) {
                    if (engine.getTradeManager() != null) {
                        engine.getTradeManager().addTrade(trade);
                    }
                    if (crossDynasty && origin.getDynasty() != null && target.getDynasty() != null) {
                        origin.getDynasty().getDiplomacyService().onCrossDynastyTradeEstablished(
                                origin.getDynasty(), target.getDynasty(), CrossDynastyTradeProposal.Kind.OFFER);
                    }
                } else {
                    UiOptionPane.showMessageDialog(this, LanguageStrings.get(LanguageStrings.TRADE_ERROR_START));
                    return;
                }
            }
            dispose();
        }
    }

    private class DiplomacyPanel extends JPanel implements LiveUpdatePanel {
        private JTable table;
        private DefaultTableModel model;
        private JScrollPane tableScrollPane;
        private JComboBox<PactRequestIncomingPolicy> pactIncomingPolicyCombo;
        private final JComboBox<String> sortCombo;
        private final UiNameSearchBar searchBar;
        private Comparator<Dynasty> currentSorter;
        private final List<Dynasty> displayedDynasties = new ArrayList<>();
        private static final int COL_DYNASTY = 0;
        private static final int COL_SPECIES = 1;
        private static final int COL_REPUTATION = 2;
        private static final int COL_STANCE = 3;
        private static final int COL_MILITARY = 4;
        private static final int COL_ACTIONS = 5;

        public DiplomacyPanel() {
            super(new BorderLayout());
            this.currentSorter = reputationSorter(true);
            this.sortCombo = new JComboBox<>(new String[]{
                    LanguageStrings.get(LanguageStrings.DYNASTY_SORT_REPUTATION_HIGH),
                    LanguageStrings.get(LanguageStrings.DYNASTY_SORT_DISTANCE_NEAR),
                    LanguageStrings.get(LanguageStrings.DYNASTY_SORT_DISTANCE_FAR)
            });
            this.sortCombo.setFocusable(false);
            AssetStyles.styleComboBox(this.sortCombo);
            this.sortCombo.addActionListener(e -> updateSorter());

            this.searchBar = new UiNameSearchBar(
                    LanguageStrings.get(LanguageStrings.DYNASTY_SEARCH),
                    LanguageStrings.get(LanguageStrings.DYNASTY_SEARCH_TOOLTIP),
                    this::updateData);
            initUI();
        }

        private Comparator<Dynasty> reputationSorter(boolean highestFirst) {
            Comparator<Dynasty> byReputation = Comparator.comparingInt((Dynasty d) -> {
                World world = engine.getWorld();
                if (world == null || dynasty.getDiplomacyService() == null) {
                    return 0;
                }
                return dynasty.getDiplomacyService().getEffectiveDiplomaticReputation(d, world);
            });
            if (highestFirst) {
                byReputation = byReputation.reversed();
            }
            return byReputation.thenComparing(Dynasty::getName, String.CASE_INSENSITIVE_ORDER);
        }

        private Comparator<Dynasty> distanceSorter(boolean closestFirst) {
            Comparator<Dynasty> byDistance = Comparator.comparingInt((Dynasty d) -> {
                World world = engine.getWorld();
                if (world == null) {
                    return Integer.MAX_VALUE;
                }
                return world.minDynastyHexDistance(dynasty, d);
            });
            if (!closestFirst) {
                byDistance = byDistance.reversed();
            }
            return byDistance.thenComparing(Dynasty::getName, String.CASE_INSENSITIVE_ORDER);
        }

        private void updateSorter() {
            int idx = sortCombo.getSelectedIndex();
            switch (idx) {
                case 1:
                    currentSorter = distanceSorter(true);
                    break;
                case 2:
                    currentSorter = distanceSorter(false);
                    break;
                case 0:
                default:
                    currentSorter = reputationSorter(true);
                    break;
            }
            updateData();
        }

        private boolean matchesDiplomacySearch(Dynasty other) {
            if (searchBar.isBlank()) {
                return true;
            }
            if (other == null) {
                return false;
            }
            if (searchBar.matchesAny(other.getName())) {
                return true;
            }
            List<Colony> colonies = other.getColonies();
            if (colonies == null) {
                return false;
            }
            for (Colony colony : colonies) {
                if (colony != null && searchBar.matchesAny(colony.getName())) {
                    return true;
                }
            }
            return false;
        }

        private void initUI() {
            model = new DefaultTableModel(new String[]{
                    LanguageStrings.get(LanguageStrings.STAT_DYNASTY),
                    LanguageStrings.get(LanguageStrings.HELP_TAB_SPECIES),
                    LanguageStrings.get(LanguageStrings.DYNASTY_REPUTATION),
                    LanguageStrings.get(LanguageStrings.DYNASTY_REPUTATION_STANCE),
                    LanguageStrings.get(LanguageStrings.STAT_MILITARY_POWER),
                    LanguageStrings.get(LanguageStrings.DYNASTY_ACTIONS)
            }, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == COL_ACTIONS;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == COL_SPECIES) {
                        return AntSpecies.class;
                    }
                    if (columnIndex == COL_STANCE) {
                        return Object.class;
                    }
                    if (columnIndex == COL_MILITARY) {
                        return Integer.class;
                    }
                    if (columnIndex == COL_ACTIONS) {
                        return DiplomacyRowData.class;
                    }
                    return String.class;
                }
            };

            table = new JTable(model);
            table.setRowHeight(45);
            table.setFocusable(false);
            AssetStyles.applyScrollableDialogTable(table);
            AssetStyles.applyTableColumnAlignment(table, COL_DYNASTY, SwingConstants.LEFT);
            AssetStyles.applyTableHeaderAlignment(table, COL_SPECIES, SwingConstants.LEFT);
            AssetStyles.applyTableHeaderAlignment(table, COL_REPUTATION, SwingConstants.CENTER);
            AssetStyles.applyTableHeaderAlignment(table, COL_STANCE, SwingConstants.CENTER);
            AssetStyles.applyTableHeaderAlignment(table, COL_MILITARY, SwingConstants.CENTER);
            AssetStyles.applyTableHeaderAlignment(table, COL_ACTIONS, SwingConstants.CENTER);
            table.getColumnModel().getColumn(COL_DYNASTY).setCellRenderer(new DynastyNameRenderer());
            table.getColumnModel().getColumn(COL_SPECIES).setCellRenderer(new SpeciesRenderer());
            table.getColumnModel().getColumn(COL_REPUTATION).setCellRenderer(new ReputationScoreRenderer());
            table.getColumnModel().getColumn(COL_STANCE).setCellRenderer(new ReputationStanceRenderer());
            table.getColumnModel().getColumn(COL_MILITARY).setCellRenderer(new MilitaryPowerScoreRenderer());
            table.getColumnModel().getColumn(COL_ACTIONS).setCellRenderer(new DiplomacyActionRenderer());
            table.getColumnModel().getColumn(COL_ACTIONS).setCellEditor(new DiplomacyActionEditor());

            table.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    updateDiplomacyTableCursor(e);
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if (row >= 0 && row < displayedDynasties.size()
                            && (col == COL_REPUTATION || col == COL_STANCE)) {
                        Dynasty other = displayedDynasties.get(row);
                        World world = engine.getWorld();
                        if (dynasty.isIntegratingDynasty(other)) {
                            int pct = (int) Math.round(
                                    DynastyIntegrationService.getIntegrationProgressPercent(dynasty, other));
                            String completionDate = world != null
                                    ? DynastyIntegrationService.getIntegrationEstimatedCompletionDate(
                                            world, dynasty, other)
                                    : "";
                            int diplomats = DynastyIntegrationService.countIntegrationDiplomats(dynasty);
                            table.setToolTipText(LanguageStrings.format(
                                    LanguageStrings.DIPLO_INTEGRATION_PROGRESS_TOOLTIP_FMT,
                                    AssetStyles.formatNumber(pct),
                                    completionDate,
                                    AssetStyles.formatNumber(diplomats)));
                            return;
                        }
                        if (world != null && dynasty.getDiplomacyService() != null) {
                            table.setToolTipText(
                                    dynasty.getDiplomacyService().buildReputationModifierTooltip(other, world));
                            return;
                        }
                    }
                    if (row >= 0 && row < displayedDynasties.size() && col == COL_MILITARY) {
                        table.setToolTipText(LanguageStrings.get(LanguageStrings.STAT_MILITARY_POWER_DESC));
                        return;
                    }
                    table.setToolTipText(null);
                }
            });

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!SwingUtilities.isLeftMouseButton(e) || e.getClickCount() != 1) {
                        return;
                    }
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if (row < 0 || row >= displayedDynasties.size() || col != COL_DYNASTY) {
                        return;
                    }
                    if (!isDiplomacyDynastySwatchClick(e.getPoint(), row)) {
                        return;
                    }
                    navigateToDynastyCapital(displayedDynasties.get(row));
                }
            });

            tableScrollPane = AssetStyles.wrapScrollableTable(table);

            JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
            filterPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
            filterPanel.setBorder(BorderFactory.createMatteBorder(
                    0, 0, AssetStyles.BORDER_THICKNESS_INTERNAL, 0, AssetStyles.BORDER_COLOR));

            JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            sortPanel.setOpaque(false);
            JLabel sortLabel = new JLabel(LanguageStrings.get(LanguageStrings.DYNASTY_SORT_BY));
            sortLabel.setForeground(AssetStyles.FONT_COLOR);
            sortPanel.add(sortLabel);
            sortPanel.add(sortCombo);
            filterPanel.add(sortPanel);
            filterPanel.add(searchBar);

            JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
            settingsPanel.setBorder(BorderFactory.createTitledBorder(
                    LanguageStrings.get(LanguageStrings.DIPLO_PACT_INCOMING_POLICY_SECTION)));
            settingsPanel.setBackground(AssetStyles.BACKGROUND_COLOR);

            pactIncomingPolicyCombo = new JComboBox<>(PactRequestIncomingPolicy.values());
            pactIncomingPolicyCombo.setFocusable(false);
            AssetStyles.styleComboBox(pactIncomingPolicyCombo);
            pactIncomingPolicyCombo.setSelectedItem(dynasty.getPactRequestIncomingPolicy());
            pactIncomingPolicyCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof PactRequestIncomingPolicy policy) {
                        setText(LanguageStrings.get(policy.getNameKey()));
                    }
                    return this;
                }
            });
            pactIncomingPolicyCombo.addActionListener(e -> {
                PactRequestIncomingPolicy selected =
                        (PactRequestIncomingPolicy) pactIncomingPolicyCombo.getSelectedItem();
                if (selected != null) {
                    dynasty.setPactRequestIncomingPolicy(selected);
                }
            });

            JLabel policyLabel = new JLabel(LanguageStrings.get(LanguageStrings.DIPLO_PACT_INCOMING_POLICY_LABEL));
            policyLabel.setForeground(AssetStyles.FONT_COLOR);
            settingsPanel.add(policyLabel);
            settingsPanel.add(pactIncomingPolicyCombo);

            JPanel northPanel = new JPanel();
            northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
            northPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
            filterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            settingsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            northPanel.add(filterPanel);
            northPanel.add(settingsPanel);
            add(northPanel, BorderLayout.NORTH);
            add(tableScrollPane, BorderLayout.CENTER);
        }

        private static final int DYNASTY_SWATCH_CELL_PADDING = 4;

        private boolean isDiplomacyDynastySwatchClick(java.awt.Point point, int row) {
            Rectangle cellRect = table.getCellRect(row, COL_DYNASTY, false);
            int relativeX = point.x - cellRect.x;
            return relativeX >= DYNASTY_SWATCH_CELL_PADDING
                    && relativeX <= DYNASTY_SWATCH_CELL_PADDING + DynastyColorSwatch.hitboxWidth();
        }

        private void updateDiplomacyTableCursor(MouseEvent e) {
            int row = table.rowAtPoint(e.getPoint());
            int col = table.columnAtPoint(e.getPoint());
            if (row >= 0 && row < displayedDynasties.size()
                    && col == COL_DYNASTY
                    && isDiplomacyDynastySwatchClick(e.getPoint(), row)
                    && canNavigateToDynastyCapital(displayedDynasties.get(row))) {
                table.setCursor(AssetStyles.cursorClickable());
                return;
            }
            table.setCursor(null);
        }

        private boolean canNavigateToDynastyCapital(Dynasty other) {
            if (other == null || engine.getWorld() == null) {
                return false;
            }
            Colony capital = other.getCapital();
            return capital != null && engine.getWorld().getHexOfColony(capital) != null;
        }

        private void navigateToDynastyCapital(Dynasty other) {
            if (!canNavigateToDynastyCapital(other)) {
                return;
            }
            performView(other.getCapital());
        }

        private class DynastyNameRenderer extends JPanel implements TableCellRenderer {
            private final DynastyColorSwatch swatch = new DynastyColorSwatch(Color.GRAY);
            private final JLabel nameLabel = new JLabel();

            DynastyNameRenderer() {
                setLayout(new BorderLayout(6, 0));
                setBorder(BorderFactory.createEmptyBorder(0, DYNASTY_SWATCH_CELL_PADDING, 0, 4));
                add(swatch, BorderLayout.WEST);
                add(nameLabel, BorderLayout.CENTER);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Color background = isSelected ? table.getSelectionBackground() : table.getBackground();
                Color foreground = isSelected ? table.getSelectionForeground() : table.getForeground();
                setBackground(background);
                nameLabel.setBackground(background);
                nameLabel.setForeground(foreground);
                nameLabel.setFont(table.getFont());

                if (row >= 0 && row < displayedDynasties.size()) {
                    Dynasty other = displayedDynasties.get(row);
                    swatch.setDynastyColor(other.getColor());
                    nameLabel.setText(other.getName());
                    if (canNavigateToDynastyCapital(other)) {
                        setToolTipText(LanguageStrings.format(
                                LanguageStrings.MAP_CLICK_VIEW_CAPITAL, other.getName()));
                    } else {
                        setToolTipText(null);
                    }
                    swatch.clearClickAction();
                } else {
                    swatch.setDynastyColor(Color.GRAY);
                    swatch.clearClickAction();
                    nameLabel.setText(value != null ? String.valueOf(value) : "");
                }
                return this;
            }
        }

        private class MilitaryPowerScoreRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                int power = value instanceof Number number ? number.intValue() : 0;
                setText(AssetStyles.formatNumber(power));
                setIcon(GameConstants.ICON_STAT_MILITARY_POWER);
                setIconTextGap(6);
                return this;
            }
        }

        private class ReputationScoreRenderer extends JPanel implements TableCellRenderer {
            private final JLabel scoreLabel = new JLabel();
            private final JProgressBar progressBar = new JProgressBar(0, 100);

            ReputationScoreRenderer() {
                setLayout(new BorderLayout());
                scoreLabel.setHorizontalAlignment(JLabel.CENTER);
                progressBar.setStringPainted(true);
                AssetStyles.styleProgressBar(progressBar);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                removeAll();
                if (row >= 0 && row < displayedDynasties.size()) {
                    Dynasty other = displayedDynasties.get(row);
                    if (dynasty.isIntegratingDynasty(other)) {
                        World world = engine.getWorld();
                        int pct = (int) Math.round(DynastyIntegrationService.getIntegrationProgressPercent(dynasty, other));
                        String completionDate = world != null
                                ? DynastyIntegrationService.getIntegrationEstimatedCompletionDate(world, dynasty, other)
                                : "";
                        int diplomats = DynastyIntegrationService.countIntegrationDiplomats(dynasty);
                        progressBar.setValue(pct);
                        progressBar.setString(LanguageStrings.format(
                                LanguageStrings.DIPLO_INTEGRATION_PROGRESS_FMT,
                                AssetStyles.formatNumber(pct),
                                completionDate,
                                AssetStyles.formatNumber(diplomats)));
                        add(progressBar, BorderLayout.CENTER);
                        return this;
                    }
                }
                scoreLabel.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                scoreLabel.setBackground(getBackground());
                scoreLabel.setOpaque(true);
                if (row >= 0 && row < displayedDynasties.size()) {
                    World world = engine.getWorld();
                    Dynasty other = displayedDynasties.get(row);
                    int score = world != null && dynasty.getDiplomacyService() != null
                            ? dynasty.getDiplomacyService().getEffectiveDiplomaticReputation(other, world)
                            : 0;
                    scoreLabel.setText(String.valueOf(score));
                    scoreLabel.setIcon(GameConstants.ICON_STAT_REPUTATION);
                    scoreLabel.setIconTextGap(6);
                } else {
                    scoreLabel.setText(value != null ? String.valueOf(value) : "");
                    scoreLabel.setIcon(null);
                }
                add(scoreLabel, BorderLayout.CENTER);
                return this;
            }
        }

        @Override
        public void liveUpdate() {
            updateData();
        }

        @Override
        public void updateData() {
            if (table.isEditing()) {
                return;
            }

            World world = engine.getWorld();
            if (world == null) {
                model.setRowCount(0);
                displayedDynasties.clear();
                return;
            }

            int selectedRow = table.getSelectedRow();
            model.setRowCount(0);
            displayedDynasties.clear();

            List<Dynasty> others = new ArrayList<>();
            for (Dynasty other : world.getDynastys()) {
                if (other != dynasty && other.isActiveForDiplomacy()
                        && matchesDiplomacySearch(other)) {
                    others.add(other);
                }
            }
            if (currentSorter != null) {
                others.sort(currentSorter);
            }

            displayedDynasties.addAll(others);
            for (Dynasty other : displayedDynasties) {
                int score = dynasty.getDiplomacyService().getEffectiveDiplomaticReputation(other, world);
                DiplomaticReputation stance = GameConstants.getDiplomaticReputationLevel(score);
                Object stanceValue = dynasty.isIntegratingDynasty(other)
                        ? LanguageStrings.get(LanguageStrings.DIPLO_INTEGRATION_STANCE)
                        : stance;
                model.addRow(new Object[]{
                        other.getName(),
                        other.getSpecies(),
                        String.valueOf(score),
                        stanceValue,
                        other.getMilitaryPower(),
                        new DiplomacyRowData(other)
                });
            }

            if (selectedRow >= 0 && selectedRow < table.getRowCount()) {
                table.setRowSelectionInterval(selectedRow, selectedRow);
            }
            AssetStyles.relayoutTableInScrollPane(tableScrollPane,
                    new boolean[]{true, false, false, false, false, true});
        }

        private void performPactAction(Dynasty other) {
            if (other == null || dynasty.getDiplomacyService() == null) {
                return;
            }
            World world = engine.getWorld();
            if (dynasty.getDiplomacyService().hasNonAggressionPact(other)) {
                dynasty.getDiplomacyService().breakNonAggressionPact(other, world);
            } else if (dynasty.getDiplomacyService().canRequestNonAggressionPact(other, world)) {
                dynasty.getDiplomacyService().requestNonAggressionPact(other, world);
                if (dynasty.getDiplomacyService().hasNonAggressionPact(other)) {
                    UiOptionPane.showMessageDialog(this,
                            LanguageStrings.format(LanguageStrings.DIPLO_PACT_ACCEPTED_FMT, other.getName()),
                            LanguageStrings.get(LanguageStrings.DIPLO_ACTION_FORM_PACT),
                            JOptionPane.INFORMATION_MESSAGE);
                } else if (other.isPlayer()) {
                    UiOptionPane.showMessageDialog(this,
                            LanguageStrings.format(LanguageStrings.DIPLO_PACT_REQUEST_SENT_FMT, other.getName()),
                            LanguageStrings.get(LanguageStrings.DIPLO_ACTION_REQUEST_PACT),
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    UiOptionPane.showMessageDialog(this,
                            LanguageStrings.format(LanguageStrings.DIPLO_PACT_DECLINED_FMT, other.getName()),
                            LanguageStrings.get(LanguageStrings.DIPLO_ACTION_REQUEST_PACT),
                            JOptionPane.WARNING_MESSAGE);
                }
            } else {
                String message;
                int pactCooldown = dynasty.getDiplomacyService().getPactRequestDeclineCooldownMonthsRemaining(other, world);
                if (pactCooldown > 0) {
                    message = LanguageStrings.format(LanguageStrings.DIPLO_ERROR_PACT_DECLINE_COOLDOWN_FMT, pactCooldown);
                } else {
                    message = LanguageStrings.get(LanguageStrings.DIPLO_ERROR_CORDIAL_REQUIRED);
                }
                UiOptionPane.showMessageDialog(this, message,
                        LanguageStrings.get(LanguageStrings.DIPLO_ACTION_FORM_PACT),
                        JOptionPane.WARNING_MESSAGE);
            }
            updateData();
        }

        private void performIntegrateAction(Dynasty other) {
            World world = engine.getWorld();
            if (other == null || world == null) {
                return;
            }
            TradeManager tradeManager = engine.getTradeManager();
            if (DynastyIntegrationService.startIntegration(world, dynasty, other, tradeManager)) {
                UiOptionPane.showMessageDialog(this,
                        LanguageStrings.format(LanguageStrings.DIPLO_INTEGRATION_STARTED_FMT, other.getName()),
                        LanguageStrings.get(LanguageStrings.DIPLO_ACTION_INTEGRATE),
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                String message = resolveIntegrationErrorMessage(other, world, tradeManager);
                UiOptionPane.showMessageDialog(this, message,
                        LanguageStrings.get(LanguageStrings.DIPLO_ACTION_INTEGRATE),
                        JOptionPane.WARNING_MESSAGE);
            }
            updateData();
        }

        private String resolveIntegrationErrorMessage(Dynasty other, World world, TradeManager tradeManager) {
            if (dynasty.hasActiveIntegration() && !dynasty.isIntegratingDynasty(other)) {
                return LanguageStrings.get(LanguageStrings.DIPLO_ERROR_INTEGRATION_ACTIVE);
            }
            if (DynastyIntegrationService.findIntegrationOverlord(world, other.getId()) != null) {
                return LanguageStrings.get(LanguageStrings.DIPLO_ERROR_INTEGRATION_TARGET_BUSY);
            }
            DynastyDiplomacyService diplo = dynasty.getDiplomacyService();
            if (diplo == null) {
                return LanguageStrings.get(LanguageStrings.DIPLO_ERROR_INTEGRATION_ACTIVE);
            }
            if (!diplo.hasNonAggressionPact(other)) {
                return LanguageStrings.get(LanguageStrings.DIPLO_ERROR_INTEGRATION_PACT);
            }
            if (!diplo.sharesBorderWith(other, world)) {
                return LanguageStrings.get(LanguageStrings.DIPLO_ERROR_INTEGRATION_BORDER);
            }
            if (!DynastyIntegrationService.meetsMilitaryRequirement(dynasty, other)) {
                return LanguageStrings.get(LanguageStrings.DIPLO_ERROR_INTEGRATION_MILITARY);
            }
            if (!DynastyIntegrationService.meetsReputationRequirement(dynasty, other, world)) {
                return LanguageStrings.get(LanguageStrings.DIPLO_ERROR_INTEGRATION_REPUTATION);
            }
            if (DynastyIntegrationService.countIntegrationDiplomatCapacity(dynasty)
                    < GameNumbers.INTEGRATION_MIN_DIPLOMATS) {
                return LanguageStrings.get(LanguageStrings.DIPLO_ERROR_INTEGRATION_DIPLOMATS);
            }
            return LanguageStrings.get(LanguageStrings.DIPLO_ERROR_INTEGRATION_ACTIVE);
        }

        private void performCancelIntegrationAction(Dynasty other) {
            if (other == null || !dynasty.isIntegratingDynasty(other)) {
                return;
            }
            int confirm = UiOptionPane.showConfirmDialog(this,
                    LanguageStrings.format(LanguageStrings.DIPLO_INTEGRATION_CANCEL_CONFIRM_FMT, other.getName()),
                    LanguageStrings.get(LanguageStrings.DIPLO_ACTION_CANCEL_INTEGRATION),
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            DynastyIntegrationService.cancelIntegration(
                    engine.getWorld(), dynasty, engine.getTradeManager(), true);
            updateData();
        }

        private void performManageIntegrationDiplomatsAction(Dynasty other) {
            if (other == null || !dynasty.isIntegratingDynasty(other)) {
                return;
            }
            int max = DynastyIntegrationService.countMaxAssignableIntegrationDiplomats(
                    dynasty, engine.getWorld(), engine.getTradeManager());
            if (max < GameNumbers.INTEGRATION_MIN_DIPLOMATS) {
                UiOptionPane.showMessageDialog(this,
                        LanguageStrings.get(LanguageStrings.DIPLO_ERROR_INTEGRATION_DIPLOMATS),
                        LanguageStrings.get(LanguageStrings.DIPLO_ACTION_MANAGE_INTEGRATION_DIPLOMATS),
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int current = DynastyIntegrationService.countIntegrationDiplomats(dynasty);
            int count = promptIntegrationDiplomatCount(this, max, current);
            if (count < GameNumbers.INTEGRATION_MIN_DIPLOMATS) {
                return;
            }
            DynastyIntegrationService.assignIntegrationDiplomats(
                    dynasty, count, true, engine.getWorld(), engine.getTradeManager());
            updateData();
        }

        private void performDeclareWarAction(Dynasty other) {
            World world = engine.getWorld();
            if (other == null || dynasty.getDiplomacyService() == null || world == null) {
                return;
            }
            DynastyDiplomacyService diplo = dynasty.getDiplomacyService();
            if (!diplo.canDeclareWar(other, world)) {
                String message;
                if (!DynastyDiplomacyService.meetsWarDeclarationPopulationRequirement(dynasty)) {
                    message = LanguageStrings.format(
                            LanguageStrings.DIPLO_ERROR_WAR_POPULATION_FMT,
                            GameNumbers.WAR_DECLARATION_MIN_POPULATION);
                } else if (!DynastyDiplomacyService.meetsWarActiveMilitaryRequirement(dynasty)) {
                    message = LanguageStrings.get(LanguageStrings.DIPLO_ERROR_WAR_ACTIVE_MILITARY);
                } else if (diplo.hasNonAggressionPact(other)) {
                    message = LanguageStrings.get(LanguageStrings.DIPLO_ERROR_PACT_BLOCKS_WAR);
                } else {
                    int remaining = diplo.getWarDeclarationCooldownMonthsRemaining(other, world);
                    if (remaining > 0) {
                        message = LanguageStrings.format(LanguageStrings.DIPLO_ERROR_WAR_COOLDOWN_FMT, remaining);
                    } else {
                        message = LanguageStrings.get(LanguageStrings.DIPLO_ERROR_WAR_NO_BORDER);
                    }
                }
                UiOptionPane.showMessageDialog(this, message,
                        LanguageStrings.get(LanguageStrings.DIPLO_ACTION_DECLARE_WAR),
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = UiOptionPane.showConfirmDialog(this,
                    LanguageStrings.format(LanguageStrings.DIPLO_WAR_DECLARE_CONFIRM_FMT, other.getName()),
                    LanguageStrings.get(LanguageStrings.DIPLO_ACTION_DECLARE_WAR),
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            diplo.declareWar(other, world, engine.getTradeManager());
            updateData();
        }

        private void performTradeAction(Dynasty other) {
            World world = engine.getWorld();
            Colony activeColony = getActivePlayerColony();
            if (other == null || dynasty.getDiplomacyService() == null || world == null) {
                return;
            }
            if (activeColony == null) {
                UiOptionPane.showMessageDialog(this,
                        LanguageStrings.get(LanguageStrings.DIPLO_ERROR_ACTIVE_COLONY),
                        LanguageStrings.get(LanguageStrings.DIPLO_ACTION_TRADE),
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Colony neighbor = dynasty.getDiplomacyService().findNeighborColony(other, activeColony, world);
            if (neighbor == null) {
                UiOptionPane.showMessageDialog(this,
                        LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NO_BORDER),
                        LanguageStrings.get(LanguageStrings.DIPLO_ACTION_TRADE),
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Trade outgoing = dynasty.getTradeService().findTrade(activeColony, neighbor);
            Trade incoming = dynasty.getTradeService().findTrade(neighbor, activeColony);
            if (outgoing != null || incoming != null) {
                if (tradePanel != null) {
                    tradePanel.showActionsMenuForNeighbor(neighbor, this);
                }
            } else if (!dynasty.getDiplomacyService().canParticipateInCrossDynastyTrade(other, activeColony, world)) {
                String message = LanguageStrings.get(LanguageStrings.DIPLO_ERROR_LOYALTY_REQUIRED);
                if (!dynasty.hasUpgrade(GameUnlocks.ABILITY_TRADE)) {
                    message = LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NEUTRAL_REQUIRED);
                }
                UiOptionPane.showMessageDialog(this, message,
                        LanguageStrings.get(LanguageStrings.DIPLO_ACTION_TRADE),
                        JOptionPane.WARNING_MESSAGE);
            } else if (!DynastyTradeAutomation.computeOutboundLoad(activeColony, neighbor).isEmpty()
                    && dynasty.getDiplomacyService().canOfferTrade(other, world)) {
                showTradeProposalResult(other,
                        dynasty.getDiplomacyService().offerTrade(
                                other, activeColony, world, engine.getTradeManager()),
                        CrossDynastyTradeProposal.Kind.OFFER);
            } else if (!DynastyTradeAutomation.computeOutboundLoad(neighbor, activeColony).isEmpty()
                    && dynasty.getDiplomacyService().canRequestTrade(other, world)) {
                showTradeProposalResult(other,
                        dynasty.getDiplomacyService().requestTrade(
                                other, activeColony, world, engine.getTradeManager()),
                        CrossDynastyTradeProposal.Kind.REQUEST);
            } else if (!dynasty.getDiplomacyService().canOfferTrade(other, world)
                    && !dynasty.getDiplomacyService().canRequestTrade(other, world)) {
                UiOptionPane.showMessageDialog(this,
                        LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NEUTRAL_REQUIRED),
                        LanguageStrings.get(LanguageStrings.DIPLO_ACTION_TRADE),
                        JOptionPane.WARNING_MESSAGE);
            } else {
                UiOptionPane.showMessageDialog(this,
                        LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NO_TRADE_CARGO),
                        LanguageStrings.get(LanguageStrings.DIPLO_ACTION_TRADE),
                        JOptionPane.WARNING_MESSAGE);
            }
            updateData();
        }

        private void performRequestTradeAction(Dynasty other) {
            if (other == null || dynasty.getDiplomacyService() == null) {
                return;
            }
            World world = engine.getWorld();
            Colony activeColony = getActivePlayerColony();
            if (activeColony == null) {
                UiOptionPane.showMessageDialog(this,
                        LanguageStrings.get(LanguageStrings.DIPLO_ERROR_ACTIVE_COLONY),
                        LanguageStrings.get(LanguageStrings.DIPLO_ACTION_REQUEST_TRADE),
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!dynasty.getDiplomacyService().canRequestTrade(other, world)) {
                UiOptionPane.showMessageDialog(this,
                        LanguageStrings.get(LanguageStrings.DIPLO_ERROR_CORDIAL_REQUIRED_REQUEST),
                        LanguageStrings.get(LanguageStrings.DIPLO_ACTION_REQUEST_TRADE),
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            showTradeProposalResult(other,
                    dynasty.getDiplomacyService().requestTrade(
                            other, activeColony, world, engine.getTradeManager()),
                    CrossDynastyTradeProposal.Kind.REQUEST);
            updateData();
        }

        private void showTradeProposalResult(
                Dynasty other,
                DynastyDiplomacyService.TradeProposalResult result,
                CrossDynastyTradeProposal.Kind kind) {
            String title = kind == CrossDynastyTradeProposal.Kind.OFFER
                    ? LanguageStrings.get(LanguageStrings.DIPLO_ACTION_TRADE)
                    : LanguageStrings.get(LanguageStrings.DIPLO_ACTION_REQUEST_TRADE);
            switch (result) {
                case QUEUED -> UiOptionPane.showMessageDialog(this,
                        kind == CrossDynastyTradeProposal.Kind.OFFER
                                ? LanguageStrings.format(LanguageStrings.DIPLO_TRADE_OFFER_SENT_FMT, other.getName())
                                : LanguageStrings.get(LanguageStrings.DIPLO_TRADE_REQUEST_SENT),
                        title,
                        JOptionPane.INFORMATION_MESSAGE);
                case ACCEPTED -> UiOptionPane.showMessageDialog(this,
                        LanguageStrings.format(LanguageStrings.DIPLO_TRADE_ACCEPTED_FMT, other.getName()),
                        title,
                        JOptionPane.INFORMATION_MESSAGE);
                case DECLINED -> UiOptionPane.showMessageDialog(this,
                        LanguageStrings.format(LanguageStrings.DIPLO_TRADE_DECLINED_FMT, other.getName()),
                        title,
                        JOptionPane.WARNING_MESSAGE);
                default -> UiOptionPane.showMessageDialog(this,
                        LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NO_TRADE_CARGO),
                        title,
                        JOptionPane.WARNING_MESSAGE);
            }
        }

        private class DiplomacyRowData {
            final Dynasty other;

            DiplomacyRowData(Dynasty other) {
                this.other = other;
            }
        }

        private class DiplomacyActionRenderer extends JPanel implements TableCellRenderer {
            private final JButton actionsBtn = new JButton(LanguageStrings.get(LanguageStrings.DYNASTY_ACTIONS));

            DiplomacyActionRenderer() {
                setLayout(new FlowLayout(FlowLayout.CENTER, 4, 2));
                actionsBtn.setFocusable(false);
                AssetStyles.styleCompactButton(actionsBtn);
                add(actionsBtn);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                AssetStyles.styleCompactButton(actionsBtn);
                return this;
            }
        }

        private class DiplomacyActionEditor extends AbstractCellEditor implements TableCellEditor {
            private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
            private final JButton actionsBtn = new JButton(LanguageStrings.get(LanguageStrings.DYNASTY_ACTIONS));
            private DiplomacyRowData currentData;
            private JTable editingTable;
            private int editingRow = -1;
            private int editingCol = -1;

            DiplomacyActionEditor() {
                actionsBtn.setFocusable(false);
                AssetStyles.styleCompactButton(actionsBtn);
                panel.add(actionsBtn);
                actionsBtn.addActionListener(e -> {
                    JTable tableRef = editingTable;
                    int row = editingRow;
                    int col = editingCol;
                    Dynasty other = currentData != null ? currentData.other : null;
                    fireEditingStopped();
                    if (other != null) {
                        showDiplomacyActionsMenu(other, tableRef, row, col);
                    }
                });
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                currentData = (DiplomacyRowData) value;
                editingTable = table;
                editingRow = row;
                editingCol = column;
                panel.setBackground(table.getSelectionBackground());
                return panel;
            }

            @Override
            public Object getCellEditorValue() {
                return currentData;
            }
        }

        private void showDiplomacyActionsMenu(Dynasty other, JTable table, int row, int column) {
            if (other == null || dynasty.getDiplomacyService() == null) {
                return;
            }
            JPopupMenu menu = new JPopupMenu();
            World world = engine.getWorld();
            Colony activeColony = getActivePlayerColony();
            DynastyDiplomacyService diplo = dynasty.getDiplomacyService();

            JMenuItem pactItem = new JMenuItem(LanguageStrings.get(LanguageStrings.DIPLO_ACTION_REQUEST_PACT));
            if (diplo.hasNonAggressionPact(other)) {
                pactItem.setText(LanguageStrings.get(LanguageStrings.DIPLO_ACTION_BREAK_PACT));
                pactItem.addActionListener(e -> performPactAction(other));
            } else if (diplo.canRequestNonAggressionPact(other, world)) {
                pactItem.addActionListener(e -> performPactAction(other));
            } else {
                pactItem.setEnabled(false);
                int pactCooldown = diplo.getPactRequestDeclineCooldownMonthsRemaining(other, world);
                if (pactCooldown > 0) {
                    pactItem.setToolTipText(LanguageStrings.format(
                            LanguageStrings.DIPLO_ERROR_PACT_DECLINE_COOLDOWN_FMT, pactCooldown));
                } else {
                    pactItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_CORDIAL_REQUIRED));
                }
            }
            AssetStyles.styleMenuItem(pactItem);
            menu.add(pactItem);

            if (dynasty.isIntegratingDynasty(other)) {
                JMenuItem cancelIntegrationItem = new JMenuItem(
                        LanguageStrings.get(LanguageStrings.DIPLO_ACTION_CANCEL_INTEGRATION));
                cancelIntegrationItem.addActionListener(e -> performCancelIntegrationAction(other));
                AssetStyles.styleMenuItem(cancelIntegrationItem);
                menu.add(cancelIntegrationItem);

                JMenuItem manageDiplomatsItem = new JMenuItem(
                        LanguageStrings.get(LanguageStrings.DIPLO_ACTION_MANAGE_INTEGRATION_DIPLOMATS));
                int maxDiplomats = DynastyIntegrationService.countMaxAssignableIntegrationDiplomats(
                        dynasty, world, engine.getTradeManager());
                if (maxDiplomats >= GameNumbers.INTEGRATION_MIN_DIPLOMATS) {
                    manageDiplomatsItem.addActionListener(e -> performManageIntegrationDiplomatsAction(other));
                } else {
                    manageDiplomatsItem.setEnabled(false);
                    manageDiplomatsItem.setToolTipText(
                            LanguageStrings.get(LanguageStrings.DIPLO_ERROR_INTEGRATION_DIPLOMATS));
                }
                AssetStyles.styleMenuItem(manageDiplomatsItem);
                menu.add(manageDiplomatsItem);
            } else if (!diplo.isAtWarWith(other)) {
                JMenuItem integrateItem = new JMenuItem(LanguageStrings.get(LanguageStrings.DIPLO_ACTION_INTEGRATE));
                if (DynastyIntegrationService.canStartIntegration(dynasty, other, world, engine.getTradeManager())) {
                    integrateItem.addActionListener(e -> performIntegrateAction(other));
                } else {
                    integrateItem.setEnabled(false);
                    integrateItem.setToolTipText(resolveIntegrationErrorMessage(other, world, engine.getTradeManager()));
                }
                AssetStyles.styleMenuItem(integrateItem);
                menu.add(integrateItem);
            }

            if (!diplo.isAtWarWith(other)) {
                JMenuItem warItem = new JMenuItem(LanguageStrings.get(LanguageStrings.DIPLO_ACTION_DECLARE_WAR));
                if (diplo.canDeclareWar(other, world)) {
                    warItem.addActionListener(e -> performDeclareWarAction(other));
                } else {
                    warItem.setEnabled(false);
                    if (!DynastyDiplomacyService.meetsWarDeclarationPopulationRequirement(dynasty)) {
                        warItem.setToolTipText(LanguageStrings.format(
                                LanguageStrings.DIPLO_ERROR_WAR_POPULATION_FMT,
                                GameNumbers.WAR_DECLARATION_MIN_POPULATION));
                    } else if (!DynastyDiplomacyService.meetsWarActiveMilitaryRequirement(dynasty)) {
                        warItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_WAR_ACTIVE_MILITARY));
                    } else if (diplo.hasNonAggressionPact(other)) {
                        warItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_PACT_BLOCKS_WAR));
                    } else {
                        int remaining = diplo.getWarDeclarationCooldownMonthsRemaining(other, world);
                        if (remaining > 0) {
                            warItem.setToolTipText(LanguageStrings.format(
                                    LanguageStrings.DIPLO_ERROR_WAR_COOLDOWN_FMT, remaining));
                        } else {
                            warItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_WAR_NO_BORDER));
                        }
                    }
                }
                AssetStyles.styleMenuItem(warItem);
                menu.add(warItem);
            }

            if (!diplo.isAtWarWith(other)) {
                JMenuItem requestItem = new JMenuItem(LanguageStrings.get(LanguageStrings.DIPLO_ACTION_REQUEST_TRADE));
                if (diplo.canRequestTrade(other, world)) {
                    requestItem.addActionListener(e -> performRequestTradeAction(other));
                } else {
                    requestItem.setEnabled(false);
                    int tradeCooldown = diplo.getTradeRequestDeclineCooldownMonthsRemaining(other, world);
                    if (tradeCooldown > 0) {
                        requestItem.setToolTipText(LanguageStrings.format(
                                LanguageStrings.DIPLO_ERROR_TRADE_DECLINE_COOLDOWN_FMT, tradeCooldown));
                    } else {
                        requestItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_CORDIAL_REQUIRED_REQUEST));
                    }
                }
                AssetStyles.styleMenuItem(requestItem);
                menu.add(requestItem);

                JMenuItem tradeItem = new JMenuItem(LanguageStrings.get(LanguageStrings.DIPLO_ACTION_TRADE));
                if (activeColony == null) {
                    tradeItem.setEnabled(false);
                    tradeItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_ACTIVE_COLONY));
                } else {
                    Colony neighbor = world != null
                            ? diplo.findNeighborColony(other, activeColony, world)
                            : null;
                    if (neighbor == null) {
                        tradeItem.setEnabled(false);
                        tradeItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NO_BORDER));
                    } else {
                        Trade outgoing = dynasty.getTradeService().findTrade(activeColony, neighbor);
                        Trade incoming = dynasty.getTradeService().findTrade(neighbor, activeColony);
                        if (outgoing != null || incoming != null) {
                            tradeItem.setText(LanguageStrings.get(LanguageStrings.UI_MANAGE));
                            tradeItem.addActionListener(e -> performTradeAction(other));
                        } else if (diplo.canParticipateInCrossDynastyTrade(other, activeColony, world)
                                && (diplo.canOfferTrade(other, world) || diplo.canRequestTrade(other, world))) {
                            tradeItem.addActionListener(e -> performTradeAction(other));
                        } else {
                            tradeItem.setEnabled(false);
                            if (!DynastyDiplomacyService.meetsTradeLoyaltyRequirement(activeColony)) {
                                tradeItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_LOYALTY_REQUIRED));
                            } else if (!diplo.canOfferTrade(other, world) && !diplo.canRequestTrade(other, world)) {
                                tradeItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_WARY_REQUIRED_OFFER));
                            } else {
                                tradeItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NO_TRADE_CARGO));
                            }
                        }
                    }
                }
                AssetStyles.styleMenuItem(tradeItem);
                menu.add(tradeItem);

                JMenuItem diplomatItem = new JMenuItem(LanguageStrings.get(LanguageStrings.DIPLO_ACTION_SEND_DIPLOMATS));
                if (diplo.canManageDynastyMissionDiplomats(other, world)) {
                    diplomatItem.addActionListener(e -> promptManageDynastyDiplomats(DynastyManagementDialog.this, other));
                } else {
                    diplomatItem.setEnabled(false);
                    if (!dynasty.hasUpgrade(GameUnlocks.ROLE_DIPLOMAT)) {
                        diplomatItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NO_DIPLOMAT_ROLE));
                    } else if (diplo.isAtWarWith(other)) {
                        diplomatItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_DIPLOMATS_AT_WAR));
                    } else if (diplo.countDynastyWideAvailableDiplomats() <= 0
                            && diplo.countDynastyMissionDiplomatsToward(other) <= 0) {
                        diplomatItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NO_DIPLOMATS));
                    } else if (!diplo.needsDiplomatMissionToDynasty(other, world)) {
                        diplomatItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_REPUTATION_STABLE));
                    }
                }
                AssetStyles.styleMenuItem(diplomatItem);
                menu.add(diplomatItem);

                JMenuItem geneticExchangeItem = new JMenuItem(
                        LanguageStrings.get(LanguageStrings.DIPLO_ACTION_GENETIC_EXCHANGE));
                if (diplo.canOfferGeneticExchange(other, world)) {
                    geneticExchangeItem.addActionListener(e -> performGeneticExchangeAction(other));
                } else {
                    geneticExchangeItem.setEnabled(false);
                    if (diplo.hasActiveGeneticExchangeWith(other)) {
                        geneticExchangeItem.setToolTipText(
                                LanguageStrings.get(LanguageStrings.DIPLO_ERROR_GENETIC_EXCHANGE_ACTIVE));
                    } else if (diplo.getEffectiveDiplomaticReputation(other, world)
                            < GameConstants.REPUTATION_CORDIAL.getMinScore()) {
                        geneticExchangeItem.setToolTipText(
                                LanguageStrings.get(LanguageStrings.DIPLO_ERROR_GENETIC_EXCHANGE_REP));
                    } else {
                        geneticExchangeItem.setToolTipText(
                                LanguageStrings.get(LanguageStrings.DIPLO_ERROR_GENETIC_EXCHANGE_DRONES));
                    }
                }
                AssetStyles.styleMenuItem(geneticExchangeItem);
                menu.add(geneticExchangeItem);
            }

            UiTableStyles.showCellPopupMenu(menu, table, row, column);
        }
    }

    private static class SpeciesRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof AntSpecies species) {
                setText(species.getName());
                setIcon(species.getIcon());
                setIconTextGap(8);
            } else {
                setText(LanguageStrings.get(LanguageStrings.WORLD_NA));
                setIcon(null);
            }
            return this;
        }
    }

    private static class ReputationStanceRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof DiplomaticReputation reputation) {
                setText(reputation.getName());
                setIcon(reputation.getIcon());
                setIconTextGap(8);
            } else if (value instanceof String stanceText) {
                setText(stanceText);
                setIcon(null);
            } else {
                setText(LanguageStrings.get(LanguageStrings.WORLD_NA));
                setIcon(null);
            }
            return this;
        }
    }

    private class OverviewPanel extends JPanel implements LiveUpdatePanel {
        private JTable table;
        private DefaultTableModel model;
        private JScrollPane tableScrollPane;
        private List<Colony> displayedColonies;
        private final boolean showAutoBuild;
        private final boolean showAutomation;
        private final boolean showAutoTunnels;
        private final boolean showAutoDiplomacy;
        private int autoBuildCol = -1;
        private int automationCol = -1;
        private int autoTunnelsCol = -1;
        private int loyaltyCol = -1;
        private int militaryCol = -1;
        private int actionCol = -1;
        
        private final JComboBox<String> sortCombo;
        private final UiNameSearchBar searchBar;
        private Comparator<Colony> currentSorter;

        private JCheckBox defaultAutoBuildCheck;
        private JCheckBox defaultAutomationCheck;
        private JCheckBox defaultAutoTunnelsCheck;
        private JCheckBox autoDiplomacyCheck;

        public OverviewPanel(boolean showAutoBuild, boolean showAutomation, boolean showAutoTunnels, boolean showAutoDiplomacy) {
            super(new BorderLayout());
            this.showAutoBuild = showAutoBuild;
            this.showAutomation = showAutomation;
            this.showAutoTunnels = showAutoTunnels;
            this.showAutoDiplomacy = showAutoDiplomacy;
            this.displayedColonies = new ArrayList<>();
            
            this.currentSorter = Comparator.comparingInt(Colony::getAntTotal).reversed();
            this.sortCombo = new JComboBox<>(new String[]{
                LanguageStrings.get(LanguageStrings.DYNASTY_SORT_POP_HIGH),
                LanguageStrings.get(LanguageStrings.DYNASTY_SORT_POP_LOW),
                LanguageStrings.get(LanguageStrings.DYNASTY_SORT_AGE_OLD),
                LanguageStrings.get(LanguageStrings.DYNASTY_SORT_AGE_NEW),
                LanguageStrings.get(LanguageStrings.DYNASTY_SORT_LOYALTY_HIGH),
                LanguageStrings.get(LanguageStrings.DYNASTY_SORT_LOYALTY_LOW),
                LanguageStrings.get(LanguageStrings.DYNASTY_SORT_MILITARY_HIGH),
                LanguageStrings.get(LanguageStrings.DYNASTY_SORT_MILITARY_LOW)
            });
            this.sortCombo.setFocusable(false);
            AssetStyles.styleComboBox(this.sortCombo);
            this.sortCombo.addActionListener(e -> updateSorter());
            this.searchBar = new UiNameSearchBar(
                    LanguageStrings.get(LanguageStrings.DYNASTY_SEARCH),
                    LanguageStrings.get(LanguageStrings.COLONY_SEARCH_TOOLTIP),
                    this::updateData);
            
            initUI();
        }

        public boolean isShowAutomation() {
            return showAutomation;
        }

        public boolean isShowAutoBuild() {
            return showAutoBuild;
        }

        public boolean isShowAutoTunnels() {
            return showAutoTunnels;
        }

        public boolean isShowAutoDiplomacy() {
            return showAutoDiplomacy;
        }
        
        private void updateSorter() {
            int idx = sortCombo.getSelectedIndex();
            switch (idx) {
                case 0: currentSorter = Comparator.comparingInt(Colony::getAntTotal).reversed(); break;
                case 1: currentSorter = Comparator.comparingInt(Colony::getAntTotal); break;
                case 2: currentSorter = Comparator.comparingInt(Colony::getAge).reversed(); break;
                case 3: currentSorter = Comparator.comparingInt(Colony::getAge); break;
                case 4: currentSorter = Comparator.comparingInt(this::effectiveLoyaltyScore).reversed(); break;
                case 5: currentSorter = Comparator.comparingInt(this::effectiveLoyaltyScore); break;
                case 6: currentSorter = Comparator.comparingInt(Colony::getMilitaryPower).reversed(); break;
                case 7: currentSorter = Comparator.comparingInt(Colony::getMilitaryPower); break;
                default: currentSorter = Comparator.comparingInt(Colony::getAntTotal).reversed(); break;
            }
            updateData();
        }

        private int effectiveLoyaltyScore(Colony colony) {
            return colony.getEffectiveLoyalty(engine.getTradeManager(), engine.getWorld());
        }

        private void initUI() {
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
            topPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
            topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, AssetStyles.BORDER_THICKNESS_INTERNAL, 0, AssetStyles.BORDER_COLOR));
            
            JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            sortPanel.setOpaque(false);
            JLabel sortLabel = new JLabel(LanguageStrings.get(LanguageStrings.DYNASTY_SORT_BY));
            sortLabel.setForeground(AssetStyles.FONT_COLOR);
            sortPanel.add(sortLabel);
            sortPanel.add(sortCombo);
            topPanel.add(sortPanel);
            topPanel.add(searchBar);
            
            topPanel.add(Box.createHorizontalStrut(20));

            if (showAutoBuild) {
                defaultAutoBuildCheck = new JCheckBox(LanguageStrings.get(LanguageStrings.DYNASTY_DEFAULT_AUTO_BUILD));
                AssetStyles.styleCheckBox(defaultAutoBuildCheck);
                defaultAutoBuildCheck.setFocusable(false);
                defaultAutoBuildCheck.setOpaque(false);
                defaultAutoBuildCheck.setToolTipText(LanguageStrings.get(LanguageStrings.DYNASTY_DEFAULT_AUTO_BUILD_TOOLTIP));
                defaultAutoBuildCheck.setSelected(dynasty.isDefaultAutoBuildEnabled());
                defaultAutoBuildCheck.addActionListener(e -> dynasty.setDefaultAutoBuildEnabled(defaultAutoBuildCheck.isSelected()));
                topPanel.add(defaultAutoBuildCheck);
            }
            
            if (showAutomation) {
                defaultAutomationCheck = new JCheckBox(LanguageStrings.get(LanguageStrings.DYNASTY_DEFAULT_AUTOMATION));
                AssetStyles.styleCheckBox(defaultAutomationCheck);
                defaultAutomationCheck.setFocusable(false);
                defaultAutomationCheck.setOpaque(false);
                defaultAutomationCheck.setToolTipText(LanguageStrings.get(LanguageStrings.DYNASTY_DEFAULT_AUTOMATION_TOOLTIP));
                defaultAutomationCheck.setSelected(dynasty.isDefaultAutomationEnabled());
                defaultAutomationCheck.addActionListener(e -> dynasty.setDefaultAutomationEnabled(defaultAutomationCheck.isSelected()));
                topPanel.add(defaultAutomationCheck);
            }

            if (showAutoTunnels) {
                defaultAutoTunnelsCheck = new JCheckBox(LanguageStrings.get(LanguageStrings.DYNASTY_DEFAULT_AUTO_TUNNELS));
                AssetStyles.styleCheckBox(defaultAutoTunnelsCheck);
                defaultAutoTunnelsCheck.setFocusable(false);
                defaultAutoTunnelsCheck.setOpaque(false);
                defaultAutoTunnelsCheck.setToolTipText(LanguageStrings.get(LanguageStrings.DYNASTY_DEFAULT_AUTO_TUNNELS_TOOLTIP));
                defaultAutoTunnelsCheck.setSelected(dynasty.isDefaultAutoTunnelsEnabled());
                defaultAutoTunnelsCheck.addActionListener(e -> dynasty.setDefaultAutoTunnelsEnabled(defaultAutoTunnelsCheck.isSelected()));
                topPanel.add(defaultAutoTunnelsCheck);
            }

            if (showAutoDiplomacy) {
                autoDiplomacyCheck = new JCheckBox(LanguageStrings.get(LanguageStrings.DYNASTY_AUTO_DIPLOMACY));
                AssetStyles.styleCheckBox(autoDiplomacyCheck);
                autoDiplomacyCheck.setFocusable(false);
                autoDiplomacyCheck.setOpaque(false);
                autoDiplomacyCheck.setToolTipText(LanguageStrings.get(LanguageStrings.DYNASTY_AUTO_DIPLOMACY_TOOLTIP));
                autoDiplomacyCheck.setSelected(dynasty.isAutoDiplomacyEnabled());
                autoDiplomacyCheck.addActionListener(e -> dynasty.setAutoDiplomacyEnabled(autoDiplomacyCheck.isSelected()));
                topPanel.add(autoDiplomacyCheck);
            }
            
            add(topPanel, BorderLayout.NORTH);

            List<String> cols = new ArrayList<>(Arrays.asList("", LanguageStrings.get(LanguageStrings.COLONY_RANK), LanguageStrings.get(LanguageStrings.COL_TYPE), LanguageStrings.get(LanguageStrings.PANEL_COLONY), LanguageStrings.get(LanguageStrings.STATS_TAB_POPULATION), LanguageStrings.get(LanguageStrings.STAT_AGE), LanguageStrings.get(LanguageStrings.STATS_TAB_LOCAL_HEX)));
            loyaltyCol = cols.size();
            cols.add(LanguageStrings.get(LanguageStrings.STAT_LOYALTY));
            militaryCol = cols.size();
            cols.add(LanguageStrings.get(LanguageStrings.STAT_MILITARY_POWER));
            
            if (showAutoBuild) {
                autoBuildCol = cols.size();
                cols.add(LanguageStrings.get(LanguageStrings.STAT_AUTO_BUILD));
            }
            if (showAutomation) {
                automationCol = cols.size();
                cols.add(LanguageStrings.get(LanguageStrings.STAT_AUTOMATION));
            }
            if (showAutoTunnels) {
                autoTunnelsCol = cols.size();
                cols.add(LanguageStrings.get(LanguageStrings.STAT_AUTO_TUNNELS));
            }
            
            actionCol = cols.size();
            cols.add(LanguageStrings.get(LanguageStrings.DYNASTY_ACTIONS));

            model = new DefaultTableModel(cols.toArray(new String[0]), 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 0) return Icon.class;
                    if (columnIndex == 4) return Integer.class;
                    if (columnIndex == 6) return String.class;
                    if (autoBuildCol != -1 && columnIndex == autoBuildCol) return Boolean.class;
                    if (automationCol != -1 && columnIndex == automationCol) return Boolean.class;
                    if (autoTunnelsCol != -1 && columnIndex == autoTunnelsCol) return Boolean.class;
                    return Object.class;
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == autoBuildCol || column == automationCol || column == autoTunnelsCol || column == actionCol;
                }
            };

            model.addTableModelListener(e -> {
                int col = e.getColumn();
                int row = e.getFirstRow();
                if (col < 0) return;
                if (row >= 0 && row < displayedColonies.size()) {
                    Colony c = displayedColonies.get(row);
                    if (c != null) {
                        if (col == autoBuildCol) c.setAutoBuildEnabled((Boolean) model.getValueAt(row, col));
                        else if (col == automationCol) c.setAutomationEnabled((Boolean) model.getValueAt(row, col));
                        else if (col == autoTunnelsCol) c.setAutoTunnelsEnabled((Boolean) model.getValueAt(row, col));
                    }
                }
            });

            table = new JTable(model);
            table.setRowHeight(45);
            AssetStyles.applyScrollableDialogTable(table);
            table.setFocusable(false);
            
            AssetStyles.applyTableHeaderAlignment(table, 0, SwingConstants.CENTER);
            AssetStyles.applyTableColumnAlignment(table, 1, SwingConstants.LEFT);
            AssetStyles.applyTableHeaderAlignment(table, 2, SwingConstants.CENTER);
            AssetStyles.applyTableColumnAlignment(table, 3, SwingConstants.LEFT);
            AssetStyles.applyTableHeaderAlignment(table, 4, SwingConstants.RIGHT);
            AssetStyles.applyTableHeaderAlignment(table, 5, SwingConstants.CENTER);
            AssetStyles.applyTableColumnAlignment(table, 6, SwingConstants.LEFT);
            
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            
            DefaultTableCellRenderer paddedRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
                    return this;
                }
            };
            
            table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(4).setCellRenderer(UiTableStyles.createNumericCellRenderer());
            table.getColumnModel().getColumn(5).setCellRenderer(paddedRenderer);
            table.getColumnModel().getColumn(6).setCellRenderer(new OverviewBiomeRenderer());
            table.getColumnModel().getColumn(loyaltyCol).setCellRenderer(new LoyaltyCellRenderer());
            AssetStyles.applyTableHeaderAlignment(table, loyaltyCol, SwingConstants.CENTER);
            table.getColumnModel().getColumn(militaryCol).setCellRenderer(new MilitaryPowerCellRenderer());
            AssetStyles.applyTableHeaderAlignment(table, militaryCol, SwingConstants.CENTER);

            table.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if (row >= 0 && row < displayedColonies.size() && col == loyaltyCol) {
                        Colony colony = displayedColonies.get(row);
                        table.setToolTipText(colony.buildLoyaltyModifierTooltip(
                                engine.getTradeManager(), engine.getWorld()));
                        return;
                    }
                    if (row >= 0 && row < displayedColonies.size() && col == militaryCol) {
                        table.setToolTipText(LanguageStrings.get(LanguageStrings.STAT_MILITARY_POWER_DESC));
                        return;
                    }
                    table.setToolTipText(null);
                }
            });

            if (showAutoBuild) {
                table.getColumnModel().getColumn(autoBuildCol).setMaxWidth(100);
                AssetStyles.styleTableBooleanColumn(table, autoBuildCol);
            }
            if (showAutomation) {
                table.getColumnModel().getColumn(automationCol).setMaxWidth(100);
                AssetStyles.styleTableBooleanColumn(table, automationCol);
            }
            if (showAutoTunnels) {
                table.getColumnModel().getColumn(autoTunnelsCol).setMaxWidth(100);
                AssetStyles.styleTableBooleanColumn(table, autoTunnelsCol);
            }

            table.getColumnModel().getColumn(actionCol).setCellRenderer(new ActionPanelRenderer());
            table.getColumnModel().getColumn(actionCol).setCellEditor(new ActionPanelEditor());
            AssetStyles.applyTableHeaderAlignment(table, actionCol, SwingConstants.CENTER);

            tableScrollPane = AssetStyles.wrapScrollableTable(table);
            add(tableScrollPane, BorderLayout.CENTER);
        }

        @Override
        public void liveUpdate() {
             updateData();
        }

        @Override
        public void updateData() {
            if (table.isEditing()) {
                return;
            }
            int selectedRow = table.getSelectedRow();
            model.setRowCount(0);
            displayedColonies.clear();

            List<Colony> rawColonies = dynasty.getColonies();
            World world = engine.getWorld();

            for (Colony colony : rawColonies) {
                if (colony != null && searchBar.matchesAny(colony.getName())) {
                    displayedColonies.add(colony);
                }
            }
            if (currentSorter != null) {
                displayedColonies.sort(currentSorter);
            }

            for (Colony colony : displayedColonies) {
                Biome biome = null;
                if (world != null) {
                    for (Hex hex : world.getHexes()) {
                        if (hex.getColony() == colony) {
                            biome = hex.getBiome();
                            break;
                        }
                    }
                }
                
                Object[] rowData = new Object[model.getColumnCount()];
                rowData[0] = colony.getRank().getIcon();
                rowData[1] = colony.getRank().getName();
                rowData[2] = colony.isCapital() ? LanguageStrings.get(LanguageStrings.STAT_CAPITAL) : LanguageStrings.get(LanguageStrings.DYNASTY_SATELLITE);
                rowData[3] = (colony.isCapital() ? "★ " : "") + colony.getName();
                rowData[4] = colony.getAntTotal();
                rowData[5] = colony.getAge();
                rowData[6] = biome != null ? biome.getName() : LanguageStrings.get(LanguageStrings.STAT_UNKNOWN);
                int effectiveLoyalty = colony.getEffectiveLoyalty(engine.getTradeManager(), engine.getWorld());
                ColonyLoyalty loyaltyTier = GameConstants.getColonyLoyaltyLevel(effectiveLoyalty);
                rowData[loyaltyCol] = String.format(
                        LanguageStrings.get(LanguageStrings.SCORE_TIER_FORMAT),
                        effectiveLoyalty,
                        loyaltyTier.getName());
                rowData[militaryCol] = colony.getMilitaryPower();
                if (showAutoBuild) rowData[autoBuildCol] = colony.isAutoBuildEnabled();
                if (showAutomation) rowData[automationCol] = colony.isAutomationEnabled();
                if (showAutoTunnels) rowData[autoTunnelsCol] = colony.isAutoTunnelsEnabled();
                rowData[actionCol] = colony;
                model.addRow(rowData);
            }
            if (selectedRow >= 0 && selectedRow < table.getRowCount()) table.setRowSelectionInterval(selectedRow, selectedRow);
            boolean[] growable = new boolean[model.getColumnCount()];
            growable[3] = true;
            growable[loyaltyCol] = true;
            growable[actionCol] = true;
            AssetStyles.relayoutTableInScrollPane(tableScrollPane, growable);
        }

        private class MilitaryPowerCellRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                int power = value instanceof Number number ? number.intValue() : 0;
                setText(AssetStyles.formatNumber(power));
                setIcon(GameConstants.ICON_STAT_MILITARY_POWER);
                setIconTextGap(6);
                return this;
            }
        }

        private class LoyaltyCellRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                if (row >= 0 && row < displayedColonies.size()) {
                    Colony colony = displayedColonies.get(row);
                    int effectiveLoyalty = colony.getEffectiveLoyalty(engine.getTradeManager(), engine.getWorld());
                    ColonyLoyalty loyaltyTier = GameConstants.getColonyLoyaltyLevel(effectiveLoyalty);
                    setText(String.format(
                            LanguageStrings.get(LanguageStrings.SCORE_TIER_FORMAT),
                            effectiveLoyalty,
                            loyaltyTier.getName()));
                    setIcon(loyaltyTier.getIcon());
                    setIconTextGap(6);
                } else {
                    setIcon(null);
                }
                return this;
            }
        }

        private class OverviewBiomeRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setText(value != null ? value.toString() : LanguageStrings.get(LanguageStrings.STAT_UNKNOWN));
                setIcon(null);
                if (row >= 0 && row < displayedColonies.size()) {
                    Colony colony = displayedColonies.get(row);
                    World world = engine.getWorld();
                    if (colony != null && world != null) {
                        Hex hex = world.getHexOfColony(colony);
                        if (hex != null && hex.getBiome() != null) {
                            setIcon(hex.getBiome().getIcon());
                            setIconTextGap(8);
                        }
                    }
                }
                return this;
            }
        }
    }

    private static class ActionPanelRenderer extends JPanel implements TableCellRenderer {
        private final JButton actionsBtn = new JButton(LanguageStrings.get(LanguageStrings.DYNASTY_ACTIONS));
        private final JProgressBar progressBar = new JProgressBar(0, 7);
        private final JLabel statusLabel = new JLabel(LanguageStrings.get(LanguageStrings.UI_MATURING), SwingConstants.CENTER);

        public ActionPanelRenderer() {
            setLayout(new CardLayout());
            setOpaque(true);
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            btnPanel.setOpaque(false);
            actionsBtn.setFocusable(false);
            AssetStyles.styleCompactButton(actionsBtn);
            btnPanel.add(actionsBtn);
            JPanel progressPanel = new JPanel(new BorderLayout());
            progressPanel.setOpaque(false);
            progressBar.setStringPainted(true);
            AssetStyles.styleProgressBar(progressBar);
            statusLabel.setFont(AssetStyles.FONT_SMALL);
            statusLabel.setForeground(AssetStyles.FONT_COLOR);
            progressPanel.add(statusLabel, BorderLayout.NORTH);
            progressPanel.add(progressBar, BorderLayout.CENTER);
            add(btnPanel, "BUTTONS");
            add(progressPanel, "PROGRESS");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            AssetStyles.styleCompactButton(actionsBtn);
            if (value instanceof Colony) {
                Colony c = (Colony) value;
                CardLayout cl = (CardLayout) getLayout();
                if (c.getAge() >= 7) cl.show(this, "BUTTONS");
                else {
                    cl.show(this, "PROGRESS");
                    progressBar.setValue(c.getAge());
                    progressBar.setString(LanguageStrings.format(LanguageStrings.DYNASTY_7_DAYS, c.getAge()));
                }
            }
            return this;
        }
    }

    private class ActionPanelEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel container;
        private final JButton actionsBtn = new JButton(LanguageStrings.get(LanguageStrings.DYNASTY_ACTIONS));
        private final JProgressBar progressBar = new JProgressBar(0, 7);
        private final JLabel statusLabel = new JLabel(LanguageStrings.get(LanguageStrings.UI_MATURING), SwingConstants.CENTER);
        private Colony currentColony;
        private JTable editingTable;
        private int editingRow = -1;
        private int editingCol = -1;
        private final CardLayout cardLayout = new CardLayout();

        public ActionPanelEditor() {
            container = new JPanel(cardLayout);
            container.setOpaque(true);
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            btnPanel.setOpaque(false);
            actionsBtn.setFocusable(false);
            AssetStyles.styleCompactButton(actionsBtn);
            actionsBtn.addActionListener(e -> {
                JTable tableRef = editingTable;
                int row = editingRow;
                int col = editingCol;
                Colony colony = currentColony;
                fireEditingStopped();
                showColonyOverviewActionsMenu(colony, tableRef, row, col);
            });
            btnPanel.add(actionsBtn);
            JPanel progressPanel = new JPanel(new BorderLayout());
            progressPanel.setOpaque(false);
            progressBar.setStringPainted(true);
            AssetStyles.styleProgressBar(progressBar);
            statusLabel.setFont(AssetStyles.FONT_SMALL);
            statusLabel.setForeground(AssetStyles.FONT_COLOR);
            progressPanel.add(statusLabel, BorderLayout.NORTH);
            progressPanel.add(progressBar, BorderLayout.CENTER);
            container.add(btnPanel, "BUTTONS");
            container.add(progressPanel, "PROGRESS");
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            editingTable = table;
            editingRow = row;
            editingCol = column;
            if (value instanceof Colony) {
                this.currentColony = (Colony) value;
                container.setBackground(table.getSelectionBackground());
                if (currentColony.getAge() >= 7) cardLayout.show(container, "BUTTONS");
                else {
                    cardLayout.show(container, "PROGRESS");
                    progressBar.setValue(currentColony.getAge());
                    progressBar.setString(LanguageStrings.format(LanguageStrings.DYNASTY_7_DAYS, currentColony.getAge()));
                }
            }
            return container;
        }
        @Override
        public Object getCellEditorValue() { return currentColony; }
    }

    private void showColonyOverviewActionsMenu(Colony colony, JTable table, int row, int column) {
        if (colony == null) {
            return;
        }
        JPopupMenu menu = new JPopupMenu();

        JMenuItem renameItem = new JMenuItem(LanguageStrings.get(LanguageStrings.UI_EDIT));
        renameItem.addActionListener(e -> performEdit(colony));
        AssetStyles.styleMenuItem(renameItem);
        menu.add(renameItem);

        JMenuItem viewItem = new JMenuItem(LanguageStrings.get(LanguageStrings.UI_VIEW));
        viewItem.addActionListener(e -> performView(colony));
        AssetStyles.styleMenuItem(viewItem);
        menu.add(viewItem);

        DynastyDiplomacyService diplo = dynasty.getDiplomacyService();
        if (diplo != null) {
            JMenuItem diplomatItem = new JMenuItem(LanguageStrings.get(LanguageStrings.DIPLO_ACTION_SEND_DIPLOMATS));
            if (diplo.canManageColonyMissionDiplomats(colony, engine.getTradeManager(), engine.getWorld())) {
                diplomatItem.addActionListener(e -> promptManageColonyDiplomats(this, colony));
            } else {
                diplomatItem.setEnabled(false);
                if (!dynasty.hasUpgrade(GameUnlocks.ROLE_DIPLOMAT)) {
                    diplomatItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NO_DIPLOMAT_ROLE));
                } else if (diplo.countDynastyWideAvailableDiplomats() <= 0
                        && diplo.countColonyMissionDiplomatsOn(colony) <= 0) {
                    diplomatItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NO_DIPLOMATS));
                } else if (!diplo.needsDiplomatMissionToColony(colony, engine.getTradeManager(), engine.getWorld())) {
                    diplomatItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_LOYALTY_STABLE));
                }
            }
            AssetStyles.styleMenuItem(diplomatItem);
            menu.add(diplomatItem);
        }

        UiTableStyles.showCellPopupMenu(menu, table, row, column);
    }

    private void promptManageColonyDiplomats(Component parent, Colony colony) {
        DynastyDiplomacyService diplo = dynasty.getDiplomacyService();
        if (diplo == null || colony == null) {
            return;
        }
        if (!dynasty.hasUpgrade(GameUnlocks.ROLE_DIPLOMAT)) {
            UiOptionPane.showMessageDialog(parent,
                    LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NO_DIPLOMAT_ROLE),
                    LanguageStrings.get(LanguageStrings.DIPLO_SEND_DIPLOMATS_TITLE),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int current = diplo.countColonyMissionDiplomatsOn(colony);
        int max = diplo.computeMaxColonyMissionDiplomatsOn(colony);
        int available = diplo.countDynastyWideAvailableDiplomats();
        int gainPer = diplo.getDiplomatStabilityGainPerAnt();
        Integer selected = promptManageDiplomatCount(parent, colony.getName(), current, max, available, gainPer);
        if (selected == null) {
            return;
        }
        diplo.assignColonyMissionDiplomats(colony, selected, engine.getTradeManager(), engine.getWorld());
        refreshDialog();
    }

    private void promptManageDynastyDiplomats(Component parent, Dynasty other) {
        DynastyDiplomacyService diplo = dynasty.getDiplomacyService();
        World world = engine.getWorld();
        if (diplo == null || other == null || world == null) {
            return;
        }
        if (!dynasty.hasUpgrade(GameUnlocks.ROLE_DIPLOMAT)) {
            UiOptionPane.showMessageDialog(parent,
                    LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NO_DIPLOMAT_ROLE),
                    LanguageStrings.get(LanguageStrings.DIPLO_SEND_DIPLOMATS_TITLE),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int current = diplo.countDynastyMissionDiplomatsToward(other);
        int max = diplo.computeMaxDynastyMissionDiplomatsToward(other);
        int available = diplo.countDynastyWideAvailableDiplomats();
        int gainPer = diplo.getDiplomatStabilityGainPerAnt();
        Integer selected = promptManageDiplomatCount(parent, other.getName(), current, max, available, gainPer);
        if (selected == null) {
            return;
        }
        diplo.assignDynastyMissionDiplomats(other, selected, world);
        refreshDialog();
    }

    private static final Dimension DIPLOMAT_ASSIGN_DIALOG_SIZE = new Dimension(520, 320);
    private static final int DIPLOMAT_ASSIGN_CONTENT_WIDTH_PX = 460;

    private Integer promptManageDiplomatCount(Component parent, String targetName, int current, int maxCount,
            int available, int gainPer) {
        int perTargetLimit = dynasty.getDiplomacyService() != null
                ? dynasty.getDiplomacyService().getMaxDiplomatsPerTarget()
                : GameNumbers.DIPLOMAT_MAX_PER_DYNASTY_MISSION;
        int spinnerMax = Math.max(0, maxCount);
        if (current > perTargetLimit) {
            spinnerMax = Math.max(spinnerMax, current);
        }
        int spinnerInitial = Math.min(current, spinnerMax);
        int maxButtonValue = current > perTargetLimit
                ? Math.min(current, Math.max(maxCount, perTargetLimit))
                : spinnerMax;

        Window owner = parent instanceof Window window ? window : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner,
                LanguageStrings.get(LanguageStrings.DIPLO_SEND_DIPLOMATS_TITLE),
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(AssetStyles.BACKGROUND_COLOR);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 30, 16, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 8, 0);

        int row = 0;
        gbc.gridy = row++;
        panel.add(diplomatCenteredLine(targetName, true), gbc);

        gbc.insets = new Insets(0, 0, 6, 0);
        gbc.gridy = row++;
        panel.add(diplomatCenteredLine(LanguageStrings.format(
                LanguageStrings.DIPLO_DIPLOMATS_CURRENT_FMT, AssetStyles.formatNumber(current)), false), gbc);

        gbc.gridy = row++;
        panel.add(diplomatCenteredLine(LanguageStrings.format(
                LanguageStrings.DIPLO_DIPLOMATS_AVAILABLE_FMT, AssetStyles.formatNumber(available)), false), gbc);

        gbc.insets = new Insets(12, 0, 12, 0);
        gbc.gridy = row++;
        gbc.fill = GridBagConstraints.NONE;
        JPanel spinnerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        spinnerRow.setOpaque(false);
        JLabel countLabel = new JLabel(LanguageStrings.get(LanguageStrings.DIPLO_DIPLOMATS_TARGET_COUNT));
        countLabel.setFont(AssetStyles.FONT_NORMAL);
        countLabel.setForeground(AssetStyles.FONT_COLOR);
        spinnerRow.add(countLabel);

        SpinnerNumberModel model = new SpinnerNumberModel(spinnerInitial, 0, spinnerMax, 1);
        JSpinner spinner = new JSpinner(model);
        spinner.setFocusable(false);
        AssetStyles.styleSpinner(spinner);
        spinnerRow.add(spinner);

        JButton maxBtn = new JButton(LanguageStrings.get(LanguageStrings.UI_MAX));
        maxBtn.setFocusable(false);
        AssetStyles.styleCompactButton(maxBtn);
        maxBtn.addActionListener(e -> spinner.setValue(maxButtonValue));
        spinnerRow.add(maxBtn);
        panel.add(spinnerRow, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridy = row;
        JLabel previewLabel = new JLabel("");
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewLabel.setFont(AssetStyles.FONT_NORMAL);
        previewLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(diplomatLinePanel(previewLabel), gbc);

        Runnable updatePreview = () -> previewLabel.setText(LanguageStrings.format(
                LanguageStrings.DIPLO_DIPLOMATS_BONUS_PREVIEW_FMT,
                AssetStyles.formatNumber(((Number) spinner.getValue()).intValue() * gainPer),
                formatDiplomatPreviewDelta(
                        (((Number) spinner.getValue()).intValue() - current) * gainPer)));
        spinner.addChangeListener(e -> updatePreview.run());
        updatePreview.run();

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(true);
        wrapper.setBackground(AssetStyles.BACKGROUND_COLOR);
        GridBagConstraints wrapGbc = new GridBagConstraints();
        wrapGbc.gridx = 0;
        wrapGbc.gridy = 0;
        wrapGbc.weightx = 1.0;
        wrapGbc.weighty = 1.0;
        wrapGbc.anchor = GridBagConstraints.CENTER;
        wrapper.add(panel, wrapGbc);
        dialog.add(wrapper, BorderLayout.CENTER);

        final Integer[] result = {null};
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        buttons.setOpaque(false);
        buttons.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        JButton cancelBtn = new JButton(LanguageStrings.get(LanguageStrings.UI_CANCEL));
        cancelBtn.setFocusable(false);
        AssetStyles.styleButton(cancelBtn);
        cancelBtn.addActionListener(e -> {
            result[0] = null;
            dialog.dispose();
        });
        JButton okBtn = new JButton(LanguageStrings.get(LanguageStrings.UI_CONFIRM));
        okBtn.setFocusable(false);
        AssetStyles.styleButton(okBtn);
        okBtn.addActionListener(e -> {
            result[0] = ((Number) spinner.getValue()).intValue();
            dialog.dispose();
        });
        buttons.add(cancelBtn);
        buttons.add(okBtn);
        dialog.add(buttons, BorderLayout.SOUTH);

        dialog.setSize(DIPLOMAT_ASSIGN_DIALOG_SIZE);
        dialog.setMinimumSize(DIPLOMAT_ASSIGN_DIALOG_SIZE);
        UiDialogUtils.prepareDialog(dialog, parent);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }

    private static JComponent diplomatCenteredLine(String text, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(bold ? AssetStyles.FONT_BOLD : AssetStyles.FONT_NORMAL);
        label.setForeground(AssetStyles.FONT_COLOR);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return diplomatLinePanel(label);
    }

    private static JPanel diplomatLinePanel(JLabel label) {
        JPanel line = new JPanel(new BorderLayout());
        line.setOpaque(false);
        line.setPreferredSize(new Dimension(DIPLOMAT_ASSIGN_CONTENT_WIDTH_PX, label.getPreferredSize().height));
        line.add(label, BorderLayout.CENTER);
        return line;
    }

    private static String formatDiplomatPreviewDelta(int delta) {
        if (delta > 0) {
            return "+" + AssetStyles.formatNumber(delta);
        }
        return AssetStyles.formatNumber(delta);
    }

    private void performGeneticExchangeAction(Dynasty other) {
        DynastyDiplomacyService diplo = dynasty.getDiplomacyService();
        World world = engine.getWorld();
        if (diplo == null || other == null || world == null) {
            return;
        }
        if (!diplo.canOfferGeneticExchange(other, world)) {
            return;
        }
        int confirmed = UiOptionPane.showConfirmDialog(this,
                LanguageStrings.format(LanguageStrings.LOG_GENETIC_EXCHANGE_FMT, other.getName()),
                LanguageStrings.get(LanguageStrings.DIPLO_ACTION_GENETIC_EXCHANGE),
                JOptionPane.YES_NO_OPTION);
        if (confirmed != JOptionPane.YES_OPTION) {
            return;
        }
        if (diplo.offerGeneticExchange(other, world)) {
            refreshDialog();
        }
    }

    private int promptIntegrationDiplomatCount(Component parent, int maxCount, int current) {
        if (maxCount < GameNumbers.INTEGRATION_MIN_DIPLOMATS) {
            return -1;
        }
        String prompt = LanguageStrings.format(
                LanguageStrings.DIPLO_INTEGRATION_DIPLOMATS_PROMPT,
                AssetStyles.formatNumber(maxCount));
        int initial = Math.max(GameNumbers.INTEGRATION_MIN_DIPLOMATS, Math.min(current, maxCount));
        SpinnerNumberModel model = new SpinnerNumberModel(
                initial, GameNumbers.INTEGRATION_MIN_DIPLOMATS, maxCount, 1);
        JSpinner spinner = new JSpinner(model);
        AssetStyles.styleSpinner(spinner);
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JLabel label = new JLabel(prompt);
        label.setFont(AssetStyles.FONT_NORMAL);
        panel.add(label, BorderLayout.NORTH);
        panel.add(spinner, BorderLayout.CENTER);
        int result = UiOptionPane.showConfirmDialog(parent, panel,
                LanguageStrings.get(LanguageStrings.DIPLO_ACTION_MANAGE_INTEGRATION_DIPLOMATS),
                JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return -1;
        }
        return ((Number) spinner.getValue()).intValue();
    }

    private void performEdit(Colony colony) {
        if (colony == null) {
            return;
        }
        String newName = UiOptionPane.showInputDialog(this,
                LanguageStrings.format(LanguageStrings.DYNASTY_RENAME_TITLE, colony.getName()), colony.getName());
        if (newName != null && !newName.trim().isEmpty()) {
            colony.setName(newName.trim());
            refreshDialog();
        }
    }

    private void performView(Colony colony) {
        if (colony == null) return;
        if (onGoToColony != null) {
            onGoToColony.accept(colony);
            dispose();
        }
    }
}
