package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface OnExecuteUpdateCallback {

    Event<OnExecuteUpdateCallback> EVENT = EventFactory.createArrayBacked(
            OnExecuteUpdateCallback.class, callbacks -> (level, blockState, blockPos, block, blockPos2, bl) -> {
                for (OnExecuteUpdateCallback callback : callbacks) {
                    InteractionResult result = callback.onExecuteUpdate(level, blockState, blockPos, block, blockPos2, bl);
                    if (result != InteractionResult.PASS) {
                        return result;

                    }
                }
                return InteractionResult.PASS;
            }
    );

    InteractionResult onExecuteUpdate(
            Level level,
            BlockState blockState,
            BlockPos blockPos,
            Block block,
            BlockPos blockPos2,
            boolean bl
    );

}
