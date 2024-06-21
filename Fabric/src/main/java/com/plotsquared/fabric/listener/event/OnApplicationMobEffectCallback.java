package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface OnApplicationMobEffectCallback {

    Event<OnApplicationMobEffectCallback> EVENT = EventFactory.createArrayBacked(
            OnApplicationMobEffectCallback.class, callbacks -> (effectInstance, source, entity, amplifier) -> {
                for (OnApplicationMobEffectCallback callback : callbacks) {
                    callback.onApplication(effectInstance, source, entity, amplifier);
                }
            }

    );
    void onApplication(@Nullable MobEffectInstance effectInstance, @Nullable Entity source, LivingEntity entity, int amplifier);

}
