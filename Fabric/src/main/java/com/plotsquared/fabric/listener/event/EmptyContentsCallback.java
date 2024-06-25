package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public interface EmptyContentsCallback {
    Event<EmptyContentsCallback> EVENT = EventFactory.createArrayBacked(
            EmptyContentsCallback.class,
            callbacks -> (player, level, blockPos, blockHitResult) -> {
                for (EmptyContentsCallback callback : callbacks) {
                    InteractionResult result = callback.emptyContentsCallback(
                            player, level, blockPos, blockHitResult
                    );
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );


    InteractionResult emptyContentsCallback(
            @Nullable Player player, Level level, BlockPos blockPos, @Nullable BlockHitResult blockHitResult
    );

}
