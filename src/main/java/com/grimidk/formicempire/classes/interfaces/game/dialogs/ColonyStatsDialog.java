package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.services.ColonyLocationService;
import com.grimidk.formicempire.classes.entities.services.ColonyStatsService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;

public class ColonyStatsDialog extends ZeroDialog {

    private final Colony colony;
    private final Engine engine;
    private final JTabbedPane tabbedPane;
    private JTable resourcesTable;
    private JTable populationTable;
    private JTable deathTable;
    
    private final Runnable refreshTask = this::liveUpdate;

    public ColonyStatsDialog(JFrame owner, Colony colony, Engine engine) {
        super(owner, "Colony Statistics", new Dimension(700, 500));
        this.colony = colony;
        this.engine = engine;
        
        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
        
        initResourceTab();
        initPopulationTab();
        initDeathTab();
        
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
                if (getRowCount() > 0) {
                    Object val = getValueAt(0, columnIndex);
                    if (val != null) return val.getClass();
                }
                return Object.class;
            }
        };
    }

    private void initResourceTab() {
        String[] columns = {"", "Resource", "Current", "Capacity", "Sources", "Prod/Day", "Cons/Day", "Net"};
        resourcesTable = new JTable(createIconModel(columns));
        resourcesTable.setRowHeight(24); 
        JScrollPane scroll = new JScrollPane(resourcesTable);
        tabbedPane.addTab("Economy", scroll);
    }

    private void initPopulationTab() {
        String[] columns = {"", "Type", "Role", "Count"};
        populationTable = new JTable(createIconModel(columns));
        populationTable.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(populationTable);
        tabbedPane.addTab("Population", scroll);
    }

    private void initDeathTab() {
        String[] columns = {"Cause", "Total"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        deathTable = new JTable(model);
        deathTable.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(deathTable);
        tabbedPane.addTab("Mortality", scroll);
    }

    @Override
    protected void refreshDialog() {
        updateResourceData();
        updatePopulationData();
        updateDeathData();
    }

    private void updateResourceData() {
        DefaultTableModel model = (DefaultTableModel) resourcesTable.getModel();
        model.setRowCount(0);

        ColonyStatsService stats = colony.getStatsService();
        ColonyLocationService loc = colony.getLocationService();
        
        int totalProd = stats.getTotalProduction(colony);
        int totalCons = stats.getTotalConsumption(colony);
        
        addResourceRow(model, GameConstants.PLANT_RESOURCE.getIcon(), "Plants", colony.getPlants(), stats.getPlantsCapacity(colony), 
            loc.getSourcesByType(GameConstants.PLANT_RESOURCE).size(), stats.getSourceCapacity(colony),
            GameConstants.PLANT_RESOURCE);
            
        addResourceRow(model, GameConstants.FUNGI_RESOURCE.getIcon(), "Mushrooms", colony.getMushrooms(), stats.getMushroomsCapacity(colony), 
            0, 0, GameConstants.FUNGI_RESOURCE);
            
        addResourceRow(model, GameConstants.MEAT_RESOURCE.getIcon(), "Protein", colony.getProtein(), stats.getProteinCapacity(colony), 
            loc.getSourcesByType(GameConstants.MEAT_RESOURCE).size(), stats.getSourceCapacity(colony),
            GameConstants.MEAT_RESOURCE);
            
        addResourceRow(model, GameConstants.WATER_RESOURCE.getIcon(), "Water", colony.getWater(), stats.getWaterCapacity(colony), 
            loc.getSourcesByType(GameConstants.WATER_RESOURCE).size(), stats.getSourceCapacity(colony),
            GameConstants.WATER_RESOURCE);

        addResourceRow(model, GameConstants.ROCK_RESOURCE.getIcon(), "Minerals", colony.getMinerals(), stats.getMineralsCapacity(colony), 
            loc.getSourcesByType(GameConstants.ROCK_RESOURCE).size(), stats.getSourceCapacity(colony),
            GameConstants.ROCK_RESOURCE);
            
        addResourceRow(model, GameConstants.SYRUP_RESOURCE.getIcon(), "Syrups", colony.getSyrups(), stats.getSyrupsCapacity(colony), 
            0, 0, null);
            
        addResourceRow(model, GameConstants.RESIN_RESOURCE.getIcon(), "Resins", colony.getResins(), stats.getResinsCapacity(colony), 
            0, 0, null);

        model.addRow(new Object[]{null, "------", "---", "---", "---", "---", "---", "---"});
        model.addRow(new Object[]{null, "Total Food", "---", "---", "---", totalProd, totalCons, (totalProd - totalCons)});
    }

    private void addResourceRow(DefaultTableModel model, ImageIcon icon, String name, int current, int cap, int sources, int maxSources, ResourceType type) {
        
        String sourceStr = (maxSources > 0) ? sources + " / " + maxSources : "N/A";
        String prodStr = "---";
        String consStr = "---";
        String netStr = "---";

        if (type == GameConstants.FUNGI_RESOURCE) {
             prodStr = String.valueOf(colony.getStatsService().getTotalProduction(colony));
             consStr = String.valueOf(colony.getStatsService().getTotalConsumption(colony));
             int net = Integer.parseInt(prodStr) - Integer.parseInt(consStr);
             netStr = (net >= 0 ? "+" : "") + net;
        }

        model.addRow(new Object[]{icon, name, current, cap, sourceStr, prodStr, consStr, netStr});
    }

    private void updatePopulationData() {
        DefaultTableModel model = (DefaultTableModel) populationTable.getModel();
        model.setRowCount(0);

        for (AntType type : GameConstants.getAntTypes()) {
            List<Ant> ants = colony.getAntsByType(type);
            if (ants.isEmpty()) continue;

            model.addRow(new Object[]{type.getIcon(), type.getName(), "Total", ants.size()});

            if (type == GameConstants.TYPE_WORKER || type == GameConstants.TYPE_SOLDIER || 
                type == GameConstants.TYPE_MAJOR || type == GameConstants.TYPE_PRINCESS || 
                type == GameConstants.TYPE_QUEEN) {
                
                for (AntRole role : GameConstants.getAntRoles()) {
                    if (role.getAntType() == type) {
                        int count = colony.getAssignedRoleCount(role);
                        if (count > 0) {
                            model.addRow(new Object[]{null, "", role.getName(), count});
                        }
                    }
                }
            }
            model.addRow(new Object[]{null, "------", "------", "------"});
        }
    }

    private void updateDeathData() {
        DefaultTableModel model = (DefaultTableModel) deathTable.getModel();
        model.setRowCount(0);

        if (colony.getTrackingService() == null) {
            model.addRow(new Object[]{"Tracking Service Not Initialized", 0});
            return;
        }

        Map<String, Integer> stats = colony.getTrackingService().getDeathStatistics();
        
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            model.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
        
        model.addRow(new Object[]{"------", "------"});
        model.addRow(new Object[]{"Total Deaths", colony.getTotalDeaths()});
    }
}