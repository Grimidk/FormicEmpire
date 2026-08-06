package com.grimidk.formicempire.classes.interfaces.menu;

import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.HelpPanel;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VersionLicenseFooter extends JPanel {
    private final Color textColor;
    private final JLabel versionLabel;
    private final JLabel licenseLabel;
    private Runnable beforeLicenseDialog;
    private Runnable afterLicenseDialog;

    public VersionLicenseFooter(Color textColor) {
        this.textColor = textColor;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 12, 8, 12));

        versionLabel = new JLabel(HelpPanel.loadVersionText());
        versionLabel.setFont(AssetStyles.FONT_NORMAL);
        versionLabel.setForeground(textColor);
        versionLabel.setOpaque(false);

        licenseLabel = new JLabel(LanguageStrings.get(LanguageStrings.UI_LICENSE));
        licenseLabel.setFont(AssetStyles.FONT_NORMAL);
        licenseLabel.setForeground(textColor);
        licenseLabel.setOpaque(false);
        AssetStyles.markClickable(licenseLabel);
        sizeLicenseHitTarget();
        licenseLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (beforeLicenseDialog != null) {
                    beforeLicenseDialog.run();
                }
                HelpPanel.showLicenseDialog(VersionLicenseFooter.this);
                if (afterLicenseDialog != null) {
                    afterLicenseDialog.run();
                }
            }
        });

        add(versionLabel, BorderLayout.WEST);
        add(licenseLabel, BorderLayout.EAST);
    }

    public void setLicenseDialogHooks(Runnable beforeLicenseDialog, Runnable afterLicenseDialog) {
        this.beforeLicenseDialog = beforeLicenseDialog;
        this.afterLicenseDialog = afterLicenseDialog;
    }

    public void refreshTranslations() {
        licenseLabel.setText(LanguageStrings.get(LanguageStrings.UI_LICENSE));
        sizeLicenseHitTarget();
        applyTextColor();
    }

    public void applyTextColor() {
        versionLabel.setForeground(textColor);
        licenseLabel.setForeground(textColor);
    }

    private void sizeLicenseHitTarget() {
        Dimension hit = AssetStyles.minControlHitSize();
        licenseLabel.setPreferredSize(null);
        Dimension textSize = licenseLabel.getPreferredSize();
        Dimension sized = new Dimension(
                Math.max(hit.width, textSize.width),
                Math.max(hit.height, textSize.height));
        licenseLabel.setMinimumSize(hit);
        licenseLabel.setPreferredSize(sized);
    }
}
