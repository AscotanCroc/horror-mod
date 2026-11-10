package com.horrormod.client.sound;

import com.horrormod.client.event.VeilRitual;
import com.horrormod.sound.HorrorSounds;
import com.horrormod.world.HorrorDimensions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class HorrorAmbience {
    private static final int DAY_LENGTH = 24000;
    /** Vanilla time markers: night begins at 13000, day begins at 1000. */
    private static final int NIGHT_START = 13000;
    private static final int DAY_START = 1000;
    private static final int FADE_TICKS = 3600;

    private static AmbienceLoop day;
    private static AmbienceLoop night;
    private static AmbienceLoop veil;

    private HorrorAmbience() {
    }

    public static void initialize() {
        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register(HorrorAmbience::onLevelChange);
    }

    private static void onLevelChange(Minecraft client, ClientLevel level) {
        client.getMusicManager().stopPlaying();
        if (VeilRitual.active()) {
            stop(client, day);
            stop(client, night);
            stop(client, veil);
            day = null;
            night = null;
            veil = null;
            return;
        }
        restart(client, level);
    }

    /**
     * Resource reloads and audio-device changes tear down the sound engine and drop these loops.
     */
    public static void resumeAfterReload() {
        Minecraft client = Minecraft.getInstance();
        restart(client, client.level);
    }

    private static void restart(Minecraft client, ClientLevel level) {
        stop(client, day);
        stop(client, night);
        stop(client, veil);
        day = null;
        night = null;
        veil = null;
        if (level == null) {
            return;
        }
        if (HorrorDimensions.VEIL.equals(level.dimension())) {
            veil = new AmbienceLoop(HorrorSounds.VEIL);
            client.getSoundManager().play(veil);
            return;
        }
        day = new AmbienceLoop(HorrorSounds.AMBIENCE);
        night = new AmbienceLoop(HorrorSounds.NIGHT);
        client.getSoundManager().play(day);
        client.getSoundManager().play(night);
    }

    private static void stop(Minecraft client, AmbienceLoop loop) {
        if (loop != null) {
            client.getSoundManager().stop(loop);
        }
    }

    static float nightAmount(long time) {
        int dayTime = Math.floorMod(time, DAY_LENGTH);
        int duskStart = NIGHT_START - FADE_TICKS;
        int dawnStart = Math.floorMod(DAY_START - FADE_TICKS, DAY_LENGTH);
        if (dayTime >= duskStart && dayTime < NIGHT_START) {
            return (dayTime - duskStart) / (float) FADE_TICKS;
        }
        if (dayTime >= NIGHT_START && dayTime < dawnStart) {
            return 1.0F;
        }
        if (dayTime >= dawnStart) {
            return (DAY_LENGTH - dayTime + DAY_START) / (float) FADE_TICKS;
        }
        if (dayTime < DAY_START) {
            return (DAY_START - dayTime) / (float) FADE_TICKS;
        }
        return 0.0F;
    }

    private static final class AmbienceLoop extends AbstractTickableSoundInstance {
        private final boolean nightTrack;
        private final boolean veilTrack;

        private AmbienceLoop(SoundEvent sound) {
            super(sound, SoundSource.MUSIC, RandomSource.create());
            this.nightTrack = sound == HorrorSounds.NIGHT;
            this.veilTrack = sound == HorrorSounds.VEIL;
            this.looping = true;
            this.volume = 1.0F;
            this.pitch = 1.0F;
            this.attenuation = SoundInstance.Attenuation.NONE;
            this.relative = true;
            this.delay = 0;
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }

        @Override
        public void tick() {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) {
                this.stop();
                return;
            }
            if (this.veilTrack) {
                if (!HorrorDimensions.VEIL.equals(level.dimension())) {
                    this.stop();
                    return;
                }
                this.volume = 1.0F;
                return;
            }
            float nightAmount = HorrorAmbience.nightAmount(level.getOverworldClockTime());
            this.volume = this.nightTrack ? nightAmount : 1.0F - nightAmount;
        }
    }
}
