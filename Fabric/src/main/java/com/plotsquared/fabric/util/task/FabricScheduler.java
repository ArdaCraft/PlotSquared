package com.plotsquared.fabric.util.task;

import com.plotsquared.fabric.FabricPlatform;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface FabricScheduler {

    int scheduleSyncDelayedTask(@NotNull FabricPlatform var1, @NotNull Runnable var2, long var3);

    /**
     * @deprecated
     */
    @Deprecated
    int scheduleSyncDelayedTask(@NotNull FabricPlatform var1, @NotNull FabricRunnable var2, long var3);

    int scheduleSyncDelayedTask(@NotNull FabricPlatform var1, @NotNull Runnable var2);

    /**
     * @deprecated
     */
    @Deprecated
    int scheduleSyncDelayedTask(@NotNull FabricPlatform var1, @NotNull FabricRunnable var2);

    int scheduleSyncRepeatingTask(@NotNull FabricPlatform var1, @NotNull Runnable var2, long var3, long var5);

    /**
     * @deprecated
     */
    @Deprecated
    int scheduleSyncRepeatingTask(@NotNull FabricPlatform var1, @NotNull FabricRunnable var2, long var3, long var5);

    /**
     * @deprecated
     */
    @Deprecated
    int scheduleAsyncDelayedTask(@NotNull FabricPlatform var1, @NotNull Runnable var2, long var3);

    /**
     * @deprecated
     */
    @Deprecated
    int scheduleAsyncDelayedTask(@NotNull FabricPlatform var1, @NotNull Runnable var2);

    /**
     * @deprecated
     */
    @Deprecated
    int scheduleAsyncRepeatingTask(@NotNull FabricPlatform var1, @NotNull Runnable var2, long var3, long var5);

    <T> @NotNull Future<T> callSyncMethod(@NotNull FabricPlatform var1, @NotNull Callable<T> var2);

    void cancelTask(int var1);

    void cancelTasks(@NotNull FabricPlatform var1);

    boolean isCurrentlyRunning(int var1);

    boolean isQueued(int var1);

    @NotNull List<FabricWorker> getActiveWorkers();

    @NotNull List<FabricTask> getPendingTasks();

    @NotNull FabricTask runTask(@NotNull FabricPlatform var1, @NotNull Runnable var2) throws IllegalArgumentException;

    /**
     * @deprecated
     */
    @Deprecated
    @NotNull FabricTask runTask(@NotNull FabricPlatform var1, @NotNull FabricRunnable var2) throws IllegalArgumentException;

    @NotNull FabricTask runTaskAsynchronously(@NotNull FabricPlatform var1, @NotNull Runnable var2) throws
            IllegalArgumentException;

    /**
     * @deprecated
     */
    @Deprecated
    @NotNull FabricTask runTaskAsynchronously(@NotNull FabricPlatform var1, @NotNull FabricRunnable var2) throws
            IllegalArgumentException;

    @NotNull FabricTask runTaskLater(@NotNull FabricPlatform var1, @NotNull Runnable var2, long var3) throws
            IllegalArgumentException;

    /**
     * @deprecated
     */
    @Deprecated
    @NotNull FabricTask runTaskLater(@NotNull FabricPlatform var1, @NotNull FabricRunnable var2, long var3) throws
            IllegalArgumentException;

    @NotNull FabricTask runTaskLaterAsynchronously(@NotNull FabricPlatform var1, @NotNull Runnable var2, long var3) throws
            IllegalArgumentException;

    /**
     * @deprecated
     */
    @Deprecated
    @NotNull FabricTask runTaskLaterAsynchronously(@NotNull FabricPlatform var1, @NotNull FabricRunnable var2, long var3) throws
            IllegalArgumentException;

    @NotNull FabricTask runTaskTimer(@NotNull FabricPlatform var1, @NotNull Runnable var2, long var3, long var5) throws
            IllegalArgumentException;

    /**
     * @deprecated
     */
    @Deprecated
    @NotNull FabricTask runTaskTimer(@NotNull FabricPlatform var1, @NotNull FabricRunnable var2, long var3, long var5) throws
            IllegalArgumentException;

    @NotNull FabricTask runTaskTimerAsynchronously(
            @NotNull FabricPlatform var1,
            @NotNull Runnable var2,
            long var3,
            long var5
    ) throws IllegalArgumentException;

    /**
     * @deprecated
     */
    @Deprecated
    @NotNull FabricTask runTaskTimerAsynchronously(
            @NotNull FabricPlatform var1,
            @NotNull FabricRunnable var2,
            long var3,
            long var5
    ) throws IllegalArgumentException;


}
