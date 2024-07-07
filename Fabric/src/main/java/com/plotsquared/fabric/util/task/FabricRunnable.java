package com.plotsquared.fabric.util.task;

import com.plotsquared.fabric.FabricPlatform;
import org.jetbrains.annotations.NotNull;

public abstract class FabricRunnable implements Runnable {

    private FabricTask task;

    public FabricRunnable() {
    }

    public synchronized boolean isCancelled() throws IllegalStateException {
        this.checkScheduled();
        return this.task.isCancelled();
    }

    public synchronized void cancel() throws IllegalStateException {
        FabricPlatform.getScheduler().cancelTask(this.getTaskId());
    }

    public synchronized @NotNull FabricTask runTask(FabricPlatform fabricPlatform) throws IllegalArgumentException,
            IllegalStateException {
        this.checkNotYetScheduled();
        return this.setupTask(FabricPlatform.getScheduler().runTask(fabricPlatform, this));
    }

    public synchronized @NotNull FabricTask runTaskAsynchronously(FabricPlatform fabricPlatform) throws IllegalArgumentException,
            IllegalStateException {
        this.checkNotYetScheduled();
        return this.setupTask(FabricPlatform.getScheduler().runTaskAsynchronously(fabricPlatform, this));
    }

    public synchronized @NotNull FabricTask runTaskLater(FabricPlatform fabricPlatform, long delay) throws IllegalArgumentException,
            IllegalStateException {
        this.checkNotYetScheduled();
        return this.setupTask(FabricPlatform.getScheduler().runTaskLater(fabricPlatform, this, delay));
    }

    public synchronized @NotNull FabricTask runTaskLaterAsynchronously(FabricPlatform fabricPlatform, long delay) throws
            IllegalArgumentException, IllegalStateException {
        this.checkNotYetScheduled();
        return this.setupTask(FabricPlatform.getScheduler().runTaskLaterAsynchronously(fabricPlatform, this, delay));
    }

    public synchronized @NotNull FabricTask runTaskTimer(FabricPlatform fabricPlatform, long delay, long period) throws
            IllegalArgumentException, IllegalStateException {
        this.checkNotYetScheduled();
        return this.setupTask(FabricPlatform.getScheduler().runTaskTimer(fabricPlatform, this, delay, period));
    }

    public synchronized @NotNull FabricTask runTaskTimerAsynchronously(FabricPlatform fabricPlatform, long delay, long period) throws
            IllegalArgumentException, IllegalStateException {
        this.checkNotYetScheduled();
        return this.setupTask(FabricPlatform.getScheduler().runTaskTimerAsynchronously(fabricPlatform, this, delay, period));
    }

    public synchronized int getTaskId() throws IllegalStateException {
        this.checkScheduled();
        return this.task.getTaskId();
    }

    private void checkScheduled() {
        if (this.task == null) {
            throw new IllegalStateException("Not scheduled yet");
        }
    }

    private void checkNotYetScheduled() {
        if (this.task != null) {
            throw new IllegalStateException("Already scheduled as " + this.task.getTaskId());
        }
    }

    private @NotNull FabricTask setupTask(@NotNull FabricTask task) {
        this.task = task;
        return task;
    }

}
