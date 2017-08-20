package com.github.bustedearlobes.themis.commands;

import java.util.HashMap;
import java.util.Map;

import com.github.bustedearlobes.themis.Themis;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {
    private Map<String, Command> commands = new HashMap<>();
    private Themis themis;
    
    public CommandListener(Themis themis) {
        this.themis = themis;
    }
    
    public void register(String commandBase, Command command) {
        commands.put(commandBase, command);
    }
    
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if(event.getMember().isOwner()) {
            String messageContent = event.getMessage().getContent();
            if(messageContent.startsWith("!")) {
                messageContent = messageContent.replaceFirst("!", "");
                String[] fullCommand = messageContent.split(" ");
                Command command = commands.get(fullCommand[0]);
                if(command != null) {
                    command.onCall(fullCommand, event.getMessage(), event.getJDA(), themis);
                }
            }
        }
    }

}
