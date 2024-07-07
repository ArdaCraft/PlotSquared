package com.plotsquared.fabric.util.task;

import com.plotsquared.fabric.FabricPlatform;
import org.jetbrains.annotations.NotNull;

public interface FabricTask {

    int getTaskId();

    @NotNull
    FabricPlatform getOwner();

    boolean isSync();

    boolean isCancelled();

    void cancel();

}
