package com.github.bustedearlobes.themis.messagelog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bustedearlobes.themis.Themis;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageLogger extends ListenerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(MessageLogger.class);

    private Themis themis;
    private TextChannel moderationChannel = null;
    private Path tempDir;
    
    public MessageLogger(Themis themis) throws IOException {
        this.themis = themis;
        this.tempDir = Files.createTempDirectory("themis-attachments");
    }

    public boolean getModerationChannel(GenericGuildMessageEvent event) {
        if(moderationChannel == null) {
            List<TextChannel> channels = event.getGuild().getTextChannelsByName(themis.getModerationChannelName(),
                    true);
            if(channels.size() > 0) {
                moderationChannel = channels.get(0);
                return true;
            }
            return false;
        }
        return true;
    }

    public File getUniqueFile(String extension) {
        String fileName = MessageFormat.format("{0}.{1}", UUID.randomUUID(), extension.trim());
        return tempDir.resolve(fileName).toFile();
    }
    
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if(!event.getAuthor().isBot()) {
            String messageContent = event.getMessage().getContentRaw();
            List<Attachment> attachments = event.getMessage().getAttachments();      
            if(getModerationChannel(event)) {
                Message replayMessage = new MessageBuilder()
                        .append("**User ")
                        .append(event.getAuthor().getAsTag())
                        .append(" posted:** ")
                        .append(messageContent)
                        .stripMentions(event.getGuild())
                        .build();
                
                moderationChannel.sendMessage(replayMessage).queue();
                if(attachments.size() > 0) {
                    for(Attachment attachment : attachments) {
                        try {
                            File f = getUniqueFile(FilenameUtils.getExtension(attachment.getFileName()));
                            if(attachment.downloadToFile().complete(f)) {
                                LOG.info("Created new attachment for replay at {}", f.getAbsolutePath());
                                moderationChannel.sendFile(f).complete();
                                f.delete();
                            }
                        } catch(Throwable e) {
                            LOG.error("An error occured while replaying file attachment", e);
                        }
                    }
                }
                
            }
        }
    }

    @Override
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        if(!event.getAuthor().isBot()) {
            String messageContent = event.getMessage().getContentRaw();
            List<Attachment> attachments = event.getMessage().getAttachments();
            if(getModerationChannel(event)) {
                Message replayMessage = new MessageBuilder()
                        .append("**User ")
                        .append(event.getAuthor().getAsTag())
                        .append(" edited:** ")
                        .append(messageContent)
                        .stripMentions(event.getGuild())
                        .build();
                
                moderationChannel.sendMessage(replayMessage).queue();
                
                if(attachments.size() > 0) {
                    for(Attachment attachment : attachments) {
                        try {
                            File f = getUniqueFile(FilenameUtils.getExtension(attachment.getFileName()));
                            if(attachment.downloadToFile().complete(f)) {
                                LOG.info("Created new attachment for replay at {}", f.getAbsolutePath());
                                moderationChannel.sendFile(f).complete();
                                f.delete();
                            }
                        } catch(Throwable e) {
                            LOG.error("An error occured while replaying file attachment", e);
                        }
                    }
                }
            }
        }
    }

}
