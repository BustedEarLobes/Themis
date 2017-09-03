package com.github.bustedearlobes.themis.commands;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import com.github.bustedearlobes.themis.Themis;
import com.github.bustedearlobes.themis.taskmanager.MuteToggleTask;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class MuteCommand extends Command {
    private static final String REGEX = "^(mute)( @\\w+)+( #\\w+){0,1}( (\\d+)(s|m|h|d)){0,1}$";
    
    
    public MuteCommand() {
        super("mute", REGEX);
    }

    @Override
    public void onCall(Matcher parsedCommand, Message message, JDA jda, Themis themis) {
        if(message.getMentionedUsers().size() > 0) {
            TextChannel channel = message.getTextChannel();
            if(message.getMentionedChannels().size() > 0) {
                channel = message.getMentionedChannels().get(0);
            }
            if(parsedCommand.group(4) != null) {
                TimeUnit timeUnit = parseTimeUnit(parsedCommand.group(6));
                long time = Integer.parseInt(parsedCommand.group(5));
                MuteToggleTask unmuteTask = new MuteToggleTask(
                        message.getMentionedUsers(),
                        channel,
                        message.getTextChannel(),
                        true,
                        time,
                        timeUnit);
                themis.getTaskManager().addTask(unmuteTask);
            } else {
                MuteToggleTask muteTask = new MuteToggleTask(message.getMentionedUsers(),
                        channel,
                        message.getTextChannel(),
                        true);
                themis.getTaskManager().addTask(muteTask);
            }
        }
    }
    
    private TimeUnit parseTimeUnit(String stringTimeUnit) {
        TimeUnit timeUnit;
        switch(stringTimeUnit) {
        case "s":
            timeUnit = TimeUnit.SECONDS;
            break;
        case "m":
            timeUnit = TimeUnit.MINUTES;
            break;
        case "h":
            timeUnit = TimeUnit.HOURS;
            break;
        case "d":
            timeUnit = TimeUnit.DAYS;
            break;
        default:
            timeUnit = TimeUnit.SECONDS;
        }
        return timeUnit;
    }

    @Override
    public String getDiscription() {
        return "Mutes user(s). If channel is not given, defaults to channel in which this "
                + "command was sent. If the time is not given, defaults to infinity.";
    }

    @Override
    public String getHumanReadablePattern() {
        return "@user(1+) #channel(0,1) time(s|m|h|d)";
    }

    @Override
    public String getExampleUsage() {
        return "@victim#1953 #general 5m";
    }

}
