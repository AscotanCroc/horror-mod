package com.horrormod.client.event;

import com.horrormod.HorrorMod;
import com.horrormod.client.sound.HorrorAmbience;
import com.horrormod.network.VeilCutsceneDonePayload;
import com.horrormod.network.VeilRitualPayload;
import com.horrormod.sound.HorrorSounds;
import com.horrormod.world.HorrorDimensions;
import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * Full-screen video over the trip into the veil, including the loading screen.
 * The portrait frame is turned on its side and scaled to cover the screen.
 * It turns upside down on a random 2–4 second beat.
 * Frames live in {@code assets/horror_mod/veil_cutscene}. The scream is {@code veil_scream.ogg}.
 */
public final class VeilRitual {
    private static final Identifier TEXTURE = HorrorMod.id("veil_cutscene_frame");
    private static final int FRAME_COUNT = 303;
    private static final int FRAME_WIDTH = 720;
    private static final int FRAME_HEIGHT = 1280;
    private static final int FPS = 30;
    /** Length of the source clip, so the picture and the scream end together. */
    private static final long DURATION_NANOS = 10_166_576_000L;
    private static final long CAP_NANOS = 20_000_000_000L;
    private static final long MIN_FLIP_NANOS = 2_000_000_000L;
    private static final long MAX_FLIP_NANOS = 4_000_000_000L;

