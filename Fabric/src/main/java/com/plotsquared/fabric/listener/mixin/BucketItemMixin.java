package com.plotsquared.fabric.listener.mixin;

import com.plotsquared.fabric.listener.event.EmptyContentsCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public class BucketItemMixin {

    @Inject(method = "emptyContents", at = @At("HEAD"), cancellable = true)
    public void onEmptyContents(
            Player player,
            Level level,
            BlockPos blockPos,
            BlockHitResult blockHitResult,
            CallbackInfoReturnable<Boolean> cir
    ) {
        InteractionResult result =
                EmptyContentsCallback.EVENT.invoker().emptyContentsCallback(
                        player,
                        level,
                        blockPos,
                        blockHitResult
                );
        if (result != InteractionResult.PASS) {
            cir.setReturnValue(false);
        }
    }



}
