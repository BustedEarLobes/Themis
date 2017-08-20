package com.github.bustedearlobes.themis.taskmanager;

public abstract class InstantTask extends ScheduledTask {
    private static final long serialVersionUID = 1L;

    public InstantTask() {
        super(0, 0, 0);
    }
}
