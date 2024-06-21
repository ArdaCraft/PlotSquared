package com.plotsquared.fabric.util.task;

import com.plotsquared.core.util.task.PlotSquaredTask;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.fabric.FabricPlatform;
import org.checkerframework.checker.nullness.qual.NonNull;


public class FabricPlotSquaredTask implements PlotSquaredTask {

    @NonNull
    private final Runnable runnable;
    private FabricTickListener.Task fabricTask;

    public FabricPlotSquaredTask(
            final @NonNull Runnable runnable
    ) {
        this.runnable = runnable;
    }

    @Override
    public void runTask() {
        this.runnable.run();
    }

    @Override
    public boolean isCancelled() {
        return this.fabricTask == null;
    }

    @Override
    public void cancel() {
        FabricTaskManager.fabricTickListener.getTasksQueue().remove(FabricTaskManager.fabricTickListener.getTasksQueue().stream().filter(t -> t.taskId == fabricTask.taskId).findFirst().orElse(null));
        this.fabricTask = null;
    }

    public void runTaskTimer(FabricPlatform fabricPlatform, long delay, long interval) {
        fabricTask = new FabricTickListener.Task(
                FabricTaskManager.fabricTickListener.getTasksQueue().size(),
                FabricPlatform.SERVER.getNextTickTime()+delay,
                this.runnable,
                interval,
                false,
                true);
                FabricTaskManager.fabricTickListener.addTask(fabricTask);
    }

    public void runTaskTimerAsynchronously(FabricPlatform fabricMain, long delay, long interval) {
        fabricTask = new FabricTickListener.Task(
                FabricTaskManager.fabricTickListener.getTasksQueue().size(),
                FabricPlatform.SERVER.getNextTickTime()+delay,
                this.runnable,
                interval,
                true,
                true);
        FabricTaskManager.fabricTickListener.addTask(fabricTask);
    }

    public void runTaskAsynchronously(FabricPlatform fabricMain) {
        fabricTask = new FabricTickListener.Task(
                FabricTaskManager.fabricTickListener.getTasksQueue().size(),
                FabricPlatform.SERVER.getNextTickTime(),
                this.runnable,
                0,
                true,
                false);
        FabricTaskManager.fabricTickListener.addTask(fabricTask);
    }

    public void runTaskLater(FabricPlatform fabricMain, long delay) {
        fabricTask = new FabricTickListener.Task(
                FabricTaskManager.fabricTickListener.getTasksQueue().size(),
                FabricPlatform.SERVER.getNextTickTime()+delay,
                this.runnable,
                0,
                false,
                false);
        FabricTaskManager.fabricTickListener.addTask(fabricTask);
    }

    public void runTaskLaterAsynchronously(FabricPlatform fabricMain, long delay) {
        fabricTask = new FabricTickListener.Task(
                FabricTaskManager.fabricTickListener.getTasksQueue().size(),
                FabricPlatform.SERVER.getNextTickTime()+delay,
                this.runnable,
                0,
                true,
                false);
        FabricTaskManager.fabricTickListener.addTask(fabricTask);
    }

}
