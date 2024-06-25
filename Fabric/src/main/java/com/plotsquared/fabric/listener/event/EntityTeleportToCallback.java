package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public interface EntityTeleportToCallback {

    Event<EntityTeleportToCallback> EVENT = EventFactory.createArrayBacked(
            EntityTeleportToCallback.class,
            callbacks -> (serverLevel, d, e, f, set, g, h, entity) -> {
                for (EntityTeleportToCallback callback : callbacks) {
                    InteractionResult result = callback.entityTeleportToCallback(
                            serverLevel, d, e, f, set, g, h, entity
                    );
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );


    InteractionResult entityTeleportToCallback(
            ServerLevel serverLevel,
            double d,
            double e,
            double f,
            Set<RelativeMovement> set,
            float g,
            float h,
            Entity entity
    );


}
