package com.github.bustedearlobes.themis.taskmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class MuteToggleTask extends ScheduledTask {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger("Themis");
    
    List<String> targetUserIds = new ArrayList<String>();

    String targetGuildId;
    String targetTextChannelId;
    String targetLogChannelId;
    boolean mute;
    
    public MuteToggleTask(List<User> targets,
            TextChannel textChannel,
            TextChannel logChannel,
            boolean mute) {
        super(0, 0, TimeUnit.MICROSECONDS, 0);
        for(User user : targets) {
            targetUserIds.add(user.getId());
        }
        this.targetGuildId = textChannel.getGuild().getId();
        this.targetTextChannelId = textChannel.getId();
        this.targetLogChannelId = logChannel.getId();
        this.mute = mute;
    }
    
    public MuteToggleTask(List<User> targets,
            TextChannel textChannel,
            TextChannel logChannel,
            boolean mute,
            long time,
            TimeUnit timeUnit) {
        super(time, 0, timeUnit, 0);
        for(User user : targets) {
            targetUserIds.add(user.getId());
        }
        this.targetGuildId = textChannel.getGuild().getId();
        this.targetTextChannelId = textChannel.getId();
        this.targetLogChannelId = logChannel.getId();
        this.mute = mute;
    }

    @Override
    protected void runTask() {
        Guild guild = getGuildById(targetGuildId);
        TextChannel channel = getTextChannelById(targetTextChannelId, guild);
        String[] userNames = new String[targetUserIds.size()];
        
        for(int i = 0; i < targetUserIds.size(); i ++) {
            String userId = targetUserIds.get(i);
            Member member = getMemberById(userId, guild);
            PermissionOverride po = channel.getPermissionOverride(member);
            if(po == null) {
                po = channel.createPermissionOverride(member).complete();
            }
            if(mute) {
                po.getManager().deny(Permission.MESSAGE_WRITE).complete();
            } else {
                po.getManager().clear(Permission.MESSAGE_WRITE).complete();
            }
            userNames[i] = member.getUser().getName();
        }
        
        String userNamesFormated = "";
        for(int i = 0; i < userNames.length; i ++) {
          if(i == 0) {
              userNamesFormated += userNames[i];
          } else if(i == userNames.length - 1) {
              userNamesFormated += ", and " + userNames[i];
          } else {
              userNamesFormated += ", " + userNames[i];
          }
        }
        String muteString = mute ? "Muted" : "Unmuted";
        LOG.info(String.format("%s %s on text channel %s in guild %s",
                muteString,
                userNamesFormated,
                channel.getName(),
                guild.getName()));
        TextChannel logChannel = getTextChannelById(targetLogChannelId, guild);
        logChannel.sendMessage(muteString + " " + userNamesFormated).complete();
    }

}
