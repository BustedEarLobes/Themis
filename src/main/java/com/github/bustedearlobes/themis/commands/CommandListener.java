package com.github.bustedearlobes.themis.commands;

import java.util.ArrayList;
import java.util.List;

import com.github.bustedearlobes.themis.Themis;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {
    private static final String COMMAND_BASE = "!";
    
    private List<Command> commands = new ArrayList<>();
    private Themis themis;
    
    public CommandListener(Themis themis) {
        this.themis = themis;
    }
    
    public void register(Command command) {
        commands.add(command);
    }
    
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if(event.getMember().isOwner()) {
            String messageContent = event.getMessage().getContent();
            if(messageContent.trim().startsWith(COMMAND_BASE)) {
                messageContent = messageContent.trim().replaceFirst(COMMAND_BASE, "");
                
                for(Command command: commands) {
                    if(command.validateCall(messageContent)) {
                        command.onCall(command.parseCommand(messageContent),
                                event.getMessage(),
                                event.getJDA(),
                                themis);
                    }
                }
            }
        }
    }

}
