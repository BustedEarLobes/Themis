package com.github.bustedearlobes.themis.commands;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import com.github.bustedearlobes.themis.Themis;
import com.github.bustedearlobes.themis.taskmanager.UnmuteMemberTask;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;

public class UnmuteCommand extends Command {
    private static final String REGEX = "^(unmute)( @[A-z0-9]+)$";
    
    
    public UnmuteCommand() {
        super(REGEX);
    }

    @Override
    public void onCall(Matcher parsedCommand, Message message, JDA jda, Themis themis) {
        if(message.getMentionedUsers().size() > 0) {
            UnmuteMemberTask unmuteTask = new UnmuteMemberTask(
                    message.getMentionedUsers(),
                    message.getTextChannel(),
                    0,
                    TimeUnit.SECONDS);
            themis.getTaskManager().addTaskToScheduler(unmuteTask);
        }
    }
}
