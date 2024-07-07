package com.plotsquared.fabric.listener.mixin;

import com.plotsquared.fabric.listener.event.LevelSetBlockAndUpdateCallback;
import com.plotsquared.fabric.listener.event.LevelSetBlockEvent;
import com.plotsquared.fabric.util.EntityTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Level.class)
public class LevelMixin {

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at =
    @At(value = "HEAD"), locals =
            LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void onSetBlocksDirty(

            BlockPos blockPos, BlockState blockState, int i, int j, CallbackInfoReturnable<Boolean> cir
    ) {
        InteractionResult result = LevelSetBlockEvent.EVENT.invoker().onSetBlock(
                blockPos, blockState, i, j, (Level) (Object) this);
        if (result != InteractionResult.PASS) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "setBlockAndUpdate", at = @At("HEAD"), cancellable = true)
    private void onSetBlockAndUpdate(BlockPos blockPos, BlockState blockState, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity currentEntity = EntityTracker.getCurrentEntity();
        if (currentEntity != null) {
            // Perform custom logic here using currentEntity
            InteractionResult result = LevelSetBlockAndUpdateCallback.EVENT.invoker().onSetBlockAndUpdate(blockPos, blockState,
                    currentEntity
            );
            if (result != InteractionResult.PASS) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }

}
