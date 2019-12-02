
package com.github.bustedearlobes.themis.taskmanager;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class ClearMessagesTask extends InstantTask {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(ClearMessagesTask.class);
    
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
        int count = 0;
        TextChannel loggingChannel = getTextChannelById(loggingChannelId, guild);
        List<Message> messages = new ArrayList<Message>();
        for(Message m : targetChannel.getIterableHistory()) {
            if(m.getAuthor().getId().equals(targetUserId)) {
                messages.add(m);
                if(messages.size() >= numberOfMessages) {
                    break;
                }
            }
            if(count >= 1000) {
                loggingChannel.sendMessage("Clear limit reached after 1000 messages").complete();
                LOG.info("Clear task stopped after searching 1000 messages");
                break;
            }
        }
        if(messages.size() < 2) {
            if(messages.size() > 0) {
                messages.get(0).delete().complete();
            }
        } else {
            targetChannel.deleteMessages(messages).complete();
        }
        String messageString = messages.size() == 1 ? "message" : "messages";
        String userName = getUserById(targetUserId).getName();
        LOG.info("Cleared {} from {}@{} in guild {}",
                messages.size(),
                userName,
                targetChannel.getName(),
                guild.getName());
        loggingChannel.sendMessage("Cleared "
                + messages.size()
                + " "
                + messageString
                + " from "
                + userName).complete();
        
    }

    @Override
    public String getName() {
        return "ClearMessagesTask";
    }

}
