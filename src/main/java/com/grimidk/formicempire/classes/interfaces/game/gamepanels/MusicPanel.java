package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.audio.MusicService;
import com.grimidk.formicempire.classes.infrasctructure.audio.MusicTrack;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class MusicPanel extends ZeroGamePanel {
    private static final int ICON_DISPLAY_SIZE = AssetStyles.MIN_CONTROL_HIT_SIZE;
    private static final Dimension ICON_SLOT = new Dimension(ICON_DISPLAY_SIZE, ICON_DISPLAY_SIZE);

    private final Engine engine;
    private final MusicService.Listener listener = this::refreshFromService;

    private JLabel trackLabel;
    private JButton prevButton;
    private JButton playPauseButton;
    private JButton nextButton;
    private JButton shuffleButton;
    private JButton muteButton;

    public MusicPanel(Engine engine) {
        super(new BorderLayout());
        this.engine = engine;
        initComponents();
        initLayout();
        MusicService music = engine.getMusicService();
        if (music != null) {
            music.addListener(listener);
        }
        refreshFromService();
    }

    @Override
    protected void initComponents() {
        setPreferredSize(new Dimension(250, 88));
        setMinimumSize(new Dimension(200, 88));
        setTitledBorder(LanguageStrings.PANEL_MUSIC);

        trackLabel = new JLabel(LanguageStrings.get(LanguageStrings.PANEL_NO_MUSIC), SwingConstants.CENTER);
        trackLabel.setFont(AssetStyles.FONT_NORMAL.deriveFont(11f));
        trackLabel.setForeground(AssetStyles.FONT_COLOR);

        prevButton = createControlButton(GameConstants.ICON_MUSIC_PREV);
        playPauseButton = createControlButton(GameConstants.ICON_MUSIC_PLAY);
        nextButton = createControlButton(GameConstants.ICON_MUSIC_NEXT);
        shuffleButton = createControlButton(GameConstants.ICON_MUSIC_SHUFFLE);
        muteButton = createControlButton(GameConstants.ICON_MUSIC_UNMUTE);

        prevButton.addActionListener(e -> {
            MusicService music = engine.getMusicService();
            if (music != null) {
                music.previous();
            }
        });
        playPauseButton.addActionListener(e -> {
            MusicService music = engine.getMusicService();
            if (music != null) {
                music.togglePlayPause();
            }
        });
        nextButton.addActionListener(e -> {
            MusicService music = engine.getMusicService();
            if (music != null) {
                music.next();
            }
        });
        shuffleButton.addActionListener(e -> {
            MusicService music = engine.getMusicService();
            if (music != null) {
                music.toggleShuffle();
            }
        });
        muteButton.addActionListener(e -> {
            MusicService music = engine.getMusicService();
            if (music != null) {
                music.toggleMute();
            }
        });
    }

    @Override
    protected void initLayout() {
        add(trackLabel, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
        controls.setOpaque(false);
        controls.add(prevButton);
        controls.add(playPauseButton);
        controls.add(nextButton);
        controls.add(Box.createHorizontalStrut(6));
        controls.add(shuffleButton);
        controls.add(muteButton);
        add(controls, BorderLayout.SOUTH);
    }

    @Override
    public void refreshTranslations() {
        super.refreshTranslations();
        refreshFromService();
    }

    @Override
    public void refreshTheme() {
        super.refreshTheme();
        trackLabel.setForeground(AssetStyles.FONT_COLOR);
        styleControls();
        refreshFromService();
    }

    public void disposeListeners() {
        MusicService music = engine.getMusicService();
        if (music != null) {
            music.removeListener(listener);
        }
    }

    private JButton createControlButton(ImageIcon icon) {
        JButton button = new JButton();
        button.setFocusable(false);
        styleIconControl(button, icon);
        return button;
    }

    private void styleControls() {
        styleIconControl(prevButton, GameConstants.ICON_MUSIC_PREV);
        styleIconControl(nextButton, GameConstants.ICON_MUSIC_NEXT);
        styleIconControl(shuffleButton, GameConstants.ICON_MUSIC_SHUFFLE);
        styleIconControl(playPauseButton, GameConstants.ICON_MUSIC_PLAY);
        styleIconControl(muteButton, GameConstants.ICON_MUSIC_UNMUTE);
    }

    private static void styleIconControl(JButton button, ImageIcon icon) {
        AssetStyles.styleIconButton(button);
        button.setIcon(icon);
        button.setText("");
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setPreferredSize(ICON_SLOT);
        button.setMinimumSize(ICON_SLOT);
        button.setMaximumSize(ICON_SLOT);
    }

    private void refreshFromService() {
        MusicService music = engine.getMusicService();
        boolean hasTracks = music != null && music.hasTracks();
        MusicTrack track = music != null ? music.getCurrentTrack() : null;

        if (!hasTracks) {
            trackLabel.setText(LanguageStrings.get(LanguageStrings.PANEL_NO_MUSIC));
            trackLabel.setToolTipText(null);
        } else if (track != null) {
            trackLabel.setText(truncate(track.getLabel(), 28));
            trackLabel.setToolTipText(track.getLabel());
        } else {
            trackLabel.setText(LanguageStrings.get(LanguageStrings.PANEL_NO_MUSIC));
            trackLabel.setToolTipText(null);
        }

        boolean playing = music != null && music.isPlaying();
        playPauseButton.setIcon(playing ? GameConstants.ICON_MUSIC_PAUSE : GameConstants.ICON_MUSIC_PLAY);
        playPauseButton.setText("");
        playPauseButton.setToolTipText(LanguageStrings.get(
                playing ? LanguageStrings.MUSIC_PAUSE_TT : LanguageStrings.MUSIC_PLAY_TT));
        prevButton.setToolTipText(LanguageStrings.get(LanguageStrings.MUSIC_PREV_TT));
        nextButton.setToolTipText(LanguageStrings.get(LanguageStrings.MUSIC_NEXT_TT));

        boolean shuffle = engine.isMusicShuffle();
        shuffleButton.setToolTipText(LanguageStrings.get(LanguageStrings.MUSIC_SHUFFLE_TT));
        shuffleButton.setEnabled(hasTracks);

        boolean muted = engine.isMusicMuted();
        muteButton.setIcon(muted ? GameConstants.ICON_MUSIC_MUTE : GameConstants.ICON_MUSIC_UNMUTE);
        muteButton.setText("");
        muteButton.setToolTipText(LanguageStrings.get(
                muted ? LanguageStrings.MUSIC_UNMUTE_TT : LanguageStrings.MUSIC_MUTE_TT));

        prevButton.setEnabled(hasTracks);
        playPauseButton.setEnabled(hasTracks);
        nextButton.setEnabled(hasTracks);
        muteButton.setEnabled(true);
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, Math.max(0, max - 1)) + "…";
    }
}
