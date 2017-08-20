package com.github.bustedearlobes.themis.commands;

import java.util.regex.Matcher;

import com.github.bustedearlobes.themis.Themis;
import com.github.bustedearlobes.themis.taskmanager.ClearMessagesTask;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class ClearCommand extends Command {
    private static final String REGEX = "^clear( @\\w+)+( #\\w+)+( (\\d+))*$";
    private static final int DEFAULT_CLEAR_NUMBER = 100;
    
    public ClearCommand() {
        super("clear", REGEX);
    }

    @Override
    public void onCall(Matcher fullCommand, Message message, JDA jda, Themis themis) {
        TextChannel targetChannel = message.getTextChannel();
        if(message.getMentionedChannels().size() > 0) {
           targetChannel = message.getMentionedChannels().get(0);
        }
        int numOfMessages = DEFAULT_CLEAR_NUMBER;
        if(fullCommand.group(3) != null) {
            numOfMessages = Integer.parseInt(fullCommand.group(4));
        }
        ClearMessagesTask cmt = new ClearMessagesTask(message.getMentionedUsers().get(0),
                targetChannel,
                numOfMessages,
                message.getTextChannel());
        themis.getTaskManager().addTaskToScheduler(cmt);
    }
    
    @Override
    public String getDiscription() {
        return "Clears messages from a particular user. If no channel is defined, "
                + "defaults to the channel the command was sent from. "
                + "If no amount is defined, defaults to clearing last "
                + DEFAULT_CLEAR_NUMBER + " messages.";
    }
    
    @Override
    public String getHumanReadablePattern() {
        return "@user(0,1) #channel(0,1) number(0,1)";
    }

    @Override
    public String getExampleUsage() {
        return "@naughty#6436 #general 100";
    }
    
}
