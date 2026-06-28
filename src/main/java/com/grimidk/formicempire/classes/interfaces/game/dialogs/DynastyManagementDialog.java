package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.misc.TradeMethod;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Trade;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
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

    private final Dynasty dynasty;
    private final Engine engine;
    private final Consumer<Colony> onGoToColony;

    private final JTabbedPane tabbedPane;
    private final Map<Integer, Integer> tabIndexMap = new HashMap<>();
    
    private OverviewPanel overviewPanel;
    private TradePanel tradePanel;

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
        int expectedTabs = 1 + (hasTrade ? 1 : 0);        
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
    }

    interface LiveUpdatePanel {
        void liveUpdate();
        void updateData();
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
            table.getTableHeader().setReorderingAllowed(false);
            table.setFocusable(false);
            
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

                String neighborName = neighborColony.getName();
                Tunnel tunnel = dynasty.getTunnelBetween(currentHex, neighborHex);
                
                Trade outgoing = dynasty.getTradeService().findTrade(activeColony, neighborColony);
                Trade incoming = dynasty.getTradeService().findTrade(neighborColony, activeColony);
                
                String outStatus = formatTradeStatus(outgoing);
                String inStatus = formatTradeStatus(incoming);

                if (outgoing == null && incoming != null && (incoming.hasPendingUpdate() ? incoming.isPendingBilateral() : incoming.isBilateral())) {
                    outStatus = LanguageStrings.get(LanguageStrings.UI_BILATERAL);
                }
                if (incoming == null && outgoing != null && (outgoing.hasPendingUpdate() ? outgoing.isPendingBilateral() : outgoing.isBilateral())) {
                    inStatus = LanguageStrings.get(LanguageStrings.UI_BILATERAL);
                }

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
            return String.format(LanguageStrings.get(LanguageStrings.DYNASTY_TRANSIT_FORMAT), transit, trade.getRemainingHours(), pending);
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
                AssetStyles.styleButton(buildBtn);
                AssetStyles.styleProgressBar(progressBar);
            }
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                removeAll();
                if (value instanceof TradeRowData) {
                    TradeRowData data = (TradeRowData) value;
                    if (data.tunnel == null) {
                        boolean sameDynasty = data.neighbor != null && data.neighbor.getDynasty() == activeColony.getDynasty();
                        if (sameDynasty) {
                            boolean alreadyBuilding = activeColony.getCurrentTunnelProject() != null;
                            buildBtn.setEnabled(!alreadyBuilding);
                            buildBtn.setToolTipText(alreadyBuilding ? LanguageStrings.get(LanguageStrings.DYNASTY_TUNNEL_SPONSORING) : null);
                            add(buildBtn, BorderLayout.CENTER);
                        } else {
                            label.setText(LanguageStrings.get(LanguageStrings.WORLD_NA));
                            label.setForeground(AssetStyles.BACKGROUND_SECONDARY);
                            add(label, BorderLayout.CENTER);
                        }
                    } else if (data.tunnel.isComplete()) {
                        label.setText(LanguageStrings.get(LanguageStrings.DYNASTY_BUILT));
                        label.setForeground(AssetStyles.FONT_COLOR_SUCCESS);
                        add(label, BorderLayout.CENTER);
                    } else {
                        double pct = (data.tunnel.getProgress() / data.tunnel.getTotalCost()) * 100;
                        progressBar.setValue((int) pct);
                        progressBar.setString(String.format(LanguageStrings.get(LanguageStrings.DYNASTY_PROGRESS_PERCENT), pct));
                        
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
                AssetStyles.styleButton(buildBtn);
                panel.add(buildBtn, BorderLayout.CENTER);
                buildBtn.addActionListener(e -> {
                    if (currentData != null && currentData.tunnel == null) {
                        int engineers = activeColony.getAssignedRoleCount(GameConstants.ROLE_ENGINEER);
                        int borers = activeColony.getAssignedRoleCount(GameConstants.ROLE_BORER);
                        
                        if (engineers <= 0 && borers <= 0) {
                            JOptionPane.showMessageDialog(panel, 
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
            public TradeActionRenderer() {
                setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
                actionBtn.setFocusable(false);
                AssetStyles.styleCompactButton(actionBtn);
                add(actionBtn);
            }
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof TradeRowData) {
                    TradeRowData data = (TradeRowData) value;
                    if (data.neighbor == null) {
                        actionBtn.setText(LanguageStrings.get(LanguageStrings.UI_ESTABLISH));
                        actionBtn.setEnabled(false);
                    } else {
                        actionBtn.setText(data.outgoingTrade == null ? LanguageStrings.get(LanguageStrings.UI_ESTABLISH) : LanguageStrings.get(LanguageStrings.UI_MANAGE));
                        actionBtn.setEnabled(true);
                    }
                }
                setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                return this;
            }
        }

        private class TradeActionEditor extends AbstractCellEditor implements TableCellEditor {
            private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
            private final JButton actionBtn = new JButton();
            private TradeRowData currentData;
            public TradeActionEditor() {
                actionBtn.setFocusable(false);
                AssetStyles.styleCompactButton(actionBtn);
                panel.add(actionBtn);
                actionBtn.addActionListener(e -> {
                    fireEditingStopped();
                    if (currentData == null || currentData.neighbor == null) return;
                    if (currentData.outgoingTrade == null) {
                        establishTrade(currentData.neighbor);
                    } else {
                        manageTrade(currentData.outgoingTrade);
                    }
                });
            }
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                currentData = (TradeRowData) value;
                if (currentData != null) {
                    actionBtn.setText(currentData.outgoingTrade == null ? LanguageStrings.get(LanguageStrings.UI_ESTABLISH) : LanguageStrings.get(LanguageStrings.UI_MANAGE));
                }
                return panel;
            }
            @Override
            public Object getCellEditorValue() { return currentData; }
        }

        private void establishTrade(Colony target) {
            TradeCreationDialog dialog = new TradeCreationDialog(SwingUtilities.getWindowAncestor(this), activeColony, target, engine, null);
            dialog.setVisible(true);
            updateData();
        }

        private void manageTrade(Trade trade) {
            String[] options = {LanguageStrings.get(LanguageStrings.DYNASTY_MODIFY), LanguageStrings.get(LanguageStrings.DYNASTY_CANCEL_ROUTE), LanguageStrings.get(LanguageStrings.UI_CLOSE)};
            int res = JOptionPane.showOptionDialog(this, String.format(LanguageStrings.get(LanguageStrings.DYNASTY_MANAGE_TRADE_MSG), trade.getDestination().getColony().getName()), LanguageStrings.get(LanguageStrings.DYNASTY_MANAGE_TRADE_TITLE), 
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            
            if (res == 0) {
                TradeCreationDialog dialog = new TradeCreationDialog(SwingUtilities.getWindowAncestor(this), activeColony, trade.getDestination().getColony(), engine, trade);
                dialog.setVisible(true);
            } else if (res == 1) {
                trade.cancel();
                engine.getTradeManager().removeTrade(trade);
            }
            updateData();
        }

        private void startTunnel(Hex targetHex) {
            Hex originHex = engine.getWorld().getHexOfColony(activeColony);
            Tunnel tunnel = new Tunnel(originHex, targetHex, GameConstants.TUNNEL_WORK_REQUIRED); 
            dynasty.addTunnel(tunnel);
            activeColony.setCurrentTunnelProject(tunnel);
            updateData();
        }
    }

    private static class TradeCreationDialog extends JDialog {
        private final Colony origin;
        private final Colony target;
        private final Engine engine;
        private final Trade existingTrade;
        private final Map<ResourceType, JSpinner> resourceSpinners = new HashMap<>();
        private final Map<ResourceType, JSpinner> returnResourceSpinners = new HashMap<>();
        private final Map<AntType, JSpinner> antSpinners = new HashMap<>();
        private final JComboBox<TradeMethod> methodCombo;
        private final JCheckBox recurrentCheck;
        private final JCheckBox bilateralCheck;
        private final JButton createBtn;
        private final JButton optimizeBtn;
        private final List<JButton> compactButtons = new ArrayList<>();
        
        private final JLabel capLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.TRADE_CAPACITY_FORMAT), 0.0, 0.0));
        private final JLabel speedLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.TRADE_SPEED_FORMAT), 0.0));
        private final JLabel timeLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.TRADE_TIME_FORMAT), 0));
        private final JLabel dangerLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.TRADE_SECURITY_FORMAT), 0.0));

        private double totalTransportCapacity = 0.0;

        public TradeCreationDialog(Window owner, Colony origin, Colony target, Engine engine, Trade existingTrade) {
            super(owner, (existingTrade == null ? LanguageStrings.get(LanguageStrings.UI_ESTABLISH) : LanguageStrings.get(LanguageStrings.DYNASTY_MODIFY)) + " " + LanguageStrings.get(LanguageStrings.PANEL_TRADE), ModalityType.APPLICATION_MODAL); // generic trade key used
            this.origin = origin;
            this.target = target;
            this.engine = engine;
            this.existingTrade = existingTrade;
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

            JLabel routeLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.TRADE_ROUTE_PREFIX), origin.getName(), target.getName()));
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
                    if (value instanceof TradeMethod) setText(((TradeMethod) value).getName());
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
            
            boolean initialBilateral = (existingTrade != null) ? (existingTrade.hasPendingUpdate() ? existingTrade.isPendingBilateral() : existingTrade.isBilateral()) : false;
            bilateralCheck = new JCheckBox(LanguageStrings.get(LanguageStrings.TRADE_BILATERAL), initialBilateral);
            AssetStyles.styleCheckBox(bilateralCheck);
            bilateralCheck.setFocusable(false);
            bilateralCheck.setOpaque(false);
            bilateralCheck.setVisible(origin.hasUpgrade(GameUnlocks.ABILITY_BILATERAL_TRADE));
            bilateralCheck.addActionListener(e -> {
                bilateralPanel.setVisible(bilateralCheck.isSelected());
                revalidate();
                repaint();
                updateStats();
            });
            configPanel.add(Box.createHorizontalStrut(20));
            configPanel.add(bilateralCheck);
            mainPanel.add(configPanel);

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
            optimizeBtn.setForeground(AssetStyles.FONT_COLOR_SUCCESS);
            optimizeBtn.setVisible(false);
            optimizeBtn.addActionListener(e -> performOptimization());
            
            JButton cancelBtn = new JButton(LanguageStrings.get(LanguageStrings.UI_CANCEL));
            cancelBtn.setFocusable(false);
            cancelBtn.addActionListener(e -> dispose());
            
            footerPanel.add(createBtn);
            footerPanel.add(optimizeBtn);
            footerPanel.add(cancelBtn);
            add(footerPanel, BorderLayout.SOUTH);
            
            AssetStyles.applyThemeToContainer(getContentPane());
            AssetStyles.styleButton(createBtn);
            AssetStyles.styleButton(optimizeBtn);
            AssetStyles.styleButton(cancelBtn);
            for (JButton compact : compactButtons) {
                AssetStyles.styleCompactButton(compact);
            }
            
            updateStats();
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

            capLabel.setText(String.format(LanguageStrings.get(LanguageStrings.TRADE_CAPACITY_FORMAT), Math.max(totalLoad, bilateralCheck.isSelected() ? totalReturnLoad : 0), totalCap));
            capLabel.setForeground(overCap ? AssetStyles.FONT_COLOR_ERROR : AssetStyles.FONT_COLOR);
            
            speedLabel.setText(String.format(LanguageStrings.get(LanguageStrings.TRADE_SPEED_FORMAT), speedFactor));
            timeLabel.setText(String.format(LanguageStrings.get(LanguageStrings.TRADE_TIME_FORMAT), hours));
            dangerLabel.setText(String.format(LanguageStrings.get(LanguageStrings.TRADE_SECURITY_FORMAT), mitigationPercent));
            dangerLabel.setForeground(mitigationPercent < 100 ? AssetStyles.FONT_COLOR_WARNING : AssetStyles.FONT_COLOR_SUCCESS);

            createBtn.setEnabled(!overCap && !noAnts && !noLoad);
            if (overCap) createBtn.setToolTipText(LanguageStrings.get(LanguageStrings.TRADE_ERROR_OVER_CAP));
            else if (noAnts) createBtn.setToolTipText(LanguageStrings.get(LanguageStrings.TRADE_ERROR_NO_ANTS));
            else if (noLoad) createBtn.setToolTipText(LanguageStrings.get(LanguageStrings.TRADE_ERROR_NO_LOAD));
            else createBtn.setToolTipText(null);

            if (origin.hasUpgrade(GameUnlocks.ABILITY_BILATERAL_TRADE) && !bilateralCheck.isSelected()) {
                Trade incoming = findIncomingTrade();
                if (incoming != null) {
                    double incomingTotal = incoming.getLoad().values().stream().mapToDouble(Double::doubleValue).sum();
                    if (incomingTotal <= totalCap) {
                        optimizeBtn.setVisible(true);
                    } else {
                        optimizeBtn.setVisible(false);
                    }
                } else {
                    optimizeBtn.setVisible(false);
                }
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

            int res = JOptionPane.showConfirmDialog(this, 
                String.format(LanguageStrings.get(LanguageStrings.TRADE_OPTIMIZE_MSG), target.getName()), 
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
                JOptionPane.showMessageDialog(this, LanguageStrings.get(LanguageStrings.TRADE_ERROR_EMPTY));
                return;
            }
            if (transport.isEmpty()) {
                JOptionPane.showMessageDialog(this, LanguageStrings.get(LanguageStrings.TRADE_ERROR_NO_PERSONNEL));
                return;
            }

            TradeMethod method = (TradeMethod) methodCombo.getSelectedItem();
            if (method == null) {
                JOptionPane.showMessageDialog(this, LanguageStrings.get(LanguageStrings.TRADE_ERROR_NO_METHOD));
                return;
            }

            if (existingTrade != null) {
                existingTrade.setPendingUpdate(load, returnLoad, transport, recurrentCheck.isSelected(), bilateralCheck.isSelected(), method);
                JOptionPane.showMessageDialog(this, LanguageStrings.get(LanguageStrings.TRADE_QUEUED_MSG));
            } else {
                World world = engine.getWorld();
                Hex originHex = world.getHexOfColony(origin);
                Hex targetHex = world.getHexOfColony(target);
                
                if (originHex == null || targetHex == null) {
                    JOptionPane.showMessageDialog(this, LanguageStrings.get(LanguageStrings.DYNASTY_ERROR_LOCATE));
                    return;
                }

                Trade trade = new Trade(originHex, targetHex, load, returnLoad, transport, recurrentCheck.isSelected(), bilateralCheck.isSelected(), method);
                boolean started = trade.startTrip();
                
                if (started) {
                    if (engine.getTradeManager() != null) {
                        engine.getTradeManager().addTrade(trade);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, LanguageStrings.get(LanguageStrings.TRADE_ERROR_START));
                    return;
                }
            }
            dispose();
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
                LanguageStrings.get(LanguageStrings.DYNASTY_SORT_AGE_NEW)
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
            }
            updateData();
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
            
            if (showAutoBuild) {
                autoBuildCol = cols.size();
                cols.add(LanguageStrings.get(LanguageStrings.STAT_AUTO_BUILD));
            }
            if (showAutomation) {
                automationCol = cols.size();
                cols.add(LanguageStrings.get(LanguageStrings.STAT_AUTOMATION));
            }
            
            actionCol = cols.size();
            cols.add(LanguageStrings.get(LanguageStrings.DYNASTY_STATUS));

            model = new DefaultTableModel(cols.toArray(new String[0]), 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 0) return Icon.class;
                    if (columnIndex == 4) return Integer.class;
                    if (columnIndex == 6) return Biome.class; 
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
            table.setShowVerticalLines(false);
            table.setIntercellSpacing(new Dimension(0, 1));
            table.getTableHeader().setReorderingAllowed(false);
            table.setFillsViewportHeight(true);
            table.setForeground(AssetStyles.FONT_COLOR);
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
            table.getColumnModel().getColumn(6).setCellRenderer(new BiomeRenderer());

            if (showAutoBuild) table.getColumnModel().getColumn(autoBuildCol).setMaxWidth(100);
            if (showAutomation) table.getColumnModel().getColumn(automationCol).setMaxWidth(100);

            table.getColumnModel().getColumn(actionCol).setMinWidth(220);
            table.getColumnModel().getColumn(actionCol).setPreferredWidth(220);
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
                rowData[6] = biome;
                if (showAutoBuild) rowData[autoBuildCol] = colony.isAutoBuildEnabled();
                if (showAutomation) rowData[automationCol] = colony.isAutomationEnabled();
                rowData[actionCol] = colony;
                model.addRow(rowData);
            }
            if (selectedRow >= 0 && selectedRow < table.getRowCount()) table.setRowSelectionInterval(selectedRow, selectedRow);
        }
    }
    
    private static class BiomeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Biome) {
                Biome b = (Biome) value;
                setText(b.getName());
                setIcon(b.getIcon());
                setIconTextGap(8);
            } else {
                setText(LanguageStrings.get(LanguageStrings.STAT_UNKNOWN));
                setIcon(null);
            }
            return this;
        }
    }

    private static class ActionPanelRenderer extends JPanel implements TableCellRenderer {
        private final JButton editBtn = new JButton(LanguageStrings.get(LanguageStrings.UI_EDIT));
        private final JButton viewBtn = new JButton(LanguageStrings.get(LanguageStrings.UI_VIEW));
        private final JProgressBar progressBar = new JProgressBar(0, 7);
        private final JLabel statusLabel = new JLabel(LanguageStrings.get(LanguageStrings.UI_MATURING), SwingConstants.CENTER);

        public ActionPanelRenderer() {
            setLayout(new CardLayout());
            setOpaque(true);
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            btnPanel.setOpaque(false);
            editBtn.setFocusable(false);
            viewBtn.setFocusable(false);
            AssetStyles.styleCompactButton(editBtn);
            AssetStyles.styleCompactButton(viewBtn);
            btnPanel.add(editBtn);
            btnPanel.add(viewBtn);
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
            if (value instanceof Colony) {
                Colony c = (Colony) value;
                CardLayout cl = (CardLayout) getLayout();
                if (c.getAge() >= 7) cl.show(this, "BUTTONS");
                else {
                    cl.show(this, "PROGRESS");
                    progressBar.setValue(c.getAge());
                    progressBar.setString(String.format(LanguageStrings.get(LanguageStrings.DYNASTY_7_DAYS), c.getAge()));
                }
            }
            return this;
        }
    }

    private class ActionPanelEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel container;
        private final JButton editBtn = new JButton(LanguageStrings.get(LanguageStrings.UI_EDIT));
        private final JButton viewBtn = new JButton(LanguageStrings.get(LanguageStrings.UI_VIEW));
        private final JProgressBar progressBar = new JProgressBar(0, 7);
        private final JLabel statusLabel = new JLabel(LanguageStrings.get(LanguageStrings.UI_MATURING), SwingConstants.CENTER);
        private Colony currentColony;
        private final CardLayout cardLayout = new CardLayout();

        public ActionPanelEditor() {
            container = new JPanel(cardLayout);
            container.setOpaque(true);
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            btnPanel.setOpaque(false);
            editBtn.setFocusable(false);
            viewBtn.setFocusable(false);
            AssetStyles.styleCompactButton(editBtn);
            AssetStyles.styleCompactButton(viewBtn);
            editBtn.addActionListener(e -> { fireEditingStopped(); performEdit(currentColony); });
            viewBtn.addActionListener(e -> { fireEditingStopped(); performView(currentColony); });
            btnPanel.add(editBtn);
            btnPanel.add(viewBtn);
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
            if (value instanceof Colony) {
                this.currentColony = (Colony) value;
                container.setBackground(table.getSelectionBackground());
                if (currentColony.getAge() >= 7) cardLayout.show(container, "BUTTONS");
                else {
                    cardLayout.show(container, "PROGRESS");
                    progressBar.setValue(currentColony.getAge());
                    progressBar.setString(String.format(LanguageStrings.get(LanguageStrings.DYNASTY_7_DAYS), currentColony.getAge()));
                }
            }
            return container;
        }
        @Override
        public Object getCellEditorValue() { return currentColony; }
    }

    private void performEdit(Colony colony) {
        if (colony == null) return;
        String newName = JOptionPane.showInputDialog(this, String.format(LanguageStrings.get(LanguageStrings.DYNASTY_RENAME_TITLE), colony.getName()), colony.getName());
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
