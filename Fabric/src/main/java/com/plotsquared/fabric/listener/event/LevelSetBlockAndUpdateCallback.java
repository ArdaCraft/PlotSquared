package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface LevelSetBlockAndUpdateCallback {

    Event<LevelSetBlockAndUpdateCallback> EVENT = EventFactory.createArrayBacked(
            LevelSetBlockAndUpdateCallback.class,
            callbacks -> (blockPos, blockState, livingEntity, level) -> {
                for (LevelSetBlockAndUpdateCallback callback : callbacks) {
                    InteractionResult result = callback.onSetBlockAndUpdate(blockPos, blockState, livingEntity, level);
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );

    InteractionResult onSetBlockAndUpdate(
            BlockPos blockPos,
            BlockState blockState,
            @Nullable LivingEntity entity, Level level);

}
