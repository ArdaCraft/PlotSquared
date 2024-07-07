package com.plotsquared.fabric.listener.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import xyz.nucleoid.stimuli.event.StimulusEvent;

import java.util.List;

public interface PistonMoveBlocksEvent {

    StimulusEvent<PistonMoveBlocksEvent> EVENT = StimulusEvent.create(PistonMoveBlocksEvent.class,
            (ctx) -> (level, blockState, blockPos, blocksPushed, blocksDestroyed, direction) -> {
        try {

            for (PistonMoveBlocksEvent listener : ctx.getListeners()) {
                InteractionResult result = listener.onMoveBlocks(level, blockState, blockPos,  blocksPushed, blocksDestroyed, direction);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        } catch (Throwable var8) {
            ctx.handleException(var8);
        }

        return InteractionResult.PASS;
    });

    InteractionResult onMoveBlocks(
            Level level, BlockPos blockPos,
            List<BlockPos> blocksPushed,List<BlockPos> blocksDestroyed, Direction direction, BlockState blockState);
}
