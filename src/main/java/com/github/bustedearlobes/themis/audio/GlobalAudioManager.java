package com.github.bustedearlobes.themis.audio;

import java.util.ArrayList;
import java.util.List;

import com.github.bustedearlobes.themis.Themis;

public class GlobalAudioManager {
    private List<GuildAudioManager> audioManagers = new ArrayList<>();;
    private Themis themis;
    
    public GlobalAudioManager(Themis themis) {
        this.themis = themis;
    }
    
    public GuildAudioManager getAudioManager(String guildId) {
        for(GuildAudioManager manager : audioManagers) {
            if(manager.getGuildId().equals(guildId)) {
                return manager;
            }
        }
        GuildAudioManager newManager = new GuildAudioManager(guildId);
        audioManagers.add(newManager);
        return newManager;
    }
    
}
