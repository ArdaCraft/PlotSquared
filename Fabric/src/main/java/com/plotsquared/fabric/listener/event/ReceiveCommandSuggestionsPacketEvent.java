package com.plotsquared.fabric.listener.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public interface ReceiveCommandSuggestionsPacketEvent {


    Event<ReceiveCommandSuggestionsPacketEvent> EVENT = EventFactory.createArrayBacked(
            ReceiveCommandSuggestionsPacketEvent.class, callbacks -> (serverboundCommandSuggestionPacket, serverPlayer) -> {
                for (ReceiveCommandSuggestionsPacketEvent callback : callbacks) {
                    InteractionResult result = callback.onSendPacket(serverboundCommandSuggestionPacket, serverPlayer);
                    if (result != InteractionResult.PASS) {
                        return result;

                    }
                }
                return InteractionResult.PASS;
            }
    );

    InteractionResult onSendPacket(
            ServerboundCommandSuggestionPacket serverboundCommandSuggestionPacket,
            ServerPlayer serverPlayer
    );
}
