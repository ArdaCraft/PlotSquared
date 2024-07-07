package com.plotsquared.fabric.listener.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public interface FarmBlockMoistureChangeEvent {

    StimulusEvent<FarmBlockMoistureChangeEvent> EVENT = StimulusEvent.create(FarmBlockMoistureChangeEvent.class, (ctx) -> (
            serverLevel,
            blockPos,
            blockState,
            i,
            from
    ) -> {
        try {

            for (FarmBlockMoistureChangeEvent listener : ctx.getListeners()) {
                InteractionResult result = listener.onMoistureChange(
                        serverLevel,
                        blockPos,
                        blockState,
                        i,
                        from
                );
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        } catch (Throwable var8) {
            ctx.handleException(var8);
        }

        return InteractionResult.PASS;
    });

    InteractionResult onMoistureChange(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, int i, BlockState from);

}
