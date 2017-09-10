package com.github.bustedearlobes.themis.taskmanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.bustedearlobes.themis.Themis;

public class TaskManager implements Runnable {
    private static final Logger LOG = Logger.getLogger("Themis");
    private static final File STATE_FILE = new File("themis_state.dat");
    
    private ExecutorService executor = Executors.newCachedThreadPool();
    private List<ScheduledTask> scheduledTasks;
    private AtomicBoolean isRunning;
    private Themis themis;
    
    public TaskManager(Themis themis) {
        scheduledTasks = new LinkedList<>();
        this.themis = themis;
        cleanupSavedTasks();
        STATE_FILE.delete();
    }
    
    public void addTask(ScheduledTask task) {
        task.setThemis(themis);
        synchronized(scheduledTasks) {
            scheduledTasks.add(task);
        }
        saveTasks();
    }
    
    public void addTaskBlocked(ScheduledTask task) throws InterruptedException {
        addTask(task);
        while(task.getState() != TaskState.DEAD) {
            Thread.sleep(100);
        }
    }
    
    @Override
    public void run() {
        isRunning = new AtomicBoolean(true);
        while(isRunning.get()) {
            ScheduledTask task;
            boolean taskClosed = false;
            synchronized(scheduledTasks) {
                for(int i = 0; i < scheduledTasks.size(); i++) {
                    task = scheduledTasks.get(i);
                    switch(task.getState()) {
                        case QUEUED:
                            if(task.taskIsReady()) {
                                executor.execute(task);
                                task.incrementRun();
                            }
                            break;
                        case CLEANUP:
                            task.cleanUpTask();
                            break;
                        case DEAD:
                            taskClosed = true;
                            scheduledTasks.remove(i);
                            i --;
                            break;
                        default:
                            break;
                    }
                }
            }
            if(taskClosed) {
                saveTasks();
            }
            
            try {
                TimeUnit.MICROSECONDS.sleep(5);
            } catch(InterruptedException e) {
                LOG.log(Level.WARNING, "Interrupted main taskmanager thread.", e);
            }
        }
    }

    public void shutdown() {
        isRunning.set(false);
        executor.shutdown();
        LOG.info("Shutting down task manager. Awaiting 5 seconds max for safe task exit...");
        try {
            if(!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                LOG.warning("Could not shut down task manager safely. Timeout exceded");
                executor.shutdownNow();
            }
        } catch(InterruptedException e) {
            LOG.log(Level.WARNING, "Could not shut down task manager safely. Interrupted Exception.", e);
        }
        LOG.log(Level.INFO, "Saving task manager state.");
        saveTasks();
    }
    
    public List<ScheduledTask> getTasks() {
        synchronized(scheduledTasks) {
            return Collections.unmodifiableList(scheduledTasks);
        }
    }
    
    public List<ScheduledTask> getTasksByName(String name) {
        List<ScheduledTask> resultList = new ArrayList<ScheduledTask>();
        synchronized(scheduledTasks) {
            for(ScheduledTask task : scheduledTasks) {
                if(task.getName().equals(name)) {
                    resultList.add(task);
                }
            }
        }
        return resultList;
    }

    private void cleanupSavedTasks() {
        if(STATE_FILE.exists()) {
            try(FileInputStream fis = new FileInputStream(STATE_FILE);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ObjectInputStream ois = new ObjectInputStream(bis)) {
                int numberOfTasks = ois.readInt();
                for(int count = 0; count < numberOfTasks; count ++) {
                    ScheduledTask task = (ScheduledTask)ois.readObject();
                    if(!task.isState(TaskState.DEAD)) {
                        task.setThemis(themis);
                        if(task.cleanUpTask()) {
                            count ++;
                        }
                    }
                }
                LOG.log(Level.INFO, "Task manager cleaned up " + numberOfTasks + " tasks from last shutdown");            
            } catch(IOException | ClassNotFoundException e) {
                LOG.log(Level.SEVERE, "Could not load older tasks to clean up.", e);
            }
        } else {
            LOG.log(Level.INFO, "Task manager had no tasks to clean up. State file does not exist.");
        }
    }
    
    private void saveTasks() {
        try(FileOutputStream fos = new FileOutputStream(STATE_FILE);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                synchronized(scheduledTasks) {
                    oos.writeInt(scheduledTasks.size());
                    for(ScheduledTask task : scheduledTasks) {
                        oos.writeObject(task);
                        oos.writeLong(task.getTimeUntilNextRun());
                    }
                }
        } catch(IOException e) {
            LOG.log(Level.SEVERE, "Could not save task manager tasks. Scheduled tasks will not "
                    + "be cleaned up on unexpected shutdown!", e);
        }
    }
}
