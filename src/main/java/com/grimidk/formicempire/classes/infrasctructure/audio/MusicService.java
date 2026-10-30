package com.grimidk.formicempire.classes.infrasctructure.audio;

import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.registries.MusicTracks;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class MusicService {
    public interface Listener {
        void onMusicStateChanged();
    }

    private static final int HISTORY_LIMIT = 32;
    private static final int BUFFER_SIZE = 4096;

    private final Engine engine;
    private final Object playbackLock = new Object();
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private final Deque<MusicTrack> history = new ArrayDeque<>();

    private List<MusicTrack> menuTracks = List.of();
    private List<MusicTrack> sessionTracks = List.of();

    private MusicContext context = MusicContext.NONE;
    private MusicTrack currentTrack;
    private boolean userPaused;
    private boolean playing;
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private final AtomicBoolean skipRequested = new AtomicBoolean(false);
    private Thread playbackThread;
    private SourceDataLine activeLine;
    private final AtomicInteger outputGainMillis = new AtomicInteger(1000);
    private int consecutivePlaybackFailures;

    public MusicService(Engine engine) {
        this.engine = Objects.requireNonNull(engine, "engine");
        reloadLibrary();
        refreshVolume();
    }

    public void reloadLibrary() {
        menuTracks = MusicTracks.getPlayableMenuTracks();
        sessionTracks = MusicTracks.getPlayableSessionTracks();
        notifyListeners();
    }

    public void addListener(Listener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public MusicContext getContext() {
        return context;
    }

    public MusicTrack getCurrentTrack() {
        return currentTrack;
    }

    public boolean isPlaying() {
        return playing && !userPaused;
    }

    public boolean isUserPaused() {
        return userPaused;
    }

    public boolean hasTracks() {
        return !playlistFor(context).isEmpty();
    }

    public List<MusicTrack> getPlaylist() {
        return List.copyOf(playlistFor(context));
    }

    public void enterMenu() {
        startContext(MusicContext.MENU, true);
    }

    public void enterSession() {
        startContext(MusicContext.SESSION, true);
    }

    public void stopAll() {
        startContext(MusicContext.NONE, false);
    }

    public void togglePlayPause() {
        if (context == MusicContext.NONE || playlistFor(context).isEmpty()) {
            return;
        }
        if (userPaused || !playing) {
            userPaused = false;
            if (!playing) {
                if (currentTrack == null) {
                    playTrack(pickNext(null), false);
                } else {
                    playTrack(currentTrack, false);
                }
            } else {
                resumeLine();
                notifyListeners();
            }
        } else {
            userPaused = true;
            pauseLine();
            notifyListeners();
        }
    }

    public void next() {
        if (context == MusicContext.NONE) {
            return;
        }
        List<MusicTrack> playlist = playlistFor(context);
        if (playlist.isEmpty()) {
            return;
        }
        userPaused = false;
        playTrack(pickNext(currentTrack), true);
    }

    public void previous() {
        if (context == MusicContext.NONE) {
            return;
        }
        List<MusicTrack> playlist = playlistFor(context);
        if (playlist.isEmpty()) {
            return;
        }
        userPaused = false;
        MusicTrack previous = history.pollLast();
        if (previous == null) {
            previous = pickPreviousSequential(currentTrack);
        }
        playTrack(previous, false);
    }

    public void toggleMute() {
        engine.setMusicMuted(!engine.isMusicMuted());
        engine.saveGlobalSettings();
        refreshVolume();
        notifyListeners();
    }

    public void toggleShuffle() {
        engine.setMusicShuffle(!engine.isMusicShuffle());
        engine.saveGlobalSettings();
        notifyListeners();
    }

    public void refreshVolume() {
        float linear = linearGain(engine.getMasterVolume(), engine.getMusicVolume(), engine.isMusicMuted());
        outputGainMillis.set(Math.round(linear * 1000f));
    }

    public void shutdown() {
        stopAll();
        listeners.clear();
    }

    static float linearGain(int masterVolume, int musicVolume, boolean muted) {
        if (muted) {
            return 0f;
        }
        float master = clamp01(masterVolume / 100f);
        float music = clamp01(musicVolume / 100f);
        return master * music;
    }

    static void applySoftwareGain(byte[] buffer, int offset, int length, float gain) {
        if (buffer == null || length <= 1) {
            return;
        }
        int end = Math.min(buffer.length, offset + length);
        if (gain <= 0.0001f) {
            for (int i = offset; i < end; i++) {
                buffer[i] = 0;
            }
            return;
        }
        if (gain >= 0.999f) {
            return;
        }
        int aligned = offset + ((end - offset) / 2) * 2;
        for (int i = offset; i < aligned; i += 2) {
            short sample = (short) ((buffer[i] & 0xff) | (buffer[i + 1] << 8));
            int scaled = Math.round(sample * gain);
            if (scaled > Short.MAX_VALUE) {
                scaled = Short.MAX_VALUE;
            } else if (scaled < Short.MIN_VALUE) {
                scaled = Short.MIN_VALUE;
            }
            buffer[i] = (byte) (scaled & 0xff);
            buffer[i + 1] = (byte) ((scaled >> 8) & 0xff);
        }
    }

    private void startContext(MusicContext nextContext, boolean autoplay) {
        if (nextContext == null) {
            nextContext = MusicContext.NONE;
        }
        boolean same = this.context == nextContext;
        this.context = nextContext;
        if (nextContext == MusicContext.NONE) {
            history.clear();
            userPaused = false;
            currentTrack = null;
            stopPlaybackThread(true);
            playing = false;
            notifyListeners();
            return;
        }
        List<MusicTrack> playlist = playlistFor(nextContext);
        if (playlist.isEmpty()) {
            history.clear();
            userPaused = false;
            currentTrack = null;
            stopPlaybackThread(true);
            playing = false;
            notifyListeners();
            return;
        }
        if (same && playing && !userPaused && currentTrack != null && playlist.contains(currentTrack)) {
            notifyListeners();
            return;
        }
        history.clear();
        userPaused = false;
        if (!autoplay) {
            stopPlaybackThread(true);
            playing = false;
            notifyListeners();
            return;
        }
        MusicTrack start = same && currentTrack != null && playlist.contains(currentTrack)
                ? currentTrack
                : pickNext(null);
        playTrack(start, false);
    }

    private void playTrack(MusicTrack track, boolean pushCurrentToHistory) {
        if (track == null) {
            stopPlaybackThread(true);
            currentTrack = null;
            playing = false;
            notifyListeners();
            return;
        }
        if (pushCurrentToHistory && currentTrack != null && !currentTrack.equals(track)) {
            history.addLast(currentTrack);
            while (history.size() > HISTORY_LIMIT) {
                history.removeFirst();
            }
        }
        currentTrack = track;
        playing = true;
        userPaused = false;
        startPlaybackThread(track);
        notifyListeners();
    }

    private void startPlaybackThread(MusicTrack track) {
        Thread previous;
        boolean onPlaybackThread;
        synchronized (playbackLock) {
            onPlaybackThread = playbackThread == Thread.currentThread();
            previous = onPlaybackThread ? null : playbackThread;
            stopRequested.set(true);
            skipRequested.set(true);
            playbackLock.notifyAll();
            if (activeLine != null) {
                try {
                    activeLine.stop();
                    activeLine.flush();
                } catch (Exception ignore) {
                }
            }
            Thread next = new Thread(() -> {
                if (previous != null) {
                    try {
                        previous.join(1500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                closeActiveLine();
                stopRequested.set(false);
                skipRequested.set(false);
                runPlayback(track);
            }, "music-player");
            next.setDaemon(true);
            playbackThread = next;
            next.start();
        }
    }

    private void runPlayback(MusicTrack track) {
        boolean completedNaturally = false;
        boolean failed = false;
        try (
                InputStream raw = openTrackStream(track);
                AudioInputStream decoded = openDecodedStream(raw)
        ) {
            if (decoded == null) {
                failed = true;
                return;
            }
            AudioFormat format = decoded.getFormat();
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            synchronized (playbackLock) {
                activeLine = line;
            }
            line.start();
            consecutivePlaybackFailures = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            while (!stopRequested.get()) {
                if (userPaused) {
                    pauseLine();
                    waitWhilePaused();
                    if (stopRequested.get()) {
                        break;
                    }
                    resumeLine();
                }
                int read = decoded.read(buffer, 0, buffer.length);
                if (read < 0) {
                    completedNaturally = !stopRequested.get();
                    break;
                }
                if (read == 0) {
                    continue;
                }
                applySoftwareGain(buffer, 0, read, outputGainMillis.get() / 1000f);
                int offset = 0;
                while (offset < read && !stopRequested.get()) {
                    if (userPaused) {
                        pauseLine();
                        waitWhilePaused();
                        if (stopRequested.get()) {
                            break;
                        }
                        resumeLine();
                    }
                    int written = line.write(buffer, offset, read - offset);
                    if (written < 0) {
                        break;
                    }
                    offset += written;
                }
            }
            if (completedNaturally && !stopRequested.get()) {
                line.drain();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            failed = true;
        } finally {
            closeActiveLine();
            if (failed) {
                consecutivePlaybackFailures++;
                List<MusicTrack> playlist = playlistFor(context);
                if (consecutivePlaybackFailures >= Math.max(1, playlist.size())) {
                    playing = false;
                    notifyListeners();
                } else if (!stopRequested.get() && !userPaused) {
                    finishTrackAndAdvance();
                }
            } else if (completedNaturally && !stopRequested.get() && !userPaused
                    && currentTrack != null && currentTrack.equals(track)) {
                finishTrackAndAdvance();
            } else if (stopRequested.get() && !skipRequested.get()) {
                playing = false;
                notifyListeners();
            }
        }
    }

    private void finishTrackAndAdvance() {
        if (context == MusicContext.NONE || userPaused) {
            playing = false;
            notifyListeners();
            return;
        }
        List<MusicTrack> playlist = playlistFor(context);
        if (playlist.isEmpty()) {
            playing = false;
            currentTrack = null;
            notifyListeners();
            return;
        }
        playTrack(pickNext(currentTrack), true);
    }

    private void waitWhilePaused() throws InterruptedException {
        synchronized (playbackLock) {
            while (userPaused && !stopRequested.get()) {
                playbackLock.wait(200);
            }
        }
    }

    private void pauseLine() {
        synchronized (playbackLock) {
            if (activeLine != null && activeLine.isRunning()) {
                activeLine.stop();
            }
        }
    }

    private void resumeLine() {
        synchronized (playbackLock) {
            if (activeLine != null && !activeLine.isRunning() && !stopRequested.get()) {
                activeLine.start();
            }
            playbackLock.notifyAll();
        }
    }

    private void stopPlaybackThread(boolean join) {
        Thread thread;
        synchronized (playbackLock) {
            stopRequested.set(true);
            skipRequested.set(true);
            playbackLock.notifyAll();
            if (activeLine != null) {
                try {
                    activeLine.stop();
                    activeLine.flush();
                } catch (Exception ignore) {
                }
            }
            thread = playbackThread;
            if (join) {
                playbackThread = null;
            }
        }
        if (join && thread != null && thread != Thread.currentThread()) {
            try {
                thread.join(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            closeActiveLine();
            stopRequested.set(false);
            skipRequested.set(false);
        } else if (join) {
            closeActiveLine();
            stopRequested.set(false);
            skipRequested.set(false);
        }
    }

    private void closeActiveLine() {
        synchronized (playbackLock) {
            if (activeLine != null) {
                try {
                    activeLine.stop();
                    activeLine.close();
                } catch (Exception ignore) {
                }
            }
            activeLine = null;
        }
    }

    private InputStream openTrackStream(MusicTrack track) throws IOException {
        InputStream raw = MusicService.class.getResourceAsStream(track.getResourcePath());
        if (raw == null) {
            throw new IOException("Missing music resource: " + track.getResourcePath());
        }
        return new BufferedInputStream(raw);
    }

    private AudioInputStream openDecodedStream(InputStream raw)
            throws UnsupportedAudioFileException, IOException {
        AudioInputStream source = AudioSystem.getAudioInputStream(raw);
        AudioFormat base = source.getFormat();
        AudioFormat decoded = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                base.getSampleRate(),
                16,
                base.getChannels(),
                base.getChannels() * 2,
                base.getSampleRate(),
                false
        );
        if (AudioSystem.isConversionSupported(decoded, base)) {
            return AudioSystem.getAudioInputStream(decoded, source);
        }
        return source;
    }

    private List<MusicTrack> playlistFor(MusicContext ctx) {
        if (ctx == MusicContext.MENU) {
            return menuTracks;
        }
        if (ctx == MusicContext.SESSION) {
            return sessionTracks;
        }
        return List.of();
    }

    private MusicTrack pickNext(MusicTrack current) {
        List<MusicTrack> playlist = playlistFor(context);
        if (playlist.isEmpty()) {
            return null;
        }
        if (playlist.size() == 1) {
            return playlist.get(0);
        }
        if (engine.isMusicShuffle()) {
            List<MusicTrack> candidates = new ArrayList<>(playlist);
            if (current != null) {
                candidates.remove(current);
            }
            if (candidates.isEmpty()) {
                return playlist.get(0);
            }
            return candidates.get(GameRandom.nextInt(candidates.size()));
        }
        if (current == null) {
            return playlist.get(0);
        }
        int index = playlist.indexOf(current);
        if (index < 0) {
            return playlist.get(0);
        }
        return playlist.get((index + 1) % playlist.size());
    }

    private MusicTrack pickPreviousSequential(MusicTrack current) {
        List<MusicTrack> playlist = playlistFor(context);
        if (playlist.isEmpty()) {
            return null;
        }
        if (current == null) {
            return playlist.get(0);
        }
        int index = playlist.indexOf(current);
        if (index < 0) {
            return playlist.get(0);
        }
        int prev = index - 1;
        if (prev < 0) {
            prev = playlist.size() - 1;
        }
        return playlist.get(prev);
    }

    private void notifyListeners() {
        Runnable fire = () -> {
            for (Listener listener : listeners) {
                listener.onMusicStateChanged();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            fire.run();
        } else {
            SwingUtilities.invokeLater(fire);
        }
    }

    private static float clamp01(float value) {
        if (value < 0f) {
            return 0f;
        }
        if (value > 1f) {
            return 1f;
        }
        return value;
    }
}
