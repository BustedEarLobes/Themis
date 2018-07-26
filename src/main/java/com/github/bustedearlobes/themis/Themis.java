package com.github.bustedearlobes.themis;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bustedearlobes.themis.commands.ClearCommand;
import com.github.bustedearlobes.themis.commands.CommandListener;
import com.github.bustedearlobes.themis.commands.HelpCommand;
import com.github.bustedearlobes.themis.commands.MusicCommand;
import com.github.bustedearlobes.themis.commands.MuteCommand;
import com.github.bustedearlobes.themis.commands.ShutdownCommand;
import com.github.bustedearlobes.themis.commands.UnmuteCommand;
import com.github.bustedearlobes.themis.music.GlobalMusicManager;
import com.github.bustedearlobes.themis.taskmanager.TaskManager;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

public class Themis {
    private final static Logger LOG = LoggerFactory.getLogger(Themis.class);
    private JDA jda;
    private TaskManager taskManager;
    private CommandListener commandListener;
    private GlobalMusicManager musicManager;
    private String themisOwner;
    
    private void init(File apiKey) {
        initJDA(apiKey);
        initCommandListener();
        initTaskManager();
        initMusicManager();
    }

    /**
     * Initializes JDA with given api-key file
     * 
     * @param apiKey
     *            The API key file location to read and initialize JDA with.
     */
    private void initJDA(File apiKey) {
        try(FileInputStream fis = new FileInputStream(apiKey);
                BufferedInputStream bis = new BufferedInputStream(fis);
                Scanner scanner = new Scanner(bis);) {
            String key = scanner.nextLine().trim();
            themisOwner = scanner.nextLine().trim();
            jda = new JDABuilder(AccountType.BOT).setToken(key).buildBlocking();
            LOG.info("JDA session successfully started.");
        } catch(IOException e) {
            LOG.error("Could not find or open api key file {}. JDA could not be created", apiKey, e);
            System.exit(1);
        } catch(Exception e) {
            LOG.error("Error building JDA.", e);
            System.exit(1);
        }
    }
    
    /**
     * Initializes the command listener and sets JDA hooks.
     */
    private void initCommandListener() {
        commandListener = new CommandListener(this);
        jda.addEventListener(commandListener);
        
        commandListener.register(new MuteCommand());
        commandListener.register(new UnmuteCommand());
        commandListener.register(new ShutdownCommand());
        commandListener.register(new HelpCommand());
        commandListener.register(new ClearCommand());
        commandListener.register(new MusicCommand());
    }
    
    /**
     * Initializes the task manager
     */
    private void initTaskManager() {
        taskManager = new TaskManager(this);
    }
    
    /**
     * Initializes the global music manager
     */
    private void initMusicManager() {
        musicManager = new GlobalMusicManager();
    }

    public void start() {
        start(new File("API_KEY.dat"));
    }
    
    /**
     * Starts up Themis.
     */
    public void start(File apiKey) {
        init(apiKey);
        new Thread(taskManager, "TaskManager").start();
        LOG.info("Themis started successfully");
    }
    
    /**
     * Shutsdown themis
     */
    public void shutdown() {
        LOG.info("Shutting down Themis");
        taskManager.shutdown();
        jda.shutdown();
    }
    
    public String getThemisOwner() {
        return themisOwner;
    }

    /**
     * Gets the Themis task manager. Used to add tasks to the queue.
     * 
     * @return The Themis task manager.
     */
    public TaskManager getTaskManager() {
        return taskManager;
    }
    
    public GlobalMusicManager getGlobalMusicManager() {
        return musicManager;
    }

    /**
     * Gets the JDA instance.
     * 
     * @return The JDA instance for themis.
     */
    public JDA getJDA() {
        return jda;
    }
    
    /**
     * Gets the command listener object.
     * @return The Themis command listener
     */
    public CommandListener getCommandListener() {
        return commandListener;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            Themis themis = new Themis();
            if(args.length == 1) {
                themis.start(new File(args[0]));
            } else {
                themis.start();
            }
            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                try {
                    themis.shutdown();
                } catch(Throwable e) {
                    LOG.error("Could not shutdown themis safely.", e);
                }
            }));
        } catch(Throwable e) {
            LOG.error("Fatal error. Themis must shut down.", e);
        }

    }
}
