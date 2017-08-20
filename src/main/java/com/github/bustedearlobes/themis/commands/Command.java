package com.github.bustedearlobes.themis.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.bustedearlobes.themis.Themis;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;

public abstract class Command {
    private Pattern pattern;
    
    public Command(String regexValidation) {
        pattern = Pattern.compile(regexValidation);
    }
    
    protected boolean validateCall(String command) {
        return pattern.matcher(command).matches();
    }
    
    public Matcher parseCommand(String command) {
        Matcher m = pattern.matcher(command);
        m.find();
        return m;
    }
    
    public abstract void onCall(Matcher fullCommand, Message message, JDA jda, Themis themis);
}
