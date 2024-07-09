package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public interface GameEventEvent {

    Event<GameEventEvent> EVENT = EventFactory.createArrayBacked(
            GameEventEvent.class,
            callbacks -> (gameEvent, vec3, context, serverLevel) -> {
                for (GameEventEvent callback : callbacks) {
                    InteractionResult result = callback.onGameEvent(gameEvent, vec3, context, serverLevel);
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );


    InteractionResult onGameEvent(GameEvent gameEvent, Vec3 vec3, GameEvent.Context context, ServerLevel serverLevel);

}
