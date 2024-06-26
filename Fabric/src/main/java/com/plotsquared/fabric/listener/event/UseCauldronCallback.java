package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public interface UseCauldronCallback {

    Event<UseCauldronCallback> EVENT = EventFactory.createArrayBacked(
            UseCauldronCallback.class,
            callbacks -> (blockState, level, blockPos, player, interactionHand, blockHitResult) -> {
                for (UseCauldronCallback callback : callbacks) {
                    InteractionResult result = callback.useCauldronCallback(
                            blockState, level, blockPos, player, interactionHand, blockHitResult
                    );
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );


    InteractionResult useCauldronCallback(
            BlockState blockState,
            Level level,
            BlockPos blockPos,
            Player player,
            InteractionHand interactionHand,
            BlockHitResult blockHitResult
    );


}
