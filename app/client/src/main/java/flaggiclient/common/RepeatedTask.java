/*
 * Author: Matěj Šťastný
 * Date created: 1/5/2025
 * Github link: https://github.com/kireiiiiiiii/Flaggi
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package flaggiclient.common;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * An object that will repeated a task at a fixed rate.
 *
 */
public class RepeatedTask {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Schedules a new task to run.
     *
     * @param task
     * @param period
     * @param timeUnit
     */
    public void scheduleTask(Runnable task, long period, TimeUnit timeUnit) {
        scheduler.scheduleAtFixedRate(() -> {
            new Thread(task).start();
        }, 0, period, timeUnit);
    }

    /**
     * Turns of the scheduler
     *
     */
    public void shutdown() {
        scheduler.shutdown();
    }

}
