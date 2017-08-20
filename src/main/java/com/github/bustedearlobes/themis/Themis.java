package com.github.bustedearlobes.themis;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.bustedearlobes.themis.commands.CommandListener;
import com.github.bustedearlobes.themis.commands.MuteCommand;
import com.github.bustedearlobes.themis.commands.UnmuteCommand;
import com.github.bustedearlobes.themis.taskmanager.TaskManager;
import com.github.bustedearlobes.themis.util.ThemisLogFormatter;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

public class Themis {
    private static Logger logger;
    private JDA jda;
    private TaskManager taskManager;
    private CommandListener commandListener;

    public Themis() {
        initLogger();
        initJDA();
        initCommandListener();
        initTaskManager();
    }

    public Themis(File apiKey) {
        initLogger();
        initJDA(apiKey);
        initCommandListener();
        initTaskManager();
    }

    /**
     * Initializes the logger and configures it to log to an output log file.
     */
    private void initLogger() {
        logger = Logger.getLogger("Themis");
        logger.setUseParentHandlers(false);

        try {
            // This block configure the logger with handler and formatter
            FileHandler fh = new FileHandler("themis.log", 10000000, 1, true);
            ConsoleHandler ch = new ConsoleHandler();
            logger.addHandler(fh);
            logger.addHandler(ch);
            ThemisLogFormatter formatter = new ThemisLogFormatter();
            fh.setFormatter(formatter);
            ch.setFormatter(formatter);
        } catch(SecurityException | IOException e) {
            System.err.println("CRITICAL: Failed to load log file for writting. Shutting down");
            System.exit(1);
        }
    }

    /**
     * Initializes JDA with default api-key file.
     */
    private void initJDA() {
        initJDA(new File("API_KEY.dat"));
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
            jda = new JDABuilder(AccountType.BOT).setToken(key).buildBlocking();
            logger.info("JDA session succesfully started.");
        } catch(IOException e) {
            logger.log(Level.SEVERE,
                    String.format("Could not find or open api key file %s. JDA could not be created", apiKey), e);
            System.exit(1);
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error building JDA.", e);
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
    }
    
    /**
     * Initializes the task manager
     */
    private void initTaskManager() {
        taskManager = new TaskManager(jda);
    }

    /**
     * Starts up Themis.
     */
    public void start() {
        new Thread(taskManager, "TaskManager").start();
        logger.info("Themis started succesfully!");
    }
    
    /**
     * Shutsdown themis
     */
    public void shutdown() {
        taskManager.shutdown();
        jda.shutdown();
        
    }

    /**
     * Gets the Themis task manager. Used to add tasks to the queue.
     * 
     * @return The themis task manager.
     */
    public TaskManager getTaskManager() {
        return taskManager;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Themis themis;
        if(args.length == 1) {
            themis = new Themis(new File(args[0]));
        } else {
            themis = new Themis();
        }
        themis.start();
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            themis.shutdown();
        }));
    }
}
