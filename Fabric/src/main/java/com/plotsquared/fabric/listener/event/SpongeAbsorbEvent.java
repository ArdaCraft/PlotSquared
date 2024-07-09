package com.plotsquared.fabric.listener.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import xyz.nucleoid.stimuli.event.StimulusEvent;
public interface SpongeAbsorbEvent {

    StimulusEvent<SpongeAbsorbEvent> EVENT = StimulusEvent.create(SpongeAbsorbEvent.class, ctx -> (pos, level, pos2) -> {
        try {
            for (var listener : ctx.getListeners()) {
                var result = listener.onRemoveWater(pos, level, pos2);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
        return InteractionResult.PASS;
    });

    InteractionResult onRemoveWater(BlockPos blockPos, Level level, BlockPos blockPos2);

}
