package com.plotsquared.fabric.listener.mixin;

import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.ConcreteHardenFlag;
import com.plotsquared.core.plot.flag.implementations.DisablePhysicsFlag;
import com.plotsquared.fabric.util.FabricUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.plotsquared.fabric.listener.HighFreqBlockEventListener.sendBlockChange;

@Mixin(FallingBlock.class)
public class FallingBlockMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void onTick(
            BlockState blockState,
            ServerLevel serverLevel,
            BlockPos blockPos,
            RandomSource randomSource,
            CallbackInfo ci
    ) {
        Location location = FabricUtil.adapt(GlobalPos.of(serverLevel.dimension(), blockPos));
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getPlot(location);
        if (plot != null) {
            if (blockState.getBlock() instanceof FallingBlock && plot.getFlag(DisablePhysicsFlag.class)) {
                sendBlockChange(GlobalPos.of(serverLevel.dimension(), blockPos), blockState);
                plot.debug("Prevented block physics and resent block change because disable-physics = true");
                ci.cancel();
            }
        }
    }
}
