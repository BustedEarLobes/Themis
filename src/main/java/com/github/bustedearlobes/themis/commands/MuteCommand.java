package com.github.bustedearlobes.themis.commands;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import com.github.bustedearlobes.themis.Themis;
import com.github.bustedearlobes.themis.taskmanager.MuteMemberTask;
import com.github.bustedearlobes.themis.taskmanager.UnmuteMemberTask;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;

public class MuteCommand extends Command {
    private static final String REGEX = "^(mute)( @[A-z0-9]+)+( (\\d+)(s|m|h|d)){0,1}$";
    
    
    public MuteCommand() {
        super(REGEX);
    }

    @Override
    public void onCall(Matcher parsedCommand, Message message, JDA jda, Themis themis) {
        if(message.getMentionedUsers().size() > 0) {
            MuteMemberTask muteTask = new MuteMemberTask(message.getMentionedUsers(), message.getTextChannel());
            themis.getTaskManager().addTaskToScheduler(muteTask);
            if(parsedCommand.group(3) != null) {
                TimeUnit timeUnit = parseTimeUnit(parsedCommand.group(5));
                long time = Integer.parseInt(parsedCommand.group(4));
                UnmuteMemberTask unmuteTask = new UnmuteMemberTask(
                        message.getMentionedUsers(),
                        message.getTextChannel(),
                        time,
                        timeUnit);
                themis.getTaskManager().addTaskToScheduler(unmuteTask);
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

}
