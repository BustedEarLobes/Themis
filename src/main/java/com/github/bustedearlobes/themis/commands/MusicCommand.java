package com.github.bustedearlobes.themis.commands;

import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bustedearlobes.themis.Themis;
import com.github.bustedearlobes.themis.music.GuildMusicManager;
import com.github.bustedearlobes.themis.taskmanager.MusicInactivityTask;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public class MusicCommand extends Command {
    private static final Logger LOG = LoggerFactory.getLogger(MusicCommand.class);
    
    private static final String REGEX = "^music (play|stop|pause|list|skip( (\\d+))?|clear|queue (.*)|ytsearch (.*))$";
    private static final int CAPTURE_GROUP_QUEUE = 4;
    private static final int CAPTURE_GROUP_YTSEARCH = 5;
    private static final int CAPTURE_GROUP_SKIP = 3;
    
    public MusicCommand() {
        super("music", REGEX, false);
    }

    @Override
    public void onCall(Matcher fullCommand, Message message, JDA jda, Themis themis) {
        switch(fullCommand.group(1).split(" ")[0]) {
        case "play":
            play(message, jda, themis);
            break;
        case "stop":
            stop(message, jda, themis);
            break;            
        case "pause":
            pause(message, jda, themis);
            break;
        case "list":
            list(message, jda, themis);
            break;
        case "skip":
            skip(fullCommand, message, jda, themis);
            break;
        case "clear":
            clear(message, jda, themis);
            break;
        case "queue":
            queue(fullCommand, message, jda, themis);
            break;
        case "ytsearch":
            youTubeSearch(fullCommand, message, jda, themis);
            break;
        default:
            throw new RuntimeException("Unexpected music subcommand: " + fullCommand.group(1).split(" ")[0]);
        }
    }

    private void play(Message message, JDA jda, Themis themis) {
        Guild guild = message.getGuild();
        AudioManager audioManager = guild.getAudioManager();
        GuildMusicManager musicManager = themis.getGlobalMusicManager().getGuildMusicManager(guild);
        if(!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            if(message.getMember().getVoiceState().inVoiceChannel()) {
                audioManager.openAudioConnection(message.getMember().getVoiceState().getChannel());
                message.getTextChannel().sendMessage("Starting music bot").complete();
                LOG.info("Starting music bot in guild {}", guild.getName());
                themis.getTaskManager().addTask(new MusicInactivityTask(guild.getId()));
            } else {
                message.getChannel().sendMessage("You are not in a voice channel").complete();
            }
        } else {
            if(musicManager.getAudioPlayer().isPaused()) {
                musicManager.getAudioPlayer().setPaused(false);
                message.getChannel().sendMessage("Resumed playing").complete();
            }
        }
    }
    
    private void stop(Message message, JDA jda, Themis themis) {
        Guild guild = message.getGuild();
        GuildMusicManager musicManager = themis.getGlobalMusicManager().getGuildMusicManager(guild);
        musicManager.getTrackScheduler().clear();
        AudioManager audioManager = guild.getAudioManager();
        if(audioManager.isConnected()) {
            musicManager.getAudioPlayer().stopTrack();
            audioManager.closeAudioConnection();
            message.getTextChannel().sendMessage("Stopping music bot").complete();
            LOG.info("Stopping music bot in guild {}", guild.getName());
        } else {
            message.getChannel().sendMessage("Themis is not connected to a voice channel!").complete();
        }        
    }
    
    private void pause(Message message, JDA jda, Themis themis) {
        Guild guild = message.getGuild();
        GuildMusicManager musicManager = themis.getGlobalMusicManager().getGuildMusicManager(guild);
        musicManager.getAudioPlayer().setPaused(true);
        message.getChannel().sendMessage("Music paused. Play to resume").complete();
    }
        
    private void list(Message message, JDA jda, Themis themis) {
        Guild guild = message.getGuild();
        GuildMusicManager musicManager = themis.getGlobalMusicManager().getGuildMusicManager(guild);
        message.getTextChannel().sendMessage(musicManager.getTrackScheduler().toString()).complete();
    }
    
    private void skip(Matcher command, Message message, JDA jda, Themis themis) {
        Guild guild = message.getGuild();
        GuildMusicManager musicManager = themis.getGlobalMusicManager().getGuildMusicManager(guild);
        int numOfSkips = 1;
        if(command.group(CAPTURE_GROUP_SKIP) != null) {
            numOfSkips = Integer.parseInt(command.group(CAPTURE_GROUP_SKIP));
        }
        for(int i = 0; i < numOfSkips; i ++) {
            musicManager.getTrackScheduler().nextTrack();
        }
        String plural = numOfSkips > 1 ? "s" : "";
        message.getTextChannel().sendMessage("Skipping track" + plural).complete();
        LOG.info("Skipping music track in guild {}", guild.getName());
    }
    
    private void clear(Message message, JDA jda, Themis themis) {
        Guild guild = message.getGuild();
        GuildMusicManager musicManager = themis.getGlobalMusicManager().getGuildMusicManager(guild);
        musicManager.getTrackScheduler().clear();
        musicManager.getAudioPlayer().stopTrack();
        message.getTextChannel().sendMessage("Cleared the song queue").complete();
        LOG.info("Cleared the song queue in guild {}", guild.getName());
    }
    
    private void queue(Matcher command, Message message, JDA jda, Themis themis) {
        Guild guild = message.getGuild();
        GuildMusicManager musicManager = themis.getGlobalMusicManager().getGuildMusicManager(guild);
        AudioPlayerManager playerManager = themis.getGlobalMusicManager().getAudioPlayerManager();
        TextChannel logChannel = message.getTextChannel();
        playerManager.loadItemOrdered(musicManager, command.group(CAPTURE_GROUP_QUEUE), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.getTrackScheduler().queue(track);
                logChannel.sendMessage(message.getAuthor().getName()
                        + " added *" 
                        + track.getInfo().title
                        +"* by "
                        + track.getInfo().author
                        + " to the queue").complete();
                LOG.info("Added {} to the queue in guild {}", track.getIdentifier(), guild.getName());
            }
            
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for(AudioTrack track : playlist.getTracks()) {
                    musicManager.getTrackScheduler().queue(track);
                }
                logChannel.sendMessage(message.getAuthor().getName()
                        + " added "
                        + playlist.getName()
                        + " playlist with "
                        + playlist.getTracks().size() + " songs to the queue").complete();
                LOG.info("Added playlist {} with {} songs to the queue in guild {}", 
                        playlist.getName(), 
                        playlist.getTracks().size(), 
                        guild.getName());

            }
            
            @Override
            public void noMatches() {
                logChannel.sendMessage("No song matched " + command.group(CAPTURE_GROUP_QUEUE)).complete();
            }
            
            @Override
            public void loadFailed(FriendlyException exception) {
                logChannel.sendMessage("Could not load song at " + command.group(CAPTURE_GROUP_QUEUE)).complete();
                LOG.warn("Failed to load song in guild {}", guild.getName(), exception);
            }
        });
        
        message.delete().complete();
    }
    
    private void youTubeSearch(Matcher command, Message message, JDA jda, Themis themis) {
        Guild guild = message.getGuild();
        GuildMusicManager musicManager = themis.getGlobalMusicManager().getGuildMusicManager(guild);
        AudioPlayerManager playerManager = themis.getGlobalMusicManager().getAudioPlayerManager();
        TextChannel logChannel = message.getTextChannel();
        playerManager.loadItemOrdered(musicManager, "ytsearch: " + command.group(CAPTURE_GROUP_YTSEARCH), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.getTrackScheduler().queue(track);
                logChannel.sendMessage(message.getAuthor().getName()
                        + " added *" 
                        + track.getInfo().title
                        +"* by "
                        + track.getInfo().author
                        + " to the queue").complete();
                LOG.info("Added {} to the queue in guild {}", track.getIdentifier(), guild.getName());
            }
            
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                musicManager.getTrackScheduler().queue(playlist.getTracks().get(0));
                logChannel.sendMessage(message.getAuthor().getName()
                        + " added *" 
                        + playlist.getTracks().get(0).getInfo().title
                        +"* by "
                        + playlist.getTracks().get(0).getInfo().author
                        + " to the queue").complete();
                LOG.info("Added {} to the queue in guild {}",
                        playlist.getTracks().get(0).getIdentifier(),
                        guild.getName());
            }
            
            @Override
            public void noMatches() {
                logChannel.sendMessage("No song matched " + command.group(CAPTURE_GROUP_YTSEARCH)).complete();
            }
            
            @Override
            public void loadFailed(FriendlyException exception) {
                logChannel.sendMessage("Could not load song at " + command.group(CAPTURE_GROUP_YTSEARCH)).complete();
                LOG.warn("Failed to load song in guild {}", guild.getName(), exception);
            }
        });
        
        message.delete().complete();
    }

    @Override
    public String getDiscription() {
        return "Command used to configure the music bot. \n"
             + "  **• play:** Starts up the music bot on the voice channel the user is currently in. Resumes if paused.\n"
             + "  **• stop:** Stops the music bot. Leaves the voice channel.\n"
             + "  **• pause:** Pauses the music bot. Play to resume.\n"
             + "  **• list:** Lists the current songs in the queue.\n"
             + "  **• skip:** Skips to the next song in the queue. Can skip multiple tracks.\n"
             + "  **• clear:** Clears the queue of all songs.\n"
             + "  **• queue:** Queues a song given the URL.\n"
             + "  **• ytsearch:** Searches for a song on youtube given a query";          
    }
    
    @Override
    public String getHumanReadablePattern() {
        return "play | stop | pause | list | skip <number> | clear | queue URL | ytsearch <query>";
    }

    @Override
    public String getExampleUsage() {
        return "queue https://www.youtube.com/watch?v=kJQP7kiw5Fk";
    }
    
}
