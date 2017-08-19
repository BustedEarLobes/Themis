package com.github.bustedearlobes.themis.taskmanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskManager implements Runnable {
    private static final Logger LOG = Logger.getLogger("Themis");
    private static final File STATE_FILE = new File("themis_state.dat");
    
    private List<ScheduledTask> scheduledTasks;
    private AtomicBoolean isRunning;
    private ExecutorService executor = Executors.newCachedThreadPool();
    
    public TaskManager() {
        scheduledTasks = new LinkedList<>();
        loadOldState();
    }
    
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
        saveState();
    }
    
    private void loadOldState() {
        try(FileInputStream fis = new FileInputStream(STATE_FILE)) {
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Object obj;
            int count = 0;
            while((obj = ois.readObject()) != null) {
                addTaskToScheduler((ScheduledTask)obj);
                count ++;
            }
            LOG.log(Level.INFO, "Task manager state loaded " + count + " tasks from " + STATE_FILE.getCanonicalPath());
        } catch(IOException | ClassNotFoundException e) {
            LOG.log(Level.SEVERE, "Could not load task manager state.", e);
        }
    }
    
    private void saveState() {
        try(FileOutputStream fos = new FileOutputStream(STATE_FILE)) {
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            for(ScheduledTask task : scheduledTasks) {
                oos.writeObject(task);
            }
            LOG.log(Level.INFO, "Task manager state saved to " + STATE_FILE.getCanonicalPath());
        } catch(IOException e) {
            LOG.log(Level.SEVERE, "Could not save task manager state. Scheduled tasks not saved on shutdown!", e);
        }
    }
}
