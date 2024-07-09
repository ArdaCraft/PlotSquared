package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;

public interface EntityPathfindEvent {

    Event<EntityPathfindEvent> EVENT = EventFactory.createArrayBacked(
            EntityPathfindEvent.class,
            callbacks -> (entity, blockPos, levelReader) -> {
                for (EntityPathfindEvent callback : callbacks) {
                    InteractionResult result = callback.onEntityPathfind(
                            entity, blockPos, levelReader
                    );
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );


    InteractionResult onEntityPathfind(
            Entity entity,
            BlockPos blockPos,
            LevelReader levelReader
    );

}
