package com.github.bustedearlobes.themis.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.bustedearlobes.themis.Themis;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {
    private static final String COMMAND_BASE = "!";
    private static final Logger LOG = Logger.getLogger("Themis");
    
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
                        try {
                            command.onCall(command.parseCommand(messageContent),
                                    event.getMessage(),
                                    event.getJDA(),
                                    themis);
                        } catch(Exception e) {
                            LOG.log(Level.WARNING,
                                    "There was an error while executing a command.",
                                    e);
                        }
                    }
                }
            }
        }
    }

}
