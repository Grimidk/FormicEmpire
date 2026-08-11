package com.grimidk.formicempire.classes.infrasctructure.audio;

import com.grimidk.formicempire.classes.infrasctructure.Engine;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class SfxService {
    private static final int BUFFER_SIZE = 4096;
    private static volatile SfxService active;

    private final Engine engine;
    private final Object playbackLock = new Object();
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private final AtomicInteger outputGainMillis = new AtomicInteger(1000);

    private Thread playbackThread;
    private SourceDataLine activeLine;

    public SfxService(Engine engine) {
        this.engine = Objects.requireNonNull(engine, "engine");
        active = this;
        refreshVolume();
    }

    public static void play(SoundEffect effect) {
        SfxService service = active;
        if (service != null) {
            service.playEffect(effect);
        }
    }

    public void playEffect(SoundEffect effect) {
        if (effect == null || !effect.isResourcePresent()) {
            return;
        }
        float gain = outputGainMillis.get() / 1000f;
        if (gain <= 0.0001f) {
            return;
        }
        stopPlaybackThread(true);
        stopRequested.set(false);
        playbackThread = new Thread(() -> runPlayback(effect), "sfx-player");
        playbackThread.setDaemon(true);
        playbackThread.start();
    }

    public void refreshVolume() {
        float linear = linearGain(engine.getMasterVolume(), engine.getSfxVolume());
        outputGainMillis.set(Math.round(linear * 1000f));
    }

    public void shutdown() {
        stopPlaybackThread(true);
        if (active == this) {
            active = null;
        }
    }

    static float linearGain(int masterVolume, int sfxVolume) {
        float master = clamp01(masterVolume / 100f);
        float sfx = clamp01(sfxVolume / 100f);
        return master * sfx;
    }

    private void runPlayback(SoundEffect effect) {
        try (
                InputStream raw = openEffectStream(effect);
                AudioInputStream decoded = openDecodedStream(raw)
        ) {
            if (decoded == null) {
                return;
            }
            AudioFormat format = decoded.getFormat();
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            synchronized (playbackLock) {
                activeLine = line;
            }
            line.start();
            byte[] buffer = new byte[BUFFER_SIZE];
            while (!stopRequested.get()) {
                int read = decoded.read(buffer, 0, buffer.length);
                if (read < 0) {
                    break;
                }
                if (read == 0) {
                    continue;
                }
                MusicService.applySoftwareGain(buffer, 0, read, outputGainMillis.get() / 1000f);
                int offset = 0;
                while (offset < read && !stopRequested.get()) {
                    int written = line.write(buffer, offset, read - offset);
                    if (written < 0) {
                        break;
                    }
                    offset += written;
                }
            }
            if (!stopRequested.get()) {
                line.drain();
            }
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException ignore) {
        } finally {
            closeActiveLine();
        }
    }

    private void stopPlaybackThread(boolean join) {
        stopRequested.set(true);
        synchronized (playbackLock) {
            if (activeLine != null) {
                try {
                    activeLine.stop();
                    activeLine.flush();
                } catch (Exception ignore) {
                }
            }
        }
        Thread thread = playbackThread;
        if (join && thread != null && thread != Thread.currentThread()) {
            try {
                thread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        closeActiveLine();
        playbackThread = null;
        stopRequested.set(false);
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

    private InputStream openEffectStream(SoundEffect effect) throws IOException {
        InputStream raw = SfxService.class.getResourceAsStream(effect.getResourcePath());
        if (raw == null) {
            throw new IOException("Missing sound effect resource: " + effect.getResourcePath());
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
