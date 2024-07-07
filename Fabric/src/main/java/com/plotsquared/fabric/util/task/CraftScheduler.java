package com.plotsquared.fabric.util.task;

import com.plotsquared.fabric.FabricPlatform;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CraftScheduler implements FabricScheduler {

    /**
     * Counter for IDs. Order doesn't matter, only uniqueness.
     */
    private final AtomicInteger ids = new AtomicInteger(1);
    /**
     * Current head of linked-list. This reference is always stale, {@link CraftTask#next} is the live reference.
     */
    private volatile CraftTask head = new CraftTask();
    /**
     * Tail of a linked-list. AtomicReference only matters when adding to queue
     */
    private final AtomicReference<CraftTask> tail = new AtomicReference<CraftTask>(head);
    /**
     * Main thread logic only
     */
    private final PriorityQueue<CraftTask> pending = new PriorityQueue<CraftTask>(10,
            new Comparator<CraftTask>() {
                public int compare(final CraftTask o1, final CraftTask o2) {
                    return (int) (o1.getNextRun() - o2.getNextRun());
                }
            });
    /**
     * Main thread logic only
     */
    private final List<CraftTask> temp = new ArrayList<CraftTask>();
    /**
     * These are tasks that are currently active. It's provided for 'viewing' the current state.
     */
    private final ConcurrentHashMap<Integer, CraftTask> runners = new ConcurrentHashMap<Integer, CraftTask>();
    private volatile int currentTick = -1;
    private final Executor executor = Executors.newCachedThreadPool();
    private static final int RECENT_TICKS;

    static {
        RECENT_TICKS = 30;
    }

    public int scheduleSyncDelayedTask(final FabricPlatform plugin, final Runnable task) {
        return this.scheduleSyncDelayedTask(plugin, task, 0l);
    }

    public FabricTask runTask(FabricPlatform plugin, Runnable runnable) {
        return runTaskLater(plugin, runnable, 0l);
    }

    @Deprecated
    public int scheduleAsyncDelayedTask(final FabricPlatform plugin, final Runnable task) {
        return this.scheduleAsyncDelayedTask(plugin, task, 0l);
    }

    public FabricTask runTaskAsynchronously(FabricPlatform plugin, Runnable runnable) {
        return runTaskLaterAsynchronously(plugin, runnable, 0l);
    }

    public int scheduleSyncDelayedTask(final FabricPlatform plugin, final Runnable task, final long delay) {
        return this.scheduleSyncRepeatingTask(plugin, task, delay, -1l);
    }

    public FabricTask runTaskLater(FabricPlatform plugin, Runnable runnable, long delay) {
        return runTaskTimer(plugin, runnable, delay, -1l);
    }

    @Deprecated
    public int scheduleAsyncDelayedTask(final FabricPlatform plugin, final Runnable task, final long delay) {
        return this.scheduleAsyncRepeatingTask(plugin, task, delay, -1l);
    }

    public FabricTask runTaskLaterAsynchronously(FabricPlatform plugin, Runnable runnable, long delay) {
        return runTaskTimerAsynchronously(plugin, runnable, delay, -1l);
    }

    public int scheduleSyncRepeatingTask(final FabricPlatform plugin, final Runnable runnable, long delay, long period) {
        return runTaskTimer(plugin, runnable, delay, period).getTaskId();
    }

    public FabricTask runTaskTimer(FabricPlatform plugin, Runnable runnable, long delay, long period) {
        validate(plugin, runnable);
        if (delay < 0l) {
            delay = 0;
        }
        if (period == 0l) {
            period = 1l;
        } else if (period < -1l) {
            period = -1l;
        }
        return handle(new CraftTask(plugin, runnable, nextId(), period), delay);
    }

    @Deprecated
    public int scheduleAsyncRepeatingTask(final FabricPlatform plugin, final Runnable runnable, long delay, long period) {
        return runTaskTimerAsynchronously(plugin, runnable, delay, period).getTaskId();
    }

    public FabricTask runTaskTimerAsynchronously(FabricPlatform plugin, Runnable runnable, long delay, long period) {
        validate(plugin, runnable);
        if (delay < 0l) {
            delay = 0;
        }
        if (period == 0l) {
            period = 1l;
        } else if (period < -1l) {
            period = -1l;
        }
        return handle(new CraftAsyncTask(runners, plugin, runnable, nextId(), period), delay);
    }

    public <T> Future<T> callSyncMethod(final FabricPlatform plugin, final Callable<T> task) {
        validate(plugin, task);
        final CraftFuture<T> future = new CraftFuture<T>(task, plugin, nextId());
        handle(future, 0l);
        return future;
    }

    public void cancelTask(final int taskId) {
        if (taskId <= 0) {
            return;
        }
        CraftTask task = runners.get(taskId);
        if (task != null) {
            task.cancel0();
        }
        task = new CraftTask(
                new Runnable() {
                    public void run() {
                        if (!check(CraftScheduler.this.temp)) {
                            check(CraftScheduler.this.pending);
                        }
                    }
                    private boolean check(final Iterable<CraftTask> collection) {
                        final Iterator<CraftTask> tasks = collection.iterator();
                        while (tasks.hasNext()) {
                            final CraftTask task = tasks.next();
                            if (task.getTaskId() == taskId) {
                                task.cancel0();
                                tasks.remove();
                                if (task.isSync()) {
                                    runners.remove(taskId);
                                }
                                return true;
                            }
                        }
                        return false;
                    }});
        handle(task, 0l);
        for (CraftTask taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext()) {
            if (taskPending == task) {
                return;
            }
            if (taskPending.getTaskId() == taskId) {
                taskPending.cancel0();
            }
        }
    }

    public void cancelTasks(final FabricPlatform plugin) {
        Validate.notNull(plugin, "Cannot cancel tasks of null plugin");
        final CraftTask task = new CraftTask(
                new Runnable() {
                    public void run() {
                        check(CraftScheduler.this.pending);
                        check(CraftScheduler.this.temp);
                    }
                    void check(final Iterable<CraftTask> collection) {
                        final Iterator<CraftTask> tasks = collection.iterator();
                        while (tasks.hasNext()) {
                            final CraftTask task = tasks.next();
                            if (task.getOwner().equals(plugin)) {
                                task.cancel0();
                                tasks.remove();
                                if (task.isSync()) {
                                    runners.remove(task.getTaskId());
                                }
                            }
                        }
                    }
                });
        handle(task, 0l);
        for (CraftTask taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext()) {
            if (taskPending == task) {
                return;
            }
            if (taskPending.getTaskId() != -1 && taskPending.getOwner().equals(plugin)) {
                taskPending.cancel0();
            }
        }
        for (CraftTask runner : runners.values()) {
            if (runner.getOwner().equals(plugin)) {
                runner.cancel0();
            }
        }
    }

    public void cancelAllTasks() {
        final CraftTask task = new CraftTask(
                new Runnable() {
                    public void run() {
                        Iterator<CraftTask> it = CraftScheduler.this.runners.values().iterator();
                        while (it.hasNext()) {
                            CraftTask task = it.next();
                            task.cancel0();
                            if (task.isSync()) {
                                it.remove();
                            }
                        }
                        CraftScheduler.this.pending.clear();
                        CraftScheduler.this.temp.clear();
                    }
                });
        handle(task, 0l);
        for (CraftTask taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext()) {
            if (taskPending == task) {
                break;
            }
            taskPending.cancel0();
        }
        for (CraftTask runner : runners.values()) {
            runner.cancel0();
        }
    }

    public boolean isCurrentlyRunning(final int taskId) {
        final CraftTask task = runners.get(taskId);
        if (task == null || task.isSync()) {
            return false;
        }
        final CraftAsyncTask asyncTask = (CraftAsyncTask) task;
        synchronized (asyncTask.getWorkers()) {
            return asyncTask.getWorkers().isEmpty();
        }
    }

    public boolean isQueued(final int taskId) {
        if (taskId <= 0) {
            return false;
        }
        for (CraftTask task = head.getNext(); task != null; task = task.getNext()) {
            if (task.getTaskId() == taskId) {
                return task.getPeriod() >= -1l; // The task will run
            }
        }
        CraftTask task = runners.get(taskId);
        return task != null && task.getPeriod() >= -1l;
    }

    public List<FabricWorker> getActiveWorkers() {
        final ArrayList<FabricWorker> workers = new ArrayList<FabricWorker>();
        for (final CraftTask taskObj : runners.values()) {
            // Iterator will be a best-effort (may fail to grab very new values) if called from an async thread
            if (taskObj.isSync()) {
                continue;
            }
            final CraftAsyncTask task = (CraftAsyncTask) taskObj;
            synchronized (task.getWorkers()) {
                // This will never have an issue with stale threads; it's state-safe
                workers.addAll(task.getWorkers());
            }
        }
        return workers;
    }

    public List<FabricTask> getPendingTasks() {
        final ArrayList<CraftTask> truePending = new ArrayList<CraftTask>();
        for (CraftTask task = head.getNext(); task != null; task = task.getNext()) {
            if (task.getTaskId() != -1) {
                // -1 is special code
                truePending.add(task);
            }
        }

        final ArrayList<FabricTask> pending = new ArrayList<FabricTask>();
        for (CraftTask task : runners.values()) {
            if (task.getPeriod() >= -1l) {
                pending.add(task);
            }
        }

        for (final CraftTask task : truePending) {
            if (task.getPeriod() >= -1l && !pending.contains(task)) {
                pending.add(task);
            }
        }
        return pending;
    }

    /**
     * This method is designed to never block or wait for locks; an immediate execution of all current tasks.
     */
    public void mainThreadHeartbeat(final int currentTick) {
        this.currentTick = currentTick;
        final List<CraftTask> temp = this.temp;
        parsePending();
        while (isReady(currentTick)) {
            final CraftTask task = pending.remove();
            if (task.getPeriod() < -1l) {
                if (task.isSync()) {
                    runners.remove(task.getTaskId(), task);
                }
                parsePending();
                continue;
            }
            if (task.isSync()) {
                try {
                    task.run();
                } catch (final Throwable throwable) {
                    task.getOwner().getLogger().log(
                            Level.ERROR,
                            String.format(
                                    "Task #%s generated an exception",
                                    task.getTaskId()));
                }
                parsePending();
            } else {
                executor.execute(task);
                // We don't need to parse pending
                // (async tasks must live with race-conditions if they attempt to cancel between these few lines of code)
            }
            final long period = task.getPeriod(); // State consistency
            if (period > 0) {
                task.setNextRun(currentTick + period);
                temp.add(task);
            } else if (task.isSync()) {
                runners.remove(task.getTaskId());
            }
        }
        pending.addAll(temp);
        temp.clear();
    }

    private void addTask(final CraftTask task) {
        final AtomicReference<CraftTask> tail = this.tail;
        CraftTask tailTask = tail.get();
        while (!tail.compareAndSet(tailTask, task)) {
            tailTask = tail.get();
        }
        tailTask.setNext(task);
    }

    private CraftTask handle(final CraftTask task, final long delay) {
        task.setNextRun(currentTick + delay);
        addTask(task);
        return task;
    }

    private static void validate(final FabricPlatform plugin, final Object task) {
        Validate.notNull(plugin, "FabricPlatform cannot be null");
        Validate.notNull(task, "Task cannot be null");
    }

    private int nextId() {
        return ids.incrementAndGet();
    }

    private void parsePending() {
        CraftTask head = this.head;
        CraftTask task = head.getNext();
        CraftTask lastTask = head;
        for (; task != null; task = (lastTask = task).getNext()) {
            if (task.getTaskId() == -1) {
                task.run();
            } else if (task.getPeriod() >= -1l) {
                pending.add(task);
                runners.put(task.getTaskId(), task);
            }
        }
        // We split this because of the way things are ordered for all of the async calls in CraftScheduler
        // (it prevents race-conditions)
        for (task = head; task != lastTask; task = head) {
            head = task.getNext();
            task.setNext(null);
        }
        this.head = lastTask;
    }

    private boolean isReady(final int currentTick) {
        return !pending.isEmpty() && pending.peek().getNextRun() <= currentTick;
    }

    @Override
    public String toString() {
        int debugTick = currentTick;
        StringBuilder string = new StringBuilder("Recent tasks from ").append(debugTick - RECENT_TICKS).append('-').append(debugTick).append('{');
        return string.append('}').toString();
    }

    @Deprecated
    @Override
    public int scheduleSyncDelayedTask(FabricPlatform plugin, FabricRunnable task, long delay) {
        return scheduleSyncDelayedTask(plugin, (Runnable) task, delay);
    }

    @Deprecated
    @Override
    public int scheduleSyncDelayedTask(FabricPlatform plugin, FabricRunnable task) {
        return scheduleSyncDelayedTask(plugin, (Runnable) task);
    }

    @Deprecated
    @Override
    public int scheduleSyncRepeatingTask(FabricPlatform plugin, FabricRunnable task, long delay, long period) {
        return scheduleSyncRepeatingTask(plugin, (Runnable) task, delay, period);
    }

    @Deprecated
    @Override
    public FabricTask runTask(FabricPlatform plugin, FabricRunnable task) throws IllegalArgumentException {
        return runTask(plugin, (Runnable) task);
    }

    @Deprecated
    @Override
    public FabricTask runTaskAsynchronously(FabricPlatform plugin, FabricRunnable task) throws IllegalArgumentException {
        return runTaskAsynchronously(plugin, (Runnable) task);
    }

    @Deprecated
    @Override
    public FabricTask runTaskLater(FabricPlatform plugin, FabricRunnable task, long delay) throws IllegalArgumentException {
        return runTaskLater(plugin, (Runnable) task, delay);
    }

    @Deprecated
    @Override
    public FabricTask runTaskLaterAsynchronously(FabricPlatform plugin, FabricRunnable task, long delay) throws IllegalArgumentException {
        return runTaskLaterAsynchronously(plugin, (Runnable) task, delay);
    }

    @Deprecated
    @Override
    public FabricTask runTaskTimer(FabricPlatform plugin, FabricRunnable task, long delay, long period) throws IllegalArgumentException {
        return runTaskTimer(plugin, (Runnable) task, delay, period);
    }

    @Deprecated
    @Override
    public FabricTask runTaskTimerAsynchronously(FabricPlatform plugin, FabricRunnable task, long delay, long period) throws IllegalArgumentException {
        return runTaskTimerAsynchronously(plugin, (Runnable) task, delay, period);
    }
}
