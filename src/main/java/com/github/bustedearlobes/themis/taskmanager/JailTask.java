package com.github.bustedearlobes.themis.taskmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class JailTask extends InstantTask {
    private static final long serialVersionUID = 2L;
    private static final Logger LOG = Logger.getLogger("Themis");
    
    private String targetUserId;
    private String guildId;
    private String loggingChannelId;
    
    private transient String jailId;
    private transient Map<String, List<Boolean>> oldPerms;
    
    public JailTask(Member targetMember, TextChannel loggingChannel) {
        targetUserId = targetMember.getUser().getId();
        guildId = targetMember.getGuild().getId();
        loggingChannelId = loggingChannel.getId();
    }

    @Override
    protected void runTask() {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioPlayer player = playerManager.createPlayer();
        YoutubeAudioSourceManager asm = new YoutubeAudioSourceManager();
        AudioTrack track = asm.buildTrackObject("PSEYXWmEse8", 
                "The 100 Greatest Movie Insults of All Time",
                "hh1edits",
                false,
                598000);
        playerManager.loadItem("Insults", new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
            }
            
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
            }
            
            @Override
            public void noMatches() {
            }
            
            @Override
            public void loadFailed(FriendlyException exception) {
            }
        });        
        
        AudioPlayerSendHandler handler = new AudioPlayerSendHandler(player);
        
        Guild guild = getGuildById(guildId);
        Member member = getMemberById(targetUserId, guild);
        
        if(guild.getVoiceChannelsByName("Jail", false).isEmpty()) {
            guild.getController()
                .createVoiceChannel("Jail")
                .complete()
                .createPermissionOverride(member)
                .complete();
        }
        VoiceChannel jailChannel = (VoiceChannel) guild.getVoiceChannelsByName("Jail", false).get(0);
        
        jailId = jailChannel.getId();
        oldPerms = new HashMap<>();
        for(VoiceChannel vc : guild.getVoiceChannels()) {
            if(vc != jailChannel) {
                List<Boolean> perms = new ArrayList<>();
                if(vc.getPermissionOverride(member) == null) {
                    vc.createPermissionOverride(member).complete();
                }
                boolean allow = false;
                for(Permission p : vc.getPermissionOverride(member).getAllowed()) {
                    if(p.equals(Permission.VOICE_CONNECT)) {
                        allow = true;
                    }
                }
                perms.add(allow);
                
                boolean inherit = false;
                for(Permission p : vc.getPermissionOverride(member).getInherit()) {
                    if(p.equals(Permission.VOICE_CONNECT)) {
                        inherit = true;
                    }
                }
                perms.add(inherit);

                boolean deny = false;
                for(Permission p : vc.getPermissionOverride(member).getDenied()) {
                    if(p.equals(Permission.VOICE_CONNECT)) {
                        deny = true;
                    }
                }
                perms.add(deny);
                oldPerms.put(vc.getId(), perms);
                vc.getPermissionOverride(member).getManager().deny(Permission.VOICE_CONNECT).complete();
            }
        }
        
        player.playTrack(track);
        guild.getAudioManager().setSendingHandler(handler);
        guild.getAudioManager().openAudioConnection(jailChannel);
        guild.getController().moveVoiceMember(member, jailChannel).complete();
       
        TextChannel loggingChannel = getTextChannelById(loggingChannelId, guild);
        loggingChannel.sendMessage("Taking " + member.getEffectiveName()+ " to jail...").complete();
        
        long stopTime = System.currentTimeMillis() + player.getPlayingTrack().getDuration();
        try {
            while(System.currentTimeMillis() < stopTime) {
                if(member.getVoiceState().getAudioChannel() == null
                        || member.getVoiceState().getAudioChannel().getIdLong() != jailChannel.getIdLong()
                        || member.getVoiceState().isDeafened()) {
                    if(!player.isPaused()) {
                        player.setPaused(true);
                        PrivateChannel pm = member.getUser().openPrivateChannel().complete();
                        pm.sendMessage("You cannot mute/escape your rehabilitation. Pausing audio...").complete();
                    }
                    stopTime += 500;
                } else {
                    if(player.isPaused()) {
                        player.setPaused(false);
                    }
                }
            }
        } finally {
            shutdown();
        }
        
    }
    
    @Override
    public void shutdown() {
        if(oldPerms != null) {
            Member member = getMemberById(targetUserId, getGuildById(guildId));
            for(VoiceChannel vc : getGuildById(guildId).getVoiceChannels()) {
                if(vc.getPermissionOverride(member) != null) {
                    List<Boolean> perms = oldPerms.get(vc.getId());
                    if(perms != null) {
                        PermissionOverride po = vc.getPermissionOverride(member);
                        if(perms.get(0)) {
                            po.getManager().grant(Permission.VOICE_CONNECT).complete();
                        }
                        if(perms.get(1)) {
                            po.getManager().clear(Permission.VOICE_CONNECT).complete();
                        }
                        if(perms.get(2)) {
                            po.getManager().deny(Permission.VOICE_CONNECT).complete();
                        }
                    }
                }
            }
        }
        if(jailId != null && getGuildById(guildId).getVoiceChannelById(jailId) != null) {
            for(VoiceChannel vc : getGuildById(guildId).getVoiceChannels()) {
                for(Member m : getGuildById(guildId).getVoiceChannelById(jailId).getMembers()) {
                    getGuildById(guildId).getController().moveVoiceMember(m, vc).complete();
                }
                break;
            }

            getGuildById(guildId).getVoiceChannelById(jailId).delete().complete();
        }
        LOG.info("JailTask shutdown succesfully");
    }
    
    class AudioPlayerSendHandler implements AudioSendHandler {
        private final AudioPlayer audioPlayer;
        private AudioFrame lastFrame;

        public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
          this.audioPlayer = audioPlayer;
        }

        @Override
        public boolean canProvide() {
          lastFrame = audioPlayer.provide();
          return lastFrame != null;
        }

        @Override
        public byte[] provide20MsAudio() {
          return lastFrame.data;
        }

        @Override
        public boolean isOpus() {
          return true;
        }
      }

}

