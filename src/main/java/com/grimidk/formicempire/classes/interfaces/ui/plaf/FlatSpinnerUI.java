package com.grimidk.formicempire.classes.interfaces.ui.plaf;

import com.grimidk.formicempire.classes.infrasctructure.audio.SfxService;
import com.grimidk.formicempire.classes.infrasctructure.audio.SoundEffect;
import com.grimidk.formicempire.classes.infrasctructure.registries.SoundEffects;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiButtonStyles;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSpinnerUI;

public final class FlatSpinnerUI extends BasicSpinnerUI {

    public static ComponentUI createUI(JComponent c) {
        return new FlatSpinnerUI();
    }

    @Override
    protected Component createPreviousButton() {
        Component button = createStepButton("\u25BC");
        installPreviousButtonListeners(button);
        installStepSound(button, SoundEffects.SPIN_DOWN);
        return button;
    }

    @Override
    protected Component createNextButton() {
        Component button = createStepButton("\u25B2");
        installNextButtonListeners(button);
        installStepSound(button, SoundEffects.SPIN_UP);
        return button;
    }

    private static void installStepSound(Component button, SoundEffect effect) {
        if (button instanceof AbstractButton abstractButton && effect != null) {
            abstractButton.addActionListener(e -> SfxService.play(effect));
        }
    }

    @Override
    protected LayoutManager createLayout() {
        return super.createLayout();
    }

    @Override
    protected JComponent createEditor() {
        JComponent editor = super.createEditor();
        if (editor != null) {
            editor.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            editor.setForeground(AssetStyles.FONT_COLOR);
        }
        return editor;
    }

    private static JButton createStepButton(String label) {
        JButton button = new JButton(label);
        applyStepButtonChrome(button);
        return button;
    }

    private static Dimension stepButtonSize() {
        return AssetStyles.minControlHitSize();
    }

    private static void applyStepButtonChrome(JButton button) {
        UiButtonStyles.styleCompact(button);
        button.setFont(AssetStyles.FONT_BOLD.deriveFont(11f));
        button.setFocusable(false);
        button.setRequestFocusEnabled(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        Dimension size = stepButtonSize();
        button.setPreferredSize(size);
        button.setMinimumSize(size);
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JSpinner spinner) {
            spinner.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            spinner.setForeground(AssetStyles.FONT_COLOR);
            spinner.setBorder(AssetStyles.INTERNAL_BORDER);
            styleStepButtons(spinner);
            applySpinnerMinimumSize(spinner);
        }
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension size = super.getPreferredSize(c);
        return enforceSpinnerSize(size);
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        Dimension size = super.getMinimumSize(c);
        return enforceSpinnerSize(size);
    }

    private static Dimension enforceSpinnerSize(Dimension size) {
        if (size == null) {
            return AssetStyles.preferredSpinnerSize(0);
        }
        Dimension min = AssetStyles.preferredSpinnerSize(size.width);
        return new Dimension(Math.max(size.width, min.width), Math.max(size.height, min.height));
    }

    public static void applySpinnerMinimumSize(JSpinner spinner) {
        if (spinner == null) {
            return;
        }
        Dimension min = AssetStyles.preferredSpinnerSize(0);
        Dimension currentMin = spinner.getMinimumSize();
        if (currentMin == null
                || currentMin.width < min.width
                || currentMin.height < min.height) {
            spinner.setMinimumSize(min);
        }
        Dimension preferred = spinner.getPreferredSize();
        if (preferred == null
                || preferred.width < min.width
                || preferred.height < min.height) {
            spinner.setPreferredSize(new Dimension(
                    preferred == null ? min.width : Math.max(preferred.width, min.width),
                    preferred == null ? min.height : Math.max(preferred.height, min.height)));
        }
    }

    public static void styleStepButtons(JSpinner spinner) {
        if (spinner == null) {
            return;
        }
        for (Component child : spinner.getComponents()) {
            if (child instanceof JButton button) {
                applyStepButtonChrome(button);
            }
        }
    }
}
