package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;

public interface ExplosionPrimedEvent {

    Event<ExplosionPrimedEvent> EVENT = EventFactory.createArrayBacked(
            ExplosionPrimedEvent.class,
            callbacks -> (radius) -> {
                for (ExplosionPrimedEvent callback : callbacks) {
                    InteractionResult result = callback.onPrime(radius);
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            }
    );


    InteractionResult onPrime(float radius);

}
