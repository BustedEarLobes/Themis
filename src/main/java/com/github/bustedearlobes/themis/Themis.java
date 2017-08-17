package com.github.bustedearlobes.themis;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import com.github.bustedearlobes.themis.util.ThemisLogFormatter;

public class Themis {
	private Logger logger;
	
	public Themis() {
		initLogger();
	}
	
	/**
	 * Initializes the logger and configures it to log to an output log file.
	 */
	public void initLogger() {
		logger = Logger.getLogger("Themis");  
        logger.setUseParentHandlers(false);
	    FileHandler fh;
	    ConsoleHandler ch;
	    
	    try {  
	        // This block configure the logger with handler and formatter  
	        fh = new FileHandler("themis.log", 10000000, 1, true);
	        ch = new ConsoleHandler();
	        logger.addHandler(fh);
	        logger.addHandler(ch);
	        ThemisLogFormatter formatter = new ThemisLogFormatter();  
	        fh.setFormatter(formatter);
	        ch.setFormatter(formatter);
	    } catch (SecurityException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }  
	}
	
	public void start() {
		
	}
	
	public static void main(String[] args) {
		Themis themis = new Themis();
		themis.start();
    }
}
