package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface EntityTypeCreateCallback<T> {


    Event<EntityTypeCreateCallback> EVENT = EventFactory.createArrayBacked(
            EntityTypeCreateCallback.class,
            callbacks -> (serverLevel, compoundTag, consumer, blockPos, mobSpawnType, bl, bl2, entity) -> {
                for (EntityTypeCreateCallback callback : callbacks) {
                    InteractionResult result = callback.onCreate(serverLevel, compoundTag, consumer, blockPos, mobSpawnType, bl
                            ,bl2, entity);
                    if (result != InteractionResult.PASS) {
                        return result;

                    }
                }
                return InteractionResult.PASS;
            }
    );

    InteractionResult onCreate(ServerLevel serverLevel, @Nullable CompoundTag compoundTag, @Nullable Consumer<?> consumer,
                               BlockPos blockPos, MobSpawnType mobSpawnType, boolean bl, boolean bl2, T entity);

}
