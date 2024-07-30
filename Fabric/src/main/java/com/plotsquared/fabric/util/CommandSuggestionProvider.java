package com.plotsquared.fabric.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.plotsquared.fabric.FabricCommand;
import net.minecraft.commands.CommandSourceStack;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class CommandSuggestionProvider implements SuggestionProvider<CommandSourceStack> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(
            final CommandContext<CommandSourceStack> context,
            final SuggestionsBuilder builder
    ) {

        List<String> suggestions = FabricCommand.onTabComplete(context.getSource(), context.getCommand(),

                context.getInput().substring(1, context.getInput().indexOf(" ")),

                context.getInput().replace(context.getInput().substring(0, context.getInput().indexOf(" ")), "").split(" ")
        );

        if (suggestions != null) {
            for (String suggestion : suggestions) {
                builder.suggest(suggestion);
            }
        }

        return builder.buildFuture();
    }

}
