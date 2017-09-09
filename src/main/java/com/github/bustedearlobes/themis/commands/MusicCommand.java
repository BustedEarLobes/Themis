package com.github.bustedearlobes.themis.commands;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import com.github.bustedearlobes.themis.Themis;
import com.github.bustedearlobes.themis.music.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.managers.AudioManager;

public class MusicCommand extends Command {
    private static final Logger LOG = Logger.getLogger("Themis");
    
    private static final String REGEX = "^music (play|stop|list|skip( (\\d+))?|clear|queue( (.*)))$";
    private static final int CAPTURE_GROUP_QUEUE = 5;
    private static final int CAPTURE_GROUP_SKIP = 3;
    
    public MusicCommand() {
        super("music", REGEX);
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
        default:
            throw new RuntimeException("Unexpected music subcommand");
        }
    }

    private void play(Message message, JDA jda, Themis themis) {
        Guild guild = message.getGuild();
        AudioManager audioManager = guild.getAudioManager();
        if(!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            if(message.getMember().getVoiceState().inVoiceChannel()) {
                audioManager.openAudioConnection(message.getMember().getVoiceState().getChannel());
                message.getTextChannel().sendMessage("Starting music bot").complete();
                LOG.info("Starting music bot in guild " + guild.getName());
            } else {
                message.getChannel().sendMessage("You are not in a voice channel").complete();
            }
        } else {
            message.getChannel().sendMessage("Themis is already in voice channel!").complete();
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
            LOG.info("Stopping music bot in guild " + guild.getName());
        } else {
            message.getChannel().sendMessage("Themis is not connected to a voice channel!").complete();
        }        
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
        LOG.info("Skipping music track in guild " + guild.getName());
    }
    
    private void clear(Message message, JDA jda, Themis themis) {
        Guild guild = message.getGuild();
        GuildMusicManager musicManager = themis.getGlobalMusicManager().getGuildMusicManager(guild);
        musicManager.getTrackScheduler().clear();
        musicManager.getAudioPlayer().stopTrack();
        message.getTextChannel().sendMessage("Cleared the song queue").complete();
        LOG.info("Cleared the song queue in quild " + guild.getName());
    }
    
    private void queue(Matcher command, Message message, JDA jda, Themis themis) {
        Guild guild = message.getGuild();
        GuildMusicManager musicManager = themis.getGlobalMusicManager().getGuildMusicManager(guild);
        AudioPlayerManager playerManager = themis.getGlobalMusicManager().getAudioPlayerManager();
        TextChannel logChannel = message.getTextChannel();
        playerManager.loadItemOrdered(musicManager, command.group(CAPTURE_GROUP_QUEUE), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                logChannel.sendMessage("Added " + track.getIdentifier() + " to the queue").complete();
                musicManager.getTrackScheduler().queue(track);
                LOG.info("Added " + track.getIdentifier() + " to the queue in guild " + guild.getName());
            }
            
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                logChannel.sendMessage("Added " + playlist.getName() + " playlist with "
                        +playlist.getTracks().size() + " songs to the queue").complete();
                for(AudioTrack track : playlist.getTracks()) {
                    musicManager.getTrackScheduler().queue(track);
                }
                LOG.info("Added playlist " + playlist.getName()
                        + " with " + playlist.getTracks().size()
                        + " songs to the queue in guild "
                        + guild.getName());
            }
            
            @Override
            public void noMatches() {
                logChannel.sendMessage("No song matched " + command.group(CAPTURE_GROUP_QUEUE)).complete();
            }
            
            @Override
            public void loadFailed(FriendlyException exception) {
                logChannel.sendMessage("Could not load song at " + command.group(CAPTURE_GROUP_QUEUE)).complete();
                LOG.log(Level.WARNING, "Failed to load song in guild " + guild.getName(), exception);
            }
        });
        
    }

    @Override
    public String getDiscription() {
        return "Command used to configure the music bot. \n"
             + "  **• play:** Starts up the music bot on the voice channel the user is currently in.\n"
             + "  **• stop:** Stops the music bot. Leaves the voice channel.\n"
             + "  **• list:** Lists the current songs in the queue.\n"
             + "  **• skip:** Skips to the next song in the queue.\n"
             + "  **• clear:** Clears the queue of all songs.\n"
             + "  **• queue:** Queues a song given the URL.";
                
    }
    
    @Override
    public String getHumanReadablePattern() {
        return "play | stop | list | skip | clear | queue URL";
    }

    @Override
    public String getExampleUsage() {
        return "queue https://www.youtube.com/watch?v=kJQP7kiw5Fk";
    }
    
}
