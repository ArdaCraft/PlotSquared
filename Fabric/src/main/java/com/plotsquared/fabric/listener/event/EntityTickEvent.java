package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;

public interface EntityTickEvent {

    Event<EntityTickEvent> EVENT = EventFactory.createArrayBacked(
            EntityTickEvent.class,
            callbacks -> (entity) -> {
                for (EntityTickEvent callback : callbacks) {
                    InteractionResult result = callback.onTick(
                            entity
                    );
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );


    InteractionResult onTick(
            Entity entity
    );
}
