package com.horrormod.mixin;

import com.horrormod.entity.ReverseMonsterSpawns;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.WeatherData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Rain is a thunderstorm, and only while reverse monsters can still spawn on the surface.
 * Outside that night the flags and the sky levels are cleared.
 */
@Mixin(ServerLevel.class)
public abstract class StormWeatherMixin {
    @Unique
    private boolean horror$wasSurfaceNight;

    @Unique
    private float horror$sentRain = Float.NaN;

    @Unique
    private float horror$sentThunder = Float.NaN;

    @Inject(method = "advanceWeatherCycle", at = @At("TAIL"))
    private void horror$snapStorm(CallbackInfo callback) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        horror$syncStorm(level);
        float levelValue = level.getWeatherData().isRaining() ? 1.0F : 0.0F;
        horror$sendLevel(level, levelValue, levelValue);
    }

    @Unique
    private void horror$syncStorm(ServerLevel level) {
        WeatherData weather = level.getWeatherData();
        boolean night = ReverseMonsterSpawns.surfaceNight(level);
        if (!night) {
            if (horror$wasSurfaceNight) {
                horror$endStorm(level, weather);
            } else if (weather.isRaining()) {
                horror$holdForSurfaceNight(level, weather);
            } else if (weather.isThundering()) {
                weather.setThundering(false);
            }
            horror$wasSurfaceNight = false;
            return;
        }
        horror$wasSurfaceNight = true;
        if (!weather.isRaining()) {
            return;
        }
        if (!weather.isThundering()) {
            weather.setThundering(true);
        }
        int rainTime = Math.max(weather.getRainTime(), 1);
        if (weather.getThunderTime() < rainTime) {
            weather.setThunderTime(rainTime);
        }
    }

    /**
     * The surface window just closed, so this storm is over. A fresh delay keeps the leftover
     * duration from turning the rain back on in daylight.
     */
    @Unique
    private static void horror$endStorm(ServerLevel level, WeatherData weather) {
        if (!weather.isRaining() && !weather.isThundering()) {
            return;
        }
        weather.setRaining(false);
        weather.setThundering(false);
        int delay = Math.max(1, ServerLevel.RAIN_DELAY.sample(level.getRandom()));
        weather.setRainTime(delay);
        weather.setThunderTime(delay);
    }

    /**
     * Rain that the cycle started in daylight waits until reverse monsters can spawn on the surface.
     */
    @Unique
    private static void horror$holdForSurfaceNight(ServerLevel level, WeatherData weather) {
        int time = Math.floorMod((int) level.getOverworldClockTime(), 24000);
        int until = time < ReverseMonsterSpawns.SURFACE_NIGHT_START
                ? ReverseMonsterSpawns.SURFACE_NIGHT_START - time
                : (24000 - time) + ReverseMonsterSpawns.SURFACE_NIGHT_START;
        if (weather.isRaining()) {
            weather.setRaining(false);
        }
        if (weather.isThundering()) {
            weather.setThundering(false);
        }
        int wait = Math.max(until, 1);
        weather.setRainTime(wait);
        weather.setThunderTime(wait);
    }

    @Unique
    private void horror$sendLevel(ServerLevel level, float rain, float thunder) {
        if (horror$sentRain != rain) {
            level.setRainLevel(rain);
            level.getServer().getPlayerList().broadcastAll(
                    new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, rain),
                    level.dimension()
            );
            horror$sentRain = rain;
        }
        if (horror$sentThunder != thunder) {
            level.setThunderLevel(thunder);
            level.getServer().getPlayerList().broadcastAll(
                    new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, thunder),
                    level.dimension()
            );
            horror$sentThunder = thunder;
        }
    }
}
