package com.plotsquared.fabric.util.task;

import com.plotsquared.fabric.FabricPlatform;
import org.jetbrains.annotations.NotNull;

public interface FabricWorker {

    int getTaskId();

    @NotNull FabricPlatform getOwner();

    @NotNull Thread getThread();

}
