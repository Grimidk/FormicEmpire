package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DynastyManagementDialog extends ZeroDialog {

    private final Dynasty dynasty;
    private final Engine engine;
    private final java.util.function.Consumer<Colony> onGoToColony;

    private final JTabbedPane tabbedPane;
    
    private OverviewPanel overviewPanel;
    private TradePanel tradePanel;

    private final Runnable refreshTask = this::liveUpdate;

    public DynastyManagementDialog(JFrame owner, Dynasty dynasty, Engine engine, java.util.function.Consumer<Colony> onGoToColony) {
        super(owner, "Dynasty Management", new Dimension(1100, 700));
        this.dynasty = dynasty;
        this.engine = engine;
        this.onGoToColony = onGoToColony;

        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        refreshDialog();
        
        if (this.engine != null) {
            this.engine.addHourTickListener(refreshTask);
        }

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (engine != null) {
                    engine.removeHourTickListener(refreshTask);
                }
            }
            @Override
            public void windowClosing(WindowEvent e) {
                if (engine != null) {
                    engine.removeHourTickListener(refreshTask);
                }
            }
        });

        registerCloseKey(KeyEvent.VK_S);
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

        boolean currentAuto = dynasty.hasUpgrade(GameUnlocks.ABILITY_AUTOMATION);
        boolean currentAutoBuild = dynasty.hasUpgrade(GameUnlocks.ABILITY_MANAGEMENT);
        
        if (overviewPanel == null || overviewPanel.isShowAutomation() != currentAuto || overviewPanel.isShowAutoBuild() != currentAutoBuild) {
            overviewPanel = new OverviewPanel(currentAutoBuild, currentAuto);
        }
        overviewPanel.updateData();
        tabbedPane.addTab("Overview", overviewPanel);

        if (dynasty.hasUpgrade(GameUnlocks.ABILITY_TRADE)) {
            if (tradePanel == null) {
                tradePanel = new TradePanel();
            }
            tabbedPane.addTab("Trade", tradePanel);
        }

        if (selectedIndex < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(selectedIndex);
        }
    }

    interface LiveUpdatePanel {
        void liveUpdate();
        void updateData();
    }

    private class TradePanel extends JPanel implements LiveUpdatePanel {
        public TradePanel() {
            setLayout(new GridBagLayout());
            add(new JLabel("Trade Routes - Coming Soon"));
        }

        @Override
        public void liveUpdate() {
        }

        @Override
        public void updateData() {
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
                
                // Ignore full-row updates (like row insertions) where column is -1
                if (col < 0) return;
                
                if (row >= 0 && row < displayedColonies.size()) {
                    Colony c = displayedColonies.get(row);
                    if (col == autoBuildCol) {
                        c.setAutoBuildEnabled((Boolean) model.getValueAt(row, col));
                    } else if (col == automationCol) {
                        c.setAutomationEnabled((Boolean) model.getValueAt(row, col));
                    }
                }
            });

            table = new JTable(model);
            
            table.setRowHeight(45); 
            table.setShowVerticalLines(false);
            table.setIntercellSpacing(new Dimension(0, 1));
            table.getTableHeader().setReorderingAllowed(false);
            table.setFillsViewportHeight(true);
            
            table.getColumnModel().getColumn(0).setMaxWidth(50);
            table.getColumnModel().getColumn(0).setPreferredWidth(50);
            table.getColumnModel().getColumn(1).setPreferredWidth(100);
            table.getColumnModel().getColumn(2).setPreferredWidth(80);
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(3).setPreferredWidth(200);   
            table.getColumnModel().getColumn(5).setPreferredWidth(80); 
            table.getColumnModel().getColumn(6).setCellRenderer(new BiomeRenderer());

            if (showAutoBuild) {
                table.getColumnModel().getColumn(autoBuildCol).setMaxWidth(100);
            }
            if (showAutomation) {
                table.getColumnModel().getColumn(automationCol).setMaxWidth(100);
            }

            table.getColumnModel().getColumn(actionCol).setMinWidth(160); 
            table.getColumnModel().getColumn(actionCol).setCellRenderer(new ActionPanelRenderer());
            table.getColumnModel().getColumn(actionCol).setCellEditor(new ActionPanelEditor());

            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.CENTER);
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
            displayedColonies.sort(
                Comparator.comparing(Colony::isCapital).reversed()
                .thenComparingInt(Colony::getAntTotal).reversed()
            );

            for (Colony colony : displayedColonies) {
                Biome biome = null;
                if (world != null && world.getHexes() != null) {
                    for (Hex hex : world.getHexes()) {
                        if (hex.getColony() == colony && hex.getBiome() != null) {
                            biome = hex.getBiome();
                            break;
                        }
                    }
                }
                
                String typeStr = colony.isCapital() ? "Capital" : "Satellite";

                Object[] rowData = new Object[model.getColumnCount()];
                rowData[0] = colony.getRank().getIcon();
                rowData[1] = colony.getRank().getName();
                rowData[2] = typeStr;
                rowData[3] = colony.getName();
                rowData[4] = colony.getAntTotal();
                rowData[5] = colony.getAge();
                rowData[6] = biome;
                
                if (showAutoBuild) rowData[autoBuildCol] = colony.isAutoBuildEnabled();
                if (showAutomation) rowData[automationCol] = colony.isAutomationEnabled();
                
                rowData[actionCol] = colony;
                
                model.addRow(rowData);
            }
            
            if (selectedRow >= 0 && selectedRow < table.getRowCount()) {
                table.setRowSelectionInterval(selectedRow, selectedRow);
            }

            table.revalidate();
            table.repaint();
        }
    }
    
    // --- Renderers & Editors ---
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
        private final JButton editBtn;
        private final JButton viewBtn;
        private final JProgressBar progressBar;
        private final JLabel statusLabel;

        public ActionPanelRenderer() {
            setLayout(new CardLayout());
            setOpaque(true);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            btnPanel.setOpaque(false);
            editBtn = new JButton("Edit");
            editBtn.setMargin(new Insets(2, 8, 2, 8));
            viewBtn = new JButton("View");
            viewBtn.setMargin(new Insets(2, 8, 2, 8));
            btnPanel.add(editBtn);
            btnPanel.add(viewBtn);

            JPanel progressPanel = new JPanel(new BorderLayout());
            progressPanel.setOpaque(false);
            progressPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            progressBar = new JProgressBar(0, 7);
            progressBar.setStringPainted(true);
            statusLabel = new JLabel("Maturing...", SwingConstants.CENTER);
            statusLabel.setFont(statusLabel.getFont().deriveFont(10f));
            progressPanel.add(statusLabel, BorderLayout.NORTH);
            progressPanel.add(progressBar, BorderLayout.CENTER);

            add(btnPanel, "BUTTONS");
            add(progressPanel, "PROGRESS");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }

            if (value instanceof Colony) {
                Colony c = (Colony) value;
                CardLayout cl = (CardLayout) getLayout();
                
                if (c.getAge() >= 7) {
                    cl.show(this, "BUTTONS");
                } else {
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
        private final JPanel btnPanel;
        private final JPanel progressPanel;
        private final JButton editBtn;
        private final JButton viewBtn;
        private final JProgressBar progressBar;
        private final JLabel statusLabel;
        
        private Colony currentColony;
        private final CardLayout cardLayout;

        public ActionPanelEditor() {
            cardLayout = new CardLayout();
            container = new JPanel(cardLayout);
            container.setOpaque(true);

            btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            btnPanel.setOpaque(false);

            editBtn = new JButton("Edit");
            editBtn.setMargin(new Insets(2, 8, 2, 8));
            editBtn.addActionListener(e -> {
                fireEditingStopped();
                performEdit(currentColony);
            });

            viewBtn = new JButton("View");
            viewBtn.setMargin(new Insets(2, 8, 2, 8));
            viewBtn.addActionListener(e -> {
                fireEditingStopped();
                performView(currentColony);
            });

            btnPanel.add(editBtn);
            btnPanel.add(viewBtn);

            progressPanel = new JPanel(new BorderLayout());
            progressPanel.setOpaque(false);
            progressPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            progressBar = new JProgressBar(0, 7);
            progressBar.setStringPainted(true);
            statusLabel = new JLabel("Maturing...", SwingConstants.CENTER);
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
                
                if (currentColony.getAge() >= 7) {
                    cardLayout.show(container, "BUTTONS");
                } else {
                    cardLayout.show(container, "PROGRESS");
                    progressBar.setValue(currentColony.getAge());
                    progressBar.setString(currentColony.getAge() + " / 7 Days");
                }
            }
            return container;
        }

        @Override
        public Object getCellEditorValue() {
            return currentColony;
        }
    }

    // --- Actions ---
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