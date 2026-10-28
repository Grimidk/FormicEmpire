package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.WorldGenStepListener;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.MainFrame;
import com.grimidk.formicempire.classes.interfaces.game.gamepanels.MiniMapPanel;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiDialogUtils;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class WorldGenVisualizerDialog extends JDialog implements WorldGenStepListener {
    private final MiniMapPanel miniMapPanel;
    private final JLabel stepLabel;
    private final JProgressBar progressBar;
    private final JButton skipButton;
    private volatile boolean skipped = false;

    public WorldGenVisualizerDialog(JFrame owner) {
        super(owner, LanguageStrings.get(LanguageStrings.WORLDGEN_STEP_TITLE), true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        if (owner instanceof MainFrame mainFrame) {
            mainFrame.applyGameCursors(this);
        }

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(AssetStyles.UI_BG_PRIMARY);
        contentPane.setBorder(BorderFactory.createLineBorder(AssetStyles.BORDER_COLOR, AssetStyles.BORDER_THICKNESS_EXTERNAL));

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setBackground(AssetStyles.UI_BG_SECONDARY);
        northPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, AssetStyles.BORDER_THICKNESS_INTERNAL, 0, AssetStyles.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JLabel titleLabel = new JLabel(LanguageStrings.get(LanguageStrings.WORLDGEN_STEP_TITLE));
        titleLabel.setFont(AssetStyles.FONT_TITLE);
        titleLabel.setForeground(AssetStyles.FONT_COLOR_BRIGHT);
        titleLabel.setAlignmentX(0.0f);

        stepLabel = new JLabel(" ");
        stepLabel.setFont(AssetStyles.FONT_NORMAL);
        stepLabel.setForeground(AssetStyles.FONT_COLOR);
        stepLabel.setAlignmentX(0.0f);

        progressBar = new JProgressBar(0, 16);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setAlignmentX(0.0f);
        AssetStyles.styleProgressBar(progressBar);
        progressBar.setPreferredSize(new Dimension(500, AssetStyles.MIN_CONTROL_HIT_SIZE));

        northPanel.add(titleLabel);
        northPanel.add(Box.createVerticalStrut(4));
        northPanel.add(stepLabel);
        northPanel.add(Box.createVerticalStrut(8));
        northPanel.add(progressBar);

        miniMapPanel = new MiniMapPanel((World) null);
        miniMapPanel.setPreferredSize(new Dimension(640, 380));

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        southPanel.setBackground(AssetStyles.UI_BG_SECONDARY);
        southPanel.setBorder(BorderFactory.createMatteBorder(AssetStyles.BORDER_THICKNESS_INTERNAL, 0, 0, 0, AssetStyles.BORDER_COLOR));

        skipButton = new JButton(LanguageStrings.get(LanguageStrings.WORLDGEN_STEP_SKIP));
        AssetStyles.styleButton(skipButton);
        skipButton.setPreferredSize(new Dimension(Math.max(100, AssetStyles.MIN_CONTROL_HIT_SIZE * 4), AssetStyles.MIN_CONTROL_HIT_SIZE + 8));
        skipButton.addActionListener(e -> {
            skipped = true;
            skipButton.setEnabled(false);
        });
        southPanel.add(skipButton);

        contentPane.add(northPanel, BorderLayout.NORTH);
        contentPane.add(miniMapPanel, BorderLayout.CENTER);
        contentPane.add(southPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
        setResizable(true);
        setPreferredSize(new Dimension(720, 540));
        UiDialogUtils.prepareDialog(this, owner);
        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isSkipped() {
        return skipped;
    }

    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }

    @Override
    public void onStepCompleted(int stepNumber, String stepName, World world) {
        if (skipped) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            stepLabel.setText("Step " + stepNumber + "/16: " + stepName);
            progressBar.setValue(stepNumber);
            miniMapPanel.setExplicitWorld(world);
            miniMapPanel.repaint();
        });
        try {
            Thread.sleep(135);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
