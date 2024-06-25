package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public interface HandleContainerCloseCallback {

    Event<HandleContainerCloseCallback> EVENT = EventFactory.createArrayBacked(
            HandleContainerCloseCallback.class,
            callbacks -> (serverboundContainerClosePacket, serverPlayer) -> {
                for (HandleContainerCloseCallback callback : callbacks) {
                    InteractionResult result = callback.handleContainerCloseCallback(
                            serverboundContainerClosePacket, serverPlayer
                    );
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );


    InteractionResult handleContainerCloseCallback(
            ServerboundContainerClosePacket serverboundContainerClosePacket,
            ServerPlayer serverPlayer
    );

}
