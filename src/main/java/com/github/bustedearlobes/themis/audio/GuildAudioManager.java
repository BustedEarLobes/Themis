package com.github.bustedearlobes.themis.audio;

public class GuildAudioManager {
    private final String guildId;
    
    public GuildAudioManager(String guildId) {
        this.guildId = guildId;
    }
    
    public String getGuildId() {
        return guildId;
    }
}
