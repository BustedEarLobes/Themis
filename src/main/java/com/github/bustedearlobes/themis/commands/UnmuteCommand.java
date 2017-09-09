package com.github.bustedearlobes.themis.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import com.github.bustedearlobes.themis.Themis;
import com.github.bustedearlobes.themis.taskmanager.MuteToggleTask;
import com.github.bustedearlobes.themis.taskmanager.ScheduledTask;
import com.github.bustedearlobes.themis.taskmanager.TaskManager;

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
                    false);
            purgeConflictingMuteTasks(unmuteTask, themis.getTaskManager());
            themis.getTaskManager().addTask(unmuteTask);
        }
    }
    
    private void purgeConflictingMuteTasks(MuteToggleTask muteTask, TaskManager taskManager) {
        for(ScheduledTask task : taskManager.getTasksByName(muteTask.getName())) {
            MuteToggleTask otherMuteTask = (MuteToggleTask) task;
            if(otherMuteTask != muteTask) {
                List<String> toRemove = new ArrayList<>();
                for(String otherTargetId : otherMuteTask.getTargetUsers()) {
                    for(String targetId : muteTask.getTargetUsers()) {
                        if(targetId.equals(otherTargetId)) {
                            toRemove.add(otherTargetId);
                        }
                    }
                }
                
                for(String remove : toRemove) {
                    otherMuteTask.removeTargetUser(remove);
                }
            }
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
