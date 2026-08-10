package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.unlocks.Synergy;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastySynergyService;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.EdgeTriggeredKeyBindings;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpgradeDialog extends ZeroDialog {

    public static final int TAB_RESEARCH = 0;
    public static final int TAB_BUILD = 1;
    public static final int TAB_ASSIMILATION = 2;
    public static final int TAB_SYNERGY = 3;

    private final Colony colony;
    private final Engine engine;
    private final JTabbedPane tabbedPane;
    
    private ResearchTreePanel researchPanel;
    private BuildingTreePanel buildPanel;
    private JPanel assimilationPanel;
    private SynergyPanel synergyPanel;

    private final Map<Integer, Integer> tabIndexMap = new HashMap<>();
    private final Map<Integer, JPanel> lazyStubs = new HashMap<>();
    
    private int targetTab = -1;
    private boolean materializingTab;

    public UpgradeDialog(JFrame owner, Colony colony, Engine engine) {
        super(owner, LanguageStrings.DIALOG_UPGRADES_TITLE, AssetStyles.DEFAULT_DIALOG_SIZE);
        this.colony = colony;
        this.engine = engine;
        setHideOnClose(true);

        tabbedPane = new JTabbedPane();
        AssetStyles.styleTabbedPane(tabbedPane);
        tabbedPane.addChangeListener(e -> {
            if (!materializingTab) {
                materializeSelectedTab();
            }
        });
        add(tabbedPane, BorderLayout.CENTER);

        initKeyBindings();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                tabbedPane.requestFocusInWindow();
            }
        });
    }

    public Colony getColony() {
        return colony;
    }

    public void showDialog(int preferredType) {
        this.targetTab = preferredType;
        super.showDialog();
    }

    public void setTab(int tabType) {
        if (tabIndexMap.containsKey(tabType)) {
            tabbedPane.setSelectedIndex(tabIndexMap.get(tabType));
            materializeSelectedTab();
        }
    }
    
    public boolean isTabOpen(int tabType) {
        if (!isShowing()) return false;
        Integer index = tabIndexMap.get(tabType);
        return index != null && tabbedPane.getSelectedIndex() == index;
    }

    @Override
    public void refreshTheme() {
        super.refreshTheme();
        AssetStyles.styleTabbedPane(tabbedPane);
        tabbedPane.updateUI();
        if (researchPanel != null) {
            researchPanel.updateUI();
        }
    }

    @Override
    protected void refreshDialog() {
        tabbedPane.removeAll();
        tabIndexMap.clear();
        lazyStubs.clear();

        int currentIndex = 0;

        if (colony.hasUpgrade(GameUnlocks.ABILITY_RESEARCH)) {
            addUpgradeTab(
                    TAB_RESEARCH,
                    LanguageStrings.get(LanguageStrings.TAB_RESEARCH),
                    GameUnlocks.ABILITY_RESEARCH.getIcon(),
                    currentIndex++);
        }

        if (colony.hasUpgrade(GameUnlocks.ROLE_BUILDER)) {
            addUpgradeTab(
                    TAB_BUILD,
                    LanguageStrings.get(LanguageStrings.TAB_CONSTRUCTION),
                    GameUnlocks.ABILITY_BUILD.getIcon(),
                    currentIndex++);
        }

        if (GameUnlocks.shouldShowAssimilationUi(colony.getDynasty())) {
            addUpgradeTab(
                    TAB_ASSIMILATION,
                    LanguageStrings.get(LanguageStrings.TAB_ASSIMILATIONS),
                    GameUnlocks.ABILITY_ASSIMILATION.getIcon(),
                    currentIndex++);
        }

        if (colony.hasUpgrade(GameUnlocks.ABILITY_SYNERGY)) {
            addUpgradeTab(
                    TAB_SYNERGY,
                    LanguageStrings.get(LanguageStrings.TAB_SYNERGIES),
                    GameUnlocks.ABILITY_SYNERGY.getIcon(),
                    currentIndex++);
        }
        
        if (targetTab != -1 && tabIndexMap.containsKey(targetTab)) {
            materializingTab = true;
            try {
                tabbedPane.setSelectedIndex(tabIndexMap.get(targetTab));
            } finally {
                materializingTab = false;
            }
            targetTab = -1;
        }
        materializeSelectedTab();
    }

    private void addUpgradeTab(int tabType, String title, Icon icon, int index) {
        tabIndexMap.put(tabType, index);
        if (shouldMaterialize(tabType)) {
            tabbedPane.addTab(title, icon, materializePanel(tabType));
            return;
        }
        JPanel stub = createLazyStub();
        lazyStubs.put(tabType, stub);
        tabbedPane.addTab(title, icon, stub);
    }

    private boolean shouldMaterialize(int tabType) {
        return targetTab == tabType;
    }

    private void materializeSelectedTab() {
        Integer selected = null;
        int selectedIndex = tabbedPane.getSelectedIndex();
        for (Map.Entry<Integer, Integer> entry : tabIndexMap.entrySet()) {
            if (entry.getValue() != null && entry.getValue() == selectedIndex) {
                selected = entry.getKey();
                break;
            }
        }
        if (selected == null) {
            return;
        }
        Component current = tabbedPane.getComponentAt(selectedIndex);
        JPanel stub = lazyStubs.get(selected);
        if (stub != null && current == stub) {
            materializingTab = true;
            try {
                tabbedPane.setComponentAt(selectedIndex, materializePanel(selected));
            } finally {
                materializingTab = false;
            }
            lazyStubs.remove(selected);
            return;
        }
        refreshPanelData(selected);
    }

    private Component materializePanel(int tabType) {
        switch (tabType) {
            case TAB_RESEARCH -> {
                if (researchPanel == null) {
                    researchPanel = new ResearchTreePanel(colony, engine, this::onTreePanelChanged);
                }
                researchPanel.updateData();
                return researchPanel;
            }
            case TAB_BUILD -> {
                if (buildPanel == null) {
                    buildPanel = new BuildingTreePanel(colony, this::onTreePanelChanged);
                }
                buildPanel.updateData();
                return buildPanel;
            }
            case TAB_ASSIMILATION -> {
                if (assimilationPanel == null) {
                    assimilationPanel = new AssimilationPanel(colony);
                }
                ((AssimilationPanel) assimilationPanel).updateData();
                return assimilationPanel;
            }
            case TAB_SYNERGY -> {
                if (synergyPanel == null) {
                    synergyPanel = new SynergyPanel(colony);
                }
                synergyPanel.updateData();
                return synergyPanel;
            }
            default -> {
                return createLazyStub();
            }
        }
    }

    private void refreshPanelData(int tabType) {
        switch (tabType) {
            case TAB_RESEARCH -> {
                if (researchPanel != null) {
                    researchPanel.updateData();
                }
            }
            case TAB_BUILD -> {
                if (buildPanel != null) {
                    buildPanel.updateData();
                }
            }
            case TAB_ASSIMILATION -> {
                if (assimilationPanel != null) {
                    ((AssimilationPanel) assimilationPanel).updateData();
                }
            }
            case TAB_SYNERGY -> {
                if (synergyPanel != null) {
                    synergyPanel.updateData();
                }
            }
            default -> {
            }
        }
    }

    private JPanel createLazyStub() {
        JPanel stub = new JPanel(new BorderLayout());
        stub.setBackground(AssetStyles.BACKGROUND_COLOR);
        return stub;
    }

    private void onTreePanelChanged() {
        if (tabsNeedRebuild()) {
            int preferred = -1;
            for (Map.Entry<Integer, Integer> entry : tabIndexMap.entrySet()) {
                if (entry.getValue() != null && entry.getValue() == tabbedPane.getSelectedIndex()) {
                    preferred = entry.getKey();
                    break;
                }
            }
            if (preferred >= 0) {
                targetTab = preferred;
            }
            refreshDialog();
            return;
        }
        if (researchPanel != null) {
            researchPanel.updateData();
        }
        if (buildPanel != null) {
            buildPanel.updateData();
        }
    }

    private boolean tabsNeedRebuild() {
        boolean wantResearch = colony.hasUpgrade(GameUnlocks.ABILITY_RESEARCH);
        boolean wantBuild = colony.hasUpgrade(GameUnlocks.ROLE_BUILDER);
        boolean wantAssimilation = GameUnlocks.shouldShowAssimilationUi(colony.getDynasty());
        boolean wantSynergy = colony.hasUpgrade(GameUnlocks.ABILITY_SYNERGY);
        return wantResearch != tabIndexMap.containsKey(TAB_RESEARCH)
                || wantBuild != tabIndexMap.containsKey(TAB_BUILD)
                || wantAssimilation != tabIndexMap.containsKey(TAB_ASSIMILATION)
                || wantSynergy != tabIndexMap.containsKey(TAB_SYNERGY);
    }

    private JPanel createPlaceholderPanel(String message) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(AssetStyles.BACKGROUND_COLOR);
        JLabel label = new JLabel(message);
        label.setForeground(AssetStyles.FONT_COLOR);
        p.add(label);
        return p;
    }

    @Override
    public void liveUpdate() {
        if (!isShowing()) return;

        Component selected = tabbedPane.getSelectedComponent();
        if (selected instanceof LiveUpdatePanel) {
            ((LiveUpdatePanel) selected).liveUpdate();
        }
    }

    private void initKeyBindings() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        bindTabToggle(inputMap, actionMap, KeyEvent.VK_Y, "toggleResearch", TAB_RESEARCH);
        bindTabToggle(inputMap, actionMap, KeyEvent.VK_U, "toggleBuild", TAB_BUILD);
        bindTabToggle(inputMap, actionMap, KeyEvent.VK_I, "toggleAssimilation", TAB_ASSIMILATION);
        bindTabToggle(inputMap, actionMap, KeyEvent.VK_O, "toggleSynergy", TAB_SYNERGY);
    }

    private void bindTabToggle(InputMap inputMap, ActionMap actionMap, int keyCode, String actionId, int tab) {
        EdgeTriggeredKeyBindings.bind(inputMap, actionMap, keyCode, actionId, () -> {
            if (isTabOpen(tab)) {
                requestClose();
            } else if (tabIndexMap.containsKey(tab)) {
                tabbedPane.setSelectedIndex(tabIndexMap.get(tab));
                materializeSelectedTab();
            }
        });
    }

    @Override
    protected boolean consumeEscape() {
        Component selected = tabbedPane.getSelectedComponent();
        if (selected instanceof ResearchTreePanel research) {
            return research.closeDetailIfOpen();
        }
        if (selected instanceof BuildingTreePanel build) {
            return build.closeDetailIfOpen();
        }
        return false;
    }

    interface LiveUpdatePanel {
        void liveUpdate();
        void updateData();
    }

    private class AssimilationPanel extends JPanel implements LiveUpdatePanel {
        private final Colony colony;
        private final JPanel listPanel;
        private final JScrollPane scrollPane;
        private final JLabel statusLabel;
        private final JLabel geneticIntegrityLabel;
        private final Map<JButton, Assimilation> buttonMap = new HashMap<>();

        public AssimilationPanel(Colony colony) {
            super(new BorderLayout());
            this.colony = colony;
            setBackground(AssetStyles.BACKGROUND_COLOR);

            JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
            northPanel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            statusLabel = new JLabel(LanguageStrings.format(LanguageStrings.ASSIMILATION_CURRENT, LanguageStrings.get(LanguageStrings.ASSIMILATION_NONE)));
            statusLabel.setFont(AssetStyles.FONT_BOLD);
            statusLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
            northPanel.add(statusLabel);

            geneticIntegrityLabel = new JLabel();
            geneticIntegrityLabel.setFont(AssetStyles.FONT_BOLD);
            geneticIntegrityLabel.setForeground(AssetStyles.FONT_COLOR_VALUE);
            geneticIntegrityLabel.setIcon(GameConstants.ICON_STAT_GENETIC_INTEGRITY);
            geneticIntegrityLabel.setIconTextGap(8);
            northPanel.add(geneticIntegrityLabel);
            add(northPanel, BorderLayout.NORTH);

            listPanel = new JPanel();
            listPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            scrollPane = new JScrollPane(listPanel);
            scrollPane.getViewport().setBackground(AssetStyles.BACKGROUND_COLOR);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            add(scrollPane, BorderLayout.CENTER);
        }

        @Override
        public void updateData() {
            listPanel.removeAll();
            buttonMap.clear();

            Dynasty dynasty = colony.getDynasty();
            if (dynasty == null) return;

            updateStatusLabel();

            if (dynasty.getCurrentAssimilation() != null) {
                listPanel.add(createProgressPanel(dynasty.getCurrentAssimilation()));
            } else {
                List<Assimilation> available = new ArrayList<>();

                for (Assimilation a : GameUnlocks.getAssimilations()) {
                    if (GameUnlocks.isAssimilationAvailable(dynasty, a)) {
                        available.add(a);
                    }
                }

                if (available.isEmpty()) {
                    JLabel emptyLabel = new JLabel(LanguageStrings.get(LanguageStrings.ASSIMILATION_NO_GENOMES));
                    emptyLabel.setForeground(AssetStyles.FONT_COLOR);
                    listPanel.add(emptyLabel);
                } else {
                    for (Assimilation a : available) {
                        listPanel.add(createAssimilationCard(a));
                        listPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                    }
                }
            }

            listPanel.revalidate();
            listPanel.repaint();
        }

        private void updateGeneticIntegrityDisplay(Dynasty dynasty) {
            geneticIntegrityLabel.setText(String.format("%s: %.1f%%",
                    LanguageStrings.get(LanguageStrings.STAT_GENETIC_INTEGRITY),
                    dynasty.getGeneticIntegrity()));
            geneticIntegrityLabel.setToolTipText(dynasty.buildGeneticIntegrityTooltip());
        }

        private void updateStatusLabel() {
            Dynasty d = colony.getDynasty();
            if (d == null) return;
            if (d.getCurrentAssimilation() != null) {
                statusLabel.setText(LanguageStrings.format(LanguageStrings.ASSIMILATION_CURRENT, d.getCurrentAssimilation().getName()));
            } else {
                statusLabel.setText(LanguageStrings.format(LanguageStrings.ASSIMILATION_CURRENT, LanguageStrings.get(LanguageStrings.ASSIMILATION_NONE)));
            }
            updateGeneticIntegrityDisplay(d);
        }

        private JPanel createAssimilationCard(Assimilation a) {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            
            TitledBorder border = new TitledBorder(AssetStyles.PANEL_BORDER, a.getName());
            border.setTitleColor(AssetStyles.FONT_COLOR_HEADER);
            border.setTitleFont(AssetStyles.FONT_BOLD);
            panel.setBorder(border);

            JTextArea desc = new JTextArea(a.getDescription());
            desc.setWrapStyleWord(true);
            desc.setLineWrap(true);
            desc.setEditable(false);
            desc.setForeground(AssetStyles.FONT_COLOR);
            desc.setFont(AssetStyles.FONT_NORMAL);
            desc.setBackground(panel.getBackground());
            panel.add(desc, BorderLayout.CENTER);

            JPanel east = new JPanel();
            east.setOpaque(false);
            east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));
            
            JLabel cost = new JLabel(LanguageStrings.format(LanguageStrings.ASSIMILATION_TARGET,
                    colony.getDynasty().getAssimilationTargetCost()));
            cost.setForeground(AssetStyles.FONT_COLOR_VALUE);
            cost.setFont(AssetStyles.FONT_BOLD);
            cost.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JButton btn = new JButton(LanguageStrings.get(LanguageStrings.UI_BEGIN));
            btn.setFocusable(false);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            AssetStyles.styleButton(btn);
            btn.addActionListener(e -> {
                colony.getDynasty().setCurrentAssimilation(a);
                colony.getDynasty().setAssimilationProgress(0);
                updateData();
            });

            east.add(cost);
            east.add(Box.createRigidArea(new Dimension(0, 5)));
            east.add(btn);
            panel.add(east, BorderLayout.EAST);

            return panel;
        }

        private JPanel createProgressPanel(Assimilation a) {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            
            TitledBorder border = new TitledBorder(AssetStyles.PANEL_BORDER, LanguageStrings.format(LanguageStrings.ASSIMILATION_ACTIVE, a.getName()));
            border.setTitleColor(AssetStyles.FONT_COLOR_HEADER);
            border.setTitleFont(AssetStyles.FONT_BOLD);
            panel.setBorder(border);

            double prog = colony.getDynasty().getAssimilationProgress();
            int targetCost = colony.getDynasty().getAssimilationTargetCost();
            int percent = targetCost > 0 ? (int) ((prog / targetCost) * 100) : 0;

            JProgressBar bar = new JProgressBar(0, 100);
            AssetStyles.styleProgressBar(bar);
            bar.setValue(percent);
            bar.setStringPainted(true);
            bar.setString(LanguageStrings.format(LanguageStrings.ASSIMILATION_PROGRESS, prog, targetCost, percent));
            panel.add(bar, BorderLayout.CENTER);

            JButton cancel = new JButton(LanguageStrings.get(LanguageStrings.UI_CANCEL));
            cancel.setFocusable(false);
            AssetStyles.styleButton(cancel);
            cancel.addActionListener(e -> {
                colony.getDynasty().setCurrentAssimilation(null);
                colony.getDynasty().setAssimilationProgress(0);
                updateData();
            });
            
            JPanel eastPanel = new JPanel(new GridBagLayout());
            eastPanel.setOpaque(false);
            eastPanel.add(cancel);
            panel.add(eastPanel, BorderLayout.EAST);

            JLabel info = new JLabel(LanguageStrings.get(LanguageStrings.ASSIMILATION_INFO));
            info.setForeground(AssetStyles.FONT_COLOR);
            info.setFont(AssetStyles.FONT_NORMAL);
            info.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(info, BorderLayout.SOUTH);

            return panel;
        }

        @Override
        public void liveUpdate() {
            Dynasty d = colony.getDynasty();
            if (d == null) {
                return;
            }
            updateGeneticIntegrityDisplay(d);
            if (d.getCurrentAssimilation() == null) {
                if (listPanel.getComponentCount() > 0 && listPanel.getComponent(0) instanceof JPanel) {
                    JPanel p = (JPanel) listPanel.getComponent(0);
                    if (p.getBorder() instanceof TitledBorder && ((TitledBorder)p.getBorder()).getTitle().contains("Assimilating:")) {
                        updateData();
                    }
                }
                return;
            }

            Assimilation a = d.getCurrentAssimilation();
            if (listPanel.getComponentCount() > 0 && listPanel.getComponent(0) instanceof JPanel) {
                JPanel p = (JPanel) listPanel.getComponent(0);
                if (p.getComponentCount() > 0 && p.getComponent(0) instanceof JProgressBar) {
                    JProgressBar bar = (JProgressBar) p.getComponent(0);
                    double prog = d.getAssimilationProgress();
                    int targetCost = d.getAssimilationTargetCost();
                    int percent = targetCost > 0 ? (int) ((prog / targetCost) * 100) : 0;
                    bar.setValue(percent);
                    bar.setString(LanguageStrings.format(LanguageStrings.ASSIMILATION_PROGRESS, prog, targetCost, percent));
                }
            }
        }
    }

    private class SynergyPanel extends JPanel implements LiveUpdatePanel {
        private final Colony colony;
        private final JPanel listPanel;
        private final JScrollPane scrollPane;

        public SynergyPanel(Colony colony) {
            super(new BorderLayout());
            this.colony = colony;
            setBackground(AssetStyles.BACKGROUND_COLOR);

            listPanel = new JPanel();
            listPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            scrollPane = new JScrollPane(listPanel);
            scrollPane.getViewport().setBackground(AssetStyles.BACKGROUND_COLOR);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            add(scrollPane, BorderLayout.CENTER);
        }

        @Override
        public void updateData() {
            listPanel.removeAll();
            Dynasty dynasty = colony.getDynasty();
            if (dynasty == null) {
                listPanel.revalidate();
                listPanel.repaint();
                return;
            }

            List<Synergy> visible = DynastySynergyService.getVisibleSynergies(dynasty);
            if (visible.isEmpty()) {
                JLabel emptyLabel = new JLabel(LanguageStrings.get(LanguageStrings.SYNERGY_NONE_IN_PROGRESS));
                emptyLabel.setForeground(AssetStyles.FONT_COLOR);
                listPanel.add(emptyLabel);
            } else {
                for (Synergy synergy : visible) {
                    listPanel.add(createSynergyCard(dynasty, synergy));
                    listPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                }
            }

            listPanel.revalidate();
            listPanel.repaint();
            SwingUtilities.invokeLater(() -> scrollPane.getViewport().setViewPosition(new Point(0, 0)));
        }

        private JPanel createSynergyCard(Dynasty dynasty, Synergy synergy) {
            boolean active = DynastySynergyService.isActive(dynasty, synergy);
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBackground(AssetStyles.BACKGROUND_SECONDARY);

            String title = synergy.getName();
            if (active) {
                title += " — " + LanguageStrings.get(LanguageStrings.SYNERGY_STATUS_ACTIVE);
            } else {
                title += " — " + LanguageStrings.format(
                        LanguageStrings.SYNERGY_STATUS_PARTIAL,
                        DynastySynergyService.countRequirementsMet(dynasty, synergy),
                        synergy.getRequirementCount());
            }
            TitledBorder border = new TitledBorder(AssetStyles.PANEL_BORDER, title);
            border.setTitleColor(active ? AssetStyles.FONT_COLOR_HEADER : AssetStyles.FONT_COLOR_VALUE);
            border.setTitleFont(AssetStyles.FONT_BOLD);
            panel.setBorder(border);

            JPanel infoPanel = new JPanel();
            infoPanel.setOpaque(false);
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

            for (Upgrade requirement : synergy.getRequirements()) {
                infoPanel.add(createRequirementLabel(dynasty, requirement));
                infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
            }
            infoPanel.add(Box.createRigidArea(new Dimension(0, 6)));

            JTextArea descriptionArea = new JTextArea(synergy.getDescription());
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setLineWrap(true);
            descriptionArea.setEditable(false);
            descriptionArea.setFocusable(false);
            descriptionArea.setBackground(panel.getBackground());
            descriptionArea.setForeground(AssetStyles.FONT_COLOR);
            descriptionArea.setFont(AssetStyles.FONT_NORMAL);
            descriptionArea.setBorder(null);
            infoPanel.add(descriptionArea);
            panel.add(infoPanel, BorderLayout.CENTER);

            return panel;
        }

        private JLabel createRequirementLabel(Dynasty dynasty, Upgrade requirement) {
            boolean met = requirement != null && dynasty.hasUpgrade(requirement);
            String key = met ? LanguageStrings.SYNERGY_REQUIREMENT_MET_FMT : LanguageStrings.SYNERGY_REQUIREMENT_MISSING_FMT;
            JLabel label = new JLabel(LanguageStrings.format(key, requirement.getFlavorName()));
            label.setForeground(met ? AssetStyles.FONT_COLOR_HEADER : AssetStyles.FONT_COLOR_VALUE);
            label.setFont(AssetStyles.FONT_NORMAL);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            return label;
        }

        @Override
        public void liveUpdate() {
        }
    }
}
