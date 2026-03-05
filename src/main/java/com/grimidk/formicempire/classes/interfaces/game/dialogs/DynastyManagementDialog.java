package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

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

import com.grimidk.formicempire.classes.entities.services.DynastyTradeService;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.TradeMethod;
import com.grimidk.formicempire.classes.entities.Trade;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;

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
        super(owner, "Dynasty Management", new Dimension(1100, 750));
        this.dynasty = dynasty;
        this.engine = engine;
        this.onGoToColony = onGoToColony;
        new DynastyTradeService(dynasty, engine.getTradeManager());

        tabbedPane = new JTabbedPane();
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
            this.engine.addTickListener(refreshTask);
        }

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                tabbedPane.requestFocusInWindow();
            }
            @Override
            public void windowClosed(WindowEvent e) {
                if (engine != null) {
                    engine.removeTickListener(refreshTask);
                }
            }
            @Override
            public void windowClosing(WindowEvent e) {
                if (engine != null) {
                    engine.removeTickListener(refreshTask);
                }
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
        tabbedPane.addTab("Overview", overviewPanel);
        tabIndexMap.put(TAB_OVERVIEW, currentIndex++);

        if (dynasty.hasUpgrade(GameUnlocks.ABILITY_TRADE)) {
            if (tradePanel == null) {
                tradePanel = new TradePanel();
            }
            tabbedPane.addTab("Logistics", tradePanel);
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
            
            List<String> cols = new ArrayList<>(List.of("Direction", "Neighbor Colony"));
            if (tunnelsVisible) cols.add("Tunnel Status");
            cols.addAll(List.of("Outgoing Route", "Incoming Route", "Actions"));

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
            JLabel label = new JLabel("Managing Logistics for: ");
            label.setForeground(Color.BLACK);
            topPanel.add(label);
            
            activeColonyLabel = new JLabel("None");
            activeColonyLabel.setForeground(Color.BLACK);
            activeColonyLabel.setFont(activeColonyLabel.getFont().deriveFont(Font.BOLD));
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
                activeColonyLabel.setText("N/A");
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
            String[] dirNames = {"North", "North-East", "South-East", "South", "South-West", "North-West"};

            for (int i = 0; i < adjacent.length; i++) {
                Hex neighborHex = adjacent[i];
                if (neighborHex == null) continue;
                
                Colony neighborColony = neighborHex.getColony();
                if (neighborColony == null) continue;

                String neighborName = neighborColony.getName();
                Tunnel tunnel = dynasty.getTunnelBetween(currentHex, neighborHex);
                
                Trade outgoing = findTrade(activeColony, neighborColony);
                Trade incoming = findTrade(neighborColony, activeColony);
                
                String outStatus = formatTradeStatus(outgoing);
                String inStatus = formatTradeStatus(incoming);

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
            if (origin == null || destination == null) return null;
            return engine.getTradeManager().getActiveTrades().stream()
                .filter(Trade::isActive)
                .filter(t -> t.getOrigin().getColony() == origin && t.getDestination().getColony() == destination)
                .findFirst().orElse(null);
        }

        private String formatTradeStatus(Trade trade) {
            if (trade == null) return "None";
            String transit = trade.isReturning() ? "Returning" : "Transit";
            String pending = trade.hasPendingUpdate() ? " [MODIFIED]" : "";
            return transit + " (" + trade.getRemainingHours() + "h)" + pending;
        }

        private class TunnelCellRenderer extends JPanel implements TableCellRenderer {
            private final JProgressBar progressBar = new JProgressBar(0, 100);
            private final JLabel label = new JLabel();
            private final JButton buildBtn = new JButton("Build Tunnel");
            public TunnelCellRenderer() {
                setLayout(new BorderLayout(5, 5));
                setOpaque(true);
                progressBar.setStringPainted(true);
                label.setHorizontalAlignment(JLabel.CENTER);
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
                            buildBtn.setToolTipText(alreadyBuilding ? "This colony is already sponsoring a tunnel project." : null);
                            add(buildBtn, BorderLayout.CENTER);
                        } else {
                            label.setText("N/A");
                            label.setForeground(Color.GRAY);
                            add(label, BorderLayout.CENTER);
                        }
                    } else if (data.tunnel.isComplete()) {
                        label.setText("Built");
                        label.setForeground(new Color(0, 128, 0));
                        add(label, BorderLayout.CENTER);
                    } else {
                        double pct = (data.tunnel.getProgress() / data.tunnel.getTotalCost()) * 100;
                        progressBar.setValue((int) pct);
                        progressBar.setString(String.format("%.0f%%", pct));
                        
                        int engineers = activeColony.getAssignedRoleCount(GameConstants.ROLE_ENGINEER);
                        int borers = activeColony.getAssignedRoleCount(GameConstants.ROLE_BORER);
                        if (engineers <= 0 && borers <= 0) {
                            progressBar.setForeground(Color.RED);
                            progressBar.setToolTipText("No Engineers or Borers assigned to progress construction!");
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
            private final JButton buildBtn = new JButton("Build Tunnel");
            private TradeRowData currentData;
            public TunnelCellEditor() {
                panel.add(buildBtn, BorderLayout.CENTER);
                buildBtn.addActionListener(e -> {
                    if (currentData != null && currentData.tunnel == null) {
                        int engineers = activeColony.getAssignedRoleCount(GameConstants.ROLE_ENGINEER);
                        int borers = activeColony.getAssignedRoleCount(GameConstants.ROLE_BORER);
                        
                        if (engineers <= 0 && borers <= 0) {
                            JOptionPane.showMessageDialog(panel, 
                                "You must assign at least one Engineer or Tunnel Borer to this colony to start tunnel construction.",
                                "Labor Required", JOptionPane.WARNING_MESSAGE);
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
                add(actionBtn);
            }
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof TradeRowData) {
                    TradeRowData data = (TradeRowData) value;
                    if (data.neighbor == null) {
                        actionBtn.setText("Establish");
                        actionBtn.setEnabled(false);
                    } else {
                        actionBtn.setText(data.outgoingTrade == null ? "Establish" : "Manage");
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
                    actionBtn.setText(currentData.outgoingTrade == null ? "Establish" : "Manage");
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
            String[] options = {"Modify", "Cancel Route", "Close"};
            int res = JOptionPane.showOptionDialog(this, "Manage trade route to " + trade.getDestination().getColony().getName(), "Trade Management", 
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
        private final Map<AntType, JSpinner> antSpinners = new HashMap<>();
        private final JComboBox<TradeMethod> methodCombo;
        private final JCheckBox recurrentCheck;
        private final JButton createBtn;
        
        private final JLabel capLabel = new JLabel("Capacity: 0.0 / 0.0");
        private final JLabel speedLabel = new JLabel("Transit Speed: 0.0x");
        private final JLabel timeLabel = new JLabel("Travel Time: 0h");
        private final JLabel dangerLabel = new JLabel("Security: 0.0%");

        private double totalTransportCapacity = 0.0;

        public TradeCreationDialog(Window owner, Colony origin, Colony target, Engine engine, Trade existingTrade) {
            super(owner, (existingTrade == null ? "Establish" : "Modify") + " Trade Route", ModalityType.APPLICATION_MODAL);
            this.origin = origin;
            this.target = target;
            this.engine = engine;
            this.existingTrade = existingTrade;
            setLayout(new BorderLayout());
            setSize(1000, 800);
            setLocationRelativeTo(owner);

            JPanel headerPanel = new JPanel(new GridLayout(1, 4, 20, 20));
            headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            headerPanel.setBackground(new Color(245, 245, 245));
            
            capLabel.setFont(capLabel.getFont().deriveFont(Font.BOLD, 14f));
            speedLabel.setFont(speedLabel.getFont().deriveFont(Font.BOLD, 14f));
            timeLabel.setFont(timeLabel.getFont().deriveFont(Font.BOLD, 14f));
            dangerLabel.setFont(dangerLabel.getFont().deriveFont(Font.BOLD, 14f));
            capLabel.setForeground(Color.BLACK);
            speedLabel.setForeground(Color.BLACK);
            timeLabel.setForeground(Color.BLACK);
            dangerLabel.setForeground(Color.BLACK);
            
            headerPanel.add(capLabel);
            headerPanel.add(speedLabel);
            headerPanel.add(timeLabel);
            headerPanel.add(dangerLabel);
            add(headerPanel, BorderLayout.NORTH);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel routeLabel = new JLabel("Route: " + origin.getName() + " -> " + target.getName());
            routeLabel.setFont(routeLabel.getFont().deriveFont(Font.BOLD));
            routeLabel.setForeground(Color.BLACK);
            mainPanel.add(routeLabel);
            mainPanel.add(Box.createVerticalStrut(15));

            JPanel resGrid = new JPanel(new GridLayout(0, 2, 15, 8));
            resGrid.setBorder(BorderFactory.createTitledBorder("Cargo (Load)"));
            for (ResourceType rt : GameConstants.getResources()) {
                if (rt.isIsLiquid()) continue;

                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
                JLabel label = new JLabel(rt.getName(), rt.getIcon(), JLabel.LEFT);
                label.setPreferredSize(new Dimension(160, 25));
                label.setForeground(Color.BLACK);
                p.add(label);
                
                double available = getColonyResource(rt);
                double initialVal = (existingTrade != null) ? existingTrade.getLoad().getOrDefault(rt, 0.0) : 0.0;
                JSpinner s = new JSpinner(new SpinnerNumberModel(initialVal, 0.0, available, 10.0));
                s.addChangeListener(e -> updateStats());
                resourceSpinners.put(rt, s);
                p.add(s);

                JButton maxBtn = new JButton("Max");
                maxBtn.setMargin(new Insets(2, 5, 2, 5));
                maxBtn.addActionListener(e -> setMaxResource(rt));
                p.add(maxBtn);

                resGrid.add(p);
            }
            mainPanel.add(resGrid);
            mainPanel.add(Box.createVerticalStrut(15));

            JPanel antGrid = new JPanel(new GridLayout(0, 2, 15, 8));
            antGrid.setBorder(BorderFactory.createTitledBorder("Personnel (Assigned Logistics Roles)"));
            
            Map<AntType, Integer> availableRoles = new HashMap<>();
            availableRoles.put(GameConstants.TYPE_WORKER, origin.getAssignedRoleCount(GameConstants.ROLE_COURIER));
            availableRoles.put(GameConstants.TYPE_MAJOR, origin.getAssignedRoleCount(GameConstants.ROLE_TRANSPORT));
            availableRoles.put(GameConstants.TYPE_SOLDIER, origin.getAssignedRoleCount(GameConstants.ROLE_ESCORT));
            availableRoles.put(GameConstants.TYPE_PRINCESS, origin.getAssignedRoleCount(GameConstants.ROLE_SKYTRANS));

            List<AntType> tradeAnts = List.of(GameConstants.TYPE_WORKER, GameConstants.TYPE_SOLDIER, GameConstants.TYPE_MAJOR, GameConstants.TYPE_PRINCESS);
            for (AntType at : tradeAnts) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
                String labelName = at.getName();
                if (at == GameConstants.TYPE_WORKER) labelName = "Couriers";
                else if (at == GameConstants.TYPE_MAJOR) labelName = "Transports";
                else if (at == GameConstants.TYPE_SOLDIER) labelName = "Escorts";
                else if (at == GameConstants.TYPE_PRINCESS) labelName = "Flyers";
                
                int available = availableRoles.getOrDefault(at, 0);
                JLabel label = new JLabel(labelName + " (" + available + ")", at.getIcon(), JLabel.LEFT);
                label.setPreferredSize(new Dimension(200, 25));
                label.setForeground(Color.BLACK);
                p.add(label);
                
                int initialVal = (existingTrade != null) ? existingTrade.getTransport().getOrDefault(at, 0) : 0;
                JSpinner s = new JSpinner(new SpinnerNumberModel(initialVal, 0, available, 1));
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
            configPanel.setBorder(BorderFactory.createTitledBorder("Logistics"));
            
            methodCombo = new JComboBox<>();
            updateAvailableMethods();

            if (existingTrade != null) methodCombo.setSelectedItem(existingTrade.getMethod());
            methodCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof TradeMethod) setText(((TradeMethod) value).getName());
                    return this;
                }
            });
            methodCombo.addActionListener(e -> updateStats());
            JLabel mLabel = new JLabel("Method:");
            mLabel.setForeground(Color.BLACK);
            configPanel.add(mLabel);
            configPanel.add(methodCombo);
            
            recurrentCheck = new JCheckBox("Recurrent Route", (existingTrade == null || existingTrade.isRecurrent()));
            recurrentCheck.setForeground(Color.BLACK);
            configPanel.add(Box.createHorizontalStrut(20));
            configPanel.add(recurrentCheck);
            mainPanel.add(configPanel);

            add(mainPanel, BorderLayout.CENTER);

            createBtn = new JButton(existingTrade == null ? "Confirm Trade Route" : "Update Trade Route");
            createBtn.setFont(createBtn.getFont().deriveFont(Font.BOLD));
            createBtn.setPreferredSize(new Dimension(0, 60));
            createBtn.addActionListener(e -> attemptCreate());
            add(createBtn, BorderLayout.SOUTH);
            
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

        private void setMaxResource(ResourceType rt) {
            double currentLoad = 0.0;
            for (Map.Entry<ResourceType, JSpinner> entry : resourceSpinners.entrySet()) {
                if (entry.getKey() != rt) {
                    currentLoad += (Double) entry.getValue().getValue();
                }
            }
            double remainingCap = Math.max(0.0, totalTransportCapacity - currentLoad);
            double available = getColonyResource(rt);
            double maxToSet = Math.min(available, remainingCap);
            resourceSpinners.get(rt).setValue(maxToSet);
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

            boolean overCap = totalLoad > totalCap;
            boolean noLoad = totalLoad <= 0;
            boolean noAnts = totalAnts <= 0;

            capLabel.setText(String.format("Capacity: %.1f / %.1f", totalLoad, totalCap));
            capLabel.setForeground(overCap ? Color.RED : Color.BLACK);
            
            speedLabel.setText(String.format("Transit Speed: %.2fx", speedFactor));
            timeLabel.setText(String.format("Travel Time: %dh", hours));
            dangerLabel.setText(String.format("Security: %.1f%%", mitigationPercent));
            dangerLabel.setForeground(mitigationPercent < 100 ? new Color(180, 100, 0) : new Color(0, 120, 0));

            createBtn.setEnabled(!overCap && !noAnts && !noLoad);
            if (overCap) createBtn.setToolTipText("Cargo exceeds transport capacity!");
            else if (noAnts) createBtn.setToolTipText("No ants assigned to transport!");
            else if (noLoad) createBtn.setToolTipText("No resources selected for trade!");
            else createBtn.setToolTipText(null);
        }

        private void attemptCreate() {
            Map<ResourceType, Double> load = new HashMap<>();
            for (Map.Entry<ResourceType, JSpinner> entry : resourceSpinners.entrySet()) {
                double val = (Double) entry.getValue().getValue();
                if (val > 0) load.put(entry.getKey(), val);
            }

            Map<AntType, Integer> transport = new HashMap<>();
            for (Map.Entry<AntType, JSpinner> entry : antSpinners.entrySet()) {
                int val = (Integer) entry.getValue().getValue();
                if (val > 0) transport.put(entry.getKey(), val);
            }

            if (load.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cargo cannot be empty.");
                return;
            }
            if (transport.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Must assign at least one ant for transport.");
                return;
            }

            TradeMethod method = (TradeMethod) methodCombo.getSelectedItem();
            if (method == null) {
                JOptionPane.showMessageDialog(this, "No valid logistics method available.");
                return;
            }

            if (existingTrade != null) {
                existingTrade.setPendingUpdate(load, transport, recurrentCheck.isSelected(), method);
                JOptionPane.showMessageDialog(this, "Modifications queued. They will apply once the convoy returns to home base.");
            } else {
                World world = engine.getWorld();
                Trade trade = new Trade(world.getHexOfColony(origin), world.getHexOfColony(target), load, transport, recurrentCheck.isSelected(), method);
                trade.startTrip();
                engine.getTradeManager().addTrade(trade);
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

        public OverviewPanel(boolean showAutoBuild, boolean showAutomation) {
            super(new BorderLayout());
            this.showAutoBuild = showAutoBuild;
            this.showAutomation = showAutomation;
            this.displayedColonies = new ArrayList<>();
            initUI();
        }

        public boolean isShowAutomation() {
            return showAutomation;
        }

        public boolean isShowAutoBuild() {
            return showAutoBuild;
        }

        private void initUI() {
            List<String> cols = new ArrayList<>(Arrays.asList("", "Rank", "Type", "Name", "Population", "Age (Days)", "Biome"));
            
            if (showAutoBuild) {
                autoBuildCol = cols.size();
                cols.add("Auto-Build");
            }
            if (showAutomation) {
                automationCol = cols.size();
                cols.add("Automation");
            }
            
            actionCol = cols.size();
            cols.add("Actions/Status");

            model = new DefaultTableModel(cols.toArray(new String[0]), 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 0) return Icon.class;
                    if (columnIndex == 4) return Integer.class;
                    if (columnIndex == 6) return Biome.class; 
                    if (columnIndex == autoBuildCol || columnIndex == automationCol) return Boolean.class;
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
            table.setForeground(Color.BLACK);
            
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
            displayedColonies.sort(Comparator.comparingInt(Colony::getAntTotal).reversed());

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
                rowData[2] = colony.isCapital() ? "Capital" : "Satellite";
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
                setText("Unknown");
                setIcon(null);
            }
            return this;
        }
    }

    private static class ActionPanelRenderer extends JPanel implements TableCellRenderer {
        private final JButton editBtn = new JButton("Edit");
        private final JButton viewBtn = new JButton("View");
        private final JProgressBar progressBar = new JProgressBar(0, 7);
        private final JLabel statusLabel = new JLabel("Maturing...", SwingConstants.CENTER);

        public ActionPanelRenderer() {
            setLayout(new CardLayout());
            setOpaque(true);
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            btnPanel.setOpaque(false);
            btnPanel.add(editBtn);
            btnPanel.add(viewBtn);
            JPanel progressPanel = new JPanel(new BorderLayout());
            progressPanel.setOpaque(false);
            progressBar.setStringPainted(true);
            statusLabel.setFont(statusLabel.getFont().deriveFont(10f));
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
                    progressBar.setString(c.getAge() + " / 7 Days");
                }
            }
            return this;
        }
    }

    private class ActionPanelEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel container;
        private final JButton editBtn = new JButton("Edit");
        private final JButton viewBtn = new JButton("View");
        private final JProgressBar progressBar = new JProgressBar(0, 7);
        private final JLabel statusLabel = new JLabel("Maturing...", SwingConstants.CENTER);
        private Colony currentColony;
        private final CardLayout cardLayout = new CardLayout();

        public ActionPanelEditor() {
            container = new JPanel(cardLayout);
            container.setOpaque(true);
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            btnPanel.setOpaque(false);
            editBtn.addActionListener(e -> { fireEditingStopped(); performEdit(currentColony); });
            viewBtn.addActionListener(e -> { fireEditingStopped(); performView(currentColony); });
            btnPanel.add(editBtn);
            btnPanel.add(viewBtn);
            JPanel progressPanel = new JPanel(new BorderLayout());
            progressPanel.setOpaque(false);
            progressBar.setStringPainted(true);
            statusLabel.setFont(statusLabel.getFont().deriveFont(10f));
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
                    progressBar.setString(currentColony.getAge() + " / 7 Days");
                }
            }
            return container;
        }
        @Override
        public Object getCellEditorValue() { return currentColony; }
    }

    private void performEdit(Colony colony) {
        if (colony == null) return;
        String newName = JOptionPane.showInputDialog(this, "Enter new name for " + colony.getName(), colony.getName());
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
