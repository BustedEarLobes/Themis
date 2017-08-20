package com.github.bustedearlobes.themis.commands;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import com.github.bustedearlobes.themis.Themis;
import com.github.bustedearlobes.themis.taskmanager.MuteToggleTask;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class UnmuteCommand extends Command {
    private static final String REGEX = "^(unmute)( @\\w+)+( #\\w+){0,1}$";
    
    
    public UnmuteCommand() {
        super("unmute", REGEX);
    }

    @Override
    public void onCall(Matcher parsedCommand, Message message, JDA jda, Themis themis) {
        if(message.getMentionedUsers().size() > 0) {
            TextChannel channel = message.getTextChannel();
            if(message.getMentionedChannels().size() > 0) {
                channel = message.getMentionedChannels().get(0);
            }
            MuteToggleTask unmuteTask = new MuteToggleTask(
                    message.getMentionedUsers(),
                    channel,
                    message.getTextChannel(),
                    false,
                    0,
                    TimeUnit.SECONDS);
            themis.getTaskManager().addTask(unmuteTask);
        }
    }
    
    @Override
    public String getDiscription() {
        return "Unmute user(s). If channel is not given, defaults to channel in which this "
                + "command was sent.";
    }

    @Override
    public String getHumanReadablePattern() {
        return "@user(1+) #channel(0,1)";
    }

    @Override
    public String getExampleUsage() {
        return "@innocent#1953 #general";
    }
}
