package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.RelativeMovement;

import java.util.Set;

public interface ServerPlayerTeleportToCallback {

    Event<ServerPlayerTeleportToCallback> EVENT = EventFactory.createArrayBacked(
            ServerPlayerTeleportToCallback.class,
            callbacks -> (serverLevel, x, y, z, relativeMovementSet, g, h, serverPlayer) -> {
                for (ServerPlayerTeleportToCallback callback : callbacks) {
                    InteractionResult result = callback.serverPlayerTeleportToCallback(serverLevel, x, y, z, relativeMovementSet, g, h, serverPlayer);
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );


    InteractionResult serverPlayerTeleportToCallback(
            ServerLevel serverLevel,
            double d,
            double e,
            double f,
            Set<RelativeMovement> set,
            float g,
            float h, ServerPlayer serverPlayer);

}
