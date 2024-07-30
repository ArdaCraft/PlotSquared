package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public interface LevelSetBlockEvent {

    Event<LevelSetBlockEvent> EVENT = EventFactory.createArrayBacked(
            LevelSetBlockEvent.class,
            callbacks -> (
                    blockPos, blockState, i, level
            ) -> {
                for (LevelSetBlockEvent callback : callbacks) {
                    InteractionResult result = callback.onSetBlock(blockPos, blockState, i, level
                    );
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );

    InteractionResult onSetBlock(
            BlockPos blockPos,
            BlockState blockState,
            int i,
            Level level
    );


}
