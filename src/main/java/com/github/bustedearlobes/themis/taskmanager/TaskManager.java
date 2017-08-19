package com.github.bustedearlobes.themis.taskmanager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskManager implements Runnable {
    private static final Logger LOG = Logger.getLogger("Themis");

    private List<ScheduledTask> scheduledTasks;
    private AtomicBoolean isRunning;
    private ExecutorService executor = Executors.newCachedThreadPool();
    
    public void addTaskToScheduler(ScheduledTask task) {
        synchronized(scheduledTasks) {
            scheduledTasks.add(task);
        }
    }

    @Override
    public void run() {
        isRunning = new AtomicBoolean(true);
        while(isRunning.get()) {
            ScheduledTask task;
            synchronized(scheduledTasks) {
                for(int i = 0; i < scheduledTasks.size(); i++) {
                    task = scheduledTasks.get(i);
                    if(task.taskIsReady()) {
                        executor.execute(task);
                    }
                    if(task.isExpired()) {
                        scheduledTasks.remove(i);
                        i--;
                    }
                }
            }
        }
    }

    public void shutdown() {
        isRunning.set(false);
        executor.shutdown();
        LOG.info("Shutting down task manager. Awaiting 30 seconds max for safe task exit...");
        try {
            if(!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                LOG.warning("Could not shut down task manager safely. Timeout exceded");
                executor.shutdownNow();
            }
        } catch(InterruptedException e) {
            LOG.log(Level.WARNING, "Could not shut down task manager safely. Interrupted Exception.", e);
        }
    }
}
