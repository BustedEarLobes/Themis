package com.github.bustedearlobes.themis.music;

import java.util.ArrayList;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat.Codec;
import com.sedmelluq.discord.lavaplayer.natives.opus.OpusEncoder;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration.ResamplingQuality;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;

import net.dv8tion.jda.core.entities.Guild;

public class GlobalMusicManager {
    private List<GuildMusicManager> audioManagers = new ArrayList<>();
    private AudioPlayerManager playerManager;

    public GlobalMusicManager() {
        playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.registerSourceManager(new SoundCloudAudioSourceManager());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager());
        playerManager.setFrameBufferDuration(100);
        playerManager.getConfiguration().setResamplingQuality(ResamplingQuality.LOW);
        playerManager.getConfiguration().setOpusEncodingQuality(1);
        AudioSourceManagers.registerLocalSource(playerManager);
        AudioSourceManagers.registerRemoteSources(playerManager);
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        for(GuildMusicManager manager : audioManagers) {
            if(manager.getGuild() == guild) {
                return manager;
            }
        }
        GuildMusicManager newManager = new GuildMusicManager(guild, playerManager);
        guild.getAudioManager().setSendingHandler(newManager.getSendHandler());
        audioManagers.add(newManager);
        return newManager;
    }
    
    public AudioPlayerManager getAudioPlayerManager() {
        return playerManager;
    }

}
