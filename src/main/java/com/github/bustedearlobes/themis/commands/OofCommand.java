package com.github.bustedearlobes.themis.commands;

import java.io.File;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bustedearlobes.themis.Themis;
import com.github.bustedearlobes.themis.music.GuildMusicManager;
import com.github.bustedearlobes.themis.taskmanager.OofShutdownTask;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor;
import com.sedmelluq.discord.lavaplayer.container.wav.WavContainerProbe;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.managers.AudioManager;

public class OofCommand extends Command {
    private static final Logger LOG = LoggerFactory.getLogger(OofCommand.class);

    public OofCommand() {
        super("oof", "oof", false);
    }
    
    @Override
    protected void onCall(Matcher fullCommand, Message message, JDA jda, Themis themis) {
        
        
        Guild guild = message.getGuild();
        AudioManager audioManager = guild.getAudioManager();
        GuildMusicManager musicManager = themis.getGlobalMusicManager().getGuildMusicManager(guild);
        if(!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            if(message.getMember().getVoiceState().inVoiceChannel()) {
                audioManager.openAudioConnection(message.getMember().getVoiceState().getChannel());
                LOG.info("Starting oof music command in guild {}", guild.getName());
            } else {
                message.getChannel().sendMessage("You are not in a voice channel").complete();
            }
        } else {
            if(musicManager.getAudioPlayer().isPaused()) {
                musicManager.getAudioPlayer().setPaused(false);
            }
        }
        
        musicManager.getTrackScheduler().clear();
        musicManager.getAudioPlayer().stopTrack();
        LOG.info(new File("roblox.wav").getAbsolutePath());
        AudioTrack track = new LocalAudioTrack(
                new AudioTrackInfo("Oof",
                "Oof",
                1000,
                new File("roblox.wav").getAbsolutePath(),
                false,
                new File("roblox.wav").getAbsolutePath()), new MediaContainerDescriptor(new WavContainerProbe(), ""), new LocalAudioSourceManager());
        
        musicManager.getTrackScheduler().queue(track);
        
        themis.getTaskManager().addTask(new OofShutdownTask(guild.getId()));

    }

}
