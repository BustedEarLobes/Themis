package com.github.bustedearlobes.themis.commands;

import net.dv8tion.jda.core.entities.Message;

public interface Command {
    public void onCall(String[] fullCommand, Message message);
}
