package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockIgniteEvent {

    Event<BlockIgniteEvent> EVENT = EventFactory.createArrayBacked(
            BlockIgniteEvent.class,
            callbacks -> (blockPos, blockState, i, level) -> {
                for (BlockIgniteEvent callback : callbacks) {
                    InteractionResult result = callback.onIgnite(blockPos, blockState, i, level);
                    if (result != InteractionResult.PASS) {
                        return result;

                    }
                }
                return InteractionResult.PASS;
            }
    );

    InteractionResult onIgnite(
            BlockPos blockPos, BlockState blockState, int i, Level level);


}
