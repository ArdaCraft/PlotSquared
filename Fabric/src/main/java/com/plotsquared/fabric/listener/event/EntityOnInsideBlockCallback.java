package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public interface EntityOnInsideBlockCallback {
    Event<EntityOnInsideBlockCallback> EVENT = EventFactory.createArrayBacked(
            EntityOnInsideBlockCallback.class,
            callbacks -> (blockState, entity) -> {
                for (EntityOnInsideBlockCallback callback : callbacks) {
                    InteractionResult result = callback.entityOnInsideBlockCallback(
                            blockState, entity
                    );
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );


    InteractionResult entityOnInsideBlockCallback(
            BlockState blockState,
            Entity entity
    );
    
}
