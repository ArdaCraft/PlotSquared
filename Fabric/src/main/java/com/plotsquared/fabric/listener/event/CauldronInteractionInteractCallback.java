package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public interface CauldronInteractionInteractCallback {

    Event<CauldronInteractionInteractCallback> EVENT = EventFactory.createArrayBacked(
            CauldronInteractionInteractCallback.class,
            callbacks -> (blockState, level, blockPos, player, interactionHand, blockHitResult, cauldronInteraction) -> {
                for (CauldronInteractionInteractCallback callback : callbacks) {
                    InteractionResult result = callback.onInteract(
                            blockState, level, blockPos, player, interactionHand, blockHitResult, cauldronInteraction
                    );
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );


    InteractionResult onInteract(
            BlockState par1,
            Level par2,
            BlockPos par3,
            Player par4,
            InteractionHand par5,
            ItemStack par6,
            CauldronInteraction cauldronInteraction
    );


}
