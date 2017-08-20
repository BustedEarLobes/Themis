package com.github.bustedearlobes.themis.taskmanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import net.dv8tion.jda.core.JDA;

public class TaskManager implements Runnable {
    private static final Logger LOG = Logger.getLogger("Themis");
    private static final File STATE_FILE = new File("themis_state.dat");
    
    private List<ScheduledTask> scheduledTasks;
    private AtomicBoolean isRunning;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private JDA jda;
    
    public TaskManager(JDA jda) {
        scheduledTasks = new LinkedList<>();
        this.jda = jda;
        loadOldState();
        STATE_FILE.delete();
    }
    
    public void addTaskToScheduler(ScheduledTask task) {
        task.setJDA(jda);
        synchronized(scheduledTasks) {
            scheduledTasks.add(task);
        }
        saveState();
    }
    
    @Override
    public void run() {
        isRunning = new AtomicBoolean(true);
        while(isRunning.get()) {
            ScheduledTask task;
            boolean stateChanged = false;
            synchronized(scheduledTasks) {
                for(int i = 0; i < scheduledTasks.size(); i++) {
                    task = scheduledTasks.get(i);
                    if(task.taskIsReady()) {
                        executor.execute(task);
                        task.incrementRun();
                    }
                    if(task.isExpired()) {
                        stateChanged = true; 
                        scheduledTasks.remove(i);
                        i--;
                    }
                }
            }
            if(stateChanged) {
                saveState();
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
        LOG.info("Shutting down task manager. Awaiting 30 seconds max for safe task exit...");
        try {
            if(!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                LOG.warning("Could not shut down task manager safely. Timeout exceded");
                executor.shutdownNow();
            }
        } catch(InterruptedException e) {
            LOG.log(Level.WARNING, "Could not shut down task manager safely. Interrupted Exception.", e);
        }
        LOG.log(Level.INFO, "Saving task manager state.");
        saveState();
    }

    private void loadOldState() {
        try(FileInputStream fis = new FileInputStream(STATE_FILE);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ObjectInputStream ois = new ObjectInputStream(bis)) {
            int numberOfTasks = ois.readInt();
            for(int count = 0; count < numberOfTasks; count ++) {
                addTaskToScheduler((ScheduledTask)ois.readObject());
                count ++;
            }
            LOG.log(Level.INFO, "Task manager state loaded " + numberOfTasks + " tasks from " + STATE_FILE.getCanonicalPath());
        } catch(FileNotFoundException e) {
            LOG.log(Level.INFO, "No state to load. State file does not exist.");
        } catch(IOException | ClassNotFoundException e) {
            LOG.log(Level.SEVERE, "Could not load task manager state.", e);
        }
    }
    
    private void saveState() {
        try(FileOutputStream fos = new FileOutputStream(STATE_FILE);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                synchronized(scheduledTasks) {
                    oos.writeInt(scheduledTasks.size());
                    for(ScheduledTask task : scheduledTasks) {
                        oos.writeObject(task);
                    }
                }
        } catch(IOException e) {
            LOG.log(Level.SEVERE, "Could not save task manager state. Scheduled tasks not saved on shutdown!", e);
        }
    }
}
