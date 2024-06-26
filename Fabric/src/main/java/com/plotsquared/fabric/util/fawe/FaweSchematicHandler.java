package com.plotsquared.fabric.util.fawe;
/*
import com.google.inject.Inject;
import com.plotsquared.core.inject.factory.ProgressSubscriberFactory;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.schematic.Schematic;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.task.RunnableVal;
import com.sk89q.jnbt.CompoundTag;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

public class FaweSchematicHandler extends SchematicHandler {

    private final FaweDelegateSchematicHandler delegate = new FaweDelegateSchematicHandler();

    @Inject
    public FaweSchematicHandler(WorldUtil worldUtil, ProgressSubscriberFactory subscriberFactory) {
        super(worldUtil, subscriberFactory);
    }

    @Override
    public boolean restoreTile(QueueCoordinator queue, CompoundTag tag, int x, int y, int z) {
        return false;
    }

    @Override
    public void paste(
            final Schematic schematic,
            final Plot plot,
            final int xOffset,
            final int yOffset,
            final int zOffset,
            final boolean autoHeight,
            final PlotPlayer<?> actor,
            final RunnableVal<Boolean> whenDone
    ) {
        delegate.paste(schematic, plot, xOffset, yOffset, zOffset, autoHeight, actor, whenDone);
    }

    @Override
    public boolean save(CompoundTag tag, String path) {
        return delegate.save(tag, path);
    }

    @SuppressWarnings("removal") // Just the override
    @Override
    public void upload(final CompoundTag tag, final UUID uuid, final String file, final RunnableVal<URL> whenDone) {
        delegate.upload(tag, uuid, file, whenDone);
    }

    @Override
    public Schematic getSchematic(@NonNull InputStream is) {
        return delegate.getSchematic(is);
    }

}
*/
