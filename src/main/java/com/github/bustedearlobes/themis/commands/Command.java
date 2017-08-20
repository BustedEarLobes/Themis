package com.github.bustedearlobes.themis.commands;

import com.github.bustedearlobes.themis.Themis;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;

public interface Command {
    public void onCall(String[] fullCommand, Message message, JDA jda, Themis themis);
}
