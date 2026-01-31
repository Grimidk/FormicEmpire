package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.Civilization;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;

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
import java.util.Comparator;
import java.util.List;

public class CivilizationManagementDialog extends ZeroDialog {

    private final Civilization civilization;
    private final Engine engine;
    private final java.util.function.Consumer<Colony> onGoToColony;

    private JTable table;
    private DefaultTableModel model;

    private List<Colony> displayedColonies;     
    private final Runnable refreshTask = this::liveUpdate;

    public CivilizationManagementDialog(JFrame owner, Civilization civilization, Engine engine, java.util.function.Consumer<Colony> onGoToColony) {
        super(owner, "Civilization Management", new Dimension(1100, 700));
        this.civilization = civilization;
        this.engine = engine;
        this.onGoToColony = onGoToColony;
        this.displayedColonies = new ArrayList<>();

        initUI();
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
            SwingUtilities.invokeLater(this::refreshDialog);
        } else {
            refreshDialog();
        }
    }

    private void initUI() {
        String[] columns = {"", "Rank", "Name", "Population", "Age", "Biome", "Automation", "Actions"};
        
        model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return Icon.class;
                    case 3: return Integer.class; 
                    case 5: return Biome.class; 
                    case 6: return Boolean.class;
                    default: return Object.class;
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6 || column == 7;
            }
        };

        model.addTableModelListener(e -> {
            if (e.getColumn() == 6 && e.getFirstRow() >= 0 && e.getFirstRow() < displayedColonies.size()) {
                boolean isChecked = (Boolean) model.getValueAt(e.getFirstRow(), 6);
                Colony c = displayedColonies.get(e.getFirstRow());
                c.setAutomationEnabled(isChecked);
            }
        });

        table = new JTable(model);
        
        // Aesthetics
        table.setRowHeight(45); 
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setReorderingAllowed(false);
        table.setFillsViewportHeight(true);

        // Column Sizing
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(200); 
        table.getColumnModel().getColumn(4).setPreferredWidth(80); 
        table.getColumnModel().getColumn(6).setMaxWidth(100);
        table.getColumnModel().getColumn(7).setMinWidth(160); 

        // Custom Renderers
        table.getColumnModel().getColumn(5).setCellRenderer(new BiomeRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionPanelRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ActionPanelEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    protected void refreshDialog() {
        int selectedRow = table.getSelectedRow();
        
        model.setRowCount(0);
        displayedColonies.clear();

        List<Colony> rawColonies = civilization.getColonies();
        World world = engine.getWorld();

        displayedColonies.addAll(rawColonies);
        displayedColonies.sort(Comparator.comparingInt(Colony::getAntTotal).reversed());

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

            Object[] rowData = new Object[] {
                colony.getRank().getIcon(),      
                colony.getRank().getName(),      
                colony.getName(),                 
                colony.getAntTotal(),              
                colony.getAge(),                   
                biome,                              
                colony.isAutomationEnabled(),       
                colony                             
            };
            
            model.addRow(rowData);
        }
        
        if (selectedRow >= 0 && selectedRow < table.getRowCount()) {
            table.setRowSelectionInterval(selectedRow, selectedRow);
        }

        table.revalidate();
        table.repaint();
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
        private final JButton editBtn;
        private final JButton viewBtn;

        public ActionPanelRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setOpaque(true);

            editBtn = new JButton("Edit");
            editBtn.setMargin(new Insets(2, 8, 2, 8));
            
            viewBtn = new JButton("View");
            viewBtn.setMargin(new Insets(2, 8, 2, 8));

            add(editBtn);
            add(viewBtn);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            return this;
        }
    }

    private class ActionPanelEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private final JButton editBtn;
        private final JButton viewBtn;
        private Colony currentChar;

        public ActionPanelEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            panel.setOpaque(true);

            editBtn = new JButton("Edit");
            editBtn.setMargin(new Insets(2, 8, 2, 8));
            editBtn.addActionListener(e -> {
                fireEditingStopped();
                performEdit(currentChar);
            });

            viewBtn = new JButton("View");
            viewBtn.setMargin(new Insets(2, 8, 2, 8));
            viewBtn.addActionListener(e -> {
                fireEditingStopped();
                performView(currentChar);
            });

            panel.add(editBtn);
            panel.add(viewBtn);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value instanceof Colony) {
                this.currentChar = (Colony) value;
            }
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentChar;
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