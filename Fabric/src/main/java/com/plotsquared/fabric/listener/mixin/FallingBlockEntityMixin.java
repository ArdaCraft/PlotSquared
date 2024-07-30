package com.plotsquared.fabric.listener.mixin;

import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.DisablePhysicsFlag;
import com.plotsquared.fabric.listener.event.FallingBlockEntityEvent;
import com.plotsquared.fabric.util.FabricUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.plotsquared.fabric.listener.HighFreqBlockEventListener.sendBlockChange;

@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        InteractionResult result = FallingBlockEntityEvent.EVENT.invoker().onFall((FallingBlockEntity) (Object) this);
        if(result != InteractionResult.PASS) {
            ci.cancel();
        }
    }
}
