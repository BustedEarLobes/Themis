package com.github.bustedearlobes.themis.taskmanager;

import java.util.concurrent.TimeUnit;

public abstract class InstantTask extends ScheduledTask {
    private static final long serialVersionUID = 1L;

    public InstantTask() {
        super(0, 0, TimeUnit.MILLISECONDS, 0);
    }
}
