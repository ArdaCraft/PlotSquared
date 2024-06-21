package com.plotsquared.fabric.placeholder;

import com.google.common.eventbus.Subscribe;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.placeholders.Placeholder;
import com.plotsquared.core.util.placeholders.PlaceholderRegistry;
import com.plotsquared.fabric.util.FabricUtil;
import io.github.miniplaceholders.api.Expansion;
import io.github.miniplaceholders.api.utils.TagsUtils;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class MiniPlaceholders {

    private Expansion expansion = null;
    private final PlaceholderRegistry registry;

    public MiniPlaceholders(final @NonNull PlaceholderRegistry registry) {
        this.registry = registry;
        this.createExpansion();
        PlotSquared.get().getEventDispatcher().registerListener(this);
    }

    @Subscribe
    public void onNewPlaceholder(final PlaceholderRegistry.@NonNull PlaceholderAddedEvent event) {
        // We cannot register placeholders on the fly, so we have to replace the expansion.
        this.createExpansion();
    }

    private synchronized void createExpansion() {
        if (this.expansion != null && this.expansion.registered()) {
            this.expansion.unregister();
        }
        final Expansion.Builder builder = Expansion.builder("plotsquared");
        for (final Placeholder placeholder : this.registry.getPlaceholders()) {
            builder.audiencePlaceholder(placeholder.getKey(), (audience, argumentQueue, context) -> {
                final PlotPlayer<?> plotPlayer;
                if (audience instanceof ServerPlayer player) {
                    plotPlayer = FabricUtil.adapt(player);
                } else {
                    plotPlayer = ConsolePlayer.getConsole();
                }
                return TagsUtils.staticTag(placeholder.getValue(plotPlayer));
            });
        }
        this.expansion = builder.build();
        this.expansion.register();
    }
}
