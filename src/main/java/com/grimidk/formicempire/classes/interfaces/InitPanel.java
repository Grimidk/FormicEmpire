package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.interfaces.menu.MenuHeaderPanel;
import com.grimidk.formicempire.classes.interfaces.menu.MenuSideRailPanel;
import com.grimidk.formicempire.classes.interfaces.menu.VersionLicenseFooter;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashSet;
import java.util.Set;

public class InitPanel extends JPanel {
    private final MainFrame frame;
    
    private final MenuHeaderPanel menuHeader = new MenuHeaderPanel();
    private final JPanel menuColumn = new JPanel(new GridBagLayout());
    private final JPanel westBalance = new JPanel();
    private final MenuSideRailPanel sideRail;
    private final VersionLicenseFooter footer =
            new VersionLicenseFooter(AssetStyles.COLOR_ABSOLUTE_BLACK);
    
    private JButton play;
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
                play.requestFocusInWindow();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        });
    }
    
    private void initComponents() {
        play = createMenuButton(LanguageStrings.UI_PLAY, e -> frame.showCard(MainFrame.CARD_SAVE));
        audit = createMenuButton(LanguageStrings.UI_AUDIT, e -> HelpPanel.showAuditDialog(this));
        roadmap = createMenuButton(LanguageStrings.UI_ROADMAP, e -> HelpPanel.showRoadmapDialog(this));
        credits = createMenuButton(LanguageStrings.UI_CREDITS, e -> HelpPanel.showCreditsDialog(this));
        quit = createMenuButton(LanguageStrings.UI_QUIT, e -> frame.requestExit());

        refreshMenuOptions();
    }

    private JButton createMenuButton(String labelKey, java.awt.event.ActionListener action) {
        JButton button = new JButton(LanguageStrings.get(labelKey));
        button.addActionListener(action);
        setupNavigation(button);
        AssetStyles.styleMenuButton(button);
        return button;
    }

    public void refreshMenuOptions() {
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

        JButton[] buttons = audit.isVisible()
                ? new JButton[] { play, audit, roadmap, credits, quit }
                : new JButton[] { play, roadmap, credits, quit };

        for (int i = 0; i < buttons.length; i++) {
            c.gridy = i + 1;
            menuColumn.add(buttons[i], c);
        }

        menuColumn.revalidate();
        menuColumn.repaint();
    }
    
    public void refreshTranslations() {
        play.setText(LanguageStrings.get(LanguageStrings.UI_PLAY));
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
