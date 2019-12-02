package com.github.bustedearlobes.themis.music;

import java.util.ArrayList;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.DefaultSoundCloudDataReader;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.DefaultSoundCloudFormatHandler;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.DefaultSoundCloudHtmlDataLoader;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.DefaultSoundCloudPlaylistLoader;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudDataReader;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudFormatHandler;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudHtmlDataLoader;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;

import net.dv8tion.jda.api.entities.Guild;

public class GlobalMusicManager {
	private List<GuildMusicManager> audioManagers = new ArrayList<>();
	private AudioPlayerManager playerManager;

	public GlobalMusicManager() {
		playerManager = new DefaultAudioPlayerManager();
		playerManager.registerSourceManager(new YoutubeAudioSourceManager());
		
		SoundCloudHtmlDataLoader defaultHtmlLoader = new DefaultSoundCloudHtmlDataLoader();
		SoundCloudFormatHandler defaultFormatHandler = new DefaultSoundCloudFormatHandler();
		SoundCloudDataReader defaultDataReader = new DefaultSoundCloudDataReader();
		
		playerManager.registerSourceManager(new SoundCloudAudioSourceManager(
				true,
				defaultDataReader,
				defaultHtmlLoader,
				defaultFormatHandler,
				new DefaultSoundCloudPlaylistLoader(defaultHtmlLoader,
						defaultDataReader, defaultFormatHandler)));
		playerManager.registerSourceManager(new BandcampAudioSourceManager());
		playerManager.registerSourceManager(new HttpAudioSourceManager());
		playerManager.setFrameBufferDuration(50000);

		AudioSourceManagers.registerLocalSource(playerManager);
		AudioSourceManagers.registerRemoteSources(playerManager);
	}

	public GuildMusicManager getGuildMusicManager(Guild guild) {
		for (GuildMusicManager manager : audioManagers) {
			if (manager.getGuild() == guild) {
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
