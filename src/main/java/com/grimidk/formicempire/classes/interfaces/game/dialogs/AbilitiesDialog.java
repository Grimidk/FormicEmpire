package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyLabourService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiOptionPane;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.MainFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class AbilitiesDialog extends ZeroDialog {

    private final Colony colony;
    private final JPanel listPanel;
    private final JLabel researchPointsLabel;
    private final JScrollPane scrollPane;

    private final Map<JButton, Upgrade> abilityButtons = new HashMap<>();

    public AbilitiesDialog(JFrame owner, Colony colony) {
        super(owner, LanguageStrings.DIALOG_ABILITIES_TITLE, AssetStyles.DEFAULT_DIALOG_SIZE);
        this.colony = colony;
        
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        northPanel.setBackground(AssetStyles.UI_BG_SECONDARY);
        northPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        researchPointsLabel = new JLabel();
        researchPointsLabel.setFont(AssetStyles.FONT_BOLD);
        researchPointsLabel.setForeground(AssetStyles.TEXT_HEADER);
        northPanel.add(researchPointsLabel);
        add(northPanel, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setBackground(AssetStyles.UI_BG_PRIMARY);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(listPanel); 
        scrollPane.getViewport().setBackground(AssetStyles.UI_BG_PRIMARY);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
        
        registerCloseKey(KeyEvent.VK_C);
    }

    @Override
    protected void refreshDialog() {
        listPanel.removeAll();
        abilityButtons.clear();
        
        updateResearchPointsLabel();
        
        boolean hasAnyAbility = false;

        if (colony.hasUpgrade(GameUnlocks.ABILITY_FORCED_FLIGHT)) {
            int currentCost = colony.getNuptialFlightCost();
            String failureReason = getNuptialFailureReason(currentCost);
            boolean enabled = (failureReason == null);
            
            JPanel p = createAbilityPanel(LanguageStrings.get(LanguageStrings.ABILITY_FORCED_FLIGHT), 
                LanguageStrings.format(LanguageStrings.ABILITY_FORCED_FLIGHT_DESC, currentCost),
                currentCost, 
                e -> {
                    if (getOwner() instanceof MainFrame main) {
                        Engine engine = main.getEngine();
                        if (engine != null) {
                            World world = engine.getWorld();
                            if (world != null) {
                                Hex targetHex = null;
                                for(Hex h : world.getHexes()) {
                                    if (h.getColony() == colony) {
                                        targetHex = h;
                                        break;
                                    }
                                }
                                
                                if (targetHex != null) {
                                    colony.forceNuptialFlight(world, targetHex);
                                    refreshDialog(); 
                                } else {
                                    UiOptionPane.showMessageDialog(this, LanguageStrings.get(LanguageStrings.ABILITY_ERROR_LOCATE_COLONY));
                                }
                            }
                        }
                    }
                },
                enabled,
                failureReason
            );
            listPanel.add(p);
            listPanel.add(Box.createVerticalStrut(10));
            hasAnyAbility = true;
        }

        if (colony.hasUpgrade(GameUnlocks.ABILITY_MASS_FLIGHT) && colony.getDynasty() != null) {
            int massCost = colony.getDynasty().getMassNuptialFlightCost();
            boolean hasRP = colony.getResearchPoints() >= massCost;
            String fail = hasRP ? null : LanguageStrings.format(LanguageStrings.ABILITY_ERROR_NOT_ENOUGH_RP, massCost);

            JPanel mp = createAbilityPanel(LanguageStrings.get(LanguageStrings.ABILITY_MASS_FLIGHT),
                LanguageStrings.format(LanguageStrings.ABILITY_MASS_FLIGHT_DESC, massCost),
                massCost,
                e -> {
                    if (getOwner() instanceof MainFrame main) {
                        if (main.getEngine() != null && main.getEngine().getWorld() != null) {
                            colony.getDynasty().runMassNuptialFlight(main.getEngine().getWorld());
                            refreshDialog();
                        }
                    }
                },
                hasRP,
                fail
            );
            listPanel.add(mp);
            listPanel.add(Box.createVerticalStrut(10));
            hasAnyAbility = true;
        }

        if (colony.hasUpgrade(GameUnlocks.ABILITY_PHEROMONE_STORM)) {
            int syrupCost = GameConstants.PHEROMONE_STORM_SYRUP_COST;
            boolean hasSyrup = colony.getSyrups() >= syrupCost;
            boolean alreadyActive = colony.isPheromoneStormActive();
            String fail = alreadyActive
                    ? LanguageStrings.get(LanguageStrings.ABILITY_ERROR_ALREADY_ACTIVE)
                    : (hasSyrup ? null : String.format(
                            LanguageStrings.get(LanguageStrings.ABILITY_ERROR_NOT_ENOUGH_RESOURCE),
                            syrupCost,
                            GameConstants.RESOURCE_SYRUP.getName()));

            JPanel p = createResourceAbilityPanel(
                    GameUnlocks.ABILITY_PHEROMONE_STORM.getName(),
                    GameUnlocks.ABILITY_PHEROMONE_STORM.getDescription(),
                    syrupCost,
                    GameConstants.RESOURCE_SYRUP.getName(),
                    e -> {
                        if (colony.activatePheromoneStorm()) {
                            refreshDialog();
                        }
                    },
                    hasSyrup && !alreadyActive,
                    fail
            );
            listPanel.add(p);
            listPanel.add(Box.createVerticalStrut(10));
            hasAnyAbility = true;
        }

        if (colony.hasUpgrade(GameUnlocks.ABILITY_CREATINE_DIET)) {
            int proteinCost = GameConstants.CREATINE_DIET_PROTEIN_COST;
            boolean hasProtein = colony.getProtein() >= proteinCost;
            boolean alreadyActive = colony.isCreatineDietActive();
            String fail = alreadyActive
                    ? LanguageStrings.get(LanguageStrings.ABILITY_ERROR_ALREADY_ACTIVE)
                    : (hasProtein ? null : String.format(
                            LanguageStrings.get(LanguageStrings.ABILITY_ERROR_NOT_ENOUGH_RESOURCE),
                            proteinCost,
                            GameConstants.RESOURCE_MEAT.getName()));

            JPanel p = createResourceAbilityPanel(
                    GameUnlocks.ABILITY_CREATINE_DIET.getName(),
                    GameUnlocks.ABILITY_CREATINE_DIET.getDescription(),
                    proteinCost,
                    GameConstants.RESOURCE_MEAT.getName(),
                    e -> {
                        if (colony.activateCreatineDiet()) {
                            refreshDialog();
                        }
                    },
                    hasProtein && !alreadyActive,
                    fail
            );
            listPanel.add(p);
            listPanel.add(Box.createVerticalStrut(10));
            hasAnyAbility = true;
        }

        if (!hasAnyAbility) {
            JLabel empty = new JLabel(LanguageStrings.get(LanguageStrings.ABILITY_NO_ABILITIES));
            empty.setForeground(AssetStyles.TEXT_NORMAL);
            empty.setFont(AssetStyles.FONT_NORMAL);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalGlue());
            listPanel.add(empty);
            listPanel.add(Box.createVerticalGlue());
        }

        listPanel.revalidate();
        listPanel.repaint();
    }
    
    private String getNuptialFailureReason(int cost) {
        if (colony.getResearchPoints() < cost) return LanguageStrings.format(LanguageStrings.ABILITY_ERROR_NOT_ENOUGH_RP, cost);
        if (!ColonyLabourService.meetsNuptialRequirements(colony)) {
            if (colony.getDrones().isEmpty()) {
                return LanguageStrings.get(LanguageStrings.ABILITY_ERROR_NO_DRONES);
            }
            return LanguageStrings.get(LanguageStrings.ABILITY_ERROR_NO_BREEDERS);
        }
        return null;
    }

    private void updateResearchPointsLabel() {
        researchPointsLabel.setText(LanguageStrings.format(LanguageStrings.ABILITY_RP_LABEL, colony.getResearchPoints()));
    }

    private JPanel createAbilityPanel(String title, String desc, int rpCost, java.awt.event.ActionListener action, boolean enabled, String tooltip) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(AssetStyles.UI_BG_SECONDARY);
        
        TitledBorder border = new TitledBorder(AssetStyles.PANEL_BORDER, title);
        border.setTitleColor(AssetStyles.TEXT_HEADER);
        border.setTitleFont(AssetStyles.FONT_BOLD);
        panel.setBorder(border);

        JTextArea descriptionArea = new JTextArea(desc);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        descriptionArea.setFocusable(false);
        descriptionArea.setBackground(panel.getBackground());
        descriptionArea.setForeground(AssetStyles.TEXT_NORMAL);
        descriptionArea.setFont(AssetStyles.FONT_NORMAL);
        
        panel.add(descriptionArea, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel();
        actionPanel.setOpaque(false);
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        
        JButton btn = new JButton(LanguageStrings.get(LanguageStrings.UI_TRIGGER));
        btn.setEnabled(enabled);
        AssetStyles.styleButton(btn);
        btn.setFocusable(false);
        btn.addActionListener(action);
        
        if (!enabled && tooltip != null) {
            btn.setToolTipText(tooltip);
        } else {
            btn.setToolTipText(null);
        }
        
        JLabel costLabel = new JLabel(LanguageStrings.format(LanguageStrings.UPGRADE_COST_RP, rpCost));
        costLabel.setForeground(AssetStyles.TEXT_NORMAL);
        costLabel.setFont(AssetStyles.FONT_BOLD);
        costLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        actionPanel.add(costLabel);
        actionPanel.add(Box.createVerticalStrut(5));
        actionPanel.add(btn);
        
        panel.add(actionPanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel createResourceAbilityPanel(String title, String desc, int resourceCost, String resourceName,
            java.awt.event.ActionListener action, boolean enabled, String tooltip) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(AssetStyles.UI_BG_SECONDARY);

        TitledBorder border = new TitledBorder(AssetStyles.PANEL_BORDER, title);
        border.setTitleColor(AssetStyles.TEXT_HEADER);
        border.setTitleFont(AssetStyles.FONT_BOLD);
        panel.setBorder(border);

        JTextArea descriptionArea = new JTextArea(desc);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        descriptionArea.setFocusable(false);
        descriptionArea.setBackground(panel.getBackground());
        descriptionArea.setForeground(AssetStyles.TEXT_NORMAL);
        descriptionArea.setFont(AssetStyles.FONT_NORMAL);
        panel.add(descriptionArea, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel();
        actionPanel.setOpaque(false);
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));

        JButton btn = new JButton(LanguageStrings.get(LanguageStrings.UI_TRIGGER));
        btn.setEnabled(enabled);
        AssetStyles.styleButton(btn);
        btn.setFocusable(false);
        btn.addActionListener(action);

        if (!enabled && tooltip != null) {
            btn.setToolTipText(tooltip);
        } else {
            btn.setToolTipText(null);
        }

        JLabel costLabel = new JLabel(String.format(
                LanguageStrings.get(LanguageStrings.ABILITY_COST_RESOURCE_FMT), resourceCost, resourceName));
        costLabel.setForeground(AssetStyles.TEXT_NORMAL);
        costLabel.setFont(AssetStyles.FONT_BOLD);
        costLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        actionPanel.add(costLabel);
        actionPanel.add(Box.createVerticalStrut(5));
        actionPanel.add(btn);

        panel.add(actionPanel, BorderLayout.EAST);
        return panel;
    }
    
    @Override
    public void liveUpdate() {
        if (!isShowing()) return;
        updateResearchPointsLabel();
        refreshDialog(); 
    }
}
