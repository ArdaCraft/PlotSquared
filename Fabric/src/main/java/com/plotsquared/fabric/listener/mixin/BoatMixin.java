package com.plotsquared.fabric.listener.mixin;

import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.fabric.util.FabricUtil;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Boat.class)
public class BoatMixin {


    @Inject(method = "canVehicleCollide", at = @At("HEAD"), cancellable = true)
    private static void checkPlotCollision(Entity entity, Entity entity2, CallbackInfoReturnable<Boolean> cir) {
        if (entity.getType().equals(EntityType.BOAT)) {
            Location location = FabricUtil.adapt(GlobalPos.of(entity2.level().dimension(), entity2.blockPosition()));
            if (location.isPlotArea()) {
                if (entity2 instanceof ServerPlayer serverPlayer) {
                    PlotPlayer<ServerPlayer> player = FabricUtil.adapt(serverPlayer);
                    Plot plot = player.getCurrentPlot();
                    if(plot != null) {
                        if(!plot.isAdded(player.getUUID())) {
                            cir.setReturnValue(false);
                        }
                    } else {
                        cir.setReturnValue(false);
                    }
                } else  {
                    cir.setReturnValue(false);
                }
            }
        }
    }

}
