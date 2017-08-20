package com.github.bustedearlobes.themis.taskmanager;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class UnmuteMemberTask extends ScheduledTask {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger("Themis");
    
    List<String> targetUserIds;
    String targetGuildId;
    String targetTextChannelId;
    
    public UnmuteMemberTask(List<User> targets,
            TextChannel textChannel,
            long delay,
            TimeUnit timeUnit) {
        super(delay, 0, timeUnit, 0);
        for(User user : targets) {
            targetUserIds.add(user.getId());
        }
        this.targetGuildId = textChannel.getGuild().getId();
        this.targetTextChannelId = textChannel.getId();
    }

    @Override
    protected void runTask() {
        Guild guild = getGuildById(targetGuildId);
        TextChannel channel = getTextChannelById(targetTextChannelId, guild);
        String[] userNames = new String[targetUserIds.size()];
        
        for(int i = 0; i < targetUserIds.size(); i ++) {
            String userId = targetUserIds.get(i);
            Member member = getMemberById(userId, guild);
            channel.createPermissionOverride(member).setAllow(Permission.MESSAGE_WRITE).complete();
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
        
        LOG.info(String.format("Unmuted %s on text channel %s in guild %s",
                userNamesFormated,
                channel.getName(),
                guild.getName()));
        channel.sendMessage("Unmuted " + userNamesFormated);
    }

}
