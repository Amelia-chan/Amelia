package pw.mihou.amelia.io;

import org.javacord.core.util.concurrent.ThreadFactory;
import java.util.concurrent.*;

public class Scheduler {

    private static final ScheduledExecutorService executor =
            Executors.newScheduledThreadPool(1, new ThreadFactory("Amelia - Central Scheduler - %d", false));

    /**
     * Shutdowns the scheduled executor service.
     * Already called when exiting.
     */
    public static void shutdown(){
        executor.shutdown();
    }

    /**
     * Returns the ScheduledExecutorService.
     * @return ScheduledExecutorService.
     */
    public ScheduledExecutorService getScheduler(){
        return executor;
    }

    /**
     * Schedules a task at a fixed rate.
     * @param task the task to schedule.
     * @param delay the delay before the first run.
     * @param time the repeat time.
     * @param measurement the time measurement.
     * @return ScheduledFuture<?>
     */
    public static ScheduledFuture<?> schedule(Runnable task, long delay, long time, TimeUnit measurement){
        return executor.scheduleAtFixedRate(task, delay, time, measurement);
    }

    /**
     * Runs a single task on a delay.
     * @param task the task to run.
     * @param delay the delay before the first execution.
     * @param measurement the time measurement.
     * @return ScheduledFuture<?>
     */
    public static ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit measurement){
        return executor.schedule(task, delay, measurement);
    }

    /**
     * Submits a task to run asynchronous.
     * @param task the task to run.
     * @return CompletableFuture<Void>
     */
    public static CompletableFuture<Void> submitTask(Runnable task){
        return CompletableFuture.runAsync(task, executor);
    }

}
