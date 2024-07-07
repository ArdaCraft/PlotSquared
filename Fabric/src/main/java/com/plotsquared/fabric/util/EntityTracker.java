package com.plotsquared.fabric.util;

import net.minecraft.world.entity.LivingEntity;

public class EntityTracker {
    private static final ThreadLocal<LivingEntity> CURRENT_ENTITY = new ThreadLocal<>();

    public static void setCurrentEntity(LivingEntity entity) {
        CURRENT_ENTITY.set(entity);
    }

    public static LivingEntity getCurrentEntity() {
        return CURRENT_ENTITY.get();
    }

    public static void clearCurrentEntity() {
        CURRENT_ENTITY.remove();
    }
}
