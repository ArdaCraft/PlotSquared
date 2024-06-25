package com.plotsquared.fabric.listener.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface BaseFireBlockOnPlaceCallback {
    Event<BaseFireBlockOnPlaceCallback> EVENT = EventFactory.createArrayBacked(
            BaseFireBlockOnPlaceCallback.class,
            callbacks -> (levelAccessor, blockPos, axis, original) -> {
                for (BaseFireBlockOnPlaceCallback callback : callbacks) {
                    InteractionResult result = callback.baseFireBlockOnPlaceCallback(
                            levelAccessor, blockPos, axis, original
                    );
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );


    InteractionResult baseFireBlockOnPlaceCallback(
            LevelAccessor levelAccessor, BlockPos blockPos, Direction.Axis axis, Operation<Optional<PortalShape>> original
    );

}
