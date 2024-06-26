package com.plotsquared.fabric.listener.mixin;


import com.plotsquared.fabric.listener.event.EmptyContentsCallback;
import com.plotsquared.fabric.listener.event.UseCauldronCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractCauldronBlock.class)
public class AbstractCauldronBlockMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void onUse(
            BlockState blockState,
            Level level,
            BlockPos blockPos,
            Player player,
            InteractionHand interactionHand,
            BlockHitResult blockHitResult,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        InteractionResult result =
                UseCauldronCallback.EVENT.invoker().useCauldronCallback(
                       blockState,
                        level,
                        blockPos,
                        player,
                        interactionHand,
                        blockHitResult
                );
        if (result != InteractionResult.PASS) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

}
