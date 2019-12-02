package com.github.bustedearlobes.themis.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.bustedearlobes.themis.Themis;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public abstract class Command {
    private Pattern pattern;
    private String commandName;
    private boolean requiresOwner;
    
    public Command(String commandName, String regexValidation, boolean requiresOwner) {
        pattern = Pattern.compile(regexValidation);
        this.requiresOwner = requiresOwner;
        this.commandName = commandName;
    }
    
    protected boolean validateCall(String command) {
        return pattern.matcher(command).matches();
    }
    
    public Matcher parseCommand(String command) {
        Matcher m = pattern.matcher(command);
        m.find();
        return m;
    }
    
    public final String getCommandName() {
        return commandName;
    }
    
    public String getDiscription() {
        return "No discription for this command...";
    }
    public String getHumanReadablePattern() {
        return "";
    }
    
    public String getExampleUsage() {
        return "";
    }
    
    public final StringBuilder getFullCommandManual() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("__**").append(CommandListener.COMMAND_BASE).append(getCommandName()).append("**__\n")
            .append("**Description:**\n")
            .append(getDiscription()).append("\n")
            .append("**Pattern:**\n")
            .append(CommandListener.COMMAND_BASE).append(getCommandName()).append(" ")
                .append(getHumanReadablePattern()).append("\n")
            .append("**Example:**\n")
            .append(CommandListener.COMMAND_BASE).append(getCommandName()).append(" ")
                .append(getExampleUsage()).append("\n");
        return sb;
    }
    
    public final StringBuilder getCommandManual() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("__**").append(CommandListener.COMMAND_BASE).append(getCommandName()).append("**__\n")
            .append("**Description:**\n")
            .append(getDiscription()).append("\n");
        return sb;
    }
    
    public void printUsage(TextChannel textChannel) {
        textChannel.sendMessage("Usage:\n\n" + getFullCommandManual().toString()).complete();
    }
    
    public boolean requiresOwner() {
        return requiresOwner;
    }
    
    protected abstract void onCall(Matcher fullCommand, Message message, JDA jda, Themis themis);

}
