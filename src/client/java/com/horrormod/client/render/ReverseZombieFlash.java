package com.horrormod.client.render;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * Each reverse zombie turns pitch black for 16 to 80 milliseconds, then waits 0.1 to 1 second.
 */
public final class ReverseZombieFlash {
    private static final Random RANDOM = new Random();
    private static final Map<Integer, Schedule> SCHEDULES = new HashMap<>();

    private ReverseZombieFlash() {
    }

    public static boolean isBlack(int entityId) {
        long now = System.nanoTime();
        Schedule schedule = SCHEDULES.computeIfAbsent(entityId, id -> Schedule.after(now));
        schedule.lastSeen = now;
        if (now >= schedule.nextFlash) {
            long duration = (1L + RANDOM.nextInt(5)) * 16L * 1_000_000L;
            schedule.flashStart = now;
            schedule.flashEnd = now + duration;
            double gapSeconds = 0.1 + RANDOM.nextDouble() * 0.9;
            schedule.nextFlash = schedule.flashEnd + (long) (gapSeconds * 1_000_000_000.0);
        }
        if (SCHEDULES.size() > 64) {
            Iterator<Schedule> schedules = SCHEDULES.values().iterator();
            while (schedules.hasNext()) {
                if (now - schedules.next().lastSeen > 30_000_000_000L) {
                    schedules.remove();
                }
            }
        }
        return now >= schedule.flashStart && now < schedule.flashEnd;
    }

    private static final class Schedule {
        private long flashStart;
        private long flashEnd;
        private long nextFlash;
        private long lastSeen;

        private static Schedule after(long now) {
            Schedule schedule = new Schedule();
            double gapSeconds = 0.1 + RANDOM.nextDouble() * 0.9;
            schedule.nextFlash = now + (long) (gapSeconds * 1_000_000_000.0);
            schedule.lastSeen = now;
            return schedule;
        }
    }
}
