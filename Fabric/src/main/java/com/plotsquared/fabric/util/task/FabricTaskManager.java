package com.plotsquared.fabric.util.task;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.util.task.PlotSquaredTask;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.plotsquared.fabric.FabricPlatform;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class FabricTaskManager extends TaskManager {

    private final FabricPlatform fabricPlatform;
    private final TaskTime.TimeConverter timeConverter;

    @Inject
    public FabricTaskManager(
            final @NonNull FabricPlatform fabricPlatform,
            final TaskTime.@NonNull TimeConverter timeConverter
    ) {
        this.fabricPlatform = fabricPlatform;
        this.timeConverter = timeConverter;
    }

    @Override
    public PlotSquaredTask taskRepeat(
            final @NonNull Runnable runnable,
            final @NonNull TaskTime taskTime
    ) {
        final long ticks = this.timeConverter.toTicks(taskTime);
        final FabricPlotSquaredTask bukkitPlotSquaredTask = new FabricPlotSquaredTask(runnable);
        bukkitPlotSquaredTask.runTaskTimer(this.fabricPlatform, ticks, ticks);
        return bukkitPlotSquaredTask;
    }

    @Override
    public PlotSquaredTask taskRepeatAsync(
            final @NonNull Runnable runnable,
            final @NonNull TaskTime taskTime
    ) {
        final long ticks = this.timeConverter.toTicks(taskTime);
        final FabricPlotSquaredTask bukkitPlotSquaredTask = new FabricPlotSquaredTask(runnable);
        bukkitPlotSquaredTask.runTaskTimerAsynchronously(this.fabricPlatform, ticks, ticks);
        return bukkitPlotSquaredTask;
    }

    @Override
    public void taskAsync(final @NonNull Runnable runnable) {
        new FabricPlotSquaredTask(runnable).runTaskAsynchronously(this.fabricPlatform);
    }

    @Override
    public <T> T sync(final @NonNull Callable<T> function, final int timeout) throws Exception {
        if (PlotSquared.get().isMainThread(Thread.currentThread())) {
            return function.call();
        }
        return this.callMethodSync(function).get(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public <T> Future<T> callMethodSync(final @NonNull Callable<T> method) {
        return fabricPlatform.getScheduler().callSyncMethod(this.fabricPlatform, method);
    }

    @Override
    public void task(final @NonNull Runnable runnable) {
        new FabricPlotSquaredTask(runnable).runTask(this.fabricPlatform);
    }

    @Override
    public void taskLater(
            final @NonNull Runnable runnable,
            final @NonNull TaskTime taskTime
    ) {
        final long delay = this.timeConverter.toTicks(taskTime);
        new FabricPlotSquaredTask(runnable).runTaskLater(this.fabricPlatform, delay);
    }

    @Override
    public void taskLaterAsync(
            final @NonNull Runnable runnable,
            final @NonNull TaskTime taskTime
    ) {
        final long delay = this.timeConverter.toTicks(taskTime);
        new FabricPlotSquaredTask(runnable).runTaskLaterAsynchronously(this.fabricPlatform, delay);
    }

}
