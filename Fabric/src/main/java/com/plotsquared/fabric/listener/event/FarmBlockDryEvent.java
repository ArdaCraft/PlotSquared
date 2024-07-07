package com.plotsquared.fabric.listener.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public interface FarmBlockDryEvent {

    StimulusEvent<FarmBlockDryEvent> EVENT = StimulusEvent.create(FarmBlockDryEvent.class, (ctx) -> (entity, blockState, level, blockPos, from) -> {
        try {

            for (FarmBlockDryEvent listener : ctx.getListeners()) {
                InteractionResult result = listener.onSoilDry(entity, blockState, level, blockPos, from);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        } catch (Throwable var8) {
            ctx.handleException(var8);
        }

        return InteractionResult.PASS;
    });

    InteractionResult onSoilDry(Entity entity, BlockState blockState, Level level, BlockPos blockPos, BlockState from);
}
