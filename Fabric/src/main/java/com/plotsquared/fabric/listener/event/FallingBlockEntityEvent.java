package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;

public interface FallingBlockEntityEvent {

    Event<FallingBlockEntityEvent> EVENT = EventFactory.createArrayBacked(
            FallingBlockEntityEvent.class,
            callbacks -> (entity) -> {
                for (FallingBlockEntityEvent callback : callbacks) {
                    InteractionResult result = callback.onFall(entity);
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );


    InteractionResult onFall(Entity entity);
}
