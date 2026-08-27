package com.grimidk.formicempire.classes.infrasctructure.audio;

import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.registries.SoundEffects;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class SfxService {
    private static final int DECODE_READ = 8192;
    private static volatile SfxService active;

    private final Engine engine;
    private final AtomicInteger outputGainMillis = new AtomicInteger(1000);
    private final AtomicLong playGeneration = new AtomicLong();
    private final ConcurrentHashMap<String, CachedPcm> pcmCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Clip> clipCache = new ConcurrentHashMap<>();
    private final ExecutorService playbackExecutor;

    private volatile Clip activeClip;

    public SfxService(Engine engine) {
        this.engine = Objects.requireNonNull(engine, "engine");
        this.playbackExecutor = Executors.newSingleThreadExecutor(sfxThreadFactory());
        active = this;
        refreshVolume();
        preloadPlayableEffectsAsync();
    }

    public static void play(SoundEffect effect) {
        SfxService service = active;
        if (service != null) {
            service.playEffect(effect);
        }
    }

    public void playEffect(SoundEffect effect) {
        if (effect == null) {
            return;
        }
        float gain = outputGainMillis.get() / 1000f;
        if (gain <= 0.0001f) {
            return;
        }
        long generation = playGeneration.incrementAndGet();
        try {
            playbackExecutor.execute(() -> runPlayback(effect, generation));
        } catch (RejectedExecutionException ignore) {
        }
    }

    public void refreshVolume() {
        float linear = linearGain(engine.getMasterVolume(), engine.getSfxVolume());
        outputGainMillis.set(Math.round(linear * 1000f));
        try {
            playbackExecutor.execute(this::applyGainToOpenClips);
        } catch (RejectedExecutionException ignore) {
            applyGainToOpenClips();
        }
    }

    public void shutdown() {
        playGeneration.incrementAndGet();
        if (active == this) {
            active = null;
        }
        playbackExecutor.execute(this::closeAllClips);
        playbackExecutor.shutdown();
        try {
            playbackExecutor.awaitTermination(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        playbackExecutor.shutdownNow();
        closeAllClips();
        pcmCache.clear();
    }

    static float linearGain(int masterVolume, int sfxVolume) {
        float master = clamp01(masterVolume / 100f);
        float sfx = clamp01(sfxVolume / 100f);
        return master * sfx;
    }

    private void preloadPlayableEffectsAsync() {
        try {
            playbackExecutor.execute(() -> {
                for (SoundEffect effect : SoundEffects.getPlayableEffects()) {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    ensureClip(effect);
                }
            });
        } catch (RejectedExecutionException ignore) {
        }
    }

    private void runPlayback(SoundEffect effect, long generation) {
        if (generation != playGeneration.get()) {
            return;
        }
        Clip clip = ensureClip(effect);
        if (clip == null || generation != playGeneration.get()) {
            return;
        }
        try {
            Clip previous = activeClip;
            if (previous != null && previous != clip && previous.isOpen()) {
                previous.stop();
                previous.setFramePosition(0);
            }
            if (generation != playGeneration.get()) {
                return;
            }
            applyGain(clip, outputGainMillis.get() / 1000f);
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            activeClip = clip;
            clip.start();
        } catch (IllegalStateException | SecurityException ignore) {
        }
    }

    private Clip ensureClip(SoundEffect effect) {
        if (effect == null) {
            return null;
        }
        Clip existing = clipCache.get(effect.getId());
        if (existing != null && existing.isOpen()) {
            return existing;
        }
        CachedPcm pcm = getOrDecode(effect);
        if (pcm == null) {
            return null;
        }
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(pcm.format, pcm.pcm, 0, pcm.pcm.length);
            applyGain(clip, outputGainMillis.get() / 1000f);
            Clip raced = clipCache.putIfAbsent(effect.getId(), clip);
            if (raced != null) {
                try {
                    clip.close();
                } catch (Exception ignore) {
                }
                return raced.isOpen() ? raced : null;
            }
            return clip;
        } catch (LineUnavailableException | IllegalArgumentException | SecurityException ignore) {
            return null;
        }
    }

    private CachedPcm getOrDecode(SoundEffect effect) {
        if (effect == null || !effect.isResourcePresent()) {
            return null;
        }
        CachedPcm existing = pcmCache.get(effect.getId());
        if (existing != null) {
            return existing;
        }
        try {
            CachedPcm decoded = decodeEffect(effect);
            if (decoded != null) {
                CachedPcm raced = pcmCache.putIfAbsent(effect.getId(), decoded);
                return raced != null ? raced : decoded;
            }
        } catch (UnsupportedAudioFileException | IOException ignore) {
        }
        return null;
    }

    private CachedPcm decodeEffect(SoundEffect effect)
            throws UnsupportedAudioFileException, IOException {
        try (
                InputStream raw = openEffectStream(effect);
                AudioInputStream decoded = openDecodedStream(raw)
        ) {
            if (decoded == null) {
                return null;
            }
            AudioFormat format = decoded.getFormat();
            ByteArrayOutputStream pcmOut = new ByteArrayOutputStream(DECODE_READ * 8);
            byte[] buffer = new byte[DECODE_READ];
            int read;
            while ((read = decoded.read(buffer, 0, buffer.length)) >= 0) {
                if (read > 0) {
                    pcmOut.write(buffer, 0, read);
                }
            }
            byte[] pcm = pcmOut.toByteArray();
            if (pcm.length == 0) {
                return null;
            }
            return new CachedPcm(format, pcm);
        }
    }

    private void applyGainToOpenClips() {
        float gain = outputGainMillis.get() / 1000f;
        for (Clip clip : clipCache.values()) {
            if (clip != null && clip.isOpen()) {
                applyGain(clip, gain);
            }
        }
    }

    private static void applyGain(Clip clip, float linearGain) {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        try {
            FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float safe = Math.max(0.0001f, Math.min(1f, linearGain));
            float dB = (float) (20.0 * Math.log10(safe));
            dB = Math.max(control.getMinimum(), Math.min(control.getMaximum(), dB));
            control.setValue(dB);
        } catch (IllegalArgumentException | IllegalStateException ignore) {
        }
    }

    private void closeAllClips() {
        activeClip = null;
        for (Clip clip : clipCache.values()) {
            if (clip == null) {
                continue;
            }
            try {
                clip.stop();
                clip.close();
            } catch (Exception ignore) {
            }
        }
        clipCache.clear();
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

    private static ThreadFactory sfxThreadFactory() {
        return runnable -> {
            Thread thread = new Thread(runnable, "sfx-player");
            thread.setDaemon(true);
            return thread;
        };
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

    private static final class CachedPcm {
        private final AudioFormat format;
        private final byte[] pcm;

        private CachedPcm(AudioFormat format, byte[] pcm) {
            this.format = format;
            this.pcm = pcm;
        }
    }
}
