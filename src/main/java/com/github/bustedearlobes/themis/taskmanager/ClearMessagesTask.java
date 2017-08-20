
package com.github.bustedearlobes.themis.taskmanager;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class ClearMessagesTask extends InstantTask {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger("Themis");
    
    private String guildId;
    private String targetUserId;
    private String targetChannelId;
    private String loggingChannelId;
    private int numberOfMessages;
    
    public ClearMessagesTask(User user, TextChannel targetChannel, int messages, TextChannel loggingChannel) {
        targetUserId = user.getId();
        guildId = targetChannel.getGuild().getId();
        targetChannelId = targetChannel.getId();
        loggingChannelId = loggingChannel.getId();
        numberOfMessages = messages;
    }

    @Override
    protected void runTask() {
        Guild guild = getGuildById(guildId);
        TextChannel targetChannel = getTextChannelById(targetChannelId, guild);
        List<Message> messages = targetChannel.getIterableHistory().stream()
            .limit(numberOfMessages)
            .filter(m -> m.getAuthor().getId().equals(targetUserId))
            .collect(Collectors.toList());
        if(messages.size() < 2) {
            if(messages.size() > 0) {
                messages.get(0).delete().complete();
            }
        } else {
            targetChannel.deleteMessages(messages).complete();
        }
        String messageString = messages.size() == 1 ? "message" : "messages";
        String userName = getUserById(targetUserId).getName();
        LOG.info("Cleared "
                + messages.size()
                + " "
                + messageString
                + " from "
                + userName
                + "@"
                + targetChannel.getName()
                + " in guild "
                + guild.getName());
        TextChannel loggingChannel = getTextChannelById(loggingChannelId, guild);
        loggingChannel.sendMessage("Cleared "
                + messages.size()
                + " "
                + messageString
                + " from "
                + userName).complete();
        
    }

}
