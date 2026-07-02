package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ColonyLoyalty;
import com.grimidk.formicempire.classes.constants.misc.DiplomaticReputation;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.misc.TradeMethod;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Trade;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.entities.services.DynastyDiplomacyService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiDialogUtils;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiOptionPane;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
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

    private final Dynasty dynasty;
    private final Engine engine;
    private final Consumer<Colony> onGoToColony;

    private final JTabbedPane tabbedPane;
    private final Map<Integer, Integer> tabIndexMap = new HashMap<>();
    
    private OverviewPanel overviewPanel;
    private TradePanel tradePanel;
    private DiplomacyPanel diplomacyPanel;

    private final Runnable refreshTask = this::liveUpdate;

    public DynastyManagementDialog(JFrame owner, Dynasty dynasty, Engine engine, Consumer<Colony> onGoToColony) {
        super(owner, LanguageStrings.DIALOG_DYNASTY_TITLE, AssetStyles.DEFAULT_DIALOG_SIZE);
        this.dynasty = dynasty;
        this.engine = engine;
        this.onGoToColony = onGoToColony;
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
        
        if (this.engine != null) {
            this.engine.addHourTickListener(refreshTask);
        }

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                tabbedPane.requestFocusInWindow();
            }
            @Override
            public void windowClosed(WindowEvent e) {
                detachTickListener();
            }
            @Override
            public void windowClosing(WindowEvent e) {
                detachTickListener();
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

    private void detachTickListener() {
        if (engine != null) {
            engine.removeHourTickListener(refreshTask);
        }
    }

    @Override
    public void dispose() {
        detachTickListener();
        super.dispose();
    }

    public void liveUpdate() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::liveUpdate);
            return;
        }

        if (!isShowing()) return;

        boolean hasTrade = dynasty.hasUpgrade(GameUnlocks.ABILITY_TRADE);
        int expectedTabs = 2 + (hasTrade ? 1 : 0);        
        boolean currentAuto = dynasty.hasUpgrade(GameUnlocks.ABILITY_AUTOMATION);
        boolean currentAutoBuild = dynasty.hasUpgrade(GameUnlocks.ABILITY_MANAGEMENT);
        
        boolean panelAuto = (overviewPanel != null) && overviewPanel.isShowAutomation();
        boolean panelAutoBuild = (overviewPanel != null) && overviewPanel.isShowAutoBuild();

        if (tabbedPane.getTabCount() != expectedTabs || currentAuto != panelAuto || currentAutoBuild != panelAutoBuild) {
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
        
        if (overviewPanel == null || overviewPanel.isShowAutomation() != currentAuto || overviewPanel.isShowAutoBuild() != currentAutoBuild) {
            overviewPanel = new OverviewPanel(currentAutoBuild, currentAuto);
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

        if (selectedIndex < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(selectedIndex);
        }
    }

    private void initKeyBindings() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "toggleOverview");
        actionMap.put("toggleOverview", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isTabOpen(TAB_OVERVIEW)) {
                    dispose();
                } else if (tabIndexMap.containsKey(TAB_OVERVIEW)) {
                    tabbedPane.setSelectedIndex(tabIndexMap.get(TAB_OVERVIEW));
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "toggleTrade");
        actionMap.put("toggleTrade", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isTabOpen(TAB_TRADE)) {
                    dispose();
                } else if (tabIndexMap.containsKey(TAB_TRADE)) {
                    tabbedPane.setSelectedIndex(tabIndexMap.get(TAB_TRADE));
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "toggleDiplomacy");
        actionMap.put("toggleDiplomacy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isTabOpen(TAB_DIPLOMACY)) {
                    dispose();
                } else if (tabIndexMap.containsKey(TAB_DIPLOMACY)) {
                    tabbedPane.setSelectedIndex(tabIndexMap.get(TAB_DIPLOMACY));
                }
            }
        });
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
        TradeCreationDialog dialog = new TradeCreationDialog(
                SwingUtilities.getWindowAncestor(this), origin, target, engine, existingTrade);
        dialog.setVisible(true);
    }

    private void promptManageTradeRoute(Component parent, Colony activeColony, Trade trade) {
        Colony neighbor = trade.getDestination().getColony();
        Trade incoming = dynasty.getTradeService().findTrade(neighbor, activeColony);
        boolean canTwoWay = activeColony.hasUpgrade(GameUnlocks.ABILITY_BILATERAL_TRADE)
                && incoming != null
                && !(trade.hasPendingUpdate() ? trade.isPendingBilateral() : trade.isBilateral())
                && !(incoming.hasPendingUpdate() ? incoming.isPendingBilateral() : incoming.isBilateral());

        java.util.List<String> options = new java.util.ArrayList<>();
        options.add(LanguageStrings.get(LanguageStrings.DYNASTY_MODIFY));
        if (canTwoWay) {
            options.add(LanguageStrings.get(LanguageStrings.TRADE_MAKE_TWO_WAY));
        }
        options.add(LanguageStrings.get(LanguageStrings.DYNASTY_CANCEL_ROUTE));
        options.add(LanguageStrings.get(LanguageStrings.UI_CLOSE));

        int res = UiOptionPane.showOptionDialog(parent,
                LanguageStrings.format(LanguageStrings.DYNASTY_MANAGE_TRADE_MSG,                         trade.getDestination().getColony().getName()),
                LanguageStrings.get(LanguageStrings.DYNASTY_MANAGE_TRADE_TITLE),
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                options.toArray(), options.get(0));

        if (res == 0) {
            openTradeDialog(activeColony, trade.getDestination().getColony(), trade);
        } else if (canTwoWay && res == 1) {
            int confirm = UiOptionPane.showConfirmDialog(parent,
                    LanguageStrings.format(LanguageStrings.TRADE_OPTIMIZE_MSG, neighbor.getName()),
                    LanguageStrings.get(LanguageStrings.TRADE_MAKE_TWO_WAY),
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                Map<ResourceType, Double> load = trade.hasPendingUpdate() && trade.getPendingLoad() != null
                        ? new HashMap<>(trade.getPendingLoad())
                        : new HashMap<>(trade.getLoad());
                Map<ResourceType, Double> returnLoad = incoming.hasPendingUpdate() && incoming.getPendingLoad() != null
                        ? new HashMap<>(incoming.getPendingLoad())
                        : new HashMap<>(incoming.getLoad());
                Map<AntType, Integer> transport = trade.hasPendingUpdate() && trade.getPendingTransport() != null
                        ? new HashMap<>(trade.getPendingTransport())
                        : new HashMap<>(trade.getTransport());
                boolean recurrent = trade.hasPendingUpdate() ? trade.isPendingRecurrent() : trade.isRecurrent();
                TradeMethod method = trade.hasPendingUpdate() ? trade.getPendingMethod() : trade.getMethod();
                trade.setPendingUpdate(load, returnLoad, transport, recurrent, true, method);
                incoming.cancel();
                engine.getTradeManager().removeTrade(incoming);
            }
        } else if (res == (canTwoWay ? 2 : 1)) {
            trade.cancel();
            engine.getTradeManager().removeTrade(trade);
        }
    }

    private class TradePanel extends JPanel implements LiveUpdatePanel {
        private JTable table;
        private DefaultTableModel model;
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
            AssetStyles.styleDialogTable(table);

            TradeRouteStatusRenderer routeStatusRenderer = new TradeRouteStatusRenderer();
            int outCol = tunnelsVisible ? 3 : 2;
            int inCol = tunnelsVisible ? 4 : 3;
            table.getColumnModel().getColumn(outCol).setCellRenderer(routeStatusRenderer);
            table.getColumnModel().getColumn(inCol).setCellRenderer(routeStatusRenderer);
            
            if (tunnelsVisible) {
                table.getColumnModel().getColumn(2).setCellRenderer(new TunnelCellRenderer());
                table.getColumnModel().getColumn(2).setCellEditor(new TunnelCellEditor());
                table.getColumnModel().getColumn(5).setCellRenderer(new TradeActionRenderer());
                table.getColumnModel().getColumn(5).setCellEditor(new TradeActionEditor());
            } else {
                table.getColumnModel().getColumn(4).setCellRenderer(new TradeActionRenderer());
                table.getColumnModel().getColumn(4).setCellEditor(new TradeActionEditor());
            }

            add(new JScrollPane(table), BorderLayout.CENTER);
            
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

                TradeRowData rowData = new TradeRowData(neighborColony, neighborHex, outgoing, tunnel);

                if (tunnelsVisible) {
                    model.addRow(new Object[]{dirNames[i], neighborName, rowData, outStatus, inStatus, rowData});
                } else {
                    model.addRow(new Object[]{dirNames[i], neighborName, outStatus, inStatus, rowData});
                }
            }
            
            if (selectedRow >= 0 && selectedRow < table.getRowCount()) {
                table.setRowSelectionInterval(selectedRow, selectedRow);
            }
            int actionCol = table.getColumnCount() - 1;
            AssetStyles.fitTableColumn(table, 0, 72, 140);
            AssetStyles.fitTableColumn(table, 1, 96, 220);
            if (tunnelsVisible) {
                AssetStyles.fitTableColumn(table, 2, 120, 200);
            }
            AssetStyles.fitTableColumn(table, actionCol, 100, 140);
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
            final Tunnel tunnel;
            TradeRowData(Colony neighbor, Hex neighborHex, Trade outgoingTrade, Tunnel tunnel) { 
                this.neighbor = neighbor; 
                this.neighborHex = neighborHex;
                this.outgoingTrade = outgoingTrade; 
                this.tunnel = tunnel;
            }
        }

        private class TradeActionRenderer extends JPanel implements TableCellRenderer {
            private final JButton actionBtn = new JButton();
            private final JButton twoWayBtn = new JButton(LanguageStrings.get(LanguageStrings.TRADE_MAKE_TWO_WAY));
            public TradeActionRenderer() {
                setLayout(new FlowLayout(FlowLayout.CENTER, 4, 2));
                actionBtn.setFocusable(false);
                twoWayBtn.setFocusable(false);
                AssetStyles.styleCompactButton(actionBtn);
                AssetStyles.styleCompactButton(twoWayBtn);
                add(actionBtn);
                add(twoWayBtn);
            }
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setEnabled(true);
                twoWayBtn.setVisible(false);
                if (value instanceof TradeRowData) {
                    TradeRowData data = (TradeRowData) value;
                    if (data.neighbor == null) {
                        actionBtn.setText(LanguageStrings.get(LanguageStrings.UI_ESTABLISH));
                        actionBtn.setEnabled(false);
                    } else {
                        actionBtn.setText(data.outgoingTrade == null ? LanguageStrings.get(LanguageStrings.UI_ESTABLISH) : LanguageStrings.get(LanguageStrings.UI_MANAGE));
                        actionBtn.setEnabled(true);
                        twoWayBtn.setVisible(canMakeTwoWayTrade(data));
                    }
                }
                AssetStyles.styleCompactButton(actionBtn);
                AssetStyles.styleCompactButton(twoWayBtn);
                setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                return this;
            }
        }

        private class TradeActionEditor extends AbstractCellEditor implements TableCellEditor {
            private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
            private final JButton actionBtn = new JButton();
            private final JButton twoWayBtn = new JButton(LanguageStrings.get(LanguageStrings.TRADE_MAKE_TWO_WAY));
            private TradeRowData currentData;
            public TradeActionEditor() {
                actionBtn.setFocusable(false);
                twoWayBtn.setFocusable(false);
                AssetStyles.styleCompactButton(actionBtn);
                AssetStyles.styleCompactButton(twoWayBtn);
                panel.add(actionBtn);
                panel.add(twoWayBtn);
                actionBtn.addActionListener(e -> {
                    fireEditingStopped();
                    if (currentData == null || currentData.neighbor == null) return;
                    if (currentData.outgoingTrade == null) {
                        establishTrade(currentData.neighbor);
                    } else {
                        manageTrade(currentData.outgoingTrade);
                    }
                });
                twoWayBtn.addActionListener(e -> {
                    fireEditingStopped();
                    if (currentData != null) {
                        makeTwoWayTrade(currentData);
                    }
                });
            }
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                currentData = (TradeRowData) value;
                if (currentData != null) {
                    actionBtn.setText(currentData.outgoingTrade == null ? LanguageStrings.get(LanguageStrings.UI_ESTABLISH) : LanguageStrings.get(LanguageStrings.UI_MANAGE));
                    twoWayBtn.setVisible(canMakeTwoWayTrade(currentData));
                } else {
                    twoWayBtn.setVisible(false);
                }
                panel.setBackground(table.getSelectionBackground());
                AssetStyles.styleCompactButton(actionBtn);
                AssetStyles.styleCompactButton(twoWayBtn);
                return panel;
            }
            @Override
            public Object getCellEditorValue() { return currentData; }
        }

        private void establishTrade(Colony target) {
            openTradeDialog(activeColony, target, null);
            updateData();
        }

        private void manageTrade(Trade trade) {
            promptManageTradeRoute(this, activeColony, trade);
            updateData();
        }

        private void startTunnel(Hex targetHex) {
            Hex originHex = engine.getWorld().getHexOfColony(activeColony);
            Tunnel existing = dynasty.getTunnelBetween(originHex, targetHex);
            if (existing != null && !existing.isComplete()) {
                activeColony.setCurrentTunnelProject(existing);
            } else if (existing == null) {
                Tunnel tunnel = new Tunnel(originHex, targetHex, GameConstants.TUNNEL_WORK_REQUIRED);
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
            createBtn.addActionListener(e -> attemptCreate());
            
            optimizeBtn = new JButton(LanguageStrings.get(LanguageStrings.TRADE_OPTIMIZE));
            optimizeBtn.setFocusable(false);
            AssetStyles.styleCompactButton(optimizeBtn);
            optimizeBtn.setVisible(false);
            optimizeBtn.addActionListener(e -> performOptimization());
            
            JButton cancelBtn = new JButton(LanguageStrings.get(LanguageStrings.UI_CANCEL));
            cancelBtn.setFocusable(false);
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

            if (origin.hasUpgrade(GameUnlocks.ABILITY_BILATERAL_TRADE) && !crossDynasty && !bilateralCheck.isSelected()) {
                Trade incoming = findIncomingTrade();
                optimizeBtn.setVisible(incoming != null);
            } else {
                optimizeBtn.setVisible(false);
            }
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
                        origin.getDynasty().getDiplomacyService().onCrossDynastyTradeEstablished(target.getDynasty());
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
        private final List<Dynasty> displayedDynasties = new ArrayList<>();
        private static final int COL_DYNASTY = 0;
        private static final int COL_SPECIES = 1;
        private static final int COL_REPUTATION = 2;
        private static final int COL_STANCE = 3;
        private static final int COL_MILITARY = 4;
        private static final int COL_ACTIONS = 5;

        public DiplomacyPanel() {
            super(new BorderLayout());
            initUI();
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
                        return Species.class;
                    }
                    if (columnIndex == COL_STANCE) {
                        return DiplomaticReputation.class;
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
            AssetStyles.styleDialogTable(table);
            table.getColumnModel().getColumn(COL_DYNASTY).setPreferredWidth(200);
            table.getColumnModel().getColumn(COL_SPECIES).setCellRenderer(new SpeciesRenderer());
            table.getColumnModel().getColumn(COL_REPUTATION).setPreferredWidth(110);
            table.getColumnModel().getColumn(COL_REPUTATION).setCellRenderer(new ReputationScoreRenderer());
            table.getColumnModel().getColumn(COL_STANCE).setCellRenderer(new ReputationStanceRenderer());
            table.getColumnModel().getColumn(COL_MILITARY).setPreferredWidth(110);
            table.getColumnModel().getColumn(COL_MILITARY).setCellRenderer(new MilitaryPowerScoreRenderer());
            table.getColumnModel().getColumn(COL_ACTIONS).setMinWidth(280);
            table.getColumnModel().getColumn(COL_ACTIONS).setPreferredWidth(280);
            table.getColumnModel().getColumn(COL_ACTIONS).setCellRenderer(new DiplomacyActionRenderer());
            table.getColumnModel().getColumn(COL_ACTIONS).setCellEditor(new DiplomacyActionEditor());

            table.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if (row >= 0 && row < displayedDynasties.size()
                            && (col == COL_REPUTATION || col == COL_STANCE)) {
                        World world = engine.getWorld();
                        Dynasty other = displayedDynasties.get(row);
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

            add(new JScrollPane(table), BorderLayout.CENTER);
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

        private class ReputationScoreRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                if (row >= 0 && row < displayedDynasties.size()) {
                    World world = engine.getWorld();
                    Dynasty other = displayedDynasties.get(row);
                    int score = world != null && dynasty.getDiplomacyService() != null
                            ? dynasty.getDiplomacyService().getEffectiveDiplomaticReputation(other, world)
                            : 0;
                    setText(String.valueOf(score));
                    setIcon(GameConstants.ICON_STAT_REPUTATION);
                    setIconTextGap(6);
                } else {
                    setIcon(null);
                }
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
                if (other != dynasty) {
                    others.add(other);
                }
            }
            others.sort(Comparator
                    .comparingInt((Dynasty d) -> dynasty.getDiplomacyService().getEffectiveDiplomaticReputation(d, world)).reversed()
                    .thenComparing(Dynasty::getName, String.CASE_INSENSITIVE_ORDER));

            displayedDynasties.addAll(others);
            for (Dynasty other : displayedDynasties) {
                int score = dynasty.getDiplomacyService().getEffectiveDiplomaticReputation(other, world);
                DiplomaticReputation stance = GameConstants.getDiplomaticReputationLevel(score);
                model.addRow(new Object[]{
                        other.getName(),
                        other.getSpecies(),
                        String.valueOf(score),
                        stance,
                        other.getMilitaryPower(),
                        new DiplomacyRowData(other)
                });
            }

            if (selectedRow >= 0 && selectedRow < table.getRowCount()) {
                table.setRowSelectionInterval(selectedRow, selectedRow);
            }
            AssetStyles.fitTableColumn(table, COL_DYNASTY, 120, 260);
            AssetStyles.fitTableColumn(table, COL_REPUTATION, 72, 130);
            AssetStyles.fitTableColumn(table, COL_STANCE, 100, 200);
            AssetStyles.fitTableColumn(table, COL_MILITARY, 80, 140);
            AssetStyles.fitTableColumn(table, COL_ACTIONS, 300, 540);
        }

        private void performPactAction(Dynasty other) {
            if (other == null || dynasty.getDiplomacyService() == null) {
                return;
            }
            if (dynasty.getDiplomacyService().hasNonAggressionPact(other)) {
                dynasty.getDiplomacyService().breakNonAggressionPact(other);
            } else if (dynasty.getDiplomacyService().canFormNonAggressionPact(other)) {
                dynasty.getDiplomacyService().formNonAggressionPact(other);
            } else {
                UiOptionPane.showMessageDialog(this,
                        LanguageStrings.get(LanguageStrings.DIPLO_ERROR_CORDIAL_REQUIRED),
                        LanguageStrings.get(LanguageStrings.DIPLO_ACTION_FORM_PACT),
                        JOptionPane.WARNING_MESSAGE);
            }
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
            if (outgoing != null) {
                promptManageTradeRoute(this, activeColony, outgoing);
            } else if (dynasty.getDiplomacyService().canEstablishCrossDynastyTrade(
                    other, activeColony, world, engine.getTradeManager())) {
                openTradeDialog(activeColony, neighbor, null);
            } else {
                String message = LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NEUTRAL_REQUIRED);
                if (!DynastyDiplomacyService.meetsTradeLoyaltyRequirement(activeColony)) {
                    message = LanguageStrings.get(LanguageStrings.DIPLO_ERROR_LOYALTY_REQUIRED);
                } else if (!dynasty.getDiplomacyService().meetsTradeReputationRequirement(other, world)) {
                    message = LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NEUTRAL_REQUIRED);
                }
                UiOptionPane.showMessageDialog(this, message,
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
            if (!dynasty.getDiplomacyService().canRequestTrade(other, world)) {
                UiOptionPane.showMessageDialog(this,
                        LanguageStrings.get(LanguageStrings.DIPLO_ERROR_CORDIAL_REQUIRED_REQUEST),
                        LanguageStrings.get(LanguageStrings.DIPLO_ACTION_REQUEST_TRADE),
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            dynasty.getDiplomacyService().requestTrade(other, world);
            UiOptionPane.showMessageDialog(this,
                    LanguageStrings.get(LanguageStrings.DIPLO_TRADE_REQUEST_SENT),
                    LanguageStrings.get(LanguageStrings.DIPLO_ACTION_REQUEST_TRADE),
                    JOptionPane.INFORMATION_MESSAGE);
            updateData();
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

            JMenuItem pactItem = new JMenuItem(LanguageStrings.get(LanguageStrings.DIPLO_ACTION_FORM_PACT));
            if (diplo.hasNonAggressionPact(other)) {
                pactItem.setText(LanguageStrings.get(LanguageStrings.DIPLO_ACTION_BREAK_PACT));
                pactItem.addActionListener(e -> performPactAction(other));
            } else if (diplo.canFormNonAggressionPact(other)) {
                pactItem.addActionListener(e -> performPactAction(other));
            } else {
                pactItem.setEnabled(false);
                pactItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_CORDIAL_REQUIRED));
            }
            AssetStyles.styleMenuItem(pactItem);
            menu.add(pactItem);

            if (!diplo.isAtWarWith(other)) {
                JMenuItem requestItem = new JMenuItem(LanguageStrings.get(LanguageStrings.DIPLO_ACTION_REQUEST_TRADE));
                if (diplo.canRequestTrade(other, world)) {
                    requestItem.addActionListener(e -> performRequestTradeAction(other));
                } else {
                    requestItem.setEnabled(false);
                    requestItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_CORDIAL_REQUIRED_REQUEST));
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
                        if (outgoing != null) {
                            tradeItem.setText(LanguageStrings.get(LanguageStrings.UI_MANAGE));
                            tradeItem.addActionListener(e -> performTradeAction(other));
                        } else if (diplo.canEstablishCrossDynastyTrade(
                                other, activeColony, world, engine.getTradeManager())) {
                            tradeItem.addActionListener(e -> performTradeAction(other));
                        } else {
                            tradeItem.setEnabled(false);
                            if (!DynastyDiplomacyService.meetsTradeLoyaltyRequirement(activeColony)) {
                                tradeItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_LOYALTY_REQUIRED));
                            } else {
                                tradeItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NEUTRAL_REQUIRED));
                            }
                        }
                    }
                }
                AssetStyles.styleMenuItem(tradeItem);
                menu.add(tradeItem);

                JMenuItem diplomatItem = new JMenuItem(LanguageStrings.get(LanguageStrings.DIPLO_ACTION_SEND_DIPLOMATS));
                if (activeColony != null && diplo.canSendDiplomatsToDynasty(activeColony, other, world)) {
                    diplomatItem.addActionListener(e -> promptSendDiplomatsToDynasty(DynastyManagementDialog.this, activeColony, other));
                } else {
                    diplomatItem.setEnabled(false);
                    if (activeColony == null) {
                        diplomatItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_ACTIVE_COLONY));
                    } else if (!activeColony.hasUpgrade(GameUnlocks.ROLE_DIPLOMAT)) {
                        diplomatItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NO_DIPLOMAT_ROLE));
                    } else if (diplo.countAvailableDiplomats(activeColony) <= 0) {
                        diplomatItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NO_DIPLOMATS));
                    } else if (!diplo.needsDiplomatMissionToDynasty(other, world)) {
                        diplomatItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_REPUTATION_STABLE));
                    }
                }
                AssetStyles.styleMenuItem(diplomatItem);
                menu.add(diplomatItem);
            }

            showTableCellPopup(menu, table, row, column);
        }
    }

    private static class SpeciesRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Species species) {
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
        private List<Colony> displayedColonies;
        private final boolean showAutoBuild;
        private final boolean showAutomation;
        private int autoBuildCol = -1;
        private int automationCol = -1;
        private int loyaltyCol = -1;
        private int militaryCol = -1;
        private int actionCol = -1;
        
        private final JComboBox<String> sortCombo;
        private Comparator<Colony> currentSorter;

        private JCheckBox defaultAutoBuildCheck;
        private JCheckBox defaultAutomationCheck;

        public OverviewPanel(boolean showAutoBuild, boolean showAutomation) {
            super(new BorderLayout());
            this.showAutoBuild = showAutoBuild;
            this.showAutomation = showAutomation;
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
            
            initUI();
        }

        public boolean isShowAutomation() {
            return showAutomation;
        }

        public boolean isShowAutoBuild() {
            return showAutoBuild;
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
                    return Object.class;
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == autoBuildCol || column == automationCol || column == actionCol;
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
                    }
                }
            });

            table = new JTable(model);
            table.setRowHeight(45);
            AssetStyles.styleDialogTable(table);
            table.setFocusable(false);
            
            table.getColumnModel().getColumn(0).setMaxWidth(50);
            table.getColumnModel().getColumn(0).setPreferredWidth(50);
            table.getColumnModel().getColumn(1).setPreferredWidth(100);
            table.getColumnModel().getColumn(2).setPreferredWidth(80);
            
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
            table.getColumnModel().getColumn(3).setPreferredWidth(200); 
            table.getColumnModel().getColumn(4).setPreferredWidth(100);
            table.getColumnModel().getColumn(5).setPreferredWidth(80);
            table.getColumnModel().getColumn(5).setCellRenderer(paddedRenderer);
            table.getColumnModel().getColumn(6).setCellRenderer(new OverviewBiomeRenderer());
            table.getColumnModel().getColumn(loyaltyCol).setPreferredWidth(140);
            table.getColumnModel().getColumn(loyaltyCol).setCellRenderer(new LoyaltyCellRenderer());
            table.getColumnModel().getColumn(militaryCol).setPreferredWidth(110);
            table.getColumnModel().getColumn(militaryCol).setCellRenderer(new MilitaryPowerCellRenderer());

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

            table.getColumnModel().getColumn(actionCol).setMinWidth(120);
            table.getColumnModel().getColumn(actionCol).setPreferredWidth(120);
            table.getColumnModel().getColumn(actionCol).setCellRenderer(new ActionPanelRenderer());
            table.getColumnModel().getColumn(actionCol).setCellEditor(new ActionPanelEditor());

            add(new JScrollPane(table), BorderLayout.CENTER);
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

            displayedColonies.addAll(rawColonies);
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
                rowData[actionCol] = colony;
                model.addRow(rowData);
            }
            if (selectedRow >= 0 && selectedRow < table.getRowCount()) table.setRowSelectionInterval(selectedRow, selectedRow);
            AssetStyles.fitTableColumn(table, 0, 40, 52);
            AssetStyles.fitTableColumn(table, 3, 120, 280);
            AssetStyles.fitTableColumn(table, loyaltyCol, 100, 220);
            AssetStyles.fitTableColumn(table, militaryCol, 80, 140);
            AssetStyles.fitTableColumn(table, actionCol, 120, 180);
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

    private static void showTableCellPopup(JPopupMenu menu, JTable table, int row, int column) {
        if (menu == null || table == null || row < 0 || column < 0 || !table.isShowing()) {
            return;
        }
        Rectangle rect = table.getCellRect(row, column, true);
        menu.show(table, rect.x, rect.y + rect.height);
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
            if (diplo.canSendDiplomatsToColony(colony, engine.getTradeManager(), engine.getWorld())) {
                diplomatItem.addActionListener(e -> promptSendDiplomatsToColony(this, colony));
            } else {
                diplomatItem.setEnabled(false);
                if (!colony.hasUpgrade(GameUnlocks.ROLE_DIPLOMAT)) {
                    diplomatItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NO_DIPLOMAT_ROLE));
                } else if (diplo.countAvailableDiplomats(colony) <= 0) {
                    diplomatItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NO_DIPLOMATS));
                } else if (!diplo.needsDiplomatMissionToColony(colony, engine.getTradeManager(), engine.getWorld())) {
                    diplomatItem.setToolTipText(LanguageStrings.get(LanguageStrings.DIPLO_ERROR_LOYALTY_STABLE));
                }
            }
            AssetStyles.styleMenuItem(diplomatItem);
            menu.add(diplomatItem);
        }

        showTableCellPopup(menu, table, row, column);
    }

    private void promptSendDiplomatsToColony(Component parent, Colony colony) {
        DynastyDiplomacyService diplo = dynasty.getDiplomacyService();
        if (diplo == null || colony == null) {
            return;
        }
        if (!colony.hasUpgrade(GameUnlocks.ROLE_DIPLOMAT)) {
            UiOptionPane.showMessageDialog(parent,
                    LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NO_DIPLOMAT_ROLE),
                    LanguageStrings.get(LanguageStrings.DIPLO_SEND_DIPLOMATS_TITLE),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!diplo.needsDiplomatMissionToColony(colony, engine.getTradeManager(), engine.getWorld())) {
            UiOptionPane.showMessageDialog(parent,
                    LanguageStrings.get(LanguageStrings.DIPLO_ERROR_LOYALTY_STABLE),
                    LanguageStrings.get(LanguageStrings.DIPLO_SEND_DIPLOMATS_TITLE),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int available = diplo.countAvailableDiplomats(colony);
        if (available <= 0) {
            UiOptionPane.showMessageDialog(parent,
                    LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NO_DIPLOMATS),
                    LanguageStrings.get(LanguageStrings.DIPLO_SEND_DIPLOMATS_TITLE),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int max = Math.min(available, diplo.getMaxDiplomatsForColonyMission());
        int gainPer = diplo.getDiplomatStabilityGainPerAnt();
        int count = promptDiplomatCount(parent, available, max, gainPer);
        if (count <= 0) {
            return;
        }
        int sent = diplo.sendDiplomatsToColony(colony, count, engine.getTradeManager(), engine.getWorld());
        if (sent > 0) {
            int totalGain = sent * gainPer;
            UiOptionPane.showMessageDialog(parent,
                    LanguageStrings.format(LanguageStrings.DIPLO_SEND_DIPLOMATS_SUCCESS_COLONY,                             sent, colony.getName(), totalGain),
                    LanguageStrings.get(LanguageStrings.DIPLO_SEND_DIPLOMATS_TITLE),
                    JOptionPane.INFORMATION_MESSAGE);
            refreshDialog();
        }
    }

    private void promptSendDiplomatsToDynasty(Component parent, Colony from, Dynasty other) {
        DynastyDiplomacyService diplo = dynasty.getDiplomacyService();
        World world = engine.getWorld();
        if (diplo == null || from == null || other == null) {
            return;
        }
        if (!from.hasUpgrade(GameUnlocks.ROLE_DIPLOMAT)) {
            UiOptionPane.showMessageDialog(parent,
                    LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NO_DIPLOMAT_ROLE),
                    LanguageStrings.get(LanguageStrings.DIPLO_SEND_DIPLOMATS_TITLE),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!diplo.needsDiplomatMissionToDynasty(other, world)) {
            UiOptionPane.showMessageDialog(parent,
                    LanguageStrings.get(LanguageStrings.DIPLO_ERROR_REPUTATION_STABLE),
                    LanguageStrings.get(LanguageStrings.DIPLO_SEND_DIPLOMATS_TITLE),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (diplo.countAvailableDiplomats(from) <= 0) {
            UiOptionPane.showMessageDialog(parent,
                    LanguageStrings.get(LanguageStrings.DIPLO_ERROR_NO_DIPLOMATS),
                    LanguageStrings.get(LanguageStrings.DIPLO_SEND_DIPLOMATS_TITLE),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!diplo.canSendDiplomatsToDynasty(from, other, world)) {
            return;
        }
        int available = diplo.countAvailableDiplomats(from);
        int max = Math.min(available, diplo.getMaxDiplomatsForDynastyMission());
        int gainPer = diplo.getDiplomatStabilityGainPerAnt();
        int count = promptDiplomatCount(parent, available, max, gainPer);
        if (count <= 0) {
            return;
        }
        int sent = diplo.sendDiplomatsToDynasty(from, other, count, world);
        if (sent > 0) {
            int totalGain = sent * gainPer;
            UiOptionPane.showMessageDialog(parent,
                    LanguageStrings.format(LanguageStrings.DIPLO_SEND_DIPLOMATS_SUCCESS_DYNASTY,                             sent, other.getName(), totalGain),
                    LanguageStrings.get(LanguageStrings.DIPLO_SEND_DIPLOMATS_TITLE),
                    JOptionPane.INFORMATION_MESSAGE);
            refreshDialog();
        }
    }

    private int promptDiplomatCount(Component parent, int assigned, int maxCount, int gainPerDiplomat) {
        if (maxCount <= 0) {
            return -1;
        }
        String prompt = String.format(
                LanguageStrings.get(LanguageStrings.DIPLO_SEND_DIPLOMATS_PROMPT),
                maxCount, assigned, gainPerDiplomat);
        SpinnerNumberModel model = new SpinnerNumberModel(1, 1, maxCount, 1);
        JSpinner spinner = new JSpinner(model);
        AssetStyles.styleSpinner(spinner);
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JLabel label = new JLabel(prompt);
        label.setFont(AssetStyles.FONT_NORMAL);
        panel.add(label, BorderLayout.NORTH);
        panel.add(spinner, BorderLayout.CENTER);
        int result = UiOptionPane.showConfirmDialog(parent, panel,
                LanguageStrings.get(LanguageStrings.DIPLO_SEND_DIPLOMATS_TITLE),
                JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return -1;
        }
        return ((Number) spinner.getValue()).intValue();
    }

    private void performEdit(Colony colony) {
        if (colony == null) return;
        String newName = UiOptionPane.showInputDialog(this, LanguageStrings.format(LanguageStrings.DYNASTY_RENAME_TITLE, colony.getName()), colony.getName());
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
