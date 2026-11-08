package com.horrormod.event;

import java.util.Arrays;
import java.util.List;

/**
 * The one-shot events. Each id is saved once it happens, so it cannot be chosen again.
 */
public enum HorrorEvent {
    STRETCHED_FLESH("stretched_flesh"),
    BARK("bark"),
    TORCH_REDSTONE("torch_redstone"),
    TORCH_VANISH("torch_vanish"),
    SNAP_TIME("snap_time"),
    HOLE("hole"),
    REVERSE_BEHIND("reverse_behind"),
    SHUFFLE("shuffle"),
    DELETE_ITEMS("delete_items"),
    GIVE_GLITCH("give_glitch"),
    SITTING_WOLF("sitting_wolf"),
    PLACED_VANISH("placed_vanish"),
    FLICKER("flicker"),
    CELESTIAL("celestial"),
    STARS("stars"),
    DESPAWN_HOSTILES("despawn_hostiles"),
    BRIGHTNESS("brightness"),
    CRASH_TOAST("crash_toast"),
    GL_ERROR("gl_error"),
    GLITCH_SOUND("glitch_sound"),
    FOOTSTEPS("footsteps"),
    HOTBAR_SKIP("hotbar_skip"),
    GRASS_DIRT("grass_dirt"),
    GHOST_MINE("ghost_mine"),
    CAVE_BREATH("cave_breath"),
    CHEST_LID("chest_lid"),
    WRONG_STEP("wrong_step");

    private final String id;

    HorrorEvent(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public static HorrorEvent byId(String id) {
        for (HorrorEvent event : values()) {
            if (event.id.equals(id)) {
                return event;
            }
        }
        return null;
    }

    public static List<String> ids() {
        return Arrays.stream(values()).map(HorrorEvent::id).toList();
    }
}
