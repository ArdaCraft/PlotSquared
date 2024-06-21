package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public interface HandleMoveVehicleCallback {

    Event<HandleMoveVehicleCallback> EVENT = EventFactory.createArrayBacked(
            HandleMoveVehicleCallback.class,
            callbacks -> (serverboundMoveVehiclePacket, serverPlayer) -> {
                for (HandleMoveVehicleCallback callback : callbacks) {
                    InteractionResult result = callback.handleMoveVehicleCallback(
                            serverboundMoveVehiclePacket, serverPlayer
                    );
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );


    InteractionResult handleMoveVehicleCallback(
            ServerboundMoveVehiclePacket serverboundMoveVehiclePacket,
            ServerPlayer serverPlayer
    );

}
