package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface RedstonePowerUpdateEvent {

    Event<RedstonePowerUpdateEvent> EVENT = EventFactory.createArrayBacked(
            RedstonePowerUpdateEvent.class,
            callbacks -> (level, blockState, blockPos) -> {
                for (RedstonePowerUpdateEvent callback : callbacks) {
                    InteractionResult result = callback.onUpdatePower(level, blockState, blockPos);
                    if (result != InteractionResult.PASS) {
                        return result;

                    }
                }
                return InteractionResult.PASS;
            }
    );

    InteractionResult onUpdatePower(
            Level level,
            BlockState blockState,
            BlockPos blockPos
    );


}
