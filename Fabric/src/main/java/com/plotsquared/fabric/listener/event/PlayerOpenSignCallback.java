package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public interface PlayerOpenSignCallback {

    Event<PlayerOpenSignCallback> EVENT = EventFactory.createArrayBacked(
            PlayerOpenSignCallback.class,
            callbacks -> (signBlockEntity, bl, serverPlayer) -> {
                for (PlayerOpenSignCallback callback : callbacks) {
                    InteractionResult result = callback.onOpenSign(
                            signBlockEntity, bl, serverPlayer);
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );


    InteractionResult onOpenSign(
            SignBlockEntity signBlockEntity, boolean bl, ServerPlayer serverPlayer);


}
