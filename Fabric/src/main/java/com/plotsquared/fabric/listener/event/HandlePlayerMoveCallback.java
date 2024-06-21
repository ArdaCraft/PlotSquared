package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public interface HandlePlayerMoveCallback {

    Event<HandlePlayerMoveCallback> EVENT = EventFactory.createArrayBacked(
            HandlePlayerMoveCallback.class, callbacks -> (serverboundMovePlayerPacket, serverPlayer) -> {
                for (HandlePlayerMoveCallback callback : callbacks) {
                    InteractionResult result = callback.handlePlayerMoveCallback(serverboundMovePlayerPacket, serverPlayer);
                    if (result != InteractionResult.PASS) {
                        return result;

                    }
                }
                return InteractionResult.PASS;
            }
    );

    InteractionResult handlePlayerMoveCallback(
            ServerboundMovePlayerPacket serverboundMovePlayerPacket,
            ServerPlayer serverPlayer
    );

}