    private static final ExecutorService DECODER = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "veil-cutscene");
        thread.setDaemon(true);
        return thread;
    });
    private static final Object DECODE_LOCK = new Object();

    private static byte[][] frames;
    private static DynamicTexture texture;
    private static byte[] packed;
    private static int[] scratch;
    private static int[] readyPixels;
    private static int readyFrame = -1;
    private static int wantedFrame = -1;
    private static boolean decoding;
    private static int paintedFrame = -1;
    private static boolean paintedUpsideDown;

    private static boolean running;
    private static long startedAt;
    private static long nextFlipAt;
    private static boolean upsideDown;
    private static RandomSource random = RandomSource.create();
    private static Scream scream;

    private VeilRitual() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(VeilRitual::tick);
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> DECODER.execute(VeilRitual::preload));
        ClientPlayNetworking.registerGlobalReceiver(VeilRitualPayload.TYPE, (payload, context) ->
                context.client().execute(VeilRitual::begin)
        );
    }

    public static boolean isScream(SoundInstance sound) {
        return sound != null && HorrorSounds.VEIL_SCREAM.location().equals(sound.getIdentifier());
    }

    public static void begin() {
        if (active()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getSoundManager().stop();
        minecraft.getMusicManager().stopPlaying();
        running = true;
        startedAt = System.nanoTime();
        upsideDown = false;
        paintedFrame = -1;
        nextFlipAt = startedAt + flipDelay();
        scream = new Scream();
        SoundEngine.PlayResult played = minecraft.getSoundManager().play(scream);
        if (played != SoundEngine.PlayResult.STARTED) {
            HorrorMod.LOGGER.warn("Veil scream did not start: {}", played);
        }
        hideCursor();
        if (!(minecraft.gui.screen() instanceof LevelLoadingScreen)) {
            minecraft.gui.setScreen(null);
        }
        hideCursor();
    }

    public static boolean active() {
        return running;
    }

    public static void tick(Minecraft minecraft) {
        if (!running) {
            return;
        }
        hideCursor();
        minecraft.getMusicManager().stopPlaying();
        long elapsed = System.nanoTime() - startedAt;
        if (elapsed < DURATION_NANOS) {
            return;
        }
        if (!arrived() && elapsed < CAP_NANOS) {
            return;
        }
        running = false;
        if (scream != null) {
            minecraft.getSoundManager().stop(scream);
            scream = null;
        }
        if (minecraft.getConnection() != null) {
            ClientPlayNetworking.send(VeilCutsceneDonePayload.INSTANCE);
        }
        HorrorAmbience.resumeAfterReload();
    }

    public static void render(GuiGraphicsExtractor graphics) {
        if (!running) {
            return;
        }
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        graphics.fill(0, 0, width, height, 0xFF000000);
        turnIfDue();
        int frame = frameIndex();
        if (frames == null || !paint(frame)) {
            return;
        }
        int texWidth = FRAME_HEIGHT;
        int texHeight = FRAME_WIDTH;
        int destWidth;
        int destHeight;
        if ((long) width * texHeight >= (long) height * texWidth) {
            destWidth = width;
            destHeight = (int) (((long) width * texHeight + texWidth - 1) / texWidth);
        } else {
            destHeight = height;
            destWidth = (int) (((long) height * texWidth + texHeight - 1) / texHeight);
        }
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                (width - destWidth) / 2,
                (height - destHeight) / 2,
                0.0F,
                0.0F,
                destWidth,
                destHeight,
                texWidth,
                texHeight,
                texWidth,
                texHeight
        );
    }

    private static int frameIndex() {
        long elapsed = System.nanoTime() - startedAt;
        int frame = (int) ((elapsed * FPS) / 1_000_000_000L);
        if (frame < 0) {
            return 0;
        }
        if (frame >= FRAME_COUNT) {
            return FRAME_COUNT - 1;
        }
        return frame;
    }

    private static void turnIfDue() {
        long now = System.nanoTime();
        if (now < nextFlipAt) {
            return;
        }
        upsideDown = !upsideDown;
        nextFlipAt = now + flipDelay();
    }

    private static long flipDelay() {
        return MIN_FLIP_NANOS + (long) (random.nextFloat() * (MAX_FLIP_NANOS - MIN_FLIP_NANOS));
    }

    private static boolean paint(int frame) {
        request(frame);
        int available;
        synchronized (DECODE_LOCK) {
            available = readyFrame <= frame ? readyFrame : -1;
        }
        if (available < 0 || (available == paintedFrame && upsideDown == paintedUpsideDown && texture != null)) {
            return texture != null;
        }
        try {
            if (texture == null) {
                texture = new DynamicTexture(() -> "veil_cutscene", new NativeImage(FRAME_HEIGHT, FRAME_WIDTH, true));
                Minecraft.getInstance().getTextureManager().register(TEXTURE, texture);
            }
            synchronized (DECODE_LOCK) {
                available = readyFrame <= frame ? readyFrame : -1;
                if (available < 0 || readyPixels == null) {
                    return texture != null;
                }
                writeInto(texture.getPixels(), readyPixels);
            }
            texture.upload();
            paintedFrame = available;
            paintedUpsideDown = upsideDown;
            return true;
        } catch (RuntimeException exception) {
            HorrorMod.LOGGER.warn("Veil frame {} could not be drawn", available, exception);
            return texture != null;
        }
    }

    private static void request(int frame) {
        if (frames == null || frames[frame] == null) {
            return;
        }
        synchronized (DECODE_LOCK) {
            wantedFrame = frame;
            if (decoding || readyFrame == frame) {
                return;
            }
            decoding = true;
        }
        DECODER.execute(VeilRitual::decodeWanted);
    }

    private static void decodeWanted() {
        while (true) {
            int frame;
            synchronized (DECODE_LOCK) {
                frame = wantedFrame;
                if (frame < 0 || frame == readyFrame) {
                    decoding = false;
                    return;
                }
            }
            int[] pixels = decode(frames[frame]);
            synchronized (DECODE_LOCK) {
                if (pixels != null) {
                    if (readyPixels == null) {
                        readyPixels = new int[pixels.length];
                    }
                    System.arraycopy(pixels, 0, readyPixels, 0, pixels.length);
                    readyFrame = frame;
                }
                if (wantedFrame == frame) {
                    decoding = false;
                    return;
                }
            }
        }
    }

    /**
     * Turns the portrait frame on its side. {@code upsideDown} turns that result another 180 degrees.
     */
    private static void writeInto(NativeImage image, int[] argb) {
        int size = FRAME_WIDTH * FRAME_HEIGHT * 4;
        if (packed == null) {
            packed = new byte[size];
        }
        for (int y = 0; y < FRAME_HEIGHT; y++) {
            int row = y * FRAME_WIDTH;
            for (int x = 0; x < FRAME_WIDTH; x++) {
                int pixel = argb[row + x];
                int destX = upsideDown ? y : FRAME_HEIGHT - 1 - y;
                int destY = upsideDown ? FRAME_WIDTH - 1 - x : x;
                int dest = (destY * FRAME_HEIGHT + destX) * 4;
                packed[dest] = (byte) (pixel >> 16);
                packed[dest + 1] = (byte) (pixel >> 8);
                packed[dest + 2] = (byte) pixel;
                packed[dest + 3] = (byte) 255;
            }
        }
        ByteBuffer pixels = image.getPixelBytes();
        pixels.clear();
        pixels.put(packed);
    }

    private static int[] decode(byte[] jpeg) {
        if (jpeg == null) {
            return null;
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(jpeg));
            if (image == null) {
                HorrorMod.LOGGER.warn("Veil frame failed to decode");
                return null;
            }
            if (image.getWidth() != FRAME_WIDTH || image.getHeight() != FRAME_HEIGHT) {
                HorrorMod.LOGGER.warn(
                        "Veil frame is {}x{}, expected {}x{}",
                        image.getWidth(),
                        image.getHeight(),
                        FRAME_WIDTH,
                        FRAME_HEIGHT
                );
                return null;
            }
            if (scratch == null) {
                scratch = new int[FRAME_WIDTH * FRAME_HEIGHT];
            }
            image.getRGB(0, 0, FRAME_WIDTH, FRAME_HEIGHT, scratch, 0, FRAME_WIDTH);
            return scratch;
        } catch (IOException | RuntimeException exception) {
            HorrorMod.LOGGER.warn("Veil frame failed to decode", exception);
            return null;
        }
    }

    private static void preload() {
        loadFrames();
        if (frames == null || frames[0] == null) {
            return;
        }
        int[] pixels = decode(frames[0]);
        if (pixels == null) {
            return;
        }
        synchronized (DECODE_LOCK) {
            if (readyPixels == null) {
                readyPixels = new int[pixels.length];
            }
            System.arraycopy(pixels, 0, readyPixels, 0, pixels.length);
            if (readyFrame < 0) {
                readyFrame = 0;
            }
        }
    }

    private static void loadFrames() {
        if (frames != null) {
            return;
        }
        byte[][] loaded = new byte[FRAME_COUNT][];
        ResourceManager resources = Minecraft.getInstance().getResourceManager();
        for (int index = 0; index < FRAME_COUNT; index++) {
            Identifier id = HorrorMod.id(String.format("veil_cutscene/%04d.jpg", index + 1));
            try (InputStream stream = resources.getResourceOrThrow(id).open()) {
                loaded[index] = stream.readAllBytes();
            } catch (IOException exception) {
                HorrorMod.LOGGER.warn("Missing veil frame {}", id, exception);
            }
        }
        frames = loaded;
    }

    private static boolean arrived() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level != null
                && !(minecraft.gui.screen() instanceof LevelLoadingScreen)
                && HorrorDimensions.VEIL.equals(minecraft.level.dimension());
    }

    private static void hideCursor() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.mouseHandler.isMouseGrabbed()) {
            minecraft.mouseHandler.grabMouse();
        }
    }

    private static final class Scream extends AbstractSoundInstance {
        private Scream() {
            super(HorrorSounds.VEIL_SCREAM, SoundSource.MASTER, RandomSource.create());
            this.volume = 1.0F;
            this.pitch = 1.0F;
            this.looping = false;
            this.attenuation = SoundInstance.Attenuation.NONE;
            this.relative = true;
            this.delay = 0;
        }
    }
}
