package com.github.bustedearlobes.themis.taskmanager;

import net.dv8tion.jda.core.JDA;

public abstract class SingleTask extends ScheduledTask {
    private static final long serialVersionUID = 1L;

    public SingleTask(JDA jda) {
        super(0, 0, 0, jda);
    }
}
