package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.MainFrame;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.EdgeTriggeredKeyBindings;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiDialogUtils;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Supplier;

public class WorldMakingDialog extends JDialog {

    private final MainFrame mainFrame;
    private final Engine engine;
    private boolean launchingChild = false;

    public WorldMakingDialog(MainFrame owner, Engine engine) {
        super(owner, LanguageStrings.get(LanguageStrings.UI_WORLD_MAKING), true);
        this.mainFrame = owner;
        this.engine = engine;
        if (this.mainFrame != null) {
            this.mainFrame.setMainMenuLoaded(false);
        }

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(540, 420));
        setPreferredSize(new Dimension(600, 480));

        JPanel contentPane = new JPanel(new BorderLayout(0, 16));
        contentPane.setBackground(AssetStyles.UI_BG_PRIMARY);
        contentPane.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(LanguageStrings.get(LanguageStrings.UI_WORLD_MAKING), SwingConstants.CENTER);
        titleLabel.setFont(AssetStyles.FONT_BOLD.deriveFont(22f));
        titleLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel(LanguageStrings.get(LanguageStrings.UI_WORLD_MAKING_DESC), SwingConstants.CENTER);
        descLabel.setFont(AssetStyles.FONT_NORMAL);
        descLabel.setForeground(AssetStyles.FONT_COLOR);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(descLabel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JButton worldBuilderBtn = createOptionButton(
                LanguageStrings.get(LanguageStrings.UI_WORLD_BUILDER),
                LanguageStrings.get(LanguageStrings.UI_WORLD_BUILDER_DESC),
                e -> openChildDialog(() -> new WorldBuilderDialog(mainFrame, engine)));

        JButton algorithmBtn = createOptionButton(
                LanguageStrings.get(LanguageStrings.UI_WORLD_ALGORITHM),
                LanguageStrings.get(LanguageStrings.UI_WORLD_ALGORITHM_DESC),
                e -> openChildDialog(() -> new MultiMapGalleryDialog(mainFrame, engine)));

        JButton savedWorldsBtn = createOptionButton(
                LanguageStrings.get(LanguageStrings.SAVED_MAP_GALLERY_TITLE),
                LanguageStrings.get(LanguageStrings.SAVED_MAP_CHOOSE_MODE_MSG),
                e -> openChildDialog(() -> new SavedMapSelectionDialog(mainFrame, engine, false)));

        buttonPanel.add(worldBuilderBtn);
        buttonPanel.add(Box.createVerticalStrut(14));
        buttonPanel.add(algorithmBtn);
        buttonPanel.add(Box.createVerticalStrut(14));
        buttonPanel.add(savedWorldsBtn);

        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        JButton closeBtn = new JButton(LanguageStrings.get(LanguageStrings.UI_CLOSE));
        AssetStyles.styleButton(closeBtn);
        closeBtn.addActionListener(e -> dispose());
        footerPanel.add(closeBtn);

        contentPane.add(headerPanel, BorderLayout.NORTH);
        contentPane.add(buttonPanel, BorderLayout.CENTER);
        contentPane.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
        pack();
        setSize(600, 480);
        UiDialogUtils.prepareDialog(this, owner);
        setLocationRelativeTo(owner);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (!launchingChild && mainFrame != null && !MainFrame.CARD_GAME.equals(mainFrame.getCurrentCard())) {
                    mainFrame.setMainMenuLoaded(true);
                }
            }
        });
        EdgeTriggeredKeyBindings.bind(
                getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW),
                getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT),
                getRootPane().getActionMap(),
                KeyEvent.VK_ESCAPE,
                "world_making_close",
                this::dispose);
    }

    private void openChildDialog(Supplier<JDialog> dialogFactory) {
        launchingChild = true;
        dispose();
        JDialog child = dialogFactory.get();
        child.setVisible(true);
        if (mainFrame == null || !MainFrame.CARD_GAME.equals(mainFrame.getCurrentCard())) {
            new WorldMakingDialog(mainFrame, engine).setVisible(true);
        }
    }

    private JButton createOptionButton(String title, String subtitle, ActionListener action) {
        JButton btn = new JButton(title);
        btn.setFont(AssetStyles.FONT_BOLD);
        AssetStyles.styleButton(btn);
        btn.setToolTipText(subtitle);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(action);
        return btn;
    }
}
