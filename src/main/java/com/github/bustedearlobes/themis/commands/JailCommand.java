package com.github.bustedearlobes.themis.commands;

import java.util.regex.Matcher;

import com.github.bustedearlobes.themis.Themis;
import com.github.bustedearlobes.themis.taskmanager.JailTask;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

public class JailCommand extends Command {
    private static final String REGEX = "^jail @\\w+$";
    
    public JailCommand() {
        super("jail", REGEX);
    }

    @Override
    public void onCall(Matcher fullCommand, Message message, JDA jda, Themis themis) {
        if(message.getMember().isOwner()) {
            if(message.getMentionedUsers().size() == 1) {
                User target = message.getMentionedUsers().get(0);
                Member targetMember = message.getGuild().getMember(target);
                JailTask jailTask = new JailTask(targetMember, message.getTextChannel());
                themis.getTaskManager().addTask(jailTask);
            }
        }
    }
    
    @Override
    public String getDiscription() {
        return "Moves a player to a channel to play a corrective behaviour training clip.";
    }
    
    @Override
    public String getHumanReadablePattern() {
        return "@user(1)";
    }

    @Override
    public String getExampleUsage() {
        return "@naughty#4135";
    }
    
}
