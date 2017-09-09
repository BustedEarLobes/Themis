package com.github.bustedearlobes.themis.commands;

import java.util.regex.Matcher;

import com.github.bustedearlobes.themis.Themis;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;

public class HelpCommand extends Command {
    private static final String REGEX = "^help( (\\w+))?$";
    
    public HelpCommand() {
        super("help", REGEX);
    }

    @Override
    public void onCall(Matcher fullCommand, Message message, JDA jda, Themis themis) {
        if(message.getMember().isOwner()) {
            if(fullCommand.group(1) != null) {
                for(Command command : themis.getCommandListener().getCommands()) {
                    if(command.getCommandName().equals(fullCommand.group(2))) {
                        command.printUsage(message.getTextChannel());
                    }
                }
            } else {
                StringBuilder sb = new StringBuilder(4000);
                sb.append("Commands:\n\n");
                for(Command command : themis.getCommandListener().getCommands()) {
                    sb.append(command.getCommandManual()).append("\n");
                }
                String outString = sb.toString();
                while(outString.length() >= 2000) {
                    message.getTextChannel().sendMessage(outString.substring(0, 2000)).complete();
                    outString = outString.substring(1999);
                }
                message.getTextChannel().sendMessage(sb.toString()).complete();
            }
        }
    }
    
    @Override
    public String getDiscription() {
        return "Shows help/usage information for various commands.";
    }
    
    @Override
    public String getHumanReadablePattern() {
        return "command(0,1)";
    }

    @Override
    public String getExampleUsage() {
        return "shutdown";
    }
    
}
