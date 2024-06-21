package com.plotsquared.fabric.schematic;

import com.google.inject.Inject;
import com.plotsquared.core.inject.factory.ProgressSubscriberFactory;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.WorldUtil;
import com.sk89q.jnbt.CompoundTag;
import org.checkerframework.checker.nullness.qual.NonNull;

public class FabricSchematicHandler extends SchematicHandler {

    @Inject
    public FabricSchematicHandler(final @NonNull WorldUtil worldUtil, @NonNull ProgressSubscriberFactory subscriberFactory) {
        super(worldUtil, subscriberFactory);
    }

    @Override
    public boolean restoreTile(QueueCoordinator queue, CompoundTag ct, int x, int y, int z) {
        return new StateWrapper(ct).restoreTag(queue.getWorld().getName(), x, y, z);
    }

}
