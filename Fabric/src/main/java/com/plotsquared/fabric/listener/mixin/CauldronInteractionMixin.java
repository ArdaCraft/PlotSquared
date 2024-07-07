package com.plotsquared.fabric.listener.mixin;


import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.plotsquared.fabric.listener.event.CauldronInteractionInteractCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CauldronInteraction.class)
public interface CauldronInteractionMixin {
/*
    @WrapMethod(method = "interact")
    default InteractionResult onInteract(
            BlockState par1,
            Level par2,
            BlockPos par3,
            Player par4,
            InteractionHand par5,
            ItemStack par6,
            Operation<InteractionResult> original
    ) {
        InteractionResult result =
                CauldronInteractionInteractCallback.EVENT.invoker().onInteract(
                        par1,
                        par2,
                        par3,
                        par4,
                        par5,
                        par6,
                        (CauldronInteraction) this
                );
        if (result != InteractionResult.PASS) {
            return InteractionResult.PASS;
        }
        return original.call(par1, par2, par3, par4, par5, par6);
    }
*/
}
