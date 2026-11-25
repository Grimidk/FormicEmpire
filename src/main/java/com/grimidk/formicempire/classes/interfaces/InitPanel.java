package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.interfaces.menu.MenuHeaderPanel;
import com.grimidk.formicempire.classes.interfaces.menu.MenuSideRailPanel;
import com.grimidk.formicempire.classes.interfaces.menu.VersionLicenseFooter;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.BattleSimulatorDialog;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.WorldMakingDialog;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InitPanel extends JPanel {
    private final MainFrame frame;
    
    private final MenuHeaderPanel menuHeader = new MenuHeaderPanel();
    private final JPanel menuColumn = new JPanel(new GridBagLayout());
    private final JPanel westBalance = new JPanel();
    private final MenuSideRailPanel sideRail;
    private final VersionLicenseFooter footer =
            new VersionLicenseFooter(AssetStyles.COLOR_ABSOLUTE_BLACK);
    
    private JButton continueGame;
    private JButton loadGame;
    private JButton play;
    private JButton worldMaking;
    private JButton battleSimulator;
    private JButton audit;
    private JButton roadmap;
    private JButton credits;
    private JButton quit;

    public InitPanel(MainFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout());

        menuColumn.setOpaque(false);
        westBalance.setOpaque(false);
        sideRail = new MenuSideRailPanel(frame);
        add(westBalance, BorderLayout.WEST);
        add(menuColumn, BorderLayout.CENTER);
        add(sideRail, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
        syncSideRailBalance();
        sideRail.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                syncSideRailBalance();
            }
        });
        
        initComponents();
        
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                if (!isShowing()) {
                    return;
                }
                refreshMenuOptions();
                JButton primaryButton = continueGame.isVisible() ? continueGame : play;
                primaryButton.requestFocusInWindow();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        });
    }
    
    private void initComponents() {
        continueGame = createMenuButton(LanguageStrings.UI_CONTINUE, e -> onContinue());
        loadGame = createMenuButton(LanguageStrings.UI_LOAD_GAME, e -> frame.showCard(MainFrame.CARD_SAVE));
        play = createMenuButton(LanguageStrings.UI_PLAY, e -> frame.showCard(MainFrame.CARD_SAVE));
        worldMaking = createMenuButton(LanguageStrings.UI_WORLD_MAKING, e -> new WorldMakingDialog(frame, frame.getEngine()).setVisible(true));
        battleSimulator = createMenuButton(LanguageStrings.UI_BATTLE_SIMULATOR, e -> new BattleSimulatorDialog(frame, frame.getEngine()).showDialog());
        audit = createMenuButton(LanguageStrings.UI_AUDIT, e -> HelpPanel.showAuditDialog(this));
        roadmap = createMenuButton(LanguageStrings.UI_ROADMAP, e -> HelpPanel.showRoadmapDialog(this));
        credits = createMenuButton(LanguageStrings.UI_CREDITS, e -> HelpPanel.showCreditsDialog(this));
        quit = createMenuButton(LanguageStrings.UI_QUIT, e -> frame.requestExit());

        refreshMenuOptions();
    }

    private void onContinue() {
        int slotId = frame.getEngine().getSaveManager()
                .resolveContinueSlotId(frame.getEngine().getLastPlayedSlotId());
        if (slotId <= 0) {
            refreshMenuOptions();
            return;
        }
        frame.getSaveSelectPanel().loadExistingSlot(slotId);
    }

    private JButton createMenuButton(String labelKey, java.awt.event.ActionListener action) {
        JButton button = new JButton(LanguageStrings.get(labelKey));
        button.addActionListener(action);
        setupNavigation(button);
        AssetStyles.styleMenuButton(button);
        return button;
    }

    public void refreshMenuOptions() {
        boolean hasSaves = frame.getEngine().getSaveManager().hasAnySaveSlot();
        continueGame.setVisible(hasSaves);
        loadGame.setVisible(hasSaves);
        play.setVisible(!hasSaves);
        boolean showAudit = frame.getEngine().isShowAuditMenu();
        audit.setVisible(showAudit);
        layoutMenuButtons();
    }

    private void syncSideRailBalance() {
        int railWidth = sideRail.getPreferredSize().width;
        Dimension balanceSize = new Dimension(railWidth, 0);
        westBalance.setPreferredSize(balanceSize);
        westBalance.setMinimumSize(balanceSize);
        westBalance.setMaximumSize(new Dimension(railWidth, Integer.MAX_VALUE));
    }

    private void layoutMenuButtons() {
        menuColumn.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;

        c.gridy = 0;
        menuColumn.add(menuHeader, c);

        List<JButton> buttons = new ArrayList<>();
        if (continueGame.isVisible()) {
            buttons.add(continueGame);
            buttons.add(loadGame);
        } else {
            buttons.add(play);
        }
        buttons.add(worldMaking);
        buttons.add(battleSimulator);
        if (audit.isVisible()) {
            buttons.add(audit);
        }
        buttons.add(roadmap);
        buttons.add(credits);
        buttons.add(quit);

        for (int i = 0; i < buttons.size(); i++) {
            c.gridy = i + 1;
            menuColumn.add(buttons.get(i), c);
        }

        menuColumn.revalidate();
        menuColumn.repaint();
    }
    
    public void refreshTranslations() {
        continueGame.setText(LanguageStrings.get(LanguageStrings.UI_CONTINUE));
        loadGame.setText(LanguageStrings.get(LanguageStrings.UI_LOAD_GAME));
        play.setText(LanguageStrings.get(LanguageStrings.UI_PLAY));
        worldMaking.setText(LanguageStrings.get(LanguageStrings.UI_WORLD_MAKING));
        battleSimulator.setText(LanguageStrings.get(LanguageStrings.UI_BATTLE_SIMULATOR));
        audit.setText(LanguageStrings.get(LanguageStrings.UI_AUDIT));
        roadmap.setText(LanguageStrings.get(LanguageStrings.UI_ROADMAP));
        credits.setText(LanguageStrings.get(LanguageStrings.UI_CREDITS));
        quit.setText(LanguageStrings.get(LanguageStrings.UI_QUIT));
        sideRail.refreshTranslations();
        footer.refreshTranslations();
    }

    public void refreshTheme() {
        AssetStyles.applyThemeToContainer(this);
        sideRail.refreshTheme();
        footer.applyTextColor();
    }

    private void setupNavigation(JButton button) {
        button.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "pressed");
        button.getActionMap().put("pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                button.doClick();
            }
        });

        Set<AWTKeyStroke> forwardKeys = new HashSet<>(button.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        forwardKeys.add(KeyStroke.getKeyStroke("DOWN"));
        forwardKeys.add(KeyStroke.getKeyStroke("RIGHT"));
        button.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);

        Set<AWTKeyStroke> backwardKeys = new HashSet<>(button.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        backwardKeys.add(KeyStroke.getKeyStroke("UP"));
        backwardKeys.add(KeyStroke.getKeyStroke("LEFT"));
        button.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardKeys);
    }
}
