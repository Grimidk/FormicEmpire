package com.grimidk.formicempire.classes.interfaces.ui.util;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.Timer;
import java.awt.Image;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DiceRollResource {

    public static final int[] DEFAULT_FACE_CYCLE = {2, 6, 3, 5, 1, 4};
    public static final int DEFAULT_STEP_MS = 60;

    private static final String DICE_ANIM_TIMER_KEY = "formicempire.diceAnimTimer";
    private static final Map<Integer, Icon[]> ICON_CACHE = new ConcurrentHashMap<>();

    private DiceRollResource() {
    }

    public static int faceAtStep(int step) {
        int index = Math.floorMod(step, DEFAULT_FACE_CYCLE.length);
        return DEFAULT_FACE_CYCLE[index];
    }

    public static Icon getFaceIcon(int face, int sizePx) {
        int clampedFace = Math.max(1, Math.min(6, face));
        int clampedSize = Math.max(8, sizePx);
        Icon[] faces = ICON_CACHE.computeIfAbsent(clampedSize, s -> {
            Icon[] loaded = new Icon[6];
            for (int i = 0; i < 6; i++) {
                loaded[i] = UiResourceLoader.loadDialogIcon(
                        DiceRollResource.class, "/icons/ui/Dice" + (i + 1) + ".png", s);
            }
            return loaded;
        });
        return faces[clampedFace - 1];
    }

    public static Image getFaceImage(int face, int sizePx) {
        Icon icon = getFaceIcon(face, sizePx);
        return (icon instanceof ImageIcon ii) ? ii.getImage() : null;
    }

    public static void triggerRollAnimation(AbstractButton button, int sizePx) {
        if (button == null) {
            return;
        }
        Object existing = button.getClientProperty(DICE_ANIM_TIMER_KEY);
        if (existing instanceof Timer oldTimer) {
            oldTimer.stop();
        }
        final int[] step = {0};
        final int totalSteps = DEFAULT_FACE_CYCLE.length * 2;
        button.setIcon(getFaceIcon(faceAtStep(0), sizePx));
        Timer timer = new Timer(DEFAULT_STEP_MS, null);
        timer.addActionListener(e -> {
            step[0]++;
            if (step[0] >= totalSteps) {
                timer.stop();
                button.putClientProperty(DICE_ANIM_TIMER_KEY, null);
                button.setIcon(getFaceIcon(faceAtStep(0), sizePx));
                return;
            }
            button.setIcon(getFaceIcon(faceAtStep(step[0]), sizePx));
        });
        button.putClientProperty(DICE_ANIM_TIMER_KEY, timer);
        timer.start();
    }
}
