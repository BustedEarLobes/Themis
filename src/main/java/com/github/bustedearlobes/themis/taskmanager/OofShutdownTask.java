package com.github.bustedearlobes.themis.taskmanager;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bustedearlobes.themis.music.GuildMusicManager;

import net.dv8tion.jda.core.entities.Guild;

public class OofShutdownTask extends ScheduledTask {
    private static final Logger LOG = LoggerFactory.getLogger(OofShutdownTask.class);
    private String guildID;
    
    public OofShutdownTask(String guildID) {
        super(3200, 0, TimeUnit.MILLISECONDS, 0);
        this.guildID = guildID;
    }

    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return "Oof Shutdown Task";
    }

    @Override
    protected void runTask() {
        Guild guild = getGuildById(guildID);
        GuildMusicManager musicManager = getThemis().getGlobalMusicManager().getGuildMusicManager(guild);
        musicManager.getTrackScheduler().clear();
        musicManager.getAudioPlayer().stopTrack();
        guild.getAudioManager().closeAudioConnection();
        LOG.info("Closing oof audio", guild.getName());
    }

}
