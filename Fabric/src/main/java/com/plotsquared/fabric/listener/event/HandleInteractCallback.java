package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public interface HandleInteractCallback {

    Event<HandleInteractCallback> EVENT = EventFactory.createArrayBacked(
            HandleInteractCallback.class,
            callbacks -> (serverboundInteractPacket, serverPlayer) -> {
                for (HandleInteractCallback callback : callbacks) {
                    InteractionResult result = callback.handleInteractCallback(
                            serverboundInteractPacket, serverPlayer
                    );
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );


    InteractionResult handleInteractCallback(
            ServerboundInteractPacket serverboundInteractPacket,
            ServerPlayer serverPlayer
    );

}
