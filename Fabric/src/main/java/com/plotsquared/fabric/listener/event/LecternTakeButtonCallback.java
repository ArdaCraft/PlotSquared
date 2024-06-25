package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface LecternTakeButtonCallback {

    Event<LecternTakeButtonCallback> EVENT = EventFactory.createArrayBacked(
            LecternTakeButtonCallback.class, callbacks -> (serverboundContainerButtonClickPacket, serverPlayer) -> {
                for (LecternTakeButtonCallback callback : callbacks) {
                    InteractionResult result = callback.onTakeButton(serverboundContainerButtonClickPacket, serverPlayer);
                    if (result != InteractionResult.PASS) {
                        return result;

                    }
                }
                return InteractionResult.PASS;
            }
    );

    InteractionResult onTakeButton(ServerboundContainerButtonClickPacket serverboundContainerButtonClickPacket, ServerPlayer serverPlayer);

}
