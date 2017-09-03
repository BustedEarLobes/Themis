package com.github.bustedearlobes.themis.taskmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
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
    
    private List<String> targetUserIds = new ArrayList<String>();

    private String targetGuildId;
    private String targetTextChannelId;
    private String targetLogChannelId;
    private boolean shouldMute;
    private TimeUnit timeUnit = null;
    private long time = 0;
    
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
        this.shouldMute = mute;
    }
    
    /**
     * This constructor is used to mute a player for a certain amount of time.
     * @param targets
     * @param textChannel
     * @param logChannel
     * @param mute
     * @param time
     * @param timeUnit
     */
    public MuteToggleTask(List<User> targets,
            TextChannel textChannel,
            TextChannel logChannel,
            boolean mute,
            long time,
            TimeUnit timeUnit) {
        super(0, 0, TimeUnit.MICROSECONDS, 0);
        for(User user : targets) {
            targetUserIds.add(user.getId());
        }
        this.targetGuildId = textChannel.getGuild().getId();
        this.targetTextChannelId = textChannel.getId();
        this.targetLogChannelId = logChannel.getId();
        this.shouldMute = mute;
        this.time = time;
        timeUnit = this.timeUnit;
    }

    @Override
    protected void runTask() {
        toggleMute(shouldMute);
        if(timeUnit != null) {
            try {
                timeUnit.sleep(time);
            } catch(InterruptedException e) {
                LOG.log(Level.SEVERE, "Interrupted sleep", e);
            }
        }
    }
    
    private void toggleMute(boolean mute) {
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
    
    @Override
    protected void cleanUpJDAChanges() {
        if(timeUnit != null) {
            toggleMute(!shouldMute);
        }
    }

}
