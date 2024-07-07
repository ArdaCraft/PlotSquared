package com.plotsquared.fabric.util.task;

import com.plotsquared.core.util.task.PlotSquaredTask;
import org.checkerframework.checker.nullness.qual.NonNull;

public class FabricPlotSquaredTask extends FabricRunnable implements PlotSquaredTask {

    @NonNull
    private final Runnable runnable;

    public FabricPlotSquaredTask(@NonNull Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void runTask() {
        this.runnable.run();
    }

}
